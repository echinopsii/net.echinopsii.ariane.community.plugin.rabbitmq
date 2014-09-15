/**
 * [DEFINE YOUR PROJECT NAME/MODULE HERE]
 * [DEFINE YOUR PROJECT DESCRIPTION HERE] 
 * Copyright (C) 15/09/14 echinopsii
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

package net.echinopsii.ariane.community.plugin.rabbitmq.directory.model;

import net.echinopsii.ariane.community.core.directory.base.model.organisational.Team;
import net.echinopsii.ariane.community.core.directory.base.model.technical.system.OSInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.HashMap;

@Entity
@XmlRootElement
@Table(name="rabbitmqComponent",uniqueConstraints = @UniqueConstraint(columnNames = {"rabbitmqComponentName","rabbitmqComponentURL"}))
public class RabbitmqComponent implements Serializable {
    private static final long serialVersionUID = 1L;

    private static final Logger log = LoggerFactory.getLogger(RabbitmqComponent.class);

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", updatable = false, nullable = false)
    private Long id = null;
    @Version
    @Column(name = "version")
    private int version = 0;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public RabbitmqComponent setIdR(Long id) {
        this.id = id;
        return this;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public RabbitmqComponent setVersionR(int version) {
        this.version = version;
        return this;
    }

    @Column(name="rabbitmqComponentName",unique=true)
    @NotNull
    private String name = null;
    @Column(name="rabbitmqComponentURL",unique=true)
    @NotNull
    private String url = null;
    @Column
    @NotNull
    private String user = null;
    @Column
    private String passwd = null;
    @Column
    private String description = null;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public RabbitmqComponent setNameR(String name) {
        this.name = name;
        return this;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public RabbitmqComponent setUserR(String user) {
        this.user = user;
        return this;
    }

    public String getPasswd() {
        return passwd;
    }

    public void setPasswd(String passwd) {
        this.passwd = passwd;
    }

    public RabbitmqComponent setPasswdR(String passwd) {
        this.passwd = passwd;
        return this;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public RabbitmqComponent setUrlR(String url) {
        this.url = url;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public RabbitmqComponent setDescriptionR(String description) {
        this.description = description;
        return this;
    }

    @ManyToOne
    @NotNull
    private OSInstance osInstance;

    @ManyToOne
    @NotNull
    private Team supportTeam;

    public OSInstance getOsInstance() {
        return osInstance;
    }

    public void setOsInstance(OSInstance osInstance) {
        this.osInstance = osInstance;
    }

    public RabbitmqComponent setOsInstanceR(OSInstance osInstance) {
        this.osInstance = osInstance;
        return this;
    }

    public Team getSupportTeam() {
        return supportTeam;
    }

    public void setSupportTeam(Team supportTeam) {
        this.supportTeam = supportTeam;
    }

    public RabbitmqComponent setSupportTeamR(Team supportTeam) {
        this.supportTeam = supportTeam;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        RabbitmqComponent tmp = (RabbitmqComponent) o;
        if (this.url == null) {
            return super.equals(o);
        }
        return (this.url.equals(tmp.getUrl()));
    }

    @Override
    public int hashCode() {
        return this.url != null ? this.url.hashCode() : super.hashCode();
    }

    public RabbitmqComponent clone() {
        return new RabbitmqComponent().setIdR(id).setVersionR(version).setNameR(name).setUrlR(url).setUserR(user).setPasswdR(passwd).
                                       setDescriptionR(description).setOsInstanceR(osInstance.clone()).setSupportTeamR(supportTeam.clone());
    }

    @Transient
    HashMap<String, Object> properties = null;

    @Transient
    String multicastArea = null;

    @Transient
    public HashMap<String, Object> getProperties() {
        return properties;
    }

    @Transient
    public void setProperties(HashMap<String, Object> properties) {
        this.properties = properties;
    }

}