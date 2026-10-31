const API_BASE_URL = (import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080').replace(/\/$/, '');
const SESSION_KEY = 'skillsync-api-session';
let memorySession = null;

export class ApiError extends Error {
  constructor(message, status, details) {
    super(message);
    this.name = 'ApiError';
    this.status = status;
    this.details = details;
  }
}

export function readApiSession() {
  try {
    const raw = window.sessionStorage.getItem(SESSION_KEY);
    const session = raw ? JSON.parse(raw) : memorySession;
    if (session?.expiresAt && session.expiresAt <= Date.now()) {
      clearApiSession();
      return null;
    }
    memorySession = session ?? null;
    return memorySession;
  } catch {
    return memorySession;
  }
}

export function saveApiSession(authResponse) {
  const session = {
    accessToken: authResponse.accessToken,
    tokenType: authResponse.tokenType || 'Bearer',
    expiresAt: Date.now() + Number(authResponse.expiresIn || 0) * 1000,
    user: authResponse.userSummary,
  };
  memorySession = session;
  try {
    window.sessionStorage.setItem(SESSION_KEY, JSON.stringify(session));
  } catch {
    // Keep the session in memory for this tab if browser storage is unavailable.
  }
  return session;
}

export function clearApiSession() {
  memorySession = null;
  try {
    window.sessionStorage.removeItem(SESSION_KEY);
  } catch {
    // The in-memory session is still cleared.
  }
}

export async function apiRequest(path, { method = 'GET', body, auth = true, signal } = {}) {
  const headers = new Headers({ Accept: 'application/json' });
  if (body !== undefined) headers.set('Content-Type', 'application/json');
  const session = auth ? readApiSession() : null;
  if (session?.accessToken) headers.set('Authorization', `${session.tokenType} ${session.accessToken}`);

  let response;
  try {
    response = await fetch(`${API_BASE_URL}${path}`, {
      method,
      headers,
      body: body === undefined ? undefined : JSON.stringify(body),
      signal,
    });
  } catch {
    throw new ApiError(`Can't reach SkillSync API at ${API_BASE_URL}. Start the backend or switch VITE_API_MODE to demo.`, 0);
  }

  const payload = response.status === 204 ? null : await response.json().catch(() => null);
  if (!response.ok) {
    if (response.status === 401 && auth) clearApiSession();
    const fieldMessages = Object.values(payload?.fieldErrors || {});
    const message = [payload?.message || payload?.error, ...fieldMessages].filter(Boolean).join(': ')
      || `SkillSync API request failed (${response.status}).`;
    throw new ApiError(message, response.status, payload);
  }
  return payload;
}

export function getApiBaseUrl() {
  return API_BASE_URL;
}
