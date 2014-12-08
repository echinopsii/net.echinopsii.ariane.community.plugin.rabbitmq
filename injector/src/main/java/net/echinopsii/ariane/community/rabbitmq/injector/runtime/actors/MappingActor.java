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

import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.japi.Creator;
import net.echinopsii.ariane.community.rabbitmq.injector.runtime.gears.MappingGear;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MappingActor extends UntypedActor {

    private static final Logger log = LoggerFactory.getLogger(MappingActor.class);

    private MappingGear gear;

    public static Props props(final MappingGear gear) {
        return Props.create(new Creator<MappingActor>() {
            private static final long serialVersionUID = 1L;

            @Override
            public MappingActor create() throws Exception {
                return new MappingActor(gear);
            }
        });
    }

    public MappingActor(MappingGear gear) {
        this.gear = gear;
    }

    @Override
    public void onReceive(Object message) throws Exception {
        //TODO
    }
}