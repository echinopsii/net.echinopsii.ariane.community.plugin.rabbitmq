package net.echinopsii.ariane.community.plugin.rabbitmq.jsonparser

import net.echinopsii.ariane.community.plugin.rabbitmq.directory.model.RabbitmqCluster

import javax.persistence.Transient

class ClusterFromRabbitREST implements Serializable {

    @Transient
    RabbitmqCluster cluster = null;

    String name;
    List<String> nodes = new ArrayList<String>();
    List<String> runningNodes = new ArrayList<String>();

    ClusterFromRabbitREST(RabbitmqCluster cluster) {
        this.cluster = cluster;
    }

    ClusterFromRabbitREST parse() {
        def rclient = RabbitRESTClient.getRESTClientFromCluster(this.cluster);

        def cluster_name_req = rclient.get(path : '/api/cluster-name')
        if (cluster_name_req.status == 200 && cluster_name_req.data != null && cluster_name_req.data.name!=null ) this.name = cluster_name_req.data.name;

        def cluster_nodes_req = rclient.get(path : '/api/nodes')
        if (cluster_nodes_req.status == 200 && cluster_nodes_req.data != null && cluster_nodes_req.data instanceof List) {
            cluster_nodes_req.data.each { node ->
                nodes.add((String)node.name)
                if (node.running)
                    runningNodes.add((String)node.name)
            }
        }
        return this;
    }
}
