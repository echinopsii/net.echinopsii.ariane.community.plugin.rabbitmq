package net.echinopsii.ariane.community.plugin.rabbitmq.jsonparser.tools

import groovyx.net.http.HttpResponseException
import org.apache.http.NoHttpResponseException
import org.apache.http.conn.HttpHostConnectException
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class RabbitClusterToConnect {

    private static final Logger log = LoggerFactory.getLogger(RabbitClusterToConnect.class);

    public static final int REST_CLU_INVALID_ID_NAME   = -11;
    public static final int REST_CLU_DEF_NODE_INVALID  = -12;

    String name
    HashSet<RabbitNodeToConnect> nodes = new HashSet<RabbitNodeToConnect>()

    HashMap<String, String> errors = new HashMap<String, String>()

    RabbitNodeToConnect selectedNodeForREST

    public RabbitClusterToConnect(String name) {
        log.debug("[init]new cluster to connect : " + name);
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

    private void selectNodeForREST() {
        for (RabbitNodeToConnect node : this.nodes) {
            int status = node.checkRabbitRESTClient()
            switch(status) {
                case RabbitNodeToConnect.REST_CLI_NODE_OK:
                    // avoid staticDbNode as much as possible for latency purpose
                    if (this.selectedNodeForREST==null || !node.isStatisticsDBNode())
                        this.selectedNodeForREST = node
                    break;
                default:
                    break;
            }
        }

        for (RabbitNodeToConnect node : this.nodes) {
            if (node.getConnectionStatus()!=RabbitNodeToConnect.REST_CLI_NODE_OK)
                this.errors.put(node.getName() + "-" + node.getConnectionStatus(), "node " + node.getName() + " connection problem: " + node.getConnectionProblemDescription())
            if (node.isErrorOnProvidedNodeName())
                this.errors.put(node.getName() + "-" + REST_CLU_DEF_NODE_INVALID, "provided node name is invalid : " + node.getName() +
                        ". Valid node name from target is " + node.getValidNodeNameFromTarget())
            if (node.isErrorOnProvidedClusterName())
                this.errors.put(this.name + "-" + REST_CLU_INVALID_ID_NAME, "provided cluster name is invalid : " + this.name +
                        ". Valid cluster name from target is : " + node.getValidClusterNameFromTarget())
        }
    }

    RabbitClusterToConnect setNodesAndSelectOneForREST(HashSet<RabbitNodeToConnect> nodes) {
        for (RabbitNodeToConnect node : nodes)
            node.setCluster(this)
        this.nodes   = nodes
        this.selectNodeForREST()
        return this
    }

    public RabbitNodeToConnect getSelectedNodeForREST() {
        if (this.selectedNodeForREST==null || this.selectedNodeForREST.checkRabbitRESTClient()!=RabbitNodeToConnect.REST_CLI_NODE_OK)
            this.selectNodeForREST()
        return this.selectedNodeForREST
    }

    private void retryGetOnException(Exception exp, String path, int maxRetry) {
        log.warn("Exception " + exp.getMessage() + "raise while getting " + path)
        log.warn("Retry get " + path)
        this.selectNodeForREST()
        this.getMaxRetry(path, --maxRetry)
    }

    private Object getMaxRetry(String path, int maxRetry) {
        Object ret = null;

        if (this.selectedNodeForREST==null)
            this.selectNodeForREST()

        if (this.selectedNodeForREST!=null) {
            try {
                ret = this.selectedNodeForREST.getRestCli().get(path: path)
            } catch (UnknownHostException urlpb) {
                this.retryGetOnException(urlpb, path, maxRetry)
            } catch (NoHttpResponseException noHttpResponseException) {
                this.retryGetOnException(noHttpResponseException, path, maxRetry)
            } catch (HttpHostConnectException httpHostConnectException) {
                this.retryGetOnException(httpHostConnectException, path, maxRetry)
            } catch (HttpResponseException httpResponseException) {
                this.retryGetOnException(httpResponseException, path, maxRetry)
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
