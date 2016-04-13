/**
 * RabbitMQ plugin jsonparser bundle
 * RabbitMQ plugin jsonparser RabbitMQ node where to connect
 * Copyright (C) 2014 Mathilde Ffrench
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package net.echinopsii.ariane.community.plugin.rabbitmq.jsonparser.tools

import groovyx.net.http.HttpResponseException
import groovyx.net.http.RESTClient
import net.echinopsii.ariane.community.plugin.rabbitmq.jsonparser.serializable.BrokerFromRabbitREST
import org.apache.http.NoHttpResponseException
import org.apache.http.conn.HttpHostConnectException
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class RabbitNodeToConnect {

    private static final Logger log = LoggerFactory.getLogger(RabbitNodeToConnect.class)

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
        log.debug("[init]new node to connect : " + name);
        this.name = name
        this.url  = url
        this.user = user
        this.password = password

        try {
            this.restCli = new RESTClient( this.url )
            this.restCli.auth.basic this.user, this.password;
        } catch (Exception e) {
            if (log.isDebugEnabled())
                e.printStackTrace();
            log.error("PB with node " + name + " (" + url + "):" + e.getMessage())
        }
        log.debug("[init done]new node to connect : " + name);
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

    RabbitClusterToConnect getCluster() {
        return cluster
    }

    RabbitNodeToConnect setCluster(RabbitClusterToConnect cluster) {
        this.cluster = cluster
        return this
    }

    int checkRabbitRESTClient() {
        this.connectionStatus = REST_CLI_NODE_OK;
        //noinspection GroovyUnusedCatchParameter
        try {
            def overview_req = this.get(BrokerFromRabbitREST.REST_RABBITMQ_BROKER_OVERVIEW_PATH)
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
            e.printStackTrace();
            log.error(e.getMessage())
            this.connectionStatus = REST_CLI_NODE_SOME_ERROR;
            this.connectionProblemDescription = "SOME ERROR : " + e.getMessage()
        }
        return this.connectionStatus;
    }

    public Object get(String path) {
        //if (this.restCli!=null) this.restCli.shutdown()
        //this.restCli = new RESTClient( this.url )
        //this.restCli.auth.basic this.user, this.password;
        this.restCli.get(path: path)
    }
}
