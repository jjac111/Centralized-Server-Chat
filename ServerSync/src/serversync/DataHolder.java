/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package serversync;

import java.util.HashMap;

/**
 *
 * @author Spellkaze
 */
public class DataHolder {
    //Guarda la lista de conexiones
    private static HashMap<String,ConnectionRunnable> connectionList = new HashMap<>();
    
    //Anade una conexion a la lista
    public static void AddConnection(String id, ConnectionRunnable connection)
    {
        connectionList.put(id, connection);
    }
    
    //Devuelve un string con la lista de las conexiones en la lista
    public static String ListConnections()
    {
        return connectionList.values().toString();
    }
    
    //Booleano que verifica si una conexion esta activa por su id
    public static boolean IsUserConnected(String user)
    {
        return connectionList.containsKey(user);
    }
    
    //Comunicacion entre hilos de conexion, es llamado de un hilo para anadir mensajes a otro hilo
    public static boolean AddMessagesToQueue(String user, String message, String id)
    {
        if(IsUserConnected(user))
        {
            connectionList.get(user).AddMessage(message, id);
            return true;
        }else{
            return false;
        }
    }
    
    //Remueve una conexion de la lista de conexiones por su id
    public static void RemoveConnection(String id)
    {
        connectionList.remove(id);
    }
    
    //Comunicacion entre hilos de conexion, es llamado entre clientes por un hilo para notificarle al otro hilo que a sido desconectado
    public static void DisconnectUsers(String otherId)
    {
        connectionList.get(otherId).DisconnectFromUser();
    }
    
}
