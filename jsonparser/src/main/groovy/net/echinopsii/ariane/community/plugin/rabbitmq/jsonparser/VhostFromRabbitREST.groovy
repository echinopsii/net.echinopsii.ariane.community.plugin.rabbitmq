package net.echinopsii.ariane.community.plugin.rabbitmq.jsonparser

import net.echinopsii.ariane.community.plugin.rabbitmq.directory.model.RabbitmqCluster

import javax.persistence.Transient

class VhostFromRabbitREST {
    @Transient
    RabbitmqCluster cluster;

    String name
    Map<String, Object> properties

    VhostFromRabbitREST(String name, RabbitmqCluster cluster) {
        this.name = name;
        this.cluster = cluster
    }

    VhostFromRabbitREST parse() {
        def restClient = RESTClientProviderFromRabbitmqCluster.getRESTClientFromCluster(this.cluster);

        String vhosts_req_path  = '/api/vhosts'
        def vhosts_req = restClient.get(path : vhosts_req_path)
        if (vhosts_req.status == 200 && vhosts_req.data != null) {
            vhosts_req.data.each { vhost ->
                if (vhost.name.equals(this.name))
                    properties = vhost
            }
            properties.remove("name")
        }

        return this
    }

}
