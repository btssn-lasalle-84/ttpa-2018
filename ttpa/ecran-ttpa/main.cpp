#include <QtGui/QApplication>
#include "ihm.h"

int main(int argc, char *argv[])
{
    QApplication a(argc, argv);

    CIhm ihm;
    ihm.show();
    
    return a.exec();
}
