import axios from 'axios'
import router from '../router'

const api = axios.create({
  baseURL: 'http://127.0.0.1:8081',
  withCredentials: true,
  headers: {
    'Accept': 'application/json',
  },
})

api.interceptors.response.use(
  (res) => res,
  (err) => {
    if (err.response?.status === 401) {
      localStorage.removeItem('loggedIn')
      router.push('/login')
    }
    return Promise.reject(err)
  }
)

export default api