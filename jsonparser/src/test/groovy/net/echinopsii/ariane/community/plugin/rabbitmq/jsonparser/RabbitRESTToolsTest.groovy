package net.echinopsii.ariane.community.plugin.rabbitmq.jsonparser

import com.rabbitmq.client.Channel
import com.rabbitmq.client.Connection
import com.rabbitmq.client.ConnectionFactory
import groovyx.net.http.RESTClient
import net.echinopsii.ariane.community.plugin.rabbitmq.directory.model.RabbitmqCluster
import net.echinopsii.ariane.community.plugin.rabbitmq.directory.model.RabbitmqNode
import net.echinopsii.ariane.community.plugin.rabbitmq.jsonparser.rabbitTestTools.Rreceiver
import net.echinopsii.ariane.community.plugin.rabbitmq.jsonparser.rabbitTestTools.Rsender
import org.junit.AfterClass
import org.junit.BeforeClass
import org.junit.Test

import static org.junit.Assert.assertTrue

class RabbitRESTToolsTest extends RabbitRESTTestSetup {

    @Test
    public void testConnectionsList() {
        if (rclient!=null) {
            List<String> connectionsList = RabbitRESTTools.getConnectionNames(rclient)
            assertTrue(connectionsList.size() == 1)
        }
    }

    @Test
    public void testChannelsList() {
        if (rclient!=null) {
            List<String> list = RabbitRESTTools.getChannelNames(rclient)
            assertTrue(list.size() == 1)
        }
    }

    @Test
    public void testExchangesList() {
        if (rclient!=null) {
            List<String> list = RabbitRESTTools.getExchangeNames(rclient)
            assertTrue(list.size() == 9)
        }
    }

    @Test
    public void testQueueList() {
        if (rclient!=null) {
            List<String> list = RabbitRESTTools.getQueueNames(rclient)
            assertTrue(list.size()==1)
        }
    }

    @Test
    public void testBindingsList() {
        if (rclient!=null) {
            List<Map<String,String>> list = RabbitRESTTools.getBindings(rclient)
            assertTrue(list.size()==2)
        }
    }

    @Test
    public void testVhostsList() {
        if (rclient!=null) {
            List<String> list = RabbitRESTTools.getVhostNames(rclient)
            assertTrue(list.size()==1)
        }
    }
}
