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

    /*
     * RabbitMQ sniff tooling
     */
    private transient RabbitClusterToConnect clusterToConnect = null;
    private ClusterFromRabbitREST          cluster     = null;
    private List<NodeFromRabbitREST>       nodes       = new ArrayList<NodeFromRabbitREST>();
    private List<VhostFromRabbitREST>      vhosts      = new ArrayList<VhostFromRabbitREST>();
    private List<ConnectionFromRabbitREST> connections = new ArrayList<ConnectionFromRabbitREST>();
    private List<ChannelFromRabbitREST>    channels    = new ArrayList<ChannelFromRabbitREST>();
    private List<QueueFromRabbitREST>      queues      = new ArrayList<QueueFromRabbitREST>();
    private List<ExchangeFromRabbitREST>   exchanges   = new ArrayList<ExchangeFromRabbitREST>();

    private ClusterFromRabbitREST          lastCluster     = null;
    private List<NodeFromRabbitREST>       lastNodes       = null;
    private List<VhostFromRabbitREST>      lastVhosts      = null;
    private List<ConnectionFromRabbitREST> lastConnections = null;
    private List<ChannelFromRabbitREST>    lastChannels    = null;
    private List<QueueFromRabbitREST>      lastQueues      = null;
    private List<ExchangeFromRabbitREST>   lastExchanges   = null;

    /*
     * RabbitmqCachedComponent cache part implementation
     */
    private Long   componentDirectoryID;
    private String componentId;
    private String componentName;
    private String componentType;
    private String componentURL;
    private transient HashMap<String,Object> componentProperties;

    public RabbitmqCachedComponent setRabbitmqComponentFields(RabbitmqCluster rabbitmqComponent) {
        if (clusterToConnect == null)
            clusterToConnect = new RabbitClusterToConnect(rabbitmqComponent.getName());

        Set<RabbitmqNode> clusterNodes = RabbitmqInjectorBootstrap.getRabbitmqDirectorySce().getNodesFromCluster(rabbitmqComponent.getId());
        HashSet<RabbitNodeToConnect> clusterNodesToConnect = new HashSet<RabbitNodeToConnect>();
        for (RabbitmqNode node : clusterNodes) {
            RabbitNodeToConnect nodeToConnect = new RabbitNodeToConnect(node.getName(), node.getUrl(),
                                                                        node.getUser(), node.getPasswd());
            nodeToConnect.setCluster(clusterToConnect);
            clusterNodesToConnect.add(nodeToConnect);
        }
        clusterToConnect.setNodes(clusterNodesToConnect);

        if (rabbitmqComponent.getId() != RabbitmqDirectoryService.FAKE_CLUSTER_ID) {
            this.componentDirectoryID = rabbitmqComponent.getId();
            this.componentType = "RabbitMQ Cluster";
        } else {
            //rabbitmqComponent.getNodes().length == 1 if component is a fake cluster
            for (RabbitmqNode node : RabbitmqInjectorBootstrap.getRabbitmqDirectorySce().getNodesFromCluster(rabbitmqComponent.getId()))
                this.componentDirectoryID = node.getId();
            this.componentType = "RabbitMQ Server";
        }

        this.componentId   = RabbitmqInjectorBootstrap.INJ_TREE_ROOT_PATH+"_"+rabbitmqComponent.getName()+"_";
        this.componentName = rabbitmqComponent.getName();
        RabbitNodeToConnect node = clusterToConnect.getNodeOnRESTCli();
        log.debug("Will sniff from : {}", (node!=null) ? node.getName() : "null");
        this.componentURL  = (node!=null) ? node.getUrl() : "";
        this.componentProperties = rabbitmqComponent.getProperties();
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

    public HashMap<String, Object> getComponentProperties() {
        return componentProperties;
    }

    @Override
    public String getComponentType() {
        return componentType;
    }

    private void sniffRuntime() {
        this.cluster = new ClusterFromRabbitREST(clusterToConnect).parse();
        if (this.cluster!=null) {
            for (String nodeName : cluster.getNodes()) {
                NodeFromRabbitREST tmp = new NodeFromRabbitREST(nodeName, clusterToConnect).parse();
                if (!this.nodes.contains(tmp))
                    this.nodes.add(tmp);
            }

            for (String vhostName : RabbitRESTTools.getVhostNames(clusterToConnect)) {
                VhostFromRabbitREST tmp = new VhostFromRabbitREST(vhostName, clusterToConnect).parse();
                if (!this.vhosts.contains(tmp))
                    this.vhosts.add(tmp);
            }

            for (String connectionName : RabbitRESTTools.getConnectionNames(clusterToConnect)) {
                ConnectionFromRabbitREST tmp = new ConnectionFromRabbitREST(connectionName, clusterToConnect).parse();
                if (!this.connections.contains(tmp))
                    this.connections.add(tmp);
            }

            for (String channelName : RabbitRESTTools.getChannelNames(clusterToConnect)) {
                ChannelFromRabbitREST tmp = new ChannelFromRabbitREST(channelName, clusterToConnect).parse();
                if (!this.channels.contains(tmp))
                    this.channels.add(tmp);
            }

            Map<String, List<String>> exchangesListing = RabbitRESTTools.getExchangeNames(clusterToConnect);
            for (String vhostName : exchangesListing.keySet()) {
                for (String exchangeName : exchangesListing.get(vhostName)) {
                    ExchangeFromRabbitREST tmp = new ExchangeFromRabbitREST(exchangeName, vhostName, clusterToConnect).parse();
                    if (!this.exchanges.contains(tmp))
                        this.exchanges.add(tmp);
                }
            }

            Map<String, List<String>> queuesListing = RabbitRESTTools.getQueueNames(clusterToConnect);
            for (String vhostName : queuesListing.keySet()) {
                for (String queueName : queuesListing.get(vhostName)) {
                    QueueFromRabbitREST tmp = new QueueFromRabbitREST(queueName, vhostName, clusterToConnect).parse();
                    if (!this.queues.contains(tmp))
                        this.queues.add(tmp);
                }
            }
        }
    }

    private void cloneCurrentRuntime() {
        this.lastCluster = this.cluster.clone();

        this.lastNodes = new ArrayList<NodeFromRabbitREST>(this.nodes);
        this.nodes.clear();

        this.lastVhosts = new ArrayList<VhostFromRabbitREST>(this.vhosts);
        this.vhosts.clear();

        this.lastConnections = new ArrayList<ConnectionFromRabbitREST>(this.connections);
        this.connections.clear();

        this.lastChannels = new ArrayList<ChannelFromRabbitREST>(this.channels);
        this.channels.clear();

        this.lastExchanges = new ArrayList<ExchangeFromRabbitREST>(this.exchanges);
        this.exchanges.clear();

        this.lastQueues = new ArrayList<QueueFromRabbitREST>(this.queues);
        this.queues.clear();
    }

    @Override
    public void refresh() {
        super.setRefreshing(true);
        log.debug("Refresh component : " + this.componentName);
        RabbitmqCluster rabbitmqCluster = RabbitmqInjectorBootstrap.getRabbitmqDirectorySce().refreshRabbitmqCluster(componentDirectoryID);
        if (rabbitmqCluster!=null) {
            setRabbitmqComponentFields(rabbitmqCluster);
            if (cluster != null)
                super.setNextAction(Component.ACTION_UPDATE);
            else
                super.setNextAction(Component.ACTION_CREATE);
        } else {
            super.setNextAction(Component.ACTION_DELETE);
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
                log.error("Unknown entity refresh !");
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