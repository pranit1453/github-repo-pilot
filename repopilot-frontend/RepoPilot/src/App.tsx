import { BrowserRouter, Routes, Route } from 'react-router-dom';
import { AuthProvider } from './context/AuthContext';
import { ThemeProvider } from './context/ThemeContext';
import { LandingPage } from './pages/LandingPage';
import { OAuthSuccessPage } from './pages/auth/OAuthSuccessPage';
import { OAuthFailurePage } from './pages/auth/OAuthFailurePage';
import { DashboardPage } from './pages/DashboardPage';

export function App() {
  return (
    <ThemeProvider defaultTheme="dark">
      <AuthProvider>
        <BrowserRouter>
          <Routes>
            <Route path="/" element={<LandingPage />} />
            <Route path="/auth/oauth2/success" element={<OAuthSuccessPage />} />
            <Route path="/auth/oauth2/failure" element={<OAuthFailurePage />} />
            <Route path="/dashboard" element={<DashboardPage />} />
          </Routes>
        </BrowserRouter>
      </AuthProvider>
    </ThemeProvider>
  );
}

export default App;
