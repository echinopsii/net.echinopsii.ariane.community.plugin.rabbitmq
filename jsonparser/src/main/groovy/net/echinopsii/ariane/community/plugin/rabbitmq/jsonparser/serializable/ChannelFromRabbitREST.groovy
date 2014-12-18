package net.echinopsii.ariane.community.plugin.rabbitmq.jsonparser.serializable

import net.echinopsii.ariane.community.plugin.rabbitmq.jsonparser.tools.RabbitClusterToConnect

import javax.persistence.Transient

class ChannelFromRabbitREST implements Serializable {
    @Transient
    RabbitClusterToConnect cluster;

    String name
    Map<String, Object> properties

    ChannelFromRabbitREST(String name, RabbitClusterToConnect cluster) {
        this.name = name
        this.cluster = cluster
    }

    ChannelFromRabbitREST parse() {
        def restClient = this.cluster.getRestCli()

        String channel_req_path =  '/api/channels/' + this.name;
        def channel_req = restClient.get(path : channel_req_path)
        if (channel_req.status == 200 && channel_req.data != null) {
            properties = channel_req.data
            properties.remove("name")
        }

        return this
    }
}
