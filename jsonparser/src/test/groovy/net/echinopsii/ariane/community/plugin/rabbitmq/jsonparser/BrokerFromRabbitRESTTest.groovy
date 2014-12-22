package net.echinopsii.ariane.community.plugin.rabbitmq.jsonparser

import net.echinopsii.ariane.community.plugin.rabbitmq.jsonparser.serializable.BrokerFromRabbitREST
import org.junit.Test

import static org.junit.Assert.assertTrue

class BrokerFromRabbitRESTTest extends RabbitRESTTestSetup {

    @Test
    public void testParser() {
        if (rclient!=null) {
            BrokerFromRabbitREST rnode = new BrokerFromRabbitREST(validNode.getName(), validCluster);
            rnode.parse();
            assertTrue(rnode.getName().equals(validNode.getName()))
            assertTrue(rnode.getCluster().equals(validCluster))
            assertTrue(rnode.getProperties().size()>0);
        }
    }
}
