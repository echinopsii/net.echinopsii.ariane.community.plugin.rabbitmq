{% extends "plan_module_parent_tpl.xml" %}
{% block name %}"net.echinopsii.ariane.community.plugin.{{module.name}}"{% endblock %}
{% block version %}"{{version}}"{% endblock %}
{%- block configuration -%}
<artifact type="configuration" name="net.echinopsii.ariane.community.plugin.rabbitmq.RabbitMQInjectorManagedService"/>
{%- endblock -%}
{% block bundle %}
        {% for s in submodules -%}
        <artifact type="bundle" name="net.echinopsii.ariane.community.plugin.{{module.name}}.{{s.name}}" version="[{{vmin}},{{vmax}})"/>
        {% endfor %}
{% endblock %}
