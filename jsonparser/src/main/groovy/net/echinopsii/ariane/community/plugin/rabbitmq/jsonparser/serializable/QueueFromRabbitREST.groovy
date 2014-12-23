package net.echinopsii.ariane.community.plugin.rabbitmq.jsonparser.serializable

import net.echinopsii.ariane.community.plugin.rabbitmq.jsonparser.tools.RabbitClusterToConnect

import javax.persistence.Transient

class QueueFromRabbitREST implements Serializable {

    transient RabbitClusterToConnect cluster;

    String name
    String vhost
    Map<String, Object> properties

    QueueFromRabbitREST(String name, String vhost, RabbitClusterToConnect cluster) {
        this.name = name
        this.vhost = vhost
        this.cluster = cluster
    }

    QueueFromRabbitREST parse() {
        def restClient = this.cluster.getRestCli()

        String queue_req_path =  '/api/queues/' + this.vhost + "/" + this.name
        def queue_req = restClient.get(path : queue_req_path)
        if (queue_req.status == 200 && queue_req.data != null) {
            //queues_req.data.each { queue ->
            //    if (queue.name.equals(this.name) && queue.vhost.equals(this.vhost))
            //        properties = queue
            //}
            properties = queue_req.data
            properties.remove("name")
            properties.remove("vhost")
        }

        return this
    }

    boolean equals(o) {
        if (this.is(o)) return true
        if (getClass() != o.class) return false

        QueueFromRabbitREST that = (QueueFromRabbitREST) o

        if (name != that.name) return false
        if (vhost != that.vhost) return false

        return true
    }

    int hashCode() {
        int result
        result = name.hashCode()
        result = 31 * result + vhost.hashCode()
        return result
    }
}
