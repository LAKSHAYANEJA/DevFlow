import { useState, useEffect } from "react";
import { useLocation, useNavigate } from "react-router-dom";
import api from "../api/axios";
import Navbar from "../components/Navbar";
import toast from "react-hot-toast";

export default function Projects() {
    const [projects, setProjects] = useState([]);
    const [loading, setLoading] = useState(true);
    const [showModal, setShowModal] = useState(false);
    const [name, setName] = useState('');
    const [description, setDescription] = useState('');
    const [creating, setCreating] = useState(false);
    const navigate = useNavigate();
    const location = useLocation();

    useEffect(() => {
        fetchProjects();
    }, [location.key]);

    const fetchProjects = async () => {
        try {
            const res = await api.get('/api/v1/projects');
            setProjects(res.data);
        }
        catch{
            toast.error('Failed to load projects');
        }
        finally{
            setLoading(false);
        }
    };

    const createProject = async (e) => {
        e.preventDefault();
        setCreating(true);
        try{
            const res = await api.post('/api/v1/projects', {name, description, isPublic: false});
            setProjects(prev => [res.data, ...prev]);
            setShowModal(false);
            setName('');
            setDescription('');
            toast.success('Project created!');
        }
        catch{
            toast.error('Failed to create project');
        }
        finally{
            setLoading(false);
        }
    };

    const deleteProject = async (id, e) => {
        e.stopPropagation();
        if(!confirm('Delete this project?')) return ;
        try{
            await api.delete(`/api/v1/projects/${id}`);
            setProjects(prev => prev.filter(p => p.id !== id));
            toast.success('Project deleted');
        }
        catch{
            toast.error('Failed to delete project');
        }
    };

      return (
    <div className="min-h-screen bg-[var(--bg)]">
      <Navbar />

      <div className="max-w-6xl mx-auto px-6 py-8">
        {/* Header */}
        <div className="flex items-center justify-between mb-8">
          <div>
            <h1 className="text-2xl font-semibold text-[var(--text)]">Projects</h1>
            <p className="text-sm text-[var(--text-secondary)] mt-1">
              {projects.length} project{projects.length !== 1 ? 's' : ''}
            </p>
          </div>
          <button
            onClick={() => setShowModal(true)}
            className="flex items-center gap-2 px-4 py-2 bg-primary-500 hover:bg-primary-600 text-white text-sm font-medium rounded-lg transition-colors"
          >
            <span className="text-lg leading-none">+</span>
            New project
          </button>
        </div>

        {/* Projects grid */}
        {loading ? (
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
            {[...Array(3)].map((_, i) => (
              <div key={i} className="h-40 bg-[var(--surface)] border border-[var(--border)] rounded-xl animate-pulse" />
            ))}
          </div>
        ) : projects.length === 0 ? (
          <div className="text-center py-20">
            <div className="w-14 h-14 bg-[var(--surface)] border border-[var(--border)] rounded-xl flex items-center justify-center mx-auto mb-4">
              <svg width="24" height="24" viewBox="0 0 24 24" fill="var(--text-secondary)">
                <path d="M9 11H7v2h2v-2zm4 0h-2v2h2v-2zm4 0h-2v2h2v-2zm2-7h-1V2h-2v2H8V2H6v2H5c-1.11 0-1.99.9-1.99 2L3 20c0 1.1.89 2 2 2h14c1.1 0 2-.9 2-2V6c0-1.1-.9-2-2-2zm0 16H5V9h14v11z"/>
              </svg>
            </div>
            <h3 className="text-sm font-medium text-[var(--text)] mb-1">No projects yet</h3>
            <p className="text-sm text-[var(--text-secondary)] mb-4">Create your first project to get started</p>
            <button
              onClick={() => setShowModal(true)}
              className="px-4 py-2 bg-primary-500 hover:bg-primary-600 text-white text-sm font-medium rounded-lg transition-colors"
            >
              Create project
            </button>
          </div>
        ) : (
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
            {projects.map(project => (
              <div
                key={project.id}
                onClick={() => navigate(`/projects/${project.id}/board`)}
                className="group bg-[var(--surface)] border border-[var(--border)] rounded-xl p-5 cursor-pointer hover:border-primary-500 hover:shadow-md transition-all"
              >
                <div className="flex items-start justify-between mb-3">
                  <div className="w-9 h-9 bg-primary-50 rounded-lg flex items-center justify-center">
                    <span className="text-primary-500 font-semibold text-sm">
                      {project.name.charAt(0).toUpperCase()}
                    </span>
                  </div>
                  <button
                    onClick={(e) => deleteProject(project.id, e)}
                    className="opacity-0 group-hover:opacity-100 p-1 rounded hover:bg-[var(--border)] text-[var(--text-secondary)] hover:text-red-500 transition-all"
                  >
                    <svg width="14" height="14" viewBox="0 0 24 24" fill="currentColor">
                      <path d="M6 19c0 1.1.9 2 2 2h8c1.1 0 2-.9 2-2V7H6v12zM19 4h-3.5l-1-1h-5l-1 1H5v2h14V4z"/>
                    </svg>
                  </button>
                </div>
                <h3 className="font-medium text-[var(--text)] text-sm mb-1">{project.name}</h3>
                <p className="text-xs text-[var(--text-secondary)] line-clamp-2">
                  {project.description || 'No description'}
                </p>
                <div className="mt-4 flex items-center justify-between">
                  <span className="text-xs text-[var(--text-secondary)]">
                    {new Date(project.createdAt).toLocaleDateString()}
                  </span>
                  <span className={`text-xs px-2 py-0.5 rounded-full ${
                    project.isPublic
                      ? 'bg-green-50 text-green-600'
                      : 'bg-[var(--border)] text-[var(--text-secondary)]'
                  }`}>
                    {project.isPublic ? 'Public' : 'Private'}
                  </span>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>

      {/* Create project modal */}
      {showModal && (
        <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50 p-4">
          <div className="bg-[var(--surface)] border border-[var(--border)] rounded-xl p-6 w-full max-w-md shadow-xl">
            <h2 className="text-base font-semibold text-[var(--text)] mb-4">New project</h2>
            <form onSubmit={createProject} className="space-y-4">
              <div>
                <label className="block text-sm font-medium text-[var(--text)] mb-1.5">Project name</label>
                <input
                  type="text"
                  value={name}
                  onChange={e => setName(e.target.value)}
                  required
                  autoFocus
                  placeholder="e.g. My Awesome Project"
                  className="w-full px-3 py-2.5 text-sm bg-[var(--bg)] border border-[var(--border)] rounded-lg text-[var(--text)] placeholder-[var(--text-secondary)] focus:outline-none focus:ring-2 focus:ring-primary-500 focus:border-transparent"
                />
              </div>
              <div>
                <label className="block text-sm font-medium text-[var(--text)] mb-1.5">Description <span className="text-[var(--text-secondary)] font-normal">(optional)</span></label>
                <textarea
                  value={description}
                  onChange={e => setDescription(e.target.value)}
                  rows={3}
                  placeholder="What is this project about?"
                  className="w-full px-3 py-2.5 text-sm bg-[var(--bg)] border border-[var(--border)] rounded-lg text-[var(--text)] placeholder-[var(--text-secondary)] focus:outline-none focus:ring-2 focus:ring-primary-500 focus:border-transparent resize-none"
                />
              </div>
              <div className="flex gap-3 pt-2">
                <button
                  type="button"
                  onClick={() => setShowModal(false)}
                  className="flex-1 py-2.5 text-sm font-medium text-[var(--text-secondary)] bg-[var(--bg)] border border-[var(--border)] rounded-lg hover:bg-[var(--border)] transition-colors"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  disabled={creating}
                  className="flex-1 py-2.5 text-sm font-medium text-white bg-primary-500 hover:bg-primary-600 disabled:opacity-60 rounded-lg transition-colors flex items-center justify-center"
                >
                  {creating ? (
                    <div className="w-4 h-4 border-2 border-white border-t-transparent rounded-full animate-spin" />
                  ) : 'Create project'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );


}