# installer plugin rabbitmq processor
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
from plugins.rabbitmq.dbRabbitmqDirectoryMySQLInitiator import dbRabbitmqDirectoryMySQLInitiator
from plugins.rabbitmq.dbIDMMySQLPopulator import dbIDMMySQLPopulator
from plugins.rabbitmq.cuRabbitmqInjectorManagedServiceProcessor import rabbitmqInjectorManagedServiceSyringe

__author__ = 'mffrench'


class rabbitmqProcessor:

    def __init__(self, homeDirPath, directoryDBConfig, idmDBConfig, silent):
        print("\n%--%--%--%--%--%--%--%--%--%--%--%--%--%--%--%--%--%--%--%--%--%--%--%--%--%--%--%--%--%--%--%--%--%--%--%--%--%--%--%--%--%--%--\n")
        print("%-- Plugin RabbitMQ configuration : \n")
        self.homeDirPath = homeDirPath
        self.silent = silent
        addonsRepositoryDirPath = self.homeDirPath + "/repository/ariane-plugins/"
        if not os.path.exists(addonsRepositoryDirPath):
            os.makedirs(addonsRepositoryDirPath, 0o755)
        self.rabbitmqInjectorManagedServiceSyringe = rabbitmqInjectorManagedServiceSyringe(addonsRepositoryDirPath, silent)
        self.rabbitmqDirectoryMySQLInitiator = dbRabbitmqDirectoryMySQLInitiator(directoryDBConfig)
        self.rabbitmqIDMMySQLPopulator = dbIDMMySQLPopulator(idmDBConfig)

    def process(self):
        self.rabbitmqInjectorManagedServiceSyringe.shootBuilder()
        self.rabbitmqInjectorManagedServiceSyringe.inject()
        self.rabbitmqDirectoryMySQLInitiator.process()
        self.rabbitmqIDMMySQLPopulator.process()
        return self