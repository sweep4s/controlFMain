import React, { useEffect, useState } from 'react';
import { useAuth } from '../../context/AuthContext';

interface PoliticoForm {
  nombreCompleto: string;
  partidoPolitico: string;
  cargoActual: string;
  region: string;
  comision: string;
  patrimonioDeclarado: string;
  antecedentes: string;
  fotoUrl: string;
  estaActivo: boolean;
}

interface LeyForm {
  titulo: string;
  codigo: string;
  categoria: string;
  estado: string;
  proponente: string;
  tipoExpediente: string;
  descripcionOriginal: string;
  descripcionSimplificada: string;
  impactoSocial: string;
  fechaIngreso: string;
}

const politicoInicial: PoliticoForm = {
  nombreCompleto: '', partidoPolitico: '', cargoActual: '', region: '', comision: '',
  patrimonioDeclarado: '', antecedentes: '', fotoUrl: '', estaActivo: true,
};

const leyInicial: LeyForm = {
  titulo: '', codigo: '', categoria: '', estado: 'DEBATE', proponente: '', tipoExpediente: '',
  descripcionOriginal: '', descripcionSimplificada: '', impactoSocial: '', fechaIngreso: '',
};

const inputClass = 'w-full rounded-xl border border-slate-200 bg-white px-3 py-2.5 text-sm text-slate-700 focus:border-accent-blue focus:outline-none';

const RegistroManual: React.FC = () => {
  const { apiFetch } = useAuth();
  const [politico, setPolitico] = useState<PoliticoForm>(politicoInicial);
  const [ley, setLey] = useState<LeyForm>(leyInicial);
  const [estados, setEstados] = useState<string[]>(['DEBATE', 'EN_DEBATE', 'APROBADA', 'VETADA']);
  const [msgPolitico, setMsgPolitico] = useState<{ tipo: 'ok' | 'error'; texto: string } | null>(null);
  const [msgLey, setMsgLey] = useState<{ tipo: 'ok' | 'error'; texto: string } | null>(null);
  const [savingPolitico, setSavingPolitico] = useState(false);
  const [savingLey, setSavingLey] = useState(false);

  useEffect(() => {
    fetch('/api/leyes/filtros')
      .then((res) => res.json())
      .then((data) => { if (Array.isArray(data?.estados) && data.estados.length) setEstados(data.estados); })
      .catch(() => { /* se usan los estados por defecto */ });
  }, []);

  const crearPolitico = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!politico.nombreCompleto.trim()) {
      setMsgPolitico({ tipo: 'error', texto: 'El nombre completo es obligatorio.' });
      return;
    }
    setSavingPolitico(true);
    setMsgPolitico(null);
    try {
      const body = {
        nombreCompleto: politico.nombreCompleto.trim(),
        partidoPolitico: politico.partidoPolitico.trim() || null,
        cargoActual: politico.cargoActual.trim() || null,
        region: politico.region.trim() || null,
        comision: politico.comision.trim() || null,
        estaActivo: politico.estaActivo,
        patrimonioDeclarado: politico.patrimonioDeclarado ? Number(politico.patrimonioDeclarado) : null,
        antecedentes: politico.antecedentes.trim() || null,
        fotoUrl: politico.fotoUrl.trim() || null,
      };
      const res = await apiFetch('/api/admin/politicos', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(body),
      });
      if (!res.ok) throw new Error(`HTTP ${res.status}`);
      setPolitico(politicoInicial);
      setMsgPolitico({ tipo: 'ok', texto: 'Político registrado correctamente.' });
    } catch (err) {
      console.error('Error al crear político:', err);
      setMsgPolitico({ tipo: 'error', texto: 'No se pudo registrar el político.' });
    } finally {
      setSavingPolitico(false);
    }
  };

  const crearLey = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!ley.titulo.trim() || !ley.codigo.trim()) {
      setMsgLey({ tipo: 'error', texto: 'El título y el código son obligatorios.' });
      return;
    }
    setSavingLey(true);
    setMsgLey(null);
    try {
      const body = {
        titulo: ley.titulo.trim(),
        codigo: ley.codigo.trim(),
        categoria: ley.categoria.trim() || null,
        estado: ley.estado,
        proponente: ley.proponente.trim() || null,
        tipoExpediente: ley.tipoExpediente.trim() || null,
        descripcionOriginal: ley.descripcionOriginal.trim() || null,
        descripcionSimplificada: ley.descripcionSimplificada.trim() || null,
        impactoSocial: ley.impactoSocial.trim() || null,
        fechaIngreso: ley.fechaIngreso || null,
      };
      const res = await apiFetch('/api/admin/leyes', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(body),
      });
      if (res.status === 409) {
        setMsgLey({ tipo: 'error', texto: 'Ya existe una ley con ese código.' });
        return;
      }
      if (!res.ok) throw new Error(`HTTP ${res.status}`);
      setLey(leyInicial);
      setMsgLey({ tipo: 'ok', texto: 'Propuesta de ley registrada correctamente.' });
    } catch (err) {
      console.error('Error al crear ley:', err);
      setMsgLey({ tipo: 'error', texto: 'No se pudo registrar la propuesta de ley.' });
    } finally {
      setSavingLey(false);
    }
  };

  const mensaje = (m: { tipo: 'ok' | 'error'; texto: string } | null) =>
    m && <p className={`mt-3 text-sm font-semibold ${m.tipo === 'ok' ? 'text-success-green' : 'text-danger-red'}`}>{m.texto}</p>;

  return (
    <div className="bg-white rounded-2xl shadow-sm border border-slate-200 overflow-hidden mb-8">
      <div className="px-8 py-6 border-b border-slate-100 bg-slate-50/50">
        <h4 className="text-sm font-black text-primary-navy uppercase tracking-wide">Registro manual</h4>
        <p className="text-sm text-slate-500 mt-1">Crea desde cero un perfil político o una propuesta de ley, sin depender de la importación.</p>
      </div>

      <div className="p-8 grid gap-8 lg:grid-cols-2">
        {/* Crear político (CF-001) */}
        <form onSubmit={crearPolitico} className="space-y-3">
          <h5 className="text-sm font-black text-primary-navy">Nuevo perfil político</h5>
          <input className={inputClass} placeholder="Nombre completo *" value={politico.nombreCompleto} onChange={(e) => setPolitico({ ...politico, nombreCompleto: e.target.value })} />
          <div className="grid grid-cols-2 gap-3">
            <input className={inputClass} placeholder="Partido político" value={politico.partidoPolitico} onChange={(e) => setPolitico({ ...politico, partidoPolitico: e.target.value })} />
            <input className={inputClass} placeholder="Cargo actual" value={politico.cargoActual} onChange={(e) => setPolitico({ ...politico, cargoActual: e.target.value })} />
            <input className={inputClass} placeholder="Región" value={politico.region} onChange={(e) => setPolitico({ ...politico, region: e.target.value })} />
            <input className={inputClass} placeholder="Comisión" value={politico.comision} onChange={(e) => setPolitico({ ...politico, comision: e.target.value })} />
          </div>
          <input className={inputClass} type="number" step="0.01" placeholder="Patrimonio declarado" value={politico.patrimonioDeclarado} onChange={(e) => setPolitico({ ...politico, patrimonioDeclarado: e.target.value })} />
          <input className={inputClass} placeholder="URL de foto" value={politico.fotoUrl} onChange={(e) => setPolitico({ ...politico, fotoUrl: e.target.value })} />
          <textarea className={`${inputClass} h-20`} placeholder="Antecedentes" value={politico.antecedentes} onChange={(e) => setPolitico({ ...politico, antecedentes: e.target.value })} />
          <label className="flex items-center gap-2 text-sm text-slate-600">
            <input type="checkbox" checked={politico.estaActivo} onChange={(e) => setPolitico({ ...politico, estaActivo: e.target.checked })} />
            Activo
          </label>
          <button type="submit" disabled={savingPolitico} className="w-full rounded-xl bg-primary-navy px-4 py-2.5 text-sm font-black text-white hover:bg-slate-800 disabled:opacity-60">
            {savingPolitico ? 'Guardando...' : 'Registrar político'}
          </button>
          {mensaje(msgPolitico)}
        </form>

        {/* Crear ley (CF-007) */}
        <form onSubmit={crearLey} className="space-y-3">
          <h5 className="text-sm font-black text-primary-navy">Nueva propuesta de ley</h5>
          <input className={inputClass} placeholder="Título *" value={ley.titulo} onChange={(e) => setLey({ ...ley, titulo: e.target.value })} />
          <div className="grid grid-cols-2 gap-3">
            <input className={inputClass} placeholder="Código de expediente *" value={ley.codigo} onChange={(e) => setLey({ ...ley, codigo: e.target.value })} />
            <input className={inputClass} placeholder="Categoría" value={ley.categoria} onChange={(e) => setLey({ ...ley, categoria: e.target.value })} />
            <select className={inputClass} value={ley.estado} onChange={(e) => setLey({ ...ley, estado: e.target.value })}>
              {estados.map((es) => <option key={es} value={es}>{es}</option>)}
            </select>
            <input className={inputClass} type="date" value={ley.fechaIngreso} onChange={(e) => setLey({ ...ley, fechaIngreso: e.target.value })} />
            <input className={inputClass} placeholder="Proponente" value={ley.proponente} onChange={(e) => setLey({ ...ley, proponente: e.target.value })} />
            <input className={inputClass} placeholder="Tipo de expediente" value={ley.tipoExpediente} onChange={(e) => setLey({ ...ley, tipoExpediente: e.target.value })} />
          </div>
          <textarea className={`${inputClass} h-20`} placeholder="Descripción original (texto oficial)" value={ley.descripcionOriginal} onChange={(e) => setLey({ ...ley, descripcionOriginal: e.target.value })} />
          <textarea className={`${inputClass} h-16`} placeholder="Resumen simplificado" value={ley.descripcionSimplificada} onChange={(e) => setLey({ ...ley, descripcionSimplificada: e.target.value })} />
          <textarea className={`${inputClass} h-16`} placeholder="Impacto social" value={ley.impactoSocial} onChange={(e) => setLey({ ...ley, impactoSocial: e.target.value })} />
          <button type="submit" disabled={savingLey} className="w-full rounded-xl bg-accent-blue px-4 py-2.5 text-sm font-black text-white hover:bg-blue-600 disabled:opacity-60">
            {savingLey ? 'Guardando...' : 'Registrar propuesta de ley'}
          </button>
          {mensaje(msgLey)}
        </form>
      </div>
    </div>
  );
};

export default RegistroManual;
