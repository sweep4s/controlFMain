import React from 'react';

interface ContenidoLeyProps {
  id: string;
  titulo: string;
  resumenEjecutivo: string | null | undefined;
  impactoSocial: string;
  onTraducir?: () => void;
  isTraduciendo?: boolean;
}

const ContenidoLey: React.FC<ContenidoLeyProps> = ({ resumenEjecutivo, impactoSocial, onTraducir, isTraduciendo }) => {
  return (
    <div className="bg-white rounded-xl shadow-sm border border-slate-200 overflow-hidden mb-8">
      <div className="px-8 py-6 border-b border-slate-100 bg-slate-50/50">
        <h4 className="text-sm font-bold text-primary-navy uppercase tracking-wide flex items-center gap-2">
          <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><path d="M4 19.5A2.5 2.5 0 0 1 6.5 17H20" /><path d="M6.5 2H20v20H6.5A2.5 2.5 0 0 1 4 19.5v-15A2.5 2.5 0 0 1 6.5 2z" /></svg>
          Contenido de la Ley (RF-02)
        </h4>
      </div>

      <div className="p-8 space-y-8">
        <div>
          <h5 className="text-xs font-black text-slate-400 uppercase tracking-widest mb-3">Resumen Ejecutivo</h5>
          {resumenEjecutivo && resumenEjecutivo.trim() ? (
            <p className="text-slate-600 leading-relaxed text-sm bg-slate-50 p-4 rounded-lg border border-slate-100 italic whitespace-pre-line">
              "{resumenEjecutivo}"
            </p>
          ) : (
            <div className="bg-slate-50 p-6 rounded-lg border border-slate-100 flex flex-col items-center justify-center text-center gap-3">
              <p className="text-slate-500 text-sm italic">
                Esta ley aún no tiene una explicación en lenguaje sencillo.
              </p>
              <button
                onClick={onTraducir}
                disabled={isTraduciendo}
                className="bg-accent-blue hover:bg-blue-600 text-white font-bold text-sm px-6 py-2.5 rounded-xl transition-colors disabled:opacity-60 flex items-center gap-2"
              >
                {isTraduciendo ? (
                  <>
                    <svg className="animate-spin h-4 w-4 text-white" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
                      <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
                      <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                    </svg>
                    Generando explicación...
                  </>
                ) : (
                  <>
                    <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round"><path d="m5 8 6 6 6-6" /><path d="m4 14 6 6 8-8" /></svg>
                    Traducir
                  </>
                )}
              </button>
            </div>
          )}
        </div>

        <div>
          <h5 className="text-xs font-black text-slate-400 uppercase tracking-widest mb-3">Impacto Social</h5>
          <p className="text-slate-600 leading-relaxed text-sm">
            {impactoSocial}
          </p>
        </div>
      </div>
    </div>
  );
};

export default ContenidoLey;
