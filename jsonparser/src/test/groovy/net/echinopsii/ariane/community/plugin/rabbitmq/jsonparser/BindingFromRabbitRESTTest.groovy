package net.echinopsii.ariane.community.plugin.rabbitmq.jsonparser

import net.echinopsii.ariane.community.plugin.rabbitmq.jsonparser.serializable.BindingFromRabbitREST
import net.echinopsii.ariane.community.plugin.rabbitmq.jsonparser.tools.RabbitRESTTools
import org.junit.Test

import static org.junit.Assert.assertTrue

class BindingFromRabbitRESTTest extends RabbitRESTTestSetup {

    @Test
    public void testParser() {
        if (rclient!=null) {
            Map<String, List<String>> bindingMap2Vhost = RabbitRESTTools.getBindingNames(validCluster)
            for (String vhostName : bindingMap2Vhost.keySet()) {
                List<String> vhostExchangesList = bindingMap2Vhost.get(vhostName)
                for (String bindingName: vhostExchangesList) {
                    BindingFromRabbitREST rbinding = new BindingFromRabbitREST(bindingName, vhostName, validCluster).parse()
                    assertTrue(rbinding.getName().equals(bindingName))
                    assertTrue(rbinding.getVhost().equals(vhostName))
                    assertTrue(rbinding.getCluster().equals(validCluster))
                    assertTrue(rbinding.getProperties().size()>0);
                }
            }
        }
    }
}
