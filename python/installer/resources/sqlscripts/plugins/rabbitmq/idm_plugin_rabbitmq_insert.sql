--
-- Dumping data for table `resource`
--
LOCK TABLES `resource` WRITE;
INSERT IGNORE INTO `resource` (description, resourceName, version) VALUES
    ('Directory middleware RabbitMQ Component','dirMdwRabbitMQComponent',1);
UNLOCK TABLES;



--
-- Dumping data for table `permission`
--
LOCK TABLES `permission` WRITE,`resource` WRITE;
INSERT IGNORE INTO `permission` (description, permissionName, version, resource_id)
SELECT 'can display Directory middleware RabbitMQ component', 'dirMdwRabbitMQComponent:display', 1, id FROM resource WHERE resourceName='dirMdwRabbitMQComponent';
INSERT IGNORE INTO `permission` (description, permissionName, version, resource_id)
SELECT 'can create Directory middleware RabbitMQ component', 'dirMdwRabbitMQComponent:create', 1, id FROM resource WHERE resourceName='dirMdwRabbitMQComponent';
INSERT IGNORE INTO `permission` (description, permissionName, version, resource_id)
SELECT 'can update Directory middleware RabbitMQ component', 'dirMdwRabbitMQComponent:update', 1, id FROM resource WHERE resourceName='dirMdwRabbitMQComponent';
INSERT IGNORE INTO `permission` (description, permissionName, version, resource_id)
SELECT 'can remove Directory middleware RabbitMQ component', 'dirMdwRabbitMQComponent:remove', 1, id FROM resource WHERE resourceName='dirMdwRabbitMQComponent';
UNLOCK TABLES;



--
-- Dumping data for table `resource_permission`
--
LOCK TABLES `resource_permission` WRITE,`permission` AS p WRITE,`resource` AS r WRITE ;
INSERT IGNORE INTO `resource_permission` (resource_id, permissions_id)
SELECT r.id, p.id FROM resource AS r, permission AS p WHERE r.resourceName='dirMdwRabbitMQComponent' AND p.permissionName='dirMdwRabbitMQComponent:display';
INSERT IGNORE INTO `resource_permission` (resource_id, permissions_id)
SELECT r.id, p.id FROM resource AS r, permission AS p WHERE r.resourceName='dirMdwRabbitMQComponent' AND p.permissionName='dirMdwRabbitMQComponent:create';
INSERT IGNORE INTO `resource_permission` (resource_id, permissions_id)
SELECT r.id, p.id FROM resource AS r, permission AS p WHERE r.resourceName='dirMdwRabbitMQComponent' AND p.permissionName='dirMdwRabbitMQComponent:update';
INSERT IGNORE INTO `resource_permission` (resource_id, permissions_id)
SELECT r.id, p.id FROM resource AS r, permission AS p WHERE r.resourceName='dirMdwRabbitMQComponent' AND p.permissionName='dirMdwRabbitMQComponent:remove';
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
SELECT p.id, r.id FROM permission AS p, role AS r WHERE p.permissionName='dirMdwRabbitMQComponent:display' AND r.roleName='Jedi';
INSERT IGNORE INTO `permission_role` (permission_id, roles_id)
SELECT p.id, r.id FROM permission AS p, role AS r WHERE p.permissionName='dirMdwRabbitMQComponent:create' AND r.roleName='Jedi';
INSERT IGNORE INTO `permission_role` (permission_id, roles_id)
SELECT p.id, r.id FROM permission AS p, role AS r WHERE p.permissionName='dirMdwRabbitMQComponent:remove' AND r.roleName='Jedi';
INSERT IGNORE INTO `permission_role` (permission_id, roles_id)
SELECT p.id, r.id FROM permission AS p, role AS r WHERE p.permissionName='dirMdwRabbitMQComponent:update' AND r.roleName='Jedi';

INSERT IGNORE INTO `permission_role` (permission_id, roles_id)
SELECT p.id, r.id FROM permission AS p, role AS r WHERE p.permissionName='dirMdwRabbitMQComponent:display' AND r.roleName='mdwrabbitadmin';
INSERT IGNORE INTO `permission_role` (permission_id, roles_id)
SELECT p.id, r.id FROM permission AS p, role AS r WHERE p.permissionName='dirMdwRabbitMQComponent:create' AND r.roleName='mdwrabbitadmin';
INSERT IGNORE INTO `permission_role` (permission_id, roles_id)
SELECT p.id, r.id FROM permission AS p, role AS r WHERE p.permissionName='dirMdwRabbitMQComponent:remove' AND r.roleName='mdwrabbitadmin';
INSERT IGNORE INTO `permission_role` (permission_id, roles_id)
SELECT p.id, r.id FROM permission AS p, role AS r WHERE p.permissionName='dirMdwRabbitMQComponent:update' AND r.roleName='mdwrabbitadmin';

INSERT IGNORE INTO `permission_role` (permission_id, roles_id)
SELECT p.id, r.id FROM permission AS p, role AS r WHERE p.permissionName='dirMdwRabbitMQComponent:display' AND r.roleName='mdwrabbitreviewer';
UNLOCK TABLES;



--
-- Dumping data for table `role_permission`
--
LOCK TABLES `role_permission` WRITE,`permission` AS p WRITE,`role` AS r WRITE;
INSERT IGNORE INTO `role_permission` (role_id, permissions_id)
SELECT r.id, p.id FROM permission AS p, role AS r WHERE p.permissionName='dirMdwRabbitMQComponent:display' AND r.roleName='Jedi';
INSERT IGNORE INTO `role_permission` (role_id, permissions_id)
SELECT r.id, p.id FROM permission AS p, role AS r WHERE p.permissionName='dirMdwRabbitMQComponent:create' AND r.roleName='Jedi';
INSERT IGNORE INTO `role_permission` (role_id, permissions_id)
SELECT r.id, p.id FROM permission AS p, role AS r WHERE p.permissionName='dirMdwRabbitMQComponent:remove' AND r.roleName='Jedi';
INSERT IGNORE INTO `role_permission` (role_id, permissions_id)
SELECT r.id, p.id FROM permission AS p, role AS r WHERE p.permissionName='dirMdwRabbitMQComponent:update' AND r.roleName='Jedi';

INSERT IGNORE INTO `role_permission` (role_id, permissions_id)
SELECT r.id, p.id FROM permission AS p, role AS r WHERE p.permissionName='dirMdwRabbitMQComponent:display' AND r.roleName='mdwrabbitadmin';
INSERT IGNORE INTO `role_permission` (role_id, permissions_id)
SELECT r.id, p.id FROM permission AS p, role AS r WHERE p.permissionName='dirMdwRabbitMQComponent:create' AND r.roleName='mdwrabbitadmin';
INSERT IGNORE INTO `role_permission` (role_id, permissions_id)
SELECT r.id, p.id FROM permission AS p, role AS r WHERE p.permissionName='dirMdwRabbitMQComponent:remove' AND r.roleName='mdwrabbitadmin';
INSERT IGNORE INTO `role_permission` (role_id, permissions_id)
SELECT r.id, p.id FROM permission AS p, role AS r WHERE p.permissionName='dirMdwRabbitMQComponent:update' AND r.roleName='mdwrabbitadmin';

INSERT IGNORE INTO `role_permission` (role_id, permissions_id)
SELECT r.id, p.id FROM permission AS p, role AS r WHERE p.permissionName='dirMdwRabbitMQComponent:display' AND r.roleName='mdwrabbitreviewer';
UNLOCK TABLES;