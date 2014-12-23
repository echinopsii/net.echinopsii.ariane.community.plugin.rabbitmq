package net.echinopsii.ariane.community.plugin.rabbitmq.jsonparser.serializable

import net.echinopsii.ariane.community.plugin.rabbitmq.jsonparser.tools.RabbitClusterToConnect
import net.echinopsii.ariane.community.plugin.rabbitmq.jsonparser.tools.RabbitNodeToConnect

class ClusterFromRabbitREST implements Serializable {

    public static final int REST_CLU_INVALID_ID_NAME   = -11;
    public static final int REST_CLU_NODE_NOT_DEFINED  = -12;
    public static final int REST_CLU_DEF_NODE_INVALID  = -13;

    transient RabbitClusterToConnect cluster = null;

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

            /*
             * some compliance between in memory cluster definition and real cluster definitions
             */
            if (!this.cluster.getName().equals(this.name))
                this.cluster.getErrors().put(this.cluster.getName(), REST_CLU_INVALID_ID_NAME)

            HashSet<RabbitNodeToConnect> invalidNodes = new HashSet<RabbitNodeToConnect>(this.cluster.getNodes());

            for (String clusterNodeName : nodes) {
                boolean isDefinedCorrectly = false;
                for (RabbitNodeToConnect node : this.cluster.getNodes()) {
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

            for (RabbitNodeToConnect node : invalidNodes) {
                this.cluster.getErrors().put(this.cluster.getName()+"-"+node.getName(), REST_CLU_DEF_NODE_INVALID);
                node.getErrors().put(node.getName(), BrokerFromRabbitREST.REST_NODE_INVALID_ID_NAME_OR_CLUSTER);
            }

            for (String error : this.cluster.getErrors().keySet())
                if (!error.contains(this.cluster.getName()))
                    for (RabbitNodeToConnect node : this.cluster.getNodes())
                        if (node.getName().equals(error))
                            node.getErrors().add(this.cluster.getErrors().get(error));

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
