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

package net.echinopsii.ariane.community.plugin.rabbitmq.injector.runtime.gears;

import net.echinopsii.ariane.community.core.injector.base.model.AbstractAkkaGear;
import net.echinopsii.ariane.community.plugin.rabbitmq.injector.RabbitmqInjectorBootstrap;
import net.echinopsii.ariane.community.plugin.rabbitmq.injector.runtime.actors.DirectoryActor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;

public class DirectoryGear extends AbstractAkkaGear implements Serializable {

    private static final Logger log = LoggerFactory.getLogger(DirectoryGear.class);

    public static final String SUBNAME = "_DirectoryGear_";

    private int directorySniffInterval ;

    private int defaultComponentSniffInterval ;

    public DirectoryGear(int dirSniffInterval, int componentSniffInterval) {
        super();
        this.directorySniffInterval = dirSniffInterval;
        this.defaultComponentSniffInterval = componentSniffInterval;
        super.setGearId(RabbitmqInjectorBootstrap.INJ_TREE_ROOT_PATH+SUBNAME);
        super.setGearName("Ariane RabbitMQ Plugin Injector Directory Gear");
        super.setGearDescription("Init RabbitMQ gears from Tibco RV directory. Start and stop mapping and RabbitMQ gears.");
    }

    public int getDefaultComponentSniffInterval() {
        return defaultComponentSniffInterval;
    }

    public void refresh() {
        super.tell(DirectoryActor.MSG_REFRESH);
    }


    @Override
    public void start() {
        super.setGearActor(super.getGearActorRefFactory().actorOf(DirectoryActor.props(this), super.getGearId()));
        super.scheduleMessage(DirectoryActor.MSG_REFRESH, directorySniffInterval*1000);
        super.setRunning(true);
        refresh();
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

        DirectoryGear that = (DirectoryGear) o;
        return super.getGearId().equals(that.getGearId());
    }

    @Override
    public int hashCode() {
        return super.getGearId().hashCode();
    }
}