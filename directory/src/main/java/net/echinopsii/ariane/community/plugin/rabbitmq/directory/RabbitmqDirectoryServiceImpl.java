/**
 * RabbitMQ plugin directory bundle
 * RabbitMQ plugin directory service mem impl
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

import net.echinopsii.ariane.community.core.directory.base.model.organisational.Team;
import net.echinopsii.ariane.community.core.directory.base.model.technical.network.Datacenter;
import net.echinopsii.ariane.community.core.directory.base.model.technical.network.Subnet;
import net.echinopsii.ariane.community.core.directory.base.model.technical.system.OSInstance;
import net.echinopsii.ariane.community.plugin.rabbitmq.directory.controller.rabbitmqcluster.RabbitmqClustersListController;
import net.echinopsii.ariane.community.plugin.rabbitmq.directory.controller.rabbitmqnode.RabbitmqNodesListController;
import net.echinopsii.ariane.community.plugin.rabbitmq.directory.model.RabbitmqCluster;
import net.echinopsii.ariane.community.plugin.rabbitmq.directory.model.RabbitmqNode;
import org.apache.felix.ipojo.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import javax.transaction.NotSupportedException;
import javax.transaction.SystemException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

@Component
@Provides
@Instantiate
public class RabbitmqDirectoryServiceImpl implements RabbitmqDirectoryService {

    private static final Logger log = LoggerFactory.getLogger(RabbitmqDirectoryServiceImpl.class);

    private static final String RABBITMQ_DIRECTORY_SERVICE = "Ariane RabbitMQ Plugin Directory Service";

    @Validate
    public void validate() throws Exception {
        log.info("{} is started", new Object[]{RABBITMQ_DIRECTORY_SERVICE});
    }

    @Invalidate
    public void invalidate() {
        log.info("{} is stopped", new Object[]{RABBITMQ_DIRECTORY_SERVICE});
    }

    @Override
    public void addNode(RabbitmqNode node) {
        if (RabbitmqDirectoryBootstrap.getDirectoryJPAProvider()==null) {
            log.error("Directory JPA provider has been unbounded !");
            return;
        }
        EntityManager em = RabbitmqDirectoryBootstrap.getDirectoryJPAProvider().createEM();
        em.getTransaction().begin();
        em.persist(node);
        em.flush();
        em.getTransaction().commit();
        log.debug("Close entity manager ...");
        em.close();
    }

    @Override
    public void addCluster(RabbitmqCluster cluster) {
        if (RabbitmqDirectoryBootstrap.getDirectoryJPAProvider()==null) {
            log.error("Directory JPA provider has been unbounded !");
            return;
        }
        EntityManager em = RabbitmqDirectoryBootstrap.getDirectoryJPAProvider().createEM();
        em.getTransaction().begin();
        em.persist(cluster);
        em.flush();
        em.getTransaction().commit();
        log.debug("Close entity manager ...");
        em.close();
    }

    @Override
    public HashSet<RabbitmqNode> getNodesList() {
        if (RabbitmqDirectoryBootstrap.getDirectoryJPAProvider()==null) {
            log.error("Directory JPA provider has been unbounded !");
            return null;
        }
        HashSet<RabbitmqNode> ret = null;
        try {
            ret = new HashSet<>(RabbitmqNodesListController.getAll());
        } catch (SystemException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (NotSupportedException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        log.debug("Close entity manager...");
        return ret;
    }

    @Override
    public HashSet<RabbitmqCluster> getClustersList() {
        if (RabbitmqDirectoryBootstrap.getDirectoryJPAProvider()==null) {
            log.error("Directory JPA provider has been unbounded !");
            return null;
        }
        HashSet<RabbitmqCluster> ret = null;
        try {
            ret = new HashSet<>(RabbitmqClustersListController.getAll());
        } catch (SystemException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (NotSupportedException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        log.debug("Close entity manager...");
        return ret;
    }

    @Override
    public void delNode(RabbitmqNode node) {
        if (RabbitmqDirectoryBootstrap.getDirectoryJPAProvider()==null) {
            log.error("Directory JPA provider has been unbounded !");
            return ;
        }
        EntityManager em = RabbitmqDirectoryBootstrap.getDirectoryJPAProvider().createEM();
        em.getTransaction().begin();
        em.remove(node);
        em.flush();
        em.getTransaction().commit();
        log.debug("Close entity manager...");
        em.close();
    }

    @Override
    public void delCluster(RabbitmqCluster cluster) {
        if (RabbitmqDirectoryBootstrap.getDirectoryJPAProvider()==null) {
            log.error("Directory JPA provider has been unbounded !");
            return ;
        }
        EntityManager em = RabbitmqDirectoryBootstrap.getDirectoryJPAProvider().createEM();
        em.getTransaction().begin();
        em.remove(cluster);
        em.flush();
        em.getTransaction().commit();
        log.debug("Close entity manager...");
        em.close();
    }

    @Override
    public void updateNode(RabbitmqNode node) {
        if (RabbitmqDirectoryBootstrap.getDirectoryJPAProvider()==null) {
            log.error("Directory JPA provider has been unbounded !");
            return;
        }
        EntityManager em = RabbitmqDirectoryBootstrap.getDirectoryJPAProvider().createEM();
        em.getTransaction().begin();
        em.merge(node);
        em.flush();
        em.getTransaction().commit();
        log.debug("Close entity manager...");
        em.close();
    }

    @Override
    public void updateCluster(RabbitmqCluster cluster) {
        if (RabbitmqDirectoryBootstrap.getDirectoryJPAProvider()==null) {
            log.error("Directory JPA provider has been unbounded !");
            return;
        }
        EntityManager em = RabbitmqDirectoryBootstrap.getDirectoryJPAProvider().createEM();
        em.getTransaction().begin();
        em.merge(cluster);
        em.flush();
        em.getTransaction().commit();
        log.debug("Close entity manager...");
        em.close();
    }

    private HashMap<String, Object> getLocationPropertiesFromOSI(OSInstance osInstance) {
        HashMap<String, Object> props = new HashMap<>();
        HashSet<Datacenter> dcs = new HashSet<>();
        HashSet<Subnet> subnets = new HashSet<>();
        for (Subnet subnet : osInstance.getNetworkSubnets()) {
            for(Datacenter datacenter : subnet.getDatacenters()) if (!dcs.contains(datacenter)) dcs.add(datacenter);
            if (!subnets.contains(subnet)) subnets.add(subnet);
        }
        for (Datacenter datacenter : dcs) props.put(Datacenter.DC_MAPPING_PROPERTIES,datacenter.toMappingProperties());
        for (Subnet subnet : subnets) {
            HashMap<String, Object> subnetProps = subnet.toMappingProperties();
            props.put(Subnet.SUBNET_MAPPING_PROPERTIES, subnetProps);
        }
        props.put(OSInstance.OSI_MAPPING_PROPERTIES, osInstance.toMappingProperties());
        return props;
    }

    private HashMap<String, Object> getTeamPropertiesFromTeam(Team team) {
        HashMap<String, Object> props = new HashMap<>();
        props.put(Team.TEAM_SUPPORT_MAPPING_PROPERTIES, team.toMappingProperties());
        return props;
    }

    private void putLocationAndTeamPropertiesToRabbitmqNodes(RabbitmqCluster cluster) {
        HashMap<String, Object> props = new HashMap<>();
        for (RabbitmqNode rabbitmqNode : cluster.getNodes()) {
            props.putAll(getLocationPropertiesFromOSI(rabbitmqNode.getOsInstance()));
            props.putAll(getTeamPropertiesFromTeam(rabbitmqNode.getSupportTeam()));
            rabbitmqNode.setProperties(props);
        }
    }

    private void putLocationAndTeamPropertiesToRabbitmqNode(RabbitmqNode rabbitmqNode) {
        HashMap<String, Object> props = new HashMap<>();
        props.putAll(getLocationPropertiesFromOSI(rabbitmqNode.getOsInstance()));
        props.putAll(getTeamPropertiesFromTeam(rabbitmqNode.getSupportTeam()));
        rabbitmqNode.setProperties(props);
    }

    @Override
    public RabbitmqCluster getClusterFromNode(RabbitmqNode node) {
        RabbitmqCluster ret = null;
        if (RabbitmqDirectoryBootstrap.getDirectoryJPAProvider()==null) {
            log.error("Directory JPA provider has been unbounded !");
            return null ;
        }

        EntityManager em = RabbitmqDirectoryBootstrap.getDirectoryJPAProvider().createEM();
        RabbitmqNode persistedNode = em.find(RabbitmqNode.class, node.getId());
        if (persistedNode!=null) {
            ret = persistedNode.getCluster();
            if (ret!=null) putLocationAndTeamPropertiesToRabbitmqNodes(ret);
            else putLocationAndTeamPropertiesToRabbitmqNode(persistedNode);
        } else {
            Set<RabbitmqNode> vnodes = new HashSet<RabbitmqNode>();
            vnodes.add(node);
            ret = new RabbitmqCluster().setIdR((long) -1).setVersionR(1).setNameR("rabbit@" + node.getName()).setDescriptionR("fake rabbit cluster").setNodesR(vnodes);
        }
        em.close();

        return ret;
    }

    @Override
    public RabbitmqCluster refreshRabbitmqCluster(Long clusterID) {
        if (RabbitmqDirectoryBootstrap.getDirectoryJPAProvider()==null) {
            log.error("Directory JPA provider has been unbounded !");
            return null ;
        }
        EntityManager em = RabbitmqDirectoryBootstrap.getDirectoryJPAProvider().createEM();
        CriteriaBuilder builder = em.getCriteriaBuilder();

        CriteriaQuery<RabbitmqCluster> rbcc = builder.createQuery(RabbitmqCluster.class);
        Root<RabbitmqCluster> rbccRoot = rbcc.from(RabbitmqCluster.class);
        rbcc.select(rbccRoot).where(builder.equal(rbccRoot.<String>get("id"), clusterID));
        TypedQuery<RabbitmqCluster> rbccQuery = em.createQuery(rbcc);

        RabbitmqCluster freshRabbitmqCluster = null;
        try {
            freshRabbitmqCluster = rbccQuery.getSingleResult();
        } catch (NoResultException e) {
            log.error("unable to retrieve RabbitMQ Cluster component {} from Directory DB!", clusterID);
        } catch (Exception e) {
            throw e;
        }

        if (freshRabbitmqCluster!=null)
            putLocationAndTeamPropertiesToRabbitmqNodes(freshRabbitmqCluster);

        log.debug("Close entity manager ...");
        em.close();
        return freshRabbitmqCluster;
    }

    @Override
    public RabbitmqNode refreshRabbitmqNode(Long nodeID) {
        if (RabbitmqDirectoryBootstrap.getDirectoryJPAProvider()==null) {
            log.error("Directory JPA provider has been unbounded !");
            return null ;
        }
        EntityManager em = RabbitmqDirectoryBootstrap.getDirectoryJPAProvider().createEM();
        CriteriaBuilder builder = em.getCriteriaBuilder();

        CriteriaQuery<RabbitmqNode> rbnc = builder.createQuery(RabbitmqNode.class);
        Root<RabbitmqNode> rbncRoot = rbnc.from(RabbitmqNode.class);
        rbnc.select(rbncRoot).where(builder.equal(rbncRoot.<String>get("id"), nodeID));
        TypedQuery<RabbitmqNode> rbncQuery = em.createQuery(rbnc);

        RabbitmqNode freshRabbitmqNode = null;
        try {
            freshRabbitmqNode = rbncQuery.getSingleResult();
        } catch (NoResultException e) {
            log.error("unable to retrieve RabbitMQ Node component {} from Directory DB!", nodeID);
        } catch (Exception e) {
            throw e;
        }

        if (freshRabbitmqNode!=null)
            putLocationAndTeamPropertiesToRabbitmqNode(freshRabbitmqNode);

        log.debug("Close entity manager ...");
        em.close();
        return freshRabbitmqNode;
    }

    public Set<RabbitmqNode> getNodesFromCluster(Long clusterID) {
        Set<RabbitmqNode> ret = null;
        if (RabbitmqDirectoryBootstrap.getDirectoryJPAProvider()==null) {
            log.error("Directory JPA provider has been unbounded !");
            return null ;
        }
        EntityManager em = RabbitmqDirectoryBootstrap.getDirectoryJPAProvider().createEM();

        CriteriaBuilder builder = em.getCriteriaBuilder();

        CriteriaQuery<RabbitmqCluster> rbcc = builder.createQuery(RabbitmqCluster.class);
        Root<RabbitmqCluster> rbccRoot = rbcc.from(RabbitmqCluster.class);
        rbcc.select(rbccRoot).where(builder.equal(rbccRoot.<String>get("id"), clusterID));
        TypedQuery<RabbitmqCluster> rbccQuery = em.createQuery(rbcc);

        RabbitmqCluster freshRabbitmqCluster = null;
        try {
            freshRabbitmqCluster = rbccQuery.getSingleResult();
            // clone the list from JPA return as we close cleanly the entity manager at the end
            // if not we may have some problems working on the returned Set depending on hibernate mood
            putLocationAndTeamPropertiesToRabbitmqNodes(freshRabbitmqCluster);
            ret = new HashSet<RabbitmqNode>(freshRabbitmqCluster.getNodes());
        } catch (NoResultException e) {
            log.error("unable to retrieve RabbitMQ Cluster component {} from Directory DB!", clusterID);
        } catch (Exception e) {
            throw e;
        }

        log.debug("Close entity manager");
        em.close();
        return ret ;
    }

    @Override
    public HashMap<String, Object> getRemoteClientContainerProperties(String osiName, String teamName) {
        HashMap<String, Object> ret = new HashMap<String, Object>();
        EntityManager em = RabbitmqDirectoryBootstrap.getDirectoryJPAProvider().createEM();

        CriteriaBuilder builder = em.getCriteriaBuilder();

        CriteriaQuery<OSInstance> osicc = builder.createQuery(OSInstance.class);
        Root<OSInstance> osiccRoot = osicc.from(OSInstance.class);
        osicc.select(osiccRoot).where(builder.equal(osiccRoot.<String>get("name"), osiName));
        TypedQuery<OSInstance> osiccQuery = em.createQuery(osicc);

        OSInstance osInstance = null;
        try {
            osInstance = osiccQuery.getSingleResult();
            ret.putAll(getLocationPropertiesFromOSI(osInstance));
        } catch (NoResultException e) {
            log.error("unable to retrieve OS instance {} from Directory DB!", osiName);
        } catch (Exception e) {
            throw e;
        }

        CriteriaQuery<Team> teamcc = builder.createQuery(Team.class);
        Root<Team> teamccRoot = teamcc.from(Team.class);
        teamcc.select(teamccRoot).where(builder.equal(teamccRoot.<String>get("name"), teamName));
        TypedQuery<Team> teamccQuery = em.createQuery(teamcc);

        Team team = null;
        try {
            team = teamccQuery.getSingleResult();
            ret.putAll(getTeamPropertiesFromTeam(team));
        } catch (NoResultException e) {
            log.error("unable to retrieve team {} from Directory DB!", teamName);
        } catch (Exception e) {
            throw e;
        }

        em.close();
        return ret;
    }
}