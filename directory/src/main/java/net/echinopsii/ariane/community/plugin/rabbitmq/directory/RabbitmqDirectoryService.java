/**
 * RabbitMQ plugin directory bundle
 * RabbitMQ plugin directory service
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

package net.echinopsii.ariane.community.plugin.rabbitmq.directory;

import net.echinopsii.ariane.community.plugin.rabbitmq.directory.model.RabbitmqCluster;
import net.echinopsii.ariane.community.plugin.rabbitmq.directory.model.RabbitmqNode;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public interface RabbitmqDirectoryService {

    String ARIANE_OTM_NOT_DEFINED = "OTM_NOT_DEFINED";

    /**
     * add provided node to rabbitmq node directory
     *
     * @param node RabbitMQ node to add
     */
    void addNode(RabbitmqNode node);

    /**
     * add provided cluster to rabbitmq cluster directory
     *
     * @param cluster RabbitMQ cluster to add
     */
    void addCluster(RabbitmqCluster cluster);

    /**
     * get all rabbitmq node from rabbitmq node directory
     *
     * @return nodes list
     */
    HashSet<RabbitmqNode> getNodesList();

    /**
     * get all rabbitmq cluster from rabbitmq cluster directory
     *
     * @return clusters list
     */
    HashSet<RabbitmqCluster> getClustersList();

    /**
     * delete provided rabbitmq node from rabbitmq node directory
     *
     * @param node delete RabbitMQ node to delete
     */
    void delNode(RabbitmqNode node);

    /**
     * delete provided rabbitmq cluster from rabbitmq cluster directory
     *
     * @param cluster RabbitMQ cluster to delete
     */
    void delCluster(RabbitmqCluster cluster);

    /**
     * update provided rabbitmq node in rabbitmq node directory
     *
     * @param node RabbitMQ node to update
     */
    void updateNode(RabbitmqNode node);

    /**
     * update provided rabbitmq node in rabbitmq cluster directory
     *
     * @param cluster RabbitMQ cluster to update
     */
    void updateCluster(RabbitmqCluster cluster);

    int FAKE_CLUSTER_ID = -1;

    /**
     * Get cluster from node. If no cluster is defined for this node return
     * a fake cluster with the node in the cluster node list.
     *
     * @param node RabbitMQ node in the cluster to return
     * @return the cluster from node
     */
    RabbitmqCluster getClusterFromNode(RabbitmqNode node);

    /**
     * Refresh a cluster according to the cluster ID
     *
     * @param clusterID RabbitMQ cluster ID to refresh
     * @return refreshed cluster associated to cluster ID
     */
    RabbitmqCluster refreshRabbitmqCluster(Long clusterID);

    /**
     * Get fresh nodes list from a cluster according cluster ID
     *
     * @param clusterID RabbitMQ cluster ID containing nodes to return
     * @return nodes list from a cluster according cluster ID
     */
    Set<RabbitmqNode> getNodesFromCluster(Long clusterID);

    /**
     * Refresh a node according to the node ID
     *
     * @param nodeID RabbitMQ node ID to refresh
     * @return refreshed cluster associated to node ID
     */
    RabbitmqNode refreshRabbitmqNode(Long nodeID);

    /**
     * Get remote client container properties from its OS Instance Name and team name
     *
     * @param osiName the RabbitMQ client OS instance provided from RabbitMQ Connection
     * @param teamName the RabbitMQ client team name provided from RabbitMQ Connection
     *
     * @return properties
     */
    HashMap<String, Object> getRemoteClientContainerProperties(String osiName, String teamName);
}