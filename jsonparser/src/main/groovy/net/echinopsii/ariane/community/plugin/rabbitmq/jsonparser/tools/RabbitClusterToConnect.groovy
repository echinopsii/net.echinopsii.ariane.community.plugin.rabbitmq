package net.echinopsii.ariane.community.plugin.rabbitmq.jsonparser.tools

import groovyx.net.http.RESTClient

class RabbitClusterToConnect {
    String name
    HashSet<RabbitNodeToConnect> nodes = new HashSet<RabbitNodeToConnect>()
    HashMap<String, Integer> errors  = new HashMap<String, Integer>()
    RESTClient restCli
    RabbitNodeToConnect nodeOnRESTCli

    public RabbitClusterToConnect(String name) {
        this.name  = name;
    }

    String getName() {
        return name
    }

    RabbitClusterToConnect setName(String name) {
        this.name = name
        return this
    }

    HashSet<RabbitNodeToConnect> getNodes() {
        return nodes
    }

    RabbitClusterToConnect setNodes(HashSet<RabbitNodeToConnect> nodes) {
        this.nodes   = nodes
        return this
    }

    HashMap<String, Integer> getErrors() {
        return errors
    }

    RabbitClusterToConnect setErrors(HashMap<String, Integer> errors) {
        this.errors = errors
        return this
    }

    public RESTClient getRestCli() {
        if (this.restCli==null || RESTClientProvider.checkRabbitRESTClient(this.restCli)!=RESTClientProvider.REST_CLI_NODE_OK)
            this.restCli = RESTClientProvider.getRESTClientFromCluster(this)
        return this.restCli
    }
}
