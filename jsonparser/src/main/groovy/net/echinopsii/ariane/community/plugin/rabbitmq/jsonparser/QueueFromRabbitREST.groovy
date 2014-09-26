package net.echinopsii.ariane.community.plugin.rabbitmq.jsonparser

import net.echinopsii.ariane.community.plugin.rabbitmq.directory.model.RabbitmqCluster

import javax.persistence.Transient

class QueueFromRabbitREST implements Serializable {
    @Transient
    RabbitmqCluster cluster;

    String name
    String vhost
    Map<String, Object> properties

    QueueFromRabbitREST(String name, String vhost, RabbitmqCluster cluster) {
        this.name = name
        this.vhost = vhost
        this.cluster = cluster
    }

    QueueFromRabbitREST parse() {
        def restClient = RESTClientProviderFromRabbitmqCluster.getRESTClientFromCluster(this.cluster);

        String queues_req_path =  '/api/queues'
        def queues_req = restClient.get(path : queues_req_path)
        if (queues_req.status == 200 && queues_req.data != null) {
            queues_req.data.each { queue ->
                if (queue.name.equals(this.name) && queue.vhost.equals(this.vhost))
                    properties = queue
            }
            properties.remove("name")
            properties.remove("vhost")
        }

        return this
    }
}
