#include "ihm.h"
#include "communicationbluetooth.h"

CIhm::CIhm(QWidget *parent) : QWidget(parent)
{
    setupUi(this);
    raccourcisClavier();

    #ifndef QT_NO_DEBUG
    qDebug() << Q_FUNC_INFO <<  "PID :" << (int)qApp->applicationPid() << "TID :" << QApplication::instance()->thread()->currentThreadId() << qApp->thread();
    #endif

    // Gestion de la communication Bluetooth
    m_pCommunicationBluetooth = new CommunicationBluetooth(PORT_BLUETOOTH, CommunicationBluetooth::Evenement);
    // ou :
    //communicationBluetooth = new CommunicationBluetooth(PORT_BLUETOOTH, CommunicationBluetooth::Scrutation);
    m_pThreadCommunicationBluetooth = new QThread;
    m_pCommunicationBluetooth->moveToThread(m_pThreadCommunicationBluetooth);

    m_pTrame = new CTrame(this);

    m_fontNoirStyle = QString::fromUtf8("QLabel\n{\n     color: #000000;\n}");
    m_fontRougeStyle = QString::fromUtf8("QLabel\n{\n     color: #FF0000;\n}");
    m_fontVertStyle = QString::fromUtf8("QLabel\n{\n     color: #006500;\n}");
    m_fontTitreStyle = QString::fromUtf8("QLabel\n{\n     color: #000000;\n background: qlineargradient(spread:reflect, x1:0.5, y1:0, x2:1, y2:0, stop:0 rgba(255, 255, 255, 255), stop:1 rgba(0, 0, 0, 0)); \n}");

    m_pTable = new CTable(this);
    QTimer::singleShot(DELAI_FIXFENETRE, this, SLOT(initialisationFenetre()));
    QTimer::singleShot(DELAI_FIXFENETRE*4, this, SLOT(initialisationFenetre()));
    m_pHBLayoutTable->addWidget(m_pTable);


    m_pTimerHeure = new QTimer(this);
    m_pTimerHeure->setInterval(3000);

    m_pTimerSeance = new QTimer(this);
    m_pTimerSeance->setInterval(1000);
    m_iTempsSeance = 0;
    setTimerSeance(0);

    gererArguments();

    m_pFenetres->setCurrentIndex(LAYER_LOGO);

    connecterSignaux();

    m_pTimerHeure->start();

    // démarre la communication Bluetooth
    m_pThreadCommunicationBluetooth->start();

}

CIhm::~CIhm()
{
    // Ferme la communication Bluetooth
    m_pCommunicationBluetooth->finir();
    m_pThreadCommunicationBluetooth->quit();
    m_pThreadCommunicationBluetooth->wait();
    delete m_pCommunicationBluetooth;
    delete m_pThreadCommunicationBluetooth;
    #ifndef QT_NO_DEBUG
    qDebug() << Q_FUNC_INFO << "fin";
    #endif
}

void CIhm::connecterSignaux()
{
    // Thread Bluetooth
    connect(m_pThreadCommunicationBluetooth, SIGNAL(started()), m_pCommunicationBluetooth, SLOT(main()));
    connect(m_pThreadCommunicationBluetooth, SIGNAL(finished()), m_pCommunicationBluetooth, SLOT(terminer()));

    // Timers
    connect(m_pTimerHeure, SIGNAL(timeout()),this, SLOT(rafraichirHeure()));
    connect(m_pTimerSeance, SIGNAL(timeout()),this, SLOT(rafraichirTimerSeance()));

    // Gestion des trames
    connect(m_pCommunicationBluetooth, SIGNAL(nouvellesDonneesRecues(QString)), m_pTrame, SLOT(traiterTrame(QString)));
    connect(m_pTrame, SIGNAL(setLayerEcran(uint8_t)), this, SLOT(setLayerEcran(uint8_t)));
    connect(m_pTrame, SIGNAL(setInfoConnect(QString)), this, SLOT(setInfoConnect(QString)));
    connect(m_pTrame, SIGNAL(resetSeance()), this, SLOT(resetSeance()));
    connect(m_pTrame, SIGNAL(commencerSeance()), this, SLOT(commencerSeance()));
    connect(m_pTrame, SIGNAL(pauserSeance()), this, SLOT(pauserSeance()));
    connect(m_pTrame, SIGNAL(reprendreSeance()), this, SLOT(reprendreSeance()));
    connect(m_pTrame, SIGNAL(finirSeance()), this, SLOT(finirSeance()));
    connect(m_pTrame, SIGNAL(impacterZone(uint8_t)), this, SLOT(impacterZone(uint8_t)));
    connect(m_pTrame, SIGNAL(balleEnJeu()), this, SLOT(balleEnJeu()));
    connect(m_pTrame, SIGNAL(setZoneRobot(uint8_t)), this, SLOT(setZoneRobot(uint8_t)));
    connect(m_pTrame, SIGNAL(setZoneObjectif(uint8_t)), this, SLOT(setZoneObjectif(uint8_t)));
    connect(m_pTrame, SIGNAL(setBallesMaximum(int)), this, SLOT(setBallesMaximum(int)));
    connect(m_pTrame, SIGNAL(rafraichirCSS()), this, SLOT(rafraichirCSS()));
    connect(m_pTrame, SIGNAL(quitter()), this, SLOT(quitter()));
    
    connect(m_pCommunicationBluetooth, SIGNAL(deconnecterJoueur()), this, SLOT(deconnecterJoueur()));
    connect(m_pCommunicationBluetooth, SIGNAL(connecterJoueur()), this, SLOT(connecterJoueur()));

    connect(m_pCommunicationBluetooth, SIGNAL(setNomPeripherique(QString)), this, SLOT(setNomPeripherique(QString)));
}

void CIhm::deconnecterJoueur()
{
    qDebug() << Q_FUNC_INFO;
    setLayerEcran(LAYER_LOGO);
    m_pQLabelLogoTexte->setText(LOGO_ATTENTECONNEXION);
    m_pQLabelLogoNom->setText("");
    m_pQLabelLogoNomMessage->setText("");
    resetSeance();
}
void CIhm::connecterJoueur()
{
    qDebug() << Q_FUNC_INFO;
    setLayerEcran(LAYER_LOGO);
//    m_pQLabelLogoTexte->setText(LOGO_ATTENTEIDENTIFICATION); Ancien Message
    m_pQLabelLogoTexte->setText(LOGO_ATTENTECONFIGURATION);
}
//=====================
// GRAPHIQUE

void CIhm::initialisationFenetre()
{
    rafraichirCSS(getRatioFenetreY());

    //=====================
    // LAYER LOGO
    m_pQLabelLogo->setFixedHeight(this->height() /2);
    m_pQLabelLogo->setFixedWidth(this->width() /2);
    m_pQLabelLogoTexte->setFixedHeight(this->height() / RATIO_ENTETE);
    m_pQLabelLogoHeure->setFixedHeight(this->height() / RATIO_ENTETE);
    m_pQLabelLogoTexte->setText(LOGO_ATTENTECONNEXION);

    //=====================
    // LAYER TABLE
    qDebug() << Q_FUNC_INFO << QTime::currentTime().toString();
    m_pQLabelTopRightHeure->setFixedHeight(this->height() / RATIO_ENTETE);
    m_pQLabelTimerSeance->setFixedHeight(this->height() / RATIO_ENTETE);
    m_pQLabelTimerSeanceRecap->setFixedHeight(this->height() / RATIO_ENTETE);
    m_pQLabelTopMid->setFixedHeight(this->height() / RATIO_ENTETE);
    m_pQLabelTopMid->setStyleSheet(m_fontNoirStyle);
    m_pTable->setFiletTaille(getRatioFenetreY());
    initialisationStats();

    //=====================
    // LAYER RECAP
    m_pQLabelTopRightHeureRecap->setFixedHeight(this->height() / RATIO_ENTETE);
    m_pQLabelTopTexteRecap->setFixedHeight(this->height() / RATIO_ENTETE);
    m_pQLabelTopTexteRecap->setStyleSheet(m_fontTitreStyle);
    m_pQLabelTopTexteRecap->setFont(m_fontNormal);
    m_pQLabelTopTexteRecap->setText(RECAP_TITRE_TEXTE);

    //=====================
    // HEURE
    rafraichirHeure();
}

void CIhm::initialisationStats()
{
    m_pQLabelLeftStat1Texte->setFont(m_font);
    m_pQLabelLeftStat1Nb->setFont(m_font);

    m_pQLabelLeftStat1Texte->setStyleSheet(m_fontNoirStyle);
    m_pQLabelLeftStat1Nb->setStyleSheet(m_fontNoirStyle);

    m_pQLabelLeftStat1Texte->setText(TABLE_STAT1);

    //RECAP
    m_pQLabelLeftStat1TexteRecap->setFont(m_font);
    m_pQLabelLeftStat1TexteRecap->setStyleSheet(m_fontNoirStyle);
    m_pQLabelLeftStat2TexteRecap->setFont(m_font);
    m_pQLabelLeftStat2TexteRecap->setStyleSheet(m_fontNoirStyle);
    m_pQLabelLeftStat3TexteRecap->setFont(m_font);
    m_pQLabelLeftStat3TexteRecap->setStyleSheet(m_fontNoirStyle);
    m_pQLabelLeftStat4TexteRecap->setFont(m_font);
    m_pQLabelLeftStat4TexteRecap->setStyleSheet(m_fontNoirStyle);

    m_pQLabelLeftStat1PerRecap->setFont(m_font);
    m_pQLabelLeftStat1PerRecap->setStyleSheet(m_fontNoirStyle);
    m_pQLabelLeftStat2PerRecap->setFont(m_font);
    m_pQLabelLeftStat2PerRecap->setStyleSheet(m_fontNoirStyle);
    m_pQLabelLeftStat3PerRecap->setFont(m_font);
    m_pQLabelLeftStat3PerRecap->setStyleSheet(m_fontNoirStyle);
    m_pQLabelLeftStat4PerRecap->setFont(m_font);
    m_pQLabelLeftStat4PerRecap->setStyleSheet(m_fontNoirStyle);

    m_pQLabelLeftStat1NbRecap->setFont(m_fontNormal);
    m_pQLabelLeftStat1NbRecap->setStyleSheet(m_fontNoirStyle);
    m_pQLabelLeftStat2NbRecap->setFont(m_fontNormal);
    m_pQLabelLeftStat2NbRecap->setStyleSheet(m_fontNoirStyle);
    m_pQLabelLeftStat3NbRecap->setFont(m_fontNormal);
    m_pQLabelLeftStat3NbRecap->setStyleSheet(m_fontNoirStyle);
    m_pQLabelLeftStat4NbRecap->setFont(m_fontNormal);
    m_pQLabelLeftStat4NbRecap->setStyleSheet(m_fontNoirStyle);

    m_pQLabelLeftStat1TexteRecap->clear();
    m_pQLabelLeftStat2TexteRecap->clear();
    m_pQLabelLeftStat3TexteRecap->clear();
    m_pQLabelLeftStat4TexteRecap->clear();

    m_pQLabelLeftStat1PerRecap->clear();
    m_pQLabelLeftStat2PerRecap->clear();
    m_pQLabelLeftStat3PerRecap->clear();
    m_pQLabelLeftStat4PerRecap->clear();

    m_pQLabelLeftStat1NbRecap->clear();
    m_pQLabelLeftStat2NbRecap->clear();
    m_pQLabelLeftStat3NbRecap->clear();
    m_pQLabelLeftStat4NbRecap->clear();

    rafraichirStats();
}

void CIhm::rafraichirStats()
{
    m_pQLabelLeftStat1Nb->setText(QString::number(m_pTable->getBallesHorsTable()) + " " + calculerPourcentageQString(m_pTable->getBallesHorsTable(), m_pTable->getBallesTotal() - 1));
    m_pQLabelLeftStat2NbRecap->setText(QString::number(m_pTable->getBallesHorsTable()) +  " / " + QString::number(m_pTable->getBallesTotal()) );
    m_pQLabelLeftStat2PerRecap->setText(calculerPourcentageQString(m_pTable->getBallesHorsTable(), m_pTable->getBallesTotal()) );
}

float CIhm::getRatioFenetreX()
{
    return ( (float)this->width()/TAILLE_FENETRE_DEFAULT_WIDTH );
}

float CIhm::getRatioFenetreY()
{
    return ( (float)this->height()/TAILLE_FENETRE_DEFAULT_HEIGHT );
}


QString CIhm::calculerPourcentageQString(int x,int y)
{
    if (!y)
        return "(0%)";

    return "(" + QString::number((double(x) / double(y))*100,'f',0) + "%)";
}

void CIhm::rafraichirCSS(float ratio)
{
    //=========================
    //   SPECIFIQUE AU LOGO
    //=========================

    m_font.setBold(true);
    m_fontSmall.setBold(true);
    m_fontNormal.setBold(true);
    m_fontNom.setBold(true);

    m_font.setPointSize((int)(TAILLE_TEXTE*ratio));
    m_fontSmall.setPointSize((int)(TAILLE_TEXTE_SMALL*ratio));
    m_fontNormal.setPointSize((int)(TAILLE_TEXTE_NORMAL*ratio));
    m_fontNom.setPointSize((int)(TAILLE_TEXTE_NOM*ratio));
    m_pQLabelLogoTexte->setFont(m_font);
    m_pQLabelLogoTexte->setStyleSheet(m_fontTitreStyle);
    m_pQLabelLogoHeure->setFont(m_fontNormal);
    m_pQLabelLogoHeure->setStyleSheet(m_fontNoirStyle);
    m_pQLabelLogoNom->setFont(m_font);
    m_pQLabelLogoNomMessage->setFont(m_font);
    m_pQLabelLogoNomMessage->setStyleSheet(m_fontNoirStyle);

    //=========================
    //  SPECIFIQUE A LA TABLE
    //=========================

    m_pQLabelTopMid->setFont(m_fontNormal);
    m_pQLabelTopLeftNom->setFont(m_fontNom);
    m_pQLabelTopLeftNomPeripherique->setFont(m_fontSmall);
    m_pQLabelTopRightHeure->setFont(m_fontNormal);
    m_pQLabelTopRightHeure->setStyleSheet(m_fontNoirStyle);
    m_pQLabelTimerSeance->setFont(m_fontNormal);
    m_pQLabelTimerSeanceRecap->setFont(m_fontNormal);
    m_pQLabelTopMid->setText(ballesTotalSurBallesMaximum());

    m_pTable->rafraichirCSS(ratio);


    //=========================
    //  SPECIFIQUE AU RECAP
    //=========================
    m_pQLabelTopRightHeureRecap->setFont(m_fontNormal);
    m_pQLabelTopRightHeureRecap->setStyleSheet(m_fontNoirStyle);
    m_pQLabelTopLeftNomRecap->setFont(m_fontNom);
    m_pQLabelTopLeftNomPeripheriqueRecap->setFont(m_fontSmall);
}
void CIhm::rafraichirCSS() { rafraichirCSS(getRatioFenetreY()); }

QString CIhm::ballesTotalSurBallesMaximum()
{
    QString ballesMax = QString::number(m_pTable->getBallesMaximum());

    if (!m_pTable->getBallesMaximum())
        ballesMax = QString::fromUtf8("∞");

    return /*QString::fromUtf8(IHM_BALLESENVOYEES) +*/ QString::number(m_pTable->getBallesTotal()) + " / " + ballesMax;
}

//=====================
// LAYER ECRAN

void CIhm::setLayerEcran(uint8_t layer)
{
    m_pTable->setLayerEcran(layer,getRatioFenetreY());
    m_pFenetres->setCurrentIndex(layer);
    qDebug() << Q_FUNC_INFO;
}
void CIhm::setLayerEcranTable() { setLayerEcran(LAYER_TABLE); }
void CIhm::setLayerEcranLogo() { setLayerEcran(LAYER_LOGO); }
void CIhm::setLayerEcranRecap() { setLayerEcran(LAYER_RECAP); }

void CIhm::resetSeance()
{
    qDebug() << Q_FUNC_INFO;
    m_pTable->resetSeance();
    rafraichirCSS(getRatioFenetreY());
    rafraichirStats();
    setInfoConnect(m_pQLabelLogoNom->text());

    m_iTempsSeance = 0;
    m_pTimerSeance->stop();
    setTimerSeance(0);
}

//=====================
// SLOTS LIE A CTRAME

void CIhm::setInfoConnect(QString nom)
{
    if (nom == "")
        return;

    m_pQLabelTopLeftNomRecap->setText(nom);
    m_pQLabelTopLeftNom->setText(nom);
//    m_pQLabelLogoNom->setText(nom);   affiche le nom du peripherique depuis 1.1
    m_pQLabelLogoTexte->setText(LOGO_ATTENTECONFIGURATION);
    qDebug() << Q_FUNC_INFO << " nom : " << nom;

    float tailleNomRatio;
    if (nom.length() > 20)
        tailleNomRatio = (4.0 / (float)(nom.length()) + (1-4.0/20));
    else
        tailleNomRatio = 1.0;

    m_fontNom.setPointSize((int)(TAILLE_TEXTE_NOM*getRatioFenetreY()) * tailleNomRatio);
//    m_pQLabelTopLeftNom->setFont(m_fontNom);       affiche le nom du peripherique depuis 1.1
    m_pQLabelTopLeftNomRecap->setFont(m_fontNom);
}

void CIhm::setNomPeripherique(QString nom = "Peripherique DEMO")
{
    m_pQLabelLogoNom->setText(nom);
    m_pQLabelTopLeftNomPeripherique->setText("(" + nom + ")");
    m_pQLabelTopLeftNomPeripheriqueRecap->setText("(" + nom + ")");

    m_pQLabelLogoNom->setStyleSheet(m_fontVertStyle);
    m_pQLabelTopLeftNomPeripherique->setStyleSheet(m_fontVertStyle);
    m_pQLabelTopLeftNomPeripheriqueRecap->setStyleSheet(m_fontVertStyle);

    m_pQLabelLogoNomMessage->setText(LOGO_JOUEUR_CONNECTE);
}


void CIhm::setZoneRobot(uint8_t zone)
{
    qDebug() << Q_FUNC_INFO << "ZONE: " << zone;
    m_pTable->setZoneRobot(zone);
}

void CIhm::setZoneObjectif(uint8_t zone)
{
    qDebug() << Q_FUNC_INFO << "ZONE: " << zone;
    m_pTable->setZoneObjectif(zone);
    rafraichirStats();
}

void CIhm::setBallesMaximum(int balles)
{
    qDebug() << Q_FUNC_INFO << "BALLES: " << balles;
    m_pTable->setBallesMaximum(balles);
//    rafraichirCSS(getRatioFenetreY()); // nécéssaire pour refresh l'IHM car ballemax est utilisé par elle aussi
    m_pQLabelTopMid->setFont(m_fontNormal);
    m_pQLabelTopMid->setText(ballesTotalSurBallesMaximum());
}

void CIhm::impacterZone(uint8_t zone)
{
    qDebug() << Q_FUNC_INFO;
    m_pTable->impacterZone(zone);
    m_pQLabelTopMid->setText(ballesTotalSurBallesMaximum());
    rafraichirStats();
}

void CIhm::balleEnJeu()
{
    qDebug() << Q_FUNC_INFO;
    m_pTable->balleEnJeu();
    m_pQLabelTopMid->setFont(m_fontNormal);
    m_pQLabelTopMid->setText(ballesTotalSurBallesMaximum());
    rafraichirStats();
}

void CIhm::commencerSeance()
{
    qDebug() << Q_FUNC_INFO;

//    if (m_pFenetres->currentIndex() != LAYER_TABLE)
//        return;

    setLayerEcran(LAYER_TABLE);

    m_iTempsSeance = 0;
    m_pTimerSeance->start(1000);

    m_pQLabelTimerSeance->setStyleSheet(CSS_TIMER_ON);

    m_pHBLayoutTable->addWidget(m_pTable);
    m_pTable->resetStatistiques();
    m_pQLabelTopMid->setText(ballesTotalSurBallesMaximum());

    setTimerSeance(0);
    rafraichirStats();
}
void CIhm::pauserSeance()
{
    qDebug() << Q_FUNC_INFO;

    m_pTimerSeance->stop();

    m_pQLabelTimerSeance->setStyleSheet(CSS_TIMER_OFF);
    m_pQLabelTimerSeance->setText(QString::fromUtf8("▮▮ ") + getTimerSeanceString(m_iTempsSeance));
}
void CIhm::reprendreSeance()
{
    qDebug() << Q_FUNC_INFO;

    m_pTimerSeance->start(1000);
    m_pQLabelTimerSeance->setStyleSheet(CSS_TIMER_RES);
    m_pQLabelTimerSeance->setText(QString::fromUtf8("▶  ") + getTimerSeanceString(m_iTempsSeance));
}
void CIhm::finirSeance()
{
    qDebug() << Q_FUNC_INFO;
    setLayerEcran(LAYER_RECAP);
    QString decalage = "        ";
    m_pTimerSeance->stop();
    m_pTable->finirSeance();

    //==================
    // STAT 1
    if(m_pTable->getZoneObjectif() == ZONE_AUCUNE)
    {
        m_pQLabelLeftStat1TexteRecap->setText(decalage+ RECAP_STAT1_ALT);
        m_pQLabelLeftStat1NbRecap->setText(QString::number(m_pTable->getBallesBonnes()) + " / " + QString::number(m_pTable->getBallesTotal()) );
        m_pQLabelLeftStat1PerRecap->setText(calculerPourcentageQString(m_pTable->getBallesBonnes(),m_pTable->getBallesTotal()) );
    }
    else
    {
        m_pQLabelLeftStat1TexteRecap->setText(decalage+ RECAP_STAT1);
        m_pQLabelLeftStat1NbRecap->setText(QString::number(m_pTable->getBallesObjectif()) + " / " + QString::number(m_pTable->getBallesTotal()) );
        m_pQLabelLeftStat1PerRecap->setText(calculerPourcentageQString(m_pTable->getBallesObjectif(),m_pTable->getBallesTotal()) );
    }
    //==================
    // STAT 2

    m_pQLabelLeftStat2TexteRecap->setText(decalage+ RECAP_STAT2);
    rafraichirStats();

    //==================
    // STAT 3

    m_pQLabelLeftStat3TexteRecap->setText(decalage+ RECAP_STAT3);
    m_pQLabelLeftStat3NbRecap->setText(QString::number(m_pTable->getBallesEnchainees()));


    //==================
    // STAT 4

    //        TODO

    m_pHBLayoutTableRecap->addWidget(m_pTable);

}

//=====================
// AUTRE

void CIhm::raccourcisClavier()
{
    // CTRL+Q
    QAction *actionQuitter = new QAction("&Quitter", this);
    actionQuitter->setShortcut(QKeySequence(Qt::CTRL + Qt::Key_Q));
    //actionQuitter->setShortcut(QKeySequence(QKeySequence::Quit)); // Ctrl+Q, NE FONCTIONNE PAS SUR PI
    addAction(actionQuitter);
    connect(actionQuitter, SIGNAL(triggered()), this, SLOT(quitter()));
}

void CIhm::quitter()
{
    close();
}

void CIhm::rafraichirHeure()
{
    QString zeroMinute = "", zeroHeure = "";    // CORRECTION LIEE AU ZERO
    if (QTime::currentTime().hour() < 10)
        zeroHeure = "0";
    if (QTime::currentTime().minute() < 10)
        zeroMinute = "0";

    QString format = zeroHeure + QString::number(QTime::currentTime().hour()) + ":" + zeroMinute + QString::number(QTime::currentTime().minute()) + " ";                                            // EASTER EGG  ( ͡° ͜ʖ ͡°)
                                                                                                                                                                                                    if (QTime::currentTime().hour() == 13 && QTime::currentTime().minute() == 37){ format = "L3:3T";}
    m_pQLabelTopRightHeureRecap->setText(format);
    m_pQLabelTopRightHeure->setText(format);
    m_pQLabelLogoHeure->setText(format);
}

void CIhm::rafraichirTimerSeance()
{
//    qDebug() << Q_FUNC_INFO;        /!\ Spam
    m_iTempsSeance++;

    m_pQLabelTimerSeance->setStyleSheet(CSS_TIMER_ON);
    setTimerSeance(m_iTempsSeance);
}

void CIhm::setTimerSeance(unsigned int iTemps)
{
    m_pQLabelTimerSeance->setText( getTimerSeanceString(iTemps) );
    m_pQLabelTimerSeanceRecap->setText(  getTimerSeanceString(iTemps)  );
}

QString CIhm::getTimerSeanceString(unsigned int iTemps)
{

    unsigned int iMinutes   = (unsigned int)((float)iTemps / (float)60);
    unsigned int iSecondes  = iTemps - (iMinutes*60);

    QString zeroSeconde = "";    // CORRECTION LIEE AU ZERO

    if (iSecondes < 10)
        zeroSeconde = "0";

    return QString::number(iMinutes) + ":" + zeroSeconde + QString::number(iSecondes) + " ";
}

//=====================
// DEBUG
void CIhm::gererArguments()
{
    QStringList args = QCoreApplication::arguments();   // Verification des arguments

    srand(QTime::currentTime().msec());

    m_pTable->m_args = args;
    //=============
    // DEV
    if (!args.contains("-dev",Qt::CaseInsensitive)) // Suppression des boutons pour les tests sans materiel
    {
        m_pQLabelLogoNom->setText("");
        m_pQLabelLogoNomMessage->setText("");

        delete m_pTestZoneRobotRandom;
        delete m_pTestZoneObjectifRandom;
        delete m_pTestZoneRandom;
        delete m_pTestZoneReset;
    }
    else
    {
        m_pQLabelTopLeftNomRecap->setText("MODE DEVELOPPEMENT");
        m_pQLabelTopLeftNomRecap->setStyleSheet(m_fontRougeStyle);

        m_pQLabelTopLeftNom->setText("MODE DEVELOPPEMENT");
        m_pQLabelTopLeftNom->setStyleSheet(m_fontRougeStyle);

        m_pQLabelLogoNom->setText("MODE DEVELOPPEMENT");
        m_pQLabelLogoNom->setStyleSheet(m_fontRougeStyle);
        m_pQLabelLogoNomMessage->setText("");

        m_pTable->setBallesMaximum(DEV_BALLESMAX);
    }
    if (!args.contains("-dev",Qt::CaseInsensitive) && !args.contains("-console",Qt::CaseInsensitive))
    {
        delete m_pTestOutrepasser;
        delete m_pTestStartRecap;
    }
    else
    {
        connect(m_pTestOutrepasser, SIGNAL(pressed()), this, SLOT(commencerSeance()));
        connect(m_pTestStartRecap, SIGNAL(pressed()), this, SLOT(commencerSeance()));
    }
    //=============
    // WINDOWED
    if (!args.contains("-windowed",Qt::CaseInsensitive)) // mode fenétré
    {
        this->showFullScreen();
    }
    //=============
    // MODE DEMO
    if (args.contains("-demo",Qt::CaseInsensitive)) // Mode de demonstration
    {

        setBallesMaximum(DEV_BALLESMAX);

        QTimer::singleShot(2000, this, SLOT(connecterJoueur()));
        QTimer::singleShot(2000, this, SLOT(setInfoConnectDemo()));
        QTimer::singleShot(2000, this, SLOT(setNomPeripheriqueDemo()));
        QTimer::singleShot(3500, this, SLOT(setZoneObjectifRandom()));
        if (!args.contains("-norobot",Qt::CaseInsensitive))
            QTimer::singleShot(4000, this, SLOT(setZoneRobotRandom()));
        QTimer::singleShot(5000, this, SLOT(commencerSeance()));
        QTimer::singleShot(4000, this, SLOT(setLayerEcranTable()));

        for(int i=0 ; i < DEV_BALLESMAX ; i++)
        {
            QTimer::singleShot((6500) + i*1000, this, SLOT(balleEnJeu()));
            if(rand() % 10) // 1 chance sur 10 de rater
                QTimer::singleShot((7000) + i*1000, this, SLOT(impacterRandom()));
        }
        QTimer::singleShot((7000) + (1+DEV_BALLESMAX)*1000, this, SLOT(finirSeance()));
        QTimer::singleShot((15000) + (DEV_BALLESMAX)*1000, this, SLOT(deconnecterJoueur()));
    }
    //=============
    // CONSOLE
    if (!args.contains("-console",Qt::CaseInsensitive))
    {
        delete m_pConsole;
    }
    else
    {
        connect(m_pConsole, SIGNAL(returnPressed()), this, SLOT(envoyerCommande()));
    }
}

void CIhm::impacterRandom()
{
    qDebug() << ">> DEV! " << Q_FUNC_INFO;
    impacterZone(rand() % 9);
}

void CIhm::setZoneRobotRandom()
{
    qDebug() << ">> DEV! " << Q_FUNC_INFO;
    int random = -1;

    while (random < 0 || random == m_pTable->getZoneObjectif())
    {
        srand(QTime::currentTime().msec());
        random = rand() % 9;
        m_pTable->setZoneRobot(random);
    }
}

void CIhm::setZoneObjectifRandom()
{
    qDebug() << ">> DEV! " << Q_FUNC_INFO;
    m_pTable->setZoneObjectif(rand() % 9);
    rafraichirStats();
}

void CIhm::setInfoConnectDemo() { setInfoConnect(IHM_NOMDETEST); }
void CIhm::setNomPeripheriqueDemo() { setNomPeripherique(IHM_PERIPHERIQUEDETEST); }

void CIhm::envoyerCommande()
{
    QString texte = m_pConsole->text();

    QTimer::singleShot(300, this, SLOT(activerConsole()));
    m_pConsole->setStyleSheet("QLineEdit#m_pConsole\n"
                              "{\n"
                              "     background-color: #00FF00;\n"
                              "}");

    if(texte.startsWith("$TTPA:"))
    {
        bool retour = m_pTrame->traiterTrame(texte);

        if (!retour)
            m_pConsole->setStyleSheet("QLineEdit#m_pConsole\n{\nbackground-color: #FF0000;\n}");

        return;
    }
    if(texte.startsWith("hide "))
    {
        QStringList args = texte.split(" ");
        if(args.length() == 2)
        {
            m_pConsole->hide();
            QTimer::singleShot(1000 * args.at(1).toInt(), m_pConsole, SLOT(show()));
        }
        return;
    }
    if (texte.toLower() == "quit")
    {
        quitter();
        return;
    }
    m_pConsole->setStyleSheet("QLineEdit#m_pConsole\n{\nbackground-color: #FF0000;\n}");
}
void CIhm::activerConsole()
{
    m_pConsole->setStyleSheet("QLineEdit#m_pConsole\n{\n\n}");
}
