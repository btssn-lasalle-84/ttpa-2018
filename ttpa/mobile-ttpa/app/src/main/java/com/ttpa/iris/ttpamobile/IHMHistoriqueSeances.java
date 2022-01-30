package com.ttpa.iris.ttpamobile;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.util.List;

/**
 * Created by smaniotto on 12/04/18.
 */

/**
 * Classe IHMHistoriqueSeances définnissant le comportement du layout 'ecran_historique_seances'.
 */
public class IHMHistoriqueSeances extends AppCompatActivity implements View.OnClickListener {

    /**
     * Attributs de la classe
     */
    private ServeurBDD serveurBDD;
    private int idJoueurActuel;
    private Joueur joueurActuel;

    /**
     * Eléments graphiques de l'IHM
     */
    TableLayout tableauSeances;
    ImageButton boutonPurgerSeances;

    /**
     * Méthode onCreate appellée au démarrage de l'activité permettant l'initialisation des composants.
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ecran_historique_seances);

        // Se connecter à la base de données
        connectionBaseDeDonnees();

        // Affecter les éléments graphiques de l'IHM
        affecterElementsIHM();

        // Récupérer les informations du joueur actuel
        recupererInformationsJoueur();

        // Modifier l'IHM pour afficher le joueur
        modifierIHMAfficherSeancesJoueur();

        // Permettre à tous les boutons de suppression de séance d'être cliquables
        rendreBoutonIHMCliquables();
    }

    /**
     * Méthode onClick() permettant la gestion du click sur un bouton.
     *
     * @param element étant l'élément sur lequel l'utilisateur a clické
     */
    @Override
    public void onClick(View element)
    {
        Log.d("IHMHistoriqueSeances", "onClick()");

        Seance seance = null;

        if(element == boutonPurgerSeances)
        {
            // Purger les séances déjà existantes du joueur
            purgerSeancesJoueur();
        }
        else // l'élément est un bouton du champ action
        {
            // Retrouver l'ID de la séance sélectionnée
            for (int i = 1; i < tableauSeances.getChildCount(); ++i) {

                TableRow ligne = (TableRow) tableauSeances.getChildAt(i);

                if (ligne == element.getParent()) {
                    TextView texteIdSeance = (TextView) ligne.getChildAt(0); // 0 : Position du champ ID de la ligne
                    int idSeanceElementSelectionne = Integer.parseInt(texteIdSeance.getText().toString()); // ID de la séance à supprimer

                    // Afficher la boite de dialogue de la séance sélectionnée
                    afficherBoiteDialogueDetailsSeance(idSeanceElementSelectionne);
                }
            }
        }
    }

    /**
     * Méthode afficherSeance() permettant d'afficher dans un tableau une séance.
     *
     * @param seance étant la séance à afficher
     */
    private void afficherSeance(Seance seance)
    {
        Log.d("IHMHistoriqueSeances", "afficherSeance()");

        TableLayout tableauSeances = (TableLayout) findViewById(R.id.tableauSeances);

        // Déclaration des paramètres visuels des champs
        TableRow.LayoutParams lpLigne = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT);
        TableRow.LayoutParams lpChamp = new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.MATCH_PARENT, 1f);


        // Déclaration et paramétrage de la ligne accueillant la séance
        TableRow ligne= new TableRow(this);
        ligne.setLayoutParams(lpLigne);
        float taille_police_ecriture = (float)Integer.parseInt(getString(R.string.taille_police_historique));

        // Déclaration et paramétrage du champ ID
        TextView texteSeanceId = new TextView(this);
        texteSeanceId.setLayoutParams(lpChamp);
        texteSeanceId.setGravity(Gravity.CENTER);
        texteSeanceId.setVisibility(View.GONE);
        texteSeanceId.setText(Integer.toString(seance.getId()));

        // Déclaration et paramétrage du champ Date
        TextView texteSeanceDate = new TextView(this);
        texteSeanceDate.setLayoutParams(lpChamp);
        texteSeanceDate.setGravity(Gravity.CENTER);
        texteSeanceDate.setTextSize(taille_police_ecriture);
        texteSeanceDate.setText(seance.getDateDebut());

        // Déclaration et paramétrage du champ Fréquence
        TextView texteSeanceFrequence = new TextView(this);
        texteSeanceFrequence.setLayoutParams(lpChamp);
        texteSeanceFrequence.setGravity(Gravity.CENTER);
        texteSeanceFrequence.setTextSize(taille_police_ecriture);
        texteSeanceFrequence.setText(Integer.toString(seance.getFrequence()) + " balles/min");

        // Déclaration et paramétrage du champ Nb Balles
        TextView texteSeanceNombreBalles = new TextView(this);
        texteSeanceNombreBalles.setLayoutParams(lpChamp);
        texteSeanceNombreBalles.setGravity(Gravity.CENTER);
        texteSeanceNombreBalles.setTextSize(taille_police_ecriture);
        texteSeanceNombreBalles.setText(Integer.toString(seance.getNombreBalles()));

        // Déclaration et paramétrage du champ Effet
        TextView texteSeanceEffet = new TextView(this);
        texteSeanceEffet.setLayoutParams(lpChamp);
        texteSeanceEffet.setGravity(Gravity.CENTER);
        texteSeanceEffet.setTextSize(taille_police_ecriture);
        texteSeanceEffet.setText(seance.getEffet());

        // Déclaration et paramétrage du champ Taux Réussite
        TextView texteSeanceTauxReussite = new TextView(this);
        texteSeanceTauxReussite.setLayoutParams(lpChamp);
        texteSeanceTauxReussite.setGravity(Gravity.CENTER);
        texteSeanceTauxReussite.setTextSize(taille_police_ecriture);
        texteSeanceTauxReussite.setText(Float.toString(seance.getTauxReussite()) + "%");

        // Déclaration et paramétrage du champ Action
        ImageButton boutonDetailsSeance = new ImageButton(this);
        boutonDetailsSeance.setLayoutParams(lpChamp);

        boutonDetailsSeance.setBackgroundColor(Color.argb(0, 0, 0, 0)); // Rendre le fond transparent
        boutonDetailsSeance.setImageResource(R.drawable.ic_loupe_afficher);
        boutonDetailsSeance.setOnClickListener(this); // Rendre le bouton cliquable

        // Ajouter les champs à la ligne
        ligne.addView(texteSeanceId);
        ligne.addView(texteSeanceDate);
        ligne.addView(texteSeanceFrequence);
        ligne.addView(texteSeanceNombreBalles);
        ligne.addView(texteSeanceEffet);
        ligne.addView(texteSeanceTauxReussite);
        ligne.addView(boutonDetailsSeance);

        // Ajouter la ligne au tableau des séances à l'index 1 : après l'entête du tableau
        tableauSeances.addView(ligne, 1);
    }

    /**
     * Méthode supprimerSeanceSelectionnee() permettant la suppression d'une séance précise.
     *
     * @param idSeanceElementSelectionne étant l'ID de la séance à supprimer
     * @param indexLigneTableau étant son index dans le tableau
     */
    private void supprimerSeanceSelectionnee(int idSeanceElementSelectionne, int indexLigneTableau)
    {
        Log.d("IHMHistoriqueSeances", "supprimerSeanceSelectionnee()");

        // Supprimer la séance selectionnée de la base de données
        serveurBDD.supprimerSeance(idSeanceElementSelectionne);

        // Supprimer le contenu de la ligne correspondante du tableau des séances
        TableLayout tableauSeances = (TableLayout)findViewById(R.id.tableauSeances);
        TableRow ligne = (TableRow) tableauSeances.getChildAt(indexLigneTableau);
        tableauSeances.removeView(ligne);

        Toast.makeText(getApplicationContext(), "Suppression de la séance " + idSeanceElementSelectionne + "...", Toast.LENGTH_SHORT).show();
    }

    /**
     * Méthode connectionBaseDeDonnees() permettant de se connecter à la base de données.
     */
    private void connectionBaseDeDonnees()
    {
        Log.d("IHMHistoriqueSeances", "connectionBaseDeDonnees()");

        serveurBDD = new ServeurBDD(this);
        serveurBDD.open();
    }

    /**
     * Méthode affecterElementsIHM() permettant d'affecter les élements graphiques actuels de l'IHM.
     */
    private void affecterElementsIHM()
    {
        Log.d("IHMHistoriqueSeances", "affecterElementsIHM()");

        // Tableau des séances
        tableauSeances = (TableLayout)findViewById(R.id.tableauSeances);
        // Bouton de puirge des séances du joueur
        boutonPurgerSeances = (ImageButton)findViewById(R.id.boutonPurgerSeances);
    }

    /**
     * Méthode recupererInformationsJoueur() affectant aux attributs les caratéristiques du joueur actuel, importées depuis la base de données.
     */
    private void recupererInformationsJoueur()
    {
        Log.d("IHMHistoriqueSeances", "recupererInformationsJoueur()");

        idJoueurActuel = serveurBDD.getIdJoueurParametres();
        joueurActuel = serveurBDD.getJoueur(idJoueurActuel);
    }

    /**
     * Méthode modifierIHMAfficherSeancesJoueur() affichant les séances du jouer actuel dans un tableau de l'IHM.
     */
    private void modifierIHMAfficherSeancesJoueur()
    {
        Log.d("IHMHistoriqueSeances", "modifierIHMAfficherSeancesJoueur()");

        TextView texteHistoriqueSeances = (TextView) findViewById(R.id.texteHistoriqueSeances);
        texteHistoriqueSeances.setText("Historique des séances de " + joueurActuel.getNom() + " :");

        // Récupérer la liste des séances existantes du joueur
        List<Seance> seances = serveurBDD.getSeances(joueurActuel.getId());
        Log.d("IHMHistoriqueSeances", "modifierIHMAfficherSeancesJoueur() Nombre de séances du joueur: " + seances.size());
        // Pour chaque séance existante
        for(int i = 0; i < seances.size(); i++)
        {
            Log.d("IHMHistoriqueSeances", "modifierIHMAfficherSeancesJoueur() Séance n° " + seances.get(i).getId() + ": \n" + seances.get(i).toString());

            // Ajouter la séance dans le tableau de séances
            afficherSeance(seances.get(i));
        }
    }

    /**
     * Méthode rendreBoutonIHMCliquables() permettant aux boutons de l'IHM d'être cliquables.
     */
    private void rendreBoutonIHMCliquables()
    {
        Log.d("IHMHistoriqueSeances", "rendreBoutonIHMCliquables()");

        // Boutons présents dans le tableau d'historique de séances du joueur
        int compteurLignes = tableauSeances.getChildCount();
        for (int i = 0; i < compteurLignes; ++i) {
            TableRow ligne = (TableRow) tableauSeances.getChildAt(i);

            if (ligne.getChildAt(ligne.getChildCount() - 1) instanceof ImageButton) {
                View element = ligne.getChildAt(ligne.getChildCount() - 1); // Le bouton de suppression de séance étant le dernier élément de la ligne
                element.setOnClickListener(this);
            }
        }

        // Bouton de purge de séance du joueur
        boutonPurgerSeances.setOnClickListener(this);
    }

    /**
     * Méthode afficherBoiteDialogueDetailsSeance() affichant une boite de dialogue avec les informations détaillées de la séance sélectionnée.
     *
     * @param idSeanceSelectionnee étant l'id de la séance à afficher
     */
    private void afficherBoiteDialogueDetailsSeance(int idSeanceSelectionnee)
    {
        Log.d("IHMHistoriqueSeances", "afficherBoiteDialogueDetailsSeance()");

        // Retrouver l'objet Seance correspondant grâce à la base de données
        Seance seanceSelectionnee = serveurBDD.getSeance(idSeanceSelectionnee);

        // Afficher la boîte de dialogue
        AlertDialog.Builder detailsSeance = new AlertDialog.Builder(this);
        LayoutInflater factory = LayoutInflater.from(this);
        final View detailsSeanceView = factory.inflate(R.layout.details_seance, null);
        detailsSeance.setView(detailsSeanceView);

        detailsSeance.setTitle("Informations complémentaires de la séance");

        // Affecter les informations de la séance aux champs correspondants
        TextView texteValeurZoneRobot = (TextView) detailsSeanceView.findViewById(R.id.texteValeurZoneRobot);
        TextView texteValeurZoneObjectif = (TextView) detailsSeanceView.findViewById(R.id.texteValeurZoneObjectif);

        switch(seanceSelectionnee.getZoneRobot())
        {
            case 0:
            case -1:
                texteValeurZoneRobot.setText("Aucune");
                break;
            default:
                texteValeurZoneRobot.setText("ZONE " + seanceSelectionnee.getZoneRobot());
                break;
        }

        switch(seanceSelectionnee.getZoneObjectif())
        {
            case 0:
            case -1:
                texteValeurZoneObjectif.setText("Aucune");
                break;
            default:
                texteValeurZoneObjectif.setText("ZONE " + seanceSelectionnee.getZoneObjectif());
                break;
        }

        detailsSeance.setNegativeButton("Retour", new DialogInterface.OnClickListener()
        {
            public void onClick(DialogInterface dialog, int which)
            {

            }
        });

        detailsSeance.show();
    }

    /**
     * Méthode purgerSeancesJoueur() affichant une boîte de dialogue qui va supprimer les séances du joueur actuel selon la décision prise.
     */
    private void purgerSeancesJoueur()
    {
        Log.d("IHMHistoriqueSeances", "purgerSeancesJoueur()");

        // Afficher la boite de dialogue
        AlertDialog.Builder detailsSeance = new AlertDialog.Builder(this);

        detailsSeance.setMessage("Vous êtes sur le point de supprimer définitivement les séance du joueur " + joueurActuel.getNom() + ".");

        detailsSeance.setPositiveButton("Continuer", new DialogInterface.OnClickListener()
        {
            public void onClick(DialogInterface dialog, int which)
            {
                // Purger les séances déjà existantes du joueur
                Toast.makeText(getApplicationContext(), "Purge des séances...", Toast.LENGTH_SHORT).show();

                // Purger la table des séances de la base de données
                serveurBDD.purgerSeancesJoueur(idJoueurActuel);

                // Supprimer le contenu du tableau des séances
                int compteur = tableauSeances.getChildCount();
                for (int i = 1; i < compteur; i++) { // On ne supprime pas la 1ere ligne qui est l'entête
                    View child = tableauSeances.getChildAt(i);
                    if (child instanceof TableRow) ((ViewGroup) child).removeAllViews();
                }
            }
        });

        detailsSeance.setNegativeButton("Annuler", new DialogInterface.OnClickListener()
        {
            public void onClick(DialogInterface dialog, int which)
            {

            }
        });

        detailsSeance.show();
    }
}
