/**
 * RabbitMQ plugin jsonparser bundle
 * RabbitMQ plugin jsonparser RabbitMQ exchange from REST api
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

class ExchangeFromRabbitREST implements Serializable {

    private final transient static String REST_RABBITMQ_EXCHANGE_PATH = "/api/exchanges/"

    private final transient static String JSON_RABBITMQ_EXCHANGE_NAME  = "name"
    private final transient static String JSON_RABBITMQ_EXCHANGE_VHOST = "vhost"

    @SuppressWarnings("GroovyUnusedDeclaration")
    public final transient static String JSON_RABBITMQ_EXCHANGE_AUTO_DELETE = "auto_delete"
    @SuppressWarnings("GroovyUnusedDeclaration")
    public final transient static String JSON_RABBITMQ_EXCHANGE_ARGUMENTS   = "arguments"
    @SuppressWarnings("GroovyUnusedDeclaration")
    public final transient static String JSON_RABBITMQ_EXCHANGE_DURABLE     = "durable"
    public final transient static String JSON_RABBITMQ_EXCHANGE_TYPE        = "type"
    @SuppressWarnings("GroovyUnusedDeclaration")
    public final transient static String JSON_RABBITMQ_EXCHANGE_INTERNAL    = "internal"

    public final transient static String RABBITMQ_EXCHANGE_TYPE_DIRECT = "direct"
    public final transient static String RABBITMQ_EXCHANGE_TYPE_FANOUT = "fanout"
    public final transient static String RABBITMQ_EXCHANGE_TYPE_TOPIC  = "topic"
    public final transient static String RABBITMQ_EXCHANGE_TYPE_HEADER = "header"

    transient RabbitClusterToConnect cluster;

    String name
    String vhost
    Map<String, Object> properties

    public static final String RABBITMQ_DEFAULT_EXCH_NAME = "[AMQP default]"

    ExchangeFromRabbitREST(String name, String vhost, RabbitClusterToConnect cluster) {
        this.name = name
        this.vhost = vhost
        this.cluster = cluster
    }

    ExchangeFromRabbitREST parse() {
        // The following exchange_req_path should be used but there is a problem in the groovy HTTPBuilder
        // api/exchanges/%2F/exchangeName for vhost "/" is re-encoded api/exchanges/%252F/exchangeName and api/exchanges///exchangeName is re-encoded api/exchanges/exchangeName
        // String exchange_req_path =  'api/exchanges/' + URLEncoder.encode(this.vhost, "ASCII") + "/" + URLEncoder.encode(this.name, "ASCII")
        def exchanges_req = cluster.get(REST_RABBITMQ_EXCHANGE_PATH)
        if (exchanges_req.status == 200 && exchanges_req.data != null) {
            exchanges_req.data.each { exchange ->
                if (((this.name.equals(RABBITMQ_DEFAULT_EXCH_NAME) && exchange.name.equals("")) || (!this.name.equals(RABBITMQ_DEFAULT_EXCH_NAME) && exchange.name.equals(this.name)))
                    && exchange.vhost.equals(this.vhost))
                    properties = exchange
            }
            properties.remove(JSON_RABBITMQ_EXCHANGE_NAME)
            properties.remove(JSON_RABBITMQ_EXCHANGE_VHOST)
        }

        return this
    }

    boolean equals(o) {
        if (this.is(o)) return true
        if (getClass() != o.class) return false

        ExchangeFromRabbitREST that = (ExchangeFromRabbitREST) o

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
