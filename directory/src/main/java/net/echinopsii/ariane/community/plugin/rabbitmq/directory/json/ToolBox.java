/**
 * RabbitMQ plugin directory bundle
 * provide a REST tools
 * Copyright (C) 2014  Mathilde Ffrench
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
package net.echinopsii.ariane.community.plugin.rabbitmq.directory.json;

import java.io.*;

public class ToolBox {

    public static String getOuputStreamContent(ByteArrayOutputStream out, String encoding) throws IOException {
        ByteArrayInputStream input = new ByteArrayInputStream(((ByteArrayOutputStream) out).toByteArray());
        BufferedReader br = new BufferedReader(new InputStreamReader(input, encoding));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) {
            sb.append(line);
            sb.append('\n');
        }
        return sb.toString();
    }
}
