/**
 * RabbitMQ plugin injector bundle
 * RabbitMQ plugin injector RabbitMQ directory component
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

package net.echinopsii.ariane.community.plugin.rabbitmq.injector.runtime.actors;

import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.japi.Creator;
import net.echinopsii.ariane.community.plugin.rabbitmq.directory.model.RabbitmqCluster;
import net.echinopsii.ariane.community.plugin.rabbitmq.directory.model.RabbitmqNode;
import net.echinopsii.ariane.community.plugin.rabbitmq.injector.RabbitmqInjectorBootstrap;
import net.echinopsii.ariane.community.plugin.rabbitmq.injector.runtime.gears.ComponentGear;
import net.echinopsii.ariane.community.plugin.rabbitmq.injector.runtime.gears.DirectoryGear;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;

public class DirectoryActor extends UntypedActor {

    private static final Logger log = LoggerFactory.getLogger(DirectoryActor.class);

    public final static String            MSG_REFRESH = "REFRESH";
    private DirectoryGear gear;
    private boolean firstRefresh = true;

    public static Props props(final DirectoryGear gear) {
        return Props.create(new Creator<DirectoryActor>() {
            private static final long serialVersionUID = 1L;

            @Override
            public DirectoryActor create() throws Exception {
                return new DirectoryActor(gear);
            }
        });
    }

    public DirectoryActor(DirectoryGear gear) {
        this.gear = gear;
    }

    private static HashSet<String> rbqClusterStarted = new HashSet<>();
    private static HashSet<String> rbqStandaloneStarted = new HashSet<>();
    private void refresh() throws InterruptedException {
        //at first Ariane startup Directory could be updated by others plugins.
        //so it could result to getting exception while requesting the directory entity manager.
        //sleep 30 sec before requesting the Directory entity manager...
        if (firstRefresh) {
            Thread.sleep(20 * 1000);
            firstRefresh = false;
        }

        HashSet<RabbitmqNode> nodesList = RabbitmqInjectorBootstrap.getRabbitmqDirectorySce().getNodesList();
        for (RabbitmqNode rabbitmqNode : nodesList) {
            RabbitmqCluster nodeCluster = RabbitmqInjectorBootstrap.getRabbitmqDirectorySce().getClusterFromNode(rabbitmqNode);
            if (nodeCluster != null) {
                if (!rbqClusterStarted.contains(nodeCluster.getName())) {
                    ComponentGear componentGear = (ComponentGear) RabbitmqInjectorBootstrap.getGearsRegisry().getEntityFromCache(RabbitmqInjectorBootstrap.INJ_TREE_ROOT_PATH + "_" + nodeCluster.getName() + "_");
                    if (componentGear == null) {
                        componentGear = new ComponentGear(nodeCluster, this.gear.getDefaultComponentSniffInterval());
                        RabbitmqInjectorBootstrap.getGearsRegisry().putEntityToCache(componentGear);
                    }
                    componentGear.start();
                    rbqClusterStarted.add(nodeCluster.getName());
                }
            } else {
                if (!rbqStandaloneStarted.contains(rabbitmqNode.getName())) {
                    ComponentGear componentGear = (ComponentGear) RabbitmqInjectorBootstrap.getGearsRegisry().getEntityFromCache(RabbitmqInjectorBootstrap.INJ_TREE_ROOT_PATH + "_" + rabbitmqNode.getName() + "_standalone_");
                    if (componentGear == null) {
                        componentGear = new ComponentGear(rabbitmqNode, this.gear.getDefaultComponentSniffInterval());
                        RabbitmqInjectorBootstrap.getGearsRegisry().putEntityToCache(componentGear);
                    }
                    componentGear.start();
                    rbqStandaloneStarted.add(rabbitmqNode.getName());
                }
            }
        }
    }

    @Override
    public void onReceive(Object message) throws Exception {
        log.debug("Actor {} receive message {}", new Object[]{getSelf().path().toStringWithoutAddress(), message.toString()});
        if (message instanceof String) {
            if (message.equals(MSG_REFRESH)) {
                refresh();
            } else {
                log.error("Unhandled message {} !", message);
                unhandled(message);
            }
        } else {
            log.error("Unhandled message type {} !", message.getClass().toString());
            unhandled(message);
        }
    }
}