package net.echinopsii.ariane.community.plugin.rabbitmq.jsonparser

import com.rabbitmq.client.Channel
import com.rabbitmq.client.Connection
import com.rabbitmq.client.ConnectionFactory
import groovyx.net.http.RESTClient
import net.echinopsii.ariane.community.plugin.rabbitmq.jsonparser.rabbitTestTools.Rreceiver
import net.echinopsii.ariane.community.plugin.rabbitmq.jsonparser.rabbitTestTools.Rsender
import net.echinopsii.ariane.community.plugin.rabbitmq.jsonparser.tools.RESTClientProvider
import net.echinopsii.ariane.community.plugin.rabbitmq.jsonparser.tools.RabbitClusterToConnect
import net.echinopsii.ariane.community.plugin.rabbitmq.jsonparser.tools.RabbitNodeToConnect
import org.junit.AfterClass
import org.junit.BeforeClass

class RabbitRESTTestSetup {

    static String          hostname
    static RabbitClusterToConnect validCluster
    static RabbitClusterToConnect invalidURLCluster
    static RabbitClusterToConnect invalidAUTHCluster
    static RabbitClusterToConnect stoppedNodeCluster
    static RabbitNodeToConnect    validNode

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


        validNode = new RabbitNodeToConnect("rabbit@"+hostname, "http://localhost:15672/", "guest", "guest")
        Set<RabbitNodeToConnect> vnodes = new HashSet<RabbitNodeToConnect>();
        vnodes.add(validNode);
        validCluster = new RabbitClusterToConnect("rabbit@"+hostname)
        validNode.setCluster(validCluster);


        RabbitNodeToConnect invalidURLNode = new RabbitNodeToConnect("rabbit@toto", "http://toto:15672/", "guest", "guest")
        Set<RabbitNodeToConnect> iurlnodes = new HashSet<RabbitNodeToConnect>();
        iurlnodes.add(invalidURLNode);
        invalidURLCluster = new RabbitClusterToConnect("rabbit@toto");
        invalidURLCluster.setNodes(iurlnodes);
        invalidURLNode.setCluster(invalidURLCluster);


        RabbitNodeToConnect invalidauthNode = new RabbitNodeToConnect("rabbit@"+hostname,"http://localhost:15672/","toto","toto");
        Set<RabbitNodeToConnect> iauthnodes = new HashSet<RabbitNodeToConnect>();
        iauthnodes.add(invalidauthNode);
        invalidAUTHCluster = new RabbitClusterToConnect("rabbit@"+hostname);
        invalidAUTHCluster.setNodes(iauthnodes);
        invalidauthNode.setCluster(invalidAUTHCluster);


        RabbitNodeToConnect stoppedNode = new RabbitNodeToConnect("rabbit@"+hostname, "http://localhost:25672/", "toto", "toto");
        Set<RabbitNodeToConnect> stoppednodes = new HashSet<RabbitNodeToConnect>();
        stoppednodes.add(stoppedNode);
        stoppedNodeCluster = new RabbitClusterToConnect("rabbit@"+hostname);
        stoppedNodeCluster.setNodes(stoppednodes);
        stoppedNode.setCluster(stoppedNodeCluster);

        try {
            rclient = RESTClientProvider.getRESTClientFromCluster(validCluster);

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
}