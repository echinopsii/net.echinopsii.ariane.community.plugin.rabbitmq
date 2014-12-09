/**
 * RabbitMQ plugin injector bundle
 * ConfigurationScheduling controller
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

import net.echinopsii.ariane.community.plugin.rabbitmq.injector.RabbitmqInjectorBootstrap;
import net.echinopsii.ariane.community.plugin.rabbitmq.injector.runtime.config.RabbitmqInjectorMainCfgLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import java.io.Serializable;
import java.util.Properties;

public class ConfigurationScheduling implements Serializable {

    private static final Logger log = LoggerFactory.getLogger(RabbitmqInjectorBootstrap.class);

    private int directoryQueryPeriod   = RabbitmqInjectorMainCfgLoader.getDirQI();
    private int rvComponentSniffPeriod = RabbitmqInjectorMainCfgLoader.getRbCompQI();

    public int getDirectoryQueryPeriod() {
        return directoryQueryPeriod;
    }

    public void setDirectoryQueryPeriod(int directoryQueryPeriod) {
        this.directoryQueryPeriod = directoryQueryPeriod;
    }

    public int getRvComponentSniffPeriod() {
        return rvComponentSniffPeriod;
    }

    public void setRvComponentSniffPeriod(int rvComponentSniffPeriod) {
        this.rvComponentSniffPeriod = rvComponentSniffPeriod;
        log.info("new rabbitmq component sniff period : {}", this.rvComponentSniffPeriod);
    }

    public void apply() {
        Properties properties = new Properties();
        properties.put(RabbitmqInjectorMainCfgLoader.RABBITMQ_INJECTOR_CFG_DIRECTORY_QUERYINTERVAL_KEY, directoryQueryPeriod);
        properties.put(RabbitmqInjectorMainCfgLoader.RABBITMQ_INJECTOR_CFG_COMPONENT_SNIFFINTERVAL_KEY, rvComponentSniffPeriod);

        log.info("Update RabbitMQ Injector with conf {}", properties.toString());
        RabbitmqInjectorBootstrap.updated(properties);
        FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO,
                                            "RabbitMQ injector updated and restarted !",
                                            "RabbitMQ directory period : " +  directoryQueryPeriod + " seconds ; RabbitMQ entities sniff period : " + rvComponentSniffPeriod + " seconds");
        FacesContext.getCurrentInstance().addMessage(null, msg);
    }
}