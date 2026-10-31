import React from 'react';

const icons = {
  grid: <><rect x="3" y="3" width="7" height="7" rx="1.5"/><rect x="14" y="3" width="7" height="7" rx="1.5"/><rect x="3" y="14" width="7" height="7" rx="1.5"/><rect x="14" y="14" width="7" height="7" rx="1.5"/></>,
  user: <><circle cx="12" cy="8" r="3.5"/><path d="M5 21a7 7 0 0 1 14 0"/></>,
  compass: <><circle cx="12" cy="12" r="9"/><path d="m15.5 8.5-2.4 5-5 2.4 2.4-5 5-2.4Z"/></>,
  spark: <><path d="m12 3 1.9 5.8L20 11l-6.1 2.2L12 19l-2-5.8L4 11l6-2.2L12 3Z"/><path d="m19 15 .8 2.2L22 18l-2.2.8L19 21l-.8-2.2L16 18l2.2-.8L19 15Z"/></>,
  people: <><circle cx="9" cy="8" r="3"/><path d="M3 20a6 6 0 0 1 12 0M16 5.5a3 3 0 0 1 0 5.8M18 14a5 5 0 0 1 3 4.6"/></>,
  chat: <><path d="M20 11.5a7.5 7.5 0 0 1-7.5 7.5H5l1.6-3.1A7.5 7.5 0 1 1 20 11.5Z"/><path d="M8 11h8M8 14h5"/></>,
  chart: <><path d="M4 19V5M4 19h17"/><path d="m7 15 4-4 3 2 6-7"/></>,
  settings: <><circle cx="12" cy="12" r="3"/><path d="M19 13.5a7 7 0 0 0 0-3l2-1.5-2-3.5-2.4 1a8 8 0 0 0-2.6-1.5L13.7 2h-4L9.3 5a8 8 0 0 0-2.6 1.5l-2.4-1-2 3.5 2 1.5a7 7 0 0 0 0 3l-2 1.5 2 3.5 2.4-1a8 8 0 0 0 2.6 1.5l.4 3h4l.4-3a8 8 0 0 0 2.6-1.5l2.4 1 2-3.5-2-1.5Z" transform="translate(0 -1) scale(.92) translate(1 1)"/></>,
  search: <><circle cx="10.8" cy="10.8" r="6.8"/><path d="m16 16 4.5 4.5"/></>,
  bell: <><path d="M18 9a6 6 0 0 0-12 0c0 7-3 7-3 9h18c0-2-3-2-3-9M10 21h4"/></>,
  arrow: <><path d="M5 12h14M13 6l6 6-6 6"/></>,
  close: <><path d="m6 6 12 12M18 6 6 18"/></>,
  plus: <><path d="M12 5v14M5 12h14"/></>,
  check: <><path d="m5 12 4 4L19 6"/></>,
  logout: <><path d="M10 17l5-5-5-5M15 12H3"/><path d="M12 3h6a2 2 0 0 1 2 2v14a2 2 0 0 1-2 2h-6"/></>,
  clock: <><circle cx="12" cy="12" r="9"/><path d="M12 7v5l3 2"/></>,
  menu: <><path d="M4 6h16M4 12h16M4 18h16"/></>,
};

export function Icon({ name, size = 18, className = '' }) {
  return <svg aria-hidden="true" className={className} width={size} height={size} viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.7" strokeLinecap="round" strokeLinejoin="round">{icons[name] ?? icons.spark}</svg>;
}

export function Button({ children, variant = 'primary', size = '', className = '', ...props }) {
  return <button className={`button button-${variant} ${size ? `button-${size}` : ''} ${className}`} {...props}>{children}</button>;
}

export function Avatar({ name = 'Learner', color = 'violet', size = 'md' }) {
  const initials = name.split(' ').map((part) => part[0]).join('').slice(0, 2).toUpperCase();
  return <span className={`avatar avatar-${color} avatar-${size}`} aria-label={name}>{initials}</span>;
}

export function SkillTag({ children, tone = 'learn' }) {
  return <span className={`skill-tag skill-${tone}`}>{children}</span>;
}

export function StatePanel({ title, detail, action, onAction, kind = 'empty' }) {
  return <div className={`state-panel state-${kind}`} role={kind === 'error' ? 'alert' : undefined}>
    <span className="state-icon"><Icon name={kind === 'error' ? 'close' : kind === 'loading' ? 'clock' : 'spark'} size={19} /></span>
    <strong>{title}</strong><p>{detail}</p>{action && <Button variant="secondary" size="sm" onClick={onAction}>{action}</Button>}
  </div>;
}

export function PageHeading({ eyebrow, title, detail, action }) {
  return <div className="page-heading"><div>{eyebrow && <div className="eyebrow">{eyebrow}</div>}<h1>{title}</h1>{detail && <p>{detail}</p>}</div>{action}</div>;
}

export function SkillGroup({ title, skills = [], tone = 'teach', empty = 'No skills added yet.' }) {
  return <section className="skill-group"><div className="group-title"><h3>{title}</h3><span>{String(skills.length).padStart(2, '0')}</span></div>{skills.length ? <div className="tag-list">{skills.map((skill) => <SkillTag tone={tone} key={skill}>{skill}</SkillTag>)}</div> : <p className="muted small">{empty}</p>}</section>;
}

export function Field({ label, hint, ...props }) {
  return <label className="field"><span>{label}</span><input {...props} />{hint && <small>{hint}</small>}</label>;
}

export function Toast({ message, onClose, kind = 'error' }) {
  if (!message) return null;
  return <div className={`toast toast-${kind}`} role={kind === 'error' ? 'alert' : 'status'}>{kind === 'success' && <Icon name="check" size={17} />}{message}<button className="icon-button" onClick={onClose} aria-label="Dismiss"><Icon name="close" size={16} /></button></div>;
}
