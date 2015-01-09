/**
 * RabbitMQ plugin jsonparser bundle
 * RabbitMQ plugin jsonparser RabbitMQ Binding from REST API
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
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class BindingFromRabbitREST implements Serializable {

    private transient static final String REST_RABBITMQ_BINDING_PATH = "/api/bindings/"

    public transient static final String JSON_RABBITMQ_BINDING_SOURCE           = "source"
    public transient static final String JSON_RABBITMQ_BINDING_DESTINATION_TYPE = "destination_type"
    public transient static final String JSON_RABBITMQ_BINDING_DESTINATION      = "destination"
    @SuppressWarnings("GroovyUnusedDeclaration")
    public transient static final String JSON_RABBITMQ_BINDING_PROPERTIES_KEY   = "properties_key"
    public transient static final String JSON_RABBITMQ_BINDING_ROUNTING_KEY     = "routing_key"
    private transient static final String JSON_RABBITMQ_BINDING_VHOST           = "vhost"

    public transient static final String RABBITMQ_BINDING_DESTINATION_TYPE_Q = "queue"
    public transient static final String RABBITMQ_BINDING_DESTINATION_TYPE_E = "exchange"

    @SuppressWarnings("GroovyUnusedDeclaration")
    private transient static final Logger log = LoggerFactory.getLogger(BindingFromRabbitREST.class);

    transient RabbitClusterToConnect cluster;

    String name;
    String vhost;
    Map<String, Object> properties

    BindingFromRabbitREST(String name,  String vhost, RabbitClusterToConnect cluster) {
        this.name = name
        this.vhost = vhost
        this.cluster = cluster
    }

    BindingFromRabbitREST parse() {
        // The following binding_req_path should be used but there is a problem in the groovy HTTPBuilder
        // api/bindings/%2F/bindingName for vhost "/" is re-encoded api/bindings/%252F/bindingName and api/bindings///bindingName is re-encoded api/bindings/bindingName
        // String binding_req_path =  'api/bindings/' + URLEncoder.encode(this.vhost, "ASCII") + "/" + URLEncoder.encode(this.name, "ASCII")
        def bindings_req = cluster.get(REST_RABBITMQ_BINDING_PATH)
        if (bindings_req.status == 200 && bindings_req.data != null) {
            bindings_req.data.each { binding ->
                if (binding.source.equals(""))
                    binding.source = ExchangeFromRabbitREST.RABBITMQ_DEFAULT_EXCH_NAME
                if (((String)binding.source +
                     "-[" + (String)binding.destination_type + "/{" + (String)binding.routing_key + "," + (String)binding.properties_key + "}]->"
                     + (String)binding.destination).equals(this.name) && binding.vhost.equals(this.vhost))
                    properties = binding
            }
            properties.remove(JSON_RABBITMQ_BINDING_VHOST)
        }
        return this;
    }

    boolean equals(o) {
        if (this.is(o)) return true
        if (getClass() != o.class) return false

        BindingFromRabbitREST that = (BindingFromRabbitREST) o

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
