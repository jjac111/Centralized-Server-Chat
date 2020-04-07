/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package src.serverclientsync;

import java.io.IOException;
import java.util.HashMap;

/**
 *
 * @author Spellkaze
 */
public class DataHolder {
    private static HashMap<String,ConnectionRunnable> connectionList = new HashMap<>();

    private static boolean disconnected = false;

    private static final Object lock = new Object(); // Object level lock
    
    public static void AddConnection(String id, ConnectionRunnable connection)
    {
        connectionList.put(id, connection);
    }
    
    public static String ListConnections()
    {
        return connectionList.values().toString();
    }
    
    public static boolean IsUserConnected(String user)
    {
        return connectionList.containsKey(user);
    }

    public static void setDisconnected ( boolean result ){
        disconnected = result;
    }

    public static boolean getDisconnected(){ return disconnected; }

    public static ConnectionRunnable getConnection ( String user ){

        ConnectionRunnable userIs = null;
        if(IsUserConnected(user))
        {
            userIs = connectionList.get(user);
        }

        return userIs;
    }

    
    public static boolean AddMessagesToQueue(String id, String message, String user)
    {
        if(IsUserConnected(user))
        {
            connectionList.get(user).AddMessage(message, id);
            return true;
        }else{
            return false;
        }
    }

    // Podrias mandar un string para que se imprima en consola (OJO)

    // methods to make the server-client connection synchronized, and not asynchronized
    // class level lock (synchronized)
    public static boolean SendMessage( String fromUser, String message, String toUser ) { // in case msg to server, toUser = null // Does this affect when searching the key in hash map?


        if(IsUserConnected(toUser) || toUser == null){
            synchronized (lock){
                boolean messageSent = false; // Podria manejar excepciones de si el mensaje se envio o no posteriormente
                ConnectionRunnable to = connectionList.get(toUser);
                ConnectionRunnable from = connectionList.get(fromUser);
                String msg= message;


                while( messageSent == false){

                    // In case the message sent was for the server, and not another client
                    if( from.getStatus() == ConnectionRunnable.ConnectionStatus.TO_SERVER){

                        if( msg.equals("connect " + toUser) ) {
                            AddMessagesToQueue(fromUser, msg, toUser);
                            messageSent = true;
                            from.status = ConnectionRunnable.ConnectionStatus.WAITING;
                        }
                        else{
                            from.AddMessage(msg, null);
                            messageSent = true;
                        }

                    }
                    // In case msg is to another user, must consider states TO_CLIENT and WAITING
                    else{
                        if( from.getStatus() == ConnectionRunnable.ConnectionStatus.TO_CLIENT) {

                            if( msg.equals(fromUser + ": connect " + toUser) || msg.equals(fromUser + ": list") ){
                                messageSent = true;

                            }
                            else{

                                if(msg.equals("disconnect")){
                                    from.message = ListConnections();

                                    to.message = "disconnect";

                                    from.status = ConnectionRunnable.ConnectionStatus.WAITING; // Bloqueo el thread que envia msg de escribir
                                    to.status = ConnectionRunnable.ConnectionStatus.TO_CLIENT; // Desbloqueo el thread que recibe, para que pueda leer


                                }
                                // send msg to other client
                                AddMessagesToQueue(from.id, msg, to.id);
                                messageSent = true;
                                from.status = ConnectionRunnable.ConnectionStatus.WAITING; // Bloqueo el thread que envia msg de escribir
                                to.status = ConnectionRunnable.ConnectionStatus.TO_CLIENT; // Desbloqueo el thread que recibe, para que pueda leer
                                lock.notifyAll();
                            }
                        }
                        else {
                            try {
                                lock.wait(); // object lock
                            } catch (InterruptedException ie) {
                                System.err.println("wait: Interrupted exception.");
                            }
                        }

                    }

                }

            }
            return true;
        }else{
            return false;
        }


    }
    public static String ReadMessage(String fromUser){

        System.out.println("Sending Message READ step1 from " + fromUser);

        String in = "";

        synchronized (lock){

            boolean messageRead = false;
            ConnectionRunnable from = connectionList.get(fromUser);

            while ( messageRead == false){
                // In case the message to read was from the server, and not another client
                if( from.getStatus() == ConnectionRunnable.ConnectionStatus.TO_SERVER){
                    in = from.message;
                    messageRead = true;
                }
                // In case msg is to another user, must consider states TO_CLIENT and WAITING
                else{
                    if( from.getStatus() == ConnectionRunnable.ConnectionStatus.TO_CLIENT) {
                        in = from.message;
                        // in case the client reading from is disconnected
                        if( in.equals( from.userToSend + ": disconnect" ) ) {
                            in = "Disconnecting from client: " + connectionList.get(fromUser).userToSend + ". Connecting back to Server";
                            from.setStatus(ConnectionRunnable.ConnectionStatus.TO_SERVER);
                            from.countWhenDisconnected = from.count+1;
                            getConnection(from.userToSend).userToSend = null;
                            from.userToSend = null;
                            messageRead = true;
                            lock.notifyAll();

                        }
                        else if( from.userToSend == null ){
                            messageRead = true;
                        }
                        else{
                            // read msg from another client
                            messageRead = true;
                        }
                    }
                    else {
                        try {
                            lock.wait(); // object lock
                        } catch (InterruptedException ie) {
                            System.err.println("wait: Interrupted exception.");
                        }
                    }

                }
            }

        }

        return in;
    }

    
    public static void RemoveConnection(String id)
    {
        connectionList.remove(id);
    }
    
}
