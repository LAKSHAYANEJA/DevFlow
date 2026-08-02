import { useState, useEffect } from "react";
import { useParams, useNavigate } from "react-router-dom";
import api from '../api/axios';
import Navbar from "../components/Navbar";
import toast from "react-hot-toast";

export default function Members(){
    const {id} = useParams();
    const navigate = useNavigate();

    const [project, setProject] = useState(null);
    const [members, setMembers] = useState([]);
    const [loading, setLoading] = useState(true);
    const [email, setEmail] = useState('');
    const [inviting, setInviting] = useState(false);

    useEffect(() => {
        fetchAll();
    }, [id]);

    const fetchAll = async () => {
        try{
            const [projectRes, memberRes] = await Promise.all([
                api.get(`/pi/v1/projects/${id}`),
                api.get(`/pi/v1/projects/${id}/members`),
            ]);
        }
            catch{
                toast.error('Failed to load members');
            }
            finally{
                setLoading(false);
            }
        
    };

    const inviteMember = async (e) => {
        e.preventDefault();
        setInviting(true);
        try{
            const res = await api.post(`/api/v1/projects/${id}/members`, {email});
            setMembers(prev => [...prev, res.data]);
            setEmail('');
            toast.success('Member invited!');
        }
        catch (err){
            toast.error(err.response?.data?.message || 'Failed to invite member');
        }
        finally{
            setInviting(false);
        }
    };

    const removeMember = async (useRevalidator, name) => {
        if(!confirm(`Remove ${name} from this project?`)) return ;

        try{
            await api.delete(`/api/v1/projects/${id}/members/${userId}`);
            setMembers(prev => prev.filter(m => m.userId !== userId));
            toast.success('Member removed');
        }
        catch{
            toast.error('Failed to remove member');
        }
    };

    if (loading) return (
        <div className="min-h-screen bg-[var(--bg)]">
            <Navbar />

            <div className="flex items-center justify-center h-[calc(100vh-56px)]">
                <div className="w-8 h-8 border-2 border-primary-500 border-t-transparent rounded-full animate-spin" />
            </div>
        </div>
    );

    return (
        <div className="min-h-screen bg-[var(--bg)]">
            <Navbar />
            <div className="max-w-2xl mx-auto px-6 py-8">
              {/* Header */}
              <div className="flex items-center gap-3 mb-8">
                <button 
                onClick={() => navigate(`/projects/${id}/board`)}
                className="text-[var(--text-secondary)] hover:text-[var(--text)] transition-colors">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="currentColor">
                <path d="M20 11H7.83l5.59-5.59L12 4l-8 8 8 8 1.41-1.41L7.83 13H20v-2z"/>
                </svg>
                </button>
                 <div>
            <h1 className="text-xl font-semibold text-[var(--text)]">Members</h1>
            <p className="text-xs text-[var(--text-secondary)] mt-0.5">{project?.name}</p>
          </div>
            </div>
         {/* Invite form */}
        <div className="bg-[var(--surface)] border border-[var(--border)] rounded-xl p-5 mb-6">
          <h2 className="text-sm font-medium text-[var(--text)] mb-3">Invite a member</h2>
        <form onSubmit={inviteMember} className="flex gap-3">
            <input
                type="email"
                placeholder="Enter email"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                className="flex-1 px-3 py-2 text-sm bg-[var(--bg)] border border-[var(--border)] rounded-lg text-[var(--text)] placeholder-[var(--text-secondary)] focus:outline-none focus:ring-2 focus:ring-primary-500 focus:border-transparent"
                required
            />
             <button
              type="submit"
              disabled={inviting}
              className="px-4 py-2 bg-primary-500 hover:bg-primary-600 disabled:opacity-60 text-white text-sm font-medium rounded-lg transition-colors flex items-center gap-2"
            >
              {inviting ? (
                <div className="w-4 h-4 border-2 border-white border-t-transparent rounded-full animate-spin" />
              ) : 'Invite'}
            </button>
        </form>
        </div>

        {/* Members list */}
        <div className="bg-[var(--surface)] border border-[var(--border)] rounded-xl overflow-hidden">
          <div className="px-5 py-3 border-b border-[var(--border)]">
            <h2 className="text-sm font-medium text-[var(--text)]">
              {members.length} member{members.length !== 1 ? 's' : ''}
            </h2>
          </div>
           <div className="divide-y divide-[var(--border)]">
            {members.map((member) => (
              <div key={member.userId} className="flex items-center justify-between px-5 py-3">
               <div className="flex items-center gap-3">
                   <div className="w-8 h-8 rounded-full bg-primary-500 flex items-center justify-center text-white text-xs font-semibold">
                    {member.name?.charAt(0).toUpperCase()}
                    </div> 
                    <div>
                        <p className="text-sm font-medium text-[var(--text)]">{member.name}</p>
                        <p className="text-xs text-[var(--text-secondary)]">{member.email}</p>
                    </div>
               </div>
               <div className="flex items-center gap-3">
                    <span className={`text-xs px-2 py-0.5 rounded-full font-medium ${
                    member.role === 'OWNER'
                      ? 'bg-primary-50 text-primary-600'
                      : 'bg-[var(--border)] text-[var(--text-secondary)]'
                  }`}>
                    {member.role}
                  </span>
                  {member.role !== 'OWNER' && (
                    <button
                    onClick={() => removeMember(member.userId, member.name)}
                    className="text-[var(--text-secondary)] hover:text-red-500 transition-colors p-1 rounded hover:bg-red-50"
                    >
                         <svg width="14" height="14" viewBox="0 0 24 24" fill="currentColor">
                        <path d="M6 19c0 1.1.9 2 2 2h8c1.1 0 2-.9 2-2V7H6v12zM19 4h-3.5l-1-1h-5l-1 1H5v2h14V4z"/>
                      </svg>

                    </button>
                  )}
               </div>
               </div>
            ))}
           </div>
        </div>

        </div>
        </div>
    );
}