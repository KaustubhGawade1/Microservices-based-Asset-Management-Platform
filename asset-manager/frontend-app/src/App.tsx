import React, { useState, useEffect, useCallback, useRef } from 'react';
import {
  LayoutDashboard,
  FolderOpen,
  Shield,
  LogOut,
  Upload,
  File,
  MoreVertical,
  Trash2,
  Eye,
  EyeOff,
  Search,
  Menu,
  X,
  User,
  CheckCircle,
  AlertCircle,
  Loader2,
  HardDrive,
  FileText,
  Image as ImageIcon,
  Users,
  UserPlus,
  Check,
  CreditCard,
  Mail,
  BadgeCheck,
  Calendar
} from 'lucide-react';

// --- Types & Interfaces ---

interface UserProfile {
  id?: number;
  username: string;
  fullName?: string;
  email?: string;
  roles: string[];
  joinedAt?: string;
}

interface UserRequest {
  id: string;
  username: string;
  email: string;
  status: 'pending' | 'approved' | 'rejected';
  requestedAt: string;
}

interface Asset {
  id: string;
  filename: string;
  owner: string;
  uploadedById?: number;
  size: string;
  isPublic: boolean;
  type: 'image' | 'document' | 'other';
  url?: string;
  createdAt: string;
}

type ViewState = 'my-assets' | 'public-assets' | 'admin-console' | 'profile';
type AuthState = 'login' | 'register' | 'authenticated';
type AdminTab = 'files' | 'users';

// --- Configuration ---

const API_URLS = {
  AUTH: 'http://localhost:8081/api/auth',
  ASSET: 'http://localhost:8085/api',
  ADMIN: 'http://localhost:8089/api/admin',
};

// --- Mock Data (For Demo Mode) ---

const MOCK_USER: UserProfile = {
  username: 'admin_user',
  fullName: 'System Administrator',
  roles: ['USER', 'ROLE_ADMIN'],
  email: 'admin@corp.net',
  joinedAt: '2023-01-15'
};

const MOCK_ALL_USERS: UserProfile[] = [
  { username: 'admin_user', fullName: 'System Admin', roles: ['USER', 'ROLE_ADMIN'], email: 'admin@corp.net', joinedAt: '2023-01-15' },
  { username: 'john_doe', fullName: 'John Doe', roles: ['USER'], email: 'john@corp.net', joinedAt: '2023-03-10' },
  { username: 'jane_smith', fullName: 'Jane Smith', roles: ['USER'], email: 'jane@corp.net', joinedAt: '2023-04-22' }
];

const MOCK_REQUESTS: UserRequest[] = [
  { id: '101', username: 'intern_dave', email: 'dave@corp.net', status: 'pending', requestedAt: '2023-10-30' },
  { id: '102', username: 'contractor_bob', email: 'bob@external.com', status: 'pending', requestedAt: '2023-10-31' }
];

const MOCK_ASSETS: Asset[] = [
  { id: '1', filename: 'Q4_Financial_Report.pdf', owner: 'admin_user', size: '2.4 MB', isPublic: false, type: 'document', createdAt: '2023-10-24' },
  { id: '2', filename: 'Project_Alpha_Logo.png', owner: 'admin_user', size: '450 KB', isPublic: true, type: 'image', createdAt: '2023-10-25' },
  { id: '3', filename: 'Meeting_Notes.txt', owner: 'john_doe', size: '12 KB', isPublic: true, type: 'document', createdAt: '2023-10-26' },
  { id: '4', filename: 'System_Architecture.png', owner: 'jane_smith', size: '1.2 MB', isPublic: false, type: 'image', createdAt: '2023-10-27' },
  { id: '5', filename: 'Backup_Config.json', owner: 'admin_user', size: '4 KB', isPublic: false, type: 'other', createdAt: '2023-10-28' },
];

// --- API Utility (Simulating Axios Interceptor) ---

const useApi = (isDemo: boolean, logout: () => void) => {
  const [tokens, setTokens] = useState({ access: '', refresh: '' });

  const apiCall = useCallback(async (endpoint: string, options: RequestInit = {}, service: 'AUTH' | 'ASSET' | 'ADMIN' = 'ASSET') => {
    if (isDemo) {
      // Simulate network delay
      await new Promise(resolve => setTimeout(resolve, 600));
      return null; // Mock handling is done in components
    }

    const baseUrl = API_URLS[service];
    let url = `${baseUrl}${endpoint}`;

    // Add Authorization header
    const headers: Record<string, string> = {
      ...options.headers as Record<string, string>,
    };

    if (!(options.body instanceof FormData)) {
      headers['Content-Type'] = 'application/json';
    }

    if (tokens.access && service !== 'AUTH') {
      headers['Authorization'] = `Bearer ${tokens.access}`;
    }

    let response = await fetch(url, { ...options, headers });

    // Interceptor: Handle 401 (Unauthorized) - Try Refresh
    if (response.status === 401 && tokens.refresh) {
      try {
        const refreshResponse = await fetch(`${API_URLS.AUTH}/refreshtoken`, {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ refreshToken: tokens.refresh }),
        });

        if (refreshResponse.ok) {
          const data = await refreshResponse.json();
          setTokens(prev => ({ ...prev, access: data.accessToken }));

          // Retry original request with new token
          const newHeaders = { ...headers, 'Authorization': `Bearer ${data.accessToken}` };
          response = await fetch(url, { ...options, headers: newHeaders });
        } else {
          logout();
          throw new Error('Session expired');
        }
      } catch (e) {
        logout();
        throw e;
      }
    }

    if (!response.ok) {
      throw new Error(`API Error: ${response.statusText}`);
    }

    // Return JSON if content exists, else null
    const text = await response.text();
    return text ? JSON.parse(text) : null;
  }, [isDemo, tokens, logout]);

  return { apiCall, setTokens, tokens };
};

// --- Components ---

const Toast = ({ message, type, onClose }: { message: string, type: 'success' | 'error', onClose: () => void }) => {
  useEffect(() => {
    const timer = setTimeout(onClose, 3000);
    return () => clearTimeout(timer);
  }, [onClose]);

  return (
    <div className={`fixed bottom-5 right-5 flex items-center gap-2 px-4 py-3 rounded-lg shadow-xl text-white transform transition-all animate-slide-up z-50 ${type === 'success' ? 'bg-emerald-600' : 'bg-rose-600'}`}>
      {type === 'success' ? <CheckCircle size={18} /> : <AlertCircle size={18} />}
      <span className="font-medium text-sm">{message}</span>
      <button onClick={onClose} className="ml-2 hover:bg-white/20 rounded-full p-1"><X size={14} /></button>
    </div>
  );
};

const Modal = ({ isOpen, onClose, title, children }: { isOpen: boolean, onClose: () => void, title: string, children: React.ReactNode }) => {
  if (!isOpen) return null;
  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-900/60 backdrop-blur-sm">
      <div className="bg-white rounded-xl shadow-2xl w-full max-w-md overflow-hidden animate-scale-in">
        <div className="flex justify-between items-center p-4 border-b border-slate-100">
          <h3 className="font-semibold text-slate-800">{title}</h3>
          <button onClick={onClose} className="text-slate-400 hover:text-slate-600"><X size={20} /></button>
        </div>
        <div className="p-6">{children}</div>
      </div>
    </div>
  );
};

// --- Main Application ---

export default function AssetManagerApp() {
  // State
  const [authState, setAuthState] = useState<AuthState>('login');
  const [user, setUser] = useState<UserProfile | null>(null);
  const [view, setView] = useState<ViewState>('my-assets');
  const [adminTab, setAdminTab] = useState<AdminTab>('files');
  const [assets, setAssets] = useState<Asset[]>([]);
  const [isSidebarOpen, setIsSidebarOpen] = useState(true);
  const [isLoading, setIsLoading] = useState(false);
  const [isDemo, setIsDemo] = useState(false);
  const [toast, setToast] = useState<{ msg: string, type: 'success' | 'error' } | null>(null);

  // Admin Data State
  const [userRequests, setUserRequests] = useState<UserRequest[]>(isDemo ? MOCK_REQUESTS : []);
  const [allUsers, setAllUsers] = useState<UserProfile[]>(isDemo ? MOCK_ALL_USERS : []);

  // Modals
  const [isUploadOpen, setIsUploadOpen] = useState(false);
  const [uploadFile, setUploadFile] = useState<File | null>(null);
  const [isPublicUpload, setIsPublicUpload] = useState(false);

  // Form Inputs
  const usernameRef = useRef<HTMLInputElement>(null);
  const passwordRef = useRef<HTMLInputElement>(null);
  const emailRef = useRef<HTMLInputElement>(null);
  const roleRef = useRef<HTMLSelectElement>(null);

  // API Hook
  const logout = useCallback(() => {
    setUser(null);
    setAuthState('login');
    showToast('Logged out successfully', 'success');
  }, []);

  const { apiCall, setTokens, tokens } = useApi(isDemo, logout);

  // Helpers
  const showToast = (msg: string, type: 'success' | 'error') => setToast({ msg, type });



  const handleAuth = async (e: React.FormEvent) => {
    e.preventDefault();
    setIsLoading(true);
    const uName = usernameRef.current?.value || '';
    const pass = passwordRef.current?.value || '';
    const email = emailRef.current?.value || '';

    try {
      if (authState === 'register') {
        // --- Registration Flow (Request to Admin) ---
        if (isDemo) {
          await new Promise(r => setTimeout(r, 1000));
          const newReq: UserRequest = {
            id: Math.random().toString(),
            username: uName,
            email: email || 'user@example.com',
            status: 'pending',
            requestedAt: new Date().toISOString().split('T')[0]
          };
          setUserRequests(prev => [...prev, newReq]);
        } else {
          // Real Register Logic (API Call to create request)
          const selectedRole = roleRef.current?.value || 'user';
          await apiCall('/signup-request', { method: 'POST', body: JSON.stringify({ username: uName, password: pass, email, role: [selectedRole] }) }, 'AUTH');
        }

        showToast('Account request sent to Admin for approval.', 'success');
        setAuthState('login'); // Switch back to login
      } else {
        // --- Login Flow ---
        if (isDemo) {
          await new Promise(r => setTimeout(r, 1000));
          if (uName === 'admin_user') {
            setUser(MOCK_USER);
            setTokens({ access: 'mock_access', refresh: 'mock_refresh' });
          } else {
            // Simple logic for non-admin login demo
            setUser({
              username: uName,
              fullName: uName.charAt(0).toUpperCase() + uName.slice(1),
              email: email || `${uName}@corp.net`,
              roles: ['USER'],
              joinedAt: new Date().toISOString().split('T')[0]
            });
          }
        } else {
          // Real Login
          const res = await apiCall('/signin', { method: 'POST', body: JSON.stringify({ username: uName, password: pass }) }, 'AUTH');
          setTokens({ access: res.accessToken, refresh: res.refreshToken });
          // Map response to UserProfile - store id and username from response
          setUser({
            id: res.id,
            username: res.username || uName,
            roles: res.roles || [],
            email: res.email,
            joinedAt: new Date().toISOString().split('T')[0]
          });
        }
        setAuthState('authenticated');
        fetchAssets('my-assets');
        showToast('Welcome back', 'success');
      }
    } catch (err: any) {
      showToast(err.message || 'Authentication failed', 'error');
    } finally {
      setIsLoading(false);
    }
  };

  const fetchAssets = async (currentView: ViewState) => {
    // Only fetch assets if we are in an asset view
    if (currentView === 'profile') return;

    setIsLoading(true);
    try {
      if (isDemo) {
        await new Promise(r => setTimeout(r, 800));
        let data = [...MOCK_ASSETS];

        if (currentView === 'my-assets') {
          data = data.filter(a => a.owner === user?.username);
        } else if (currentView === 'public-assets') {
          data = data.filter(a => a.isPublic);
        } else if (currentView === 'admin-console') {
          // Admin sees all
        }
        setAssets(data);
      } else {
        let endpoint = '/search?query=';
        if (currentView === 'my-assets') endpoint = '/asset/my-assets';
        if (currentView === 'public-assets') endpoint = '/public/asset';
        if (currentView === 'admin-console') endpoint = '/assets';

        const service = currentView === 'admin-console' ? 'ADMIN' : 'ASSET';

        const data = await apiCall(endpoint, { method: 'GET' }, service);

        // Map backend response (DTO/Entity) to Frontend Asset Interface
        const mappedData = (data || []).map((item: any) => ({
          ...item,
          id: item.asset_id || item.id, // Map asset_id to id
          uploadedById: item.uploadedById, // Keep numeric ID for ownership check
          isPublic: item.public !== undefined ? item.public : item.isPublic,
          createdAt: item.uploadedAt || item.createdAt,
          owner: item.uploadedByUsername || String(item.uploadedById) || item.owner
        }));

        setAssets(mappedData);
      }
    } catch (err) {
      showToast('Failed to load assets', 'error');
    } finally {
      setIsLoading(false);
    }
  };

  // Fetch User Requests for Admin Console
  const fetchUserRequests = async () => {
    if (isDemo) return; // Demo mode uses mock data
    if (!user?.roles.some(r => r === 'ROLE_ADMIN' || r === 'ADMIN')) return; // Only admins fetch

    try {
      const data = await apiCall('/users/requests', { method: 'GET' }, 'ADMIN');
      const mappedData = (data || []).map((item: any) => ({
        id: String(item.id),
        username: item.username,
        email: item.email,
        status: 'pending' as const,
        requestedAt: item.requestedAt || new Date().toISOString().split('T')[0]
      }));
      setUserRequests(mappedData);
    } catch (err) {
      console.error('Failed to fetch user requests:', err);
    }
  };

  // Fetch All Users for Admin Console
  const fetchAllUsers = async () => {
    if (isDemo) return; // Demo mode uses mock data
    if (!user?.roles.some(r => r === 'ROLE_ADMIN' || r === 'ADMIN')) return; // Only admins fetch

    try {
      // Call auth-service internal endpoint directly since it's publicly accessible
      const response = await fetch(`${API_URLS.AUTH.replace('/api/auth', '')}/api/internal/requests/users`, {
        method: 'GET',
        headers: {
          'Content-Type': 'application/json',
        },
      });
      const data = await response.json();
      const mappedData = (data || []).map((item: any) => ({
        id: item.user_id || item.id,
        username: item.username,
        email: item.email,
        roles: item.roles?.map((r: any) => r.userRole || r.roleName || r) || ['USER'],
        joinedAt: item.createdAt || new Date().toISOString().split('T')[0]
      }));
      setAllUsers(mappedData);
    } catch (err) {
      console.error('Failed to fetch all users:', err);
    }
  };

  const handleUpload = async () => {
    if (!uploadFile) return;
    setIsLoading(true);
    try {
      if (isDemo) {
        await new Promise(r => setTimeout(r, 1200));
        const newAsset: Asset = {
          id: Math.random().toString(),
          filename: uploadFile.name,
          owner: user?.username || 'unknown',
          size: `${(uploadFile.size / 1024).toFixed(1)} KB`,
          isPublic: isPublicUpload,
          type: uploadFile.type.includes('image') ? 'image' : 'document',
          createdAt: new Date().toISOString().split('T')[0]
        };
        MOCK_ASSETS.push(newAsset);
        setAssets(prev => [newAsset, ...prev]);
      } else {
        const formData = new FormData();
        formData.append('file', uploadFile);
        formData.append('isPublic', String(isPublicUpload));
        await apiCall('/upload', { method: 'POST', body: formData }, 'ASSET');
        fetchAssets(view);
      }
      showToast('Upload successful', 'success');
      setIsUploadOpen(false);
      setUploadFile(null);
    } catch (err) {
      showToast('Upload failed', 'error');
    } finally {
      setIsLoading(false);
    }
  };

  const handleDeleteAsset = async (id: string) => {
    if (!confirm('Are you sure you want to delete this asset?')) return;
    try {
      if (isDemo) {
        setAssets(prev => prev.filter(a => a.id !== id));
      } else {
        const endpoint = view === 'admin-console' ? `/assets/${id}` : `/asset/${id}`;
        const service = view === 'admin-console' ? 'ADMIN' : 'ASSET';
        await apiCall(endpoint, { method: 'DELETE' }, service);
        fetchAssets(view);
      }
      showToast('Asset deleted', 'success');
    } catch (err) {
      showToast('Action failed', 'error');
    }
  };

  const handleDownload = async (asset: Asset) => {
    try {
      if (isDemo) {
        showToast('Download simulation started', 'success');
        return;
      }

      const url = `${API_URLS.ASSET}/asset/download/${asset.id}`;
      const headers: HeadersInit = {};
      if (tokens.access) {
        headers['Authorization'] = `Bearer ${tokens.access}`;
      }

      const response = await fetch(url, { headers });

      if (!response.ok) throw new Error('Download failed');

      const blob = await response.blob();
      const downloadUrl = window.URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = downloadUrl;
      a.download = asset.filename;
      document.body.appendChild(a);
      a.click();
      window.URL.revokeObjectURL(downloadUrl);
      document.body.removeChild(a);
      showToast('Download started', 'success');
    } catch (err) {
      showToast('Download failed', 'error');
    }
  };

  const handleToggleVisibility = async (asset: Asset) => {
    const newStatus = !asset.isPublic;
    try {
      if (isDemo) {
        setAssets(prev => prev.map(a => a.id === asset.id ? { ...a, isPublic: newStatus } : a));
      } else {
        const endpoint = view === 'admin-console'
          ? `/assets/${asset.id}/visibility`
          : `/asset/${asset.id}/visibility`;

        const service = view === 'admin-console' ? 'ADMIN' : 'ASSET';

        await apiCall(endpoint, {
          method: 'POST',
          body: JSON.stringify({ makePublic: newStatus })
        }, service);

        fetchAssets(view);
      }
      showToast(`Asset is now ${newStatus ? 'Public' : 'Private'}`, 'success');
    } catch (err) {
      showToast('Failed to update visibility', 'error');
    }
  };

  // --- Admin User Management ---

  const handleApproveUser = async (req: UserRequest) => {
    setIsLoading(true);
    try {
      if (isDemo) {
        await new Promise(r => setTimeout(r, 600));
        // Add to all users
        setAllUsers(prev => [...prev, { username: req.username, email: req.email, roles: ['USER'], fullName: req.username }]);
        // Remove from requests
        setUserRequests(prev => prev.filter(r => r.id !== req.id));
      } else {
        await apiCall(`/users/${req.username}/approve`, { method: 'POST' }, 'ADMIN');
      }
      showToast(`User ${req.username} approved`, 'success');
    } catch (err) {
      showToast('Approval failed', 'error');
    } finally {
      setIsLoading(false);
    }
  };

  const handleRejectUser = async (id: string) => {
    if (!confirm('Reject this user request?')) return;
    try {
      if (isDemo) {
        setUserRequests(prev => prev.filter(r => r.id !== id));
      } else {
        await apiCall(`/users/requests/${id}`, { method: 'DELETE' }, 'ADMIN');
      }
      showToast('Request rejected', 'success');
    } catch (err) { showToast('Action failed', 'error'); }
  };

  const handleDeleteUser = async (username: string) => {
    if (!confirm(`Delete user ${username}? This will remove all their access.`)) return;
    try {
      if (isDemo) {
        setAllUsers(prev => prev.filter(u => u.username !== username));
      } else {
        await apiCall(`/users/${username}`, { method: 'DELETE' }, 'ADMIN');
      }
      showToast('User deleted', 'success');
    } catch (err) { showToast('Delete failed', 'error'); }
  };

  // --- Side Effects ---

  useEffect(() => {
    if (authState === 'authenticated') {
      fetchAssets(view);
      if (view === 'admin-console') {
        fetchUserRequests();
        fetchAllUsers();
      }
    }
  }, [view, authState]);

  // --- Render Helpers ---

  const getFileIcon = (type: Asset['type']) => {
    switch (type) {
      case 'image': return <ImageIcon className="text-indigo-500" size={24} />;
      case 'document': return <FileText className="text-orange-500" size={24} />;
      default: return <File className="text-slate-400" size={24} />;
    }
  };

  // --- Views ---

  if (authState === 'login' || authState === 'register') {
    return (
      <div className="min-h-screen bg-gradient-to-br from-slate-50 to-slate-200 flex items-center justify-center p-4 font-sans text-slate-800">
        <div className="bg-white rounded-2xl shadow-xl w-full max-w-md overflow-hidden flex flex-col md:flex-row max-w-4xl h-[550px]">

          {/* Brand Side */}
          <div className="w-full md:w-1/2 bg-indigo-600 p-8 flex flex-col justify-between text-white relative overflow-hidden">
            <div className="z-10">
              <div className="flex items-center gap-2 mb-2">
                <HardDrive size={32} />
                <span className="text-2xl font-bold tracking-tight">AssetFlow</span>
              </div>
              <p className="text-indigo-100 opacity-90">Enterprise Grade Asset Management System</p>
            </div>
            <div className="z-10 space-y-4">
              <div className="flex items-center gap-3 bg-white/10 p-3 rounded-lg backdrop-blur-sm">
                <Shield size={20} />
                <span className="text-sm">Admin Controlled Access</span>
              </div>
              <div className="flex items-center gap-3 bg-white/10 p-3 rounded-lg backdrop-blur-sm">
                <Users size={20} />
                <span className="text-sm">Role Based Management</span>
              </div>
            </div>
            {/* Background Deco */}
            <div className="absolute -bottom-10 -right-10 w-64 h-64 bg-indigo-500 rounded-full blur-3xl opacity-50"></div>
            <div className="absolute top-10 -left-10 w-40 h-40 bg-indigo-400 rounded-full blur-3xl opacity-30"></div>
          </div>

          {/* Form Side */}
          <div className="w-full md:w-1/2 p-8 bg-white flex flex-col justify-center">
            <div className="mb-6">
              <h2 className="text-2xl font-bold text-slate-800 mb-1">
                {authState === 'login' ? 'Welcome Back' : 'Request Access'}
              </h2>
              <p className="text-slate-500 text-sm">
                {authState === 'login' ? 'Enter your credentials to access.' : 'Submit details for admin approval.'}
              </p>
            </div>

            <form onSubmit={handleAuth} className="space-y-4">
              <div className="space-y-1">
                <label className="text-xs font-semibold uppercase text-slate-500">Username</label>
                <input
                  ref={usernameRef}
                  type="text"
                  className="w-full px-4 py-2 border border-slate-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-indigo-500 transition-all text-sm"
                  placeholder="e.g. admin_user"
                  defaultValue="admin_user"
                  required
                />
              </div>

              {authState === 'register' && (
                <div className="space-y-1 animate-slide-up">
                  <label className="text-xs font-semibold uppercase text-slate-500">Email</label>
                  <input
                    ref={emailRef}
                    type="email"
                    className="w-full px-4 py-2 border border-slate-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-indigo-500 transition-all text-sm"
                    placeholder="you@company.com"
                    required
                  />
                </div>
              )}

              {authState === 'register' && (
                <div className="space-y-1 animate-slide-up">
                  <label className="text-xs font-semibold uppercase text-slate-500">Requested Role</label>
                  <select
                    ref={roleRef}
                    className="w-full px-4 py-2 border border-slate-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-indigo-500 transition-all text-sm bg-white"
                  >
                    <option value="user">User (Basic Access)</option>
                    <option value="editor">Editor (Upload & Manage)</option>
                  </select>
                </div>
              )}

              <div className="space-y-1">
                <label className="text-xs font-semibold uppercase text-slate-500">Password</label>
                <input
                  ref={passwordRef}
                  type="password"
                  className="w-full px-4 py-2 border border-slate-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-indigo-500 transition-all text-sm"
                  placeholder="••••••••"
                  defaultValue="password"
                  required
                />
              </div>

              <div className="flex items-center justify-between text-xs">
                <div className="flex items-center gap-2">
                  <input
                    type="checkbox"
                    id="demo"
                    checked={isDemo}
                    onChange={(e) => setIsDemo(e.target.checked)}
                    className="rounded text-indigo-600 focus:ring-indigo-500"
                  />
                  <label htmlFor="demo" className="text-slate-600 cursor-pointer">Demo Mode</label>
                </div>
                {authState === 'login' && (
                  <a href="mailto:admin@example.com" className="text-indigo-600 hover:underline">Contact Admin</a>
                )}
              </div>

              <button
                type="submit"
                disabled={isLoading}
                className="w-full bg-indigo-600 hover:bg-indigo-700 text-white font-semibold py-2.5 rounded-lg transition-all flex items-center justify-center gap-2 disabled:opacity-70 disabled:cursor-not-allowed"
              >
                {isLoading && <Loader2 size={18} className="animate-spin" />}
                {authState === 'login' ? 'Sign In' : 'Submit Request'}
              </button>
            </form>

            <div className="mt-6 text-center text-xs text-slate-400">
              {authState === 'login' ? (
                <>New employee? <button onClick={() => setAuthState('register')} className="text-indigo-600 font-medium hover:underline">Request account</button></>
              ) : (
                <>Have an account? <button onClick={() => setAuthState('login')} className="text-indigo-600 font-medium hover:underline">Sign in</button></>
              )}
            </div>
          </div>
        </div>
        {toast && <Toast message={toast.msg} type={toast.type} onClose={() => setToast(null)} />}
      </div>
    );
  }

  // --- Dashboard Layout ---

  return (
    <div className="flex h-screen bg-slate-50 font-sans text-slate-800 overflow-hidden">

      {/* Sidebar */}
      <aside className={`bg-white border-r border-slate-200 transition-all duration-300 flex flex-col ${isSidebarOpen ? 'w-64' : 'w-20'}`}>
        <div className="p-6 flex items-center gap-3 border-b border-slate-100 h-16">
          <div className="min-w-[32px] w-8 h-8 bg-indigo-600 rounded-lg flex items-center justify-center text-white">
            <HardDrive size={18} />
          </div>
          {isSidebarOpen && <span className="font-bold text-lg tracking-tight text-slate-800">AssetFlow</span>}
        </div>

        <nav className="flex-1 p-4 space-y-1 overflow-y-auto">
          <SidebarItem
            icon={<LayoutDashboard size={20} />}
            label="My Assets"
            isActive={view === 'my-assets'}
            onClick={() => setView('my-assets')}
            expanded={isSidebarOpen}
          />
          <SidebarItem
            icon={<FolderOpen size={20} />}
            label="Public Assets"
            isActive={view === 'public-assets'}
            onClick={() => setView('public-assets')}
            expanded={isSidebarOpen}
          />
          <SidebarItem
            icon={<User size={20} />}
            label="My Profile"
            isActive={view === 'profile'}
            onClick={() => setView('profile')}
            expanded={isSidebarOpen}
          />
          {user?.roles.includes('ROLE_ADMIN') && (
            <>
              <div className="my-4 border-t border-slate-100"></div>
              <div className={`px-3 mb-2 text-xs font-semibold text-slate-400 uppercase ${!isSidebarOpen && 'hidden'}`}>Admin Console</div>
              <SidebarItem
                icon={<Shield size={20} />}
                label="Console"
                isActive={view === 'admin-console'}
                onClick={() => setView('admin-console')}
                expanded={isSidebarOpen}
              />
            </>
          )}
        </nav>

        <div className="p-4 border-t border-slate-100">
          <div className={`flex items-center gap-3 p-2 rounded-lg bg-slate-50 mb-2 cursor-pointer hover:bg-slate-100 transition-colors ${!isSidebarOpen && 'justify-center'}`} onClick={() => setView('profile')}>
            <div className="w-8 h-8 rounded-full bg-indigo-100 text-indigo-600 flex items-center justify-center">
              <User size={16} />
            </div>
            {isSidebarOpen && (
              <div className="overflow-hidden">
                <p className="text-sm font-medium truncate">{user?.username}</p>
                <p className="text-xs text-slate-500 truncate">{user?.roles.join(', ')}</p>
              </div>
            )}
          </div>
          <button
            onClick={logout}
            className={`flex items-center gap-3 w-full p-2 text-slate-500 hover:text-rose-600 hover:bg-rose-50 rounded-lg transition-colors ${!isSidebarOpen && 'justify-center'}`}
          >
            <LogOut size={20} />
            {isSidebarOpen && <span className="text-sm font-medium">Sign Out</span>}
          </button>
        </div>
      </aside>

      {/* Main Content */}
      <main className="flex-1 flex flex-col h-full overflow-hidden">

        {/* Top Header */}
        <header className="h-16 bg-white border-b border-slate-200 flex items-center justify-between px-6 flex-shrink-0 z-20">
          <div className="flex items-center gap-4">
            <button onClick={() => setIsSidebarOpen(!isSidebarOpen)} className="p-2 hover:bg-slate-100 rounded-lg text-slate-500">
              <Menu size={20} />
            </button>
            <h1 className="text-xl font-semibold text-slate-800">
              {view === 'my-assets' && 'My Assets'}
              {view === 'public-assets' && 'Public Network'}
              {view === 'admin-console' && 'Admin Console'}
              {view === 'profile' && 'My Profile'}
            </h1>
          </div>

          <div className="flex items-center gap-4">
            {view !== 'admin-console' && view !== 'profile' && (
              <div className="relative hidden md:block">
                <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 text-slate-400" size={16} />
                <input
                  type="text"
                  placeholder="Search files..."
                  className="pl-10 pr-4 py-2 bg-slate-100 border-none rounded-full text-sm focus:ring-2 focus:ring-indigo-500 w-64 transition-all"
                  onKeyDown={(e) => { if (e.key === 'Enter') fetchAssets(view); }}
                />
              </div>
            )}
            {view !== 'profile' && (
              <button
                onClick={() => setIsUploadOpen(true)}
                className="bg-indigo-600 hover:bg-indigo-700 text-white px-4 py-2 rounded-lg text-sm font-medium flex items-center gap-2 shadow-lg shadow-indigo-200 transition-all hover:-translate-y-0.5"
              >
                <Upload size={16} />
                <span className="hidden sm:inline">Upload New</span>
              </button>
            )}
          </div>
        </header>

        {/* Content Area */}
        <div className="flex-1 overflow-y-auto p-6 relative">
          {isLoading && (
            <div className="absolute inset-0 bg-white/50 backdrop-blur-sm z-10 flex items-center justify-center">
              <Loader2 size={40} className="text-indigo-600 animate-spin" />
            </div>
          )}

          {/* PROFILE VIEW */}
          {view === 'profile' ? (
            <div className="max-w-3xl mx-auto space-y-6">
              {/* Profile Header Card */}
              <div className="bg-white rounded-xl shadow-sm border border-slate-200 overflow-hidden relative group">
                <div className="h-36 bg-gradient-to-r from-indigo-500 to-purple-600 relative">
                  <div className="absolute inset-0 bg-black/10"></div>
                </div>
                <div className="px-8 pb-8 flex flex-col md:flex-row items-start md:items-end -mt-12 gap-6 relative">
                  <div className="w-24 h-24 rounded-2xl border-4 border-white bg-white shadow-md flex items-center justify-center text-3xl font-bold text-indigo-600 overflow-hidden">
                    {user?.username.substring(0, 2).toUpperCase()}
                  </div>
                  <div className="flex-1 mb-2">
                    <h2 className="text-2xl font-bold text-slate-800">{user?.fullName || user?.username}</h2>
                    <div className="flex items-center gap-2 text-slate-500 text-sm">
                      <Mail size={14} />
                      {user?.email || 'N/A'}
                    </div>
                  </div>
                  <div className="mb-3 hidden md:block">
                    <span className={`px-3 py-1 rounded-full text-xs font-semibold ${user?.roles.includes('ROLE_ADMIN') ? 'bg-purple-100 text-purple-700' : 'bg-indigo-100 text-indigo-700'}`}>
                      {user?.roles.includes('ROLE_ADMIN') ? 'Administrator' : 'Standard User'}
                    </span>
                  </div>
                </div>
              </div>

              {/* Details Grid */}
              <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                {/* Account Details */}
                <div className="bg-white rounded-xl shadow-sm border border-slate-200 p-6">
                  <div className="flex items-center gap-2 mb-6 pb-2 border-b border-slate-100">
                    <BadgeCheck size={18} className="text-indigo-500" />
                    <h3 className="font-semibold text-slate-800">Account Details</h3>
                  </div>

                  <div className="space-y-4">
                    <div>
                      <label className="text-xs font-semibold text-slate-400 uppercase tracking-wider block mb-1">Username</label>
                      <p className="font-medium text-slate-700">{user?.username}</p>
                    </div>
                    <div>
                      <label className="text-xs font-semibold text-slate-400 uppercase tracking-wider block mb-1">Email Address</label>
                      <p className="font-medium text-slate-700">{user?.email || 'Not provided'}</p>
                    </div>
                    <div>
                      <label className="text-xs font-semibold text-slate-400 uppercase tracking-wider block mb-1">System Role</label>
                      <div className="flex gap-2">
                        {user?.roles.map(role => (
                          <span key={role} className="text-xs px-2 py-0.5 bg-slate-100 text-slate-600 rounded border border-slate-200 font-mono">
                            {role}
                          </span>
                        ))}
                      </div>
                    </div>
                  </div>
                </div>

                {/* Status & Stats */}
                <div className="bg-white rounded-xl shadow-sm border border-slate-200 p-6">
                  <div className="flex items-center gap-2 mb-6 pb-2 border-b border-slate-100">
                    <CreditCard size={18} className="text-indigo-500" />
                    <h3 className="font-semibold text-slate-800">Status & Activity</h3>
                  </div>

                  <div className="space-y-4">
                    <div>
                      <label className="text-xs font-semibold text-slate-400 uppercase tracking-wider block mb-1">Account Status</label>
                      <div className="flex items-center gap-2 text-emerald-600 font-medium bg-emerald-50 px-3 py-2 rounded-lg border border-emerald-100 w-fit">
                        <CheckCircle size={16} />
                        Active
                      </div>
                    </div>
                    <div>
                      <label className="text-xs font-semibold text-slate-400 uppercase tracking-wider block mb-1">Member Since</label>
                      <div className="flex items-center gap-2 text-slate-600">
                        <Calendar size={16} className="text-slate-400" />
                        {user?.joinedAt || 'Unknown'}
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          ) : view === 'admin-console' ? (
            /* ADMIN CONSOLE VIEW */
            <div className="space-y-6">
              {/* Admin Tabs */}
              <div className="flex space-x-1 bg-slate-100 p-1 rounded-lg w-fit">
                <button
                  onClick={() => setAdminTab('files')}
                  className={`px-4 py-2 rounded-md text-sm font-medium transition-all ${adminTab === 'files' ? 'bg-white shadow text-indigo-600' : 'text-slate-500 hover:text-slate-700'}`}
                >
                  File Management
                </button>
                <button
                  onClick={() => setAdminTab('users')}
                  className={`px-4 py-2 rounded-md text-sm font-medium transition-all flex items-center gap-2 ${adminTab === 'users' ? 'bg-white shadow text-indigo-600' : 'text-slate-500 hover:text-slate-700'}`}
                >
                  User Management
                  {userRequests.length > 0 && <span className="bg-rose-500 text-white text-[10px] px-1.5 rounded-full">{userRequests.length}</span>}
                </button>
              </div>

              {/* File Management Tab */}
              {adminTab === 'files' && (
                <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6">
                  {assets.map(asset => (
                    <div key={asset.id} className="group bg-white rounded-xl border border-slate-200 hover:border-indigo-300 hover:shadow-lg transition-all p-4 flex flex-col">
                      <div className="flex items-center justify-between mb-2">
                        {getFileIcon(asset.type)}
                        <span className="text-xs text-slate-400">{asset.isPublic ? 'Public' : 'Private'}</span>
                      </div>
                      <h3 className="font-medium truncate mb-1">{asset.filename}</h3>
                      <p className="text-xs text-slate-500 mb-4">{asset.owner}</p>
                      <div className="mt-auto grid grid-cols-2 gap-2">
                        <button
                          onClick={() => handleToggleVisibility(asset)}
                          className={`w-full py-2 text-xs rounded font-medium flex items-center justify-center gap-1 transition-colors ${asset.isPublic ? 'bg-indigo-50 text-indigo-600 hover:bg-indigo-100' : 'bg-slate-100 text-slate-500 hover:bg-slate-200'}`}
                          title={asset.isPublic ? "Make Private" : "Make Public"}
                        >
                          {asset.isPublic ? <Eye size={14} /> : <EyeOff size={14} />}
                          {asset.isPublic ? "Public" : "Private"}
                        </button>
                        <button
                          onClick={() => handleDeleteAsset(asset.id)}
                          className="w-full py-2 bg-rose-50 text-rose-600 text-xs rounded hover:bg-rose-100 font-medium flex items-center justify-center gap-1"
                        >
                          <Trash2 size={14} /> Delete
                        </button>
                      </div>
                    </div>
                  ))}
                </div>
              )}

              {/* User Management Tab */}
              {adminTab === 'users' && (
                <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
                  {/* Pending Requests */}
                  <div className="bg-white rounded-xl border border-slate-200 shadow-sm overflow-hidden h-fit">
                    <div className="p-4 border-b border-slate-100 bg-slate-50 flex justify-between items-center">
                      <h3 className="font-semibold text-slate-800 flex items-center gap-2">
                        <UserPlus size={18} className="text-indigo-500" /> Pending Approvals
                      </h3>
                      <span className="text-xs bg-indigo-100 text-indigo-700 px-2 py-0.5 rounded-full">{userRequests.length}</span>
                    </div>
                    <div className="divide-y divide-slate-100">
                      {userRequests.length === 0 ? (
                        <div className="p-8 text-center text-slate-400 text-sm">No pending requests</div>
                      ) : (
                        userRequests.map(req => (
                          <div key={req.id} className="p-4 flex items-center justify-between hover:bg-slate-50">
                            <div>
                              <p className="font-medium text-slate-800">{req.username}</p>
                              <p className="text-xs text-slate-500">{req.email}</p>
                              <p className="text-[10px] text-slate-400 mt-1">Requested: {req.requestedAt}</p>
                            </div>
                            <div className="flex gap-2">
                              <button onClick={() => handleApproveUser(req)} className="p-2 bg-emerald-50 text-emerald-600 rounded-lg hover:bg-emerald-100" title="Approve">
                                <Check size={16} />
                              </button>
                              <button onClick={() => handleRejectUser(req.id)} className="p-2 bg-rose-50 text-rose-600 rounded-lg hover:bg-rose-100" title="Reject">
                                <X size={16} />
                              </button>
                            </div>
                          </div>
                        ))
                      )}
                    </div>
                  </div>

                  {/* Active Users */}
                  <div className="bg-white rounded-xl border border-slate-200 shadow-sm overflow-hidden h-fit">
                    <div className="p-4 border-b border-slate-100 bg-slate-50 flex justify-between items-center">
                      <h3 className="font-semibold text-slate-800 flex items-center gap-2">
                        <Users size={18} className="text-slate-500" /> Active Users
                      </h3>
                      <span className="text-xs bg-slate-100 text-slate-600 px-2 py-0.5 rounded-full">{allUsers.length}</span>
                    </div>
                    <div className="divide-y divide-slate-100">
                      {allUsers.map(u => (
                        <div key={u.username} className="p-4 flex items-center justify-between hover:bg-slate-50">
                          <div className="flex items-center gap-3">
                            <div className="w-8 h-8 rounded-full bg-slate-100 flex items-center justify-center text-slate-500 text-xs font-bold">
                              {u.username.substring(0, 2).toUpperCase()}
                            </div>
                            <div>
                              <p className="font-medium text-slate-800">{u.username}</p>
                              <div className="flex gap-2">
                                {u.roles.map(r => <span key={r} className="text-[10px] bg-slate-100 text-slate-500 px-1.5 rounded">{r}</span>)}
                              </div>
                            </div>
                          </div>
                          {u.username !== user?.username && (
                            <button onClick={() => handleDeleteUser(u.username)} className="text-slate-400 hover:text-rose-600 p-2">
                              <Trash2 size={16} />
                            </button>
                          )}
                        </div>
                      ))}
                    </div>
                  </div>
                </div>
              )}
            </div>

          ) : (
            /* ASSET GRID VIEW (My Assets / Public) */
            <>
              {assets.length === 0 && !isLoading ? (
                <div className="h-full flex flex-col items-center justify-center text-slate-400">
                  <div className="w-24 h-24 bg-slate-100 rounded-full flex items-center justify-center mb-4">
                    <FolderOpen size={40} className="text-slate-300" />
                  </div>
                  <p className="text-lg font-medium text-slate-500">No assets found</p>
                  <p className="text-sm">Upload a file to get started.</p>
                </div>
              ) : (
                <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6">
                  {assets.map(asset => (
                    <div key={asset.id} className="group bg-white rounded-xl border border-slate-200 hover:border-indigo-300 hover:shadow-lg transition-all duration-200 overflow-hidden flex flex-col">
                      {/* Card Header / Preview */}
                      <div className="h-32 bg-slate-50 flex items-center justify-center border-b border-slate-100 relative group-hover:bg-indigo-50/30 transition-colors">
                        {getFileIcon(asset.type)}
                        {asset.isPublic && (
                          <div className="absolute top-2 right-2 bg-emerald-100 text-emerald-700 text-[10px] px-2 py-0.5 rounded-full font-bold uppercase tracking-wide border border-emerald-200">
                            Public
                          </div>
                        )}
                      </div>

                      {/* Card Body */}
                      <div className="p-4 flex-1 flex flex-col">
                        <div className="flex items-start justify-between mb-2">
                          <h3 className="font-medium text-slate-800 truncate pr-2 flex-1" title={asset.filename}>{asset.filename}</h3>
                          <button className="text-slate-400 hover:text-indigo-600 opacity-0 group-hover:opacity-100 transition-opacity">
                            <MoreVertical size={16} />
                          </button>
                        </div>

                        <div className="mt-auto space-y-1">
                          <div className="flex justify-between text-xs text-slate-500">
                            <span>Size</span>
                            <span>{asset.size}</span>
                          </div>
                          <div className="flex justify-between text-xs text-slate-500">
                            <span>Owner</span>
                            <span>{asset.owner}</span>
                          </div>
                          <div className="flex justify-between text-xs text-slate-500">
                            <span>Date</span>
                            <span>{asset.createdAt}</span>
                          </div>
                        </div>

                        {/* Actions Row */}
                        <div className="mt-4 pt-3 border-t border-slate-50 flex gap-2">
                          <button
                            onClick={() => handleDownload(asset)}
                            className="flex-1 bg-slate-50 hover:bg-indigo-50 text-slate-600 hover:text-indigo-600 text-xs py-1.5 rounded font-medium transition-colors"
                          >
                            Download
                          </button>
                          {(user?.id === asset.uploadedById || user?.roles.some(r => r === 'ROLE_ADMIN' || r === 'ADMIN')) && (
                            <>
                              <button
                                onClick={() => handleToggleVisibility(asset)}
                                className={`px-2 rounded transition-colors ${asset.isPublic ? 'bg-indigo-50 text-indigo-600 hover:bg-indigo-100' : 'bg-slate-100 text-slate-500 hover:bg-slate-200'}`}
                                title={asset.isPublic ? "Make Private" : "Make Public"}
                              >
                                {asset.isPublic ? <Eye size={14} /> : <EyeOff size={14} />}
                              </button>
                              <button
                                onClick={() => handleDeleteAsset(asset.id)}
                                className="px-2 bg-rose-50 hover:bg-rose-100 text-rose-600 rounded transition-colors"
                                title="Delete"
                              >
                                <Trash2 size={14} />
                              </button>
                            </>
                          )}
                        </div>
                      </div>
                    </div>
                  ))}
                </div>
              )}
            </>
          )}
        </div>
      </main>

      {/* Upload Modal */}
      <Modal isOpen={isUploadOpen} onClose={() => setIsUploadOpen(false)} title="Upload New Asset">
        <div className="space-y-4">
          <div
            className={`border-2 border-dashed rounded-xl p-8 flex flex-col items-center justify-center text-center transition-all cursor-pointer ${uploadFile ? 'border-indigo-500 bg-indigo-50' : 'border-slate-300 hover:border-indigo-400 hover:bg-slate-50'}`}
            onClick={() => document.getElementById('file-upload')?.click()}
          >
            <input
              type="file"
              id="file-upload"
              className="hidden"
              onChange={(e) => setUploadFile(e.target.files?.[0] || null)}
            />
            {uploadFile ? (
              <>
                <FileText size={48} className="text-indigo-600 mb-2" />
                <p className="font-medium text-slate-800">{uploadFile.name}</p>
                <p className="text-xs text-slate-500">{(uploadFile.size / 1024).toFixed(1)} KB</p>
              </>
            ) : (
              <>
                <Upload size={48} className="text-slate-300 mb-2" />
                <p className="font-medium text-slate-600">Click to select file</p>
                <p className="text-xs text-slate-400">or drag and drop here</p>
              </>
            )}
          </div>

          <div className="flex items-center gap-3 p-3 bg-slate-50 rounded-lg border border-slate-100">
            <div className={`w-10 h-6 rounded-full p-1 cursor-pointer transition-colors ${isPublicUpload ? 'bg-indigo-600' : 'bg-slate-300'}`} onClick={() => setIsPublicUpload(!isPublicUpload)}>
              <div className={`w-4 h-4 rounded-full bg-white shadow-sm transform transition-transform ${isPublicUpload ? 'translate-x-4' : 'translate-x-0'}`}></div>
            </div>
            <div className="flex-1">
              <p className="text-sm font-medium text-slate-700">Make Public</p>
              <p className="text-xs text-slate-500">Anyone on the network can view this file.</p>
            </div>
            {isPublicUpload ? <Eye size={18} className="text-indigo-600" /> : <EyeOff size={18} className="text-slate-400" />}
          </div>

          <div className="flex gap-3 pt-2">
            <button onClick={() => setIsUploadOpen(false)} className="flex-1 px-4 py-2 text-slate-600 font-medium hover:bg-slate-100 rounded-lg transition-colors">Cancel</button>
            <button
              onClick={handleUpload}
              disabled={!uploadFile}
              className="flex-1 px-4 py-2 bg-indigo-600 hover:bg-indigo-700 text-white font-medium rounded-lg disabled:opacity-50 disabled:cursor-not-allowed transition-all shadow-lg shadow-indigo-200"
            >
              Upload Asset
            </button>
          </div>
        </div>
      </Modal>

      {toast && <Toast message={toast.msg} type={toast.type} onClose={() => setToast(null)} />}
    </div>
  );
}

// --- Subcomponents ---

function SidebarItem({ icon, label, isActive, onClick, expanded }: { icon: React.ReactNode, label: string, isActive: boolean, onClick: () => void, expanded: boolean }) {
  return (
    <button
      onClick={onClick}
      className={`flex items-center gap-3 w-full p-3 rounded-lg transition-all duration-200 group ${isActive
        ? 'bg-indigo-50 text-indigo-600 shadow-sm'
        : 'text-slate-600 hover:bg-slate-50 hover:text-slate-900'
        } ${!expanded && 'justify-center px-0'}`}
      title={!expanded ? label : ''}
    >
      <div className={`${isActive ? 'text-indigo-600' : 'text-slate-400 group-hover:text-slate-600'}`}>
        {icon}
      </div>
      {expanded && <span className="font-medium text-sm whitespace-nowrap">{label}</span>}
      {isActive && expanded && <div className="ml-auto w-1.5 h-1.5 rounded-full bg-indigo-600"></div>}
    </button>
  );
}