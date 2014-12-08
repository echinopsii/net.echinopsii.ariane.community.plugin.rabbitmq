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

package net.echinopsii.ariane.community.rabbitmq.injector;

import net.echinopsii.ariane.community.core.injector.base.registry.InjectorComponentsRegistry;
import net.echinopsii.ariane.community.core.injector.base.registry.InjectorGearsRegistry;
import net.echinopsii.ariane.community.core.mapping.ds.service.MappingSce;
import net.echinopsii.ariane.community.core.portal.base.plugin.FaceletsResourceResolverService;
import net.echinopsii.ariane.community.plugin.rabbitmq.directory.RabbitmqDirectoryService;
import org.apache.felix.ipojo.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.Dictionary;

@Component(managedservice="net.echinopsii.ariane.community.plugin.rabbitmq.RabbitMQInjectorManagedService")
@Provides(properties= {@StaticServiceProperty(name="targetArianeComponent", type="java.lang.String", value="Portal")})
@Instantiate
public class RabbitmqInjectorBootstrap implements FaceletsResourceResolverService {

    private static final Logger log = LoggerFactory.getLogger(RabbitmqInjectorBootstrap.class);
    private static final String RABBITMQ_INJECTOR_SERVICE_NAME = "Ariane RabbitMQ Plugin Injector Component";

    private static Dictionary<Object, Object> config = null;

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

    @Override
    public URL resolveURL(String path) {
        return null;
    }

    public static  String INJ_TREE_ROOT_PATH = "_Mapping_Middleware_RabbitMQ_";
}