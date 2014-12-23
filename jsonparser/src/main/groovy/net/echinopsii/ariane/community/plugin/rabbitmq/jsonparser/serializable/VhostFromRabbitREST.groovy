package net.echinopsii.ariane.community.plugin.rabbitmq.jsonparser.serializable

import net.echinopsii.ariane.community.plugin.rabbitmq.jsonparser.tools.RabbitClusterToConnect

import javax.persistence.Transient

class VhostFromRabbitREST implements Serializable {

    transient RabbitClusterToConnect cluster;

    String name
    Map<String, Object> properties

    VhostFromRabbitREST(String name, RabbitClusterToConnect cluster) {
        this.name = name;
        this.cluster = cluster
    }

    VhostFromRabbitREST parse() {
        String vhosts_req_path  = '/api/vhosts'
        def vhosts_req = cluster.get(vhosts_req_path)
        if (vhosts_req.status == 200 && vhosts_req.data != null) {
            vhosts_req.data.each { vhost ->
                if (vhost.name.equals(this.name))
                    properties = vhost
            }
            properties.remove("name")
        }
        return this
    }

    boolean equals(o) {
        if (this.is(o)) return true
        if (getClass() != o.class) return false

        VhostFromRabbitREST that = (VhostFromRabbitREST) o

        if (name != that.name) return false

        return true
    }

    int hashCode() {
        return name.hashCode()
    }
}
