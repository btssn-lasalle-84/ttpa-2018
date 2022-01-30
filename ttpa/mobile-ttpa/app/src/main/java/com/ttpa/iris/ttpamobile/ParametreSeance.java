package com.ttpa.iris.ttpamobile;

/**
 * Created by smaniotto on 19/03/18.
 */

/**
 * Classe ParametreSeance définnissant les caractéristiques et le comportement d'un paramètre de séance.
 */
public class ParametreSeance
{
    /*
     * Attributs de la classe ParametreSeance.
     */
    private String nomJoueur;
    private String effetBalles;
    private int intensiteEffet;
    private int puissanceBalles;
    private int frequenceBalles;
    private int rotation;
    private int nombreBalles;

    /* Normes des caractéristiques d'un paramétrage */
    public static final int    MIN_FREQUENCE_PARAMETRE = 1;
    public static final int    MAX_FREQUENCE_PARAMETRE = 60;
    public static final int    MIN_NOMBRE_BALLES_PARAMETRE = 1;
    public static final int    MAX_NOMBRE_BALLES_PARAMETRE = 50;
    public static final int    MIN_PUISSANCE_BALLES_PARAMETRE = 1;
    public static final int    MAX_PUISSANCE_BALLES_PARAMETRE = 10;
    public static final int    MIN_ROTATION_PARAMETRE = 0;
    public static final int    MAX_ROTATION_PARAMETRE = 180;

    /**
     * Méthode ParametreSeance constructeur par défaut de la classe ParametreSeance.
     */
    public ParametreSeance() {}

    /**
     * Méthode ParametreSeance constructeur de la classe ParametreSeance.
     *
     * @param nomJoueur étant le nom du joueur pratiquant la séance.
     * @param effetBalles étant l'effet appliqué aux balles tout au long de la séance.
     * @param intensiteEffet étant l'intensité de l'effet appliqué aux balles tout au long de la séance.
     * @param frequenceBalles étant la fréquence d'envoi des balles (en balles/minute) de la séance.
     * @param puissanceBalles étant la puissance ou coefficiant de vitesse balles à envoyer.
     * @param rotation étant la rotation du lanceur en degrés.
     * @param nombreBalles étant le nombre de balles à envoyer.
     */
    public ParametreSeance(String nomJoueur, String effetBalles, int intensiteEffet, int puissanceBalles, int frequenceBalles, int rotation, int nombreBalles)
    {
        this.nomJoueur = nomJoueur;
        this.effetBalles = effetBalles;
        this.intensiteEffet = intensiteEffet;
        this.puissanceBalles = puissanceBalles;
        this.frequenceBalles = frequenceBalles;
        this.rotation = rotation;
        this.nombreBalles = nombreBalles;
    }

    /**
     * Méthode getNomJoueur() accesseur de l'attribut nomJoueur.
     *
     * @return nomJoueur
     */
    public String getNomJoueur() { return nomJoueur; }

    /**
     * Méthode setNomJoueur mutateur de l'attribut nomJoueur.
     *
     * @param nomJoueur étant le nom à affecter
     */
    public void setNomJoueur(String nomJoueur) { this.nomJoueur = nomJoueur; }

    /**
     * Méthode getFrequenceBalles() accesseur de l'attribut frequenceBalles.
     *
     * @return frequenceBalles
     */
    public int getFrequenceBalles()
    {
        return frequenceBalles;
    }

    /**
     * Méthode setFrequenceBalles mutateur de l'attribut frequenceBalles.
     *
     * @param frequenceBalles étant la fréquence à affecter.
     */
    public void setFrequenceBalles(int frequenceBalles)
    {
        this.frequenceBalles = frequenceBalles;
    }

    /**
     * Méthode getNombreBalles() accesseur de l'attribut nombreBalles.
     *
     * @return nombreBalles
     */
    public int getNombreBalles()
    {
        return nombreBalles;
    }

    /**
     * Méthode setNombreBalles() mutateur de l'attribut nombreBalles.
     *
     * @param nombreBalles étant le nombre de balles à affecter.
     */
    public void setNombreBalles(int nombreBalles)
    {
        this.nombreBalles = nombreBalles;
    }

    /**
     * Méthode getEffetBalles() accesseur de l'attribut effetBalles.
     *
     * @return effetBalles
     */
    public String getEffetBalles() { return effetBalles; }

    /**
     * Méthode getEffet vérifiant la validité de l'effet du paramétrage.
     *
     * @return char l'effet pour la trame
     */
    final char getEffet()
    {
        switch (this.effetBalles)
        {
            case "Lifté":
                return 'L';
            case "Aucun":
                return 'S';
            case "Coupé":
                return 'C';
            default:
                return 'S';
        }
    }

    /**
     * Méthode getEffetComplet accesseur de l'attribut effetBalles.
     *
     * @return String l'effet pour la trame
     */
    final String getEffetComplet() { return this.effetBalles; }

    /**
     * Méthode setEffet mutateur de l'attribut effetBalles.
     *
     * @param effetBalles étant l'effet à affecter.
     */
    public void setEffetBalles(String effetBalles)
    {
        this.effetBalles = effetBalles;
    }

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
     * @return intensiteEffet l'effet pour la trame
     */
    final int getIntensiteEffet() { return this.intensiteEffet; }

    public int getPuissanceBalles()
    {
        return puissanceBalles;
    }

    public void setPuissanceBalles(int puissanceBalles)
    {
        this.puissanceBalles = puissanceBalles;
    }

    public int getRotation()
    {
        return rotation;
    }

    public void setRotation(int rotation)
    {
        this.rotation = rotation;
    }

    /**
     * Méthode toString permettant la visualisation des caractéristiques du paramétrage.
     *
     * @return les caractéristiques du paramétrage.
     */
    public String toString()
    {
        return "Frequence : " + frequenceBalles + "\nNombre balles : " + nombreBalles + "\nEffet : " + effetBalles + "\nIntensité Effet : " + intensiteEffet + "\nPuissance balles : " + puissanceBalles;
    }

    /**
     * Méthode estValide vérifiant la validité des caratéristiques du paramétrage.
     *
     * @return
     */
    public boolean estValide()
    {
        return (frequenceEstValide(this.frequenceBalles) && nombreBallesEstValide(this.nombreBalles) && effetEstValide(this.effetBalles) && puissanceBallesEstValide(this.puissanceBalles) && rotationEstValide(this.rotation));
    }

    /**
     * Méthode frequenceEstValide vérifiant la validité de la fréquence du paramétrage.
     *
     * @return
     */
    final public boolean frequenceEstValide(int frequence)
    {
        return (frequence >= MIN_FREQUENCE_PARAMETRE && frequence <= MAX_FREQUENCE_PARAMETRE);
    }

    /**
     * Méthode nombreBallesEstValide vérifiant la validité du nombre de balles du paramétrage.
     *
     * @param nombreBalles étant le nombre de balles à vérifier.
     * @return
     */
    final public boolean nombreBallesEstValide(int nombreBalles)
    {
        return (nombreBalles >= MIN_NOMBRE_BALLES_PARAMETRE && nombreBalles <= MAX_NOMBRE_BALLES_PARAMETRE);
    }

    /**
     * Méthode puissanceBallesEstValide vérifiant la validité de la puissance des balles.
     *
     * @param puissanceBalles étant la puissance
     * @return
     */
    final public boolean puissanceBallesEstValide(int puissanceBalles)
    {
        return (puissanceBalles >= MIN_PUISSANCE_BALLES_PARAMETRE && puissanceBalles <= MAX_PUISSANCE_BALLES_PARAMETRE);
    }

    /**
     * Méthode rotationEstValide vérifiant la validité de la rotation.
     *
     * @param rotation étant la rotation
     * @return
     */
    final public boolean rotationEstValide(int rotation)
    {
        return (rotation >= MIN_ROTATION_PARAMETRE && rotation <= MAX_ROTATION_PARAMETRE);
    }

    /**
     * Méthode effetEstValide vérifiant la validité de l'effet du paramétrage.
     *
     * @param effet étant l'effet à vérifier.
     * @return
     */
    final public boolean effetEstValide(String effet)
    {
        switch (effet)
        {
            case "Lifté":
                return true;
            case "Aucun":
                return true;
            case "Coupé":
                return true;
            default:
                return false;
        }
    }
}