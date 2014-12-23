package net.echinopsii.ariane.community.plugin.rabbitmq.jsonparser

import net.echinopsii.ariane.community.plugin.rabbitmq.jsonparser.serializable.ChannelFromRabbitREST
import net.echinopsii.ariane.community.plugin.rabbitmq.jsonparser.tools.RabbitRESTTools
import org.junit.Test

import static org.junit.Assert.assertTrue

class ChannelFromRabbitRESTTest extends RabbitRESTTestSetup {

    @Test
    public void testParser() {
        if (rclient!=null) {
            for (String channelName : RabbitRESTTools.getChannelNames(validCluster)) {
                ChannelFromRabbitREST rchan = new ChannelFromRabbitREST(channelName, validCluster).parse()
                assertTrue(rchan.getName().equals(channelName))
                assertTrue(rchan.getCluster().equals(validCluster))
                assertTrue(rchan.getProperties()!=null && rchan.getProperties().size()>0);
            }
        }
    }
}
