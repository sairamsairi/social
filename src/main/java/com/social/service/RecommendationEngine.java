package com.social.service;

import com.social.repository.DataStore;
import com.social.model.User;
import java.util.*;

public class RecommendationEngine {
    private final DataStore db = DataStore.getInstance();

    private static RecommendationEngine instance;

    private RecommendationEngine() {}

    public static synchronized RecommendationEngine getInstance() {
        if (instance == null) {
            instance = new RecommendationEngine();
        }
        return instance;
    }

    // Custom data structure for PQ
    private static class FriendScore {
        String userId;
        int mutualCount;
        String name;

        FriendScore(String userId, int mutualCount, String name) {
            this.userId = userId;
            this.mutualCount = mutualCount;
            this.name = name;
        }
    }

    public List<String> suggestFriends(String userId, int maxSuggestions) {
        List<String> suggestions = new ArrayList<>();
        if (!db.users.containsKey(userId)) return suggestions;

        Map<String, Integer> mutualCount = new HashMap<>();
        Set<String> visited = new HashSet<>();
        visited.add(userId);

        Set<String> myFriends = db.adjacencyList.get(userId);
        if (myFriends == null) return suggestions;

        for (String frnd : myFriends) {
            visited.add(frnd);
            Set<String> frndOfFrnd = db.adjacencyList.get(frnd);
            if (frndOfFrnd != null) {
                for (String fof : frndOfFrnd) {
                    if (!visited.contains(fof)) {
                        mutualCount.put(fof, mutualCount.getOrDefault(fof, 0) + 1);
                    }
                }
            }
        }

        // Use PriorityQueue for top N
        PriorityQueue<FriendScore> pq = new PriorityQueue<>(
            (a, b) -> {
                if (a.mutualCount != b.mutualCount) {
                    return b.mutualCount - a.mutualCount; // descending
                }
                return a.name.compareTo(b.name); // ascending by name
            }
        );

        for (Map.Entry<String, Integer> entry : mutualCount.entrySet()) {
            String uid = entry.getKey();
            User u = db.users.get(uid);
            if (u != null) {
                pq.offer(new FriendScore(uid, entry.getValue(), u.getName()));
            }
        }

        int count = 0;
        while (!pq.isEmpty() && count < maxSuggestions) {
            suggestions.add(pq.poll().userId);
            count++;
        }

        return suggestions;
    }
}
