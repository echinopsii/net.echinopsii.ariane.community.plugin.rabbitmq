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

import net.echinopsii.ariane.community.plugin.rabbitmq.directory.controller.rabbitmqcluster.RabbitmqClustersListController;
import net.echinopsii.ariane.community.plugin.rabbitmq.directory.controller.rabbitmqnode.RabbitmqNodesListController;
import net.echinopsii.ariane.community.plugin.rabbitmq.directory.model.RabbitmqCluster;
import net.echinopsii.ariane.community.plugin.rabbitmq.directory.model.RabbitmqNode;
import org.apache.felix.ipojo.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import javax.transaction.NotSupportedException;
import javax.transaction.SystemException;
import java.util.HashSet;

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
}