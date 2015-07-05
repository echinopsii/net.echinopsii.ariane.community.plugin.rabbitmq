/**
 * RabbitMQ plugin directory bundle
 * RabbitMQ plugin directory bootstrap
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

package net.echinopsii.ariane.community.plugin.rabbitmq.directory;

import com.fasterxml.jackson.core.JsonFactory;
import net.echinopsii.ariane.community.core.directory.base.model.organisational.Application;
import net.echinopsii.ariane.community.core.directory.base.model.organisational.Company;
import net.echinopsii.ariane.community.core.directory.base.persistence.DirectoryJPAProvider;
import net.echinopsii.ariane.community.core.portal.base.model.MenuEntityType;
import net.echinopsii.ariane.community.core.portal.base.model.TreeMenuEntity;
import net.echinopsii.ariane.community.core.portal.base.plugin.FaceletsResourceResolverService;
import net.echinopsii.ariane.community.core.portal.base.plugin.FacesMBeanRegistry;
import net.echinopsii.ariane.community.core.portal.base.plugin.RestResourceRegistry;
import net.echinopsii.ariane.community.core.portal.base.plugin.TreeMenuRootsRegistry;
import org.apache.felix.ipojo.annotations.*;
import org.osgi.framework.FrameworkUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.net.URL;

@Component
@Provides(properties= {@StaticServiceProperty(name="targetArianeComponent", type="java.lang.String", value="Portal")})
@Instantiate
public class RabbitmqDirectoryBootstrap implements FaceletsResourceResolverService {
    private static final Logger log = LoggerFactory.getLogger(RabbitmqDirectoryBootstrap.class);
    private static final String RBMQ_DIRECTORY_PLUGIN = "Ariane RabbitMQ Plugin Directory";

    private static final String basePath = "/META-INF";
    private static final String FACES_CONFIG_FILE_PATH= basePath + "/faces-config.xml";
    private static final String REST_EP_FILE_PATH = basePath + "/rest.endpoints";

    private static String MAIN_MENU_DIRECTORY_CONTEXT;

    @Requires
    private DirectoryJPAProvider directoryJpaProvider = null;
    private static DirectoryJPAProvider directoryJPAProviderSgt = null;

    @Bind
    public void bindJPAProvider(DirectoryJPAProvider r) {
        log.debug("Bound to directory JPA provider...");
        directoryJpaProvider = r;
        directoryJPAProviderSgt = r;
    }

    @Unbind
    public void unbindJPAProvider() {
        log.debug("Unbound from directory JPA provider...");
        directoryJpaProvider = null;
        directoryJPAProviderSgt = null;
    }

    public static DirectoryJPAProvider getDirectoryJPAProvider() {
        return directoryJPAProviderSgt;
    }

    @Requires(from="ArianePortalFacesMBeanRegistry")
    private FacesMBeanRegistry portalPluginFacesMBeanRegistry = null;

    @Bind(from="ArianePortalFacesMBeanRegistry")
    public void bindDirectoryPluginFacesMBeanRegistry(FacesMBeanRegistry r) {
        log.debug("Bound to portal plugin faces managed bean registry...");
        portalPluginFacesMBeanRegistry = r;
    }

    @Unbind
    public void unbindDirectoryPluginFacesMBeanRegistry() {
        log.debug("Unbound from portal plugin faces managed bean registry...");
        portalPluginFacesMBeanRegistry = null;
    }

    @Requires(from="DirectoryMenuRootsTreeRegistryImpl")
    private TreeMenuRootsRegistry rootDirectoryRegistry;
    private TreeMenuEntity rabbitmqTreeMenuEntity;

    @Bind(from="DirectoryMenuRootsTreeRegistryImpl")
    public void bindRootDirectoryRegistry(TreeMenuRootsRegistry r) {
        log.debug("Bound to directory tree menu roots registry...");
        rootDirectoryRegistry = r;
    }

    @Unbind
    public void unbindRootDirectoryRegistry() {
        log.debug("Unbound from directory tree menu roots registry...");
        rootDirectoryRegistry = null;
    }

    @Requires
    private RestResourceRegistry restResourceRegistry = null;

    @Bind
    public void bindRestResourceRegistry(RestResourceRegistry r) {
        log.debug("Bound to rest resource registry...");
        restResourceRegistry = r;
    }

    @Unbind
    public void unbindRestResourceRegistry() {
        log.debug("Bound to rest resource registry...");
        restResourceRegistry = null;
    }

    @Validate
    public void validate() throws Exception {
        plugDirectoryJPAProvider();
        plugPortalFacesMBeanRegistry();
        plugDirectoryTreeMenuRootRegistry();
        plugRESTEndpoints();
        log.info("{} is started", new Object[]{RBMQ_DIRECTORY_PLUGIN});
    }

    @Invalidate
    public void invalidate() {
        unplugRESTEndpoints();
        unplugDirectoryTreeMenuRootRegistry();
        unplugPortalFacesMBeanRegistry();
        unplugDirectoryJPAProvider();
        log.info("{} is stopped", new Object[]{RBMQ_DIRECTORY_PLUGIN});
    }

    @Override
    public URL resolveURL(String path) {
        log.debug("Resolve {} from rabbitmq directory...", new Object[]{path});
        return RabbitmqDirectoryBootstrap.class.getResource(basePath + path);
    }

    private static JsonFactory jFactory = new JsonFactory();
    public static JsonFactory getjFactory() {
        return jFactory;
    }

    private void plugDirectoryJPAProvider() {
        Company pivotal = null;
        Application rabbitmq = null;

        directoryJpaProvider.addSubPersistenceBundle(FrameworkUtil.getBundle(RabbitmqDirectoryBootstrap.class));

        EntityManager em = directoryJpaProvider.createEM();
        CriteriaBuilder builder = em.getCriteriaBuilder();

        CriteriaQuery<Company> cmpCriteria = builder.createQuery(Company.class);
        Root<Company> cmpRoot = cmpCriteria.from(Company.class);
        cmpCriteria.select(cmpRoot).where(builder.equal(cmpRoot.<String>get("name"), "Pivotal"));
        TypedQuery<Company> cmpQuery = em.createQuery(cmpCriteria);
        try {
            pivotal = cmpQuery.getSingleResult();
            log.debug("Pivotal company already defined ...");
        } catch (NoResultException e) {
            log.debug("Pivotal company will be defined ...");
        } catch (Exception e) {
            throw e;
        }

        CriteriaQuery<Application> appCriteria = builder.createQuery(Application.class);
        Root<Application> appRoot = appCriteria.from(Application.class);
        appCriteria.select(appRoot).where(builder.equal(appRoot.<String>get("name"), "RabbitMQ"));
        TypedQuery<Application> appQuery = em.createQuery(appCriteria);
        try {
            rabbitmq = appQuery.getSingleResult();
            log.debug("RabbitMQ application already defined ...");
        } catch (NoResultException e) {
            log.debug("RabbitMQ application will be defined ...");
        } catch (Exception e) {
            throw e;
        }

        em.getTransaction().begin();

        if (pivotal == null) {
            pivotal  = new Company().setNameR("Pivotal").setDescriptionR("Pivotal");
            em.persist(pivotal);
        }

        if (rabbitmq == null) {
            rabbitmq = new Application().setNameR("RabbitMQ").setCompanyR(pivotal).setShortNameR("RabbitMQ").
                                        setColorCodeR("ff6600").setDescriptionR("Robust messaging for applications");
            em.persist(rabbitmq);
        }

        if (!pivotal.getApplications().contains(rabbitmq)) {
            pivotal.getApplications().add(rabbitmq);
        }

        em.flush();
        em.getTransaction().commit();
    }

    private void unplugDirectoryJPAProvider() {
        //TODO : hibernate plugin unplug
    }

    private void plugPortalFacesMBeanRegistry() {
        portalPluginFacesMBeanRegistry.registerPluginFacesMBeanConfig(RabbitmqDirectoryBootstrap.class.getResource(FACES_CONFIG_FILE_PATH));
    }

    private void unplugPortalFacesMBeanRegistry() {
        try {
            portalPluginFacesMBeanRegistry.unregisterPluginFacesMBeanConfig(RabbitmqDirectoryBootstrap.class.getResource(FACES_CONFIG_FILE_PATH));
        } catch (IllegalAccessException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (ClassNotFoundException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (InstantiationException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    private void plugDirectoryTreeMenuRootRegistry() {
        try {
            MAIN_MENU_DIRECTORY_CONTEXT = portalPluginFacesMBeanRegistry.getRegisteredServletContext().getContextPath()+"/";

            rootDirectoryRegistry.getLinkedMainMenuEntity().getDisplayRoles().add("mdwrabbitadmin");
            rootDirectoryRegistry.getLinkedMainMenuEntity().getDisplayRoles().add("mdwrabbitreviewer");
            rootDirectoryRegistry.getLinkedMainMenuEntity().getDisplayPermissions().add("dirMdwRabbitMQNode:display");
            rootDirectoryRegistry.getLinkedMainMenuEntity().getDisplayPermissions().add("dirMdwRabbitMQCluster:display");

            TreeMenuEntity middlewareRootTreeMenuEntity = rootDirectoryRegistry.getTreeMenuEntityFromValue("Middleware");
            if (middlewareRootTreeMenuEntity == null) {
                middlewareRootTreeMenuEntity = new TreeMenuEntity().setId("mdwDir").setValue("Middleware").setType(MenuEntityType.TYPE_MENU_SUBMENU);
                rootDirectoryRegistry.registerTreeMenuRootEntity(middlewareRootTreeMenuEntity);
            }
            middlewareRootTreeMenuEntity.addDisplayRole("mdwrabbitadmin").addDisplayRole("mdwrabbitreviewer").
                                         addDisplayPermission("dirMdwRabbitMQNode:display").addDisplayPermission("dirMdwRabbitMQCluster:display");

            rabbitmqTreeMenuEntity = new TreeMenuEntity().setId("rabbitMQDir").setValue("RabbitMQ").
                                                         setType(MenuEntityType.TYPE_MENU_SUBMENU).setParentTreeMenuEntity(middlewareRootTreeMenuEntity).
                                                         addDisplayRole("mdwrabbitmqadmin").addDisplayRole("mdwrabbitreviewer").
                                                         addDisplayPermission("dirMdwRabbitMQNode:display").addDisplayPermission("dirMdwRabbitMQCluster:display");
            middlewareRootTreeMenuEntity.addChildTreeMenuEntity(rabbitmqTreeMenuEntity);
            rabbitmqTreeMenuEntity.addChildTreeMenuEntity(new TreeMenuEntity().setId("rabbitMQNodeTreeID").setValue("RabbitMQ Node").
                                                                               setParentTreeMenuEntity(rabbitmqTreeMenuEntity).setIcon("icon-rabbitmq-node").
                                                                               setType(MenuEntityType.TYPE_MENU_ITEM).setContextAddress(MAIN_MENU_DIRECTORY_CONTEXT + "views/directories/rabbitmqNode.jsf").
                                                                               setDescription("Your RabbitMQ Node definitions").
                                                                               addDisplayRole("mdwrabbitadmin").addDisplayRole("mdwrabbitreviewer").
                                                                               addDisplayPermission("dirMdwRabbitMQNode:display"));
            rabbitmqTreeMenuEntity.addChildTreeMenuEntity(new TreeMenuEntity().setId("rabbitMQClusterTreeID").setValue("RabbitMQ Cluster").
                                                                               setParentTreeMenuEntity(rabbitmqTreeMenuEntity).setIcon("icon-rabbitmq-cluster").
                                                                               setType(MenuEntityType.TYPE_MENU_ITEM).setContextAddress(MAIN_MENU_DIRECTORY_CONTEXT + "views/directories/rabbitmqCluster.jsf").
                                                                               setDescription("Your RabbitMQ Cluster definitions").
                                                                               addDisplayRole("mdwrabbitadmin").addDisplayRole("mdwrabbitreviewer").
                                                                               addDisplayPermission("dirMdwRabbitMQCluster:display"));
        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    private void unplugDirectoryTreeMenuRootRegistry() {
        try {
            TreeMenuEntity middlewareRootTreeMenuEntity = rootDirectoryRegistry.getTreeMenuEntityFromValue("Middleware");
            middlewareRootTreeMenuEntity.getChildTreeMenuEntities().remove(rabbitmqTreeMenuEntity);
            if (middlewareRootTreeMenuEntity.getChildTreeMenuEntities().size()==0)
                rootDirectoryRegistry.unregisterTreeMenuRootEntity(middlewareRootTreeMenuEntity);
            else
                middlewareRootTreeMenuEntity.removeDisplayRole("mdwrabbitadmin").
                                             removeDisplayRole("mdwrabbitreviewer").
                                             removeDisplayPermission("dirMdwRabbitMQCluster:display").
                                             removeDisplayPermission("dirMdwRabbitMQNode:display");

            rootDirectoryRegistry.getLinkedMainMenuEntity().getDisplayRoles().remove("mdwrabbitadmin");
            rootDirectoryRegistry.getLinkedMainMenuEntity().getDisplayRoles().remove("mdwrabbitreviewer");
            rootDirectoryRegistry.getLinkedMainMenuEntity().getDisplayPermissions().remove("dirMdwRabbitMQCluster:display");
            rootDirectoryRegistry.getLinkedMainMenuEntity().getDisplayPermissions().remove("dirMdwRabbitMQNode:display");
        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    private void plugRESTEndpoints() {
        restResourceRegistry.registerPluginRestEndpoints(RabbitmqDirectoryBootstrap.class.getResource(REST_EP_FILE_PATH));
    }

    private void unplugRESTEndpoints() {
        restResourceRegistry.unregisterPluginRestEndpoints(RabbitmqDirectoryBootstrap.class.getResource(REST_EP_FILE_PATH));
    }

}