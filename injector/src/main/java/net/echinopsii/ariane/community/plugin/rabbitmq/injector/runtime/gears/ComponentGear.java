/**
 * RabbitMQ plugin injector bundle
 * RabbitMQ plugin injector RabbitMQ component gear
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

package net.echinopsii.ariane.community.plugin.rabbitmq.injector.runtime.gears;

import net.echinopsii.ariane.community.core.injector.base.model.AbstractAkkaGear;
import net.echinopsii.ariane.community.plugin.rabbitmq.directory.model.RabbitmqCluster;
import net.echinopsii.ariane.community.plugin.rabbitmq.directory.model.RabbitmqNode;
import net.echinopsii.ariane.community.plugin.rabbitmq.injector.RabbitmqInjectorBootstrap;
import net.echinopsii.ariane.community.plugin.rabbitmq.injector.cache.RabbitmqCachedComponent;
import net.echinopsii.ariane.community.plugin.rabbitmq.injector.runtime.actors.ComponentActor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.text.SimpleDateFormat;

public class ComponentGear extends AbstractAkkaGear implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final Logger log = LoggerFactory.getLogger(ComponentGear.class);

    private transient RabbitmqCachedComponent cacheEntity ;

    private int componentSniffInterval ;

    public ComponentGear(RabbitmqCluster rabbitmqComponent, int sniffInterval) {
        super();
        this.componentSniffInterval = sniffInterval;
        this.cacheEntity = (RabbitmqCachedComponent) RabbitmqInjectorBootstrap.getComponentsRegistry().getEntityFromCache(
                                                             RabbitmqInjectorBootstrap.INJ_TREE_ROOT_PATH + "_" + rabbitmqComponent.getName() + "_");
        if (this.cacheEntity == null)
            this.cacheEntity = new RabbitmqCachedComponent().setRabbitmqComponentFields(rabbitmqComponent);
        else {
            SimpleDateFormat ft = new SimpleDateFormat ("yyyy.MM.dd '-' hh:mm:ss a zzz");
            log.debug("{} get from persisted cache. Last sniff : {}.", rabbitmqComponent.getName(), ft.format(cacheEntity.getLastRefresh()));
        }
        super.setGearId(this.cacheEntity.getComponentId());
        super.setGearName("Ariane RabbitMQ Plugin Injector Sniffing Gear (" + this.cacheEntity.getComponentName() + ")");
        super.setGearDescription("Schedule sniff of a " + this.cacheEntity.getComponentType());
        this.cacheEntity.setAttachedGearId(super.getGearId());
    }

    public ComponentGear(RabbitmqNode rabbitmqComponent, int sniffInterval) {
        super();
        this.componentSniffInterval = sniffInterval;
        this.cacheEntity = (RabbitmqCachedComponent) RabbitmqInjectorBootstrap.getComponentsRegistry().getEntityFromCache(
                                                     RabbitmqInjectorBootstrap.INJ_TREE_ROOT_PATH + "_" + rabbitmqComponent.getName() + "_standalone_");

        if (this.cacheEntity == null)
            this.cacheEntity = new RabbitmqCachedComponent().setRabbitmqComponentFields(rabbitmqComponent);
        else {
            SimpleDateFormat ft = new SimpleDateFormat ("yyyy.MM.dd '-' hh:mm:ss a zzz");
            log.debug("{} get from persisted cache. Last sniff : {}.", rabbitmqComponent.getName(), ft.format(cacheEntity.getLastRefresh()));
        }
        super.setGearId(this.cacheEntity.getComponentId());
        super.setGearName("Ariane RabbitMQ Plugin Injector Sniffing Gear (" + this.cacheEntity.getComponentName() + ")");
        super.setGearDescription("Schedule sniff of a " + this.cacheEntity.getComponentType());
        this.cacheEntity.setAttachedGearId(super.getGearId());
    }

    public RabbitmqCachedComponent getCacheEntity() {
        return cacheEntity;
    }

    public void refresh() {
        super.tell(ComponentActor.MSG_REFRESH);
    }

    @Override
    public void start() {
        super.setGearActor(super.getGearActorRefFactory().actorOf(ComponentActor.props(this), super.getGearId()));
        super.scheduleMessage(ComponentActor.MSG_REFRESH, componentSniffInterval*1000);
        super.setRunning(true);
        log.info("{} is started", super.getGearName());
    }

    @Override
    public void stop() {
        super.cancelMessagesScheduling();
        super.getGearActorRefFactory().stop(super.getGearActor());
        while(!super.getGearActor().isTerminated())
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        super.setRunning(false);
        log.info("{} is stopped", super.getGearName());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ComponentGear componentGear = (ComponentGear) o;
        return super.getGearId().equals(componentGear.getGearId());
    }

    @Override
    public int hashCode() {
        return super.getGearId().hashCode();
    }
}