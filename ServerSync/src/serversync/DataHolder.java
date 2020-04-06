/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package src.serversync;

import java.util.HashMap;

/**
 *
 * @author Spellkaze
 */
public class DataHolder {
    private static HashMap<String,ConnectionRunnable> connectionList = new HashMap<>();
    
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
    
    public static void RemoveConnection(String id)
    {
        connectionList.remove(id);
    }
    
}
