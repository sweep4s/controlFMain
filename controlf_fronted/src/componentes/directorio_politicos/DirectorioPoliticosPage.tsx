import React, { useState, useEffect } from 'react';
import BarraBusqueda from './Componente busqueda politico/BarraBusqueda';
import GrillaPoliticos from './Componente resumen politico/GrillaPoliticos';
import Paginacion from './Componente pie pagina/Paginacion';

const DirectorioPoliticosPage: React.FC = () => {
  const [politicos, setPoliticos] = useState([]);
  const [paginaActual, setPaginaActual] = useState(1);
  const [totalPaginas, setTotalPaginas] = useState(1);
  const [isLoading, setIsLoading] = useState(true);
  const [busqueda, setBusqueda] = useState('');
  const [filtros, setFiltros] = useState<Array<{ id: string; label: string; valorSeleccionado: string | null; opciones: string[] }>>([
    { id: 'partido', label: 'Partido Político', valorSeleccionado: null, opciones: [] },
    { id: 'provincia', label: 'Provincia / Región', valorSeleccionado: null, opciones: [] },
    { id: 'comision', label: 'Comisión Legislativa', valorSeleccionado: null, opciones: [] },
  ]);

  const fetchFiltros = async () => {
    try {
      const response = await fetch('/api/politicos/filtros');
      const data = await response.json();
      
      setFiltros(prev => prev.map(f => {
        if (f.id === 'partido') return { ...f, opciones: data.partidos };
        if (f.id === 'provincia') return { ...f, opciones: data.regiones };
        if (f.id === 'comision') return { ...f, opciones: data.comisiones };
        return f;
      }));
    } catch (error) {
      console.error("Error al cargar filtros:", error);
    }
  };

  const fetchPoliticos = async (page: number) => {
    setIsLoading(true);
    try {
      // Construir query params reales basados en el estado
      const params = new URLSearchParams({
        pagina: page.toString(),
        size: '6',
        nombre: busqueda,
        partido: filtros.find(f => f.id === 'partido')?.valorSeleccionado || '',
        region: filtros.find(f => f.id === 'provincia')?.valorSeleccionado || '',
        comision: filtros.find(f => f.id === 'comision')?.valorSeleccionado || ''
      });

      const response = await fetch(`/api/politicos?${params.toString()}`);
      const data = await response.json();
      
      setPoliticos(data.cartas);
      setPaginaActual(data.paginaActual);
      setTotalPaginas(data.totalPaginas);
    } catch (error) {
      console.error("Error al cargar políticos:", error);
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    fetchFiltros();
  }, []);

  useEffect(() => {
    fetchPoliticos(paginaActual);
  }, [paginaActual]);

  const handleSearchChange = (value: string) => {
    setBusqueda(value);
  };

  const handleFilterChange = (id: string, value: string) => {
    setFiltros(prev => prev.map(f => f.id === id ? { ...f, valorSeleccionado: value } : f));
  };

  const handleSearchSubmit = () => {
    // Aquí se implementaría la búsqueda real enviando los filtros a la API
    console.log("Buscando:", busqueda, filtros);
    setPaginaActual(1);
    fetchPoliticos(1);
  };

  const handleExportarReporte = async () => {
    try {
      // Exportación detallada por actor político (CF-022). El endpoint global /api/dashboard/export
      // se conserva intacto para otros usos.
      const response = await fetch('/api/dashboard/export/politicos');
      if (!response.ok) {
        throw new Error(`HTTP ${response.status}`);
      }
      const csv = await response.text();
      const blob = new Blob([csv], { type: 'text/csv;charset=utf-8;' });
      const url = window.URL.createObjectURL(blob);
      const link = document.createElement('a');
      link.href = url;
      link.download = `reporte-politicos-detallado-${new Date().toISOString().slice(0, 10)}.csv`;
      document.body.appendChild(link);
      link.click();
      document.body.removeChild(link);
      window.URL.revokeObjectURL(url);
    } catch (error) {
      console.error('Error al exportar reporte:', error);
    }
  };

  return (
    <div className="max-w-6xl mx-auto">
      <BarraBusqueda
        titulo="Directorio de Políticos"
        subtitulo="Auditoría de coherencia y gestión legislativa"
        filtros={filtros}
        textoBusqueda={busqueda}
        placeholderBusqueda="Buscar por nombre del político..."
        textoBotonExportar="Exportar Reporte"
        onSearchChange={handleSearchChange}
        onFilterChange={handleFilterChange}
        onSearchSubmit={handleSearchSubmit}
        onExport={handleExportarReporte}
      />

      <GrillaPoliticos
        cartas={politicos}
        isLoading={isLoading}
      />

      <Paginacion
        paginaActual={paginaActual}
        totalPaginas={totalPaginas}
        onPageChange={(page) => setPaginaActual(page)}
      />
    </div>
  );
};

export default DirectorioPoliticosPage;
