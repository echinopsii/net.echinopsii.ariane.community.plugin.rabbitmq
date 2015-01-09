/**
 * RabbitMQ plugin directory bundle
 * Directories RabbitMQ cluster RUD Controller
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

package net.echinopsii.ariane.community.plugin.rabbitmq.directory.controller.rabbitmqcluster;

import net.echinopsii.ariane.community.plugin.rabbitmq.directory.RabbitmqDirectoryBootstrap;
import net.echinopsii.ariane.community.plugin.rabbitmq.directory.controller.rabbitmqnode.RabbitmqNodesListController;
import net.echinopsii.ariane.community.plugin.rabbitmq.directory.model.RabbitmqCluster;
import net.echinopsii.ariane.community.plugin.rabbitmq.directory.model.RabbitmqNode;
import org.primefaces.event.ToggleEvent;
import org.primefaces.model.LazyDataModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import javax.transaction.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class RabbitmqClustersListController implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final Logger log = LoggerFactory.getLogger(RabbitmqClustersListController.class);

    private LazyDataModel<RabbitmqCluster> lazyModel = new RabbitmqClusterLazyModel();
    private RabbitmqCluster[]              selectedRabbitmqClusterList ;

    public LazyDataModel<RabbitmqCluster> getLazyModel() {
        return lazyModel;
    }

    public RabbitmqCluster[] getSelectedRabbitmqClusterList() {
        return selectedRabbitmqClusterList;
    }

    public void setSelectedRabbitmqClusterList(RabbitmqCluster[] selectedRabbitmqClusterList) {
        this.selectedRabbitmqClusterList = selectedRabbitmqClusterList;
    }

    private HashMap<Long,String> addedRBMQNode                 = new HashMap<Long, String>();
    private HashMap<Long,List<RabbitmqNode>> removedNodes = new HashMap<Long, List<RabbitmqNode>>();

    public HashMap<Long, String> getAddedRBMQNode() {
        return addedRBMQNode;
    }

    public void setAddedRBMQNode(HashMap<Long, String> addedRBMQNode) {
        this.addedRBMQNode = addedRBMQNode;
    }

    /**
     * Synchronize added RabbitMQ node into a RabbitMQ cluster to database
     *
     * @param cluster bean UI is working on
     */
    public void syncAddedRBMQNode(RabbitmqCluster cluster) {
        EntityManager em = RabbitmqDirectoryBootstrap.getDirectoryJPAProvider().createEM();
        try {
            for (RabbitmqNode rbmqnode: RabbitmqNodesListController.getAll()) {
                if (rbmqnode.getName().equals(this.addedRBMQNode.get(cluster.getId()))) {
                    em.getTransaction().begin();
                    rbmqnode = em.find(rbmqnode.getClass(), rbmqnode.getId());
                    cluster = em.find(cluster.getClass(), cluster.getId());
                    cluster.getNodes().add(rbmqnode);
                    if (rbmqnode.getCluster()!=null)
                        rbmqnode.getCluster().getNodes().remove(rbmqnode);
                    rbmqnode.setCluster(cluster);
                    em.flush();
                    em.getTransaction().commit();
                    FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO,
                                                               "RabbitMQ cluster updated successfully !",
                                                               "RabbitMQ cluster name : " + cluster.getName());
                    FacesContext.getCurrentInstance().addMessage(null, msg);
                    break;
                }
            }
        } catch (Throwable t) {
            log.debug("Throwable catched !");
            t.printStackTrace();
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                                       "Throwable raised while updating RabbitMQ cluster " + cluster.getName() + " !",
                                                       "Throwable message : " + t.getMessage());
            FacesContext.getCurrentInstance().addMessage(null, msg);
            if (em.getTransaction().isActive())
                em.getTransaction().rollback();
        } finally {
            em.close();
        }
    }

    public HashMap<Long, List<RabbitmqNode>> getRemovedNodes() {
        return removedNodes;
    }

    public void setRemovedNodes(HashMap<Long, List<RabbitmqNode>> removedNodes) {
        this.removedNodes = removedNodes;
    }

    /**
     * Synchronize removed RabbitMQ node from a RabbitMQ cluster to database
     *
     * @param cluster bean UI is working on
     */
    public void syncRemovedNodes(RabbitmqCluster cluster) {
        EntityManager em = RabbitmqDirectoryBootstrap.getDirectoryJPAProvider().createEM();
        try {
            em.getTransaction().begin();
            cluster = em.find(cluster.getClass(), cluster.getId());
            List<RabbitmqNode> rbmqnodes2beRM = this.removedNodes.get(cluster.getId());
            log.debug("syncRemovedNodes:{} ", new Object[]{rbmqnodes2beRM});
            for (RabbitmqNode rbmqc2beRM : rbmqnodes2beRM) {
                rbmqc2beRM = em.find(rbmqc2beRM.getClass(), rbmqc2beRM.getId());
                cluster.getNodes().remove(rbmqc2beRM);
                rbmqc2beRM.setCluster(null);
            }
            em.flush();
            em.getTransaction().commit();
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO,
                                                       "RabbitMQ cluster updated successfully !",
                                                       "RabbitMQ cluster name : " + cluster.getName());
            FacesContext.getCurrentInstance().addMessage(null, msg);
        } catch (Throwable t) {
            log.debug("Throwable catched !");
            t.printStackTrace();
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                                       "Throwable raised while updating RabbitMQ cluster " + cluster.getName() + " !",
                                                       "Throwable message : " + t.getMessage());
            FacesContext.getCurrentInstance().addMessage(null, msg);
            if (em.getTransaction().isActive())
                em.getTransaction().rollback();
        } finally {
            em.close();
        }
    }

    public void onRowToggle(ToggleEvent event) throws CloneNotSupportedException {
        log.debug("Row Toogled : {}", new Object[]{event.getVisibility().toString()});
        RabbitmqCluster eventRabbitmqCluster = ((RabbitmqCluster) event.getData());
        if (event.getVisibility().toString().equals("HIDDEN")) {
            addedRBMQNode.remove(eventRabbitmqCluster.getId());
            removedNodes.remove(eventRabbitmqCluster.getId());
        } else {
            addedRBMQNode.put(eventRabbitmqCluster.getId(),"");
            removedNodes.put(eventRabbitmqCluster.getId(), new ArrayList<RabbitmqNode>());
        }
    }

    public void update(RabbitmqCluster rabbitmqCluster) throws SystemException, NotSupportedException, HeuristicRollbackException, HeuristicMixedException, RollbackException {
        EntityManager em = RabbitmqDirectoryBootstrap.getDirectoryJPAProvider().createEM();
        try {
            em.getTransaction().begin();
            rabbitmqCluster = em.find(rabbitmqCluster.getClass(), rabbitmqCluster.getId()).setNameR(rabbitmqCluster.getName()).
                                                                                              setDescriptionR(rabbitmqCluster.getDescription());
            em.flush();
            em.getTransaction().commit();
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO,
                                                       "RabbitmqCluster updated successfully !",
                                                       "RabbitmqCluster name : " + rabbitmqCluster.getName());
            FacesContext.getCurrentInstance().addMessage(null, msg);
        } catch (Throwable t) {
            log.debug("Throwable catched !");
            t.printStackTrace();
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                                       "Throwable raised while updating RabbitmqCluster " + rabbitmqCluster.getName() + " !",
                                                       "Throwable message : " + t.getMessage());
            FacesContext.getCurrentInstance().addMessage(null, msg);
            if (em.getTransaction().isActive())
                em.getTransaction().rollback();
        } finally {
            em.close();
        }
    }

    /*
     * RabbitmqCluster delete tool
     */
    public void delete() {
        log.debug("Remove selected RabbitmqCluster !");
        for (RabbitmqCluster rabbitmqCluster: selectedRabbitmqClusterList) {
            EntityManager em = RabbitmqDirectoryBootstrap.getDirectoryJPAProvider().createEM();
            try {
                em.getTransaction().begin();
                rabbitmqCluster = em.find(rabbitmqCluster.getClass(), rabbitmqCluster.getId());
                for (RabbitmqNode node: rabbitmqCluster.getNodes())
                    node.setCluster(null);
                em.remove(rabbitmqCluster);
                em.flush();
                em.getTransaction().commit();
                FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO,
                                                           "RabbitmqCluster deleted successfully !",
                                                           "RabbitmqCluster name : " + rabbitmqCluster.getName());
                FacesContext.getCurrentInstance().addMessage(null, msg);
            } catch (Throwable t) {
                log.debug("Throwable catched !");
                t.printStackTrace();
                FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                                           "Throwable raised while creating RabbitmqCluster " + rabbitmqCluster.getName() + " !",
                                                           "Throwable message : " + t.getMessage());
                FacesContext.getCurrentInstance().addMessage(null, msg);
                if(em.getTransaction().isActive())
                    em.getTransaction().rollback();
            } finally {
                em.close();
            }
        }
        selectedRabbitmqClusterList=null;
    }

    /*
     * RabbitmqCluster join tool
     */
    public static List<RabbitmqCluster> getAll() throws SystemException, NotSupportedException {
        EntityManager em = RabbitmqDirectoryBootstrap.getDirectoryJPAProvider().createEM();
        log.debug("Get all RabbitMQ Cluster from : \n\t{}\n\t{}\n\t{}\n\t{}\n\t{}\n\t{}\n\t{}",
                         new Object[]{
                                             (Thread.currentThread().getStackTrace().length>0) ? Thread.currentThread().getStackTrace()[0].getClassName() : "",
                                             (Thread.currentThread().getStackTrace().length>1) ? Thread.currentThread().getStackTrace()[1].getClassName() : "",
                                             (Thread.currentThread().getStackTrace().length>2) ? Thread.currentThread().getStackTrace()[2].getClassName() : "",
                                             (Thread.currentThread().getStackTrace().length>3) ? Thread.currentThread().getStackTrace()[3].getClassName() : "",
                                             (Thread.currentThread().getStackTrace().length>4) ? Thread.currentThread().getStackTrace()[4].getClassName() : "",
                                             (Thread.currentThread().getStackTrace().length>5) ? Thread.currentThread().getStackTrace()[5].getClassName() : "",
                                             (Thread.currentThread().getStackTrace().length>6) ? Thread.currentThread().getStackTrace()[6].getClassName() : ""
                         });
        CriteriaBuilder builder = em.getCriteriaBuilder();
        CriteriaQuery<RabbitmqCluster> criteria = builder.createQuery(RabbitmqCluster.class);
        Root<RabbitmqCluster> root = criteria.from(RabbitmqCluster.class);
        criteria.select(root).orderBy(builder.asc(root.get("name")));

        List<RabbitmqCluster> ret = em.createQuery(criteria).getResultList();
        em.close();
        return ret;
    }

    public static List<RabbitmqCluster> getAllForSelector() throws SystemException, NotSupportedException {
        EntityManager em = RabbitmqDirectoryBootstrap.getDirectoryJPAProvider().createEM();
        log.debug("Get all RabbitMQ Cluster from : \n\t{}\n\t{}\n\t{}\n\t{}\n\t{}\n\t{}\n\t{}",
                         new Object[]{
                                             (Thread.currentThread().getStackTrace().length>0) ? Thread.currentThread().getStackTrace()[0].getClassName() : "",
                                             (Thread.currentThread().getStackTrace().length>1) ? Thread.currentThread().getStackTrace()[1].getClassName() : "",
                                             (Thread.currentThread().getStackTrace().length>2) ? Thread.currentThread().getStackTrace()[2].getClassName() : "",
                                             (Thread.currentThread().getStackTrace().length>3) ? Thread.currentThread().getStackTrace()[3].getClassName() : "",
                                             (Thread.currentThread().getStackTrace().length>4) ? Thread.currentThread().getStackTrace()[4].getClassName() : "",
                                             (Thread.currentThread().getStackTrace().length>5) ? Thread.currentThread().getStackTrace()[5].getClassName() : "",
                                             (Thread.currentThread().getStackTrace().length>6) ? Thread.currentThread().getStackTrace()[6].getClassName() : ""
                         });
        CriteriaBuilder builder  = em.getCriteriaBuilder();
        CriteriaQuery<RabbitmqCluster> criteria = builder.createQuery(RabbitmqCluster.class);
        Root<RabbitmqCluster> root = criteria.from(RabbitmqCluster.class);
        criteria.select(root).orderBy(builder.asc(root.get("name")));

        List<RabbitmqCluster> list =  em.createQuery(criteria).getResultList();
        list.add(0, new RabbitmqCluster().setNameR("Select RabbitMQ Cluster"));
        em.close();
        return list;
    }
}