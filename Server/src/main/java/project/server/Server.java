/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package project.server;

import java.io.IOException;
import java.net.ServerSocket;

/**
 *
 * @author Juan Javier
 */
public class Server {
    public static Proxy proxy = new Proxy();
    
    public static void main(String[] args) throws IOException{
        
        Thread proxyThread = new Thread(proxy);
        
        proxyThread.start();
        
        
        while(true){
            ServerSocket server = new ServerSocket();
        }
        
    }
}
