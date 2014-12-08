package net.echinopsii.ariane.community.plugin.rabbitmq.jsonparser

import net.echinopsii.ariane.community.plugin.rabbitmq.directory.model.RabbitmqCluster

import javax.persistence.Transient

class ExchangeFromRabbitREST implements Serializable {
    @Transient
    RabbitmqCluster cluster;

    String name
    String vhost
    Map<String, Object> properties

    ExchangeFromRabbitREST(String name, String vhost, RabbitmqCluster cluster) {
        this.name = name
        this.vhost = vhost
        this.cluster = cluster
    }

    ExchangeFromRabbitREST parse() {
        def restClient = RESTClientProviderFromRabbitmqCluster.getRESTClientFromCluster(this.cluster);

        String exchanges_req_path =  '/api/exchanges'
        def exchanges_req = restClient.get(path : exchanges_req_path)
        if (exchanges_req.status == 200 && exchanges_req.data != null) {
            exchanges_req.data.each { exchange ->
                if (exchange.name.equals(this.name) && exchange.vhost.equals(this.vhost))
                    properties = exchange
            }
            properties.remove("name")
            properties.remove("vhost")
        }

        return this
    }
}