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

class RabbitRESTToolsTest {

    static RabbitmqCluster cluster;
    static RESTClient      rclient;

    static String testExchange = "testE"
    static String testQueue    = "testQ"
    static Connection connection;
    static Channel channel;
    static sender
    static receiver

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

            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost("localhost");
            factory.setPort(5672);

            connection = factory.newConnection();

            channel = connection.createChannel();
            channel.exchangeDeclare(testExchange, "direct");
            channel.queueDeclare(testQueue, false, false, true, null);
            channel.queueBind(testQueue, testExchange, testQueue);

            receiver = new Rreceiver(channel, testExchange, testQueue)
            sender = new Rsender(channel, testExchange, testQueue)

            new Thread(receiver).start()
            new Thread(sender).start()

            System.out.println("Local test ready !")

        } catch (Exception e) {
            e.printStackTrace()
            System.err.println("No local rabbit to test");
            rclient = null;
        }

    }

    @AfterClass
    public static void testCleanup() {
        if (rclient!=null) {
            sender.stop()
            receiver.stop()
            channel.close()
            connection.close()
        }
    }

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
