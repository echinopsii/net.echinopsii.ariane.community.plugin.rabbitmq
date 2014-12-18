package net.echinopsii.ariane.community.plugin.rabbitmq.jsonparser.serializable

import net.echinopsii.ariane.community.plugin.rabbitmq.jsonparser.tools.RabbitClusterToConnect

import javax.persistence.Transient

class VhostFromRabbitREST implements Serializable {
    @Transient
    RabbitClusterToConnect cluster;

    String name
    Map<String, Object> properties

    VhostFromRabbitREST(String name, RabbitClusterToConnect cluster) {
        this.name = name;
        this.cluster = cluster
    }

    VhostFromRabbitREST parse() {
        def restClient = this.cluster.getRestCli()

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
