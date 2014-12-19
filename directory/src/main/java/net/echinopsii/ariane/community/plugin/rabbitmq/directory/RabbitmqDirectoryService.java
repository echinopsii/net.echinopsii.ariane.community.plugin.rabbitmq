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

package net.echinopsii.ariane.community.plugin.rabbitmq.directory;

import net.echinopsii.ariane.community.plugin.rabbitmq.directory.model.RabbitmqCluster;
import net.echinopsii.ariane.community.plugin.rabbitmq.directory.model.RabbitmqNode;

import java.util.HashSet;
import java.util.Set;

public interface RabbitmqDirectoryService {

    /**
     * add provided node to rabbitmq node directory
     *
     * @param node
     */
    public void addNode(RabbitmqNode node);

    /**
     * add provided cluster to rabbitmq cluster directory
     *
     * @param cluster
     */
    public void addCluster(RabbitmqCluster cluster);

    /**
     * get all rabbitmq node from rabbitmq node directory
     *
     * @return nodes list
     */
    public HashSet<RabbitmqNode> getNodesList();

    /**
     * get all rabbitmq cluster from rabbitmq cluster directory
     *
     * @return clusters list
     */
    public HashSet<RabbitmqCluster> getClustersList();

    /**
     * delete provided rabbitmq node from rabbitmq node directory
     *
     * @param node
     */
    public void delNode(RabbitmqNode node);

    /**
     * delete provided rabbitmq cluster from rabbitmq cluster directory
     *
     * @param cluster
     */
    public void delCluster(RabbitmqCluster cluster);

    /**
     * update provided rabbitmq node in rabbitmq node directory
     *
     * @param node
     */
    public void updateNode(RabbitmqNode node);

    /**
     * update provided rabbitmq node in rabbitmq cluster directory
     *
     * @param cluster
     */
    public void updateCluster(RabbitmqCluster cluster);

    public static int FAKE_CLUSTER_ID = -1;

    /**
     * Get cluster from node. If no cluster is defined for this node return
     * a fake cluster with the node in the cluster node list.
     *
     * @param node
     * @return
     */
    public RabbitmqCluster getClusterFromNode(RabbitmqNode node);

    /**
     * Refresh a cluster according to the cluster ID
     *
     * @param clusterID
     * @return refreshed cluster associated to cluster ID
     */
    public RabbitmqCluster refreshRabbitmqCluster(Long clusterID);

    /**
     * Get fresh nodes list from a cluster according cluster ID
     *
     * @param clusterID
     * @return nodes list from a cluster according cluster ID
     */
    public Set<RabbitmqNode> getNodesFromCluster(Long clusterID);
}