/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chord;

import static chord.Sender.data;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Semaphore;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author GayashanRathnavibush
 */
public class Node {
    private String address;
    private int port;
    private String existingNodeAddress = null;
    private int existingNodePort;
    private Finger secondPredecessor;
    private Finger firstPredecessor;
    private Finger firstSuccessor;
    private Finger secondSuccessor;
    private Map<Integer, Finger> fingers = new HashMap<>();
    private Map<String, List<Finger>> keys = new HashMap<>();
    private long id;
    private String hex;
    private Semaphore semaphore = new Semaphore(1);

    /**
     * Constructor for creating a new Chord node that is the first in the ring.
     *
     * @param address   The address of this node
     * @param port      The port that this Chord node needs to listen on
     */
    public Node(String address, String port) {
        // Set node fields
        this.address = address;
        this.port = Integer.valueOf(port);

        // Hash address
        SHA1Hasher sha1Hasher = new SHA1Hasher(this.address + ":" + this.port);
        this.id = sha1Hasher.getLong();
        this.hex = sha1Hasher.getHex();

        // Logging
        System.out.println("Creating a new Chord ring");
        System.out.println("You are listening on port " + this.port);
        System.out.println("Your position is " + this.hex + " (" + this.id + ")");

        // Initialize finger table and successors
        this.initializeFingers();
        this.initializeSuccessors();

        // Start listening for connections and heartbeats from neighbors
        new Thread(new NodeListener(this)).start();
        new Thread(new NodeStabilizer(this)).start();
        new Thread(new Heart(this)).start();
    }

    /**
     * Constructor for creating a new Chord node that will join an existing ring.
     *
     * @param address               The address of this node
     * @param port                  The port that this Chord node needs to listen on
     * @param existingNodeIps       The IP addresses of the existing ring members
     * @param existingNodePorts      The ports of the existing ring members
     */
    public Node(String address, String port, String[] existingNodeIps, int[] existingNodePorts) {
        // Set node fields
        this.address = address;
        this.port = Integer.valueOf(port);
        
        // Hash address
        SHA1Hasher sha1Hasher = new SHA1Hasher(this.address + ":" + this.port);
        this.id = sha1Hasher.getLong();
        this.hex = sha1Hasher.getHex();

        // Logging
        System.out.println("Joining the Chord ring");
        System.out.println("You are listening on port " + this.port);
        System.out.println("Your position is " + this.hex + " (" + this.id + ")");

        int closestNodeIndex = getClosestNodeArrayIndex(this.id, existingNodeIps, existingNodePorts);
        
        // Set contact node fields
        this.existingNodeAddress = existingNodeIps[closestNodeIndex];
        this.existingNodePort = existingNodePorts[closestNodeIndex];
        System.out.println("Connected to existing node " + this.existingNodeAddress + ":" + this.existingNodePort);

        // Initialize finger table and successors
        this.initializeFingers();
        this.initializeSuccessors();

        // Start listening for connections and heartbeats from neighbors
        new Thread(new NodeListener(this)).start();
        new Thread(new NodeStabilizer(this)).start();
        new Thread(new Heart(this)).start();
    }

    /**
     * Initializes finger table. If an existing node has been defined it will use that node to perform lookups. Otherwise, this node is the only node in the ring and all fingers will refer to self.
     */
    private void initializeFingers() {
        // If this ring is the only node in the ring
        if (this.existingNodeAddress == null) {
            // Initialize all fingers to refer to self
            for (int i = 0; i < 32; i++) {
                this.fingers.put(i, new Finger(this.address, this.port));
            }
        } else {
            // Open connection to contact node
            DatagramSocket socket;
            try {
                socket = new DatagramSocket();

                // Open reader/writer to chord node
                BigInteger bigQuery = BigInteger.valueOf(2L);
                BigInteger bigSelfId = BigInteger.valueOf(this.id);

                for (int i = 0; i < 32; i++) {
                    BigInteger bigResult = bigQuery.pow(i);
                    bigResult = bigResult.add(bigSelfId);

                    // Send query to chord
                    String message = Chord.FIND_NODE + " " + bigResult.longValue();
                    message = Message.customFormat("0000", message.length()) + " " + message;
                    byte[] toSend  = message.getBytes(); 
                    InetAddress IPAddress; 
                    try {
                        IPAddress = InetAddress.getByName(this.existingNodeAddress);
                        DatagramPacket packet =new DatagramPacket(toSend, toSend.length, IPAddress, this.existingNodePort); 
                        System.out.println("sending message:"+message+"\nfrom-"+Config.MY_IP+":"+Config.MY_PORT+",to-"+this.existingNodeAddress+":"+this.existingNodePort);
                        try {
                            socket.send(packet);
                        } catch (IOException ex) {
                            Logger.getLogger(Node.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    } catch (UnknownHostException ex) {
                        Logger.getLogger(Node.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    System.out.println("Sent: " + message);

                    byte[] receive = new byte[65535]; 
                    DatagramPacket DpReceive = new DatagramPacket(receive, receive.length); 
                    try {
                        socket.receive(DpReceive);
                    } catch (IOException ex) {
                        Logger.getLogger(Node.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    
                    // Read response from chord
                    String serverResponse = data(receive).toString();

                    // Parse out address and port
                    String[] serverResponseFragments = serverResponse.split(" ", 3);
                    String[] addressFragments = serverResponseFragments[2].split(" ");

                    // Add response finger to table
                    this.fingers.put(i, new Finger(addressFragments[0], Integer.valueOf(addressFragments[1])));

                    System.out.println("Received: " + serverResponse);
                }
                socket.close();
            } catch (IOException e) {
                this.logError("Could not open connection to existing node");
                e.printStackTrace();
            }
        }
    }

    /**
     * Initializes successors. Uses the finger table to get the successors and defaults the predecessors to self until it learns about new ones.
     */
    private void initializeSuccessors() {
        this.firstSuccessor = this.fingers.get(0);
        this.secondSuccessor = this.fingers.get(1);
        this.firstPredecessor = new Finger(this.address, this.port);
        this.secondPredecessor = new Finger(this.address, this.port);

        // Notify the first successor that we are the new predecessor, provided we do not open a connection to ourselves
        if (!this.address.equals(this.firstSuccessor.getAddress()) || (this.port != this.firstSuccessor.getPort())) {
            DatagramSocket socket;
            try {
                socket = new DatagramSocket();

                // Tell successor that this node is its new predecessor
                String message = Chord.JOIN + " " + this.getAddress() + " " + this.getPort();
                message = Message.customFormat("0000", message.length()) + " " + message;
                byte[] toSend  = message.getBytes(); 
                InetAddress IPAddress; 
                try {
                    IPAddress = InetAddress.getByName(this.firstSuccessor.getAddress());
                    DatagramPacket packet =new DatagramPacket(toSend, toSend.length, IPAddress, this.firstSuccessor.getPort()); 
                    System.out.println("sending message:"+message+"\nfrom-"+Config.MY_IP+":"+Config.MY_PORT+",to-"+this.firstSuccessor.getAddress()+":"+this.firstSuccessor.getPort());
                    try {
                        socket.send(packet);
                    } catch (IOException ex) {
                        Logger.getLogger(Node.class.getName()).log(Level.SEVERE, null, ex);
                    }
                } catch (UnknownHostException ex) {
                    Logger.getLogger(Node.class.getName()).log(Level.SEVERE, null, ex);
                }
                System.out.println("Sent: " + message);

                // Close connections
                socket.close();
            } catch (IOException e) {
                this.logError("Could not open connection to first successor");
                e.printStackTrace();
            }
        }
    }
    
    private int getClosestNodeArrayIndex(long currentNodeId, String[] existingNodeIps, int[] existingNodePorts){
        long minimumDistance = getDistanceToNode(currentNodeId, existingNodeIps[0], existingNodePorts[0]);
        int minimumDistanceNodeIndex=0;
        
        for(int i=1; i<existingNodeIps.length; i++){
            long distanceToNeighbour = getDistanceToNode(currentNodeId, existingNodeIps[i], existingNodePorts[i]);
            if(minimumDistance>distanceToNeighbour){
                minimumDistanceNodeIndex = i;
            }
        }
        
        return minimumDistanceNodeIndex;
    }
    
    private long getDistanceToNode(long currentNodeId, String toNodeIp, int toNodePort){
        SHA1Hasher sha1Hasher = new SHA1Hasher(toNodeIp + ":" + toNodePort);
        long toNodeId = sha1Hasher.getLong();
        
        if(toNodeId>=currentNodeId){
            return toNodeId-currentNodeId;
        }
        else{
            return getUpperBoundForNodeIds()+toNodeId-currentNodeId;
        }
    }
    
    private long getUpperBoundForNodeIds(){
        BigInteger bigResult = BigInteger.valueOf(2L).pow(32);
        return bigResult.longValue();
    }

    /**
     * Logs error messages to the console
     *
     * @param errorMessage  The message to print to the console
     */
    private void logError(String errorMessage) {
        System.err.println("Error (" + this.id + "): " + errorMessage);
    }

    public void acquire() {
        try {
            this.semaphore.acquire();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void release() {
        this.semaphore.release();
    }

    public Map<Integer, Finger> getFingers() {
        return this.fingers;
    }
    
    public Map<String, List<Finger>> getKeys() {
        return this.keys;
    }
    
    public void setKeys(Map<String, List<Finger>> keys) {
        this.keys = keys;
    }

    public void addKey(String id, Finger node) {
        keys.get(id).add(node);
        this.keys.put(id, keys.get(id));
    }
    
    public void addKeys(String id, List<Finger> nodeList) {
        this.keys.put(id, nodeList);
    }
    
    public void removeKey(String id) {
        keys.remove(id);
    }
    
    public List<Finger> getKey(String id) {
        return this.keys.get(id);
    }
    
    public int getPort() {
        return this.port;
    }

    public String getAddress() {
        return this.address;
    }

    public Finger getFirstSuccessor() {
        return this.firstSuccessor;
    }

    public void setFirstSuccessor(Finger firstSuccessor) {
        this.firstSuccessor = firstSuccessor;
    }

    public Finger getFirstPredecessor() {
        return this.firstPredecessor;
    }

    public void setFirstPredecessor(Finger firstPredecessor) {
        this.firstPredecessor = firstPredecessor;
    }

    public Finger getSecondSuccessor() {
        return secondSuccessor;
    }

    public void setSecondSuccessor(Finger secondSuccessor) {
        this.secondSuccessor = secondSuccessor;
    }

    public Finger getSecondPredecessor() {
        return secondPredecessor;
    }

    public void setSecondPredecessor(Finger secondPredecessor) {
        this.secondPredecessor = secondPredecessor;
    }

    public long getId() {
        return this.id;
    }

    public Semaphore getSemaphore() {
        return semaphore;
    }
    
    
}
