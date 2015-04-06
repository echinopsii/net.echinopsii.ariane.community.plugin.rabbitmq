/**
 * RabbitMQ plugin jsonparser bundle
 * RabbitMQ plugin jsonparser RabbitMQ channel from REST api
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

class ChannelFromRabbitREST implements Serializable {

    private transient static final String REST_RABBITMQ_CHANNEL_PATH = "/api/channels/"

    private transient static final String JSON_RABBITMQ_CHANNEL_NAME                         = "name"
    public transient static final String JSON_RABBITMQ_CHANNEL_NODE                          = "node"
    public transient static final String JSON_RABBITMQ_CHANNEL_CONNECTION_DETAILS            = "connection_details"
    public transient static final String JSON_RABBITMQ_CHANNEL_CONNECTION_DETAILS_NAME       = "name"
    public transient static final String JSON_RABBITMQ_CHANNEL_CONNECTION_DETAILS_PEER_PORT  = "peer_port"
    public transient static final String JSON_RABBITMQ_CHANNEL_CONNECTION_DETAILS_PEER_HOST  = "peer_host"
    public transient static final String JSON_RABBITMQ_CHANNEL_CONSUMER_DETAILS              = "consumer_details"
    public transient static final String JSON_RABBITMQ_CHANNEL_CONSUMER_DETAILS_QUEUE        = "queue"
    public transient static final String JSON_RABBITMQ_CHANNEL_CONSUMER_DETAILS_QUEUE_VHOST  = "vhost"
    public transient static final String JSON_RABBITMQ_CHANNEL_CONSUMER_DETAILS_QUEUE_NAME   = "name"
    public transient static final String JSON_RABBITMQ_CHANNEL_CONSUMER_DETAILS_CONSUMER_TAG = "consumer_tag"
    public transient static final String JSON_RABBITMQ_CHANNEL_PUBLISHES                     = "publishes"
    public transient static final String JSON_RABBITMQ_CHANNEL_PUBLISHES_EXCHANGE            = "exchange"
    public transient static final String JSON_RABBITMQ_CHANNEL_PUBLISHES_EXCHANGE_VHOST      = "vhost"
    public transient static final String JSON_RABBITMQ_CHANNEL_PUBLISHES_EXCHANGE_NAME       = "name"


    transient RabbitClusterToConnect cluster;
    transient RabbitNodeToConnect node;

    String name
    Map<String, Object> properties

    ChannelFromRabbitREST(String name, RabbitNodeToConnect node) {
        this.name = name;
        this.node = node;
    }

    ChannelFromRabbitREST(String name, RabbitClusterToConnect cluster) {
        this.name = name
        this.cluster = cluster
    }

    ChannelFromRabbitREST parse() {
        String channel_req_path =  REST_RABBITMQ_CHANNEL_PATH + this.name;
        def channel_req = (cluster!=null) ? cluster.get(channel_req_path) : ((node!=null) ? node.getRestCli().get(path: channel_req_path) : null);
        if (channel_req != null && channel_req.status == 200 && channel_req.data != null) {
            channel_req.data.publishes.each { publish ->
                if (publish.exchange.name.equals(""))
                    publish.exchange.name=ExchangeFromRabbitREST.RABBITMQ_DEFAULT_EXCH_NAME
            }
            properties = channel_req.data
            properties.remove(JSON_RABBITMQ_CHANNEL_NAME)
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
