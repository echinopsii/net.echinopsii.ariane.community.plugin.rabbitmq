package net.echinopsii.ariane.community.plugin.rabbitmq.jsonparser

import groovyx.net.http.RESTClient
import net.echinopsii.ariane.community.plugin.rabbitmq.directory.model.RabbitmqCluster
import net.echinopsii.ariane.community.plugin.rabbitmq.directory.model.RabbitmqNode

class RabbitRESTClient {

    static RESTClient getRESTClientFromCluster(RabbitmqCluster cluster) {
        boolean    defined  = false;
        RESTClient rest     = null;
        for (RabbitmqNode node : cluster.getNodes()) {
            rest = getRESTClientFromNode(node);
            if (checkRabbitRESTClient(rest)) {
                defined = true;
                break;
            }
        }
        if (!defined)
            rest = null;
        return rest;
    }

    static RESTClient getRESTClientFromNode(RabbitmqNode node) {
        RESTClient rest = new RESTClient( node.getUrl() )
        rest.auth.basic node.getUser(), node.getPasswd();
        return rest;
    }

    static boolean checkRabbitRESTClient(RESTClient client) {
        def test = client.get(path : '/api/overview')
        return (test.status == 200);
    }
}
