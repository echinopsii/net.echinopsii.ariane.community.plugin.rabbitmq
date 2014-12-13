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
import net.echinopsii.ariane.community.plugin.rabbitmq.directory.model.RabbitmqCluster;
import net.echinopsii.ariane.community.plugin.rabbitmq.directory.model.RabbitmqNode;
import net.echinopsii.ariane.community.plugin.rabbitmq.injector.RabbitmqInjectorBootstrap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;

public class RabbitmqCachedComponent extends AbstractComponent implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final Logger log = LoggerFactory.getLogger(RabbitmqCachedComponent.class);


    /*
     * RabbitmqCachedComponent cache part implementation
     */
    private Long componentDirectoryID;
    private String componentId;
    private String componentName;
    private String componentURL;
    private transient HashMap<String,Object> componentProperties;
    private transient RabbitmqNode rabbitmqNode;

    public RabbitmqCachedComponent setRabbitmqComponentFields(RabbitmqNode rabbitmqComponent) {
        this.componentDirectoryID = rabbitmqComponent.getId();
        this.componentId = RabbitmqInjectorBootstrap.INJ_TREE_ROOT_PATH+"_"+rabbitmqComponent.getName()+"_";
        this.componentName = rabbitmqComponent.getName();
        this.componentURL = rabbitmqComponent.getUrl();
        this.componentProperties = rabbitmqComponent.getProperties();
        this.rabbitmqNode = rabbitmqComponent;
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
        return "RabbitMQ Server";
    }

    @Override
    public void refresh() {
        super.setRefreshing(true);
        log.debug("Refresh component : " + this.componentURL);
        this.rabbitmqNode = RabbitmqInjectorBootstrap.getRabbitmqDirectorySce().refreshRabbitmqNode(this.rabbitmqNode);
        if (this.rabbitmqNode!=null) {
            setRabbitmqComponentFields(this.rabbitmqNode);
            super.setNextAction(Component.ACTION_CREATE);
        } else {
            super.setNextAction(Component.ACTION_DELETE);
        }
        log.debug("nextAction for {} : {}", componentName, super.getNextAction());

        RabbitmqCluster nodeCluster = RabbitmqInjectorBootstrap.getRabbitmqDirectorySce().getClusterFromNode(this.rabbitmqNode);
        switch (super.getNextAction()) {
            case Component.ACTION_UPDATE:
                break;
            case Component.ACTION_CREATE:
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