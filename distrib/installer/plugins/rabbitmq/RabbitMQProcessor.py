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
from plugins.rabbitmq.DBRabbitMQDirectoryMySQLInitiator import DBRabbitMQDirectoryMySQLInitiator
from plugins.rabbitmq.DBRabbitMQIDMMySQLPopulator import DBRabbitMQIDMMySQLPopulator
from plugins.rabbitmq.CURabbitMQInjectorManagedServiceProcessor import RabbitMQInjectorManagedServiceSyringe
from plugins.rabbitmq.CURabbitMQInjectorComponentsCacheProcessor import CURabbitMQInjectorComponentsCacheProcessor
from plugins.rabbitmq.CURabbitMQInjectorGearsCacheProcessor import CURabbitMQInjectorGearsCacheProcessor, CPInjectorGearsCacheDir

__author__ = 'mffrench'


class RabbitMQProcessor:

    def __init__(self, home_dir_path, directory_db_config, idm_db_config, silent):
        print("\n%--%--%--%--%--%--%--%--%--%--%--%--%--%--%--%--%--%--%--%--%--%--%--%--%--%--%--%--%--%--%--%--%--%--"
              "%--%--%--%--%--%--%--%--%--\n")
        print("%-- Plugin RabbitMQ configuration : \n")
        self.homeDirPath = home_dir_path
        self.silent = silent
        if not os.path.exists(self.homeDirPath + "/ariane/cache/plugins/rabbitmq/"):
            os.makedirs(self.homeDirPath + "/ariane/cache/plugins/rabbitmq/", 0o755)
        self.rabbitmqInjectorGearsCache = CURabbitMQInjectorGearsCacheProcessor(
            self.homeDirPath + "/ariane/cache/plugins/rabbitmq/"
        )
        self.rabbitmqInjectorComponentsCache = CURabbitMQInjectorComponentsCacheProcessor(
            self.homeDirPath + "/ariane/cache/plugins/rabbitmq/"
        )
        self.rabbitmqInjectorManagedServiceSyringe = RabbitMQInjectorManagedServiceSyringe(home_dir_path, silent)
        self.rabbitmqDirectoryMySQLInitiator = DBRabbitMQDirectoryMySQLInitiator(directory_db_config)
        self.rabbitmqIDMMySQLPopulator = DBRabbitMQIDMMySQLPopulator(idm_db_config)

    def process(self):
        self.rabbitmqInjectorGearsCache.process()
        self.rabbitmqInjectorComponentsCache.process()
        self.rabbitmqInjectorManagedServiceSyringe.shoot_builder()
        self.rabbitmqInjectorManagedServiceSyringe.inject()
        self.rabbitmqDirectoryMySQLInitiator.process()
        self.rabbitmqIDMMySQLPopulator.process()
        return self
