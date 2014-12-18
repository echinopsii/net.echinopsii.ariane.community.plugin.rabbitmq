package net.echinopsii.ariane.community.plugin.rabbitmq.jsonparser.serializable

import net.echinopsii.ariane.community.plugin.rabbitmq.jsonparser.tools.RESTClientProvider
import net.echinopsii.ariane.community.plugin.rabbitmq.jsonparser.tools.RabbitClusterToConnect
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import javax.persistence.Transient

class NodeFromRabbitREST implements Serializable {

    public static final int REST_NODE_INVALID_ID_NAME_OR_CLUSTER = -21;

    private static final Logger log = LoggerFactory.getLogger(NodeFromRabbitREST.class);

    @Transient
    RabbitClusterToConnect cluster;

    String name;
    Map<String, Object> properties

    NodeFromRabbitREST(String name, RabbitClusterToConnect cluster) {
        this.name = name;
        this.cluster = cluster;
    }

    NodeFromRabbitREST parse() {
        def restClient = RESTClientProvider.getRESTClientFromCluster(this.cluster);

        String node_req_path =  '/api/nodes/' + this.name;
        def node_req = restClient.get(path : node_req_path)
        if (node_req.status == 200 && node_req.data != null)
            properties = node_req.data
        properties.remove("name")

        return this;
    }
}
