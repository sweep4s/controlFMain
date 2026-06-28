import React from 'react';

interface MantenimientoSistemaProps {
  info: {
    id: string;
    titulo: string;
    codigoReferencia: string;
    estadoBaseDeDatos: boolean;
    estadoEtiqueta: string;
    fechaUltimoRespaldo: string;
    cargaServidorPorcentaje: number;
    accionesDisponibles: string[];
  };
  onAccion: (accion: string) => void;
}

const MantenimientoSistema: React.FC<MantenimientoSistemaProps> = ({ info, onAccion }) => {
  return (
    <div className="bg-white rounded-xl shadow-sm border border-slate-200 overflow-hidden mb-8">
      <div className="px-8 py-6 border-b border-slate-100 bg-slate-50/50">
        <h4 className="text-sm font-bold text-primary-navy uppercase tracking-wide flex items-center gap-2">
          <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><path d="M12 20V10"/><path d="M18 20V4"/><path d="M6 20v-4"/></svg>
          Mantenimiento del Sistema ({info.codigoReferencia})
        </h4>
      </div>

      <div className="p-8 grid grid-cols-1 md:grid-cols-2 gap-12">
        <div className="space-y-6">
          <div className="flex items-center justify-between p-4 bg-slate-50 rounded-xl border border-slate-100">
            <span className="text-xs font-bold text-slate-500 uppercase">Estado de la Base de Datos</span>
            <span className={`px-3 py-1 rounded-full text-[10px] font-black border ${info.estadoBaseDeDatos ? 'bg-success-green/10 text-success-green border-success-green/20' : 'bg-danger-red/10 text-danger-red border-danger-red/20'}`}>
              {info.estadoBaseDeDatos ? 'OK - CONECTADO' : 'OFFLINE'}
            </span>
          </div>

          <div className="flex items-center justify-between p-4 bg-slate-50 rounded-xl border border-slate-100">
            <span className="text-xs font-bold text-slate-500 uppercase">Último Respaldo</span>
            <span className="text-sm font-bold text-slate-700">{info.fechaUltimoRespaldo}</span>
          </div>

          <div className="space-y-2 p-4 bg-slate-50 rounded-xl border border-slate-100">
            <div className="flex justify-between items-end mb-2">
              <span className="text-xs font-bold text-slate-500 uppercase">Carga del Servidor</span>
              <span className="text-sm font-black text-primary-navy">{info.cargaServidorPorcentaje}%</span>
            </div>
            <div className="w-full h-2 bg-slate-200 rounded-full overflow-hidden">
              <div 
                className={`h-full transition-all duration-1000 ${info.cargaServidorPorcentaje > 80 ? 'bg-danger-red' : info.cargaServidorPorcentaje > 50 ? 'bg-warning-amber' : 'bg-accent-blue'}`}
                style={{ width: `${info.cargaServidorPorcentaje}%` }}
              ></div>
            </div>
          </div>
        </div>

        <div className="grid grid-cols-1 gap-4">
          <button 
            onClick={() => onAccion('BACKUP')}
            className="flex items-center justify-center gap-2 py-4 bg-white border border-slate-200 rounded-xl text-sm font-black text-slate-600 hover:bg-slate-50 hover:border-accent-blue/50 transition-all shadow-sm"
          >
            <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/><polyline points="7 10 12 15 17 10"/><line x1="12" y1="15" x2="12" y2="3"/></svg>
            EJECUTAR RESPALDO MANUAL
          </button>
          <button 
            onClick={() => onAccion('CACHE_CLEAR')}
            className="flex items-center justify-center gap-2 py-4 bg-white border border-slate-200 rounded-xl text-sm font-black text-slate-600 hover:bg-slate-50 hover:border-accent-blue/50 transition-all shadow-sm"
          >
            <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><path d="M20 5H9l-7 7 7 7h11a2 2 0 0 0 2-2V7a2 2 0 0 0-2-2Z"/><line x1="18" y1="9" x2="12" y2="15"/><line x1="12" y1="9" x2="18" y2="15"/></svg>
            LIMPIAR CACHÉ DEL SISTEMA
          </button>
          <button 
            onClick={() => onAccion('IMPORT_LEYES')}
            className="flex items-center justify-center gap-2 py-4 bg-accent-blue text-white rounded-xl text-sm font-black hover:bg-blue-600 transition-all shadow-lg"
          >
            <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/><polyline points="17 8 12 3 7 8"/><line x1="12" y1="3" x2="12" y2="15"/></svg>
            IMPORTAR LEYES DESDE API
          </button>
          <button
              onClick={() => onAccion('IMPORT_POLITICOS')}
              className="flex items-center justify-center gap-2 py-4 bg-emerald-600 text-white rounded-xl text-sm font-black hover:bg-emerald-700 transition-all shadow-lg"
          >
            <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><path d="M16 21v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M22 21v-2a4 4 0 0 0-3-3.87"/><path d="M16 3.13a4 4 0 0 1 0 7.75"/></svg>
            SINCRONIZAR POLÍTICOS
          </button>
        </div>
      </div>
    </div>
  );
};

export default MantenimientoSistema;
