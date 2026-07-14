import React, { useState, useEffect } from 'react';
import BarraBusquedaLeyes from './BarraBusquedaLeyes';
import ListaLeyes from './ListaLeyes';
import Paginacion from '../directorio_politicos/Componente pie pagina/Paginacion';

const DirectorioLeyesPage: React.FC = () => {
  const [leyes, setLeyes] = useState([]);
  const [paginaActual, setPaginaActual] = useState(1);
  const [totalPaginas, setTotalPaginas] = useState(1);
  const [isLoading, setIsLoading] = useState(true);
  const [termino, setTermino] = useState('');
  const [filtros, setFiltros] = useState<Array<{ id: string; label: string; valorSeleccionado: string | null; opciones: string[] }>>([
    { id: 'categoria', label: 'Categoría', valorSeleccionado: null, opciones: [] },
    { id: 'estado', label: 'Estado', valorSeleccionado: null, opciones: [] },
  ]);

  const fetchFiltros = async () => {
    try {
      const response = await fetch('/api/leyes/filtros');
      const data = await response.json();
      setFiltros(prev => prev.map(f => {
        if (f.id === 'categoria') return { ...f, opciones: data.categorias };
        if (f.id === 'estado') return { ...f, opciones: data.estados };
        return f;
      }));
    } catch (error) {
      console.error("Error al cargar filtros de leyes:", error);
    }
  };

  const fetchLeyes = async (page: number) => {
    setIsLoading(true);
    try {
      const params = new URLSearchParams({
        pagina: page.toString(),
        size: '10',
        termino: termino,
        categoria: filtros.find(f => f.id === 'categoria')?.valorSeleccionado || '',
        estado: filtros.find(f => f.id === 'estado')?.valorSeleccionado || ''
      });

      const response = await fetch(`/api/leyes?${params.toString()}`);
      const data = await response.json();
      
      setLeyes(data.leyes);
      setPaginaActual(data.paginaActual);
      setTotalPaginas(data.totalPaginas);
    } catch (error) {
      console.error("Error al cargar leyes:", error);
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    fetchFiltros();
  }, []);

  useEffect(() => {
    fetchLeyes(paginaActual);
  }, [paginaActual]);

  const handleSearchSubmit = () => {
    setPaginaActual(1);
    fetchLeyes(1);
  };

  const handleExportarLeyes = async () => {
    try {
      // Exportación detallada por ley (CF-022).
      const response = await fetch('/api/dashboard/export/leyes');
      if (!response.ok) throw new Error(`HTTP ${response.status}`);
      const csv = await response.text();
      const blob = new Blob([csv], { type: 'text/csv;charset=utf-8;' });
      const url = window.URL.createObjectURL(blob);
      const link = document.createElement('a');
      link.href = url;
      link.download = `reporte-leyes-detallado-${new Date().toISOString().slice(0, 10)}.csv`;
      document.body.appendChild(link);
      link.click();
      document.body.removeChild(link);
      window.URL.revokeObjectURL(url);
    } catch (error) {
      console.error('Error al exportar leyes:', error);
    }
  };

  return (
    <div className="max-w-6xl mx-auto">
      <BarraBusquedaLeyes
        termino={termino}
        filtros={filtros}
        onSearchChange={setTermino}
        onFilterChange={(id, val) => setFiltros(prev => prev.map(f => f.id === id ? { ...f, valorSeleccionado: val } : f))}
        onSearchSubmit={handleSearchSubmit}
      />

      <div className="flex justify-end mb-4">
        <button
          onClick={handleExportarLeyes}
          className="inline-flex items-center gap-2 rounded-lg border border-slate-200 bg-white px-4 py-2 text-xs font-black uppercase tracking-wide text-slate-600 shadow-sm hover:bg-slate-50"
        >
          <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round"><path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4" /><polyline points="7 10 12 15 17 10" /><line x1="12" y1="15" x2="12" y2="3" /></svg>
          Exportar leyes (detalle)
        </button>
      </div>

      <ListaLeyes
        leyes={leyes}
        isLoading={isLoading}
      />

      <Paginacion
        paginaActual={paginaActual}
        totalPaginas={totalPaginas}
        onPageChange={setPaginaActual}
      />
    </div>
  );
};

export default DirectorioLeyesPage;
