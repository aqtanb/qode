import { execFileSync } from 'node:child_process';
import { createClient } from '@supabase/supabase-js';
import ws from 'ws';

// The project is hard-coded so these scripts can never run against prod.
const PROJECT_REF = 'sjfzxrnqqlgywwqofdhu';

type ApiKey = { name: string; type: string; api_key: string };

function apiKeys(): ApiKey[] {
  const output = execFileSync(
    'supabase',
    ['projects', 'api-keys', '--project-ref', PROJECT_REF, '-o', 'json', '--reveal'],
    { encoding: 'utf8', stdio: ['ignore', 'pipe', 'ignore'] },
  );
  return JSON.parse(output.slice(output.indexOf('[')));
}

export function adminClientConfig() {
  const keys = apiKeys();
  const find = (type: string) => keys.find((key) => key.type === type)?.api_key;
  return {
    url: `https://${PROJECT_REF}.supabase.co`,
    secretKey: find('secret')!,
    publishableKey: find('publishable')!,
  };
}

// Node 20 has no built-in WebSocket, which supabase-js requires at startup.
export function clientFor(key: string) {
  const { url } = adminClientConfig();
  return createClient(url, key, { auth: { persistSession: false }, realtime: { transport: ws as never } });
}
