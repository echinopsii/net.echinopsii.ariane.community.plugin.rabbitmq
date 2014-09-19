/**
 * RabbitMQ plugin directory bundle
 * Directories RabbitMQ Cluster
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

package net.echinopsii.ariane.community.plugin.rabbitmq.directory.model;

import net.echinopsii.ariane.community.core.directory.base.model.organisational.Team;
import net.echinopsii.ariane.community.core.directory.base.model.technical.system.OSInstance;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

@Entity
@XmlRootElement
@Table(name="rabbitmqCluster",uniqueConstraints = @UniqueConstraint(columnNames = {"rabbitmqClusterName"}))
public class RabbitmqCluster implements Serializable {
    private static final long serialVersionUID = 1L;

    private static final Logger log = LoggerFactory.getLogger(RabbitmqCluster.class);

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

    public RabbitmqCluster setIdR(Long id) {
        this.id = id;
        return this;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public RabbitmqCluster setVersionR(int version) {
        this.version = version;
        return this;
    }

    @Column(name="rabbitmqClusterName",unique=true)
    @NotNull
    private String name = null;
    @Column
    private String description = null;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public RabbitmqCluster setNameR(String name) {
        this.name = name;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public RabbitmqCluster setDescriptionR(String description) {
        this.description = description;
        return this;
    }

    @OneToMany(mappedBy = "cluster", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Fetch(FetchMode.SUBSELECT)
    private Set<RabbitmqComponent> nodes = new HashSet<RabbitmqComponent>();

    public Set<RabbitmqComponent> getNodes() {
        return nodes;
    }

    public void setNodes(Set<RabbitmqComponent> nodes) {
        this.nodes = nodes;
    }

    public RabbitmqCluster setNodesR(Set<RabbitmqComponent> nodes) {
        this.nodes = nodes;
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

        RabbitmqCluster tmp = (RabbitmqCluster) o;
        if (this.name == null) {
            return super.equals(o);
        }
        return (this.name.equals(tmp.getName()));
    }

    @Override
    public int hashCode() {
        return this.name != null ? this.name.hashCode() : super.hashCode();
    }

    public RabbitmqCluster clone() {
        return new RabbitmqCluster().setIdR(id).setVersionR(version).setNameR(name).setDescriptionR(description).setNodesR(new HashSet<>(this.nodes));
    }

    @Transient
    HashMap<String, Object> properties = null;

    @Transient
    public HashMap<String, Object> getProperties() {
        return properties;
    }

    @Transient
    public void setProperties(HashMap<String, Object> properties) {
        this.properties = properties;
    }

}