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
import net.echinopsii.ariane.community.core.mapping.ds.domain.Cluster;
import net.echinopsii.ariane.community.core.mapping.ds.domain.Container;
import net.echinopsii.ariane.community.core.mapping.ds.domain.Node;
import net.echinopsii.ariane.community.plugin.rabbitmq.injector.RabbitmqInjectorBootstrap;
import net.echinopsii.ariane.community.plugin.rabbitmq.injector.cache.RabbitmqCachedComponent;
import net.echinopsii.ariane.community.plugin.rabbitmq.injector.runtime.gears.MappingGear;
import net.echinopsii.ariane.community.plugin.rabbitmq.jsonparser.serializable.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    private static final String RABBITMQ_TYPE     = "Message Broker";

    private static final String RABBITMQ_NAME_KEY = "name";

    private void applyEntityDifferencesFromLastSniff(RabbitmqCachedComponent entity) {



    }

    private void pushEntityToMappingDS(RabbitmqCachedComponent entity) throws MappingDSException {
        Cluster cluster = RabbitmqInjectorBootstrap.getMappingSce().getClusterSce().createCluster(entity.getComponentName());

        for (BrokerFromRabbitREST broker : entity.getBrokers()) {
            String url = broker.getUrl();
            String gateName = "webadmingate." + url.split("://")[1].split("\\.")[0];
            Container rbqBroker = RabbitmqInjectorBootstrap.getMappingSce().getContainerSce().createContainer(url, gateName);
            rbqBroker.setContainerCompany(RABBITMQ_COMPANY);
            rbqBroker.setContainerProduct(RABBITMQ_PRODUCT);
            rbqBroker.setContainerType(RABBITMQ_TYPE);

            rbqBroker.addContainerProperty(RABBITMQ_NAME_KEY, broker.getName());
            Map<String, Object> props = entity.getComponentProperties().get(broker.getName());
            for (String key : props.keySet()) {
                Object value =  props.get(key);
                log.debug("Add property {} to rabbitmq container {} : {}", new Object[]{key, url, value.toString()});
                rbqBroker.addContainerProperty(key, value);
            }

            cluster.addClusterContainer(rbqBroker);
        }

        List<Node> vhosts = new ArrayList<Node>();
        for (VhostFromRabbitREST vhost : entity.getVhosts()) {
            for (Container broker : cluster.getClusterContainers()) {
                Node vhostNode = RabbitmqInjectorBootstrap.getMappingSce().getNodeSce().createNode(vhost.getName(), broker.getContainerID(), 0);
                for (String propsKey : vhost.getProperties().keySet()) {
                    log.debug("Add property {} to rabbitmq node {} : {}", new Object[]{propsKey, vhostNode.getNodeName(), vhost.getProperties().get(propsKey).toString()});
                    vhostNode.addNodeProperty(propsKey, vhost.getProperties().get(propsKey));
                }
                vhosts.add(vhostNode);
            }
        }

        if (vhosts.size()>1)
            for (Node vhost : vhosts)
                for (Node twinVhost : vhosts)
                    if (!twinVhost.equals(vhost))
                        vhost.addTwinNode(twinVhost);

        List<Node> queues = new ArrayList<Node>();
        for (QueueFromRabbitREST queue : entity.getQueues()) {
            for (Node vhost : vhosts) {
                if (queue.getVhost().equals(vhost.getNodeName())) {
                    Node queueNode = RabbitmqInjectorBootstrap.getMappingSce().getNodeSce().createNode(queue.getName(), vhost.getNodeContainer().getContainerID(), vhost.getNodeID());
                    for (String propsKey : queue.getProperties().keySet())
                        queueNode.addNodeProperty(propsKey, queue.getProperties().get(propsKey));
                    queues.add(queueNode);
                    vhost.addNodeChildNode(queueNode);
                }
            }
        }

        if (queues.size()>1)
            for (Node queue : queues)
                for (Node twinQueue : queues)
                    if (!twinQueue.equals(queue) && twinQueue.getNodeName().equals(queue.getNodeName()))
                        queue.addTwinNode(twinQueue);

        List<Node> exchanges = new ArrayList<Node>();
        for (ExchangeFromRabbitREST exchange : entity.getExchanges()) {
            for (Node vhost : vhosts) {
                if (exchange.getVhost().equals(vhost.getNodeName())) {
                    Node exchangeNode = RabbitmqInjectorBootstrap.getMappingSce().getNodeSce().createNode(exchange.getName(), vhost.getNodeContainer().getContainerID(), vhost.getNodeID());
                    for (String propsKey : exchange.getProperties().keySet())
                        exchangeNode.addNodeProperty(propsKey, exchange.getProperties().get(propsKey));
                    exchanges.add(exchangeNode);
                    vhost.addNodeChildNode(exchangeNode);
                }
            }
        }

        //for (ConnectionFromRabbitREST connectionFromRabbitREST : entity.getConnections()) {
        //    for (ChannelFromRabbitREST channelFromRabbitREST : entity.getChannels()) {
        //
        //    }
        //}
    }

    private void removeEntityFromMappingDS(RabbitmqCachedComponent entity) {

    }

    private void inject(RabbitmqCachedComponent entity) {
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