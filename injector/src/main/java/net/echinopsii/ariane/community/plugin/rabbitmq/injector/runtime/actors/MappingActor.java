/**
 * [DEFINE YOUR PROJECT NAME/MODULE HERE]
 * [DEFINE YOUR PROJECT DESCRIPTION HERE] 
 * Copyright (C) 08/12/14 echinopsii
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
import net.echinopsii.ariane.community.plugin.rabbitmq.injector.RabbitmqInjectorBootstrap;
import net.echinopsii.ariane.community.plugin.rabbitmq.injector.cache.RabbitmqCachedComponent;
import net.echinopsii.ariane.community.plugin.rabbitmq.injector.runtime.gears.MappingGear;
import net.echinopsii.ariane.community.plugin.rabbitmq.jsonparser.serializable.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

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

    private void applyEntityDifferencesFromLastSniff(RabbitmqCachedComponent entity) {

    }

    private void pushEntityToMappingDS(RabbitmqCachedComponent entity) throws MappingDSException {
        log.debug("");
        log.debug("-----------------------------------");
        Cluster cluster = RabbitmqInjectorBootstrap.getMappingSce().getClusterSce().createCluster(entity.getComponentName());
        log.debug("Create or get cluster ({},{})", new Object[]{cluster.getClusterContainers(),entity.getComponentName()});

        log.debug("");
        log.debug("");
        log.debug("");
        ArrayList<Gate> clusterGates = new ArrayList<>();
        for (BrokerFromRabbitREST broker : entity.getBrokers()) {
            String adminGateUrl = broker.getUrl();
            String serverFQDN = adminGateUrl.split("://")[1].split(":")[0];
            String serverName = adminGateUrl.split("://")[1].split("\\.")[0];
            String adminGateName = "webadmingate." + serverName;

            log.debug("");
            log.debug("-----------------------------------");
            Container rbqBroker = RabbitmqInjectorBootstrap.getMappingSce().getContainerSce().createContainer(adminGateUrl, adminGateName);
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
                log.debug("Add property {} to rabbitmq container {} : {}", new Object[]{key, adminGateUrl, value.toString()});
                rbqBroker.addContainerProperty(key, value);
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
                                                                                                         clusterTransport.getTransportID(), 0);
                                log.debug("Create or get link ({},{},{})", new Object[]{sourceEndpoint.getEndpointParentNode().getNodeID(),
                                                                                        targetEndpoint.getEndpointParentNode().getNodeID(),
                                                                                        clusterTransport.getTransportID()});
                            }
                    }
                    clusterGates.add(gate);
                }
            }

            cluster.addClusterContainer(rbqBroker);
        }

        log.debug("");
        log.debug("");
        log.debug("");
        List<Node> vhosts = new ArrayList<>();
        for (VhostFromRabbitREST vhost : entity.getVhosts()) {
            for (Container broker : cluster.getClusterContainers()) {
                log.debug("");
                log.debug("-----------------------------------");
                Node vHostNode = RabbitmqInjectorBootstrap.getMappingSce().getNodeSce().createNode(vhost.getName(), broker.getContainerID(), 0);
                log.debug("Create or get node for vhost ({},{},{})", new Object[]{vHostNode.getNodeID(), vhost.getName(), broker.getContainerID()});
                log.debug("");
                for (String propsKey : vhost.getProperties().keySet()) {
                    log.debug("Add property {} to rabbitmq node {} : {}", new Object[]{propsKey, vHostNode.getNodeName(), vhost.getProperties().get(propsKey).toString()});
                    vHostNode.addNodeProperty(propsKey, vhost.getProperties().get(propsKey));
                }
                vhosts.add(vHostNode);
            }
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
        for (QueueFromRabbitREST queue : entity.getQueues()) {
            for (Node vHost : vhosts) {
                if (queue.getVhost().equals(vHost.getNodeName())) {
                    log.debug("");
                    log.debug("-----------------------------------");
                    Node queueNode = RabbitmqInjectorBootstrap.getMappingSce().getNodeSce().createNode(queue.getName(), vHost.getNodeContainer().getContainerID(), vHost.getNodeID());
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
        for (ExchangeFromRabbitREST exchange : entity.getExchanges())
            for (Node vHost : vhosts)
                if (exchange.getVhost().equals(vHost.getNodeName())) {
                    log.debug("");
                    log.debug("-----------------------------------");
                    Node exchangeNode = RabbitmqInjectorBootstrap.getMappingSce().getNodeSce().createNode(exchange.getName(), vHost.getNodeContainer().getContainerID(), vHost.getNodeID());
                    log.debug("Create or get node for exchange ({},{},{},{})", new Object[]{exchangeNode.getNodeID(), exchange.getName(), vHost.getNodeContainer().getContainerID(), vHost.getNodeID()});
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

        for (BindingFromRabbitREST binding : entity.getBindings()) {
            log.debug("");
            log.debug("binding {} on {} : {}", new Object[]{binding.getName(), binding.getVhost(), binding.getProperties().toString()});
            String bindingDestinationType = (String) binding.getProperties().get(BindingFromRabbitREST.JSON_RABBITMQ_BINDING_DESTINATION_TYPE);

            for (Node exchange : exchanges) {
                if (exchange.getNodeName().equals(binding.getProperties().get(BindingFromRabbitREST.JSON_RABBITMQ_BINDING_SOURCE)) &&
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

                            if (queue.getNodeName().equals(binding.getProperties().get(BindingFromRabbitREST.JSON_RABBITMQ_BINDING_DESTINATION)) &&
                                queue.getNodeParentNode().getNodeName().equals(binding.getVhost()) &&
                                exchange.getNodeContainer().equals(queue.getNodeContainer())) {
                                log.debug("");
                                log.debug("target queue ({},{},{},{})", new Object[]{queue.getNodeID(), queue.getNodeName(), queue.getNodeParentNode().getNodeName(),
                                                                                     queue.getNodeContainer().getContainerProperties().get(RABBITMQ_BROKER_NAME_KEY)});

                                String exchangeSourceEndpointURL= null;
                                String queueTargetEndpointURL = null;
                                String routingKey = (String)binding.getProperties().get(BindingFromRabbitREST.JSON_RABBITMQ_BINDING_ROUNTING_KEY);
                                log.debug("exchangeType : {} ; routingKey : {}", new Object[]{exchangeType, routingKey});

                                if (exchangeType.equals(ExchangeFromRabbitREST.RABBITMQ_EXCHANGE_TYPE_DIRECT)) {
                                    exchangeSourceEndpointURL = RABBITMQ_TRANSPORT_MEM_BINDING + exchange.getNodeContainer().getContainerProperties().get(RABBITMQ_BROKER_NAME_KEY) + "/" +
                                                                exchange.getNodeName() + "/" + routingKey;
                                    queueTargetEndpointURL = RABBITMQ_TRANSPORT_MEM_BINDING + exchange.getNodeContainer().getContainerProperties().get(RABBITMQ_BROKER_NAME_KEY) + "/" +
                                                             queue.getNodeName() + "/" + routingKey;
                                } else if (exchangeType.equals(ExchangeFromRabbitREST.RABBITMQ_EXCHANGE_TYPE_FANOUT)) {
                                    exchangeSourceEndpointURL = RABBITMQ_TRANSPORT_MEM_BINDING + exchange.getNodeContainer().getContainerProperties().get(RABBITMQ_BROKER_NAME_KEY) + "/" +
                                                                exchange.getNodeName();
                                    queueTargetEndpointURL = RABBITMQ_TRANSPORT_MEM_BINDING + exchange.getNodeContainer().getContainerProperties().get(RABBITMQ_BROKER_NAME_KEY) + "/" +
                                                             queue.getNodeName();
                                } else if (exchangeType.equals(ExchangeFromRabbitREST.RABBITMQ_EXCHANGE_TYPE_TOPIC)) {
                                    exchangeSourceEndpointURL = RABBITMQ_TRANSPORT_MEM_BINDING + exchange.getNodeContainer().getContainerProperties().get(RABBITMQ_BROKER_NAME_KEY) + "/" +
                                                                exchange.getNodeName() + "/" + routingKey;
                                    queueTargetEndpointURL = RABBITMQ_TRANSPORT_MEM_BINDING + exchange.getNodeContainer().getContainerProperties().get(RABBITMQ_BROKER_NAME_KEY) + "/" +
                                                             queue.getNodeName() + "/" + routingKey;
                                } else if (exchangeType.equals(ExchangeFromRabbitREST.RABBITMQ_EXCHANGE_TYPE_HEADER)) {
                                    exchangeSourceEndpointURL = RABBITMQ_TRANSPORT_MEM_BINDING + exchange.getNodeContainer().getContainerProperties().get(RABBITMQ_BROKER_NAME_KEY) + "/" +
                                                                exchange.getNodeName();
                                    queueTargetEndpointURL = RABBITMQ_TRANSPORT_MEM_BINDING + exchange.getNodeContainer().getContainerProperties().get(RABBITMQ_BROKER_NAME_KEY) + "/" +
                                                             queue.getNodeName();
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
                                                                                                      transport.getTransportID(), 0);
                                    log.debug("Link endpoints together ({},{},{},{}).", new Object[]{link.getLinkID(), sourceEp.getEndpointID(),
                                                                                                     targetEp.getEndpointID(), transport.getTransportID()});
                                }
                            }
                        }

                    } else if (bindingDestinationType.equals(BindingFromRabbitREST.RABBITMQ_BINDING_DESTINATION_TYPE_E)) {
                        for (Node exchangeToLink : exchanges) {
                            if (exchangeToLink.getNodeName().equals(binding.getProperties().get(BindingFromRabbitREST.JSON_RABBITMQ_BINDING_DESTINATION)) &&
                                exchangeToLink.getNodeParentNode().getNodeName().equals(binding.getVhost()) &&
                                exchange.getNodeContainer().equals(exchangeToLink.getNodeContainer())) {

                                log.debug("");
                                log.debug("target exchange ({},{},{},{})", new Object[]{exchangeToLink.getNodeID(), exchangeToLink.getNodeName(),
                                                                                        exchangeToLink.getNodeParentNode().getNodeName(),
                                                                                        exchangeToLink.getNodeContainer().getContainerProperties().get(RABBITMQ_BROKER_NAME_KEY)});

                                String exchangeSourceEndpointURL = null;
                                String exchangeTargetEndpointURL = null;
                                String routingKey = (String)binding.getProperties().get(BindingFromRabbitREST.JSON_RABBITMQ_BINDING_ROUNTING_KEY);

                                if (exchangeType.equals(ExchangeFromRabbitREST.RABBITMQ_EXCHANGE_TYPE_DIRECT)) {
                                    exchangeSourceEndpointURL = RABBITMQ_TRANSPORT_MEM_BINDING + exchange.getNodeContainer().getContainerProperties().get(RABBITMQ_BROKER_NAME_KEY) + "/" +
                                                                exchange.getNodeName() + "/" + routingKey;
                                    exchangeTargetEndpointURL = RABBITMQ_TRANSPORT_MEM_BINDING + exchange.getNodeContainer().getContainerProperties().get(RABBITMQ_BROKER_NAME_KEY) + "/" +
                                                                exchangeToLink.getNodeName() + "/" + routingKey;
                                } else if (exchangeType.equals(ExchangeFromRabbitREST.RABBITMQ_EXCHANGE_TYPE_FANOUT)) {
                                    exchangeSourceEndpointURL = RABBITMQ_TRANSPORT_MEM_BINDING + exchange.getNodeContainer().getContainerProperties().get(RABBITMQ_BROKER_NAME_KEY) + "/" +
                                                                exchange.getNodeName();
                                    exchangeTargetEndpointURL = RABBITMQ_TRANSPORT_MEM_BINDING + exchange.getNodeContainer().getContainerProperties().get(RABBITMQ_BROKER_NAME_KEY) + "/" +
                                                                exchangeToLink.getNodeName();
                                } else if (exchangeType.equals(ExchangeFromRabbitREST.RABBITMQ_EXCHANGE_TYPE_TOPIC)) {
                                    exchangeSourceEndpointURL = RABBITMQ_TRANSPORT_MEM_BINDING + exchange.getNodeContainer().getContainerProperties().get(RABBITMQ_BROKER_NAME_KEY) + "/" +
                                                                exchange.getNodeName() + "/" + routingKey;
                                    exchangeTargetEndpointURL = RABBITMQ_TRANSPORT_MEM_BINDING + exchange.getNodeContainer().getContainerProperties().get(RABBITMQ_BROKER_NAME_KEY) + "/" +
                                                                exchangeToLink.getNodeName() + "/" + routingKey;
                                } else if (exchangeType.equals(ExchangeFromRabbitREST.RABBITMQ_EXCHANGE_TYPE_HEADER)) {
                                    exchangeSourceEndpointURL = RABBITMQ_TRANSPORT_MEM_BINDING + exchange.getNodeContainer().getContainerProperties().get(RABBITMQ_BROKER_NAME_KEY) + "/" +
                                                                exchange.getNodeName();
                                    exchangeTargetEndpointURL = RABBITMQ_TRANSPORT_MEM_BINDING + exchange.getNodeContainer().getContainerProperties().get(RABBITMQ_BROKER_NAME_KEY) + "/" +
                                                                exchangeToLink.getNodeName();
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
                                                                                                             transport.getTransportID(), 0);
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
        for (ConnectionFromRabbitREST connectionFromRabbitREST : entity.getConnections()) {

            Container rbqBroker = null;
            for (Container broker : cluster.getClusterContainers()) {
                if (broker.getContainerProperties().get(RABBITMQ_BROKER_NAME_KEY).equals(connectionFromRabbitREST.getProperties().get(ConnectionFromRabbitREST.JSON_RABBITMQ_CONNECTION_NODE))) {
                    rbqBroker = broker;
                    break;
                }
            }

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

                if (remoteCliPGURL!=null && remoteCliOSI!=null && remoteCliOTM!=null && remoteCliAPP!=null && remoteCliCMP!=null) {
                    String serverFQDN = remoteCliPGURL.split("://")[1].split(":")[0];
                    String serverName = remoteCliPGURL.split("://")[1].split("\\.")[0];
                    String adminGateName = "rbqcliadmingate." + serverName;

                    log.debug("");
                    log.debug("---");
                    Container rbqClient = RabbitmqInjectorBootstrap.getMappingSce().getContainerSce().createContainer(remoteCliPGURL, adminGateName);
                    log.debug("Create or get container ({},{},{})", new Object[]{rbqClient.getContainerID(), remoteCliPGURL, adminGateName});
                    rbqClient.setContainerCompany(remoteCliCMP);
                    rbqClient.setContainerProduct((String)connection_client_props.get(ConnectionFromRabbitREST.JSON_RABBITMQ_CONNECTION_CLIENT_PROPERTIES_PRODUCT));
                    rbqClient.setContainerType(remoteCliAPP);

                    HashMap<String, Object> rbqClientProps = RabbitmqInjectorBootstrap.getRabbitmqDirectorySce().getRemoteClientContainerProperties(remoteCliOSI, remoteCliOTM);
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

                    for (ChannelFromRabbitREST channelFromRabbitREST : entity.getChannels()) {
                        HashMap<String, Object> connectionDetails = (HashMap<String, Object>)channelFromRabbitREST.getProperties().get(ChannelFromRabbitREST.JSON_RABBITMQ_CHANNEL_CONNECTION_DETAILS);
                        if (connectionFromRabbitREST.getName().equals(connectionDetails.get("name"))) {
                            String channelName = channelFromRabbitREST.getName();
                            String channelNumber = channelName.split("\\(")[1].split("\\)")[0];

                            ArrayList<HashMap<String, Object>> consumers_details = (ArrayList)channelFromRabbitREST.getProperties().get(ChannelFromRabbitREST.JSON_RABBITMQ_CHANNEL_CONSUMER_DETAILS);
                            for (HashMap<String, Object> consumerDetails : consumers_details) {
                                HashMap<String, Object> queue_details = (HashMap) consumerDetails.get(ChannelFromRabbitREST.JSON_RABBITMQ_CHANNEL_CONSUMER_DETAILS_QUEUE);
                                String consumerTag = (String) consumerDetails.get(ChannelFromRabbitREST.JSON_RABBITMQ_CHANNEL_CONSUMER_DETAILS_CONSUMER_TAG);
                                String queueName = (String)queue_details.get(ChannelFromRabbitREST.JSON_RABBITMQ_CHANNEL_CONSUMER_DETAILS_QUEUE_NAME);
                                String vhostName = (String)queue_details.get(ChannelFromRabbitREST.JSON_RABBITMQ_CHANNEL_CONSUMER_DETAILS_QUEUE_VHOST);
                                log.debug("---");
                                log.debug("channel consumer tag : {}", consumerTag);
                                log.debug("channel consumer queue : {} on {}", new Object[]{queueName, vhostName});

                                for (Node queue : queues) {
                                    if (queue.getNodeName().equals(queueName) && queue.getNodeParentNode().getNodeName().equals(vhostName) &&
                                        queue.getNodeContainer().equals(rbqBroker)) {

                                        String consumerNodeName = queueName + " consumer" ;
                                        Node consumerNode = RabbitmqInjectorBootstrap.getMappingSce().getNodeSce().createNode(consumerNodeName, rbqClient.getContainerID(), 0);
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
                                                                                                                      transport.getTransportID(), 0);
                                        log.debug("Create or get link : ({},{},{},{})", new Object[]{link.getLinkID(), sourceEp.getEndpointID(),
                                                                                                     targetEp.getEndpointID(), transport.getTransportID()});

                                        break;
                                    }
                                }
                            }
                            ArrayList<HashMap<String, Object>> publishes = (ArrayList)channelFromRabbitREST.getProperties().get(ChannelFromRabbitREST.JSON_RABBITMQ_CHANNEL_PUBLISHES);
                            for (HashMap<String, Object> publish: publishes) {
                                HashMap<String, Object> targetExchange = (HashMap)publish.get(ChannelFromRabbitREST.JSON_RABBITMQ_CHANNEL_PUBLISHES_EXCHANGE);
                                String exchangeName = (String) targetExchange.get(ChannelFromRabbitREST.JSON_RABBITMQ_CHANNEL_PUBLISHES_EXCHANGE_NAME);
                                String exchangeVhost = (String) targetExchange.get(ChannelFromRabbitREST.JSON_RABBITMQ_CHANNEL_PUBLISHES_EXCHANGE_VHOST);
                                log.debug("---");
                                log.debug("channel publisher exchange name: {}", exchangeName);
                                log.debug("channel publisher exchange vhost: {}", exchangeVhost);

                                for (Node exchange : exchanges) {
                                    if (exchange.getNodeName().equals(exchangeName) && exchange.getNodeParentNode().getNodeName().equals(exchangeVhost) &&
                                        exchange.getNodeContainer().equals(rbqBroker)) {

                                        String publisherNodeName = exchangeName + " publisher";
                                        Node publisherNode = RabbitmqInjectorBootstrap.getMappingSce().getNodeSce().createNode(publisherNodeName, rbqClient.getContainerID(), 0);
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
                                                                                                                      transport.getTransportID(), 0);
                                        log.debug("Create or get link : ({},{},{},{})", new Object[]{link.getLinkID(), sourceEp.getEndpointID(),
                                                                                                     targetEp.getEndpointID(), transport.getTransportID()});

                                        break;
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
                               ConnectionFromRabbitREST.JSON_RABBITMQ_CONNECTION_CLIENT_PROPERTIES_ARIANE_APP + ": " + remoteCliAPP);
            } else
                log.error("Unable to find target RabbitMQ node for connection {}", connectionFromRabbitREST.getName());
        }
    }

    private void removeEntityFromMappingDS(RabbitmqCachedComponent entity) {

    }

    private void inject(RabbitmqCachedComponent entity) {
        try {
            log.error("Injection begin... Action {} on {}", new Object[]{entity.getNextAction(), entity.getComponentURL()});
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
            log.error("Injection end...");
            RabbitmqInjectorBootstrap.getMappingSce().commit();
        } catch (Exception E) {
            String msg = "Exception catched while injecting RabbitMQ data into DB... Rollback injection !";
            E.printStackTrace();
            log.error(msg);
            RabbitmqInjectorBootstrap.getMappingSce().rollback();
        }
    }

    @Override
    public void onReceive(Object rabbitmqComponent) throws Exception {
        log.debug("Actor {} receive message {}", new Object[]{getSelf().path().toStringWithoutAddress(), rabbitmqComponent.toString()});
        if (rabbitmqComponent instanceof RabbitmqCachedComponent) {
            inject((RabbitmqCachedComponent)rabbitmqComponent);
        } else {
            log.debug("Unhandled message type {} !", rabbitmqComponent.getClass().toString());
            unhandled(rabbitmqComponent);
        }
    }
}