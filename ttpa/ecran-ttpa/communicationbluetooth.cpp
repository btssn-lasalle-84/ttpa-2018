#include "communicationbluetooth.h"

/**
  *
  * @file communicationbluetooth.cpp
  *
  * @brief Définition de la classe CommunicationBluetooth
  *
  */

CommunicationBluetooth::CommunicationBluetooth(QString nomPort, CommunicationBluetooth::Mode mode /*= Scrutation*/, QObject *parent) : QObject(parent), fini(false), periode(PERIODE_SURVEILLANCE), etatRFCOMM(RFCOMM_ARRETE)
{
    #ifndef QT_NO_DEBUG
    qDebug() << Q_FUNC_INFO << QThread::currentThreadId() << this;
    #endif
    demarrerRFCOMM();

    // cf. méthode main()
    if(mode == CommunicationBluetooth::Scrutation)
    {
        m_pPortSerie = new QextSerialPort(QextSerialPort::Polling, this);
        m_pPortSerie->setPortName(QString("/dev/") + nomPort);
        surveillerConnexion();
        #ifndef QT_NO_DEBUG
        qDebug() << Q_FUNC_INFO << QString::fromUtf8("Mode Polling (par scrutation)");
        #endif
    }
    else /* CommunicationBluetooth::Evenement */
    {
        m_pPortSerie = new QextSerialPort(QextSerialPort::EventDriven, this);
        m_pPortSerie->setPortName(QString("/dev/") + nomPort);
        surveillerConnexion();

        timerSurveillance = new QTimer(this);
        timerSurveillance->setInterval(periode);
        connect(timerSurveillance, SIGNAL(timeout()), this, SLOT(surveillerConnexion()));
        timerSurveillance->start();
        #ifndef QT_NO_DEBUG
        qDebug() << Q_FUNC_INFO << QString::fromUtf8("Mode EventDriven (par évènement)");
        #endif
    }
}

CommunicationBluetooth::~CommunicationBluetooth()
{
    #ifndef QT_NO_DEBUG
    qDebug() << Q_FUNC_INFO << QThread::currentThreadId() << this;
    #endif
    fermer();
    arreterRFCOMM();
}

void CommunicationBluetooth::ouvrir()
{
    if (m_pPortSerie->isOpen())
    {
        #ifndef QT_NO_DEBUG
        qDebug() << Q_FUNC_INFO << QString::fromUtf8("Le port %1 est ouvert").arg(m_pPortSerie->portName());
        #endif
        return;
    }
    #ifndef QT_NO_DEBUG
    qDebug() << Q_FUNC_INFO << QThread::currentThreadId() << this;
    #endif
    // ouverture du port
    m_pPortSerie->open(QIODevice::ReadWrite);
    if (!m_pPortSerie->isOpen())
    {
        #ifndef QT_NO_DEBUG
        qDebug() << Q_FUNC_INFO << QString::fromUtf8("Le port %1 n'est pas ouvert").arg(m_pPortSerie->portName());
        #endif
        return;
    }
    else
        #ifndef QT_NO_DEBUG
        qDebug() << Q_FUNC_INFO << QString::fromUtf8("Ouverture du port %1 réussie").arg(m_pPortSerie->portName());
        #endif
    // configuration du port
    m_pPortSerie->setBaudRate(BAUD9600);
    m_pPortSerie->setDataBits(DATA_8);
    m_pPortSerie->setParity(PAR_NONE);
    m_pPortSerie->setStopBits(STOP_1);
    m_pPortSerie->setFlowControl(FLOW_OFF);
    if(m_pPortSerie->queryMode() == QextSerialPort::EventDriven)
    {
        if (m_pPortSerie->isOpen())
        {
            connect(m_pPortSerie, SIGNAL(readyRead()), this, SLOT(lirePort()));
            recupererNomBluetooth();
        }
    }
}

void CommunicationBluetooth::fermer()
{
    #ifndef QT_NO_DEBUG
    qDebug() << Q_FUNC_INFO << QThread::currentThreadId() << this;
    #endif
    if (m_pPortSerie->isOpen())
    {
        if(m_pPortSerie->queryMode() == QextSerialPort::EventDriven)
            disconnect(m_pPortSerie, SIGNAL(readyRead()), this, SLOT(lirePort()));
        m_pPortSerie->close();
    }
}

void CommunicationBluetooth::main()
{
    #ifndef QT_NO_DEBUG
    qDebug() << Q_FUNC_INFO << QThread::currentThreadId() << this << QString::fromUtf8("Communication Bluetooth démarrée");
    #endif

    if(m_pPortSerie->queryMode() == QextSerialPort::Polling)
    {
        // On interroge périodiquement le port de communication et on sureveille l'état de la (connexion)
        while(!fini)
        {
            lirePort();

            surveillerConnexion();
        }
    }
    else
    {
        // On crée une boucle d'événements nécessaire pour gérer les signaux (readyRead() et timeout()
        while(!fini)
        {
            QApplication::processEvents();
            //QApplication::processEvents(QEventLoop::ExcludeUserInputEvents, periode);
        }
    }
    #ifndef QT_NO_DEBUG
    qDebug() << Q_FUNC_INFO << QThread::currentThreadId() << this << QString::fromUtf8("Communication Bluetooth arrêtée");
    #endif
}

void CommunicationBluetooth::lirePort()
{
    if (!m_pPortSerie->isOpen())
        return;

    QByteArray donnees;

    // lecture des données disponibles
    while (m_pPortSerie->bytesAvailable())
    {
        donnees += m_pPortSerie->readAll();
        this->msleep(20); // en ms
        //::usleep(100000); // cf. timeout
    }

    QString donneesRecues = QString(QString::fromUtf8(donnees.data()));
    if(!donneesRecues.isEmpty())
    {
        #ifndef QT_NO_DEBUG
        qDebug() << Q_FUNC_INFO << QThread::currentThreadId() << this << QString::fromUtf8("Données reçues : ") << donneesRecues;
        #endif
        emit nouvellesDonneesRecues(donneesRecues);
    }
}

void CommunicationBluetooth::demarrerRFCOMM()
{
    FILE *resultat;
    resultat = popen("sudo systemctl start rfcomm.service 2> /dev/null", "r");
    pclose(resultat);
}

void CommunicationBluetooth::redemarrerRFCOMM()
{
    FILE *resultat;
    resultat = popen("sudo systemctl restart rfcomm.service 2> /dev/null", "r");
    pclose(resultat);
}

QString CommunicationBluetooth::lireEtatRFCOMM()
{
    FILE *resultat;
    char ligne[1024];
    QString reponse;

    // lit l'état de la connexion
    resultat = popen("rfcomm -a", "r");
    fgets(ligne, 1024, resultat);
    while (! feof(resultat))
    {
        reponse += ligne;
        fgets(ligne, 1024, resultat);
    }
    pclose(resultat);

    return reponse;
}

QString CommunicationBluetooth::lireEtatServiceRFCOMM()
{
    FILE *resultat;
    char ligne[1024];
    QString reponse;

    // lit l'état de la connexion
    resultat = popen("sudo systemctl status rfcomm.service 2> /dev/null", "r");
    fgets(ligne, 1024, resultat);
    while (! feof(resultat))
    {
        reponse += ligne;
        fgets(ligne, 1024, resultat);
    }
    pclose(resultat);

    return reponse;
}

void CommunicationBluetooth::arreterRFCOMM()
{
    FILE *resultat;
    resultat = popen("sudo systemctl stop rfcomm.service 2> /dev/null", "r");
    pclose(resultat);
}

void CommunicationBluetooth::surveillerConnexion()
{
    if(m_pPortSerie->bytesAvailable())
        return;

    attendrePeriode();

    QString etat = lireEtatServiceRFCOMM();

    if(!etat.isEmpty())
    {
        if(etat.contains("inactive"))
        {
            #ifndef QT_NO_DEBUG
            qDebug() << Q_FUNC_INFO << QString::fromUtf8("Bluetooth rfcomm0 inactif");
            #endif
            demarrerRFCOMM();
            //return;
            etat = lireEtatServiceRFCOMM();
        }
        if(etat.contains("active"))
        {
            #ifndef QT_NO_DEBUG
            //qDebug() << Q_FUNC_INFO << QString::fromUtf8("Bluetooth rfcomm0 actif");
            #endif
            etat = lireEtatRFCOMM();

            #ifndef QT_NO_DEBUG
            //qDebug() << Q_FUNC_INFO << etat << etatRFCOMM;
            #endif
            if(!etat.isEmpty())
            {
                if(etat.contains("connected"))
                {
                    if(etatRFCOMM != RFCOMM_CONNECTE)
                    {
                        etatRFCOMM = RFCOMM_CONNECTE;
                        #ifndef QT_NO_DEBUG
                        qDebug() << Q_FUNC_INFO << QString::fromUtf8("Bluetooth rfcomm0 connecté");
                        #endif
                        ouvrir();
                        emit connecterJoueur();
                        return;
                    }
                }
                else if(etat.contains("closed"))
                {
                    if(etatRFCOMM != RFCOMM_FERME)
                    {
                        etatRFCOMM = RFCOMM_FERME;
                        #ifndef QT_NO_DEBUG
                        qDebug() << Q_FUNC_INFO << QString::fromUtf8("Bluetooth rfcomm0 fermé");
                        #endif
                        fermer();
                        this->msleep(100); // en ms
                        redemarrerRFCOMM();
                        emit deconnecterJoueur();
                        return;
                    }
                }

            }
            else
            {
                if(etatRFCOMM != RFCOMM_ARRETE)
                {
                    etatRFCOMM = RFCOMM_ARRETE;
                    #ifndef QT_NO_DEBUG
                    qDebug() << Q_FUNC_INFO << QString::fromUtf8("Aucune connexion Bluetooth rfcomm0");
                    #endif
                }
            }
        }
    }
}

void CommunicationBluetooth::finir()
{
    fini = true;
    annuler();
}

void CommunicationBluetooth::terminer()
{
}

void CommunicationBluetooth::attendrePeriode()
{
    this->msleep(periode); // en ms
}

void CommunicationBluetooth::recupererNomBluetooth()
{
    FILE *resultat;
    char ligne[1024];
    QString reponse;

    resultat = popen("rfcomm -a", "r");     // récupération de l'adresse
    fgets(ligne, 1024, resultat);
    while (! feof(resultat))
    {
        reponse += ligne;
        fgets(ligne, 1024, resultat);
    }
    pclose(resultat);

    QString adresseMAC;

    if (reponse.size() == 0)
    {
        qDebug() << Q_FUNC_INFO << "/!\\ NE PEUT PAS RECUPERER L'ADRESSE MAC /!\\";
        return;
    }

    adresseMAC = reponse.mid(30, 17);

    QString commande = "hcitool name " + adresseMAC;

    reponse = "";

    resultat = popen(commande.toAscii(), "r");     // récupération du nom
    fgets(ligne, 1024, resultat);
    while (! feof(resultat))
    {
        reponse += ligne;
        fgets(ligne, 1024, resultat);
    }
    pclose(resultat);

    if (reponse.size() == 0)
    {
        qDebug() << Q_FUNC_INFO << "/!\\ NE PEUT PAS RECUPERER LE NOM DE L'APPAREIL /!\\";
        return;
    }

    qDebug() << Q_FUNC_INFO << "Nom de l'appareil connecte : " << reponse.trimmed();

    emit setNomPeripherique(reponse.trimmed());
}

int CommunicationBluetooth::getEtatRFCOMM() { return etatRFCOMM; }
