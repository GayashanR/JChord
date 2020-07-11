/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chord;

import java.text.DecimalFormat;
import java.util.ArrayList;

/**
 *
 * @author GayashanRathnavibush
 */
public class Message {
    private String message;
    
    public Message(MessageType type, String ip, int port, String name){

        switch(type){
            case REG:message = appendLength(Chord.REG+" "+ip+" "+port+" "+name);
                break;
            case UNREG:message=appendLength(Chord.UNREG+" "+ip+" "+port+" "+name);
                break;
            case JOIN:message=appendLength(Chord.JOIN+" "+ip+" "+port+" "+name);
                break;
            case JOINOK: message=appendLength(Chord.JOINOK+" "+ip+" "+port+" "+0);
                break;
            case STORE: message=appendLength(Chord.STORE+" "+ip+" "+port+" "+ name);
                break;
            case FILES:
                message = appendLength(Chord.FILES+" "+ip+" "+port+" "+name);
                break;
            case SER:
            {
                String fileName = name;
                message=appendLength(Chord.SER+" "+ip+" "+port+" "+fileName);
                break;
            }
            case INQUIRE: message= appendLength(Chord.INQUIRE+" "+ip+" "+port);
                break; 
            case INQUIREOK: message= appendLength(Chord.INQUIREOK+" "+ip+" "+port);
                break;
            case LEAVE:
                String peerIpPort = name;
                if(peerIpPort!=null){
                    message=appendLength(Chord.LEAVE+" "+ip+" "+port+" "+name);
                }else{
                    message=appendLength(Chord.LEAVE+" "+ip+" "+port+" "+"CHILD-LEAVING");
                }
                break;
        }
    }
    
    public Message(MessageType type, int success){
        switch(type){
            case JOINOK: message=appendLength(Chord.JOINOK+" "+success);
                break;
//            case SEROK: message=appendLength("SEROK"+" "+success);
//                break;
        }
    }
    
    static public String customFormat(String pattern, double value ) {
      DecimalFormat myFormatter = new DecimalFormat(pattern);
      String output = myFormatter.format(value);
      return output;
      //System.out.println(value + "  " + pattern + "  " + output);
   }
    
    //public Message(MessageType type,String searchKey){
    public Message(MessageType type,String searchKey,String intermediateIp,int intermediatePort){
        switch(type){
            //case SEROK: message=appendLength("SEROK"+" "+"0"+" "+searchKey);
            case SEROK: message=appendLength(Chord.SEROK+" "+"0"+" "+searchKey+" "+intermediateIp+" "+intermediatePort);
                break;        
        }
    }
    
    public Message(MessageType type, String ip, int port){
        switch (type){
            case LEAVE:
                message = appendLength(Chord.LEAVE+" "+ip+" "+port);
                break;
            case LEAVEOK:
                message = appendLength(Chord.LEAVEOK+" "+ip+" "+port);
        }
    }
    
    public Message(MessageType type, String ip, int port, String fileNanme, int hops){
        switch(type){
            case SER:
                message=appendLength(Chord.SER+" "+ip+" "+port+" "+fileNanme+" "+hops);
                break;
        }
    }
    
  //  public Message(MessageType type, int noOfFiles, String fileDestinationIp, int fileDestinationPort, int hops, ArrayList<String> files, String fileKey){
        public Message(MessageType type, int noOfFiles, String fileDestinationIp, int fileDestinationPort, int hops, ArrayList<String> files, String fileKey,String intermediateIp,int intermediatePort){

        switch(type){
        
            case SEROK: 
            {
                String filesString=fileKey;
                for (String file : files) {
                    filesString = filesString +" "+ file;
                }
                //message=appendLength("SEROK"+" "+noOfFiles+" "+fileDestinationIp+" "+fileDestinationPort+" "+hops+" "+filesString);
                message=appendLength(Chord.SEROK+" "+noOfFiles+" "+fileDestinationIp+" "+fileDestinationPort+" "+hops+" "+filesString+" "+intermediateIp+" "+intermediatePort);
                break;
        
            }
        }
    }
    
        public Message(MessageType type, int noOfFiles, String fileDestinationIp, int fileDestinationPort, int hops, String fileString,String intermediateIp,int intermediatePort){
        switch(type){
        
            case SEROK: 
            {
                message=appendLength(Chord.SEROK+" "+noOfFiles+" "+fileDestinationIp+" "+fileDestinationPort+" "+hops+" "+fileString+" "+intermediateIp+" "+intermediatePort);
                break;
        
            }
        }
    }
    public String getMessage(){
        return message;
    }
    
    private String appendLength(String message){
         int messageLength = message.length()+4+1;
        String messageLengthString = Integer.toString(messageLength);
        String prefix="";
        switch(messageLengthString.length()){
            case 1: prefix="000"+messageLengthString+" ";
                break;
            case 2:prefix = "00"+messageLengthString+" ";
                break;
            case 3:prefix="0"+messageLengthString+" ";
                break;
            case 4: prefix=messageLengthString+" ";
                break;
        }
        message=prefix+message;
        
        return message;
    }    
    
    
}

