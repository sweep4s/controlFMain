import React from 'react';

interface IndiceReputacionProps {
  indice: number;              // promedio 1-5
  totalCalificaciones: number;
  etiqueta: string;
}

const MAX = 5;

const IndiceReputacion: React.FC<IndiceReputacionProps> = ({ indice, totalCalificaciones, etiqueta }) => {
  const porcentaje = Math.max(0, Math.min(100, (indice / MAX) * 100));

  const getColor = (val: number) => {
    if (val >= 4) return 'text-success-green bg-success-green/10 border-success-green/20';
    if (val >= 3) return 'text-accent-blue bg-accent-blue/10 border-accent-blue/20';
    if (val >= 2) return 'text-warning-amber bg-warning-amber/10 border-warning-amber/20';
    return 'text-danger-red bg-danger-red/10 border-danger-red/20';
  };

  const getBar = (val: number) => {
    if (val >= 4) return 'bg-success-green';
    if (val >= 3) return 'bg-accent-blue';
    if (val >= 2) return 'bg-warning-amber';
    return 'bg-danger-red';
  };

  const sinDatos = totalCalificaciones === 0;

  return (
    <div className="bg-white rounded-xl shadow-sm border border-slate-200 p-8 mb-8">
      <h4 className="text-sm font-bold text-primary-navy uppercase mb-6 tracking-wide flex items-center gap-2">
        <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"/></svg>
        Índice de Reputación Ciudadana
      </h4>

      <div className="flex flex-col md:flex-row items-center gap-12">
        <div className={`relative flex flex-col items-center justify-center w-32 h-32 rounded-full border-4 ${getColor(indice)}`}>
          <span className="text-3xl font-black">{sinDatos ? '—' : indice.toFixed(1)}</span>
          <span className="text-[10px] font-bold uppercase tracking-tighter opacity-70">de {MAX}</span>
        </div>

        <div className="flex-grow w-full space-y-6">
          <div className="space-y-2">
            <div className="flex justify-between items-end">
              <span className="text-sm font-bold text-slate-700">Promedio de calificaciones ciudadanas</span>
              <span className={`text-xs font-black px-2 py-0.5 rounded border uppercase ${getColor(indice)}`}>
                {etiqueta}
              </span>
            </div>
            <div className="w-full h-4 bg-slate-100 rounded-full overflow-hidden border border-slate-200 shadow-inner">
              <div
                className={`h-full rounded-full transition-all duration-1000 ${getBar(indice)} shadow-sm`}
                style={{ width: `${porcentaje}%` }}
              ></div>
            </div>
            <div className="flex items-center justify-between pt-1">
              <div className="flex gap-1">
                {[1, 2, 3, 4, 5].map((star) => (
                  <svg
                    key={star}
                    xmlns="http://www.w3.org/2000/svg"
                    width="18"
                    height="18"
                    viewBox="0 0 24 24"
                    fill={!sinDatos && indice >= star - 0.25 ? 'currentColor' : 'none'}
                    stroke="currentColor"
                    strokeWidth="2"
                    className={!sinDatos && indice >= star - 0.25 ? 'text-warning-amber' : 'text-slate-300'}
                  >
                    <polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2" />
                  </svg>
                ))}
              </div>
              <span className="text-[11px] font-bold text-slate-400 uppercase tracking-widest">
                {totalCalificaciones} {totalCalificaciones === 1 ? 'calificación' : 'calificaciones'}
              </span>
            </div>
          </div>

          <p className="text-xs text-slate-500 italic">
            * El índice de reputación consolida las calificaciones ciudadanas (escala 1 a 5) enviadas desde la sección de participación.
          </p>
        </div>
      </div>
    </div>
  );
};

export default IndiceReputacion;
