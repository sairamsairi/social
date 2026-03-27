package com.social;

import com.social.service.SocialNetworkService;
import com.social.service.NotificationManager;
import com.social.repository.DataStore;
import com.social.api.APIServer;

import java.util.Scanner;
import java.util.List;
import java.util.Set;

public class Main {
    public static void displayMenu() {
        System.out.println("\n========== ADVANCED SOCIAL NETWORK ==========");
        System.out.println("1. Add User");
        System.out.println("2. Add Friendship");
        System.out.println("3. Remove Friendship");
        System.out.println("4. Check if Users are Connected");
        System.out.println("5. Find Mutual Friends");
        System.out.println("6. Get Friend Suggestions");
        System.out.println("7. Display Group Statistics");
        System.out.println("8. Search User (Trie)");
        System.out.println("9. Display User Info & Notifications");
        System.out.println("10. Exit");
        System.out.println("=============================================");
        System.out.print("Enter choice: ");
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        SocialNetworkService service = SocialNetworkService.getInstance();
        DataStore db = DataStore.getInstance();

        System.out.println("Welcome to the Advanced Modular Social Network System");
        
        try {
            APIServer.startServer();
            
            // --- INJECT MOCK DATA FOR FRONTEND TESTING ---
            System.out.println("Injecting Mock Data...");
            String u1 = service.addUser("Alice");
            String u2 = service.addUser("Bob");
            String u3 = service.addUser("Charlie");
            String u4 = service.addUser("David");
            String u5 = service.addUser("Eve");
            
            service.addFriendship(u1, u2); // Alice & Bob
            service.addFriendship(u2, u3); // Bob & Charlie
            service.addFriendship(u3, u4); // Charlie & David
            
            System.out.println("Mock Data Ready! Refresh your browser.");
            // ---------------------------------------------
            
        } catch (Exception e) {
            e.printStackTrace();
        }

        while (true) {
            displayMenu();
            if (!scanner.hasNextLine()) {
                // If the terminal detached or EOF is reached, keep the API server alive indefinitely.
                try { Thread.sleep(10000000); } catch (InterruptedException e) {}
                continue;
            }
            String input = scanner.nextLine();
            int choice = -1;
            try {
                choice = Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("Invalid input.");
                continue;
            }

            switch (choice) {
                case 1 -> {
                    System.out.print("Enter user name: ");
                    String name = scanner.nextLine();
                    String uId = service.addUser(name);
                    System.out.println("User Added! ID: " + uId);
                }
                case 2 -> {
                    System.out.print("Enter User 1 ID: ");
                    String u1 = scanner.nextLine();
                    System.out.print("Enter User 2 ID: ");
                    String u2 = scanner.nextLine();
                    if(service.addFriendship(u1, u2)) {
                        System.out.println("Friendship Created!");
                    } else {
                        System.out.println("Failed to create friendship.");
                    }
                }
                case 3 -> {
                    System.out.print("Enter User 1 ID: ");
                    String u1 = scanner.nextLine();
                    System.out.print("Enter User 2 ID: ");
                    String u2 = scanner.nextLine();
                    if(service.removeFriendship(u1, u2)) {
                        System.out.println("Friendship Removed.");
                    } else {
                        System.out.println("Failed to remove friendship.");
                    }
                }
                case 4 -> {
                    System.out.print("Enter User 1 ID: ");
                    String u1 = scanner.nextLine();
                    System.out.print("Enter User 2 ID: ");
                    String u2 = scanner.nextLine();
                    if (service.areConnected(u1, u2)) {
                        System.out.println("Users are structurally connected.");
                    } else {
                        System.out.println("Users are NOT connected.");
                    }
                }
                case 5 -> {
                    System.out.print("Enter User 1 ID: ");
                    String u1 = scanner.nextLine();
                    System.out.print("Enter User 2 ID: ");
                    String u2 = scanner.nextLine();
                    List<String> mutual = service.getMutualFriends(u1, u2);
                    System.out.println("Mutual friends IDs: " + mutual);
                }
                case 6 -> {
                    System.out.print("Enter User ID: ");
                    String u1 = scanner.nextLine();
                    List<String> suggestions = service.suggestFriends(u1, 5);
                    System.out.println("Friend Suggestions IDs: " + suggestions);
                }
                case 7 -> service.displayGroupStatistics();
                case 8 -> {
                    System.out.print("Enter prefix to search: ");
                    String prefix = scanner.nextLine();
                    Set<String> searchResults = service.searchUsersByName(prefix);
                    System.out.println("Search Results (IDs): " + searchResults);
                    for (String r : searchResults) {
                        System.out.println(" -> " + db.users.get(r).getName());
                    }
                }
                case 9 -> {
                    System.out.print("Enter User ID: ");
                    String u1 = scanner.nextLine();
                    service.displayUser(u1);
                }
                case 10 -> {
                    System.out.println("Shutting down the system...");
                    NotificationManager.getInstance().shutdown();
                    return;
                }
                default -> System.out.println("Unknown option.");
            }
        }
    }
}
