import React from 'react';
import { Navigate, useLocation } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';

interface ProtectedRouteProps {
  children: React.ReactNode;
  adminOnly?: boolean;
  roles?: string[];
}

const ProtectedRoute: React.FC<ProtectedRouteProps> = ({ children, adminOnly = false, roles }) => {
  const { isAuthenticated, role } = useAuth();
  const location = useLocation();

  if (!isAuthenticated) {
    return <Navigate to="/login" replace state={{ from: location.pathname }} />;
  }

  if (adminOnly && role !== 'ADMIN') {
    return <div className="rounded-2xl border border-amber-200 bg-amber-50 p-8 text-center text-amber-700">No tienes permisos para ver esta página.</div>;
  }

  if (roles && (!role || !roles.includes(role))) {
    return <div className="rounded-2xl border border-amber-200 bg-amber-50 p-8 text-center text-amber-700">No tienes permisos para ver esta página.</div>;
  }

  return <>{children}</>;
};

export default ProtectedRoute;
