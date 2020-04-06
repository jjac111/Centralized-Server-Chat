/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package src.serversync;

/**
 *
 * @author Spellkaze
 */
public class ServerSync {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        SocketClient socketMaker = new SocketClient(8080);
        new Thread(socketMaker).start();
        
        
    }
    
}
