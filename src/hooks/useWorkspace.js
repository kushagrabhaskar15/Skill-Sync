import { useCallback, useEffect, useState } from 'react';
import { demoApi } from '../services/demoApi.js';

export function useWorkspace() {
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  const refresh = useCallback(async () => {
    setLoading(true);
    setError('');
    try {
      setData(await demoApi.load());
    } catch (cause) {
      setError(cause instanceof Error ? cause.message : 'Something went wrong while loading the demo.');
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => { refresh(); }, [refresh]);

  const mutate = useCallback(async (action) => {
    setError('');
    try {
      const next = await action();
      setData(next);
      return next;
    } catch (cause) {
      const message = cause instanceof Error ? cause.message : 'That action could not be completed.';
      setError(message);
      throw new Error(message);
    }
  }, []);

  return { data, setData, loading, error, setError, refresh, mutate };
}
