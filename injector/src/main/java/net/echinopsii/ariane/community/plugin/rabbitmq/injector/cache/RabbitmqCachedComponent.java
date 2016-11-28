/**
 * RabbitMQ plugin injector bundle
 * RabbitMQ plugin injector RabbitMQ cached component
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

package net.echinopsii.ariane.community.plugin.rabbitmq.injector.cache;

import net.echinopsii.ariane.community.core.injector.base.model.AbstractComponent;
import net.echinopsii.ariane.community.core.injector.base.model.Component;
import net.echinopsii.ariane.community.plugin.rabbitmq.directory.RabbitmqDirectoryService;
import net.echinopsii.ariane.community.plugin.rabbitmq.directory.model.RabbitmqCluster;
import net.echinopsii.ariane.community.plugin.rabbitmq.directory.model.RabbitmqNode;
import net.echinopsii.ariane.community.plugin.rabbitmq.injector.RabbitmqInjectorBootstrap;
import net.echinopsii.ariane.community.plugin.rabbitmq.injector.runtime.gears.ComponentGear;
import net.echinopsii.ariane.community.plugin.rabbitmq.jsonparser.serializable.*;
import net.echinopsii.ariane.community.plugin.rabbitmq.jsonparser.tools.RabbitClusterToConnect;
import net.echinopsii.ariane.community.plugin.rabbitmq.jsonparser.tools.RabbitNodeToConnect;
import net.echinopsii.ariane.community.plugin.rabbitmq.jsonparser.tools.RabbitRESTTools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.*;

public class RabbitmqCachedComponent extends AbstractComponent implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final Logger log = LoggerFactory.getLogger(RabbitmqCachedComponent.class);

    public static final String RABBIT_MQ_CACHED_CMP_TYPE_CLUSTER  = "RabbitMQ Cluster";
    public static final String RABBIT_MQ_CACHED_CMP_TYPE_FCLUSTER = "RabbitMQ Fake Cluster";
    public static final String RABBIT_MQ_CACHED_CMP_TYPE_SNODE    = "RabbitMQ Standalone Node";

    /*
     * RabbitMQ sniff tooling
     */
    //cluster def null if standalone node
    private transient Set<RabbitmqNode> clusterNodes = null;
    private transient RabbitClusterToConnect clusterToConnect = null;
    private ClusterFromRabbitREST          cluster     = null;
    private List<BrokerFromRabbitREST>     brokers     = new ArrayList<>();

    //standalone def null if clustered node
    private transient RabbitmqNode        standaloneNode = null;
    private transient RabbitNodeToConnect nodeToConnect  = null;
    private BrokerFromRabbitREST          broker         = null;

    private List<VhostFromRabbitREST>      vhosts      = new ArrayList<>();
    private List<ConnectionFromRabbitREST> connections = new ArrayList<>();
    private List<ChannelFromRabbitREST>    channels    = new ArrayList<>();
    private List<QueueFromRabbitREST>      queues      = new ArrayList<>();
    private List<ExchangeFromRabbitREST>   exchanges   = new ArrayList<>();
    private List<BindingFromRabbitREST>    bindings    = new ArrayList<>();

    //last cluster def empty if standalone node
    private ClusterFromRabbitREST          lastCluster     = null;
    private List<BrokerFromRabbitREST>     lastBrokers     = null;

    //last broker def null if cluster
    private BrokerFromRabbitREST           lastBroker      = null;

    private List<VhostFromRabbitREST>      lastVhosts      = null;
    private List<ConnectionFromRabbitREST> lastConnections = null;
    private List<ChannelFromRabbitREST>    lastChannels    = null;
    private List<QueueFromRabbitREST>      lastQueues      = null;
    private List<ExchangeFromRabbitREST>   lastExchanges   = null;
    private List<BindingFromRabbitREST>    lastBindings    = null;

    public ClusterFromRabbitREST getCluster() {
        return cluster;
    }

    public List<BrokerFromRabbitREST> getBrokers() {
        return brokers;
    }

    public BrokerFromRabbitREST getBroker() {
        return broker;
    }

    public List<VhostFromRabbitREST> getVhosts() {
        return vhosts;
    }

    public List<ConnectionFromRabbitREST> getConnections() {
        return connections;
    }

    public List<ChannelFromRabbitREST> getChannels() {
        return channels;
    }

    public List<QueueFromRabbitREST> getQueues() {
        return queues;
    }

    public List<ExchangeFromRabbitREST> getExchanges() {
        return exchanges;
    }

    public List<BindingFromRabbitREST> getBindings() {
        return bindings;
    }

    public ClusterFromRabbitREST getLastCluster() {
        return lastCluster;
    }

    public List<BrokerFromRabbitREST> getLastBrokers() {
        return lastBrokers;
    }

    public List<VhostFromRabbitREST> getLastVhosts() {
        return lastVhosts;
    }

    public List<ConnectionFromRabbitREST> getLastConnections() {
        return lastConnections;
    }

    public List<ChannelFromRabbitREST> getLastChannels() {
        return lastChannels;
    }

    public List<QueueFromRabbitREST> getLastQueues() {
        return lastQueues;
    }

    public List<ExchangeFromRabbitREST> getLastExchanges() {
        return lastExchanges;
    }

    public List<BindingFromRabbitREST> getLastBindings() {
        return lastBindings;
    }

    /*
     * RabbitmqCachedComponent cache part implementation
     */
    private Long   componentDirectoryID;
    private String componentId;
    private String componentName;
    private String componentType;
    private String componentURL;
    private transient Map<String,Map<String,Object>> componentProperties;

    public RabbitmqCachedComponent setRabbitmqComponentFields(RabbitmqNode rabbitmqComponent) {
        this.standaloneNode = rabbitmqComponent;
        this.nodeToConnect = new RabbitNodeToConnect(rabbitmqComponent.getName(), rabbitmqComponent.getUrl(),
                                                     rabbitmqComponent.getUser(), rabbitmqComponent.getPasswd());

        //TODO: check if the standalone node provided from directory is really standalone !

        this.componentDirectoryID = rabbitmqComponent.getId();
        this.componentId   = RabbitmqInjectorBootstrap.INJ_TREE_ROOT_PATH+"_"+rabbitmqComponent.getName()+"_standalone_";
        this.componentName = rabbitmqComponent.getName();
        this.componentType = RABBIT_MQ_CACHED_CMP_TYPE_SNODE;

        this.broker = new BrokerFromRabbitREST(this.standaloneNode.getName(), nodeToConnect).parse();
        this.broker.setUrl(this.standaloneNode.getUrl());

        log.debug("Will sniff from : {}", this.nodeToConnect.getName());
        this.componentURL  = (this.nodeToConnect!=null) ? this.nodeToConnect.getUrl() : "";
        if (this.componentProperties==null)
            this.componentProperties = new HashMap<>();

        return this;
    }

    public RabbitmqCachedComponent setRabbitmqComponentFields(RabbitmqCluster rabbitmqComponent) {
        if (clusterToConnect == null)
            clusterToConnect = new RabbitClusterToConnect(rabbitmqComponent.getName());
        else
            clusterToConnect.setName(rabbitmqComponent.getName());

        this.clusterNodes = RabbitmqInjectorBootstrap.getRabbitmqDirectorySce().getNodesFromCluster(rabbitmqComponent.getId());
        HashSet<RabbitNodeToConnect> clusterNodesToConnect = new HashSet<>();
        for (RabbitmqNode node : clusterNodes) {
            RabbitNodeToConnect nodeToConnect = new RabbitNodeToConnect(node.getName(), node.getUrl(),
                                                                        node.getUser(), node.getPasswd());
            nodeToConnect.setCluster(clusterToConnect);
            nodeToConnect.setUrl(node.getUrl());
            clusterNodesToConnect.add(nodeToConnect);
        }
        clusterToConnect.setNodesAndSelectOneForREST(clusterNodesToConnect);
        if (clusterToConnect.getErrors().size()>0)
            log.warn("Some errors are detected from cluster {} defined in Ariane Directory", clusterToConnect.getName());
        for (String errorShort : clusterToConnect.getErrors().keySet())
            log.warn(clusterToConnect.getErrors().get(errorShort));

        this.cluster = new ClusterFromRabbitREST(clusterToConnect).parse();

        if (rabbitmqComponent.getId() != RabbitmqDirectoryService.FAKE_CLUSTER_ID) {
            this.componentDirectoryID = rabbitmqComponent.getId();
            this.componentId   = RabbitmqInjectorBootstrap.INJ_TREE_ROOT_PATH+"_"+cluster.getName()+"_";
            this.componentName = cluster.getName();
            if (clusterNodesToConnect.size()>1)
                this.componentType = RABBIT_MQ_CACHED_CMP_TYPE_CLUSTER;
            else
                this.componentType = RABBIT_MQ_CACHED_CMP_TYPE_FCLUSTER;
        } else {
            //rabbitmqComponent.getNodes().length == 1 if component is a fake cluster
            for (RabbitmqNode node : RabbitmqInjectorBootstrap.getRabbitmqDirectorySce().getNodesFromCluster(rabbitmqComponent.getId())) {
                this.componentDirectoryID = node.getId();
                this.componentId   = RabbitmqInjectorBootstrap.INJ_TREE_ROOT_PATH+"_"+node.getName()+"_";
                this.componentName = node.getName();
            }
            this.componentType = RABBIT_MQ_CACHED_CMP_TYPE_SNODE;
        }

        RabbitNodeToConnect nodeToConnect = clusterToConnect.getSelectedNodeForREST();
        log.debug("Will sniff from : {}", (nodeToConnect != null) ? nodeToConnect.getName() : "null");
        this.componentURL  = (nodeToConnect!=null) ? nodeToConnect.getUrl() : "";
        if (this.componentProperties==null)
            this.componentProperties = new HashMap<>();

        return this;
    }

    @Override
    public String  getComponentId() {
        return componentId;
    }

    @Override
    public String getComponentName() {
        return componentName;
    }

    public String getComponentURL() {
        return componentURL;
    }

    public Map<String, Map<String, Object>> getComponentProperties() {
        return componentProperties;
    }

    @Override
    public String getComponentType() {
        return componentType;
    }

    private void sniffRuntime() {
        if (this.cluster!=null) {
            for (String nodeName : cluster.getNodes()) {
                BrokerFromRabbitREST tmp = new BrokerFromRabbitREST(nodeName, clusterToConnect).parse();
                if (!this.brokers.contains(tmp)) {
                    for (RabbitmqNode node : this.clusterNodes)
                        if (node.getName().equals(nodeName) && node.getProperties() != null)
                            tmp.getProperties().putAll(node.getProperties());
                    for (RabbitNodeToConnect nodeToConnect : clusterToConnect.getNodes())
                        if (nodeToConnect.getName().equals(nodeName))
                            tmp.setUrl(nodeToConnect.getUrl());
                    this.brokers.add(tmp);
                    Map<String, Object> nodeProperties = tmp.getProperties();
                    this.componentProperties.put(nodeName, nodeProperties);
                }
            }
        } else if (this.standaloneNode!=null) {
            if (this.broker==null)
                this.broker = new BrokerFromRabbitREST(standaloneNode.getName(), nodeToConnect).parse();
            else
                this.broker.parse();
            this.broker.setUrl(standaloneNode.getUrl());
            this.broker.getProperties().putAll(standaloneNode.getProperties());
            this.componentProperties.put(standaloneNode.getName(), this.broker.getProperties());
        }

        List<String> vhostNames = (clusterToConnect!=null) ? RabbitRESTTools.getVhostNames(clusterToConnect) :
                                  ((nodeToConnect!=null) ? RabbitRESTTools.getVhostNames(nodeToConnect): new ArrayList<String>());
        for (String vhostName : vhostNames) {
            VhostFromRabbitREST tmp = (clusterToConnect!=null) ? new VhostFromRabbitREST(vhostName, clusterToConnect).parse() :
                                              (nodeToConnect!=null) ? new VhostFromRabbitREST(vhostName, nodeToConnect).parse() : null;
            if (tmp!=null && !this.vhosts.contains(tmp))
                this.vhosts.add(tmp);
        }

        List<String> connectionNames = (clusterToConnect!=null) ? RabbitRESTTools.getConnectionNames(clusterToConnect) :
                                       ((nodeToConnect!=null) ? RabbitRESTTools.getConnectionNames(nodeToConnect): new ArrayList<String>());
        for (String connectionName : connectionNames) {
            ConnectionFromRabbitREST tmp = (clusterToConnect!=null) ? new ConnectionFromRabbitREST(connectionName, clusterToConnect).parse() :
                                                   (nodeToConnect!=null) ? new ConnectionFromRabbitREST(connectionName, nodeToConnect).parse() : null;
            if (tmp!=null && !this.connections.contains(tmp))
                this.connections.add(tmp);
        }

        List<String> channelNames = (clusterToConnect!=null) ? RabbitRESTTools.getChannelNames(clusterToConnect) :
                                    ((nodeToConnect!=null) ? RabbitRESTTools.getChannelNames(nodeToConnect): new ArrayList<String>());
        for (String channelName : channelNames) {
            ChannelFromRabbitREST tmp = (clusterToConnect!=null) ? new ChannelFromRabbitREST(channelName, clusterToConnect).parse() :
                                                (nodeToConnect!=null) ? new ChannelFromRabbitREST(channelName, nodeToConnect).parse() : null;
            if (tmp!=null && !this.channels.contains(tmp))
                this.channels.add(tmp);
        }

        Map<String, List<String>> exchangesListing = (clusterToConnect!=null) ? RabbitRESTTools.getExchangeNames(clusterToConnect) :
                                                     ((nodeToConnect!=null) ? RabbitRESTTools.getExchangeNames(nodeToConnect):new HashMap<String, List<String>>());
        for (String vhostName : exchangesListing.keySet()) {
            for (String exchangeName : exchangesListing.get(vhostName)) {
                ExchangeFromRabbitREST tmp = (clusterToConnect!=null) ? new ExchangeFromRabbitREST(exchangeName, vhostName, clusterToConnect).parse():
                                                     (nodeToConnect!=null) ? new ExchangeFromRabbitREST(exchangeName, vhostName, nodeToConnect).parse() : null;
                if (tmp!=null && !this.exchanges.contains(tmp))
                    this.exchanges.add(tmp);
            }
        }

        Map<String, List<String>> queuesListing = (clusterToConnect!=null) ? RabbitRESTTools.getQueueNames(clusterToConnect) :
                                                  ((nodeToConnect!=null) ? RabbitRESTTools.getQueueNames(nodeToConnect):new HashMap<String, List<String>>());
        for (String vhostName : queuesListing.keySet()) {
            for (String queueName : queuesListing.get(vhostName)) {
                QueueFromRabbitREST tmp = (clusterToConnect!=null) ? new QueueFromRabbitREST(queueName, vhostName, clusterToConnect).parse() :
                                                  (nodeToConnect!=null) ? new QueueFromRabbitREST(queueName, vhostName, nodeToConnect).parse() : null;
                if (tmp!=null && !this.queues.contains(tmp))
                    this.queues.add(tmp);
            }
        }

        Map<String, List<String>> bindingsListing = (clusterToConnect!=null) ? RabbitRESTTools.getBindingNames(clusterToConnect) :
                                                    ((nodeToConnect!=null) ? RabbitRESTTools.getBindingNames(nodeToConnect):new HashMap<String, List<String>>());
        for (String vhostName : bindingsListing.keySet()) {
            for (String bindingName : bindingsListing.get(vhostName)) {
                BindingFromRabbitREST tmp = (clusterToConnect!=null) ? new BindingFromRabbitREST(bindingName, vhostName, clusterToConnect).parse() :
                                                    (nodeToConnect!=null) ? new BindingFromRabbitREST(bindingName, vhostName, nodeToConnect).parse() : null;
                if (tmp!=null && !this.bindings.contains(tmp))
                    this.bindings.add(tmp);
            }
        }
    }

    private void cloneCurrentRuntime() {
        if (this.cluster!=null) this.lastCluster = this.cluster.clone();

        if (this.brokers!=null) {
            this.lastBrokers = new ArrayList<>(this.brokers);
            this.brokers.clear();
        }

        if (this.broker!=null) this.lastBroker = this.broker.clone();

        this.lastVhosts = new ArrayList<>(this.vhosts);
        this.vhosts.clear();

        this.lastConnections = new ArrayList<>(this.connections);
        this.connections.clear();

        this.lastChannels = new ArrayList<>(this.channels);
        this.channels.clear();

        this.lastExchanges = new ArrayList<>(this.exchanges);
        this.exchanges.clear();

        this.lastQueues = new ArrayList<>(this.queues);
        this.queues.clear();

        this.lastBindings = new ArrayList<>(this.bindings);
        this.bindings.clear();
    }

    public void rollback() {
        if (this.lastCluster!=null) this.cluster = this.lastCluster;
        if (this.lastBrokers!=null) this.brokers = this.lastBrokers;
        if (this.lastBroker!=null) this.broker = this.lastBroker;
        this.vhosts = this.lastVhosts;
        this.connections = this.lastConnections;
        this.channels = this.lastChannels;
        this.exchanges = this.lastExchanges;
        this.queues = this.lastQueues;
        this.bindings = this.lastBindings;
        super.setLastRefresh(new Date());
        RabbitmqInjectorBootstrap.getComponentsRegistry().putEntityToCache(this);
        super.setRefreshing(false);
    }

    @Override
    public void refresh() {
        super.setRefreshing(true);
        log.debug("Refresh component : " + this.componentName);
        if (cluster!=null) {
            RabbitmqCluster rabbitmqCluster = RabbitmqInjectorBootstrap.getRabbitmqDirectorySce().refreshRabbitmqCluster(componentDirectoryID);
            if (rabbitmqCluster != null) {
                setRabbitmqComponentFields(rabbitmqCluster);
                if (this.brokers.size() > 0)
                    super.setNextAction(Component.ACTION_UPDATE);
                else
                    super.setNextAction(Component.ACTION_CREATE);
            } else {
                super.setNextAction(Component.ACTION_DELETE);
            }
        } else if (broker!=null) {
            RabbitmqNode rabbitmqNode = RabbitmqInjectorBootstrap.getRabbitmqDirectorySce().refreshRabbitmqNode(componentDirectoryID);
            if (rabbitmqNode != null) {
                setRabbitmqComponentFields(rabbitmqNode);
                if (this.vhosts.size() > 0)
                    super.setNextAction(Component.ACTION_UPDATE);
                else
                    super.setNextAction(Component.ACTION_CREATE);
            } else {
                super.setNextAction(Component.ACTION_DELETE);
            }
        }

        log.debug("nextAction for {} : {}", new Object[]{componentName, super.getNextAction()});
        switch (super.getNextAction()) {
            case Component.ACTION_UPDATE:
                cloneCurrentRuntime();
                sniffRuntime();
                break;
            case Component.ACTION_CREATE:
                sniffRuntime();
                break;
            case Component.ACTION_DELETE:
                break;
            default:
                log.error("Unknown entity refresh code !");
                break;
        }

        super.setLastRefresh(new Date());
        RabbitmqInjectorBootstrap.getComponentsRegistry().putEntityToCache(this);
        super.setRefreshing(false);
    }

    @Override
    public void refreshAndMap() {
        log.debug("attached gear id {}", this.getAttachedGearId());
        ComponentGear attachedGear = ((ComponentGear)RabbitmqInjectorBootstrap.getGearsRegisry().getEntityFromCache(this.getAttachedGearId()));
        attachedGear.refresh();
    }
}