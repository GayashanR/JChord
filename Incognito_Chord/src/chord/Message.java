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
public class Message {
    private String message;
    
    public Message(MessageType type, String ip, int port, String name){

        switch(type){
            case REG:message = appendLength("REG"+" "+ip+" "+port+" "+name);
                break;
            case UNREG:message=appendLength("UNREG"+" "+ip+" "+port+" "+name);
                break;
            case JOIN:message=appendLength("JOIN"+" "+ip+" "+port+" "+name);
                break;
            case JOINOK: message=appendLength("JOINOK"+" "+ip+" "+port+" "+0);
                break;
            case FILES:
                message = appendLength("FILES"+" "+ip+" "+port+" "+name);
                break;
            case SER:
            {
                String fileName = name;
                message=appendLength("SER"+" "+ip+" "+port+" "+fileName);
                break;
            }
            case INQUIRE: message= appendLength("INQUIRE"+" "+ip+" "+port);
                break; 
            case INQUIREOK: message= appendLength("INQUIREOK"+" "+ip+" "+port);
                break;
            case LEAVE:
                String peerIpPort = name;
                if(peerIpPort!=null){
                    message=appendLength("LEAVE"+" "+ip+" "+port+" "+name);
                }else{
                    message=appendLength("LEAVE"+" "+ip+" "+port+" "+"CHILD-LEAVING");
                }
                break;
        }
    }
    
    public Message(MessageType type, int success){
        switch(type){
            case JOINOK: message=appendLength("JOINOK"+" "+success);
                break;
//            case SEROK: message=appendLength("SEROK"+" "+success);
//                break;
        }
    }
    
    //public Message(MessageType type,String searchKey){
    public Message(MessageType type,String searchKey,String intermediateIp,int intermediatePort){
        switch(type){
            //case SEROK: message=appendLength("SEROK"+" "+"0"+" "+searchKey);
            case SEROK: message=appendLength("SEROK"+" "+"0"+" "+searchKey+" "+intermediateIp+" "+intermediatePort);
                break;        
        }
    }
    
    public Message(MessageType type, String ip, int port){
        switch (type){
            case LEAVE:
                message = appendLength("LEAVE"+" "+ip+" "+port);
                break;
            case LEAVEOK:
                message = appendLength("LEAVEOK"+" "+ip+" "+port);
        }
    }
    
    public Message(MessageType type, String ip, int port, String fileNanme, int hops){
        switch(type){
            case SER:
                message=appendLength("SER"+" "+ip+" "+port+" "+fileNanme+" "+hops);
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
                message=appendLength("SEROK"+" "+noOfFiles+" "+fileDestinationIp+" "+fileDestinationPort+" "+hops+" "+filesString+" "+intermediateIp+" "+intermediatePort);
                break;
        
            }
        }
    }
    
        public Message(MessageType type, int noOfFiles, String fileDestinationIp, int fileDestinationPort, int hops, String fileString,String intermediateIp,int intermediatePort){
        switch(type){
        
            case SEROK: 
            {
                message=appendLength("SEROK"+" "+noOfFiles+" "+fileDestinationIp+" "+fileDestinationPort+" "+hops+" "+fileString+" "+intermediateIp+" "+intermediatePort);
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

