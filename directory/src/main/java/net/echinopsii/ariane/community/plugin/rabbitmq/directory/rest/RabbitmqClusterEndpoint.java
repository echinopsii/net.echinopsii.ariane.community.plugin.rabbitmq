/**
 * RabbitMQ plugin directory bundle
 * RabbitMQ cluster REST endpoint
 *
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

package net.echinopsii.ariane.community.plugin.rabbitmq.directory.rest;

import net.echinopsii.ariane.community.core.directory.base.model.organisational.Team;
import net.echinopsii.ariane.community.core.directory.base.model.technical.system.OSInstance;
import net.echinopsii.ariane.community.core.directory.wat.json.ToolBox;
import net.echinopsii.ariane.community.core.directory.wat.rest.organisational.TeamEndpoint;
import net.echinopsii.ariane.community.core.directory.wat.rest.technical.system.OSInstanceEndpoint;
import net.echinopsii.ariane.community.plugin.rabbitmq.directory.RabbitmqDirectoryBootstrap;
import net.echinopsii.ariane.community.plugin.rabbitmq.directory.json.RabbitmqClusterJSON;
import net.echinopsii.ariane.community.plugin.rabbitmq.directory.model.RabbitmqCluster;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import java.io.ByteArrayOutputStream;
import java.util.HashSet;

@Path("/directories/middleware/rabbitmq/clusters")
public class RabbitmqClusterEndpoint {
    private static final Logger log = LoggerFactory.getLogger(RabbitmqClusterEndpoint.class);
    private EntityManager em;

    public static Response rabbitmqComponentToJSON(RabbitmqCluster entity) {
        Response ret = null;
        String result;
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        try {
            RabbitmqClusterJSON.oneRabbitmqCluster2JSON(entity, outStream);
            result = ToolBox.getOuputStreamContent(outStream, "UTF-8");
            ret = Response.status(Status.OK).entity(result).build();
        } catch (Exception e) {
            log.error(e.getMessage());
            e.printStackTrace();
            result = e.getMessage();
            ret = Response.status(Status.INTERNAL_SERVER_ERROR).entity(result).build();
        }
        return ret;
    }

    public static RabbitmqCluster findRabbitmqClusterById(EntityManager em, long id) {
        TypedQuery<RabbitmqCluster> findByIdQuery = em.createQuery("SELECT DISTINCT t FROM RabbitmqCluster t WHERE t.id = :entityId ORDER BY t.id", RabbitmqCluster.class);
        findByIdQuery.setParameter("entityId", id);
        RabbitmqCluster entity;
        try {
            entity = findByIdQuery.getSingleResult();
        } catch (NoResultException nre) {
            entity = null;
        }
        return entity;
    }

    public static RabbitmqCluster findRabbitmqClusterByName(EntityManager em, String name) {
        TypedQuery<RabbitmqCluster> findByNameQuery = em.createQuery("SELECT DISTINCT t FROM RabbitmqCluster t WHERE t.name = :entityName ORDER BY t.name", RabbitmqCluster.class);
        findByNameQuery.setParameter("entityName", name);
        RabbitmqCluster entity;
        try {
            entity = findByNameQuery.getSingleResult();
        } catch (NoResultException nre) {
            entity = null;
        }
        return entity;
    }

    @GET
    @Path("/{id:[0-9][0-9]*}")
    public Response displayRabbitmqCluster(@PathParam("id") Long id) {
        Subject subject = SecurityUtils.getSubject();
        log.debug("[{}-{}] get rabbitmq component : {}", new Object[]{Thread.currentThread().getId(), subject.getPrincipal(), id});
        if (subject.hasRole("mdwrabbitadmin") || subject.hasRole("mdwrabbitreviewer") || subject.isPermitted("dirMdwRabbitMQCluster:display") ||
                    subject.hasRole("Jedi") || subject.isPermitted("ccuniverse:zeone"))
        {
            em = RabbitmqDirectoryBootstrap.getDirectoryJPAProvider().createEM();
            RabbitmqCluster entity = findRabbitmqClusterById(em, id);
            if (entity == null) {
                em.close();
                return Response.status(Status.NOT_FOUND).build();
            }

            Response ret = rabbitmqComponentToJSON(entity);
            em.close();
            return ret;
        } else {
            return Response.status(Status.UNAUTHORIZED).entity("You're not authorized to display rabbitmq components. Contact your administrator.").build();
        }
    }

    @GET
    public Response displayAllRabbitmqClusters() {
        Subject subject = SecurityUtils.getSubject();
        log.debug("[{}-{}] get rabbitmq components", new Object[]{Thread.currentThread().getId(), subject.getPrincipal()});
        if (subject.hasRole("mdwrabbitadmin") || subject.hasRole("mdwrabbitreviewer") || subject.isPermitted("dirMdwRabbitMQCluster:display") ||
                    subject.hasRole("Jedi") || subject.isPermitted("ccuniverse:zeone"))
        {
            em = RabbitmqDirectoryBootstrap.getDirectoryJPAProvider().createEM();
            final HashSet<RabbitmqCluster> results = new HashSet(em.createQuery("SELECT DISTINCT t FROM RabbitmqCluster t ORDER BY t.id", RabbitmqCluster.class).getResultList());

            Response ret = null;
            String result;
            ByteArrayOutputStream outStream = new ByteArrayOutputStream();
            try {
                RabbitmqClusterJSON.manyRabbitmqClusters2JSON(results, outStream);
                result = ToolBox.getOuputStreamContent(outStream, "UTF-8");
                ret = Response.status(Status.OK).entity(result).build();
            } catch (Exception e) {
                log.error(e.getMessage());
                e.printStackTrace();
                result = e.getMessage();
                ret = Response.status(Status.INTERNAL_SERVER_ERROR).entity(result).build();
            } finally {
                em.close();
                return ret;
            }
        } else {
            return Response.status(Status.UNAUTHORIZED).entity("You're not authorized to display rabbitmq components. Contact your administrator.").build();
        }
    }

    @GET
    @Path("/get")
    public Response getRabbitmqCluster(@QueryParam("name")String name, @QueryParam("id")long id) {
        if (id!=0) {
            return displayRabbitmqCluster(id);
        } else if (name!=null) {
            Subject subject = SecurityUtils.getSubject();
            log.debug("[{}-{}] get rabbitmq component : {}", new Object[]{Thread.currentThread().getId(), subject.getPrincipal(), name});
            if (subject.hasRole("mdwrabbitadmin") || subject.hasRole("mdwrabbitreviewer") || subject.isPermitted("dirMdwRabbitMQCluster:display") ||
                        subject.hasRole("Jedi") || subject.isPermitted("ccuniverse:zeone"))
            {
                em = RabbitmqDirectoryBootstrap.getDirectoryJPAProvider().createEM();
                RabbitmqCluster entity = findRabbitmqClusterByName(em, name);
                if (entity == null) {
                    em.close();
                    return Response.status(Status.NOT_FOUND).build();
                }

                Response ret = rabbitmqComponentToJSON(entity);
                em.close();
                return ret;

            } else {
                return Response.status(Status.UNAUTHORIZED).entity("You're not authorized to display rabbitmq clusters. Contact your administrator.").build();
            }
        } else {
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Request error: id and name are not defined. You must define one of these parameters.").build();
        }
    }

    @GET
    @Path("/create")
    public Response createRabbitmqCluster(@QueryParam("name")String name, @QueryParam("url")String url, @QueryParam("user")String user, @QueryParam("password")String password,
                                           @QueryParam("osInstance")Long osiID, @QueryParam("supportTeam")Long teamID, @QueryParam("type")String type,
                                           @QueryParam("description")String description) {
        if (name!=null && url!=null && user!=null && osiID!=0 && teamID!=0 && type!=null) {
            Subject subject = SecurityUtils.getSubject();
            log.debug("[{}-{}] create rabbitmq cluster : ({},{},{},{},{},{},{},{})", new Object[]{Thread.currentThread().getId(), subject.getPrincipal(), name,
                                                                                                url, user, password, osiID, teamID, type, description});
            if (subject.hasRole("mdwrabbitadmin") || subject.isPermitted("dirMdwRabbitMQCluster:create") ||
                        subject.hasRole("Jedi") || subject.isPermitted("ccuniverse:zeone"))
            {
                em = RabbitmqDirectoryBootstrap.getDirectoryJPAProvider().createEM();
                RabbitmqCluster entity = findRabbitmqClusterByName(em, name);
                if (entity == null) {
                    OSInstance osInstance = OSInstanceEndpoint.findOSInstanceById(em, osiID);
                    if (osInstance!=null) {
                        Team team = TeamEndpoint.findTeamById(em, teamID);
                        if (team!=null) {
                            entity = new RabbitmqCluster().setNameR(name).setDescriptionR(description);
                            try {
                                em.getTransaction().begin();
                                em.persist(entity);
                                em.getTransaction().commit();
                            } catch (Throwable t) {
                                if(em.getTransaction().isActive())
                                    em.getTransaction().rollback();
                                em.close();
                                return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Throwable raised while creating rabbitmq cluster " + entity.getName() + " : " + t.getMessage()).build();
                            }
                        } else {
                            em.close();
                            return Response.status(Status.NOT_FOUND).entity("Support team " + teamID + " not found.").build();
                        }
                    } else {
                        em.close();
                        return Response.status(Status.NOT_FOUND).entity("OS instance " + osiID + " not found.").build();
                    }
                }

                Response ret = rabbitmqComponentToJSON(entity);
                em.close();
                return ret;
            } else {
                return Response.status(Status.UNAUTHORIZED).entity("You're not authorized to create rabbitmq clusters. Contact your administrator.").build();
            }
        } else {
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Request error: name and/or url and/or user and/or osInstance and/or supportTeam and/or type are not defined." +
                                                                        " You must define these parameters.").build();
        }
    }

    @GET
    @Path("/delete")
    public Response deleteRabbitmqCluster(@QueryParam("id")Long id) {
        if (id!=0) {
            Subject subject = SecurityUtils.getSubject();
            log.debug("[{}-{}] delete rabbitmq cluster : {}", new Object[]{Thread.currentThread().getId(), subject.getPrincipal(), id});
            if (subject.hasRole("mdwrabbitadmin") || subject.isPermitted("dirMdwRabbitMQCluster:delete") ||
                subject.hasRole("Jedi") || subject.isPermitted("ccuniverse:zeone"))
            {
                em = RabbitmqDirectoryBootstrap.getDirectoryJPAProvider().createEM();
                RabbitmqCluster entity = findRabbitmqClusterById(em, id);
                if (entity!=null) {
                    try {
                        em.getTransaction().begin();
                        em.remove(entity);
                        em.getTransaction().commit();
                        em.close();
                        return Response.status(Status.OK).entity("Rabbitmq cluster " + id + " has been successfully deleted").build();
                    } catch (Throwable t) {
                        if(em.getTransaction().isActive())
                            em.getTransaction().rollback();
                        em.close();
                        return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Throwable raised while deleting rabbitmq cluster " + entity.getName() + " : " + t.getMessage()).build();
                    }
                } else {
                    em.close();
                    return Response.status(Status.NOT_FOUND).build();
                }
            } else {
                return Response.status(Status.UNAUTHORIZED).entity("You're not authorized to delete rabbitmq clusters. Contact your administrator.").build();
            }
        } else {
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Request error: id is not defined. You must define this parameter.").build();
        }
    }

    @GET
    @Path("/update/name")
    public Response updateRabbitmqClusterName(@QueryParam("id")Long id, @QueryParam("name")String name) {
        if (id!=0 && name!=null) {
            Subject subject = SecurityUtils.getSubject();
            log.debug("[{}-{}] update rabbitmq cluster {} with name : {}", new Object[]{Thread.currentThread().getId(), subject.getPrincipal(), id, name});
            if (subject.hasRole("mdwrabbitadmin") || subject.isPermitted("dirMdwRabbitMQCluster:update") ||
                        subject.hasRole("Jedi") || subject.isPermitted("ccuniverse:zeone"))
            {
                em = RabbitmqDirectoryBootstrap.getDirectoryJPAProvider().createEM();
                RabbitmqCluster entity = findRabbitmqClusterById(em, id);
                if (entity!=null) {
                    try {
                        em.getTransaction().begin();
                        entity.setName(name);
                        em.getTransaction().commit();
                        em.close();
                        return Response.status(Status.OK).entity("Rabbitmq cluster " + id + " has been successfully updated with name " + name).build();
                    } catch (Throwable t) {
                        if(em.getTransaction().isActive())
                            em.getTransaction().rollback();
                        em.close();
                        return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Throwable raised while deleting rabbitmq cluster " + entity.getName() + " : " + t.getMessage()).build();
                    }
                } else {
                    em.close();
                    return Response.status(Status.NOT_FOUND).build();
                }
            } else {
                return Response.status(Status.UNAUTHORIZED).entity("You're not authorized to delete rabbitmq clusters. Contact your administrator.").build();
            }
        } else {
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Request error: id and/or name are not defined. You must define these parameters.").build();
        }
    }

    @GET
    @Path("/update/description")
    public Response updateRabbitmqClusterDescription(@QueryParam("id")Long id, @QueryParam("description")String description) {
        if (id!=0 && description!=null) {
            Subject subject = SecurityUtils.getSubject();
            log.debug("[{}-{}] update rabbitmq cluster {} with description : {}", new Object[]{Thread.currentThread().getId(), subject.getPrincipal(), id, description});
            if (subject.hasRole("mdwrabbitadmin") || subject.isPermitted("dirMdwRabbitMQCluster:update") ||
                        subject.hasRole("Jedi") || subject.isPermitted("ccuniverse:zeone"))
            {
                em = RabbitmqDirectoryBootstrap.getDirectoryJPAProvider().createEM();
                RabbitmqCluster entity = findRabbitmqClusterById(em, id);
                if (entity!=null) {
                    try {
                        em.getTransaction().begin();
                        entity.setDescription(description);
                        em.getTransaction().commit();
                        em.close();
                        return Response.status(Status.OK).entity("Rabbitmq cluster " + id + " has been successfully updated with description " + description).build();
                    } catch (Throwable t) {
                        if(em.getTransaction().isActive())
                            em.getTransaction().rollback();
                        em.close();
                        return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Throwable raised while deleting rabbitmq cluster " + entity.getName() + " : " + t.getMessage()).build();
                    }
                } else {
                    em.close();
                    return Response.status(Status.NOT_FOUND).build();
                }
            } else {
                return Response.status(Status.UNAUTHORIZED).entity("You're not authorized to delete rabbitmq clusters. Contact your administrator.").build();
            }
        } else {
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Request error: id and/or description are not defined. You must define these parameters.").build();
        }
    }
}