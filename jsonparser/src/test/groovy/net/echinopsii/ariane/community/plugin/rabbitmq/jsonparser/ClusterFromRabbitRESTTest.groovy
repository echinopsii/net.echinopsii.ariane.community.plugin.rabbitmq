package net.echinopsii.ariane.community.plugin.rabbitmq.jsonparser

import net.echinopsii.ariane.community.plugin.rabbitmq.jsonparser.serializable.ClusterFromRabbitREST
import net.echinopsii.ariane.community.plugin.rabbitmq.jsonparser.tools.RabbitNodeToConnect
import org.junit.Test

import static org.junit.Assert.assertNotNull
import static org.junit.Assert.assertNull
import static org.junit.Assert.assertTrue

class ClusterFromRabbitRESTTest extends RabbitRESTTestSetup {

    @Test
    public void parseValidCluster() {
        if (rclient!=null) {
            ClusterFromRabbitREST clu = new ClusterFromRabbitREST(validCluster).parse();
            assertTrue(clu.getName().startsWith("rabbit@"))
            assertTrue(clu.getNodes().size()>=1)
            assertTrue(clu.getRunningNodes().size()>=1)
            assertNull(validCluster.getErrors().get("rabbit@"+hostname));
        }
    }

    @Test
    public void parseInvalidURLCluster() {
        new ClusterFromRabbitREST(invalidURLCluster).parse();
        assertNotNull(invalidURLCluster.getErrors().get("rabbit@toto-"+RabbitNodeToConnect.REST_CLI_NODE_URL_ERROR));
    }

    @Test
    public void parseInvalidAUTHCluster() {
        if (rclient!=null) {
            new ClusterFromRabbitREST(invalidAUTHCluster).parse();
            assertNotNull(invalidAUTHCluster.getErrors().get("rabbit@"+hostname+"-"+RabbitNodeToConnect.REST_CLI_NODE_AUTH_ERROR));
        }
    }

    @Test
    public void parseStoppedNodeCluster() {
        new ClusterFromRabbitREST(stoppedNodeCluster).parse();
        assertNotNull(stoppedNodeCluster.getErrors().get("rabbit@"+hostname+"-"+RabbitNodeToConnect.REST_CLI_NODE_NO_RESPONSE));
    }
}
