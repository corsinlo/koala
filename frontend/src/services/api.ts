export const api = {
  baseUrl: (import.meta.env.VITE_API_BASE || 'http://localhost:8080/api') as string,

  async get<T = any>(path: string): Promise<T> {
    const res = await fetch(this.baseUrl + path)
    if (!res.ok) throw new Error(await res.text())
    return res.json()
  },

  async post<T = any>(path: string, body?: any): Promise<T> {
    const opts: RequestInit = { method: 'POST' }
    if (body) { opts.headers = {'Content-Type':'application/json'}; opts.body = JSON.stringify(body) }
    const res = await fetch(this.baseUrl + path, opts)
    if (!res.ok) throw new Error(await res.text())
    try { return await res.json() } catch { return {} as T }
  }
}
