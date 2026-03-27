
import java.util.*;

class SocialNetwork {
    private List<Integer> parent = new ArrayList<>();
    private List<Integer> size = new ArrayList<>();
    private Map<Integer, Set<Integer>> adjacencyList = new HashMap<>();
    public Map<Integer, String> userNames = new HashMap<>();
    private int nextUserId = 0;

    // Find operation with path compression
    private int findUP(int x) {
        if (x >= parent.size()) return -1;
        if (!parent.get(x).equals(x)) {
            parent.set(x, findUP(parent.get(x)));
        }
        return parent.get(x);
    }

    // Union by size
    private void unionSets(int x, int y) {
        int rootX = findUP(x);
        int rootY = findUP(y);
        if (rootX == rootY) return;
        if (size.get(rootX) < size.get(rootY)) {
            parent.set(rootX, rootY);
            size.set(rootY, size.get(rootY) + size.get(rootX));
        } else {
            parent.set(rootY, rootX);
            size.set(rootX, size.get(rootX) + size.get(rootY));
        }
    }

    // Rebuild DSU after removing friendship
    private void rebuildDSU() {
        for (int i = 0; i < nextUserId; i++) {
            parent.set(i, i);
            size.set(i, 1);
        }
        for (Map.Entry<Integer, Set<Integer>> entry : adjacencyList.entrySet()) {
            int u = entry.getKey();
            for (int v : entry.getValue()) {
                if (u < v) unionSets(u, v);
            }
        }
    }

    // Add new user
    public int addUser(String name) {
        int userId = nextUserId++;
        userNames.put(userId, name);
        parent.add(userId);
        size.add(1);
        adjacencyList.put(userId, new HashSet<>());
        System.out.println("User " + name + " added with ID: " + userId);
        return userId;
    }

    // Add friendship
    public boolean addFriendship(int user1, int user2) {
        if (!userNames.containsKey(user1) || !userNames.containsKey(user2)) {
            System.out.println("Error: One or both users don't exist.");
            return false;
        }
        if (user1 == user2) {
            System.out.println("Error: Users cannot be friends with themselves.");
            return false;
        }
        if (adjacencyList.get(user1).contains(user2)) {
            System.out.println("Users are already friends.");
            return false;
        }
        adjacencyList.get(user1).add(user2);
        adjacencyList.get(user2).add(user1);
        unionSets(user1, user2);
        System.out.println(userNames.get(user1) + " and " + userNames.get(user2) + " are now friends!");
        return true;
    }

    // Remove friendship
    public boolean removeFriendship(int user1, int user2) {
        if (!userNames.containsKey(user1) || !userNames.containsKey(user2)) {
            System.out.println("Error: One or both users don't exist.");
            return false;
        }
        if (!adjacencyList.get(user1).contains(user2)) {
            System.out.println("Users are not friends.");
            return false;
        }
        adjacencyList.get(user1).remove(user2);
        adjacencyList.get(user2).remove(user1);
        rebuildDSU();
        System.out.println(userNames.get(user1) + " and " + userNames.get(user2) + " are no longer friends.");
        return true;
    }

    // Check if users are connected (same group)
    public boolean areConnected(int user1, int user2) {
        if (!userNames.containsKey(user1) || !userNames.containsKey(user2)) return false;
        return findUP(user1) == findUP(user2);
    }

    // Get mutual friends
    public List<Integer> getMutualFriends(int user1, int user2) {
        List<Integer> mutualFriends = new ArrayList<>();
        if (!userNames.containsKey(user1) || !userNames.containsKey(user2)) return mutualFriends;
        Set<Integer> friends1 = adjacencyList.get(user1);
        Set<Integer> friends2 = adjacencyList.get(user2);
        for (int f : friends1) {
            if (friends2.contains(f)) mutualFriends.add(f);
        }
        return mutualFriends;
    }

    // Suggest friends based on mutual connections
    public List<Integer> suggestFriends(int userId, int maxSuggestions) {
        List<Integer> suggestions = new ArrayList<>();
        if (!userNames.containsKey(userId)) return suggestions;

        Map<Integer, Integer> mutualCount = new HashMap<>();
        Set<Integer> visited = new HashSet<>();
        visited.add(userId);

        for (int frnd : adjacencyList.get(userId)) {
            visited.add(frnd);
            for (int frndOfFrnd : adjacencyList.get(frnd)) {
                if (!visited.contains(frndOfFrnd)) {
                    mutualCount.put(frndOfFrnd, mutualCount.getOrDefault(frndOfFrnd, 0) + 1);
                }
            }
        }

        List<Map.Entry<Integer, Integer>> sorted = new ArrayList<>(mutualCount.entrySet());
        sorted.sort((a, b) -> {
            if (!a.getValue().equals(b.getValue())) return b.getValue() - a.getValue();
            return userNames.get(a.getKey()).compareTo(userNames.get(b.getKey()));
        });

        for (int i = 0; i < Math.min(maxSuggestions, sorted.size()); i++) {
            suggestions.add(sorted.get(i).getKey());
        }
        return suggestions;
    }

    // Display group statistics
    public void displayGroupStatistics() {
        Map<Integer, List<Integer>> groups = new HashMap<>();
        for (int i = 0; i < nextUserId; i++) {
            if (userNames.containsKey(i)) {
                int root = findUP(i);
                groups.computeIfAbsent(root, k -> new ArrayList<>()).add(i);
            }
        }
        System.out.println("\n========== GROUP STATISTICS ==========");
        System.out.println("Total number of groups: " + groups.size());
        int groupNum = 1;
        for (Map.Entry<Integer, List<Integer>> group : groups.entrySet()) {
            System.out.println("\nGroup " + groupNum++ + " (Size: " + group.getValue().size() + "):");
            for (int userId : group.getValue()) {
                System.out.println(" - " + userNames.get(userId) + " (ID: " + userId + ")");
            }
        }
        int maxSize = groups.values().stream().mapToInt(List::size).max().orElse(0);
        System.out.println("\nLargest group size: " + maxSize);
    }

    // Display user info
    public void displayUserInfo(int userId) {
        if (!userNames.containsKey(userId)) {
            System.out.println("User not found.");
            return;
        }
        System.out.println("\n========== USER INFO ==========");
        System.out.println("Name: " + userNames.get(userId));
        System.out.println("ID: " + userId);
        System.out.println("Number of friends: " + adjacencyList.get(userId).size());
        System.out.println("Group size: " + size.get(findUP(userId)));
        System.out.print("Friends: ");
        for (int f : adjacencyList.get(userId)) {
            System.out.print(userNames.get(f) + " ");
        }
        System.out.println();
    }

    // Display all users
    public void displayAllUsers() {
        System.out.println("\n========== ALL USERS ==========");
        for (int userId : userNames.keySet()) {
            System.out.println("ID: " + userId + " | Name: " + userNames.get(userId) +
                    " | Friends: " + adjacencyList.get(userId).size());
        }
    }
}

public class Main {
    public static void displayMenu() {
        System.out.println("\n========== SOCIAL NETWORK SYSTEM ==========");
        System.out.println("1. Add User");
        System.out.println("2. Add Friendship");
        System.out.println("3. Remove Friendship");
        System.out.println("4. Check if Users are Connected");
        System.out.println("5. Find Mutual Friends");
        System.out.println("6. Get Friend Suggestions");
        System.out.println("7. Display Group Statistics");
        System.out.println("8. Display User Info");
        System.out.println("9. Display All Users");
        System.out.println("10. Exit");
        System.out.println("==========================================");
        System.out.print("Enter your choice: ");
    }

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        SocialNetwork network = new SocialNetwork();
        int choice;

        System.out.println("Welcome to the Social Network System!");

        while (true) {
            displayMenu();
            choice = sc.nextInt();
            sc.nextLine(); // consume newline

            switch (choice) {
                case 1 -> {
                    System.out.print("Enter user name: ");
                    String name = sc.nextLine();
                    network.addUser(name);
                }
                case 2 -> {
                    System.out.print("Enter first user ID: ");
                    int u1 = sc.nextInt();
                    System.out.print("Enter second user ID: ");
                    int u2 = sc.nextInt();
                    network.addFriendship(u1, u2);
                }
                case 3 -> {
                    System.out.print("Enter first user ID: ");
                    int u1 = sc.nextInt();
                    System.out.print("Enter second user ID: ");
                    int u2 = sc.nextInt();
                    network.removeFriendship(u1, u2);
                }
                case 4 -> {
                    System.out.print("Enter first user ID: ");
                    int u1 = sc.nextInt();
                    System.out.print("Enter second user ID: ");
                    int u2 = sc.nextInt();
                    if (network.areConnected(u1, u2))
                        System.out.println("Users are connected (in same group).");
                    else
                        System.out.println("Users are not connected.");
                }
                case 5 -> {
                    System.out.print("Enter first user ID: ");
                    int u1 = sc.nextInt();
                    System.out.print("Enter second user ID: ");
                    int u2 = sc.nextInt();
                    List<Integer> mutual = network.getMutualFriends(u1, u2);
                    System.out.print("Mutual friends: ");
                    if (mutual.isEmpty()) System.out.print("None");
                    else mutual.forEach(id -> System.out.print("ID:" + id + " "));
                    System.out.println();
                }
                case 6 -> {
                    System.out.println("\n========== FRIEND SUGGESTIONS FOR ALL USERS ==========");
                    for (int uid : network.userNames.keySet()) {
                        String uname = network.userNames.get(uid);
                        List<Integer> suggestions = network.suggestFriends(uid, 5);
                        System.out.print("Suggestions for " + uname + " (ID: " + uid + "): ");
                        if (suggestions.isEmpty()) System.out.print("None available");
                        else suggestions.forEach(sid -> System.out.print(network.userNames.get(sid) + " (ID:" + sid + ") "));
                        System.out.println();
                    }
                    System.out.println("============================================");
                }
                case 7 -> network.displayGroupStatistics();
                case 8 -> {
                    System.out.print("Enter user ID: ");
                    int uid = sc.nextInt();
                    network.displayUserInfo(uid);
                }
                case 9 -> network.displayAllUsers();
                case 10 -> {
                    System.out.println("Thank you for using the Social Network System!");
                    return;
                }
                default -> System.out.println("Invalid choice. Please try again.");
            }
        }
    }
}
