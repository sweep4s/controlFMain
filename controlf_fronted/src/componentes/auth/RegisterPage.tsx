import React, { useState, useEffect } from 'react';
import { Link, useNavigate, Navigate } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import { evaluatePassword, isPasswordStrong, isValidEmail } from './authValidation';

const FRASES = [
  'La herramienta del pueblo y para el pueblo',
  'Transparencia que rinde cuentas',
  'Fiscaliza el poder, defiende lo público',
  'Cada peso, bajo la lupa',
  'La rendición de cuentas al alcance de todos',
  'Datos que exigen responsabilidad',
  'Vigilancia ciudadana, gestión clara',
  'Auditar es un derecho, no un privilegio',
  'Del dato a la acción, sin intermediarios',
  'El control ciudadano empieza aquí',
  'Poder observado, poder responsable',
];

// Roles ya existentes en el sistema. Se ofrecen solo para pruebas de desarrollo;
// no se crean roles nuevos ni se altera la lógica de permisos.
const ROLES_DISPONIBLES = [
  { value: 'CIUDADANO', label: 'Ciudadano' },
  { value: 'ADMIN', label: 'Administrador' },
  { value: 'VALIDADOR', label: 'Validador' },
];

interface FieldErrors {
  nombre?: string;
  email?: string;
  password?: string;
}

const inputBaseClass =
  'w-full rounded-xl border bg-slate-50/50 px-4 py-3 text-sm text-slate-800 shadow-sm transition-colors placeholder:text-slate-400 focus:bg-white focus:outline-none focus:ring-2';

const RegisterPage: React.FC = () => {
  const navigate = useNavigate();
  const { login, isAuthenticated } = useAuth();
  const [nombre, setNombre] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [rol, setRol] = useState('CIUDADANO');
  const [error, setError] = useState('');
  const [fieldErrors, setFieldErrors] = useState<FieldErrors>({});
  const [touched, setTouched] = useState({ nombre: false, email: false, password: false });
  const [isSubmitting, setIsSubmitting] = useState(false);

  const [nombreDisponible, setNombreDisponible] = useState<boolean | null>(null);
  const [sugerencias, setSugerencias] = useState<string[]>([]);
  const [isCheckingNombre, setIsCheckingNombre] = useState(false);
  const [emailDisponible, setEmailDisponible] = useState<boolean | null>(null);
  const [isCheckingEmail, setIsCheckingEmail] = useState(false);

  const [fraseIndex, setFraseIndex] = useState(0);
  const [fraseVisible, setFraseVisible] = useState(true);

  const requisitos = evaluatePassword(password);

  useEffect(() => {
    const CICLO = 4500;
    const DURACION_FADE = 600;

    const interval = setInterval(() => {
      setFraseVisible(false);
      window.setTimeout(() => {
        setFraseIndex((prev) => (prev + 1) % FRASES.length);
        setFraseVisible(true);
      }, DURACION_FADE);
    }, CICLO);

    return () => clearInterval(interval);
  }, []);

  // Comprobación de disponibilidad del nombre (con retraso para no consultar en cada tecla).
  useEffect(() => {
    const value = nombre.trim();
    if (value.length < 2) {
      setNombreDisponible(null);
      setSugerencias([]);
      setIsCheckingNombre(false);
      return;
    }

    setIsCheckingNombre(true);
    const timeout = window.setTimeout(async () => {
      try {
        const response = await fetch('/api/auth/availability', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ nombre: value }),
        });
        if (response.ok) {
          const data = await response.json();
          setNombreDisponible(typeof data.nombreDisponible === 'boolean' ? data.nombreDisponible : null);
          setSugerencias(Array.isArray(data.sugerencias) ? data.sugerencias : []);
        }
      } catch {
        // La comprobación final ocurre al enviar el formulario; aquí fallamos en silencio.
      } finally {
        setIsCheckingNombre(false);
      }
    }, 450);

    return () => window.clearTimeout(timeout);
  }, [nombre]);

  // Comprobación de disponibilidad del correo (solo si el formato es válido).
  useEffect(() => {
    const value = email.trim();
    if (!isValidEmail(value)) {
      setEmailDisponible(null);
      setIsCheckingEmail(false);
      return;
    }

    setIsCheckingEmail(true);
    const timeout = window.setTimeout(async () => {
      try {
        const response = await fetch('/api/auth/availability', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ email: value }),
        });
        if (response.ok) {
          const data = await response.json();
          setEmailDisponible(typeof data.emailDisponible === 'boolean' ? data.emailDisponible : null);
        }
      } catch {
        // Silencioso: la validación definitiva es la respuesta del registro.
      } finally {
        setIsCheckingEmail(false);
      }
    }, 450);

    return () => window.clearTimeout(timeout);
  }, [email]);

  if (isAuthenticated) {
    return <Navigate to="/" replace />;
  }

  const validar = (): FieldErrors => {
    const errores: FieldErrors = {};

    if (!nombre.trim()) {
      errores.nombre = 'Ingresa un nombre de usuario.';
    } else if (nombre.trim().length < 2) {
      errores.nombre = 'El nombre es demasiado corto.';
    } else if (nombreDisponible === false) {
      errores.nombre = 'Este nombre ya está en uso. Prueba una de las sugerencias.';
    }

    if (!email.trim()) {
      errores.email = 'Ingresa tu correo electrónico.';
    } else if (!isValidEmail(email)) {
      errores.email = 'El correo no tiene un formato válido.';
    } else if (emailDisponible === false) {
      errores.email = 'Este correo ya está registrado.';
    }

    if (!isPasswordStrong(password)) {
      errores.password = 'La contraseña aún no cumple todos los requisitos.';
    }

    return errores;
  };

  const seleccionarSugerencia = (sugerencia: string) => {
    setNombre(sugerencia);
    setFieldErrors((prev) => ({ ...prev, nombre: undefined }));
  };

  const handleSubmit = async (event: React.FormEvent) => {
    event.preventDefault();
    setError('');
    setTouched({ nombre: true, email: true, password: true });

    // Se valida antes de enviar; si algo falla, no se envía ni se borra lo ingresado.
    const errores = validar();
    setFieldErrors(errores);
    if (Object.keys(errores).length > 0) {
      return;
    }

    setIsSubmitting(true);
    try {
      const response = await fetch('/api/auth/registro', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ nombre: nombre.trim(), email: email.trim(), password, rol }),
      });

      if (!response.ok) {
        const data = await response.json().catch(() => null);
        if (response.status === 409 && data?.campo === 'nombre') {
          setNombreDisponible(false);
          setFieldErrors((prev) => ({ ...prev, nombre: data.mensaje ?? 'Este nombre de usuario ya está en uso.' }));
        } else if (response.status === 409 && data?.campo === 'email') {
          setEmailDisponible(false);
          setFieldErrors((prev) => ({ ...prev, email: data.mensaje ?? 'Este correo ya está registrado.' }));
        } else {
          setError('No se pudo crear la cuenta. Revisa los datos e inténtalo de nuevo.');
        }
        return;
      }

      const data = await response.json();
      login(data.token, {
        id: data.user?.id ?? 0,
        email: data.user?.email ?? email,
        nombre: data.user?.nombre ?? nombre,
        rol: data.user?.rol ?? 'CIUDADANO',
      });
      navigate('/');
    } catch {
      setError('No se pudo conectar con el servidor.');
    } finally {
      setIsSubmitting(false);
    }
  };

  const nombreEnUso = touched.nombre && nombreDisponible === false;
  const nombreLibre = touched.nombre && nombreDisponible === true && !fieldErrors.nombre;
  const emailEnUso = touched.email && emailDisponible === false;

  const claseInput = (hayError: boolean) =>
    `${inputBaseClass} ${hayError ? 'border-rose-300 focus:border-rose-400 focus:ring-rose-200' : 'border-slate-200 focus:border-accent-blue focus:ring-accent-blue/20'}`;

  return (
    <div className="login-animated-bg min-h-screen w-full flex flex-col items-center justify-center px-4 py-12">
      {/* Encabezado corporativo */}
      <header className="mb-10 text-center">
        <span className="inline-flex items-center gap-2 rounded-full border border-white/20 bg-white/10 px-4 py-1.5 text-[11px] font-bold uppercase tracking-[0.25em] text-white/80 shadow-sm backdrop-blur-sm">
          <span className="h-1.5 w-1.5 rounded-full bg-violet-300"></span>
          Plataforma de auditoría ciudadana
        </span>
        <h1 className="mt-6 text-5xl sm:text-6xl font-black tracking-tighter text-white drop-shadow-sm">
          Control<span className="text-violet-300">&nbsp;F</span>
        </h1>
        <div className="mx-auto mt-4 h-px w-24 bg-gradient-to-r from-transparent via-white/40 to-transparent"></div>
      </header>

      {/* Tarjeta de registro */}
      <div className="w-full max-w-md rounded-2xl border border-slate-200 bg-white p-8 shadow-xl shadow-slate-200/60">
        <h2 className="text-2xl font-black tracking-tight text-primary-navy">Crear cuenta</h2>
        <p className="mt-2 text-sm text-slate-500">
          Regístrate para participar en el debate público.
        </p>

        <form onSubmit={handleSubmit} className="mt-7 space-y-4" noValidate>
          {error && (
            <p className="rounded-xl border border-rose-200 bg-rose-50 p-3 text-sm font-medium text-rose-600">
              {error}
            </p>
          )}

          {/* Nombre de usuario */}
          <div className="space-y-1.5">
            <label htmlFor="nombre" className="block text-xs font-bold uppercase tracking-wide text-slate-500">
              Nombre de usuario
            </label>
            <input
              id="nombre"
              value={nombre}
              onChange={(e) => {
                setNombre(e.target.value);
                setFieldErrors((prev) => ({ ...prev, nombre: undefined }));
              }}
              onBlur={() => setTouched((prev) => ({ ...prev, nombre: true }))}
              type="text"
              autoComplete="username"
              placeholder="¿Cómo quieres que te vean?"
              className={claseInput(Boolean(fieldErrors.nombre) || nombreEnUso)}
            />
            {fieldErrors.nombre ? (
              <p className="text-xs font-medium text-rose-600">{fieldErrors.nombre}</p>
            ) : isCheckingNombre ? (
              <p className="text-xs text-slate-400">Comprobando disponibilidad…</p>
            ) : nombreEnUso ? (
              <p className="text-xs font-medium text-rose-600">Este nombre ya está en uso.</p>
            ) : nombreLibre ? (
              <p className="text-xs font-medium text-emerald-600">¡Nombre disponible!</p>
            ) : null}

            {nombreEnUso && sugerencias.length > 0 && (
              <div className="pt-1">
                <p className="text-[11px] font-semibold text-slate-500">Prueba con:</p>
                <div className="mt-1.5 flex flex-wrap gap-2">
                  {sugerencias.map((sugerencia) => (
                    <button
                      key={sugerencia}
                      type="button"
                      onClick={() => seleccionarSugerencia(sugerencia)}
                      className="rounded-full border border-accent-blue/30 bg-accent-blue/10 px-3 py-1 text-xs font-semibold text-accent-blue transition-colors hover:bg-accent-blue/20"
                    >
                      {sugerencia}
                    </button>
                  ))}
                </div>
              </div>
            )}
          </div>

          {/* Correo electrónico */}
          <div className="space-y-1.5">
            <label htmlFor="email" className="block text-xs font-bold uppercase tracking-wide text-slate-500">
              Correo electrónico
            </label>
            <input
              id="email"
              value={email}
              onChange={(e) => {
                setEmail(e.target.value);
                setFieldErrors((prev) => ({ ...prev, email: undefined }));
              }}
              onBlur={() => setTouched((prev) => ({ ...prev, email: true }))}
              type="email"
              autoComplete="email"
              placeholder="correo@dominio.com"
              className={claseInput(Boolean(fieldErrors.email) || emailEnUso)}
            />
            {fieldErrors.email ? (
              <p className="text-xs font-medium text-rose-600">{fieldErrors.email}</p>
            ) : touched.email && email.trim() && !isValidEmail(email) ? (
              <p className="text-xs font-medium text-rose-600">El correo no tiene un formato válido.</p>
            ) : isCheckingEmail ? (
              <p className="text-xs text-slate-400">Comprobando disponibilidad…</p>
            ) : emailEnUso ? (
              <p className="text-xs font-medium text-rose-600">Este correo ya está registrado.</p>
            ) : null}
          </div>

          {/* Contraseña */}
          <div className="space-y-1.5">
            <label htmlFor="password" className="block text-xs font-bold uppercase tracking-wide text-slate-500">
              Contraseña
            </label>
            <input
              id="password"
              value={password}
              onChange={(e) => {
                setPassword(e.target.value);
                setFieldErrors((prev) => ({ ...prev, password: undefined }));
              }}
              onBlur={() => setTouched((prev) => ({ ...prev, password: true }))}
              type="password"
              autoComplete="new-password"
              placeholder="Crea una contraseña segura"
              className={claseInput(Boolean(fieldErrors.password))}
            />
            {fieldErrors.password && (
              <p className="text-xs font-medium text-rose-600">{fieldErrors.password}</p>
            )}

            {(password.length > 0 || touched.password) && (
              <ul className="mt-2 grid grid-cols-1 gap-1 rounded-xl border border-slate-100 bg-slate-50/70 p-3">
                {requisitos.map((requisito) => (
                  <li
                    key={requisito.id}
                    className={`flex items-center gap-2 text-xs ${requisito.met ? 'text-emerald-600' : 'text-slate-400'}`}
                  >
                    <span
                      className={`flex h-4 w-4 shrink-0 items-center justify-center rounded-full text-[10px] font-black ${
                        requisito.met ? 'bg-emerald-100 text-emerald-600' : 'bg-slate-200 text-slate-400'
                      }`}
                    >
                      {requisito.met ? '✓' : '·'}
                    </span>
                    {requisito.label}
                  </li>
                ))}
              </ul>
            )}
          </div>

          {/* Rol (solo pruebas) */}
          <div className="space-y-1.5">
            <label htmlFor="rol" className="block text-xs font-bold uppercase tracking-wide text-slate-500">
              Rol <span className="font-medium normal-case text-slate-400">(solo para pruebas)</span>
            </label>
            <select
              id="rol"
              value={rol}
              onChange={(e) => setRol(e.target.value)}
              className={`${inputBaseClass} border-slate-200 focus:border-accent-blue focus:ring-accent-blue/20`}
            >
              {ROLES_DISPONIBLES.map((opcion) => (
                <option key={opcion.value} value={opcion.value}>
                  {opcion.label}
                </option>
              ))}
            </select>
          </div>

          <button
            type="submit"
            disabled={isSubmitting}
            className="w-full rounded-xl bg-primary-navy px-4 py-3 text-sm font-bold text-white shadow-sm transition-all hover:bg-slate-800 focus:outline-none focus:ring-2 focus:ring-primary-navy/30 disabled:cursor-not-allowed disabled:opacity-60"
          >
            {isSubmitting ? 'Creando cuenta...' : 'Registrarme'}
          </button>
        </form>

        <p className="mt-6 text-center text-sm text-slate-500">
          ¿Ya tienes cuenta?{' '}
          <Link to="/login" className="font-semibold text-accent-blue hover:text-blue-600">
            Iniciar sesión
          </Link>
        </p>
      </div>

      {/* Frases rotativas */}
      <div className="mt-10 flex h-12 items-center justify-center px-4">
        <p
          className={`max-w-lg text-center text-base sm:text-lg font-medium italic text-white/85 drop-shadow-sm transition-opacity duration-[600ms] ease-in-out ${
            fraseVisible ? 'opacity-100' : 'opacity-0'
          }`}
        >
          “{FRASES[fraseIndex]}”
        </p>
      </div>
    </div>
  );
};

export default RegisterPage;
