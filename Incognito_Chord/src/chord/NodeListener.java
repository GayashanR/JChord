/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chord;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 *
 * @author GayashanRathnavibush
 */
public class NodeListener implements Runnable {
    private Node chordNode;

    public NodeListener(Node chordNode) {
        this.chordNode = chordNode;
    }

    public void run() {
        try {
            // Listen for connections on port
            ServerSocket serverSocket = new ServerSocket(this.chordNode.getPort());

            // Continuously loop for connections
            while (true) {
                // When connection is established launch a new thread for communicating with client
                Socket clientSocket = serverSocket.accept();
                new Thread(new ChordThread(this.chordNode, clientSocket)).start();
            }
        } catch (IOException e) {
            System.err.println("error when listening for connections");
            e.printStackTrace();
        }
    }
}
