/**
 * RabbitMQ plugin directory bundle
 * Directories RabbitMQ Component RUD Controller
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

package net.echinopsii.ariane.community.plugin.rabbitmq.directory.controller.rabbitmqcomponent;

import net.echinopsii.ariane.community.core.directory.base.model.organisational.Team;
import net.echinopsii.ariane.community.core.directory.base.model.technical.system.OSInstance;
import net.echinopsii.ariane.community.core.directory.wat.controller.organisational.team.TeamsListController;
import net.echinopsii.ariane.community.core.directory.wat.controller.technical.system.OSInstance.OSInstancesListController;
import net.echinopsii.ariane.community.plugin.rabbitmq.directory.RabbitmqDirectoryBootstrap;
import net.echinopsii.ariane.community.plugin.rabbitmq.directory.controller.rabbitmqcluster.RabbitmqClustersListController;
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
import java.util.HashMap;
import java.util.List;

public class RabbitmqComponentsListController implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final Logger log = LoggerFactory.getLogger(RabbitmqComponentsListController.class);

    private LazyDataModel<RabbitmqComponent> lazyModel = new RabbitmqComponentLazyModel();
    private RabbitmqComponent[]              selectedRabbitmqComponentList ;

    private HashMap<Long, String> changedOSInstance      = new HashMap<Long, String>();
    private HashMap<Long, String> changedSupportTeam     = new HashMap<Long,String>();
    private HashMap<Long, String> changedRabbitmqCluster = new HashMap<Long, String>();

    public LazyDataModel<RabbitmqComponent> getLazyModel() {
        return lazyModel;
    }

    public RabbitmqComponent[] getSelectedRabbitmqComponentList() {
        return selectedRabbitmqComponentList;
    }

    public void setSelectedRabbitmqComponentList(RabbitmqComponent[] selectedRabbitmqComponentList) {
        this.selectedRabbitmqComponentList = selectedRabbitmqComponentList;
    }

    /*
     * RabbitmqComponent update tools
     */
    public HashMap<Long, String> getChangedOSInstance() {
        return changedOSInstance;
    }

    public void setChangedOSInstance(HashMap<Long, String> changedOSInstance) {
        this.changedOSInstance = changedOSInstance;
    }

    public void syncOSInstance(RabbitmqComponent rabbitmqComponent) throws NotSupportedException, SystemException {
        EntityManager em = RabbitmqDirectoryBootstrap.getDirectoryJPAProvider().createEM();
        try {
            for(OSInstance osInstance: OSInstancesListController.getAll()) {
                if (osInstance.getName().equals(changedOSInstance.get(rabbitmqComponent.getId()))) {
                    em.getTransaction().begin();
                    rabbitmqComponent = em.find(rabbitmqComponent.getClass(),rabbitmqComponent.getId());
                    osInstance = em.find(osInstance.getClass(), osInstance.getId());
                    rabbitmqComponent.setOsInstance(osInstance);
                    em.flush();
                    em.getTransaction().commit();
                    FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO,
                                                        "RabbitmqComponent updated successfully !",
                                                        "RabbitmqComponent name : " + rabbitmqComponent.getName());
                    FacesContext.getCurrentInstance().addMessage(null, msg);
                    break;
                }
            }
        } catch (Throwable t) {
            log.debug("Throwable catched !");
            t.printStackTrace();
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                                "Throwable raised while updating RabbitmqComponent " + rabbitmqComponent.getName() + " !",
                                                "Throwable message : " + t.getMessage());
            FacesContext.getCurrentInstance().addMessage(null, msg);
            if (em.getTransaction().isActive())
                em.getTransaction().rollback();
        } finally {
            em.close();
        }
    }

    public String getRabbitmqComponentOSInstanceName(RabbitmqComponent rabbitmqComponent) {
        EntityManager em = RabbitmqDirectoryBootstrap.getDirectoryJPAProvider().createEM();
        rabbitmqComponent = em.find(rabbitmqComponent.getClass(), rabbitmqComponent.getId());
        String name = (rabbitmqComponent.getOsInstance()!=null) ? rabbitmqComponent.getOsInstance().getName() : "None";
        em.close();
        return name;
    }

    public HashMap<Long, String> getChangedSupportTeam() {
        return changedSupportTeam;
    }

    public void setChangedSupportTeam(HashMap<Long, String> changedSupportTeam) {
        this.changedSupportTeam = changedSupportTeam;
    }

    public void syncSupportTeam(RabbitmqComponent rabbitmqComponent) throws NotSupportedException, SystemException {
        EntityManager em = RabbitmqDirectoryBootstrap.getDirectoryJPAProvider().createEM();
        try {
            for(Team team: TeamsListController.getAll()) {
                if (team.getName().equals(changedSupportTeam.get(rabbitmqComponent.getId()))) {
                    em.getTransaction().begin();
                    rabbitmqComponent = em.find(rabbitmqComponent.getClass(),rabbitmqComponent.getId());
                    team = em.find(team.getClass(), team.getId());
                    rabbitmqComponent.setSupportTeam(team);
                    em.flush();
                    em.getTransaction().commit();
                    FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO,
                                                               "RabbitmqComponent updated successfully !",
                                                               "RabbitmqComponent name : " + rabbitmqComponent.getName());
                    FacesContext.getCurrentInstance().addMessage(null, msg);
                    break;
                }
            }
        } catch (Throwable t) {
            log.debug("Throwable catched !");
            t.printStackTrace();
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                                       "Throwable raised while updating RabbitmqComponent " + rabbitmqComponent.getName() + " !",
                                                       "Throwable message : " + t.getMessage());
            FacesContext.getCurrentInstance().addMessage(null, msg);
            if (em.getTransaction().isActive())
                em.getTransaction().rollback();
        } finally {
            em.close();
        }
    }

    public String getRabbitmqComponentTeamName(RabbitmqComponent rabbitmqComponent) {
        EntityManager em = RabbitmqDirectoryBootstrap.getDirectoryJPAProvider().createEM();
        rabbitmqComponent = em.find(rabbitmqComponent.getClass(), rabbitmqComponent.getId());
        String name = (rabbitmqComponent.getSupportTeam()!=null) ? rabbitmqComponent.getSupportTeam().getName() : "None";
        em.close();
        return name;
    }

    public HashMap<Long, String> getChangedRabbitmqCluster() {
        return changedRabbitmqCluster;
    }

    public void setChangedRabbitmqCluster(HashMap<Long, String> changedRabbitmqCluster) {
        this.changedRabbitmqCluster = changedRabbitmqCluster;
    }

    public void syncRabbitmqComponentCluster(RabbitmqComponent rabbitmqComponent) throws NotSupportedException, SystemException {
        EntityManager em = RabbitmqDirectoryBootstrap.getDirectoryJPAProvider().createEM();
        try {
            for(RabbitmqCluster cluster: RabbitmqClustersListController.getAll()) {
                if (cluster.getName().equals(changedRabbitmqCluster.get(rabbitmqComponent.getId()))) {
                    em.getTransaction().begin();
                    rabbitmqComponent = em.find(rabbitmqComponent.getClass(),rabbitmqComponent.getId());
                    cluster = em.find(cluster.getClass(), cluster.getId());
                    rabbitmqComponent.setCluster(cluster);
                    em.flush();
                    em.getTransaction().commit();
                    FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO,
                                                               "RabbitmqComponent updated successfully !",
                                                               "RabbitmqComponent name : " + rabbitmqComponent.getName());
                    FacesContext.getCurrentInstance().addMessage(null, msg);
                    break;
                }
            }
        } catch (Throwable t) {
            log.debug("Throwable catched !");
            t.printStackTrace();
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                                       "Throwable raised while updating RabbitmqComponent " + rabbitmqComponent.getName() + " !",
                                                       "Throwable message : " + t.getMessage());
            FacesContext.getCurrentInstance().addMessage(null, msg);
            if (em.getTransaction().isActive())
                em.getTransaction().rollback();
        } finally {
            em.close();
        }
    }

    public String getRabbitmqComponentClusterName(RabbitmqComponent rabbitmqComponent) {
        EntityManager em = RabbitmqDirectoryBootstrap.getDirectoryJPAProvider().createEM();
        rabbitmqComponent = em.find(rabbitmqComponent.getClass(), rabbitmqComponent.getId());
        String name = (rabbitmqComponent.getCluster()!=null) ? rabbitmqComponent.getCluster().getName() : "None";
        em.close();
        return name;
    }

    public void onRowToggle(ToggleEvent event) throws CloneNotSupportedException {
        log.debug("Row Toogled : {}", new Object[]{event.getVisibility().toString()});
        RabbitmqComponent eventRabbitmqComponent = ((RabbitmqComponent) event.getData());
        if (event.getVisibility().toString().equals("HIDDEN")) {
            changedOSInstance.remove(eventRabbitmqComponent.getId());
            changedSupportTeam.remove(eventRabbitmqComponent.getId());
            changedRabbitmqCluster.remove(eventRabbitmqComponent.getId());
        } else {
            changedOSInstance.put(eventRabbitmqComponent.getId(),"");
            changedSupportTeam.put(eventRabbitmqComponent.getId(),"");
            changedRabbitmqCluster.put(eventRabbitmqComponent.getId(),"");
        }
    }

    public void update(RabbitmqComponent rabbitmqComponent) throws SystemException, NotSupportedException, HeuristicRollbackException, HeuristicMixedException, RollbackException {
        EntityManager em = RabbitmqDirectoryBootstrap.getDirectoryJPAProvider().createEM();
        try {
            em.getTransaction().begin();
            rabbitmqComponent = em.find(rabbitmqComponent.getClass(), rabbitmqComponent.getId()).setNameR(rabbitmqComponent.getName()).
                                                                                              setDescriptionR(rabbitmqComponent.getDescription()).
                                                                                              setPasswdR(rabbitmqComponent.getPasswd()).
                                                                                              setUrlR(rabbitmqComponent.getUrl()).
                                                                                              setUserR(rabbitmqComponent.getUser());
            em.flush();
            em.getTransaction().commit();
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO,
                                                       "RabbitmqComponent updated successfully !",
                                                       "RabbitmqComponent name : " + rabbitmqComponent.getName());
            FacesContext.getCurrentInstance().addMessage(null, msg);
        } catch (Throwable t) {
            log.debug("Throwable catched !");
            t.printStackTrace();
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                                       "Throwable raised while updating RabbitmqComponent " + rabbitmqComponent.getName() + " !",
                                                       "Throwable message : " + t.getMessage());
            FacesContext.getCurrentInstance().addMessage(null, msg);
            if (em.getTransaction().isActive())
                em.getTransaction().rollback();
        } finally {
            em.close();
        }
    }

    /*
     * RabbitmqComponent delete tool
     */
    public void delete() {
        log.debug("Remove selected RabbitmqComponent !");
        for (RabbitmqComponent rabbitmqComponent: selectedRabbitmqComponentList) {
            EntityManager em = RabbitmqDirectoryBootstrap.getDirectoryJPAProvider().createEM();
            try {
                em.getTransaction().begin();
                rabbitmqComponent = em.find(rabbitmqComponent.getClass(), rabbitmqComponent.getId());
                if (rabbitmqComponent.getCluster()!=null)
                    rabbitmqComponent.getCluster().getNodes().remove(rabbitmqComponent);
                em.remove(rabbitmqComponent);
                em.flush();
                em.getTransaction().commit();
                FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO,
                                                           "RabbitmqComponent deleted successfully !",
                                                           "RabbitmqComponent name : " + rabbitmqComponent.getName());
                FacesContext.getCurrentInstance().addMessage(null, msg);
            } catch (Throwable t) {
                log.debug("Throwable catched !");
                t.printStackTrace();
                FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                                           "Throwable raised while creating RabbitmqComponent " + rabbitmqComponent.getName() + " !",
                                                           "Throwable message : " + t.getMessage());
                FacesContext.getCurrentInstance().addMessage(null, msg);
                if(em.getTransaction().isActive())
                    em.getTransaction().rollback();
            } finally {
                em.close();
            }
        }
        selectedRabbitmqComponentList=null;
    }

    /*
     * RabbitmqComponent join tool
     */
    public static List<RabbitmqComponent> getAll() throws SystemException, NotSupportedException {
        EntityManager em = RabbitmqDirectoryBootstrap.getDirectoryJPAProvider().createEM();
        log.debug("Get all RabbitMQ Component from : \n\t{}\n\t{}\n\t{}\n\t{}\n\t{}\n\t{}\n\t{}",
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
        CriteriaQuery<RabbitmqComponent> criteria = builder.createQuery(RabbitmqComponent.class);
        Root<RabbitmqComponent> root = criteria.from(RabbitmqComponent.class);
        criteria.select(root).orderBy(builder.asc(root.get("name")));

        List<RabbitmqComponent> ret = em.createQuery(criteria).getResultList();
        em.close();
        return ret;
    }

    public static List<RabbitmqComponent> getAllForSelector() throws SystemException, NotSupportedException {
        EntityManager em = RabbitmqDirectoryBootstrap.getDirectoryJPAProvider().createEM();
        log.debug("Get all RabbitMQ Component from : \n\t{}\n\t{}\n\t{}\n\t{}\n\t{}\n\t{}\n\t{}",
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
        CriteriaQuery<RabbitmqComponent> criteria = builder.createQuery(RabbitmqComponent.class);
        Root<RabbitmqComponent> root = criteria.from(RabbitmqComponent.class);
        criteria.select(root).orderBy(builder.asc(root.get("name")));

        List<RabbitmqComponent> list =  em.createQuery(criteria).getResultList();
        list.add(0, new RabbitmqComponent().setNameR("Select RabbitMQ Component"));
        em.close();
        return list;
    }
}