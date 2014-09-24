package net.echinopsii.ariane.community.plugin.rabbitmq.jsonparser

import net.echinopsii.ariane.community.plugin.rabbitmq.directory.model.RabbitmqCluster
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import javax.persistence.Transient

class NodeFromRabbitREST implements Serializable {

    public static final int REST_NODE_INVALID_ID_NAME_OR_CLUSTER = -21;

    private static final Logger log = LoggerFactory.getLogger(NodeFromRabbitREST.class);

    @Transient
    RabbitmqCluster cluster;

    String name;
    Map<String, Object> properties = new HashMap<String, Object>();

    NodeFromRabbitREST(String name, RabbitmqCluster cluster) {
        this.name = name;
        this.cluster = cluster;
    }

    NodeFromRabbitREST parse() {
        def restClient = RESTClientProviderFromRabbitmqCluster.getRESTClientFromCluster(this.cluster);

        String node_req_path =  '/api/nodes/' + this.name;
        def node_req = restClient.get(path : node_req_path)
        if (node_req.status == 200 && node_req.data != null) {
            node_req.data.each { key, value ->
                if (!key.equals("name"))
                    properties.put((String)key, value);
            }
        }

        return this;
    }
}
