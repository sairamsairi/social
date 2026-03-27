package com.social.service;

import com.social.model.Notification;
import com.social.model.User;
import com.social.repository.DataStore;

import java.util.concurrent.*;

// Observer pattern and thread execution logic
public class NotificationManager {
    private static NotificationManager instance;
    private final ExecutorService executorService;
    private final DataStore db = DataStore.getInstance();

    private NotificationManager() {
        // Core pool size 2, Max 10. For processing async notifications
        executorService = Executors.newFixedThreadPool(4);
    }

    public static synchronized NotificationManager getInstance() {
        if (instance == null) {
            instance = new NotificationManager();
        }
        return instance;
    }

    public void notifyUser(String userId, String message) {
        executorService.submit(() -> {
            try {
                User user = db.users.get(userId);
                if (user != null) {
                    Notification notification = new Notification(message);
                    user.addNotification(notification.toString());
                    System.out.println("[ASYNC] Notification sent to " + user.getName() + " (Thread: " + Thread.currentThread().getName() + ")");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public void shutdown() {
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(800, TimeUnit.MILLISECONDS)) {
                executorService.shutdownNow();
            } 
        } catch (InterruptedException e) {
            executorService.shutdownNow();
        }
    }
}
