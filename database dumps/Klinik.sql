#
# SQL Export
# Created by Querious (948)
# Created: 3. Februar 2015 13:54:17 MEZ
# Encoding: Unicode (UTF-8)
#


DROP TABLE IF EXISTS `Visit`;
DROP TABLE IF EXISTS `Patient`;
DROP TABLE IF EXISTS `HPOInfo`;
DROP TABLE IF EXISTS `found_terms`;
DROP TABLE IF EXISTS `found_term_word`;
DROP TABLE IF EXISTS `found_names`;


CREATE TABLE `found_names` (
  `info_id` int(11) NOT NULL,
  `name` text NOT NULL,
  `id` int(11) NOT NULL AUTO_INCREMENT,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=latin1;


CREATE TABLE `found_term_word` (
  `found_term` int(11) NOT NULL,
  `word` text NOT NULL,
  `id` int(11) NOT NULL AUTO_INCREMENT,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=18 DEFAULT CHARSET=latin1;


CREATE TABLE `found_terms` (
  `info_id` int(11) NOT NULL,
  `term_id` int(11) NOT NULL,
  `custom_id` int(11) NOT NULL,
  `custom_name` text NOT NULL,
  `custom_description` text NOT NULL,
  `id` int(11) NOT NULL AUTO_INCREMENT,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=latin1;


CREATE TABLE `HPOInfo` (
  `id` int(11) unsigned NOT NULL AUTO_INCREMENT,
  `visit_id` int(11) unsigned NOT NULL,
  `hpo_matches` text,
  `names_found` text,
  `hpo_multiple_matches` text,
  `sentence_detection_and_tokenization_in_ms` int(11) DEFAULT NULL,
  `name_finding_in_ms` int(11) DEFAULT NULL,
  `chunking_in_ms` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `visit_id` (`visit_id`),
  CONSTRAINT `hpoinfo_ibfk_1` FOREIGN KEY (`visit_id`) REFERENCES `Visit` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8;


CREATE TABLE `Patient` (
  `id` int(11) unsigned NOT NULL AUTO_INCREMENT,
  `birthday` date DEFAULT NULL,
  `gender` tinyint(1) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8;


CREATE TABLE `Visit` (
  `id` int(11) unsigned NOT NULL AUTO_INCREMENT,
  `patient_id` int(11) unsigned NOT NULL,
  `date` date DEFAULT NULL,
  `symptoms` text,
  `additional_text` text,
  PRIMARY KEY (`id`),
  KEY `patient_id` (`patient_id`),
  CONSTRAINT `visit_ibfk_1` FOREIGN KEY (`patient_id`) REFERENCES `Patient` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8;




SET @PREVIOUS_FOREIGN_KEY_CHECKS = @@FOREIGN_KEY_CHECKS;
SET FOREIGN_KEY_CHECKS = 0;


LOCK TABLES `found_names` WRITE;
ALTER TABLE `found_names` DISABLE KEYS;
INSERT INTO `found_names` (`info_id`, `name`, `id`) VALUES 
	(1,'Peter',1),
	(5,'Peter',2);
ALTER TABLE `found_names` ENABLE KEYS;
UNLOCK TABLES;


LOCK TABLES `found_term_word` WRITE;
ALTER TABLE `found_term_word` DISABLE KEYS;
INSERT INTO `found_term_word` (`found_term`, `word`, `id`) VALUES 
	(1,'large',1),
	(1,'hands',2),
	(2,'nose',3),
	(2,'bleeding',4),
	(3,'large',5),
	(3,'hands',6),
	(4,'nose',7),
	(4,'bleeding',8),
	(5,'4',9),
	(5,'fingers',10),
	(6,'4',11),
	(6,'fingers',12),
	(7,'large',13),
	(7,'hands',14),
	(7,'bleeding',15),
	(8,'4',16),
	(8,'fingers',17);
ALTER TABLE `found_term_word` ENABLE KEYS;
UNLOCK TABLES;


LOCK TABLES `found_terms` WRITE;
ALTER TABLE `found_terms` DISABLE KEYS;
INSERT INTO `found_terms` (`info_id`, `term_id`, `custom_id`, `custom_name`, `custom_description`, `id`) VALUES 
	(1,1176,0,'','',1),
	(1,421,0,'','',2),
	(2,1176,0,'','',3),
	(2,421,0,'','',4),
	(3,6097,0,'','',5),
	(4,6097,0,'','',6),
	(2,0,1422967968,'bleeding large hands','Suffering from bleeding large hands',7),
	(5,6097,0,'','',8);
ALTER TABLE `found_terms` ENABLE KEYS;
UNLOCK TABLES;


LOCK TABLES `HPOInfo` WRITE;
ALTER TABLE `HPOInfo` DISABLE KEYS;
INSERT INTO `HPOInfo` (`id`, `visit_id`, `hpo_matches`, `names_found`, `hpo_multiple_matches`, `sentence_detection_and_tokenization_in_ms`, `name_finding_in_ms`, `chunking_in_ms`) VALUES 
	(1,1,NULL,NULL,' sr java.util.ArrayListxa I sizexp    w    x',1461,701,1289),
	(2,1,NULL,NULL,' sr java.util.ArrayListxa I sizexp    w    x',3,9,1273),
	(3,2,NULL,NULL,' sr java.util.ArrayListxa I sizexp    w    x',3,61,2084),
	(4,2,NULL,NULL,' sr java.util.ArrayListxa I sizexp    w    x',2,18,1456),
	(5,2,NULL,NULL,' sr java.util.ArrayListxa I sizexp    w    x',1,5,1387);
ALTER TABLE `HPOInfo` ENABLE KEYS;
UNLOCK TABLES;


LOCK TABLES `Patient` WRITE;
ALTER TABLE `Patient` DISABLE KEYS;
INSERT INTO `Patient` (`id`, `birthday`, `gender`) VALUES 
	(1,'1996-02-10',1),
	(2,'1860-07-11',0);
ALTER TABLE `Patient` ENABLE KEYS;
UNLOCK TABLES;


LOCK TABLES `Visit` WRITE;
ALTER TABLE `Visit` DISABLE KEYS;
INSERT INTO `Visit` (`id`, `patient_id`, `date`, `symptoms`, `additional_text`) VALUES 
	(1,1,'2014-05-16','melanome','[ patient ] suffers from large hands . He is not able to stop his nose bleeding . A melanome is at his neck . '),
	(2,2,'2015-02-12','4 fingers','Only having 4 fingers [patient] is not able to grab many things . Under inspection of his arm we found cancer . ');
ALTER TABLE `Visit` ENABLE KEYS;
UNLOCK TABLES;




SET FOREIGN_KEY_CHECKS = @PREVIOUS_FOREIGN_KEY_CHECKS;


