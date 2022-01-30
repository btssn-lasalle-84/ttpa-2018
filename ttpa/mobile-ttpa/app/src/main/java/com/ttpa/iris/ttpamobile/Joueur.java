package com.ttpa.iris.ttpamobile;

/**
 * Created by smaniotto on 10/04/18.
 */

public class Joueur
{
    private int id;
    private String nom;

    public Joueur()
    {
    }

    public Joueur(String nom)
    {
        this.nom = nom;
    }

    public int getId()
    {
        return id;
    }

    public void setId(int id)
    {
        this.id = id;
    }

    public String getNom()
    {
        return nom;
    }

    public void setNom(String nom)
    {
        this.nom = nom;
    }

    /**
     * Méthode toString permettant la visualisation des caractéristiques d'un joueur.
     *
     * @return String les caractéristiques d'un joueur.
     */
    public String toString()
    {
        return "Nom : " + nom;
    }
}
