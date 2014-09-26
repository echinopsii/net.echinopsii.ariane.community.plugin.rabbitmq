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

class RabbitRESTTestSetup {

    static String          hostname
    static RabbitmqCluster validCluster
    static RabbitmqCluster invalidURLCluster
    static RabbitmqCluster invalidAUTHCluster
    static RabbitmqCluster stoppedNodeCluster
    static RabbitmqNode    validNode

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
        hostname  = cmdReader.readLine();


        validNode = new RabbitmqNode().setIdR(1).setVersionR(1).setNameR("rabbit@"+hostname).setDescriptionR("testing valid rabbit").
                                       setUrlR("http://localhost:15672/").setUserR("guest").setPasswdR("guest");
        Set<RabbitmqNode> vnodes = new HashSet<RabbitmqNode>();
        vnodes.add(validNode);
        validCluster = new RabbitmqCluster().setIdR(1).setVersionR(1).setNameR("rabbit@"+hostname).setDescriptionR("testing rabbit").setNodesR(vnodes);
        validNode.setCluster(validCluster);


        RabbitmqNode invalidURLNode = new RabbitmqNode().setIdR(1).setVersionR(1).setNameR("rabbit@toto").setDescriptionR("testing invalid URL rabbit").
                                                         setUrlR("http://toto:15672/").setUserR("guest").setPasswdR("guest");
        Set<RabbitmqNode> iurlnodes = new HashSet<RabbitmqNode>();
        iurlnodes.add(invalidURLNode);
        invalidURLCluster = new RabbitmqCluster().setIdR(1).setVersionR(1).setNameR("rabbit@toto").setDescriptionR("testing invalid URL rabbit").setNodesR(iurlnodes);
        invalidURLNode.setCluster(invalidURLCluster);


        RabbitmqNode invalidauthNode = new RabbitmqNode().setIdR(1).setVersionR(1).setNameR("rabbit@"+hostname).setDescriptionR("testing invalid AUTH rabbit").
                                                          setUrlR("http://localhost:15672/").setUserR("toto").setPasswdR("toto");
        Set<RabbitmqNode> iauthnodes = new HashSet<RabbitmqNode>();
        iauthnodes.add(invalidauthNode);
        invalidAUTHCluster = new RabbitmqCluster().setIdR(1).setVersionR(1).setNameR("rabbit@"+hostname).setDescriptionR("testing invalid AUTH rabbit").setNodesR(iauthnodes);
        invalidauthNode.setCluster(invalidAUTHCluster);


        RabbitmqNode stoppedNode = new RabbitmqNode().setIdR(1).setVersionR(1).setNameR("rabbit@"+hostname).setDescriptionR("testing stopped rabbit").
                                                      setUrlR("http://localhost:25672/").setUserR("toto").setPasswdR("toto");
        Set<RabbitmqNode> stoppednodes = new HashSet<RabbitmqNode>();
        stoppednodes.add(stoppedNode);
        stoppedNodeCluster = new RabbitmqCluster().setIdR(1).setVersionR(1).setNameR("rabbit@"+hostname).setDescriptionR("testing invalid AUTH rabbit").setNodesR(stoppednodes);
        stoppedNode.setCluster(stoppedNodeCluster);

        try {
            rclient = RESTClientProviderFromRabbitmqCluster.getRESTClientFromCluster(validCluster);

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
    public void testOverview() {
        if (rclient!=null) {
            def test = rclient.get(path : '/api/overview')
            assertTrue(test.status == 200);
        }
    }
}