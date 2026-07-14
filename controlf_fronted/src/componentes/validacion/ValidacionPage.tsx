import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';

interface ComentarioModeracion {
  id: number;
  texto: string;
  usuario: string;
  fecha: string;
  estado: string;
  notaModeracion: string | null;
  contextoTipo: string;   // LEY | POLITICO | N/D
  contextoTitulo: string;
  contextoId: string;
}

const ESTADOS = ['TODOS', 'PENDIENTE', 'APROBADO', 'OBSERVADO', 'RECHAZADO'];

const estadoBadge = (estado: string) => {
  switch (estado) {
    case 'APROBADO': return 'bg-emerald-100 text-emerald-700 border-emerald-200';
    case 'RECHAZADO': return 'bg-rose-100 text-rose-700 border-rose-200';
    case 'OBSERVADO': return 'bg-amber-100 text-amber-700 border-amber-200';
    case 'PENDIENTE': return 'bg-blue-100 text-blue-700 border-blue-200';
    default: return 'bg-slate-100 text-slate-600 border-slate-200';
  }
};

const ValidacionPage: React.FC = () => {
  const { apiFetch } = useAuth();
  const [comentarios, setComentarios] = useState<ComentarioModeracion[]>([]);
  const [resumen, setResumen] = useState<Record<string, number>>({});
  const [filtro, setFiltro] = useState('TODOS');
  const [isLoading, setIsLoading] = useState(true);
  const [procesando, setProcesando] = useState<number | null>(null);

  const cargar = async (estado: string) => {
    setIsLoading(true);
    try {
      const query = estado && estado !== 'TODOS' ? `?estado=${estado}` : '';
      const [listRes, resumenRes] = await Promise.all([
        apiFetch(`/api/validacion/comentarios${query}`),
        apiFetch('/api/validacion/resumen'),
      ]);
      const lista = listRes.ok ? await listRes.json() : [];
      const res = resumenRes.ok ? await resumenRes.json() : {};
      setComentarios(Array.isArray(lista) ? lista : []);
      setResumen(res || {});
    } catch (err) {
      console.error('Error al cargar comentarios:', err);
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    cargar(filtro);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [filtro]);

  const moderar = async (id: number, estado: string) => {
    let nota: string | null = null;
    if (estado === 'OBSERVADO' || estado === 'RECHAZADO') {
      nota = window.prompt(`Nota de moderación (${estado.toLowerCase()}), opcional:`, '') || null;
    }
    setProcesando(id);
    try {
      const res = await apiFetch(`/api/validacion/comentarios/${id}/estado`, {
        method: 'PATCH',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ estado, nota }),
      });
      if (res.ok) await cargar(filtro);
    } catch (err) {
      console.error('Error al moderar comentario:', err);
    } finally {
      setProcesando(null);
    }
  };

  return (
    <div className="max-w-5xl mx-auto space-y-8 pb-12">
      <div>
        <h2 className="text-2xl font-black text-primary-navy uppercase tracking-tighter">Panel de Validación</h2>
        <p className="text-slate-500">Revisa el contenido ciudadano. Solo los comentarios aprobados se publican en las vistas públicas.</p>
      </div>

      {/* Resumen */}
      <div className="grid gap-3 grid-cols-2 md:grid-cols-4">
        {['PENDIENTE', 'APROBADO', 'OBSERVADO', 'RECHAZADO'].map((e) => (
          <div key={e} className="rounded-2xl border border-slate-200 bg-white p-4 shadow-sm">
            <p className="text-[10px] font-black uppercase tracking-widest text-slate-400">{e}</p>
            <p className="mt-1 text-2xl font-black text-primary-navy">{resumen[e] ?? 0}</p>
          </div>
        ))}
      </div>

      {/* Filtros */}
      <div className="flex flex-wrap gap-2">
        {ESTADOS.map((e) => (
          <button
            key={e}
            onClick={() => setFiltro(e)}
            className={`px-4 py-2 rounded-full text-xs font-black uppercase tracking-wide border transition-colors ${filtro === e ? 'bg-primary-navy text-white border-primary-navy' : 'bg-white text-slate-500 border-slate-200 hover:bg-slate-50'}`}
          >
            {e}
          </button>
        ))}
      </div>

      {/* Lista */}
      {isLoading ? (
        <div className="space-y-3 animate-pulse">
          {[1, 2, 3].map((i) => <div key={i} className="h-28 bg-white rounded-2xl border border-slate-200"></div>)}
        </div>
      ) : comentarios.length === 0 ? (
        <div className="bg-white rounded-2xl border border-slate-200 p-12 text-center text-slate-500">
          No hay comentarios para este filtro.
        </div>
      ) : (
        <div className="space-y-3">
          {comentarios.map((c) => (
            <div key={c.id} className="bg-white rounded-2xl shadow-sm border border-slate-200 p-5">
              <div className="flex flex-wrap items-center gap-2 mb-2">
                <span className={`text-[9px] font-black px-2 py-0.5 rounded-full border uppercase ${estadoBadge(c.estado)}`}>{c.estado}</span>
                {c.contextoTipo !== 'N/D' && (
                  <Link
                    to={c.contextoTipo === 'LEY' ? `/ley/${c.contextoId}` : `/politico/${c.contextoId}`}
                    className="text-[10px] font-bold text-accent-blue hover:underline"
                  >
                    {c.contextoTipo === 'LEY' ? 'Ley' : 'Político'}: {c.contextoTitulo}
                  </Link>
                )}
                <span className="text-[10px] font-bold text-slate-400 ml-auto">{c.fecha}</span>
              </div>

              <p className="text-sm text-slate-700 bg-slate-50 border border-slate-100 rounded-xl p-3">"{c.texto}"</p>
              <p className="text-xs text-slate-500 mt-2">Por <span className="font-bold text-slate-600">{c.usuario}</span></p>
              {c.notaModeracion && (
                <p className="text-xs text-amber-700 mt-1 italic">Nota del validador: {c.notaModeracion}</p>
              )}

              <div className="mt-4 flex flex-wrap gap-2">
                <button
                  onClick={() => moderar(c.id, 'APROBADO')}
                  disabled={procesando === c.id || c.estado === 'APROBADO'}
                  className="rounded-xl bg-success-green px-4 py-2 text-xs font-black text-white hover:bg-green-600 transition-all disabled:opacity-50"
                >
                  Aprobar
                </button>
                <button
                  onClick={() => moderar(c.id, 'OBSERVADO')}
                  disabled={procesando === c.id || c.estado === 'OBSERVADO'}
                  className="rounded-xl bg-warning-amber px-4 py-2 text-xs font-black text-white hover:bg-amber-600 transition-all disabled:opacity-50"
                >
                  Observar
                </button>
                <button
                  onClick={() => moderar(c.id, 'RECHAZADO')}
                  disabled={procesando === c.id || c.estado === 'RECHAZADO'}
                  className="rounded-xl bg-danger-red px-4 py-2 text-xs font-black text-white hover:bg-red-600 transition-all disabled:opacity-50"
                >
                  Rechazar
                </button>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
};

export default ValidacionPage;
