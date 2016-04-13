/**
 * RabbitMQ plugin jsonparser bundle
 * RabbitMQ plugin jsonparser connection from REST api
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

class ConnectionFromRabbitREST implements Serializable {

    private transient static final String REST_RABBITMQ_CONNECTION_PATH = "/api/connections/"

    public transient static final String JSON_RABBITMQ_CONNECTION_NAME                           = "name";
    public transient static final String JSON_RABBITMQ_CONNECTION_NODE                           = "node";
    public transient static final String JSON_RABBITMQ_CONNECTION_PROTOCOL                       = "protocol";
    public transient static final String JSON_RABBITMQ_CONNECTION_SSL                            = "ssl";
    public transient static final String JSON_RABBITMQ_CONNECTION_HOST                           = "host";
    public transient static final String JSON_RABBITMQ_CONNECTION_PORT                           = "port";
    public transient static final String JSON_RABBITMQ_CONNECTION_PEER_HOST                      = "peer_host";
    public transient static final String JSON_RABBITMQ_CONNECTION_PEER_PORT                      = "peer_port";
    public transient static final String JSON_RABBITMQ_CONNECTION_CLIENT_PROPERTIES              = "client_properties";
    public transient static final String JSON_RABBITMQ_CONNECTION_CLIENT_PROPERTIES_PRODUCT      = "product";
    @SuppressWarnings("GroovyUnusedDeclaration")
    public transient static final String JSON_RABBITMQ_CONNECTION_CLIENT_PROPERTIES_PLATFORM     = "platform";
    @SuppressWarnings("GroovyUnusedDeclaration")
    public transient static final String JSON_RABBITMQ_CONNECTION_CLIENT_PROPERTIES_INFORMATION  = "information";
    public transient static final String JSON_RABBITMQ_CONNECTION_CLIENT_PROPERTIES_ARIANE_PGURL = "ariane.pgurl"
    public transient static final String JSON_RABBITMQ_CONNECTION_CLIENT_PROPERTIES_ARIANE_OSI   = "ariane.osi";
    public transient static final String JSON_RABBITMQ_CONNECTION_CLIENT_PROPERTIES_ARIANE_OTM   = "ariane.otm";
    public transient static final String JSON_RABBITMQ_CONNECTION_CLIENT_PROPERTIES_ARIANE_CMP   = "ariane.cmp";
    public transient static final String JSON_RABBITMQ_CONNECTION_CLIENT_PROPERTIES_ARIANE_APP   = "ariane.app";

    public transient static final String RABBITMQ_CONNECTION_PROTOCOL_AMQP  = "AMQP"
    public transient static final String RABBITMQ_CONNECTION_PROTOCOL_MQTT  = "MQTT"
    public transient static final String RABBITMQ_CONNECTION_PROTOCOL_STOMP = "STOMP"

    private static final Logger log = LoggerFactory.getLogger(ConnectionFromRabbitREST.class)

    transient RabbitClusterToConnect cluster;
    transient RabbitNodeToConnect    node;

    String name
    Map<String, Object> properties

    ConnectionFromRabbitREST(String name, RabbitNodeToConnect node) {
        this.node = node
        this.name = name
    }

    ConnectionFromRabbitREST(String name, RabbitClusterToConnect cluster) {
        this.cluster = cluster
        this.name = name
    }

    ConnectionFromRabbitREST parse() {
        try {
            String connection_req_path = REST_RABBITMQ_CONNECTION_PATH + this.name;
            def connection_req = (cluster != null) ? cluster.get(connection_req_path) : (node != null) ? node.get(connection_req_path) : null
            if (connection_req != null && connection_req.status == 200 && connection_req.data != null) {
                properties = connection_req.data
                properties.remove(JSON_RABBITMQ_CONNECTION_NAME)
            }
        } catch (Exception e) {
            if (log.isDebugEnabled())
                e.printStackTrace();
            log.error("PB with node " + name + " (" + node.getUrl() + "):" + e.getMessage())
        }
        return this;
    }

    boolean equals(o) {
        if (this.is(o)) return true
        if (getClass() != o.class) return false

        ConnectionFromRabbitREST that = (ConnectionFromRabbitREST) o

        if (name != that.name) return false

        return true
    }

    int hashCode() {
        return name.hashCode()
    }
}
