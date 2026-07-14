import React, { useEffect, useMemo, useState } from 'react';
import { Link } from 'react-router-dom';

interface EventoAgenda {
  tipo: string;      // INGRESO_LEY | VOTACION
  fecha: string;     // yyyy-MM-dd
  titulo: string;
  detalle: string;
  categoria: string | null;
  estado: string | null;
  leyId: string;
  conteoVotos: number | null;
}

interface Agenda {
  eventos: EventoAgenda[];
  totalEventos: number;
  totalIngresos: number;
  totalVotaciones: number;
}

interface DebateLegislativo {
  leyId: string;
  titulo: string;
  codigo: string;
  estado: string;
  categoria: string | null;
  proponente: string | null;
  fechaIngreso: string | null;
  resumenOficial: string | null;
  resumenSimplificado: string | null;
  votosFavor: number;
  votosContra: number;
  votosAbstencion: number;
  totalVotos: number;
}

const MESES = ['Enero', 'Febrero', 'Marzo', 'Abril', 'Mayo', 'Junio', 'Julio', 'Agosto', 'Septiembre', 'Octubre', 'Noviembre', 'Diciembre'];

const etiquetaMes = (fecha: string) => {
  const [anio, mes] = fecha.split('-');
  const idx = Number(mes) - 1;
  return `${MESES[idx] ?? mes} ${anio}`;
};

const estadoBadge = (estado: string | null) => {
  switch ((estado || '').toUpperCase()) {
    case 'APROBADA': return 'bg-emerald-100 text-emerald-700 border-emerald-200';
    case 'VETADA': return 'bg-rose-100 text-rose-700 border-rose-200';
    case 'DEBATE':
    case 'EN_DEBATE': return 'bg-amber-100 text-amber-700 border-amber-200';
    default: return 'bg-slate-100 text-slate-600 border-slate-200';
  }
};

const AgendaPage: React.FC = () => {
  const [tab, setTab] = useState<'calendario' | 'debates'>('calendario');
  const [agenda, setAgenda] = useState<Agenda | null>(null);
  const [debates, setDebates] = useState<DebateLegislativo[]>([]);
  const [estadoDebate, setEstadoDebate] = useState('');
  const [isLoading, setIsLoading] = useState(true);
  const [expandido, setExpandido] = useState<string | null>(null);

  useEffect(() => {
    setIsLoading(true);
    fetch('/api/leyes/agenda')
      .then((res) => res.json())
      .then((data) => setAgenda(data))
      .catch((err) => console.error('Error al cargar agenda:', err))
      .finally(() => setIsLoading(false));
  }, []);

  useEffect(() => {
    const query = estadoDebate ? `?estado=${encodeURIComponent(estadoDebate)}` : '';
    fetch(`/api/leyes/debates${query}`)
      .then((res) => res.json())
      .then((data) => setDebates(Array.isArray(data) ? data : []))
      .catch((err) => console.error('Error al cargar debates:', err));
  }, [estadoDebate]);

  const eventosPorMes = useMemo(() => {
    const grupos: { mes: string; eventos: EventoAgenda[] }[] = [];
    const indice: Record<string, EventoAgenda[]> = {};
    (agenda?.eventos ?? []).forEach((ev) => {
      const clave = ev.fecha.slice(0, 7);
      if (!indice[clave]) {
        indice[clave] = [];
        grupos.push({ mes: clave, eventos: indice[clave] });
      }
      indice[clave].push(ev);
    });
    return grupos;
  }, [agenda]);

  return (
    <div className="max-w-5xl mx-auto space-y-8 pb-12">
      <div>
        <h2 className="text-2xl font-black text-primary-navy uppercase tracking-tighter">Agenda y Debates Legislativos</h2>
        <p className="text-slate-500">Calendario de la actividad legislativa y seguimiento de debates con sus transcripciones oficiales.</p>
      </div>

      {/* Tabs */}
      <div className="flex gap-2 border-b border-slate-200">
        <button
          onClick={() => setTab('calendario')}
          className={`px-5 py-3 text-sm font-black uppercase tracking-wide border-b-2 -mb-px transition-colors ${tab === 'calendario' ? 'border-accent-blue text-accent-blue' : 'border-transparent text-slate-400 hover:text-slate-600'}`}
        >
          Calendario
        </button>
        <button
          onClick={() => setTab('debates')}
          className={`px-5 py-3 text-sm font-black uppercase tracking-wide border-b-2 -mb-px transition-colors ${tab === 'debates' ? 'border-accent-blue text-accent-blue' : 'border-transparent text-slate-400 hover:text-slate-600'}`}
        >
          Debates y transcripciones
        </button>
      </div>

      {tab === 'calendario' && (
        <div className="space-y-6">
          <div className="grid gap-4 md:grid-cols-3">
            <div className="rounded-2xl border border-slate-200 bg-white p-6 shadow-sm">
              <p className="text-[10px] font-black uppercase tracking-widest text-slate-400">Eventos</p>
              <p className="mt-2 text-3xl font-black text-primary-navy">{agenda?.totalEventos ?? 0}</p>
            </div>
            <div className="rounded-2xl border border-slate-200 bg-white p-6 shadow-sm">
              <p className="text-[10px] font-black uppercase tracking-widest text-slate-400">Ingresos de leyes</p>
              <p className="mt-2 text-3xl font-black text-primary-navy">{agenda?.totalIngresos ?? 0}</p>
            </div>
            <div className="rounded-2xl border border-slate-200 bg-white p-6 shadow-sm">
              <p className="text-[10px] font-black uppercase tracking-widest text-slate-400">Votaciones</p>
              <p className="mt-2 text-3xl font-black text-primary-navy">{agenda?.totalVotaciones ?? 0}</p>
            </div>
          </div>

          {isLoading ? (
            <div className="animate-pulse bg-white h-96 rounded-2xl border border-slate-200"></div>
          ) : eventosPorMes.length === 0 ? (
            <div className="bg-white rounded-2xl border border-slate-200 p-12 text-center text-slate-500">
              No hay eventos con fecha registrada en la agenda.
            </div>
          ) : (
            eventosPorMes.map((grupo) => (
              <div key={grupo.mes} className="bg-white rounded-2xl shadow-sm border border-slate-200 overflow-hidden">
                <div className="px-6 py-4 bg-slate-50/70 border-b border-slate-100">
                  <h3 className="text-sm font-black text-primary-navy uppercase tracking-wide">{etiquetaMes(grupo.mes)}</h3>
                </div>
                <div className="divide-y divide-slate-50">
                  {grupo.eventos.map((ev, idx) => (
                    <div key={`${ev.leyId}-${ev.tipo}-${idx}`} className="px-6 py-4 flex items-start gap-4 hover:bg-slate-50/50 transition-colors">
                      <div className={`mt-1 p-2 rounded-lg ${ev.tipo === 'VOTACION' ? 'bg-blue-50 text-accent-blue' : 'bg-emerald-50 text-success-green'}`}>
                        {ev.tipo === 'VOTACION' ? (
                          <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round"><polyline points="20 6 9 17 4 12" /></svg>
                        ) : (
                          <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z" /><polyline points="14 2 14 8 20 8" /></svg>
                        )}
                      </div>
                      <div className="flex-grow min-w-0">
                        <div className="flex flex-wrap items-center gap-2 mb-1">
                          <span className="text-[10px] font-black text-slate-400">{ev.fecha}</span>
                          <span className="text-[10px] font-black text-slate-500 uppercase">{ev.tipo === 'VOTACION' ? 'Votación' : 'Ingreso'}</span>
                          {ev.estado && <span className={`text-[9px] font-black px-2 py-0.5 rounded-full border uppercase ${estadoBadge(ev.estado)}`}>{ev.estado}</span>}
                          {ev.categoria && <span className="text-[9px] font-bold px-2 py-0.5 rounded-full bg-slate-100 text-slate-500">{ev.categoria}</span>}
                        </div>
                        <Link to={`/ley/${ev.leyId}`} className="text-sm font-semibold text-primary-navy hover:text-accent-blue line-clamp-1">{ev.titulo}</Link>
                        <p className="text-xs text-slate-500">{ev.detalle}{ev.conteoVotos ? ` · ${ev.conteoVotos} votos` : ''}</p>
                      </div>
                    </div>
                  ))}
                </div>
              </div>
            ))
          )}
        </div>
      )}

      {tab === 'debates' && (
        <div className="space-y-6">
          <div className="flex flex-wrap items-center gap-2">
            {[
              { v: '', l: 'Todos' },
              { v: 'EN_DEBATE', l: 'En debate' },
              { v: 'APROBADA', l: 'Aprobadas' },
              { v: 'VETADA', l: 'Vetadas' },
            ].map((f) => (
              <button
                key={f.v}
                onClick={() => setEstadoDebate(f.v)}
                className={`px-4 py-2 rounded-full text-xs font-black uppercase tracking-wide border transition-colors ${estadoDebate === f.v ? 'bg-primary-navy text-white border-primary-navy' : 'bg-white text-slate-500 border-slate-200 hover:bg-slate-50'}`}
              >
                {f.l}
              </button>
            ))}
          </div>

          {debates.length === 0 ? (
            <div className="bg-white rounded-2xl border border-slate-200 p-12 text-center text-slate-500">
              No hay debates para el estado seleccionado.
            </div>
          ) : (
            debates.map((d) => {
              const abierto = expandido === d.leyId;
              return (
                <div key={d.leyId} className="bg-white rounded-2xl shadow-sm border border-slate-200 overflow-hidden">
                  <button
                    onClick={() => setExpandido(abierto ? null : d.leyId)}
                    className="w-full text-left px-6 py-5 flex items-start justify-between gap-4 hover:bg-slate-50/50 transition-colors"
                  >
                    <div className="min-w-0">
                      <div className="flex flex-wrap items-center gap-2 mb-1">
                        <span className={`text-[9px] font-black px-2 py-0.5 rounded-full border uppercase ${estadoBadge(d.estado)}`}>{d.estado}</span>
                        {d.categoria && <span className="text-[9px] font-bold px-2 py-0.5 rounded-full bg-slate-100 text-slate-500">{d.categoria}</span>}
                        <span className="text-[10px] font-bold text-slate-400">{d.codigo}</span>
                      </div>
                      <h3 className="text-sm font-black text-primary-navy line-clamp-1">{d.titulo}</h3>
                      <p className="text-xs text-slate-500 mt-1">
                        {d.proponente ? `Proponente: ${d.proponente}` : 'Sin proponente'} · {d.fechaIngreso ?? 'Sin fecha'} · {d.totalVotos} votos
                      </p>
                    </div>
                    <span className={`text-slate-400 transition-transform ${abierto ? 'rotate-180' : ''}`}>
                      <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round"><polyline points="6 9 12 15 18 9" /></svg>
                    </span>
                  </button>

                  {abierto && (
                    <div className="px-6 pb-6 space-y-5 border-t border-slate-100">
                      <div className="grid gap-3 sm:grid-cols-3 pt-4">
                        <div className="rounded-xl border border-emerald-100 bg-emerald-50 p-3 text-center">
                          <p className="text-[10px] font-bold text-emerald-600 uppercase">A favor</p>
                          <p className="text-xl font-black text-emerald-700">{d.votosFavor}</p>
                        </div>
                        <div className="rounded-xl border border-rose-100 bg-rose-50 p-3 text-center">
                          <p className="text-[10px] font-bold text-rose-600 uppercase">En contra</p>
                          <p className="text-xl font-black text-rose-700">{d.votosContra}</p>
                        </div>
                        <div className="rounded-xl border border-slate-200 bg-slate-50 p-3 text-center">
                          <p className="text-[10px] font-bold text-slate-500 uppercase">Abstención</p>
                          <p className="text-xl font-black text-slate-700">{d.votosAbstencion}</p>
                        </div>
                      </div>

                      <div>
                        <h4 className="text-[11px] font-black text-slate-400 uppercase tracking-widest mb-2">Resumen simplificado</h4>
                        <p className="text-sm text-slate-600 leading-relaxed bg-slate-50 border border-slate-100 rounded-xl p-4">
                          {d.resumenSimplificado || 'No hay resumen simplificado disponible.'}
                        </p>
                      </div>

                      <div>
                        <h4 className="text-[11px] font-black text-slate-400 uppercase tracking-widest mb-2">Transcripción / exposición de motivos (texto oficial)</h4>
                        <p className="text-sm text-slate-600 leading-relaxed whitespace-pre-line bg-slate-50 border border-slate-100 rounded-xl p-4 max-h-64 overflow-y-auto">
                          {d.resumenOficial || 'No hay texto oficial disponible para este expediente.'}
                        </p>
                      </div>

                      <Link to={`/ley/${d.leyId}`} className="inline-block text-xs font-black text-accent-blue hover:underline">
                        Ver expediente completo →
                      </Link>
                    </div>
                  )}
                </div>
              );
            })
          )}
        </div>
      )}
    </div>
  );
};

export default AgendaPage;
