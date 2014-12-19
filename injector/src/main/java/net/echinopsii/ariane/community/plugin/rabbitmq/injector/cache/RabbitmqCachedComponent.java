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
import net.echinopsii.ariane.community.plugin.rabbitmq.jsonparser.serializable.*;
import net.echinopsii.ariane.community.plugin.rabbitmq.jsonparser.tools.RabbitClusterToConnect;
import net.echinopsii.ariane.community.plugin.rabbitmq.jsonparser.tools.RabbitNodeToConnect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class RabbitmqCachedComponent extends AbstractComponent implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final Logger log = LoggerFactory.getLogger(RabbitmqCachedComponent.class);

    /*
     * RabbitMQ sniff tooling
     */
    private transient RabbitClusterToConnect clusterToConnect = null;
    private ClusterFromRabbitREST    cluster     = null;
    private NodeFromRabbitREST       node        = null;
    private VhostFromRabbitREST      vhosts      = null;
    private ConnectionFromRabbitREST connections = null;
    private ChannelFromRabbitREST    channels    = null;
    private QueueFromRabbitREST      queues      = null;
    private ExchangeFromRabbitREST   exchanges   = null;

    /*
     * RabbitmqCachedComponent cache part implementation
     */
    private Long componentDirectoryID;
    private String componentId;
    private String componentName;
    private String componentType;
    private String componentURL;
    private transient HashMap<String,Object> componentProperties;

    public RabbitmqCachedComponent setRabbitmqComponentFields(RabbitmqCluster rabbitmqComponent) {
        clusterToConnect = new RabbitClusterToConnect(rabbitmqComponent.getName());
        log.debug("begin define cluster nodes list");
        Set<RabbitmqNode> clusterNodes = RabbitmqInjectorBootstrap.getRabbitmqDirectorySce().getNodesFromCluster(rabbitmqComponent.getId());
        HashSet<RabbitNodeToConnect> clusterNodesToConnect = new HashSet<RabbitNodeToConnect>();
        for (RabbitmqNode node : clusterNodes) {
            RabbitNodeToConnect nodeToConnect = new RabbitNodeToConnect(node.getName(), node.getUrl(), node.getUser(), node.getPasswd());
            nodeToConnect.setCluster(clusterToConnect);
            clusterNodesToConnect.add(nodeToConnect);
        }
        clusterToConnect.setNodes(clusterNodesToConnect);
        log.debug("end define cluster nodes list");

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
        this.componentURL  = clusterToConnect.getNodeOnRESTCli().getUrl();
        this.componentProperties = rabbitmqComponent.getProperties();
        log.debug("end setRbmqComponentFields");
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

    private void sniffConfiguration(RabbitmqCluster cluster) {
        //RabbitClusterToConnect clusterToConnect = new RabbitClusterToConnect();
        //clusterToConnect.s
        //this.node = new NodeFromRabbitREST(this.rabbitmqNode.getName(), cluster).parse();
        //log.debug(node.getProperties().toString());
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
                sniffConfiguration(rabbitmqCluster);
                break;
            case Component.ACTION_CREATE:
                sniffConfiguration(rabbitmqCluster);
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

    }
}