/**
 * RabbitMQ plugin directory bundle
 * Directories RabbitMQ Node RUD Controller
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

package net.echinopsii.ariane.community.plugin.rabbitmq.directory.controller.rabbitmqnode;

import net.echinopsii.ariane.community.core.directory.base.model.organisational.Team;
import net.echinopsii.ariane.community.core.directory.base.model.technical.system.OSInstance;
import net.echinopsii.ariane.community.core.directory.wat.controller.organisational.team.TeamsListController;
import net.echinopsii.ariane.community.core.directory.wat.controller.technical.system.OSInstance.OSInstancesListController;
import net.echinopsii.ariane.community.plugin.rabbitmq.directory.RabbitmqDirectoryBootstrap;
import net.echinopsii.ariane.community.plugin.rabbitmq.directory.controller.rabbitmqcluster.RabbitmqClustersListController;
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
import java.util.HashMap;
import java.util.List;

public class RabbitmqNodesListController implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final Logger log = LoggerFactory.getLogger(RabbitmqNodesListController.class);

    private LazyDataModel<RabbitmqNode> lazyModel = new RabbitmqNodeLazyModel();
    private RabbitmqNode[]              selectedRabbitmqNodeList ;

    private HashMap<Long, String> changedOSInstance      = new HashMap<Long, String>();
    private HashMap<Long, String> changedSupportTeam     = new HashMap<Long,String>();
    private HashMap<Long, String> changedRabbitmqCluster = new HashMap<Long, String>();

    public LazyDataModel<RabbitmqNode> getLazyModel() {
        return lazyModel;
    }

    public RabbitmqNode[] getSelectedRabbitmqNodeList() {
        return selectedRabbitmqNodeList;
    }

    public void setSelectedRabbitmqNodeList(RabbitmqNode[] selectedRabbitmqNodeList) {
        this.selectedRabbitmqNodeList = selectedRabbitmqNodeList;
    }

    /*
     * RabbitmqNode update tools
     */
    public HashMap<Long, String> getChangedOSInstance() {
        return changedOSInstance;
    }

    public void setChangedOSInstance(HashMap<Long, String> changedOSInstance) {
        this.changedOSInstance = changedOSInstance;
    }

    public void syncOSInstance(RabbitmqNode rabbitmqNode) throws NotSupportedException, SystemException {
        EntityManager em = RabbitmqDirectoryBootstrap.getDirectoryJPAProvider().createEM();
        try {
            for(OSInstance osInstance: OSInstancesListController.getAll()) {
                if (osInstance.getName().equals(changedOSInstance.get(rabbitmqNode.getId()))) {
                    em.getTransaction().begin();
                    rabbitmqNode = em.find(rabbitmqNode.getClass(),rabbitmqNode.getId());
                    osInstance = em.find(osInstance.getClass(), osInstance.getId());
                    rabbitmqNode.setOsInstance(osInstance);
                    em.flush();
                    em.getTransaction().commit();
                    FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO,
                                                        "RabbitmqNode updated successfully !",
                                                        "RabbitmqNode name : " + rabbitmqNode.getName());
                    FacesContext.getCurrentInstance().addMessage(null, msg);
                    break;
                }
            }
        } catch (Throwable t) {
            log.debug("Throwable catched !");
            t.printStackTrace();
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                                "Throwable raised while updating RabbitmqNode " + rabbitmqNode.getName() + " !",
                                                "Throwable message : " + t.getMessage());
            FacesContext.getCurrentInstance().addMessage(null, msg);
            if (em.getTransaction().isActive())
                em.getTransaction().rollback();
        } finally {
            em.close();
        }
    }

    public String getRabbitmqNodeOSInstanceName(RabbitmqNode rabbitmqNode) {
        EntityManager em = RabbitmqDirectoryBootstrap.getDirectoryJPAProvider().createEM();
        rabbitmqNode = em.find(rabbitmqNode.getClass(), rabbitmqNode.getId());
        String name = (rabbitmqNode.getOsInstance()!=null) ? rabbitmqNode.getOsInstance().getName() : "None";
        em.close();
        return name;
    }

    public HashMap<Long, String> getChangedSupportTeam() {
        return changedSupportTeam;
    }

    public void setChangedSupportTeam(HashMap<Long, String> changedSupportTeam) {
        this.changedSupportTeam = changedSupportTeam;
    }

    public void syncSupportTeam(RabbitmqNode rabbitmqNode) throws NotSupportedException, SystemException {
        EntityManager em = RabbitmqDirectoryBootstrap.getDirectoryJPAProvider().createEM();
        try {
            for(Team team: TeamsListController.getAll()) {
                if (team.getName().equals(changedSupportTeam.get(rabbitmqNode.getId()))) {
                    em.getTransaction().begin();
                    rabbitmqNode = em.find(rabbitmqNode.getClass(),rabbitmqNode.getId());
                    team = em.find(team.getClass(), team.getId());
                    rabbitmqNode.setSupportTeam(team);
                    em.flush();
                    em.getTransaction().commit();
                    FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO,
                                                               "RabbitmqNode updated successfully !",
                                                               "RabbitmqNode name : " + rabbitmqNode.getName());
                    FacesContext.getCurrentInstance().addMessage(null, msg);
                    break;
                }
            }
        } catch (Throwable t) {
            log.debug("Throwable catched !");
            t.printStackTrace();
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                                       "Throwable raised while updating RabbitmqNode " + rabbitmqNode.getName() + " !",
                                                       "Throwable message : " + t.getMessage());
            FacesContext.getCurrentInstance().addMessage(null, msg);
            if (em.getTransaction().isActive())
                em.getTransaction().rollback();
        } finally {
            em.close();
        }
    }

    public String getRabbitmqNodeTeamName(RabbitmqNode rabbitmqNode) {
        EntityManager em = RabbitmqDirectoryBootstrap.getDirectoryJPAProvider().createEM();
        rabbitmqNode = em.find(rabbitmqNode.getClass(), rabbitmqNode.getId());
        String name = (rabbitmqNode.getSupportTeam()!=null) ? rabbitmqNode.getSupportTeam().getName() : "None";
        em.close();
        return name;
    }

    public HashMap<Long, String> getChangedRabbitmqCluster() {
        return changedRabbitmqCluster;
    }

    public void setChangedRabbitmqCluster(HashMap<Long, String> changedRabbitmqCluster) {
        this.changedRabbitmqCluster = changedRabbitmqCluster;
    }

    public void syncRabbitmqNodeCluster(RabbitmqNode rabbitmqNode) throws NotSupportedException, SystemException {
        EntityManager em = RabbitmqDirectoryBootstrap.getDirectoryJPAProvider().createEM();
        try {
            for(RabbitmqCluster cluster: RabbitmqClustersListController.getAll()) {
                if (cluster.getName().equals(changedRabbitmqCluster.get(rabbitmqNode.getId()))) {
                    em.getTransaction().begin();
                    rabbitmqNode = em.find(rabbitmqNode.getClass(),rabbitmqNode.getId());
                    cluster = em.find(cluster.getClass(), cluster.getId());
                    rabbitmqNode.setCluster(cluster);
                    em.flush();
                    em.getTransaction().commit();
                    FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO,
                                                               "RabbitmqNode updated successfully !",
                                                               "RabbitmqNode name : " + rabbitmqNode.getName());
                    FacesContext.getCurrentInstance().addMessage(null, msg);
                    break;
                }
            }
        } catch (Throwable t) {
            log.debug("Throwable catched !");
            t.printStackTrace();
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                                       "Throwable raised while updating RabbitmqNode " + rabbitmqNode.getName() + " !",
                                                       "Throwable message : " + t.getMessage());
            FacesContext.getCurrentInstance().addMessage(null, msg);
            if (em.getTransaction().isActive())
                em.getTransaction().rollback();
        } finally {
            em.close();
        }
    }

    public String getRabbitmqNodeClusterName(RabbitmqNode rabbitmqNode) {
        EntityManager em = RabbitmqDirectoryBootstrap.getDirectoryJPAProvider().createEM();
        rabbitmqNode = em.find(rabbitmqNode.getClass(), rabbitmqNode.getId());
        String name = (rabbitmqNode.getCluster()!=null) ? rabbitmqNode.getCluster().getName() : "None";
        em.close();
        return name;
    }

    public void onRowToggle(ToggleEvent event) throws CloneNotSupportedException {
        log.debug("Row Toogled : {}", new Object[]{event.getVisibility().toString()});
        RabbitmqNode eventRabbitmqNode = ((RabbitmqNode) event.getData());
        if (event.getVisibility().toString().equals("HIDDEN")) {
            changedOSInstance.remove(eventRabbitmqNode.getId());
            changedSupportTeam.remove(eventRabbitmqNode.getId());
            changedRabbitmqCluster.remove(eventRabbitmqNode.getId());
        } else {
            changedOSInstance.put(eventRabbitmqNode.getId(),"");
            changedSupportTeam.put(eventRabbitmqNode.getId(),"");
            changedRabbitmqCluster.put(eventRabbitmqNode.getId(),"");
        }
    }

    public void update(RabbitmqNode rabbitmqNode) throws SystemException, NotSupportedException, HeuristicRollbackException, HeuristicMixedException, RollbackException {
        EntityManager em = RabbitmqDirectoryBootstrap.getDirectoryJPAProvider().createEM();
        try {
            em.getTransaction().begin();
            rabbitmqNode = em.find(rabbitmqNode.getClass(), rabbitmqNode.getId()).setNameR(rabbitmqNode.getName()).
                                                                                              setDescriptionR(rabbitmqNode.getDescription()).
                                                                                              setPasswdR(rabbitmqNode.getPasswd()).
                                                                                              setUrlR(rabbitmqNode.getUrl()).
                                                                                              setUserR(rabbitmqNode.getUser());
            em.flush();
            em.getTransaction().commit();
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO,
                                                       "RabbitmqNode updated successfully !",
                                                       "RabbitmqNode name : " + rabbitmqNode.getName());
            FacesContext.getCurrentInstance().addMessage(null, msg);
        } catch (Throwable t) {
            log.debug("Throwable catched !");
            t.printStackTrace();
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                                       "Throwable raised while updating RabbitmqNode " + rabbitmqNode.getName() + " !",
                                                       "Throwable message : " + t.getMessage());
            FacesContext.getCurrentInstance().addMessage(null, msg);
            if (em.getTransaction().isActive())
                em.getTransaction().rollback();
        } finally {
            em.close();
        }
    }

    /*
     * RabbitmqNode delete tool
     */
    public void delete() {
        log.debug("Remove selected RabbitmqNode !");
        for (RabbitmqNode rabbitmqNode: selectedRabbitmqNodeList) {
            EntityManager em = RabbitmqDirectoryBootstrap.getDirectoryJPAProvider().createEM();
            try {
                em.getTransaction().begin();
                rabbitmqNode = em.find(rabbitmqNode.getClass(), rabbitmqNode.getId());
                if (rabbitmqNode.getCluster()!=null)
                    rabbitmqNode.getCluster().getNodes().remove(rabbitmqNode);
                em.remove(rabbitmqNode);
                em.flush();
                em.getTransaction().commit();
                FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO,
                                                           "RabbitmqNode deleted successfully !",
                                                           "RabbitmqNode name : " + rabbitmqNode.getName());
                FacesContext.getCurrentInstance().addMessage(null, msg);
            } catch (Throwable t) {
                log.debug("Throwable catched !");
                t.printStackTrace();
                FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                                           "Throwable raised while creating RabbitmqNode " + rabbitmqNode.getName() + " !",
                                                           "Throwable message : " + t.getMessage());
                FacesContext.getCurrentInstance().addMessage(null, msg);
                if(em.getTransaction().isActive())
                    em.getTransaction().rollback();
            } finally {
                em.close();
            }
        }
        selectedRabbitmqNodeList=null;
    }

    /*
     * RabbitmqNode join tool
     */
    public static List<RabbitmqNode> getAll() throws SystemException, NotSupportedException {
        EntityManager em = RabbitmqDirectoryBootstrap.getDirectoryJPAProvider().createEM();
        log.debug("Get all RabbitMQ Node from : \n\t{}\n\t{}\n\t{}\n\t{}\n\t{}\n\t{}\n\t{}",
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
        CriteriaQuery<RabbitmqNode> criteria = builder.createQuery(RabbitmqNode.class);
        Root<RabbitmqNode> root = criteria.from(RabbitmqNode.class);
        criteria.select(root).orderBy(builder.asc(root.get("name")));

        List<RabbitmqNode> ret = em.createQuery(criteria).getResultList();
        em.close();
        return ret;
    }

    public static List<RabbitmqNode> getAllForSelector() throws SystemException, NotSupportedException {
        EntityManager em = RabbitmqDirectoryBootstrap.getDirectoryJPAProvider().createEM();
        log.debug("Get all RabbitMQ Node from : \n\t{}\n\t{}\n\t{}\n\t{}\n\t{}\n\t{}\n\t{}",
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
        CriteriaQuery<RabbitmqNode> criteria = builder.createQuery(RabbitmqNode.class);
        Root<RabbitmqNode> root = criteria.from(RabbitmqNode.class);
        criteria.select(root).orderBy(builder.asc(root.get("name")));

        List<RabbitmqNode> list =  em.createQuery(criteria).getResultList();
        list.add(0, new RabbitmqNode().setNameR("Select RabbitMQ Node"));
        em.close();
        return list;
    }
}