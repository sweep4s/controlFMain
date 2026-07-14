import React, { useState } from 'react';
import { Link } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import { type ComentarioDebate } from './type_perfil_politico';

interface ParticipacionCiudadanaProps {
  comentarios: ComentarioDebate[];
  onAddComentario: (texto: string, puntaje: number) => void;
  tipoEntidad?: 'politico' | 'ley';
}

const ParticipacionCiudadana: React.FC<ParticipacionCiudadanaProps> = ({ comentarios, onAddComentario, tipoEntidad = 'politico' }) => {
  const [nuevoComentario, setNuevoComentario] = useState('');
  const [puntaje, setPuntaje] = useState(0);
  const { isAuthenticated } = useAuth();

  // Textos adaptados al contexto (político vs ley) para no reutilizar copy incorrecto (CF-026).
  const esLey = tipoEntidad === 'ley';
  const tituloCalificar = esLey ? 'Califica esta ley' : 'Califica a este político';
  const placeholderOpinion = esLey
    ? 'Escribe tu opinión sobre esta ley...'
    : 'Escribe tu análisis sobre su coherencia...';

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (nuevoComentario.trim() && puntaje > 0) {
      onAddComentario(nuevoComentario, puntaje);
      setNuevoComentario('');
      setPuntaje(0);
    }
  };

  return (
    <div className="bg-white rounded-xl shadow-sm border border-slate-200 p-8 mb-8">
      <h4 className="text-sm font-bold text-primary-navy uppercase mb-8 tracking-wide flex items-center gap-2">
        <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/></svg>
        Participación Ciudadana (RF-06)
      </h4>

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-12">
        {/* Formulario de participación */}
        <div className="space-y-6">
          <div className="bg-slate-50 rounded-xl p-6 border border-slate-100">
            <h5 className="text-sm font-bold text-slate-700 mb-4">{tituloCalificar}</h5>
            {!isAuthenticated ? (
              <div className="rounded-xl border border-slate-200 bg-white p-4 text-sm text-slate-600">
                Inicia sesión para publicar comentarios y calificaciones. <Link to="/login" className="font-semibold text-accent-blue">Entrar ahora</Link>
              </div>
            ) : (
              <>
            <div className="flex gap-4 mb-6">
              {[1, 2, 3, 4, 5].map((star) => (
                <button
                  key={star}
                  onClick={() => setPuntaje(star)}
                  className={`w-10 h-10 rounded-lg flex items-center justify-center transition-all ${
                    puntaje >= star 
                      ? 'bg-accent-blue text-white shadow-md scale-110' 
                      : 'bg-white text-slate-300 border border-slate-200 hover:border-accent-blue/50'
                  }`}
                >
                  <span className="font-bold">{star}</span>
                </button>
              ))}
            </div>

            <form onSubmit={handleSubmit} className="space-y-4">
              <div className="space-y-2">
                <label className="text-xs font-bold text-slate-400 uppercase tracking-wider">Tu opinión ciudadana</label>
                <textarea
                  value={nuevoComentario}
                  onChange={(e) => setNuevoComentario(e.target.value)}
                  placeholder={placeholderOpinion}
                  className="w-full h-32 px-4 py-3 bg-white border border-slate-200 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-accent-blue/20 focus:border-accent-blue transition-all"
                />
              </div>
              <button
                type="submit"
                disabled={!nuevoComentario.trim() || puntaje === 0}
                className="w-full py-3 bg-accent-blue text-white rounded-lg font-bold text-sm shadow-sm hover:bg-blue-600 transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
              >
                Publicar Comentario
              </button>
            </form>
            </>
            )}
          </div>
        </div>

        {/* Lista de comentarios */}
        <div className="space-y-6">
          <h5 className="text-sm font-bold text-slate-700 flex items-center justify-between">
            Comentarios Recientes
            <span className="bg-slate-100 text-slate-500 px-2 py-0.5 rounded text-[10px] font-bold">{comentarios.length}</span>
          </h5>

          <div className="space-y-4 max-h-[400px] overflow-y-auto pr-2 scrollbar-thin scrollbar-thumb-slate-200">
            {comentarios.length === 0 ? (
              <div className="text-center py-12 bg-slate-50 rounded-xl border border-dashed border-slate-200">
                <p className="text-slate-400 text-sm italic">Aún no hay comentarios. ¡Sé el primero en opinar!</p>
              </div>
            ) : (
              comentarios.map((c) => (
                <div key={c.id} className="bg-white border border-slate-100 rounded-xl p-4 shadow-sm">
                  <div className="flex items-center gap-3 mb-3">
                    <div className="w-8 h-8 rounded-full bg-slate-200 flex-shrink-0">
                      {c.avatarUrl && <img src={c.avatarUrl} alt={c.usuario} className="w-full h-full rounded-full object-cover" />}
                    </div>
                    <div className="flex-grow">
                      <div className="text-xs font-bold text-slate-700">{c.usuario}</div>
                      <div className="text-[10px] text-slate-400">{c.fecha}</div>
                    </div>
                    {typeof c.puntaje === 'number' && c.puntaje > 0 && (
                      <div className="flex items-center gap-1 rounded-full bg-warning-amber/10 px-2 py-1" title={`Calificación: ${c.puntaje} de 5`}>
                        <svg xmlns="http://www.w3.org/2000/svg" width="12" height="12" viewBox="0 0 24 24" fill="currentColor" className="text-warning-amber"><polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2" /></svg>
                        <span className="text-[10px] font-black text-warning-amber">{c.puntaje}/5</span>
                      </div>
                    )}
                  </div>
                  <p className="text-xs text-slate-600 leading-relaxed bg-slate-50 p-3 rounded-lg border border-slate-50">
                    "{c.mensaje}"
                  </p>
                  <div className="flex items-center gap-4 mt-3 pt-3 border-t border-slate-50">
                    <button className="text-[10px] font-bold text-slate-400 hover:text-accent-blue flex items-center gap-1 transition-colors">
                      <svg xmlns="http://www.w3.org/2000/svg" width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="3" strokeLinecap="round" strokeLinejoin="round"><path d="M14 9V5a3 3 0 0 0-3-3l-4 9v11h11.28a2 2 0 0 0 2-1.7l1.38-9a2 2 0 0 0-2-2.3zM7 22H4a2 2 0 0 1-2-2v-7a2 2 0 0 1 2-2h3"/></svg>
                      ÚTIL
                    </button>
                    <button className="text-[10px] font-bold text-slate-400 hover:text-primary-navy flex items-center gap-1 transition-colors">
                      <svg xmlns="http://www.w3.org/2000/svg" width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="3" strokeLinecap="round" strokeLinejoin="round"><path d="M21 11.5a8.38 8.38 0 0 1-.9 3.8 8.5 8.5 0 0 1-7.6 4.7 8.38 8.38 0 0 1-3.8-.9L3 21l1.9-5.7a8.38 8.38 0 0 1-.9-3.8 8.5 8.5 0 0 1 4.7-7.6 8.38 8.38 0 0 1 3.8-.9h.5a8.48 8.48 0 0 1 8 8v.5z"/></svg>
                      RESPONDER
                    </button>
                  </div>
                </div>
              ))
            )}
          </div>
        </div>
      </div>
    </div>
  );
};

export default ParticipacionCiudadana;
