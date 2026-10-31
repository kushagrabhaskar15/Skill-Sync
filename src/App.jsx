import React, { useEffect, useState } from 'react';
import { AppShell } from './components/AppShell.jsx';
import { Toast, StatePanel } from './components/ui.jsx';
import { useWorkspace } from './hooks/useWorkspace.js';
import { demoApi } from './services/demoApi.js';
import {
  Admin, AuthPage, Connections, Dashboard, Discover, Landing, Messages,
  Onboarding, Profile, RecommendationDetail, Recommendations, Settings, SkillAnalysis,
} from './pages/Pages.jsx';

export default function App() {
  const { data, loading, error, setError, refresh, mutate } = useWorkspace();
  const [page, setPage] = useState('');
  const [authMode, setAuthMode] = useState('signup');
  const [authLoading, setAuthLoading] = useState(false);
  const [busy, setBusy] = useState(false);
  const [selectedPeer, setSelectedPeer] = useState('');
  const [initialMessagePeer, setInitialMessagePeer] = useState('');
  const [toast, setToast] = useState('');
  const [theme, setTheme] = useState(() => {
    try {
      return window.localStorage.getItem('skillsync-theme') === 'dark' ? 'dark' : 'light';
    } catch {
      return 'light';
    }
  });

  useEffect(() => {
    document.documentElement.dataset.theme = theme;
    try {
      window.localStorage.setItem('skillsync-theme', theme);
    } catch {
      // The selected theme still works for this session when storage is unavailable.
    }
  }, [theme]);

  useEffect(() => {
    if (data && !page) setPage(data.loggedIn ? 'dashboard' : 'landing');
  }, [data, page]);

  useEffect(() => {
    const handleNavigation = (event) => setPage(event.detail);
    window.addEventListener('skillsync:navigate', handleNavigation);
    return () => window.removeEventListener('skillsync:navigate', handleNavigation);
  }, []);

  async function run(action, successMessage = '') {
    setBusy(true);
    try {
      const next = await mutate(action);
      if (successMessage) setToast(successMessage);
      return next;
    } catch (cause) {
      setToast(cause instanceof Error ? cause.message : 'That action could not be completed. Please try again.');
      return null;
    } finally {
      setBusy(false);
    }
  }

  async function authenticate(values) {
    setAuthLoading(true);
    try {
      const next = await mutate(() => demoApi.authenticate(values));
      setToast('Welcome to SkillSync. Your demo profile is ready.');
      setPage(authMode === 'signup' ? 'onboarding' : 'dashboard');
      return next;
    } catch {
      // The shared error is rendered on the auth form.
      return null;
    } finally {
      setAuthLoading(false);
    }
  }

  async function logout() {
    await run(() => demoApi.toggleLogin(false), 'You have signed out of the demo.');
    setPage('landing');
  }

  function openPeer(peerId) {
    setSelectedPeer(peerId);
    setPage('recommendation-detail');
  }

  async function connect(peerId) {
    const next = await run(() => demoApi.setPeerStatus(peerId, 'pending'), 'Connection request sent.');
    if (next) setPage('connections');
  }

  async function sendMessage(peerId, text) {
    await run(() => demoApi.sendMessage(peerId, text), 'Message sent.');
  }

  if (loading || !data) return <div className="startup-state"><div className="startup-logo"><span className="brand-mark">✦</span><strong>skillsync</strong></div><StatePanel kind={loading ? 'loading' : 'error'} title={loading ? 'Getting your learning space ready' : 'Your demo data could not load'} detail={loading ? 'Loading your profile and peer suggestions…' : error || 'Try loading your workspace again.'} action={!loading ? 'Try again' : undefined} onAction={refresh} /></div>;

  if (page === 'landing' || !page) return <><Landing onLogin={() => { setAuthMode('login'); setPage('auth'); }} onSignup={() => { setAuthMode('signup'); setPage('auth'); }} /><Toast message={toast} kind="success" onClose={() => setToast('')} /></>;

  if (page === 'auth') return <><AuthPage mode={authMode} onModeChange={setAuthMode} onSubmit={authenticate} onBack={() => { setError(''); setPage('landing'); }} error={error} loading={authLoading} /><Toast message={toast} kind="success" onClose={() => setToast('')} /></>;

  if (page === 'onboarding') return <><Onboarding user={data.user} loading={busy} onSave={async (updates) => { const next = await run(() => demoApi.updateUser(updates), 'Your learning profile is ready.'); if (next) setPage('dashboard'); }} /><Toast message={toast} kind="success" onClose={() => setToast('')} /></>;

  const peer = data.peers.find((item) => item.id === selectedPeer);
  const screen = (() => {
    switch (page) {
      case 'dashboard': return <Dashboard user={data.user} peers={data.peers} activities={data.activities} onNavigate={setPage} onSelectPeer={openPeer} onConnect={connect} />;
      case 'profile': return <Profile user={data.user} loading={busy} onSave={(updates) => run(() => demoApi.updateUser(updates), 'Your profile has been updated.')} />;
      case 'discover': return <Discover peers={data.peers} user={data.user} onSelectPeer={openPeer} onConnect={connect} />;
      case 'recommendations': return <Recommendations peers={data.peers} onSelectPeer={openPeer} onConnect={connect} />;
      case 'recommendation-detail': return <RecommendationDetail peer={peer} user={data.user} onBack={() => setPage('recommendations')} onConnect={connect} onMessage={(id) => { setInitialMessagePeer(id); setPage('messages'); }} />;
      case 'connections': return <Connections peers={data.peers} onMessage={(id) => { setInitialMessagePeer(id); setPage('messages'); }} onDiscover={() => setPage('discover')} onAccept={(id) => run(() => demoApi.setPeerStatus(id, 'connected'), 'Connection added to your learning circle.')} />;
      case 'messages': return <Messages key={initialMessagePeer} peers={data.peers} messages={data.messages} onSend={sendMessage} initialPeer={initialMessagePeer} />;
      case 'analysis': return <SkillAnalysis user={data.user} onEditProfile={() => setPage('profile')} onDiscover={() => setPage('discover')} />;
      case 'settings': return <Settings user={data.user} theme={theme} onThemeChange={setTheme} onSave={(updates) => run(() => demoApi.updateUser(updates), 'Account details saved.')} onReset={async () => { const next = await run(() => demoApi.reset(), 'Demo data was reset.'); if (next) setPage('landing'); }} />;
      case 'admin': return <Admin peers={data.peers} />;
      default: return <Dashboard user={data.user} peers={data.peers} activities={data.activities} onNavigate={setPage} onSelectPeer={openPeer} onConnect={connect} />;
    }
  })();

  return <AppShell page={page} onNavigate={setPage} user={data.user} onLogout={logout} onAdmin={() => setPage('admin')}><Toast message={toast} kind="success" onClose={() => setToast('')} />{error && !toast && <Toast message={error} onClose={() => setError('')} />}{screen}</AppShell>;
}
