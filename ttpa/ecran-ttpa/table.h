#ifndef TABLE_H
#define TABLE_H

#include <QtGui>
#include <QVector>
#include "ihm.h"


/**
  *
  * @file table.h
  *
  * @brief Classe gérant la table et les calculs lié a la seance, impacts, affichage des zones de la table
  *
  * @author Racamond Adrien
  *
  * @version 1.0
  *
  */
class CTable : public QWidget
{
    Q_OBJECT
public:
    QStringList         m_args;
    QLabel*             m_pOverlayText;

    explicit CTable(QWidget *parent = 0);

    void setFiletTaille(float ratio); /** *@brief Definis la hauteur du filet en utilisant la hauteur de la fenetre actuelle @param usuellement getRatioFenetreY() de CIhm  */
    void rafraichirCSS(float ratio); /** *@brief Rafraichit le CSS en utilisant la hauteur de la fenetre actuelle @param usuellement getRatioFenetreY() de CIhm  */


    bool impacterZone(uint8_t numeroZone); /** *@brief Calcul et affiche l'impact sur l'IHM et la table @param enum zones_e, voir const.h  */
    void setZoneRobot(uint8_t zone); /** *@brief Place le robot sur la table @param enum zones_e, voir const.h  */
    void setZoneObjectif(uint8_t zone); /** *@brief Place la zone objectif sur la table @param enum zones_e, voir const.h  */
    void resetSeance(); /** *@brief Reset complet des variables des statistiques */
    void resetStatistiques(); /** *@brief Reset des statistiques uniquement */
    void balleEnJeu(); /** *@brief La balle a bien été mise en jeu par la machine, BalleTotal est incrémenté */
    void resetNbBallesZone(); /** *@brief Reset du nombre de balles dans chaque zone */
    void finirSeance();

    int getBallesBonnes();  /** *@brief Récupere le nombre de balles ayant été jouées et renvoyé par le joueur */
    int getBallesTotal(); /** *@brief Récupere le nombre de balles ayant été jouées par la machine (en comptant celles n'ayant pas atteint une zone car le joueur est mauvais ( ͡° ͜ʖ ͡°))  */
    int getBallesMaximum(); /** *@brief Récupere le nombre de balles maximum pour la seance */
    int getBallesObjectif(); /** *@brief Récupere le nombre de balles correspondant à la zone objectif actuel */
    int getBallesEnchainees(); /** *@brief Récupere le nombre de balles enchainées */
    int getBallesHorsTable(); /** *@brief Récupere le nombre de balles hors table */
    int getZoneToucheePrec(); /** *@brief Récupere la zone touchée précédente */
    void setBallesMaximum(int nb); /** *@brief Définis le nombre de balles maximum pour la séance @param nombre de balles en INT */
    uint8_t getZoneObjectif(); /** *@brief Recupère le numero de la case Objectif  */

    bool getBalleCoteTablePrec();   /** @brief Récupère le coté de la table ou la balle a frapé en dernier */
    bool getBalleCoteTable();   /** @brief Récupère le coté de la table ou la balle a frapé juste avant la derniere */

    void setLayerEcran(uint8_t layer, float tailleFenetreY); /** @brief Corrections lié au changement de fenetre */

//------------------------------------

private:
    void rafraichirNbBallesZone(); /** *@brief Rafraichit le nombre de balles par zone */
    QGridLayout*        m_pGridLayout;
    QVector<QLabel*>    m_pZones;
    QLabel*             m_pFilet;
    QLabel*             m_pOverlay;

    // LOGIQUE
    int                 m_iBallesBonnes;                 //Balles ayant touché la table du robot
    int                 m_iBallesMaximum;                //Balles maximum pour la seance
    int                 m_iBallesTotal;                  //Balles ayant été lancé et bonne du robot
    int                 m_iBallesEnchainees;          //Balles renvoyé d'affilée, reset en cas de faute
    int                 m_iBallesEnchaineesMax;          //Balles renvoyé d'affilée
    int                 m_iBallesHorsTable;                 //Balles hors table
    int                 m_iBallesDansZone[NB_ZONES];    //Balles dans la zone n

    // PARTIE GRAPHIQUE, CASES
    int                 m_iZoneTouchee;     //ACTUELLE
    int                 m_iZoneToucheePrec; //PRECEDENTE
    int                 m_iZoneRobot;
    int                 m_iZoneObjectif;

    bool                m_bBalleCoteTable;       //Coté de la table 1 = robot
    bool                m_bBalleCoteTablePrec;   //Coté de la table précedent

    // CSS
    QFont               m_font;
    QFont               m_fontBig;
    QFont               m_fontOverlay;
    QString             m_fondInactif;
    QString             m_fondActif;
    QString             m_fondRobot;
    QString             m_fondObjectif;
    QString             m_fondRate;

//------------------------------------
    
public slots:
    void rafraichirInactif(); /** *@brief Rafraichit les cases inactives (cases précédement activées redeviennent désactivées)  */
};

#endif // TABLE_H
