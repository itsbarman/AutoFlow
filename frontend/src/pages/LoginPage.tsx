import { useState, type FormEvent } from 'react';
import { useAuth } from '../auth/AuthContext';
import { ApiRequestError } from '../api/client';
import { TextField } from '../components/TextField';
import { Button } from '../components/Button';

export function LoginPage() {
  const { login } = useAuth();
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState<string | null>(null);
  const [submitting, setSubmitting] = useState(false);

  async function handleSubmit(e: FormEvent) {
    e.preventDefault();
    setError(null);
    setSubmitting(true);
    try {
      await login(username.trim(), password);
    } catch (err) {
      if (err instanceof ApiRequestError) {
        setError(err.message);
      } else {
        setError('Kunne ikke logge inn. Sjekk at API-et kjører.');
      }
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <div className="flex min-h-screen items-center justify-center bg-slate-50 px-4">
      <div className="w-full max-w-sm">
        <div className="mb-6 flex flex-col items-center gap-2">
          <div className="flex h-12 w-12 items-center justify-center rounded-xl bg-brand-600 text-xl font-bold text-white">
            A
          </div>
          <h1 className="text-xl font-semibold text-slate-800">AutoFlow</h1>
          <p className="text-sm text-slate-400">Logg inn for å fortsette</p>
        </div>

        <form
          onSubmit={handleSubmit}
          className="rounded-2xl border border-slate-200 bg-white p-6 shadow-sm"
          noValidate
        >
          {error && (
            <div className="mb-4 rounded-lg border border-red-200 bg-red-50 px-3 py-2 text-sm text-red-700">
              {error}
            </div>
          )}

          <TextField
            id="username"
            label="Brukernavn"
            required
            autoComplete="username"
            value={username}
            onChange={(e) => setUsername(e.target.value)}
            className="mb-4"
          />
          <TextField
            id="password"
            label="Passord"
            type="password"
            required
            autoComplete="current-password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            className="mb-6"
          />

          <Button type="submit" loading={submitting} className="w-full">
            Logg inn
          </Button>
        </form>
      </div>
    </div>
  );
}
