package net.echinopsii.ariane.community.plugin.rabbitmq.jsonparser.serializable

import net.echinopsii.ariane.community.plugin.rabbitmq.jsonparser.tools.RabbitClusterToConnect

import javax.persistence.Transient

class ChannelFromRabbitREST implements Serializable {

    transient RabbitClusterToConnect cluster;

    String name
    Map<String, Object> properties

    ChannelFromRabbitREST(String name, RabbitClusterToConnect cluster) {
        this.name = name
        this.cluster = cluster
    }

    ChannelFromRabbitREST parse() {
        String channel_req_path =  '/api/channels/' + this.name;
        def channel_req = cluster.get(channel_req_path)
        if (channel_req.status == 200 && channel_req.data != null) {
            properties = channel_req.data
            properties.remove("name")
        }

        return this
    }

    boolean equals(o) {
        if (this.is(o)) return true
        if (getClass() != o.class) return false

        ChannelFromRabbitREST that = (ChannelFromRabbitREST) o

        if (name != that.name) return false

        return true
    }

    int hashCode() {
        return name.hashCode()
    }
}
