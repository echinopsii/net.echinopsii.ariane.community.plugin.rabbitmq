/**
 * RabbitMQ plugin directory bundle
 * RabbitMQ component to JSON
 *
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

package net.echinopsii.ariane.community.plugin.rabbitmq.directory.json;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonGenerator;
import net.echinopsii.ariane.community.plugin.rabbitmq.directory.RabbitmqDirectoryBootstrap;
import net.echinopsii.ariane.community.plugin.rabbitmq.directory.model.RabbitmqComponent;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashSet;

/**
 * Our own object to JSON tools as :
 *  - we can have cycle in object graphs
 *  - we still want to have references to linked objects through IDs (and so we don't want @XmlTransient or @JsonIgnore)
 */
public class RabbitmqComponentJSON {
    public final static String RMQC_ID          = "rbmqComponentID";
    public final static String RMQC_VERSION     = "rbmqComponentVersion";
    public final static String RMQC_NAME        = "rbmqComponentName";
    public final static String RMQC_URL         = "rbmqComponentURL";
    public final static String RMQC_USER        = "rbmqComponentUser";
    public final static String RMQC_PASSWD      = "rbmqComponentIsPasswordDefined";
    public final static String RMQC_DESCRIPTION = "rbmqComponentDescription";
    public final static String RMQC_OSINSTANCE  = "rbmqComponentOSinstanceID";
    public final static String RMQC_SPT_TEAM    = "rbmqComponentSupportTeamID";

    public final static void rabbitmqComponent2JSON(RabbitmqComponent component, JsonGenerator jgenerator) throws IOException {
        jgenerator.writeStartObject();
        jgenerator.writeNumberField(RMQC_ID, component.getId());
        jgenerator.writeNumberField(RMQC_VERSION, component.getVersion());
        jgenerator.writeStringField(RMQC_NAME, component.getName());
        jgenerator.writeStringField(RMQC_URL, component.getUrl());
        jgenerator.writeStringField(RMQC_USER, component.getUser());
        jgenerator.writeBooleanField(RMQC_PASSWD, component.getPasswd() != null);
        jgenerator.writeStringField(RMQC_DESCRIPTION, component.getDescription());
        jgenerator.writeNumberField(RMQC_OSINSTANCE, component.getOsInstance().getId());
        jgenerator.writeNumberField(RMQC_SPT_TEAM, component.getSupportTeam().getId());
        jgenerator.writeEndObject();
    }

    public final static void oneRabbitmqComponent2JSON(RabbitmqComponent component, ByteArrayOutputStream outStream) throws IOException {
        JsonGenerator jgenerator = RabbitmqDirectoryBootstrap.getjFactory().createGenerator(outStream, JsonEncoding.UTF8);
        rabbitmqComponent2JSON(component, jgenerator);
        jgenerator.close();
    }

    public final static void manyRabbitmqComponents2JSON(HashSet<RabbitmqComponent> components, ByteArrayOutputStream outStream) throws IOException {
        JsonGenerator jgenerator = RabbitmqDirectoryBootstrap.getjFactory().createGenerator(outStream, JsonEncoding.UTF8);
        jgenerator.writeStartObject();
        jgenerator.writeArrayFieldStart("rabbitmqcomponents");
        for (RabbitmqComponent component : components)
            rabbitmqComponent2JSON(component, jgenerator);
        jgenerator.writeEndArray();
        jgenerator.writeEndObject();
        jgenerator.close();
    }
}