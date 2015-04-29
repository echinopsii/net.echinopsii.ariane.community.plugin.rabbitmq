<?xml version="1.0" encoding="UTF-8"?>
<infinispan
        xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
        xsi:schemaLocation="urn:infinispan:config:6.0 http://www.infinispan.org/schemas/infinispan-config-6.0.xsd"
        xmlns="urn:infinispan:config:6.0">

   <global>
      <globalJmxStatistics enabled="true" cacheManagerName="ARIANE_PLUGIN_RABBITMQ_GEARS_CACHE_MGR"/>
   </global>

    <namedCache name="ariane.community.plugin.rabbitmq.gears.cache">
        <eviction strategy="LRU" maxEntries="2000"/>
        <persistence passivation="true">
            <singleFile fetchPersistentState="true"
                        ignoreModifications="false"
                        purgeOnStartup="false" 
			location="##rabbitmqInjectorGearsCacheDir">
                <async enabled="true"/>
            </singleFile>
        </persistence>
    </namedCache>
</infinispan>
