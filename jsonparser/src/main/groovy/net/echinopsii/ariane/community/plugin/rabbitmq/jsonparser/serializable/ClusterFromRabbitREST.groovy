/**
 * RabbitMQ plugin jsonparser bundle
 * RabbitMQ plugin jsonparser RabbitMQ cluster from REST api
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

class ClusterFromRabbitREST implements Serializable {

    transient RabbitClusterToConnect  cluster = null;

    String       name;
    List<String> nodes        = new ArrayList<String>();
    List<String> runningNodes = new ArrayList<String>();

    ClusterFromRabbitREST(RabbitClusterToConnect cluster) {
        this.cluster = cluster;
    }

    ClusterFromRabbitREST parse() {
        def cluster_name_req = this.cluster.get('/api/cluster-name')
        if (cluster_name_req!=null && cluster_name_req.status == 200 && cluster_name_req.data != null && cluster_name_req.data.name!=null ) this.name = cluster_name_req.data.name;

        if (this.name != null)  {
            def cluster_nodes_req = this.cluster.get('/api/nodes')
            if (cluster_name_req!=null && cluster_nodes_req.status == 200 && cluster_nodes_req.data != null && cluster_nodes_req.data instanceof List) {
                cluster_nodes_req.data.each { node ->
                    nodes.add((String)node.name)
                    if (node.running)
                        runningNodes.add((String)node.name)
                }
            }
        }

        return this;
    }

    ClusterFromRabbitREST clone() {
        ClusterFromRabbitREST ret = new ClusterFromRabbitREST(this.cluster);
        ret.setName(this.name);
        ret.setNodes(new ArrayList<String>(this.nodes));
        ret.setRunningNodes(new ArrayList<String>(this.runningNodes));
        return ret;
    }
}
