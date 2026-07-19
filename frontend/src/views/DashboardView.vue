<template>
  <div class="p-3 md:p-5 bg-white h-screen max-h-screen flex flex-col text-slate-900 font-sans antialiased overflow-hidden select-none">
    <header class="flex flex-col sm:flex-row justify-between items-start sm:items-center border-b border-slate-200 pb-3 shrink-0 gap-2">
      <div>
        <h1 class="text-[13px] font-black tracking-tight text-slate-950 uppercase font-mono">Payload Dashboard</h1>
        <p class="text-[11px] font-mono text-slate-500 mt-0.5 uppercase tracking-widest font-bold">SDQ Hub // Payload Despachado por Vuelo</p>
      </div>
      <div class="flex items-center gap-3 text-[10px] font-mono font-bold flex-wrap">
        <span class="flex items-center gap-1 text-slate-500">
          <span class="h-2 w-2 rounded-full" style="background: var(--accent)"></span> LIVE
        </span>
        <span class="text-slate-300 hidden sm:inline">|</span>
        <span class="text-slate-950">{{ filteredFlights.length }} vuelos</span>
        <span class="text-slate-300 hidden sm:inline">|</span>
        <button @click="descargarReporte"
          class="flex items-center gap-1 px-3 py-1.5 rounded border border-slate-950 font-mono uppercase tracking-wider font-bold text-[11px] bg-slate-950 text-white hover:bg-slate-800 transition active:scale-95 shadow-sm">
         <span class="text-[12px] font-semibold leading-none">↓</span> Descargar Reporte
        </button>
      </div>
    </header>

    <section class="flex flex-col sm:flex-row items-start sm:items-center gap-2 sm:gap-3 my-3 shrink-0">
      <div class="flex items-center gap-2">
        <label class="text-[10px] font-mono font-black uppercase tracking-widest text-slate-950">Desde</label>
        <input v-model="dateFrom" type="date"
          class="text-[12px] font-mono px-3 py-1.5 rounded border border-slate-400 bg-white outline-none focus:border-slate-950" />
      </div>
      <div class="flex items-center gap-2">
        <label class="text-[10px] font-mono font-black uppercase tracking-widest text-slate-950">Hasta</label>
        <input v-model="dateTo" type="date"
          class="text-[12px] font-mono px-3 py-1.5 rounded border border-slate-400 bg-white outline-none focus:border-slate-950" />
      </div>
      <div class="text-[10px] font-mono text-slate-500 ml-auto flex items-center gap-4">
        <span>Total Neto: <strong class="text-slate-950">{{ totalNetPayload }} lbs</strong></span>
        <span>Total ULDs: <strong class="text-slate-950">{{ totalUldsCount }}</strong></span>
        <span>Total MAWBs: <strong class="text-slate-950">{{ totalMawbsCount }}</strong></span>
      </div>
    </section>

    <section class="flex-1 min-h-0 border border-slate-300 rounded overflow-hidden shadow-sm bg-white flex flex-col mb-1.5">
      <div ref="tableWrapper" class="overflow-auto flex-1 min-h-0 scrollbar-none">
        <div class="table-scroll-wrapper h-full">
        <table class="w-full border-collapse text-[11px] font-mono" :style="{ minWidth: tableMinWidth + 'px' }">
          <thead class="sticky top-0 z-20">
            <tr class="bg-slate-700 text-white text-[11px] font-bold uppercase tracking-wider border-b border-slate-500 shadow-sm">
              <th class="text-center px-2 py-2.5 whitespace-nowrap w-8 sticky left-0 z-10 bg-slate-700">#</th>
              <th class="text-center px-2 py-2.5 whitespace-nowrap w-8 sticky left-8 z-10 bg-slate-700">
                <button @click="toggleAllExpanded" class="flex items-center justify-center gap-1 hover:opacity-70 transition"
                  :title="allExpanded ? 'Colapsar todos' : 'Expandir todos'">
                  <span class="text-[12px]">{{ allExpanded ? '▲' : '▼' }}</span>
                </button>
              </th>
              <th class="text-left px-2 py-2.5 whitespace-nowrap sticky left-16 z-10 bg-slate-700">Vuelo</th>
              <th class="text-center px-2 py-2.5 whitespace-nowrap">Ruta</th>
              <th class="text-center px-2 py-2.5 whitespace-nowrap">Fecha</th>
              <th class="text-center px-2 py-2.5 whitespace-nowrap">Estado</th>
              <th class="text-center px-2 py-2.5 whitespace-nowrap w-16">ULDs</th>
              <th class="text-center px-2 py-2.5 whitespace-nowrap w-14">Pos</th>
              <th class="text-right px-2 py-2.5 whitespace-nowrap w-24">Gross</th>
              <th class="text-right px-2 py-2.5 whitespace-nowrap w-24">Tare</th>
              <th class="text-right px-2 py-2.5 whitespace-nowrap w-24">Neto</th>
              <th class="text-center px-2 py-2.5 whitespace-nowrap w-12">Docs</th>
              <th class="text-right px-2 py-2.5 whitespace-nowrap w-24 text-emerald-600">Payload</th>
              <!-- Commodity columns - dynamic based on filtered flights -->
              <th v-for="c in visibleCommodities" :key="c.type"
                class="text-right px-2 py-2.5 whitespace-nowrap w-20 text-[10px]"
                :style="{ background: c.color + '20', borderLeft: '1px solid ' + c.color + '40' }"
                :title="c.label">
                <div class="flex items-center justify-end gap-1">
                  <span class="w-1.5 h-1.5 rounded-full" :style="{ background: c.color }"></span>
                  <span class="font-mono">{{ c.short }}</span>
                </div>
              </th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="loading" class="h-32">
              <td :colspan="14 + visibleCommodities.length" class="text-center text-[12px] font-mono text-slate-400 ">Cargando datos...</td>
            </tr>
            <tr v-else-if="filteredFlights.length === 0" class="h-32">
              <td :colspan="14 + visibleCommodities.length" class="text-center text-[12px] font-mono text-slate-400 uppercase tracking-widest">No hay vuelos en este rango</td>
            </tr>
            <template v-for="(f, fi) in filteredFlights" :key="f.id">
              <tr class="border-b border-slate-100 transition-colors hover:bg-slate-50"
                :class="{ 'bg-slate-50/50': isExpanded(f.id) }">
                <td class="text-center px-2 py-2 text-slate-400 sticky left-0 z-10 bg-white">{{ fi + 1 }}</td>
                <td class="text-center px-2 py-2 sticky left-8 z-10 bg-white">
                  <button @click="toggleExpand(f.id)"
                    class="flex items-center justify-center w-6 h-6 rounded hover:bg-slate-200 transition text-slate-500 hover:text-slate-900"
                    :aria-expanded="isExpanded(f.id)"
                    :title="isExpanded(f.id) ? 'Colapsar detalle' : 'Expandir detalle'">
                    <span class="text-[10px] transition-transform duration-200" :style="{ transform: isExpanded(f.id) ? 'rotate(180deg)' : '' }">▼</span>
                  </button>
                </td>
                <td class="px-2 py-2 font-mono text-slate-950 sticky left-16 z-10 bg-white">UPS-{{ f.flightNumber }}</td>
                <td class="text-center px-2 py-2 text-slate-700">{{ f.origin }}→{{ f.destination }}</td>
                <td class="text-center px-2 py-2 text-slate-500">{{ f.flightDate }}</td>
                <td class="text-center px-2 py-2">
                  <span class="inline-flex items-center gap-1">
                    <span :class="getStatusDot(f.status)" class="inline-block w-2 h-2 rounded-full"></span>
                    <span class="px-1.5 py-0.5 rounded text-[10px] font-medium" :style="statusStyle(f.status)">{{ statusLabel(f.status) }}</span>
                  </span>
                </td>
                <td class="text-center px-2 py-2 font-mono text-slate-900">{{ flightUlds(f.id).length }}</td>
                <td class="text-center px-2 py-2 font-mono text-slate-600">{{ flightPositions(f.id) }}</td>
                <td class="text-right px-2 py-2 font-mono text-slate-950">{{ grossLbs(f.id) }}</td>
                <td class="text-right px-2 py-2 font-mono text-slate-600">{{ totalTareLbs(f.id) }}</td>
                <td class="text-right px-2 py-2 font-mono text-slate-900">{{ netLbs(f.id) }}</td>
                <td class="text-center px-2 py-2 text-slate-400 font-mono">5</td>
                <td class="text-right px-2 py-2 font-bold text-emerald-700" style="font-family: 'SF Mono', 'Fira Code', monospace;">{{ payloadLbs(f.id) }}</td>
                <!-- Commodity payload columns -->
                <td v-for="c in visibleCommodities" :key="c.type"
                  class="text-right px-2 py-2 font-mono text-slate-900 tabular-nums"
                  :style="{ background: c.color + '08' }"
                  :title="commodityTooltip(f.id, c.type)">
                  {{ commodityPayload(f.id, c.type) || '—' }}
                </td>
              </tr>

              <!-- Drill-down row -->
              <tr v-show="isExpanded(f.id)" class="bg-slate-50/30 border-t border-slate-200">
                <td :colspan="14 + visibleCommodities.length" class="p-0">
                  <div class="p-3 md:p-4 border-t border-slate-200" style="animation: slideDown 0.2s ease-out;">
                    <FlightDetail :flight="f" :flight-id="f.id" />
                  </div>
                </td>
              </tr>
            </template>

            <!-- Totals row -->
            <tr class="bg-slate-50 border-t-2 border-slate-300 font-bold">
              <td class="text-center px-2 py-2 text-slate-400">Σ</td>
              <td class="text-center px-2 py-2"></td>
              <td class="px-2 py-2 text-slate-500">TOTAL</td>
              <td class="text-center px-2 py-2"></td>
              <td class="text-center px-2 py-2"></td>
              <td class="text-center px-2 py-2"></td>
              <td class="text-center px-2 py-2">{{ totalUldsCount }}</td>
              <td class="text-center px-2 py-2">{{ totalPositionsAll }}</td>
              <td class="text-right px-2 py-2">{{ totalGrossAll }}</td>
              <td class="text-right px-2 py-2">{{ totalTareAll }}</td>
              <td class="text-right px-2 py-2">{{ totalNetAll }}</td>
              <td class="text-center px-2 py-2">{{ filteredFlights.length * 5 }}</td>
              <td class="text-right px-2 py-2 text-emerald-700">{{ totalNetPayload }}</td>
              <td v-for="c in visibleCommodities" :key="c.type"
                class="text-right px-2 py-2 text-slate-900 tabular-nums"
                :style="{ background: c.color + '15' }">
                {{ totalCommodityPayload(c.type) }}
              </td>
            </tr>
          </tbody>
        </table>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useAppStore } from '../stores/app'
import { downloadCSV } from '../utils/csv'
import FlightDetail from '../components/FlightDetail.vue'

const appStore = useAppStore()

const dateFrom = ref('')
const dateTo = ref('')
const loading = ref(false)
const expandedFlights = ref(new Set())

const filteredFlights = computed(() => {
  let list = appStore.flights
  if (dateFrom.value) {
    list = list.filter(f => f.flightDate >= dateFrom.value)
  }
  if (dateTo.value) {
    list = list.filter(f => f.flightDate <= dateTo.value)
  }
  return list
})

const allUlDs = computed(() => appStore.ulds)
const allMawbs = computed(() => appStore.mawbs)

function flightUlds(flightId) {
  return allUlDs.value.filter(u => u.flightId === flightId)
}

function flightMawbs(flightId) {
  return allMawbs.value.filter(m => m.flightId === flightId)
}

function flightPositions(flightId) {
  const ulds = flightUlds(flightId)
  return new Set(ulds.map(u => u.position).filter(Boolean)).size
}

function grossLbs(flightId) {
  const ulds = flightUlds(flightId)
  return ulds.reduce((s, u) => s + (Number(u.grossWeightLbs) || 0), 0)
}

function isBellyPosition(position) {
  if (!position) return false
  const p = position.toString().trim().toUpperCase()
  return p === '31' || p === '34' || p === 'AB' || p === 'LOOSE' || p === 'BULK' || p.includes('BELLY')
}

function totalTareLbs(flightId) {
  const ulds = flightUlds(flightId)
  return ulds.reduce((s, u) => s + (Number(u.tareLbs) || 0), 0)
}

function bellyTareLbs(flightId) {
  const ulds = flightUlds(flightId)
  return ulds
    .filter(u => isBellyPosition(u.position))
    .reduce((s, u) => s + (Number(u.tareLbs) || 0), 0)
}

function netLbs(flightId) {
  return grossLbs(flightId) - totalTareLbs(flightId)
}

function payloadLbs(flightId) {
  return grossLbs(flightId) - bellyTareLbs(flightId) + 5
}

// ── Commodity definitions & ordering ──────────────────────────
const COMMODITY_ORDER = [
  'PERISHABLE', 'DRY_CARGO', 'ELECTRONICS', 'HIGH_VALUES', 'CIGARETTES',
  'SMALL_PACKAGES', 'WWEF', 'LIVE_PLANTS', 'GENERAL', 'COMAT', 'FCC',
  'EMPTY_ULD', 'EMPTY_PALLET', 'RED_TAG', 'EMPTY_BAGS', 'NETS',
  'SDQ_SDF', 'SDQ_MIA'
]

const COMMODITY_MAP = {
  PERISHABLE:       { label: 'PERISHABLE',        short: 'PER',  color: '#ef4444' },
  DRY_CARGO:        { label: 'DRY CARGO',         short: 'DRY',  color: '#64748b' },
  ELECTRONICS:      { label: 'ELECTRONICS',       short: 'ELEC', color: '#8b5cf6' },
  HIGH_VALUES:      { label: 'HIGH VALUES',       short: 'HIGH', color: '#f59e0b' },
  CIGARETTES:       { label: 'CIGARETTES',        short: 'CIG',  color: '#78716c' },
  SMALL_PACKAGES:   { label: 'SMALL PACKAGES',    short: 'SMP',  color: '#06b6d4' },
  WWEF:             { label: 'WWEF',              short: 'WWEF', color: '#ec4899' },
  LIVE_PLANTS:      { label: 'LIVE PLANTS',       short: 'PLNT', color: '#22c55e' },
  GENERAL:          { label: 'GENERAL',           short: 'GEN',  color: '#94a3b8' },
  COMAT:            { label: 'COMAT',             short: 'COMT', color: '#a3a3a3' },
  FCC:              { label: 'FCC',               short: 'FCC',  color: '#78716c' },
  EMPTY_ULD:        { label: 'EMPTY ULD',         short: 'EMP',  color: '#d1d5db' },
  EMPTY_PALLET:     { label: 'EMPTY PALLET',      short: 'EPT',  color: '#d1d5db' },
  RED_TAG:          { label: 'RED TAG',           short: 'RED',  color: '#dc2626' },
  EMPTY_BAGS:       { label: 'EMPTY BAGS',        short: 'BAG',  color: '#a3a3a3' },
  NETS:             { label: 'NETS',              short: 'NET',  color: '#52525b' },
  SDQ_SDF:          { label: 'SDQ-SDF',           short: 'SDQ',  color: '#2563eb' },
  SDQ_MIA:          { label: 'SDQ-MIA',           short: 'MIA',  color: '#2563eb' },
}

// Commodity payload per flight (computed from MAWBs)
function commodityPayload(flightId, commodityType) {
  const mawbs = flightMawbs(flightId)
  const totalLbs = mawbs
    .filter(m => m.commodityType === commodityType)
    .reduce((s, m) => s + (Number(m.chargeableWeightKg || m.reportedWeightKg || 0) * 2.20462), 0)
  return totalLbs > 0 ? Math.round(totalLbs) : null
}

function commodityTooltip(flightId, commodityType) {
  const mawbs = flightMawbs(flightId)
  const items = mawbs.filter(m => m.commodityType === commodityType)
  if (!items.length) return `${COMMODITY_MAP[commodityType]?.label || commodityType}: 0 lbs`
  const totalLbs = items.reduce((s, m) => s + (Number(m.chargeableWeightKg || m.reportedWeightKg || 0) * 2.20462), 0)
  const totalPcs = items.reduce((s, m) => s + Number(m.pieces || 0), 0)
  const awbNumbers = items.map(m => m.awbNumber).join(', ')
  return `${COMMODITY_MAP[commodityType]?.label || commodityType}: ${Math.round(totalLbs)} lbs (${totalPcs} pcs) • ${awbNumbers}`
}

// Visible commodities = those with payload > 0 in ANY filtered flight
const visibleCommodities = computed(() => {
  const activeTypes = new Set()
  filteredFlights.value.forEach(f => {
    flightMawbs(f.id).forEach(m => {
      const type = m.commodityType || 'DRY_CARGO'
      const weight = Number(m.chargeableWeightKg || m.reportedWeightKg || 0) * 2.20462
      if (weight > 0) activeTypes.add(type)
    })
  })
  return COMMODITY_ORDER
    .filter(t => activeTypes.has(t))
    .map(t => ({ type: t, ...COMMODITY_MAP[t] }))
})

// Table min-width for horizontal scroll
const tableMinWidth = computed(() => {
  const base = 1100 // fixed columns
  const commodityCols = visibleCommodities.value.length * 80 // 80px per commodity col
  return base + commodityCols
})

// Totals
const totalNetPayload = computed(() => {
  return filteredFlights.value.reduce((s, f) => s + payloadLbs(f.id), 0)
})

const totalUldsCount = computed(() => {
  return filteredFlights.value.reduce((s, f) => s + flightUlds(f.id).length, 0)
})

const totalMawbsCount = computed(() => {
  return filteredFlights.value.reduce((s, f) => s + flightMawbs(f.id).length, 0)
})

const totalPositionsAll = computed(() => {
  return filteredFlights.value.reduce((s, f) => s + flightPositions(f.id), 0)
})

const totalGrossAll = computed(() => {
  return filteredFlights.value.reduce((s, f) => s + grossLbs(f.id), 0)
})

const totalTareAll = computed(() => {
  return filteredFlights.value.reduce((s, f) => s + totalTareLbs(f.id), 0)
})

const totalNetAll = computed(() => {
  return filteredFlights.value.reduce((s, f) => s + netLbs(f.id), 0)
})

function totalCommodityPayload(commodityType) {
  const total = filteredFlights.value.reduce((s, f) => s + (commodityPayload(f.id, commodityType) || 0), 0)
  return total > 0 ? total : '—'
}

// Expand logic
const allExpanded = computed(() => {
  return filteredFlights.value.length > 0 && filteredFlights.value.every(f => expandedFlights.value.has(f.id))
})

function toggleExpand(flightId) {
  if (expandedFlights.value.has(flightId)) {
    expandedFlights.value.delete(flightId)
  } else {
    expandedFlights.value.add(flightId)
  }
}

function toggleAllExpanded() {
  if (allExpanded.value) {
    expandedFlights.value.clear()
  } else {
    filteredFlights.value.forEach(f => expandedFlights.value.add(f.id))
  }
}

function isExpanded(flightId) {
  return expandedFlights.value.has(flightId)
}

function descargarReporte() {
  // Build headers: fixed + commodity columns
  const fixedHeaders = ['Flight Number', 'Route', 'Date', 'Status', 'ULD Count', 'Positions', 'Gross Lbs', 'Tare Lbs', 'Net Lbs', 'Payload Lbs']
  const commodityHeaders = visibleCommodities.value.map(c => c.short)
  const headers = [...fixedHeaders, ...commodityHeaders]

  const rows = filteredFlights.value.map(f => {
    const fixed = [
      `UPS-${f.flightNumber}`,
      `${f.origin}→${f.destination}`,
      f.flightDate || '',
      statusLabel(f.status),
      flightUlds(f.id).length,
      flightPositions(f.id),
      grossLbs(f.id),
      totalTareLbs(f.id),
      netLbs(f.id),
      payloadLbs(f.id),
    ]
    const commodityVals = visibleCommodities.value.map(c => commodityPayload(f.id, c.type) || '')
    return [...fixed, ...commodityVals]
  })
  downloadCSV(headers, rows, `reporte-vuelos-${new Date().toISOString().slice(0, 10)}.csv`)
}

function getStatusDot(status) {
  if (status === 'SCHEDULED') return 'bg-slate-300'
  if (status === 'BOARDING') return 'bg-slate-400'
  if (status === 'DEPARTED') return 'bg-slate-600'
  if (status === 'ARRIVED') return 'bg-slate-800'
  if (status === 'CANCELLED') return 'bg-slate-200'
  if (status === 'DELAYED') return 'bg-slate-400'
  return 'bg-slate-200'
}

function statusStyle(status) {
  const map = {
    SCHEDULED: { background: '#e2e8f0', color: '#475569' },
    BOARDING: { background: '#e2e8f0', color: '#475569' },
    DEPARTED: { background: '#94a3b8', color: '#fff' },
    ARRIVED: { background: '#1e293b', color: '#fff' },
    CANCELLED: { background: '#f1f5f9', color: '#94a3b8' },
    DELAYED: { background: '#fef08a', color: '#854d0e' },
  }
  return map[status] || { background: '#e2e8f0', color: '#475569' }
}

function statusLabel(status) {
  const map = {
    SCHEDULED: 'SCH',
    BOARDING: 'BRD',
    DEPARTED: 'DPT',
    ARRIVED: 'ARR',
    CANCELLED: 'CNL',
    DELAYED: 'DLY',
  }
  return map[status] || status?.slice(0, 3) || '—'
}

onMounted(async () => {
  loading.value = true
  await appStore.loadFlights()
  if (appStore.flights.length) {
    await Promise.all([
      appStore.loadUlds(),
      appStore.loadAllMawbs(),
    ])
  }
  loading.value = false
})
</script>

<style scoped>
.scrollbar-none::-webkit-scrollbar { display: none; }
.scrollbar-none { -ms-overflow-style: none; scrollbar-width: none; }

@keyframes slideDown {
  from { opacity: 0; transform: translateY(-4px); }
  to { opacity: 1; transform: translateY(0); }
}

tr[v-show="true"] td > div {
  animation: slideDown 0.2s ease-out;
}
</style>