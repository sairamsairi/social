package com.social.service;

import com.social.repository.DataStore;
import com.social.model.User;

import java.util.*;
import java.util.concurrent.CopyOnWriteArraySet;

public class SocialNetworkService {

    private final DataStore db = DataStore.getInstance();
    private final NotificationManager notificationManager = NotificationManager.getInstance();
    private final SearchService searchService = SearchService.getInstance();
    private final RecommendationEngine recommendationEngine = RecommendationEngine.getInstance();
    
    private static SocialNetworkService instance;
    private SocialNetworkService() {}

    public static synchronized SocialNetworkService getInstance() {
        if (instance == null) instance = new SocialNetworkService();
        return instance;
    }

    public String addUser(String name) {
        User user = new User(name);
        db.users.put(user.getId(), user);
        db.adjacencyList.put(user.getId(), new CopyOnWriteArraySet<>());
        db.graphLock.writeLock().lock();
        try {
            db.parent.put(user.getId(), user.getId());
            db.size.put(user.getId(), 1);
        } finally {
            db.graphLock.writeLock().unlock();
        }
        searchService.insertUser(name, user.getId());
        notificationManager.notifyUser(user.getId(), "Welcome to NexusSocial! Build your network.");
        return user.getId();
    }

    public boolean addFriendship(String uId1, String uId2) {
        if (!db.users.containsKey(uId1) || !db.users.containsKey(uId2)) return false;
        if (uId1.equals(uId2)) return false;
        Set<String> set1 = db.adjacencyList.get(uId1);
        Set<String> set2 = db.adjacencyList.get(uId2);
        if (set1.contains(uId2)) return false; 
        set1.add(uId2);
        set2.add(uId1);
        db.unionSets(uId1, uId2);
        notificationManager.notifyUser(uId1, "You are now friends with " + db.users.get(uId2).getName());
        notificationManager.notifyUser(uId2, "You are now friends with " + db.users.get(uId1).getName());
        return true;
    }

    public boolean removeFriendship(String uId1, String uId2) {
        if (!db.users.containsKey(uId1) || !db.users.containsKey(uId2)) return false;
        Set<String> set1 = db.adjacencyList.get(uId1);
        Set<String> set2 = db.adjacencyList.get(uId2);
        if (!set1.contains(uId2)) return false; 
        set1.remove(uId2);
        set2.remove(uId1);
        db.rebuildDSU();
        notificationManager.notifyUser(uId1, "You unfollowed " + db.users.get(uId2).getName());
        return true;
    }

    public boolean areConnected(String uId1, String uId2) {
        if (!db.users.containsKey(uId1) || !db.users.containsKey(uId2)) return false;
        return db.findUp(uId1).equals(db.findUp(uId2));
    }

    public List<String> getMutualFriends(String uId1, String uId2) {
        List<String> mutual = new ArrayList<>();
        if (!db.users.containsKey(uId1) || !db.users.containsKey(uId2)) return mutual;
        Set<String> set1 = db.adjacencyList.get(uId1);
        Set<String> set2 = db.adjacencyList.get(uId2);
        for (String f : set1) {
            if (set2.contains(f)) mutual.add(f);
        }
        return mutual;
    }

    public Map<String, Integer> getGroupStats() {
        Map<String, List<String>> groups = new HashMap<>();
        for (String userId : db.users.keySet()) {
            String root = db.findUp(userId);
            groups.computeIfAbsent(root, k -> new ArrayList<>()).add(userId);
        }
        int[] maxSize = {0};
        groups.values().forEach(group -> {
            if (group.size() > maxSize[0]) maxSize[0] = group.size();
        });
        Map<String, Integer> stats = new HashMap<>();
        stats.put("total", groups.size());
        stats.put("max", maxSize[0]);
        return stats;
    }

    public void displayGroupStatistics() { /* Console Legacy */ }

    public Set<String> getFriends(String uId) {
        return db.adjacencyList.getOrDefault(uId, Collections.emptySet());
    }

    public List<String> suggestFriends(String userId, int limit) {
        return recommendationEngine.suggestFriends(userId, limit);
    }
    
    public Set<String> searchUsersByName(String prefix) {
        return searchService.searchPrefix(prefix);
    }

    public void displayUser(String userId) { /* Console Legacy */ }
}
