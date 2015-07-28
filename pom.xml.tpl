<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
        <!-- General information -->
{% block attributes %}
    <groupId>{{ groupId }}</groupId>
    <artifactId>{{ artifactId }}</artifactId>
    <version>{{ version }}</version>
    <name>Ariane Community Plugin For RabbitMQ</name>
    <packaging>{{ packaging }}</packaging>
{% endblock %}
{% block modules %}
    <modules>
        {%- for mod in modules %}
        <module>{{mod.name}}</module>
        {% endfor -%}
    </modules>
{% endblock %}
 <repositories>
        <repository>
            <id>nexus.echinopsii.net</id>
            <name>echinopsii.net repository</name>
            <url>http://nexus.echinopsii.net/nexus/content/groups/public/</url>
            <releases>
                <enabled>true</enabled>
            </releases>
            <snapshots>
                <enabled>true</enabled>
                <updatePolicy>always</updatePolicy>
            </snapshots>
        </repository>
    </repositories>

    <distributionManagement>
        <repository>
            <id>echinopsii.community</id>
            <url>http://nexus.echinopsii.net/nexus/content/repositories/echinopsii.community.releases</url>
        </repository>
        <snapshotRepository>
            <id>echinopsii.community</id>
            <url>http://nexus.echinopsii.net/nexus/content/repositories/echinopsii.community.snapshots</url>
        </snapshotRepository>
    </distributionManagement>


    <properties>
        <!-- Encoding -->
        <file.encoding>UTF-8</file.encoding>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
        <project.reporting.outputEncoding>UTF-8</project.reporting.outputEncoding>

        <!-- Compiler java version -->
        <version.java.source>1.7</version.java.source>
        <version.java.target>1.7</version.java.target>

        <!-- Build instructions versions -->
        <version.clean.plugin>2.5</version.clean.plugin>
        <version.compiler.plugin>3.1</version.compiler.plugin>
        <version.deploy.plugin>2.7</version.deploy.plugin>
        <version.dependency.plugin>2.8</version.dependency.plugin>
        <version.install.plugin>2.4</version.install.plugin>
        <version.resources.plugin>2.6</version.resources.plugin>
        <version.surefire.plugin>2.15</version.surefire.plugin>
        <version.bundle.plugin>2.4.0</version.bundle.plugin>
        <version.jar.plugin>2.4</version.jar.plugin>
        <version.javadoc.plugin>2.9</version.javadoc.plugin>
        <version.antrun.plugin>1.7</version.antrun.plugin>
        <version.assembly.plugin>2.4</version.assembly.plugin>
        <version.release.plugin>2.4</version.release.plugin>
        <version.source.plugin>2.2.1</version.source.plugin>
        <version.build.helper.plugin>1.8</version.build.helper.plugin>
        <version.license.plugin>1.5</version.license.plugin>
        <version.war.plugin>2.1.1</version.war.plugin>
        <version.ipojo.plugin>1.11.0</version.ipojo.plugin>

{% block dependencies -%}
    <!-- Ariane module dependencies -->
    {% for d in dependencies -%}
    {% if d.module.type == 'none' %}
        <version.net.echinopsii.ariane.community.{{ d.module.name }}>{{ d.version }}</version.net.echinopsii.ariane.community.{{ d.module.name }}>
        <version.net.echinopsii.ariane.community.{{ d.module.name }}.min>{{ d.version_min }} </version.net.echinopsii.ariane.community.{{ d.module.name }}.min>
        <version.net.echinopsii.ariane.community.{{ d.module.name }}.max>{{ d.version_max }} </version.net.echinopsii.ariane.community.{{ d.module.name }}.max>
    {% else %}
        <version.net.echinopsii.ariane.community.{{ d.module.type}}.{{ d.module.name }}>{{ d.version }}</version.net.echinopsii.ariane.community.{{ d.module.type}}.{{ d.module.name }}>
        <version.net.echinopsii.ariane.community.{{ d.module.type}}.{{ d.module.name }}.min>{{ d.version_min }}</version.net.echinopsii.ariane.community.{{ d.module.type}}.{{ d.module.name }}.min>
        <version.net.echinopsii.ariane.community.{{ d.module.type}}.{{ d.module.name }}.max>{{ d.version_max }}</version.net.echinopsii.ariane.community.{{ d.module.type}}.{{ d.module.name }}.max>
    {% endif %}
    {%- endfor %}
{%- endblock %}


        <!-- Dependencies versions-->
               <version.com.fasterxml.jackson>2.1.2</version.com.fasterxml.jackson>
               <version.com.typesafe.akka>2.3.4</version.com.typesafe.akka>
               <version.org.ccil.cowan.tagsoup>1.2</version.org.ccil.cowan.tagsoup>
               <version.org.codehaus.groovy>2.1.6</version.org.codehaus.groovy>
               <version.org.codehaus.groovy.http-builder>0.7</version.org.codehaus.groovy.http-builder>
               <version.commons.fileupload>1.3</version.commons.fileupload>
               <version.commons.io>2.4</version.commons.io>
               <version.com.lowagie.itext>2.1.7</version.com.lowagie.itext>
               <version.com.sun.faces.jsf-api>2.1.26</version.com.sun.faces.jsf-api>
               <version.com.sun.faces.jsf-impl>2.1.26</version.com.sun.faces.jsf-impl>
               <version.javax.el>2.2</version.javax.el>
               <version.javax.annotations.jsr250-api>1.0</version.javax.annotations.jsr250-api>
               <version.javax.faces.api>2.1</version.javax.faces.api>
               <version.javax.servlet.jsp.jsp-api>2.2</version.javax.servlet.jsp.jsp-api>
               <version.javax.servlet.jstl>1.2</version.javax.servlet.jstl>
               <version.javax.servlet.servlet-api>2.5</version.javax.servlet.servlet-api>
               <version.org.apache.poi>3.9</version.org.apache.poi>
               <version.org.apache.shiro>1.2.2</version.org.apache.shiro>
               <version.org.atmosphere>2.0.0</version.org.atmosphere>
               <version.org.glassfish.web.el-impl>2.2.1-b05</version.org.glassfish.web.el-impl>
               <version.org.jboss.resteasy>2.3.6.Final</version.org.jboss.resteasy>
               <version.org.primefaces.primefaces>3.5</version.org.primefaces.primefaces>
               <version.org.primefaces.extensions.primefaces-extension>0.7.1</version.org.primefaces.extensions.primefaces-extension>
               <version.org.primefaces.extensions.resources-codemirror>0.7.1</version.org.primefaces.extensions.resources-codemirror>
               <version.org.primefaces.themes.all-themes>1.0.10</version.org.primefaces.themes.all-themes>
               <version.rome.rome>1.0</version.rome.rome>

               <!-- Technical stuff -->
               <version.org.apache.ipojo>1.11.0</version.org.apache.ipojo>
               <version.org.slf4j>1.6.4</version.org.slf4j>

               <!-- JPA / JTA / Hibernate-->
               <version.javax.transaction.jta>1.1</version.javax.transaction.jta>
               <version.javax.validation.validation-api>1.0.0.GA</version.javax.validation.validation-api>
               <version.com.h2db>1.3.170</version.com.h2db>
               <version.org.hibernate>4.3.0.Final</version.org.hibernate>
               <version.org.hibernate.javax.persistence.hibernate-jpa-2.1-api>1.0.0.Final</version.org.hibernate.javax.persistence.hibernate-jpa-2.1-api>

               <!-- jUnit test version -->
               <version.junit.junit>4.11</version.junit.junit>

               <!-- OSGI versions -->
               <version.osgimin.org.osgi.framework>1.5</version.osgimin.org.osgi.framework>
               <version.osgimax.org.osgi.framework>2</version.osgimax.org.osgi.framework>

               <version.osgimin.org.osgi.util.tracker>1.4</version.osgimin.org.osgi.util.tracker>
               <version.osgimax.org.osgi.util.tracker>2</version.osgimax.org.osgi.util.tracker>

               <version.osgimin.org.osgi.service.cm>1.3.0</version.osgimin.org.osgi.service.cm>
               <version.osgimax.org.osgi.service.cm>2</version.osgimax.org.osgi.service.cm>

               <version.osgimin.org.jboss.resteasy>2.3.6</version.osgimin.org.jboss.resteasy>
               <version.osgimax.org.jboss.resteasy>2.4</version.osgimax.org.jboss.resteasy>

               <version.osgimin.javax.persistence>2.0.0</version.osgimin.javax.persistence>
               <version.osgimax.javax.persistence>3.0.0</version.osgimax.javax.persistence>

               <version.osgimin.hibernate>4.2.8</version.osgimin.hibernate>
               <version.osgimax.hibernate>5.0.0</version.osgimax.hibernate>

           </properties>

           <build>
               <pluginManagement>
                   <plugins>
                       <plugin>
                           <groupId>org.apache.felix</groupId>
                           <artifactId>maven-bundle-plugin</artifactId>
                           <version>${version.bundle.plugin}</version>
                           <extensions>true</extensions>
                       </plugin>
                       <plugin>
                           <groupId>org.apache.maven.plugins</groupId>
                           <artifactId>maven-antrun-plugin</artifactId>
                           <version>${version.antrun.plugin}</version>
                       </plugin>
                       <plugin>
                           <groupId>org.apache.maven.plugins</groupId>
                           <artifactId>maven-assembly-plugin</artifactId>
                           <version>${version.assembly.plugin}</version>
                       </plugin>
                       <plugin>
                           <groupId>org.apache.maven.plugins</groupId>
                           <artifactId>maven-dependency-plugin</artifactId>
                           <version>${version.dependency.plugin}</version>
                       </plugin>
                       <plugin>
                           <groupId>org.apache.maven.plugins</groupId>
                           <artifactId>maven-clean-plugin</artifactId>
                           <version>${version.clean.plugin}</version>
                       </plugin>
                       <plugin>
                           <groupId>org.apache.maven.plugins</groupId>
                           <artifactId>maven-compiler-plugin</artifactId>
                           <version>${version.compiler.plugin}</version>
                       </plugin>
                       <plugin>
                           <groupId>org.apache.maven.plugins</groupId>
                           <artifactId>maven-deploy-plugin</artifactId>
                           <version>${version.deploy.plugin}</version>
                       </plugin>
                       <plugin>
                           <groupId>org.apache.maven.plugins</groupId>
                           <artifactId>maven-install-plugin</artifactId>
                           <version>${version.install.plugin}</version>
                       </plugin>
                       <plugin>
                           <groupId>org.apache.maven.plugins</groupId>
                           <artifactId>maven-jar-plugin</artifactId>
                           <version>${version.jar.plugin}</version>
                       </plugin>
                       <plugin>
                           <groupId>org.apache.maven.plugins</groupId>
                           <artifactId>maven-javadoc-plugin</artifactId>
                           <version>${version.javadoc.plugin}</version>
                       </plugin>
                       <plugin>
                           <groupId>org.apache.maven.plugins</groupId>
                           <artifactId>maven-release-plugin</artifactId>
                           <version>${version.release.plugin}</version>
                       </plugin>
                       <plugin>
                           <groupId>org.apache.maven.plugins</groupId>
                           <artifactId>maven-resources-plugin</artifactId>
                           <version>${version.resources.plugin}</version>
                       </plugin>
                       <plugin>
                           <groupId>org.apache.maven.plugins</groupId>
                           <artifactId>maven-source-plugin</artifactId>
                           <version>${version.source.plugin}</version>
                       </plugin>
                       <plugin>
                           <groupId>org.apache.maven.plugins</groupId>
                           <artifactId>maven-surefire-plugin</artifactId>
                           <version>${version.surefire.plugin}</version>
                       </plugin>
                       <plugin>
                           <groupId>org.codehaus.mojo</groupId>
                           <artifactId>build-helper-maven-plugin</artifactId>
                           <version>${version.build.helper.plugin}</version>
                       </plugin>
                       <plugin>
                           <groupId>org.codehaus.mojo</groupId>
                           <artifactId>license-maven-plugin</artifactId>
                           <version>${version.license.plugin}</version>
                       </plugin>
                       <plugin>
                           <groupId>org.apache.maven.plugins</groupId>
                           <artifactId>maven-war-plugin</artifactId>
                           <version>${version.war.plugin}</version>
                       </plugin>
                       <plugin>
                           <groupId>org.jboss.as.plugins</groupId>
                           <artifactId>jboss-as-maven-plugin</artifactId>
                           <version>${version.jboss.maven.plugin}</version>
                       </plugin>
                       <plugin>
                           <groupId>org.apache.felix</groupId>
                           <artifactId>maven-ipojo-plugin</artifactId>
                           <version>${version.ipojo.plugin}</version>
                       </plugin>
                   </plugins>
               </pluginManagement>
           </build>

           <dependencies>
               <dependency>
                   <groupId>com.fasterxml.jackson.core</groupId>
                   <artifactId>jackson-core</artifactId>
                   <scope>provided</scope>
                   <version>${version.com.fasterxml.jackson}</version>
               </dependency>
               <dependency>
                   <groupId>com.fasterxml.jackson.core</groupId>
                   <artifactId>jackson-databind</artifactId>
                   <scope>provided</scope>
                   <version>${version.com.fasterxml.jackson}</version>
               </dependency>
               <dependency>
                   <groupId>com.typesafe.akka</groupId>
                   <artifactId>akka-actor_2.10</artifactId>
                   <version>${version.com.typesafe.akka}</version>
                   <scope>provided</scope>
               </dependency>
               <dependency>
                   <groupId>com.typesafe.akka</groupId>
                   <artifactId>akka-osgi_2.10</artifactId>
                   <version>${version.com.typesafe.akka}</version>
                   <scope>provided</scope>
               </dependency>
               <dependency>
                   <groupId>commons-fileupload</groupId>
                   <artifactId>commons-fileupload</artifactId>
                   <scope>provided</scope>
                   <version>${version.commons.fileupload}</version>
               </dependency>
               <dependency>
                   <groupId>commons-io</groupId>
                   <artifactId>commons-io</artifactId>
                   <scope>provided</scope>
                   <version>${version.commons.io}</version>
               </dependency>
               <dependency>
                   <groupId>com.h2database</groupId>
                   <artifactId>h2</artifactId>
                   <version>${version.com.h2db}</version>
                   <scope>provided</scope>
               </dependency>
               <dependency>
                   <groupId>com.lowagie</groupId>
                   <artifactId>itext</artifactId>
                   <scope>provided</scope>
                   <version>${version.com.lowagie.itext}</version>
               </dependency>
               <dependency>
                   <groupId>com.sun.faces</groupId>
                   <artifactId>jsf-api</artifactId>
                   <exclusions>
                       <exclusion>
                           <groupId>jstl</groupId>
                           <artifactId>jstl</artifactId>
                       </exclusion>
                   </exclusions>
                   <version>${version.com.sun.faces.jsf-api}</version>
                   <scope>provided</scope>
               </dependency>
               <dependency>
                   <groupId>com.sun.faces</groupId>
                   <artifactId>jsf-impl</artifactId>
                   <exclusions>
                       <exclusion>
                           <groupId>jstl</groupId>
                           <artifactId>jstl</artifactId>
                       </exclusion>
                   </exclusions>
                   <version>${version.com.sun.faces.jsf-impl}</version>
                   <scope>provided</scope>
               </dependency>
               <dependency>
                   <groupId>javax.annotation</groupId>
                   <artifactId>jsr250-api</artifactId>
                   <scope>provided</scope>
                   <version>${version.javax.annotations.jsr250-api}</version>
               </dependency>
               <dependency>
                   <groupId>javax.el</groupId>
                   <artifactId>el-api</artifactId>
                   <scope>provided</scope>
                   <version>${version.javax.el}</version>
               </dependency>
               <dependency>
                   <groupId>javax.servlet</groupId>
                   <artifactId>jstl</artifactId>
                   <scope>provided</scope>
                   <version>${version.javax.servlet.jstl}</version>
               </dependency>
               <dependency>
                   <groupId>javax.servlet.jsp</groupId>
                   <artifactId>jsp-api</artifactId>
                   <scope>provided</scope>
                   <version>${version.javax.servlet.jsp.jsp-api}</version>
               </dependency>
               <dependency>
                   <groupId>javax.servlet</groupId>
                   <artifactId>servlet-api</artifactId>
                   <scope>provided</scope>
                   <version>${version.javax.servlet.servlet-api}</version>
               </dependency>
               <dependency>
                   <groupId>javax.transaction</groupId>
                   <artifactId>jta</artifactId>
                   <version>${version.javax.transaction.jta}</version>
                   <scope>provided</scope>
               </dependency>
               <dependency>
                   <groupId>javax.validation</groupId>
                   <artifactId>validation-api</artifactId>
                   <version>${version.javax.validation.validation-api}</version>
                   <scope>provided</scope>
               </dependency>
               <dependency>
                   <groupId>org.apache.felix</groupId>
                   <artifactId>org.apache.felix.ipojo</artifactId>
                   <version>${version.org.apache.ipojo}</version>
                   <scope>provided</scope>
               </dependency>
               <dependency>
                   <groupId>org.apache.felix</groupId>
                   <artifactId>org.apache.felix.ipojo.annotations</artifactId>
                   <version>${version.org.apache.ipojo}</version>
                   <scope>provided</scope>
               </dependency>
               <dependency>
                   <groupId>org.apache.poi</groupId>
                   <artifactId>poi</artifactId>
                   <scope>provided</scope>
                   <version>${version.org.apache.poi}</version>
               </dependency>
               <dependency>
                   <groupId>org.apache.shiro</groupId>
                   <artifactId>shiro-core</artifactId>
                   <version>${version.org.apache.shiro}</version>
                   <scope>provided</scope>
               </dependency>
               <dependency>
                   <groupId>org.apache.shiro</groupId>
                   <artifactId>shiro-web</artifactId>
                   <version>${version.org.apache.shiro}</version>
                   <scope>provided</scope>
               </dependency>
               <dependency>
                   <groupId>org.atmosphere</groupId>
                   <artifactId>atmosphere-runtime</artifactId>
                   <version>${version.org.atmosphere}</version>
                   <scope>provided</scope>
               </dependency>
               <dependency>
                   <groupId>org.codehaus.groovy</groupId>
                   <artifactId>groovy-all</artifactId>
                   <version>${version.org.codehaus.groovy}</version>
               </dependency>
               <dependency>
                   <groupId>org.codehaus.groovy.modules.http-builder</groupId>
                   <artifactId>http-builder</artifactId>
                   <version>${version.org.codehaus.groovy.http-builder}</version>
               </dependency>
               <dependency>
                   <groupId>org.ccil.cowan.tagsoup</groupId>
                   <artifactId>tagsoup</artifactId>
                   <version>${version.org.ccil.cowan.tagsoup}</version>
                   <scope>provided</scope>
               </dependency>
               <dependency>
                   <groupId>org.glassfish.web</groupId>
                   <artifactId>el-impl</artifactId>
                   <scope>provided</scope>
                   <version>${version.org.glassfish.web.el-impl}</version>
               </dependency>
               <dependency>
                   <groupId>org.jboss.resteasy</groupId>
                   <artifactId>resteasy-jaxrs</artifactId>
                   <scope>provided</scope>
                   <version>${version.org.jboss.resteasy}</version>
               </dependency>
               <dependency>
                   <groupId>org.hibernate</groupId>
                   <artifactId>hibernate-osgi</artifactId>
                   <version>${version.org.hibernate}</version>
                   <scope>provided</scope>
               </dependency>
               <dependency>
                   <groupId>org.hibernate</groupId>
                   <artifactId>hibernate-entitymanager</artifactId>
                   <version>${version.org.hibernate}</version>
                   <scope>provided</scope>
               </dependency>
               <dependency>
                   <groupId>org.hibernate.javax.persistence</groupId>
                   <artifactId>hibernate-jpa-2.1-api</artifactId>
                   <version>${version.org.hibernate.javax.persistence.hibernate-jpa-2.1-api}</version>
                   <scope>provided</scope>
               </dependency>
               <dependency>
                   <groupId>org.primefaces</groupId>
                   <artifactId>primefaces</artifactId>
                   <scope>provided</scope>
                   <version>${version.org.primefaces.primefaces}</version>
               </dependency>
               <dependency>
                   <groupId>org.slf4j</groupId>
                   <artifactId>slf4j-api</artifactId>
                   <scope>provided</scope>
                   <version>${version.org.slf4j}</version>
               </dependency>
               <dependency>
                   <groupId>rome</groupId>
                   <artifactId>rome</artifactId>
                   <scope>provided</scope>
                   <version>${version.rome.rome}</version>
               </dependency>
               <dependency>
                   <groupId>junit</groupId>
                   <artifactId>junit</artifactId>
                   <version>${version.junit.junit}</version>
                   <scope>test</scope>
               </dependency>
           </dependencies>
       </project>
