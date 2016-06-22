# installer plugin RabbitMQ directory MySQL db initiator
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
from tools.AMySQLdbInit import AMySQLdbInit

__author__ = 'mffrench'


class DBRabbitMQDirectoryMySQLInitiator(AMySQLdbInit):

    def __init__(self, db_config):
        self.dbServerUser = db_config['user']
        self.dbServerPassword = db_config['password']
        self.dbServerHost = db_config['host']
        self.dbServerPort = db_config['port']
        self.dbName = db_config['database']
        self.sqlScriptFilePath = "resources/sqlscripts/plugins/rabbitmq/directory_plugin_rabbitmq.sql"

    def process(self):
        return super(DBRabbitMQDirectoryMySQLInitiator, self).process()
