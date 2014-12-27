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
