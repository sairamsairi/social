package com.social.service;

import java.util.concurrent.*;
import java.util.*;

public class SearchService {
    
    private static class TrieNode {
        Map<Character, TrieNode> children = new ConcurrentHashMap<>();
        boolean isEndOfWord;
        Set<String> userIds = new ConcurrentSkipListSet<>(); // store users with this exact name
    }

    private final TrieNode root = new TrieNode();

    // Singleton Pattern
    private static SearchService instance;

    private SearchService() {}

    public static synchronized SearchService getInstance() {
        if (instance == null) {
            instance = new SearchService();
        }
        return instance;
    }

    // Insert user into Trie
    public void insertUser(String name, String userId) {
        if (name == null || name.isEmpty()) return;
        TrieNode current = root;
        // Case insensitive search
        name = name.toLowerCase();
        
        for (char ch : name.toCharArray()) {
            current.children.putIfAbsent(ch, new TrieNode());
            current = current.children.get(ch);
            current.userIds.add(userId); // Add to path for fast prefix search
        }
        current.isEndOfWord = true;
    }

    // Search users by prefix
    public Set<String> searchPrefix(String prefix) {
        if (prefix == null || prefix.isEmpty()) return Collections.emptySet();
        TrieNode current = root;
        prefix = prefix.toLowerCase();

        for (char ch : prefix.toCharArray()) {
            TrieNode node = current.children.get(ch);
            if (node == null) return Collections.emptySet();
            current = node;
        }
        // Return user ids matching the prefix
        return current.userIds;
    }
}
