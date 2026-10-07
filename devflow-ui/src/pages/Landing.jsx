import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { motion, AnimatePresence } from 'framer-motion';
import { useAuth } from '../context/AuthContext';

const features = [
  {
    icon: (
      <svg width="22" height="22" viewBox="0 0 24 24" fill="currentColor">
        <path d="M17 12h-5v5h5v-5zM16 1v2H8V1H6v2H5c-1.11 0-1.99.9-1.99 2L3 19c0 1.1.89 2 2 2h14c1.1 0 2-.9 2-2V5c0-1.1-.9-2-2-2h-1V1h-2zm3 18H5V8h14v11z"/>
      </svg>
    ),
    title: 'Kanban Board',
    desc: 'Visualize work across To Do, In Progress, In Review, and Done. Move tasks with a click.',
    color: '#0052CC',
  },
  {
    icon: (
      <svg width="22" height="22" viewBox="0 0 24 24" fill="currentColor">
        <path d="M16 11c1.66 0 2.99-1.34 2.99-3S17.66 5 16 5c-1.66 0-3 1.34-3 3s1.34 3 3 3zm-8 0c1.66 0 2.99-1.34 2.99-3S9.66 5 8 5C6.34 5 5 6.34 5 8s1.34 3 3 3zm0 2c-2.33 0-7 1.17-7 3.5V19h14v-2.5c0-2.33-4.67-3.5-7-3.5zm8 0c-.29 0-.62.02-.97.05 1.16.84 1.97 1.97 1.97 3.45V19h6v-2.5c0-2.33-4.67-3.5-7-3.5z"/>
      </svg>
    ),
    title: 'Team Collaboration',
    desc: 'Invite members by email, assign roles, and collaborate across projects in real time.',
    color: '#6554C0',
  },
  {
    icon: (
      <svg width="22" height="22" viewBox="0 0 24 24" fill="currentColor">
        <path d="M17.63 5.84C17.27 5.33 16.67 5 16 5L5 5.01C3.9 5.01 3 5.9 3 7v10c0 1.1.9 1.99 2 1.99L16 19c.67 0 1.27-.33 1.63-.84L22 12l-4.37-6.16z"/>
      </svg>
    ),
    title: 'Smart Labels',
    desc: 'Create color-coded labels, tag tasks, and filter your board instantly by label.',
    color: '#00875A',
  },
  {
    icon: (
      <svg width="22" height="22" viewBox="0 0 24 24" fill="currentColor">
        <path d="M13 2.05v2.02c3.95.49 7 3.85 7 7.93 0 3.21-1.81 6-4.72 7.28L13 17v5h5l-1.22-1.22C19.91 19.07 22 15.76 22 12c0-5.18-3.95-9.45-9-9.95zM11 2.05C5.95 2.55 2 6.82 2 12c0 3.76 2.09 7.07 5.22 8.78L6 22h5v-5l-2.28 2.28C7.81 18 6 15.21 6 12c0-4.08 3.05-7.44 7-7.93V2.05z"/>
      </svg>
    ),
    title: 'Activity Timeline',
    desc: 'Every status change, assignment, and update is automatically tracked with full history.',
    color: '#FF5630',
  },
  {
    icon: (
      <svg width="22" height="22" viewBox="0 0 24 24" fill="currentColor">
        <path d="M12 1L3 5v6c0 5.55 3.84 10.74 9 12 5.16-1.26 9-6.45 9-12V5l-9-4z"/>
      </svg>
    ),
    title: 'Secure by Default',
    desc: 'JWT authentication, role-based access control, and rate limiting protect your data.',
    color: '#0747A6',
  },
  {
    icon: (
      <svg width="22" height="22" viewBox="0 0 24 24" fill="currentColor">
        <path d="M19 3H5c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h14c1.1 0 2-.9 2-2V5c0-1.1-.9-2-2-2zm-7 3c1.93 0 3.5 1.57 3.5 3.5S13.93 13 12 13s-3.5-1.57-3.5-3.5S10.07 6 12 6zm7 13H5v-.23c0-.62.28-1.2.76-1.58C7.47 15.82 9.64 15 12 15s4.53.82 6.24 2.19c.48.38.76.97.76 1.58V19z"/>
      </svg>
    ),
    title: 'Member Management',
    desc: 'Full project membership control — invite, remove, and manage roles with one click.',
    color: '#008DA6',
  },
];

const STATUSES = [
  { label: 'To Do', color: '#F4F5F7', text: '#5E6C84' },
  { label: 'In Progress', color: '#DEEBFF', text: '#0052CC' },
  { label: 'In Review', color: '#FFFAE6', text: '#FF991F' },
  { label: 'Done', color: '#E3FCEF', text: '#00875A' },
];

const DEMO_TASKS = [
  { title: 'Design system setup', status: 0, priority: '#FF5630', label: 'Frontend', labelBg: '#FFEBE6', labelText: '#BF2600' },
  { title: 'JWT auth flow', status: 2, priority: '#FF5630', label: 'Backend', labelBg: '#E3FCEF', labelText: '#006644' },
  { title: 'Redis caching layer', status: 3, priority: '#FF991F', label: 'Backend', labelBg: '#E3FCEF', labelText: '#006644' },
  { title: 'Kanban drag & drop', status: 1, priority: '#FF991F', label: 'Frontend', labelBg: '#FFEBE6', labelText: '#BF2600' },
  { title: 'API documentation', status: 1, priority: '#36B37E', label: 'Docs', labelBg: '#FFFAE6', labelText: '#974F0C' },
  { title: 'Rate limiting', status: 3, priority: '#FF5630', label: 'Backend', labelBg: '#E3FCEF', labelText: '#006644' },
];

export default function Landing() {
  const [introComplete, setIntroComplete] = useState(false);
  const [showWelcome, setShowWelcome] = useState(false);
  const navigate = useNavigate();
  const { user } = useAuth();

  useEffect(() => {
    const seen = sessionStorage.getItem('introSeen');
    if (seen) {
      setIntroComplete(true);
    }
  }, []);

  useEffect(() => {
    if (user && introComplete) {
      setTimeout(() => setShowWelcome(true), 400);
    }
  }, [user, introComplete]);

  const handleIntroComplete = () => {
    sessionStorage.setItem('introSeen', 'true');
    setIntroComplete(true);
  };

  return (
    <div className="min-h-screen bg-white text-[#172B4D] overflow-x-hidden">

      {/* ── INTRO ANIMATION ── */}
      <AnimatePresence>
        {!introComplete && (
          <motion.div
            className="fixed inset-0 z-50 flex flex-col items-center justify-center bg-white"
            exit={{ opacity: 0 }}
            transition={{ duration: 0.5 }}
          >
            <motion.div
              className="flex items-center gap-3 mb-5"
              initial={{ opacity: 0, scale: 0.6 }}
              animate={{ opacity: 1, scale: 1 }}
              transition={{ duration: 0.5, ease: 'backOut' }}
            >
              <motion.img
                src="/logo.png"
                alt="DevFlow"
                className="w-12 h-12 object-contain"
                animate={{ rotate: [0, -8, 8, 0] }}
                transition={{ delay: 0.5, duration: 0.5 }}
                onError={(e) => {
                  e.target.style.display = 'none';
                  e.target.nextSibling.style.display = 'flex';
                }}
              />
              <div
                className="w-12 h-12 bg-[#0052CC] rounded-xl items-center justify-center hidden"
              >
                <svg width="26" height="26" viewBox="0 0 24 24" fill="white">
                  <path d="M9 11H7v2h2v-2zm4 0h-2v2h2v-2zm4 0h-2v2h2v-2zm2-7h-1V2h-2v2H8V2H6v2H5c-1.11 0-1.99.9-1.99 2L3 20c0 1.1.89 2 2 2h14c1.1 0 2-.9 2-2V6c0-1.1-.9-2-2-2zm0 16H5V9h14v11z"/>
                </svg>
              </div>
              <motion.span
                className="text-3xl font-bold text-[#172B4D]"
                initial={{ opacity: 0, x: -15 }}
                animate={{ opacity: 1, x: 0 }}
                transition={{ delay: 0.3, duration: 0.4 }}
              >
                DevFlow
              </motion.span>
            </motion.div>

            <motion.p
              className="text-[#5E6C84] text-sm mb-8"
              initial={{ opacity: 0 }}
              animate={{ opacity: 1 }}
              transition={{ delay: 0.7 }}
            >
              Your team's project command center
            </motion.p>

            <motion.div
              className="w-40 h-1 bg-[#F4F5F7] rounded-full overflow-hidden"
              initial={{ opacity: 0 }}
              animate={{ opacity: 1 }}
              transition={{ delay: 0.8 }}
            >
              <motion.div
                className="h-full bg-[#0052CC] rounded-full"
                initial={{ width: '0%' }}
                animate={{ width: '100%' }}
                transition={{ delay: 0.9, duration: 1.3, ease: 'easeInOut' }}
                onAnimationComplete={handleIntroComplete}
              />
            </motion.div>

            <motion.button
              className="absolute bottom-8 right-8 text-xs text-[#5E6C84] hover:text-[#172B4D] transition-colors"
              initial={{ opacity: 0 }}
              animate={{ opacity: 1 }}
              transition={{ delay: 1.2 }}
              onClick={handleIntroComplete}
            >
              Skip →
            </motion.button>
          </motion.div>
        )}
      </AnimatePresence>

      {/* ── WELCOME BACK PROMPT (for logged-in users) ── */}
      <AnimatePresence>
        {showWelcome && user && (
          <motion.div
            className="fixed top-20 right-6 z-50 bg-white border border-[#DFE1E6] rounded-xl shadow-xl p-4 w-72"
            initial={{ opacity: 0, y: -20, scale: 0.95 }}
            animate={{ opacity: 1, y: 0, scale: 1 }}
            exit={{ opacity: 0, y: -10 }}
            transition={{ duration: 0.3 }}
          >
            <div className="flex items-start justify-between mb-3">
              <div>
                <p className="text-sm font-semibold text-[#172B4D]">Welcome back, {user.name?.split(' ')[0]} 👋</p>
                <p className="text-xs text-[#5E6C84] mt-0.5">Pick up where you left off.</p>
              </div>
              <button
                onClick={() => setShowWelcome(false)}
                className="text-[#5E6C84] hover:text-[#172B4D] transition-colors ml-2"
              >
                <svg width="14" height="14" viewBox="0 0 24 24" fill="currentColor">
                  <path d="M19 6.41L17.59 5 12 10.59 6.41 5 5 6.41 10.59 12 5 17.59 6.41 19 12 13.41 17.59 19 19 17.59 13.41 12z"/>
                </svg>
              </button>
            </div>
            <button
              onClick={() => navigate('/projects')}
              className="w-full flex items-center gap-3 p-2.5 bg-[#F4F5F7] hover:bg-[#EBECF0] rounded-lg transition-colors"
            >
              <div className="w-8 h-8 bg-[#0052CC] rounded-lg flex items-center justify-center flex-shrink-0">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="white">
                  <path d="M9 11H7v2h2v-2zm4 0h-2v2h2v-2zm4 0h-2v2h2v-2zm2-7h-1V2h-2v2H8V2H6v2H5c-1.11 0-1.99.9-1.99 2L3 20c0 1.1.89 2 2 2h14c1.1 0 2-.9 2-2V6c0-1.1-.9-2-2-2zm0 16H5V9h14v11z"/>
                </svg>
              </div>
              <div className="text-left flex-1">
                <p className="text-xs font-semibold text-[#172B4D]">DevFlow</p>
                <p className="text-[10px] text-[#5E6C84]">{user.email}</p>
              </div>
              <span className="text-xs font-medium text-white bg-[#0052CC] px-3 py-1 rounded-md">Go</span>
            </button>
          </motion.div>
        )}
      </AnimatePresence>

      {/* ── MAIN CONTENT ── */}
      <AnimatePresence>
        {introComplete && (
          <motion.div
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            transition={{ duration: 0.4 }}
          >
            {/* NAV */}
            <nav className="fixed top-0 left-0 right-0 z-40 bg-white border-b border-[#DFE1E6]">
              <div className="max-w-6xl mx-auto px-6 h-14 flex items-center justify-between">
                <div className="flex items-center gap-2.5">
                  <img
                    src="/logo.png"
                    alt="DevFlow"
                    className="w-7 h-7 object-contain"
                    onError={(e) => {
                      e.target.style.display = 'none';
                      e.target.nextSibling.style.display = 'flex';
                    }}
                  />
                  <div className="w-7 h-7 bg-[#0052CC] rounded-md items-center justify-center hidden">
                    <svg width="14" height="14" viewBox="0 0 24 24" fill="white">
                      <path d="M9 11H7v2h2v-2zm4 0h-2v2h2v-2zm4 0h-2v2h2v-2zm2-7h-1V2h-2v2H8V2H6v2H5c-1.11 0-1.99.9-1.99 2L3 20c0 1.1.89 2 2 2h14c1.1 0 2-.9 2-2V6c0-1.1-.9-2-2-2zm0 16H5V9h14v11z"/>
                    </svg>
                  </div>
                  <span className="text-sm font-bold text-[#172B4D]">DevFlow</span>
                </div>

                <div className="hidden sm:flex items-center gap-6">
                  <a href="#features" className="text-sm text-[#5E6C84] hover:text-[#172B4D] transition-colors">Features</a>
                  <a href="https://devflow-fcbo.onrender.com/swagger-ui/index.html" target="_blank" rel="noreferrer" className="text-sm text-[#5E6C84] hover:text-[#172B4D] transition-colors">API Docs</a>
                  <a href="https://github.com/LAKSHAYANEJA/DevFlow" target="_blank" rel="noreferrer" className="text-sm text-[#5E6C84] hover:text-[#172B4D] transition-colors">GitHub</a>
                </div>

                <div className="flex items-center gap-3">
                  {user ? (
                    <button
                      onClick={() => navigate('/projects')}
                      className="text-sm font-medium bg-[#0052CC] hover:bg-[#0043A6] text-white px-4 py-1.5 rounded-md transition-colors"
                    >
                      Open DevFlow
                    </button>
                  ) : (
                    <>
                      <button
                        onClick={() => navigate('/login')}
                        className="text-sm text-[#5E6C84] hover:text-[#172B4D] transition-colors px-3 py-1.5"
                      >
                        Log in
                      </button>
                      <button
                        onClick={() => navigate('/register')}
                        className="text-sm font-medium bg-[#0052CC] hover:bg-[#0043A6] text-white px-4 py-1.5 rounded-md transition-colors"
                      >
                        Get started free
                      </button>
                    </>
                  )}
                </div>
              </div>
            </nav>

            {/* HERO */}
            <section className="pt-28 pb-16 px-6 max-w-5xl mx-auto text-center">
              <motion.div
                initial={{ opacity: 0, y: 24 }}
                animate={{ opacity: 1, y: 0 }}
                transition={{ duration: 0.6 }}
              >
                <div className="inline-flex items-center gap-2 bg-[#DEEBFF] text-[#0052CC] rounded-full px-4 py-1.5 text-xs font-medium mb-6">
                  <span className="w-1.5 h-1.5 rounded-full bg-[#0052CC]" />
                  Built for development teams
                </div>

                <h1 className="text-5xl sm:text-6xl font-bold text-[#172B4D] leading-tight mb-6">
                  Manage your projects
                  <br />
                  <span className="text-[#0052CC]">the right way</span>
                </h1>

                <p className="text-lg text-[#5E6C84] max-w-xl mx-auto mb-10 leading-relaxed">
                  DevFlow gives your team everything they need to plan, track, and ship great software — with the reliability your projects deserve.
                </p>

                <div className="flex flex-wrap gap-3 justify-center">
                  <motion.button
                    onClick={() => navigate('/register')}
                    className="px-6 py-3 bg-[#0052CC] hover:bg-[#0043A6] text-white font-semibold rounded-md transition-colors text-sm shadow-sm"
                    whileHover={{ scale: 1.02 }}
                    whileTap={{ scale: 0.98 }}
                  >
                    Get started — it's free
                  </motion.button>
                  <motion.button
                    onClick={() => navigate('/login')}
                    className="px-6 py-3 bg-white hover:bg-[#F4F5F7] border border-[#DFE1E6] text-[#172B4D] font-semibold rounded-md transition-colors text-sm"
                    whileHover={{ scale: 1.02 }}
                    whileTap={{ scale: 0.98 }}
                  >
                    Already have an account →
                  </motion.button>
                </div>
              </motion.div>

              {/* DEMO BOARD */}
              <motion.div
                className="mt-14 bg-white border border-[#DFE1E6] rounded-2xl shadow-xl overflow-hidden text-left"
                initial={{ opacity: 0, y: 40 }}
                animate={{ opacity: 1, y: 0 }}
                transition={{ delay: 0.3, duration: 0.7 }}
              >
                {/* Window chrome */}
                <div className="bg-[#F4F5F7] border-b border-[#DFE1E6] px-4 py-2.5 flex items-center gap-2">
                  <div className="w-3 h-3 rounded-full bg-[#FF5F57]" />
                  <div className="w-3 h-3 rounded-full bg-[#FEBC2E]" />
                  <div className="w-3 h-3 rounded-full bg-[#28C840]" />
                  <div className="ml-4 flex-1 bg-white border border-[#DFE1E6] rounded px-3 py-1 text-xs text-[#5E6C84]">
                    devflow.app/projects/1/board
                  </div>
                </div>

                {/* Board header */}
                <div className="flex items-center gap-3 px-5 py-3 border-b border-[#DFE1E6]">
                  <img src="/logo.png" alt="" className="w-5 h-5 object-contain"
                    onError={(e) => e.target.style.display = 'none'} />
                  <span className="text-sm font-semibold text-[#172B4D]">DevFlow Backend</span>
                  <span className="text-xs text-[#5E6C84]">• 6 tasks</span>
                  <div className="ml-auto flex gap-2">
                    <span className="text-xs bg-[#0052CC] text-white px-2 py-0.5 rounded font-medium">Board</span>
                    <span className="text-xs text-[#5E6C84] px-2 py-0.5 rounded border border-[#DFE1E6]">Backlog</span>
                  </div>
                </div>

                {/* Kanban */}
                <div className="p-5 grid grid-cols-4 gap-3 bg-[#FAFBFC]">
                  {STATUSES.map((status, si) => (
                    <div key={status.label}>
                      <div className="flex items-center gap-2 mb-3">
                        <span
                          className="text-xs font-semibold px-2 py-0.5 rounded-full"
                          style={{ backgroundColor: status.color, color: status.text }}
                        >
                          {status.label}
                        </span>
                        <span className="text-xs text-[#5E6C84]">
                          {DEMO_TASKS.filter(t => t.status === si).length}
                        </span>
                      </div>
                      <div className="space-y-2">
                        {DEMO_TASKS.filter(t => t.status === si).map((task, ti) => (
                          <motion.div
                            key={task.title}
                            className="bg-white border border-[#DFE1E6] rounded-lg p-3 shadow-sm hover:shadow-md hover:border-[#0052CC]/40 transition-all cursor-pointer"
                            initial={{ opacity: 0, y: 8 }}
                            animate={{ opacity: 1, y: 0 }}
                            transition={{ delay: 0.5 + ti * 0.08 }}
                          >
                            <div className="flex items-start gap-1.5 mb-2">
                              <div
                                className="w-2 h-2 rounded-full flex-shrink-0 mt-1"
                                style={{ backgroundColor: task.priority }}
                              />
                              <p className="text-xs font-medium text-[#172B4D] leading-snug">{task.title}</p>
                            </div>
                            <span
                              className="text-[10px] px-1.5 py-0.5 rounded font-medium"
                              style={{ backgroundColor: task.labelBg, color: task.labelText }}
                            >
                              {task.label}
                            </span>
                          </motion.div>
                        ))}
                        {DEMO_TASKS.filter(t => t.status === si).length === 0 && (
                          <div className="border-2 border-dashed border-[#DFE1E6] rounded-lg p-3 text-center">
                            <p className="text-xs text-[#B3BAC5]">+ Add task</p>
                          </div>
                        )}
                      </div>
                    </div>
                  ))}
                </div>
              </motion.div>
            </section>

            {/* SOCIAL PROOF BAR */}
            <section className="py-10 border-y border-[#DFE1E6] bg-[#FAFBFC]">
              <div className="max-w-4xl mx-auto px-6 flex flex-wrap justify-center gap-10 text-center">
                {[
                  { value: '15+', label: 'API Endpoints' },
                  { value: '< 100ms', label: 'Avg Response Time' },
                  { value: 'JWT + RBAC', label: 'Security Model' },
                  { value: '100%', label: 'Free to Use' },
                ].map((stat, i) => (
                  <motion.div
                    key={stat.label}
                    initial={{ opacity: 0, y: 16 }}
                    whileInView={{ opacity: 1, y: 0 }}
                    transition={{ delay: i * 0.08 }}
                    viewport={{ once: true }}
                  >
                    <div className="text-2xl font-bold text-[#172B4D]">{stat.value}</div>
                    <div className="text-xs text-[#5E6C84] mt-1">{stat.label}</div>
                  </motion.div>
                ))}
              </div>
            </section>

            {/* FEATURES */}
            <section id="features" className="py-20 px-6 max-w-6xl mx-auto">
              <motion.div
                className="text-center mb-14"
                initial={{ opacity: 0, y: 20 }}
                whileInView={{ opacity: 1, y: 0 }}
                viewport={{ once: true }}
              >
                <h2 className="text-3xl font-bold text-[#172B4D] mb-3">
                  Everything your team needs to ship
                </h2>
                <p className="text-[#5E6C84] max-w-lg mx-auto">
                  From task creation to delivery — DevFlow keeps your team aligned and your projects moving.
                </p>
              </motion.div>

              <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-5">
                {features.map((f, i) => (
                  <motion.div
                    key={f.title}
                    className="bg-white border border-[#DFE1E6] rounded-xl p-6 hover:shadow-md hover:border-[#0052CC]/30 transition-all"
                    initial={{ opacity: 0, y: 20 }}
                    whileInView={{ opacity: 1, y: 0 }}
                    transition={{ delay: i * 0.07 }}
                    viewport={{ once: true }}
                    whileHover={{ y: -3 }}
                  >
                    <div
                      className="w-10 h-10 rounded-lg flex items-center justify-center mb-4"
                      style={{ backgroundColor: f.color + '15', color: f.color }}
                    >
                      {f.icon}
                    </div>
                    <h3 className="text-sm font-semibold text-[#172B4D] mb-2">{f.title}</h3>
                    <p className="text-xs text-[#5E6C84] leading-relaxed">{f.desc}</p>
                  </motion.div>
                ))}
              </div>
            </section>

            {/* HOW IT WORKS */}
            <section className="py-16 bg-[#FAFBFC] border-y border-[#DFE1E6]">
              <div className="max-w-4xl mx-auto px-6 text-center">
                <motion.div
                  initial={{ opacity: 0, y: 20 }}
                  whileInView={{ opacity: 1, y: 0 }}
                  viewport={{ once: true }}
                >
                  <h2 className="text-2xl font-bold text-[#172B4D] mb-10">Get started in minutes</h2>
                  <div className="grid grid-cols-1 sm:grid-cols-3 gap-8">
                    {[
                      { step: '1', title: 'Create your account', desc: 'Sign up free — no credit card required.' },
                      { step: '2', title: 'Set up your project', desc: 'Create a project and invite your team members.' },
                      { step: '3', title: 'Start tracking', desc: 'Add tasks, assign work, and ship faster together.' },
                    ].map((s, i) => (
                      <motion.div
                        key={s.step}
                        initial={{ opacity: 0, y: 16 }}
                        whileInView={{ opacity: 1, y: 0 }}
                        transition={{ delay: i * 0.1 }}
                        viewport={{ once: true }}
                      >
                        <div className="w-10 h-10 bg-[#0052CC] text-white rounded-full flex items-center justify-center font-bold text-sm mx-auto mb-4">
                          {s.step}
                        </div>
                        <h3 className="text-sm font-semibold text-[#172B4D] mb-2">{s.title}</h3>
                        <p className="text-xs text-[#5E6C84]">{s.desc}</p>
                      </motion.div>
                    ))}
                  </div>
                </motion.div>
              </div>
            </section>

            {/* CTA */}
            <section className="py-24 px-6 text-center bg-[#0052CC]">
              <motion.div
                initial={{ opacity: 0, y: 20 }}
                whileInView={{ opacity: 1, y: 0 }}
                viewport={{ once: true }}
              >
                <h2 className="text-3xl font-bold text-white mb-3">
                  Ready to work better, together?
                </h2>
                <p className="text-[#B3D4FF] mb-8 max-w-md mx-auto text-sm">
                  Join your team on DevFlow and start managing projects the way great teams do.
                </p>
                <div className="flex flex-wrap gap-3 justify-center">
                  <motion.button
                    onClick={() => navigate('/register')}
                    className="px-6 py-3 bg-white hover:bg-[#F4F5F7] text-[#0052CC] font-semibold rounded-md transition-colors text-sm"
                    whileHover={{ scale: 1.02 }}
                    whileTap={{ scale: 0.98 }}
                  >
                    Get started — it's free
                  </motion.button>
                  <motion.button
                    onClick={() => navigate('/login')}
                    className="px-6 py-3 border border-white/30 hover:bg-white/10 text-white font-semibold rounded-md transition-colors text-sm"
                    whileHover={{ scale: 1.02 }}
                    whileTap={{ scale: 0.98 }}
                  >
                    Log in
                  </motion.button>
                </div>
              </motion.div>
            </section>

            {/* FOOTER */}
            <footer className="border-t border-[#DFE1E6] py-8 px-6 bg-white">
              <div className="max-w-6xl mx-auto flex flex-wrap items-center justify-between gap-4">
                <div className="flex items-center gap-2">
                  <img src="/logo.png" alt="DevFlow" className="w-5 h-5 object-contain"
                    onError={(e) => e.target.style.display = 'none'} />
                  <span className="text-xs text-[#5E6C84] font-medium">DevFlow</span>
                  <span className="text-xs text-[#B3BAC5]">— Built by Lakshay Aneja</span>
                </div>
                <div className="flex gap-5">
                  <a href="https://github.com/LAKSHAYANEJA/DevFlow" target="_blank" rel="noreferrer"
                    className="text-xs text-[#5E6C84] hover:text-[#172B4D] transition-colors">GitHub</a>
                  <a href="https://devflow-fcbo.onrender.com/swagger-ui/index.html" target="_blank" rel="noreferrer"
                    className="text-xs text-[#5E6C84] hover:text-[#172B4D] transition-colors">API Docs</a>
                  <a href="https://linkedin.com/in/lakshayaneja4976" target="_blank" rel="noreferrer"
                    className="text-xs text-[#5E6C84] hover:text-[#172B4D] transition-colors">LinkedIn</a>
                </div>
              </div>
            </footer>

          </motion.div>
        )}
      </AnimatePresence>
    </div>
  );
}