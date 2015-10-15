--
-- Dumping data for table `resource`
--
LOCK TABLES `resource` WRITE;
INSERT IGNORE INTO `resource` (description, resourceName, version) VALUES
    ('Directory middleware RabbitMQ Cluster','dirMdwRabbitMQCluster',1),
    ('Directory middleware RabbitMQ Node','dirMdwRabbitMQNode',1),
    ('Injector for mapping middleware RabbitMQ','injMapMdwRabbitMQ',1);
UNLOCK TABLES;



--
-- Dumping data for table `permission`
--
LOCK TABLES `permission` WRITE,`resource` WRITE;
INSERT IGNORE INTO `permission` (description, permissionName, version, resource_id)
SELECT 'can display Directory middleware RabbitMQ cluster', 'dirMdwRabbitMQCluster:display', 1, id FROM resource WHERE resourceName='dirMdwRabbitMQCluster';
INSERT IGNORE INTO `permission` (description, permissionName, version, resource_id)
SELECT 'can create Directory middleware RabbitMQ cluster', 'dirMdwRabbitMQCluster:create', 1, id FROM resource WHERE resourceName='dirMdwRabbitMQCluster';
INSERT IGNORE INTO `permission` (description, permissionName, version, resource_id)
SELECT 'can update Directory middleware RabbitMQ cluster', 'dirMdwRabbitMQCluster:update', 1, id FROM resource WHERE resourceName='dirMdwRabbitMQCluster';
INSERT IGNORE INTO `permission` (description, permissionName, version, resource_id)
SELECT 'can remove Directory middleware RabbitMQ cluster', 'dirMdwRabbitMQCluster:remove', 1, id FROM resource WHERE resourceName='dirMdwRabbitMQCluster';
INSERT IGNORE INTO `permission` (description, permissionName, version, resource_id)
SELECT 'can display Directory middleware RabbitMQ component', 'dirMdwRabbitMQNode:display', 1, id FROM resource WHERE resourceName='dirMdwRabbitMQNode';
INSERT IGNORE INTO `permission` (description, permissionName, version, resource_id)
SELECT 'can create Directory middleware RabbitMQ component', 'dirMdwRabbitMQNode:create', 1, id FROM resource WHERE resourceName='dirMdwRabbitMQNode';
INSERT IGNORE INTO `permission` (description, permissionName, version, resource_id)
SELECT 'can update Directory middleware RabbitMQ component', 'dirMdwRabbitMQNode:update', 1, id FROM resource WHERE resourceName='dirMdwRabbitMQNode';
INSERT IGNORE INTO `permission` (description, permissionName, version, resource_id)
SELECT 'can remove Directory middleware RabbitMQ component', 'dirMdwRabbitMQNode:remove', 1, id FROM resource WHERE resourceName='dirMdwRabbitMQNode';
INSERT IGNORE INTO `permission` (description, permissionName, version, resource_id)
SELECT 'can display middleware mapping RabbitMQ injector', 'injMapMdwRabbitMQ:display', 1, id FROM resource WHERE resourceName='injMapMdwRabbitMQ';
INSERT IGNORE INTO `permission` (description, permissionName, version, resource_id)
SELECT 'can play action on middleware mapping RabbitMQ injector', 'injMapMdwRabbitMQ:action', 1, id FROM resource WHERE resourceName='injMapMdwRabbitMQ';
UNLOCK TABLES;



--
-- Dumping data for table `resource_permission`
--
LOCK TABLES `resource_permission` WRITE,`permission` AS p WRITE,`resource` AS r WRITE ;
INSERT IGNORE INTO `resource_permission` (resource_id, permissions_id)
SELECT r.id, p.id FROM resource AS r, permission AS p WHERE r.resourceName='dirMdwRabbitMQCluster' AND p.permissionName='dirMdwRabbitMQCluster:display';
INSERT IGNORE INTO `resource_permission` (resource_id, permissions_id)
SELECT r.id, p.id FROM resource AS r, permission AS p WHERE r.resourceName='dirMdwRabbitMQCluster' AND p.permissionName='dirMdwRabbitMQCluster:create';
INSERT IGNORE INTO `resource_permission` (resource_id, permissions_id)
SELECT r.id, p.id FROM resource AS r, permission AS p WHERE r.resourceName='dirMdwRabbitMQCluster' AND p.permissionName='dirMdwRabbitMQCluster:update';
INSERT IGNORE INTO `resource_permission` (resource_id, permissions_id)
SELECT r.id, p.id FROM resource AS r, permission AS p WHERE r.resourceName='dirMdwRabbitMQCluster' AND p.permissionName='dirMdwRabbitMQCluster:remove';
INSERT IGNORE INTO `resource_permission` (resource_id, permissions_id)
SELECT r.id, p.id FROM resource AS r, permission AS p WHERE r.resourceName='dirMdwRabbitMQNode' AND p.permissionName='dirMdwRabbitMQNode:display';
INSERT IGNORE INTO `resource_permission` (resource_id, permissions_id)
SELECT r.id, p.id FROM resource AS r, permission AS p WHERE r.resourceName='dirMdwRabbitMQNode' AND p.permissionName='dirMdwRabbitMQNode:create';
INSERT IGNORE INTO `resource_permission` (resource_id, permissions_id)
SELECT r.id, p.id FROM resource AS r, permission AS p WHERE r.resourceName='dirMdwRabbitMQNode' AND p.permissionName='dirMdwRabbitMQNode:update';
INSERT IGNORE INTO `resource_permission` (resource_id, permissions_id)
SELECT r.id, p.id FROM resource AS r, permission AS p WHERE r.resourceName='dirMdwRabbitMQNode' AND p.permissionName='dirMdwRabbitMQNode:remove';
INSERT IGNORE INTO `resource_permission` (resource_id, permissions_id)
SELECT r.id, p.id FROM resource AS r, permission AS p WHERE r.resourceName='injMapMdwRabbitMQ' AND p.permissionName='injMapMdwRabbitMQ:display';
INSERT IGNORE INTO `resource_permission` (resource_id, permissions_id)
SELECT r.id, p.id FROM resource AS r, permission AS p WHERE r.resourceName='injMapMdwRabbitMQ' AND p.permissionName='injMapMdwRabbitMQ:action';
UNLOCK TABLES;



--
-- Dumping data for table `role`
--
LOCK TABLES `role` WRITE;
INSERT IGNORE INTO `role` (description, roleName, version) VALUES
    ('middleware RabbitMQ administrator role','mdwrabbitadmin',1),
    ('middleware RabbitMQ administrator role','mdwrabbitreviewer',1);
UNLOCK TABLES;



--
-- Dumping data for table `permission_role`
--
LOCK TABLES `permission_role` WRITE,`permission` AS p WRITE,`role` AS r WRITE;
INSERT IGNORE INTO `permission_role` (permission_id, roles_id)
SELECT p.id, r.id FROM permission AS p, role AS r WHERE p.permissionName='dirMdwRabbitMQCluster:display' AND r.roleName='Jedi';
INSERT IGNORE INTO `permission_role` (permission_id, roles_id)
SELECT p.id, r.id FROM permission AS p, role AS r WHERE p.permissionName='dirMdwRabbitMQCluster:create' AND r.roleName='Jedi';
INSERT IGNORE INTO `permission_role` (permission_id, roles_id)
SELECT p.id, r.id FROM permission AS p, role AS r WHERE p.permissionName='dirMdwRabbitMQCluster:remove' AND r.roleName='Jedi';
INSERT IGNORE INTO `permission_role` (permission_id, roles_id)
SELECT p.id, r.id FROM permission AS p, role AS r WHERE p.permissionName='dirMdwRabbitMQCluster:update' AND r.roleName='Jedi';
INSERT IGNORE INTO `permission_role` (permission_id, roles_id)
SELECT p.id, r.id FROM permission AS p, role AS r WHERE p.permissionName='dirMdwRabbitMQNode:display' AND r.roleName='Jedi';
INSERT IGNORE INTO `permission_role` (permission_id, roles_id)
SELECT p.id, r.id FROM permission AS p, role AS r WHERE p.permissionName='dirMdwRabbitMQNode:create' AND r.roleName='Jedi';
INSERT IGNORE INTO `permission_role` (permission_id, roles_id)
SELECT p.id, r.id FROM permission AS p, role AS r WHERE p.permissionName='dirMdwRabbitMQNode:remove' AND r.roleName='Jedi';
INSERT IGNORE INTO `permission_role` (permission_id, roles_id)
SELECT p.id, r.id FROM permission AS p, role AS r WHERE p.permissionName='dirMdwRabbitMQNode:update' AND r.roleName='Jedi';
INSERT IGNORE INTO `permission_role` (permission_id, roles_id)
SELECT p.id, r.id FROM permission AS p, role AS r WHERE p.permissionName='injMapMdwRabbitMQ:display' AND r.roleName='Jedi';
INSERT IGNORE INTO `permission_role` (permission_id, roles_id)
SELECT p.id, r.id FROM permission AS p, role AS r WHERE p.permissionName='injMapMdwRabbitMQ:action' AND r.roleName='Jedi';

INSERT IGNORE INTO `permission_role` (permission_id, roles_id)
SELECT p.id, r.id FROM permission AS p, role AS r WHERE p.permissionName='dirMdwRabbitMQCluster:display' AND r.roleName='mdwrabbitadmin';
INSERT IGNORE INTO `permission_role` (permission_id, roles_id)
SELECT p.id, r.id FROM permission AS p, role AS r WHERE p.permissionName='dirMdwRabbitMQCluster:create' AND r.roleName='mdwrabbitadmin';
INSERT IGNORE INTO `permission_role` (permission_id, roles_id)
SELECT p.id, r.id FROM permission AS p, role AS r WHERE p.permissionName='dirMdwRabbitMQCluster:remove' AND r.roleName='mdwrabbitadmin';
INSERT IGNORE INTO `permission_role` (permission_id, roles_id)
SELECT p.id, r.id FROM permission AS p, role AS r WHERE p.permissionName='dirMdwRabbitMQCluster:update' AND r.roleName='mdwrabbitadmin';
INSERT IGNORE INTO `permission_role` (permission_id, roles_id)
SELECT p.id, r.id FROM permission AS p, role AS r WHERE p.permissionName='dirMdwRabbitMQNode:display' AND r.roleName='mdwrabbitadmin';
INSERT IGNORE INTO `permission_role` (permission_id, roles_id)
SELECT p.id, r.id FROM permission AS p, role AS r WHERE p.permissionName='dirMdwRabbitMQNode:create' AND r.roleName='mdwrabbitadmin';
INSERT IGNORE INTO `permission_role` (permission_id, roles_id)
SELECT p.id, r.id FROM permission AS p, role AS r WHERE p.permissionName='dirMdwRabbitMQNode:remove' AND r.roleName='mdwrabbitadmin';
INSERT IGNORE INTO `permission_role` (permission_id, roles_id)
SELECT p.id, r.id FROM permission AS p, role AS r WHERE p.permissionName='dirMdwRabbitMQNode:update' AND r.roleName='mdwrabbitadmin';
INSERT IGNORE INTO `permission_role` (permission_id, roles_id)
SELECT p.id, r.id FROM permission AS p, role AS r WHERE p.permissionName='injMapMdwRabbitMQ:display' AND r.roleName='mdwrabbitadmin';
INSERT IGNORE INTO `permission_role` (permission_id, roles_id)
SELECT p.id, r.id FROM permission AS p, role AS r WHERE p.permissionName='injMapMdwRabbitMQ:action' AND r.roleName='mdwrabbitadmin';

INSERT IGNORE INTO `permission_role` (permission_id, roles_id)
SELECT p.id, r.id FROM permission AS p, role AS r WHERE p.permissionName='dirMdwRabbitMQCluster:display' AND r.roleName='mdwrabbitreviewer';
INSERT IGNORE INTO `permission_role` (permission_id, roles_id)
SELECT p.id, r.id FROM permission AS p, role AS r WHERE p.permissionName='dirMdwRabbitMQNode:display' AND r.roleName='mdwrabbitreviewer';
INSERT IGNORE INTO `permission_role` (permission_id, roles_id)
SELECT p.id, r.id FROM permission AS p, role AS r WHERE p.permissionName='injMapMdwRabbitMQ:display' AND r.roleName='mdwrabbitreviewer';
UNLOCK TABLES;



--
-- Dumping data for table `role_permission`
--
LOCK TABLES `role_permission` WRITE,`permission` AS p WRITE,`role` AS r WRITE;
INSERT IGNORE INTO `role_permission` (role_id, permissions_id)
SELECT r.id, p.id FROM permission AS p, role AS r WHERE p.permissionName='dirMdwRabbitMQCluster:display' AND r.roleName='Jedi';
INSERT IGNORE INTO `role_permission` (role_id, permissions_id)
SELECT r.id, p.id FROM permission AS p, role AS r WHERE p.permissionName='dirMdwRabbitMQCluster:create' AND r.roleName='Jedi';
INSERT IGNORE INTO `role_permission` (role_id, permissions_id)
SELECT r.id, p.id FROM permission AS p, role AS r WHERE p.permissionName='dirMdwRabbitMQCluster:remove' AND r.roleName='Jedi';
INSERT IGNORE INTO `role_permission` (role_id, permissions_id)
SELECT r.id, p.id FROM permission AS p, role AS r WHERE p.permissionName='dirMdwRabbitMQCluster:update' AND r.roleName='Jedi';
INSERT IGNORE INTO `role_permission` (role_id, permissions_id)
SELECT r.id, p.id FROM permission AS p, role AS r WHERE p.permissionName='dirMdwRabbitMQNode:display' AND r.roleName='Jedi';
INSERT IGNORE INTO `role_permission` (role_id, permissions_id)
SELECT r.id, p.id FROM permission AS p, role AS r WHERE p.permissionName='dirMdwRabbitMQNode:create' AND r.roleName='Jedi';
INSERT IGNORE INTO `role_permission` (role_id, permissions_id)
SELECT r.id, p.id FROM permission AS p, role AS r WHERE p.permissionName='dirMdwRabbitMQNode:remove' AND r.roleName='Jedi';
INSERT IGNORE INTO `role_permission` (role_id, permissions_id)
SELECT r.id, p.id FROM permission AS p, role AS r WHERE p.permissionName='dirMdwRabbitMQNode:update' AND r.roleName='Jedi';
INSERT IGNORE INTO `role_permission` (role_id, permissions_id)
SELECT r.id, p.id FROM permission AS p, role AS r WHERE p.permissionName='injMapMdwRabbitMQ:display' AND r.roleName='Jedi';
INSERT IGNORE INTO `role_permission` (role_id, permissions_id)
SELECT r.id, p.id FROM permission AS p, role AS r WHERE p.permissionName='injMapMdwRabbitMQ:action' AND r.roleName='Jedi';

INSERT IGNORE INTO `role_permission` (role_id, permissions_id)
SELECT r.id, p.id FROM permission AS p, role AS r WHERE p.permissionName='dirMdwRabbitMQCluster:display' AND r.roleName='mdwrabbitadmin';
INSERT IGNORE INTO `role_permission` (role_id, permissions_id)
SELECT r.id, p.id FROM permission AS p, role AS r WHERE p.permissionName='dirMdwRabbitMQCluster:create' AND r.roleName='mdwrabbitadmin';
INSERT IGNORE INTO `role_permission` (role_id, permissions_id)
SELECT r.id, p.id FROM permission AS p, role AS r WHERE p.permissionName='dirMdwRabbitMQCluster:remove' AND r.roleName='mdwrabbitadmin';
INSERT IGNORE INTO `role_permission` (role_id, permissions_id)
SELECT r.id, p.id FROM permission AS p, role AS r WHERE p.permissionName='dirMdwRabbitMQCluster:update' AND r.roleName='mdwrabbitadmin';
INSERT IGNORE INTO `role_permission` (role_id, permissions_id)
SELECT r.id, p.id FROM permission AS p, role AS r WHERE p.permissionName='dirMdwRabbitMQNode:display' AND r.roleName='mdwrabbitadmin';
INSERT IGNORE INTO `role_permission` (role_id, permissions_id)
SELECT r.id, p.id FROM permission AS p, role AS r WHERE p.permissionName='dirMdwRabbitMQNode:create' AND r.roleName='mdwrabbitadmin';
INSERT IGNORE INTO `role_permission` (role_id, permissions_id)
SELECT r.id, p.id FROM permission AS p, role AS r WHERE p.permissionName='dirMdwRabbitMQNode:remove' AND r.roleName='mdwrabbitadmin';
INSERT IGNORE INTO `role_permission` (role_id, permissions_id)
SELECT r.id, p.id FROM permission AS p, role AS r WHERE p.permissionName='dirMdwRabbitMQNode:update' AND r.roleName='mdwrabbitadmin';
INSERT IGNORE INTO `role_permission` (role_id, permissions_id)
SELECT r.id, p.id FROM permission AS p, role AS r WHERE p.permissionName='injMapMdwRabbitMQ:display' AND r.roleName='mdwrabbitadmin';
INSERT IGNORE INTO `role_permission` (role_id, permissions_id)
SELECT r.id, p.id FROM permission AS p, role AS r WHERE p.permissionName='injMapMdwRabbitMQ:action' AND r.roleName='mdwrabbitadmin';

INSERT IGNORE INTO `role_permission` (role_id, permissions_id)
SELECT r.id, p.id FROM permission AS p, role AS r WHERE p.permissionName='dirMdwRabbitMQCluster:display' AND r.roleName='mdwrabbitreviewer';
INSERT IGNORE INTO `role_permission` (role_id, permissions_id)
SELECT r.id, p.id FROM permission AS p, role AS r WHERE p.permissionName='dirMdwRabbitMQNode:display' AND r.roleName='mdwrabbitreviewer';
INSERT IGNORE INTO `role_permission` (role_id, permissions_id)
SELECT r.id, p.id FROM permission AS p, role AS r WHERE p.permissionName='injMapMdwRabbitMQ:display' AND r.roleName='mdwrabbitreviewer';
INSERT IGNORE INTO `role_permission` (role_id, permissions_id)
SELECT r.id, p.id FROM permission AS p, role AS r WHERE p.permissionName='injMapMdwRabbitMQ:action' AND r.roleName='mdwrabbitreviewer';
UNLOCK TABLES;