/**
 * RabbitMQ plugin directory bundle
 * RabbitMQ Node REST endpoint
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
import net.echinopsii.ariane.community.plugin.rabbitmq.directory.json.RabbitmqNodeJSON;
import net.echinopsii.ariane.community.plugin.rabbitmq.directory.model.RabbitmqNode;
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

@Path("/directories/middleware/rabbitmq/nodes")
public class RabbitmqNodeEndpoint {
    private static final Logger log = LoggerFactory.getLogger(RabbitmqNodeEndpoint.class);
    private EntityManager em;

    public static Response rabbitmqNodeToJSON(RabbitmqNode entity) {
        Response ret = null;
        String result;
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        try {
            RabbitmqNodeJSON.oneRabbitmqNode2JSON(entity, outStream);
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

    public static RabbitmqNode findRabbitmqNodeById(EntityManager em, long id) {
        TypedQuery<RabbitmqNode> findByIdQuery = em.createQuery("SELECT DISTINCT t FROM RabbitmqNode t LEFT JOIN FETCH t.osInstance LEFT JOIN FETCH t.supportTeam WHERE t.id = :entityId ORDER BY t.id", RabbitmqNode.class);
        findByIdQuery.setParameter("entityId", id);
        RabbitmqNode entity;
        try {
            entity = findByIdQuery.getSingleResult();
        } catch (NoResultException nre) {
            entity = null;
        }
        return entity;
    }

    public static RabbitmqNode findRabbitmqNodeByName(EntityManager em, String name) {
        TypedQuery<RabbitmqNode> findByNameQuery = em.createQuery("SELECT DISTINCT t FROM RabbitmqNode t LEFT JOIN FETCH t.osInstance LEFT JOIN FETCH t.supportTeam WHERE t.name = :entityName ORDER BY t.name", RabbitmqNode.class);
        findByNameQuery.setParameter("entityName", name);
        RabbitmqNode entity;
        try {
            entity = findByNameQuery.getSingleResult();
        } catch (NoResultException nre) {
            entity = null;
        }
        return entity;
    }

    @GET
    @Path("/{id:[0-9][0-9]*}")
    public Response displayRabbitmqNode(@PathParam("id") Long id) {
        Subject subject = SecurityUtils.getSubject();
        log.debug("[{}-{}] get rabbitmq component : {}", new Object[]{Thread.currentThread().getId(), subject.getPrincipal(), id});
        if (subject.hasRole("mdwrabbitadmin") || subject.hasRole("mdwrabbitreviewer") || subject.isPermitted("dirMdwRabbitMQNode:display") ||
                    subject.hasRole("Jedi") || subject.isPermitted("ccuniverse:zeone"))
        {
            em = RabbitmqDirectoryBootstrap.getDirectoryJPAProvider().createEM();
            RabbitmqNode entity = findRabbitmqNodeById(em, id);
            if (entity == null) {
                em.close();
                return Response.status(Status.NOT_FOUND).build();
            }

            Response ret = rabbitmqNodeToJSON(entity);
            em.close();
            return ret;
        } else {
            return Response.status(Status.UNAUTHORIZED).entity("You're not authorized to display rabbitmq components. Contact your administrator.").build();
        }
    }

    @GET
    public Response displayAllRabbitmqNodes() {
        Subject subject = SecurityUtils.getSubject();
        log.debug("[{}-{}] get rabbitmq nodes", new Object[]{Thread.currentThread().getId(), subject.getPrincipal()});
        if (subject.hasRole("mdwrabbitadmin") || subject.hasRole("mdwrabbitreviewer") || subject.isPermitted("dirMdwRabbitMQNode:display") ||
                    subject.hasRole("Jedi") || subject.isPermitted("ccuniverse:zeone"))
        {
            em = RabbitmqDirectoryBootstrap.getDirectoryJPAProvider().createEM();
            final HashSet<RabbitmqNode> results = new HashSet(em.createQuery("SELECT DISTINCT t FROM RabbitmqNode t LEFT JOIN FETCH t.osInstance LEFT JOIN FETCH t.supportTeam ORDER BY t.id", RabbitmqNode.class).getResultList());

            Response ret = null;
            String result;
            ByteArrayOutputStream outStream = new ByteArrayOutputStream();
            try {
                RabbitmqNodeJSON.manyRabbitmqNodes2JSON(results, outStream);
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
            return Response.status(Status.UNAUTHORIZED).entity("You're not authorized to display rabbitmq nodes. Contact your administrator.").build();
        }
    }

    @GET
    @Path("/get")
    public Response getRabbitmqNode(@QueryParam("name")String name, @QueryParam("id")long id) {
        if (id!=0) {
            return displayRabbitmqNode(id);
        } else if (name!=null) {
            Subject subject = SecurityUtils.getSubject();
            log.debug("[{}-{}] get rabbitmq node : {}", new Object[]{Thread.currentThread().getId(), subject.getPrincipal(), name});
            if (subject.hasRole("mdwrabbitadmin") || subject.hasRole("mdwrabbitreviewer") || subject.isPermitted("dirMdwRabbitMQNode:display") ||
                        subject.hasRole("Jedi") || subject.isPermitted("ccuniverse:zeone"))
            {
                em = RabbitmqDirectoryBootstrap.getDirectoryJPAProvider().createEM();
                RabbitmqNode entity = findRabbitmqNodeByName(em, name);
                if (entity == null) {
                    em.close();
                    return Response.status(Status.NOT_FOUND).build();
                }

                Response ret = rabbitmqNodeToJSON(entity);
                em.close();
                return ret;

            } else {
                return Response.status(Status.UNAUTHORIZED).entity("You're not authorized to display rabbitmq nodes. Contact your administrator.").build();
            }
        } else {
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Request error: id and name are not defined. You must define one of these parameters.").build();
        }
    }

    @GET
    @Path("/create")
    public Response createRabbitmqNode(@QueryParam("name")String name, @QueryParam("url")String url, @QueryParam("user")String user, @QueryParam("password")String password,
                                           @QueryParam("osInstance")Long osiID, @QueryParam("supportTeam")Long teamID, @QueryParam("type")String type,
                                           @QueryParam("description")String description) {
        if (name!=null && url!=null && user!=null && osiID!=0 && teamID!=0 && type!=null) {
            Subject subject = SecurityUtils.getSubject();
            log.debug("[{}-{}] create rabbitmq node : ({},{},{},{},{},{},{},{})", new Object[]{Thread.currentThread().getId(), subject.getPrincipal(), name,
                                                                                                url, user, password, osiID, teamID, type, description});
            if (subject.hasRole("mdwrabbitadmin") || subject.isPermitted("dirMdwRabbitMQNode:create") ||
                        subject.hasRole("Jedi") || subject.isPermitted("ccuniverse:zeone"))
            {
                em = RabbitmqDirectoryBootstrap.getDirectoryJPAProvider().createEM();
                RabbitmqNode entity = findRabbitmqNodeByName(em, name);
                if (entity == null) {
                    OSInstance osInstance = OSInstanceEndpoint.findOSInstanceById(em, osiID);
                    if (osInstance!=null) {
                        Team team = TeamEndpoint.findTeamById(em, teamID);
                        if (team!=null) {
                            entity = new RabbitmqNode().setNameR(name).setUrlR(url).setUserR(user).setPasswdR(password).setOsInstanceR(osInstance).
                                                             setSupportTeamR(team).setDescriptionR(description);
                            try {
                                em.getTransaction().begin();
                                em.persist(entity);
                                em.getTransaction().commit();
                            } catch (Throwable t) {
                                if(em.getTransaction().isActive())
                                    em.getTransaction().rollback();
                                em.close();
                                return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Throwable raised while creating rabbitmq node " + entity.getName() + " : " + t.getMessage()).build();
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

                Response ret = rabbitmqNodeToJSON(entity);
                em.close();
                return ret;
            } else {
                return Response.status(Status.UNAUTHORIZED).entity("You're not authorized to create rabbitmq nodes. Contact your administrator.").build();
            }
        } else {
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Request error: name and/or url and/or user and/or osInstance and/or supportTeam and/or type are not defined." +
                                                                        " You must define these parameters.").build();
        }
    }

    @GET
    @Path("/delete")
    public Response deleteRabbitmqNode(@QueryParam("id")Long id) {
        if (id!=0) {
            Subject subject = SecurityUtils.getSubject();
            log.debug("[{}-{}] delete rabbitmq node : {}", new Object[]{Thread.currentThread().getId(), subject.getPrincipal(), id});
            if (subject.hasRole("mdwrabbitadmin") || subject.isPermitted("dirMdwRabbitMQNode:delete") ||
                subject.hasRole("Jedi") || subject.isPermitted("ccuniverse:zeone"))
            {
                em = RabbitmqDirectoryBootstrap.getDirectoryJPAProvider().createEM();
                RabbitmqNode entity = findRabbitmqNodeById(em, id);
                if (entity!=null) {
                    try {
                        em.getTransaction().begin();
                        em.remove(entity);
                        em.getTransaction().commit();
                        em.close();
                        return Response.status(Status.OK).entity("Rabbitmq node " + id + " has been successfully deleted").build();
                    } catch (Throwable t) {
                        if(em.getTransaction().isActive())
                            em.getTransaction().rollback();
                        em.close();
                        return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Throwable raised while deleting rabbitmq node " + entity.getName() + " : " + t.getMessage()).build();
                    }
                } else {
                    em.close();
                    return Response.status(Status.NOT_FOUND).build();
                }
            } else {
                return Response.status(Status.UNAUTHORIZED).entity("You're not authorized to delete rabbitmq nodes. Contact your administrator.").build();
            }
        } else {
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Request error: id is not defined. You must define this parameter.").build();
        }
    }

    @GET
    @Path("/update/name")
    public Response updateRabbitmqNodeName(@QueryParam("id")Long id, @QueryParam("name")String name) {
        if (id!=0 && name!=null) {
            Subject subject = SecurityUtils.getSubject();
            log.debug("[{}-{}] update rabbitmq node {} with name : {}", new Object[]{Thread.currentThread().getId(), subject.getPrincipal(), id, name});
            if (subject.hasRole("mdwrabbitadmin") || subject.isPermitted("dirMdwRabbitMQNode:update") ||
                        subject.hasRole("Jedi") || subject.isPermitted("ccuniverse:zeone"))
            {
                em = RabbitmqDirectoryBootstrap.getDirectoryJPAProvider().createEM();
                RabbitmqNode entity = findRabbitmqNodeById(em, id);
                if (entity!=null) {
                    try {
                        em.getTransaction().begin();
                        entity.setName(name);
                        em.getTransaction().commit();
                        em.close();
                        return Response.status(Status.OK).entity("Rabbitmq node " + id + " has been successfully updated with name " + name).build();
                    } catch (Throwable t) {
                        if(em.getTransaction().isActive())
                            em.getTransaction().rollback();
                        em.close();
                        return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Throwable raised while deleting rabbitmq node " + entity.getName() + " : " + t.getMessage()).build();
                    }
                } else {
                    em.close();
                    return Response.status(Status.NOT_FOUND).build();
                }
            } else {
                return Response.status(Status.UNAUTHORIZED).entity("You're not authorized to delete rabbitmq nodes. Contact your administrator.").build();
            }
        } else {
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Request error: id and/or name are not defined. You must define these parameters.").build();
        }
    }

    @GET
    @Path("/update/url")
    public Response updateRabbitmqNodeURL(@QueryParam("id")Long id, @QueryParam("url")String url) {
        if (id!=0 && url!=null) {
            Subject subject = SecurityUtils.getSubject();
            log.debug("[{}-{}] update rabbitmq node {} with url : {}", new Object[]{Thread.currentThread().getId(), subject.getPrincipal(), id, url});
            if (subject.hasRole("mdwrabbitadmin") || subject.isPermitted("dirMdwRabbitMQNode:update") ||
                        subject.hasRole("Jedi") || subject.isPermitted("ccuniverse:zeone"))
            {
                em = RabbitmqDirectoryBootstrap.getDirectoryJPAProvider().createEM();
                RabbitmqNode entity = findRabbitmqNodeById(em, id);
                if (entity!=null) {
                    try {
                        em.getTransaction().begin();
                        entity.setUrl(url);
                        em.getTransaction().commit();
                        em.close();
                        return Response.status(Status.OK).entity("Rabbitmq node " + id + " has been successfully updated with url " + url).build();
                    } catch (Throwable t) {
                        if(em.getTransaction().isActive())
                            em.getTransaction().rollback();
                        em.close();
                        return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Throwable raised while deleting rabbitmq node " + entity.getName() + " : " + t.getMessage()).build();
                    }
                } else {
                    em.close();
                    return Response.status(Status.NOT_FOUND).build();
                }
            } else {
                return Response.status(Status.UNAUTHORIZED).entity("You're not authorized to delete rabbitmq nodes. Contact your administrator.").build();
            }
        } else {
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Request error: id and/or url are not defined. You must define these parameters.").build();
        }
    }

    @GET
    @Path("/update/user")
    public Response updateRabbitmqNodeUser(@QueryParam("id")Long id, @QueryParam("user")String user) {
        if (id!=0 && user!=null) {
            Subject subject = SecurityUtils.getSubject();
            log.debug("[{}-{}] update rabbitmq node {} with user : {}", new Object[]{Thread.currentThread().getId(), subject.getPrincipal(), id, user});
            if (subject.hasRole("mdwrabbitadmin") || subject.isPermitted("dirMdwRabbitMQNode:update") ||
                        subject.hasRole("Jedi") || subject.isPermitted("ccuniverse:zeone"))
            {
                em = RabbitmqDirectoryBootstrap.getDirectoryJPAProvider().createEM();
                RabbitmqNode entity = findRabbitmqNodeById(em, id);
                if (entity!=null) {
                    try {
                        em.getTransaction().begin();
                        entity.setUser(user);
                        em.getTransaction().commit();
                        em.close();
                        return Response.status(Status.OK).entity("Rabbitmq node " + id + " has been successfully updated with user " + user).build();
                    } catch (Throwable t) {
                        if(em.getTransaction().isActive())
                            em.getTransaction().rollback();
                        em.close();
                        return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Throwable raised while deleting rabbitmq node " + entity.getName() + " : " + t.getMessage()).build();
                    }
                } else {
                    em.close();
                    return Response.status(Status.NOT_FOUND).build();
                }
            } else {
                return Response.status(Status.UNAUTHORIZED).entity("You're not authorized to delete rabbitmq nodes. Contact your administrator.").build();
            }
        } else {
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Request error: id and/or user are not defined. You must define these parameters.").build();
        }
    }

    @GET
    @Path("/update/password")
    public Response updateRabbitmqNodePassword(@QueryParam("id")Long id, @QueryParam("password")String password) {
        if (id!=0) {
            Subject subject = SecurityUtils.getSubject();
            log.debug("[{}-{}] update rabbitmq node {} with password : *****", new Object[]{Thread.currentThread().getId(), subject.getPrincipal(), id});
            if (subject.hasRole("mdwrabbitadmin") || subject.isPermitted("dirMdwRabbitMQNode:update") ||
                        subject.hasRole("Jedi") || subject.isPermitted("ccuniverse:zeone"))
            {
                em = RabbitmqDirectoryBootstrap.getDirectoryJPAProvider().createEM();
                RabbitmqNode entity = findRabbitmqNodeById(em, id);
                if (entity!=null) {
                    try {
                        em.getTransaction().begin();
                        entity.setPasswd(password);
                        em.getTransaction().commit();
                        em.close();
                        return Response.status(Status.OK).entity("Rabbitmq node " + id + " has been successfully updated with password " + password).build();
                    } catch (Throwable t) {
                        if(em.getTransaction().isActive())
                            em.getTransaction().rollback();
                        em.close();
                        return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Throwable raised while deleting rabbitmq node " + entity.getName() + " : " + t.getMessage()).build();
                    }
                } else {
                    em.close();
                    return Response.status(Status.NOT_FOUND).build();
                }
            } else {
                return Response.status(Status.UNAUTHORIZED).entity("You're not authorized to delete rabbitmq nodes. Contact your administrator.").build();
            }
        } else {
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Request error: id is not defined. You must define this parameter.").build();
        }
    }

    @GET
    @Path("/update/osInstance")
    public Response udpateRabbitmqNodeOSInstance(@QueryParam("id")Long id, @QueryParam("osInstance")Long osiID) {
        if (id!=0 && osiID!=0) {
            Subject subject = SecurityUtils.getSubject();
            log.debug("[{}-{}] update rabbitmq node {} with os instance : {}", new Object[]{Thread.currentThread().getId(), subject.getPrincipal(), id, osiID});
            if (subject.hasRole("mdwrabbitadmin") || subject.isPermitted("dirMdwRabbitMQNode:update") ||
                        subject.hasRole("Jedi") || subject.isPermitted("ccuniverse:zeone"))
            {
                em = RabbitmqDirectoryBootstrap.getDirectoryJPAProvider().createEM();
                RabbitmqNode entity = findRabbitmqNodeById(em, id);
                if (entity!=null) {
                    OSInstance osInstance = OSInstanceEndpoint.findOSInstanceById(em, osiID);
                    if (osInstance!=null) {
                        try {
                            em.getTransaction().begin();
                            entity.setOsInstance(osInstance);
                            em.getTransaction().commit();
                            em.close();
                            return Response.status(Status.OK).entity("Rabbitmq node " + id + " has been successfully updated with os instance " + osiID).build();
                        } catch (Throwable t) {
                            if(em.getTransaction().isActive())
                                em.getTransaction().rollback();
                            em.close();
                            return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Throwable raised while deleting rabbitmq node " + entity.getName() + " : " + t.getMessage()).build();
                        }
                    } else {
                        em.close();
                        return Response.status(Status.NOT_FOUND).entity("OS instance " + osiID + " not found.").build();
                    }
                } else {
                    em.close();
                    return Response.status(Status.NOT_FOUND).entity("Rabbitmq node " + id + " not found.").build();
                }
            } else {
                return Response.status(Status.UNAUTHORIZED).entity("You're not authorized to delete rabbitmq nodes. Contact your administrator.").build();
            }
        } else {
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Request error: id and/or osInstance are not defined. You must define these parameters.").build();
        }
    }

    @GET
    @Path("/update/supportTeam")
    public Response updateRabbitmqNodeSupportTeam(@QueryParam("id")Long id, @QueryParam("supportTeam")Long teamID) {
        if (id!=0 && teamID!=0) {
            Subject subject = SecurityUtils.getSubject();
            log.debug("[{}-{}] update rabbitmq node {} with team : {}", new Object[]{Thread.currentThread().getId(), subject.getPrincipal(), id, teamID});
            if (subject.hasRole("mdwrabbitadmin") || subject.isPermitted("dirMdwRabbitMQNode:update") ||
                        subject.hasRole("Jedi") || subject.isPermitted("ccuniverse:zeone"))
            {
                em = RabbitmqDirectoryBootstrap.getDirectoryJPAProvider().createEM();
                RabbitmqNode entity = findRabbitmqNodeById(em, id);
                if (entity!=null) {
                    Team team = TeamEndpoint.findTeamById(em, teamID);
                    if (team!=null) {
                        try {
                            em.getTransaction().begin();
                            entity.setSupportTeam(team);
                            em.getTransaction().commit();
                            em.close();
                            return Response.status(Status.OK).entity("Rabbitmq node " + id + " has been successfully updated with support team " + teamID).build();
                        } catch (Throwable t) {
                            if(em.getTransaction().isActive())
                                em.getTransaction().rollback();
                            em.close();
                            return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Throwable raised while deleting rabbitmq node " + entity.getName() + " : " + t.getMessage()).build();
                        }
                    } else {
                        em.close();
                        return Response.status(Status.NOT_FOUND).entity("Team " + teamID + " not found.").build();
                    }
                } else {
                    em.close();
                    return Response.status(Status.NOT_FOUND).entity("Rabbitmq node " + id + " not found.").build();
                }
            } else {
                return Response.status(Status.UNAUTHORIZED).entity("You're not authorized to delete rabbitmq nodes. Contact your administrator.").build();
            }
        } else {
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Request error: id and/or supportTeam are not defined. You must define these parameters.").build();
        }
    }

    @GET
    @Path("/update/description")
    public Response updateRabbitmqNodeDescription(@QueryParam("id")Long id, @QueryParam("description")String description) {
        if (id!=0 && description!=null) {
            Subject subject = SecurityUtils.getSubject();
            log.debug("[{}-{}] update rabbitmq node {} with description : {}", new Object[]{Thread.currentThread().getId(), subject.getPrincipal(), id, description});
            if (subject.hasRole("mdwrabbitadmin") || subject.isPermitted("dirMdwRabbitMQNode:update") ||
                        subject.hasRole("Jedi") || subject.isPermitted("ccuniverse:zeone"))
            {
                em = RabbitmqDirectoryBootstrap.getDirectoryJPAProvider().createEM();
                RabbitmqNode entity = findRabbitmqNodeById(em, id);
                if (entity!=null) {
                    try {
                        em.getTransaction().begin();
                        entity.setDescription(description);
                        em.getTransaction().commit();
                        em.close();
                        return Response.status(Status.OK).entity("Rabbitmq node " + id + " has been successfully updated with description " + description).build();
                    } catch (Throwable t) {
                        if(em.getTransaction().isActive())
                            em.getTransaction().rollback();
                        em.close();
                        return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Throwable raised while deleting rabbitmq node " + entity.getName() + " : " + t.getMessage()).build();
                    }
                } else {
                    em.close();
                    return Response.status(Status.NOT_FOUND).build();
                }
            } else {
                return Response.status(Status.UNAUTHORIZED).entity("You're not authorized to delete rabbitmq nodes. Contact your administrator.").build();
            }
        } else {
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Request error: id and/or description are not defined. You must define these parameters.").build();
        }
    }
}