#ifndef COMMUNICATIONBLUETOOTH_H
#define COMMUNICATIONBLUETOOTH_H

/**
  *
  * @file communicationbluetooth.h
  *
  * @brief Déclaration de la classe CommunicationBluetooth
  *
  */

#include <QtCore>
#include <QApplication>
#include "QextSerialPort/qextserialport.h"
#include "const.h"

#define PERIODE_SURVEILLANCE    200 /**< \brief durée de la temporisation périodique en ms */

/**
 * @brief Assure la réception des trames via le Bluetooth
 *
 * @class CommunicationBluetooth
 */
class CommunicationBluetooth : public QObject
{
    Q_OBJECT
public:
    /**
     * @brief Mode de gestion du port de communication
     *
     */
    enum Mode
    {
        Scrutation,
        Evenement
    };
    /**
     * @brief Constructeur
     *
     * @fn CommunicationBluetooth
     * @param nomPort QString le nom du fichier de périphérique Bluetooth (par défaut rfcomm0)
     * @param parent QObject Adresse de l'objet Qt parent (par défaut 0)
     */
    explicit CommunicationBluetooth(QString nomPort = "rfcomm0", Mode mode = Scrutation, QObject *parent = 0);
    /**
     * @brief Destructeur
     *
     * @fn ~CommunicationBluetooth
     */
    ~CommunicationBluetooth();

    /**
     * @brief Ouvre et configure le port série
     *
     * @fn ouvrir
     */
    void ouvrir();
    /**
     * @brief Ferme le port série
     *
     * @fn fermer
     */
    void fermer();
    /**
     * @brief Retourne l'état du port RFCOMM
     *
     * @fn getEtatRFCOMM
     */
    int getEtatRFCOMM();

private:
    QextSerialPort *m_pPortSerie; /**< \brief Agrégation vers la classe QextSerialPort */
    bool fini; /**< \brief état du Thread */
    QMutex mutex; /**< \brief pour gérer les temporisations */
    QWaitCondition waitCondition; /**< \brief pour les temporisations */
    int periode; /**< \brief la périodicité du thread */
    int etatRFCOMM; /**< \brief état du service RFCOMM */
    QTimer* timerSurveillance; /**< @brief pour surveiller périodiquement l'état de la connexsion Bluetooth  */

    /**
     * @brief Temporise l'exécution du Thread
     *
     * @fn msleep
     * @param sleepMS unsigned long durée de la temporisation en millisecondes
     */
    void msleep(unsigned long sleepMS)
    {
        waitCondition.wait(&mutex, sleepMS);
    }
    /**
     * @brief Termine la temporisation en cours
     *
     * @fn annuler
     */
    void annuler()
    {
        waitCondition.wakeAll();
    }
    /**
     * @brief Attend une durée PERIODE_SURVEILLANCE
     *
     * @fn attendrePeriode
     */
    void attendrePeriode();
    /**
     * @brief Démarre le service RFCOMM
     *
     * @fn demarrerRFCOMM
     */
    void demarrerRFCOMM();
    /**
     * @brief Redémarre le service RFCOMM
     *
     * @fn redemarrerRFCOMM
     */
    void redemarrerRFCOMM();
    /**
     * @brief Lit l'état du service RFCOMM
     *
     * @fn lireEtatRFCOMM
     * @return QString
     */
    QString lireEtatRFCOMM();
    QString lireEtatServiceRFCOMM();
    /**
     * @brief Arrête le service RFCOMM
     *
     * @fn arreterRFCOMM
     */
    void arreterRFCOMM();    
    /**
     * @brief Récupère le nom de l'appareil connécté
     *
     * @fn recupererNomBluetooth
     */
    void recupererNomBluetooth();

signals:
    /**
     * @brief Signale les données reçues sur le port Blutooth
     *
     * @fn nouvellesDonneesRecues
     * @param donneesRecues
     */
    void nouvellesDonneesRecues(QString donneesRecues);
    /**
     * @brief Signale une déconnexion du Bluetooth
     *
     * @fn deconnecterJoueur
     */
    void deconnecterJoueur();
    /**
     * @brief Signale une connexion du Bluetooth
     *
     * @fn connecterJoueur
     */
    void connecterJoueur();
    /**
     * @brief Affiche le nom du périférique connecté
     *
     * @fn setNomPeripherique
     * @param nom QString nom du périférique
     */
    void setNomPeripherique(QString nom);

public slots:
    /**
     * @brief Le corps du thread assurant la réception des données via le Bluetooth
     *
     * @fn main
     */
    void main();
    /**
     * @brief Met fin au Thread
     *
     * @fn finir
     */
    void finir();
    /**
     * @brief Termine le thread
     *
     * @fn terminer
     */
    void terminer();
    /**
     * @brief Lit les données disponibles sur le port Bluetooth
     *
     * @fn lirePort
     */
    void lirePort();
    /**
     * @brief Surveille l'état du service RFCOMM
     *
     * @fn surveillerConnexion
     */
    void surveillerConnexion();
};

#endif // COMMUNICATIONBLUETOOTH_H
