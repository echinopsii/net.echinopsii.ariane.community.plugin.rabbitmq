/**
 * RabbitMQ plugin jsonparser bundle
 * RabbitMQ plugin jsonparser RabbitMQ vhost from REST api
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

class VhostFromRabbitREST implements Serializable {

    private static final Logger log = LoggerFactory.getLogger(VhostFromRabbitREST.class)

    private transient static final String REST_RABBITMQ_VHOST_PATH = "/api/vhosts/"

    private transient static final String JSON_RABBITMQ_VHOST_NAME = "name"

    transient RabbitClusterToConnect cluster;
    transient RabbitNodeToConnect    node;

    String name
    Map<String, Object> properties

    VhostFromRabbitREST(String name, RabbitNodeToConnect node) {
        this.name = name;
        this.node = node
    }

    VhostFromRabbitREST(String name, RabbitClusterToConnect cluster) {
        this.name = name;
        this.cluster = cluster
    }

    VhostFromRabbitREST parse() {
        try {
            def vhosts_req = (cluster != null) ? cluster.get(REST_RABBITMQ_VHOST_PATH) : (node != null) ? node.get(REST_RABBITMQ_VHOST_PATH) : null
            if (vhosts_req != null && vhosts_req.status == 200 && vhosts_req.data != null) {
                vhosts_req.data.each { vhost ->
                    if (vhost.name.equals(this.name))
                        properties = vhost
                }
                properties.remove(JSON_RABBITMQ_VHOST_NAME)
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

        VhostFromRabbitREST that = (VhostFromRabbitREST) o

        if (name != that.name) return false

        return true
    }

    int hashCode() {
        return name.hashCode()
    }
}
