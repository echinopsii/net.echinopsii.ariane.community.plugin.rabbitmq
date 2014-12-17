package net.echinopsii.ariane.community.plugin.rabbitmq.jsonparser

import net.echinopsii.ariane.community.plugin.rabbitmq.jsonparser.serializable.ClusterFromRabbitREST
import org.junit.Test

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
        ClusterFromRabbitREST clu = new ClusterFromRabbitREST(invalidURLCluster).parse();
        assertTrue(invalidURLCluster.getErrors().get("rabbit@toto")==RESTClientProviderFromRabbitmqCluster.REST_CLI_NODE_URL_ERROR);
    }

    @Test
    public void parseInvalidAUTHCluster() {
        if (rclient!=null) {
            ClusterFromRabbitREST clu = new ClusterFromRabbitREST(invalidAUTHCluster).parse();
            System.out.println(invalidAUTHCluster.getErrors().toString());
            assertTrue(invalidAUTHCluster.getErrors().get("rabbit@"+hostname)==RESTClientProviderFromRabbitmqCluster.REST_CLI_NODE_AUTH_ERROR);
        }
    }

    @Test
    public void parseStoppedNodeCluster() {
        ClusterFromRabbitREST clu = new ClusterFromRabbitREST(stoppedNodeCluster).parse();
        System.out.println(stoppedNodeCluster.getErrors().toString());
        assertTrue(stoppedNodeCluster.getErrors().get("rabbit@"+hostname)==RESTClientProviderFromRabbitmqCluster.REST_CLI_NODE_NO_RESPONSE);
    }
}
