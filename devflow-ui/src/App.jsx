import { BrowserRouter, Navigate, Routes, Route } from "react-router-dom";
import { AuthProvider, useAuth } from "./context/AuthContext";
import { Toaster } from "react-hot-toast";
import Login from "./pages/Login";
import Register from "./pages/Register";
import Projects from "./pages/Projects";
import Board from "./pages/Board";
import Members from "./pages/Members";
import Landing from './pages/Landing';

function ProtectedRoute({ children }) {
  const {user, loading }= useAuth();

  if(loading) return  <div className="flex items-center justify-center h-screen bg-[var(--bg)]">
      <div className="w-8 h-8 border-2 border-primary-500 border-t-transparent rounded-full animate-spin" />
    </div>

  return user ? children : <Navigate to="/login" replace />;

}

function PublicRoute({ children }) {
  const { user, loading } = useAuth();

  if(loading) return null;

  return user ? <Navigate to="/projects" replace /> : children;
}

function App() {
  return (
    <AuthProvider>
      <BrowserRouter>
      <Toaster position="top-right"
      toastOptions={{
        style: {
          background: 'var(--surface)',
          color: 'var(--text)',
          border: '1px solid var(--border)',
          fontFamily: 'Inter, sans-serif',
          fontSize: '13px',
        },
      }}
      />
      <Routes>
        <Route path="/" element={<Landing />} />
        <Route path="/login" element={<PublicRoute><Login /></PublicRoute>} />
        <Route path="/register" element={<PublicRoute><Register /></PublicRoute>} />
        <Route path="/projects" element={
         <ProtectedRoute> <Projects /></ProtectedRoute>
          } />
        <Route path="/projects/:id/board" element={
          
          <ProtectedRoute> <Board /></ProtectedRoute>
          } />

          <Route path="*" element={<Navigate to="/" replace />} />
          <Route path="/projects/:id/members" element={
            <ProtectedRoute> <Members /></ProtectedRoute>
          }
           />
      </Routes>
      </BrowserRouter>
    </AuthProvider>
  );
}

export default App;