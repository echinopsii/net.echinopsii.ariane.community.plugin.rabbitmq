package net.echinopsii.ariane.community.plugin.rabbitmq.jsonparser

import net.echinopsii.ariane.community.plugin.rabbitmq.directory.model.RabbitmqCluster
import net.echinopsii.ariane.community.plugin.rabbitmq.directory.model.RabbitmqNode
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import javax.persistence.Transient

class NodeFromRabbitREST implements Serializable {

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
        def restClient = RabbitRESTClient.getRESTClientFromCluster(this.cluster);

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
