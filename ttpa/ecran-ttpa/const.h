#ifndef CONST_H
#define CONST_H

//=========== DEFINES ============
#define WIDGET_SIZE_MAX 16777215

//=================
// IHM
#define TAILLE_FENETRE_DEFAULT_WIDTH 960
#define TAILLE_FENETRE_DEFAULT_HEIGHT 540
#define RATIO_ENTETE 10

#define TAILLE_TEXTE 20 //20
#define TAILLE_TEXTE_SMALL 10 //20
#define TAILLE_TEXTE_NORMAL 30
#define TAILLE_TEXTE_BIG 42
#define TAILLE_TEXTE_NOM 25

#define DELAI_FIXFENETRE 200

#define DEV_BALLESMAX 30

#define IHM_BALLESENVOYEES "" //"Balles Envoyées\n"
#define IHM_NOMDETEST QString::fromUtf8("Simon GAUZY")
#define IHM_PERIPHERIQUEDETEST QString::fromUtf8("Périférique_de_demonstration")

//=================
// TABLE
#define NB_ZONES 9
#define BALLES_MAX_DEFAUT 20

#define DELAI_COUP 400 // en ms

        // En pixels:
#define HAUTEUR_FILET 20
#define TAILLE_TEXTE_NB 30 //20
#define TAILLE_TEXTE_NB_BIG 42 //52
#define TAILLE_OVERLAY 128

#define TABLE_STAT1 QString::fromUtf8("Hors Table :")


//=================
// LOGO
#define LOGO_ATTENTECONNEXION       "ATTENTE DE CONNEXION"
#define LOGO_ATTENTECONFIGURATION   QString::fromUtf8("ATTENTE DE CONFIGURATION DE LA SÉANCE")
#define LOGO_ATTENTEIDENTIFICATION  "ATTENTE D'IDENTIFICATION DU JOUEUR"
#define LOGO_JOUEUR_CONNECTE        QString::fromUtf8(" est connecté")  //QString::fromUtf8(" EST CONNECTÉ")

//=================
// RECAP
#define RECAP_TITRE_TEXTE        QString::fromUtf8("FIN DE SÉANCE")  //QString::fromUtf8(" EST CONNECTÉ")

#define RECAP_STAT1             QString::fromUtf8("Balles Dans L'Objectif :")
#define RECAP_STAT1_ALT         QString::fromUtf8("Balles Renvoyées :")

#define RECAP_STAT2             QString::fromUtf8("Balles Hors Table :")

#define RECAP_STAT3             QString::fromUtf8("Série Maximale :")
#define RECAP_STAT4             QString::fromUtf8("")


//=========== ENUMS ============
/**
 * @brief Enumeration des zones de la table coté robot
 */
enum zones_e
{
    ZONE_HAUTGAUCHE = 0,
    ZONE_HAUTMILIEU,
    ZONE_HAUTDROITE,
    ZONE_MILIEUGAUCHE,
    ZONE_MILIEUMILIEU,
    ZONE_MILIEUDROITE,
    ZONE_BASGAUCHE,
    ZONE_BASMILIEU,
    ZONE_BASDROITE,

    ZONE_ENJEU = 15,
    ZONE_AUCUNE = 20
};
/**
 * @brief Enumeration des fenetres de l'IHM
 */
enum layer_e
{
    LAYER_LOGO = 0,
    LAYER_TABLE = 1,
    LAYER_RECAP = 2
};

/**
 * @brief Enumeration des etats possible du port RFCOMM
 */
enum etatRFCOMM_e
{
    RFCOMM_ARRETE = 0,
    RFCOMM_CONNECTE,
    RFCOMM_FERME
};

//=========== CSS ============

#define CSS_TIMER_ON    QString::fromUtf8("QLabel{color: #B08000;}")
#define CSS_TIMER_OFF   QString::fromUtf8("QLabel{color: #A00000;}")
#define CSS_TIMER_RES   QString::fromUtf8("QLabel{color: #00A000;}")

#define CSS_FOND_INACTIF    QString::fromUtf8("QLabel\n{\nbackground-color: rgba(50, 150, 255, 0);\nborder: 3px solid rgba(255,255,255,30);\ncolor: #FFFFFF;\n}")
#define CSS_FOND_ACTIF      QString::fromUtf8("QLabel\n{\nbackground-color: rgb(0, 150, 50);\nborder: 3px solid #00FF00;\ncolor: #FFFFFF;\n}")
#define CSS_FOND_RATE       QString::fromUtf8("QLabel\n{\nbackground-color: rgb(175, 50, 25);\nborder: 3px solid #FF0000;\ncolor: #FFFFFF;\n}")

#define CSS_FOND_ROBOT      QString::fromUtf8("QLabel\n{\nbackground-color: rgba(0, 0, 0, 120);\nborder: 3px solid #000000;\ncolor: #00FF00;\n}")
#define CSS_FOND_OBJECTIF   QString::fromUtf8("QLabel\n{\nbackground-color: rgba(200, 160, 30, 120);\nborder: 3px solid #FFEE00;\ncolor: #FFFF77;\n}")



/*  HAZARD WARNING
                              ("QLabel\n"
                              "{\n"
                              "     background: qlineargradient(spread:reflect, x1:0.6, y1:0.45, x2:0.5, y2:0.5, stop:0.50 rgba(170, 170, 0, 75), stop:0.51 rgba(50, 50, 0, 75));\n"
                              "     border: 1px solid rgba(255,255,255,15);\n"
                              "     color: #FFFFFF;\n"
                              "}");
*/
/*  BLACK LINES
                              ("QLabel\n"
                              "{\n"
                              "     background: qlineargradient(spread:reflect, x1:0.972868, y1:0.017, x2:1, y2:0, stop:0.554264 rgba(20,20,20,200), stop:1 rgba(0, 0, 0, 0));\n"
                              "     border: 1px solid rgba(255,255,255,15);\n"
                              "     color: #00AA00;\n"
                              "}");
*/


#endif // CONST_H
