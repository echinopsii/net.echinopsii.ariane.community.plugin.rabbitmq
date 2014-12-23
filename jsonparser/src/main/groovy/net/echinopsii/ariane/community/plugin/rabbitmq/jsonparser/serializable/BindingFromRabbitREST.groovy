package net.echinopsii.ariane.community.plugin.rabbitmq.jsonparser.serializable

import net.echinopsii.ariane.community.plugin.rabbitmq.jsonparser.tools.RabbitClusterToConnect
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class BindingFromRabbitREST implements Serializable {

    private static final Logger log = LoggerFactory.getLogger(BindingFromRabbitREST.class);

    transient RabbitClusterToConnect cluster;

    String name;
    String vhost;
    Map<String, Object> properties

    BindingFromRabbitREST(String name,  String vhost, RabbitClusterToConnect cluster) {
        this.name = name
        this.vhost = vhost
        this.cluster = cluster
    }

    BindingFromRabbitREST parse() {
        def restClient = this.cluster.getRestCli()

        String binding_req_path =  '/api/bindings/' + this.vhost + "/" + this.name;
        def binding_req = restClient.get(path : binding_req_path)
        if (binding_req.status == 200 && binding_req.data != null)
            properties = binding_req.data
            properties.remove("name")
            properties.remove("vhost")

        return this;
    }

    boolean equals(o) {
        if (this.is(o)) return true
        if (getClass() != o.class) return false

        BindingFromRabbitREST that = (BindingFromRabbitREST) o

        if (name != that.name) return false
        if (vhost != that.vhost) return false

        return true
    }

    int hashCode() {
        int result
        result = name.hashCode()
        result = 31 * result + vhost.hashCode()
        return result
    }
}
