/**
 * [DEFINE YOUR PROJECT NAME/MODULE HERE]
 * [DEFINE YOUR PROJECT DESCRIPTION HERE] 
 * Copyright (C) 08/12/14 echinopsii
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

package net.echinopsii.ariane.community.plugin.rabbitmq.injector;

import net.echinopsii.ariane.community.core.injector.base.registry.InjectorComponentsRegistry;
import net.echinopsii.ariane.community.core.injector.base.registry.InjectorGearsRegistry;
import net.echinopsii.ariane.community.core.mapping.ds.service.MappingSce;
import net.echinopsii.ariane.community.core.portal.base.model.MenuEntityType;
import net.echinopsii.ariane.community.core.portal.base.model.TreeMenuEntity;
import net.echinopsii.ariane.community.core.portal.base.plugin.FaceletsResourceResolverService;
import net.echinopsii.ariane.community.core.portal.base.plugin.FacesMBeanRegistry;
import net.echinopsii.ariane.community.core.portal.base.plugin.TreeMenuRootsRegistry;
import net.echinopsii.ariane.community.plugin.rabbitmq.directory.RabbitmqDirectoryService;
import net.echinopsii.ariane.community.plugin.rabbitmq.injector.runtime.config.RabbitmqInjectorMainCfgLoader;
import net.echinopsii.ariane.community.plugin.rabbitmq.injector.runtime.gears.DirectoryGear;
import net.echinopsii.ariane.community.plugin.rabbitmq.injector.runtime.gears.MappingGear;
import org.apache.felix.ipojo.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.util.Dictionary;

@Component(managedservice="net.echinopsii.ariane.community.plugin.rabbitmq.RabbitMQInjectorManagedService")
@Provides(properties= {@StaticServiceProperty(name="targetArianeComponent", type="java.lang.String", value="Portal")})
@Instantiate
public class RabbitmqInjectorBootstrap implements FaceletsResourceResolverService {

    private static final Logger log = LoggerFactory.getLogger(RabbitmqInjectorBootstrap.class);
    private static final String RABBITMQ_INJECTOR_SERVICE_NAME = "Ariane RabbitMQ Plugin Injector Component";

    private static Dictionary<Object, Object> config = null;

    /*
     * directory gear singleton
     */
    private static DirectoryGear directoryAkkaGear = null;

    public static DirectoryGear getDirectoryAkkaGear() {
        return directoryAkkaGear;
    }

    /*
     * mapping gear singleton
     */
    private static MappingGear mappingAkkaGear;

    public static MappingGear getMappingAkkaGear() {
        return mappingAkkaGear;
    }


    private static boolean isStarted = false;

    @Requires
    private MappingSce mappingService = null;
    private static MappingSce mappingSceSgt  = null;

    @Bind
    public void bindMappingService(MappingSce t) {
        log.debug("Bound to Mapping Service...");
        mappingService = t;
        mappingSceSgt = t;
    }

    @Unbind
    public void unbindMappingService() {
        log.debug("Unbound from Mapping Service...");
        mappingService = null;
        mappingSceSgt = null;
    }

    public static MappingSce getMappingSce() {
        return mappingSceSgt;
    }

    @Requires
    private RabbitmqDirectoryService rabbitmqDirService = null;
    private static RabbitmqDirectoryService rabbitmqDirSceSgt = null;

    @Bind
    public void bindRabbitmqDirectoryService(RabbitmqDirectoryService d) {
        log.debug("Bound to RabbitMQ Directory Service...");
        rabbitmqDirService = d;
        rabbitmqDirSceSgt = d;
    }

    @Unbind
    public void unbindRabbitmqDirectoryService() {
        log.debug("Unbound from RabbitMQ Directory Service...");
        rabbitmqDirService = null;
        rabbitmqDirSceSgt = null;
    }

    public static synchronized RabbitmqDirectoryService getRabbitmqDirectorySce() {
        return rabbitmqDirSceSgt;
    }

    @Requires(from="ArianePortalFacesMBeanRegistry")
    private FacesMBeanRegistry portalPluginFacesMBeanRegistry = null;

    @Bind
    public void bindPortalPluginFacesMBeanRegistry(FacesMBeanRegistry r) {
        log.debug("Bound to portal plugin faces managed bean registry...");
        portalPluginFacesMBeanRegistry = r;
    }

    @Unbind
    public void unbindPortalPluginFacesMBeanRegistry() {
        log.debug("Unbound from portal plugin faces managed bean registry...");
        portalPluginFacesMBeanRegistry = null;
    }

    @Requires(from="InjectorTreeMenuRootsRegistryImpl")
    private TreeMenuRootsRegistry rootInjectorRegistry;

    @Bind
    public void bindRootInjectorRegistry(TreeMenuRootsRegistry r) {
        log.debug("Bound to injector tree menu root registry...");
        rootInjectorRegistry = r;
    }

    @Unbind
    public void unbindRootInjectorRegistry() {
        log.debug("Unbound from injector tree menu root registry...");
        rootInjectorRegistry = null;
    }

    @Requires
    private InjectorComponentsRegistry componentsRegistry;
    private static InjectorComponentsRegistry sgtComponentsRegistry;

    @Bind
    public void bindComponentsRegistry(InjectorComponentsRegistry registry) {
        log.debug("Bound to injector components registry...");
        componentsRegistry = registry;
        sgtComponentsRegistry = registry;
    }

    @Unbind
    public void unbindComponentsRegistry() {
        log.debug("Unbound from injector components registry...");
        componentsRegistry = null;
        sgtComponentsRegistry = null;
    }

    public static InjectorComponentsRegistry getComponentsRegistry() {
        return sgtComponentsRegistry;
    }

    @Requires
    private InjectorGearsRegistry gearsRegistry;
    private static InjectorGearsRegistry sgtGearsRegistry;

    @Bind
    public void bindGearsRegistry(InjectorGearsRegistry registry) {
        log.debug("Bound to injector gears registry...");
        gearsRegistry = registry;
        sgtGearsRegistry = registry;
    }

    @Unbind
    public void unbindGearsRegistry() {
        log.debug("Unbound from injector gears registry...");
        gearsRegistry = null;
        sgtGearsRegistry = null;
    }

    public static InjectorGearsRegistry getGearsRegisry() {
        return sgtGearsRegistry;
    }

    private final static void start() throws IOException, InterruptedException {
        if (!isStarted) {
            while(config==null) {
                log.info("Config is missing to load RabbitMQ Injector. Sleep some times...");
                Thread.sleep(1000);
            }

            if (sgtGearsRegistry.keySetFromPrefix(INJ_TREE_ROOT_PATH).size()!=0) {
                for (String key: sgtGearsRegistry.keySetFromPrefix(INJ_TREE_ROOT_PATH))
                    sgtGearsRegistry.getEntityFromCache(key).start();
            } else {
                //mappingSimpleGear = new MappingSimpleGear();
                //mappingSimpleGear.start();
                mappingAkkaGear = new MappingGear();
                mappingAkkaGear.start();
                sgtGearsRegistry.putEntityToCache(mappingAkkaGear);

                //directorySimpleGear = new DirectorySimpleGear();
                //directorySimpleGear.start();
                directoryAkkaGear = new DirectoryGear(RabbitmqInjectorMainCfgLoader.getDirQI(), RabbitmqInjectorMainCfgLoader.getRbCompQI());
                directoryAkkaGear.start();
                sgtGearsRegistry.putEntityToCache(directoryAkkaGear);
            }

            isStarted=true;
        }
    }

    @Validate
    public void validate() throws IOException, InterruptedException {
        start();
        portalFacesMBeanRegistryPlug();
        rootInjectorRegistryPlug();
        log.info("{} started",new Object[]{RABBITMQ_INJECTOR_SERVICE_NAME});
    }

    private final static void stop() throws InterruptedException {
        if (isStarted) {
            for (String key: sgtGearsRegistry.keySetFromPrefix(INJ_TREE_ROOT_PATH))
                sgtGearsRegistry.getEntityFromCache(key).stop();

            isStarted = false;
        }
    }

    @Invalidate
    public void invalidate() throws InterruptedException {
        portalFacesMBeanRegistryUnplug();
        rootInjectorRegistryUnplug();
        stop();
        log.info("{} is stopped", new Object[]{RABBITMQ_INJECTOR_SERVICE_NAME});
    }

    @Updated
    public static void updated(Dictionary properties) {
        log.debug("{} is being updated by {}", new Object[]{RABBITMQ_INJECTOR_SERVICE_NAME, Thread.currentThread().toString()});
        if (RabbitmqInjectorMainCfgLoader.isValid(properties)) {
            config=properties;
            if (isStarted) {
                final Runnable applyConfigUpdate = new Runnable() {
                    @Override
                    public void run() {
                        log.debug("{} will be restart to apply configuration changes...",RABBITMQ_INJECTOR_SERVICE_NAME);
                        try {
                            stop();
                            start();
                        } catch (Exception e) {
                            log.error("{} restart failed !", RABBITMQ_INJECTOR_SERVICE_NAME);
                            e.printStackTrace();
                        }
                    }
                };
                new Thread(applyConfigUpdate).start();
            }
        }
    }

    private static final String basePath = "/META-INF";

    @Override
    public URL resolveURL(String path) {
        log.debug("Resolve {} from rabbitmq injector...", new Object[]{path});
        return RabbitmqInjectorBootstrap.class.getResource(basePath + path);
    }

    private static String FACES_CONFIG_FILE_PATH="/META-INF/faces-config.xml";

    private void portalFacesMBeanRegistryPlug() {
        portalPluginFacesMBeanRegistry.registerPluginFacesMBeanConfig(RabbitmqInjectorBootstrap.class.getResource(FACES_CONFIG_FILE_PATH));
    }

    private void portalFacesMBeanRegistryUnplug() {
        try {
            portalPluginFacesMBeanRegistry.unregisterPluginFacesMBeanConfig(RabbitmqInjectorBootstrap.class.getResource(FACES_CONFIG_FILE_PATH));
        } catch (IllegalAccessException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (ClassNotFoundException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (InstantiationException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    private static String MAIN_MENU_INJECTOR_CONTEXT;
    public static  String INJ_TREE_ROOT_PATH = "_Mapping_Middleware_RabbitMQ_";

    private void rootInjectorRegistryPlug() {
        try {
            MAIN_MENU_INJECTOR_CONTEXT = portalPluginFacesMBeanRegistry.getRegisteredServletContext().getContextPath()+"/";

            rootInjectorRegistry.getLinkedMainMenuEntity().getDisplayRoles().add("mdwrabbitadmin");
            rootInjectorRegistry.getLinkedMainMenuEntity().getDisplayRoles().add("mdwrabbitreviewer");
            rootInjectorRegistry.getLinkedMainMenuEntity().getDisplayPermissions().add("injMapMdwRabbitMQ:display");

            /*
             * directories injector registration
             */
            /*
            TreeMenuEntity directoriesRootInjectorMenuEntity = rootInjectorRegistry.getTreeMenuEntityFromValue("Directories");
            if (directoriesRootInjectorMenuEntity == null) {
                log.warn("Directories injectors root is missing ! ");
            } else {
                directoriesRootInjectorMenuEntity.addDisplayRole("mdwtibrvadmin").addDisplayRole("mdwtibrvreviewer").addDisplayPermission("injDirMdwTibRV:display");

                TreeMenuEntity directoryMdwInjectorMenuEntity = directoriesRootInjectorMenuEntity.findTreeMenuEntityFromValue("Middleware");
                if (directoryMdwInjectorMenuEntity ==null) {
                    directoryMdwInjectorMenuEntity = new TreeMenuEntity().setId("dirmdwDir").setValue("Middleware").setType(MenuEntityType.TYPE_MENU_SUBMENU).setParentTreeMenuEntity(directoriesRootInjectorMenuEntity);
                    directoriesRootInjectorMenuEntity.addChildTreeMenuEntity(directoryMdwInjectorMenuEntity);
                }
                directoryMdwInjectorMenuEntity.addDisplayRole("mdwtibrvadmin").addDisplayRole("mdwtibrvreviewer").addDisplayPermission("injDirMdwTibRV:display");

                directoryMdwInjectorMenuEntity.addChildTreeMenuEntity(new TreeMenuEntity().setId("tibcorvDirTreeID").setValue("Tibco RendezVous").setParentTreeMenuEntity(directoryMdwInjectorMenuEntity).
                                                                                           setIcon("icon-asterisk").setType(MenuEntityType.TYPE_MENU_ITEM).
                                                                                           setContextAddress(MAIN_MENU_INJECTOR_CONTEXT + "views/injectors/main.jsf").
                                                                                           setDescription("Inject data from your local Tibco RV CMDB to CC Tibco RV directory").
                                                                                           addDisplayRole("mdwtibrvadmin").addDisplayRole("mdwtibrvreviewer").
                                                                                           addDisplayPermission("injDirMdwTibRV:display"));
            }
            */

            /*
             * mapping injector registration
             */
            TreeMenuEntity mappingRootInjectorMenuEntity = rootInjectorRegistry.getTreeMenuEntityFromValue("Mapping");
            if (mappingRootInjectorMenuEntity == null) {
                mappingRootInjectorMenuEntity = new TreeMenuEntity().setId("mappingDir").setValue("Mapping").setType(MenuEntityType.TYPE_MENU_SUBMENU);
                rootInjectorRegistry.registerTreeMenuRootEntity(mappingRootInjectorMenuEntity);
            }
            mappingRootInjectorMenuEntity.addDisplayRole("mdwrabbitadmin").addDisplayRole("mdwrabbitreviewer").addDisplayPermission("injMapMdwRabbitMQ:display");

            TreeMenuEntity mappingMdwInjectorMenuEntity = mappingRootInjectorMenuEntity.findTreeMenuEntityFromValue("Middleware");
            if (mappingMdwInjectorMenuEntity ==null) {
                mappingMdwInjectorMenuEntity = new TreeMenuEntity().setId("mapmdwDir").setValue("Middleware").setType(MenuEntityType.TYPE_MENU_SUBMENU).setParentTreeMenuEntity(mappingRootInjectorMenuEntity);
                mappingRootInjectorMenuEntity.addChildTreeMenuEntity(mappingMdwInjectorMenuEntity);
            }
            mappingMdwInjectorMenuEntity.addDisplayRole("mdwrabbitadmin").addDisplayRole("mdwrabbitreviewer").addDisplayPermission("injMapMdwRabbitMQ:display");
            mappingMdwInjectorMenuEntity.addChildTreeMenuEntity(new TreeMenuEntity().setId("rabbitmqMapTreeID").setValue("RabbitMQ").setParentTreeMenuEntity(mappingMdwInjectorMenuEntity).
                                                                                     setIcon("icon-asterisk").setType(MenuEntityType.TYPE_MENU_ITEM).
                                                                                     setContextAddress(MAIN_MENU_INJECTOR_CONTEXT + "views/injectors/rabbitmq.jsf").
                                                                                     setDescription("Inject data from your local RabbitMQ infrastructure to Ariane mapping").
                                                                                     addDisplayRole("mdwrabbitmqadmin").addDisplayRole("mdwrabbitmqreviewer").
                                                                                     addDisplayPermission("injMapMdwRabbitMQ:display"));
        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    private void rootInjectorRegistryUnplug() {
        try {
            /*
            TreeMenuEntity directoriesRootInjectorMenuEntity = rootInjectorRegistry.getTreeMenuEntityFromValue("Directories");
            if (directoriesRootInjectorMenuEntity == null) {
                log.warn("Directories injectors root is missing ! ");
            } else {
                directoriesRootInjectorMenuEntity.removeDisplayRole("mdwtibrvadmin").removeDisplayRole("mdwtibrvreviewer").removeDisplayPermission("injDirMdwTibRV:display");

                TreeMenuEntity directoryMdwInjectorMenuEntity = directoriesRootInjectorMenuEntity.findTreeMenuEntityFromValue("Middleware");
                directoryMdwInjectorMenuEntity.getChildTreeMenuEntities().remove(directoryMdwInjectorMenuEntity.findTreeMenuEntityFromValue("Tibco RendezVous"));
                if (directoryMdwInjectorMenuEntity.getChildTreeMenuEntities().size()==0)
                    directoriesRootInjectorMenuEntity.getChildTreeMenuEntities().remove(directoryMdwInjectorMenuEntity);
                else
                    directoryMdwInjectorMenuEntity.removeDisplayRole("mdwtibrvadmin").removeDisplayRole("mdwtibrvreviewer").removeDisplayPermission("injDirMdwTibRV:display");
            }
            */

            TreeMenuEntity mappingRootInjectorMenuEntity = rootInjectorRegistry.getTreeMenuEntityFromValue("Mapping");
            mappingRootInjectorMenuEntity.removeDisplayRole("mdwrabbitadmin").removeDisplayRole("mdwrabbitreviewer").removeDisplayPermission("injMapMdwRabbitMQ:display");

            TreeMenuEntity mappingMdwInjectorMenuEntity = mappingRootInjectorMenuEntity.findTreeMenuEntityFromValue("Middleware");
            mappingMdwInjectorMenuEntity.getChildTreeMenuEntities().remove(mappingMdwInjectorMenuEntity.findTreeMenuEntityFromValue("RabbitMQ"));
            if (mappingMdwInjectorMenuEntity.getChildTreeMenuEntities().size()==0) {
                mappingRootInjectorMenuEntity.getChildTreeMenuEntities().remove(mappingMdwInjectorMenuEntity);
                if (mappingRootInjectorMenuEntity.getChildTreeMenuEntities().size()==0)
                    rootInjectorRegistry.unregisterTreeMenuRootEntity(mappingRootInjectorMenuEntity);
            } else
                mappingMdwInjectorMenuEntity.removeDisplayRole("mdwrabbitadmin").removeDisplayRole("mdwrabbitreviewer").removeDisplayPermission("injMapMdwRabbitMQ:display");

        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }
}