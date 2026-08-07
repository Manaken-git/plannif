-- --------------------------------------------------------
-- Hôte:                         localhost
-- Version du serveur:           12.0.2-MariaDB-ubu2404 - mariadb.org binary distribution
-- SE du serveur:                debian-linux-gnu
-- HeidiSQL Version:             12.12.0.7122
-- --------------------------------------------------------

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET NAMES utf8 */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;


-- Listage de la structure de la base pour planning_ecole
CREATE DATABASE IF NOT EXISTS `planning_ecole` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_uca1400_ai_ci */;
USE `planning_ecole`;

-- Listage de la structure de table planning_ecole. classe
CREATE TABLE IF NOT EXISTS `classe` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `nom` varchar(100) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci;

-- Listage des données de la table planning_ecole.classe : ~6 rows (environ)
INSERT INTO `classe` (`id`, `nom`) VALUES
	(1, 'Term A'),
	(2, 'Seconde 1');

-- Listage de la structure de table planning_ecole. DATABASECHANGELOG
CREATE TABLE IF NOT EXISTS `DATABASECHANGELOG` (
  `ID` varchar(255) NOT NULL,
  `AUTHOR` varchar(255) NOT NULL,
  `FILENAME` varchar(255) NOT NULL,
  `DATEEXECUTED` datetime NOT NULL,
  `ORDEREXECUTED` int(11) NOT NULL,
  `EXECTYPE` varchar(10) NOT NULL,
  `MD5SUM` varchar(35) DEFAULT NULL,
  `DESCRIPTION` varchar(255) DEFAULT NULL,
  `COMMENTS` varchar(255) DEFAULT NULL,
  `TAG` varchar(255) DEFAULT NULL,
  `LIQUIBASE` varchar(20) DEFAULT NULL,
  `CONTEXTS` varchar(255) DEFAULT NULL,
  `LABELS` varchar(255) DEFAULT NULL,
  `DEPLOYMENT_ID` varchar(10) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci;

-- Listage des données de la table planning_ecole.DATABASECHANGELOG : ~21 rows (environ)
INSERT INTO `DATABASECHANGELOG` (`ID`, `AUTHOR`, `FILENAME`, `DATEEXECUTED`, `ORDEREXECUTED`, `EXECTYPE`, `MD5SUM`, `DESCRIPTION`, `COMMENTS`, `TAG`, `LIQUIBASE`, `CONTEXTS`, `LABELS`, `DEPLOYMENT_ID`) VALUES
	('1', 'manaken', 'db/changelog/db.changelog-master.yaml', '2025-10-21 19:50:29', 1, 'EXECUTED', '9:361f1afa1afb956885b198c9feee6bb8', 'createTable tableName=professeur', '', NULL, '5.0.0', NULL, NULL, '1076229020'),
	('2', 'manaken', 'db/changelog/db.changelog-master.yaml', '2025-10-21 19:50:29', 2, 'EXECUTED', '9:9d0240feb393cfe0f9d78f3d16e6cd0c', 'createTable tableName=classe', '', NULL, '5.0.0', NULL, NULL, '1076229020'),
	('3', 'manaken', 'db/changelog/db.changelog-master.yaml', '2025-10-21 19:50:29', 3, 'EXECUTED', '9:f91aff9eec5d0c86928dc5ff6464dec8', 'createTable tableName=matiere', '', NULL, '5.0.0', NULL, NULL, '1076229020'),
	('4', 'manaken', 'db/changelog/db.changelog-master.yaml', '2025-10-21 19:50:29', 4, 'EXECUTED', '9:aa3181e2fdf0288f2177d8ecb84e9666', 'createTable tableName=salle', '', NULL, '5.0.0', NULL, NULL, '1076229020'),
	('5', 'manaken', 'db/changelog/db.changelog-master.yaml', '2025-10-21 19:50:29', 5, 'EXECUTED', '9:e0a6bf3f9f34d178cc9ed3434a470ee7', 'createTable tableName=seance', '', NULL, '5.0.0', NULL, NULL, '1076229020'),
	('6', 'manaken', 'db/changelog/db.changelog-master.yaml', '2025-10-21 19:53:31', 6, 'EXECUTED', '9:e02328da4f6ce004ea34caad20c6df6b', 'createTable tableName=eleve', '', NULL, '5.0.0', NULL, NULL, '1076411229'),
	('7', 'manaken', 'db/changelog/db.changelog-master.yaml', '2025-10-25 13:09:22', 7, 'EXECUTED', '9:c6156795a853077abb44f10b5eda578a', 'addColumn tableName=professeur', '', NULL, '5.0.0', NULL, NULL, '1397761890'),
	('8', 'manaken', 'db/changelog/db.changelog-master.yaml', '2025-11-11 12:54:36', 8, 'EXECUTED', '9:a7396ec200ea2c64cb268e2d1e8443f3', 'addColumn tableName=seance', '', NULL, '5.0.0', NULL, NULL, '2865675914'),
	('9', 'manaken', 'db/changelog/db.changelog-master.yaml', '2025-11-11 12:54:36', 9, 'EXECUTED', '9:b6da06e2ace6896dc0fd9f3b2401e577', 'addColumn tableName=salle', '', NULL, '5.0.0', NULL, NULL, '2865675914'),
	('10', 'manaken', 'db/changelog/db.changelog-master.yaml', '2025-11-11 12:54:36', 10, 'EXECUTED', '9:d1ec72a8262c087a92009897c2cd8abb', 'createTable tableName=tj_distance_salle', '', NULL, '5.0.0', NULL, NULL, '2865675914'),
	('11', 'manaken', 'db/changelog/db.changelog-master.yaml', '2025-11-11 12:54:36', 11, 'EXECUTED', '9:27be2f6e1953ca0171d5bf9a5255487d', 'createTable tableName=t_equipement', '', NULL, '5.0.0', NULL, NULL, '2865675914'),
	('12', 'manaken', 'db/changelog/db.changelog-master.yaml', '2025-11-11 12:54:36', 12, 'EXECUTED', '9:574a51ee6c8779b419132096fffc1917', 'createTable tableName=tj_equipements_salle', '', NULL, '5.0.0', NULL, NULL, '2865675914'),
	('13', 'manaken', 'db/changelog/db.changelog-master.yaml', '2025-11-11 12:54:36', 13, 'EXECUTED', '9:a7e4b79d32a1359c1ff0e65ad42900c6', 'createTable tableName=t_plage_horaire', '', NULL, '5.0.0', NULL, NULL, '2865675914'),
	('14', 'manaken', 'db/changelog/db.changelog-master.yaml', '2025-11-11 12:58:14', 14, 'EXECUTED', '9:9835f23494ee01e3ba5b59aac59bb583', 'addColumn tableName=professeur', '', NULL, '5.0.0', NULL, NULL, '2865894121'),
	('15', 'manaken', 'db/changelog/db.changelog-master.yaml', '2025-11-11 12:58:14', 15, 'EXECUTED', '9:11ecc74066a2dcfa48821bcc12900f74', 'addColumn tableName=matiere', '', NULL, '5.0.0', NULL, NULL, '2865894121'),
	('16', 'manaken', 'db/changelog/db.changelog-master.yaml', '2025-11-11 13:15:08', 16, 'EXECUTED', '9:dba947481f3d7fb1bb92ba628665d52d', 'createTable tableName=t_creneau', '', NULL, '5.0.0', NULL, NULL, '2866907426'),
	('17', 'manaken', 'db/changelog/db.changelog-master.yaml', '2025-11-11 13:15:08', 17, 'EXECUTED', '9:44a04c4a19ecfd06d156358711154918', 'addColumn tableName=seance', '', NULL, '5.0.0', NULL, NULL, '2866907426'),
	('18', 'manaken', 'db/changelog/db.changelog-master.yaml', '2025-11-11 13:15:08', 18, 'EXECUTED', '9:203b72a8df32ae160844fa81c8cb1477', 'dropColumn tableName=seance', '', NULL, '5.0.0', NULL, NULL, '2866907426'),
	('19', 'manaken', 'db/changelog/db.changelog-master.yaml', '2026-01-17 17:23:34', 19, 'EXECUTED', '9:a3ff790a45eee31d8c3e82046fb28f33', 'createTable tableName=t_professeur_dayoff', '', NULL, '4.31.1', NULL, NULL, '8670614237'),
	('20', 'manaken', 'db/changelog/db.changelog-master.yaml', '2026-01-17 17:23:34', 20, 'EXECUTED', '9:e70b60032b491d4a09b900900e37bd0f', 'addColumn tableName=professeur', '', NULL, '4.31.1', NULL, NULL, '8670614237'),
	('21', 'manaken', 'db/changelog/db.changelog-master.yaml', '2026-01-17 17:23:34', 21, 'EXECUTED', '9:c53619ee917ad5def5dfca27ff730c42', 'createTable tableName=t_classe_presence', '', NULL, '4.31.1', NULL, NULL, '8670614237'),
	('22', 'manaken', 'db/changelog/db.changelog-master.yaml', '2026-04-14 19:29:57', 22, 'EXECUTED', '9:0ed25531e6683dfd68c521da968a5f63', 'createTable tableName=tj_professeur_matiere; addPrimaryKey constraintName=pk_professeur_matiere, tableName=tj_professeur_matiere', '', NULL, '4.31.1', NULL, NULL, '6194996776'),
	('23', 'manaken', 'db/changelog/db.changelog-master.yaml', '2026-07-20 20:39:39', 23, 'EXECUTED', '9:50187c0254d96deb90f66dbd7c980ff4', 'createTable tableName=t_matiere_classe_config', '', NULL, '5.0.2', NULL, NULL, '4579978418'),
	('24', 'manaken', 'db/changelog/db.changelog-master.yaml', '2026-07-20 20:39:39', 24, 'EXECUTED', '9:a958973754d0655dc21ea9d7f8d6211c', 'modifyDataType columnName=debut, tableName=t_creneau; modifyDataType columnName=fin, tableName=t_creneau', '', NULL, '5.0.2', NULL, NULL, '4579978418'),
	('25', 'manaken', 'db/changelog/db.changelog-master.yaml', '2026-07-20 20:39:39', 25, 'EXECUTED', '9:6a143127a4e7ae05481c433b1f7a0be6', 'addColumn tableName=seance', '', NULL, '5.0.2', NULL, NULL, '4579978418'),
	('26', 'manaken', 'db/changelog/db.changelog-master.yaml', '2026-07-25 20:19:37', 26, 'EXECUTED', '9:012e9f21dbda0e008affb515cbc31846', 'modifyDataType columnName=debut, tableName=t_creneau; modifyDataType columnName=fin, tableName=t_creneau', '', NULL, '5.0.2', NULL, NULL, '5010775779'),
	('27', 'manaken', 'db/changelog/db.changelog-master.yaml', '2026-07-25 20:19:37', 27, 'EXECUTED', '9:18da8298de7bdf5faead4deef304885c', 'dropColumn tableName=matiere; addColumn tableName=t_matiere_classe_config', '', NULL, '5.0.2', NULL, NULL, '5010775779'),
	('28', 'manaken', 'db/changelog/db.changelog-master.yaml', '2026-07-28 20:21:39', 28, 'EXECUTED', '9:6ac0b4702262a313bdad822f36077ab8', 'createTable tableName=t_vacances', '', NULL, '5.0.2', NULL, NULL, '5270098237'),
	('29', 'manaken', 'db/changelog/db.changelog-master.yaml', '2026-08-06 19:19:04', 29, 'EXECUTED', '9:08594f0a43830268bc079e9401ea7fb1', 'addColumn tableName=t_creneau', '', NULL, '5.0.2', NULL, NULL, '6043943390');

-- Listage de la structure de table planning_ecole. DATABASECHANGELOGLOCK
CREATE TABLE IF NOT EXISTS `DATABASECHANGELOGLOCK` (
  `ID` int(11) NOT NULL,
  `LOCKED` tinyint(1) NOT NULL,
  `LOCKGRANTED` datetime DEFAULT NULL,
  `LOCKEDBY` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci;

-- Listage des données de la table planning_ecole.DATABASECHANGELOGLOCK : ~0 rows (environ)
INSERT INTO `DATABASECHANGELOGLOCK` (`ID`, `LOCKED`, `LOCKGRANTED`, `LOCKEDBY`) VALUES
	(1, 0, NULL, NULL);

-- Listage de la structure de table planning_ecole. eleve
CREATE TABLE IF NOT EXISTS `eleve` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `nom` varchar(50) NOT NULL,
  `prenom` varchar(50) NOT NULL,
  `classe_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_eleve_classe` (`classe_id`),
  CONSTRAINT `fk_eleve_classe` FOREIGN KEY (`classe_id`) REFERENCES `classe` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci;

-- Listage des données de la table planning_ecole.eleve : ~0 rows (environ)
INSERT INTO `eleve` (`id`, `nom`, `prenom`, `classe_id`) VALUES
	(2, 'Marty', 'Titouan', 1),
	(3, 'Paul', 'Vincent', 2);

-- Listage de la structure de procédure planning_ecole. generate_creneaux
DELIMITER //
CREATE PROCEDURE `generate_creneaux`(
    IN p_start DATE,
    IN p_end DATE
)
BEGIN
    DECLARE d DATE;

    SET d = p_start;

    WHILE d <= p_end DO

        -- Lundi à vendredi uniquement
        IF WEEKDAY(d) < 5 THEN

            INSERT INTO t_creneau(debut, fin) VALUES
            (TIMESTAMP(d,'08:00:00'), TIMESTAMP(d,'09:00:00')),
            (TIMESTAMP(d,'09:00:00'), TIMESTAMP(d,'10:00:00')),
            (TIMESTAMP(d,'10:00:00'), TIMESTAMP(d,'11:00:00')),
            (TIMESTAMP(d,'11:00:00'), TIMESTAMP(d,'12:00:00')),
            (TIMESTAMP(d,'13:00:00'), TIMESTAMP(d,'14:00:00')),
            (TIMESTAMP(d,'14:00:00'), TIMESTAMP(d,'15:00:00')),
            (TIMESTAMP(d,'15:00:00'), TIMESTAMP(d,'16:00:00')),
            (TIMESTAMP(d,'16:00:00'), TIMESTAMP(d,'17:00:00'));

        END IF;

        SET d = DATE_ADD(d, INTERVAL 1 DAY);

    END WHILE;

END//
DELIMITER ;

-- Listage de la structure de table planning_ecole. matiere
CREATE TABLE IF NOT EXISTS `matiere` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `nom` varchar(100) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci;

-- Listage des données de la table planning_ecole.matiere : ~4 rows (environ)
INSERT INTO `matiere` (`id`, `nom`) VALUES
	(1, 'Anglais'),
	(2, 'Mathématique'),
	(3, 'Biologie'),
	(5, 'Vie de classe');

-- Listage de la structure de table planning_ecole. professeur
CREATE TABLE IF NOT EXISTS `professeur` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `nom` varchar(100) NOT NULL,
  `prenom` varchar(100) NOT NULL,
  `email` varchar(150) DEFAULT NULL,
  `nb_heures` bigint(20) DEFAULT NULL,
  `plage_horaire_preferee_id` bigint(20) DEFAULT NULL,
  `max_heures_par_jour` decimal(5,2) DEFAULT NULL,
  `max_heures_par_semaine` decimal(5,2) DEFAULT NULL,
  `max_heures_par_seance` decimal(5,2) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `email` (`email`),
  KEY `fk_plage_pref` (`plage_horaire_preferee_id`),
  CONSTRAINT `fk_plage_pref` FOREIGN KEY (`plage_horaire_preferee_id`) REFERENCES `t_plage_horaire` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci;

-- Listage des données de la table planning_ecole.professeur : ~8 rows (environ)
INSERT INTO `professeur` (`id`, `nom`, `prenom`, `email`, `nb_heures`, `plage_horaire_preferee_id`, `max_heures_par_jour`, `max_heures_par_semaine`, `max_heures_par_seance`) VALUES
	(3, 'Auriol', 'Benoît', 'benoit.auriol@sfr.fr', 180, 4, 5.00, 18.00, 2.00),
	(4, 'Auriol', 'Laurine', 'laurine.carrascosa@yahoo.fr', 280, 5, 5.00, 21.00, 4.00),
	(7, 'Bot', '1', 'benoit.ariol@sfr.fr', 180, NULL, 5.00, 18.00, 2.00);

-- Listage de la structure de table planning_ecole. salle
CREATE TABLE IF NOT EXISTS `salle` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `code` varchar(50) NOT NULL,
  `capacite` int(11) DEFAULT NULL,
  `type` varchar(50) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `code` (`code`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci;

-- Listage des données de la table planning_ecole.salle : ~7 rows (environ)
INSERT INTO `salle` (`id`, `code`, `capacite`, `type`) VALUES
	(1, 'B201', 21, 'Cours'),
	(2, 'A201', 20, 'Cours'),
	(3, 'B1', 20, 'Cours'),
	(4, 'A1', 20, 'Cours'),
	(5, 'A2', 20, 'Cours'),
	(6, 'B2', 20, 'Cours'),
	(7, 'A3', 20, 'Cours'),
	(8, 'B3', 20, 'Cours');

-- Listage de la structure de table planning_ecole. seance
CREATE TABLE IF NOT EXISTS `seance` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `professeur_id` bigint(20) NOT NULL,
  `classe_id` bigint(20) NOT NULL,
  `matiere_id` bigint(20) NOT NULL,
  `salle_id` bigint(20) NOT NULL,
  `type` varchar(50) DEFAULT NULL,
  `creneau_id` bigint(20) NOT NULL,
  `debut` datetime DEFAULT NULL,
  `fin` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_seance_matiere` (`matiere_id`),
  KEY `fk_seance_professeur` (`professeur_id`),
  KEY `fk_seance_classe` (`classe_id`),
  KEY `fk_seance_salle` (`salle_id`),
  KEY `fk_creneau_seance` (`creneau_id`),
  CONSTRAINT `fk_creneau_seance` FOREIGN KEY (`creneau_id`) REFERENCES `t_creneau` (`id`),
  CONSTRAINT `fk_seance_classe` FOREIGN KEY (`classe_id`) REFERENCES `classe` (`id`),
  CONSTRAINT `fk_seance_matiere` FOREIGN KEY (`matiere_id`) REFERENCES `matiere` (`id`),
  CONSTRAINT `fk_seance_professeur` FOREIGN KEY (`professeur_id`) REFERENCES `professeur` (`id`),
  CONSTRAINT `fk_seance_salle` FOREIGN KEY (`salle_id`) REFERENCES `salle` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci;

-- Listage des données de la table planning_ecole.seance : ~1 rows (environ)

-- Listage de la structure de table planning_ecole. tj_distance_salle
CREATE TABLE IF NOT EXISTS `tj_distance_salle` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `fk_salle1_id` bigint(20) NOT NULL,
  `fk_salle2_id` bigint(20) NOT NULL,
  `distance` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_salle_2` (`fk_salle2_id`),
  KEY `fk_salle_1` (`fk_salle1_id`),
  CONSTRAINT `fk_salle_1` FOREIGN KEY (`fk_salle1_id`) REFERENCES `salle` (`id`),
  CONSTRAINT `fk_salle_2` FOREIGN KEY (`fk_salle2_id`) REFERENCES `salle` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci;

-- Listage des données de la table planning_ecole.tj_distance_salle : ~0 rows (environ)

-- Listage de la structure de table planning_ecole. tj_equipements_salle
CREATE TABLE IF NOT EXISTS `tj_equipements_salle` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `fk_salle_id` bigint(20) NOT NULL,
  `fk_equipement_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_equ_salle` (`fk_equipement_id`),
  KEY `fk_salle` (`fk_salle_id`),
  CONSTRAINT `fk_equ_salle` FOREIGN KEY (`fk_equipement_id`) REFERENCES `t_equipement` (`id`),
  CONSTRAINT `fk_salle` FOREIGN KEY (`fk_salle_id`) REFERENCES `salle` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci;

-- Listage des données de la table planning_ecole.tj_equipements_salle : ~0 rows (environ)

-- Listage de la structure de table planning_ecole. tj_professeur_matiere
CREATE TABLE IF NOT EXISTS `tj_professeur_matiere` (
  `professeur_id` bigint(20) NOT NULL,
  `matiere_id` bigint(20) NOT NULL,
  PRIMARY KEY (`professeur_id`,`matiere_id`),
  KEY `fk_pm_matiere` (`matiere_id`),
  CONSTRAINT `fk_pm_matiere` FOREIGN KEY (`matiere_id`) REFERENCES `matiere` (`id`),
  CONSTRAINT `fk_pm_professeur` FOREIGN KEY (`professeur_id`) REFERENCES `professeur` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci;

-- Listage des données de la table planning_ecole.tj_professeur_matiere : ~10 rows (environ)
INSERT INTO `tj_professeur_matiere` (`professeur_id`, `matiere_id`) VALUES
	(3, 1),
	(4, 2),
	(4, 5),
	(7, 3);

-- Listage de la structure de table planning_ecole. t_classe_presence
CREATE TABLE IF NOT EXISTS `t_classe_presence` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `classe_id` bigint(20) NOT NULL,
  `date_debut` date NOT NULL,
  `date_fin` date NOT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_presence_classe` (`classe_id`),
  CONSTRAINT `fk_presence_classe` FOREIGN KEY (`classe_id`) REFERENCES `classe` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=100 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci;

-- Listage des données de la table planning_ecole.t_classe_presence : ~12 rows (environ)
INSERT INTO `t_classe_presence` (`id`, `classe_id`, `date_debut`, `date_fin`) VALUES
	(70, 1, '2026-09-01', '2026-09-21'),
	(71, 1, '2026-11-02', '2026-11-22'),
	(75, 2, '2026-09-22', '2026-10-12'),
	(76, 2, '2026-11-23', '2026-12-13');

-- Listage de la structure de table planning_ecole. t_creneau
CREATE TABLE IF NOT EXISTS `t_creneau` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `debut` datetime DEFAULT NULL,
  `fin` datetime DEFAULT NULL,
  `semaine_type` varchar(50) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=541 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci;

-- Listage des données de la table planning_ecole.t_creneau : ~0 rows (environ)

-- Listage de la structure de table planning_ecole. t_equipement
CREATE TABLE IF NOT EXISTS `t_equipement` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `libelle` varchar(200) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci;

-- Listage des données de la table planning_ecole.t_equipement : ~0 rows (environ)

-- Listage de la structure de table planning_ecole. t_matiere_classe_config
CREATE TABLE IF NOT EXISTS `t_matiere_classe_config` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `classe_id` bigint(20) NOT NULL,
  `matiere_id` bigint(20) NOT NULL,
  `date_debut` date DEFAULT NULL,
  `date_fin` date DEFAULT NULL,
  `volume_horaire_periode` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_mcc_matiere` (`matiere_id`),
  KEY `fk_mcc_classe` (`classe_id`),
  CONSTRAINT `fk_mcc_classe` FOREIGN KEY (`classe_id`) REFERENCES `classe` (`id`),
  CONSTRAINT `fk_mcc_matiere` FOREIGN KEY (`matiere_id`) REFERENCES `matiere` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=33 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci;

-- Listage des données de la table planning_ecole.t_matiere_classe_config : ~6 rows (environ)
INSERT INTO `t_matiere_classe_config` (`id`, `classe_id`, `matiere_id`, `date_debut`, `date_fin`, `volume_horaire_periode`) VALUES
	(9, 1, 1, '2026-07-18', '2026-12-30', 20),
	(10, 1, 2, '2026-07-01', '2026-12-30', 20),
	(11, 2, 1, '2026-07-01', '2026-12-30', 20),
	(12, 2, 2, '2026-07-01', '2026-12-30', 20),
	(13, 1, 3, '2026-07-01', '2026-12-30', 20),
	(15, 2, 3, '2026-07-01', '2026-12-30', 20);

-- Listage de la structure de table planning_ecole. t_plage_horaire
CREATE TABLE IF NOT EXISTS `t_plage_horaire` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `libelle` varchar(200) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci;

-- Listage des données de la table planning_ecole.t_plage_horaire : ~0 rows (environ)
INSERT INTO `t_plage_horaire` (`id`, `libelle`) VALUES
	(1, 'Mercredi 10h'),
	(2, 'Mardin matin'),
	(3, 'Mardin matin'),
	(4, 'Mercredi 10h'),
	(5, 'Mardin matin');

-- Listage de la structure de table planning_ecole. t_professeur_dayoff
CREATE TABLE IF NOT EXISTS `t_professeur_dayoff` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `professeur_id` bigint(20) NOT NULL,
  `day_of_week` int(11) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_prof_dayoff_prof` (`professeur_id`),
  CONSTRAINT `fk_prof_dayoff_prof` FOREIGN KEY (`professeur_id`) REFERENCES `professeur` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci;

-- Listage des données de la table planning_ecole.t_professeur_dayoff : ~0 rows (environ)

-- Listage de la structure de table planning_ecole. t_vacances
CREATE TABLE IF NOT EXISTS `t_vacances` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `nom` varchar(150) NOT NULL,
  `date_debut` date NOT NULL,
  `date_fin` date NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci;

-- Listage des données de la table planning_ecole.t_vacances : ~3 rows (environ)
INSERT INTO `t_vacances` (`id`, `nom`, `date_debut`, `date_fin`) VALUES
	(1, 'été', '2026-07-06', '2026-08-30'),
	(2, 'toussaint', '2026-10-26', '2026-11-08'),
	(3, 'Noel', '2026-12-21', '2027-01-03'),
	(4, 'Février', '2027-02-01', '2027-02-14');

/*!40103 SET TIME_ZONE=IFNULL(@OLD_TIME_ZONE, 'system') */;
/*!40101 SET SQL_MODE=IFNULL(@OLD_SQL_MODE, '') */;
/*!40014 SET FOREIGN_KEY_CHECKS=IFNULL(@OLD_FOREIGN_KEY_CHECKS, 1) */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40111 SET SQL_NOTES=IFNULL(@OLD_SQL_NOTES, 1) */;
