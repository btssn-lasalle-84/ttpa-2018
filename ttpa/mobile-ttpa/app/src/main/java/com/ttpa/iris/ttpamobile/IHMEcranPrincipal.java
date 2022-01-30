package com.ttpa.iris.ttpamobile;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

/**
 * Created by smaniotto on 05/04/18.
 */

/**
 * Classe IHMEcranPrincipal définnissant le comportement du layout 'ecran_principal'.
 */
public class IHMEcranPrincipal extends AppCompatActivity implements View.OnClickListener, SeekBar.OnSeekBarChangeListener {

    /**
     * Définitions
     */
    // Différentes couleurs des boutons
    private final static int COULEUR_BOUTON_VERT = Color.parseColor("#5eed7b");
    private final static int COULEUR_BOUTON_ORANGE = Color.parseColor("#f7bb31");
    private final static int COULEUR_BOUTON_GRIS = Color.parseColor("#c0c5c6");
    private final static int COULEUR_BOUTON_ROUGE = Color.parseColor("#ee5e5e");

    // Différents états possibles d'une séance
    private final static int ETAT_SEANCE_ARRETEE = 0;
    private final static int ETAT_SEANCE_DEMARREE = 1;
    private final static int ETAT_SEANCE_PAUSE = 2;

    // Noms des différents périphériques BlueTooth des systèmes du projet
    private final static String NOM_PERIPHERIQUE_BLUETOOTH_ECRAN = "TTPA-Ecran";
    private final static String NOM_PERIPHERIQUE_BLUETOOTH_TABLE = "TTPA-Table";
    private final static String NOM_PERIPHERIQUE_BLUETOOTH_LANCEUR = "TTPA-Lanceur";

    // Conventions de trames
    private final static String TRAME_ENTETE = "$TTPA";
    private final static String TRAME_FIN = "\r\n"; // CR+LF en ASCII

    // Trames dédiées à l'écran
    private final static String TRAME_ECRAN_DEBUT_SEANCE = TRAME_ENTETE + ":START";
    private final static String TRAME_ECRAN_PAUSE_SEANCE = TRAME_ENTETE + ":PAUSE";
    private final static String TRAME_ECRAN_REPRISE_SEANCE = TRAME_ENTETE + ":RESUME";
    private final static String TRAME_ECRAN_FIN_SEANCE = TRAME_ENTETE + ":FINSEANCE";

    // Trames dédiées au lanceur
    private final static String TRAME_LANCEUR_PAUSE_SEANCE = TRAME_ENTETE + ":PAUSE:" + TRAME_FIN;
    private final static String TRAME_LANCEUR_REPRISE_SEANCE = TRAME_ENTETE + ":RESUME:" + TRAME_FIN;
    private final static String TRAME_LANCEUR_ARRET_SEANCE = TRAME_ENTETE + ":STOP:" + TRAME_FIN;
    private final static String TRAME_LANCEUR_PING = TRAME_ENTETE + ":PING:" + TRAME_FIN;

    // Trames dédiées à la table
    private final static String TRAME_TABLE_ARRET_SEANCE = TRAME_ENTETE + ":RESET";

    private final static int REQUEST_CODE_ENABLE_BLUETOOTH = 0;

    /**
     * Gestion des zones
     */
    private final static int SELECTION_ZONE_OBJECTIF = 0;
    private final static int SELECTION_ZONE_ROBOT = 1;

    int idZonePrecedente = -1;
    int idZonePrecedenteObjectif = -1;
    int idZonePrecedenteRobot = -1;
    String strZoneObjectif = new String("ZONE 0");
    int zoneObjectif = 0;
    String strZoneRobot = new String("ZONE 0");
    int zoneRobot = 0;

    /**
     * Ressources IHM
     */
    private ImageButton boutonBluetooth;
    private ImageButton boutonHistorique;
    private ImageButton boutonParametres;
    private ImageView voyantTable;
    private ImageView voyantLanceur;
    private ImageView voyantEcran;
    private ImageView boutonActionSeance;
    private ImageView boutonArreterSeance;
    private Spinner spinnerListeJoueurs;
    private ImageButton boutonAppliquerNomJoueur;
    private SeekBar barreProgressionNombreBalles;
    private TextView texteValeurNombreBalles;
    private SeekBar barreProgressionFrequenceBalles;
    private TextView texteValeurFrequenceBalles;
    private SeekBar barreProgressionEffetBalles;
    private TextView texteValeurEffetBalles;
    private SeekBar barreProgressionPuissanceBalles;
    private TextView texteValeurPuissanceBalles;
    private SeekBar barreProgressionRotationLanceur;
    private TextView texteValeurRotationLanceur;

    /**
     * Attributs de la classe
     */
    private ServeurBDD serveurBDD;
    private int etatSeance = ETAT_SEANCE_ARRETEE;
    private Seance seanceEnCours;
    private int ballesJouees;
    private int ballesReussies;
    public ParametreSeance parametresActuels = new ParametreSeance();
    private String nomJoueur;
    private BluetoothAdapter adaptateurBluetooth = null;
    private Set<BluetoothDevice> devices;
    List<PeripheriqueBluetooth> peripheriques;
    PeripheriqueBluetooth peripheriqueBluetoothEcran = null;
    PeripheriqueBluetooth peripheriqueBluetoothTable = null;
    PeripheriqueBluetooth peripheriqueBluetoothLanceur = null;

    // Gère les communications avec le thread réseau
    final private Handler handler = new Handler()
    {
        @Override
        public void handleMessage(Message msg)
        {
            super.handleMessage(msg);

            Bundle b = msg.getData();

            switch(b.getInt("etat"))
            {
                case PeripheriqueBluetooth.CODE_CONNEXION:
                    System.out.println("<Bluetooth> Connexion " + b.getString("nom") + " [" + b.getString("adresse") + "] ok");
                    Log.d("IHMEcranPrincipal","<Bluetooth> Connexion " + b.getString("nom") + " [" + b.getString("adresse") + "] ok");
                    verifierConnexionAppareilsBluetoothRequis();
                    switch (b.getString("nom"))
                    {
                        case NOM_PERIPHERIQUE_BLUETOOTH_LANCEUR:
                            peripheriqueBluetoothLanceur.envoyer(TRAME_LANCEUR_PING);
                            break;
                    }
                    break;
                case PeripheriqueBluetooth.CODE_RECEPTION:
                    String donnees = b.getString("donnees");
                    if(donnees.contains("\r\n"))
                    {
                        System.out.println("<Bluetooth> Données reçues " + b.getString("nom") + " [" + b.getString("adresse") + "] : " + donnees.replace("\r\n", ""));
                        Log.d("IHMEcranPrincipal","<Bluetooth> Données reçues " + b.getString("nom") + " [" + b.getString("adresse") + "] : " + donnees.replace("\r\n", ""));
                    }
                    else
                    {
                        System.out.println("<Bluetooth> Données reçues " + b.getString("nom") + " [" + b.getString("adresse") + "] : " + donnees);
                        Log.d("IHMEcranPrincipal","<Bluetooth> Données reçues " + b.getString("nom") + " [" + b.getString("adresse") + "] : " + donnees);
                    }
                    traiterDonneesRecues(b.getString("nom"), donnees.replace("\r\n", ""));

                    break;
                case PeripheriqueBluetooth.CODE_DECONNEXION:
                    System.out.println("<Bluetooth> Déconnexion " + b.getString("nom") + " [" + b.getString("adresse") + "] ok");
                    Log.d("IHMEcranPrincipal","<Bluetooth> Déconnexion " + b.getString("nom") + " [" + b.getString("adresse") + "] ok");
                    verifierConnexionAppareilsBluetoothRequis();
                    break;
                default:
                    System.out.println("<Bluetooth> état inconnu !");
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        Log.d("IHMEcranPrincipal", "onCreate()");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.ecran_principal);

        // Affectation des membres de l'IHM
        affecterMembresIHM();

        // Modifier les valeurs des champs de paramètre de l'IHM en fonction des états des barres de progression
        modifierValeursParametresIHM();

        // Rendre les boutons présents dans l'IHM cliquables
        rendreBoutonsIHMCliquables();

        // Rendre les barres de progression présents dans l'IHM utilisables
        rendreBarresProgressionIHMUtilisables();

        // Connexion à la base de données
        connexionBaseDeDonnees();

        // Crée une liste des joueurs enregistrés
        creerListeJoueurs();

        // Appliquer les paramètres de séance actuels
        appliquerParametresSeance();

        // Démarrage du Bluetooth puis connexion aux appareils du projet détectés
        demarrageBluetooth();
    }

    /**
     * Méthode onCLick pour la gestion de l'évènement d'un click.
     *
     * @param element étant la vue sur laquelle l'utilisateur a cliqué.
     */
    @Override
    public void onClick(View element)
    {
        Log.d("IHMEcranPrincipal", "onClick()");

        if(element == boutonAppliquerNomJoueur)
        {
            ajouterJoueur();
        }
        else if(element == boutonBluetooth)
        {
            if(!verifierConnexionAppareilsBluetoothRequis()) // Si tous les appareils Bluetooth ne sont pas connectés
            {
                //Toast.makeText(getApplicationContext(), "Attendre..", Toast.LENGTH_SHORT).show();
                demarrageBluetooth();
            }
        }
        else if(element == boutonHistorique)
        {
            Log.d("IHMEcranPrincipal", "onClick() boutonHistorique");

            // Redirection vers l'activité de l'historique des séances
            redirectionActiviteHistorique();
        }
        else if(element == boutonParametres)
        {
            // Gérer le click sur le bouton paramétres (engrenage)
            selectionnerZone(SELECTION_ZONE_ROBOT, 0, 0);
        }
        else if(element == boutonActionSeance)
        {
            Log.d("IHMEcranPrincipal", "onClick() boutonActionSeance");
            parametresActuels.setNomJoueur(nomJoueur);
            envoyerTrameConnexionPeripheriqueBluetoothEcran();
            actionnerSeance();
        }
        else if(element == boutonArreterSeance)
        {
            Log.d("IHMEcranPrincipal", "onClick() boutonArreterSeance");
            arreterSeance(false);
        }
    }

    /**
     * Méthode onProgressChanged() appellée lorsqu'une barre de progression est modifiée.
     *
     * @param seekBar étant la barre de progression modifiée
     * @param progress étant le progrès actuel de la barre de progression modifiée
     * @param fromUser
     */
    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
    {
        modifierValeursParametresIHM();
    }

    /**
     * Méthode onStartTrackingTouch() appellée lorsqu'une barre de progression commence à être modifiée.
     *
     * @param seekBar étant la barre de progression modifiée
     */
    @Override
    public void onStartTrackingTouch(SeekBar seekBar)
    {}

    /**
     * Méthode onStopTrackingTouch() appellée lorsqu'une barre de progression a fini d'être modifiée.
     *
     * @param seekBar étant la barre de progression modifiée
     */
    @Override
    public void onStopTrackingTouch(SeekBar seekBar)
    {
        //appliquerParametresSeance();
    }

    /**
     * Méthode affecterMembresIHM() permettant l'affectation des membres de l'IHM (boutons, barres de progressions, textes, ...) aux attributs correspondants.
     */
    private void affecterMembresIHM()
    {
        Log.d("IHMEcranPrincipal", "affecterMembresIHM()");

        //editTextNomJoueur = (EditText) findViewById(R.id.editTextNomJoueur);
        boutonAppliquerNomJoueur = (ImageButton) findViewById(R.id.boutonAppliquerNomJoueur);
        boutonBluetooth = (ImageButton) findViewById(R.id.boutonBluetooth);
        boutonHistorique = (ImageButton) findViewById(R.id.boutonHistorique);
        boutonParametres = (ImageButton) findViewById(R.id.boutonParametres);
        voyantTable = (ImageView) findViewById(R.id.voyantTable);
        voyantLanceur = (ImageView) findViewById(R.id.voyantLanceur);
        voyantEcran = (ImageView) findViewById(R.id.voyantEcran);
        boutonActionSeance = (ImageView) findViewById(R.id.boutonActionSeance);
        boutonArreterSeance = (ImageView) findViewById(R.id.boutonArreterSeance);
        barreProgressionNombreBalles = (SeekBar) findViewById(R.id.barreProgressionNombreBalles);
        texteValeurNombreBalles = (TextView) findViewById(R.id.texteValeurNombreBalles);
        barreProgressionFrequenceBalles = (SeekBar) findViewById(R.id.barreProgressionFrequenceBalles);
        texteValeurFrequenceBalles = (TextView) findViewById(R.id.texteValeurFrequenceBalles);
        barreProgressionEffetBalles = (SeekBar) findViewById(R.id.barreProgressionEffetBalles);
        texteValeurEffetBalles = (TextView) findViewById(R.id.texteValeurEffetBalles);
        barreProgressionPuissanceBalles = (SeekBar) findViewById(R.id.barreProgressionPuissanceBalles);
        texteValeurPuissanceBalles = (TextView) findViewById(R.id.texteValeurPuissanceBalles);
        barreProgressionRotationLanceur = (SeekBar) findViewById(R.id.barreProgressionRotationLanceur);
        texteValeurRotationLanceur = (TextView) findViewById(R.id.texteValeurRotationLanceur);
        spinnerListeJoueurs = (Spinner)findViewById(R.id.spinnerListeJoueurs);
        spinnerListeJoueurs.setContentDescription("La liste des joueurs");
    }

    /**
     * Méthode rendreBoutonsIHMCliquables()
     */
    private void rendreBoutonsIHMCliquables()
    {
        Log.d("IHMEcranPrincipal", "rendreBoutonsIHMCliquables()");

        boutonAppliquerNomJoueur.setOnClickListener(this);
        boutonBluetooth.setOnClickListener(this);
        boutonHistorique.setOnClickListener(this);
        boutonParametres.setOnClickListener(this);
        boutonActionSeance.setOnClickListener(this);
        boutonArreterSeance.setOnClickListener(this);
    }

    /**
     * Méthode rendreBarresProgressionIHMUtilisables()
     */
    private void rendreBarresProgressionIHMUtilisables()
    {
        Log.d("IHMEcranPrincipal", "rendreBarresProgressionIHMUtilisables()");

        barreProgressionNombreBalles.setOnSeekBarChangeListener(this);
        barreProgressionFrequenceBalles.setOnSeekBarChangeListener(this);
        barreProgressionEffetBalles.setOnSeekBarChangeListener(this);
        barreProgressionPuissanceBalles.setOnSeekBarChangeListener(this);
        barreProgressionRotationLanceur.setOnSeekBarChangeListener(this);
    }

    /**
     * Méthode connexionBaseDeDonnees() permettant la création puis la connexion à la base de données.
     */
    private void connexionBaseDeDonnees()
    {
        Log.d("IHMEcranPrincipal", "connexionBaseDeDonnees()");

        serveurBDD = new ServeurBDD(this);
        serveurBDD.open();
    }

    /**
     * Méthode demarrageBluetooth() permettant le déamrrage du Bluetooth puis la connexion automatique aux appareils du projet.
     */
    private void demarrageBluetooth()
    {
        Log.d("IHMEcranPrincipal", "demarrageBluetooth()");

        adaptateurBluetooth = BluetoothAdapter.getDefaultAdapter();
        if (adaptateurBluetooth == null)
        {
            Toast.makeText(getApplicationContext(), "Bluetooth non activé !", Toast.LENGTH_SHORT).show();
            modifierIHMBluetoothInsuffisant();
        }
        else
        {
            if (!adaptateurBluetooth.isEnabled())
            {
                Toast.makeText(getApplicationContext(), "Bluetooth non activé !", Toast.LENGTH_SHORT).show();

                modifierIHMBluetoothInsuffisant();

                Intent activeBlueTooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(activeBlueTooth, REQUEST_CODE_ENABLE_BLUETOOTH);
                //bluetoothAdapter.enable();
            }
            else
            {
                Toast.makeText(getApplicationContext(), "Bluetooth activé, recherche en cours...", Toast.LENGTH_LONG).show();

                // Recherche des périphériques connus
                peripheriques = new ArrayList<PeripheriqueBluetooth>();
                devices = adaptateurBluetooth.getBondedDevices();

                // Connexion aux appareils Bluetooth du projet détectés
                connexionPeripheriquesBluetooth();
            }
        }
    }

    /**
     * Méthode modifierIHMBluetoothInsuffisant() appellée lorsque le Bluetooth n'est pas activé ou que les appareils nécessaires ne sont pas présents/connectés
     */
    private void modifierIHMBluetoothInsuffisant()
    {
        Log.d("IHMEcranPrincipal", "modifierIHMBluetoothInsuffisant()");

        // Arrêter la séance en cours, sans enregistrer
        arreterSeance(false);

        // Modifier la couleur du bouton Bluetooth
        boutonBluetooth.setBackgroundColor(COULEUR_BOUTON_ROUGE);
        // Modifier la couleur du bouton réglages des zones
        boutonParametres.setBackgroundColor(COULEUR_BOUTON_ROUGE);
        // Autoriser le réglage des zones
        boutonParametres.setEnabled(false);
        // Changer l'icône du bouton d'action
        boutonActionSeance.setImageResource(R.drawable.bouton_demarrer_desactive);
        // Empêcher une séance d'être jouée
        boutonActionSeance.setEnabled(false);
    }

    /**
     * Méthode modifierIHMBluetoothSuffisant() appellée lorsque les appareils Bluetooth nécessaires sont connectés.
     */
    private void modifierIHMBluetoothSuffisant()
    {
        Log.d("IHMEcranPrincipal", "modifierIHMBluetoothSuffisant()");

        // Modifier la couleur du bouton Bluetooth
        boutonBluetooth.setBackgroundColor(COULEUR_BOUTON_ORANGE);
        // Modifier la couleur du bouton réglages des zones
        boutonParametres.setBackgroundColor(COULEUR_BOUTON_VERT);
        // Autoriser le réglage des zones
        boutonParametres.setEnabled(true);
        // Changer l'icône du bouton d'action
        boutonActionSeance.setImageResource(R.drawable.bouton_demarrer);
        // Autoriser le déroulement d'une séance
        boutonActionSeance.setEnabled(true);
    }

    /**
     * Méthode modifierIHMBluetoothOperationnel() appellée lorsque tous les appareils Bluetooth sont connectés.
     */
    private void modifierIHMBluetoothOperationnel()
    {
        Log.d("IHMEcranPrincipal", "modifierIHMBluetoothOperationnel()");

        // Modifier la couleur du bouton Bluetooth
        boutonBluetooth.setBackgroundColor(COULEUR_BOUTON_VERT);
        // Modifier la couleur du bouton réglages des zones
        boutonParametres.setBackgroundColor(COULEUR_BOUTON_VERT);
        // Autoriser le réglage des zones
        boutonParametres.setEnabled(true);
        // Changer l'icône du bouton d'action
        boutonActionSeance.setImageResource(R.drawable.bouton_demarrer);
        // Autoriser le déroulement d'une séance
        boutonActionSeance.setEnabled(true);
    }

    /**
     * Méthode connexionPeripheriquesBluetooth() permetant la connexion aux appareils Bluetooth du projet détectés.
     */
    private void connexionPeripheriquesBluetooth()
    {
        Log.d("IHMEcranPrincipal", "connexionPeripheriquesBluetooth()");

        // Déconnexion de tous les appareils avant une possible connexion
        deconnexionPeripheriquesBluetooth();

        for (BluetoothDevice appareilBluetooth : devices)
        {
            //Toast.makeText(getApplicationContext(), "Périphérique = " + appareilBluetooth.getName(), Toast.LENGTH_SHORT).show();
            peripheriques.add(new PeripheriqueBluetooth(appareilBluetooth, handler));

            switch (appareilBluetooth.getName())
            {
                case NOM_PERIPHERIQUE_BLUETOOTH_ECRAN:
                    connexionPeripheriqueBluetoothEcran(appareilBluetooth);
                    break;
                case NOM_PERIPHERIQUE_BLUETOOTH_TABLE:
                    connexionPeripheriqueBluetoothTable(appareilBluetooth);
                    break;
                case NOM_PERIPHERIQUE_BLUETOOTH_LANCEUR:
                    connexionPeripheriqueBluetoothLanceur(appareilBluetooth);
                    break;
                default:
                    break;
            }
        }

        if(peripheriques.size() == 0)
        {
            Toast.makeText(getApplicationContext(), "Aucun périphérique détecté ! ", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Méthode deconnexionPeripheriquesBluetooth() permetant la déconnexion des appareils Bluetooth du projet connectés.
     */
    private void deconnexionPeripheriquesBluetooth()
    {
        Log.d("IHMEcranPrincipal", "deconnexionPeripheriquesBluetooth()");

        if(peripheriqueBluetoothEcran != null)
            peripheriqueBluetoothEcran.deconnecter(true);

        if(peripheriqueBluetoothTable != null)
            peripheriqueBluetoothTable.deconnecter(true);

        if(peripheriqueBluetoothLanceur != null)
            peripheriqueBluetoothLanceur.deconnecter(true);

        modifierIHMBluetoothInsuffisant();
    }

    /**
     * Méthode connexionPeripheriqueBluetoothEcran() permettant la connexion Bluetooth à l'écran.
     *
     * @param appareilBluetooth étant le Bluetooth de l'écran
     */
    private void connexionPeripheriqueBluetoothEcran(BluetoothDevice appareilBluetooth)
    {
        Log.d("IHMEcranPrincipal", "connexionPeripheriqueBluetoothEcran()");

        peripheriqueBluetoothEcran = new PeripheriqueBluetooth(appareilBluetooth, handler);

        //Toast.makeText(getApplicationContext(), "Connexion à l'écran ...", Toast.LENGTH_SHORT).show();

        peripheriqueBluetoothEcran.connecter();

        //attendre(2000);

        if (peripheriqueBluetoothEcran.estConnecte())
            Toast.makeText(getApplicationContext(), "Ecran connecté !", Toast.LENGTH_SHORT).show();
    }

    /**
     * Méthode connexionPeripheriqueBluetoothTable() permettant la connexion Bluetooth à la table.
     *
     * @param appareilBluetooth étant le Bluetooth de la table
     */
    private void connexionPeripheriqueBluetoothTable(BluetoothDevice appareilBluetooth)
    {
        Log.d("IHMEcranPrincipal", "connexionPeripheriqueBluetoothTable()");

        peripheriqueBluetoothTable = new PeripheriqueBluetooth(appareilBluetooth, handler);

        //Toast.makeText(getApplicationContext(), "Connexion à la table ...", Toast.LENGTH_SHORT).show();

        peripheriqueBluetoothTable.connecter();

        //attendre(2000);

        if (peripheriqueBluetoothTable.estConnecte())
            Toast.makeText(getApplicationContext(), "Table connectée !", Toast.LENGTH_SHORT).show();
    }

    /**
     * Méthode connexionPeripheriqueBluetoothLanceur() permettant la connexion Bluetooth du lanceur.
     *
     * @param appareilBluetooth étant le Bluetooth du lanceur
     */
    private void connexionPeripheriqueBluetoothLanceur(BluetoothDevice appareilBluetooth)
    {
        Log.d("IHMEcranPrincipal", "connexionPeripheriqueBluetoothLanceur()");

        peripheriqueBluetoothLanceur = new PeripheriqueBluetooth(appareilBluetooth, handler);

        //Toast.makeText(getApplicationContext(), "Connexion au lanceur ...", Toast.LENGTH_SHORT).show();

        peripheriqueBluetoothLanceur.connecter();

        //attendre(2000);

        if (peripheriqueBluetoothLanceur.estConnecte())
            Toast.makeText(getApplicationContext(), "Lanceur connecté !", Toast.LENGTH_SHORT).show();
    }

    /**
     * Méthode verifierConnexionAppareilsBluetoothRequis() vérifiant si les appareils Bluetooth nécessaires au bon déroulement d'une séance sont détectés et connectés, puis modifie l'IHM en conséquent.
     *
     * @return booléen tous les appareils sont connectés ou non
     */
    private boolean verifierConnexionAppareilsBluetoothRequis()
    {
        Log.d("IHMEcranPrincipal", "verifierConnexionAppareilsBluetoothRequis()");

        boolean ecranEstDetecte = (peripheriqueBluetoothEcran!=null);
        boolean lanceurEstDetecte = (peripheriqueBluetoothLanceur != null);
        boolean tableEstDetectee = (peripheriqueBluetoothTable != null);
        boolean ecranEstConnecte = false;
        boolean lanceurEstConnecte = false;
        boolean tableEstConnectee = false;

        if(ecranEstDetecte)
            ecranEstConnecte = peripheriqueBluetoothEcran.estConnecte();

        if(lanceurEstDetecte)
            lanceurEstConnecte = peripheriqueBluetoothLanceur.estConnecte();

        if(tableEstDetectee)
            tableEstConnectee = peripheriqueBluetoothTable.estConnecte();

        actualiserIHMAppareilsBluetooth(tableEstConnectee, lanceurEstConnecte, ecranEstConnecte);

        boolean appareilsTousConnectes = ecranEstConnecte && lanceurEstConnecte && tableEstConnectee;
        boolean aucunAppareilConnecte = !ecranEstConnecte && !lanceurEstConnecte && !tableEstConnectee;

        if(appareilsTousConnectes) // Si tous les appareils sont connectés
        {
            modifierIHMBluetoothOperationnel();
            return true;
        }
        else if(aucunAppareilConnecte) // Si aucun appareil n'est connecté
        {
            modifierIHMBluetoothInsuffisant();
            return false;
        }
        else
        {
            modifierIHMBluetoothSuffisant(); // Si au moins un appareil est connecté
        }

        return false;
    }

    /**
     * Méthode actualiserIHMAppareilsBluetooth() permettant de modifier les voyants des appareils Bluetooth en fonction des appareils connectés
     */
    private void actualiserIHMAppareilsBluetooth(boolean tableEstConnectee, boolean lanceurEstConnecte, boolean ecranEstConnecte)
    {
        if(tableEstConnectee)
            voyantTable.setImageResource(R.drawable.table_connectee);
        else
            voyantTable.setImageResource(R.drawable.table_deconnectee);

        if(lanceurEstConnecte)
            voyantLanceur.setImageResource(R.drawable.lanceur_connecte);
        else
            voyantLanceur.setImageResource(R.drawable.lanceur_deconnecte);

        if(ecranEstConnecte)
            voyantEcran.setImageResource(R.drawable.ecran_connecte);
        else
            voyantEcran.setImageResource(R.drawable.ecran_deconnecte);
    }

    /**
     * Méthode actionnerSeance() permettant de démarrer ou d'arrêter une séance en fonction de l'état actuel.
     */
    private void actionnerSeance()
    {
        Log.d("IHMEcranPrincipal", "actionnerSeance() état : " + etatSeance);

        // Démarrer ou arrêter la séance selon son état actuel
        switch (etatSeance)
        {
            case ETAT_SEANCE_ARRETEE:
                // La séance n'est pas commencée, il faut la démarrer
                if(idZonePrecedente == -1) // Si les zones n'ont pas été selectionnées
                    selectionnerZone(SELECTION_ZONE_ROBOT, 0, 0);
                else // Sinon, on peut démarrer la séance
                {
                    demarrerSeance();
                    etatSeance = ETAT_SEANCE_DEMARREE;
                }
                break;
            case ETAT_SEANCE_DEMARREE:
                // La séance est en cours, il faut la metre en pause
                pauserSeance();
                etatSeance = ETAT_SEANCE_PAUSE;
                break;
            case ETAT_SEANCE_PAUSE:
                // La séance est en pause, il faut la reprendre
                reprendreSeance();
                etatSeance = ETAT_SEANCE_DEMARREE;
                break;
            default:
                break;
        }
    }

    /**
     * Méthode demarrerSeance() permettant d'envoyer les trames correspondantes aux appareils Bluetooth du projet.
     */
    private void demarrerSeance()
    {
        Log.d("IHMEcranPrincipal", "demarrerSeance()");

        // Lire les valeurs des paramètres actuels afin de les appliquer aux paramètres de la séance
        appliquerParametresSeance();

        // Envoyer la trame de paramétrage à l'écran, puis la trame de début de séance
        envoyerTrameDebutSeancePeripheriqueBluetoothEcran();

        envoyerTrameParametrageSeancePeripheriqueBluetoothEcran();

        // On applique les paramètres actuels à la séance en cours
        seanceEnCours = new Seance(parametresActuels.getFrequenceBalles(), parametresActuels.getNombreBalles(), parametresActuels.getEffetComplet(), parametresActuels.getIntensiteEffet(), parametresActuels.getPuissanceBalles(), parametresActuels.getRotation());
        seanceEnCours.setIdJoueur(serveurBDD.getIdJoueurParametres());
        seanceEnCours.setZoneObjectif(zoneObjectif);
        seanceEnCours.setZoneRobot(zoneRobot);

        // Mettre à zéro les statistiques
        ballesJouees = 0;
        ballesReussies = 0;

        // Envoyer la trame de départ au lanceur
        envoyerTrameDebutSeancePeripheriqueBluetoothLanceur();

        // Changer l'icône du bouton d'action
        boutonActionSeance.setImageResource(R.drawable.bouton_pause);

        // Changer l'état et la visibilité du bouton d'arrêt de séance
        boutonArreterSeance.setEnabled(true);
        boutonArreterSeance.setVisibility(View.VISIBLE);

        // Changer les états des barres de paramétrage
        barreProgressionNombreBalles.setEnabled(false);
        barreProgressionFrequenceBalles.setEnabled(false);
        barreProgressionEffetBalles.setEnabled(false);
        barreProgressionPuissanceBalles.setEnabled(false);
        barreProgressionRotationLanceur.setEnabled(false);
    }

    /**
     * Méthode pauserSeance() permettant de mettre en pause un séance en cours.
     */
    private void pauserSeance()
    {
        Log.d("IHMEcranPrincipal", "pauserSeance()");

        // Changer l'icône du bouton d'action
        boutonActionSeance.setImageResource(R.drawable.bouton_reprendre);

        envoyerTramePeripheriqueBluetoothEcran(TRAME_ECRAN_PAUSE_SEANCE);
        envoyerTramePeripheriqueBluetoothLanceur(TRAME_LANCEUR_PAUSE_SEANCE);

        etatSeance = ETAT_SEANCE_PAUSE;
    }

    /**
     * Méthode reprendreSeance() permettant de reprendre une séance actuellement en pause.
     */
    private void reprendreSeance()
    {
        Log.d("IHMEcranPrincipal", "reprendreSeance()");

        // Changer l'icône du bouton d'action
        boutonActionSeance.setImageResource(R.drawable.bouton_pause);

        envoyerTramePeripheriqueBluetoothEcran(TRAME_ECRAN_REPRISE_SEANCE);
        envoyerTramePeripheriqueBluetoothLanceur(TRAME_LANCEUR_REPRISE_SEANCE);

        etatSeance = ETAT_SEANCE_DEMARREE;
    }

    /**
     * Méthode arreterSeance() permettant d'envoyer les trames correspondantes aux appareils Bluetooth du projet.
     */
    private void arreterSeance(boolean seanceAEnregistrer)
    {
        Log.d("IHMEcranPrincipal", "arreterSeance()");
        // Envoyer la trame d'arrêt à l'écran
        envoyerTrameArretPeripheriqueBluetoothEcran();

        // Envoyer la trame d'arrêt au lanceur
        envoyerTrameArretPeripheriqueBluetoothLanceur();

        // Envoyer la trame d'arrêt à la table
        envoyerTrameArretPeripheriqueBluetoothTable();

        if (seanceAEnregistrer)
        {
            // Enregistrer la séance dans la base de données
            serveurBDD.insererSeance(seanceEnCours);
        }

        // Changer l'icône du bouton d'action
        boutonActionSeance.setImageResource(R.drawable.bouton_demarrer);
        boutonActionSeance.setEnabled(true);

        // Changer l'état et la visibilité du bouton d'arrêt de séance
        boutonArreterSeance.setEnabled(false);
        boutonArreterSeance.setVisibility(View.INVISIBLE);

        // Changer l'état de la séance
        etatSeance = ETAT_SEANCE_ARRETEE;

        // Changer les états des barres de paramétrage
        barreProgressionNombreBalles.setEnabled(true);
        barreProgressionFrequenceBalles.setEnabled(true);
        barreProgressionEffetBalles.setEnabled(true);
        barreProgressionPuissanceBalles.setEnabled(true);
        barreProgressionRotationLanceur.setEnabled(true);
    }

    /**
     * Méthode appliquerParametresSeance() appliquant les valeurs des paramètres à l'objet parametresActuels.
     */
    private void appliquerParametresSeance()
    {
        Log.d("IHMEcranPrincipal", "appliquerParametresSeance()");

        if(etatSeance == ETAT_SEANCE_ARRETEE)
        {
            parametresActuels.setNomJoueur(nomJoueur);
            parametresActuels.setNombreBalles((barreProgressionNombreBalles.getProgress() * 5) + 5); // 5 balles par palier, 5 balles minimum
            parametresActuels.setFrequenceBalles((barreProgressionFrequenceBalles.getProgress() * 5) + 30); // 5 balles par palier, 30 balles minimum

            String effet;
            int intensiteEffet = barreProgressionEffetBalles.getProgress() - 8;

            if (barreProgressionEffetBalles.getProgress() == 8)
            {
                effet = "Aucun";
                intensiteEffet = 1;
            }
            else if (barreProgressionEffetBalles.getProgress() < 8)
            {
                effet = "Coupé";
                intensiteEffet = 0 - intensiteEffet;
            }
            else
                effet = "Lifté";

            parametresActuels.setEffetBalles(effet);
            parametresActuels.setIntensiteEffet(intensiteEffet);
            parametresActuels.setPuissanceBalles(barreProgressionPuissanceBalles.getProgress() + 1); // 1 minimum (soit 10% minimum)
            parametresActuels.setRotation(barreProgressionRotationLanceur.getProgress() * 5); // 5° par pallier
        }
    }

    /**
     * Méthode modifierValeursParametresIHM() modifiant les valeurs des champs de paramètre en fonction de leur barre de progression correspondante.
     */
    private void modifierValeursParametresIHM()
    {
        Log.d("IHMEcranPrincipal", "modifierValeursParametresIHM()");

        texteValeurNombreBalles.setText(((barreProgressionNombreBalles.getProgress() * 5) + 5) + " balles"); // 5 balles par palier, 5 balles minimum
        texteValeurFrequenceBalles.setText(((barreProgressionFrequenceBalles.getProgress() * 5) + 30) + " balles/min"); // 5 balles par palier, 30 balles minimum

        String effet;
        int intensiteEffet = barreProgressionEffetBalles.getProgress() - 8;

        if (barreProgressionEffetBalles.getProgress() == 8)
            effet = "Aucun";
        else if (barreProgressionEffetBalles.getProgress() < 8)
        {
            effet = "Coupé";
            intensiteEffet = 0 - intensiteEffet;
        }
        else
            effet = "Lifté";

        switch(effet)
        {
            case "Aucun":
                texteValeurEffetBalles.setText(effet);
                break;
            default:
                texteValeurEffetBalles.setText(effet + " " + intensiteEffet); // 10% par palier
        }

        texteValeurPuissanceBalles.setText(((barreProgressionPuissanceBalles.getProgress() * 10) + 10) + "%"); // 10% par pallier, 10% minimum

        String stringRotation;
        int rotationActuelle = barreProgressionRotationLanceur.getProgress() * 5; // 5° par pallier

        if(rotationActuelle > 45)
            stringRotation = (rotationActuelle - 45) + "° à droite";
        else if(rotationActuelle < 45)
            stringRotation = (45 - rotationActuelle) + "° à gauche";
        else
            stringRotation = (rotationActuelle - 45) +  "°";

        texteValeurRotationLanceur.setText(stringRotation);
    }

    /**
     * Méthode attendre() permettant d'attendre un temps données.
     *
     * @param tempsMillisecondes
     */
    private void attendre(int tempsMillisecondes)
    {
        Log.d("IHMEcranPrincipal", "attendre()");

        try
        {
            Thread.sleep(tempsMillisecondes);
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }
    }

    /**
     * Méthode envoyerTramePeripheriqueBluetoothEcran() envoyant la trame à l'écran, si l'écran est connecté.
     *
     * @param trame étant la trame à envoyer
     */
    private void envoyerTramePeripheriqueBluetoothEcran(String trame)
    {
        Log.d("IHMEcranPrincipal", "envoyerTramePeripheriqueBluetoothEcran()");

        if(peripheriqueBluetoothEcran != null)
        {
            if (peripheriqueBluetoothEcran.estConnecte())
                peripheriqueBluetoothEcran.envoyer(trame);
        }
    }

    /**
     * Méthode envoyerTrameConnexionPeripheriqueBluetoothEcran() envoyant la trame de connexion à l'écran.
     */
    private void envoyerTrameConnexionPeripheriqueBluetoothEcran()
    {
        Log.d("IHMEcranPrincipal", "envoyerTrameConnexionPeripheriqueBluetoothEcran()");

        String trame = TRAME_ENTETE + ":CONNECT:" + parametresActuels.getNomJoueur();

        envoyerTramePeripheriqueBluetoothEcran(trame);
    }

    /**
     * Méthode envoyerTrameParametrageSeancePeripheriqueBluetoothEcran() envoyant la trame de paramétrage à l'écran.
     */
    private void envoyerTrameParametrageSeancePeripheriqueBluetoothEcran()
    {
        Log.d("IHMEcranPrincipal", "envoyerTrameParametrageSeancePeripheriqueBluetoothEcran()");

        String trame = TRAME_ENTETE + ":SETSEANCE:" + zoneRobot + ":" + zoneObjectif + ":" + parametresActuels.getNombreBalles();

        envoyerTramePeripheriqueBluetoothEcran(trame);
    }

    /**
     * Méthode envoyerTrameFinParametrageSeancePeripheriqueBluetoothEcran() envoyant la trame de fin de paramétrage à l'écran.
     */
    private void envoyerTrameDebutSeancePeripheriqueBluetoothEcran()
    {
        Log.d("IHMEcranPrincipal", "envoyerTrameDebutSeancePeripheriqueBluetoothEcran()");

        envoyerTramePeripheriqueBluetoothEcran(TRAME_ECRAN_DEBUT_SEANCE);
    }

    /**
     * Méthode envoyerTrameArretPeripheriqueBluetoothEcran() envoyant la trame de finde séance à l'écran
     */
    private void envoyerTrameArretPeripheriqueBluetoothEcran()
    {
        Log.d("IHMEcranPrincipal", "envoyerTrameArretPeripheriqueBluetoothEcran()");

        envoyerTramePeripheriqueBluetoothEcran(TRAME_ECRAN_FIN_SEANCE);
    }

    /**
     * Méthode ajouterJoueur() ajoutant un joueur saisit à la base de données.
     */
    private void ajouterJoueur()
    {
        AlertDialog.Builder ajoutJoueur = new AlertDialog.Builder(this);
        LayoutInflater factory = LayoutInflater.from(this);
        final View ajoutJoueurView = factory.inflate(R.layout.ajout_joueur, null);
        ajoutJoueur.setView(ajoutJoueurView);

        ajoutJoueur.setTitle("Ajouter un nouveau joueur");

        ajoutJoueur.setPositiveButton("Valider", new DialogInterface.OnClickListener()
        {
            public void onClick(DialogInterface dialog, int which)
            {
                //Lorsque l'on cliquera sur le bouton "OK", on récupère l'EditText correspondant à notre vue personnalisée (cad à alertDialogView)
                EditText nomJoueur = (EditText)ajoutJoueurView.findViewById(R.id.editTextNom);
                Joueur joueur = new Joueur(nomJoueur.getText().toString());
                long id = serveurBDD.insererJoueur(joueur);
                Log.d("IHMEcranPrincipal", "Nom joueur : " + nomJoueur.getText().toString() + " - id : " + id);
                Toast.makeText(getApplicationContext(), "Joueur " + nomJoueur.getText() + " ajouté", Toast.LENGTH_SHORT).show();
                creerListeJoueurs();
            }
        });

        ajoutJoueur.setNegativeButton("Annuler", new DialogInterface.OnClickListener()
        {
            public void onClick(DialogInterface dialog, int which)
            {

            }
        });

        ajoutJoueur.show();
    }

    /**
     * Méthode creerListeJoueurs() créant la liste des joueurs présents dans la base de données
     */
    private void creerListeJoueurs()
    {
        final List<Joueur> listeJoueurs = serveurBDD.getJoueurs();
        final List<String> noms = new ArrayList<String>();

        // le dernier joueur a avoir utilisé l'application
        int idJoueur = serveurBDD.getIdJoueurParametres();
        for(int i = 0; i < listeJoueurs.size(); i++)
        {
            Joueur joueur = listeJoueurs.get(i);
            if(joueur.getId() == idJoueur)
            {
                noms.add(joueur.getNom());
                break;
            }
        }
        for(int i = 0; i < listeJoueurs.size(); i++)
        {
            Joueur joueur = listeJoueurs.get(i);
            if(joueur.getId() == idJoueur)
                continue;
            noms.add(joueur.getNom());
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, noms);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerListeJoueurs.setAdapter(adapter);

        spinnerListeJoueurs.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
        {
            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1, int position, long id)
            {
                Joueur joueur = serveurBDD.getJoueur(noms.get(position));
                Log.d("IHMEcranPrincipal", "Nom joueur séléctionné : " + noms.get(position));
                // On conserve son id pour la prochaine session
                serveurBDD.setIdJoueurParametres(joueur.getId());
                nomJoueur = joueur.getNom();
                if(etatSeance == ETAT_SEANCE_ARRETEE)
                    parametresActuels.setNomJoueur(nomJoueur);

                // On envoi l'information à l'écran, si la séance n'a pas encore été démarrée
                envoyerTrameConnexionPeripheriqueBluetoothEcran();
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0)
            {
            }
        });
    }

    /**
     * Méthode traiterDonneesRecues() permettant la traitement des données bluetooth reçues en fonction de l'appareil source de ces données.
     *
     * @param nomAppareilSource étant l'appareil duquel on reçoit les données
     * @param donnees étant les données reçues
     */
    private void traiterDonneesRecues(String nomAppareilSource, String donnees)
    {
        Log.d("IHMEcranPrincipal", "traiterDonneesRecues()");

        switch (nomAppareilSource)
        {
            case NOM_PERIPHERIQUE_BLUETOOTH_LANCEUR:
                traiterDonneesRecuesLanceur(donnees);
                break;
            case NOM_PERIPHERIQUE_BLUETOOTH_TABLE:
                traiterDonneesRecuesTable(donnees);
                break;
            default:
                break;
        }
    }

    /**
     * Méthode traiterDonneesRecuesLanceur() permettant le traitement des données reçues par le lanceur.
     *
     * @param donnees étant les données reçues
     */
    private void traiterDonneesRecuesLanceur(String donnees)
    {
        Log.d("IHMEcranPrincipal", "traiterDonneesRecuesLanceur()");

        List<String> donneeRecue = new ArrayList<String>(Arrays.asList(donnees.split(":")));

        switch (donneeRecue.get(0))
        {
            case TRAME_ENTETE:
                if(donneeRecue.get(1).contains("ERREUR"))
                    traiterErreurRecueLanceur(donneeRecue.get(1));
                break;
            default:
                Log.e("IHMEcranPrincipal", "traiterDonneesRecuesLanceur() : trame non reconnue !");
        }
    }

    /**
     * Méthode traiterErreurRecueLanceur() permettant la gestion des erreurs en provenance du lanceur.
     *
     * @param erreur étant le code d'erreur reçu
     */
    private void traiterErreurRecueLanceur(String erreur)
    {
        Log.d("IHMEcranPrincipal", "traiterErreurRecueLanceur() code d'erreur : " + erreur);

        pauserSeance();
    }

    /**
     * Méthode traiterDonneesRecuesTable() permettant le traitement des données reçues par la table.
     *
     * @param donnees étant les données reçues
     */
    private void traiterDonneesRecuesTable(String donnees)
    {
        Log.d("IHMEcranPrincipal", "traiterDonneesRecuesTable()");

        List<String> donneeRecue = new ArrayList<String>(Arrays.asList(donnees.split(":")));

        switch (donneeRecue.get(0))
        {
            case TRAME_ENTETE:
                // Transferer la trame reçue à l'écran
                envoyerTramePeripheriqueBluetoothEcran(donnees);

                switch(donneeRecue.get(1))
                {
                    case "IMPACT":
                        if(Integer.parseInt(donneeRecue.get(2)) != 0) // Si l'impacte a eu lieu sur une des 9 zones et non sur le côté du lanceur
                            calculerReussiteSeance(Integer.parseInt(donneeRecue.get(2)));
                        else
                            incrementerBallesJouees();
                        break;
                    case "FAUTE":
                        calculerReussiteSeance(-1);
                }

                break;
            default:
                Log.e("IHMEcranPrincipal", "traiterDonneesRecuesTable() : trame non reconnue !");
        }
    }

    /**
     * Méthode calculerReussiteSeance() calculant le taux de réussite de la séance selon le nombre de balles ayant déjà touché l'objectif.
     *
     * @param zoneTouchee étant la zone de l'impacte
     */
    private void calculerReussiteSeance(int zoneTouchee)
    {
        Log.d("IHMEcranPrincipal", "calculerReussiteSeance() zone touchée : " + zoneTouchee);

        if(etatSeance == ETAT_SEANCE_DEMARREE)
        {
            if (zoneTouchee != -1) // Si la balle a bien touché la table
            {
                if ((seanceEnCours.getZoneObjectif() != -1) && (seanceEnCours.getZoneObjectif() != 0)) { // Si l'objectif a été défini
                    if (zoneTouchee == seanceEnCours.getZoneObjectif()) // Si la zone touchée est la même que l'objectif
                        ballesReussies++;
                } else // Si aucun objectif n'a été défini: la table entière est l'objectif
                    ballesReussies++;
            }

            seanceEnCours.setTauxReussite((float) ballesReussies / (float) seanceEnCours.getNombreBalles() * 100);

            if (ballesJouees == seanceEnCours.getNombreBalles())
                arreterSeance(true);
        }
    }

    /**
     * Méthode incrementerBallesJouees() permettant de traiter les nombre de balles jouées de la séance, et d'arrêter la séance si la dernière balle est en dehors.
     */
    private void incrementerBallesJouees()
    {
        Log.d("IHMEcranPrincipal", "incrementerBallesJouees()");

        if(etatSeance == ETAT_SEANCE_DEMARREE)
        {
            ++ballesJouees;

            if (ballesJouees == seanceEnCours.getNombreBalles())
            {
                boutonActionSeance.setEnabled(false);

                final Timer timerAsync = new Timer();
                final TimerTask timerTaskAsync = new TimerTask() {
                    @Override
                    public void run() {
                        runOnUiThread(new Runnable() {
                            @Override public void run() {
                                if(etatSeance == ETAT_SEANCE_DEMARREE)
                                    arreterSeance(true);

                                timerAsync.cancel();
                            }
                        });
                    }
                };
                timerAsync.schedule(timerTaskAsync, 2000, 2000);
            }
        }
    }

    /**
     * Méthode envoyerTrameArretPeripheriqueBluetoothLanceur() envoyant la trame de fin de séance au lanceur.
     */
    private void envoyerTrameArretPeripheriqueBluetoothLanceur()
    {
        Log.d("IHMEcranPrincipal", "envoyerTrameArretPeripheriqueBluetoothLanceur()");

        if (peripheriqueBluetoothLanceur != null)
        {
            if (peripheriqueBluetoothLanceur.estConnecte())
            {
                peripheriqueBluetoothLanceur.envoyer(TRAME_LANCEUR_ARRET_SEANCE);
                Log.d("IHMEcranPrincipal", "Trame arrêt séance Lanceur : " + TRAME_LANCEUR_ARRET_SEANCE);
            }
        }
    }

    /**
     * Méthode envoyerTrameArretPeripheriqueBluetoothTable() envoyant la trame de fin de séance au lanceur.
     */
    private void envoyerTrameArretPeripheriqueBluetoothTable()
    {
        Log.d("IHMEcranPrincipal", "envoyerTrameArretPeripheriqueBluetoothTable()");

        if (peripheriqueBluetoothTable != null)
        {
            if (peripheriqueBluetoothTable.estConnecte())
            {
                peripheriqueBluetoothTable.envoyer(TRAME_TABLE_ARRET_SEANCE);
                Log.d("IHMEcranPrincipal", "Trame arrêt séance Table : " + TRAME_TABLE_ARRET_SEANCE);
            }
        }
    }

    /**
     * Méthode envoyerTrameDebutSeancePeripheriqueBluetoothLanceur() envoyant la trame de début de séance au lanceur.
     */
    private void envoyerTrameDebutSeancePeripheriqueBluetoothLanceur()
    {
        Log.d("IHMEcranPrincipal", "envoyerTrameDebutSeancePeripheriqueBluetoothLanceur()");

        if (peripheriqueBluetoothLanceur != null)
        {
            if (peripheriqueBluetoothLanceur.estConnecte())
            {
                String trame = TRAME_ENTETE + ":SET:" + parametresActuels.getEffet() + ":" + parametresActuels.getIntensiteEffet() + ":" + parametresActuels.getPuissanceBalles() + ":" + parametresActuels.getFrequenceBalles() + ":" + parametresActuels.getRotation() + ":" + parametresActuels.getNombreBalles() + TRAME_FIN;
                peripheriqueBluetoothLanceur.envoyer(trame);
                Log.d("IHMEcranPrincipal", "Trame départ séance Lanceur : " + trame);
            }
        }
    }

    /**
     * Méthode envoyerTrameRepriseSeancePeripheriqueBluetoothLanceur() envoyant la trame de dreprise de séance au lanceur.
     */
    private void envoyerTrameRepriseSeancePeripheriqueBluetoothLanceur()
    {
        Log.d("IHMEcranPrincipal", "envoyerTrameRepriseSeancePeripheriqueBluetoothLanceur()");

        if (peripheriqueBluetoothLanceur != null)
        {
            if (peripheriqueBluetoothLanceur.estConnecte())
            {
                String trame = TRAME_ENTETE + ":SET:" + parametresActuels.getEffet() + ":" + parametresActuels.getIntensiteEffet() + ":" + parametresActuels.getPuissanceBalles() + ":" + parametresActuels.getFrequenceBalles() + ":" + parametresActuels.getRotation() + ":" + (parametresActuels.getNombreBalles() - ballesJouees) + TRAME_FIN;
                peripheriqueBluetoothLanceur.envoyer(trame);
                Log.d("IHMEcranPrincipal", "Trame reprise séance Lanceur : " + trame);
            }
        }
    }

    /**
     * Méthode envoyerTramePeripheriqueBluetoothLanceur() envoyant la trame au lanceur, si le lanceur est connecté.
     *
     * @param trame étant la trame à envoyer
     */
    private void envoyerTramePeripheriqueBluetoothLanceur(String trame)
    {
        Log.d("IHMEcranPrincipal", "envoyerTramePeripheriqueBluetoothLanceur()");

        if(peripheriqueBluetoothLanceur != null)
        {
            if (peripheriqueBluetoothLanceur.estConnecte())
                peripheriqueBluetoothLanceur.envoyer(trame);
        }
    }

    /**
     * Méthode selectionnerZone() affichant une boite de dialogue permettant de selectionner la zone du robot ou la zone de l'objectif.
     *
     * @param typeSelection
     * @param choixObjectif
     * @param choixRobot
     */
    private void selectionnerZone(final int typeSelection, int choixObjectif, int choixRobot)
    {
        Log.d("IHMEcranPrincipal", "selectionnerZone()");

        final AlertDialog.Builder selectionZone = new AlertDialog.Builder(this);
        LayoutInflater factory = LayoutInflater.from(this);
        final View selectionZoneView = factory.inflate(R.layout.zones, null);
        selectionZone.setView(selectionZoneView);

        List<Button> boutonsZone = new ArrayList<Button>();

        Button btnZone1 = (Button)selectionZoneView.findViewById(R.id.case1);
        boutonsZone.add(btnZone1);
        Button btnZone2 = (Button)selectionZoneView.findViewById(R.id.case2);
        boutonsZone.add(btnZone2);
        Button btnZone3 = (Button)selectionZoneView.findViewById(R.id.case3);
        boutonsZone.add(btnZone3);
        Button btnZone4 = (Button)selectionZoneView.findViewById(R.id.case4);
        boutonsZone.add(btnZone4);
        Button btnZone5 = (Button)selectionZoneView.findViewById(R.id.case5);
        boutonsZone.add(btnZone5);
        Button btnZone6 = (Button)selectionZoneView.findViewById(R.id.case6);
        boutonsZone.add(btnZone6);
        Button btnZone7 = (Button)selectionZoneView.findViewById(R.id.case7);
        boutonsZone.add(btnZone7);
        Button btnZone8 = (Button)selectionZoneView.findViewById(R.id.case8);
        boutonsZone.add(btnZone8);
        Button btnZone9 = (Button)selectionZoneView.findViewById(R.id.case9);
        boutonsZone.add(btnZone9);

        if(choixObjectif > 0)
        {
            Button btnZone = boutonsZone.get(choixObjectif - 1);
            btnZone.setBackgroundResource(R.drawable.case_cible);
        }
        if(choixRobot > 0)
        {
            Button btnZone = boutonsZone.get(choixRobot - 1);
            btnZone.setBackgroundResource(R.drawable.case_robot);
        }

        for(int i = 0; i < 9; i++)
        {
            Button btnZone = boutonsZone.get(i);

            // Changer l'icône de fond du bouton si la zone correspondante est actuellement occupée par le robot ou l'objectif
            if((i + 1) ==  zoneRobot) // i + 1 étant le numéro de zone actuel
                btnZone.setBackgroundResource(R.drawable.case_robot);

            /*if((i + 1) ==  zoneObjectif) // i + 1 étant le numéro de zone actuel
                btnZone.setBackgroundResource(R.drawable.case_cible);*/

            btnZone.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    Button myButton = (Button)selectionZoneView.findViewById(v.getId());
                    int zoneActuelle = Integer.parseInt(myButton.getText().toString().substring(myButton.getText().toString().length() - 1));

                    switch (typeSelection)
                    {
                        case SELECTION_ZONE_OBJECTIF:
                            idZonePrecedente = idZonePrecedenteObjectif;
                            break;
                        case SELECTION_ZONE_ROBOT:
                            idZonePrecedente = idZonePrecedenteRobot;
                            break;
                    }

                    if(idZonePrecedente != -1)
                    {
                        Button myButtonPrecedent = (Button)selectionZoneView.findViewById(idZonePrecedente);
                        int numeroZonePrecedente = Integer.parseInt(myButtonPrecedent.getText().toString().substring(myButtonPrecedent.getText().toString().length() - 1));

                        switch(typeSelection)
                        {
                            case SELECTION_ZONE_OBJECTIF:
                                if((v.getId() != idZonePrecedenteRobot) && (myButtonPrecedent.getId() != idZonePrecedenteRobot))
                                    myButtonPrecedent.setBackgroundResource(R.drawable.case_libre);
                                break;
                            /*case SELECTION_ZONE_ROBOT:
                                if(v.getId() != idZonePrecedenteObjectif)
                                    myButtonPrecedent.setBackgroundResource(R.drawable.case_libre);
                                break;*/
                            default:
                                myButtonPrecedent.setBackgroundResource(R.drawable.case_libre);
                                break;
                        }
                    }

                    switch (typeSelection)
                    {
                        case SELECTION_ZONE_OBJECTIF:
                            if (v.getId() != idZonePrecedenteRobot)
                                idZonePrecedenteObjectif = v.getId();
                            break;
                        case SELECTION_ZONE_ROBOT:
                            idZonePrecedenteRobot = v.getId();
                            break;
                    }

                    idZonePrecedente = v.getId();

                    if(typeSelection == SELECTION_ZONE_OBJECTIF)
                    {
                        // Vérifier si l'objectif ne tombe pas sur la zone du robot
                        if(zoneActuelle !=  zoneRobot)
                        {
                            if (strZoneObjectif == myButton.getText().toString())
                            {
                                myButton.setBackgroundResource(R.drawable.case_libre);
                                strZoneObjectif = "ZONE 0";
                            }
                            else
                            {
                                myButton.setBackgroundResource(R.drawable.case_cible);
                                strZoneObjectif = myButton.getText().toString();
                            }

                            Log.d("IHMEcranPrincipal", "Objectif : " + strZoneObjectif);
                        }
                    }
                    else if(typeSelection == SELECTION_ZONE_ROBOT)
                    {
                        if (strZoneRobot == myButton.getText().toString())
                        {
                            myButton.setBackgroundResource(R.drawable.case_libre);
                            strZoneRobot = "ZONE 0";
                        }
                        else
                        {
                            myButton.setBackgroundResource(R.drawable.case_robot);
                            strZoneRobot = myButton.getText().toString();
                        }

                        Log.d("IHMEcranPrincipal", "Robot : " + strZoneRobot);
                    }
                }
            });
        }

        if(typeSelection == SELECTION_ZONE_OBJECTIF)
            selectionZone.setTitle("Placer l'objectif");
        if(typeSelection == SELECTION_ZONE_ROBOT)
            selectionZone.setTitle("Placer le robot");

        selectionZone.setPositiveButton("Valider", new DialogInterface.OnClickListener()
        {
            public void onClick(DialogInterface dialog, int which)
            {
                if(typeSelection == SELECTION_ZONE_OBJECTIF)
                {
                    Log.d("IHMEcranPrincipal", "Zone objectif validée : " + strZoneObjectif);
                    zoneObjectif = Integer.parseInt(strZoneObjectif.substring(strZoneObjectif.length() - 1));
                }
                else if(typeSelection == SELECTION_ZONE_ROBOT)
                {
                    Log.d("IHMEcranPrincipal", "Zone robot validée : " + strZoneRobot);
                    zoneRobot = Integer.parseInt(strZoneRobot.substring(strZoneRobot.length() - 1));
                    // Puis sélectionner l'objectif
                    selectionnerZone(SELECTION_ZONE_OBJECTIF, 0, 0);
                }

                if (idZonePrecedente == -1)
                    idZonePrecedente = 0;
            }
        });

        selectionZone.setNegativeButton("Annuler", new DialogInterface.OnClickListener()
        {
            public void onClick(DialogInterface dialog, int which)
            {
                if(typeSelection == SELECTION_ZONE_OBJECTIF)
                {
                    strZoneObjectif = "";
                    zoneObjectif = 0;
                }
                else if(typeSelection == SELECTION_ZONE_ROBOT)
                {
                    strZoneRobot = "";
                    zoneRobot = 0;
                    // Puis sélectionner l'objectif
                    selectionnerZone(SELECTION_ZONE_OBJECTIF, 0, 0);
                }
            }
        });

        selectionZone.show();
    }

    /**
     * Méthode redirectionActiviteHistorique() démarrant l'activité IHMHistoriqueSeances permettant de visualiser l'historique des séances du joueur actuel.
     */
    private void redirectionActiviteHistorique()
    {
        Log.d("IHMEcranPrincipal", "redirectionActiviteHistorique()");

        Intent intent = new Intent(IHMEcranPrincipal.this, IHMHistoriqueSeances.class);
        startActivity(intent);
    }
}
