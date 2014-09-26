package net.echinopsii.ariane.community.plugin.rabbitmq.jsonparser

import org.junit.Test

import static org.junit.Assert.assertTrue

class NodeFromRabbitRESTTest extends RabbitRESTTestSetup {

    @Test
    public void testParser() {
        if (rclient!=null) {
            NodeFromRabbitREST rnode = new NodeFromRabbitREST(validNode.getName(), validCluster);
            rnode.parse();
            assertTrue(rnode.getName().equals(validNode.getName()))
            assertTrue(rnode.getCluster().equals(validCluster))
            assertTrue(rnode.getProperties().size()>0);
        }
    }
}
