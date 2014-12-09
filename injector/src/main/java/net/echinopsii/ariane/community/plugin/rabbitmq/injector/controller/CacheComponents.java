/**
 * RabbitMQ plugin injector bundle
 * Cache components controller
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

package net.echinopsii.ariane.community.plugin.rabbitmq.injector.controller;

import net.echinopsii.ariane.community.core.injector.base.model.Component;
import net.echinopsii.ariane.community.plugin.rabbitmq.injector.RabbitmqInjectorBootstrap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class CacheComponents implements Serializable, Runnable{
    private static final Logger log = LoggerFactory.getLogger(CacheComponents.class);

    private boolean running = false;
    private Thread  thread  ;

    private List<Component> cachedEntityList;
    private List<Component> filteredCachedEntityList;

    @PostConstruct
    public void init() {
        cachedEntityList = new ArrayList<Component>();
        if (RabbitmqInjectorBootstrap.getComponentsRegistry()!=null) {
            for (String key: RabbitmqInjectorBootstrap.getComponentsRegistry().keySetFromPrefix(RabbitmqInjectorBootstrap.INJ_TREE_ROOT_PATH))
                cachedEntityList.add(RabbitmqInjectorBootstrap.getComponentsRegistry().getEntityFromCache(key));
        }
        thread = new Thread(this);
        thread.start();
    }

    @PreDestroy
    public void clear(){
        running = false;
        if (thread!=null) {
            thread.interrupt();
            while(thread.isAlive()) {
                log.info("Cache controller associated to session {} is stopping...", FacesContext.getCurrentInstance().getExternalContext().getSession(false));
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
            }
            thread = null;
        }
        cachedEntityList.clear();
        log.info("Cache controller associated to session {} is stopped...", FacesContext.getCurrentInstance().getExternalContext().getSession(false));
    }

    private void refresh() {
        cachedEntityList = new ArrayList<Component>();
        if (RabbitmqInjectorBootstrap.getComponentsRegistry()!=null) {
            for (String key: RabbitmqInjectorBootstrap.getComponentsRegistry().keySetFromPrefix(RabbitmqInjectorBootstrap.INJ_TREE_ROOT_PATH))
                cachedEntityList.add(RabbitmqInjectorBootstrap.getComponentsRegistry().getEntityFromCache(key));
        }
    }

    @Override
    public void run() {
        running = true;
        while(running) {
            refresh();
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                if (running)
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                else
                    log.info("Cache controller associated to session {} is stopping...", FacesContext.getCurrentInstance().getExternalContext().getSession(false));
            }
        }
    }

    public List<Component> getCachedEntityList() {
        return cachedEntityList;
    }

    public void setCachedEntityList(List<Component> cachedEntityList) {
        this.cachedEntityList = cachedEntityList;
    }

    public List<Component> getFilteredCachedEntityList() {
        return filteredCachedEntityList;
    }

    public void setFilteredCachedEntityList(List<Component> filteredCachedEntityList) {
        this.filteredCachedEntityList = filteredCachedEntityList;
    }

    public String getEntityName(Component entity) {
        return entity.getComponentName();
    }

    public String getEntityType(Component entity) {
        return entity.getComponentType();
    }

    public String getEntityLastRefresh(Component entity) {
        String ret = null;
        if (entity.isRefreshing()) {
            ret = "NOW !";
        } else {
            SimpleDateFormat ft = new SimpleDateFormat ("yyyy.MM.dd '-' hh:mm:ss a zzz");
            ret = ft.format(entity.getLastRefresh());
        }
        return ret;
    }

    public void refreshEntity(Component entity) {
        entity.refreshAndMap();
        FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO,
                                                   "RabbitMQ injector cache entity has been succesfully refreshed !",
                                                   "RabbitMQ injector cache entity name : " + getEntityName(entity));
        FacesContext.getCurrentInstance().addMessage(null, msg);
    }

    public void refreshCache() {
        RabbitmqInjectorBootstrap.getDirectoryAkkaGear().refresh();
        if (RabbitmqInjectorBootstrap.getComponentsRegistry()!=null) {
            for (String key: RabbitmqInjectorBootstrap.getComponentsRegistry().keySetFromPrefix(RabbitmqInjectorBootstrap.INJ_TREE_ROOT_PATH))
                RabbitmqInjectorBootstrap.getComponentsRegistry().getEntityFromCache(key).refreshAndMap();
        }

        FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO,
                                                   "RabbitMQ injector cache is refreshed !",
                                                   "RabbitMQ injector cache size : " + RabbitmqInjectorBootstrap.getComponentsRegistry().
                                                                                                                keySetFromPrefix(RabbitmqInjectorBootstrap.INJ_TREE_ROOT_PATH).size());
        FacesContext.getCurrentInstance().addMessage(null, msg);
    }
}