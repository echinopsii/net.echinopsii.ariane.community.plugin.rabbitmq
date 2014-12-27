package net.echinopsii.ariane.community.plugin.rabbitmq.jsonparser.tools

import groovyx.net.http.HttpResponseException
import groovyx.net.http.RESTClient
import net.echinopsii.ariane.community.plugin.rabbitmq.jsonparser.serializable.BrokerFromRabbitREST
import org.apache.http.NoHttpResponseException
import org.apache.http.conn.HttpHostConnectException

class RabbitNodeToConnect {
    String name
    String url
    String user
    String password

    boolean statisticsDBNode
    String rabbitmqVersion
    String managementVersion
    String erlangVersion
    String erlangFullVersion
    Map<String, String>  listeningAddress = new HashMap<String, String>()
    Map<String, Integer> listeningPorts = new HashMap<String, Integer>()

    RabbitClusterToConnect cluster

    public final static int REST_CLI_NODE_OK          = 0;
    public final static int REST_CLI_NODE_SOME_ERROR  = -1;
    public final static int REST_CLI_NODE_URL_ERROR   = -2;
    public final static int REST_CLI_NODE_NO_RESPONSE = -3;
    public final static int REST_CLI_NODE_AUTH_ERROR  = 401;

    RESTClient restCli

    int connectionStatus
    String connectionProblemDescription

    boolean errorOnProvidedNodeName = false
    String  validNodeNameFromTarget = null

    boolean errorOnProvidedClusterName = false
    String  validClusterNameFromTarget = null

    public RabbitNodeToConnect(String name, String url, String user, String password) {
        this.name = name
        this.url  = url
        this.user = user
        this.password = password

        this.restCli = new RESTClient( this.url )
        this.restCli.auth.basic this.user, this.password;
    }

    String getName() {
        return name
    }

    RabbitNodeToConnect setName(String name) {
        this.name = name
        return this
    }

    String getUrl() {
        return url
    }

    RabbitNodeToConnect setUrl(String url) {
        this.url = url
        return this
    }

    String getUser() {
        return user
    }

    RabbitNodeToConnect setUser(String user) {
        this.user = user
        return this
    }

    String getPassword() {
        return password
    }

    RabbitNodeToConnect setPassword(String password) {
        this.password = password
        return this
    }

    RabbitClusterToConnect getCluster() {
        return cluster
    }

    RabbitNodeToConnect setCluster(RabbitClusterToConnect cluster) {
        this.cluster = cluster
        return this
    }

    int checkRabbitRESTClient() {
        this.connectionStatus = REST_CLI_NODE_OK;
        try {
            def overview_req = this.restCli.get(path : BrokerFromRabbitREST.REST_RABBITMQ_BROKER_OVERVIEW_PATH)
            this.errorOnProvidedNodeName = !overview_req.data.node.equals(this.name)
            this.validNodeNameFromTarget = overview_req.data.node
            this.statisticsDBNode = overview_req.data.node.equals(overview_req.data.statistics_db_node)
            this.managementVersion = (String)overview_req.data.management_version
            this.rabbitmqVersion = (String)overview_req.data.rabbitmq_version
            this.erlangVersion = (String)overview_req.data.erlang_version
            this.erlangFullVersion = (String)overview_req.data.erlang_full_version
            ArrayList<HashMap<String, Object>> listeners = overview_req.data.listeners
            for (HashMap<String, Object> listener : listeners)
                if (listener.get(BrokerFromRabbitREST.JSON_RABBITMQ_BROKER_OVERVIEW_LISTENERS_NODE).equals(overview_req.data.node)) {
                    String protocol = (String)listener.get(BrokerFromRabbitREST.JSON_RABBITMQ_BROKER_OVERVIEW_LISTENERS_PROTOCOL)
                    String ip_addr  = (String)listener.get(BrokerFromRabbitREST.JSON_RABBITMQ_BROKER_OVERVIEW_LISTENERS_IPADDRESS)
                    int port        = new Integer((String)listener.get(BrokerFromRabbitREST.JSON_RABBITMQ_BROKER_OVERVIEW_LISTENERS_PORT))
                    this.listeningAddress.put(protocol, ip_addr)
                    this.listeningPorts.put(protocol, port)
                }
            if (this.cluster!=null) {
                this.errorOnProvidedClusterName = !overview_req.data.cluster_name.equals(this.cluster.getName())
                this.validClusterNameFromTarget = overview_req.data.cluster_name
            }
        } catch (UnknownHostException urlpb) {
            this.connectionStatus = REST_CLI_NODE_URL_ERROR;
            this.connectionProblemDescription = "PATH URL ERROR"
        } catch (NoHttpResponseException noHttpResponseException) {
            this.connectionStatus = REST_CLI_NODE_NO_RESPONSE;
            this.connectionProblemDescription = "NO HTTP RESPONSE"
        } catch (HttpHostConnectException httpHostConnectException) {
            this.connectionStatus = REST_CLI_NODE_NO_RESPONSE;
            this.connectionProblemDescription = "CONNECTION PROBLEM"
        } catch (HttpResponseException httpResponseException) {
            this.connectionStatus = httpResponseException.statusCode;
            if (this.connectionStatus == REST_CLI_NODE_AUTH_ERROR)
                this.connectionProblemDescription = "AUTHENTICATION ERROR"
            else
                this.connectionProblemDescription = "HTTP RESPONSE ERROR " + this.connectionStatus
        } catch (Exception e) {
            log.error(e.getMessage());
            this.connectionStatus = REST_CLI_NODE_SOME_ERROR;
            this.connectionProblemDescription = "SOME ERROR : " + e.getMessage()
        }
        return this.connectionStatus;
    }
}
