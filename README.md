========================================================================
            ADVANCED SOCIAL NETWORK PROJECT - COMPLETE OVERVIEW
========================================================================

1. ORIGINAL STATE vs FINAL STATE
--------------------------------
Original State: The project consisted of a single massive `Main.java` file (290+ lines) containing everything: raw data structures (Arrays/Lists/Sets), Disjoint Set Union (DSU) algorithms, interactive CLI menu, and business logic layered without strong separation of concerns.

Final State: We refactored the entire project into a full-stack, enterprise-grade architecture. The backend is split into independent micro-modules (Models, Repository, Services) governed by SOLID principles, with advanced multithreading for scalability, and exposed via a REST API to a completely separate React (Vite) Frontend named "NexusSocial". The React frontend now successfully handles all 9 core backend algorithmic features interactively!


2. ARCHITECTURE OVERVIEW (Backend)
----------------------------------
The Java project is logically separated using standard Model-View-Controller (MVC) paradigms via packages:

- src/main/java/com/social/model/: Contains raw data entities (User, Notification).
- src/main/java/com/social/repository/: Handles memory storage and data retrieval globally (DataStore).
- src/main/java/com/social/service/: Handles the core business functionality separately (SocialNetworkService, RecommendationEngine, SearchService, NotificationManager).
- src/main/java/com/social/api/: Hosts the native lightweight HTTP Server converting Java data to standard local JSON endpoints.
- src/main/java/com/social/Main.java: The startup script hooking everything together, now protected against Terminal EOF crashes and injecting base mock data automatically!


3. OOP & SOLID PRINCIPLES APPLIED
---------------------------------
- Single Responsibility Principle (SRP): `SocialNetworkService` only controls friendships. `SearchService` only builds search indexes. `NotificationManager` only sends messages.
- Encapsulation: Class variables like user IDs or notifications are marked `private` and rely on strictly defined `getters` and `setters`.
- Abstraction: Highly complex internal graph mapping is completely hidden from the REST APIs.


4. DESIGN PATTERNS APPLIED
--------------------------
- Singleton Pattern: Used widely in `DataStore.java`, `SocialNetworkService`, and engines. We only want exactly *one instance* of the database and one engine floating in RAM globally at any time.
- Observer Pattern: Integrated into the `NotificationManager`. When an action (like making a friendship) completes, the manager gets "notified" and async processes a welcome/status message without holding back the UI thread.


5. ADVANCED DATA STRUCTURES IN USE
----------------------------------
- Disjoint Set Union (DSU) w/ Path Compression: When friendships form, this structure instantly knows if two people are structurally united/chained together through mutuals in O(1) time. Integrated natively into the UI's "Network Sandbox" feature to verify structural connections.
- Trie (Prefix Tree): To search for user names significantly fast! Used in `SearchService.java`, every name letter creates a new branch, making "lookup suggestions" insanely fast via the frontend Navbar Search Dropdown.
- Max-Heap (PriorityQueue): Used in `RecommendationEngine.java`. When looking for suggested friends, the PriorityQueue auto-ranks who you have the *most* mutuals with so it always pops the best recommendations first on the right sidebar.
- Graph Adjacency List: The entire connection system maps strings (UUID) to hash sets showing explicit 1-on-1 friendship edges natively.


6. MULTITHREADING AND CONCURRENCY
---------------------------------
To mimic a real-world server, concurrency was applied so multiple simulated users clicking buttons at once wouldn't corrupt the RAM.
- ExecutorService: Built into `NotificationManager.class`. It isolates a fixed pool of exactly 4 threads to shoot notifications around in the dark while the main JVM thread goes back to routing standard API calls.
- ConcurrentHashMap & CopyOnWriteArrayList: Instead of standard arrays/lists, we use highly synchronized native JDK collections to handle real-time modifications.
- ReentrantReadWriteLock: Locks down the entire graph state physically during extremely dangerous operations (like recalculating DSU components).


7. THE REST API PLATFORM (ALL 9 FEATURES EXPOSED)
--------------------------------------------------
Without bloating your OS with Spring Boot/Tomcat, we deployed the native `com.sun.net.httpserver` tool natively on Port 8085 covering all 9 project requirements:
- GET /api/users (Add and list global users)
- POST /api/friends (Add friendships)
- POST /api/unfriend (Remove friendships gracefully)
- GET /api/search?q= (Search User via Trie algorithm)
- GET /api/suggestions?userId= (Get Friend Suggestions via Max-Heap)
- GET /api/user?id= (Display User Info, network edges, & Notifications)
- GET /api/stats (Display total DSU Group Statistics globally)
- GET /api/mutuals (Find Mutual Friends and explicitly check Connection paths)


8. THE REACT.JS FRONTEND ("NEXUS SOCIAL" UI)
--------------------------------------------
Housed entirely separated in `frontend/src/App.jsx`.
- Advanced Modern 3-Column Layout: Left (Registry/Stats/Explore), Center (Live Activity Feed Timeline), Right (Engine Suggestions/Network Sandbox).
- Uses `useState` and `useEffect` alongside asynchronous Javascript `fetch()` APIs to poll the Java backend HTTP server dynamically every 2 seconds.
- Fully simulated "Login" system letting you swap user profiles and visually track their isolated data.
- Wrapped in a dark-mode Glassmorphism styled CSS container mimicking premium UI platforms like GitHub and Twitter.
- Auto-generates Avatars and custom UI pills/badges for dynamic feedback.


9. HOW TO RUN EVERYTHING AGAIN?
-------------------------------
Start the backend Engine first:
1. Terminal -> cd Desktop\social
2. javac -d out $(Get-ChildItem -Path src/main/java -Recurse -Filter *.java | Select-Object -ExpandProperty FullName)
3. java -cp out com.social.Main

Note: Upon launching, `Alice, Bob, Charlie, David, and Eve` are injected sequentially with live edges so your Frontend algorithms immediately have graph context!

Start the React Frontend:
1. Secondary Terminal -> cd Desktop\social\frontend
2. npm run dev
3. View on http://localhost:5173 
========================================================================
