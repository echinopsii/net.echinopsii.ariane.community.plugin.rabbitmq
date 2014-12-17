package net.echinopsii.ariane.community.plugin.rabbitmq.jsonparser.tools

class RabbitClusterToConnect {
    String name;
    HashSet<RabbitNodeToConnect> nodes = new HashSet<RabbitNodeToConnect>();
    HashMap<String, Integer> errors = new HashMap<String, Integer>();
}
