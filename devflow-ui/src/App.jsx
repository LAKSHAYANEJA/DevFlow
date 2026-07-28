import { BrowserRouter, Navigate, Routes, Route } from "react-router-dom";
import { AuthProvider, useAuth } from "./context/AuthContext";
import Login from "./pages/Login";
import Register from "./pages/Register";
import Projects from "./pages/Projects";
import Board from "./pages/Board";

function ProtectedRoute({ children }) {
  const {user, loading }= useAuth();

  if(loading) return <div className="flex items-center justify-center h-screen">
    Loading...
  </div>

  return user ? children : <Navigate to="/login"/>;

}

function App() {
  return (
    <AuthProvider>
      <BrowserRouter>
      <Toaster position="top-right" />
      <Routes>
        <Route path="/login" element={<Login />} />
        <Route path="/register" element={<Register />} />
        <Route path="/projects" element={
         <ProtectedRoute> <Projects /></ProtectedRoute>
          } />
        <Route path="/projects/:id/board" element={
          
          <ProtectedRoute> <Board /></ProtectedRoute>
          } />

          <Route path="*" element={<Navigate to="/projects" />} />
      </Routes>
      </BrowserRouter>
    </AuthProvider>
  )
}

export default App;