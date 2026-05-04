import { useState, useEffect, useCallback } from 'react'
import { fetchHistory } from '../api'

export default function HistoryPage() {
  const [data, setData] = useState([])
  const [total, setTotal] = useState(0)
  const [page, setPage] = useState(1)
  const [user, setUser] = useState('')
  const [keyword, setKeyword] = useState('')
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')
  const pageSize = 20

  const load = useCallback(async (p, u, k) => {
    setLoading(true)
    setError('')
    try {
      const res = await fetchHistory(p, pageSize, u, k)
      if (res.error) { setError(res.error); setData([]); setTotal(0) }
      else { setData(res.data || []); setTotal(res.total || 0) }
    } catch (e) {
      setError('无法连接到服务器，请确认代理已启动')
      setData([]); setTotal(0)
    }
    setLoading(false)
  }, [])

  useEffect(() => { load(page, user, keyword) }, [page])

  const handleSearch = () => {
    setPage(1)
    load(1, user, keyword)
  }

  const totalPages = Math.ceil(total / pageSize)

  return (
    <div>
      <div className="page-header"><h1>浏览记录</h1></div>

      <div className="toolbar">
        <input placeholder="用户名" value={user} onChange={e => setUser(e.target.value)} style={{width:130}} />
        <input placeholder="域名/URL 关键字" value={keyword} onChange={e => setKeyword(e.target.value)} style={{width:240}} />
        <button className="btn btn-primary" onClick={handleSearch} disabled={loading}>搜索</button>
      </div>

      {error && <div className="error">{error}</div>}

      <table>
        <thead>
          <tr><th style={{width:80}}>ID</th><th style={{width:120}}>用户</th><th>URL</th><th style={{width:170}}>访问时间</th></tr>
        </thead>
        <tbody>
          {data.length === 0 && !loading && <tr><td colSpan={4} className="empty">暂无数据</td></tr>}
          {data.map(r => (
            <tr key={r.id}>
              <td>{r.id}</td>
              <td>{r.userName}</td>
              <td className="url" title={r.url}>{r.url}</td>
              <td>{r.visitTime}</td>
            </tr>
          ))}
        </tbody>
      </table>

      <div className="pagination">
        <button disabled={page <= 1} onClick={() => setPage(p => p - 1)}>上一页</button>
        <span>第 {page} / {totalPages || 1} 页（共 {total} 条）</span>
        <button disabled={page >= totalPages} onClick={() => setPage(p => p + 1)}>下一页</button>
      </div>
    </div>
  )
}
