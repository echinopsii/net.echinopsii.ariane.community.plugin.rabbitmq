# installer injector gears cache processor
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
import os
from tools.AConfParamNotNone import AConfParamNotNone
from tools.AConfUnit import AConfUnit

__author__ = 'mffrench'


class CPInjectorGearsCacheDir(AConfParamNotNone):

    name = "##rabbitmqInjectorGearsCacheDir"
    description = "RabbitMQ Injector Gears Cache Directory"
    hide = False

    def __init__(self):
        self.value = None

    def is_valid(self):
        if not super().is_valid:
            return False
        else:
            if os.path.exists(self.value) and os.path.isdir(self.value) and \
                    os.access(self.value, os.W_OK) and os.access(self.value, os.W_OK):
                return True
            else:
                print(self.description + " (" + str(self.value) +
                      ") is not valid. Check if it exists and it has good rights.")
                return False


class CURabbitMQInjectorGearsCacheProcessor(AConfUnit):

    def __init__(self, target_conf_dir):
        self.confUnitName = "RabbitMQ Injector Gears Cache"
        self.confTemplatePath = os.path.abspath("resources/templates/plugins/rabbitmq/infinispan.gears.cache.xml.tpl")
        self.confFinalPath = target_conf_dir + "infinispan.gears.cache.xml"
        injector_gears_cache_dir = CPInjectorGearsCacheDir()
        self.paramsDictionary = {
            injector_gears_cache_dir.name: injector_gears_cache_dir
        }
        self.set_key_param_value(CPInjectorGearsCacheDir.name, target_conf_dir)

    def set_key_param_value(self, key, value):
        return super(CURabbitMQInjectorGearsCacheProcessor, self).set_key_param_value(key, value)

    def get_params_keys_list(self):
        return super(CURabbitMQInjectorGearsCacheProcessor, self).get_params_keys_list()

    def process(self):
        return super(CURabbitMQInjectorGearsCacheProcessor, self).process()

    def get_param_from_key(self, key):
        return super(CURabbitMQInjectorGearsCacheProcessor, self).get_param_from_key(key)
