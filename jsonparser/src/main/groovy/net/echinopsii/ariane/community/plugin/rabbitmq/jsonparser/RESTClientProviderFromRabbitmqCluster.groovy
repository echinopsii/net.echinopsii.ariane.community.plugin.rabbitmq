package net.echinopsii.ariane.community.plugin.rabbitmq.jsonparser

import groovyx.net.http.HttpResponseException
import groovyx.net.http.RESTClient
import net.echinopsii.ariane.community.plugin.rabbitmq.directory.model.RabbitmqCluster
import net.echinopsii.ariane.community.plugin.rabbitmq.directory.model.RabbitmqNode

class RESTClientProviderFromRabbitmqCluster {

    public final static int NODE_OK         = 0;
    public final static int NODE_URL_ERROR  = 1;
    public final static int NODE_AUTH_ERROR = 2;

    private static Map<String, Map<String, Integer>> clustersErrors = new HashMap<String, Map<String, Integer>>();

    static RESTClient getRESTClientFromCluster(RabbitmqCluster cluster) {
        RESTClient ret = null;
        for (RabbitmqNode node : cluster.getNodes()) {
            RESTClient test = getRESTClientFromNode(node);
            int status = checkRabbitRESTClient(test);
            switch(status) {
                case NODE_OK:
                    ret = test;
                    removeClusterError(cluster.getName(), node.getName());
                    break;
                case NODE_URL_ERROR:
                case NODE_AUTH_ERROR:
                    addClusterError(cluster.getName(), node.getName(), status);
                    break;
            }
        }
        return ret;
    }

    static Map<String, Integer> getClusterErrors(String clusterName) {
        return clustersErrors.get(clusterName);
    }

    private static removeClusterError(String clusterName, String nodeName) {
        if (clustersErrors.get(clusterName)!=null)
            clustersErrors.get(clusterName).remove(nodeName)
    }

    private static addClusterError(String clusterName, String nodeName, int error) {
        if (clustersErrors.get(clusterName)==null) {
            Map<String, Integer> clusterErrors = new HashMap<String, Integer>();
            clustersErrors.put(clusterName, clusterErrors);
        }

        clustersErrors.get(clusterName).put(nodeName,error);
    }

    private static RESTClient getRESTClientFromNode(RabbitmqNode node) {
        RESTClient rest = new RESTClient( node.getUrl() )
        rest.auth.basic node.getUser(), node.getPasswd();
        return rest;
    }

    private static int checkRabbitRESTClient(RESTClient client) {
        def test;
        int ret = NODE_OK;
        try {
            client.get(path : '/api/overview')
        } catch (UnknownHostException urlpb) {
            ret = NODE_URL_ERROR;
        } catch (HttpResponseException httpResponseException) {
            ret = NODE_AUTH_ERROR;
        }

        return ret;
    }
}
