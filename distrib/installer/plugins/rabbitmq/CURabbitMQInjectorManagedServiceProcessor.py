# installer plugin RabbitMQ directory jpa provider configuration unit processor
#
# Copyright (C) 2014 Mathilde Ffrench
#
# This program is free software: you can redistribute it and/or modify
# it under the terms of the GNU Affero General Public License as
# published by the Free Software Foundation, either version 3 of the
# License, or (at your option) any later version.
#
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU Affero General Public License for more details.
#
# You should have received a copy of the GNU Affero General Public License
# along with this program.  If not, see <http://www.gnu.org/licenses/>.
import json
import os
import sys
from tools.AConfParamNotNone import AConfParamNotNone
from tools.AConfUnit import AConfUnit

__author__ = 'mffrench'


class CPComponentSniffInterval(AConfParamNotNone):

    name = "##componentSniffInterval"
    description = "Plugin RabbitMQ component sniff interval (seconds)"
    hide = False

    def __init__(self):
        self.value = None

    def is_valid(self):
        if not super().is_valid:
            return False
        else:
            try:
                sleep_interval = int(self.value)
            except ValueError:
                print("!! Invalid component sniff interval " + str(self.value) + " : not a number", file=sys.stderr)
                return False

            if sleep_interval <= 1:
                print("!! Invalid component sniff interval " + str(self.value) + " : must be > 1", file=sys.stderr)
                return False

            return True


class CPDirectoryQueryInterval(AConfParamNotNone):

    name = "##directoryQueryInterval"
    description = "Plugin RabbitMQ directory query interval (seconds)"
    hide = False

    def __init__(self):
        self.value = None

    def is_valid(self):
        if not super().is_valid:
            return False
        else:
            try:
                sleep_interval = int(self.value)
            except ValueError:
                print("!! Invalid component sniff interval " + str(self.value) + " : not a number", file=sys.stderr)
                return False

            if sleep_interval <= 1:
                print("!! Invalid component sniff interval " + str(self.value) + " : must be > 1", file=sys.stderr)
                return False

            return True


class CPRabbitmqInjectorComponentsCacheConfPath(AConfParamNotNone):
    
    name = "##rabbitmqInjectorComponentsCacheConfFilePath"
    description = "Plugin RabbitMQ Injector Components Cache Configuration File Path"
    hide = False
    
    def __init__(self):
        self.value = None 

    def is_valid(self):
        if not super().is_valid:
            return False
        else:
            if os.path.exists(self.value) and \
                    os.access(self.value, os.W_OK) and os.access(self.value, os.W_OK):
                return True
            else:
                print(self.description + " (" + str(self.value) +
                      ") is not valid.Check if it exists and it has good rights.")
                return False


class CPRabbitmqInjectorGearsCacheConfPath(AConfParamNotNone):

    name = "##rabbitmqInjectorGearsCacheConfFilePath"
    description = "Plugin RabbitMQ Injector Gears Cache Configuration File Path"
    hide = False

    def __init__(self):
        self.value = None

    def is_valid(self):
        if not super().is_valid:
            return False
        else:
            if os.path.exists(self.value) and \
                    os.access(self.value, os.W_OK) and os.access(self.value, os.W_OK):
                return True
            else:
                print(self.description + " (" + str(self.value) +
                      ") is not valid.Check if it exists and it has good rights.")
                return False
            

class CURabbitMQInjectorManagedServiceProcessor(AConfUnit):

    def __init__(self, ariane_dir):
        addons_repository_dir_path = ariane_dir + "/repository/ariane-plugins/"
        rbq_cache_dir_path = ariane_dir + "/ariane/cache/plugins/rabbitmq/"

        self.confUnitName = "Plugin RabbitMQ injector managed service"
        self.confTemplatePath = os.path.abspath(
            "resources/templates/plugins/rabbitmq/"
            "net.echinopsii.ariane.community.plugin.rabbitmq.RabbitMQInjectorManagedService.properties.tpl"
        )
        self.confFinalPath = addons_repository_dir_path + \
            "net.echinopsii.ariane.community.plugin.rabbitmq.RabbitMQInjectorManagedService.properties"
        component_sniff_interval = CPComponentSniffInterval()
        directory_query_interval = CPDirectoryQueryInterval()
        injector_components_conf_path = CPRabbitmqInjectorComponentsCacheConfPath()
        injector_gears_conf_path = CPRabbitmqInjectorGearsCacheConfPath()

        self.paramsDictionary = {
            component_sniff_interval.name: component_sniff_interval,
            directory_query_interval.name: directory_query_interval,
            injector_components_conf_path.name: injector_components_conf_path,
            injector_gears_conf_path.name: injector_gears_conf_path
        }

    def set_key_param_value(self, key, value):
        return super(CURabbitMQInjectorManagedServiceProcessor, self).set_key_param_value(key, value)

    def get_params_keys_list(self):
        return super(CURabbitMQInjectorManagedServiceProcessor, self).get_params_keys_list()

    def process(self):
        return super(CURabbitMQInjectorManagedServiceProcessor, self).process()

    def get_param_from_key(self, key):
        return super(CURabbitMQInjectorManagedServiceProcessor, self).get_param_from_key(key)


class RabbitMQInjectorManagedServiceSyringe:

    def __init__(self, ariane_dir, silent):
        self.addonsRepositoryDirPath = ariane_dir + "/repository/ariane-plugins/"
        if not os.path.exists(self.addonsRepositoryDirPath):
            os.makedirs(self.addonsRepositoryDirPath, 0o755)

        self.rbqCacheDirPath = ariane_dir + "/ariane/cache/plugins/rabbitmq/"
        if not os.path.exists(self.rbqCacheDirPath):
            os.makedirs(self.rbqCacheDirPath, 0o755)

        self.rbqInjectorManagedService = CURabbitMQInjectorManagedServiceProcessor(ariane_dir)
        cujson = open("resources/configvalues/plugins/rabbitmq/cuRabbitmqInjectorManagedService.json")
        self.rbqInjectorManagedServiceCUValues = json.load(cujson)
        cujson.close()
        self.silent = silent

    def shoot_builder(self):
        for key in self.rbqInjectorManagedService.get_params_keys_list():

            if key == CPRabbitmqInjectorGearsCacheConfPath.name:
                self.rbqInjectorManagedService.set_key_param_value(
                    key, self.rbqCacheDirPath + "infinispan.gears.cache.xml"
                )

            elif key == CPRabbitmqInjectorComponentsCacheConfPath.name:
                self.rbqInjectorManagedService.set_key_param_value(
                    key, self.rbqCacheDirPath + "infinispan.components.cache.xml"
                )

            elif key == CPComponentSniffInterval.name:
                if not self.silent:
                    component_sniff_interval_is_defined = False
                    component_sniff_interval_default = self.rbqInjectorManagedServiceCUValues[
                        CPComponentSniffInterval.name
                    ]
                    component_sniff_interval_default_ui = "[default - " + component_sniff_interval_default + "] "

                    while not component_sniff_interval_is_defined:
                        sniff_int = input("%-- >> Define Plugin RabbitMQ components sniff interval " +
                                          component_sniff_interval_default_ui + ": ")
                        try:
                            if sniff_int is None or sniff_int == "":
                                sniff_int = str(component_sniff_interval_default)
                            self.rbqInjectorManagedService.set_key_param_value(key, sniff_int)
                            component_sniff_interval_is_defined = True
                        except Exception:
                            pass
                else:
                    self.rbqInjectorManagedService.set_key_param_value(
                        key, self.rbqInjectorManagedServiceCUValues[CPComponentSniffInterval.name]
                    )

            elif key == CPDirectoryQueryInterval.name:
                if not self.silent:
                    directory_query_interval_is_defined = False
                    directory_query_interval_default = self.rbqInjectorManagedServiceCUValues[CPDirectoryQueryInterval.name]
                    directory_query_interval_default_ui = "[default - " + directory_query_interval_default + "] "
                    while not directory_query_interval_is_defined:
                        query_int = input("%-- >> Define Plugin RabbitMQ directory query interval " +
                                          directory_query_interval_default_ui + ": ")
                        try:
                            if query_int is None or query_int == "":
                                query_int = str(directory_query_interval_default)
                            self.rbqInjectorManagedService.set_key_param_value(key, query_int)
                            directory_query_interval_is_defined = True
                        except Exception:
                            pass                
                else:
                    self.rbqInjectorManagedService.set_key_param_value(
                        key, self.rbqInjectorManagedServiceCUValues[CPDirectoryQueryInterval.name]
                    )

    def inject(self):
        self.rbqInjectorManagedService.process()
