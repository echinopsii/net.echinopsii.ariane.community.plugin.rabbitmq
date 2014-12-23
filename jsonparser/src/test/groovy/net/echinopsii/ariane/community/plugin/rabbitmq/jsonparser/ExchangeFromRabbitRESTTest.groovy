package net.echinopsii.ariane.community.plugin.rabbitmq.jsonparser

import net.echinopsii.ariane.community.plugin.rabbitmq.jsonparser.serializable.ExchangeFromRabbitREST
import net.echinopsii.ariane.community.plugin.rabbitmq.jsonparser.tools.RabbitRESTTools
import org.junit.Test

import static org.junit.Assert.assertTrue

class ExchangeFromRabbitRESTTest extends RabbitRESTTestSetup {

    @Test
    public void testParser() {
        if (rclient!=null) {
            Map<String, List<String>> exchangeMap2Vhost = RabbitRESTTools.getExchangeNames(validCluster)
            for (String vhostName : exchangeMap2Vhost.keySet()) {
                List<String> vhostExchangesList = exchangeMap2Vhost.get(vhostName)
                for (String exchangeName: vhostExchangesList) {
                    ExchangeFromRabbitREST rexchange = new ExchangeFromRabbitREST(exchangeName, vhostName, validCluster).parse()
                    assertTrue(rexchange.getName().equals(exchangeName))
                    assertTrue(rexchange.getVhost().equals(vhostName))
                    assertTrue(rexchange.getCluster().equals(validCluster))
                    assertTrue(rexchange.getProperties()!=null && rexchange.getProperties().size()>0);
                }
            }
        }
    }
}
