import React from 'react';
import { Avatar, Icon } from './ui.jsx';

export const navItems = [
  { id: 'dashboard', label: 'Overview', icon: 'grid' },
  { id: 'profile', label: 'My profile', icon: 'user' },
  { id: 'discover', label: 'Discover people', icon: 'compass' },
  { id: 'recommendations', label: 'Recommendations', icon: 'spark' },
  { id: 'connections', label: 'Connections', icon: 'people' },
  { id: 'messages', label: 'Messages', icon: 'chat' },
  { id: 'analysis', label: 'Skill analysis', icon: 'chart' },
];

export function AppShell({ page, onNavigate, user, children, onLogout, onAdmin, backendMode = false }) {
  const active = navItems.find((item) => item.id === page)?.id;
  return <div className="app-frame">
    <aside className="sidebar">
      <button className="brand" onClick={() => onNavigate('dashboard')} aria-label="SkillSync home"><span className="brand-mark"><Icon name="spark" size={20} /></span><span>skill<span className="brand-accent">sync</span><small>LEARN IN GOOD COMPANY</small></span></button>
      <div className="side-label">WORKSPACE</div>
      <nav className="main-nav" aria-label="Main navigation">{navItems.map((item) => <button key={item.id} className={`nav-link ${active === item.id ? 'is-active' : ''}`} onClick={() => onNavigate(item.id)}><Icon name={item.icon} size={18} /><span>{item.label}</span>{item.id === 'messages' && <span className="nav-count">2</span>}</button>)}</nav>
      <div className="side-label side-label-bottom">YOUR ACCOUNT</div>
      <nav className="main-nav"><button className={`nav-link ${page === 'settings' ? 'is-active' : ''}`} onClick={() => onNavigate('settings')}><Icon name="settings" size={18} /><span>Settings</span></button>{!backendMode&&<button className="nav-link" onClick={onAdmin}><Icon name="chart" size={18} /><span>Admin overview</span><span className="beta-label">DEMO</span></button>}</nav>
      <div className="sidebar-bottom"><div className="sidebar-promo"><span className="promo-spark"><Icon name="spark" size={18} /></span><strong>Small steps, real progress.</strong><p>Learning gets easier when you do it together.</p><button onClick={() => onNavigate('analysis')}>View your skill plan <Icon name="arrow" size={14} /></button></div><button className="profile-switch" onClick={() => onNavigate('profile')}><Avatar name={user.name} size="sm" /><span><strong>{user.name}</strong><small>Learner account</small></span><Icon name="settings" size={16} /></button><button className="logout-button" onClick={onLogout}><Icon name="logout" size={17} /> Sign out</button></div>
    </aside>
    <main className="main-area">
      <header className="topbar"><div className="breadcrumbs"><span>Workspace</span><b>/</b><strong>{navItems.find((item) => item.id === page)?.label ?? (page === 'admin' ? 'Admin overview' : page === 'recommendation-detail' ? 'Match details' : page === 'onboarding' ? 'Getting started' : 'Settings')}</strong></div><div className="topbar-actions"><span className="demo-pill"><i /> {backendMode ? 'BACKEND MODE' : 'DEMO MODE'}</span><button className="icon-button notification-button" aria-label="Notifications"><Icon name="bell" size={19} /><i /></button><button className="topbar-user" onClick={() => onNavigate('profile')}><Avatar name={user.name} size="sm" /><span>{user.name.split(' ')[0]}</span></button></div></header>
      <div className="page-content">{children}</div>
    </main>
    <nav className="mobile-nav" aria-label="Mobile navigation">{navItems.slice(0, 5).map((item) => <button key={item.id} className={active === item.id ? 'is-active' : ''} onClick={() => onNavigate(item.id)}><Icon name={item.icon} size={19} /><span>{item.label.split(' ')[0]}</span></button>)}<button onClick={() => onNavigate('settings')}><Icon name="settings" size={19} /><span>More</span></button></nav>
  </div>;
}
