package com.ttpa.iris.ttpamobile;

import android.widget.TableRow;

import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Created by smaniotto on 16/03/18.
 */

/**
 * Classe Seance définissant les caractéristiques et le comportement d'une séance.
 */
public class Seance
{
    /*
     * Attributs de la classe Seance.
     */
    private int id;
    private int frequence;
    private int nombreBalles;
    private String effet;
    private int intensiteEffet;
    private int puissance;
    private int rotation;
    private int zoneObjectif;
    private int zoneRobot;
    private float tauxReussite;
    private String dateDebut;
    private String dateFin;
    private int idJoueur;

    /**
     * Méthode Seance constructeur par défaut de la classe Seance.
     */
    public Seance() {}

    /**
     * Méthode Seance constructeur de la classe Seance.
     *
     * @param frequence étant la fréquence d'envoi des balles (en balles/minute) de la séance.
     * @param nombreBalles étant le nombre de balles à envoyer.
     * @param effet étant l'effet appliqué aux balles tout au long de la séance.
     * @param puissance étant la puissance de la balle envoyée.
     * @param rotation étant la rotation du lanceur.
     */
    public Seance(int frequence, int nombreBalles, String effet, int intensiteEffet, int puissance, int rotation)
    {
        this.frequence = frequence;
        this.nombreBalles = nombreBalles;
        this.effet = effet;
        this.intensiteEffet = intensiteEffet;
        this.puissance = puissance;
        this.rotation = rotation;
        this.zoneObjectif = -1;
        this.zoneRobot = -1;
        this.tauxReussite = 0;
        this.dateDebut = horodaterBD();
        this.dateFin = horodaterBD();
        this.idJoueur = -1;
    }

    public String horodaterBD()
    {
        Calendar calendar = Calendar.getInstance();
        // Format SQLite : "2018-04-28 14:11:52"
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        final String strDate = simpleDateFormat.format(calendar.getTime());
        return strDate;
    }

    /**
     * Méthode getId accesseur de l'attribut id.
     *
     * @return id
     */
    public int getId() { return id; }

    /**
     * Méthode setId mutateur de l'attribut id.
     *
     * @param id étant l'id à affecter.
     */
    public void setId(int id) { this.id = id; }

    /**
     * Méthode getFrequence accesseur de l'attribut frequence.
     *
     * @return frequence
     */
    public int getFrequence()
    {
        return frequence;
    }

    /**
     * Méthode setFrequence mutateur de l'attribut frequence.
     *
     * @param frequence étant la fréquence à affecter.
     */
    public void setFrequence(int frequence)
    {
        this.frequence = frequence;
    }

    /**
     * Méthode getNombreBalles accesseur de l'attribut nombreBalles.
     *
     * @return nombreBalles
     */
    public int getNombreBalles()
    {
        return nombreBalles;
    }

    /**
     * Méthode setNombreBalles mutateur de l'attribut nombreBalles.
     *
     * @param nombreBalles étant le nombre de balles à affecter.
     */
    public void setNombreBalles(int nombreBalles)
    {
        this.nombreBalles = nombreBalles;
    }

    /**
     * Méthode setEffet mutateur de l'attribut effet.
     *
     * @param effet étant l'effet à affecter.
     */
    public void setEffet(String effet)
    {
        this.effet = effet;
    }

    /**
     * Méthode getEffet accesseur de l'attribut effet.
     *
     * @return effet
     */
    public String getEffet() { return effet; }

    /**
     * Méthode setIntensiteEffet mutateur de l'attribut intensiteEffet.
     *
     * @param intensiteEffet étant l'intensité de l'effet à affecter.
     */
    public void setIntensiteEffet(int intensiteEffet)
    {
        this.intensiteEffet = intensiteEffet;
    }

    /**
     * Méthode getIntensiteEffet accesseur de l'attribut intensiteEffet.
     *
     * @return intensiteEffet
     */
    final int getIntensiteEffet() { return intensiteEffet; }

    /**
     * Méthode getTauxReussite accesseur de l'attribut tauxReussite.
     *
     * @return
     */
    public float getTauxReussite()
    {
        return tauxReussite;
    }

    /**
     * Méthode setTauxReussite mutateur de l'attribut tauxReussite.
     *
     * @param tauxReussite étant le tausx de réussite à affecter.
     */
    public void setTauxReussite(float tauxReussite) { this.tauxReussite = tauxReussite; }

    public int getPuissance()
    {
        return puissance;
    }

    public void setPuissance(int puissance)
    {
        this.puissance = puissance;
    }

    public int getRotation()
    {
        return rotation;
    }

    public void setRotation(int rotation)
    {
        this.rotation = rotation;
    }

    public int getZoneObjectif()
    {
        return zoneObjectif;
    }

    public void setZoneObjectif(int zoneObjectif)
    {
        this.zoneObjectif = zoneObjectif;
    }

    public int getZoneRobot()
    {
        return zoneRobot;
    }

    public void setZoneRobot(int zoneRobot)
    {
        this.zoneRobot = zoneRobot;
    }

    public String getDateDebut()
    {
        return dateDebut;
    }

    public void setDateDebut(String dateDebut)
    {
        this.dateDebut = dateDebut;
    }

    public String getDateFin()
    {
        return dateFin;
    }

    public void setDateFin(String dateFin)
    {
        this.dateFin = dateFin;
    }

    public int getIdJoueur()
    {
        return idJoueur;
    }

    public void setIdJoueur(int idJoueur)
    {
        this.idJoueur = idJoueur;
    }

    /**
     * Méthode toString permettant la visualisation des caractéristiques de la séance.
     *
     * @return les caractéristiques de la séance.
     */
    public String toString()
    {
        return "Frequence : " + frequence + "\nNombre balles : " + nombreBalles + "\nEffet : " + effet + "\nIntensité effet : " + intensiteEffet + "\nTaux réussite : " + tauxReussite + "\nPuissance : " + puissance + "\nRotation robot : " + rotation + "\nZone objectif : " + zoneObjectif + "\nZone robot : " + zoneRobot + "\nDate début : " + dateDebut + "\nDate fin : " + dateFin + "\nID joueur : " + idJoueur;
    }
}