import { useEffect, useState } from 'react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';

const Header = () => {
  const location = useLocation();
  const navigate = useNavigate();
  const { isAuthenticated, user, logout } = useAuth();
  const [menuAbierto, setMenuAbierto] = useState(false);

  const role = user?.rol;
  const navItems = [
    { name: 'Dashboard', path: '/dashboard' },
    { name: 'Políticos', path: '/' },
    { name: 'Leyes', path: '/leyes' },
    { name: 'Agenda', path: '/agenda' },
    { name: 'Métricas', path: '/metricas' },
    { name: 'Comparar', path: '/comparar' },
    ...(isAuthenticated ? [{ name: 'Alertas', path: '/alertas' }] : []),
    ...(role === 'VALIDADOR' || role === 'ADMIN' ? [{ name: 'Validación', path: '/validacion' }] : []),
    ...(role === 'ADMIN' ? [{ name: 'Admin', path: '/admin' }] : []),
  ];

  // El panel lateral se cierra automáticamente al cambiar de ruta.
  useEffect(() => {
    setMenuAbierto(false);
  }, [location.pathname]);

  // Cierre con la tecla Escape mientras el panel está abierto.
  useEffect(() => {
    if (!menuAbierto) return;
    const onKey = (e: KeyboardEvent) => {
      if (e.key === 'Escape') setMenuAbierto(false);
    };
    document.addEventListener('keydown', onKey);
    return () => document.removeEventListener('keydown', onKey);
  }, [menuAbierto]);

  return (
    <>
      <header className="bg-primary-navy text-white shadow-md">
        <div className="container mx-auto px-4 h-16 flex items-center justify-between">
          <div className="flex items-center gap-2">
            <button
              type="button"
              onClick={() => setMenuAbierto(true)}
              aria-label="Abrir menú de navegación"
              aria-expanded={menuAbierto}
              className="-ml-1 mr-1 rounded-md p-2 text-slate-200 transition-colors hover:bg-slate-700 hover:text-white"
            >
              <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" aria-hidden="true">
                <line x1="3" y1="6" x2="21" y2="6" />
                <line x1="3" y1="12" x2="21" y2="12" />
                <line x1="3" y1="18" x2="21" y2="18" />
              </svg>
            </button>
            <div className="bg-accent-blue p-1.5 rounded">
              <span className="font-bold text-lg leading-none">CF</span>
            </div>
            <h1 className="text-lg font-bold tracking-tight hidden sm:block">
              Plataforma de Auditoría Ciudadana
            </h1>
          </div>

          <div className="flex items-center gap-3">
            {isAuthenticated ? (
              <>
                <div className="rounded-full bg-slate-700 px-3 py-1 text-xs font-semibold text-slate-100">{user?.nombre ?? user?.email}</div>
                <button onClick={() => { logout(); navigate('/login'); }} className="rounded-full border border-slate-600 px-3 py-1 text-sm text-slate-100">Salir</button>
              </>
            ) : (
              <Link to="/login" className="rounded-full bg-accent-blue px-3 py-1 text-sm font-semibold text-white">Entrar</Link>
            )}
          </div>
        </div>
      </header>

      {/* Fondo semitransparente: cierra el panel al hacer clic fuera */}
      <div
        className={`fixed inset-0 z-40 bg-black/50 transition-opacity duration-300 ${menuAbierto ? 'opacity-100' : 'pointer-events-none opacity-0'}`}
        onClick={() => setMenuAbierto(false)}
        aria-hidden="true"
      ></div>

      {/* Panel lateral desplegable con la navegación (reemplaza la barra superior deslizante) */}
      <aside
        className={`fixed inset-y-0 left-0 z-50 flex w-64 max-w-[80vw] flex-col bg-primary-navy text-white shadow-2xl transition-transform duration-300 ease-out ${menuAbierto ? 'translate-x-0' : '-translate-x-full'}`}
        aria-hidden={!menuAbierto}
      >
        <div className="flex h-16 shrink-0 items-center justify-between border-b border-slate-700 px-4">
          <div className="flex items-center gap-2">
            <div className="bg-accent-blue p-1.5 rounded">
              <span className="font-bold text-lg leading-none">CF</span>
            </div>
            <span className="text-lg font-bold tracking-tight">ControlF</span>
          </div>
          <button
            type="button"
            onClick={() => setMenuAbierto(false)}
            aria-label="Cerrar menú de navegación"
            className="rounded-md p-2 text-slate-300 transition-colors hover:bg-slate-700 hover:text-white"
          >
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" aria-hidden="true">
              <line x1="18" y1="6" x2="6" y2="18" />
              <line x1="6" y1="6" x2="18" y2="18" />
            </svg>
          </button>
        </div>

        <nav className="flex flex-col gap-1 overflow-y-auto p-3">
          {navItems.map((item) => {
            const isActive = location.pathname === item.path || (item.path !== '/' && location.pathname.startsWith(item.path));
            return (
              <Link
                key={item.path}
                to={item.path}
                onClick={() => setMenuAbierto(false)}
                className={`rounded-md px-3 py-2.5 text-sm font-medium transition-colors ${
                  isActive
                    ? 'bg-accent-blue text-white'
                    : 'text-slate-300 hover:text-white hover:bg-slate-700'
                }`}
              >
                {item.name}
              </Link>
            );
          })}
        </nav>
      </aside>
    </>
  );
};

export default Header;
