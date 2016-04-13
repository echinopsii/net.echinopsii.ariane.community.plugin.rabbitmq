/**
 * RabbitMQ plugin jsonparser bundle
 * RabbitMQ plugin jsonparser RabbitMQ node where to connect
 * Copyright (C) 2014 Mathilde Ffrench
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package net.echinopsii.ariane.community.plugin.rabbitmq.jsonparser.serializable

import net.echinopsii.ariane.community.plugin.rabbitmq.jsonparser.tools.RabbitClusterToConnect
import net.echinopsii.ariane.community.plugin.rabbitmq.jsonparser.tools.RabbitNodeToConnect
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class QueueFromRabbitREST implements Serializable {

    private transient static final String REST_RABBITMQ_QUEUE_PATH = "/api/queues"

    private transient static final String JSON_RABBITMQ_QUEUE_NAME  = "name"
    private transient static final String JSON_RABBITMQ_QUEUE_VHOST = "vhost"

    @SuppressWarnings("GroovyUnusedDeclaration")
    private transient static final Logger log = LoggerFactory.getLogger(QueueFromRabbitREST.class);

    transient RabbitClusterToConnect cluster;
    transient RabbitNodeToConnect    node;

    String name
    String vhost
    Map<String, Object> properties

    QueueFromRabbitREST(String name, String vhost, RabbitNodeToConnect node) {
        this.name = name
        this.vhost = vhost
        this.node = node
    }

    QueueFromRabbitREST(String name, String vhost, RabbitClusterToConnect cluster) {
        this.name = name
        this.vhost = vhost
        this.cluster = cluster
    }

    QueueFromRabbitREST parse() {
        // The following queue_req_path should be used but there is a problem in the groovy HTTPBuilder
        // api/queues/%2F/queueName for vhost "/" is re-encoded api/queues/%252F/queueName and api/queues///queueName is re-encoded api/queues/queueName
        // String queue_req_path =  'api/queues/' + URLEncoder.encode(this.vhost, "ASCII") + "/" + URLEncoder.encode(this.name, "ASCII")
        try {
            def queues_req = (cluster != null) ? cluster.get(REST_RABBITMQ_QUEUE_PATH) : (node != null) ? node.get(REST_RABBITMQ_QUEUE_PATH) : null
            if (queues_req != null && queues_req.status == 200 && queues_req.data != null) {
                queues_req.data.each { queue ->
                    if (queue.name.equals(this.name) && queue.vhost.equals(this.vhost))
                        properties = queue
                }
                properties.remove(JSON_RABBITMQ_QUEUE_NAME)
                properties.remove(JSON_RABBITMQ_QUEUE_VHOST)
            }
        } catch (Exception e) {
            if (log.isDebugEnabled())
                e.printStackTrace();
            log.error("PB with node " + name + " (" + node.getUrl() + "):" + e.getMessage())
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
