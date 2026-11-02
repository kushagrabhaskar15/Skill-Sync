import { initialUser } from '../data/seed.js';
import { apiRequest, clearApiSession, readApiSession, saveApiSession } from './apiClient.js';

const EMPTY_WORKSPACE = {
  user: { ...initialUser, name: '', email: '', role: 'SkillSync learner', school: '', bio: '', teaches: [], learns: [], interests: [], goal: '' },
  peers: [], messages: {}, activities: [], recommendations: [], skillCatalog: [], loggedIn: false,
};
let currentWorkspace = null;
const outgoingRequests = new Map();

const peerColor = (id) => ['violet', 'orange', 'mint', 'pink', 'blue'][Math.abs(Number(id) || 0) % 5];
const formatTime = (value) => value ? new Intl.DateTimeFormat('en', { hour: 'numeric', minute: '2-digit' }).format(new Date(value)) : '';
const otherUser = (connection, currentId) => Number(connection.requester.id) === Number(currentId) ? connection.receiver : connection.requester;

function mapPeer(profile, summary = {}) {
  const id = String(profile.id ?? summary.id);
  return {
    id,
    backendId: Number(profile.id ?? summary.id),
    name: profile.name || summary.name || 'SkillSync learner',
    email: summary.email || '',
    role: 'SkillSync learner',
    school: profile.location || '',
    location: profile.location || '',
    bio: profile.bio || '',
    teaches: (profile.teachSkills || []).map((skill) => skill.skillName),
    learns: (profile.learnSkills || []).map((skill) => skill.skillName),
    interests: profile.interests || [],
    availability: '',
    level: '',
    color: peerColor(id),
    status: 'suggested',
  };
}

function mapMessage(message, currentUserId) {
  return {
    id: String(message.id),
    from: Number(message.senderId) === Number(currentUserId) ? 'self' : String(message.senderId),
    text: message.content,
    time: formatTime(message.sentAt),
  };
}

async function loadWorkspace() {
  const session = readApiSession();
  if (!session?.accessToken) {
    currentWorkspace = { ...EMPTY_WORKSPACE, user: { ...EMPTY_WORKSPACE.user }, peers: [], messages: {}, loggedIn: false };
    return structuredClone(currentWorkspace);
  }

  const [profile, recommendations, connections, incoming, conversations, skillCatalog] = await Promise.all([
    apiRequest('/api/v1/users/me'),
    apiRequest('/api/v1/recommendations'),
    apiRequest('/api/v1/connections'),
    apiRequest('/api/v1/connections/requests'),
    apiRequest('/api/v1/chat/conversations'),
    apiRequest('/api/v1/skills'),
  ]);

  const connectionRecords = [...connections, ...incoming];
  const recommendationIds = recommendations
    .filter((item) => item.status !== 'DISMISSED')
    .map((item) => item.recommendedUser.id);
  const otherUserIds = [
    ...connectionRecords.map((item) => otherUser(item, profile.id).id),
    ...conversations.map((item) => item.otherParticipant.id),
    ...recommendationIds,
  ].filter((id) => Number(id) !== Number(profile.id));
  const uniqueIds = [...new Set(otherUserIds.map(Number))];
  const publicProfiles = await Promise.all(uniqueIds.map(async (id) => {
    try {
      return await apiRequest(`/api/v1/users/${id}`);
    } catch (error) {
      if (error.status === 404) return null;
      throw error;
    }
  }));
  const profilesById = new Map(publicProfiles.filter(Boolean).map((item) => [Number(item.id), item]));
  const peersById = new Map();
  const ensurePeer = (summary) => {
    const id = Number(summary.id);
    if (!peersById.has(id)) peersById.set(id, mapPeer(profilesById.get(id) || { id, ...summary }, summary));
    return peersById.get(id);
  };

  const mappedRecommendations = recommendations.map((item) => {
    const peer = ensurePeer(item.recommendedUser);
    peer.recommendationId = item.id;
    peer.score = Number(item.score);
    peer.scorePercent = Math.round(peer.score * 100);
    peer.explanation = item.explanation || '';
    peer.recommendationStatus = item.status;
    if (item.status === 'CONNECTED') peer.status = 'connected';
    return { ...item, peerId: peer.id };
  }).filter((item) => item.status !== 'DISMISSED');

  const connectionStatus = new Map();
  for (const connection of connections) {
    const summary = otherUser(connection, profile.id);
    const peer = ensurePeer(summary);
    peer.status = 'connected';
    peer.connectionId = connection.id;
    connectionStatus.set(Number(summary.id), { status: 'connected', connectionId: connection.id });
  }
  for (const connection of incoming) {
    const summary = otherUser(connection, profile.id);
    const peer = ensurePeer(summary);
    peer.status = 'pending';
    peer.direction = 'incoming';
    peer.connectionId = connection.id;
    connectionStatus.set(Number(summary.id), { status: 'pending', direction: 'incoming', connectionId: connection.id });
  }
  for (const [peerId, connectionId] of outgoingRequests.entries()) {
    const peer = peersById.get(Number(peerId));
    if (peer && peer.status === 'suggested') {
      peer.status = 'pending';
      peer.direction = 'outgoing';
      peer.connectionId = connectionId;
    }
  }

  const messages = {};
  const conversationIds = {};
  await Promise.all(conversations.map(async (conversation) => {
    const other = ensurePeer(conversation.otherParticipant);
    other.status = 'connected';
    conversationIds[other.id] = conversation.id;
    other.conversationId = conversation.id;
    try {
      const page = await apiRequest(`/api/v1/chat/conversations/${conversation.id}/messages?page=0&size=50&sort=sentAt,asc`);
      messages[other.id] = (page?.content || []).map((item) => mapMessage(item, profile.id));
    } catch {
      messages[other.id] = [];
    }
  }));

  const currentUser = {
    ...EMPTY_WORKSPACE.user,
    id: String(profile.id),
    backendId: profile.id,
    name: profile.name || session.user?.name || '',
    email: session.user?.email || '',
    role: 'SkillSync learner',
    school: profile.location || '',
    location: profile.location || '',
    bio: profile.bio || '',
    profileImageUrl: profile.profileImageUrl || session.user?.profileImageUrl || '',
    teaches: (profile.teachSkills || []).map((skill) => skill.skillName),
    learns: (profile.learnSkills || []).map((skill) => skill.skillName),
    teachSkillRecords: profile.teachSkills || [],
    learnSkillRecords: profile.learnSkills || [],
    interests: profile.interests || [],
    goal: '',
  };
  currentWorkspace = {
    user: currentUser,
    peers: [...peersById.values()],
    messages,
    activities: [],
    recommendations: mappedRecommendations,
    skillCatalog,
    conversationIds,
    loggedIn: true,
  };
  return structuredClone(currentWorkspace);
}

async function synchronizeSkills(type, currentRecords = [], desiredNames = []) {
  const wantedNames = [...new Set(desiredNames.map((name) => name.trim()).filter(Boolean))];
  const catalogByName = new Map((currentWorkspace?.skillCatalog || []).map((skill) => [skill.name.toLowerCase(), skill]));
  const missing = wantedNames.filter((name) => !catalogByName.has(name.toLowerCase()));
  if (missing.length) throw new Error(`These skills aren't in the backend skill catalog yet: ${missing.join(', ')}. Choose a catalog skill or switch to demo mode.`);

  const wanted = new Set(wantedNames.map((name) => name.toLowerCase()));
  for (const record of currentRecords) {
    if (!wanted.has(record.skillName.toLowerCase())) {
      await apiRequest(`/api/v1/users/me/skills/${record.id}`, { method: 'DELETE' });
    }
  }
  const existing = new Set(currentRecords.map((record) => record.skillName.toLowerCase()));
  for (const name of wantedNames) {
    if (existing.has(name.toLowerCase())) continue;
    const skill = catalogByName.get(name.toLowerCase());
    await apiRequest('/api/v1/users/me/skills', {
      method: 'POST',
      body: { skillId: skill.id, skillType: type, proficiencyLevel: 'INTERMEDIATE', yearsExperience: null },
    });
  }
}

export const backendApi = {
  mode: 'backend',
  async load() { return loadWorkspace(); },
  async searchDirectory(query = '', page = 0, size = 50) {
    const params = new URLSearchParams({ page: String(page), size: String(size) });
    if (query.trim()) params.set('query', query.trim());
    const result = await apiRequest(`/api/v1/users/search?${params}`);
    const knownPeers = new Map((currentWorkspace?.peers || []).map((peer) => [peer.id, peer]));
    return {
      ...result,
      content: (result.content || []).map((profile) => {
        const peer = mapPeer(profile);
        const known = knownPeers.get(peer.id);
        const outgoingRequestId = outgoingRequests.get(Number(peer.id));
        return known
          ? { ...peer, ...known, isDirectoryProfile: true }
          : outgoingRequestId
            ? { ...peer, status: 'pending', direction: 'outgoing', connectionId: outgoingRequestId, isDirectoryProfile: true }
            : { ...peer, isDirectoryProfile: true };
      }),
    };
  },
  async authenticate({ name, email, password }, mode = 'login') {
    const authResponse = await apiRequest(`/api/v1/auth/${mode === 'signup' ? 'register' : 'login'}`, {
      method: 'POST',
      auth: false,
      body: mode === 'signup' ? { name, email, password } : { email, password },
    });
    saveApiSession(authResponse);
    outgoingRequests.clear();
    return loadWorkspace();
  },
  async updateUser(updates) {
    const user = currentWorkspace?.user;
    if (!user) throw new Error('Your profile is not loaded. Please sign in again.');
    const profilePayload = {};
    if (updates.name !== undefined) profilePayload.name = updates.name;
    if (updates.bio !== undefined) profilePayload.bio = updates.bio;
    if (updates.location !== undefined) profilePayload.location = updates.location;
    if (updates.interests !== undefined) profilePayload.interests = updates.interests;
    if (Object.keys(profilePayload).length) await apiRequest('/api/v1/users/me', { method: 'PUT', body: profilePayload });
    if (updates.teaches !== undefined) await synchronizeSkills('TEACH', user.teachSkillRecords, updates.teaches);
    if (updates.learns !== undefined) await synchronizeSkills('LEARN', user.learnSkillRecords, updates.learns);
    return loadWorkspace();
  },
  async setPeerStatus(peerId, status) {
    if (status === 'pending') {
      const result = await apiRequest('/api/v1/connections', { method: 'POST', body: { receiverId: Number(peerId) } });
      outgoingRequests.set(Number(peerId), result.id);
    } else if (status === 'connected') {
      await apiRequest(`/api/v1/connections/${peerId}/accept`, { method: 'PATCH' });
      for (const [id, connectionId] of outgoingRequests.entries()) if (connectionId === peerId) outgoingRequests.delete(id);
    }
    return loadWorkspace();
  },
  async sendMessage(peerId, text) {
    let conversationId = currentWorkspace?.conversationIds?.[String(peerId)];
    if (!conversationId) {
      const conversation = await apiRequest('/api/v1/chat/conversations', {
        method: 'POST',
        body: { otherUserId: Number(peerId) },
      });
      conversationId = conversation.id;
    }
    await apiRequest(`/api/v1/chat/conversations/${conversationId}/messages`, {
      method: 'POST',
      body: { content: text.trim(), messageType: 'TEXT' },
    });
    return loadWorkspace();
  },
  async markRecommendationViewed(recommendationId) {
    await apiRequest(`/api/v1/recommendations/${recommendationId}/viewed`, { method: 'PATCH' });
    return loadWorkspace();
  },
  async toggleLogin(loggedIn) {
    if (!loggedIn) {
      clearApiSession();
      outgoingRequests.clear();
    }
    return loadWorkspace();
  },
  async reset() {
    clearApiSession();
    outgoingRequests.clear();
    currentWorkspace = null;
    return loadWorkspace();
  },
};
