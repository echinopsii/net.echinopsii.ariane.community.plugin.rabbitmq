package net.echinopsii.ariane.community.plugin.rabbitmq.jsonparser.serializable

import net.echinopsii.ariane.community.plugin.rabbitmq.jsonparser.tools.RabbitClusterToConnect
import net.echinopsii.ariane.community.plugin.rabbitmq.jsonparser.tools.RabbitNodeToConnect
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class BrokerFromRabbitREST implements Serializable {

    public static final int REST_NODE_INVALID_ID_NAME_OR_CLUSTER = -21;

    private static final Logger log = LoggerFactory.getLogger(BrokerFromRabbitREST.class);

    transient RabbitClusterToConnect cluster;

    String name;
    String url;
    Map<String, Object> properties = new HashMap<String, Objects>()

    Map<String, String>  listeningAddress
    Map<String, Integer> listeningPorts

    BrokerFromRabbitREST(String name, RabbitClusterToConnect cluster) {
        this.name = name;
        this.cluster = cluster;
    }

    BrokerFromRabbitREST parse() {
        for (RabbitNodeToConnect node : cluster.getNodes())
            if (node.getName().equals(this.name)) {
                listeningAddress = node.getListeningAddress()
                listeningPorts   = node.getListeningPorts()
                properties.put("statistics_db_node", node.isStatisticsDBNode)
                properties.put("erlang_version", node.getErlangVersion())
                properties.put("erlang_full_version", node.getErlangFullVersion())
                properties.put("management_version", node.getManagementVersion())
                properties.put("rabbitmq_version", node.getRabbitmqVersion())
                break;
            }

        String node_req_path =  '/api/nodes/' + this.name;
        def node_req = cluster.get(node_req_path)
        if (node_req.status == 200 && node_req.data != null)
            properties.putAll((Map<String,Object>)node_req.data)
        properties.remove("name")

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
