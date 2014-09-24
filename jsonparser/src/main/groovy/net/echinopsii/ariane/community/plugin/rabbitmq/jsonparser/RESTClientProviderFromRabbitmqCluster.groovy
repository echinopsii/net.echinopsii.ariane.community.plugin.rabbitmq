package net.echinopsii.ariane.community.plugin.rabbitmq.jsonparser

import groovyx.net.http.HttpResponseException
import groovyx.net.http.RESTClient
import net.echinopsii.ariane.community.plugin.rabbitmq.directory.model.RabbitmqCluster
import net.echinopsii.ariane.community.plugin.rabbitmq.directory.model.RabbitmqNode
import org.apache.http.NoHttpResponseException
import org.apache.http.conn.HttpHostConnectException
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class RESTClientProviderFromRabbitmqCluster {

    private static final Logger log = LoggerFactory.getLogger(RESTClientProviderFromRabbitmqCluster.class);

    public final static int NODE_OK          = 0;
    public final static int NODE_SOME_ERROR  = -1;
    public final static int NODE_URL_ERROR   = -2;
    public final static int NODE_NO_RESPONSE = -3;
    public final static int NODE_AUTH_ERROR  = 401;


    static RESTClient getRESTClientFromCluster(RabbitmqCluster cluster) {
        RESTClient ret = null;
        for (RabbitmqNode node : cluster.getNodes()) {
            RESTClient test = getRESTClientFromNode(node);
            int status = checkRabbitRESTClient(test);
            switch(status) {
                case NODE_OK:
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

    private static RESTClient getRESTClientFromNode(RabbitmqNode node) {
        RESTClient rest = new RESTClient( node.getUrl() )
        rest.auth.basic node.getUser(), node.getPasswd();
        return rest;
    }

    private static int checkRabbitRESTClient(RESTClient client) {
        int ret = NODE_OK;
        try {
            client.get(path : '/api/overview')
        } catch (UnknownHostException urlpb) {
            ret = NODE_URL_ERROR;
        } catch (NoHttpResponseException noHttpResponseException) {
            ret = NODE_NO_RESPONSE;
        } catch (HttpHostConnectException httpHostConnectException) {
            ret = NODE_NO_RESPONSE;
        } catch (HttpResponseException httpResponseException) {
            ret = httpResponseException.statusCode;
        } catch (Exception e) {
            log.error(e.getMessage());
            //e.printStackTrace();
            ret = NODE_SOME_ERROR;
        }

        return ret;
    }
}
