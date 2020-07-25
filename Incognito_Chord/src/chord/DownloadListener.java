/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package chord;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toList;

/**
 *
 * @author lakindu
 */
public class DownloadListener implements Runnable{
    private Node chordNode;

    public DownloadListener(Node chordNode) {
        this.chordNode = chordNode;
    }

    public void run() {
        try {
            int serverPort = chordNode.getPort()+1000;
            System.out.println("Starting Node "+chordNode.getAddress()+":"+chordNode.getPort()+" Download Listener on port "+serverPort);
            
            HttpServer server = HttpServer.create(new InetSocketAddress(serverPort), 0);
            server.createContext("/api/download", (exchange -> {

            //Get Filename
            Map<String, List<String>> params = splitQuery(exchange.getRequestURI().getRawQuery());
            String noNameText = "Anonymous";
            String name = params.get("file").get(0);
            String songName = String.format("%s.mp3", name.replace(" ",""));

            System.out.println("Sending File "+songName);
            String respText = "Random Content For Song " + songName;
            Headers responseHeaders = exchange.getResponseHeaders();
            responseHeaders.add("Content-Type", "application/octet-stream");
            responseHeaders.add("Content-Disposition", "attachment;filename="+songName);

            //Sending File
            exchange.sendResponseHeaders(200, respText.getBytes().length);
            OutputStream output = exchange.getResponseBody();
            output.write(respText.getBytes());
            output.flush();
            exchange.close();
        }));
        server.setExecutor(null); // creates a default executor
        server.start();
            
        } catch (IOException e) {
            System.err.println("error when downlod thread listening for connections");
        }
    }
    
    public Map<String, List<String>> splitQuery(String query) {
        if (query == null || "".equals(query)) {
            return Collections.emptyMap();
        }

        return Pattern.compile("&").splitAsStream(query)
            .map(s -> Arrays.copyOf(s.split("="), 2))
            .collect(groupingBy(s -> decode(s[0]), mapping(s -> decode(s[1]), toList())));

    }

    private String decode(final String encoded) {
        try {
            return encoded == null ? null : URLDecoder.decode(encoded, "UTF-8");
        } catch (final UnsupportedEncodingException e) {
            throw new RuntimeException("UTF-8 is a required encoding", e);
        }
    }
}
