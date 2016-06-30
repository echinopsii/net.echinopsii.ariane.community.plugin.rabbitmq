/**
 * RabbitMQ plugin injector bundle
 * RabbitMQ plugin injector RabbitMQ mapping actor
 * Copyright (C) 2014 Mathilde Ffrench
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package net.echinopsii.ariane.community.plugin.rabbitmq.injector.runtime.actors;

import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.japi.Creator;
import net.echinopsii.ariane.community.core.mapping.ds.MappingDSException;
import net.echinopsii.ariane.community.core.mapping.ds.domain.*;
import net.echinopsii.ariane.community.core.mapping.ds.service.tools.Session;
import net.echinopsii.ariane.community.plugin.rabbitmq.directory.RabbitmqDirectoryService;
import net.echinopsii.ariane.community.plugin.rabbitmq.injector.RabbitmqInjectorBootstrap;
import net.echinopsii.ariane.community.plugin.rabbitmq.injector.cache.RabbitmqCachedComponent;
import net.echinopsii.ariane.community.plugin.rabbitmq.injector.runtime.gears.MappingGear;
import net.echinopsii.ariane.community.plugin.rabbitmq.jsonparser.serializable.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

@SuppressWarnings("ALL")
public class MappingActor extends UntypedActor {

    private static final Logger log = LoggerFactory.getLogger(MappingActor.class);

    private MappingGear gear;

    public static Props props(final MappingGear gear) {
        return Props.create(new Creator<MappingActor>() {
            private static final long serialVersionUID = 1L;

            @Override
            public MappingActor create() throws Exception {
                return new MappingActor(gear);
            }
        });
    }

    public MappingActor(MappingGear gear) {
        this.gear = gear;
    }

    private static final String RABBITMQ_COMPANY  = "Pivotal";
    private static final String RABBITMQ_PRODUCT  = "RabbitMQ";

    private static final String RABBITMQ_BROKER_NAME_KEY     = "name";
    private static final String RABBITMQ_BROKER_LISTENER_KEY = "listener";
    private static final String RABBITMQ_BROKER_CLUSTERING_P = "clustering";

    private static final String RABBITMQ_TRANSPORT_TCP_CLUSTER = "tcp-rbq-clustering://";
    private static final String RABBITMQ_TRANSPORT_TCP_AMQP    = "tcp-rbq-amqp://";
    private static final String RABBITMQ_TRANSPORT_TCP_MQTT    = "tcp-rbq-mqtt://";
    private static final String RABBITMQ_TRANSPORT_TCP_STOMP   = "tcp-rbq-stomp://";
    private static final String RABBITMQ_TRANSPORT_SSL_AMQP    = "ssl-rbq-amqp://";
    private static final String RABBITMQ_TRANSPORT_SSL_MQTT    = "ssl-rbq-mqtt://";
    private static final String RABBITMQ_TRANSPORT_SSL_STOMP   = "ssl-rbq-stomp://";
    private static final String RABBITMQ_TRANSPORT_MEM_BINDING = "mem-rbq-binding://";

    private void applyDifferencesOnVHost(Container container, VhostFromRabbitREST lastVHost, Set<String> deletedVHTs) throws MappingDSException {
        Node nodeToDelete = RabbitmqInjectorBootstrap.getMappingSce().getNodeByName(container, lastVHost.getName());
        if (nodeToDelete!=null) {
            try {
                log.debug("Deleting VHost node ({},{})", new Object[]{container.getContainerID(), lastVHost.getName()});
                RabbitmqInjectorBootstrap.getMappingSce().getNodeSce().deleteNode(nodeToDelete.getNodeID());
                deletedVHTs.add(lastVHost.getName());
            } catch (MappingDSException e) {
                log.error("Error raised while deleting VHost node ({},{})... Continue", new Object[]{container.getContainerID(), lastVHost.getName()});
                e.printStackTrace();
            }
        } else log.error("VHost node ({},{}) doesn't exist.", new Object[]{container.getContainerID(), lastVHost.getName()});
    }

    private void applyDifferencesOnQueues(Container container, QueueFromRabbitREST lastQueue, Set<String> deletedQ) throws MappingDSException {
        Node vhostNode = RabbitmqInjectorBootstrap.getMappingSce().getNodeByName(container, lastQueue.getVhost());
        if (vhostNode != null) {
            Node nodeToDelete = RabbitmqInjectorBootstrap.getMappingSce().getNodeSce().getNodeByName(vhostNode, lastQueue.getName() + " (queue)");
            if (nodeToDelete != null) {
                try {
                    log.debug("Deleting queue node ({}/{},{})", new Object[]{container.getContainerID(), lastQueue.getVhost(), lastQueue.getName()});
                    RabbitmqInjectorBootstrap.getMappingSce().getNodeSce().deleteNode(nodeToDelete.getNodeID());
                    deletedQ.add(lastQueue.getName());
                } catch (MappingDSException e) {
                    log.error("Error raised while deleting queue node ({}/{},{})... Continue", new Object[]{container.getContainerID(), lastQueue.getVhost(), lastQueue.getName()});
                    e.printStackTrace();
                }
            } else log.error("Deleting queue {} in ({}/{}): doesn't exist.", new Object[]{lastQueue.getName(), container.getContainerID(), lastQueue.getVhost()});
        } else log.error("Deleting queue {} : vhost {} doesn't exist.", new Object[]{lastQueue.getName(), lastQueue.getVhost()});
    }

    private void applyDifferencesOnExchanges(Container container, ExchangeFromRabbitREST lastExchange, Set<String> deletedExchange) throws MappingDSException {
        Node vhostNode = RabbitmqInjectorBootstrap.getMappingSce().getNodeByName(container, lastExchange.getVhost());
        if (vhostNode != null) {
            Node nodeToDelete = RabbitmqInjectorBootstrap.getMappingSce().getNodeSce().getNodeByName(vhostNode, lastExchange.getName() + " (exchange)");
            if (nodeToDelete!=null) {
                try {
                    log.debug("Deleting exchange node ({}/{},{})",
                                     new Object[]{container.getContainerID(), lastExchange.getVhost(), lastExchange.getName()});
                    RabbitmqInjectorBootstrap.getMappingSce().getNodeSce().deleteNode(nodeToDelete.getNodeID());
                    deletedExchange.add(lastExchange.getName());
                } catch (MappingDSException e) {
                    log.error("Error raised while deleting exchange node ({}/{},{})... Continue",
                                     new Object[]{container.getContainerID(), lastExchange.getVhost(), lastExchange.getName()});
                    e.printStackTrace();
                }
            } else log.error("Deleting exchange {} in ({}/{}): doesn't exist.",
                                    new Object[]{lastExchange.getName(), container.getContainerID(), lastExchange.getVhost()});
        }
    }

    private void applyDifferencesOnBindings(Container container, BindingFromRabbitREST lastBinding,Set<String> deletedQ,Set<String> deletedExchange,
                                            String bindingDestinationType, String destination, String  exchangeSrc, String routingKey) throws MappingDSException {
        Node vhostNode = RabbitmqInjectorBootstrap.getMappingSce().getNodeByName(container, lastBinding.getVhost());
        if (bindingDestinationType.equals(BindingFromRabbitREST.RABBITMQ_BINDING_DESTINATION_TYPE_Q)) {
            if (!deletedQ.contains(destination) && !deletedExchange.contains(exchangeSrc)) {
                Node sourceNode = RabbitmqInjectorBootstrap.getMappingSce().getNodeSce().getNodeByName(vhostNode, exchangeSrc + " (exchange)");
                Node destNode = RabbitmqInjectorBootstrap.getMappingSce().getNodeSce().getNodeByName(vhostNode, destination + " (queue)");

                if (destNode != null && sourceNode != null) {
                    String exchangeType = (String) sourceNode.getNodeProperties().get(ExchangeFromRabbitREST.JSON_RABBITMQ_EXCHANGE_TYPE);
                    String exchangeSourceEndpointURL = null;
                    String queueTargetEndpointURL = null;

                    if (exchangeType.equals(ExchangeFromRabbitREST.RABBITMQ_EXCHANGE_TYPE_DIRECT)) {
                        exchangeSourceEndpointURL = RABBITMQ_TRANSPORT_MEM_BINDING + sourceNode.getNodeContainer().getContainerProperties().get(RABBITMQ_BROKER_NAME_KEY) + "/" +
                                                            exchangeSrc + "/" + routingKey;
                        queueTargetEndpointURL = RABBITMQ_TRANSPORT_MEM_BINDING + sourceNode.getNodeContainer().getContainerProperties().get(RABBITMQ_BROKER_NAME_KEY) + "/" +
                                                         destination + "/" + routingKey;
                    } else if (exchangeType.equals(ExchangeFromRabbitREST.RABBITMQ_EXCHANGE_TYPE_FANOUT)) {
                        exchangeSourceEndpointURL = RABBITMQ_TRANSPORT_MEM_BINDING + sourceNode.getNodeContainer().getContainerProperties().get(RABBITMQ_BROKER_NAME_KEY) + "/" +
                                                            exchangeSrc;
                        queueTargetEndpointURL = RABBITMQ_TRANSPORT_MEM_BINDING + sourceNode.getNodeContainer().getContainerProperties().get(RABBITMQ_BROKER_NAME_KEY) + "/" +
                                                         destination;
                    } else if (exchangeType.equals(ExchangeFromRabbitREST.RABBITMQ_EXCHANGE_TYPE_TOPIC)) {
                        exchangeSourceEndpointURL = RABBITMQ_TRANSPORT_MEM_BINDING + sourceNode.getNodeContainer().getContainerProperties().get(RABBITMQ_BROKER_NAME_KEY) + "/" +
                                                            exchangeSrc + "/" + routingKey;
                        queueTargetEndpointURL = RABBITMQ_TRANSPORT_MEM_BINDING + sourceNode.getNodeContainer().getContainerProperties().get(RABBITMQ_BROKER_NAME_KEY) + "/" +
                                                         destination + "/" + routingKey;
                    } else if (exchangeType.equals(ExchangeFromRabbitREST.RABBITMQ_EXCHANGE_TYPE_HEADER)) {
                        exchangeSourceEndpointURL = RABBITMQ_TRANSPORT_MEM_BINDING + sourceNode.getNodeContainer().getContainerProperties().get(RABBITMQ_BROKER_NAME_KEY) + "/" +
                                                            exchangeSrc;
                        queueTargetEndpointURL = RABBITMQ_TRANSPORT_MEM_BINDING + sourceNode.getNodeContainer().getContainerProperties().get(RABBITMQ_BROKER_NAME_KEY) + "/" +
                                                         destination;
                    } else log.error("Unknown exchange type : {}", new Object[]{exchangeType});

                    if (exchangeSourceEndpointURL != null) {
                        Endpoint sourceEp = RabbitmqInjectorBootstrap.getMappingSce().getEndpointSce().getEndpointByURL(exchangeSourceEndpointURL);
                        if (sourceEp!=null)
                            try {
                                log.debug("Deleting binding source endpoint ({}/{},{})",
                                                 new Object[]{container.getContainerID(), lastBinding.getVhost(), exchangeSourceEndpointURL});
                                RabbitmqInjectorBootstrap.getMappingSce().getEndpointSce().deleteEndpoint(sourceEp.getEndpointID());
                            } catch (MappingDSException e) {
                                log.error("Error raised whilde deleting binding source endpoint ({}/{},{})... Continue",
                                                 new Object[]{container.getContainerID(), lastBinding.getVhost(), exchangeSourceEndpointURL});
                                e.printStackTrace();
                            }
                        Endpoint targetEp = RabbitmqInjectorBootstrap.getMappingSce().getEndpointSce().getEndpointByURL(queueTargetEndpointURL);
                        if (targetEp!=null)
                            try {
                                log.debug("Deleting binding target endpoint ({}/{},{})",
                                                 new Object[]{container.getContainerID(), lastBinding.getVhost(), queueTargetEndpointURL});
                                RabbitmqInjectorBootstrap.getMappingSce().getEndpointSce().deleteEndpoint(targetEp.getEndpointID());
                            } catch (MappingDSException e) {
                                log.debug("Error raised while deleting binding target endpoint ({}/{},{})... Continue",
                                                 new Object[]{container.getContainerID(), lastBinding.getVhost(), exchangeSourceEndpointURL});
                                e.printStackTrace();
                            }
                    }

                } else {
                    if (sourceNode != null) log.error("Deleting binding {} : destination node {} doesn't exists...", new Object[]{lastBinding.getName(), destination});
                    else if (destNode != null) log.error("Deleting binding {} : source node {} doesn't exists...", new Object[]{lastBinding.getName(), exchangeSrc});
                    else log.error("Deleting binding {} : source and destination nodes ({},{}) doesn't exists...", new Object[]{lastBinding.getName(), exchangeSrc, destination});
                }

            }
        } else if (bindingDestinationType.equals(BindingFromRabbitREST.RABBITMQ_BINDING_DESTINATION_TYPE_E)) {
            if (!deletedExchange.contains(destination) && !deletedExchange.contains(exchangeSrc)) {
                Node sourceNode = RabbitmqInjectorBootstrap.getMappingSce().getNodeSce().getNodeByName(vhostNode, exchangeSrc + " (exchange)");
                Node destNode = RabbitmqInjectorBootstrap.getMappingSce().getNodeSce().getNodeByName(vhostNode, destination + " (exchange)");

                if (destNode != null && sourceNode != null) {
                    String exchangeType = (String) sourceNode.getNodeProperties().get(ExchangeFromRabbitREST.JSON_RABBITMQ_EXCHANGE_TYPE);
                    String exchangeSourceEndpointURL = null;
                    String exchangeTargetEndpointURL = null;

                    if (exchangeType.equals(ExchangeFromRabbitREST.RABBITMQ_EXCHANGE_TYPE_DIRECT)) {
                        exchangeSourceEndpointURL = RABBITMQ_TRANSPORT_MEM_BINDING + sourceNode.getNodeContainer().getContainerProperties().get(RABBITMQ_BROKER_NAME_KEY) + "/" +
                                                            exchangeSrc + "/" + routingKey;
                        exchangeTargetEndpointURL = RABBITMQ_TRANSPORT_MEM_BINDING + sourceNode.getNodeContainer().getContainerProperties().get(RABBITMQ_BROKER_NAME_KEY) + "/" +
                                                            destination + "/" + routingKey;
                    } else if (exchangeType.equals(ExchangeFromRabbitREST.RABBITMQ_EXCHANGE_TYPE_FANOUT)) {
                        exchangeSourceEndpointURL = RABBITMQ_TRANSPORT_MEM_BINDING + sourceNode.getNodeContainer().getContainerProperties().get(RABBITMQ_BROKER_NAME_KEY) + "/" +
                                                            exchangeSrc;
                        exchangeTargetEndpointURL = RABBITMQ_TRANSPORT_MEM_BINDING + sourceNode.getNodeContainer().getContainerProperties().get(RABBITMQ_BROKER_NAME_KEY) + "/" +
                                                            destination;
                    } else if (exchangeType.equals(ExchangeFromRabbitREST.RABBITMQ_EXCHANGE_TYPE_TOPIC)) {
                        exchangeSourceEndpointURL = RABBITMQ_TRANSPORT_MEM_BINDING + sourceNode.getNodeContainer().getContainerProperties().get(RABBITMQ_BROKER_NAME_KEY) + "/" +
                                                            exchangeSrc + "/" + routingKey;
                        exchangeTargetEndpointURL = RABBITMQ_TRANSPORT_MEM_BINDING + sourceNode.getNodeContainer().getContainerProperties().get(RABBITMQ_BROKER_NAME_KEY) + "/" +
                                                            destination + "/" + routingKey;
                    } else if (exchangeType.equals(ExchangeFromRabbitREST.RABBITMQ_EXCHANGE_TYPE_HEADER)) {
                        exchangeSourceEndpointURL = RABBITMQ_TRANSPORT_MEM_BINDING + sourceNode.getNodeContainer().getContainerProperties().get(RABBITMQ_BROKER_NAME_KEY) + "/" +
                                                            exchangeSrc;
                        exchangeTargetEndpointURL = RABBITMQ_TRANSPORT_MEM_BINDING + sourceNode.getNodeContainer().getContainerProperties().get(RABBITMQ_BROKER_NAME_KEY) + "/" +
                                                            destination;
                    } else log.error("Unknown exchange type : {}", new Object[]{exchangeType});

                    if (exchangeSourceEndpointURL != null) {
                        Endpoint sourceEp = RabbitmqInjectorBootstrap.getMappingSce().getEndpointSce().getEndpointByURL(exchangeSourceEndpointURL);
                        if (sourceEp!=null)
                            try {
                                log.debug("Deleting binding source endpoint ({}/{},{})",
                                                 new Object[]{container.getContainerID(), lastBinding.getVhost(), exchangeSourceEndpointURL});
                                RabbitmqInjectorBootstrap.getMappingSce().getEndpointSce().deleteEndpoint(sourceEp.getEndpointID());
                            } catch (MappingDSException e) {
                                log.error("Error raised whilde deleting binding source endpoint ({}/{},{})... Continue",
                                                 new Object[]{container.getContainerID(), lastBinding.getVhost(), exchangeSourceEndpointURL});
                                e.printStackTrace();
                            }
                        Endpoint targetEp = RabbitmqInjectorBootstrap.getMappingSce().getEndpointSce().getEndpointByURL(exchangeTargetEndpointURL);
                        if (targetEp!=null)
                            try {
                                log.debug("Deleting binding target endpoint ({}/{},{})",
                                                 new Object[]{container.getContainerID(), lastBinding.getVhost(), exchangeTargetEndpointURL});
                                RabbitmqInjectorBootstrap.getMappingSce().getEndpointSce().deleteEndpoint(targetEp.getEndpointID());
                            } catch (MappingDSException e) {
                                log.debug("Error raised while deleting binding target endpoint ({}/{},{})... Continue",
                                                 new Object[]{container.getContainerID(), lastBinding.getVhost(), exchangeSourceEndpointURL});
                                e.printStackTrace();
                            }
                    }


                } else {
                    if (sourceNode != null) log.error("Deleting binding {} : destination node {} doesn't exists...", new Object[]{lastBinding.getName(), destination});
                    else if (destNode != null) log.error("Deleting binding {} : source node {} doesn't exists...", new Object[]{lastBinding.getName(), exchangeSrc});
                    else log.error("Deleting binding {} : source and destination nodes ({},{}) doesn't exists...", new Object[]{lastBinding.getName(), exchangeSrc, destination});
                }
            }
        } else log.error("Unknown binding destination type : {}", new Object[]{bindingDestinationType});
    }

    private void applyEntityDifferencesFromLastSniff(RabbitmqCachedComponent entity) throws MappingDSException {
        Set<String> deletedBrk        = new HashSet<>();
        Set<String> deletedVHTs       = new HashSet<>();
        Set<String> deletedQ          = new HashSet<>();
        Set<String> deletedExchange   = new HashSet<>();
        Set<String> deletedConnection = new HashSet<>();

        Cluster cluster          = null;
        Container standaloneNode = null;

        if (!entity.getComponentType().equals(RabbitmqCachedComponent.RABBIT_MQ_CACHED_CMP_TYPE_SNODE)) {
            cluster = RabbitmqInjectorBootstrap.getMappingSce().getClusterSce().getClusterByName(entity.getComponentName());
            if (cluster==null) {
                log.error("Cluster {} doesn't exists... Exit", entity.getComponentName());
                return;
            }

            if (entity.getLastBrokers()!=null) {
                for (BrokerFromRabbitREST lastBroker : entity.getLastBrokers()) {
                    BrokerFromRabbitREST currentBroker = null;
                    for (BrokerFromRabbitREST curBrok : entity.getBrokers())
                        if (curBrok.equals(lastBroker)) {
                            currentBroker = curBrok;
                            break;
                        }

                    if (currentBroker==null) {
                        try {
                            log.debug("Deleting broker ({},{})...", new Object[]{lastBroker.getName(), lastBroker.getUrl()});
                            RabbitmqInjectorBootstrap.getMappingSce().getContainerSce().deleteContainer(lastBroker.getUrl());
                            deletedBrk.add(lastBroker.getName());
                        } catch (MappingDSException e) {
                            log.error("Error raised while deleting broker ({},{})... Continue.", new Object[]{lastBroker.getName(), lastBroker.getUrl()});
                            e.printStackTrace();
                        }
                    }
                }
            }
        } else {
            String adminGateUrl = entity.getBroker().getUrl();
            standaloneNode = RabbitmqInjectorBootstrap.getMappingSce().getContainerSce().getContainerByPrimaryAdminURL(adminGateUrl);
            if (standaloneNode==null){
                log.error("RabbitMQ Node {} doesn't exists... Exit", adminGateUrl);
                return;
            }
        }

        if (entity.getLastVhosts()!=null) {
            for (VhostFromRabbitREST lastVHost : entity.getLastVhosts())  {
                VhostFromRabbitREST currentVHost = null;
                for (VhostFromRabbitREST curVH : entity.getVhosts())
                    if (curVH.equals(lastVHost)) {
                        currentVHost = curVH;
                        break;
                    }

                if (currentVHost==null) {
                    if (!entity.getComponentType().equals(RabbitmqCachedComponent.RABBIT_MQ_CACHED_CMP_TYPE_SNODE))
                        for (Container container : cluster.getClusterContainers())
                            applyDifferencesOnVHost(container, lastVHost, deletedVHTs);
                    else
                        applyDifferencesOnVHost(standaloneNode, lastVHost, deletedVHTs);
                }
            }
        }

        if (entity.getLastQueues()!=null) {
            for (QueueFromRabbitREST lastQueue : entity.getLastQueues()) {
                if (!deletedVHTs.contains(lastQueue.getVhost())) {
                    QueueFromRabbitREST currentQueue = null;
                    for (QueueFromRabbitREST curQ : entity.getQueues())
                        if (curQ.equals(lastQueue)) {
                            currentQueue = curQ;
                            break;
                        }

                    if (currentQueue == null) {
                        if (!entity.getComponentType().equals(RabbitmqCachedComponent.RABBIT_MQ_CACHED_CMP_TYPE_SNODE))
                            for (Container container : cluster.getClusterContainers())
                                applyDifferencesOnQueues(container, lastQueue, deletedQ);
                        else
                            applyDifferencesOnQueues(standaloneNode, lastQueue, deletedQ);
                    }
                } // else VHost has been deleted with the child nodes and so the queue
            }
        }

        if (entity.getLastExchanges()!=null) {
            for (ExchangeFromRabbitREST lastExchange : entity.getLastExchanges()) {
                if (!deletedVHTs.contains(lastExchange.getVhost())) {
                    ExchangeFromRabbitREST currentExchange = null;
                    for (ExchangeFromRabbitREST curEx : entity.getExchanges())
                        if (curEx.equals(lastExchange)) {
                            currentExchange = curEx;
                            break;
                        }

                    if (currentExchange == null) {
                        if (!entity.getComponentType().equals(RabbitmqCachedComponent.RABBIT_MQ_CACHED_CMP_TYPE_SNODE))
                            for (Container container : cluster.getClusterContainers())
                                applyDifferencesOnExchanges(container, lastExchange, deletedExchange);
                        else
                            applyDifferencesOnExchanges(standaloneNode, lastExchange, deletedExchange);
                    }
                }
            } // else VHost has been deleted with the child nodes and so the exchange
        }

        if (entity.getLastBindings()!=null) {
            for (BindingFromRabbitREST lastBinding : entity.getLastBindings()) {
                if (!deletedVHTs.contains(lastBinding.getVhost())) {
                    BindingFromRabbitREST currentBinding = null;
                    for (BindingFromRabbitREST curBind : entity.getBindings())
                        if (curBind.equals(lastBinding)) {
                            currentBinding = curBind;
                            break;
                        }

                    if (currentBinding==null) {
                        String bindingDestinationType = (String) lastBinding.getProperties().get(BindingFromRabbitREST.JSON_RABBITMQ_BINDING_DESTINATION_TYPE);
                        String routingKey = (String) lastBinding.getProperties().get(BindingFromRabbitREST.JSON_RABBITMQ_BINDING_ROUNTING_KEY);
                        String exchangeSrc = (String) lastBinding.getProperties().get(BindingFromRabbitREST.JSON_RABBITMQ_BINDING_SOURCE);
                        String destination = (String) lastBinding.getProperties().get(BindingFromRabbitREST.JSON_RABBITMQ_BINDING_DESTINATION);
                        if (!entity.getComponentType().equals(RabbitmqCachedComponent.RABBIT_MQ_CACHED_CMP_TYPE_SNODE))
                            for (Container container : cluster.getClusterContainers())
                                applyDifferencesOnBindings(container, lastBinding, deletedQ, deletedExchange, bindingDestinationType, destination, exchangeSrc, routingKey);
                        else
                            applyDifferencesOnBindings(standaloneNode, lastBinding, deletedQ, deletedExchange, bindingDestinationType, destination, exchangeSrc, routingKey);
                    }
                }
            }
        }

        if (entity.getLastConnections()!=null) {
            for (ConnectionFromRabbitREST lastConnection : entity.getLastConnections()) {

                String rbqBrokerNodeName = (String)lastConnection.getProperties().get(ConnectionFromRabbitREST.JSON_RABBITMQ_CONNECTION_NODE);
                if (!deletedBrk.contains(rbqBrokerNodeName)) {

                    ConnectionFromRabbitREST currentConnection = null;
                    for (ConnectionFromRabbitREST curConn : entity.getConnections())
                        if (curConn.equals(lastConnection)) {
                            currentConnection = curConn;
                            break;
                        }

                    if (currentConnection == null) {

                        Container rbqBroker = null;
                        if (!entity.getComponentType().equals(RabbitmqCachedComponent.RABBIT_MQ_CACHED_CMP_TYPE_SNODE) && cluster!=null)
                            for (Container broker : cluster.getClusterContainers())
                                if (broker.getContainerProperties().get(RABBITMQ_BROKER_NAME_KEY).equals(rbqBrokerNodeName)) {
                                    rbqBroker = broker;
                                    break;
                                }
                        else
                            rbqBroker = standaloneNode;


                        if (rbqBroker != null) {

                            HashMap<String, Object> connection_client_props = (HashMap) lastConnection.getProperties().get(ConnectionFromRabbitREST.JSON_RABBITMQ_CONNECTION_CLIENT_PROPERTIES);
                            String remoteCliPGURL = (String) connection_client_props.get(ConnectionFromRabbitREST.JSON_RABBITMQ_CONNECTION_CLIENT_PROPERTIES_ARIANE_PGURL);
                            String remoteCliOSI = (String) connection_client_props.get(ConnectionFromRabbitREST.JSON_RABBITMQ_CONNECTION_CLIENT_PROPERTIES_ARIANE_OSI);
                            String remoteCliOTM = (String) connection_client_props.get(ConnectionFromRabbitREST.JSON_RABBITMQ_CONNECTION_CLIENT_PROPERTIES_ARIANE_OTM);
                            String remoteCliAPP = (String) connection_client_props.get(ConnectionFromRabbitREST.JSON_RABBITMQ_CONNECTION_CLIENT_PROPERTIES_ARIANE_APP);
                            String remoteCliCMP = (String) connection_client_props.get(ConnectionFromRabbitREST.JSON_RABBITMQ_CONNECTION_CLIENT_PROPERTIES_ARIANE_CMP);

                            if (remoteCliPGURL != null && remoteCliOSI != null && remoteCliOTM != null && remoteCliAPP != null && remoteCliCMP != null) {
                                String serverName = remoteCliPGURL.split("://")[1].split("\\.")[0];

                                Container rbqClient = RabbitmqInjectorBootstrap.getMappingSce().getContainerSce().getContainerByPrimaryAdminURL(remoteCliPGURL);
                                if (rbqClient != null) {
                                    String protocol = (String) lastConnection.getProperties().get(ConnectionFromRabbitREST.JSON_RABBITMQ_CONNECTION_PROTOCOL);
                                    String transportName = null;
                                    boolean ssl = (Boolean) lastConnection.getProperties().get(ConnectionFromRabbitREST.JSON_RABBITMQ_CONNECTION_SSL);
                                    String peerHost = (String) lastConnection.getProperties().get(ConnectionFromRabbitREST.JSON_RABBITMQ_CONNECTION_PEER_HOST);
                                    int peerPort = (Integer) lastConnection.getProperties().get(ConnectionFromRabbitREST.JSON_RABBITMQ_CONNECTION_PEER_PORT);
                                    String brokerHost = (String) lastConnection.getProperties().get(ConnectionFromRabbitREST.JSON_RABBITMQ_CONNECTION_HOST);
                                    int brokerPort = (Integer) lastConnection.getProperties().get(ConnectionFromRabbitREST.JSON_RABBITMQ_CONNECTION_PORT);

                                    if (protocol.startsWith(ConnectionFromRabbitREST.RABBITMQ_CONNECTION_PROTOCOL_AMQP))
                                        if (ssl)
                                            transportName = RABBITMQ_TRANSPORT_SSL_AMQP;
                                        else
                                            transportName = RABBITMQ_TRANSPORT_TCP_AMQP;

                                    else if (protocol.startsWith(ConnectionFromRabbitREST.RABBITMQ_CONNECTION_PROTOCOL_MQTT))
                                        if (ssl)
                                            transportName = RABBITMQ_TRANSPORT_SSL_MQTT;
                                        else
                                            transportName = RABBITMQ_TRANSPORT_TCP_MQTT;

                                    else if (protocol.startsWith(ConnectionFromRabbitREST.RABBITMQ_CONNECTION_PROTOCOL_STOMP))
                                        if (ssl)
                                            transportName = RABBITMQ_TRANSPORT_SSL_STOMP;
                                        else
                                            transportName = RABBITMQ_TRANSPORT_TCP_STOMP;

                                    else
                                        log.error("Unknown protocol type : {} ", protocol);

                                    if (transportName != null) {
                                        for (ChannelFromRabbitREST lastChannel : entity.getLastChannels()) {
                                            HashMap<String, Object> connectionDetails = (HashMap) lastChannel.getProperties().get(ChannelFromRabbitREST.JSON_RABBITMQ_CHANNEL_CONNECTION_DETAILS);
                                            if (lastConnection.getName().equals(connectionDetails.get(ChannelFromRabbitREST.JSON_RABBITMQ_CHANNEL_CONNECTION_DETAILS_NAME))) {
                                                String channelName = lastChannel.getName();
                                                String channelNumber = channelName.split("\\(")[1].split("\\)")[0];

                                                ArrayList<HashMap<String, Object>> consumers_details = (ArrayList) lastChannel.getProperties().get(ChannelFromRabbitREST.JSON_RABBITMQ_CHANNEL_CONSUMER_DETAILS);
                                                for (HashMap<String, Object> consumerDetails : consumers_details) {
                                                    HashMap<String, Object> queue_details = (HashMap) consumerDetails.get(ChannelFromRabbitREST.JSON_RABBITMQ_CHANNEL_CONSUMER_DETAILS_QUEUE);
                                                    String consumerTag = (String) consumerDetails.get(ChannelFromRabbitREST.JSON_RABBITMQ_CHANNEL_CONSUMER_DETAILS_CONSUMER_TAG);
                                                    String queueName = (String) queue_details.get(ChannelFromRabbitREST.JSON_RABBITMQ_CHANNEL_CONSUMER_DETAILS_QUEUE_NAME);
                                                    String vhostName = (String) queue_details.get(ChannelFromRabbitREST.JSON_RABBITMQ_CHANNEL_CONSUMER_DETAILS_QUEUE_VHOST);

                                                    if (!deletedVHTs.contains(vhostName)) {
                                                        String consumerNodeName = queueName + " consumer";
                                                        String sourceEpUrl = transportName + brokerHost + ":" + brokerPort + "/" + peerHost + ":" + peerPort + "/(" + channelNumber + ")/" + consumerTag;
                                                        String targetEpUrl = transportName + peerHost + ":" + peerPort + "/" + brokerHost + ":" + brokerPort + "/(" + channelNumber + ")/" + consumerTag;

                                                        Node vhostNode = RabbitmqInjectorBootstrap.getMappingSce().getNodeByName(rbqBroker, vhostName);
                                                        if (vhostNode != null) {
                                                            Node consumedQueueNode = RabbitmqInjectorBootstrap.getMappingSce().getNodeSce().getNodeByName(vhostNode, queueName + " (queue)");
                                                            if (consumedQueueNode!=null) {
                                                                Endpoint sourceEP = RabbitmqInjectorBootstrap.getMappingSce().getEndpointSce().getEndpointByURL(sourceEpUrl);
                                                                if (sourceEP != null)
                                                                    try {
                                                                        log.debug("Deleting connection-channel source endpoint ({},{}).",
                                                                                         new Object[]{vhostName, sourceEpUrl});
                                                                        RabbitmqInjectorBootstrap.getMappingSce().getEndpointSce().deleteEndpoint(sourceEP.getEndpointID());
                                                                    } catch (MappingDSException e) {
                                                                        log.error("Error raised while deleting connection-channel source endpoint ({},{})... Continue",
                                                                                         new Object[]{vhostName, sourceEpUrl});
                                                                        e.printStackTrace();
                                                                    }
                                                                else log.error("Source endpoint {} doesn't exists... Continue", sourceEpUrl);
                                                            } else {
                                                                if (!deletedQ.contains(queueName))
                                                                    log.error("Consumed queue {} doesn't exists... Continue", queueName);
                                                            }

                                                            Node consumerNode = RabbitmqInjectorBootstrap.getMappingSce().getNodeByName(rbqClient, consumerNodeName);
                                                            if (consumerNode != null) {
                                                                Endpoint targetEP = RabbitmqInjectorBootstrap.getMappingSce().getEndpointSce().getEndpointByURL(targetEpUrl);
                                                                if (targetEP != null)
                                                                    try {
                                                                        log.debug("Deleting connection-channel target endpoint ({},{}).",
                                                                                         new Object[]{vhostName, targetEpUrl});
                                                                        RabbitmqInjectorBootstrap.getMappingSce().getEndpointSce().deleteEndpoint(targetEP.getEndpointID());
                                                                    } catch (MappingDSException e) {
                                                                        log.error("Error raised while deleting connection-channel target endpoint ({},{})... Continue",
                                                                                         new Object[]{vhostName, targetEpUrl});
                                                                        e.printStackTrace();
                                                                    }

                                                                if (consumerNode.getNodeChildNodes().size() == 0 && consumerNode.getNodeEndpoints().size() == 0)
                                                                    try {
                                                                        log.debug("Deleting consumer node {}.", consumerNodeName);
                                                                        RabbitmqInjectorBootstrap.getMappingSce().getNodeSce().deleteNode(consumerNode.getNodeID());
                                                                    } catch (MappingDSException e) {
                                                                        log.debug("Error raised while deleting consumer node {}... Continue", consumerNodeName);
                                                                        e.printStackTrace();
                                                                    }

                                                                else log.error("Target endpoint {} doesn't exists... Continue", targetEpUrl);

                                                            } else //debug as this node could be removed by injector of the parent container
                                                                log.debug("Client consumer node {} doesn't exists.", consumerNodeName);

                                                        } else log.error("Channel vhost {} node doesn't exits.", vhostName);
                                                    }
                                                }

                                                ArrayList<HashMap<String, Object>> publishes = (ArrayList) lastChannel.getProperties().get(ChannelFromRabbitREST.JSON_RABBITMQ_CHANNEL_PUBLISHES);
                                                for (HashMap<String, Object> publish : publishes) {
                                                    HashMap<String, Object> targetExchange = (HashMap) publish.get(ChannelFromRabbitREST.JSON_RABBITMQ_CHANNEL_PUBLISHES_EXCHANGE);
                                                    String exchangeName = (String) targetExchange.get(ChannelFromRabbitREST.JSON_RABBITMQ_CHANNEL_PUBLISHES_EXCHANGE_NAME);
                                                    String vhostName = (String) targetExchange.get(ChannelFromRabbitREST.JSON_RABBITMQ_CHANNEL_PUBLISHES_EXCHANGE_VHOST);

                                                    if (!deletedVHTs.contains(vhostName)) {
                                                        String publisherNodeName = exchangeName + " publisher";
                                                        String sourceEpUrl = transportName + peerHost + ":" + peerPort + "/" + brokerHost + ":" + brokerPort + "/(" + channelNumber + ")/" + exchangeName;
                                                        String targetEpUrl = transportName + brokerHost + ":" + brokerPort + "/" + peerHost + ":" + peerPort + "/(" + channelNumber + ")";

                                                        Node vhostNode = RabbitmqInjectorBootstrap.getMappingSce().getNodeByName(rbqBroker, vhostName);
                                                        if (vhostNode != null) {
                                                            Node publisherNode = RabbitmqInjectorBootstrap.getMappingSce().getNodeByName(rbqClient, publisherNodeName);
                                                            if (publisherNode != null) {
                                                                Endpoint sourceEP = RabbitmqInjectorBootstrap.getMappingSce().getEndpointSce().getEndpointByURL(sourceEpUrl);
                                                                if (sourceEP != null)
                                                                    try {
                                                                        log.debug("Deleting connection-channel source endpoint ({},{}).",
                                                                                         new Object[]{vhostName, sourceEpUrl});
                                                                        RabbitmqInjectorBootstrap.getMappingSce().getEndpointSce().deleteEndpoint(sourceEP.getEndpointID());
                                                                    } catch (MappingDSException e) {
                                                                        log.error("Error raised while deleting connection-channel source endpoint ({},{})... Continue",
                                                                                         new Object[]{vhostName, sourceEpUrl});
                                                                        e.printStackTrace();
                                                                    }

                                                                else log.error("Source endpoint {} doesn't exists... Continue", sourceEpUrl);

                                                            } else //
                                                                log.debug("Client publisher node {} does't exists", publisherNodeName);

                                                            Node targetExchangeNode = RabbitmqInjectorBootstrap.getMappingSce().getNodeSce().getNodeByName(vhostNode, exchangeName + " (exchange)");
                                                            if (targetExchangeNode!=null) {
                                                                Endpoint targetEP = RabbitmqInjectorBootstrap.getMappingSce().getEndpointSce().getEndpointByURL(targetEpUrl);
                                                                if (targetEP != null)
                                                                    try {
                                                                        log.debug("Deleting connection-channel target endpoint ({},{}).",
                                                                                         new Object[]{vhostName, targetEpUrl});
                                                                        RabbitmqInjectorBootstrap.getMappingSce().getEndpointSce().deleteEndpoint(targetEP.getEndpointID());
                                                                    } catch (MappingDSException e) {
                                                                        log.error("Error raised while deleting connection-channel target endpoint ({},{})... Continue",
                                                                                         new Object[]{vhostName, targetEpUrl});
                                                                        e.printStackTrace();
                                                                    }

                                                                if (publisherNode.getNodeChildNodes().size() == 0 && publisherNode.getNodeEndpoints().size() == 0)
                                                                    try {
                                                                        log.debug("Deleting publisher node {}.", publisherNodeName);
                                                                        RabbitmqInjectorBootstrap.getMappingSce().getNodeSce().deleteNode(publisherNode.getNodeID());
                                                                    } catch (MappingDSException e) {
                                                                        log.debug("Error raised while deleting publisher node {}... Continue", publisherNodeName);
                                                                        e.printStackTrace();
                                                                    }

                                                                else log.error("Target endpoint {} doesn't exists... Continue", targetEpUrl);

                                                            } else log.error("Target exchange {} doesn't exists ... Continue", exchangeName);

                                                        } else log.error("Channel vhost {} node doesn't exits.", vhostName);
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    if (rbqClient.getContainerChildContainers().size() <= 1) {
                                        try {
                                            log.debug("Deleting RabbitMQ client container {}.", remoteCliPGURL);
                                            RabbitmqInjectorBootstrap.getMappingSce().getContainerSce().deleteContainer(remoteCliPGURL);
                                        } catch (MappingDSException e) {
                                            log.debug("Error raised while deleting RabbitMQ client container {}... Continue.", remoteCliPGURL);
                                            e.printStackTrace();
                                        }
                                    }

                                    deletedConnection.add(lastConnection.getName());

                                } else log.error("Client container {} doesn't exists.", remoteCliPGURL);
                            }
                        } else log.error("RabbitMQ broker container {} doesn't exists.", rbqBrokerNodeName);
                    }
                }
            }
        }

        if (entity.getLastChannels()!=null) {
            for (ChannelFromRabbitREST lastChannel : entity.getLastChannels()) {
                HashMap<String, Object> connectionDetails = (HashMap) lastChannel.getProperties().get(ChannelFromRabbitREST.JSON_RABBITMQ_CHANNEL_CONNECTION_DETAILS);
                if (!deletedConnection.contains(connectionDetails.get(ChannelFromRabbitREST.JSON_RABBITMQ_CHANNEL_CONNECTION_DETAILS_NAME))) {
                    ChannelFromRabbitREST currentChannel = null;
                    for (ChannelFromRabbitREST curChan : entity.getChannels()) {
                        if (curChan.equals(lastChannel)) {
                            currentChannel = curChan;
                            break;
                        }
                    }

                    if (currentChannel==null) {
                        String rbqBrokerNodeName = (String)lastChannel.getProperties().get(ChannelFromRabbitREST.JSON_RABBITMQ_CHANNEL_NODE);
                        Container rbqBroker = null;
                        if (!entity.getComponentType().equals(RabbitmqCachedComponent.RABBIT_MQ_CACHED_CMP_TYPE_SNODE) && cluster!=null)
                            for (Container broker : cluster.getClusterContainers()) {
                                if (broker.getContainerProperties().get(RABBITMQ_BROKER_NAME_KEY).equals(rbqBrokerNodeName)) {
                                    rbqBroker = broker;
                                    break;
                                }
                            }
                        else
                            rbqBroker = standaloneNode;

                        Container rbqClientBroker = null;

                        if (rbqBroker != null) {

                            String channelName = lastChannel.getName();
                            String channelNumber = channelName.split("\\(")[1].split("\\)")[0];
                            String channelPeerHost = (String)connectionDetails.get(ChannelFromRabbitREST.JSON_RABBITMQ_CHANNEL_CONNECTION_DETAILS_PEER_HOST);
                            int channelPeerPort = (Integer)connectionDetails.get(ChannelFromRabbitREST.JSON_RABBITMQ_CHANNEL_CONNECTION_DETAILS_PEER_PORT);

                            ArrayList<HashMap<String, Object>> consumers_details = (ArrayList) lastChannel.getProperties().get(ChannelFromRabbitREST.JSON_RABBITMQ_CHANNEL_CONSUMER_DETAILS);
                            for (HashMap<String, Object> consumerDetails : consumers_details) {
                                HashMap<String, Object> queue_details = (HashMap) consumerDetails.get(ChannelFromRabbitREST.JSON_RABBITMQ_CHANNEL_CONSUMER_DETAILS_QUEUE);
                                String consumerTag = (String) consumerDetails.get(ChannelFromRabbitREST.JSON_RABBITMQ_CHANNEL_CONSUMER_DETAILS_CONSUMER_TAG);
                                String queueName = (String) queue_details.get(ChannelFromRabbitREST.JSON_RABBITMQ_CHANNEL_CONSUMER_DETAILS_QUEUE_NAME);
                                String vhostName = (String) queue_details.get(ChannelFromRabbitREST.JSON_RABBITMQ_CHANNEL_CONSUMER_DETAILS_QUEUE_VHOST);

                                if (!deletedVHTs.contains(vhostName)) {
                                    Node vhostNode = RabbitmqInjectorBootstrap.getMappingSce().getNodeByName(rbqBroker, vhostName);
                                    if (vhostNode != null) {
                                        Node queueNode = RabbitmqInjectorBootstrap.getMappingSce().getNodeSce().getNodeByName(vhostNode, vhostName + " (queue)");
                                        if (queueNode!=null) {
                                            Endpoint sourceEndpoint = null;
                                            for (Endpoint endpoint : queueNode.getNodeEndpoints())
                                                if (endpoint.getEndpointURL().contains(channelPeerHost + ":" + channelPeerPort + "/(" + channelNumber + ")/" + consumerTag)) {
                                                    sourceEndpoint = endpoint;
                                                    break;
                                                }

                                            if (sourceEndpoint!=null) {
                                                for (Link link : RabbitmqInjectorBootstrap.getMappingSce().getLinksBySourceEP(sourceEndpoint)) {
                                                    Endpoint targetEndpoint = link.getLinkEndpointTarget();
                                                    if (targetEndpoint!=null) {

                                                        Node consumerNode = targetEndpoint.getEndpointParentNode();
                                                        rbqClientBroker = consumerNode.getNodeContainer();

                                                        try {
                                                            log.debug("Deleting connection-channel source endpoint ({},{}).",
                                                                             new Object[]{vhostName, sourceEndpoint.getEndpointURL()});
                                                            RabbitmqInjectorBootstrap.getMappingSce().getEndpointSce().deleteEndpoint(sourceEndpoint.getEndpointID());
                                                        } catch (MappingDSException e) {
                                                            log.error("Error raised while deleting connection-channel source endpoint ({},{})... Continue",
                                                                             new Object[]{vhostName, sourceEndpoint.getEndpointURL()});
                                                            e.printStackTrace();
                                                        }

                                                        try {
                                                            log.debug("Deleting connection-channel target endpoint ({},{}).",
                                                                             new Object[]{vhostName, targetEndpoint.getEndpointURL()});
                                                            RabbitmqInjectorBootstrap.getMappingSce().getEndpointSce().deleteEndpoint(targetEndpoint.getEndpointID());
                                                        } catch (MappingDSException e) {
                                                            log.error("Error raised while deleting connection-channel target endpoint ({},{})... Continue",
                                                                             new Object[]{vhostName, targetEndpoint.getEndpointURL()});
                                                            e.printStackTrace();
                                                        }

                                                        if (consumerNode.getNodeEndpoints().size()==0 && consumerNode.getNodeChildNodes().size()==0) {
                                                            try {
                                                                log.debug("Deleting consumer node {}.", consumerNode.getNodeName());
                                                                RabbitmqInjectorBootstrap.getMappingSce().getNodeSce().deleteNode(consumerNode.getNodeID());
                                                            } catch (MappingDSException e) {
                                                                log.debug("Error raised while deleting consumer node {}... Continue", consumerNode.getNodeName());
                                                                e.printStackTrace();
                                                            }
                                                        }

                                                    } else log.error("Channel target endpoint for consumerTag {} doesn't exits.", consumerTag);
                                                }

                                            } else log.error("Channel source endpoint for consumerTag {} doesn't exits.", consumerTag);

                                        } else log.error("Channel queue {} node doesn't exits.", queueName);

                                    } else log.error("Channel vhost {} node doesn't exits.", vhostName);
                                }
                            }

                            ArrayList<HashMap<String, Object>> publishes = (ArrayList) lastChannel.getProperties().get(ChannelFromRabbitREST.JSON_RABBITMQ_CHANNEL_PUBLISHES);
                            for (HashMap<String, Object> publish : publishes) {
                                HashMap<String, Object> targetExchange = (HashMap) publish.get(ChannelFromRabbitREST.JSON_RABBITMQ_CHANNEL_PUBLISHES_EXCHANGE);
                                String exchangeName = (String) targetExchange.get(ChannelFromRabbitREST.JSON_RABBITMQ_CHANNEL_PUBLISHES_EXCHANGE_NAME);
                                String vhostName = (String) targetExchange.get(ChannelFromRabbitREST.JSON_RABBITMQ_CHANNEL_PUBLISHES_EXCHANGE_VHOST);

                                if (!deletedVHTs.contains(vhostName)) {
                                    Node vhostNode = RabbitmqInjectorBootstrap.getMappingSce().getNodeByName(rbqBroker, vhostName);
                                    if (vhostNode != null) {
                                        Node exchangeNode = RabbitmqInjectorBootstrap.getMappingSce().getNodeSce().getNodeByName(vhostNode, exchangeName + " (exchange)");
                                        if (exchangeNode != null) {
                                            Endpoint targetEndpoint = null;
                                            for (Endpoint ep : exchangeNode.getNodeEndpoints())
                                                if (ep.getEndpointURL().contains(channelPeerHost + ":" + channelPeerPort) &&
                                                            ep.getEndpointURL().contains("("+channelNumber+")")) {
                                                    targetEndpoint = ep;
                                                    break;
                                                }

                                            if (targetEndpoint!=null) {
                                                for (Link link : RabbitmqInjectorBootstrap.getMappingSce().getLinksByDestinationEP(targetEndpoint)) {
                                                    Endpoint sourceEndpoint = link.getLinkEndpointSource();

                                                    if (sourceEndpoint!=null) {

                                                        Node publisherNode = targetEndpoint.getEndpointParentNode();
                                                        rbqClientBroker = publisherNode.getNodeContainer();

                                                        try {
                                                            log.debug("Deleting connection-channel source endpoint ({},{}).",
                                                                             new Object[]{vhostName, sourceEndpoint.getEndpointURL()});
                                                            RabbitmqInjectorBootstrap.getMappingSce().getEndpointSce().deleteEndpoint(sourceEndpoint.getEndpointID());
                                                        } catch (MappingDSException e) {
                                                            log.error("Error raised while deleting connection-channel source endpoint ({},{})... Continue",
                                                                             new Object[]{vhostName, sourceEndpoint.getEndpointURL()});
                                                            e.printStackTrace();
                                                        }

                                                        try {
                                                            log.debug("Deleting connection-channel target endpoint ({},{}).",
                                                                             new Object[]{vhostName, targetEndpoint.getEndpointURL()});
                                                            RabbitmqInjectorBootstrap.getMappingSce().getEndpointSce().deleteEndpoint(targetEndpoint.getEndpointID());
                                                        } catch (MappingDSException e) {
                                                            log.error("Error raised while deleting connection-channel target endpoint ({},{})... Continue",
                                                                             new Object[]{vhostName, targetEndpoint.getEndpointURL()});
                                                            e.printStackTrace();
                                                        }

                                                        if (publisherNode.getNodeChildNodes().size()==0 && publisherNode.getNodeEndpoints().size()==0) {
                                                            try {
                                                                log.debug("Deleting publisher node {}.", publisherNode.getNodeName());
                                                                RabbitmqInjectorBootstrap.getMappingSce().getNodeSce().deleteNode(publisherNode.getNodeID());
                                                            } catch (MappingDSException e) {
                                                                log.debug("Error raised while deleting publisher node {}... Continue", publisherNode.getNodeName());
                                                                e.printStackTrace();
                                                            }
                                                        }
                                                    } else log.error("Channel source endpoint {}:{}/({}) doesn't exits.", new Object[]{channelPeerHost, channelPeerPort, channelNumber});

                                                }
                                            } else log.error("Channel target endpoint {}:{}/({}) doesn't exits.", new Object[]{channelPeerHost, channelPeerPort, channelNumber});

                                        } else log.error("Channel exchange {} node doesn't exits.", exchangeName);

                                    } else log.error("Channel vhost {} node doesn't exits.", vhostName);
                                }
                            }

                            if (rbqClientBroker!=null) {
                                if (rbqClientBroker.getContainerChildContainers().size() <= 1) {
                                    try {
                                        log.debug("Deleting RabbitMQ client container {}.", rbqClientBroker.getContainerPrimaryAdminGateURL());
                                        RabbitmqInjectorBootstrap.getMappingSce().getContainerSce().deleteContainer(rbqClientBroker.getContainerPrimaryAdminGateURL());
                                    } catch (MappingDSException e) {
                                        log.debug("Error raised while deleting RabbitMQ client container {}... Continue.", rbqClientBroker.getContainerPrimaryAdminGateURL());
                                        e.printStackTrace();
                                    }
                                }
                            } else log.error("RabbitMQ Client broker container doesn't exists.");

                        } else log.error("RabbitMQ broker container {} doesn't exists.", rbqBrokerNodeName);
                    }
                }
            }
        }
    }

    private Container pushBrokerToMappingDS(RabbitmqCachedComponent entity, ArrayList<Gate> clusterGates, BrokerFromRabbitREST broker) throws MappingDSException {
        String adminGateUrl = broker.getUrl();
        String serverFQDN = adminGateUrl.split("://")[1].split(":")[0];
        String serverName = adminGateUrl.split("://")[1].split(":|\\.")[0];
        String adminGateName = "webadmingate." + serverName;

        log.debug("");
        log.debug("-----------------------------------");
        Container rbqBroker = RabbitmqInjectorBootstrap.getMappingSce().getContainerSce().createContainer(broker.getName(), adminGateUrl, adminGateName);
        log.debug("Create or get container ({},{},{})", new Object[]{rbqBroker.getContainerID(), adminGateUrl, adminGateName});
        rbqBroker.setContainerCompany(RABBITMQ_COMPANY);
        rbqBroker.setContainerProduct(RABBITMQ_PRODUCT);
        rbqBroker.setContainerType(entity.getComponentType());

        log.debug("");
        log.debug("Add property {} to rabbitmq container {} : {}", new Object[]{RABBITMQ_BROKER_NAME_KEY, adminGateUrl, broker.getName()});
        rbqBroker.addContainerProperty(RABBITMQ_BROKER_NAME_KEY, broker.getName());
        Map<String, Object> props = entity.getComponentProperties().get(broker.getName());
        for (String key : props.keySet()) {
            Object value =  props.get(key);
            if (value != null) {
                log.debug("Add property {} to rabbitmq container {} : {}", new Object[]{key, adminGateUrl, value.toString()});
                rbqBroker.addContainerProperty(key, value);
            }
        }

        for (String protocol : broker.getListeningAddress().keySet()) {
            String ip_addr = broker.getListeningAddress().get(protocol);
            int    port    = broker.getListeningPorts().get(protocol);

            String key = RABBITMQ_BROKER_LISTENER_KEY+"_"+protocol;
            String value = protocol+"://"+ip_addr+":"+port;
            log.debug("");
            log.debug("Add property {} to rabbitmq container {} : {}", new Object[]{key, adminGateUrl, value});
            rbqBroker.addContainerProperty(RABBITMQ_BROKER_LISTENER_KEY+"_"+protocol, value);

            String gateURL = (ip_addr.equals("::"))?protocol+"://"+serverFQDN+":"+port:protocol+"://"+ip_addr+":"+port;
            // NOTE1: we don't have currently the way to get real cluster connection definitions
            // so we define a generic gateURL to say a connection can come from other node to clustering port
            // or come from this node to target node cluster port.
            // NOTE2: When we will have the way to get the cluster connections definition we will add specific endpoint for
            // clustering output connection from the gate
            if (protocol.equals(RABBITMQ_BROKER_CLUSTERING_P))
                gateURL += "[*]";
            String gateName = protocol+"."+serverName;
            log.debug("");
            log.debug("---");
            Gate gate = RabbitmqInjectorBootstrap.getMappingSce().getGateSce().createGate(gateURL, gateName, rbqBroker.getContainerID(), false);
            log.debug("Create or get gate for container listening addr ({},{},{},{})", new Object[]{gate.getNodeID(), gateURL, gateName, rbqBroker.getContainerID()});
            if (protocol.equals(RABBITMQ_BROKER_CLUSTERING_P)) {
                log.debug("");
                log.debug("---");
                Transport clusterTransport = RabbitmqInjectorBootstrap.getMappingSce().getTransportSce().createTransport(RABBITMQ_TRANSPORT_TCP_CLUSTER);
                log.debug("Create of get transport ({},{})", new Object[]{clusterTransport.getTransportID(), RABBITMQ_TRANSPORT_TCP_CLUSTER});

                for (Gate gateToLink : clusterGates) {
                    for (Endpoint sourceEndpoint : gate.getNodeEndpoints())
                        for (Endpoint targetEndpoint : gateToLink.getNodeEndpoints()) {
                            log.debug("");
                            log.debug("---");
                            RabbitmqInjectorBootstrap.getMappingSce().getLinkSce().createLink(sourceEndpoint.getEndpointID(), targetEndpoint.getEndpointID(),
                                                                                                     clusterTransport.getTransportID());
                            log.debug("Create or get link ({},{},{})", new Object[]{sourceEndpoint.getEndpointParentNode().getNodeID(),
                                                                                           targetEndpoint.getEndpointParentNode().getNodeID(),
                                                                                           clusterTransport.getTransportID()});
                        }
                }
                clusterGates.add(gate);
            }
        }

        return rbqBroker;
    }

    private void pushVHostToMappingDS(RabbitmqCachedComponent entity, List<Node> vhosts, VhostFromRabbitREST vhost, Container broker) throws MappingDSException {
        log.debug("");
        log.debug("-----------------------------------");
        Node vHostNode = RabbitmqInjectorBootstrap.getMappingSce().getNodeSce().createNode(vhost.getName(), broker.getContainerID(), null);
        log.debug("Create or get node for vhost ({},{},{})", new Object[]{vHostNode.getNodeID(), vhost.getName(), broker.getContainerID()});
        log.debug("");
        for (String propsKey : vhost.getProperties().keySet()) {
            log.debug("Add property {} to rabbitmq node {} : {}", new Object[]{propsKey, vHostNode.getNodeName(), vhost.getProperties().get(propsKey).toString()});
            vHostNode.addNodeProperty(propsKey, vhost.getProperties().get(propsKey));
        }
        vhosts.add(vHostNode);
    }

    private void pushEntityToMappingDS(RabbitmqCachedComponent entity) throws MappingDSException {

        ArrayList<VhostFromRabbitREST> entityVHosts = new ArrayList<>(entity.getVhosts());
        ArrayList<QueueFromRabbitREST> entityQueues = new ArrayList<>(entity.getQueues());
        ArrayList<ExchangeFromRabbitREST> entityExchanges = new ArrayList<>(entity.getExchanges());
        ArrayList<BindingFromRabbitREST> entityBindings = new ArrayList<>(entity.getBindings());
        ArrayList<ConnectionFromRabbitREST> entityConnections = new ArrayList<>(entity.getConnections());
        ArrayList<ChannelFromRabbitREST> entityChannels = new ArrayList<>(entity.getChannels());

        Cluster   cluster = null;
        Container standaloneRBQ = null;

        if (!entity.getComponentType().equals(RabbitmqCachedComponent.RABBIT_MQ_CACHED_CMP_TYPE_SNODE)) {
            log.debug("");
            log.debug("-----------------------------------");
            cluster = RabbitmqInjectorBootstrap.getMappingSce().getClusterSce().createCluster(entity.getComponentName());
            log.debug("Create or get cluster ({},{})", new Object[]{cluster.getClusterContainers(), entity.getComponentName()});
        }

        log.debug("");
        log.debug("");
        log.debug("");
        ArrayList<Gate> clusterGates = new ArrayList<>();
        if (!entity.getComponentType().equals(RabbitmqCachedComponent.RABBIT_MQ_CACHED_CMP_TYPE_SNODE)) {
            ArrayList<BrokerFromRabbitREST> brokers = new ArrayList(entity.getBrokers());
            for (BrokerFromRabbitREST broker : brokers)
                cluster.addClusterContainer(pushBrokerToMappingDS(entity, clusterGates, broker));
        } else standaloneRBQ = pushBrokerToMappingDS(entity, clusterGates, entity.getBroker());

        log.debug("");
        log.debug("");
        log.debug("");
        List<Node> vhosts = new ArrayList<>();
        for (VhostFromRabbitREST vhost : entityVHosts) {
            if (cluster!=null)
                for (Container broker : cluster.getClusterContainers())
                    pushVHostToMappingDS(entity, vhosts, vhost, broker);
            else if (standaloneRBQ!=null)
                pushVHostToMappingDS(entity, vhosts, vhost, standaloneRBQ);
        }

        if (vhosts.size()>1)
            for (Node vHost : vhosts)
                for (Node twinVHost : vhosts)
                    if (!twinVHost.equals(vHost) && twinVHost.getNodeName().equals(vHost.getNodeName())) {
                        log.debug("");
                        log.debug("---");
                        vHost.addTwinNode(twinVHost);
                        log.debug("Twin vHosts node together ({},{})", new Object[]{vHost.getNodeContainer().getContainerPrimaryAdminGate().getNodeName()+"-"+vHost.getNodeName()},
                                         twinVHost.getNodeContainer().getContainerPrimaryAdminGate().getNodeName()+"-"+twinVHost.getNodeName());
                    }

        log.debug("");
        log.debug("");
        log.debug("");
        List<Node> queues = new ArrayList<>();
        for (QueueFromRabbitREST queue : entityQueues) {
            for (Node vHost : vhosts) {
                if (queue.getVhost().equals(vHost.getNodeName())) {
                    log.debug("");
                    log.debug("-----------------------------------");
                    Node queueNode = RabbitmqInjectorBootstrap.getMappingSce().getNodeSce().createNode(queue.getName() + " (queue)", vHost.getNodeContainer().getContainerID(), vHost.getNodeID());
                    log.debug("Create or get node for queue ({},{},{},{})", new Object[]{queueNode.getNodeID(), queue.getName(), vHost.getNodeContainer().getContainerID(), vHost.getNodeID()});
                    for (String propsKey : queue.getProperties().keySet()) {
                        log.debug("Add property {} to rabbitmq node {} : {}", new Object[]{propsKey, queueNode.getNodeName(), queue.getProperties().get(propsKey).toString()});
                        queueNode.addNodeProperty(propsKey, queue.getProperties().get(propsKey));
                    }

                    queues.add(queueNode);
                    vHost.addNodeChildNode(queueNode);
                }
            }
        }

        if (queues.size()>1)
            for (Node queue : queues)
                for (Node twinQueue : queues)
                    if (!twinQueue.equals(queue) && twinQueue.getNodeName().equals(queue.getNodeName()) &&
                        // be sure to twin queues in same clustered vHost
                        twinQueue.getNodeParentNode().getNodeName().equals(queue.getNodeParentNode().getNodeName())) {
                        log.debug("");
                        log.debug("---");
                        queue.addTwinNode(twinQueue);
                        log.debug("Twin queues node together ({},{})", new Object[]{queue.getNodeContainer().getContainerPrimaryAdminGate().getNodeName()+"-"+queue.getNodeName()},
                                         twinQueue.getNodeContainer().getContainerPrimaryAdminGate().getNodeName()+"-"+twinQueue.getNodeName());
                    }

        log.debug("");
        log.debug("");
        log.debug("");
        List<Node> exchanges = new ArrayList<>();
        for (ExchangeFromRabbitREST exchange : entityExchanges)
            for (Node vHost : vhosts)
                if (exchange.getVhost().equals(vHost.getNodeName())) {
                    log.debug("");
                    log.debug("-----------------------------------");
                    Node exchangeNode = RabbitmqInjectorBootstrap.getMappingSce().getNodeSce().createNode(exchange.getName() + " (exchange)", vHost.getNodeContainer().getContainerID(), vHost.getNodeID());
                    log.debug("Create or get node for exchange ({},{},{},{},{},{})", new Object[]{exchangeNode.getNodeID(), exchange.getName(), vHost.getNodeContainer().getContainerID(), vHost.getNodeContainer().getContainerName(), vHost.getNodeID(), vHost.getNodeName()});
                    for (String propsKey : exchange.getProperties().keySet()) {
                        log.debug("Add property {} to rabbitmq node {} : {}", new Object[]{propsKey, exchangeNode.getNodeName(), exchange.getProperties().get(propsKey).toString()});
                        exchangeNode.addNodeProperty(propsKey, exchange.getProperties().get(propsKey));
                    }
                    exchanges.add(exchangeNode);
                    vHost.addNodeChildNode(exchangeNode);
                }

        if (exchanges.size()>1)
            for (Node exchange : exchanges)
                for (Node twinExchange : exchanges)
                    if (!twinExchange.equals(exchange) && twinExchange.getNodeName().equals(exchange.getNodeName()) &&
                        // be sure to twin exchanges in same clustered vHost
                        twinExchange.getNodeParentNode().getNodeName().equals(exchange.getNodeParentNode().getNodeName())) {
                        log.debug("");
                        log.debug("---");
                        exchange.addTwinNode(twinExchange);
                        log.debug("Twin exchanges node together ({},{})", new Object[]{twinExchange.getNodeContainer().getContainerPrimaryAdminGate().getNodeName()+"-"+twinExchange.getNodeName()},
                                         exchange.getNodeContainer().getContainerPrimaryAdminGate().getNodeName()+"-"+exchange.getNodeName());
                    }

        for (BindingFromRabbitREST binding : entityBindings) {
            log.debug("");
            log.debug("binding {} on {} : {}", new Object[]{binding.getName(), binding.getVhost(), binding.getProperties().toString()});
            String bindingDestinationType = (String) binding.getProperties().get(BindingFromRabbitREST.JSON_RABBITMQ_BINDING_DESTINATION_TYPE);
            String routingKey = (String)binding.getProperties().get(BindingFromRabbitREST.JSON_RABBITMQ_BINDING_ROUNTING_KEY);
            String destination = (String)binding.getProperties().get(BindingFromRabbitREST.JSON_RABBITMQ_BINDING_DESTINATION);
            String source = (String)binding.getProperties().get(BindingFromRabbitREST.JSON_RABBITMQ_BINDING_SOURCE);

            for (Node exchange : exchanges) {
                if (exchange.getNodeName().equals(source+ " (exchange)") &&
                    exchange.getNodeParentNode().getNodeName().equals(binding.getVhost())) {

                    log.debug("");
                    log.debug("");
                    log.debug("");
                    log.debug("-----------------------------------");
                    log.debug("source exchange ({},{},{},{})", new Object[]{exchange.getNodeID(), exchange.getNodeName(), exchange.getNodeParentNode().getNodeName(),
                            exchange.getNodeContainer().getContainerProperties().get(RABBITMQ_BROKER_NAME_KEY)});

                    String exchangeType = (String)exchange.getNodeProperties().get(ExchangeFromRabbitREST.JSON_RABBITMQ_EXCHANGE_TYPE);

                    if (bindingDestinationType.equals(BindingFromRabbitREST.RABBITMQ_BINDING_DESTINATION_TYPE_Q)) {
                        for (Node queue : queues) {

                            if (queue.getNodeName().equals(destination + " (queue)") &&
                                queue.getNodeParentNode().getNodeName().equals(binding.getVhost()) &&
                                exchange.getNodeContainer().equals(queue.getNodeContainer())) {
                                log.debug("");
                                log.debug("target queue ({},{},{},{})", new Object[]{queue.getNodeID(), destination, queue.getNodeParentNode().getNodeName(),
                                        queue.getNodeContainer().getContainerProperties().get(RABBITMQ_BROKER_NAME_KEY)});

                                String exchangeSourceEndpointURL= null;
                                String queueTargetEndpointURL = null;

                                log.debug("exchangeType : {} ; routingKey : {}", new Object[]{exchangeType, routingKey});

                                if (exchangeType.equals(ExchangeFromRabbitREST.RABBITMQ_EXCHANGE_TYPE_DIRECT)) {
                                    exchangeSourceEndpointURL = RABBITMQ_TRANSPORT_MEM_BINDING + exchange.getNodeContainer().getContainerProperties().get(RABBITMQ_BROKER_NAME_KEY) + "/" +
                                                                source + "/" + routingKey;
                                    queueTargetEndpointURL = RABBITMQ_TRANSPORT_MEM_BINDING + exchange.getNodeContainer().getContainerProperties().get(RABBITMQ_BROKER_NAME_KEY) + "/" +
                                                             destination + "/" + routingKey;
                                } else if (exchangeType.equals(ExchangeFromRabbitREST.RABBITMQ_EXCHANGE_TYPE_FANOUT)) {
                                    exchangeSourceEndpointURL = RABBITMQ_TRANSPORT_MEM_BINDING + exchange.getNodeContainer().getContainerProperties().get(RABBITMQ_BROKER_NAME_KEY) + "/" +
                                                                source;
                                    queueTargetEndpointURL = RABBITMQ_TRANSPORT_MEM_BINDING + exchange.getNodeContainer().getContainerProperties().get(RABBITMQ_BROKER_NAME_KEY) + "/" +
                                                             destination;
                                } else if (exchangeType.equals(ExchangeFromRabbitREST.RABBITMQ_EXCHANGE_TYPE_TOPIC)) {
                                    exchangeSourceEndpointURL = RABBITMQ_TRANSPORT_MEM_BINDING + exchange.getNodeContainer().getContainerProperties().get(RABBITMQ_BROKER_NAME_KEY) + "/" +
                                                                source + "/" + routingKey;
                                    queueTargetEndpointURL = RABBITMQ_TRANSPORT_MEM_BINDING + exchange.getNodeContainer().getContainerProperties().get(RABBITMQ_BROKER_NAME_KEY) + "/" +
                                                             destination + "/" + routingKey;
                                } else if (exchangeType.equals(ExchangeFromRabbitREST.RABBITMQ_EXCHANGE_TYPE_HEADER)) {
                                    exchangeSourceEndpointURL = RABBITMQ_TRANSPORT_MEM_BINDING + exchange.getNodeContainer().getContainerProperties().get(RABBITMQ_BROKER_NAME_KEY) + "/" +
                                                                source;
                                    queueTargetEndpointURL = RABBITMQ_TRANSPORT_MEM_BINDING + exchange.getNodeContainer().getContainerProperties().get(RABBITMQ_BROKER_NAME_KEY) + "/" +
                                                             destination;
                                } else
                                    log.error("Unknown exchange type : {}", new Object[]{exchangeType});

                                if (exchangeSourceEndpointURL!=null) {
                                    log.debug("");
                                    log.debug("---");
                                    Endpoint sourceEp = RabbitmqInjectorBootstrap.getMappingSce().getEndpointSce().createEndpoint(exchangeSourceEndpointURL, exchange.getNodeID());
                                    log.debug("Create or get endpoint ({},{},{}).", new Object[]{sourceEp.getEndpointID(), exchangeSourceEndpointURL, exchange.getNodeID()});
                                    Endpoint targetEp = RabbitmqInjectorBootstrap.getMappingSce().getEndpointSce().createEndpoint(queueTargetEndpointURL, queue.getNodeID());
                                    log.debug("Create or get endpoint ({},{},{}).", new Object[]{targetEp.getEndpointID(), queueTargetEndpointURL, queue.getNodeID()});

                                    for (String key : binding.getProperties().keySet()) {
                                        if (binding.getProperties().get(key)!=null) {
                                            log.debug("Add property {} to rabbitmq endpoints : {}", new Object[]{key, binding.getProperties().get(key).toString()});
                                            sourceEp.addEndpointProperty(key, binding.getProperties().get(key));
                                            targetEp.addEndpointProperty(key, binding.getProperties().get(key));
                                        }
                                    }

                                    Transport transport = RabbitmqInjectorBootstrap.getMappingSce().getTransportSce().createTransport(RABBITMQ_TRANSPORT_MEM_BINDING);
                                    log.debug("Create or get transport ({},{}).", new Object[]{transport.getTransportID(), RABBITMQ_TRANSPORT_MEM_BINDING});

                                    Link link = RabbitmqInjectorBootstrap.getMappingSce().getLinkSce().createLink(sourceEp.getEndpointID(), targetEp.getEndpointID(),
                                                                                                      transport.getTransportID());
                                    log.debug("Link endpoints together ({},{},{},{}).", new Object[]{link.getLinkID(), sourceEp.getEndpointID(),
                                                                                                     targetEp.getEndpointID(), transport.getTransportID()});
                                }
                            }
                        }

                    } else if (bindingDestinationType.equals(BindingFromRabbitREST.RABBITMQ_BINDING_DESTINATION_TYPE_E)) {
                        for (Node exchangeToLink : exchanges) {
                            if (exchangeToLink.getNodeName().equals(destination + " (exchange)") &&
                                exchangeToLink.getNodeParentNode().getNodeName().equals(binding.getVhost()) &&
                                exchange.getNodeContainer().equals(exchangeToLink.getNodeContainer())) {

                                log.debug("");
                                log.debug("target exchange ({},{},{},{})", new Object[]{exchangeToLink.getNodeID(), exchangeToLink.getNodeName(),
                                                                                        exchangeToLink.getNodeParentNode().getNodeName(),
                                                                                        exchangeToLink.getNodeContainer().getContainerProperties().get(RABBITMQ_BROKER_NAME_KEY)});

                                String exchangeSourceEndpointURL = null;
                                String exchangeTargetEndpointURL = null;

                                if (exchangeType.equals(ExchangeFromRabbitREST.RABBITMQ_EXCHANGE_TYPE_DIRECT)) {
                                    exchangeSourceEndpointURL = RABBITMQ_TRANSPORT_MEM_BINDING + exchange.getNodeContainer().getContainerProperties().get(RABBITMQ_BROKER_NAME_KEY) + "/" +
                                                                source + "/" + routingKey;
                                    exchangeTargetEndpointURL = RABBITMQ_TRANSPORT_MEM_BINDING + exchange.getNodeContainer().getContainerProperties().get(RABBITMQ_BROKER_NAME_KEY) + "/" +
                                                                destination + "/" + routingKey;
                                } else if (exchangeType.equals(ExchangeFromRabbitREST.RABBITMQ_EXCHANGE_TYPE_FANOUT)) {
                                    exchangeSourceEndpointURL = RABBITMQ_TRANSPORT_MEM_BINDING + exchange.getNodeContainer().getContainerProperties().get(RABBITMQ_BROKER_NAME_KEY) + "/" +
                                                                source;
                                    exchangeTargetEndpointURL = RABBITMQ_TRANSPORT_MEM_BINDING + exchange.getNodeContainer().getContainerProperties().get(RABBITMQ_BROKER_NAME_KEY) + "/" +
                                                                destination;
                                } else if (exchangeType.equals(ExchangeFromRabbitREST.RABBITMQ_EXCHANGE_TYPE_TOPIC)) {
                                    exchangeSourceEndpointURL = RABBITMQ_TRANSPORT_MEM_BINDING + exchange.getNodeContainer().getContainerProperties().get(RABBITMQ_BROKER_NAME_KEY) + "/" +
                                                                source + "/" + routingKey;
                                    exchangeTargetEndpointURL = RABBITMQ_TRANSPORT_MEM_BINDING + exchange.getNodeContainer().getContainerProperties().get(RABBITMQ_BROKER_NAME_KEY) + "/" +
                                                                destination + "/" + routingKey;
                                } else if (exchangeType.equals(ExchangeFromRabbitREST.RABBITMQ_EXCHANGE_TYPE_HEADER)) {
                                    exchangeSourceEndpointURL = RABBITMQ_TRANSPORT_MEM_BINDING + exchange.getNodeContainer().getContainerProperties().get(RABBITMQ_BROKER_NAME_KEY) + "/" +
                                                                source;
                                    exchangeTargetEndpointURL = RABBITMQ_TRANSPORT_MEM_BINDING + exchange.getNodeContainer().getContainerProperties().get(RABBITMQ_BROKER_NAME_KEY) + "/" +
                                                                destination;
                                } else
                                    log.error("Unknown exchange type : {}", new Object[]{exchangeType});

                                if (exchangeSourceEndpointURL!=null) {
                                    log.debug("");
                                    log.debug("---");
                                    Endpoint sourceEp = RabbitmqInjectorBootstrap.getMappingSce().getEndpointSce().createEndpoint(exchangeSourceEndpointURL, exchange.getNodeID());
                                    log.debug("Create or get endpoint ({},{},{}).", new Object[]{sourceEp.getEndpointID(), exchangeSourceEndpointURL, exchange.getNodeID()});
                                    Endpoint targetEp = RabbitmqInjectorBootstrap.getMappingSce().getEndpointSce().createEndpoint(exchangeTargetEndpointURL, exchangeToLink.getNodeID());
                                    log.debug("Create or get endpoint ({},{},{}).", new Object[]{targetEp.getEndpointID(), exchangeTargetEndpointURL, exchangeToLink.getNodeID()});

                                    for (String key : binding.getProperties().keySet()) {
                                        if (binding.getProperties().get(key)!=null) {
                                            log.debug("Add property {} to rabbitmq endpoints : {}", new Object[]{key, binding.getProperties().get(key).toString()});
                                            sourceEp.addEndpointProperty(key, binding.getProperties().get(key));
                                            targetEp.addEndpointProperty(key, binding.getProperties().get(key));
                                        }
                                    }

                                    Transport transport = RabbitmqInjectorBootstrap.getMappingSce().getTransportSce().createTransport(RABBITMQ_TRANSPORT_MEM_BINDING);
                                    log.debug("Create or get transport ({},{}).", new Object[]{transport.getTransportID(), RABBITMQ_TRANSPORT_MEM_BINDING});

                                    Link link = RabbitmqInjectorBootstrap.getMappingSce().getLinkSce().createLink(sourceEp.getEndpointID(), targetEp.getEndpointID(),

                                    transport.getTransportID());
                                    log.debug("Link endpoints together ({},{},{},{}).", new Object[]{link.getLinkID(), sourceEp.getEndpointID(),
                                                                                                            targetEp.getEndpointID(), transport.getTransportID()});
                                }
                            }
                        }

                    } else
                        log.error("Unknown binding destination type : {}", new Object[]{binding.getProperties().get(BindingFromRabbitREST.JSON_RABBITMQ_BINDING_DESTINATION_TYPE)});
                }
            }
        }

        log.debug("");
        log.debug("");
        log.debug("");
        for (ConnectionFromRabbitREST connectionFromRabbitREST : entityConnections) {

            Container rbqBroker = null;
            if (!entity.getComponentType().equals(RabbitmqCachedComponent.RABBIT_MQ_CACHED_CMP_TYPE_SNODE)) {
                for (Container broker : cluster.getClusterContainers()) {
                    if (broker.getContainerProperties().get(RABBITMQ_BROKER_NAME_KEY).equals(connectionFromRabbitREST.getProperties().get(ConnectionFromRabbitREST.JSON_RABBITMQ_CONNECTION_NODE))) {
                        rbqBroker = broker;
                        break;
                    }
                }
            } else
                rbqBroker = standaloneRBQ;


            if (rbqBroker!=null) {
                log.debug("");
                log.debug("");
                log.debug("");
                log.debug("-----------------------------------");

                HashMap<String, Object> connection_client_props = (HashMap)connectionFromRabbitREST.getProperties().get(ConnectionFromRabbitREST.JSON_RABBITMQ_CONNECTION_CLIENT_PROPERTIES);
                //log.debug("connection client properties (product) : {}", connection_client_props.get(ConnectionFromRabbitREST.JSON_RABBITMQ_CONNECTION_CLIENT_PROPERTIES_PRODUCT));
                //log.debug("connection client properties (platform) : {}", connection_client_props.get(ConnectionFromRabbitREST.JSON_RABBITMQ_CONNECTION_CLIENT_PROPERTIES_PLATFORM));
                //log.debug("connection client properties (information) : {}", connection_client_props.get(ConnectionFromRabbitREST.JSON_RABBITMQ_CONNECTION_CLIENT_PROPERTIES_INFORMATION));

                log.debug("");
                log.debug("RabbitMQ broker container : ({},{})", new Object[]{rbqBroker.getContainerID(), rbqBroker.getContainerProperties().get(RABBITMQ_BROKER_NAME_KEY)});

                //HashMap<String, Object> connection_client_props = (HashMap)connectionFromRabbitREST.getProperties().get(ConnectionFromRabbitREST.JSON_RABBITMQ_CONNECTION_CLIENT_PROPERTIES);
                String remoteCliPGURL = (String)connection_client_props.get(ConnectionFromRabbitREST.JSON_RABBITMQ_CONNECTION_CLIENT_PROPERTIES_ARIANE_PGURL);
                String remoteCliOSI   = (String)connection_client_props.get(ConnectionFromRabbitREST.JSON_RABBITMQ_CONNECTION_CLIENT_PROPERTIES_ARIANE_OSI);
                String remoteCliOTM   = (String)connection_client_props.get(ConnectionFromRabbitREST.JSON_RABBITMQ_CONNECTION_CLIENT_PROPERTIES_ARIANE_OTM);
                String remoteCliAPP   = (String)connection_client_props.get(ConnectionFromRabbitREST.JSON_RABBITMQ_CONNECTION_CLIENT_PROPERTIES_ARIANE_APP);
                String remoteCliCMP   = (String)connection_client_props.get(ConnectionFromRabbitREST.JSON_RABBITMQ_CONNECTION_CLIENT_PROPERTIES_ARIANE_CMP);

                String remoteCliPID   = null;
                if (connection_client_props.get(ConnectionFromRabbitREST.JSON_RABBITMQ_CONNECTION_CLIENT_PROPERTIES_ARIANE_PID)!=null)
                    remoteCliPID = connection_client_props.get(ConnectionFromRabbitREST.JSON_RABBITMQ_CONNECTION_CLIENT_PROPERTIES_ARIANE_PID).toString();

                if (remoteCliPGURL!=null && remoteCliOSI!=null && remoteCliOTM!=null && remoteCliAPP!=null && remoteCliCMP!=null) {
                    String serverName = remoteCliPGURL.split("://")[1].split(":|/|\\.")[0];
                    String adminGateName = "rbqcliadmingate." + serverName;

                    log.debug("");
                    log.debug("---");
                    String containerName = "rabbit-cli";
                    if (remoteCliPID!=null) containerName += "-"+remoteCliPID;
                    containerName += "@" + serverName;
                    Container rbqClient = RabbitmqInjectorBootstrap.getMappingSce().getContainerSce().createContainer(containerName, remoteCliPGURL, adminGateName);
                    log.debug("Create or get container ({},{},{})", new Object[]{rbqClient.getContainerID(), remoteCliPGURL, adminGateName});
                    rbqClient.setContainerCompany(remoteCliCMP);
                    rbqClient.setContainerProduct((String)connection_client_props.get(ConnectionFromRabbitREST.JSON_RABBITMQ_CONNECTION_CLIENT_PROPERTIES_PRODUCT));
                    rbqClient.setContainerType(remoteCliAPP);

                    if (remoteCliPID!=null)
                        rbqClient.addContainerProperty(ConnectionFromRabbitREST.JSON_RABBITMQ_CONNECTION_CLIENT_PROPERTIES_ARIANE_PID, remoteCliPID);

                    HashMap<String, Object> rbqClientProps = RabbitmqInjectorBootstrap.getRabbitmqDirectorySce().getRemoteClientContainerProperties(remoteCliOSI, remoteCliOTM);
                    if (remoteCliOTM.equals(RabbitmqDirectoryService.ARIANE_OTM_NOT_DEFINED)) log.warn("Operations team for RabbitMQ client " + containerName + " has not been defined !");
                    for (String key : rbqClientProps.keySet())
                        if (rbqClientProps.get(key)!=null) {
                            rbqClient.addContainerProperty(key, rbqClientProps.get(key));
                            log.debug("Add property {} to rabbitmq client container {} : {}", new Object[]{key, rbqClient.getContainerPrimaryAdminGate().getNodeName(),
                                                                                                                rbqClientProps.get(key).toString()});
                        }

                    String protocol = (String)connectionFromRabbitREST.getProperties().get(ConnectionFromRabbitREST.JSON_RABBITMQ_CONNECTION_PROTOCOL);
                    String transportName = null;
                    boolean ssl     = (Boolean)connectionFromRabbitREST.getProperties().get(ConnectionFromRabbitREST.JSON_RABBITMQ_CONNECTION_SSL);
                    String peerHost = (String)connectionFromRabbitREST.getProperties().get(ConnectionFromRabbitREST.JSON_RABBITMQ_CONNECTION_PEER_HOST);
                    int peerPort = (Integer)connectionFromRabbitREST.getProperties().get(ConnectionFromRabbitREST.JSON_RABBITMQ_CONNECTION_PEER_PORT);
                    String brokerHost = (String)connectionFromRabbitREST.getProperties().get(ConnectionFromRabbitREST.JSON_RABBITMQ_CONNECTION_HOST);
                    int brokerPort = (Integer)connectionFromRabbitREST.getProperties().get(ConnectionFromRabbitREST.JSON_RABBITMQ_CONNECTION_PORT);

                    if (protocol.startsWith(ConnectionFromRabbitREST.RABBITMQ_CONNECTION_PROTOCOL_AMQP))
                        if (ssl)
                            transportName = RABBITMQ_TRANSPORT_SSL_AMQP;
                        else
                            transportName = RABBITMQ_TRANSPORT_TCP_AMQP;

                    else if (protocol.startsWith(ConnectionFromRabbitREST.RABBITMQ_CONNECTION_PROTOCOL_MQTT))
                        if (ssl)
                            transportName = RABBITMQ_TRANSPORT_SSL_MQTT;
                        else
                            transportName = RABBITMQ_TRANSPORT_TCP_MQTT;

                    else if (protocol.startsWith(ConnectionFromRabbitREST.RABBITMQ_CONNECTION_PROTOCOL_STOMP))
                        if (ssl)
                            transportName = RABBITMQ_TRANSPORT_SSL_STOMP;
                        else
                            transportName = RABBITMQ_TRANSPORT_TCP_STOMP;

                    else
                        log.error("Unknown protocol type : {} ", protocol);

                    if (transportName!=null) {
                        for (ChannelFromRabbitREST channelFromRabbitREST : entityChannels) {
                            HashMap<String, Object> connectionDetails = (HashMap<String, Object>) channelFromRabbitREST.getProperties().get(ChannelFromRabbitREST.JSON_RABBITMQ_CHANNEL_CONNECTION_DETAILS);
                            if (connectionFromRabbitREST.getName().equals(connectionDetails.get(ChannelFromRabbitREST.JSON_RABBITMQ_CHANNEL_CONNECTION_DETAILS_NAME))) {
                                String channelName = channelFromRabbitREST.getName();
                                String channelNumber = channelName.split("\\(")[1].split("\\)")[0];

                                ArrayList<HashMap<String, Object>> consumers_details = (ArrayList) channelFromRabbitREST.getProperties().get(ChannelFromRabbitREST.JSON_RABBITMQ_CHANNEL_CONSUMER_DETAILS);
                                for (HashMap<String, Object> consumerDetails : consumers_details) {
                                    HashMap<String, Object> queue_details = (HashMap) consumerDetails.get(ChannelFromRabbitREST.JSON_RABBITMQ_CHANNEL_CONSUMER_DETAILS_QUEUE);
                                    String consumerTag = (String) consumerDetails.get(ChannelFromRabbitREST.JSON_RABBITMQ_CHANNEL_CONSUMER_DETAILS_CONSUMER_TAG);
                                    String queueName = (String) queue_details.get(ChannelFromRabbitREST.JSON_RABBITMQ_CHANNEL_CONSUMER_DETAILS_QUEUE_NAME);
                                    String vhostName = (String) queue_details.get(ChannelFromRabbitREST.JSON_RABBITMQ_CHANNEL_CONSUMER_DETAILS_QUEUE_VHOST);
                                    log.debug("---");
                                    log.debug("channel consumer tag : {}", consumerTag);
                                    log.debug("channel consumer queue : {} on {}", new Object[]{queueName, vhostName});

                                    for (Node queue : queues) {
                                        if (queue.getNodeName().equals(queueName + " (queue)") && queue.getNodeParentNode().getNodeName().equals(vhostName) &&
                                                    queue.getNodeContainer().equals(rbqBroker)) {

                                            String consumerNodeName = queueName + " consumer";
                                            Node consumerNode = RabbitmqInjectorBootstrap.getMappingSce().getNodeSce().createNode(consumerNodeName, rbqClient.getContainerID(), null);
                                            log.debug("Create or get node for consumer ({},{},{})", new Object[]{consumerNode.getNodeID(), consumerNodeName, rbqClient.getContainerID()});

                                            String sourceEpUrl = transportName + brokerHost + ":" + brokerPort + "/" + peerHost + ":" + peerPort + "/(" + channelNumber + ")/" + consumerTag;
                                            Endpoint sourceEp = RabbitmqInjectorBootstrap.getMappingSce().getEndpointSce().createEndpoint(sourceEpUrl, queue.getNodeID());
                                            log.debug("Create or get endpoint : ({},{},{})", new Object[]{sourceEp.getEndpointID(), sourceEpUrl, queue.getNodeID()});
                                            String targetEpUrl = transportName + peerHost + ":" + peerPort + "/" + brokerHost + ":" + brokerPort + "/(" + channelNumber + ")/" + consumerTag;
                                            Endpoint targetEp = RabbitmqInjectorBootstrap.getMappingSce().getEndpointSce().createEndpoint(targetEpUrl, consumerNode.getNodeID());
                                            log.debug("Create or get endpoint : ({},{},{})", new Object[]{targetEp.getEndpointID(), targetEpUrl, consumerNode.getNodeID()});

                                            Transport transport = RabbitmqInjectorBootstrap.getMappingSce().getTransportSce().createTransport(transportName);
                                            log.debug("Create or get transport : ({},{})", new Object[]{transport.getTransportID(), transportName});

                                            Link link = RabbitmqInjectorBootstrap.getMappingSce().getLinkSce().createLink(sourceEp.getEndpointID(), targetEp.getEndpointID(),
                                                                                                                                 transport.getTransportID());
                                            log.debug("Create or get link : ({},{},{},{})", new Object[]{link.getLinkID(), sourceEp.getEndpointID(),
                                                                                                                targetEp.getEndpointID(), transport.getTransportID()});

                                            break;
                                        }
                                    }
                                }
                                ArrayList<HashMap<String, Object>> publishes = (ArrayList) channelFromRabbitREST.getProperties().get(ChannelFromRabbitREST.JSON_RABBITMQ_CHANNEL_PUBLISHES);
                                for (HashMap<String, Object> publish : publishes) {
                                    HashMap<String, Object> targetExchange = (HashMap) publish.get(ChannelFromRabbitREST.JSON_RABBITMQ_CHANNEL_PUBLISHES_EXCHANGE);
                                    String exchangeName = (String) targetExchange.get(ChannelFromRabbitREST.JSON_RABBITMQ_CHANNEL_PUBLISHES_EXCHANGE_NAME);
                                    String exchangeVhost = (String) targetExchange.get(ChannelFromRabbitREST.JSON_RABBITMQ_CHANNEL_PUBLISHES_EXCHANGE_VHOST);
                                    log.debug("---");
                                    log.debug("channel publisher exchange name: {}", exchangeName);
                                    log.debug("channel publisher exchange vhost: {}", exchangeVhost);

                                    for (Node exchange : exchanges) {
                                        if (exchange.getNodeName().equals(exchangeName + " (exchange)") && exchange.getNodeParentNode().getNodeName().equals(exchangeVhost) &&
                                                    exchange.getNodeContainer().equals(rbqBroker)) {

                                            String publisherNodeName = exchangeName + " publisher";
                                            Node publisherNode = RabbitmqInjectorBootstrap.getMappingSce().getNodeSce().createNode(publisherNodeName, rbqClient.getContainerID(), null);
                                            log.debug("Create or get node for publisher ({},{},{})", new Object[]{publisherNode.getNodeID(), publisherNodeName, rbqClient.getContainerID()});

                                            String sourceEpUrl = transportName + peerHost + ":" + peerPort + "/" + brokerHost + ":" + brokerPort + "/(" + channelNumber + ")/" + exchangeName;
                                            Endpoint sourceEp = RabbitmqInjectorBootstrap.getMappingSce().getEndpointSce().createEndpoint(sourceEpUrl, publisherNode.getNodeID());
                                            log.debug("Create or get endpoint : ({},{},{})", new Object[]{sourceEp.getEndpointID(), sourceEpUrl, publisherNode.getNodeID()});

                                            String targetEpUrl = transportName + brokerHost + ":" + brokerPort + "/" + peerHost + ":" + peerPort + "/(" + channelNumber + ")";
                                            Endpoint targetEp = RabbitmqInjectorBootstrap.getMappingSce().getEndpointSce().createEndpoint(targetEpUrl, exchange.getNodeID());
                                            log.debug("Create or get endpoint : ({},{},{})", new Object[]{targetEp.getEndpointID(), targetEpUrl, exchange.getNodeID()});

                                            Transport transport = RabbitmqInjectorBootstrap.getMappingSce().getTransportSce().createTransport(transportName);
                                            log.debug("Create or get transport : ({},{})", new Object[]{transport.getTransportID(), transportName});

                                            Link link = RabbitmqInjectorBootstrap.getMappingSce().getLinkSce().createLink(sourceEp.getEndpointID(), targetEp.getEndpointID(),
                                                                                                                                 transport.getTransportID());
                                            log.debug("Create or get link : ({},{},{},{})", new Object[]{link.getLinkID(), sourceEp.getEndpointID(),
                                                                                                                targetEp.getEndpointID(), transport.getTransportID()});

                                            break;
                                        }
                                    }
                                }
                            }
                        }
                    }

                } else
                    log.error("Remote RabbitMQ client didn't define necessary Ariane properties to map it...\n" +
                               ConnectionFromRabbitREST.JSON_RABBITMQ_CONNECTION_CLIENT_PROPERTIES_ARIANE_PGURL + ": " + remoteCliPGURL + "\n" +
                               ConnectionFromRabbitREST.JSON_RABBITMQ_CONNECTION_CLIENT_PROPERTIES_ARIANE_OSI + ": " + remoteCliOSI + "\n" +
                               ConnectionFromRabbitREST.JSON_RABBITMQ_CONNECTION_CLIENT_PROPERTIES_ARIANE_OTM + ": " + remoteCliOTM + "\n" +
                               ConnectionFromRabbitREST.JSON_RABBITMQ_CONNECTION_CLIENT_PROPERTIES_ARIANE_CMP + ": " + remoteCliCMP + "\n" +
                               ConnectionFromRabbitREST.JSON_RABBITMQ_CONNECTION_CLIENT_PROPERTIES_ARIANE_APP + ": " + remoteCliAPP + "\n" +
                               ConnectionFromRabbitREST.JSON_RABBITMQ_CONNECTION_CLIENT_PROPERTIES_ARIANE_PID + ": " + remoteCliPID
                    );
            } else
                log.error("Unable to find target RabbitMQ node for connection {}", connectionFromRabbitREST.getName());
        }
    }

    private void removeEntityFromMappingDS(RabbitmqCachedComponent entity) throws MappingDSException {
        RabbitmqInjectorBootstrap.getMappingSce().getClusterSce().deleteCluster(entity.getComponentId());
        RabbitmqInjectorBootstrap.getComponentsRegistry().removeEntityFromCache(entity);
    }

    private void inject(RabbitmqCachedComponent entity) {
        Session mappingSession = RabbitmqInjectorBootstrap.getMappingSce().openSession(this.gear.getGearId());
        try {
            log.debug("Injection begin... Action {} on {}", new Object[]{entity.getNextAction(), entity.getComponentURL()});
            switch (entity.getNextAction()) {
                case RabbitmqCachedComponent.ACTION_UPDATE:
                    applyEntityDifferencesFromLastSniff(entity);
                    pushEntityToMappingDS(entity);
                    break;
                case RabbitmqCachedComponent.ACTION_CREATE:
                    pushEntityToMappingDS(entity);
                    break;
                case RabbitmqCachedComponent.ACTION_DELETE:
                    removeEntityFromMappingDS(entity);
                    break;
                default:
                    log.error("Action {} unknown !", new Object[]{entity.getNextAction()});
                    break;
            }
            log.debug("Injection end...");
            mappingSession.commit();
        } catch (Exception E) {
            String msg = "Exception catched while injecting RabbitMQ data into DB... Rollback injection !";
            E.printStackTrace();
            log.error(msg);
            entity.rollback();
            try {
                mappingSession.rollback();
            } catch (MappingDSException e) {
                log.error("Rollback failed !");
                e.printStackTrace();
            }
        }
        RabbitmqInjectorBootstrap.getMappingSce().closeSession();
    }

    @Override
    public void onReceive(Object rabbitmqComponent) throws Exception {
        log.debug("Actor {} receive message {}", new Object[]{getSelf().path().toStringWithoutAddress(), rabbitmqComponent.toString()});
        if (rabbitmqComponent instanceof RabbitmqCachedComponent) {
            Thread.currentThread().setName("RabbitMQ Mapping Actor - " + Thread.currentThread().getId());
            inject((RabbitmqCachedComponent) rabbitmqComponent);
        } else {
            log.debug("Unhandled message type {} !", rabbitmqComponent.getClass().toString());
            unhandled(rabbitmqComponent);
        }
    }
}