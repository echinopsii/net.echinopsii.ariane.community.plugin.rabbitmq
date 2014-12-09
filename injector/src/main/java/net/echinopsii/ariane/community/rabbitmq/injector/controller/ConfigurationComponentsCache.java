/**
 * RabbitMQ plugin injector bundle
 * Components Cache Configuration controller
 * Copyright (C) 2014 Mathilde Ffrench
 *
 * Copyright (C) 2014  Mathilde Ffrench
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

package net.echinopsii.ariane.community.rabbitmq.injector.controller;

import net.echinopsii.ariane.community.rabbitmq.injector.RabbitmqInjectorBootstrap;

import javax.annotation.PostConstruct;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.TreeSet;

public class ConfigurationComponentsCache implements Serializable {

    private Properties componentsCacheConf;
    private List<String> keys ;

    @PostConstruct
    public void init() {
        componentsCacheConf = RabbitmqInjectorBootstrap.getComponentsRegistry().getConfiguration();

        keys = new ArrayList<String>();
        TreeSet<Object> sortedKeys = new TreeSet<Object>();
        sortedKeys.addAll(componentsCacheConf.keySet());
        for (Object key: sortedKeys) {
            if (key instanceof String)
                keys.add((String)key);
        }
    }

    public List<String> getKeys() {
        return keys;
    }

    public String getComponentsCacheConf(String key) {
        return componentsCacheConf.get(key).toString();
    }
}