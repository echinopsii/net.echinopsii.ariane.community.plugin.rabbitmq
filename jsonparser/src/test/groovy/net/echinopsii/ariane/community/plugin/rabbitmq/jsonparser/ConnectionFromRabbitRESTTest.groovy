package net.echinopsii.ariane.community.plugin.rabbitmq.jsonparser

import org.junit.Test

import static org.junit.Assert.assertTrue

class ConnectionFromRabbitRESTTest extends RabbitRESTTestSetup {

    @Test
    public void testParser() {
        if (rclient!=null) {
            for (String connectionName : RabbitRESTTools.getConnectionNames(rclient)) {
                ConnectionFromRabbitREST rconn = new ConnectionFromRabbitREST(connectionName, validCluster).parse()
                assertTrue(rconn.getName().equals(connectionName))
                assertTrue(rconn.getCluster().equals(validCluster))
                assertTrue(rconn.getProperties().size()>0);
            }
        }
    }
}
