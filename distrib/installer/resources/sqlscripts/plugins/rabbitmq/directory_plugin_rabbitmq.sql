--
-- Table structure for table `rabbitmqCluster`
--

CREATE TABLE IF NOT EXISTS `rabbitmqCluster` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `description` varchar(255) DEFAULT NULL,
  `rabbitmqClusterName` varchar(255) DEFAULT NULL,
  `version` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_qyf9nbitp86n5gsq3bbqm2fd5` (`rabbitmqClusterName`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=latin1;


--
-- Table structure for table `rabbitmqNode`
--

CREATE TABLE IF NOT EXISTS `rabbitmqNode` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `description` varchar(255) DEFAULT NULL,
  `rabbitmqNodeName` varchar(255) DEFAULT NULL,
  `passwd` varchar(255) DEFAULT NULL,
  `rabbitmqNodeURL` varchar(255) DEFAULT NULL,
  `user` varchar(255) DEFAULT NULL,
  `version` int(11) DEFAULT NULL,
  `cluster_id` bigint(20) DEFAULT NULL,
  `osInstance_id` bigint(20) DEFAULT NULL,
  `supportTeam_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_l9f3ywcc5jm65rrip4944t6lr` (`rabbitmqNodeName`,`rabbitmqNodeURL`),
  UNIQUE KEY `UK_25yajky9j6et1lffh9fehcd2s` (`rabbitmqNodeName`),
  UNIQUE KEY `UK_lrciprchqj9lv05ettlrf1uq5` (`rabbitmqNodeURL`),
  KEY `FK_q64u468bp6bcqx0o63jq3jp09` (`cluster_id`),
  KEY `FK_6lpb2ycv5nunu3d2jf7ibxpwt` (`osInstance_id`),
  KEY `FK_sn3vreqh5jj6xotsuvgqt2gtv` (`supportTeam_id`),
  CONSTRAINT `FK_sn3vreqh5jj6xotsuvgqt2gtv` FOREIGN KEY (`supportTeam_id`) REFERENCES `team` (`id`),
  CONSTRAINT `FK_6lpb2ycv5nunu3d2jf7ibxpwt` FOREIGN KEY (`osInstance_id`) REFERENCES `osInstance` (`id`),
  CONSTRAINT `FK_q64u468bp6bcqx0o63jq3jp09` FOREIGN KEY (`cluster_id`) REFERENCES `rabbitmqCluster` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=latin1;