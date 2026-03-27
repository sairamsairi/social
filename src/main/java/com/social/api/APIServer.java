package com.social.api;

import com.social.service.SocialNetworkService;
import com.social.repository.DataStore;
import com.social.model.User;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class APIServer {

    public static void startServer() throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(8085), 0);
        System.out.println("Starting advanced backend API on http://localhost:8085");
        SocialNetworkService sns = SocialNetworkService.getInstance();

        server.createContext("/api/users", exchange -> {
            addCorsHeaders(exchange);
            if ("OPTIONS".equals(exchange.getRequestMethod())) { sendResponse(exchange, 204, ""); return; }
            if ("GET".equals(exchange.getRequestMethod())) {
                StringBuilder json = new StringBuilder("[");
                int size = DataStore.getInstance().users.size();
                int count = 0;
                for (User u : DataStore.getInstance().users.values()) {
                    json.append("{\"id\":\"").append(u.getId()).append("\", \"name\":\"").append(u.getName()).append("\"}");
                    if (++count < size) json.append(",");
                }
                json.append("]");
                sendResponse(exchange, 200, json.toString());
            } else if ("POST".equals(exchange.getRequestMethod())) {
                String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                String name = extractJsonValue(body, "name");
                if (name != null && !name.isEmpty()) {
                    String id = sns.addUser(name);
                    sendResponse(exchange, 201, "{\"id\":\"" + id + "\"}");
                } else sendResponse(exchange, 400, "{}");
            }
        });

        server.createContext("/api/friends", exchange -> {
            addCorsHeaders(exchange);
            if ("OPTIONS".equals(exchange.getRequestMethod())) { sendResponse(exchange, 204, ""); return; }
            if ("POST".equals(exchange.getRequestMethod())) {
                String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                String u1 = extractJsonValue(body, "u1");
                String u2 = extractJsonValue(body, "u2");
                if(u1 != null && u2 != null) {
                    boolean success = sns.addFriendship(u1, u2);
                    sendResponse(exchange, success ? 200 : 400, "{\"success\":" + success + "}");
                } else sendResponse(exchange, 400, "{}");
            }
        });

        server.createContext("/api/unfriend", exchange -> {
            addCorsHeaders(exchange);
            if ("OPTIONS".equals(exchange.getRequestMethod())) { sendResponse(exchange, 204, ""); return; }
            if ("POST".equals(exchange.getRequestMethod())) {
                String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                String u1 = extractJsonValue(body, "u1");
                String u2 = extractJsonValue(body, "u2");
                if(u1 != null && u2 != null) {
                    boolean success = sns.removeFriendship(u1, u2);
                    sendResponse(exchange, success ? 200 : 400, "{\"success\":" + success + "}");
                } else sendResponse(exchange, 400, "{}");
            }
        });

        server.createContext("/api/search", exchange -> {
             addCorsHeaders(exchange);
             if ("OPTIONS".equals(exchange.getRequestMethod())) { sendResponse(exchange, 204, ""); return; }
             Map<String, String> query = queryToMap(exchange.getRequestURI().getQuery());
             String q = query.get("q");
             if (q == null) { sendResponse(exchange, 400, "[]"); return; }
             Set<String> ids = sns.searchUsersByName(q);
             StringBuilder json = new StringBuilder("[");
             int count = 0;
             for(String id : ids) {
                 User u = DataStore.getInstance().users.get(id);
                 if(u!=null) {
                     json.append("{\"id\":\"").append(u.getId()).append("\",\"name\":\"").append(u.getName()).append("\"}");
                     if (++count < ids.size()) json.append(",");
                 }
             }
             json.append("]");
             sendResponse(exchange, 200, json.toString());
        });

        server.createContext("/api/suggestions", exchange -> {
            addCorsHeaders(exchange);
            if ("OPTIONS".equals(exchange.getRequestMethod())) { sendResponse(exchange, 204, ""); return; }
            Map<String, String> query = queryToMap(exchange.getRequestURI().getQuery());
            String userId = query.get("userId");
            if (userId == null) { sendResponse(exchange, 400, "[]"); return; }
            List<String> suggestions = sns.suggestFriends(userId, 5);
            StringBuilder json = new StringBuilder("[");
            int count = 0;
            for(String id : suggestions) {
                User u = DataStore.getInstance().users.get(id);
                if(u!=null) {
                    json.append("{\"id\":\"").append(u.getId()).append("\",\"name\":\"").append(u.getName()).append("\"}");
                    if (++count < suggestions.size()) json.append(",");
                }
            }
            json.append("]");
            sendResponse(exchange, 200, json.toString());
        });

        server.createContext("/api/user", exchange -> {
            addCorsHeaders(exchange);
            if ("OPTIONS".equals(exchange.getRequestMethod())) { sendResponse(exchange, 204, ""); return; }
            Map<String, String> query = queryToMap(exchange.getRequestURI().getQuery());
            String id = query.get("id");
            User u = DataStore.getInstance().users.get(id);
            if (u != null) {
                // Notifs
                StringBuilder notifJson = new StringBuilder("[");
                List<String> notifs = u.getNotifications();
                for(int i=0; i<notifs.size(); i++){
                    notifJson.append("\"").append(notifs.get(i).replace("\"", "\\\"")).append("\"");
                    if(i < notifs.size()-1) notifJson.append(",");
                }
                notifJson.append("]");

                // Friends
                StringBuilder friendsJson = new StringBuilder("[");
                Set<String> friends = sns.getFriends(id);
                int count = 0;
                for(String f : friends) {
                    friendsJson.append("\"").append(f).append("\"");
                    if (++count < friends.size()) friendsJson.append(",");
                }
                friendsJson.append("]");
                
                String res = "{\"id\":\"" + u.getId() + "\",\"name\":\"" + u.getName() 
                    + "\",\"notifications\":" + notifJson.toString() 
                    + ",\"friends\":" + friendsJson.toString() + "}";
                sendResponse(exchange, 200, res);
            } else {
                sendResponse(exchange, 404, "{}");
            }
        });

        server.createContext("/api/stats", exchange -> {
             addCorsHeaders(exchange);
             if ("OPTIONS".equals(exchange.getRequestMethod())) { sendResponse(exchange, 204, ""); return; }
             Map<String, Integer> stats = sns.getGroupStats();
             sendResponse(exchange, 200, "{\"totalGroups\":" + stats.get("total") + ",\"largestSize\":" + stats.get("max") + "}");
        });

        server.createContext("/api/mutuals", exchange -> {
             addCorsHeaders(exchange);
             if ("OPTIONS".equals(exchange.getRequestMethod())) { sendResponse(exchange, 204, ""); return; }
             Map<String, String> query = queryToMap(exchange.getRequestURI().getQuery());
             String u1 = query.get("u1"); String u2 = query.get("u2");
             if(u1!=null && u2!=null) {
                 List<String> mutuals = sns.getMutualFriends(u1, u2);
                 StringBuilder json = new StringBuilder("[");
                 for(int i=0; i<mutuals.size(); i++) {
                     User u = DataStore.getInstance().users.get(mutuals.get(i));
                     if(u!=null){
                         json.append("{\"id\":\"").append(u.getId()).append("\",\"name\":\"").append(u.getName()).append("\"}");
                         if(i < mutuals.size()-1) json.append(",");
                     }
                 }
                 json.append("]");
                 sendResponse(exchange, 200, "{\"mutuals\":"+json.toString()+", \"connected\":"+sns.areConnected(u1, u2)+"}");
             } else sendResponse(exchange, 400, "{}");
        });

        server.setExecutor(null);
        server.start();
    }

    private static String extractJsonValue(String json, String key) {
        try { return json.split("\"" + key + "\"[\\s]*:[\\s]*\"")[1].split("\"")[0]; } catch(Exception e) { return null; }
    }

    private static Map<String, String> queryToMap(String query) {
        Map<String, String> result = new HashMap<>();
        if (query == null) return result;
        for (String param : query.split("&")) {
            String[] entry = param.split("=");
            if (entry.length > 1) result.put(entry[0], entry[1]);
            else result.put(entry[0], "");
        }
        return result;
    }

    private static void addCorsHeaders(HttpExchange exchange) {
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type,Authorization");
    }

    private static void sendResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
        exchange.getResponseHeaders().add("Content-Type", "application/json");
        byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(statusCode, bytes.length);
        OutputStream os = exchange.getResponseBody();
        os.write(bytes);
        os.close();
    }
}
