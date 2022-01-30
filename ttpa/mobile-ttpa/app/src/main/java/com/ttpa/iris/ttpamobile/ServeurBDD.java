package com.ttpa.iris.ttpamobile;

/**
 * Created by smaniotto on 17/03/18.
 */

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Classe ServeurBDD définnissant les caratéristiques et le comportement d'un serveur de base de données.
 */
public class ServeurBDD
{
    /*
     * Attributs de la classe ServeurBDD.
     */
    private SQLiteDatabase bdd = null;
    private ServeurSQLite serveurSQLite = null;

    /**
     * Méthode ServeurBDD constructeur de la classe ServeurBDD.
     *
     * @param context
     */
    public ServeurBDD(Context context)
    {
        // cn crée la BDD et ses tables
        serveurSQLite = new ServeurSQLite(context);
    }

    /**
     * Méthode open ouvrant la base de données en écriture.
     */
    public void open()
    {
        // on ouvre la BDD en écriture
        if (bdd == null)
            bdd = serveurSQLite.getWritableDatabase();
    }

    /**
     * Méthode close fermant la base de données.
     */
    public void close()
    {
        if (bdd != null)
            if (bdd.isOpen())
                bdd.close();
    }

    /**
     * Méthode getBDD accesseur de l'attribut bdd.
     *
     * @return
     */
    public SQLiteDatabase getBDD()
    {
        return bdd;
    }

    /**
     * Méthode insererSeance permettant l'insertion d'une séance dans la base de données.
     *
     * @param seance étant la séance à insérer dans la base de données.
     * @return l'id de la séance dans la base de données.
     */
    public long insererSeance(Seance seance)
    {
        ContentValues values = new ContentValues();

        values.put(ServeurSQLite.COL_FREQUENCE, seance.getFrequence());
        values.put(ServeurSQLite.COL_NOMBRE_BALLES, seance.getNombreBalles());
        values.put(ServeurSQLite.COL_EFFET, seance.getEffet());
        values.put(ServeurSQLite.COL_INTENSITE_EFFET, seance.getIntensiteEffet());
        values.put(ServeurSQLite.COL_PUISSANCE, seance.getPuissance());
        values.put(ServeurSQLite.COL_ROTATION, seance.getRotation());
        values.put(ServeurSQLite.COL_ZONE_OBJECTIF, seance.getZoneObjectif());
        values.put(ServeurSQLite.COL_ZONE_ROBOT, seance.getZoneRobot());
        values.put(ServeurSQLite.COL_TAUX_REUSSITE, seance.getTauxReussite());
        values.put(ServeurSQLite.COL_DATE_DEBUT, seance.getDateDebut());
        values.put(ServeurSQLite.COL_DATE_FIN, seance.getDateFin());
        values.put(ServeurSQLite.COL_ID_JOUEUR, seance.getIdJoueur());

        return bdd.insert(ServeurSQLite.TABLE_SEANCES, null, values);
    }

    /**
     * Méthode supprimerSeance permettant la suppression d'une séance dans la base de données.
     *
     * @param id étant l'id de la séance à supprimer.
     * @return
     */
    public int supprimerSeance(int id)
    {
        return bdd.delete(ServeurSQLite.TABLE_SEANCES, ServeurSQLite.COL_ID + " = " + id, null);
    }

    /**
     * Méthode getSeance permettant l'accès à une séance grâce à son attribut id.
     *
     * @param id étant l'id de la séance à retourner.
     * @return
     */
    public Seance getSeance(int id)
    {
        Cursor c = bdd.query(ServeurSQLite.TABLE_SEANCES, new String[] {ServeurSQLite.COL_ID, ServeurSQLite.COL_NOMBRE_BALLES, ServeurSQLite.COL_FREQUENCE, ServeurSQLite.COL_EFFET, ServeurSQLite.COL_INTENSITE_EFFET, ServeurSQLite.COL_PUISSANCE, ServeurSQLite.COL_ROTATION, ServeurSQLite.COL_ZONE_OBJECTIF, ServeurSQLite.COL_ZONE_ROBOT, ServeurSQLite.COL_TAUX_REUSSITE, ServeurSQLite.COL_DATE_DEBUT, ServeurSQLite.COL_DATE_FIN, ServeurSQLite.COL_ID_JOUEUR}, ServeurSQLite.COL_ID + " = " + id, null, null, null, null);

        return cursorToSeance(c, true);
    }

    /**
     * Méthode purgerTableSeances permettant la purge (suppression totale) de la table des séances.
     */
    public void purgerTableSeances()
    {
        bdd.execSQL("DROP TABLE IF EXISTS " + serveurSQLite.TABLE_SEANCES); // Supprimer la table
        bdd.execSQL(ServeurSQLite.CREATE_BDD_SEANCES); // Recréer la table
    }

    /**
     * Méthode purgerSeancesJoueur permettant la suppression des séances d'un joueur.
     *
     * @param idJoueur étant l'id du joueur auquel on doit purger les séances.
     * @return
     */
    public int purgerSeancesJoueur(int idJoueur)
    {
        return bdd.delete(ServeurSQLite.TABLE_SEANCES, ServeurSQLite.COL_ID_JOUEUR + " = " + idJoueur, null);
    }

    /**
     * Méthode getSeances retournant toutes les séances présentes dans la table des séances.
     *
     * @return liste d'objets Seance.
     */
    public List<Seance> getSeances()
    {
        List<Seance> seances = new ArrayList<Seance>();

        Cursor cursor = bdd.query(ServeurSQLite.TABLE_SEANCES, new String[] {ServeurSQLite.COL_ID, ServeurSQLite.COL_FREQUENCE, ServeurSQLite.COL_NOMBRE_BALLES, ServeurSQLite.COL_EFFET, ServeurSQLite.COL_INTENSITE_EFFET, ServeurSQLite.COL_PUISSANCE, ServeurSQLite.COL_ROTATION, ServeurSQLite.COL_ZONE_OBJECTIF, ServeurSQLite.COL_ZONE_ROBOT, ServeurSQLite.COL_TAUX_REUSSITE, ServeurSQLite.COL_DATE_DEBUT, ServeurSQLite.COL_DATE_FIN, ServeurSQLite.COL_ID_JOUEUR}, null, null, null, null, null);

        cursor.moveToFirst();
        while (!cursor.isAfterLast())
        {
            Seance seance = cursorToSeance(cursor, false);
            seances.add(seance);
            cursor.moveToNext();
        }

        cursor.close();

        return seances;
    }

    /**
     * Méthode getSeances retournant toutes les séances présentes dans la table des séances ayant pour id idJoueur.
     *
     * @param idJoueur
     * @return liste d'objets Seance.
     */
    public List<Seance> getSeances(int idJoueur)
    {
        List<Seance> seances = new ArrayList<Seance>();

        Cursor cursor = bdd.query(ServeurSQLite.TABLE_SEANCES, new String[] {ServeurSQLite.COL_ID, ServeurSQLite.COL_FREQUENCE, ServeurSQLite.COL_NOMBRE_BALLES, ServeurSQLite.COL_EFFET, ServeurSQLite.COL_INTENSITE_EFFET, ServeurSQLite.COL_PUISSANCE, ServeurSQLite.COL_ROTATION, ServeurSQLite.COL_ZONE_OBJECTIF, ServeurSQLite.COL_ZONE_ROBOT, ServeurSQLite.COL_TAUX_REUSSITE, ServeurSQLite.COL_DATE_DEBUT, ServeurSQLite.COL_DATE_FIN, ServeurSQLite.COL_ID_JOUEUR},  ServeurSQLite.COL_ID_JOUEUR + " = " + idJoueur, null, null, null, null);

        cursor.moveToFirst();
        while (!cursor.isAfterLast())
        {
            Seance seance = cursorToSeance(cursor, false);
            seances.add(seance);
            cursor.moveToNext();
        }

        cursor.close();

        return seances;
    }

    /**
     * Méthode cursorToSeance permettant de convertir un curseur en un objet de type Seance.
     *
     * @param c
     * @param one
     * @return
     */
    private Seance cursorToSeance(Cursor c, boolean one)
    {
        if (c.getCount() == 0)
            return null;

        if(one == true)
            c.moveToFirst();

        Seance seance = new Seance();

        seance.setId(c.getInt(ServeurSQLite.NUM_COL_ID));
        seance.setNombreBalles(c.getInt(ServeurSQLite.NUM_COL_NOMBRE_BALLES));
        seance.setFrequence(c.getInt(ServeurSQLite.NUM_COL_FREQUENCE));
        seance.setEffet(c.getString(ServeurSQLite.NUM_COL_EFFET));
        seance.setIntensiteEffet(c.getInt(ServeurSQLite.NUM_COL_INTENSITE_EFFET));
        seance.setPuissance(c.getInt(ServeurSQLite.NUM_COL_PUISSANCE));
        seance.setRotation(c.getInt(ServeurSQLite.NUM_COL_ROTATION));
        seance.setZoneObjectif(c.getInt(ServeurSQLite.NUM_COL_ZONE_OBJECTIF));
        seance.setZoneRobot(c.getInt(ServeurSQLite.NUM_COL_ZONE_ROBOT));
        seance.setTauxReussite(c.getInt(ServeurSQLite.NUM_COL_TAUX_REUSSITE));
        seance.setDateDebut(c.getString(ServeurSQLite.NUM_COL_DATE_DEBUT));
        seance.setDateFin(c.getString(ServeurSQLite.NUM_COL_DATE_FIN));
        seance.setIdJoueur(c.getInt(ServeurSQLite.NUM_COL_ID_JOUEUR_SEANCE));

        if(one == true)
            c.close();

        return seance;
    }

    /**
     * Méthode insererJoueur permettant l'insertion d'un joueur.
     *
     * @param joueur étant le joueur à insérer dans la base de données.
     * @return l'id dans la base de données.
     */
    public long insererJoueur(Joueur joueur)
    {
        ContentValues values = new ContentValues();

        //values.put(ServeurSQLite.COL_ID_JOUEUR, joueur.getId());
        values.put(ServeurSQLite.COL_NOM, joueur.getNom());

        return bdd.insert(ServeurSQLite.TABLE_JOUEURS, null, values);
    }

    /**
     * Méthode supprimerJoueur permettant la suppression d'un joueur.
     *
     * @param id étant l'id du joueur à supprimer.
     * @return
     */
    public int supprimerJoueur(int id)
    {
        return bdd.delete(ServeurSQLite.TABLE_JOUEURS, ServeurSQLite.COL_ID_JOUEUR + " = " + id, null);
    }

    /**
     * Méthode supprimerJoueur permettant la suppression d'un joueur.
     *
     * @param nom String le nom du joueur à supprimer.
     * @return
     */
    public int supprimerJoueur(String nom)
    {
        return bdd.delete(ServeurSQLite.TABLE_JOUEURS, ServeurSQLite.COL_NOM + " = '" + nom + "'", null);
    }

    /**
     * Méthode getJoueur permettant l'accès à un joueur grâce à son attribut id.
     *
     * @param id étant l'id du joueur à retourner.
     * @return
     */
    public Joueur getJoueur(int id)
    {
        Cursor c = bdd.query(ServeurSQLite.TABLE_JOUEURS, new String[] {ServeurSQLite.COL_ID_JOUEUR, ServeurSQLite.COL_NOM}, ServeurSQLite.COL_ID_JOUEUR + " = " + id, null, null, null, null);

        return cursorToJoueur(c, true);
    }

    /**
     * Méthode getJoueur permettant l'accès à un joueur grâce à son nom.
     *
     * @param nom String le nom du joueur à retourner.
     * @return
     */
    public Joueur getJoueur(String nom)
    {
        Cursor c = bdd.query(ServeurSQLite.TABLE_JOUEURS, new String[] {ServeurSQLite.COL_ID_JOUEUR, ServeurSQLite.COL_NOM}, ServeurSQLite.COL_NOM + " = '" + nom + "'", null, null, null, null);

        return cursorToJoueur(c, true);
    }

    /**
     * Méthode purgerTableJoueurs permettant la purge (suppression totale) de la table des joueurs.
     */
    public void purgerTableJoueurs()
    {
        bdd.execSQL("DROP TABLE IF EXISTS " + serveurSQLite.TABLE_JOUEURS); // Supprimer la table
        bdd.execSQL(ServeurSQLite.CREATE_BDD_JOUEURS); // Recréer la table
    }

    /**
     * Méthode getJoueurs retournant tous les joueurs présents dans la table des joueurs.
     *
     * @return liste d'objets Joueur.
     */
    public List<Joueur> getJoueurs()
    {
        List<Joueur> joueurs = new ArrayList<Joueur>();

        Cursor cursor = bdd.query(ServeurSQLite.TABLE_JOUEURS, new String[] {ServeurSQLite.COL_ID_JOUEUR, ServeurSQLite.COL_NOM}, null, null, null, null, null);

        cursor.moveToFirst();
        while (!cursor.isAfterLast())
        {
            Joueur joueur = cursorToJoueur(cursor, false);
            joueurs.add(joueur);
            cursor.moveToNext();
        }

        cursor.close();

        return joueurs;
    }

    /**
     * Méthode cursorToJoueur permettant de convertir un curseur en un objet de type Joueur.
     *
     * @param c
     * @param one
     * @return
     */
    private Joueur cursorToJoueur(Cursor c, boolean one)
    {
        if (c.getCount() == 0)
            return null;

        if(one == true)
            c.moveToFirst();

        Joueur joueur = new Joueur();

        joueur.setId(c.getInt(ServeurSQLite.NUM_COL_ID_JOUEUR));
        joueur.setNom(c.getString(ServeurSQLite.NUM_COL_NOM));

        if(one == true)
            c.close();

        return joueur;
    }

    /**
     *
     *
     * @return int l'id du joueur
     */
    public int getIdJoueurParametres()
    {
        Cursor c = bdd.query(ServeurSQLite.TABLE_PARAMETRES, new String[] {ServeurSQLite.COL_ID_JOUEUR}, ServeurSQLite.COL_ID_PARAMETRE + " = '1'", null, null, null, null);

        if (c.getCount() == 0)
            return 0;

        c.moveToFirst();

        int idJoueur = c.getInt(0);

        c.close();

        return idJoueur;
    }

    /**
     * @brief Permet de mettre à jour un enregistrement de la table
     * @param idJoueur int représente l'identifiant du joueur
     * @return un int qui permet de savoir si la mise à jour de l'enregistrement a réussi
     */
    public int setIdJoueurParametres(int idJoueur)
    {
        ContentValues values = new ContentValues();
        values.put("ID_JOUEUR", idJoueur);

        return bdd.update("table_parametres", values, "ID_PARAMETRE = " + 1, null);
    }
}
