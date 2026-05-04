const BASE = '/api'

export async function fetchHistory(page = 1, size = 20, user = '', keyword = '') {
  const params = new URLSearchParams({ page, size, user, keyword })
  const res = await fetch(`${BASE}/history?${params}`)
  return res.json()
}

export async function fetchBlacklist() {
  const res = await fetch(`${BASE}/blacklist`)
  return res.json()
}

export async function addBlacklist(url) {
  const res = await fetch(`${BASE}/blacklist`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ url })
  })
  return res.json()
}

export async function deleteBlacklist(id) {
  const res = await fetch(`${BASE}/blacklist/${id}`, { method: 'DELETE' })
  return res.json()
}
