/**
 * RabbitMQ plugin injector bundle
 * CacheGears controller
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

import net.echinopsii.ariane.community.core.injector.base.model.Gear;
import net.echinopsii.ariane.community.plugin.rabbitmq.injector.RabbitmqInjectorBootstrap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class CacheGears implements Serializable, Runnable{

    private static final Logger log = LoggerFactory.getLogger(CacheGears.class);

    private boolean running = false;
    private Thread  thread  ;

    private List<Gear> gearList;
    private List<Gear> filteredGearList;

    private SelectItem[] statusSelectOptions;

    @PostConstruct
    public void init() {
        gearList = new ArrayList<Gear>();

        if (RabbitmqInjectorBootstrap.getGearsRegisry()!=null) {
            for (String key: RabbitmqInjectorBootstrap.getGearsRegisry().keySetFromPrefix(RabbitmqInjectorBootstrap.INJ_TREE_ROOT_PATH))
                gearList.add(RabbitmqInjectorBootstrap.getGearsRegisry().getEntityFromCache(key));
        }

        statusSelectOptions = new SelectItem[3];
        statusSelectOptions[0] = new SelectItem("","Select");
        statusSelectOptions[1] = new SelectItem("Started","Started");
        statusSelectOptions[2] = new SelectItem("Stopped","Stopped");

        thread = new Thread(this);
        thread.start();
    }

    @PreDestroy
    public void clear(){
        running = false;
        if (thread!=null) {
            thread.interrupt();
            while(thread.isAlive()) {
                log.info("CacheGears controller associated to session {} is stopping...", FacesContext.getCurrentInstance().getExternalContext().getSession(false));
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
            }
            thread = null;
        }
        gearList.clear();
        log.info("CacheGears controller associated to session {} is stopped...", FacesContext.getCurrentInstance().getExternalContext().getSession(false));
    }

    private void refresh() {
        gearList.clear();
        if (RabbitmqInjectorBootstrap.getGearsRegisry()!=null) {
            for (String key: RabbitmqInjectorBootstrap.getGearsRegisry().keySetFromPrefix(RabbitmqInjectorBootstrap.INJ_TREE_ROOT_PATH))
                gearList.add(RabbitmqInjectorBootstrap.getGearsRegisry().getEntityFromCache(key));
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
                    log.info("CacheGears controller associated to session {} is stopping...", FacesContext.getCurrentInstance().getExternalContext().getSession(false));
            }
        }
    }

    public List<Gear> getGearList() {
        return gearList;
    }

    public List<Gear> getFilteredGearList() {
        return filteredGearList;
    }

    public void setFilteredGearList(List<Gear> filteredGearList) {
        this.filteredGearList = filteredGearList;
    }

    public SelectItem[] getStatusSelectOptions() {
        return statusSelectOptions;
    }

    public String isRunning(Gear gear) {
        if (gear.isRunning()) return "Started"; else return "Stopped";
    }

    public String statusColor(Gear gear) {
        if (gear.isRunning()) return "008000"; else return "800000";
    }

    public void start(Gear gear) {
        gear.start();
        FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO,
                                            "RabbitMQ injector gear started !",
                                            "RabbitMQ injector name : " + gear.getGearName());
        FacesContext.getCurrentInstance().addMessage(null, msg);
    }

    public void stop(Gear gear) {
        gear.stop();
        FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO,
                                            "RabbitMQ injector gear stopped !",
                                            "RabbitMQ injector name : " + gear.getGearName());
        FacesContext.getCurrentInstance().addMessage(null, msg);
    }
}