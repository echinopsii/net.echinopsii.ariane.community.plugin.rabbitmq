package net.echinopsii.ariane.community.plugin.rabbitmq.jsonparser

import net.echinopsii.ariane.community.plugin.rabbitmq.directory.model.RabbitmqCluster
import net.echinopsii.ariane.community.plugin.rabbitmq.directory.model.RabbitmqNode

import javax.persistence.Transient

class ClusterFromRabbitREST implements Serializable {

    public static final int REST_CLU_INVALID_ID_NAME   = -11;
    public static final int REST_CLU_NODE_NOT_DEFINED  = -12;
    public static final int REST_CLU_DEF_NODE_INVALID  = -13;

    @Transient
    RabbitmqCluster cluster = null;

    String       name;
    List<String> nodes        = new ArrayList<String>();
    List<String> runningNodes = new ArrayList<String>();

    ClusterFromRabbitREST(RabbitmqCluster cluster) {
        this.cluster = cluster;
    }

    ClusterFromRabbitREST parse() {
        def rclient = RESTClientProviderFromRabbitmqCluster.getRESTClientFromCluster(this.cluster);

        if (rclient!=null) {
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

            /*
             * some compliance between Ariane RabbitMQ Dir and real cluster definitions
             */
            if (!this.cluster.getName().equals(this.name))
                this.cluster.getErrors().put(this.cluster.getName(), REST_CLU_INVALID_ID_NAME)

            HashSet<RabbitmqNode> invalidNodes = new HashSet<RabbitmqNode>(this.cluster.getNodes());

            for (String clusterNodeName : nodes) {
                boolean isDefinedCorrectly = false;
                for (RabbitmqNode node : this.cluster.getNodes()) {
                    node.getErrors().clear();
                    if (node.getName().equals(clusterNodeName)) {
                        isDefinedCorrectly = true;
                        invalidNodes.remove(node);
                        break;
                    }
                }
                if (!isDefinedCorrectly)
                    this.cluster.getErrors().put(clusterNodeName, REST_CLU_NODE_NOT_DEFINED);
            }

            for (RabbitmqNode node : invalidNodes) {
                this.cluster.getErrors().put(this.cluster.getName()+"-"+node.getName(), REST_CLU_DEF_NODE_INVALID);
                node.getErrors().put(NodeFromRabbitREST.REST_NODE_INVALID_ID_NAME_OR_CLUSTER);
            }

            for (String error : this.cluster.getErrors().keySet())
                if (!error.contains(this.cluster.getName()))
                    for (RabbitmqNode node : this.cluster.getNodes())
                        if (node.getName().equals(error))
                            node.getErrors().add(this.cluster.getErrors().get(error));
        }

        return this;
    }
}
