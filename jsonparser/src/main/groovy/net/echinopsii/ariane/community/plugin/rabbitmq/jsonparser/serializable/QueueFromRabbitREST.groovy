package net.echinopsii.ariane.community.plugin.rabbitmq.jsonparser.serializable

import net.echinopsii.ariane.community.plugin.rabbitmq.jsonparser.tools.RabbitClusterToConnect
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class QueueFromRabbitREST implements Serializable {

    private static final Logger log = LoggerFactory.getLogger(QueueFromRabbitREST.class);

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
        // The following queue_req_path should be used but there is a problem in the groovy HTTPBuilder
        // api/queues/%2F/queueName for vhost "/" is re-encoded api/queues/%252F/queueName and api/queues///queueName is re-encoded api/queues/queueName
        // String queue_req_path =  'api/queues/' + URLEncoder.encode(this.vhost, "ASCII") + "/" + URLEncoder.encode(this.name, "ASCII")
        String queues_req_path = 'api/queues'
        def queues_req = cluster.get(queues_req_path)
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
