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


class cpComponentSniffInterval(AConfParamNotNone):

    name = "##componentSniffInterval"
    description = "Plugin RabbitMQ component sniff interval (seconds)"
    hide = False

    def __init__(self):
        self.value = None

    def isValid(self):
        if not super().isValid:
            return False
        else:
            try:
                sleepInterval = int(self.value)
            except ValueError:
                print("!! Invalid component sniff interval " + self.value + " : not a number", file=sys.stderr)
                return False

            if sleepInterval <= 1:
                print("!! Invalid component sniff interval " + self.value + " : must be > 1", file=sys.stderr)
                return False

            return True


class cpDirectoryQueryInterval(AConfParamNotNone):

    name = "##directoryQueryInterval"
    description = "Plugin RabbitMQ directory query interval (seconds)"
    hide = False

    def __init__(self):
        self.value = None

    def isValid(self):
        if not super().isValid:
            return False
        else:
            try:
                sleepInterval = int(self.value)
            except ValueError:
                print("!! Invalid component sniff interval " + self.value + " : not a number", file=sys.stderr)
                return False

            if sleepInterval <= 1:
                print("!! Invalid component sniff interval " + self.value + " : must be > 1", file=sys.stderr)
                return False

            return True


class cpRabbitmqInjectorComponentsCacheConfPath(AConfParamNotNone):
    
    name = "##rabbitmqInjectorComponentsCacheConfFilePath"
    description = "Plugin RabbitMQ Injector Components Cache Configuration File Path"
    hide = False
    
    def __init__(self):
        self.value = None 

    def isValid(self):
        if not super().isValid:
            return False
        else:
            if os.path.exists(self.value) and os.access(self.value, os.W_OK) and os.access(self.value, os.W_OK):
                return True
            else:
                print(self.description + " (" + self.value + ") is not valid.Check if it exists and it has good rights.")
                return False


class cpRabbitmqInjectorGearsCacheConfPath(AConfParamNotNone):

    name = "##rabbitmqInjectorGearsCacheConfFilePath"
    description = "Plugin RabbitMQ Injector Gears Cache Configuration File Path"
    hide = False

    def __init__(self):
        self.value = None

    def isValid(self):
        if not super().isValid:
            return False
        else:
            if os.path.exists(self.value) and os.access(self.value, os.W_OK) and os.access(self.value, os.W_OK):
                return True
            else:
                print(self.description + " (" + self.value + ") is not valid.Check if it exists and it has good rights.")
                return False
            

class cuRabbitmqInjectorManagedServiceProcessor(AConfUnit):

    def __init__(self, arianeDir):
        addonsRepositoryDirPath = arianeDir + "/repository/ariane-plugins/"
        rbqCacheDirPath = arianeDir + "/ariane/cache/plugins/rabbitmq/"

        self.confUnitName = "Plugin RabbitMQ injector managed service"
        self.confTemplatePath = os.path.abspath("resources/templates/plugins/rabbitmq/net.echinopsii.ariane.community.plugin.rabbitmq.RabbitMQInjectorManagedService.properties.tpl")
        self.confFinalPath = addonsRepositoryDirPath + "net.echinopsii.ariane.community.plugin.rabbitmq.RabbitMQInjectorManagedService.properties"
        componentSniffInterval = cpComponentSniffInterval()
        directoryQueryInterval = cpDirectoryQueryInterval()
        injectorComponentsConfPath = cpRabbitmqInjectorComponentsCacheConfPath()
        injectorGearsConfPath = cpRabbitmqInjectorGearsCacheConfPath()

        self.paramsDictionary = {
            componentSniffInterval.name: componentSniffInterval,
            directoryQueryInterval.name: directoryQueryInterval,
            injectorComponentsConfPath.name: injectorComponentsConfPath,
            injectorGearsConfPath.name: injectorGearsConfPath 
        }


class rabbitmqInjectorManagedServiceSyringe:

    def __init__(self, arianeDir, silent):
        self.addonsRepositoryDirPath = arianeDir + "/repository/ariane-plugins/"
        if not os.path.exists(self.addonsRepositoryDirPath):
            os.makedirs(self.addonsRepositoryDirPath, 0o755)

        self.rbqCacheDirPath = arianeDir + "/ariane/cache/plugins/rabbitmq/"
        if not os.path.exists(self.rbqCacheDirPath):
            os.makedirs(self.rbqCacheDirPath, 0o755)

        self.rbqInjectorManagedService = cuRabbitmqInjectorManagedServiceProcessor(arianeDir)
        rbqInjectorManagedServiceCUJSON = open("resources/configvalues/plugins/rabbitmq/cuRabbitmqInjectorManagedService.json")
        self.rbqInjectorManagedServiceCUValues = json.load(rbqInjectorManagedServiceCUJSON)
        rbqInjectorManagedServiceCUJSON.close()
        self.silent = silent

    def shootBuilder(self):
        for key in self.rbqInjectorManagedService.getParamsKeysList():

            if key == cpRabbitmqInjectorGearsCacheConfPath.name:
                self.rbqInjectorManagedService.setKeyParamValue(key, self.rbqCacheDirPath + "infinispan.gears.cache.xml")

            elif key == cpRabbitmqInjectorComponentsCacheConfPath.name:
                self.rbqInjectorManagedService.setKeyParamValue(key, self.rbqCacheDirPath + "infinispan.components.cache.xml")

            elif key == cpComponentSniffInterval.name:
                if not self.silent:
                    componentSniffIntervalIsDefined = False
                    componentSniffIntervalDefault = self.rbqInjectorManagedServiceCUValues[cpComponentSniffInterval.name]
                    componentSniffIntervalDefaultUI = "[default - " + componentSniffIntervalDefault + "] "

                    while not componentSniffIntervalIsDefined:
                        sniffInt = input("%-- >> Define Plugin RabbitMQ components sniff interval " + componentSniffIntervalDefaultUI + ": ")
                        try:
                            if sniffInt is None or sniffInt == "":
                                sniffInt = str(componentSniffIntervalDefault)
                            self.rbqInjectorManagedService.setKeyParamValue(key, sniffInt)
                            componentSniffIntervalIsDefined = True
                        except Exception:
                            pass
                else:
                    self.rbqInjectorManagedService.setKeyParamValue(key, self.rbqInjectorManagedServiceCUValues[cpComponentSniffInterval.name])

            elif key == cpDirectoryQueryInterval.name:
                if not self.silent:
                    directoryQueryIntervalIsDefined = False
                    directoryQueryIntervalDefault = self.rbqInjectorManagedServiceCUValues[cpDirectoryQueryInterval.name]
                    directoryQueryIntervalDefaultUI = "[default - " + directoryQueryIntervalDefault + "] "
                    while not directoryQueryIntervalIsDefined:
                        queryInt = input("%-- >> Define Plugin RabbitMQ directory query interval " + directoryQueryIntervalDefaultUI + ": ")
                        try:
                            if queryInt is None or queryInt == "":
                                queryInt = str(directoryQueryIntervalDefault)
                            self.rbqInjectorManagedService.setKeyParamValue(key, queryInt)
                            directoryQueryIntervalIsDefined = True
                        except Exception:
                            pass                
                else:
                    self.rbqInjectorManagedService.setKeyParamValue(key, self.rbqInjectorManagedServiceCUValues[cpDirectoryQueryInterval.name])

    def inject(self):
        self.rbqInjectorManagedService.process()
