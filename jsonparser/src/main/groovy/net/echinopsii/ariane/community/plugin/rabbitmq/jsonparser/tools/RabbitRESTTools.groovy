/**
 * RabbitMQ plugin jsonparser bundle
 * RabbitMQ plugin jsonparser REST tools
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
package net.echinopsii.ariane.community.plugin.rabbitmq.jsonparser.tools

import net.echinopsii.ariane.community.plugin.rabbitmq.jsonparser.serializable.ExchangeFromRabbitREST

class RabbitRESTTools {

    static List<String> getConnectionNames(RabbitNodeToConnect node) {
        List<String> ret = new ArrayList<String>()
        def connections_list_req = node.getRestCli().get(path:'/api/connections')
        if (connections_list_req.status == 200 && connections_list_req.data != null) {
            connections_list_req.data.each { aconnection ->
                ret.add((String)aconnection.name)
            }
        }
        return ret;
    }

    static List<String> getConnectionNames(RabbitClusterToConnect cluster) {
        List<String> ret = new ArrayList<String>()
        def connections_list_req = cluster.get('/api/connections')
        if (connections_list_req.status == 200 && connections_list_req.data != null) {
            connections_list_req.data.each { aconnection ->
                ret.add((String)aconnection.name)
            }
        }
        return ret;
    }

    static List<String> getChannelNames(RabbitNodeToConnect node) {
        List<String> ret = new ArrayList<String>()
        def channels_list_req = node.getRestCli().get(path:'/api/channels')
        if (channels_list_req.status == 200 && channels_list_req.data != null) {
            channels_list_req.data.each { achannel ->
                ret.add((String) achannel.name);
            }
        }
        return ret;
    }

    static List<String> getChannelNames(RabbitClusterToConnect cluster) {
        List<String> ret = new ArrayList<String>()
        def channels_list_req = cluster.get('/api/channels')
        if (channels_list_req.status == 200 && channels_list_req.data != null) {
            channels_list_req.data.each { achannel ->
                ret.add((String) achannel.name);
            }
        }
        return ret;
    }

    static Map<String,List<String>> getExchangeNames(RabbitNodeToConnect node) {
        Map<String,List<String>> ret = new HashMap<String,List<String>>()
        def exchanges_list_req = node.getRestCli().get(path:'/api/exchanges')
        if (exchanges_list_req.status == 200 && exchanges_list_req.data != null) {
            exchanges_list_req.data.each { anexchange ->
                if (ret.get(anexchange.vhost)==null)
                    ret.put((String)anexchange.vhost, new ArrayList<String>())
            }
            exchanges_list_req.data.each { anexchange ->
                if (anexchange.name.equals(""))
                    ret.get((String)anexchange.vhost).add(ExchangeFromRabbitREST.RABBITMQ_DEFAULT_EXCH_NAME)
                else
                    ret.get((String)anexchange.vhost).add((String)anexchange.name)
            }
        }
        return ret;
    }

    static Map<String,List<String>> getExchangeNames(RabbitClusterToConnect cluster) {
        Map<String,List<String>> ret = new HashMap<String,List<String>>()
        def exchanges_list_req = cluster.get('/api/exchanges')
        if (exchanges_list_req.status == 200 && exchanges_list_req.data != null) {
            exchanges_list_req.data.each { anexchange ->
                if (ret.get(anexchange.vhost)==null)
                    ret.put((String)anexchange.vhost, new ArrayList<String>())
            }
            exchanges_list_req.data.each { anexchange ->
                if (anexchange.name.equals(""))
                    ret.get((String)anexchange.vhost).add(ExchangeFromRabbitREST.RABBITMQ_DEFAULT_EXCH_NAME)
                else
                    ret.get((String)anexchange.vhost).add((String)anexchange.name)
            }
        }
        return ret;
    }

    static Map<String, List<String>> getBindingNames(RabbitNodeToConnect node) {
        Map<String,List<String>> ret = new HashMap<String, List<String>>()
        def bindings_list_req = node.getRestCli().get(path:'/api/bindings')
        if (bindings_list_req.status == 200 && bindings_list_req.data != null) {
            bindings_list_req.data.each { abinding ->
                if (ret.get(abinding.vhost)==null)
                    ret.put((String)abinding.vhost, new ArrayList<String>())
            }
            bindings_list_req.data.each { abinding ->
                if (abinding.source.equals(""))
                    abinding.source = ExchangeFromRabbitREST.RABBITMQ_DEFAULT_EXCH_NAME
                ret.get((String)abinding.vhost).add((String)abinding.source +
                        "-[" + (String)abinding.destination_type + "/{" + (String)abinding.routing_key + "," + (String)abinding.properties_key + "}]->"
                        + (String)abinding.destination)
            }
        }
        return ret;
    }

    static Map<String, List<String>> getBindingNames(RabbitClusterToConnect cluster) {
        Map<String,List<String>> ret = new HashMap<String, List<String>>()
        def bindings_list_req = cluster.get('/api/bindings')
        if (bindings_list_req.status == 200 && bindings_list_req.data != null) {
            bindings_list_req.data.each { abinding ->
                if (ret.get(abinding.vhost)==null)
                    ret.put((String)abinding.vhost, new ArrayList<String>())
            }
            bindings_list_req.data.each { abinding ->
                if (abinding.source.equals(""))
                    abinding.source = ExchangeFromRabbitREST.RABBITMQ_DEFAULT_EXCH_NAME
                ret.get((String)abinding.vhost).add((String)abinding.source +
                        "-[" + (String)abinding.destination_type + "/{" + (String)abinding.routing_key + "," + (String)abinding.properties_key + "}]->"
                        + (String)abinding.destination)
            }
        }
        return ret;
    }

    static Map<String,List<String>> getQueueNames(RabbitNodeToConnect node) {
        Map<String,List<String>> ret = new HashMap<String, List<String>>()
        def queues_list_req = node.getRestCli().get(path:'/api/queues')
        if (queues_list_req.status == 200 && queues_list_req.data != null) {
            queues_list_req.data.each { aqueue ->
                if (ret.get(aqueue.vhost)==null)
                    ret.put((String)aqueue.vhost, new ArrayList<String>())
            }
            queues_list_req.data.each { aqueue ->
                ret.get((String)aqueue.vhost).add((String)aqueue.name)
            }
        }
        return ret;
    }

    static Map<String,List<String>> getQueueNames(RabbitClusterToConnect cluster) {
        Map<String,List<String>> ret = new HashMap<String, List<String>>()
        def queues_list_req = cluster.get('/api/queues')
        if (queues_list_req.status == 200 && queues_list_req.data != null) {
            queues_list_req.data.each { aqueue ->
                if (ret.get(aqueue.vhost)==null)
                    ret.put((String)aqueue.vhost, new ArrayList<String>())
            }
            queues_list_req.data.each { aqueue ->
                ret.get((String)aqueue.vhost).add((String)aqueue.name)
            }
        }
        return ret;
    }

    static List<String> getVhostNames(RabbitNodeToConnect node) {
        Map<String,List<String>> ret = new HashMap<String, List<String>>()
        def queues_list_req = node.getRestCli().get(path:'/api/queues')
        if (queues_list_req.status == 200 && queues_list_req.data != null) {
            queues_list_req.data.each { aqueue ->
                if (ret.get(aqueue.vhost)==null)
                    ret.put((String)aqueue.vhost, new ArrayList<String>())
            }
            queues_list_req.data.each { aqueue ->
                ret.get((String)aqueue.vhost).add((String)aqueue.name)
            }
        }
        return ret;
    }

    static List<String> getVhostNames(RabbitClusterToConnect cluster) {
        List<String> ret = new ArrayList<String>()
        def vhosts_list_req = cluster.get('/api/vhosts')
        if (vhosts_list_req.status == 200 && vhosts_list_req.data != null) {
            vhosts_list_req.data.each { avhost ->
                ret.add((String)avhost.name);
            }
        }
        return ret;
    }
}