#-------------------------------------------------
#
# Project created by QtCreator 2018-02-07T15:57:42
#
#-------------------------------------------------

QT       += core gui

TARGET = bin/ecran-ttpa
TEMPLATE = app

QMAKE_CXXFLAGS += -std=c++0x

SOURCES += main.cpp\
        ihm.cpp \
    table.cpp \
    trame.cpp \
    communicationbluetooth.cpp

HEADERS  += ihm.h \
    table.h \
    const.h \
    trame.h \
    communicationbluetooth.h

FORMS    += ihm.ui

RESOURCES += \
    images.qrc

include(QextSerialPort/qextserialport.pri)
