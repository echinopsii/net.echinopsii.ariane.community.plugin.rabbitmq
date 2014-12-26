package net.echinopsii.ariane.community.plugin.rabbitmq.jsonparser.tools

import groovyx.net.http.HttpResponseException
import groovyx.net.http.RESTClient
import org.apache.http.NoHttpResponseException
import org.apache.http.conn.HttpHostConnectException
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class RabbitClusterToConnect {

    private static final Logger log = LoggerFactory.getLogger(RabbitClusterToConnect.class);

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

    RabbitClusterToConnect setNodesAndDefineRESTCli(HashSet<RabbitNodeToConnect> nodes) {
        this.nodes   = nodes
        this.restCli = RESTClientProvider.getRESTClientFromCluster(this)
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
        if (this.restCli==null || RESTClientProvider.checkRabbitRESTClient(this.restCli, this.nodeOnRESTCli)!=RESTClientProvider.REST_CLI_NODE_OK)
            this.restCli = RESTClientProvider.getRESTClientFromCluster(this)
        return this.restCli
    }

    public RabbitNodeToConnect getNodeOnRESTCli() {
        if (this.nodeOnRESTCli==null || this.restCli==null || RESTClientProvider.checkRabbitRESTClient(this.restCli, this.nodeOnRESTCli)!=RESTClientProvider.REST_CLI_NODE_OK)
            this.restCli = RESTClientProvider.getRESTClientFromCluster(this)
        return this.nodeOnRESTCli
    }

    private void retryGetOnException(Exception exp, String path, int maxRetry) {
        log.warn("Exception " + exp.getMessage() + "raise while getting " + path)
        log.warn("Retry get " + path)
        this.restCli = RESTClientProvider.getRESTClientFromCluster(this)
        this.getMaxRetry(path, --maxRetry)
    }

    private Object getMaxRetry(String path, int maxretry) {
        Object ret = null;

        if (this.restCli==null)
            this.restCli = RESTClientProvider.getRESTClientFromCluster(this)

        if (this.restCli!=null) {
            try {
                ret = this.restCli.get(path: path)
            } catch (UnknownHostException urlpb) {
                this.retryGetOnException(urlpb, path, maxretry)
            } catch (NoHttpResponseException noHttpResponseException) {
                this.retryGetOnException(noHttpResponseException, path, maxretry)
            } catch (HttpHostConnectException httpHostConnectException) {
                this.retryGetOnException(httpHostConnectException, path, maxretry)
            } catch (HttpResponseException httpResponseException) {
                this.retryGetOnException(httpResponseException, path, maxretry)
            } catch (Exception e) {
                ret = null
                e.printStackTrace()
            }
        } else
            log.error("No available REST node on cluster " + this.name)
        return ret;
    }

    public Object get(String path) {
        return getMaxRetry(path, this.getNodes().size());
    }
}
