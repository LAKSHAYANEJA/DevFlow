import axios from 'axios';

const api = axios.create({
  baseURL: import.meta.env.VITE_API_URL || 'http://localhost:8080',
  headers: {
    'Content-Type': 'application/json',
  },
});

// Attach token to every request 

api.interceptors.request.use(config => {
    const token = localStorage.getItem('accessToken');
    if(token) config.headers.Authorization = `Bearer ${token}`;
    return config;
});

// Handle 401 - redirect to login
api.interceptors.response.use(
    res => res,
    err => {
        if(err.response?.status === 401) {
            localStorage.removeItem('accessToken');
            window.location.href = '/login';
        }
        return Promise.reject(err);
    }
)

export default api;