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
        RESTClient ret = null;
        for (RabbitNodeToConnect node : cluster.getNodes()) {
            RESTClient test = getRESTClientFromNode(node);
            int status = checkRabbitRESTClient(test);
            switch(status) {
                case REST_CLI_NODE_OK:
                    ret = test;
                    cluster.getErrors().remove(node.getName());
                    break;
                default:
                    cluster.getErrors().put(node.getName(), status)
                    break;
            }
        }
        return ret;
    }

    private static RESTClient getRESTClientFromNode(RabbitNodeToConnect node) {
        RESTClient rest = new RESTClient( node.getUrl() )
        rest.auth.basic node.getUser(), node.getPassword();
        return rest;
    }

    private static int checkRabbitRESTClient(RESTClient client) {
        int ret = REST_CLI_NODE_OK;
        try {
            client.get(path : '/api/overview')
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
            //e.printStackTrace();
            ret = REST_CLI_NODE_SOME_ERROR;
        }

        return ret;
    }
}
