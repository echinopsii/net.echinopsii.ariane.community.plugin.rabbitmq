/**
 * RabbitMQ plugin directory bundle
 * RabbitMQ cluster to JSON
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
import net.echinopsii.ariane.community.plugin.rabbitmq.directory.model.RabbitmqCluster;
import net.echinopsii.ariane.community.plugin.rabbitmq.directory.model.RabbitmqNode;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashSet;

/**
 * Our own object to JSON tools as :
 *  - we can have cycle in object graphs
 *  - we still want to have references to linked objects through IDs (and so we don't want @XmlTransient or @JsonIgnore)
 */
public class RabbitmqClusterJSON {
    public final static String RMQC_ID          = "rbmqClusterID";
    public final static String RMQC_VERSION     = "rbmqClusterVersion";
    public final static String RMQC_NAME        = "rbmqClusterName";
    public final static String RMQC_DESCRIPTION = "rbmqClusterDescription";
    public final static String RMQC_NODES_ID    = "rbmqClusterNodesID";

    public final static void rabbitmqCluster2JSON(RabbitmqCluster cluster, JsonGenerator jgenerator) throws IOException {
        jgenerator.writeStartObject();
        jgenerator.writeNumberField(RMQC_ID, cluster.getId());
        jgenerator.writeNumberField(RMQC_VERSION, cluster.getVersion());
        jgenerator.writeStringField(RMQC_NAME, cluster.getName());
        jgenerator.writeStringField(RMQC_DESCRIPTION, cluster.getDescription());
        jgenerator.writeArrayFieldStart(RMQC_NODES_ID);
        for (RabbitmqNode node : cluster.getNodes())
            jgenerator.writeNumber(node.getId());
        jgenerator.writeEndArray();
        jgenerator.writeEndObject();
    }

    public final static void oneRabbitmqCluster2JSON(RabbitmqCluster cluster, ByteArrayOutputStream outStream) throws IOException {
        JsonGenerator jgenerator = RabbitmqDirectoryBootstrap.getjFactory().createGenerator(outStream, JsonEncoding.UTF8);
        rabbitmqCluster2JSON(cluster, jgenerator);
        jgenerator.close();
    }

    public final static void manyRabbitmqClusters2JSON(HashSet<RabbitmqCluster> clusters, ByteArrayOutputStream outStream) throws IOException {
        JsonGenerator jgenerator = RabbitmqDirectoryBootstrap.getjFactory().createGenerator(outStream, JsonEncoding.UTF8);
        jgenerator.writeStartObject();
        jgenerator.writeArrayFieldStart("rabbitmqclusters");
        for (RabbitmqCluster cluster : clusters)
            rabbitmqCluster2JSON(cluster, jgenerator);
        jgenerator.writeEndArray();
        jgenerator.writeEndObject();
        jgenerator.close();
    }
}