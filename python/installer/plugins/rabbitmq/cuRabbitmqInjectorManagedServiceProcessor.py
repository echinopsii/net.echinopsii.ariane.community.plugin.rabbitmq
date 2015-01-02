# installer plugin rabbitmq directory jpa provider configuration unit processor
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


class cuRabbitmqInjectorManagedServiceProcessor(AConfUnit):

    def __init__(self, targetConfDir):
        self.confUnitName = "Plugin RabbitMQ injector managed service"
        self.confTemplatePath = os.path.abspath("resources/templates/plugins/rabbitmq/net.echinopsii.ariane.community.plugin.rabbitmq.RabbitMQInjectorManagedService.properties.tpl")
        self.confFinalPath = targetConfDir + "net.echinopsii.ariane.community.plugin.rabbitmq.RabbitMQInjectorManagedService.properties"
        componentSniffInterval = cpComponentSniffInterval()
        directoryQueryInterval = cpDirectoryQueryInterval()
        self.paramsDictionary = {
            componentSniffInterval.name: componentSniffInterval,
            directoryQueryInterval.name: directoryQueryInterval
        }


class rabbitmqInjectorManagedServiceSyringe:

    def __init__(self, targetConfDir, silent):
        self.tibcoRVInjectorManagedService = cuRabbitmqInjectorManagedServiceProcessor(targetConfDir)
        tibcorvInjectorManagedServiceCUJSON = open("resources/configvalues/plugins/rabbitmq/cuRabbitmqInjectorManagedService.json")
        self.tibcorvInjectorManagedServiceCUValues = json.load(tibcorvInjectorManagedServiceCUJSON)
        tibcorvInjectorManagedServiceCUJSON.close()
        self.silent = silent

    def shootBuilder(self):
        for key in self.tibcoRVInjectorManagedService.getParamsKeysList():

            if key == cpComponentSniffInterval.name:
                if not self.silent:
                    componentSniffIntervalIsDefined = False
                    componentSniffIntervalDefault = self.tibcorvInjectorManagedServiceCUValues[cpComponentSniffInterval.name]
                    componentSniffIntervalDefaultUI = "[default - " + componentSniffIntervalDefault + "] "

                    while not componentSniffIntervalIsDefined:
                        sniffInt = input("%-- >> Define Plugin RabbitMQ components sniff interval " + componentSniffIntervalDefaultUI + ": ")
                        try:
                            if sniffInt is None or sniffInt == "":
                                sniffInt = str(componentSniffIntervalDefault)
                            self.tibcoRVInjectorManagedService.setKeyParamValue(key, sniffInt)
                            componentSniffIntervalIsDefined = True
                        except Exception:
                            pass
                else:
                    self.tibcoRVInjectorManagedService.setKeyParamValue(key, self.tibcorvInjectorManagedServiceCUValues[cpComponentSniffInterval.name])

            elif key == cpDirectoryQueryInterval.name:
                if not self.silent:
                    directoryQueryIntervalIsDefined = False
                    directoryQueryIntervalDefault = self.tibcorvInjectorManagedServiceCUValues[cpDirectoryQueryInterval.name]
                    directoryQueryIntervalDefaultUI = "[default - " + directoryQueryIntervalDefault + "] "
                    while not directoryQueryIntervalIsDefined:
                        queryInt = input("%-- >> Define Plugin RabbitMQ directory query interval " + directoryQueryIntervalDefaultUI + ": ")
                        try:
                            if queryInt is None or queryInt == "":
                                queryInt = str(directoryQueryIntervalDefault)
                            self.tibcoRVInjectorManagedService.setKeyParamValue(key, queryInt)
                            directoryQueryIntervalIsDefined = True
                        except Exception:
                            pass
                else:
                    self.tibcoRVInjectorManagedService.setKeyParamValue(key, self.tibcorvInjectorManagedServiceCUValues[cpDirectoryQueryInterval.name])

    def inject(self):
        self.tibcoRVInjectorManagedService.process()