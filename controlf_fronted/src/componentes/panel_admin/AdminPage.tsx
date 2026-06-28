import React, { useState, useEffect } from 'react';
import PanelControlSeguridad from './COMPONENTE_PANEL_DE_CONTROL/PanelControlSeguridad';
import MotorCoherencia from './COMPONENTE_MOTOR_COHERENCIA/MotorCoherencia';
import MantenimientoSistema from './COMPONENTE_MANTENIMIENTO_DEL_SISTEMA/MantenimientoSistema';

const AdminPage: React.FC = () => {
  const [seguridad, setSeguridad] = useState<any>(null);
  const [mantenimiento, setMantenimiento] = useState<any>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [assemblyMembers, setAssemblyMembers] = useState<any[]>([]);
  const [selectedMemberId, setSelectedMemberId] = useState('');
  const [memberSearch, setMemberSearch] = useState('');
  const [showMemberDropdown, setShowMemberDropdown] = useState(false);
  const [votings, setVotings] = useState<any[]>([]);
  const [selectedVotingIds, setSelectedVotingIds] = useState<number[]>([]);
  const [isLoadingMembers, setIsLoadingMembers] = useState(true);
  const [isLoadingVotings, setIsLoadingVotings] = useState(false);
  const [isImporting, setIsImporting] = useState(false);
  const [importResult, setImportResult] = useState<any>(null);
  const [currentPage, setCurrentPage] = useState(1);
  const ITEMS_PER_PAGE = 20;

  const fetchData = async () => {
    setIsLoading(true);
    try {
      const [segRes, mantRes] = await Promise.all([
        fetch('/api/admin/panel'),
        fetch('/api/admin/mantenimiento')
      ]);

      setSeguridad(await segRes.json());
      setMantenimiento(await mantRes.json());
    } catch (error) {
      console.error("Error al cargar datos administrativos:", error);
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    fetchData();
    const loadAssemblyMembers = async () => {
      try {
        setIsLoadingMembers(true);
        const response = await fetch('/admin/assembly-members');
        const data = await response.json();
        setAssemblyMembers(data || []);
      } catch (error) {
        console.error('Error al cargar asambleístas:', error);
      } finally {
        setIsLoadingMembers(false);
      }
    };

    loadAssemblyMembers();
  }, []);

  const handleAccionMantenimiento = async (accion: string) => {
    let endpoint = '';

    switch (accion) {
      case 'BACKUP': endpoint = '/mantenimiento/respaldo'; break;
      case 'CACHE_CLEAR': endpoint = '/mantenimiento/limpiar-cache'; break;
      case 'IMPORT_LEYES': endpoint = '/importar-leyes'; break;
      case 'IMPORT_POLITICOS': endpoint = '/import-politicos'; break;
    }

    try {
      await fetch(`/api/admin${endpoint}`, { method: 'POST' });
      alert(`Acción ${accion} ejecutada correctamente`);
      fetchData(); // Refrescar info
    } catch (error) {
      console.error(`Error al ejecutar ${accion}:`, error);
    }
  };

  const handleSeedDatabase = async () => {
    if (!confirm("¿Deseas poblar la base de datos con datos de ejemplo? Esto solo funcionará si la base de datos está vacía.")) return;

    try {
      const response = await fetch('/api/admin/seed', { method: 'POST' });
      if (response.ok) {
        alert("Base de datos poblada correctamente.");
        fetchData();
      } else {
        alert("Error al poblar la base de datos.");
      }
    } catch (error) {
      console.error("Error en seed:", error);
      alert("Error de conexión con el servidor.");
    }
  };

  const filteredMembers = assemblyMembers.filter((m) =>
    `${m.firstName} ${m.lastname} ${m.territorial}`
      .toLowerCase()
      .includes(memberSearch.toLowerCase())
  );

  const handleMemberChange = async (memberId: string) => {
    setSelectedMemberId(memberId);
    setSelectedVotingIds([]);
    setImportResult(null);
    setCurrentPage(1);

    if (!memberId) {
      setVotings([]);
      return;
    }

    try {
      setIsLoadingVotings(true);
      const response = await fetch(`/admin/assembly-members/${memberId}/votings`);
      const data = await response.json();
      setVotings(data || []);
    } catch (error) {
      console.error('Error al cargar votaciones:', error);
      setVotings([]);
    } finally {
      setIsLoadingVotings(false);
    }
  };

  const handleMemberSearchChange = (value: string) => {
    setMemberSearch(value);

    if (!value.trim()) {
      setShowMemberDropdown(false);
      handleMemberChange('');
      return;
    }

    setShowMemberDropdown(true);
  };

  const handleMemberSelect = (member: { id: number | string; firstName: string; lastname: string; territorial?: string }) => {
    const label = `${member.firstName} ${member.lastname} - ${member.territorial || ''}`.trim();
    setMemberSearch(label);
    setShowMemberDropdown(false);
    handleMemberChange(member.id.toString());
  };

  const toggleVotingSelection = (votingId: number) => {
    setSelectedVotingIds((current) =>
      current.includes(votingId)
        ? current.filter((id) => id !== votingId)
        : [...current, votingId]
    );
  };

  const selectAllVotings = () => {
    setSelectedVotingIds(votings.map((voting) => voting.id));
  };

  const clearAllVotings = () => {
    setSelectedVotingIds([]);
  };

  const handleImportSelected = async () => {
    if (!selectedMemberId || selectedVotingIds.length === 0) return;

    try {
      setIsImporting(true);
      const response = await fetch(`/admin/import-votings/${selectedMemberId}/selected`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify(selectedVotingIds)
      });
      const data = await response.json();
      setImportResult(data);
    } catch (error) {
      console.error('Error al importar votaciones:', error);
    } finally {
      setIsImporting(false);
    }
  };

  const getVoteBadgeClass = (description: string) => {
    const normalized = (description || '').trim().toUpperCase();
    if (normalized === 'SI') return 'bg-emerald-100 text-emerald-700 border-emerald-200';
    if (normalized === 'NO') return 'bg-rose-100 text-rose-700 border-rose-200';
    if (normalized === 'ABSTENCION') return 'bg-slate-200 text-slate-700 border-slate-300';
    return 'bg-slate-100 text-slate-600 border-slate-200';
  };

  const totalPages = Math.ceil(votings.length / ITEMS_PER_PAGE);
  const paginatedVotings = votings.slice(
    (currentPage - 1) * ITEMS_PER_PAGE,
    currentPage * ITEMS_PER_PAGE
  );

  const startIndex = votings.length === 0 ? 0 : (currentPage - 1) * ITEMS_PER_PAGE + 1;
  const endIndex = Math.min(currentPage * ITEMS_PER_PAGE, votings.length);

  if (isLoading) {
    return (
      <div className="max-w-6xl mx-auto animate-pulse space-y-8">
        <div className="bg-white h-48 rounded-xl border border-slate-200"></div>
        <div className="bg-white h-96 rounded-xl border border-slate-200"></div>
        <div className="bg-white h-64 rounded-xl border border-slate-200"></div>
      </div>
    );
  }

  return (
    <div className="max-w-6xl mx-auto pb-12">
      <div className="mb-8 flex justify-between items-start">
        <div>
          <h2 className="text-2xl font-black text-primary-navy uppercase tracking-tighter">Panel de Control y Administración</h2>
          <p className="text-slate-500">Gestión de auditoría ciudadana, seguridad y mantenimiento de infraestructura</p>
        </div>
        <button
          onClick={handleSeedDatabase}
          className="px-4 py-2 bg-accent-blue text-white rounded-lg text-xs font-bold shadow-sm hover:bg-blue-600 transition-colors flex items-center gap-2"
        >
          <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round"><path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4" /><polyline points="7 10 12 15 17 10" /><line x1="12" y1="15" x2="12" y2="3" /></svg>
          POBLAR BASE DE DATOS (SEED)
        </button>
      </div>

      <PanelControlSeguridad
        titulo={seguridad.tituloSeccion}
        opciones={seguridad.opciones}
      />

      <MotorCoherencia />

      <MantenimientoSistema
        info={mantenimiento}
        onAccion={handleAccionMantenimiento}
      />

      <div className="bg-white rounded-xl shadow-sm border border-slate-200 overflow-hidden mb-8">
        <div className="px-8 py-6 border-b border-slate-100 bg-slate-50/50">
          <h4 className="text-sm font-bold text-primary-navy uppercase tracking-wide flex items-center gap-2">
            <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><path d="M12 20V10" /><path d="M18 20V4" /><path d="M6 20v-4" /></svg>
            IMPORTACIÓN DE VOTACIONES
          </h4>
        </div>

        <div className="p-8 space-y-8">
          <div className="space-y-4">
            <label className="text-xs font-bold text-slate-500 uppercase">1. SELECCIONAR ASAMBLEÍSTA</label>
            {isLoadingMembers ? (
              <p className="text-sm text-slate-600">Cargando asambleístas...</p>
            ) : (
              <div className="relative">
                <input
                  type="text"
                  value={memberSearch}
                  onChange={(e) => handleMemberSearchChange(e.target.value)}
                  onFocus={() => setShowMemberDropdown(true)}
                  placeholder="Buscar asambleísta..."
                  className="w-full rounded-xl border border-slate-200 bg-white px-4 py-3 text-sm text-slate-700 shadow-sm focus:border-accent-blue focus:outline-none"
                />

                {showMemberDropdown && (
                  <div className="absolute z-10 w-full mt-1 bg-white border border-slate-200 rounded-xl shadow-lg max-h-64 overflow-y-auto">
                    {filteredMembers.length > 0 ? (
                      filteredMembers.map((member) => {
                        const label = `${member.firstName} ${member.lastname} - ${member.territorial || ''}`.trim();
                        const isSelected = selectedMemberId === member.id.toString();

                        return (
                          <div
                            key={member.id}
                            onClick={() => handleMemberSelect(member)}
                            className={`px-4 py-3 text-sm text-slate-700 cursor-pointer hover:bg-slate-50 border-b border-slate-100 last:border-0 ${isSelected ? 'bg-accent-blue/10 text-accent-blue font-bold' : ''}`}
                          >
                            {label}
                          </div>
                        );
                      })
                    ) : (
                      <div className="px-4 py-3 text-sm text-slate-400 italic">Sin resultados</div>
                    )}
                  </div>
                )}
              </div>
            )}
          </div>

          <div className="space-y-4">
            <div className="flex items-center justify-between gap-4">
              <label className="text-xs font-bold text-slate-500 uppercase">2. SELECCIONAR VOTACIONES A IMPORTAR</label>
              <div className="flex gap-2">
                <button
                  type="button"
                  onClick={selectAllVotings}
                  className="bg-white border border-slate-200 rounded-xl text-sm font-black text-slate-600 hover:bg-slate-50 px-4 py-2"
                >
                  SELECCIONAR TODAS
                </button>
                <button
                  type="button"
                  onClick={clearAllVotings}
                  className="bg-white border border-slate-200 rounded-xl text-sm font-black text-slate-600 hover:bg-slate-50 px-4 py-2"
                >
                  DESELECCIONAR TODAS
                </button>
              </div>
            </div>

            {isLoadingVotings ? (
              <p className="text-sm text-slate-600">Cargando votaciones...</p>
            ) : votings.length === 0 ? (
              <div className="p-4 bg-slate-50 rounded-xl border border-slate-100 text-sm text-slate-600">
                Selecciona un asambleísta para ver sus votaciones
              </div>
            ) : (
              <>
                <div className="space-y-3">
                  {paginatedVotings.map((voting) => {
                    const isSelected = selectedVotingIds.includes(voting.id);
                    const label = (voting.proposalDescription || 'Sin descripción').length > 80
                      ? `${(voting.proposalDescription || 'Sin descripción').slice(0, 80)}...`
                      : (voting.proposalDescription || 'Sin descripción');

                    return (
                      <label
                        key={voting.id}
                        className="flex items-start gap-3 p-4 bg-slate-50 rounded-xl border border-slate-100 cursor-pointer"
                      >
                        <input
                          type="checkbox"
                          checked={isSelected}
                          onChange={() => toggleVotingSelection(voting.id)}
                          className="mt-1 h-4 w-4 rounded border-slate-300 text-accent-blue focus:ring-accent-blue"
                        />
                        <div className="flex-1 min-w-0">
                          <div className="flex items-center justify-between gap-3">
                            <p className="text-sm font-semibold text-slate-700">{label}</p>
                            <span className={`px-2.5 py-1 rounded-full text-[10px] font-black border ${getVoteBadgeClass(voting.description)}`}>
                              {voting.description || 'SIN DATO'}
                            </span>
                          </div>
                        </div>
                      </label>
                    );
                  })}
                </div>

                {votings.length > 0 && (
                  <div className="flex items-center justify-between gap-4 mt-4">
                    <span className="text-xs font-bold text-slate-500 uppercase">
                      Mostrando {startIndex} - {endIndex} de {votings.length} votaciones
                    </span>
                    <div className="flex items-center gap-2">
                      <button
                        type="button"
                        onClick={() => setCurrentPage((page) => Math.max(1, page - 1))}
                        disabled={currentPage === 1}
                        className="bg-white border border-slate-200 rounded-xl text-sm font-black text-slate-600 hover:bg-slate-50 px-4 py-2 disabled:opacity-40 disabled:cursor-not-allowed"
                      >
                        ANTERIOR
                      </button>
                      <span className="text-xs font-bold text-slate-500 uppercase">
                        Página {currentPage} de {totalPages}
                      </span>
                      <button
                        type="button"
                        onClick={() => setCurrentPage((page) => Math.min(totalPages, page + 1))}
                        disabled={currentPage === totalPages || totalPages === 0}
                        className="bg-white border border-slate-200 rounded-xl text-sm font-black text-slate-600 hover:bg-slate-50 px-4 py-2 disabled:opacity-40 disabled:cursor-not-allowed"
                      >
                        SIGUIENTE
                      </button>
                    </div>
                  </div>
                )}
              </>
            )}
          </div>

          <div className="flex items-center justify-between gap-4">
            <button
              type="button"
              onClick={handleImportSelected}
              disabled={isImporting || selectedVotingIds.length === 0 || !selectedMemberId}
              className="flex items-center justify-center gap-2 px-6 py-3 bg-accent-blue text-white rounded-xl text-sm font-black hover:bg-blue-600 transition-all shadow-lg disabled:cursor-not-allowed disabled:opacity-60"
            >
              {isImporting ? 'IMPORTANDO...' : 'IMPORTAR SELECCIONADAS'}
            </button>
          </div>

          {importResult && (
            <div className="p-4 bg-slate-50 rounded-xl border border-slate-100">
              <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
                <div className="p-4 bg-white rounded-xl border border-slate-200">
                  <p className="text-[10px] font-bold text-slate-500 uppercase">ENCONTRADAS</p>
                  <p className="mt-2 text-xl font-black text-primary-navy">{importResult.found}</p>
                </div>
                <div className="p-4 bg-white rounded-xl border border-slate-200">
                  <p className="text-[10px] font-bold text-slate-500 uppercase">IMPORTADAS</p>
                  <p className="mt-2 text-xl font-black text-primary-navy">{importResult.imported}</p>
                </div>
                <div className="p-4 bg-white rounded-xl border border-slate-200">
                  <p className="text-[10px] font-bold text-slate-500 uppercase">IGNORADAS</p>
                  <p className="mt-2 text-xl font-black text-primary-navy">{importResult.ignored}</p>
                </div>
                <div className="p-4 bg-white rounded-xl border border-slate-200">
                  <p className="text-[10px] font-bold text-slate-500 uppercase">DUPLICADAS</p>
                  <p className="mt-2 text-xl font-black text-primary-navy">{importResult.duplicates}</p>
                </div>
              </div>
            </div>
          )}
        </div>
      </div>
    </div>
  );
};

export default AdminPage;
