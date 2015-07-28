<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    {% block attributes %}
    <parent>
        <groupId>{{ groupId }}</groupId>
        <artifactId>{{ artifactId }}</artifactId>
        <version>{{ version }}</version>
        <relativePath>..</relativePath>
    </parent>

    <groupId>{{ artifactId }}</groupId>
    <artifactId>{{ artifactId }}.{{ name }}</artifactId>
    <name>Ariane Community Plugin For RabbitMQ - Injector</name>
    <packaging>bundle</packaging>
    {% endblock %}

    <dependencies>
        <!-- Internal -->
        <dependency>
            <groupId>net.echinopsii.ariane.community.core.portal</groupId>
            <artifactId>net.echinopsii.ariane.community.core.portal.base</artifactId>
            <version>${version.net.echinopsii.ariane.community.core.portal}</version>
            <scope>provided</scope>
        </dependency>
        <dependency>
            <groupId>net.echinopsii.ariane.community.core.injector</groupId>
            <artifactId>net.echinopsii.ariane.community.core.injector.base</artifactId>
            <version>${version.net.echinopsii.ariane.community.core.injector}</version>
            <scope>provided</scope>
        </dependency>
        <dependency>
            <groupId>net.echinopsii.ariane.community.core.mapping.ds</groupId>
            <artifactId>net.echinopsii.ariane.community.core.mapping.ds.api</artifactId>
            <version>${version.net.echinopsii.ariane.community.core.mapping}</version>
            <scope>provided</scope>
        </dependency>
        <dependency>
            <groupId>net.echinopsii.ariane.community.plugin.rabbitmq</groupId>
            <artifactId>net.echinopsii.ariane.community.plugin.rabbitmq.directory</artifactId>
            <version>${project.version}</version>
            <scope>provided</scope>
        </dependency>
        <dependency>
            <groupId>net.echinopsii.ariane.community.plugin.rabbitmq</groupId>
            <artifactId>net.echinopsii.ariane.community.plugin.rabbitmq.jsonparser</artifactId>
            <version>${project.version}</version>
            <scope>provided</scope>
        </dependency>
    </dependencies>

    <build>
        <plugins>

            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-compiler-plugin</artifactId>
                <configuration>
                    <source>${version.java.source}</source>
                    <target>${version.java.target}</target>
                    <showDeprecation>true</showDeprecation>
                    <showWarnings>true</showWarnings>
                    <optimize>true</optimize>
                </configuration>
            </plugin>

            <plugin>
                <!-- This plugin takes care of packaging the artifact as an OSGi Bundle -->
                <groupId>org.apache.felix</groupId>
                <artifactId>maven-bundle-plugin</artifactId>
                <configuration>
                    <instructions>
                        <Export-Package>
                            net.echinopsii.ariane.community.plugin.rabbitmq.injector;version=${project.version},
                            net.echinopsii.ariane.community.plugin.rabbitmq.injector.cache;version=${project.version},
                            net.echinopsii.ariane.community.plugin.rabbitmq.injector.controller;version=${project.version},
                            net.echinopsii.ariane.community.plugin.rabbitmq.injector.runtime.gears;version=${project.version}
                        </Export-Package>
                    </instructions>
                </configuration>
            </plugin>

            <plugin>
                <groupId>org.apache.felix</groupId>
                <artifactId>maven-ipojo-plugin</artifactId>
                <extensions>true</extensions>
                <executions>
                    <execution>
                        <goals>
                            <goal>ipojo-bundle</goal>
                        </goals>
                    </execution>
                </executions>
            </plugin>

        </plugins>
    </build>
</project>
