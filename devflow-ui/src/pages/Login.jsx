import { useState } from 'react';
import {Link, useNavigate} from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import toast from 'react-hot-toast';

export default function Login() {
    const [email, setEmail] = useState('');
    const [password, setPassword] = useState('');
    const [loading, setLoading] = useState(false);
    const {login} = useAuth();
    const navigate = useNavigate();

    const handleSubmit = async (e) => {
        e.preventDefault();
        setLoading(true);
        try{
            await login(email, password);
            toast.success('Welcome back!');
            navigate('/projects');
        } catch (err) {
            toast.error(err.response?.data?.message || 'Login failed');
        }
        finally{
            setLoading(false);
        }
    };

    return (
        <div className="className= min-h-screen flex items-center justify-center bg-[var(--bg)]">
            <div className="w-full max-w-md">
                {/* Logo */}
                <div className="text-center mb-8">
                    <div className="inline-flex items-center gap-2 mb-4">
                        <div className="w-8 h-8 bg-primary-500 rounded-lg flex items-center justify-center">
                            <svg width="18" height="18" viewBox="0 0 24 24" fill="white">
                                <path d="M9 11H7v2h2v-2zm4 0h-2v2h2v-2zm4 0h-2v2h2v-2zm2-7h-1V2h-2v2H8V2H6v2H5c-1.11 0-1.99.9-1.99 2L3 20c0 1.1.89 2 2 2h14c1.1 0 2-.9 2-2V6c0-1.1-.9-2-2-2zm0 16H5V9h14v11z"/>                               
                            </svg>
                        </div>
                        <span className="text-xl font-semibold text-[var(--text)]">DevFlow</span>
                    </div>
                    <h1 className="text-2xl font-semibold text-[var(--text)]">Sign in to your account</h1>
                    <p className="text-sm text-[var(--text-secondary)] mt-1">Don't have an account? <Link to="/register" className="text-primary-500 hover:text-primary-600 font-medium">Create one</Link></p>
                </div>

                {/* Card */}
                <div className="bg-[var(--surface)] border border-[var(--border)] rounded-xl p-8 shadow-sm">
                    <form onSubmit={handleSubmit} className="space-y-4">
                        <div>
                            <label className="block text-sm font-medium text-[var(--text)] mb-1.5">Email address</label>
                            <input type="email" value={email} onChange={e => setEmail(e.target.value)} required placeholder="you@example.com" 
                            className="w-full px-3 py-2.5 text-sm bg-[var(--bg)] border border-[var(--border)] rounded-lg text-[var(--text)] placeholder-[var(--text-secondary)] focus:outline-none focus:ring-2 focus:ring-primary-500 focus:border-transparent transition-all" />
                        </div>

                        <div>
                            <label className="block text-sm font-medium text-[var(--text)] mb-1.5">Password</label>
                            <input type="password" value={password} onChange={e => setPassword(e.target.value)} required placeholder="••••••••" 
                            className="w-full px-3 py-2.5 text-sm bg-[var(--bg)] border border-[var(--border)] rounded-lg text-[var(--text)] placeholder-[var(--text-secondary)] focus:outline-none focus:ring-2 focus:ring-primary-500 focus:border-transparent transition-all" />
                        </div>

                        <button type="submit" disabled={loading} className="w-full py-2.5 px-4 bg-primary-500 hover:bg-primary-600 disabled:opacity-60 text-white text-sm font-medium rounded-lg transition-colors flex items-center justify-center gap-2">
                            {loading ? (
                                <div className="w-4 h-4 border-2 border-white border-t-transparent rounded-full animate-spin" />
                            ) : 'Sign in'}
                        </button>

                        <div className="flex justify-end">
                            <button type="button"
                                    onClick={() => toast('Password reset via email coming soon', { icon: '🔐' })}
                                    className="text-xs text-primary-500 hover:text-primary-600 transition-colors">
                                        Forgot password?
                            </button>
                        </div>

                    </form>
                </div>

            </div>
        </div>
    );
}