#include "trame.h"

CTrame::CTrame(QObject* parent) : QObject(parent)
{
}

CTrame::~CTrame()
{
}

bool CTrame::traiterTrame(QString donneesRecues)
{
    if(donneesRecues.isEmpty())
        return false;

    bool retour = false;

    QStringList listeElements;
    listeElements = donneesRecues.split("$",QString::SkipEmptyParts);

    for(uint8_t i = 0; i < listeElements.length(); i++)
    {
        if (listeElements.at(i).startsWith("TTPA:"))
            retour = gererTrame("$" + listeElements.at(i));
    }
    return retour;
}

bool CTrame::gererTrame(QString donneesRecues)
{
    qDebug() << Q_FUNC_INFO << QThread::currentThreadId() << this << QString::fromUtf8("Données reçues : ") << donneesRecues;

    bool trameValide = true;

    if (getTrameLength(donneesRecues) == 2)                 // sans arguments
        trameValide = gererTramesSansParametre(donneesRecues);

    else if (getTrameLength(donneesRecues) == 3)            // 1 arguments
        trameValide = gererTrames1Parametre(donneesRecues);

    //$TTPA:SETSEANCE:[POS_ROBOT]:[POS_OBJECTIF]:[NB_BALLES_MAX]:[FREQ_ENVOI]
    else if (getTrameLength(donneesRecues) == 5 && extraireElement(donneesRecues,1).startsWith("SETSEANCE"))
    {
        if ((extraireElement(donneesRecues,2)).toInt() <= 0)    // AUCUN dans le cas d'un negatif ou zero
            emit setZoneRobot(ZONE_AUCUNE);
        else
            emit setZoneRobot((extraireElement(donneesRecues,2)).toInt() - 1);

        if ((extraireElement(donneesRecues,3)).toInt() <= 0)    // AUCUN dans le cas d'un negatif ou zero
            emit setZoneObjectif(ZONE_AUCUNE);
        else
            emit setZoneObjectif((extraireElement(donneesRecues,3)).toInt() - 1);

        emit setBallesMaximum((extraireElement(donneesRecues,4)).toInt());
    }
    else
    {
        qDebug() << Q_FUNC_INFO << "/!\\ TRAME NON RECONNUE /!\\";
        trameValide = false;
    }
    return trameValide;
}
bool CTrame::gererTramesSansParametre(QString donneesRecues)
{
    bool trameValide = true;
    //$TTPA:JOUEE
    if (extraireElement(donneesRecues,1).startsWith("JOUEE"))
    {
        emit balleEnJeu();
    }
    //$TTPA:START
    else if (extraireElement(donneesRecues,1).startsWith("START"))
    {
        emit commencerSeance();
    }
    //$TTPA:FINSEANCE
    else if (extraireElement(donneesRecues,1).startsWith("FINSEANCE"))
    {
        emit finirSeance();
    }
    //$TTPA:PAUSE
    else if (extraireElement(donneesRecues,1).startsWith("PAUSE"))
    {
        emit pauserSeance();
    }
    //$TTPA:RESUME
    else if (extraireElement(donneesRecues,1).startsWith("RESUME"))
    {
        emit reprendreSeance();
    }
    //$TTPA:RESET
    else if (extraireElement(donneesRecues,1).startsWith("RESET"))
    {
        emit resetSeance();
    }
    //$TTPA:QUIT
    else if (extraireElement(donneesRecues,1).startsWith("QUIT"))
    {
        emit quitter();
    }
    else
    {
        messageNonReconnu(donneesRecues,1);
        trameValide = false;
    }
    return trameValide;
}
bool CTrame::gererTrames1Parametre(QString donneesRecues)
{
    bool trameValide = true;

    // $TTPA:CONNECT:[nom]
    if (extraireElement(donneesRecues,1).startsWith("CONNECT"))
    {
        emit setInfoConnect(extraireElement(donneesRecues,2));
    }
    //$TTPA:SETROBOT:[POS_ROBOT]
    else if (extraireElement(donneesRecues,1).startsWith("SETROBOT"))
    {
        if ((extraireElement(donneesRecues,2)).toInt() <= 0)    // Adaptation au systeme de la table
            emit setZoneRobot(ZONE_AUCUNE);
        else
            emit setZoneRobot((extraireElement(donneesRecues,2)).toInt() - 1);
    }
    //$TTPA:SETOBJECTIF:[POS_OBJECTIF]
    else if (extraireElement(donneesRecues,1).startsWith("SETOBJECTIF"))
    {
        if ((extraireElement(donneesRecues,2)).toInt() <= 0)    // Adaptation au systeme de la table
            emit setZoneObjectif(ZONE_AUCUNE);
        else
            emit setZoneObjectif((extraireElement(donneesRecues,2)).toInt() - 1);
    }
    //$TTPA:SETBALLESMAX:[BALLES]
    else if (extraireElement(donneesRecues,1).startsWith("SETBALLESMAX"))
    {
        emit setBallesMaximum((extraireElement(donneesRecues,2)).toInt());
    }
    //$TTPA:IMPACT:[X]
    else if (extraireElement(donneesRecues,1).startsWith("IMPACT"))
    {
        if ((extraireElement(donneesRecues,2)).toInt() <= 0)        // Adaptation au systeme de la table
            emit balleEnJeu();
        else
            emit impacterZone((extraireElement(donneesRecues,2)).toInt() - 1);
    }
    else
    {
        messageNonReconnu(donneesRecues,1);
        trameValide = false;
    }
    return trameValide;
}

void CTrame::messageNonReconnu(QString donneesRecues, int element)
{
    qDebug() << Q_FUNC_INFO << "/!\\ TRAME NON RECONNUE /!\\";

    QString testChars = "  [Element " + QString::number(element) +"]:  [|";

    for(int i=0; i<extraireElement(donneesRecues,element).length();i++)
    {
        testChars += (extraireElement(donneesRecues,element)[i]) + "|";
    }
    testChars+="]";
    qDebug() << testChars;
}

int CTrame::getTrameLength(QString donneesRecues)
{
    if(donneesRecues.isEmpty())
        return 0;

    QStringList listeElements;
    listeElements = donneesRecues.split(":");

    return listeElements.length();
}

QString CTrame::extraireElement(QString donneesRecues, const int iElement)
{
    if(donneesRecues.isEmpty())
        return QString();

    QStringList listeElements;
    listeElements = donneesRecues.split(":");

    return listeElements.at(iElement).trimmed();
}
