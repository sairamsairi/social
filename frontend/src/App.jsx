import { useState, useEffect } from 'react';
import './App.css';

function App() {
  const [users, setUsers] = useState([]);
  const [currentUser, setCurrentUser] = useState(''); 
  const [newUserName, setNewUserName] = useState('');
  
  // Search State
  const [searchQuery, setSearchQuery] = useState('');
  const [searchResults, setSearchResults] = useState([]);

  // Suggestions State
  const [suggestions, setSuggestions] = useState([]);

  // Logged-in User Profile Data
  const [userProfile, setUserProfile] = useState(null);

  // Network Stats
  const [networkStats, setNetworkStats] = useState({totalGroups: 0, largestSize: 0});

  // Sandbox State
  const [sandboxTarget, setSandboxTarget] = useState('');
  const [sandboxData, setSandboxData] = useState(null); // {mutuals: [], connected: true/false}

  const fetchData = async () => {
    try {
      const uResp = await fetch('http://localhost:8085/api/users');
      setUsers(await uResp.json());
      const sResp = await fetch('http://localhost:8085/api/stats');
      setNetworkStats(await sResp.json());
    } catch (err) { }
  };

  useEffect(() => {
    fetchData();
    const interval = setInterval(() => {
        fetchData();
        if(currentUser) fetchUserProfile(currentUser);
    }, 2000); 
    return () => clearInterval(interval);
  }, [currentUser]);

  useEffect(() => {
    if (currentUser) {
        fetchSuggestions(currentUser);
        fetchUserProfile(currentUser);
    } else {
        setSuggestions([]);
        setUserProfile(null);
    }
  }, [currentUser]);

  const handleAddUser = async (e) => {
    e.preventDefault();
    if (!newUserName.trim()) return;
    try {
        const res = await fetch('http://localhost:8085/api/users', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ name: newUserName.trim() })
        });
        const data = await res.json();
        setNewUserName('');
        fetchData();
        if (!currentUser && data.id) setCurrentUser(data.id);
    } catch (err) {}
  };

  const handleToggleFriendship = async (targetId) => {
    if (!currentUser || currentUser === targetId) return;
    
    // Check if we are unfollowing or following based on userProfile
    const isFriend = userProfile?.friends?.includes(targetId);
    const endpoint = isFriend ? '/api/unfriend' : '/api/friends';

    await fetch(`http://localhost:8085${endpoint}`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ u1: currentUser, u2: targetId })
    });
    
    fetchSuggestions(currentUser);
    fetchUserProfile(currentUser);
    if(sandboxTarget === targetId) checkSandboxValidation(targetId); // refresh sandbox
  };

  const handleSearch = async (e) => {
      e.preventDefault();
      if(!searchQuery.trim()) { setSearchResults([]); return; }
      const res = await fetch(`http://localhost:8085/api/search?q=${searchQuery.trim()}`);
      setSearchResults(await res.json());
  };

  const fetchSuggestions = async (userId) => {
      const res = await fetch(`http://localhost:8085/api/suggestions?userId=${userId}`);
      if(res.ok) setSuggestions(await res.json());
  };

  const fetchUserProfile = async (userId) => {
      const res = await fetch(`http://localhost:8085/api/user?id=${userId}`);
      if(res.ok) setUserProfile(await res.json());
  };

  const checkSandboxValidation = async (targetId) => {
      setSandboxTarget(targetId);
      if(!targetId || !currentUser) { setSandboxData(null); return; }
      const res = await fetch(`http://localhost:8085/api/mutuals?u1=${currentUser}&u2=${targetId}`);
      if(res.ok) setSandboxData(await res.json());
  };

  const getInitials = (name) => name ? name.substring(0, 2).toUpperCase() : '?';

  return (
    <div className="social-layout">
      {/* 🌟 TOP NAVIGATION BAR */}
      <nav className="navbar">
        <div className="nav-brand">🌐 NexusSocial</div>
        <div className="nav-search">
            <form onSubmit={handleSearch} className="search-bar">
                <input type="text" placeholder="Search people..." value={searchQuery} onChange={e => setSearchQuery(e.target.value)} />
            </form>
            {searchResults.length > 0 && searchQuery && (
                <div className="search-dropdown">
                    {searchResults.map(u => (
                        <div key={u.id} className="search-item">
                            <div className="avatar small">{getInitials(u.name)}</div>
                            <span>{u.name}</span>
                            {currentUser && u.id !== currentUser && (
                                <button className={`add-btn micro ${userProfile?.friends?.includes(u.id) ? 'danger' : ''}`}
                                        onClick={() => handleToggleFriendship(u.id)}>
                                    {userProfile?.friends?.includes(u.id) ? 'x Unfollow' : '+ Follow'}
                                </button>
                            )}
                        </div>
                    ))}
                </div>
            )}
        </div>
        <div className="nav-profile">
            <span className="login-text">Login as:</span>
            <select value={currentUser} onChange={e => setCurrentUser(e.target.value)} className="user-switcher">
                <option value="">Guest Mode</option>
                {users.map(u => <option key={u.id} value={u.id}>{u.name}</option>)}
            </select>
        </div>
      </nav>

      <div className="main-container">
        
        {/* 🌟 LEFT SIDEBAR */}
        <aside className="left-sidebar">
             <div className="sidebar-card">
                <h3>Join Network</h3>
                <p className="subtitle">Register New Profile</p>
                <form onSubmit={handleAddUser} className="join-form">
                    <input type="text" placeholder="Full Name..." value={newUserName} onChange={e => setNewUserName(e.target.value)} />
                    <button type="submit" className="primary-btn">Sign Up</button>
                </form>
             </div>

             <div className="sidebar-card">
                 <h3>Network Statistics 📊</h3>
                 <p className="subtitle">Live Global DSU Analysis</p>
                 <div className="stat-row"><span>Total Global Users:</span> <strong>{users.length}</strong></div>
                 <div className="stat-row"><span>Distinct Social Rings:</span> <strong>{networkStats.totalGroups}</strong></div>
                 <div className="stat-row"><span>Largest Cluster Size:</span> <strong>{networkStats.largestSize}</strong></div>
             </div>

             <div className="sidebar-card scrollable">
                 <h3>Explore 🌍</h3>
                 <div className="explore-list">
                    {users.slice(0, 15).map(u => {
                        const isSelf = u.id === currentUser;
                        const isFrnd = userProfile?.friends?.includes(u.id);
                        return (
                        <div className="explore-item" key={u.id}>
                            <div className="avatar-wrapper">
                                <div className="avatar medium">{getInitials(u.name)}</div>
                                <div className="status-dot"></div>
                            </div>
                            <div className="explore-info">
                                <strong>{u.name} {isSelf && "(You)"}</strong>
                                <span>Following: {isFrnd ? 'Yes' : 'No'}</span>
                            </div>
                            {!isSelf && currentUser && (
                                <button className={`add-btn micro ${isFrnd ? 'danger' : ''}`} onClick={() => handleToggleFriendship(u.id)}>
                                    {isFrnd ? 'Unfollow' : 'Follow'}
                                </button>
                            )}
                        </div>
                    )})}
                 </div>
             </div>
        </aside>

        {/* 🌟 CENTER FEED */}
        <main className="center-feed">
            {currentUser && userProfile ? (
                 <>
                    <div className="feed-header">
                        <h2>Your Activity Feed</h2>
                        <span className="badge">{userProfile.notifications.length} Updates</span>
                    </div>

                    <div className="timeline">
                        {userProfile.notifications.length === 0 ? (
                            <div className="empty-state">Welcome! Follow people to build your timeline!</div>
                        ) : (
                            userProfile.notifications.slice().reverse().map((notif, i) => (
                                <div className="post-card fade-in" key={i}>
                                    <div className="post-header">
                                        <div className="avatar system">🤖</div>
                                        <div className="post-meta">
                                            <strong>System Matchmaker</strong>
                                            <span className="timestamp">Active Feed Update</span>
                                        </div>
                                    </div>
                                    <div className="post-content">{notif}</div>
                                </div>
                            ))
                        )}
                    </div>
                 </>
            ) : (
                <div className="guest-state">
                    <h2>Welcome to NexusSocial.</h2>
                    <p>Select a user from the top right to view their Activity Feed and test algorithms.</p>
                </div>
            )}
        </main>

        {/* 🌟 RIGHT SIDEBAR */}
        <aside className="right-sidebar">
            <div className="sidebar-card">
                <h3>Suggested For You 💡</h3>
                <p className="subtitle">Max-Heap Algorithm Recommendations</p>
                {!currentUser ? (
                    <div className="muted-text text-center">Login to see algorithmic suggestions</div>
                ) : suggestions.length === 0 ? (
                    <div className="muted-text text-center">No mutual suggestions right now</div>
                ) : (
                    <div className="suggestions-list">
                        {suggestions.map((u, i) => (
                            <div className="suggestion-item" key={u.id}>
                                <div className="sugg-left">
                                    <div className={`avatar`}>{getInitials(u.name)}</div>
                                    <div className="sugg-info">
                                        <strong>{u.name}</strong>
                                        <span>Highly linked</span>
                                    </div>
                                </div>
                                <button className="add-btn" onClick={() => handleToggleFriendship(u.id)}>Follow</button>
                            </div>
                        ))}
                    </div>
                )}
            </div>

            {currentUser && (
               <div className="sidebar-card sandbox-card">
                    <h3>Network Sandbox 🔬</h3>
                    <p className="subtitle">Check relation with another user</p>
                    <select value={sandboxTarget} onChange={(e) => checkSandboxValidation(e.target.value)} className="user-switcher" style={{width: '100%', marginBottom: '10px'}}>
                        <option value="">Select target user...</option>
                        {users.filter(u=>u.id!==currentUser).map(u => <option key={u.id} value={u.id}>{u.name}</option>)}
                    </select>
                    
                    {sandboxData && (
                        <div className="sandbox-results">
                            <div className="stat-row">
                                <span>Structurally Connect:</span> 
                                <strong style={{color: sandboxData.connected ? '#3fb950' : '#f85149'}}>{sandboxData.connected ? 'YES' : 'NO'}</strong>
                            </div>
                            <div className="stat-row">
                                <span>Mutual Friends:</span> 
                                <strong>{sandboxData.mutuals.length}</strong>
                            </div>
                            <div className="mutuals-list">
                                {sandboxData.mutuals.map(m => <span key={m.id} className="mutual-pill">{m.name}</span>)}
                            </div>
                        </div>
                    )}
               </div> 
            )}
        </aside>

      </div>
    </div>
  );
}

export default App;
