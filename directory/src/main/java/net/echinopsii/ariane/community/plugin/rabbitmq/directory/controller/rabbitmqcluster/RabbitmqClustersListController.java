/**
 * RabbitMQ plugin directory bundle
 * Directories RabbitMQ cluster RUD Controller
 * Copyright (C) 2013 Mathilde Ffrench
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
import net.echinopsii.ariane.community.plugin.rabbitmq.directory.controller.rabbitmqcomponent.RabbitmqComponentsListController;
import net.echinopsii.ariane.community.plugin.rabbitmq.directory.model.RabbitmqCluster;
import net.echinopsii.ariane.community.plugin.rabbitmq.directory.model.RabbitmqComponent;
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

    private HashMap<Long,String> addedRBMQComponent                 = new HashMap<Long, String>();
    private HashMap<Long,List<RabbitmqComponent>> removedComponents = new HashMap<Long, List<RabbitmqComponent>>();

    public HashMap<Long, String> getAddedRBMQComponent() {
        return addedRBMQComponent;
    }

    public void setAddedRBMQComponent(HashMap<Long, String> addedRBMQComponent) {
        this.addedRBMQComponent = addedRBMQComponent;
    }

    /**
     * Synchronize added RabbitMQ component into a RabbitMQ cluster to database
     *
     * @param cluster bean UI is working on
     */
    public void syncAddedRBMQComponent(RabbitmqCluster cluster) {
        EntityManager em = RabbitmqDirectoryBootstrap.getDirectoryJPAProvider().createEM();
        try {
            for (RabbitmqComponent rbmqcomponent: RabbitmqComponentsListController.getAll()) {
                if (rbmqcomponent.getName().equals(this.addedRBMQComponent.get(cluster.getId()))) {
                    em.getTransaction().begin();
                    rbmqcomponent = em.find(rbmqcomponent.getClass(), rbmqcomponent.getId());
                    cluster = em.find(cluster.getClass(), cluster.getId());
                    cluster.getNodes().add(rbmqcomponent);
                    if (rbmqcomponent.getCluster()!=null)
                        rbmqcomponent.getCluster().getNodes().remove(rbmqcomponent);
                    rbmqcomponent.setCluster(cluster);
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

    public HashMap<Long, List<RabbitmqComponent>> getRemovedComponents() {
        return removedComponents;
    }

    public void setRemovedComponents(HashMap<Long, List<RabbitmqComponent>> removedComponents) {
        this.removedComponents = removedComponents;
    }

    /**
     * Synchronize removed RabbitMQ component from a RabbitMQ cluster to database
     *
     * @param cluster bean UI is working on
     */
    public void syncRemovedComponents(RabbitmqCluster cluster) {
        EntityManager em = RabbitmqDirectoryBootstrap.getDirectoryJPAProvider().createEM();
        try {
            em.getTransaction().begin();
            cluster = em.find(cluster.getClass(), cluster.getId());
            List<RabbitmqComponent> rbmqcomponents2beRM = this.removedComponents.get(cluster.getId());
            log.debug("syncRemovedComponents:{} ", new Object[]{rbmqcomponents2beRM});
            for (RabbitmqComponent rbmqc2beRM : rbmqcomponents2beRM) {
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
            addedRBMQComponent.remove(eventRabbitmqCluster.getId());
            removedComponents.remove(eventRabbitmqCluster.getId());
        } else {
            addedRBMQComponent.put(eventRabbitmqCluster.getId(),"");
            removedComponents.put(eventRabbitmqCluster.getId(), new ArrayList<RabbitmqComponent>());
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
                for (RabbitmqComponent component: rabbitmqCluster.getNodes())
                    component.setCluster(null);
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
                                             (Thread.currentThread().getStackTrace().length>0) ? Thread.currentThread().getStackTrace()[6].getClassName() : ""
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
                                             (Thread.currentThread().getStackTrace().length>0) ? Thread.currentThread().getStackTrace()[6].getClassName() : ""
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