/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chord;

import java.util.ArrayList;

/**
 *
 * @author GayashanRathnavibush
 */
public class Config {
    static  String MY_IP = "127.0.0.1";

    static  int MY_PORT = 500;

    static  String MY_NAME = "S";
    static  boolean isSuper = false;
    static boolean isWebService=false;
    

    static  String BOOTSTRAP_IP = "127.0.0.1";

    static int noOfNodes = 1;
    static int myNodeNumber = 0;
    
    static  int BOOTSTRAP_PORT =  9876;
    static ArrayList<String> availableFiles = new ArrayList<>(); 
   
    static int TTL = 10;
    static int noOfPeersPreset = 8;
    
    public void addNewFile(String fileName){
        availableFiles.add(fileName); 
    }
}
