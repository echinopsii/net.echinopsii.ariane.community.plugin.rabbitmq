package net.echinopsii.ariane.community.plugin.rabbitmq.jsonparser.serializable

import net.echinopsii.ariane.community.plugin.rabbitmq.jsonparser.tools.RabbitClusterToConnect

class ChannelFromRabbitREST implements Serializable {

    private transient static final String REST_RABBITMQ_CHANNEL_PATH = "/api/channels/"

    private transient static final String JSON_RABBITMQ_CHANNEL_NAME                         = "name"
    public transient static final String JSON_RABBITMQ_CHANNEL_NODE                          = "node"
    public transient static final String JSON_RABBITMQ_CHANNEL_CONNECTION_DETAILS            = "connection_details"
    public transient static final String JSON_RABBITMQ_CHANNEL_CONNECTION_DETAILS_NAME       = "name"
    public transient static final String JSON_RABBITMQ_CHANNEL_CONNECTION_DETAILS_PEER_PORT  = "peer_port"
    public transient static final String JSON_RABBITMQ_CHANNEL_CONNECTION_DETAILS_PEER_HOST  = "peer_host"
    public transient static final String JSON_RABBITMQ_CHANNEL_CONSUMER_DETAILS              = "consumer_details"
    public transient static final String JSON_RABBITMQ_CHANNEL_CONSUMER_DETAILS_QUEUE        = "queue"
    public transient static final String JSON_RABBITMQ_CHANNEL_CONSUMER_DETAILS_QUEUE_VHOST  = "vhost"
    public transient static final String JSON_RABBITMQ_CHANNEL_CONSUMER_DETAILS_QUEUE_NAME   = "name"
    public transient static final String JSON_RABBITMQ_CHANNEL_CONSUMER_DETAILS_CONSUMER_TAG = "consumer_tag"
    public transient static final String JSON_RABBITMQ_CHANNEL_PUBLISHES                     = "publishes"
    public transient static final String JSON_RABBITMQ_CHANNEL_PUBLISHES_EXCHANGE            = "exchange"
    public transient static final String JSON_RABBITMQ_CHANNEL_PUBLISHES_EXCHANGE_VHOST      = "vhost"
    public transient static final String JSON_RABBITMQ_CHANNEL_PUBLISHES_EXCHANGE_NAME       = "name"


    transient RabbitClusterToConnect cluster;

    String name
    Map<String, Object> properties

    ChannelFromRabbitREST(String name, RabbitClusterToConnect cluster) {
        this.name = name
        this.cluster = cluster
    }

    ChannelFromRabbitREST parse() {
        String channel_req_path =  REST_RABBITMQ_CHANNEL_PATH + this.name;
        def channel_req = cluster.get(channel_req_path)
        if (channel_req.status == 200 && channel_req.data != null) {
            properties = channel_req.data
            properties.remove(JSON_RABBITMQ_CHANNEL_NAME)
        }

        return this
    }

    boolean equals(o) {
        if (this.is(o)) return true
        if (getClass() != o.class) return false

        ChannelFromRabbitREST that = (ChannelFromRabbitREST) o

        if (name != that.name) return false

        return true
    }

    int hashCode() {
        return name.hashCode()
    }
}
