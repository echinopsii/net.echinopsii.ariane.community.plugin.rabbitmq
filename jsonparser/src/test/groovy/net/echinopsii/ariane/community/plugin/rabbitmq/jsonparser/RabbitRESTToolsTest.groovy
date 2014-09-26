package net.echinopsii.ariane.community.plugin.rabbitmq.jsonparser

import org.junit.Test

import static org.junit.Assert.assertTrue

class RabbitRESTToolsTest extends RabbitRESTTestSetup {

    @Test
    public void testConnectionsList() {
        if (rclient!=null) {
            List<String> connectionsList = RabbitRESTTools.getConnectionNames(validCluster)
            assertTrue(connectionsList.size() == 1)
        }
    }

    @Test
    public void testChannelsList() {
        if (rclient!=null) {
            List<String> list = RabbitRESTTools.getChannelNames(validCluster)
            assertTrue(list.size() == 1)
        }
    }

    @Test
    public void testExchangesList() {
        if (rclient!=null) {
            List<String> list = RabbitRESTTools.getExchangeNames(validCluster)
            assertTrue(list.size() == 9)
        }
    }

    @Test
    public void testQueueList() {
        if (rclient!=null) {
            List<String> list = RabbitRESTTools.getQueueNames(validCluster)
            assertTrue(list.size()==1)
        }
    }

    @Test
    public void testBindingsList() {
        if (rclient!=null) {
            List<Map<String,String>> list = RabbitRESTTools.getBindings(validCluster)
            assertTrue(list.size()==2)
        }
    }

    @Test
    public void testVhostsList() {
        if (rclient!=null) {
            List<String> list = RabbitRESTTools.getVhostNames(validCluster)
            assertTrue(list.size()==1)
        }
    }
}
