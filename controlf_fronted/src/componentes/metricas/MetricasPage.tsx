import React, { useEffect, useMemo, useState } from 'react';

interface MetricaItem {
  etiqueta: string;
  valor: number;
}

interface Metricas {
  categoriaFiltro: string | null;
  estadoFiltro: string | null;
  desde: string | null;
  hasta: string | null;
  totalLeyes: number;
  totalVotos: number;
  promedioCoherenciaGlobal: number;
  leyesPorEstado: MetricaItem[];
  leyesPorCategoria: MetricaItem[];
  votosPorTipo: MetricaItem[];
  coherenciaPorCategoria: MetricaItem[];
  serieVotosPorMes: MetricaItem[];
}

const PALETA = ['#2563eb', '#059669', '#d97706', '#dc2626', '#7c3aed', '#0891b2', '#db2777', '#65a30d'];

// Colores semánticos para indicadores de coherencia (0-100%).
const VERDE = '#22c55e';
const AMBAR = '#f59e0b';
const ROJO = '#ef4444';

// Hook reutilizable de "animación al montar": arranca en falso y pasa a
// verdadero en el siguiente frame, de modo que las transiciones CSS de los
// gráficos crezcan desde su estado inicial. Al remontar el componente (cambio
// de vista o recarga por filtros) la animación se repite.
const useMounted = (): boolean => {
  const [montado, setMontado] = useState(false);
  useEffect(() => {
    const raf = requestAnimationFrame(() => setMontado(true));
    return () => cancelAnimationFrame(raf);
  }, []);
  return montado;
};

const SinDatos: React.FC = () => (
  <p className="text-sm text-slate-400 italic">Sin datos para los filtros actuales.</p>
);

const HorizontalBars: React.FC<{ datos: MetricaItem[]; sufijo?: string; max?: number }> = ({ datos, sufijo = '', max }) => {
  const maximo = max ?? Math.max(1, ...datos.map((d) => d.valor));
  if (datos.length === 0) {
    return <p className="text-sm text-slate-400 italic">Sin datos para los filtros actuales.</p>;
  }
  return (
    <div className="space-y-3">
      {datos.map((d, i) => (
        <div key={d.etiqueta}>
          <div className="flex justify-between text-xs font-bold text-slate-600 mb-1">
            <span className="truncate">{d.etiqueta}</span>
            <span>{d.valor}{sufijo}</span>
          </div>
          <div className="w-full h-3 bg-slate-100 rounded-full overflow-hidden border border-slate-200">
            <div
              className="h-full rounded-full transition-all duration-700"
              style={{ width: `${(d.valor / maximo) * 100}%`, backgroundColor: PALETA[i % PALETA.length] }}
            ></div>
          </div>
        </div>
      ))}
    </div>
  );
};

const MESES_CORTOS = ['Ene', 'Feb', 'Mar', 'Abr', 'May', 'Jun', 'Jul', 'Ago', 'Sep', 'Oct', 'Nov', 'Dic'];

// Convierte una etiqueta "yyyy-MM" en un mes legible + año corto ("Ene" / "'24").
const formatearMes = (etiqueta: string): { mes: string; anio: string } => {
  const m = /^(\d{4})-(\d{2})$/.exec(etiqueta);
  if (!m) return { mes: etiqueta, anio: '' };
  const idx = parseInt(m[2], 10) - 1;
  return { mes: MESES_CORTOS[idx] ?? m[2], anio: `'${m[1].slice(2)}` };
};

const ColumnChart: React.FC<{ datos: MetricaItem[] }> = ({ datos }) => {
  // Animación de crecimiento: al montar, las barras parten de 0 y transicionan
  // hasta su altura real. El componente se remonta en cada carga (skeleton de
  // isLoading), por lo que la animación se repite al aplicar filtros.
  const [animado, setAnimado] = useState(false);
  useEffect(() => {
    const raf = requestAnimationFrame(() => setAnimado(true));
    return () => cancelAnimationFrame(raf);
  }, []);

  if (datos.length === 0) {
    return <p className="text-sm text-slate-400 italic">Sin datos para los filtros actuales.</p>;
  }
  const maximo = Math.max(1, ...datos.map((d) => d.valor));
  const guias = [1, 0.75, 0.5, 0.25, 0]; // niveles de referencia (100%..0% del máximo)

  return (
    <div className="flex gap-3">
      {/* Eje Y con valores de referencia */}
      <div className="flex flex-col justify-between h-56 pt-6 pr-1 text-right text-[9px] font-bold text-slate-300 select-none">
        {guias.map((g) => (
          <span key={g} className="leading-none">{Math.round(maximo * g)}</span>
        ))}
      </div>

      <div className="flex-1 overflow-x-auto">
        {/* Zona de barras con líneas guía de fondo */}
        <div className="relative">
          <div className="absolute inset-0 flex flex-col justify-between pt-6 pointer-events-none">
            {guias.map((g) => (
              <div key={g} className="border-t border-dashed border-slate-100"></div>
            ))}
          </div>

          <div className="relative flex items-end gap-2 sm:gap-3 h-56 pt-6">
            {datos.map((d) => {
              const alturaPct = d.valor > 0 ? Math.max((d.valor / maximo) * 100, 3) : 0;
              return (
                <div key={d.etiqueta} className="group flex h-full min-w-[38px] flex-1 items-end justify-center">
                  <div
                    className="relative w-full max-w-[44px] rounded-t-md bg-gradient-to-t from-accent-blue/60 to-accent-blue shadow-[0_1px_3px_rgba(59,130,246,0.35)] transition-[height,background-color] duration-700 ease-out group-hover:from-accent-blue group-hover:to-primary-navy"
                    style={{ height: animado ? `${alturaPct}%` : '0%' }}
                  >
                    <span className="absolute -top-5 left-1/2 -translate-x-1/2 text-[11px] font-black text-slate-500 opacity-70 transition-all group-hover:text-accent-blue group-hover:opacity-100">
                      {d.valor}
                    </span>
                  </div>
                </div>
              );
            })}
          </div>
        </div>

        {/* Etiquetas de mes (fila alineada con las barras) */}
        <div className="flex gap-2 sm:gap-3 mt-2">
          {datos.map((d) => {
            const { mes, anio } = formatearMes(d.etiqueta);
            return (
              <div key={d.etiqueta} className="flex min-w-[38px] flex-1 flex-col items-center leading-none">
                <span className="text-[10px] font-black text-slate-500 whitespace-nowrap">{mes}</span>
                <span className="text-[8px] font-bold text-slate-300">{anio}</span>
              </div>
            );
          })}
        </div>
      </div>
    </div>
  );
};

// ─────────────────────────────────────────────────────────────────────────────
// Nuevas formas de visualización (SVG/CSS puro, sin dependencias externas).
// Todas consumen la misma estructura MetricaItem[] que los gráficos originales.
// ─────────────────────────────────────────────────────────────────────────────

// Columnas verticales para series categóricas (color por categoría).
const CategoryColumns: React.FC<{ datos: MetricaItem[]; sufijo?: string; max?: number }> = ({ datos, sufijo = '', max }) => {
  const animado = useMounted();
  if (datos.length === 0) return <SinDatos />;
  const maximo = max ?? Math.max(1, ...datos.map((d) => d.valor));
  const guias = [1, 0.75, 0.5, 0.25, 0];

  return (
    <div className="flex gap-3">
      <div className="flex flex-col justify-between h-56 pt-6 pr-1 text-right text-[9px] font-bold text-slate-300 select-none">
        {guias.map((g) => (
          <span key={g} className="leading-none">{Math.round(maximo * g)}{sufijo}</span>
        ))}
      </div>

      <div className="flex-1 overflow-x-auto">
        <div className="relative">
          <div className="absolute inset-0 flex flex-col justify-between pt-6 pointer-events-none">
            {guias.map((g) => (
              <div key={g} className="border-t border-dashed border-slate-100"></div>
            ))}
          </div>

          <div className="relative flex items-end gap-2 sm:gap-3 h-56 pt-6">
            {datos.map((d, i) => {
              const alturaPct = d.valor > 0 ? Math.max((d.valor / maximo) * 100, 3) : 0;
              return (
                <div key={d.etiqueta} className="group flex h-full min-w-[44px] flex-1 items-end justify-center">
                  <div
                    className="relative w-full max-w-[52px] rounded-t-md shadow-sm transition-[height] duration-700 ease-out"
                    style={{ height: animado ? `${alturaPct}%` : '0%', backgroundColor: PALETA[i % PALETA.length] }}
                  >
                    <span className="absolute -top-5 left-1/2 -translate-x-1/2 text-[11px] font-black text-slate-500 opacity-80">
                      {d.valor}{sufijo}
                    </span>
                  </div>
                </div>
              );
            })}
          </div>
        </div>

        <div className="flex gap-2 sm:gap-3 mt-2">
          {datos.map((d) => (
            <div key={d.etiqueta} className="flex min-w-[44px] flex-1 flex-col items-center leading-tight">
              <span className="text-[10px] font-bold text-slate-500 text-center break-words">{d.etiqueta}</span>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
};

// Gráfico de dona para distribuciones de conteo (partes de un todo).
const DonutChart: React.FC<{ datos: MetricaItem[]; sufijo?: string }> = ({ datos, sufijo = '' }) => {
  const animado = useMounted();
  const total = datos.reduce((s, d) => s + d.valor, 0);
  if (datos.length === 0 || total <= 0) return <SinDatos />;

  const size = 176;
  const stroke = 24;
  const r = (size - stroke) / 2;
  const c = size / 2;
  const circ = 2 * Math.PI * r;

  let acc = 0;
  const segmentos = datos.map((d, i) => {
    const frac = d.valor / total;
    const seg = { etiqueta: d.etiqueta, valor: d.valor, frac, inicio: acc, color: PALETA[i % PALETA.length] };
    acc += frac;
    return seg;
  });

  return (
    <div className="flex flex-col sm:flex-row items-center gap-6">
      <div className="relative shrink-0" style={{ width: size, height: size }}>
        <svg width={size} height={size} viewBox={`0 0 ${size} ${size}`}>
          <circle cx={c} cy={c} r={r} fill="none" stroke="#f1f5f9" strokeWidth={stroke} />
          {segmentos.map((s, i) => (
            <circle
              key={s.etiqueta}
              cx={c}
              cy={c}
              r={r}
              fill="none"
              stroke={s.color}
              strokeWidth={stroke}
              strokeLinecap="butt"
              strokeDasharray={`${animado ? s.frac * circ : 0} ${circ}`}
              transform={`rotate(${s.inicio * 360 - 90} ${c} ${c})`}
              style={{ transition: `stroke-dasharray 0.8s ease ${i * 0.08}s` }}
            />
          ))}
        </svg>
        <div className="absolute inset-0 flex flex-col items-center justify-center pointer-events-none">
          <span className="text-2xl font-black text-primary-navy">{total}</span>
          <span className="text-[9px] font-black uppercase tracking-widest text-slate-400">Total</span>
        </div>
      </div>

      <div className="flex-1 w-full space-y-2">
        {segmentos.map((s) => (
          <div key={s.etiqueta} className="flex items-center gap-2 text-xs">
            <span className="h-3 w-3 rounded-sm shrink-0" style={{ backgroundColor: s.color }}></span>
            <span className="flex-1 truncate font-bold text-slate-600">{s.etiqueta}</span>
            <span className="font-black text-slate-700 tabular-nums">{s.valor}{sufijo}</span>
            <span className="w-9 text-right font-bold text-slate-400 tabular-nums">{Math.round(s.frac * 100)}%</span>
          </div>
        ))}
      </div>
    </div>
  );
};

// Indicadores radiales para métricas en porcentaje (p. ej. coherencia por categoría).
const RadialGauges: React.FC<{ datos: MetricaItem[]; max?: number }> = ({ datos, max = 100 }) => {
  const animado = useMounted();
  if (datos.length === 0) return <SinDatos />;

  const size = 104;
  const stroke = 10;
  const r = (size - stroke) / 2;
  const c = size / 2;
  const circ = 2 * Math.PI * r;

  return (
    <div className="grid grid-cols-2 sm:grid-cols-3 gap-4">
      {datos.map((d, i) => {
        const pct = Math.max(0, Math.min(1, d.valor / max));
        const color = d.valor >= 75 ? VERDE : d.valor >= 50 ? AMBAR : ROJO;
        return (
          <div key={d.etiqueta} className="flex flex-col items-center">
            <div className="relative" style={{ width: size, height: size }}>
              <svg width={size} height={size} viewBox={`0 0 ${size} ${size}`} className="-rotate-90">
                <circle cx={c} cy={c} r={r} fill="none" stroke="#f1f5f9" strokeWidth={stroke} />
                <circle
                  cx={c}
                  cy={c}
                  r={r}
                  fill="none"
                  stroke={color}
                  strokeWidth={stroke}
                  strokeLinecap="round"
                  strokeDasharray={circ}
                  strokeDashoffset={animado ? circ * (1 - pct) : circ}
                  style={{ transition: `stroke-dashoffset 0.9s ease ${i * 0.08}s` }}
                />
              </svg>
              <div className="absolute inset-0 flex items-center justify-center">
                <span className="text-lg font-black text-primary-navy tabular-nums">{d.valor}%</span>
              </div>
            </div>
            <span className="mt-1 text-[11px] font-bold text-slate-500 text-center break-words">{d.etiqueta}</span>
          </div>
        );
      })}
    </div>
  );
};

// Gráfico de radar para comparar una métrica entre categorías (≥3 ejes).
const RadarChart: React.FC<{ datos: MetricaItem[]; max?: number; sufijo?: string }> = ({ datos, max, sufijo = '' }) => {
  const animado = useMounted();
  // Salvaguarda: el radar necesita al menos 3 ejes para ser legible.
  if (datos.length < 3) return <HorizontalBars datos={datos} sufijo={sufijo} max={max} />;

  const size = 280;
  const c = size / 2;
  const R = size / 2 - 46;
  const N = datos.length;
  const maximo = max ?? Math.max(1, ...datos.map((d) => d.valor));
  const niveles = [0.25, 0.5, 0.75, 1];

  const punto = (i: number, radio: number) => {
    const ang = -Math.PI / 2 + (i * 2 * Math.PI) / N;
    return { x: c + radio * Math.cos(ang), y: c + radio * Math.sin(ang) };
  };

  const dataPts = datos.map((d, i) => punto(i, (Math.max(0, d.valor) / maximo) * R));
  const poligono = dataPts.map((p) => `${p.x},${p.y}`).join(' ');

  return (
    <div className="flex justify-center">
      <svg width="100%" viewBox={`0 0 ${size} ${size}`} style={{ maxWidth: size }}>
        {niveles.map((n) => (
          <polygon
            key={n}
            points={datos.map((_, i) => { const p = punto(i, R * n); return `${p.x},${p.y}`; }).join(' ')}
            fill="none"
            stroke="#e2e8f0"
            strokeWidth={1}
          />
        ))}
        {datos.map((_, i) => {
          const p = punto(i, R);
          return <line key={i} x1={c} y1={c} x2={p.x} y2={p.y} stroke="#e2e8f0" strokeWidth={1} />;
        })}
        <polygon
          points={poligono}
          fill="rgba(59,130,246,0.18)"
          stroke="#3b82f6"
          strokeWidth={2}
          strokeLinejoin="round"
          style={{
            transformOrigin: `${c}px ${c}px`,
            transform: animado ? 'scale(1)' : 'scale(0)',
            transition: 'transform 0.7s cubic-bezier(0.22, 1, 0.36, 1)',
          }}
        />
        {dataPts.map((p, i) => (
          <circle
            key={i}
            cx={p.x}
            cy={p.y}
            r={3.5}
            fill="#3b82f6"
            style={{ opacity: animado ? 1 : 0, transition: `opacity 0.4s ease ${0.3 + i * 0.05}s` }}
          />
        ))}
        {datos.map((d, i) => {
          const p = punto(i, R + 22);
          const etiqueta = d.etiqueta.length > 12 ? `${d.etiqueta.slice(0, 11)}…` : d.etiqueta;
          return (
            <text key={i} x={p.x} y={p.y} textAnchor="middle" dominantBaseline="middle" fill="#64748b" style={{ fontSize: 10, fontWeight: 700 }}>
              <tspan x={p.x} dy="-0.3em">{etiqueta}</tspan>
              <tspan x={p.x} dy="1.1em" fill="#94a3b8" style={{ fontSize: 9 }}>{d.valor}{sufijo}</tspan>
            </text>
          );
        })}
      </svg>
    </div>
  );
};

// Gráfico de línea/área. Ideal para la serie temporal, y válido como perfil
// alternativo para series categóricas ordenadas.
const LineAreaChart: React.FC<{ datos: MetricaItem[]; sufijo?: string; esTiempo?: boolean; max?: number }> = ({ datos, sufijo = '', esTiempo = false, max }) => {
  const animado = useMounted();
  if (datos.length === 0) return <SinDatos />;

  const W = 560;
  const H = 220;
  const padL = 34;
  const padR = 12;
  const padT = 16;
  const padB = 34;
  // Ancla el eje Y a un máximo fijo cuando se provee (p. ej. 100 para
  // porcentajes), para ser consistente con las demás vistas; si no, auto-escala.
  const maximo = max ?? Math.max(1, ...datos.map((d) => d.valor));
  const n = datos.length;
  const x = (i: number) => padL + (n === 1 ? (W - padL - padR) / 2 : (i * (W - padL - padR)) / (n - 1));
  const y = (v: number) => padT + (1 - v / maximo) * (H - padT - padB);
  const linePts = datos.map((d, i) => `${x(i)},${y(d.valor)}`).join(' ');
  const areaPts = `${x(0)},${H - padB} ${linePts} ${x(n - 1)},${H - padB}`;
  const guias = [1, 0.5, 0];

  return (
    <div className="overflow-x-auto">
      <svg width="100%" viewBox={`0 0 ${W} ${H}`} style={{ minWidth: n > 8 ? W : undefined }}>
        <defs>
          <linearGradient id="metricLineArea" x1="0" y1="0" x2="0" y2="1">
            <stop offset="0%" stopColor="#3b82f6" stopOpacity="0.28" />
            <stop offset="100%" stopColor="#3b82f6" stopOpacity="0" />
          </linearGradient>
        </defs>

        {guias.map((g) => {
          const yy = padT + (1 - g) * (H - padT - padB);
          return (
            <g key={g}>
              <line x1={padL} y1={yy} x2={W - padR} y2={yy} stroke="#f1f5f9" strokeWidth={1} />
              <text x={padL - 6} y={yy} textAnchor="end" dominantBaseline="middle" fill="#cbd5e1" style={{ fontSize: 9, fontWeight: 700 }}>
                {Math.round(maximo * g)}{sufijo}
              </text>
            </g>
          );
        })}

        {n > 1 && (
          <polygon points={areaPts} fill="url(#metricLineArea)" style={{ opacity: animado ? 1 : 0, transition: 'opacity 0.8s ease 0.2s' }} />
        )}
        {n > 1 && (
          <polyline
            points={linePts}
            fill="none"
            stroke="#3b82f6"
            strokeWidth={2.5}
            strokeLinejoin="round"
            strokeLinecap="round"
            pathLength={1}
            strokeDasharray={1}
            strokeDashoffset={animado ? 0 : 1}
            style={{ transition: 'stroke-dashoffset 1s ease' }}
          />
        )}
        {datos.map((d, i) => (
          <circle
            key={d.etiqueta}
            cx={x(i)}
            cy={y(d.valor)}
            r={3}
            fill="#fff"
            stroke="#3b82f6"
            strokeWidth={2}
            style={{ opacity: animado ? 1 : 0, transition: `opacity 0.4s ease ${0.4 + i * 0.03}s` }}
          />
        ))}
        {datos.map((d, i) => {
          const mostrar = n <= 8 || i % Math.ceil(n / 8) === 0;
          if (!mostrar) return null;
          const bruta = esTiempo ? (() => { const { mes, anio } = formatearMes(d.etiqueta); return `${mes} ${anio}`; })() : d.etiqueta;
          const etiqueta = bruta.length > 8 ? bruta.slice(0, 8) : bruta;
          return (
            <text key={d.etiqueta} x={x(i)} y={H - padB + 14} textAnchor="middle" fill="#94a3b8" style={{ fontSize: 9, fontWeight: 700 }}>
              {etiqueta}
            </text>
          );
        })}
      </svg>
    </div>
  );
};

// Tabla interactiva: ordenable por etiqueta o por valor, con mini-barra y % del total.
type ClaveOrden = 'etiqueta' | 'valor';
type SentidoOrden = 'asc' | 'desc';

const DataTable: React.FC<{ datos: MetricaItem[]; sufijo?: string; esTiempo?: boolean; esPorcentaje?: boolean }> = ({ datos, sufijo = '', esTiempo = false, esPorcentaje = false }) => {
  const [clave, setClave] = useState<ClaveOrden>('valor');
  const [sentido, setSentido] = useState<SentidoOrden>('desc');
  if (datos.length === 0) return <SinDatos />;

  const total = datos.reduce((s, d) => s + d.valor, 0);
  const maximo = Math.max(1, ...datos.map((d) => d.valor));
  // El "% del total" solo tiene sentido en conteos (partes de un todo). Para
  // series de tiempo y para métricas que ya son porcentajes (p. ej. coherencia,
  // un promedio) esa columna sería un número sin significado, así que se oculta.
  const mostrarPct = !esTiempo && !esPorcentaje && total > 0;

  const ordenados = [...datos].sort((a, b) => {
    const va = clave === 'valor' ? a.valor : a.etiqueta;
    const vb = clave === 'valor' ? b.valor : b.etiqueta;
    if (va < vb) return sentido === 'asc' ? -1 : 1;
    if (va > vb) return sentido === 'asc' ? 1 : -1;
    return 0;
  });

  const alternar = (k: ClaveOrden) => {
    if (clave === k) setSentido((s) => (s === 'asc' ? 'desc' : 'asc'));
    else { setClave(k); setSentido(k === 'valor' ? 'desc' : 'asc'); }
  };
  const flecha = (k: ClaveOrden) => (clave !== k ? '↕' : sentido === 'asc' ? '↑' : '↓');

  return (
    <div className="overflow-x-auto">
      <table className="w-full text-sm">
        <thead>
          <tr className="border-b border-slate-200 text-[10px] uppercase tracking-widest text-slate-400">
            <th className="py-2 text-left font-black cursor-pointer select-none hover:text-slate-600" onClick={() => alternar('etiqueta')}>
              {esTiempo ? 'Mes' : 'Etiqueta'} <span className="text-slate-300">{flecha('etiqueta')}</span>
            </th>
            <th className="py-2 text-right font-black cursor-pointer select-none hover:text-slate-600" onClick={() => alternar('valor')}>
              Valor <span className="text-slate-300">{flecha('valor')}</span>
            </th>
            <th className="py-2 pl-4 text-left font-black w-1/3">Distribución</th>
            {mostrarPct && <th className="py-2 text-right font-black">%</th>}
          </tr>
        </thead>
        <tbody>
          {ordenados.map((d, i) => {
            const etiqueta = esTiempo ? (() => { const { mes, anio } = formatearMes(d.etiqueta); return `${mes} ${anio}`; })() : d.etiqueta;
            return (
              <tr key={d.etiqueta} className="border-b border-slate-100 hover:bg-slate-50 transition-colors">
                <td className="py-2.5 font-bold text-slate-700">{etiqueta}</td>
                <td className="py-2.5 text-right font-black text-slate-800 tabular-nums">{d.valor}{sufijo}</td>
                <td className="py-2.5 pl-4">
                  <div className="h-2 w-full rounded-full bg-slate-100 overflow-hidden">
                    <div
                      className="h-full rounded-full transition-all duration-700"
                      style={{ width: `${(d.valor / maximo) * 100}%`, backgroundColor: PALETA[i % PALETA.length] }}
                    ></div>
                  </div>
                </td>
                {mostrarPct && (
                  <td className="py-2.5 text-right font-bold text-slate-400 tabular-nums">{Math.round((d.valor / total) * 100)}%</td>
                )}
              </tr>
            );
          })}
        </tbody>
      </table>
    </div>
  );
};

// ─────────────────────────────────────────────────────────────────────────────
// Selector de forma de visualización + despachador que elige el gráfico según
// la vista activa y el tipo de dato (conteo vs. porcentaje).
// ─────────────────────────────────────────────────────────────────────────────

type Vista = 'barras' | 'columnas' | 'circular' | 'radar' | 'linea' | 'tabla';

const ICONOS: Record<Vista, React.ReactNode> = {
  barras: (
    <svg width="16" height="16" viewBox="0 0 16 16" aria-hidden="true" fill="currentColor">
      <rect x="2" y="3" width="9" height="2.2" rx="1" />
      <rect x="2" y="7" width="12" height="2.2" rx="1" />
      <rect x="2" y="11" width="6" height="2.2" rx="1" />
    </svg>
  ),
  columnas: (
    <svg width="16" height="16" viewBox="0 0 16 16" aria-hidden="true" fill="currentColor">
      <rect x="2.5" y="7" width="2.6" height="6" rx="1" />
      <rect x="6.7" y="4" width="2.6" height="9" rx="1" />
      <rect x="10.9" y="9" width="2.6" height="4" rx="1" />
    </svg>
  ),
  circular: (
    <svg width="16" height="16" viewBox="0 0 16 16" aria-hidden="true" fill="none">
      <circle cx="8" cy="8" r="5.4" stroke="currentColor" strokeWidth="3" />
    </svg>
  ),
  radar: (
    <svg width="16" height="16" viewBox="0 0 16 16" aria-hidden="true" fill="none">
      <polygon points="8,2 13,5 13,11 8,14 3,11 3,5" stroke="currentColor" strokeWidth="1.3" />
      <polygon points="8,5 11,6.6 10.4,10 6,10.4 5,7" fill="currentColor" fillOpacity="0.25" stroke="currentColor" strokeWidth="1" />
    </svg>
  ),
  linea: (
    <svg width="16" height="16" viewBox="0 0 16 16" aria-hidden="true" fill="none">
      <polyline points="2,11 6,6 9,9 14,3" stroke="currentColor" strokeWidth="1.6" strokeLinecap="round" strokeLinejoin="round" />
    </svg>
  ),
  tabla: (
    <svg width="16" height="16" viewBox="0 0 16 16" aria-hidden="true" fill="none">
      <rect x="2.5" y="3" width="11" height="10" rx="1.2" stroke="currentColor" strokeWidth="1.3" />
      <line x1="2.5" y1="6.5" x2="13.5" y2="6.5" stroke="currentColor" strokeWidth="1.1" />
      <line x1="8" y1="3" x2="8" y2="13" stroke="currentColor" strokeWidth="1.1" />
    </svg>
  ),
};

const VISTAS: { id: Vista; label: string }[] = [
  { id: 'barras', label: 'Barras' },
  { id: 'columnas', label: 'Columnas' },
  { id: 'circular', label: 'Circular' },
  { id: 'radar', label: 'Radar' },
  { id: 'linea', label: 'Línea/Área' },
  { id: 'tabla', label: 'Tabla' },
];

const SelectorVista: React.FC<{ vista: Vista; onChange: (v: Vista) => void }> = ({ vista, onChange }) => (
  <div className="inline-flex flex-wrap gap-1 rounded-xl border border-slate-200 bg-slate-50 p-1" role="tablist" aria-label="Forma de visualización">
    {VISTAS.map((v) => {
      const activo = vista === v.id;
      return (
        <button
          key={v.id}
          type="button"
          role="tab"
          aria-selected={activo}
          onClick={() => onChange(v.id)}
          title={v.label}
          className={`flex items-center gap-1.5 rounded-lg px-3 py-2 text-xs font-black transition-all ${
            activo ? 'bg-white text-accent-blue shadow-sm' : 'text-slate-500 hover:text-slate-700'
          }`}
        >
          {ICONOS[v.id]}
          <span className="hidden sm:inline">{v.label}</span>
        </button>
      );
    })}
  </div>
);

// Despachador: misma información desde distintas perspectivas. Elige el gráfico
// compatible con la vista y el tipo de dato; para 'radar' cae a barras cuando
// no hay ejes suficientes, y para 'circular' usa indicadores en porcentajes.
const SerieChart: React.FC<{
  vista: Vista;
  datos: MetricaItem[];
  tipo: 'conteo' | 'porcentaje';
  sufijo?: string;
  max?: number;
  esTiempo?: boolean;
}> = ({ vista, datos, tipo, sufijo = '', max, esTiempo = false }) => {
  switch (vista) {
    case 'barras':
      return <HorizontalBars datos={datos} sufijo={sufijo} max={max} />;
    case 'columnas':
      return <CategoryColumns datos={datos} sufijo={sufijo} max={max} />;
    case 'circular':
      return tipo === 'porcentaje'
        ? <RadialGauges datos={datos} max={max} />
        : <DonutChart datos={datos} sufijo={sufijo} />;
    case 'radar':
      return datos.length >= 3
        ? <RadarChart datos={datos} max={max} sufijo={sufijo} />
        : <HorizontalBars datos={datos} sufijo={sufijo} max={max} />;
    case 'linea':
      return <LineAreaChart datos={datos} sufijo={sufijo} esTiempo={esTiempo} max={max} />;
    case 'tabla':
      return <DataTable datos={datos} sufijo={sufijo} esTiempo={esTiempo} esPorcentaje={tipo === 'porcentaje'} />;
    default:
      return <HorizontalBars datos={datos} sufijo={sufijo} max={max} />;
  }
};

const Card: React.FC<{ titulo: string; children: React.ReactNode }> = ({ titulo, children }) => (
  <div className="bg-white rounded-2xl shadow-sm border border-slate-200 p-6">
    <h3 className="text-sm font-black text-primary-navy uppercase tracking-wide mb-4">{titulo}</h3>
    {children}
  </div>
);

// Tarjeta con entrada animada (fade + slide) al cambiar de vista; el stagger
// produce una cascada discreta. El estado del gráfico se reinicia al remontar.
const AnimCard: React.FC<{ titulo: string; delay?: number; children: React.ReactNode }> = ({ titulo, delay = 0, children }) => (
  <div className="metric-anim-enter" style={{ animationDelay: `${delay}ms` }}>
    <Card titulo={titulo}>{children}</Card>
  </div>
);

const MetricasPage: React.FC = () => {
  const [metricas, setMetricas] = useState<Metricas | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [categorias, setCategorias] = useState<string[]>([]);
  const [estados, setEstados] = useState<string[]>([]);

  const [categoria, setCategoria] = useState('');
  const [estado, setEstado] = useState('');
  const [desde, setDesde] = useState('');
  const [hasta, setHasta] = useState('');

  const [vista, setVista] = useState<Vista>('barras');

  useEffect(() => {
    fetch('/api/leyes/filtros')
      .then((res) => res.json())
      .then((data) => {
        setCategorias(Array.isArray(data?.categorias) ? data.categorias : []);
        setEstados(Array.isArray(data?.estados) ? data.estados : []);
      })
      .catch((err) => console.error('Error al cargar filtros:', err));
  }, []);

  const cargarMetricas = useMemo(
    () => async (params: { categoria: string; estado: string; desde: string; hasta: string }) => {
      setIsLoading(true);
      try {
        const query = new URLSearchParams();
        if (params.categoria) query.set('categoria', params.categoria);
        if (params.estado) query.set('estado', params.estado);
        if (params.desde) query.set('desde', params.desde);
        if (params.hasta) query.set('hasta', params.hasta);
        const res = await fetch(`/api/dashboard/metricas?${query.toString()}`);
        const data = await res.json();
        setMetricas(data);
      } catch (err) {
        console.error('Error al cargar métricas:', err);
      } finally {
        setIsLoading(false);
      }
    },
    []
  );

  useEffect(() => {
    cargarMetricas({ categoria: '', estado: '', desde: '', hasta: '' });
  }, [cargarMetricas]);

  const aplicar = () => cargarMetricas({ categoria, estado, desde, hasta });
  const limpiar = () => {
    setCategoria('');
    setEstado('');
    setDesde('');
    setHasta('');
    cargarMetricas({ categoria: '', estado: '', desde: '', hasta: '' });
  };

  return (
    <div className="max-w-6xl mx-auto space-y-8 pb-12">
      <div>
        <h2 className="text-2xl font-black text-primary-navy uppercase tracking-tighter">Métricas de Cumplimiento</h2>
        <p className="text-slate-500">Explora las métricas con filtros, rangos de tiempo y comparaciones por categoría.</p>
      </div>

      {/* Filtros */}
      <div className="bg-white rounded-2xl shadow-sm border border-slate-200 p-6">
        <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-5 items-end">
          <div>
            <label className="text-[10px] font-black uppercase tracking-widest text-slate-500">Categoría</label>
            <select value={categoria} onChange={(e) => setCategoria(e.target.value)} className="mt-1 w-full rounded-xl border border-slate-200 bg-white px-3 py-2.5 text-sm text-slate-700 focus:border-accent-blue focus:outline-none">
              <option value="">Todas</option>
              {categorias.map((c) => <option key={c} value={c}>{c}</option>)}
            </select>
          </div>
          <div>
            <label className="text-[10px] font-black uppercase tracking-widest text-slate-500">Estado</label>
            <select value={estado} onChange={(e) => setEstado(e.target.value)} className="mt-1 w-full rounded-xl border border-slate-200 bg-white px-3 py-2.5 text-sm text-slate-700 focus:border-accent-blue focus:outline-none">
              <option value="">Todos</option>
              {estados.map((e) => <option key={e} value={e}>{e}</option>)}
            </select>
          </div>
          <div>
            <label className="text-[10px] font-black uppercase tracking-widest text-slate-500">Desde</label>
            <input type="date" value={desde} onChange={(e) => setDesde(e.target.value)} className="mt-1 w-full rounded-xl border border-slate-200 bg-white px-3 py-2.5 text-sm text-slate-700 focus:border-accent-blue focus:outline-none" />
          </div>
          <div>
            <label className="text-[10px] font-black uppercase tracking-widest text-slate-500">Hasta</label>
            <input type="date" value={hasta} onChange={(e) => setHasta(e.target.value)} className="mt-1 w-full rounded-xl border border-slate-200 bg-white px-3 py-2.5 text-sm text-slate-700 focus:border-accent-blue focus:outline-none" />
          </div>
          <div className="flex gap-2">
            <button onClick={aplicar} className="flex-1 rounded-xl bg-primary-navy px-4 py-2.5 text-sm font-black text-white hover:bg-slate-800 transition-all">Aplicar</button>
            <button onClick={limpiar} className="rounded-xl border border-slate-200 bg-white px-4 py-2.5 text-sm font-bold text-slate-600 hover:bg-slate-50">Limpiar</button>
          </div>
        </div>
      </div>

      {isLoading || !metricas ? (
        <div className="animate-pulse grid gap-6 md:grid-cols-2">
          {[1, 2, 3, 4].map((i) => <div key={i} className="bg-white h-64 rounded-2xl border border-slate-200"></div>)}
        </div>
      ) : (
        <>
          {/* Totales */}
          <div className="grid gap-4 md:grid-cols-3">
            <div className="rounded-2xl border border-slate-200 bg-white p-6 shadow-sm border-b-4 border-b-accent-blue">
              <p className="text-[10px] font-black uppercase tracking-widest text-slate-400">Leyes (filtradas)</p>
              <p className="mt-2 text-3xl font-black text-primary-navy">{metricas.totalLeyes}</p>
            </div>
            <div className="rounded-2xl border border-slate-200 bg-white p-6 shadow-sm border-b-4 border-b-success-green">
              <p className="text-[10px] font-black uppercase tracking-widest text-slate-400">Votos (filtrados)</p>
              <p className="mt-2 text-3xl font-black text-primary-navy">{metricas.totalVotos}</p>
            </div>
            <div className="rounded-2xl border border-slate-200 bg-white p-6 shadow-sm border-b-4 border-b-warning-amber">
              <p className="text-[10px] font-black uppercase tracking-widest text-slate-400">Coherencia global</p>
              <p className="mt-2 text-3xl font-black text-primary-navy">{metricas.promedioCoherenciaGlobal}%</p>
            </div>
          </div>

          {/* Selector de forma de visualización */}
          <div className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
            <p className="text-[10px] font-black uppercase tracking-widest text-slate-400">Forma de visualización</p>
            <SelectorVista vista={vista} onChange={setVista} />
          </div>

          {/* Gráficos. La key por vista remonta el bloque para reproducir la
              animación de entrada al cambiar de forma de visualización. Todas
              las vistas comparten los mismos datos y filtros. */}
          <div key={vista} className="space-y-6">
            <div className="grid gap-6 md:grid-cols-2">
              <AnimCard titulo="Leyes por estado" delay={0}>
                <SerieChart vista={vista} datos={metricas.leyesPorEstado} tipo="conteo" />
              </AnimCard>
              <AnimCard titulo="Leyes por categoría" delay={70}>
                <SerieChart vista={vista} datos={metricas.leyesPorCategoria} tipo="conteo" />
              </AnimCard>
              <AnimCard titulo="Votos por tipo" delay={140}>
                <SerieChart vista={vista} datos={metricas.votosPorTipo} tipo="conteo" />
              </AnimCard>
              <AnimCard titulo="Cumplimiento (coherencia) por categoría" delay={210}>
                <SerieChart vista={vista} datos={metricas.coherenciaPorCategoria} tipo="porcentaje" sufijo="%" max={100} />
              </AnimCard>
            </div>

            <AnimCard titulo="Votos registrados por mes" delay={280}>
              {vista === 'tabla' ? (
                <DataTable datos={metricas.serieVotosPorMes} esTiempo />
              ) : vista === 'linea' ? (
                <LineAreaChart datos={metricas.serieVotosPorMes} esTiempo />
              ) : (
                <ColumnChart datos={metricas.serieVotosPorMes} />
              )}
            </AnimCard>
          </div>
        </>
      )}
    </div>
  );
};

export default MetricasPage;
