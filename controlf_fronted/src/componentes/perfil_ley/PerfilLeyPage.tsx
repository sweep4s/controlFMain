import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import ContenidoLey from './Componente_contenido_ley/ContenidoLey';
import ResultadoVotacion from './Componente_resultado_votacion/ResultadoVotacion';
import AuditoriaLey from './Componente_auditoria_coherencia/AuditoriaLey';
import ParticipacionCiudadana from '../perfil_politico_screen/ParticipacionCiudadana';

const PerfilLeyPage: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const [perfil, setPerfil] = useState<any>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [categoria, setCategoria] = useState('');
  const [estado, setEstado] = useState('');
  const [votoId, setVotoId] = useState('');
  const [asistencia, setAsistencia] = useState('true');
  const [isSavingLey, setIsSavingLey] = useState(false);
  const [mensajeLey, setMensajeLey] = useState<string | null>(null);
  const [isLinkingVotes, setIsLinkingVotes] = useState(false);
  const [linkResult, setLinkResult] = useState<any>(null);
  const [isTraduciendo, setIsTraduciendo] = useState(false);
  const { apiFetch, isAuthenticated, role } = useAuth();

  const fetchPerfil = async () => {
    setIsLoading(true);
    try {
      const response = await fetch(`/api/leyes/${id}/perfil`);
      const data = await response.json();
      setPerfil(data);
    } catch (error) {
      console.error("Error al cargar perfil de ley:", error);
      navigate('/leyes');
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    fetchPerfil();
  }, [id]);

  const handleAddComentario = async (texto: string, puntaje: number) => {
    try {
      if (!isAuthenticated) return;
      await apiFetch(`/api/leyes/${id}/comentarios`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ texto, puntaje })
      });
      await apiFetch(`/api/leyes/${id}/calificaciones`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ puntaje })
      });
      fetchPerfil();
    } catch (error) {
      console.error("Error al publicar comentario:", error);
    }
  };

  const handleActualizarCategoria = async (event: React.FormEvent) => {
    event.preventDefault();
    if (!id || !categoria.trim()) return;

    setIsSavingLey(true);
    setMensajeLey(null);

    try {
      const response = await apiFetch(`/api/leyes/${id}/categoria`, {
        method: 'PATCH',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ categoria: categoria.trim().toUpperCase() })
      });

      if (!response.ok) throw new Error('No se pudo actualizar la categoría');
      setMensajeLey('Categoría actualizada correctamente.');
      setCategoria('');
    } catch (error) {
      console.error('Error al actualizar categoría:', error);
      setMensajeLey('No se pudo guardar la categoría.');
    } finally {
      setIsSavingLey(false);
    }
  };

  const handleActualizarEstado = async (event: React.FormEvent) => {
    event.preventDefault();
    if (!id || !estado.trim()) return;

    setIsSavingLey(true);
    setMensajeLey(null);

    try {
      const response = await apiFetch(`/api/leyes/${id}/estado`, {
        method: 'PATCH',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ estado: estado.trim().toUpperCase() })
      });

      if (!response.ok) throw new Error('No se pudo actualizar el estado');
      setMensajeLey('Estado actualizado correctamente.');
      setEstado('');
    } catch (error) {
      console.error('Error al actualizar estado:', error);
      setMensajeLey('No se pudo guardar el estado.');
    } finally {
      setIsSavingLey(false);
    }
  };

  const handleActualizarAsistencia = async (event: React.FormEvent) => {
    event.preventDefault();
    if (!id || !votoId.trim()) return;

    setIsSavingLey(true);
    setMensajeLey(null);

    try {
      const response = await apiFetch(`/api/leyes/${id}/votos/${votoId}/asistencia`, {
        method: 'PATCH',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ asistencia: asistencia === 'true' })
      });

      if (!response.ok) throw new Error('No se pudo actualizar la asistencia');
      setMensajeLey('Asistencia actualizada correctamente.');
      setVotoId('');
    } catch (error) {
      console.error('Error al actualizar asistencia:', error);
      setMensajeLey('No se pudo guardar la asistencia.');
    } finally {
      setIsSavingLey(false);
    }
  };

  const handleLinkExternalVotes = async () => {
    if (!id) return;
    setIsLinkingVotes(true);
    setLinkResult(null);

    try {
      const response = await apiFetch(`/api/leyes/${id}/import-voting-detail`, { method: 'POST' });
      if (!response.ok) {
        throw new Error(`HTTP ${response.status}`);
      }
      const data = await response.json();
      setLinkResult(data);
      fetchPerfil();
    } catch (error) {
      console.error('Error al enlazar votos externos:', error);
      setLinkResult({ error: true });
    } finally {
      setIsLinkingVotes(false);
    }
  };

  const handleExplicarLey = async () => {
    if (!id) return;
    setIsTraduciendo(true);
    try {
      const response = await apiFetch(`/api/leyes/${id}/explicar`, {
        method: 'POST'
      });
      if (!response.ok) {
        throw new Error('No se pudo generar la explicación');
      }
      fetchPerfil();
    } catch (error) {
      console.error('Error al generar la explicación de la ley:', error);
      alert('Hubo un error al generar la explicación con IA. Por favor, intente de nuevo.');
    } finally {
      setIsTraduciendo(false);
    }
  };

  if (isLoading) {
    return (
      <div className="max-w-5xl mx-auto animate-pulse space-y-8">
        <div className="bg-white h-48 rounded-xl border border-slate-200"></div>
        <div className="bg-white h-64 rounded-xl border border-slate-200"></div>
        <div className="bg-white h-96 rounded-xl border border-slate-200"></div>
      </div>
    );
  }

  if (!perfil) return null;

  return (
    <div className="max-w-5xl mx-auto">
      <button
        onClick={() => navigate('/leyes')}
        className="flex items-center gap-2 text-slate-500 hover:text-primary-navy font-bold text-sm mb-6 transition-colors group"
      >
        <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round" className="group-hover:-translate-x-1 transition-transform"><line x1="19" y1="12" x2="5" y2="12" /><polyline points="12 19 5 12 12 5" /></svg>
        Volver al Directorio de Leyes
      </button>

      <div className="bg-white rounded-xl shadow-sm border border-slate-200 p-8 mb-8 border-l-8 border-l-accent-blue">
        <div className="text-slate-500 text-[10px] font-black uppercase tracking-widest mb-2">Expediente Legislativo</div>
        <h2 className="text-2xl font-black text-primary-navy mb-4 leading-tight">
          {perfil.contenido.titulo}
        </h2>
        <div className="flex flex-wrap gap-4 text-xs font-bold text-slate-500">
          <span className="bg-slate-100 px-3 py-1 rounded">ID: {perfil.contenido.id}</span>
          <span className="bg-slate-100 px-3 py-1 rounded">Categoría: {perfil.contenido.categoria || 'Sin asignar'}</span>
          <span className="bg-success-green/10 text-success-green px-3 py-1 rounded border border-success-green/20">{perfil.contenido.estado || 'SIN ESTADO'}</span>
        </div>
        {perfil.votingMatchSummary && (
          <div className="mt-4 grid gap-3 sm:grid-cols-3">
            <div className="rounded-xl border border-slate-200 bg-slate-50 p-4 text-sm">
              <p className="text-[10px] font-bold uppercase tracking-[0.2em] text-slate-500">Políticos encontrados</p>
              <p className="mt-2 text-2xl font-black text-primary-navy">{perfil.votingMatchSummary.found}</p>
            </div>
            <div className="rounded-xl border border-slate-200 bg-slate-50 p-4 text-sm">
              <p className="text-[10px] font-bold uppercase tracking-[0.2em] text-slate-500">Políticos no encontrados</p>
              <p className="mt-2 text-2xl font-black text-primary-navy">{perfil.votingMatchSummary.notFound}</p>
            </div>
            <div className="rounded-xl border border-slate-200 bg-slate-50 p-4 text-sm">
              <p className="text-[10px] font-bold uppercase tracking-[0.2em] text-slate-500">Total votantes externos</p>
              <p className="mt-2 text-2xl font-black text-primary-navy">{perfil.votingMatchSummary.total}</p>
            </div>
          </div>
        )}
        {role === 'ADMIN' && (
          <div className="mt-4 flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
            <button
              type="button"
              onClick={handleLinkExternalVotes}
              disabled={isLinkingVotes}
              className="rounded-xl bg-accent-blue px-4 py-3 text-sm font-bold text-white hover:bg-blue-600 disabled:opacity-60"
            >
              {isLinkingVotes ? 'Enlazando votos...' : 'Enlazar votos externos'}
            </button>
            {linkResult && (
              <div className="rounded-xl border border-slate-200 bg-slate-50 p-4 text-sm text-slate-700">
                {linkResult.error ? (
                  <p className="font-bold text-rose-600">Error al enlazar votos externos.</p>
                ) : (
                  <div className="grid gap-2 sm:grid-cols-4">
                    <div><span className="font-black text-primary-navy">{linkResult.found}</span> totales</div>
                    <div><span className="font-black text-primary-navy">{linkResult.imported}</span> importados</div>
                    <div><span className="font-black text-primary-navy">{linkResult.ignored}</span> no Coinc.</div>
                    <div><span className="font-black text-primary-navy">{linkResult.duplicates}</span> repetidos</div>
                  </div>
                )}
              </div>
            )}
          </div>
        )}
      </div>

      <div className="bg-white rounded-xl shadow-sm border border-slate-200 p-6 mb-8">
        <h3 className="text-lg font-black text-primary-navy mb-4">Gestionar ley</h3>
        <p className="text-sm text-slate-500 mb-4">Actualiza categoría, estado y asistencia de votos usando los endpoints del backend.</p>
        {role !== 'ADMIN' && <p className="mb-4 text-sm font-semibold text-amber-700">Solo los administradores pueden modificar esta información.</p>}

        <div className="grid gap-6 md:grid-cols-2">
          <form onSubmit={handleActualizarCategoria} className="space-y-3 rounded-xl border border-slate-100 p-4">
            <label className="text-sm font-bold text-slate-600">Categoría</label>
            <input
              type="text"
              value={categoria}
              onChange={(event) => setCategoria(event.target.value)}
              placeholder="EDUCACION"
              className="w-full rounded-xl border border-slate-200 bg-white px-4 py-3 text-sm text-slate-700 focus:border-accent-blue focus:outline-none"
            />
            <button type="submit" disabled={isSavingLey || role !== 'ADMIN'} className="rounded-xl bg-primary-navy px-4 py-3 text-sm font-bold text-white hover:bg-slate-800 disabled:opacity-60">
              {isSavingLey ? 'Guardando...' : 'Actualizar categoría'}
            </button>
          </form>

          <form onSubmit={handleActualizarEstado} className="space-y-3 rounded-xl border border-slate-100 p-4">
            <label className="text-sm font-bold text-slate-600">Estado</label>
            <input
              type="text"
              value={estado}
              onChange={(event) => setEstado(event.target.value)}
              placeholder="APROBADA"
              className="w-full rounded-xl border border-slate-200 bg-white px-4 py-3 text-sm text-slate-700 focus:border-accent-blue focus:outline-none"
            />
            <button type="submit" disabled={isSavingLey || role !== 'ADMIN'} className="rounded-xl bg-accent-blue px-4 py-3 text-sm font-bold text-white hover:bg-blue-600 disabled:opacity-60">
              {isSavingLey ? 'Guardando...' : 'Actualizar estado'}
            </button>
          </form>
        </div>

        <form onSubmit={handleActualizarAsistencia} className="mt-6 space-y-3 rounded-xl border border-slate-100 p-4">
          <label className="text-sm font-bold text-slate-600">Asistencia de voto</label>
          <div className="grid gap-4 md:grid-cols-[140px_1fr_auto]">
            <input
              type="number"
              value={votoId}
              onChange={(event) => setVotoId(event.target.value)}
              placeholder="ID de voto"
              className="rounded-xl border border-slate-200 bg-white px-4 py-3 text-sm text-slate-700 focus:border-accent-blue focus:outline-none"
            />
            <select
              value={asistencia}
              onChange={(event) => setAsistencia(event.target.value)}
              className="rounded-xl border border-slate-200 bg-white px-4 py-3 text-sm text-slate-700 focus:border-accent-blue focus:outline-none"
            >
              <option value="true">Asistió</option>
              <option value="false">No asistió</option>
            </select>
            <button type="submit" disabled={isSavingLey || role !== 'ADMIN'} className="rounded-xl bg-success-green px-4 py-3 text-sm font-bold text-white hover:bg-green-600 disabled:opacity-60">
              {isSavingLey ? 'Guardando...' : 'Actualizar asistencia'}
            </button>
          </div>
        </form>

        {mensajeLey && (
          <p className="mt-4 text-sm text-success-green">{mensajeLey}</p>
        )}
      </div>

      <ContenidoLey
        id={perfil.contenido.id}
        titulo={perfil.contenido.titulo}
        resumenEjecutivo={perfil.contenido.resumenEjecutivo}
        impactoSocial={perfil.contenido.impactoSocial}
        onTraducir={handleExplicarLey}
        isTraduciendo={isTraduciendo}
      />

      <ResultadoVotacion
        favor={perfil.votacion.votosFavor}
        contra={perfil.votacion.votosContra}
        abstencion={perfil.votacion.votosAbstencion}
        total={perfil.votacion.votosFavor + perfil.votacion.votosContra + perfil.votacion.votosAbstencion}
      />

      <AuditoriaLey
        filas={perfil.auditoria.filas}
      />

      <ParticipacionCiudadana
        comentarios={perfil.debate.comentarios}
        onAddComentario={handleAddComentario}
        tipoEntidad="ley"
      />
    </div>
  );
};

export default PerfilLeyPage;
