package net.echinopsii.ariane.community.plugin.rabbitmq.jsonparser

import groovyx.net.http.RESTClient
import net.echinopsii.ariane.community.plugin.rabbitmq.directory.model.RabbitmqCluster

class RabbitRESTTools {

    static List<String> getConnectionNames(RabbitmqCluster cluster) {
        RESTClient client = RESTClientProviderFromRabbitmqCluster.getRESTClientFromCluster(cluster);
        List<String> ret = new ArrayList<String>()
        def connections_list_req = client.get(path : '/api/connections')
        if (connections_list_req.status == 200 && connections_list_req.data != null) {
            connections_list_req.data.each { aconnection ->
                ret.add((String)aconnection.name)
            }
        }
        return ret;
    }

    static List<String> getChannelNames(RabbitmqCluster cluster) {
        RESTClient client = RESTClientProviderFromRabbitmqCluster.getRESTClientFromCluster(cluster);
        List<String> ret = new ArrayList<String>()
        def channels_list_req = client.get(path : '/api/channels')
        if (channels_list_req.status == 200 && channels_list_req.data != null) {
            channels_list_req.data.each { achannel ->
                ret.add((String) achannel.name);
            }
        }
        return ret;
    }

    static Map<String,List<String>> getExchangeNames(RabbitmqCluster cluster) {
        RESTClient client = RESTClientProviderFromRabbitmqCluster.getRESTClientFromCluster(cluster);
        Map<String,List<String>> ret = new HashMap<String,List<String>>()
        def exchanges_list_req = client.get(path : '/api/exchanges')
        if (exchanges_list_req.status == 200 && exchanges_list_req.data != null) {
            exchanges_list_req.data.each { anexchange ->
                if (ret.get(anexchange.vhost)==null)
                    ret.put((String)anexchange.vhost, new ArrayList<String>())
            }
            exchanges_list_req.data.each { anexchange ->
                ret.get((String)anexchange.vhost).add((String)anexchange.name)
            }
        }
        return ret;
    }

    static List<Map<String,String>> getBindings(RabbitmqCluster cluster) {
        RESTClient client = RESTClientProviderFromRabbitmqCluster.getRESTClientFromCluster(cluster);
        List<Map<String,String>> ret = null;
        def bindings_list_req = client.get(path : '/api/bindings')
        if (bindings_list_req.status == 200 && bindings_list_req.data != null) {
            ret = bindings_list_req.data
        }
        return ret;
    }

    static List<String> getQueueNames(RabbitmqCluster cluster) {
        RESTClient client = RESTClientProviderFromRabbitmqCluster.getRESTClientFromCluster(cluster);
        List<String> ret = new ArrayList<String>()
        def queues_list_req = client.get(path : '/api/queues')
        if (queues_list_req.status == 200 && queues_list_req.data != null) {
            queues_list_req.data.each { aqueue ->
                ret.add((String) aqueue.name);
            }
        }
        return ret;
    }

    static List<String> getVhostNames(RabbitmqCluster cluster) {
        RESTClient client = RESTClientProviderFromRabbitmqCluster.getRESTClientFromCluster(cluster);
        List<String> ret = new ArrayList<String>()
        def vhosts_list_req = client.get(path : '/api/vhosts')
        if (vhosts_list_req.status == 200 && vhosts_list_req.data != null) {
            vhosts_list_req.data.each { avhost ->
                ret.add((String)avhost.name);
            }
        }
        return ret;
    }
}