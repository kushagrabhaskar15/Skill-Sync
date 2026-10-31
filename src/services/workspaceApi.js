import { demoApi } from './demoApi.js';
import { backendApi } from './backendApi.js';

const configuredMode = import.meta.env.VITE_API_MODE?.trim().toLowerCase();
const inferredMode = import.meta.env.VITE_API_BASE_URL ? 'backend' : 'demo';
export const workspaceApi = configuredMode === 'backend' || (!configuredMode && inferredMode === 'backend')
  ? backendApi
  : demoApi;

export const apiMode = workspaceApi.mode;
