package net.echinopsii.ariane.community.plugin.rabbitmq.jsonparser.serializable

import net.echinopsii.ariane.community.plugin.rabbitmq.jsonparser.RESTClientProviderFromRabbitmqCluster
import net.echinopsii.ariane.community.plugin.rabbitmq.jsonparser.tools.RabbitClusterToConnect

import javax.persistence.Transient

class ConnectionFromRabbitREST implements Serializable {

    @Transient
    RabbitClusterToConnect cluster;

    String name
    Map<String, Object> properties

    ConnectionFromRabbitREST(String name, RabbitClusterToConnect cluster) {
        this.cluster = cluster
        this.name = name
    }

    ConnectionFromRabbitREST parse() {
        def restClient = RESTClientProviderFromRabbitmqCluster.getRESTClientFromCluster(this.cluster);

        String connection_req_path =  '/api/connections/' + this.name;
        def connection_req = restClient.get(path : connection_req_path)
        if (connection_req.status == 200 && connection_req.data != null) {
            properties = connection_req.data
            properties.remove("name")
        }

        return this;
    }
}
