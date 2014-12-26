package net.echinopsii.ariane.community.plugin.rabbitmq.jsonparser.serializable

import net.echinopsii.ariane.community.plugin.rabbitmq.jsonparser.tools.RabbitClusterToConnect

class ConnectionFromRabbitREST implements Serializable {

    private transient static final String REST_RABBITMQ_CONNECTION_PATH = "/api/connections/"

    public transient static final String JSON_RABBITMQ_CONNECTION_NAME                          = "name";
    public transient static final String JSON_RABBITMQ_CONNECTION_NODE                          = "node";
    public transient static final String JSON_RABBITMQ_CONNECTION_PROTOCOL                      = "protocol";
    public transient static final String JSON_RABBITMQ_CONNECTION_SSL                           = "ssl";
    public transient static final String JSON_RABBITMQ_CONNECTION_HOST                          = "host";
    public transient static final String JSON_RABBITMQ_CONNECTION_PORT                          = "port";
    public transient static final String JSON_RABBITMQ_CONNECTION_PEER_HOST                     = "peer_host";
    public transient static final String JSON_RABBITMQ_CONNECTION_PEER_PORT                     = "peer_port";
    public transient static final String JSON_RABBITMQ_CONNECTION_CLIENT_PROPERTIES             = "client_properties";
    public transient static final String JSON_RABBITMQ_CONNECTION_CLIENT_PROPERTIES_PRODUCT     = "product";
    public transient static final String JSON_RABBITMQ_CONNECTION_CLIENT_PROPERTIES_PLATFORM    = "platform";
    public transient static final String JSON_RABBITMQ_CONNECTION_CLIENT_PROPERTIES_INFORMATION = "information";

    transient RabbitClusterToConnect cluster;

    String name
    Map<String, Object> properties

    ConnectionFromRabbitREST(String name, RabbitClusterToConnect cluster) {
        this.cluster = cluster
        this.name = name
    }

    ConnectionFromRabbitREST parse() {
        String connection_req_path =  REST_RABBITMQ_CONNECTION_PATH + this.name;
        def connection_req = cluster.get(connection_req_path)
        if (connection_req.status == 200 && connection_req.data != null) {
            properties = connection_req.data
            properties.remove(JSON_RABBITMQ_CONNECTION_NAME)
        }
        return this;
    }

    boolean equals(o) {
        if (this.is(o)) return true
        if (getClass() != o.class) return false

        ConnectionFromRabbitREST that = (ConnectionFromRabbitREST) o

        if (name != that.name) return false

        return true
    }

    int hashCode() {
        return name.hashCode()
    }
}
