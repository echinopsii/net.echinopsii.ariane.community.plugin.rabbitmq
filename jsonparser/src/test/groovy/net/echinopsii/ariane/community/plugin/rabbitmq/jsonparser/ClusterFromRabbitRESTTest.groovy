package net.echinopsii.ariane.community.plugin.rabbitmq.jsonparser

import groovyx.net.http.RESTClient
import net.echinopsii.ariane.community.plugin.rabbitmq.directory.model.RabbitmqCluster
import net.echinopsii.ariane.community.plugin.rabbitmq.directory.model.RabbitmqNode
import org.junit.AfterClass
import org.junit.BeforeClass
import org.junit.Test

import static org.junit.Assert.assertNull
import static org.junit.Assert.assertTrue

class ClusterFromRabbitRESTTest {

    static String          hostname;
    static RabbitmqCluster validCluster;
    static RabbitmqCluster invalidURLCluster;
    static RabbitmqCluster invalidAUTHCluster;
    static RabbitmqCluster stoppedNodeCluster;
    static RESTClient      rclient;

    @BeforeClass
    public static void testSetup() {
        BufferedReader cmdReader = new BufferedReader(new InputStreamReader(Runtime.getRuntime().exec("hostname").getInputStream()));
        hostname  = cmdReader.readLine();


        RabbitmqNode validNode = new RabbitmqNode().setIdR(1).setVersionR(1).setNameR("rabbit@"+hostname).setDescriptionR("testing valid rabbit").
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


        System.err.println("rabbit@"+hostname);
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
        } catch (Exception e) {
            System.err.println("No local valid rabbit to test");
            rclient = null;
        }
    }

    @AfterClass
    public static void testCleanup() {

    }

    @Test
    public void parseValidCluster() {
        if (rclient!=null) {
            ClusterFromRabbitREST clu = new ClusterFromRabbitREST(validCluster).parse();
            assertTrue(clu.getName().startsWith("rabbit@"))
            assertTrue(clu.getNodes().size()>=1)
            assertTrue(clu.getRunningNodes().size()>=1)
            assertNull(validCluster.getErrors().get("rabbit@"+hostname));
        }
    }

    @Test
    public void parseInvalidURLCluster() {
        ClusterFromRabbitREST clu = new ClusterFromRabbitREST(invalidURLCluster).parse();
        assertTrue(invalidURLCluster.getErrors().get("rabbit@toto")==RESTClientProviderFromRabbitmqCluster.REST_CLI_NODE_URL_ERROR);
    }

    @Test
    public void parseInvalidAUTHCluster() {
        if (rclient!=null) {
            ClusterFromRabbitREST clu = new ClusterFromRabbitREST(invalidAUTHCluster).parse();
            System.out.println(invalidAUTHCluster.getErrors().toString());
            assertTrue(invalidAUTHCluster.getErrors().get("rabbit@"+hostname)==RESTClientProviderFromRabbitmqCluster.REST_CLI_NODE_AUTH_ERROR);
        }
    }

    @Test
    public void parseStoppedNodeCluster() {
        ClusterFromRabbitREST clu = new ClusterFromRabbitREST(stoppedNodeCluster).parse();
        System.out.println(stoppedNodeCluster.getErrors().toString());
        assertTrue(stoppedNodeCluster.getErrors().get("rabbit@"+hostname)==RESTClientProviderFromRabbitmqCluster.REST_CLI_NODE_NO_RESPONSE);
    }
}
