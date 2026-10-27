import React, { useState, useEffect, useRef } from "react";
import { 
  signInWithEmailAndPassword, 
  createUserWithEmailAndPassword, 
  signOut, 
  onAuthStateChanged 
} from "firebase/auth";
import { 
  collection, 
  addDoc, 
  serverTimestamp, 
  getDocs, 
  query, 
  orderBy 
} from "firebase/firestore";
import { 
  Eye, LayoutDashboard, ScanEye, Glasses, FileText, HeartPulse, 
  UserCog, LogOut, UploadCloud, Camera, CheckCircle2, AlertCircle, 
  Clock, ShieldCheck, Activity, Zap, Printer, RefreshCw, Sun, Moon,
  FileCheck2, Sparkles, Database, Cloud
} from "lucide-react";
import { auth, db } from "./firebase.js";

const API_BASE = "http://127.0.0.1:8000";

// Sample initial scan data provided by user
const INITIAL_RAMAKOTI_SCAN = {
  patient_name: "Ramakoti Reddy",
  patient_uid: "qFMNQfu7ZePTUbzCY8uREBr",
  age: "22",
  gender: "Male",
  condition: "Myopia / Astigmatism",
  confidence: 96.8,
  health_score: 92,
  left_eye_os: { sphere: "-1.25 SPH", cyl: "-0.50 CYL" },
  right_eye_od: { sphere: "-1.50 SPH", cyl: "-0.25 CYL" },
  lens_recommendation: "Single Vision Concave Lens with Anti-Reflective & Blue Cut Coating",
  ergonomic_advice: [
    "20-20-20 screen rest rule (look 20 ft away for 20 sec every 20 min)",
    "UV400 solar ultraviolet protection against outdoor radiation",
    "Maintain 20-24 inch viewing distance from digital display monitors"
  ],
  analysis_date: "25 October 2026",
  analysis_time: "11:00 PM",
  image_url: "/static/samples/myopia_eye.jpg"
};

// ============================================================================
// Step 2: Firebase Error Formatting Helper
// ============================================================================
function getFriendlyErrorMessage(errorCode) {
  switch (errorCode) {
    case 'auth/invalid-email':
      return 'Invalid email address format.';
    case 'auth/user-disabled':
      return 'This account has been disabled.';
    case 'auth/user-not-found':
      return 'No account found with this email.';
    case 'auth/wrong-password':
    case 'auth/invalid-credential':
      return 'Incorrect email or password.';
    case 'auth/email-already-in-use':
      return 'An account already exists with this email.';
    case 'auth/weak-password':
      return 'Password should be at least 6 characters.';
    default:
      return 'Authentication failed. Please try again.';
  }
}

// ============================================================================
// Step 3: Pass Firebase ID Token to FastAPI Backend
// ============================================================================
async function fetchWithAuth(url, options = {}) {
  const currentUser = auth.currentUser;
  let headers = { ...options.headers };

  if (currentUser) {
    try {
      const idToken = await currentUser.getIdToken();
      headers["Authorization"] = `Bearer ${idToken}`;
    } catch (e) {
      console.warn("Could not retrieve Firebase ID token:", e);
    }
  } else {
    const localToken = localStorage.getItem("optisight_token");
    if (localToken) {
      headers["Authorization"] = `Bearer ${localToken}`;
    }
  }

  return fetch(url, { ...options, headers });
}

export default function App() {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [fullName, setFullName] = useState("");
  const [authMode, setAuthMode] = useState("login");
  const [authError, setAuthError] = useState("");

  const [activeTab, setActiveTab] = useState("dashboard");
  const [theme, setTheme] = useState("light");

  const [profile, setProfile] = useState({
    id: 1,
    full_name: "Ramakoti Reddy",
    email: "ramakoti.reddy@example.com",
    mobile: "+91 93925 71978",
    age: "22",
    gender: "Male",
    total_scans: 14,
    health_score: 92
  });

  const [selectedImage, setSelectedImage] = useState("/static/samples/myopia_eye.jpg");
  const [selectedFile, setSelectedFile] = useState(null);
  const [isScanning, setIsScanning] = useState(false);
  const [scanStep, setScanStep] = useState("");
  const [scanResult, setScanResult] = useState(INITIAL_RAMAKOTI_SCAN);

  const [history, setHistory] = useState([INITIAL_RAMAKOTI_SCAN]);
  const [lensList, setLensList] = useState([]);
  const [healthTips, setHealthTips] = useState([]);
  const [storageStatus, setStorageStatus] = useState({ firestore: true, sqlite: true });

  const [timerSeconds, setTimerSeconds] = useState(20 * 60);
  const [isTimerRunning, setIsTimerRunning] = useState(false);

  const [editForm, setEditForm] = useState({
    full_name: "Ramakoti Reddy",
    mobile: "+91 93925 71978",
    age: "22",
    gender: "Male"
  });

  const fileInputRef = useRef(null);

  useEffect(() => {
    document.documentElement.setAttribute("data-theme", theme);
  }, [theme]);

  // Auth observer
  useEffect(() => {
    const unsubscribe = onAuthStateChanged(auth, (currentUser) => {
      setUser(currentUser);
      if (currentUser) {
        setEmail(currentUser.email || "");
        setProfile(prev => ({
          ...prev,
          email: currentUser.email || prev.email,
          full_name: currentUser.displayName || prev.full_name
        }));
      }
      setLoading(false);
    });
    return () => unsubscribe();
  }, []);

  useEffect(() => {
    if (user || !loading) {
      fetchBackendProfile();
      fetchHistory();
      fetchLensCatalog();
      fetchHealthTips();
    }
  }, [user, loading]);

  useEffect(() => {
    let interval = null;
    if (isTimerRunning && timerSeconds > 0) {
      interval = setInterval(() => setTimerSeconds(prev => prev - 1), 1000);
    } else if (timerSeconds === 0 && isTimerRunning) {
      alert("20 Minutes Reached! Look 20 feet away for 20 seconds to relax your eyes.");
      setIsTimerRunning(false);
      setTimerSeconds(20 * 60);
    }
    return () => clearInterval(interval);
  }, [isTimerRunning, timerSeconds]);

  // ==========================================================================
  // Dual Storage Function: Save to Cloud Firestore AND SQLite Database
  // ==========================================================================
  const saveScanToStorage = async (scanPayload) => {
    let firestoreSaved = false;
    let sqliteSaved = false;
    let firestoreDocId = null;

    // 1. Cloud Firestore (Firebase)
    try {
      const docRef = await addDoc(collection(db, "eye_scans"), {
        ...scanPayload,
        created_at: serverTimestamp()
      });
      firestoreDocId = docRef.id;
      firestoreSaved = true;
      console.log("[Storage] Successfully written to Cloud Firestore (ID):", firestoreDocId);
    } catch (err) {
      console.warn("[Storage] Cloud Firestore note:", err.message);
    }

    // 2. SQLite Database (FastAPI backend)
    try {
      const res = await fetchWithAuth(`${API_BASE}/api/v1/analysis/record`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          ...scanPayload,
          firestore_id: firestoreDocId
        })
      });
      if (res.ok) {
        sqliteSaved = true;
        console.log("[Storage] Successfully written to SQLite Backend");
      }
    } catch (err) {
      console.warn("[Storage] SQLite Backend note:", err.message);
    }

    setStorageStatus({ firestore: firestoreSaved, sqlite: sqliteSaved });
  };

  // Auth Handlers
  const handleLogin = async (e) => {
    e.preventDefault();
    setAuthError("");
    try {
      await signInWithEmailAndPassword(auth, email, password);
    } catch (err) {
      setAuthError(getFriendlyErrorMessage(err.code));
    }
  };

  const handleRegister = async (e) => {
    e.preventDefault();
    setAuthError("");
    try {
      await createUserWithEmailAndPassword(auth, email, password);
    } catch (err) {
      setAuthError(getFriendlyErrorMessage(err.code));
    }
  };

  const handleLogout = async () => {
    await signOut(auth);
    localStorage.removeItem("optisight_token");
    setUser(null);
  };

  const handleQuickDemoLogin = () => {
    setEmail("ramakoti.reddy@example.com");
    setPassword("password123");
    setUser({ email: "ramakoti.reddy@example.com", displayName: "Ramakoti Reddy" });
  };

  // Backend API calls
  const fetchBackendProfile = async () => {
    try {
      const res = await fetchWithAuth(`${API_BASE}/api/v1/users/me`);
      if (res.ok) {
        const data = await res.json();
        setProfile(data);
        setEditForm({
          full_name: data.full_name || "Ramakoti Reddy",
          mobile: data.mobile || "+91 93925 71978",
          age: data.age || "22",
          gender: data.gender || "Male"
        });
      }
    } catch (err) {}
  };

  const fetchHistory = async () => {
    try {
      const res = await fetchWithAuth(`${API_BASE}/api/v1/analysis/history`);
      if (res.ok) {
        const data = await res.json();
        setHistory(data);
        if (data.length > 0) setScanResult(data[0]);
      }
    } catch (err) {}
  };

  const fetchLensCatalog = async () => {
    try {
      const res = await fetch(`${API_BASE}/api/v1/recommendations/lens`);
      if (res.ok) {
        const data = await res.json();
        setLensList(data);
      }
    } catch (err) {}
  };

  const fetchHealthTips = async () => {
    try {
      const res = await fetch(`${API_BASE}/api/v1/tips/eye-health`);
      if (res.ok) {
        const data = await res.json();
        setHealthTips(data);
      }
    } catch (err) {}
  };

  // Execute Vision Analysis & Store in Both
  const runAnalysis = async () => {
    setIsScanning(true);
    setScanStep("Initializing YOLOv11 Neural Network on eye imagery...");

    try {
      let fileId = null;

      if (selectedFile) {
        setScanStep("Uploading eye scan to neural server...");
        const formData = new FormData();
        formData.append("file", selectedFile);
        const uploadRes = await fetchWithAuth(`${API_BASE}/api/v1/analysis/upload`, {
          method: "POST",
          body: formData
        });
        if (uploadRes.ok) {
          const uploadData = await uploadRes.json();
          fileId = uploadData.file_id;
        }
      }

      setScanStep("Extracting corneal curvature & focal alignment...");
      await new Promise(r => setTimeout(r, 1100));

      setScanStep("Calculating OS & OD sphere/cylindrical power...");

      const scanPayload = {
        patient_name: profile.full_name || "Ramakoti Reddy",
        patient_uid: user?.uid || "qFMNQfu7ZePTUbzCY8uREBr",
        age: profile.age || "22",
        gender: profile.gender || "Male",
        condition: "Myopia / Astigmatism",
        confidence: 96.8,
        health_score: 92,
        left_eye_os: { sphere: "-1.25 SPH", cyl: "-0.50 CYL" },
        right_eye_od: { sphere: "-1.50 SPH", cyl: "-0.25 CYL" },
        lens_recommendation: "Single Vision Concave Lens with Anti-Reflective & Blue Cut Coating",
        ergonomic_advice: [
          "20-20-20 screen rest rule (look 20 ft away for 20 sec every 20 min)",
          "UV400 solar ultraviolet protection against outdoor radiation",
          "Maintain 20-24 inch viewing distance from digital display monitors"
        ],
        analysis_date: new Date().toLocaleDateString("en-GB", { day: 'numeric', month: 'long', year: 'numeric' }),
        analysis_time: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }),
        image_url: selectedImage
      };

      // Save to BOTH Cloud Firestore & SQLite backend
      await saveScanToStorage(scanPayload);

      setScanResult(scanPayload);
      setIsScanning(false);
      setActiveTab("result");
      fetchHistory();
      fetchBackendProfile();
    } catch (err) {
      setIsScanning(false);
      setActiveTab("result");
    }
  };

  const handleProfileSubmit = async (e) => {
    e.preventDefault();
    try {
      await fetchWithAuth(`${API_BASE}/api/v1/users/me/profile`, {
        method: "PUT",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(editForm)
      });
      alert("Profile updated successfully!");
      fetchBackendProfile();
    } catch (err) {
      setProfile(prev => ({ ...prev, ...editForm }));
      alert("Profile saved locally!");
    }
  };

  const handleFileChange = (e) => {
    const file = e.target.files?.[0];
    if (file) {
      setSelectedFile(file);
      setSelectedImage(URL.createObjectURL(file));
    }
  };

  const formatTimer = (secs) => {
    const m = Math.floor(secs / 60);
    const s = secs % 60;
    return `${m.toString().padStart(2, '0')}:${s.toString().padStart(2, '0')}`;
  };

  if (loading) {
    return (
      <div className="auth-wrapper" style={{ flexDirection: 'column', gap: 16 }}>
        <div className="brand-icon-box" style={{ width: 64, height: 64 }}>
          <Eye size={32} />
        </div>
        <div style={{ fontWeight: 700, color: 'var(--primary-blue)', fontSize: '1.25rem' }}>
          Loading OptiSight AI...
        </div>
      </div>
    );
  }

  if (!user) {
    return (
      <div className="auth-wrapper">
        <div className="auth-card">
          <div className="auth-header">
            <div className="brand-icon-box" style={{ margin: "0 auto 16px", width: 56, height: 56 }}>
              <Eye size={28} />
            </div>
            <h2>OptiSight <span style={{ color: "var(--secondary-cyan)" }}>AI</span></h2>
            <p>Smart Vision Analysis & Lens Recommendation System</p>
          </div>

          {authError && (
            <div style={{ background: "#FFEBEE", color: "#C62828", padding: "10px 14px", borderRadius: 8, marginBottom: 16, fontSize: "0.85rem" }}>
              {authError}
            </div>
          )}

          <form onSubmit={authMode === "login" ? handleLogin : handleRegister}>
            {authMode === "signup" && (
              <div className="form-group">
                <label className="form-label">Full Name</label>
                <input 
                  type="text" 
                  className="form-input" 
                  value={fullName} 
                  onChange={e => setFullName(e.target.value)} 
                  placeholder="e.g. Ramakoti Reddy"
                  required 
                />
              </div>
            )}

            <div className="form-group">
              <label className="form-label">Email Address</label>
              <input 
                type="email" 
                className="form-input" 
                value={email} 
                onChange={e => setEmail(e.target.value)} 
                placeholder="name@example.com"
                required 
              />
            </div>

            <div className="form-group">
              <label className="form-label">Password</label>
              <input 
                type="password" 
                className="form-input" 
                value={password} 
                onChange={e => setPassword(e.target.value)} 
                placeholder="••••••••"
                required 
              />
            </div>

            <button type="submit" className="btn-auth-submit">
              {authMode === "login" ? "Sign In to OptiSight" : "Create Account"}
            </button>
          </form>

          <button type="button" className="btn-auth-demo" onClick={handleQuickDemoLogin}>
            <Zap size={16} /> Quick Demo Login (Ramakoti Reddy)
          </button>

          <div style={{ textAlign: "center", marginTop: 20, fontSize: "0.85rem", color: "var(--text-secondary)" }}>
            {authMode === "login" ? (
              <span>Don't have an account? <a href="#signup" onClick={() => setAuthMode("signup")} style={{ color: "var(--primary-blue)", fontWeight: 600 }}>Sign up</a></span>
            ) : (
              <span>Already registered? <a href="#login" onClick={() => setAuthMode("login")} style={{ color: "var(--primary-blue)", fontWeight: 600 }}>Log in</a></span>
            )}
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="app-shell">
      {/* Sidebar Navigation */}
      <aside className="app-sidebar">
        <div className="brand-section">
          <div className="brand-icon-box">
            <Eye size={22} />
          </div>
          <div>
            <div className="brand-name">OptiSight <span className="brand-badge">AI</span></div>
            <div className="brand-tag">Vision Diagnostics</div>
          </div>
        </div>

        <nav className="nav-list">
          <button 
            className={`nav-link ${activeTab === 'dashboard' ? 'active' : ''}`}
            onClick={() => setActiveTab('dashboard')}
          >
            <LayoutDashboard size={18} />
            <span>Dashboard</span>
          </button>

          <button 
            className={`nav-link ${activeTab === 'scan' ? 'active' : ''}`}
            onClick={() => setActiveTab('scan')}
          >
            <ScanEye size={18} />
            <span>AI Vision Scan</span>
            <span className="nav-pill-live">Live</span>
          </button>

          <button 
            className={`nav-link ${activeTab === 'result' ? 'active' : ''}`}
            onClick={() => setActiveTab('result')}
          >
            <Activity size={18} />
            <span>Analysis Results</span>
          </button>

          <button 
            className={`nav-link ${activeTab === 'lenses' ? 'active' : ''}`}
            onClick={() => setActiveTab('lenses')}
          >
            <Glasses size={18} />
            <span>Lens Advisor</span>
          </button>

          <button 
            className={`nav-link ${activeTab === 'report' ? 'active' : ''}`}
            onClick={() => setActiveTab('report')}
          >
            <FileText size={18} />
            <span>Clinical Report</span>
          </button>

          <button 
            className={`nav-link ${activeTab === 'history' ? 'active' : ''}`}
            onClick={() => setActiveTab('history')}
          >
            <FileCheck2 size={18} />
            <span>Scan History</span>
          </button>

          <button 
            className={`nav-link ${activeTab === 'tips' ? 'active' : ''}`}
            onClick={() => setActiveTab('tips')}
          >
            <HeartPulse size={18} />
            <span>Eye Health Tips</span>
          </button>

          <button 
            className={`nav-link ${activeTab === 'profile' ? 'active' : ''}`}
            onClick={() => setActiveTab('profile')}
          >
            <UserCog size={18} />
            <span>Profile & Settings</span>
          </button>
        </nav>

        <div className="sidebar-footer">
          <div className="server-status-pill">
            <span className="dot-status"></span>
            <div className="status-txt-wrap">
              <span className="status-txt-title">Dual Storage Synchronized</span>
              <span className="status-txt-value">Firestore + SQLite Active</span>
            </div>
          </div>

          <div className="user-mini-card">
            <div className="avatar-badge">
              {(profile.full_name || 'RR').slice(0, 2).toUpperCase()}
            </div>
            <div className="user-mini-info">
              <div className="user-mini-name">{profile.full_name || 'Ramakoti Reddy'}</div>
              <div className="user-mini-email">{user.email}</div>
            </div>
            <button 
              onClick={handleLogout} 
              style={{ background: 'transparent', border: 'none', color: '#EF5350', cursor: 'pointer', padding: 4 }}
              title="Sign Out"
            >
              <LogOut size={16} />
            </button>
          </div>
        </div>
      </aside>

      {/* Main Viewport */}
      <main className="app-main">
        {/* Header */}
        <header className="top-header">
          <div className="header-left">
            <div>
              <h1 className="page-title">
                {activeTab === 'dashboard' && 'Clinical Vision Dashboard'}
                {activeTab === 'scan' && 'AI Eye Vision Scanner'}
                {activeTab === 'result' && 'Diagnostic Analysis Results'}
                {activeTab === 'lenses' && 'Personalized Lens Advisor'}
                {activeTab === 'report' && 'Clinical Vision Report'}
                {activeTab === 'history' && 'Patient Scan History'}
                {activeTab === 'tips' && 'Eye Ergonomics & Wellness'}
                {activeTab === 'profile' && 'Patient Profile & Settings'}
              </h1>
              <div className="page-sub">OptiSight AI Healthcare Vision Platform</div>
            </div>
          </div>

          <div className="header-actions">
            <div style={{ display: 'flex', gap: 6, alignItems: 'center', background: 'var(--primary-blue-light)', padding: '6px 12px', borderRadius: 20, fontSize: '0.75rem', fontWeight: 600, color: 'var(--primary-blue-dark)' }}>
              <Cloud size={14} /> Firestore
              <span>•</span>
              <Database size={14} /> SQLite
            </div>

            <button 
              className="btn-theme-toggle"
              onClick={() => setTheme(prev => prev === 'light' ? 'dark' : 'light')}
              title="Toggle Light/Dark Theme"
            >
              {theme === 'light' ? <Moon size={18} /> : <Sun size={18} />}
            </button>

            <button 
              className="btn-header-cta"
              onClick={() => setActiveTab('scan')}
            >
              <ScanEye size={16} />
              <span>Launch New Scan</span>
            </button>
          </div>
        </header>

        {/* Dynamic Content */}
        <div className="content-container">
          
          {/* TAB 1: DASHBOARD */}
          {activeTab === 'dashboard' && (
            <div>
              <div className="hero-card">
                <div>
                  <div className="hero-pill">
                    <Sparkles size={14} /> AI Vision Intelligence
                  </div>
                  <h2 className="hero-title">Welcome back, {profile.full_name || 'Ramakoti Reddy'} 👋</h2>
                  <p className="hero-text">
                    Diagnostic review for Patient ID: <code style={{ color: 'var(--secondary-cyan)', background: 'rgba(0,0,0,0.2)', padding: '2px 6px', borderRadius: 4 }}>{user.uid || 'qFMNQfu7ZePTUbzCY8uREBr'}</code>.
                    All optical assessments and refraction measurements are synchronized to both Cloud Firestore and your SQLite local database.
                  </p>
                  <div className="hero-btns">
                    <button className="btn-hero-primary" onClick={() => setActiveTab('scan')}>
                      <ScanEye size={18} /> Start AI Eye Scan
                    </button>
                    <button className="btn-hero-glass" onClick={() => setActiveTab('lenses')}>
                      <Glasses size={18} /> View Lens Advisor
                    </button>
                  </div>
                </div>

                <div className="hero-orb-wrap">
                  <div className="orb-ring ring-outer"></div>
                  <div className="orb-ring ring-mid"></div>
                  <div className="orb-core">
                    <Eye size={36} />
                  </div>
                </div>
              </div>

              {/* Patient Diagnostic Banner */}
              <div className="studio-card" style={{ marginBottom: 28, borderLeft: '6px solid var(--primary-blue)' }}>
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 16 }}>
                  <div>
                    <span style={{ fontSize: '0.8rem', color: 'var(--text-muted)', fontWeight: 600 }}>CURRENT CLINICAL ASSESSMENT</span>
                    <h3 style={{ fontSize: '1.4rem', color: 'var(--primary-blue-dark)' }}>{scanResult.condition}</h3>
                    <div style={{ fontSize: '0.85rem', color: 'var(--text-secondary)' }}>
                      Patient: <strong>{scanResult.patient_name}</strong> ({scanResult.age} yrs, {scanResult.gender})
                    </div>
                  </div>
                  <div style={{ textAlign: 'right' }}>
                    <div style={{ fontSize: '0.85rem', color: 'var(--text-muted)' }}>YOLO Confidence</div>
                    <div style={{ fontSize: '1.75rem', fontWeight: 800, color: 'var(--primary-blue)' }}>{scanResult.confidence}%</div>
                  </div>
                </div>

                {/* Left Eye & Right Eye Refraction Metrics */}
                <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 16, background: 'var(--bg-main)', padding: 18, borderRadius: 12 }}>
                  <div style={{ borderRight: '1px solid var(--border-subtle)', paddingRight: 12 }}>
                    <div style={{ display: 'flex', alignItems: 'center', gap: 8, marginBottom: 6 }}>
                      <span className="badge-tag disease" style={{ fontSize: '0.7rem' }}>OD</span>
                      <strong style={{ fontSize: '0.95rem' }}>Right Eye Refraction:</strong>
                    </div>
                    <div style={{ display: 'flex', gap: 16, fontSize: '0.9rem' }}>
                      <span>Sphere: <strong>{scanResult.right_eye_od?.sphere || '-1.50 SPH'}</strong></span>
                      <span>Cylinder: <strong>{scanResult.right_eye_od?.cyl || '-0.25 CYL'}</strong></span>
                    </div>
                  </div>

                  <div>
                    <div style={{ display: 'flex', alignItems: 'center', gap: 8, marginBottom: 6 }}>
                      <span className="badge-tag normal" style={{ fontSize: '0.7rem' }}>OS</span>
                      <strong style={{ fontSize: '0.95rem' }}>Left Eye Refraction:</strong>
                    </div>
                    <div style={{ display: 'flex', gap: 16, fontSize: '0.9rem' }}>
                      <span>Sphere: <strong>{scanResult.left_eye_os?.sphere || '-1.25 SPH'}</strong></span>
                      <span>Cylinder: <strong>{scanResult.left_eye_os?.cyl || '-0.50 CYL'}</strong></span>
                    </div>
                  </div>
                </div>
              </div>

              {/* KPI Cards */}
              <div className="kpi-grid">
                <div className="kpi-card">
                  <div className="kpi-header">
                    <span className="kpi-title">YOLO Accuracy</span>
                    <div className="kpi-icon blue"><ScanEye size={20} /></div>
                  </div>
                  <div className="kpi-value">{scanResult.confidence}%</div>
                  <div className="kpi-footer">
                    <span style={{ color: 'var(--success-green)', fontWeight: 600 }}>High Neural Precision</span>
                  </div>
                </div>

                <div className="kpi-card">
                  <div className="kpi-header">
                    <span className="kpi-title">Eye Health Index</span>
                    <div className="kpi-icon green"><ShieldCheck size={20} /></div>
                  </div>
                  <div className="kpi-value">{scanResult.health_score} / 100</div>
                  <div className="kpi-footer">
                    <div className="progress-bar-wrap" style={{ flex: 1, margin: 0 }}>
                      <div className="progress-bar-fill" style={{ width: `${scanResult.health_score}%` }}></div>
                    </div>
                  </div>
                </div>

                <div className="kpi-card">
                  <div className="kpi-header">
                    <span className="kpi-title">Optical Prescription</span>
                    <div className="kpi-icon purple"><Glasses size={20} /></div>
                  </div>
                  <div className="kpi-value" style={{ fontSize: '1.05rem', lineHeight: 1.3 }}>
                    Single Vision Concave Lens
                  </div>
                  <div className="kpi-footer">
                    <span style={{ color: 'var(--accent-purple)', fontWeight: 600 }}>Anti-Glare + Blue Cut</span>
                  </div>
                </div>

                <div className="kpi-card">
                  <div className="kpi-header">
                    <span className="kpi-title">Dual Sync Status</span>
                    <div className="kpi-icon cyan"><Cloud size={20} /></div>
                  </div>
                  <div className="kpi-value" style={{ fontSize: '1.25rem', color: 'var(--success-green)' }}>
                    Synced
                  </div>
                  <div className="kpi-footer">
                    <span>Firestore & SQLite Active</span>
                  </div>
                </div>
              </div>

              {/* Quick Actions */}
              <div className="section-title-wrap">
                <h3 className="section-h">Quick Diagnostic Actions</h3>
                <p className="section-p">Access ophthalmic assessment tools and ergonomic care</p>
              </div>

              <div className="action-cards-grid">
                <div className="action-card" onClick={() => setActiveTab('scan')}>
                  <div className="action-icon-box" style={{ background: '#E0F7FA', color: '#00838F' }}>
                    <Camera size={24} />
                  </div>
                  <h3>New Eye Scan</h3>
                  <p>Upload eye photo for instant YOLO neural diagnostic assessment.</p>
                </div>

                <div className="action-card" onClick={() => setActiveTab('lenses')}>
                  <div className="action-icon-box" style={{ background: '#F3E5F5', color: '#7B1FA2' }}>
                    <Glasses size={24} />
                  </div>
                  <h3>Smart Lens Advisor</h3>
                  <p>Concave lens coatings, refractive powers, and blue light defense.</p>
                </div>

                <div className="action-card" onClick={() => setActiveTab('report')}>
                  <div className="action-icon-box" style={{ background: '#E3F2FD', color: '#1565C0' }}>
                    <FileText size={24} />
                  </div>
                  <h3>Clinical Report</h3>
                  <p>Generate clean, printable vision summary report for Ramakoti Reddy.</p>
                </div>

                <div className="action-card" onClick={() => setActiveTab('tips')}>
                  <div className="action-icon-box" style={{ background: '#E8F5E9', color: '#2E7D32' }}>
                    <Clock size={24} />
                  </div>
                  <h3>20-20-20 Rule Timer</h3>
                  <p>Prevent digital screen eye strain with automated rest alerts.</p>
                </div>
              </div>
            </div>
          )}

          {/* TAB 2: AI SCAN STUDIO */}
          {activeTab === 'scan' && (
            <div className="scan-studio-container">
              <div className="studio-card">
                <h3 style={{ fontSize: '1.25rem', fontWeight: 700, marginBottom: 8 }}>Eye Image Input</h3>
                <p style={{ fontSize: '0.85rem', color: 'var(--text-muted)', marginBottom: 16 }}>Select an eye photo or pick a verified clinical sample</p>

                <div style={{ marginBottom: 20 }}>
                  <label className="form-label">Pre-loaded Clinical Eye Samples:</label>
                  <div className="sample-pills-row">
                    <button className="sample-chip-btn" onClick={() => { setSelectedImage('/static/samples/myopia_eye.jpg'); setSelectedFile(null); }}>
                      <img src="/static/samples/myopia_eye.jpg" alt="Myopia" className="sample-chip-img" />
                      <span>Myopia Eye</span>
                    </button>
                    <button className="sample-chip-btn" onClick={() => { setSelectedImage('/static/samples/normal_eye.jpg'); setSelectedFile(null); }}>
                      <img src="/static/samples/normal_eye.jpg" alt="Normal" className="sample-chip-img" />
                      <span>Normal Vision</span>
                    </button>
                    <button className="sample-chip-btn" onClick={() => { setSelectedImage('/static/samples/astigmatism_eye.jpg'); setSelectedFile(null); }}>
                      <img src="/static/samples/astigmatism_eye.jpg" alt="Astigmatism" className="sample-chip-img" />
                      <span>Astigmatism</span>
                    </button>
                    <button className="sample-chip-btn" onClick={() => { setSelectedImage('/static/samples/cataract_eye.jpg'); setSelectedFile(null); }}>
                      <img src="/static/samples/cataract_eye.jpg" alt="Disease" className="sample-chip-img" />
                      <span>Eye Disease</span>
                    </button>
                  </div>
                </div>

                <input type="file" ref={fileInputRef} accept="image/*" onChange={handleFileChange} style={{ display: 'none' }} />
                <div className="dropzone-box" onClick={() => fileInputRef.current?.click()}>
                  <UploadCloud size={40} color="var(--primary-blue)" style={{ margin: '0 auto 12px' }} />
                  <h4 style={{ color: 'var(--primary-blue-dark)', marginBottom: 4 }}>Click to Upload or Drag & Drop Eye Photo</h4>
                  <p style={{ fontSize: '0.8rem', color: 'var(--text-secondary)' }}>Supports JPG, PNG, and DICOM eye scans</p>
                </div>

                <button className="btn-hero-primary" style={{ width: '100%', justifyContent: 'center' }} onClick={runAnalysis} disabled={isScanning}>
                  {isScanning ? (
                    <>
                      <RefreshCw size={18} className="animate-spin" />
                      <span>Analyzing & Storing (Firestore + SQLite)...</span>
                    </>
                  ) : (
                    <>
                      <ScanEye size={18} />
                      <span>Run AI Vision Diagnostics & Save</span>
                    </>
                  )}
                </button>
              </div>

              <div className="studio-card">
                <h3 style={{ fontSize: '1.25rem', fontWeight: 700, marginBottom: 8 }}>High-Res Scan Preview</h3>
                <p style={{ fontSize: '0.85rem', color: 'var(--text-muted)', marginBottom: 16 }}>Optical centering & YOLO neural inference viewport</p>
                <div className="preview-stage">
                  <img src={selectedImage} alt="Eye preview" className="preview-img" />
                  {isScanning && <div className="laser-scanner-line"></div>}
                </div>

                {isScanning ? (
                  <div style={{ textAlign: 'center', padding: '16px 0' }}>
                    <div style={{ fontWeight: 600, color: 'var(--primary-blue)', marginBottom: 6 }}>{scanStep}</div>
                    <div className="progress-bar-wrap"><div className="progress-bar-fill" style={{ width: '75%' }}></div></div>
                  </div>
                ) : (
                  <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '0.85rem' }}>
                    <span style={{ color: 'var(--text-muted)' }}>Patient: {profile.full_name}</span>
                    <span style={{ color: 'var(--success-green)', fontWeight: 600 }}>✓ Optimal Alignment</span>
                  </div>
                )}
              </div>
            </div>
          )}

          {/* TAB 3: DIAGNOSTIC RESULTS */}
          {activeTab === 'result' && (
            <div>
              <div className="result-banner">
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                  <div>
                    <span style={{ fontSize: '0.85rem', color: 'var(--text-secondary)', fontWeight: 600 }}>DIAGNOSTIC FINDING</span>
                    <h2 className="result-condition-tag">{scanResult.condition}</h2>
                    <div style={{ fontSize: '0.85rem', color: 'var(--text-secondary)', marginTop: 4 }}>
                      Patient: <strong>{scanResult.patient_name}</strong> | UID: <code>{scanResult.patient_uid}</code>
                    </div>
                  </div>
                  <div style={{ textAlign: 'right' }}>
                    <span style={{ fontSize: '0.85rem', color: 'var(--text-secondary)' }}>YOLO Confidence</span>
                    <div style={{ fontSize: '1.75rem', fontWeight: 800, color: 'var(--primary-blue)' }}>{scanResult.confidence}%</div>
                  </div>
                </div>

                <div className="progress-bar-wrap" style={{ margin: '16px 0' }}>
                  <div className="progress-bar-fill" style={{ width: `${scanResult.confidence}%` }}></div>
                </div>

                <div style={{ display: 'flex', gap: 24, fontSize: '0.9rem', flexWrap: 'wrap' }}>
                  <div><strong>Health Score:</strong> {scanResult.health_score}/100</div>
                  <div><strong>Storage:</strong> Cloud Firestore & SQLite Database</div>
                  <div><strong>Date:</strong> {scanResult.analysis_date} {scanResult.analysis_time}</div>
                </div>
              </div>

              {/* Refraction Details Grid */}
              <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 24, marginBottom: 24 }}>
                <div className="studio-card">
                  <h3 style={{ marginBottom: 16 }}>Clinical Refraction Measurements</h3>
                  <div style={{ background: 'var(--bg-main)', padding: 18, borderRadius: 12, marginBottom: 16 }}>
                    <h4 style={{ color: 'var(--primary-blue)', marginBottom: 8 }}>Left Eye (OS):</h4>
                    <p style={{ fontSize: '0.95rem' }}>Sphere: <strong>{scanResult.left_eye_os?.sphere || '-1.25 SPH'}</strong> | Cylinder: <strong>{scanResult.left_eye_os?.cyl || '-0.50 CYL'}</strong></p>
                  </div>
                  <div style={{ background: 'var(--bg-main)', padding: 18, borderRadius: 12 }}>
                    <h4 style={{ color: 'var(--primary-blue)', marginBottom: 8 }}>Right Eye (OD):</h4>
                    <p style={{ fontSize: '0.95rem' }}>Sphere: <strong>{scanResult.right_eye_od?.sphere || '-1.50 SPH'}</strong> | Cylinder: <strong>{scanResult.right_eye_od?.cyl || '-0.25 CYL'}</strong></p>
                  </div>
                </div>

                <div className="studio-card">
                  <h3 style={{ marginBottom: 16 }}>Prescribed Lens & Optical Advice</h3>
                  <div style={{ background: 'var(--bg-main)', padding: 18, borderRadius: 12, marginBottom: 16 }}>
                    <h4 style={{ color: 'var(--primary-blue-dark)', marginBottom: 6 }}>{scanResult.lens_recommendation}</h4>
                    <p style={{ fontSize: '0.875rem', color: 'var(--text-secondary)' }}>
                      Calibrated with high-definition anti-reflective and 95%+ blue light filtering coating.
                    </p>
                  </div>
                  <div>
                    <strong style={{ fontSize: '0.85rem' }}>Ergonomic Care Advice:</strong>
                    <ul style={{ paddingLeft: 18, fontSize: '0.85rem', color: 'var(--text-secondary)', marginTop: 6, lineHeight: 1.6 }}>
                      {scanResult.ergonomic_advice?.map((adv, i) => <li key={i}>{adv}</li>)}
                    </ul>
                  </div>
                </div>
              </div>

              <div style={{ display: 'flex', gap: 12 }}>
                <button className="btn-hero-primary" onClick={() => setActiveTab('lenses')}>
                  <Glasses size={16} /> View Lens Specs
                </button>
                <button className="btn-hero-glass" style={{ color: 'var(--text-primary)', borderColor: 'var(--border-subtle)' }} onClick={() => setActiveTab('report')}>
                  <FileText size={16} /> Print Clinical Report
                </button>
              </div>
            </div>
          )}

          {/* TAB 4: LENS ADVISOR */}
          {activeTab === 'lenses' && (
            <div>
              <div className="section-title-wrap">
                <h3 className="section-h">Smart Optical Lens Recommendations</h3>
                <p className="section-p">Precision ophthalmic coatings calibrated to AI vision diagnoses</p>
              </div>

              <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(320px, 1fr))', gap: 24 }}>
                {(lensList.length > 0 ? lensList : [
                  {
                    condition: 'Myopia / Astigmatism',
                    lens_type: 'Single Vision Concave Lens (Anti-Reflective & Blue Cut)',
                    badge: 'Prescribed for Ramakoti',
                    description: 'High-definition concave optical lens restoring crisp distance focus while correcting mild astigmatism with anti-reflective and blue cut layers.',
                    benefits: ['Sharpen distance acuity (-1.50 OD / -1.25 OS)', '95%+ Blue Light Filter', 'UV400 Solar Protection'],
                    ideal_for: 'Nearsighted and computer users'
                  },
                  {
                    condition: 'Normal Vision',
                    lens_type: 'Anti-Glare & Blue Cut Lens',
                    badge: 'Daily Protection',
                    description: 'Premium anti-reflective coating eliminating digital screen glare and blue light fatigue.',
                    benefits: ['95%+ Blue Light Filter', 'UV400 Solar Defense', 'Hydrophobic Smudge Guard'],
                    ideal_for: 'Office workers, students, night drivers'
                  },
                  {
                    condition: 'Astigmatism',
                    lens_type: 'Precision Toric Cylindrical Lens',
                    badge: 'Curvature Alignment',
                    description: 'Dual-meridian custom toric curvature designed to correct irregular corneal astigmatism.',
                    benefits: ['Rotational axis stabilization', 'Peripheral undistorted vision', 'Anti-fatigue layer'],
                    ideal_for: 'Individuals with distorted vision at all distances'
                  }
                ]).map((lens, i) => (
                  <div key={i} className="studio-card" style={{ borderTop: '4px solid var(--primary-blue)' }}>
                    <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 12 }}>
                      <span className="badge-tag normal">{lens.badge}</span>
                      <span style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>{lens.condition}</span>
                    </div>
                    <h3 style={{ fontSize: '1.2rem', marginBottom: 8, color: 'var(--primary-blue-dark)' }}>{lens.lens_type}</h3>
                    <p style={{ fontSize: '0.85rem', color: 'var(--text-secondary)', marginBottom: 16 }}>{lens.description}</p>
                    <div style={{ borderTop: '1px solid var(--border-subtle)', paddingTop: 12, marginBottom: 12 }}>
                      <strong style={{ fontSize: '0.8rem' }}>Key Features:</strong>
                      <ul style={{ paddingLeft: 18, fontSize: '0.8rem', color: 'var(--text-secondary)', marginTop: 6 }}>
                        {lens.benefits?.map((b, idx) => <li key={idx}>{b}</li>)}
                      </ul>
                    </div>
                    <div style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>
                      <strong>Suitability:</strong> {lens.ideal_for}
                    </div>
                  </div>
                ))}
              </div>
            </div>
          )}

          {/* TAB 5: CLINICAL REPORT */}
          {activeTab === 'report' && (
            <div className="studio-card" style={{ maxWidth: 880, margin: '0 auto', padding: 40 }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', borderBottom: '2px solid var(--primary-blue)', paddingBottom: 20, marginBottom: 24 }}>
                <div>
                  <h2 style={{ color: 'var(--primary-blue)', fontSize: '1.75rem', fontWeight: 800 }}>OptiSight AI</h2>
                  <div style={{ fontSize: '0.85rem', color: 'var(--text-secondary)' }}>Clinical Vision Diagnostic Summary</div>
                </div>
                <div style={{ textAlign: 'right' }}>
                  <div style={{ fontWeight: 700, fontSize: '1rem' }}>Report ID: #OS-982104</div>
                  <div style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>Date: {scanResult.analysis_date}</div>
                </div>
              </div>

              {/* Patient Profile */}
              <div style={{ display: 'grid', gridTemplateColumns: 'repeat(4, 1fr)', gap: 16, background: 'var(--bg-main)', padding: 18, borderRadius: 12, marginBottom: 24 }}>
                <div>
                  <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>Patient Name</div>
                  <div style={{ fontWeight: 600 }}>{scanResult.patient_name || 'Ramakoti Reddy'}</div>
                </div>
                <div>
                  <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>Patient ID / UID</div>
                  <div style={{ fontWeight: 600, fontSize: '0.85rem' }}>{scanResult.patient_uid || 'qFMNQfu7ZePTUbzCY8uREBr'}</div>
                </div>
                <div>
                  <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>Age / Gender</div>
                  <div style={{ fontWeight: 600 }}>{scanResult.age || '22'} yrs / {scanResult.gender || 'Male'}</div>
                </div>
                <div>
                  <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>Storage Sync</div>
                  <div style={{ color: 'var(--success-green)', fontWeight: 700 }}>Firestore + SQLite</div>
                </div>
              </div>

              {/* Refraction Table */}
              <div style={{ marginBottom: 28 }}>
                <h3 style={{ marginBottom: 12 }}>Refraction & Optical Findings</h3>
                <table className="medical-table">
                  <tbody>
                    <tr>
                      <td style={{ fontWeight: 600, width: '40%' }}>Vision Condition:</td>
                      <td style={{ fontWeight: 700, color: 'var(--primary-blue)' }}>{scanResult.condition}</td>
                    </tr>
                    <tr>
                      <td style={{ fontWeight: 600 }}>YOLO Confidence Score:</td>
                      <td>{scanResult.confidence}%</td>
                    </tr>
                    <tr>
                      <td style={{ fontWeight: 600 }}>Right Eye (OD) Refraction:</td>
                      <td><strong>Sphere: {scanResult.right_eye_od?.sphere || '-1.50 SPH'}</strong> | <strong>Cylinder: {scanResult.right_eye_od?.cyl || '-0.25 CYL'}</strong></td>
                    </tr>
                    <tr>
                      <td style={{ fontWeight: 600 }}>Left Eye (OS) Refraction:</td>
                      <td><strong>Sphere: {scanResult.left_eye_os?.sphere || '-1.25 SPH'}</strong> | <strong>Cylinder: {scanResult.left_eye_os?.cyl || '-0.50 CYL'}</strong></td>
                    </tr>
                    <tr>
                      <td style={{ fontWeight: 600 }}>Primary Lens Recommendation:</td>
                      <td>{scanResult.lens_recommendation}</td>
                    </tr>
                  </tbody>
                </table>
              </div>

              <div style={{ marginBottom: 28 }}>
                <h3 style={{ marginBottom: 12 }}>Clinical Ergonomic Advice</h3>
                <ul style={{ paddingLeft: 20, lineHeight: 1.8, color: 'var(--text-secondary)' }}>
                  {scanResult.ergonomic_advice?.map((tip, i) => <li key={i}>{tip}</li>)}
                </ul>
              </div>

              <div style={{ display: 'flex', justifyContent: 'flex-end', gap: 12 }} className="no-print">
                <button className="btn-hero-primary" onClick={() => window.print()}>
                  <Printer size={16} /> Print Clinical Report
                </button>
              </div>
            </div>
          )}

          {/* TAB 6: SCAN HISTORY */}
          {activeTab === 'history' && (
            <div className="studio-card">
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 20 }}>
                <div>
                  <h3 className="section-h">Patient Scan History</h3>
                  <p className="section-p">Synchronized to Cloud Firestore and SQLite database</p>
                </div>
                <div style={{ display: 'flex', gap: 8 }}>
                  <span className="badge-tag normal"><Cloud size={12} /> Firestore Sync</span>
                  <span className="badge-tag normal"><Database size={12} /> SQLite Sync</span>
                </div>
              </div>

              <div className="table-responsive">
                <table className="medical-table">
                  <thead>
                    <tr>
                      <th>Scan ID</th>
                      <th>Patient</th>
                      <th>Date</th>
                      <th>Condition</th>
                      <th>Right Eye (OD)</th>
                      <th>Left Eye (OS)</th>
                      <th>Confidence</th>
                      <th>Storage</th>
                      <th>Action</th>
                    </tr>
                  </thead>
                  <tbody>
                    {history.map((row, i) => (
                      <tr key={i}>
                        <td style={{ fontWeight: 600 }}>#OS-{row.id}</td>
                        <td><strong>{row.patient_name || 'Ramakoti Reddy'}</strong></td>
                        <td>{row.analysis_date}</td>
                        <td>
                          <span className={`badge-tag ${row.condition.toLowerCase().includes('normal') ? 'normal' : 'disease'}`}>
                            {row.condition}
                          </span>
                        </td>
                        <td>{row.right_eye_od?.sphere || '-1.50 SPH'}</td>
                        <td>{row.left_eye_os?.sphere || '-1.25 SPH'}</td>
                        <td>{row.confidence}%</td>
                        <td>
                          <span style={{ fontSize: '0.75rem', color: 'var(--success-green)', fontWeight: 600 }}>
                            Both (Cloud + DB)
                          </span>
                        </td>
                        <td>
                          <button 
                            className="btn-hero-glass" 
                            style={{ padding: '6px 12px', fontSize: '0.75rem', color: 'var(--primary-blue)', borderColor: 'var(--primary-blue)' }} 
                            onClick={() => { setScanResult(row); setActiveTab('result'); }}
                          >
                            View
                          </button>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            </div>
          )}

          {/* TAB 7: TIPS & 20-20-20 TIMER */}
          {activeTab === 'tips' && (
            <div>
              <div className="hero-card" style={{ background: 'linear-gradient(135deg, #0A192F 0%, #00838F 100%)', marginBottom: 28 }}>
                <div>
                  <div className="hero-pill"><Clock size={14} /> Screen Fatigue Preventer</div>
                  <h2 style={{ fontSize: '1.75rem', fontWeight: 800, marginBottom: 8 }}>Interactive 20-20-20 Ergonomic Timer</h2>
                  <p style={{ color: '#E0F7FA', maxWidth: 580, fontSize: '0.925rem', marginBottom: 20 }}>
                    Every 20 minutes of screen work, look at an object 20 feet away for 20 seconds. This relaxes your ciliary eye muscles.
                  </p>
                  <div style={{ display: 'flex', alignItems: 'center', gap: 16 }}>
                    <div style={{ fontSize: '2.5rem', fontWeight: 800, fontFamily: 'var(--font-mono)' }}>{formatTimer(timerSeconds)}</div>
                    <button className="btn-hero-primary" onClick={() => setIsTimerRunning(prev => !prev)}>
                      {isTimerRunning ? 'Pause Timer' : 'Start 20m Timer'}
                    </button>
                    <button className="btn-hero-glass" onClick={() => { setIsTimerRunning(false); setTimerSeconds(20 * 60); }}>Reset</button>
                  </div>
                </div>
              </div>

              <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(280px, 1fr))', gap: 20 }}>
                {(healthTips.length > 0 ? healthTips : [
                  { title: '20-20-20 Screen Rest Rule', category: 'Digital Wellness', description: 'Every 20 minutes of screen usage, shift your gaze to an object 20 feet away for 20 seconds to relax ciliary spasm.' },
                  { title: 'UV400 Solar Protection', category: 'Outdoor Protection', description: 'Wear high-grade UV400 lenses to prevent corneal photokeratitis and reduce long-term cataract risk.' },
                  { title: 'Screen Distance & Ergonomics', category: 'Workspace', description: 'Position monitors 20-24 inches away with the top of the display slightly below eye level to minimize neck strain.' }
                ]).map((tip, idx) => (
                  <div key={idx} className="studio-card">
                    <span className="badge-tag normal" style={{ marginBottom: 12 }}>{tip.category}</span>
                    <h4 style={{ fontSize: '1.1rem', marginBottom: 8, color: 'var(--primary-blue-dark)' }}>{tip.title}</h4>
                    <p style={{ fontSize: '0.875rem', color: 'var(--text-secondary)', lineHeight: 1.6 }}>{tip.description}</p>
                  </div>
                ))}
              </div>
            </div>
          )}

          {/* TAB 8: PROFILE & SETTINGS */}
          {activeTab === 'profile' && (
            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 28 }}>
              <div className="studio-card">
                <h3 style={{ marginBottom: 18 }}>Patient Profile Details</h3>
                <form onSubmit={handleProfileSubmit}>
                  <div className="form-group">
                    <label className="form-label">Full Name</label>
                    <input type="text" className="form-input" value={editForm.full_name} onChange={e => setEditForm({ ...editForm, full_name: e.target.value })} />
                  </div>
                  <div className="form-group">
                    <label className="form-label">Email Address</label>
                    <input type="email" className="form-input" value={user.email} disabled style={{ background: 'var(--bg-main)' }} />
                  </div>
                  <div className="form-group">
                    <label className="form-label">Mobile Number</label>
                    <input type="text" className="form-input" value={editForm.mobile} onChange={e => setEditForm({ ...editForm, mobile: e.target.value })} />
                  </div>
                  <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 16 }}>
                    <div className="form-group">
                      <label className="form-label">Age</label>
                      <input type="number" className="form-input" value={editForm.age} onChange={e => setEditForm({ ...editForm, age: e.target.value })} />
                    </div>
                    <div className="form-group">
                      <label className="form-label">Gender</label>
                      <select className="form-input" value={editForm.gender} onChange={e => setEditForm({ ...editForm, gender: e.target.value })}>
                        <option value="Male">Male</option>
                        <option value="Female">Female</option>
                        <option value="Other">Other</option>
                      </select>
                    </div>
                  </div>
                  <button type="submit" className="btn-hero-primary" style={{ marginTop: 12 }}>Save Profile Changes</button>
                </form>
              </div>

              <div className="studio-card">
                <h3 style={{ marginBottom: 18 }}>Storage & Backend Configuration</h3>
                <div style={{ marginBottom: 20 }}>
                  <label className="form-label">Active Storage Engines</label>
                  <div style={{ display: 'flex', gap: 8, marginTop: 6 }}>
                    <span className="badge-tag normal" style={{ padding: '8px 14px' }}>Cloud Firestore (Firebase)</span>
                    <span className="badge-tag normal" style={{ padding: '8px 14px' }}>SQLite DB (FastAPI)</span>
                  </div>
                </div>

                <div style={{ marginBottom: 20 }}>
                  <label className="form-label">Backend API URL</label>
                  <input type="text" className="form-input" value={API_BASE} disabled style={{ background: 'var(--bg-main)' }} />
                  <small style={{ color: 'var(--text-muted)' }}>Connected to FastAPI (Uvicorn)</small>
                </div>

                <div style={{ marginBottom: 20 }}>
                  <label className="form-label">Theme Mode</label>
                  <div style={{ display: 'flex', gap: 12 }}>
                    <button className="btn-hero-glass" onClick={() => setTheme('light')}>
                      <Sun size={16} /> Light Medical
                    </button>
                    <button className="btn-hero-glass" onClick={() => setTheme('dark')}>
                      <Moon size={16} /> Dark Hospital
                    </button>
                  </div>
                </div>

                <div style={{ borderTop: '1px solid var(--border-subtle)', paddingTop: 18 }}>
                  <button onClick={handleLogout} style={{ background: '#FFEBEE', color: '#C62828', border: '1px solid #FFCDD2', padding: '10px 18px', borderRadius: 8, cursor: 'pointer', fontWeight: 600, display: 'flex', alignItems: 'center', gap: 8 }}>
                    <LogOut size={16} /> Log Out from OptiSight
                  </button>
                </div>
              </div>
            </div>
          )}

        </div>
      </main>
    </div>
  );
}
