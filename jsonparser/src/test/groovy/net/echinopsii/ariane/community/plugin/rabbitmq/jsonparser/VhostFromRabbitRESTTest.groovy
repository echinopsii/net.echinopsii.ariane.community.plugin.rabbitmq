package net.echinopsii.ariane.community.plugin.rabbitmq.jsonparser

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
                assertTrue(rvhost.getProperties().size()>0);
            }
        }
    }
}
