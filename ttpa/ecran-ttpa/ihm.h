/*===================================

Taille table de tenis:  x = 152.5 cm
                        y = 137 cm

Taille d'une case:      x = 50.83
                        y = 45.66

=====================================*/

#ifndef IHM_H
#define IHM_H

/**
  *
  * @file ihm.h
  *
  * @brief La fenêtre principale de l'application
  *
  * @author Racamond Adrien
  *
  * @version 1.0
  *
  */

#include <QWidget>
#include <QThread>
#include <QDebug>
#include <unistd.h>
#include <stdio.h>
#include "ui_ihm.h"
#include "const.h"
#include "table.h"
#include "trame.h"
#include <cstdint>

#define PORT_BLUETOOTH  "rfcomm0"

//===================================

namespace Ui {class CIhm;}

class CommunicationBluetooth;
class CTrame;
class CTable;

/**
 * @brief Classe principale de l'application (IHM)
 *
 * @class CIhm
 *
 * @author Racamond Adrien
 *
 * @version 0.9
 *
 */
class CIhm : public QWidget, public Ui::CIhm
{
    Q_OBJECT
    
public:
    explicit CIhm(QWidget *parent = 0);
    float getRatioFenetreX(); /**< @brief Récupere le ratio du width par rapport à la résolution par default 960x540  */
    float getRatioFenetreY();/**< @brief Récupere le ratio du height par rapport à la résolution par default 960x540  */
    /**
     * @brief Rafraichit le CSS lié à l'affichage (fontes, couleurs)
     *
     * @fn rafraichirCSS
     * @param ratio
     */
    void rafraichirCSS(float ratio);
    ~CIhm();

public slots:
    void rafraichirCSS(); /**< @brief Rafraichit le CSS lié à l'affichage (fontes, couleurs) utilisant getRatioFenetreY() */
    /**
     * @brief Affiche le nom du joueur sur la table
     *
     * @fn setInfoConnect
     * @param nom QString nom du joueur
     */
    void setInfoConnect(QString nom);
    /**
     * @brief Affiche le nom du périférique connecté
     *
     * @fn setNomPeripherique
     * @param nom QString nom du périférique
     */
    void setNomPeripherique(QString nom);
    /**
     * @brief Change d'ecran
     *
     * @fn setLayerEcran
     * @param layer enum voir const.h
     */
    void setLayerEcran(uint8_t layer);
    /**
     * @brief Commencer la seance.
     *
     * @fn commencerSeance
     */
    void commencerSeance();
    /**
     * @brief Finir la seance et afficher la fenêtre Recap.
     *
     * @fn finirSeance
     */
    void finirSeance();
    /**
     * @brief Reset des statistiques et de la configuration
     *
     * @fn resetSeance
     */
    void pauserSeance();
    /**
     * @brief Reprendre la seance suite a une pause
     *
     * @fn reprendreSeance
     */
    void reprendreSeance();
    /**
     * @brief Reprendre la seance suite a une pause
     *
     * @fn reprendreSeance
     */
    void resetSeance();
    /**
     * @brief Calcul et affiche l'impact sur l'IHM et la table
     *
     * @fn impacterZone
     * @param zone enum voir const.h
     */
    void impacterZone(uint8_t zone);
    /**
     * @brief La balle a été capté sur le capteur coté joueur, ajout de la balle
     *
     * @fn balleEnJeu
     */
    void balleEnJeu();
    /**
     * @brief Place le robot sur la table
     *
     * @fn setZoneRobot
     * @param zone enum voir const.h
     */
    void setZoneRobot(uint8_t zone);
    /**
     * @brief Place la zone objectif sur la table
     *
     * @fn setZoneObjectif
     * @param zone enum voir const.h
     */
    void setZoneObjectif(uint8_t zone);
    /**
     * @brief Definit le nombre de balles maximum pour la seance
     *
     * @fn setBallesMaximum
     * @param balles int
     */
    void setBallesMaximum(int balles);
    void rafraichirHeure(); /**< @brief Rafraichit l'heure sur l'IHM (Logo et Table) */
    void rafraichirTimerSeance(); /**< @brief Rafraichit le timer de la seance */
    void quitter(); /**< @brief Quitte l'application (utilisé par le raccourcit CTRL+Q)  */

private:
    QString ballesTotalSurBallesMaximum(); /**< @brief String affichant le l'état des balles renvoyées sur le nombre de balles parametrée pour la seance */
    void connecterSignaux(); /**< @brief Réalise la connexion des slots/signaux  */

    void initialisationStats(); /**< @brief Initialisation des statistiques de la seance*/
    void rafraichirStats(); /**< @brief Rafraichissement des statistiques de la seance exécuté a chaque impact/changement de parametre */

    void raccourcisClavier();  /**< @brief Implémentation des raccourcis clavier */
    void gererArguments();  /**< @brief Verification des options de lancement */

    QString calculerPourcentageQString(int x,int y); /**< @brief Récupere sous forme de QString un pourcentage: "(X%)" utilisé pour les statistiques */

    void setTimerSeance(unsigned int iTemps = 0); /**< @brief Ecriture du temps dans m_pQLabelTimerSeance @param Minutes, Secondes */
    QString getTimerSeanceString(unsigned int iTemps);

    CommunicationBluetooth* m_pCommunicationBluetooth; /**< @brief Gestion de la communication Bluetooth */
    QThread*                m_pThreadCommunicationBluetooth; /**< @brief Thread pour la classe CommunicationBluetooth */
    CTable*                 m_pTable; /**< @brief Association vers la classe CTable */
    CTrame*                 m_pTrame;
    QFont                   m_font;
    QFont                   m_fontSmall;
    QFont                   m_fontNormal;
    QFont                   m_fontNom;
    QString                 m_fontNoirStyle;
    QString                 m_fontRougeStyle;
    QString                 m_fontVertStyle;
    QString                 m_fontTitreStyle;

    unsigned int            m_iTempsSeance; /**< @brief Temps en seconde de la seance */

    QTimer* m_pTimerHeure; /**< @brief Timer rafraichissant l'Heure  */
    QTimer* m_pTimerSeance; /**< @brief Timer incrémentant le temps de la seance  */

private slots:
    void initialisationFenetre(); /**< @brief Initialise la taille des fenetres en fonction de la résolution de l'ecran en mode plein-ecran */
    void setLayerEcranLogo(); /**< @brief Bascule sur le menu d'attente  */
    void setLayerEcranTable(); /**< @brief Bascule sur l'interface de la table  */
    void setLayerEcranRecap(); /**< @brief Bascule sur la récapitulation de la séance  */
    void deconnecterJoueur(); /**< @brief Actions nécéssaires a la deconnexion du joueur */
    void connecterJoueur(); /**< @brief Actions nécéssaires a la connexion du joueur */

    //==========
    // DEBUG
    void impacterRandom(); /**< @brief [DEBUG] Envoi une balle aléatoire sur la table coté robot */
    void setZoneRobotRandom(); /**< @brief [DEBUG] Definir une position du robot aléatoire (executé depuis un button sur l'IHM) */
    void setZoneObjectifRandom(); /**< @brief [DEBUG] Definir un objectif aléatoire (executé depuis un button sur l'IHM) */
    void setInfoConnectDemo(); /**< @brief [DEBUG] Execute setInfoConnect() avec le nom de demonstration */
    void setNomPeripheriqueDemo(); /**< @brief [DEBUG] Execute setNomPeripherique() avec le nom de demonstration */
    void envoyerCommande(); /**< @brief [DEBUG] Envoi une commande de la console, accepte aussi les trames  */
    void activerConsole(); /**< @brief [DEBUG] Reset le CSS de la console (nécéssaire a cause du delai)  */
};

#endif // IHM_H
