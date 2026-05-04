import { useState, useEffect, useCallback } from 'react'
import { fetchBlacklist, addBlacklist, deleteBlacklist } from '../api'

export default function BlacklistPage() {
  const [list, setList] = useState([])
  const [newUrl, setNewUrl] = useState('')
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')

  const load = useCallback(async () => {
    setLoading(true)
    setError('')
    try {
      const res = await fetchBlacklist()
      if (Array.isArray(res)) setList(res)
      else if (res.error) setError(res.error)
    } catch (e) {
      setError('无法连接到服务器，请确认代理已启动')
    }
    setLoading(false)
  }, [])

  useEffect(() => { load() }, [load])

  const handleAdd = async () => {
    const url = newUrl.trim()
    if (!url) return
    try {
      const res = await addBlacklist(url)
      if (res.error) { setError(res.error) } else { setNewUrl(''); load() }
    } catch (e) { setError('添加失败') }
  }

  const handleDelete = async (id) => {
    try {
      await deleteBlacklist(id)
      load()
    } catch (e) { setError('删除失败') }
  }

  return (
    <div>
      <div className="page-header"><h1>黑名单管理</h1></div>

      <div className="add-form">
        <input placeholder="输入要拦截的域名（如 baidu.com）"
          value={newUrl} onChange={e => setNewUrl(e.target.value)}
          onKeyDown={e => e.key === 'Enter' && handleAdd()} />
        <button className="btn btn-primary" onClick={handleAdd} disabled={loading}>添加</button>
      </div>

      {error && <div className="error">{error}</div>}

      <table>
        <thead>
          <tr><th style={{width:80}}>ID</th><th>拦截域名</th><th style={{width:170}}>添加时间</th><th style={{width:80}}>操作</th></tr>
        </thead>
        <tbody>
          {list.length === 0 && !loading && <tr><td colSpan={4} className="empty">黑名单为空</td></tr>}
          {list.map(e => (
            <tr key={e.id}>
              <td>{e.id}</td>
              <td>{e.url}</td>
              <td>{e.addTime}</td>
              <td><button className="btn btn-danger btn-sm" onClick={() => handleDelete(e.id)}>删除</button></td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  )
}
