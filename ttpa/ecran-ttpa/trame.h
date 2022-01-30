#ifndef TRAME_H
#define TRAME_H

#include "ihm.h"

class CIhm;

/**
  *
  * @file trame.h
  *
  * @brief Classe gérant la table et les calculs lié a la seance, impacts, affichage des zones de la table
  *
  * @author Racamond Adrien
  *
  * @version 1.0
  *
  */
class CTrame : public QObject
{

    Q_OBJECT

    public:
        CTrame(QObject* parent = 0);
        ~CTrame();

    signals:
        void    setInfoConnect(QString nom); /** *@brief voir setInfoConnect(QString nom) de CIhm */
        void    setLayerEcran(uint8_t layer); /** *@brief voir setLayerEcran(int layer) de CIhm  */
        void    commencerSeance(); /** * @brief voir commencerSeance() de CIhm  */
        void    pauserSeance(); /** * @brief voir pauserSeance() de CIhm  */
        void    reprendreSeance(); /** * @brief voir reprendreSeance() de CIhm  */
        void    finirSeance(); /** * @brief voir finirSeance() de CIhm  */
        void    resetSeance(); /** *@brief voir resetSeance() de CIhm  */
        void    impacterZone(uint8_t zone); /** *@brief voir impacterZone(int zone) de CIhm  */
        void    balleEnJeu(); /** *@brief voir balleEnJeu() de CIhm  */
        void    setZoneRobot(uint8_t zone); /** *@brief  voir setZoneRobot(int zone) de CIhm  */
        void    setZoneObjectif(uint8_t zone); /** *@brief  voir setZoneObjectif(int zone) de CIhm  */
        void    setBallesMaximum(int balles); /** *@brief  voir setBalleMaximum(int balles) de CIhm  */
        void    setFrequenceRobot(float freq); /** *@brief voirsetFrequenceRobot(int freq) de CTable */
        void    rafraichirCSS(); /** *@brief  voir rafraichirCSS() de CIhm  */
        void    quitter(); /** *@brief  voir quitter() de CIhm  */

    public slots:
        bool    traiterTrame(QString donneesRecues); /** *@brief découpe les trames de la reception */
        bool    gererTrame(QString donneesRecues); /** *@brief extrait les elements de la trame avec extraireElement(QString donneesRecues) puis effectue les signals en fonction @param Trame en QString @returns validité de la trame */

    private:
        QString extraireElement(QString donneesRecues, const int element); /** *@brief Découpe la trame et retourne l'élement @param Trame en QString, index de l'élément */

        int     getTrameLength(QString donneesRecues); /** *@brief Retourne la longeur de la trame @param Trame en QString */
        void    messageNonReconnu(QString donneesRecues, int element); /** *@brief Affiche les chars dans l'element demandé par rapport a la liste d'elements de la trame @param Trame en QString, element en int */
        bool    gererTramesSansParametre(QString donneesRecues); /** *@brief verrifie, identifie et execute les trames ne possédant pas de parametre */
        bool    gererTrames1Parametre(QString donneesRecues); /** *@brief verrifie, identifie et execute les trames composé d'un parametre */
};

#endif // TRAME_H
