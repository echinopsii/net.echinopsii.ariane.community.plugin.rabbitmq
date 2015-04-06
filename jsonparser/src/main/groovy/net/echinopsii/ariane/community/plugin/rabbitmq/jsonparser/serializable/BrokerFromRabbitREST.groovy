/**
 * RabbitMQ plugin jsonparser bundle
 * RabbitMQ plugin jsonparser RabbitMQ Broker from REST api
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

class BrokerFromRabbitREST implements Serializable {

    public transient static final String REST_RABBITMQ_BROKER_OVERVIEW_PATH = '/api/overview'

    public transient static final String JSON_RABBITMQ_BROKER_OVERVIEW_STATISTICS_DB_NODE  = "statistics_db_node"
    public transient static final String JSON_RABBITMQ_BROKER_OVERVIEW_MANAGEMENT_VERSION  = "management_version"
    public transient static final String JSON_RABBITMQ_BROKER_OVERVIEW_RABBITMQ_VERSION    = "rabbitmq_version"
    public transient static final String JSON_RABBITMQ_BROKER_OVERVIEW_ERLANG_VERSION      = "erlang_version"
    public transient static final String JSON_RABBITMQ_BROKER_OVERVIEW_ERLANG_FULL_VERSION = "erlang_full_version"
    @SuppressWarnings("GroovyUnusedDeclaration")
    public transient static final String JSON_RABBITMQ_BROKER_OVERVIEW_LISTENERS           = "listeners"
    public transient static final String JSON_RABBITMQ_BROKER_OVERVIEW_LISTENERS_NODE      = "node"
    public transient static final String JSON_RABBITMQ_BROKER_OVERVIEW_LISTENERS_PROTOCOL  = "protocol"
    public transient static final String JSON_RABBITMQ_BROKER_OVERVIEW_LISTENERS_IPADDRESS = "ip_address"
    public transient static final String JSON_RABBITMQ_BROKER_OVERVIEW_LISTENERS_PORT      = "port"

    private transient static final String JSON_RABBITMQ_NODE_PATH = "/api/nodes/"
    private transient static final String JSON_RABBITMQ_NODE_NAME = "name"

    @SuppressWarnings("GroovyUnusedDeclaration")
    public transient static final int REST_NODE_INVALID_ID_NAME_OR_CLUSTER = -21;

    @SuppressWarnings("GroovyUnusedDeclaration")
    private transient static final Logger log = LoggerFactory.getLogger(BrokerFromRabbitREST.class);

    transient RabbitClusterToConnect cluster;
    transient RabbitNodeToConnect    node;

    String name;
    String url;
    Map<String, Object> properties = new HashMap<String, Objects>()

    Map<String, String>  listeningAddress
    Map<String, Integer> listeningPorts

    BrokerFromRabbitREST(String name, RabbitNodeToConnect node) {
        this.node = node;
        this.name = name;
    }

    BrokerFromRabbitREST(String name, RabbitClusterToConnect cluster) {
        this.name = name;
        this.cluster = cluster;
    }

    BrokerFromRabbitREST parse() {
        if (cluster != null) {
            for (RabbitNodeToConnect node : cluster.getNodes())
                if (node.getName().equals(this.name)) {
                    listeningAddress = node.getListeningAddress()
                    listeningPorts = node.getListeningPorts()
                    properties.put(JSON_RABBITMQ_BROKER_OVERVIEW_STATISTICS_DB_NODE, node.statisticsDBNode)
                    properties.put(JSON_RABBITMQ_BROKER_OVERVIEW_ERLANG_VERSION, node.getErlangVersion())
                    properties.put(JSON_RABBITMQ_BROKER_OVERVIEW_ERLANG_FULL_VERSION, node.getErlangFullVersion())
                    properties.put(JSON_RABBITMQ_BROKER_OVERVIEW_MANAGEMENT_VERSION, node.getManagementVersion())
                    properties.put(JSON_RABBITMQ_BROKER_OVERVIEW_RABBITMQ_VERSION, node.getRabbitmqVersion())
                    break;
                }
        } else if (node!=null) {
            listeningAddress = node.getListeningAddress()
            listeningPorts = node.getListeningPorts()
            properties.put(JSON_RABBITMQ_BROKER_OVERVIEW_STATISTICS_DB_NODE, node.statisticsDBNode)
            properties.put(JSON_RABBITMQ_BROKER_OVERVIEW_ERLANG_VERSION, node.getErlangVersion())
            properties.put(JSON_RABBITMQ_BROKER_OVERVIEW_ERLANG_FULL_VERSION, node.getErlangFullVersion())
            properties.put(JSON_RABBITMQ_BROKER_OVERVIEW_MANAGEMENT_VERSION, node.getManagementVersion())
            properties.put(JSON_RABBITMQ_BROKER_OVERVIEW_RABBITMQ_VERSION, node.getRabbitmqVersion())
        }

        String node_req_path = JSON_RABBITMQ_NODE_PATH + this.name;
        def node_req = (clustet!=null) ? cluster.get(node_req_path) : ((node !=null) ? node.getRestCli().get(path: node_req_path) : null)
        if (node_req!=null && node_req.status == 200 && node_req.data != null)
            properties.putAll((Map<String,Object>)node_req.data)
        properties.remove(JSON_RABBITMQ_NODE_NAME)

        return this;
    }

    BrokerFromRabbitREST setUrl(String url) {
        this.url = url
        return this
    }

    boolean equals(o) {
        if (this.is(o)) return true
        if (getClass() != o.class) return false

        BrokerFromRabbitREST that = (BrokerFromRabbitREST) o

        if (name != that.name) return false

        return true
    }

    int hashCode() {
        return name.hashCode()
    }
}
