import { createContext, useContext, useState, useEffect } from 'react';
import api from '../api/axios';

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
    const [user, setUser] = useState(null);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        const token = localStorage.getItem('accessToken');
        if(token){
            api.get('/api/v1/users/me')
            .then(res => {
                setUser(res.data);
            })
            .catch(() => localStorage.removeItem('accessToken'))
            .finally(() => setLoading(false));
        }
        else{
            setLoading(false);
        }
}
, []);

const login = async (email, password) => {
    const res = await api.post('/api/v1/auth/login', { email, password });
    localStorage.setItem('accessToken', res.data.accessToken);
    setUser(res.data.user);
    return res.data;
};

const register = async (name, email, password) => {
    const res = await api.post('/api/v1/auth/register', {name, email, password});
    localStorage.setItem('accessToken', res.data.accessToken);
    setUser(res.data.user);
    return res.data;
};

const logout = () => {
    localStorage.removeItem('accessToken');
    setUser(null);
};

return (
    <AuthContext.Provider value={{ user, loading, login, register, logout}}>
        {children}
    </AuthContext.Provider>
);

}

export const useAuth = () => useContext(AuthContext);