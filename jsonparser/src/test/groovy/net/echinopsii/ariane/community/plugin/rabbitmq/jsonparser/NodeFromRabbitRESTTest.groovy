package net.echinopsii.ariane.community.plugin.rabbitmq.jsonparser

import groovyx.net.http.RESTClient
import net.echinopsii.ariane.community.plugin.rabbitmq.directory.model.RabbitmqCluster
import net.echinopsii.ariane.community.plugin.rabbitmq.directory.model.RabbitmqNode
import org.junit.AfterClass
import org.junit.BeforeClass
import org.junit.Test

import static org.junit.Assert.assertTrue

class NodeFromRabbitRESTTest {

    static RabbitmqNode    node;
    static RabbitmqCluster cluster;
    static RESTClient      rclient;

    @BeforeClass
    public static void testSetup() {
        BufferedReader cmdReader = new BufferedReader(new InputStreamReader(Runtime.getRuntime().exec("hostname").getInputStream()));
        String hostname = cmdReader.readLine();

        node = new RabbitmqNode().setIdR(1).setVersionR(1).setNameR("rabbit@"+hostname).setDescriptionR("testing rabbit").
                                            setUrlR("http://localhost:15672/").setUserR("guest").setPasswdR("guest");

        Set<RabbitmqNode> nodes = new HashSet<RabbitmqNode>();
        nodes.add(node);

        cluster = new RabbitmqCluster().setIdR(1).setVersionR(1).setNameR("rabbit@"+hostname).setDescriptionR("testing rabbit").setNodesR(nodes);
        node.setCluster(cluster);

        try {
            rclient = RabbitRESTClient.getRESTClientFromCluster(cluster);
        } catch (Exception e) {
            System.err.println("No local rabbit to test");
            rclient = null;
        }
    }

    @AfterClass
    public static void testCleanup() {

    }

    @Test
    public void testParser() {
        if (rclient!=null) {
            NodeFromRabbitREST rnode = new NodeFromRabbitREST(node.getName(), cluster);
            rnode.parse();
            assertTrue(rnode.getName().equals(node.getName()))
            assertTrue(rnode.getCluster().equals(cluster))
            assertTrue(rnode.getProperties().size()>0);
        }
    }
}
