package net.echinopsii.ariane.community.plugin.rabbitmq.jsonparser.tools

class RabbitNodeToConnect {
    String name;
    String url;
    String user;
    String password;
    RabbitClusterToConnect cluster;
    HashMap<String, Integer> errors = new HashMap<String, Integer>();
}
