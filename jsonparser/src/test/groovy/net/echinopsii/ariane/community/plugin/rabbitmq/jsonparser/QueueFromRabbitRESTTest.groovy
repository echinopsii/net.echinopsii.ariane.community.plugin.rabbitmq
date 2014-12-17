package net.echinopsii.ariane.community.plugin.rabbitmq.jsonparser

import net.echinopsii.ariane.community.plugin.rabbitmq.jsonparser.serializable.QueueFromRabbitREST
import net.echinopsii.ariane.community.plugin.rabbitmq.jsonparser.tools.RabbitRESTTools
import org.junit.Test

import static org.junit.Assert.assertTrue

class QueueFromRabbitRESTTest extends RabbitRESTTestSetup {

    @Test
    public void testParser() {
        if (rclient!=null) {
            Map<String, List<String>> queueMap2Vhost = RabbitRESTTools.getQueueNames(validCluster)
            for (String vhostName : queueMap2Vhost.keySet()) {
                List<String> vhostExchangesList = queueMap2Vhost.get(vhostName)
                for (String queueName: vhostExchangesList) {
                    QueueFromRabbitREST rqueue = new QueueFromRabbitREST(queueName, vhostName, validCluster).parse()
                    assertTrue(rqueue.getName().equals(queueName))
                    assertTrue(rqueue.getVhost().equals(vhostName))
                    assertTrue(rqueue.getCluster().equals(validCluster))
                    assertTrue(rqueue.getProperties().size()>0);
                }
            }
        }
    }
}
