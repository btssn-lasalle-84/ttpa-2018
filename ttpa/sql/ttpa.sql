pragma foreign_keys = on;

CREATE TABLE table_joueurs (
    "ID_JOUEUR" INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, 
    "NOM" VARCHAR(255) NOT NULL
);

INSERT INTO table_joueurs(NOM) VALUES('LEGOUT Christophe');
INSERT INTO table_joueurs(NOM) VALUES('MARTINEZ Michel');
INSERT INTO table_joueurs(NOM) VALUES('LEBESSON Emmanuel');
INSERT INTO table_joueurs(NOM) VALUES('ELOI Damien');
INSERT INTO table_joueurs(NOM) VALUES('MATTENET Adrien');
INSERT INTO table_joueurs(NOM) VALUES('CHILA Patrick');
INSERT INTO table_joueurs(NOM) VALUES('BEAUMONT Jérôme');

CREATE TABLE table_seances (
    "ID_SEANCE" INTEGER PRIMARY KEY AUTOINCREMENT,
    "NOMBRE_BALLES" INTEGER NOT NULL,
    "FREQUENCE" INTEGER NOT NULL,
    "EFFET" VARCHAR(1) NOT NULL,
    "PUISSANCE" INTEGER NOT NULL,
    "ROTATION" INTEGER NOT NULL,
    "ZONE_OBJECTIF" INTEGER NOT NULL,
    "ZONE_ROBOT" INTEGER NOT NULL,
    "TAUX_REUSSITE" REAL NOT NULL,
    "DATE_DEBUT" DATETIME NOT NULL,
    "DATE_FIN" DATETIME NOT NULL,
    "ID_JOUEUR" INTEGER NOT NULL, 
    CONSTRAINT fk_seances_1 FOREIGN KEY (ID_JOUEUR) REFERENCES table_joueurs (ID_JOUEUR) ON DELETE CASCADE
);

CREATE TABLE table_parametres (
            "ID_PARAMETRE" INTEGER PRIMARY KEY CHECK (ID_PARAMETRE = 1),
            "ID_JOUEUR" INTEGER NOT NULL,
            CONSTRAINT fk_parametres_1 FOREIGN KEY (ID_JOUEUR) REFERENCES table_joueurs (ID_JOUEUR)
);

INSERT INTO table_parametres(ID_PARAMETRE, ID_JOUEUR) VALUES(1, 1);
