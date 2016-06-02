/**
 * RabbitMQ plugin injector bundle
 * RabbitMQ plugin injector bootstrap
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

package net.echinopsii.ariane.community.plugin.rabbitmq.injector;

import net.echinopsii.ariane.community.core.injector.base.registry.InjectorComponentsRegistry;
import net.echinopsii.ariane.community.core.injector.base.registry.InjectorGearsRegistry;
import net.echinopsii.ariane.community.core.injector.base.registry.InjectorRegistryFactory;
import net.echinopsii.ariane.community.core.mapping.ds.service.MappingSce;
import net.echinopsii.ariane.community.core.mapping.ds.service.proxy.SProxMappingSce;
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

@SuppressWarnings("ALL")
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
    private SProxMappingSce mappingService = null;
    private static SProxMappingSce mappingSceSgt  = null;

    @Bind
    public void bindMappingService(SProxMappingSce t) {
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

    public static SProxMappingSce getMappingSce() {
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

    @Bind(from="ArianePortalFacesMBeanRegistry")
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

    @Bind(from="InjectorTreeMenuRootsRegistryImpl")
    public void bindRootInjectorRegistry(TreeMenuRootsRegistry r) {
        log.debug("Bound to injector tree menu root registry...");
        rootInjectorRegistry = r;
    }

    @Unbind
    public void unbindRootInjectorRegistry() {
        log.debug("Unbound from injector tree menu root registry...");
        rootInjectorRegistry = null;
    }

    private static InjectorComponentsRegistry sgtComponentsRegistry;
    private static InjectorGearsRegistry sgtGearsRegistry;
    public static InjectorComponentsRegistry getComponentsRegistry() {
        return sgtComponentsRegistry;
    }
    public static InjectorGearsRegistry getGearsRegisry() {
        return sgtGearsRegistry;
    }

    @Requires
    private InjectorRegistryFactory registryFactory;
    private static InjectorRegistryFactory sgtRegistryFactory;

    @Bind
    public void bindRegistryFactory(InjectorRegistryFactory factory) {
        log.debug("Bound to injector registry factory...");
        registryFactory = factory;
        sgtRegistryFactory = factory;
    }

    @Unbind
    public void unbindRegistryFactory() {
        log.debug("Unbound from injector registry factory...");
        registryFactory = null;
        sgtRegistryFactory = null;
    }

    private final static void start() throws IOException, InterruptedException {
        if (!isStarted) {
            while(config==null) {
                log.info("Config is missing to load RabbitMQ Injector. Sleep some times...");
                Thread.sleep(1000);
            }

            sgtGearsRegistry = sgtRegistryFactory.makeGearsRegistry(config);
            sgtComponentsRegistry = sgtRegistryFactory.makeComponentsRegistry(config);

            sgtGearsRegistry.startRegistry();
            sgtComponentsRegistry.startRegistry();

            if (sgtGearsRegistry.keySetFromPrefix(INJ_TREE_ROOT_PATH).size()!=0) {
                for (String key: sgtGearsRegistry.keySetFromPrefix(INJ_TREE_ROOT_PATH))
                    sgtGearsRegistry.getEntityFromCache(key).start();
            } else {
                mappingAkkaGear = new MappingGear();
                mappingAkkaGear.start();
                sgtGearsRegistry.putEntityToCache(mappingAkkaGear);

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

            sgtGearsRegistry.stopRegistry();
            sgtComponentsRegistry.stopRegistry();
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
        if (isStarted) {
            config.put(RabbitmqInjectorMainCfgLoader.RABBITMQ_INJECTOR_CFG_DIRECTORY_QUERYINTERVAL_KEY, properties.get(RabbitmqInjectorMainCfgLoader.RABBITMQ_INJECTOR_CFG_DIRECTORY_QUERYINTERVAL_KEY));
            config.put(RabbitmqInjectorMainCfgLoader.RABBITMQ_INJECTOR_CFG_COMPONENT_SNIFFINTERVAL_KEY, properties.get(RabbitmqInjectorMainCfgLoader.RABBITMQ_INJECTOR_CFG_COMPONENT_SNIFFINTERVAL_KEY));

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
        } else if (RabbitmqInjectorMainCfgLoader.isValid(properties)) config=properties;
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
                                                                                     setIcon("icon-rabbitmq-injector").setType(MenuEntityType.TYPE_MENU_ITEM).
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