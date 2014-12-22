package net.echinopsii.ariane.community.plugin.rabbitmq.jsonparser.serializable

import net.echinopsii.ariane.community.plugin.rabbitmq.jsonparser.tools.RabbitClusterToConnect
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class BrokerFromRabbitREST implements Serializable {

    public static final int REST_NODE_INVALID_ID_NAME_OR_CLUSTER = -21;

    private static final Logger log = LoggerFactory.getLogger(BrokerFromRabbitREST.class);

    transient RabbitClusterToConnect cluster;

    String name;
    String url;
    Map<String, Object> properties

    BrokerFromRabbitREST(String name, RabbitClusterToConnect cluster) {
        this.name = name;
        this.cluster = cluster;
    }

    BrokerFromRabbitREST parse() {
        def restClient = this.cluster.getRestCli()

        String node_req_path =  '/api/nodes/' + this.name;
        def node_req = restClient.get(path : node_req_path)
        if (node_req.status == 200 && node_req.data != null)
            properties = node_req.data
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
