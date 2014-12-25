package net.echinopsii.ariane.community.plugin.rabbitmq.jsonparser.tools

import groovyx.net.http.HttpResponseException
import groovyx.net.http.RESTClient
import org.apache.http.NoHttpResponseException
import org.apache.http.conn.HttpHostConnectException
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class RESTClientProvider {

    private static final Logger log = LoggerFactory.getLogger(RESTClientProvider.class);

    public final static int REST_CLI_NODE_OK          = 0;
    public final static int REST_CLI_NODE_SOME_ERROR  = -1;
    public final static int REST_CLI_NODE_URL_ERROR   = -2;
    public final static int REST_CLI_NODE_NO_RESPONSE = -3;
    public final static int REST_CLI_NODE_AUTH_ERROR  = 401;

    static RESTClient getRESTClientFromCluster(RabbitClusterToConnect cluster) {
        RESTClient ret = null
        boolean statisticNodeFound = false;
        RabbitNodeToConnect nodeOnRESTCli = null
        for (RabbitNodeToConnect node : cluster.getNodes()) {
            RESTClient test = getRESTClientFromNode(node)
            int status = checkRabbitRESTClient(test, node)
            switch(status) {
                case REST_CLI_NODE_OK:
                    if (!statisticNodeFound) {
                        ret = test
                        nodeOnRESTCli = node
                    }
                    if (node.isStatisticsDBNode)
                        statisticNodeFound=true
                    cluster.getErrors().remove(node.getName())
                    break;
                default:
                    cluster.getErrors().put(node.getName(), status)
                    break;
            }
        }
        cluster.setNodeOnRESTCli(nodeOnRESTCli)
        return ret;
    }

    static RESTClient getRESTClientFromNode(RabbitNodeToConnect node) {
        RESTClient rest = new RESTClient( node.getUrl() )
        rest.auth.basic node.getUser(), node.getPassword();
        return rest;
    }

    static int checkRabbitRESTClient(RESTClient client, RabbitNodeToConnect node) {
        int ret = REST_CLI_NODE_OK;
        try {
            def overview_req =client.get(path : '/api/overview')
            node.setIsStatisticsDBNode(overview_req.data.node.equals(overview_req.data.statistics_db_node))
            node.setManagementVersion((String)overview_req.data.management_version)
            node.setRabbitmqVersion((String)overview_req.data.rabbitmq_version)
            node.setErlangVersion((String)overview_req.data.erlang_version)
            node.setErlangFullVersion((String)overview_req.data.erlang_full_version)
            ArrayList<HashMap<String, Object>> listeners = overview_req.data.listeners
            for (HashMap<String, Object> listener : listeners)
                if (listener.get("node").equals(overview_req.data.node)) {
                    String protocol = (String)listener.get("protocol")
                    String ip_addr  = (String)listener.get("ip_address")
                    int port        = new Integer((String)listener.get("port"))
                    node.getListeningAddress().put(protocol, ip_addr)
                    node.getListeningPorts().put(protocol, port)
                    break;
                }
        } catch (UnknownHostException urlpb) {
            ret = REST_CLI_NODE_URL_ERROR;
        } catch (NoHttpResponseException noHttpResponseException) {
            ret = REST_CLI_NODE_NO_RESPONSE;
        } catch (HttpHostConnectException httpHostConnectException) {
            ret = REST_CLI_NODE_NO_RESPONSE;
        } catch (HttpResponseException httpResponseException) {
            ret = httpResponseException.statusCode;
        } catch (Exception e) {
            log.error(e.getMessage());
            ret = REST_CLI_NODE_SOME_ERROR;
        }
        return ret;
    }
}
