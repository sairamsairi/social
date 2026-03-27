package com.social.repository;

import com.social.model.User;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.HashMap;

// Thread-safe Singleton DataStore
public class DataStore {
    private static volatile DataStore instance;

    // Users Store
    public final ConcurrentHashMap<String, User> users = new ConcurrentHashMap<>();
    
    // Adjacency List for Graph
    public final ConcurrentHashMap<String, Set<String>> adjacencyList = new ConcurrentHashMap<>();
    
    // DSU structures
    public final Map<String, String> parent = new HashMap<>(); // Needs external sync
    public final Map<String, Integer> size = new HashMap<>(); // Needs external sync
    
    // Graph Lock
    public final ReentrantReadWriteLock graphLock = new ReentrantReadWriteLock();

    private DataStore() {
        // Private constructor for Singleton
    }

    public static DataStore getInstance() {
        if (instance == null) {
            synchronized (DataStore.class) {
                if (instance == null) {
                    instance = new DataStore();
                }
            }
        }
        return instance;
    }

    // Graph Operations requiring Lock
    public String findUp(String x) {
        graphLock.writeLock().lock();
        try {
            if (!parent.containsKey(x)) return x;
            if (!parent.get(x).equals(x)) {
                parent.put(x, findUp(parent.get(x))); // Path compression
            }
            return parent.get(x);
        } finally {
            graphLock.writeLock().unlock();
        }
    }

    public void unionSets(String x, String y) {
        graphLock.writeLock().lock();
        try {
            String rootX = findUp(x);
            String rootY = findUp(y);
            if (rootX.equals(rootY)) return;

            int sizeX = size.getOrDefault(rootX, 1);
            int sizeY = size.getOrDefault(rootY, 1);

            if (sizeX < sizeY) {
                parent.put(rootX, rootY);
                size.put(rootY, sizeY + sizeX);
            } else {
                parent.put(rootY, rootX);
                size.put(rootX, sizeX + sizeY);
            }
        } finally {
            graphLock.writeLock().unlock();
        }
    }

    public void rebuildDSU() {
        graphLock.writeLock().lock();
        try {
            parent.clear();
            size.clear();
            for (String uId : users.keySet()) {
                parent.put(uId, uId);
                size.put(uId, 1);
            }
            for (Map.Entry<String, Set<String>> entry : adjacencyList.entrySet()) {
                String u = entry.getKey();
                for (String v : entry.getValue()) {
                    if (u.compareTo(v) < 0) { // To avoid double union
                        unionSets(u, v);
                    }
                }
            }
        } finally {
            graphLock.writeLock().unlock();
        }
    }
}
