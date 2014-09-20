package net.echinopsii.ariane.community.plugin.rabbitmq.jsonparser

import groovyx.net.http.RESTClient
import net.echinopsii.ariane.community.plugin.rabbitmq.directory.model.RabbitmqCluster
import net.echinopsii.ariane.community.plugin.rabbitmq.directory.model.RabbitmqNode

class Cluster implements Serializable {

    RESTClient rest = null;

    Cluster(RabbitmqCluster rbmqCluster) {
        for (RabbitmqNode node : rbmqCluster.getNodes()) {
            this.rest = new RESTClient( node.getUrl() )
            rest.auth.basic node.getUser(), node.getPasswd()

            def test = rest.get(path : '/api/overview')
            if (test.get == 200)
                break;
        }
    }

    Cluster parse() {
        return null;
    }
}
