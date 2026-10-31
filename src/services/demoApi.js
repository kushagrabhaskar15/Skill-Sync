import { initialActivities, initialMessages, initialPeers, initialUser } from '../data/seed.js';

const STORAGE_KEY = 'skillsync-demo-v1';
const pause = (ms = 220) => new Promise((resolve) => window.setTimeout(resolve, ms));

function readStore() {
  try {
    const stored = window.localStorage.getItem(STORAGE_KEY);
    if (!stored) return { user: initialUser, peers: initialPeers, messages: initialMessages, activities: initialActivities, loggedIn: false };
    const parsed = JSON.parse(stored);
    return {
      user: parsed.user ?? initialUser,
      peers: Array.isArray(parsed.peers) ? parsed.peers : initialPeers,
      messages: parsed.messages ?? initialMessages,
      activities: Array.isArray(parsed.activities) ? parsed.activities : initialActivities,
      loggedIn: Boolean(parsed.loggedIn),
    };
  } catch {
    return { user: initialUser, peers: initialPeers, messages: initialMessages, activities: initialActivities, loggedIn: false };
  }
}

function writeStore(data) {
  try {
    window.localStorage.setItem(STORAGE_KEY, JSON.stringify(data));
  } catch {
    throw new Error('Your browser could not save this demo. Check that local storage is enabled.');
  }
}

async function request(mutator) {
  await pause();
  const current = readStore();
  const next = mutator(current);
  writeStore(next);
  return structuredClone(next);
}

export const demoApi = {
  async load() {
    await pause(380);
    return structuredClone(readStore());
  },
  async authenticate({ name, email, password }) {
    if (!email?.includes('@') || !password || password.length < 6) throw new Error('Enter a valid email and a password of at least 6 characters.');
    return request((data) => ({ ...data, loggedIn: true, user: { ...data.user, ...(name ? { name } : {}), email } }));
  },
  async updateUser(updates) {
    return request((data) => ({ ...data, user: { ...data.user, ...updates }, activities: ['You updated your learning profile.', ...data.activities].slice(0, 6) }));
  },
  async setPeerStatus(peerId, status) {
    return request((data) => ({ ...data, peers: data.peers.map((peer) => peer.id === peerId ? { ...peer, status } : peer), activities: [`Connection with ${data.peers.find((peer) => peer.id === peerId)?.name ?? 'peer'}: ${status}.`, ...data.activities].slice(0, 6) }));
  },
  async sendMessage(peerId, text) {
    const clean = text.trim();
    if (!clean) throw new Error('Write a message before sending.');
    return request((data) => ({ ...data, messages: { ...data.messages, [peerId]: [...(data.messages[peerId] ?? []), { id: crypto.randomUUID(), from: 'self', text: clean, time: new Intl.DateTimeFormat('en', { hour: 'numeric', minute: '2-digit' }).format(new Date()) }] } }));
  },
  async toggleLogin(loggedIn) {
    return request((data) => ({ ...data, loggedIn }));
  },
  async reset() {
    window.localStorage.removeItem(STORAGE_KEY);
    return this.load();
  },
};
