import { useState, useEffect } from "react";
import { useParams, useNavigate } from "react-router-dom";
import api from "../api/axios";
import Navbar from "../components/Navbar";
import toast from "react-hot-toast";
 
const STATUSES = ['TODO', 'IN_PROGRESS', 'IN_REVIEW', 'DONE'];
 
const STATUS_CONFIG = {
    TODO: { label: 'To Do', color: 'bg-slate-100 text-slate-600' },
    IN_PROGRESS: { label: 'In Progress', color: 'bg-blue-100 text-blue-600' },
    IN_REVIEW: { label: 'In Review', color: 'bg-yellow-100 text-yellow-700' },
    DONE: { label: 'Done', color: 'bg-green-100 text-green-600' }
};
 
const PRIORITY_CONFIG = {
    URGENT: { label: 'Urgent', color: 'text-red-500', dot: 'bg-red-500' },
    HIGH: { label: 'High', color: 'text-orange-500', dot: 'bg-orange-500' },
    MEDIUM: { label: 'Medium', color: 'text-yellow-500', dot: 'bg-yellow-500' },
    LOW: { label: 'Low', color: 'text-slate-500', dot: 'bg-slate-400' }
};
 
export default function Board() {
    const { id } = useParams();
    const navigate = useNavigate();
    const [project, setProject] = useState(null);
    const [tasks, setTasks] = useState([]);
    const [loading, setLoading] = useState(true);
    const [selectedTask, setSelectedTask] = useState(null);
    const [showCreateModal, setShowCreateModal] = useState(false);
    const [createStatus, setCreateStatus] = useState('TODO');
    const [activity, setActivity] = useState([]);
    const [labels, setLabels] = useState([]);
    const [showLabelForm, setShowLabelForm] = useState(false);
    const [labelName, setLabelName] = useState('');
    const [labelColor, setLabelColor] = useState('#0052CC');
    const [taskForm, setTaskForm] = useState({
        title: '', description: '', priority: 'MEDIUM', dueDate: ''
    });
    const [creating, setCreating] = useState(false);
 
    useEffect(() => {
        fetchAll();
    }, [id]);
 
    const fetchAll = async () => {
        setLoading(true);
        setTasks([]);
        setProject(null);
        setLabels([]);
        try {
            const [projectRes, tasksRes, labelsRes] = await Promise.all([
                api.get(`/api/v1/projects/${id}`),
                api.get(`/api/v1/projects/${id}/tasks?size=100`),
                api.get(`/api/v1/projects/${id}/labels`),
            ]);
            setProject(projectRes.data);
            setTasks(tasksRes.data.content || []);
            setLabels(labelsRes.data || []);
        } catch (err) {
          console.error('fetchAll error:', err.response?.status, err.response?.data);
            toast.error(`Failed to load board ${err.response?.status || 'network error'}`);
        } finally {
            setLoading(false);
        }
    };
 
    const fetchActivity = async (taskId) => {
        try {
            const res = await api.get(`/api/v1/tasks/${taskId}/activity`);
            setActivity(res.data);
        } catch {
            setActivity([]);
        }
    };
 
    const openTask = (task) => {
        setSelectedTask(task);
        fetchActivity(task.id);
    };
 
    const updateStatus = async (taskId, newStatus) => {
        try {
            const res = await api.patch(`/api/v1/tasks/${taskId}/status`, { status: newStatus });
            setTasks(prev => prev.map(t => t.id === taskId ? res.data : t));
            if (selectedTask?.id === taskId) {
                setSelectedTask(res.data);
                fetchActivity(taskId);
            }
            toast.success('Status updated');
        } catch {
            toast.error('Failed to update status');
        }
    };
 
    const deleteTask = async (taskId) => {
        if (!confirm('Delete this task?')) return;
        try {
            await api.delete(`/api/v1/tasks/${taskId}`);
            setTasks(prev => prev.filter(t => t.id !== taskId));
            setSelectedTask(null);
            toast.success('Task deleted');
        } catch {
            toast.error('Failed to delete task');
        }
    };

    const [submitted, setSubmitted] = useState(false);
 
    const createTask = async (e) => {
        e.preventDefault();
        if (submitted) return ;
        setSubmitted(true);
        setCreating(true);
        try {
            const res = await api.post(`/api/v1/projects/${id}/tasks`, {
                ...taskForm,
                status: createStatus,
                dueDate: taskForm.dueDate || null,
            });
            // setTasks(prev => [res.data, ...prev]);
            setShowCreateModal(false);
            setTaskForm({ title: '', description: '', priority: 'MEDIUM', dueDate: '' });
            toast.success('Task created!');
            await fetchAll(); // Refresh the board to include the new task
        } catch (err) {
            toast.error(err.response?.data?.message || 'Failed to create task');
        } finally {
            setCreating(false);
            setSubmitted(false);
        }
    };
 
    const createLabel = async (e) => {
        e.preventDefault();
        try {
            const res = await api.post(`/api/v1/projects/${id}/labels`, { name: labelName, color: labelColor });
            setLabels(prev => [...prev, res.data]);
            setLabelName('');
            setShowLabelForm(false);
            toast.success('Label created!');
        } catch {
            toast.error('Failed to create label');
        }
    };
 
    const attachLabel = async (taskId, labelId) => {
        try {
            await api.post(`/api/v1/tasks/${taskId}/labels/${labelId}`);
            const res = await api.get(`/api/v1/tasks/${taskId}`);
            setTasks(prev => prev.map(t => t.id === taskId ? res.data : t));
            setSelectedTask(res.data);
            toast.success('Label attached!');
        } catch {
            toast.error('Failed to attach label');
        }
    };
 
    const removeLabel = async (taskId, labelId) => {
        try {
            await api.delete(`/api/v1/tasks/${taskId}/labels/${labelId}`);
            const res = await api.get(`/api/v1/tasks/${taskId}`);
            setTasks(prev => prev.map(t => t.id === taskId ? res.data : t));
            setSelectedTask(res.data);
            toast.success('Label removed');
        } catch {
            toast.error('Failed to remove label');
        }
    };
 
    const tasksByStatus = (status) => tasks.filter(t => t.status === status);
    const isOverdue = (dueDate) => dueDate && new Date(dueDate) < new Date();
 
    if (loading) return (
        <div className="min-h-screen bg-[var(--bg)]">
            <Navbar />
            <div className="flex items-center justify-center h-[calc(100vh-56px)]">
                <div className="w-8 h-8 border-2 border-primary-500 border-t-transparent rounded-full animate-spin" />
            </div>
        </div>
    );
 
    return (
        <div className="min-h-screen bg-[var(--bg)] flex flex-col">
            <Navbar />
 
            {/* Board header */}
            <div className="border-b border-[var(--border)] bg-[var(--surface)] px-6 py-3 flex items-center justify-between">
                <div className="flex items-center gap-3">
                    <button
                        onClick={() => navigate('/projects')}
                        className="text-[var(--text-secondary)] hover:text-[var(--text)] transition-colors"
                    >
                        <svg width="16" height="16" viewBox="0 0 24 24" fill="currentColor">
                            <path d="M20 11H7.83l5.59-5.59L12 4l-8 8 8 8 1.41-1.41L7.83 13H20v-2z" />
                        </svg>
                    </button>
                    <h1 className="text-sm font-semibold text-[var(--text)]">{project?.name}</h1>
                    <span className="text-[var(--border)]">•</span>
                    <span className="text-xs text-[var(--text-secondary)]">{tasks.length} tasks</span>
                    <span className="text-[var(--border)]">•</span>
                    <button
                        onClick={() => navigate(`/projects/${id}/members`)}
                        className="text-xs text-[var(--text-secondary)] hover:text-[var(--text)] transition-colors flex items-center gap-1"
                    >
                        <svg width="12" height="12" viewBox="0 0 24 24" fill="currentColor">
                            <path d="M16 11c1.66 0 2.99-1.34 2.99-3S17.66 5 16 5c-1.66 0-3 1.34-3 3s1.34 3 3 3zm-8 0c1.66 0 2.99-1.34 2.99-3S9.66 5 8 5C6.34 5 5 6.34 5 8s1.34 3 3 3zm0 2c-2.33 0-7 1.17-7 3.5V19h14v-2.5c0-2.33-4.67-3.5-7-3.5zm8 0c-.29 0-.62.02-.97.05 1.16.84 1.97 1.97 1.97 3.45V19h6v-2.5c0-2.33-4.67-3.5-7-3.5z" />
                        </svg>
                        Members
                    </button>
                </div>
                <button
                    onClick={() => { setCreateStatus('TODO'); setShowCreateModal(true); }}
                    className="flex items-center gap-1.5 px-3 py-1.5 bg-primary-500 hover:bg-primary-600 text-white text-xs font-medium rounded-lg transition-colors"
                >
                    <span className="text-base leading-none">+</span>
                    Add task
                </button>
            </div>
 
            {/* Kanban board */}
            <div className="flex-1 overflow-x-auto p-6">
                <div className="flex gap-4 min-w-max h-full">
                    {STATUSES.map(status => (
                        <div key={status} className="w-72 flex flex-col">
                            <div className="flex items-center justify-between mb-3 px-1">
                                <div className="flex items-center gap-2">
                                    <span className={`text-xs font-medium px-2 py-0.5 rounded-full ${STATUS_CONFIG[status].color}`}>
                                        {STATUS_CONFIG[status].label}
                                    </span>
                                    <span className="text-xs text-[var(--text-secondary)] font-medium">
                                        {tasksByStatus(status).length}
                                    </span>
                                </div>
                                <button
                                    onClick={() => { setCreateStatus(status); setShowCreateModal(true); }}
                                    className="text-[var(--text-secondary)] hover:text-[var(--text)] p-0.5 rounded transition-colors"
                                >
                                    <svg width="14" height="14" viewBox="0 0 24 24" fill="currentColor">
                                        <path d="M19 13h-6v6h-2v-6H5v-2h6V5h2v6h6v2z" />
                                    </svg>
                                </button>
                            </div>
 
                            <div className="flex-1 space-y-2">
                                {tasksByStatus(status).map(task => (
                                    <div
                                        key={task.id}
                                        onClick={() => openTask(task)}
                                        className="bg-[var(--surface)] border border-[var(--border)] rounded-lg p-3 cursor-pointer hover:border-primary-500 hover:shadow-sm transition-all"
                                    >
                                        <div className="flex items-start gap-1.5 mb-2">
                                            <div className={`w-2 h-2 rounded-full flex-shrink-0 mt-1 ${PRIORITY_CONFIG[task.priority]?.dot || 'bg-slate-400'}`} />
                                            <p className="text-sm font-medium text-[var(--text)] leading-snug">{task.title}</p>
                                        </div>
 
                                        {task.labels?.length > 0 && (
                                            <div className="flex flex-wrap gap-1 mb-2">
                                                {task.labels.map(label => (
                                                    <span
                                                        key={label.id}
                                                        style={{ backgroundColor: label.color + '20', color: label.color }}
                                                        className="text-xs px-1.5 py-0.5 rounded font-medium"
                                                    >
                                                        {label.name}
                                                    </span>
                                                ))}
                                            </div>
                                        )}
 
                                        <div className="flex items-center justify-between mt-2">
                                            {task.dueDate && (
                                                <span className={`text-xs ${isOverdue(task.dueDate) ? 'text-red-500 font-medium' : 'text-[var(--text-secondary)]'}`}>
                                                    {isOverdue(task.dueDate) ? '⚠ ' : ''}
                                                    {new Date(task.dueDate).toLocaleDateString()}
                                                </span>
                                            )}
                                            {task.assigneeName && (
                                                <div className="w-5 h-5 rounded-full bg-primary-500 flex items-center justify-center text-white text-[10px] font-semibold ml-auto">
                                                    {task.assigneeName.charAt(0).toUpperCase()}
                                                </div>
                                            )}
                                        </div>
                                    </div>
                                ))}
 
                                {tasksByStatus(status).length === 0 && (
                                    <div
                                        onClick={() => { setCreateStatus(status); setShowCreateModal(true); }}
                                        className="border-2 border-dashed border-[var(--border)] rounded-lg p-4 text-center cursor-pointer hover:border-primary-500 transition-colors"
                                    >
                                        <p className="text-xs text-[var(--text-secondary)]">+ Add task</p>
                                    </div>
                                )}
                            </div>
                        </div>
                    ))}
                </div>
            </div>
 
            {/* Task detail panel */}
            {selectedTask && (
                <div className="fixed inset-0 z-50 flex">
                    <div className="flex-1 bg-black/30" onClick={() => setSelectedTask(null)} />
                    <div className="w-full max-w-lg bg-[var(--surface)] border-l border-[var(--border)] flex flex-col overflow-hidden">
                        <div className="flex items-center justify-between px-6 py-4 border-b border-[var(--border)]">
                            <div className="flex items-center gap-2">
                                <span className={`text-xs px-2 py-0.5 rounded-full ${STATUS_CONFIG[selectedTask.status]?.color}`}>
                                    {STATUS_CONFIG[selectedTask.status]?.label}
                                </span>
                                <span className={`text-xs font-medium ${PRIORITY_CONFIG[selectedTask.priority]?.color}`}>
                                    {PRIORITY_CONFIG[selectedTask.priority]?.label}
                                </span>
                            </div>
                            <div className="flex items-center gap-2">
                                <button
                                    onClick={() => deleteTask(selectedTask.id)}
                                    className="p-1.5 rounded hover:bg-red-50 text-[var(--text-secondary)] hover:text-red-500 transition-colors"
                                >
                                    <svg width="14" height="14" viewBox="0 0 24 24" fill="currentColor">
                                        <path d="M6 19c0 1.1.9 2 2 2h8c1.1 0 2-.9 2-2V7H6v12zM19 4h-3.5l-1-1h-5l-1 1H5v2h14V4z" />
                                    </svg>
                                </button>
                                <button
                                    onClick={() => setSelectedTask(null)}
                                    className="p-1.5 rounded hover:bg-[var(--border)] text-[var(--text-secondary)] transition-colors"
                                >
                                    <svg width="14" height="14" viewBox="0 0 24 24" fill="currentColor">
                                        <path d="M19 6.41L17.59 5 12 10.59 6.41 5 5 6.41 10.59 12 5 17.59 6.41 19 12 13.41 17.59 19 19 17.59 13.41 12z" />
                                    </svg>
                                </button>
                            </div>
                        </div>
 
                        <div className="flex-1 overflow-y-auto p-6 space-y-6">
                            <h2 className="text-lg font-semibold text-[var(--text)]">{selectedTask.title}</h2>
 
                            {selectedTask.description && (
                                <p className="text-sm text-[var(--text-secondary)] leading-relaxed">{selectedTask.description}</p>
                            )}
 
                            <div className="grid grid-cols-2 gap-4">
                                <div>
                                    <p className="text-xs font-medium text-[var(--text-secondary)] mb-1.5">Status</p>
                                    <select
                                        value={selectedTask.status}
                                        onChange={e => updateStatus(selectedTask.id, e.target.value)}
                                        className="w-full text-xs px-2 py-1.5 bg-[var(--bg)] border border-[var(--border)] rounded-md text-[var(--text)] focus:outline-none focus:ring-1 focus:ring-primary-500"
                                    >
                                        {STATUSES.map(s => (
                                            <option key={s} value={s}>{STATUS_CONFIG[s].label}</option>
                                        ))}
                                    </select>
                                </div>
                                <div>
                                    <p className="text-xs font-medium text-[var(--text-secondary)] mb-1.5">Priority</p>
                                    <div className={`flex items-center gap-1.5 text-xs font-medium ${PRIORITY_CONFIG[selectedTask.priority]?.color}`}>
                                        <div className={`w-2 h-2 rounded-full ${PRIORITY_CONFIG[selectedTask.priority]?.dot}`} />
                                        {PRIORITY_CONFIG[selectedTask.priority]?.label}
                                    </div>
                                </div>
                                {selectedTask.assigneeName && (
                                    <div>
                                        <p className="text-xs font-medium text-[var(--text-secondary)] mb-1.5">Assignee</p>
                                        <div className="flex items-center gap-1.5">
                                            <div className="w-5 h-5 rounded-full bg-primary-500 flex items-center justify-center text-white text-[10px] font-semibold">
                                                {selectedTask.assigneeName.charAt(0)}
                                            </div>
                                            <span className="text-xs text-[var(--text)]">{selectedTask.assigneeName}</span>
                                        </div>
                                    </div>
                                )}
                                {selectedTask.dueDate && (
                                    <div>
                                        <p className="text-xs font-medium text-[var(--text-secondary)] mb-1.5">Due date</p>
                                        <span className={`text-xs ${isOverdue(selectedTask.dueDate) ? 'text-red-500 font-medium' : 'text-[var(--text)]'}`}>
                                            {new Date(selectedTask.dueDate).toLocaleDateString()}
                                        </span>
                                    </div>
                                )}
                            </div>
 
                            {/* Labels display */}
                            {selectedTask.labels?.length > 0 && (
                                <div>
                                    <p className="text-xs font-medium text-[var(--text-secondary)] mb-2">Labels</p>
                                    <div className="flex flex-wrap gap-1.5">
                                        {selectedTask.labels.map(label => (
                                            <span
                                                key={label.id}
                                                style={{ backgroundColor: label.color + '20', color: label.color }}
                                                className="text-xs px-2 py-1 rounded-full font-medium"
                                            >
                                                {label.name}
                                            </span>
                                        ))}
                                    </div>
                                </div>
                            )}
 
                            {/* Manage labels */}
                            <div>
                                <p className="text-xs font-medium text-[var(--text-secondary)] mb-2">Manage labels</p>
                                <div className="flex flex-wrap gap-1.5 mb-2">
                                    {labels.map(label => {
                                        const attached = selectedTask.labels?.some(l => l.id === label.id);
                                        return (
                                            <button
                                                key={label.id}
                                                onClick={() => attached
                                                    ? removeLabel(selectedTask.id, label.id)
                                                    : attachLabel(selectedTask.id, label.id)
                                                }
                                                style={{
                                                    backgroundColor: attached ? label.color + '30' : 'transparent',
                                                    color: label.color,
                                                    borderColor: label.color + '60',
                                                }}
                                                className="text-xs px-2 py-1 rounded-full border font-medium transition-all hover:opacity-80"
                                            >
                                                {attached ? '✓ ' : ''}{label.name}
                                            </button>
                                        );
                                    })}
                                </div>
                                {showLabelForm ? (
                                    <form onSubmit={createLabel} className="flex gap-2 mt-2">
                                        <input
                                            type="text"
                                            value={labelName}
                                            onChange={e => setLabelName(e.target.value)}
                                            placeholder="Label name"
                                            required
                                            className="flex-1 px-2 py-1 text-xs bg-[var(--bg)] border border-[var(--border)] rounded-md text-[var(--text)] focus:outline-none focus:ring-1 focus:ring-primary-500"
                                        />
                                        <input
                                            type="color"
                                            value={labelColor}
                                            onChange={e => setLabelColor(e.target.value)}
                                            className="w-8 h-7 rounded cursor-pointer border border-[var(--border)]"
                                        />
                                        <button type="submit" className="px-2 py-1 bg-primary-500 text-white text-xs rounded-md">Add</button>
                                        <button type="button" onClick={() => setShowLabelForm(false)} className="px-2 py-1 text-xs text-[var(--text-secondary)] hover:text-[var(--text)]">Cancel</button>
                                    </form>
                                ) : (
                                    <button
                                        onClick={() => setShowLabelForm(true)}
                                        className="text-xs text-primary-500 hover:text-primary-600 transition-colors"
                                    >
                                        + Create label
                                    </button>
                                )}
                            </div>
 
                            {/* Activity timeline */}
                            <div>
                                <p className="text-xs font-medium text-[var(--text-secondary)] mb-3">Activity</p>
                                {activity.length === 0 ? (
                                    <p className="text-xs text-[var(--text-secondary)]">No activity yet</p>
                                ) : (
                                    <div className="space-y-3">
                                        {activity.map(log => (
                                            <div key={log.id} className="flex gap-3">
                                                <div className="w-5 h-5 rounded-full bg-primary-500 flex items-center justify-center text-white text-[10px] font-semibold flex-shrink-0 mt-0.5">
                                                    {log.actorName?.charAt(0)}
                                                </div>
                                                <div>
                                                    <p className="text-xs text-[var(--text)]">
                                                        <span className="font-medium">{log.actorName}</span>
                                                        {' changed '}
                                                        <span className="font-medium">{log.action.replace('_', ' ').toLowerCase()}</span>
                                                        {' from '}
                                                        <span className="bg-[var(--border)] px-1 rounded text-[var(--text-secondary)]">{log.oldValue}</span>
                                                        {' to '}
                                                        <span className="bg-primary-50 text-primary-600 px-1 rounded">{log.newValue}</span>
                                                    </p>
                                                    <p className="text-[10px] text-[var(--text-secondary)] mt-0.5">
                                                        {new Date(log.createdAt).toLocaleString()}
                                                    </p>
                                                </div>
                                            </div>
                                        ))}
                                    </div>
                                )}
                            </div>
                        </div>
                    </div>
                </div>
            )}
 
            {/* Create task modal */}
            {showCreateModal && (
                <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50 p-4">
                    <div className="bg-[var(--surface)] border border-[var(--border)] rounded-xl p-6 w-full max-w-md shadow-xl">
                        <h2 className="text-base font-semibold text-[var(--text)] mb-4">New task</h2>
                        <form onSubmit={createTask} className="space-y-4">
                            <div>
                                <label className="block text-sm font-medium text-[var(--text)] mb-1.5">Title</label>
                                <input
                                    type="text"
                                    value={taskForm.title}
                                    onChange={e => setTaskForm(p => ({ ...p, title: e.target.value }))}
                                    required
                                    autoFocus
                                    placeholder="Task title"
                                    className="w-full px-3 py-2.5 text-sm bg-[var(--bg)] border border-[var(--border)] rounded-lg text-[var(--text)] placeholder-[var(--text-secondary)] focus:outline-none focus:ring-2 focus:ring-primary-500 focus:border-transparent"
                                />
                            </div>
                            <div>
                                <label className="block text-sm font-medium text-[var(--text)] mb-1.5">Description</label>
                                <textarea
                                    value={taskForm.description}
                                    onChange={e => setTaskForm(p => ({ ...p, description: e.target.value }))}
                                    rows={2}
                                    placeholder="Optional description"
                                    className="w-full px-3 py-2.5 text-sm bg-[var(--bg)] border border-[var(--border)] rounded-lg text-[var(--text)] placeholder-[var(--text-secondary)] focus:outline-none focus:ring-2 focus:ring-primary-500 focus:border-transparent resize-none"
                                />
                            </div>
                            <div className="grid grid-cols-2 gap-3">
                                <div>
                                    <label className="block text-sm font-medium text-[var(--text)] mb-1.5">Priority</label>
                                    <select
                                        value={taskForm.priority}
                                        onChange={e => setTaskForm(p => ({ ...p, priority: e.target.value }))}
                                        className="w-full px-3 py-2.5 text-sm bg-[var(--bg)] border border-[var(--border)] rounded-lg text-[var(--text)] focus:outline-none focus:ring-2 focus:ring-primary-500"
                                    >
                                        <option value="LOW">Low</option>
                                        <option value="MEDIUM">Medium</option>
                                        <option value="HIGH">High</option>
                                        <option value="URGENT">Urgent</option>
                                    </select>
                                </div>
                                <div>
                                    <label className="block text-sm font-medium text-[var(--text)] mb-1.5">Status</label>
                                    <select
                                        value={createStatus}
                                        onChange={e => setCreateStatus(e.target.value)}
                                        className="w-full px-3 py-2.5 text-sm bg-[var(--bg)] border border-[var(--border)] rounded-lg text-[var(--text)] focus:outline-none focus:ring-2 focus:ring-primary-500"
                                    >
                                        {STATUSES.map(s => (
                                            <option key={s} value={s}>{STATUS_CONFIG[s].label}</option>
                                        ))}
                                    </select>
                                </div>
                            </div>
                            <div>
                                <label className="block text-sm font-medium text-[var(--text)] mb-1.5">
                                    Due date <span className="text-[var(--text-secondary)] font-normal">(optional)</span>
                                </label>
                                <input
                                    type="date"
                                    value={taskForm.dueDate}
                                    onChange={e => setTaskForm(p => ({ ...p, dueDate: e.target.value }))}
                                    className="w-full px-3 py-2.5 text-sm bg-[var(--bg)] border border-[var(--border)] rounded-lg text-[var(--text)] focus:outline-none focus:ring-2 focus:ring-primary-500"
                                />
                            </div>
                            <div className="flex gap-3 pt-2">
                                <button
                                    type="button"
                                    onClick={() => setShowCreateModal(false)}
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
                                    ) : 'Create task'}
                                </button>
                            </div>
                        </form>
                    </div>
                </div>
            )}
        </div>
    );
}
 