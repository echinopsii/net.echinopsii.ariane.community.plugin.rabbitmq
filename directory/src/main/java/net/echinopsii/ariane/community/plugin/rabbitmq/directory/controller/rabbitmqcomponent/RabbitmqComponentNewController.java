/**
 * Tibco rv addon directory bundle
 * Directories TibcoRV Component Create controller
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
import net.echinopsii.ariane.community.plugin.rabbitmq.directory.model.RabbitmqComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PreDestroy;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.persistence.EntityManager;
import javax.transaction.*;
import java.io.Serializable;

public class RabbitmqComponentNewController implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final Logger log = LoggerFactory.getLogger(RabbitmqComponentNewController.class);

    private EntityManager em = RabbitmqDirectoryBootstrap.getDirectoryJPAProvider().createEM();

    @PreDestroy
    public void clean() {
        log.debug("Close entity manager");
        em.close();
    }

    public EntityManager getEm() {
        return em;
    }

    private String name;
    private String url;
    private String user;
    private String password;
    private String description;

    private String osInstance;
    private OSInstance osInt ;

    private String supportTeam;
    private Team   suppTeam;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getOsInstance() {
        return osInstance;
    }

    public void setOsInstance(String osInstance) {
        this.osInstance = osInstance;
    }

    public OSInstance getOsInt() {
        return osInt;
    }

    public void setOsInt(OSInstance osInt) {
        this.osInt = osInt;
    }

    private void syncOSIntance() throws NotSupportedException, SystemException {
        OSInstance finalOSI = null;
        for (OSInstance instance: OSInstancesListController.getAll()) {
            if (instance.getName().equals(this.osInstance)) {
                instance = em.find(instance.getClass(), instance.getId());
                finalOSI = instance;
                break;
            }
        }
        if (finalOSI != null) {
            this.osInt  = finalOSI;
            log.debug("Synced OSInstance : {} {}", new Object[]{this.osInt.getId(), this.osInt.getName()});
        }
    }

    public String getSupportTeam() {
        return supportTeam;
    }

    public void setSupportTeam(String supportTeam) {
        this.supportTeam = supportTeam;
    }

    public Team getSuppTeam() {
        return suppTeam;
    }

    public void setSuppTeam(Team suppTeam) {
        this.suppTeam = suppTeam;
    }

    private void syncSupportTeam() throws NotSupportedException, SystemException {
        Team finalTeam = null;
        for (Team team: TeamsListController.getAll()) {
            if (team.getName().equals(this.supportTeam)) {
                team = em.find(team.getClass(), team.getId());
                finalTeam = team;
                break;
            }
        }
        if (finalTeam != null) {
            this.suppTeam  = finalTeam;
            log.debug("Synced Team : {} {}", new Object[]{this.suppTeam.getId(), this.suppTeam.getName()});
        }
    }

    public void save() throws SystemException, NotSupportedException, HeuristicRollbackException, HeuristicMixedException, RollbackException {
        try {
            syncOSIntance();
            syncSupportTeam();
        } catch (Exception e) {
            e.printStackTrace();
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                                       "Exception raise while creating TibcoRV Bus " + name + " !",
                                                       "Exception message : " + e.getMessage());
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;
        }

        RabbitmqComponent rabbitmqComponent = new RabbitmqComponent();
        rabbitmqComponent.setName(name);
        rabbitmqComponent.setUser(user);
        rabbitmqComponent.setPasswd(password);
        rabbitmqComponent.setUrl(url);
        rabbitmqComponent.setDescription(description);
        rabbitmqComponent.setOsInstance(osInt);
        rabbitmqComponent.setSupportTeam(suppTeam);

        try {
            em.getTransaction().begin();
            em.persist(rabbitmqComponent);
            em.flush();
            em.getTransaction().commit();
            log.debug("Save new RabbitmqBus {} !", new Object[]{name});
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO,
                                                       "RabbitmqComponent created successfully !",
                                                       "RabbitmqComponent name : " + rabbitmqComponent.getName());
            FacesContext.getCurrentInstance().addMessage(null, msg);
        } catch (Throwable t) {
            log.debug("Throwable catched !");
            t.printStackTrace();
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                                       "Throwable raised while creating RabbitmqComponent " + rabbitmqComponent.getName() + " !",
                                                       "Throwable message : " + t.getMessage());
            FacesContext.getCurrentInstance().addMessage(null, msg);

            if (em.getTransaction().isActive())
                em.getTransaction().rollback();
        }
    }
}