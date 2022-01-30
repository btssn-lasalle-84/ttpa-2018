#include "table.h"

CTable::CTable(QWidget *parent) :
    QWidget(parent)
{

    //===============================
    //          GRAPHIQUE
    //===============================

    m_pGridLayout = new QGridLayout(this);

    //===============================
    //          OVERLAY TABLE
    //===============================
    m_pOverlay = new QLabel(this);
    m_pOverlay->setStyleSheet(QString::fromUtf8("QLabel\n"
    "{\n"
    "	border-image: url(:/images/resources/table.jpg) 0 0 0 0 stretch stretch;\n"
    "}"));
    m_pOverlay->setMinimumSize(QSize(0, WIDGET_SIZE_MAX));
    m_pOverlay->setMaximumSize(QSize(WIDGET_SIZE_MAX, WIDGET_SIZE_MAX));
    m_pGridLayout->addWidget(m_pOverlay, 0, 0, 3, 3);

    //===============================
    //          ZONES
    //===============================
    QLabel* pZone;
    for(uint8_t i=0; i < NB_ZONES; i++)
    {
        pZone = new QLabel(this);
        pZone->setText("Zone " + QString::number(i+1));
        pZone->setFont(m_font);
        pZone->setStyleSheet(CSS_FOND_INACTIF);
        pZone->setAlignment(Qt::AlignCenter);

        //----------------------------------
        m_pZones.push_back(pZone);
        m_pGridLayout->addWidget(pZone, (i/3), (i%3));
    }

    //===============================
    //          OVERLAY TEXT
    //===============================


    /* Affichage SUR la table

    m_pOverlayText = new QLabel(this);
    m_pOverlayText->setMinimumSize(QSize(0, WIDGET_SIZE_MAX));
    m_pOverlayText->setMaximumSize(QSize(WIDGET_SIZE_MAX, WIDGET_SIZE_MAX));
    m_pOverlayText->setText("");
    m_fontOverlay.setPointSize(12);
    m_pOverlayText->setFont(m_fontOverlay);
    m_pOverlayText->setAlignment(Qt::AlignCenter);
    m_pGridLayout->addWidget(m_pOverlayText, 0, 0, 3, 3);
    m_pOverlayText->setStyleSheet(QString::fromUtf8("QLabel\n"
    "{\n"
    "   background-color: rgb(0, 0, 0, 0);\n"
    "	color: rgba(0,0,0,0);\n"
    "}"));
    */


    //===============================
    //          FILET
    //===============================
    m_pFilet = new QLabel(this);
    m_pFilet->setStyleSheet(QString::fromUtf8("QLabel\n"
    "{\n"
    "	background: url(:/images/resources/filet.jpg) cover;\n"
    "}"));
    m_pFilet->setMinimumSize(QSize(4, WIDGET_SIZE_MAX));
    m_pFilet->setMaximumSize(QSize(WIDGET_SIZE_MAX, 50));
    m_pGridLayout->addWidget(m_pFilet, 4, 0, 1, 0);

    //===============================
    setLayout(m_pGridLayout);
    m_pGridLayout->setSpacing(0);


    //===============================
    // LOGIQUE
    //===============================
    resetSeance();

    //AUTRE
    m_args.clear();
}

void CTable::setFiletTaille(float ratio)
{
    m_pFilet->setMinimumSize(QSize(4, (float)HAUTEUR_FILET*ratio));
    m_pFilet->setMaximumSize(QSize(WIDGET_SIZE_MAX, (float)HAUTEUR_FILET*ratio));
    qDebug() << Q_FUNC_INFO << ((float)HAUTEUR_FILET*ratio);
}

void CTable::rafraichirCSS(float ratio)
{
    m_font.setPointSize((int)(TAILLE_TEXTE_NB*ratio));
    m_font.setBold(true);
    m_fontBig.setPointSize((int)(TAILLE_TEXTE_NB_BIG*ratio));
    m_fontBig.setBold(true);
    m_fontOverlay.setPointSize((int)(TAILLE_OVERLAY*ratio));
    qDebug() << Q_FUNC_INFO << (TAILLE_TEXTE*ratio);

    for(uint8_t i=0; i < NB_ZONES; i++)
    {
        m_pZones[i]->setFont(m_font);
        m_pZones[i]->setStyleSheet(CSS_FOND_INACTIF);
    }
    //m_pOverlayText->setFont(m_fontOverlay);
}

void CTable::setLayerEcran(uint8_t layer, float tailleFenetreY)
{
    qDebug() << Q_FUNC_INFO  << " layer: " << layer << "tailleFenetreY: " << tailleFenetreY;
    int tailleTexte;

    if (layer == LAYER_RECAP)
        tailleTexte = TAILLE_TEXTE_NB*(tailleFenetreY/1.75);
    else
        tailleTexte = TAILLE_TEXTE_NB*(tailleFenetreY);

    m_font.setPointSize((int)tailleTexte);

    for(uint8_t i=0; i < NB_ZONES; i++)
    {
        m_pZones[i]->setFont(m_font);
    }
}

void CTable::resetNbBallesZone()
{
    for(uint8_t i=0; i < NB_ZONES; i++)
    {
        m_iBallesDansZone[i] = 0;
    //    m_pZones[i]->setText(QString::number(0) + '%');
    //    m_pZones[i]->setStyleSheet(CSS_FOND_INACTIF);
    }
    rafraichirNbBallesZone();
}

void CTable::rafraichirNbBallesZone()
{
    qDebug() << Q_FUNC_INFO;
    for(uint8_t i=0; i < NB_ZONES; i++)
    {
        if (i != m_iZoneRobot)
        {
            if (m_iBallesTotal)
                m_pZones[i]->setText(QString::number(((double(m_iBallesDansZone[i])/double(m_iBallesTotal)*100)),'f',0) + '%');
            else
                m_pZones[i]->setText(QString::number(0) + '%');
        }
    }
}

void CTable::rafraichirInactif()
{
    qDebug() << Q_FUNC_INFO;
    for(uint8_t i=0; i < NB_ZONES; i++)
    {
        if (i != m_iZoneRobot)
        {
            if (i == m_iZoneObjectif)
                m_pZones[i]->setStyleSheet(CSS_FOND_OBJECTIF);
            else
                m_pZones[i]->setStyleSheet(CSS_FOND_INACTIF);
        }
        m_pZones[i]->setFont(m_font);
    }
}

void CTable::resetSeance()
{
    m_iBallesMaximum        = 0;
    m_iBallesEnchainees     = 0;
    m_iBallesEnchaineesMax  = 0;
    m_iZoneRobot = m_iZoneObjectif = ZONE_AUCUNE;
    resetNbBallesZone();

    resetStatistiques();

    if (m_args.contains("-dev"))
        setBallesMaximum(DEV_BALLESMAX);
}
void CTable::resetStatistiques()
{
    m_iZoneToucheePrec = m_iZoneTouchee = ZONE_AUCUNE;
    m_iBallesBonnes         = 0;
    m_iBallesTotal          = 0;
    m_iBallesEnchainees     = 0;
    m_iBallesHorsTable      = 0;
    m_bBalleCoteTable            = true;
    m_bBalleCoteTablePrec        = true;
    resetNbBallesZone();
}

void CTable::balleEnJeu()
{
    m_iBallesTotal++;
//    rafraichirNbBallesZone();

    m_bBalleCoteTablePrec = m_bBalleCoteTable;
    m_bBalleCoteTable = false;

    if(getBalleCoteTablePrec() == false && getBalleCoteTable() == false)
    {
        qDebug() << Q_FUNC_INFO << " La Balle n'as pas ete renvoye";
        m_iBallesHorsTable++;
    }
}
void CTable::finirSeance()
{
    m_bBalleCoteTablePrec = m_bBalleCoteTable;
    m_bBalleCoteTable = false;

    if(getBalleCoteTablePrec() == false && getBalleCoteTable() == false)
    {
        qDebug() << Q_FUNC_INFO << " La derniere balle n'as jamais ete renvoyé";
        m_iBallesHorsTable++;
    }
}

bool CTable::impacterZone(uint8_t numeroZone)
{
    qDebug() << Q_FUNC_INFO;
    if (numeroZone >= NB_ZONES || numeroZone == m_iZoneRobot)
        return false;

    if (m_iBallesBonnes + m_iBallesHorsTable >= m_iBallesTotal)
        return false;

    m_bBalleCoteTablePrec = m_bBalleCoteTable;
    m_bBalleCoteTable = true;

    qDebug() << Q_FUNC_INFO << numeroZone;
    m_iZoneToucheePrec = m_iZoneTouchee;
    m_iZoneTouchee = numeroZone;

    m_iBallesBonnes++;
    m_iBallesDansZone[numeroZone]++;

    rafraichirNbBallesZone();

    if (numeroZone != ZONE_AUCUNE)
    {
        if (numeroZone == m_iZoneObjectif || m_iZoneObjectif == ZONE_AUCUNE)
        {
            m_pZones[numeroZone]->setStyleSheet(CSS_FOND_ACTIF);
            m_iBallesEnchainees++;
        }
        else
        {
            m_pZones[numeroZone]->setStyleSheet(CSS_FOND_RATE);
            m_iBallesEnchainees = 0;
        }

        m_pZones[numeroZone]->setFont(m_fontBig);
    }
    else
        m_iBallesEnchainees = 0;

    if (m_iBallesEnchainees > m_iBallesEnchaineesMax)
        m_iBallesEnchaineesMax = m_iBallesEnchainees;

    m_iZoneTouchee = numeroZone;
    QTimer::singleShot(DELAI_COUP, this, SLOT(rafraichirInactif()));

    return true;
}

void CTable::setZoneRobot(uint8_t numeroZone)
{
    qDebug() << Q_FUNC_INFO;
    if ((numeroZone >= NB_ZONES && numeroZone != ZONE_AUCUNE))
    {
        qDebug() << "/!\\ Erreur Zone /!\\";
        return;
    }

    if (m_iZoneRobot < NB_ZONES)
        m_pZones[m_iZoneRobot]->setStyleSheet(CSS_FOND_INACTIF);

    m_iZoneRobot = numeroZone;

    if (m_iZoneTouchee == numeroZone)
        m_iZoneTouchee = ZONE_AUCUNE;
    if (m_iZoneToucheePrec == numeroZone)
        m_iZoneToucheePrec = ZONE_AUCUNE;

    if (m_iZoneRobot < NB_ZONES)
    {
        m_pZones[numeroZone]->setStyleSheet(CSS_FOND_ROBOT);
        m_pZones[numeroZone]->setText("ROBOT");
    }

    rafraichirNbBallesZone();
}

void CTable::setZoneObjectif(uint8_t numeroZone)
{
    qDebug() << Q_FUNC_INFO;
    if ((numeroZone >= NB_ZONES && numeroZone != ZONE_AUCUNE))
    {
        qDebug() << "/!\\ Erreur Zone /!\\";
        return;
    }
    if (m_iZoneObjectif < NB_ZONES)
        m_pZones[m_iZoneObjectif]->setStyleSheet(CSS_FOND_INACTIF);

    m_iZoneObjectif = numeroZone;

    if (m_iZoneTouchee == numeroZone)
        m_iZoneTouchee = ZONE_AUCUNE;
    if (m_iZoneToucheePrec == numeroZone)
        m_iZoneToucheePrec = ZONE_AUCUNE;

    if (m_iZoneObjectif < NB_ZONES)
    {
        m_pZones[numeroZone]->setStyleSheet(CSS_FOND_OBJECTIF);
    }

    rafraichirNbBallesZone();
}

int CTable::getBallesHorsTable(){ return m_iBallesHorsTable;}

bool CTable::getBalleCoteTablePrec() {return m_bBalleCoteTablePrec;}
bool CTable::getBalleCoteTable() {return m_bBalleCoteTable;}

int CTable::getBallesBonnes() { return m_iBallesBonnes; }
int CTable::getBallesTotal() { return m_iBallesTotal; }
int CTable::getBallesMaximum() { return m_iBallesMaximum; }
int CTable::getBallesEnchainees() { return m_iBallesEnchainees; }
void CTable::setBallesMaximum(int nb) { m_iBallesMaximum = nb; }

uint8_t CTable::getZoneObjectif() { return m_iZoneObjectif; }
int CTable::getBallesObjectif() { return m_iBallesDansZone[m_iZoneObjectif]; }
