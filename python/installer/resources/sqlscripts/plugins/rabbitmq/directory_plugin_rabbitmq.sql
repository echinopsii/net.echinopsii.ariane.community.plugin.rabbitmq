--
-- Table structure for table `tibcorvComponent`
--

CREATE TABLE IF NOT EXISTS `rabbitmqComponent` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `description` varchar(255) DEFAULT NULL,
  `rabbitmqComponentName` varchar(255) DEFAULT NULL,
  `passwd` varchar(255) DEFAULT NULL,
  `rabbitmqComponentURL` varchar(255) DEFAULT NULL,
  `user` varchar(255) DEFAULT NULL,
  `version` int(11) DEFAULT NULL,
  `osInstance_id` bigint(20) DEFAULT NULL,
  `supportTeam_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_4j6jq1wnsw9we9r2j6yrcp16y` (`rabbitmqComponentName`,`rabbitmqComponentURL`),
  UNIQUE KEY `UK_q5xhx6dg8jbhxg7lh8vafsk1w` (`rabbitmqComponentName`),
  UNIQUE KEY `UK_kpws50q7tb4rn0wv6jickw7c2` (`rabbitmqComponentURL`),
  KEY `FK_k7tshoh8ml1si2hbef42rgce5` (`osInstance_id`),
  KEY `FK_q4w1r29c0rmtv7s0pk5brm2wt` (`supportTeam_id`),
  CONSTRAINT `FK_k7tshoh8ml1si2hbef42rgce5` FOREIGN KEY (`osInstance_id`) REFERENCES `osInstance` (`id`),
  CONSTRAINT `FK_q4w1r29c0rmtv7s0pk5brm2wt` FOREIGN KEY (`supportTeam_id`) REFERENCES `team` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=latin1;