package com.ttpa.iris.ttpamobile;

/**
 * Created by smaniotto on 17/03/18.
 */

import android.content.Context;
import android.database.sqlite.*;
import android.util.Log;

import java.io.File;

/*
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
    "TAUX_REUSSITE" INTEGER NOT NULL,
    "DATE_DEBUT" DATETIME NOT NULL,
    "DATE_FIN" DATETIME NOT NULL,
    "ID_JOUEUR" INTEGER NOT NULL,
    CONSTRAINT fk_seances_1 FOREIGN KEY (ID_JOUEUR) REFERENCES table_joueurs (ID_JOUEUR) ON DELETE CASCADE
);

 */

/**
 * Classe ServeurSQLite définissant les caratéristiques de la base de données.
 */
public class ServeurSQLite extends SQLiteOpenHelper
{
    /**
     * Attributs de la classe ServeurSQLite.
     */
    public static final String DATABASE_NAME = "ttpa_mobile.db";
    public static final int    DATABASE_VERSION = 1;

    /* TABLE DES JOUEURS */
    public static final String TABLE_JOUEURS = "table_joueurs";
    public static final String COL_ID_JOUEUR = "ID_JOUEUR";
    public static final String COL_NOM = "NOM";
    public static final int    NUM_COL_ID_JOUEUR = 0;
    public static final int    NUM_COL_NOM = 1;

    /* TABLE DES SEANCES */
    public static final String TABLE_SEANCES = "table_seances";
    public static final String COL_ID = "ID_SEANCE";
    public static final String COL_NOMBRE_BALLES = "NOMBRE_BALLES";
    public static final String COL_FREQUENCE = "FREQUENCE";
    public static final String COL_EFFET = "EFFET";
    public static final String COL_INTENSITE_EFFET = "INTENSITE_EFFET";
    public static final String COL_PUISSANCE = "PUISSANCE";
    public static final String COL_ROTATION = "ROTATION";
    public static final String COL_ZONE_OBJECTIF = "ZONE_OBJECTIF";
    public static final String COL_ZONE_ROBOT = "ZONE_ROBOT";
    public static final String COL_TAUX_REUSSITE = "TAUX_REUSSITE";
    public static final String COL_DATE_DEBUT = "DATE_DEBUT"; //DATETIME : format "YYYY-MM-DD HH:MM:SS"
    public static final String COL_DATE_FIN = "DATE_FIN"; //DATETIME : format "YYYY-MM-DD HH:MM:SS"
    public static final int    NUM_COL_ID = 0;
    public static final int    NUM_COL_FREQUENCE = 1;
    public static final int    NUM_COL_NOMBRE_BALLES = 2;
    public static final int    NUM_COL_EFFET = 3;
    public static final int    NUM_COL_INTENSITE_EFFET = 4;
    public static final int    NUM_COL_PUISSANCE = 5;
    public static final int    NUM_COL_ROTATION = 6;
    public static final int    NUM_COL_ZONE_OBJECTIF = 7;
    public static final int    NUM_COL_ZONE_ROBOT = 8;
    public static final int    NUM_COL_TAUX_REUSSITE = 9;
    public static final int    NUM_COL_DATE_DEBUT = 10;
    public static final int    NUM_COL_DATE_FIN = 11;
    public static final int    NUM_COL_ID_JOUEUR_SEANCE = 12;

    /* TABLE DES PARAMETRES */
    public static final String TABLE_PARAMETRES = "table_parametres";
    public static final String COL_ID_PARAMETRE = "ID_PARAMETRE";
    public static final int    NUM_COL_ID_PARAMETRE = 0;
    public static final int    NUM_COL_ID_JOUEUR_PARAMETRE = 1;

    public static final String CREATE_BDD_JOUEURS = "CREATE TABLE " + TABLE_JOUEURS + " ("
            + COL_ID_JOUEUR + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + COL_NOM + " VARCHAR(255) NOT NULL);";

    public static final String CREATE_BDD_SEANCES = "CREATE TABLE " + TABLE_SEANCES + " ("
            + COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + COL_NOMBRE_BALLES + " INTEGER NOT NULL,"
            + COL_FREQUENCE + " INTEGER NOT NULL, "
            + COL_EFFET + " VARCHAR(1) NOT NULL,"
            + COL_INTENSITE_EFFET + " INTEGER NOT NULL,"
            + COL_PUISSANCE + " INTEGER NOT NULL,"
            + COL_ROTATION + " INTEGER NOT NULL,"
            + COL_ZONE_OBJECTIF + " INTEGER NOT NULL,"
            + COL_ZONE_ROBOT + " INTEGER NOT NULL,"
            + COL_TAUX_REUSSITE + " REAL NOT NULL,"
            + COL_DATE_DEBUT + " DATETIME NOT NULL,"
            + COL_DATE_FIN + " DATETIME NOT NULL,"
            + COL_ID_JOUEUR + " INTEGER NOT NULL,"
            + "CONSTRAINT fk_seances_1 FOREIGN KEY (ID_JOUEUR) REFERENCES table_joueurs (ID_JOUEUR) ON DELETE CASCADE);";

    public static final String CREATE_BDD_PARAMETRES = "CREATE TABLE " + TABLE_PARAMETRES + " ("
            + COL_ID_PARAMETRE + " INTEGER PRIMARY KEY NOT NULL, "
            + COL_ID_JOUEUR + " INTEGER NOT NULL,"
            + "CONSTRAINT fk_parametres_1 FOREIGN KEY (ID_JOUEUR) REFERENCES table_joueurs (ID_JOUEUR));";

    private static final String INSERT_TABLE_JOUEURS_1 = "INSERT INTO table_joueurs(NOM) VALUES('LEGOUT Christophe');";
    private static final String INSERT_TABLE_JOUEURS_2 = "INSERT INTO table_joueurs(NOM) VALUES('MARTINEZ Michel');";
    private static final String INSERT_TABLE_JOUEURS_3 = "INSERT INTO table_joueurs(NOM) VALUES('LEBESSON Emmanuel');";
    private static final String INSERT_TABLE_JOUEURS_4 = "INSERT INTO table_joueurs(NOM) VALUES('ELOI Damien');";
    private static final String INSERT_TABLE_JOUEURS_5 = "INSERT INTO table_joueurs(NOM) VALUES('MATTENET Adrien');";
    private static final String INSERT_TABLE_JOUEURS_6 = "INSERT INTO table_joueurs(NOM) VALUES('CHILA Patrick');";
    private static final String INSERT_TABLE_JOUEURS_7 = "INSERT INTO table_joueurs(NOM) VALUES('BEAUMONT Jérôme');";

    private static final String INSERT_TABLE_PARAMETRES = "INSERT INTO table_parametres(ID_PARAMETRE, ID_JOUEUR) VALUES(1, 1);";

    /**
     * Méthode ServeurSQLite constructeur de la classe ServeurSQLite.
     *
     * @param context
     */
    public ServeurSQLite(Context context)
    {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * Méthode onCreate appellée à la création de l'objet et permettant l'exécution des requêtes créant les tables de la base de données.
     *
     * @param db
     */
    @Override
    public void onCreate(SQLiteDatabase db)
    {
        // On crée la table des séances
        db.execSQL("pragma foreign_keys = on;");
        db.execSQL(CREATE_BDD_JOUEURS);
        db.execSQL(CREATE_BDD_SEANCES);
        db.execSQL(CREATE_BDD_PARAMETRES);
        db.execSQL(INSERT_TABLE_JOUEURS_1);
        db.execSQL(INSERT_TABLE_JOUEURS_2);
        db.execSQL(INSERT_TABLE_JOUEURS_3);
        db.execSQL(INSERT_TABLE_JOUEURS_4);
        db.execSQL(INSERT_TABLE_JOUEURS_5);
        db.execSQL(INSERT_TABLE_JOUEURS_6);
        db.execSQL(INSERT_TABLE_JOUEURS_7);
        db.execSQL(INSERT_TABLE_PARAMETRES);

        String path = db.getPath();
        File f = new File(path);
        boolean r = f.setReadable(true, false);
        if(r)
        {
            Log.d("TTPA", "onCreate : Ajout droit lecture " + path); // d = debug
        }
        else
        {
            Log.e("TTPA", "onCreate : Erreur ajout droit lecture " + path); // e = erreur
        }
        r = f.setWritable(true, false);
        if(r)
        {
            Log.d("TTPA", "onCreate : Ajout droit écriture " + path); // d = debug
        }
        else
        {
            Log.e("TTPA", "onCreate : Erreur ajout droit écriture " + path); // e = erreur
        }
        File parentDir = f.getAbsoluteFile().getParentFile();
        r = parentDir.setReadable(true, false);
        if(r)
        {
            Log.d("TTPA", "onCreate : Ajout droit lecture " + parentDir.getPath()); // d = debug
        }
        else
        {
            Log.e("TTPA", "onCreate : Erreur ajout droit lecture " + parentDir.getPath()); // e = erreur
        }
        r = parentDir.setWritable(true, false);
        if(r)
        {
            Log.d("TTPA", "onCreate : Ajout droit écriture " + parentDir.getPath()); // d = debug
        }
        else
        {
            Log.e("TTPA", "onCreate : Erreur ajout droit écriture " + parentDir.getPath()); // e = erreur
        }
    }

    /**
     * Méthode onUpgrade supprimant et recréant toutes les tables de la base de données.
     *
     * @param db
     * @param oldVersion
     * @param newVersion
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {
        // On supprime la table puis on la recrée
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_JOUEURS + ";");
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SEANCES + ";");
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PARAMETRES + ";");
        onCreate(db);
    }

    /**
     * @brief Ajoute les droits en lecture et en écriture à la base de données lors de son ouverture
     * @param db un type SQLiteDatabase qui représente la base de données
     */
    @Override
    public void onOpen(SQLiteDatabase db)
    {
        //onUpgrade(db, 1, 2);

        String path = db.getPath();
        File f = new File(path);
        boolean r = f.setReadable(true, false);
        if(r)
        {
            Log.d("TTPA", "onOpen : Ajout droit lecture " + path); // d = debug
        }
        else
        {
            Log.e("TTPA", "onOpen : Erreur ajout droit lecture " + path); // e = erreur
        }
        r = f.setWritable(true, false);
        if(r)
        {
            Log.d("TTPA", "onOpen : Ajout droit écriture " + path); // d = debug
        }
        else
        {
            Log.e("TTPA", "onOpen : Erreur ajout droit écriture " + path); // e = erreur
        }
        File parentDir = f.getAbsoluteFile().getParentFile();
        r = parentDir.setReadable(true, false);
        if(r)
        {
            Log.d("TTPA", "onOpen : Ajout droit lecture " + parentDir.getPath()); // d = debug
        }
        else
        {
            Log.e("TTPA", "onOpen : Erreur ajout droit lecture " + parentDir.getPath()); // e = erreur
        }
        r = parentDir.setWritable(true, false);
        if(r)
        {
            Log.d("TTPA", "onOpen : Ajout droit écriture " + parentDir.getPath()); // d = debug
        }
        else
        {
            Log.e("TTPA", "onOpen : Erreur ajout droit écriture " + parentDir.getPath()); // e = erreur
        }
    }
}
