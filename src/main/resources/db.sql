-- ============================================================
--  Base de données : planning_ecole
--  Compatible MariaDB 10.x / 11.x
-- ============================================================

CREATE DATABASE IF NOT EXISTS planning_ecole
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_general_ci;

USE planning_ecole;

-- ============================================================
-- Table : professeur
-- ============================================================
CREATE TABLE professeur (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nom VARCHAR(100) NOT NULL,
    prenom VARCHAR(100) NOT NULL,
    email VARCHAR(150) UNIQUE
) ENGINE=InnoDB;

-- ============================================================
-- Table : classe
-- ============================================================
CREATE TABLE classe (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nom VARCHAR(100) NOT NULL
) ENGINE=InnoDB;

-- ============================================================
-- Table : matiere
-- ============================================================
CREATE TABLE matiere (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nom VARCHAR(100) NOT NULL
) ENGINE=InnoDB;

-- ============================================================
-- Table : salle
-- ============================================================
CREATE TABLE salle (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(50) NOT NULL,
    capacite INT DEFAULT NULL,
    UNIQUE KEY uq_salle_code (code)
) ENGINE=InnoDB;

-- ============================================================
-- Table : seance
-- ============================================================
CREATE TABLE seance (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    debut DATETIME NOT NULL,
    fin DATETIME NOT NULL,

    professeur_id BIGINT NOT NULL,
    classe_id BIGINT NOT NULL,
    matiere_id BIGINT NOT NULL,
    salle_id BIGINT NOT NULL,

    CONSTRAINT fk_seance_professeur FOREIGN KEY (professeur_id)
        REFERENCES professeur(id) ON DELETE CASCADE ON UPDATE CASCADE,

    CONSTRAINT fk_seance_classe FOREIGN KEY (classe_id)
        REFERENCES classe(id) ON DELETE CASCADE ON UPDATE CASCADE,

    CONSTRAINT fk_seance_matiere FOREIGN KEY (matiere_id)
        REFERENCES matiere(id) ON DELETE CASCADE ON UPDATE CASCADE,

    CONSTRAINT fk_seance_salle FOREIGN KEY (salle_id)
        REFERENCES salle(id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB;

-- ============================================================
-- Index utiles
-- ============================================================
CREATE INDEX idx_seance_professeur ON seance (professeur_id);
CREATE INDEX idx_seance_classe ON seance (classe_id);
CREATE INDEX idx_seance_matiere ON seance (matiere_id);
CREATE INDEX idx_seance_salle ON seance (salle_id);
CREATE INDEX idx_seance_debut_fin ON seance (debut, fin);
