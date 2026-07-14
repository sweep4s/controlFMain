import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';

interface Suscripcion {
  id: number;
  categoria: string | null;
  fechaCreacion: string | null;
}

interface Alerta {
  tipo: string;       // LEY | VOTACION
  titulo: string;
  categoria: string | null;
  fecha: string;
  detalle: string;
  leyId: string;
  nuevo: boolean;
}

const AlertasPage: React.FC = () => {
  const { apiFetch } = useAuth();
  const [suscripciones, setSuscripciones] = useState<Suscripcion[]>([]);
  const [alertas, setAlertas] = useState<Alerta[]>([]);
  const [categorias, setCategorias] = useState<string[]>([]);
  const [nuevaCategoria, setNuevaCategoria] = useState('');
  const [isLoading, setIsLoading] = useState(true);
  const [guardando, setGuardando] = useState(false);

  const cargar = async () => {
    setIsLoading(true);
    try {
      const [subsRes, alertasRes] = await Promise.all([
        apiFetch('/api/alertas/suscripciones'),
        apiFetch('/api/alertas'),
      ]);
      const subs = subsRes.ok ? await subsRes.json() : [];
      const alrt = alertasRes.ok ? await alertasRes.json() : [];
      setSuscripciones(Array.isArray(subs) ? subs : []);
      setAlertas(Array.isArray(alrt) ? alrt : []);
    } catch (err) {
      console.error('Error al cargar alertas:', err);
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    fetch('/api/leyes/filtros')
      .then((res) => res.json())
      .then((data) => setCategorias(Array.isArray(data?.categorias) ? data.categorias : []))
      .catch((err) => console.error('Error al cargar categorías:', err));
    cargar();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const suscribir = async () => {
    setGuardando(true);
    try {
      const body = nuevaCategoria ? { categoria: nuevaCategoria } : {};
      const res = await apiFetch('/api/alertas/suscripciones', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(body),
      });
      if (res.ok) {
        setNuevaCategoria('');
        await cargar();
      }
    } catch (err) {
      console.error('Error al suscribir:', err);
    } finally {
      setGuardando(false);
    }
  };

  const eliminar = async (id: number) => {
    try {
      const res = await apiFetch(`/api/alertas/suscripciones/${id}`, { method: 'DELETE' });
      if (res.ok) await cargar();
    } catch (err) {
      console.error('Error al eliminar suscripción:', err);
    }
  };

  const nuevas = alertas.filter((a) => a.nuevo).length;

  return (
    <div className="max-w-5xl mx-auto space-y-8 pb-12">
      <div>
        <h2 className="text-2xl font-black text-primary-navy uppercase tracking-tighter">Alertas y Suscripciones</h2>
        <p className="text-slate-500">Suscríbete a categorías y recibe alertas de nuevas leyes y votaciones relevantes.</p>
      </div>

      {/* Gestión de suscripciones */}
      <div className="bg-white rounded-2xl shadow-sm border border-slate-200 p-6">
        <h3 className="text-sm font-black text-primary-navy uppercase tracking-wide mb-4">Mis suscripciones</h3>
        <div className="flex flex-col gap-3 md:flex-row md:items-end">
          <div className="flex-grow">
            <label className="text-[10px] font-black uppercase tracking-widest text-slate-500">Categoría</label>
            <select
              value={nuevaCategoria}
              onChange={(e) => setNuevaCategoria(e.target.value)}
              className="mt-1 w-full rounded-xl border border-slate-200 bg-white px-3 py-2.5 text-sm text-slate-700 focus:border-accent-blue focus:outline-none"
            >
              <option value="">Todas las categorías</option>
              {categorias.map((c) => <option key={c} value={c}>{c}</option>)}
            </select>
          </div>
          <button
            onClick={suscribir}
            disabled={guardando}
            className="rounded-xl bg-primary-navy px-5 py-2.5 text-sm font-black text-white hover:bg-slate-800 transition-all disabled:opacity-60"
          >
            {guardando ? 'Guardando...' : 'Suscribirme'}
          </button>
        </div>

        <div className="mt-4 flex flex-wrap gap-2">
          {suscripciones.length === 0 ? (
            <p className="text-sm text-slate-400 italic">No tienes suscripciones activas.</p>
          ) : (
            suscripciones.map((s) => (
              <span key={s.id} className="inline-flex items-center gap-2 rounded-full border border-accent-blue/20 bg-accent-blue/10 px-3 py-1.5 text-xs font-bold text-accent-blue">
                {s.categoria ?? 'Todas las categorías'}
                <button onClick={() => eliminar(s.id)} className="text-slate-500 hover:text-rose-600" aria-label="Eliminar suscripción">×</button>
              </span>
            ))
          )}
        </div>
      </div>

      {/* Feed de alertas */}
      <div className="bg-white rounded-2xl shadow-sm border border-slate-200 overflow-hidden">
        <div className="px-6 py-4 border-b border-slate-100 bg-slate-50/50 flex items-center justify-between">
          <h3 className="text-sm font-black text-primary-navy uppercase tracking-wide">Alertas relevantes</h3>
          {nuevas > 0 && (
            <span className="rounded-full bg-danger-red/10 text-danger-red px-3 py-1 text-[10px] font-black uppercase">{nuevas} nuevas</span>
          )}
        </div>

        {isLoading ? (
          <div className="p-6 space-y-3 animate-pulse">
            {[1, 2, 3].map((i) => <div key={i} className="h-16 bg-slate-100 rounded-xl"></div>)}
          </div>
        ) : suscripciones.length === 0 ? (
          <div className="p-12 text-center text-slate-500">
            Suscríbete a una categoría para empezar a recibir alertas.
          </div>
        ) : alertas.length === 0 ? (
          <div className="p-12 text-center text-slate-500">
            No hay leyes ni votaciones en tus categorías suscritas por el momento.
          </div>
        ) : (
          <div className="divide-y divide-slate-50">
            {alertas.map((a, idx) => (
              <div key={`${a.leyId}-${a.tipo}-${idx}`} className="px-6 py-4 flex items-start gap-4 hover:bg-slate-50/50 transition-colors">
                <div className={`mt-1 p-2 rounded-lg ${a.tipo === 'VOTACION' ? 'bg-blue-50 text-accent-blue' : 'bg-emerald-50 text-success-green'}`}>
                  {a.tipo === 'VOTACION' ? (
                    <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round"><polyline points="20 6 9 17 4 12" /></svg>
                  ) : (
                    <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z" /><polyline points="14 2 14 8 20 8" /></svg>
                  )}
                </div>
                <div className="flex-grow min-w-0">
                  <div className="flex flex-wrap items-center gap-2 mb-1">
                    <span className="text-[10px] font-black text-slate-400">{a.fecha}</span>
                    <span className="text-[10px] font-black text-slate-500 uppercase">{a.tipo === 'VOTACION' ? 'Votación' : 'Nueva ley'}</span>
                    {a.categoria && <span className="text-[9px] font-bold px-2 py-0.5 rounded-full bg-slate-100 text-slate-500">{a.categoria}</span>}
                    {a.nuevo && <span className="text-[9px] font-black px-2 py-0.5 rounded-full bg-danger-red/10 text-danger-red uppercase">Nuevo</span>}
                  </div>
                  <Link to={`/ley/${a.leyId}`} className="text-sm font-semibold text-primary-navy hover:text-accent-blue line-clamp-1">{a.titulo}</Link>
                  <p className="text-xs text-slate-500">{a.detalle}</p>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
};

export default AlertasPage;
