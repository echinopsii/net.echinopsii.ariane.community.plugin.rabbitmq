package net.echinopsii.ariane.community.plugin.rabbitmq.jsonparser

import groovyx.net.http.RESTClient
import net.echinopsii.ariane.community.plugin.rabbitmq.directory.model.RabbitmqCluster
import net.echinopsii.ariane.community.plugin.rabbitmq.directory.model.RabbitmqNode
import org.junit.AfterClass
import org.junit.BeforeClass
import org.junit.Test

import static org.junit.Assert.assertTrue

class RESTClientProviderFromRabbitmqClusterTest {

    static RabbitmqCluster cluster;
    static RESTClient      rclient;

    @BeforeClass
    public static void testSetup() {
        BufferedReader cmdReader = new BufferedReader(new InputStreamReader(Runtime.getRuntime().exec("hostname").getInputStream()));
        String hostname = cmdReader.readLine();

        RabbitmqNode node = new RabbitmqNode().setIdR(1).setVersionR(1).setNameR("rabbit@"+hostname).setDescriptionR("testing rabbit").
                                               setUrlR("http://localhost:15672/").setUserR("guest").setPasswdR("guest");

        Set<RabbitmqNode> nodes = new HashSet<RabbitmqNode>();
        nodes.add(node);

        cluster = new RabbitmqCluster().setIdR(1).setVersionR(1).setNameR("rabbit@"+hostname).setDescriptionR("testing rabbit").setNodesR(nodes);
        node.setCluster(cluster);
        try {
            rclient = RESTClientProviderFromRabbitmqCluster.getRESTClientFromCluster(cluster);
        } catch (Exception e) {
            System.err.println("No local rabbit to test");
            rclient = null;
        }

    }

    @AfterClass
    public static void testCleanup() {

    }

    @Test
    public void test() {
        if (rclient!=null) {
            def test = rclient.get(path : '/api/overview')
            assertTrue(test.status == 200);
        }
    }
}
