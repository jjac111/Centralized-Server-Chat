/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package serversync;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Spellkaze
 */
public class ConnectionRunnable implements Runnable {

    private enum ConnectionStatus {
        TO_SERVER, TO_CLIENT
    };
    ConnectionStatus status = ConnectionStatus.TO_SERVER;
    private List<String> messageQueue = new ArrayList<>();
    private String id = null;
    private int port = 0;
    private InetAddress ip = null;
    private String userToSend = null;
    boolean isConnected = true;
    
    protected Socket clientSocket = null;
    final DataInputStream input;
    final DataOutputStream output;

    public ConnectionRunnable(Socket clientSocket, InputStream input, OutputStream output) {
        this.clientSocket = clientSocket;
        this.input = new DataInputStream(input);
        this.output = new DataOutputStream(output);
        ip = clientSocket.getInetAddress();
        port = clientSocket.getPort();
        System.out.println("Opening Thread Connection with: " + ip + ":" + port);
    }

    @Override
    public void run() {
        String recieved;
        String toReturn;
        try {
            output.writeUTF("CONNECTION STABLISHED");
            id = input.readUTF();
            System.out.println("Assigning id:  " + id);
            DataHolder.AddConnection(id, this);
            System.out.println(DataHolder.ListConnections());
            output.writeUTF(DataHolder.ListConnections());
        } catch (IOException ex) {
            Logger.getLogger(ConnectionRunnable.class.getName()).log(Level.SEVERE, null, ex);
        }

        while (isConnected) {
            toReturn = "EMPTY_MESSAGE";

            try {
                recieved = input.readUTF();
                if (status == ConnectionStatus.TO_SERVER) {
                    System.out.println(recieved);
                    String[] command = recieved.split(" ");
                    switch (command[0]) {
                        case "list":
                            toReturn = DataHolder.ListConnections();
                            break;
                        case "connect":
                            System.out.println("Attempting to connect client with: " + command[1]);
                            if (DataHolder.IsUserConnected(command[1])) {
                                toReturn = "Connected to user " + command[1];
                                userToSend = command[1];
                                status = ConnectionStatus.TO_CLIENT;
                            } else {
                                toReturn = "No user called " + command[1] + " is currently connected";
                            }
                            break;
                        case "disconnect":
                            isConnected = false;
                            toReturn = "Order of Disconnect!";
                            DataHolder.RemoveConnection(id);
                            break;
                    }
                } else {
                    if(recieved.equals("disconnect"))
                    {
                        toReturn = "Disconnecting from client: " + userToSend + ". Connecting back to Server";
                        status = ConnectionStatus.TO_SERVER;
                        userToSend = null;
                    }
                    else if (DataHolder.AddMessagesToQueue(userToSend, id + ": " +recieved)) {
                        toReturn = "Message sent correctly";
                    } else {
                        toReturn = "Message could not be sent because user was not connected";
                    }
                }
                System.out.println(toReturn);
                if(messageQueue.isEmpty())
                {
                    output.writeUTF(toReturn);
                }else{
                    output.writeUTF(messageQueue.toString() +" - " + toReturn);
                }
            } catch (IOException ex) {
                Logger.getLogger(ConnectionRunnable.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public String getId() {
        return id;
    }

    public int getPort() {
        return port;
    }

    public InetAddress getIp() {
        return ip;
    }

    @Override
    public String toString() {
        return "Client{" + "id=" + id + ", port=" + port + ", ip=" + ip + '}';
    }

    public void AddMessageToQueue(String message) {
        messageQueue.add(message);
    }

}
