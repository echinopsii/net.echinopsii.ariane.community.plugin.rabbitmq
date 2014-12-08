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

package net.echinopsii.ariane.community.rabbitmq.injector.runtime.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Dictionary;

public class RabbitmqInjectorMainCfgLoader {
    private static final Logger log = LoggerFactory.getLogger(RabbitmqInjectorMainCfgLoader.class);

    public static final String  RABBITMQ_INJECTOR_CFG_DIRECTORY_QUERYINTERVAL_KEY = "rabbitmq.injector.scheduler.directory.query.interval";
    public static final String  RABBITMQ_INJECTOR_CFG_COMPONENT_SNIFFINTERVAL_KEY = "rabbitmq.injector.scheduler.component.sniff.interval";

    private static int    dirQI       = -1;
    private static int    rbCompQI    = -1;

    public static boolean isValid(Dictionary properties) {
        boolean ret = false;
        if (properties!=null) {
            if (properties.get(RABBITMQ_INJECTOR_CFG_DIRECTORY_QUERYINTERVAL_KEY)!=null && properties.get(RABBITMQ_INJECTOR_CFG_COMPONENT_SNIFFINTERVAL_KEY)!=null) {
                Object idirQI = properties.get(RABBITMQ_INJECTOR_CFG_DIRECTORY_QUERYINTERVAL_KEY);
                Object icmpSI = properties.get(RABBITMQ_INJECTOR_CFG_COMPONENT_SNIFFINTERVAL_KEY);

                if (idirQI instanceof String) {
                    dirQI = new Integer((String) idirQI);
                    log.debug("[String to integer] Directory Query Interval: {}", new Object[]{dirQI});
                } else if (idirQI instanceof Integer) {
                    dirQI = (Integer) idirQI;
                    log.debug("[Integer] Directory Query Interval : {}", new Object[]{dirQI});
                } else {
                    log.error("Error with for configuration property type {} : {}...", new Object[]{RABBITMQ_INJECTOR_CFG_DIRECTORY_QUERYINTERVAL_KEY,idirQI.getClass().getName()});
                }

                if (icmpSI instanceof String) {
                    rbCompQI = new Integer((String) icmpSI);
                    log.debug("[String to integer] RabbitMQ component sniff interval: {}", new Object[]{rbCompQI});
                } else if (idirQI instanceof Integer) {
                    rbCompQI = (Integer) icmpSI;
                    log.debug("[Integer] RabbitMQ component sniff interval : {}", new Object[]{rbCompQI});
                } else {
                    log.error("Error with for configuration property type {} : {}...", new Object[]{RABBITMQ_INJECTOR_CFG_COMPONENT_SNIFFINTERVAL_KEY,icmpSI.getClass().getName()});
                }


                if (dirQI > 0 && rbCompQI > 0) ret = true;
                else {
                    if (dirQI<=0) log.error("RabbitMQ directory query interval is not strictly positive !");
                    if (rbCompQI<=0) log.error("RabbitMQ component sniff interval is not strictly positive !");
                }
            } else {
                log.error("RabbitMQ injector scheduler configuration fields are missing !");
            }
        } else {
            log.error("RabbitMQ scheduler configuration is null !");
        }

        return ret;
    }

    public static int getDirQI() {
        return dirQI;
    }

    public static int getRbCompQI() {
        return rbCompQI;
    }
}