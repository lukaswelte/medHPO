#
# SQL Export
# Created by Querious (971)
# Created: 21. April 2015 09:26:29 MESZ
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
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=latin1;


CREATE TABLE `found_term_word` (
  `found_term` int(11) NOT NULL,
  `word` text NOT NULL,
  `id` int(11) NOT NULL AUTO_INCREMENT,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=54 DEFAULT CHARSET=latin1;


CREATE TABLE `found_terms` (
  `info_id` int(11) NOT NULL,
  `term_id` int(11) NOT NULL,
  `custom_id` int(11) NOT NULL,
  `custom_name` text NOT NULL,
  `custom_description` text NOT NULL,
  `id` int(11) NOT NULL AUTO_INCREMENT,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=26 DEFAULT CHARSET=latin1;


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
  FOREIGN KEY (`visit_id`) REFERENCES `Visit` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB AUTO_INCREMENT=20 DEFAULT CHARSET=utf8;


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
  FOREIGN KEY (`patient_id`) REFERENCES `Patient` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8;




SET @PREVIOUS_FOREIGN_KEY_CHECKS = @@FOREIGN_KEY_CHECKS;
SET FOREIGN_KEY_CHECKS = 0;


LOCK TABLES `found_names` WRITE;
ALTER TABLE `found_names` DISABLE KEYS;
INSERT INTO `found_names` (`info_id`, `name`, `id`) VALUES 
	(1,'Peter',1),
	(5,'Peter',2),
	(10,'George',3),
	(15,'Thomas',4),
	(17,'Thomas',5),
	(19,'Thomas',6);
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
	(5,'4',9),
	(5,'fingers',10),
	(6,'4',11),
	(6,'fingers',12),
	(7,'large',13),
	(7,'hands',14),
	(7,'bleeding',15),
	(8,'4',16),
	(8,'fingers',17),
	(9,'peter',18),
	(10,'4',19),
	(10,'fingers',20),
	(11,'4',21),
	(11,'fingers',22),
	(12,'4',23),
	(12,'fingers',24),
	(4,'nose',25),
	(4,'bleeding',26),
	(4,'neck',27),
	(13,'large',28),
	(13,'hands',29),
	(14,'nose',30),
	(14,'bleeding',31),
	(15,'cancer',32),
	(15,'inspection',33),
	(16,'4',34),
	(16,'fingers',35),
	(17,'4',36),
	(17,'fingers',37),
	(18,'skin',38),
	(18,'the',39),
	(19,'large',40),
	(19,'hands',41),
	(20,'nose',42),
	(20,'bleeding',43),
	(21,'A',44),
	(21,'melanoma',45),
	(22,'large',46),
	(22,'hands',47),
	(23,'nose',48),
	(23,'bleeding',49),
	(24,'A',50),
	(24,'melanoma',51),
	(25,'hands',52),
	(25,'suffers',53);
ALTER TABLE `found_term_word` ENABLE KEYS;
UNLOCK TABLES;


LOCK TABLES `found_terms` WRITE;
ALTER TABLE `found_terms` DISABLE KEYS;
INSERT INTO `found_terms` (`info_id`, `term_id`, `custom_id`, `custom_name`, `custom_description`, `id`) VALUES 
	(1,1176,0,'','',1),
	(1,421,0,'','',2),
	(2,1176,0,'','',3),
	(3,6097,0,'','',5),
	(4,6097,0,'','',6),
	(2,0,1422967968,'bleeding large hands','Suffering from bleeding large hands',7),
	(5,6097,0,'','',8),
	(6,659,0,'','',9),
	(8,6097,0,'','',10),
	(9,6097,0,'','',11),
	(11,1176,0,'','',13),
	(11,421,0,'','',14),
	(10,0,1429597297,'cancer','Cancer inspection',15),
	(15,6097,0,'','',16),
	(17,6097,0,'','',17),
	(18,1176,0,'','',19),
	(18,421,0,'','',20),
	(18,2861,0,'','',21),
	(19,1176,0,'','',22),
	(19,421,0,'','',23),
	(19,0,1429599495,'hands','hands suffering',25);
ALTER TABLE `found_terms` ENABLE KEYS;
UNLOCK TABLES;


LOCK TABLES `HPOInfo` WRITE;
ALTER TABLE `HPOInfo` DISABLE KEYS;
INSERT INTO `HPOInfo` (`id`, `visit_id`, `hpo_matches`, `names_found`, `hpo_multiple_matches`, `sentence_detection_and_tokenization_in_ms`, `name_finding_in_ms`, `chunking_in_ms`) VALUES 
	(3,2,NULL,NULL,' sr java.util.ArrayListxa I sizexp    w    x',3,61,2084),
	(4,2,NULL,NULL,' sr java.util.ArrayListxa I sizexp    w    x',2,18,1456),
	(5,2,NULL,NULL,' sr java.util.ArrayListxa I sizexp    w    x',1,5,1387),
	(6,2,NULL,NULL,' sr java.util.ArrayListxa I sizexp    w    x',1702,662,1669),
	(7,2,NULL,NULL,' sr java.util.ArrayListxa I sizexp    w    x',29,35,2091),
	(8,2,NULL,NULL,' sr java.util.ArrayListxa I sizexp    w    x',4,292,2348),
	(9,2,NULL,NULL,' sr java.util.ArrayListxa I sizexp    w    x',2,1944,2002),
	(10,2,NULL,NULL,' sr java.util.ArrayListxa I sizexp    w    x',1,20,1514),
	(12,2,NULL,NULL,' sr java.util.ArrayListxa I sizexp    w    x',7,27,1363),
	(13,2,NULL,NULL,' sr java.util.ArrayListxa I sizexp    w    x',4,10,1426),
	(14,2,NULL,NULL,' sr java.util.ArrayListxa I sizexp    w    x',3,56,1520),
	(15,2,NULL,NULL,' sr java.util.ArrayListxa I sizexp    w    x',3,13,1534),
	(16,2,NULL,NULL,' sr java.util.ArrayListxa I sizexp    w    x',5,78,1393),
	(17,2,NULL,NULL,' sr java.util.ArrayListxa I sizexp   w   sq ~     w   sr Main.model.TermQ J customIDI idL customDescriptiont Ljava/lang/String;L \ncustomNameq ~ L descriptionq ~ L nameq ~ L tagq ~ L wordst Ljava/util/List;xp          epppt Redundant neck skint \nHP:0005989sr java.util.Arrays$ArrayList< [ at [Ljava/lang/Object;xpur [Ljava.lang.String;V{G  xp   t skint thesq ~           &pppt BPyramidal skinfold extending from the base to the top of the nailst \nHP:0009758sq ~ 	q ~ \rxx',7,38,1345),
	(19,1,NULL,NULL,' sr java.util.ArrayListxa I sizexp   w   sq ~     	w   	sr Main.model.TermQ J customIDI idL customDescriptiont Ljava/lang/String;L \ncustomNameq ~ L descriptionq ~ L nameq ~ L tagq ~ L wordst Ljava/util/List;xp          -ppt The presence of a `melanoma` (MPATH:359), a malignant cancer originating from pigment producing melanocytes. Melanoma can originate from the skin or the pigmented layers of the eye (the uvea).t Melanomat \nHP:0002861sr java.util.Arrays$ArrayList< [ at [Ljava/lang/Object;xpur [Ljava.lang.String;V{G  xp   t At melanomasq ~           $pppt Intraocular melanomat \nHP:0007716sq ~ \nq ~ sq ~           /pppt Choroidal melanomat \nHP:0012054sq ~ \nq ~ sq ~           /pppt Ciliary body melanomat \nHP:0012055sq ~ \nq ~ sq ~           /pppt Cutaneous melanomat \nHP:0012056sq ~ \nq ~ sq ~           /pppt Superficial spreading melanomat \nHP:0012057sq ~ \nq ~ sq ~           /pppt Nodular melanomat \nHP:0012058sq ~ \nq ~ sq ~           /pppt Lentigo maligna melanomat \nHP:0012059sq ~ \nq ~ sq ~           /pppt Acral lentiginous melanomat \nHP:0012060sq ~ \nq ~ xx',2,6,1342);
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
	(1,1,'2014-05-16','melanome','Thomas suffers from large hands . He is not able to stop his nose bleeding . A melanoma is at his neck . '),
	(2,2,'2015-02-12','4 fingers','Only having 4 fingers [patient] is not able to grab many things . Under inspection of his arm we found cancer of the skin . ');
ALTER TABLE `Visit` ENABLE KEYS;
UNLOCK TABLES;




SET FOREIGN_KEY_CHECKS = @PREVIOUS_FOREIGN_KEY_CHECKS;


