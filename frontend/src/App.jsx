import { Routes, Route, NavLink, Navigate } from 'react-router-dom'
import HistoryPage from './pages/HistoryPage'
import BlacklistPage from './pages/BlacklistPage'

export default function App() {
  return (
    <div className="app">
      <nav className="sidebar">
        <h2>Network Trace</h2>
        <ul>
          <li><NavLink to="/history">浏览记录</NavLink></li>
          <li><NavLink to="/blacklist">黑名单管理</NavLink></li>
        </ul>
      </nav>
      <main className="content">
        <Routes>
          <Route path="/" element={<Navigate to="/history" replace />} />
          <Route path="/history" element={<HistoryPage />} />
          <Route path="/blacklist" element={<BlacklistPage />} />
        </Routes>
      </main>
    </div>
  )
}
