/**
 * RabbitMQ plugin directory bundle
 * RabbitMQ node to JSON
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
import net.echinopsii.ariane.community.plugin.rabbitmq.directory.model.RabbitmqNode;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashSet;

/**
 * Our own object to JSON tools as :
 *  - we can have cycle in object graphs
 *  - we still want to have references to linked objects through IDs (and so we don't want @XmlTransient or @JsonIgnore)
 */
public class RabbitmqNodeJSON {
    public final static String RMQC_ID          = "rbmqNodeID";
    public final static String RMQC_VERSION     = "rbmqNodeVersion";
    public final static String RMQC_NAME        = "rbmqNodeName";
    public final static String RMQC_URL         = "rbmqNodeURL";
    public final static String RMQC_USER        = "rbmqNodeUser";
    public final static String RMQC_PASSWD      = "rbmqNodeIsPasswordDefined";
    public final static String RMQC_DESCRIPTION = "rbmqNodeDescription";
    public final static String RMQC_OSINSTANCE  = "rbmqNodeOSinstanceID";
    public final static String RMQC_SPT_TEAM    = "rbmqNodeSupportTeamID";

    public final static void rabbitmqNode2JSON(RabbitmqNode node, JsonGenerator jgenerator) throws IOException {
        jgenerator.writeStartObject();
        jgenerator.writeNumberField(RMQC_ID, node.getId());
        jgenerator.writeNumberField(RMQC_VERSION, node.getVersion());
        jgenerator.writeStringField(RMQC_NAME, node.getName());
        jgenerator.writeStringField(RMQC_URL, node.getUrl());
        jgenerator.writeStringField(RMQC_USER, node.getUser());
        jgenerator.writeBooleanField(RMQC_PASSWD, node.getPasswd() != null);
        jgenerator.writeStringField(RMQC_DESCRIPTION, node.getDescription());
        jgenerator.writeNumberField(RMQC_OSINSTANCE, node.getOsInstance().getId());
        jgenerator.writeNumberField(RMQC_SPT_TEAM, node.getSupportTeam().getId());
        jgenerator.writeEndObject();
    }

    public final static void oneRabbitmqNode2JSON(RabbitmqNode node, ByteArrayOutputStream outStream) throws IOException {
        JsonGenerator jgenerator = RabbitmqDirectoryBootstrap.getjFactory().createGenerator(outStream, JsonEncoding.UTF8);
        rabbitmqNode2JSON(node, jgenerator);
        jgenerator.close();
    }

    public final static void manyRabbitmqNodes2JSON(HashSet<RabbitmqNode> nodes, ByteArrayOutputStream outStream) throws IOException {
        JsonGenerator jgenerator = RabbitmqDirectoryBootstrap.getjFactory().createGenerator(outStream, JsonEncoding.UTF8);
        jgenerator.writeStartObject();
        jgenerator.writeArrayFieldStart("rabbitmqnodes");
        for (RabbitmqNode node : nodes)
            rabbitmqNode2JSON(node, jgenerator);
        jgenerator.writeEndArray();
        jgenerator.writeEndObject();
        jgenerator.close();
    }
}