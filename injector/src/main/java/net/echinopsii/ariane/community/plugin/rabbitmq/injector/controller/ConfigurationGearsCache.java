/**
 * RabbitMQ plugin addon injector bundle
 * Cache Gears Configuration controller
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

import javax.annotation.PostConstruct;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.TreeSet;

public class ConfigurationGearsCache implements Serializable {

    private Properties gearsCacheConf;
    private List<String> keys ;

    @PostConstruct
    public void init() {
        gearsCacheConf = RabbitmqInjectorBootstrap.getGearsRegisry().getConfiguration();

        keys = new ArrayList<String>();
        TreeSet<Object> sortedKeys = new TreeSet<Object>();
        sortedKeys.addAll(gearsCacheConf.keySet());
        for (Object key: sortedKeys) {
            if (key instanceof String)
                keys.add((String)key);
        }
    }

    public List<String> getKeys() {
        return keys;
    }

    public String getGearsCacheConf(String key) {
        return gearsCacheConf.get(key).toString();
    }
}