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

package net.echinopsii.ariane.community.rabbitmq.injector.runtime.actors;

import akka.actor.ActorSelection;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.japi.Creator;
import net.echinopsii.ariane.community.rabbitmq.injector.RabbitmqInjectorBootstrap;
import net.echinopsii.ariane.community.rabbitmq.injector.cache.RabbitmqCachedComponent;
import net.echinopsii.ariane.community.rabbitmq.injector.runtime.gears.ComponentGear;
import net.echinopsii.ariane.community.rabbitmq.injector.runtime.gears.MappingGear;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ComponentActor extends UntypedActor {

    private static final Logger log = LoggerFactory.getLogger(DirectoryActor.class);

    public final static String            MSG_REFRESH = "REFRESH";
    private ComponentGear gear;

    public static Props props(final ComponentGear gear) {
        return Props.create(new Creator<ComponentActor>() {
            private static final long serialVersionUID = 1L;

            @Override
            public ComponentActor create() throws Exception {
                return new ComponentActor(gear);
            }
        });
    }

    public ComponentActor(ComponentGear gear) {
        this.gear = gear;
    }

    @Override
    public void onReceive(Object message) throws Exception {
        log.debug("Actor {} receive message {}", new Object[]{getSelf().path().toStringWithoutAddress(), message.toString()});
        if (message instanceof String) {
            if (message.equals(MSG_REFRESH)) {
                this.gear.getCacheEntity().refresh();
                log.debug("send msg to /user/" + RabbitmqInjectorBootstrap.INJ_TREE_ROOT_PATH + MappingGear.SUBNAME);
                ActorSelection selection = this.gear.getGearActorRefFactory().actorSelection(
                                                     "/user/" + RabbitmqInjectorBootstrap.INJ_TREE_ROOT_PATH+ MappingGear.SUBNAME);
                selection.tell(this.gear.getCacheEntity(), this.gear.getGearActor());
                if (this.gear.getCacheEntity().getNextAction() == RabbitmqCachedComponent.ACTION_DELETE) {
                    RabbitmqInjectorBootstrap.getGearsRegisry().removeEntityFromCache(this.gear);
                    this.gear.stop();
                }
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