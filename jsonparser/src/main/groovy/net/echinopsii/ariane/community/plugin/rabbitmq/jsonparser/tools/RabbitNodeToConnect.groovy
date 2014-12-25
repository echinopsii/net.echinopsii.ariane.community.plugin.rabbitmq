package net.echinopsii.ariane.community.plugin.rabbitmq.jsonparser.tools

class RabbitNodeToConnect {
    String name;
    String url;
    String user;
    String password;
    boolean isStatisticsDBNode;
    RabbitClusterToConnect cluster;
    HashMap<String, Integer> errors = new HashMap<String, Integer>();

    public RabbitNodeToConnect(String name, String url, String user, String password) {
        this.name = name
        this.url  = url
        this.user = user
        this.password = password
    }

    String getName() {
        return name
    }

    RabbitNodeToConnect setName(String name) {
        this.name = name
        return this
    }

    String getUrl() {
        return url
    }

    RabbitNodeToConnect setUrl(String url) {
        this.url = url
        return this
    }

    String getUser() {
        return user
    }

    RabbitNodeToConnect setUser(String user) {
        this.user = user
        return this
    }

    String getPassword() {
        return password
    }

    RabbitNodeToConnect setPassword(String password) {
        this.password = password
        return this
    }

    RabbitClusterToConnect getCluster() {
        return cluster
    }

    RabbitNodeToConnect setCluster(RabbitClusterToConnect cluster) {
        this.cluster = cluster
        return this
    }

    HashMap<String, Integer> getErrors() {
        return errors
    }

    RabbitNodeToConnect setErrors(HashMap<String, Integer> errors) {
        this.errors = errors
        return this
    }
}
