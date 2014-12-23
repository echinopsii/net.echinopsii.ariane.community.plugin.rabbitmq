package net.echinopsii.ariane.community.plugin.rabbitmq.jsonparser

import net.echinopsii.ariane.community.plugin.rabbitmq.jsonparser.serializable.VhostFromRabbitREST
import net.echinopsii.ariane.community.plugin.rabbitmq.jsonparser.tools.RabbitRESTTools
import org.junit.Test

import static org.junit.Assert.assertTrue

class VhostFromRabbitRESTTest extends RabbitRESTTestSetup {

    @Test
    public void testParser() {
        if (rclient!=null) {
            for (String vhostName : RabbitRESTTools.getVhostNames(validCluster)) {
                VhostFromRabbitREST rvhost = new VhostFromRabbitREST(vhostName, validCluster).parse()
                assertTrue(rvhost.getName().equals(vhostName))
                assertTrue(rvhost.getCluster().equals(validCluster))
                assertTrue(rvhost!=null && rvhost.getProperties().size()>0);
            }
        }
    }
}
