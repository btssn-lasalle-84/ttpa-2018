package com.ttpa.iris.ttpamobile;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.os.Message;
import android.os.Handler;
import android.os.ParcelUuid;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

/**
 * Created by smaniotto on 04/04/18.
 */

public class PeripheriqueBluetooth extends Thread
{
    private String nom;
    private String adresse;
    private Handler handler = null;
    private BluetoothDevice device = null;
    private BluetoothSocket socket = null;
    private InputStream receiveStream = null;
    private OutputStream sendStream = null;
    private TReception tReception;
    public final static int CODE_CONNEXION = 0;
    public final static int CODE_RECEPTION = 1;
    public final static int CODE_DECONNEXION = 2;

    public PeripheriqueBluetooth(BluetoothDevice device, Handler handler)
    {
        if(device != null)
        {
            this.device = device;
            this.nom = device.getName();
            this.adresse = device.getAddress();
            this.handler = handler;
        }
        else
        {
            this.device = device;
            this.nom = "Aucun";
            this.adresse = "";
            this.handler = handler;
        }

        try
        {
            System.out.println("<Bluetooth> nom " + device.getName());
            ParcelUuid[] uuids = device.getUuids();
            if(uuids != null)
                for(int i = 0; i < uuids.length; i++)
                    System.out.println("<Bluetooth> uuid " + uuids[i].getUuid().toString());
            else
                System.out.println("<Bluetooth> uuid null !");

            socket = device.createRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"));
            //socket = device.createRfcommSocketToServiceRecord(uuids[0].getUuid());
            System.out.println("<Bluetooth> new socket");

            // API 23
            //System.out.println("<Bluetooth> max receive packet size : " + socket.getMaxReceivePacketSize());
            //System.out.println("<Bluetooth> max transmit packet size : " + socket.getMaxTransmitPacketSize());
            //System.out.println("<Bluetooth> connection type : " + socket.getConnectionType());

            receiveStream = socket.getInputStream();
            sendStream = socket.getOutputStream();
        }
        catch (IOException e)
        {
            e.printStackTrace();
            System.out.println("<Bluetooth> Erreur socket !");
            socket = null;
        }

        if(socket != null)
            tReception = new TReception(handler);
    }

    public BluetoothDevice getDevice()
    {
        return device;
    }

    public String getNom()
    {
        return nom;
    }

    public String getAdresse()
    {
        return adresse;
    }

    public boolean estConnecte()
    {
        if(socket != null)
            return socket.isConnected();
        return false;
    }

    public void setNom(String nom)
    {
        this.nom = nom;
    }

    public String toString()
    {
        return "\nNom : " + nom + "\nAdresse : " + adresse;
    }

    public void envoyer(String data)
    {
        if(socket == null)
            return;

        try
        {
            //if(estConnecte())
            if(socket.isConnected())
            {
                System.out.println("<Bluetooth> Envoyer " + data);
                sendStream.write(data.getBytes());
                sendStream.flush();
            }
            else
            {
                System.out.println("<Bluetooth> Envoyer (non connecté) " + data);
            }
        }
        catch (IOException e)
        {
            System.out.println("<Bluetooth> Erreur socket write !");
            e.printStackTrace();
        }
    }

    public void connecter()
    {
        new Thread()
        {
            @Override public void run()
            {
                try
                {
                    System.out.println("<Bluetooth> socket connect ...");
                    socket.connect();
                    System.out.println("<Bluetooth> socket connect ok");
                    Message msg = Message.obtain();
                    //msg.arg1 = CODE_CONNEXION;
                    Bundle b = new Bundle();
                    b.putString("nom", getNom());
                    b.putString("adresse", getAdresse());
                    b.putInt("etat", CODE_CONNEXION);
                    b.putString("donnees", "");
                    msg.setData(b);
                    handler.sendMessage(msg);

                    tReception.start();
                }
                catch (IOException e)
                {
                    System.out.println("<Bluetooth> Erreur connect !");
                    e.printStackTrace();
                }
            }
        }.start();
    }

    public boolean deconnecter(boolean fermeture)
    {
        try
        {
            //if(estConnecte())
            {
                tReception.arreter();

                if(fermeture)
                {
                    socket.close();
                    System.out.println("<Bluetooth> socket close");
                }
                return true;
            }
        }
        catch (IOException e)
        {
            System.out.println("<Bluetooth> Erreur close !");
            e.printStackTrace();
            return false;
        }
    }

    private class TReception extends Thread
    {
        Handler handlerUI;
        private boolean fini;

        TReception(Handler h)
        {
            handlerUI = h;
            fini = false;
        }

        @Override public void run()
        {
            System.out.println("<Bluetooth> Attente reception");
            while(!fini)
            {
                try
                {
                    if(receiveStream.available() > 0)
                    {
                        byte buffer[] = new byte[100];
                        int k = receiveStream.read(buffer, 0, 100);

                        if(k > 0)
                        {
                            byte rawdata[] = new byte[k];
                            for(int i=0;i<k;i++)
                                rawdata[i] = buffer[i];

                            String data = new String(rawdata);
                            System.out.println("<Bluetooth> Reception " + data);

                            Message msg = Message.obtain();
                            /*msg.what = Peripherique.CODE_RECEPTION;
                            msg.obj = data;
                            handlerUI.sendMessage(msg);*/

                            //Message msg = handlerUI.obtainMessage();
                            Bundle b = new Bundle();
                            b.putString("nom", getNom());
                            b.putString("adresse", getAdresse());
                            b.putInt("etat", CODE_RECEPTION);
                            b.putString("donnees", data);
                            msg.setData(b);
                            handlerUI.sendMessage(msg);
                        }
                    }
                    try
                    {
                        Thread.sleep(250);
                    }
                    catch (InterruptedException e)
                    {
                        e.printStackTrace();
                    }
                }
                catch (IOException e)
                {
                    //System.out.println("<Socket> error read");
                    e.printStackTrace();
                }
            }
            Message msg = Message.obtain();
            //msg.arg1 = CODE_DECONNEXION;
            Bundle b = new Bundle();
            b.putString("nom", getNom());
            b.putString("adresse", getAdresse());
            b.putInt("etat", CODE_DECONNEXION);
            b.putString("donnees", "");
            msg.setData(b);
            handler.sendMessage(msg);
            System.out.println("<Bluetooth> Fin reception");
        }

        public void arreter()
        {
            if(fini == false)
            {
                fini = true;
            }
            try
            {
                Thread.sleep(500);
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
            }
        }
    }
}
