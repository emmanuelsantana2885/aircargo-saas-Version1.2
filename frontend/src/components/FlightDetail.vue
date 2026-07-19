<template>
  <div class="space-y-3 md:space-y-4">
    <!-- Header with MAWBs summary -->
    <div class="flex flex-col md:flex-row md:items-center md:justify-between gap-2 p-3 bg-white rounded border border-slate-200">
      <div class="flex items-center gap-3 flex-wrap">
        <span class="text-[11px] font-mono font-bold text-slate-500 uppercase tracking-wider">MAWBs en vuelo</span>
        <template v-for="(mawb, idx) in flightMawbs" :key="idx">
          <div class="flex items-center gap-1 px-2 py-1 bg-slate-50 rounded border border-slate-200 hover:border-slate-300 transition">
            <span class="text-[10px] font-mono font-medium text-slate-700">{{ mawb.awbNumber || '—' }}</span>
            <span class="text-[9px] text-slate-400">|</span>
            <span class="text-[10px] font-mono text-slate-600">{{ mawb.pieces || 0 }} pcs</span>
            <span class="text-[9px] text-slate-400">|</span>
            <span class="text-[10px] font-mono text-emerald-700">{{ mawb.chargeableWeightKg ? (mawb.chargeableWeightKg * 2.20462).toFixed(0) : '—' }} lbs</span>
            <span class="text-[9px] text-slate-400">|</span>
            <span class="text-[9px] px-1.5 py-0.5 rounded text-slate-600 bg-slate-100" :style="commodityChipStyle(mawb.commodityType)">
              {{ commodityShort(mawb.commodityType) }}
            </span>
          </div>
        </template>
        <span v-if="flightMawbs.length === 0" class="text-[11px] text-slate-400 font-mono">Sin MAWBs asignados</span>
      </div>

      <div class="flex items-center gap-4 text-[11px] font-mono text-slate-500">
        <span>ULDs: <strong class="text-slate-900">{{ flightUlds.length }}</strong></span>
        <span>Pos: <strong class="text-slate-900">{{ flightPositions }}</strong></span>
        <span>Gross: <strong class="text-slate-900">{{ grossLbs }} lbs</strong></span>
        <span>Net: <strong class="text-slate-900">{{ netLbs }} lbs</strong></span>
        <span>Payload: <strong class="text-emerald-700">{{ payloadLbs }} lbs</strong></span>
      </div>
    </div>

    <!-- Commodity Breakdown Horizontal Bar Chart -->
    <section class="p-3 bg-white rounded border border-slate-200">
      <div class="flex items-center justify-between mb-2">
        <h4 class="text-[10px] font-mono font-bold uppercase tracking-wider text-slate-500">Desglose por Commodity (lbs)</h4>
        <span class="text-[10px] font-mono text-slate-400">Total: {{ totalCommodityWeight }} lbs</span>
      </div>
      <div class="space-y-2" v-if="commodityBreakdown.length">
        <div v-for="(c, idx) in commodityBreakdown" :key="idx" class="group">
          <div class="flex items-center gap-2 mb-1">
            <span class="w-2 h-2 rounded-full" :style="{ background: c.color }"></span>
            <span class="text-[10px] font-mono text-slate-600 w-16">{{ c.shortLabel }}</span>
            <span class="text-[10px] font-mono text-slate-900 font-medium tabular-nums">{{ c.weight.toFixed(0) }} lbs</span>
            <span class="text-[9px] text-slate-400">({{ c.pieces }} pcs)</span>
          </div>
          <div class="h-3 bg-slate-100 rounded-full overflow-hidden ml-6">
            <div class="h-full rounded-full transition-all duration-500" :style="{ width: c.pct + '%', background: c.color }"></div>
          </div>
        </div>
      </div>
      <div v-else class="text-center py-4 text-[11px] text-slate-400 font-mono">Sin datos de commodity</div>
    </section>

    <!-- ULD Detail Table -->
    <section class="p-3 bg-white rounded border border-slate-200 overflow-x-auto">
      <h4 class="text-[10px] font-mono font-bold uppercase tracking-wider text-slate-500 mb-2">ULDs en este vuelo</h4>
      <table class="w-full text-[10px] font-mono border-collapse" style="min-width: 700px;">
        <thead>
          <tr class="bg-slate-50 border-b border-slate-200 text-slate-500 uppercase tracking-wider">
            <th class="px-2 py-1.5 text-left">Pos</th>
            <th class="px-2 py-1.5 text-left">ULD</th>
            <th class="px-2 py-1.5 text-left">Tipo</th>
            <th class="px-2 py-1.5 text-right">Gross lbs</th>
            <th class="px-2 py-1.5 text-right">Tare lbs</th>
            <th class="px-2 py-1.5 text-right">Net lbs</th>
            <th class="px-2 py-1.5 text-left">Contenido (MAWBs)</th>
            <th class="px-2 py-1.5 text-center">Seal</th>
            <th class="px-2 py-1.5 text-center">Estado</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="u in flightUlds" :key="u.id" class="border-b border-slate-100 hover:bg-slate-50/50">
            <td class="px-2 py-1.5 text-slate-400">{{ u.position || '—' }}</td>
            <td class="px-2 py-1.5 font-medium text-slate-900">{{ u.uldNumber }}</td>
            <td class="px-2 py-1.5 text-slate-600">{{ u.uldType || inferUldType(u.uldNumber) }}</td>
            <td class="px-2 py-1.5 text-right tabular-nums">{{ u.grossWeightLbs || 0 }}</td>
            <td class="px-2 py-1.5 text-right tabular-nums text-slate-500">{{ u.tareLbs || 0 }}</td>
            <td class="px-2 py-1.5 text-right tabular-nums font-medium">{{ (u.grossWeightLbs || 0) - (u.tareLbs || 0) }}</td>
            <td class="px-2 py-1.5 text-slate-600">
              <div v-if="uldMawbs(u.id).length" class="flex flex-wrap gap-1">
                <span v-for="m in uldMawbs(u.id)" :key="m.id"
                  class="px-1.5 py-0.5 text-[9px] font-mono rounded bg-slate-100 border border-slate-200 hover:bg-slate-200 transition"
                  :style="commodityChipStyle(m.commodityType)"
                  :title="`${m.awbNumber} • ${m.pieces} pcs • ${m.commodityType || 'DRY_CARGO'}`">
                  {{ m.awbNumber }}
                </span>
              </div>
              <span v-else class="text-slate-300 italic">Vacío</span>
            </td>
            <td class="px-2 py-1.5 text-center text-slate-500 font-mono">{{ u.sealNumber || '—' }}</td>
            <td class="px-2 py-1.5 text-center">
              <span class="px-1.5 py-0.5 rounded text-[9px] font-medium" :style="uldStatusStyle(u.status)">
                {{ uldStatusLabel(u.status) }}
              </span>
            </td>
          </tr>
        </tbody>
        <tbody v-if="flightUlds.length === 0">
          <tr>
            <td colspan="9" class="text-center py-4 text-slate-400 font-mono text-[11px]">No hay ULDs asignados a este vuelo</td>
          </tr>
        </tbody>
      </table>
    </section>

    <!-- Equipment Summary -->
    <section class="p-3 bg-white rounded border border-slate-200">
      <h4 class="text-[10px] font-mono font-bold uppercase tracking-wider text-slate-500 mb-2">Equipos y Configuración</h4>
      <div class="grid grid-cols-2 md:grid-cols-4 gap-2 text-[10px]">
        <div class="bg-slate-50 p-2 rounded border border-slate-200">
          <div class="text-slate-400">PMC (Main Deck)</div>
          <div class="font-mono font-bold text-slate-900">{{ equipmentCounts.PMC }}</div>
        </div>
        <div class="bg-slate-50 p-2 rounded border border-slate-200">
          <div class="text-slate-400">PAH/PAG (Contoured)</div>
          <div class="font-mono font-bold text-slate-900">{{ equipmentCounts.PAH + equipmentCounts.PAG }}</div>
        </div>
        <div class="bg-slate-50 p-2 rounded border border-slate-200">
          <div class="text-slate-400">AAY/AAZ (LD3)</div>
          <div class="font-mono font-bold text-slate-900">{{ equipmentCounts.AAY + equipmentCounts.AAZ }}</div>
        </div>
        <div class="bg-slate-50 p-2 rounded border border-slate-200">
          <div class="text-slate-400">Otros / Empty</div>
          <div class="font-mono font-bold text-slate-900">{{ equipmentCounts.OTHER }}</div>
        </div>
      </div>
    </section>

    <!-- Volume / Utilization -->
    <section class="p-3 bg-white rounded border border-slate-200">
      <h4 class="text-[10px] font-mono font-bold uppercase tracking-wider text-slate-500 mb-2">Utilización & Volumen</h4>
      <div class="grid grid-cols-2 md:grid-cols-4 gap-2 text-[10px]">
        <div class="bg-slate-50 p-2 rounded border border-slate-200">
          <div class="text-slate-400">Posiciones Main Deck</div>
          <div class="font-mono font-bold text-slate-900">{{ mainDeckPositions }} / {{ maxMainDeckPositions }}</div>
        </div>
        <div class="bg-slate-50 p-2 rounded border border-slate-200">
          <div class="text-slate-400">Utilización MD</div>
          <div class="font-mono font-bold text-slate-900">{{ mdUtilization }}%</div>
        </div>
        <div class="bg-slate-50 p-2 rounded border border-slate-200">
          <div class="text-slate-400">Posiciones Belly</div>
          <div class="font-mono font-bold text-slate-900">{{ bellyPositions }}</div>
        </div>
        <div class="bg-slate-50 p-2 rounded border border-slate-200">
          <div class="text-slate-400">Volumen Total</div>
          <div class="font-mono font-bold text-slate-900">{{ totalVolume }} ft³</div>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup>
import { computed, ref } from 'vue'
import { useAppStore } from '../stores/app'

const props = defineProps({
  flight: { type: Object, required: true },
  flightId: { type: String, required: true },
})

const appStore = useAppStore()

// ── Data Access ──────────────────────────────────────────────
const flightUlds = computed(() => appStore.ulds.filter(u => u.flightId === props.flightId))
const flightMawbs = computed(() => appStore.mawbs.filter(m => m.flightId === props.flightId))

function uldMawbs(uldId) {
  const links = appStore.uldAwbs?.filter?.(l => l.uldId === uldId) || []
  return links.map(l => appStore.mawbs.find(m => m.id === l.mawbLabel)).filter(Boolean)
}

// ── Helpers ──────────────────────────────────────────────────
function inferUldType(uldNumber) {
  const code = (uldNumber || '').toUpperCase().substring(0, 3)
  const map = { PMC: 'PMC', PAH: 'PAH', PAG: 'PAG', PAJ: 'PAJ', AAY: 'AAY', AAZ: 'AAZ', AAD: 'AAD', PIP: 'PIP', AMP: 'AMP', AMJ: 'AMJ' }
  return map[code] || 'UNK'
}

// ── Commodity Breakdown ──────────────────────────────────────
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

const commodityBreakdown = computed(() => {
  const map = {}
  flightMawbs.value.forEach(m => {
    const type = m.commodityType || 'DRY_CARGO'
    const info = COMMODITY_MAP[type] || COMMODITY_MAP.DRY_CARGO
    const weight = Number(m.chargeableWeightKg || m.reportedWeightKg || 0) * 2.20462
    const pieces = Number(m.pieces || 0)
    if (!map[type]) {
      map[type] = { type, label: info.label, shortLabel: info.short, color: info.color, weight: 0, pieces: 0 }
    }
    map[type].weight += weight
    map[type].pieces += pieces
  })
  const arr = Object.values(map).filter(c => c.weight > 0 || c.pieces > 0).sort((a, b) => b.weight - a.weight)
  const total = arr.reduce((s, c) => s + c.weight, 0)
  return arr.map(c => ({ ...c, pct: total > 0 ? (c.weight / total) * 100 : 0 }))
})

const totalCommodityWeight = computed(() =>
  commodityBreakdown.value.reduce((s, c) => s + c.weight, 0).toFixed(0)
)

// ── Equipment Counts ─────────────────────────────────────────
const equipmentCounts = computed(() => {
  const counts = { PMC: 0, PAH: 0, PAG: 0, AAY: 0, AAZ: 0, OTHER: 0 }
  flightUlds.value.forEach(u => {
    const type = inferUldType(u.uldNumber)
    if (type === 'PMC') counts.PMC++
    else if (type === 'PAH') counts.PAH++
    else if (type === 'PAG') counts.PAG++
    else if (type === 'AAY') counts.AAY++
    else if (type === 'AAZ') counts.AAZ++
    else counts.OTHER++
  })
  return counts
})

// ── Utilization ──────────────────────────────────────────────
const maxMainDeckPositions = ref(20) // configurable per aircraft type

const mainDeckPositions = computed(() =>
  flightUlds.value.filter(u => !isBellyPosition(u.position)).length
)

const mdUtilization = computed(() =>
  maxMainDeckPositions.value > 0 ? Math.round((mainDeckPositions.value / maxMainDeckPositions.value) * 100) : 0
)

const bellyPositions = computed(() =>
  flightUlds.value.filter(u => isBellyPosition(u.position)).length
)

const totalVolume = computed(() =>
  flightUlds.value.reduce((s, u) => s + (Number(u.volumeCuFt) || estimateVolume(u.uldType)), 0)
)

function estimateVolume(type) {
  const vol = { PMC: 460, PAH: 400, PAG: 360, AAY: 155, AAZ: 155, AAD: 155, PIP: 240 }
  return vol[type] || 300
}

function isBellyPosition(position) {
  if (!position) return false
  const p = position.toString().trim().toUpperCase()
  return p === '31' || p === '34' || p === 'AB' || p === 'LOOSE' || p === 'BULK' || p.includes('BELLY')
}

// ── ULD Status ───────────────────────────────────────────────
function uldStatusStyle(status) {
  const map = {
    OPEN:      { background: '#e2e8f0', color: '#475569' },
    BUILT:     { background: '#dbeafe', color: '#1e40af' },
    SEALED:    { background: '#fef08a', color: '#854d0e' },
    LOADED:    { background: '#dcfce7', color: '#166534' },
    LEFT_BEHIND: { background: '#fee2e2', color: '#991b1b' },
  }
  return map[status] || { background: '#f1f5f9', color: '#64748b' }
}

function uldStatusLabel(status) {
  const map = { OPEN: 'OPN', BUILT: 'BUILT', SEALED: 'SEAL', LOADED: 'LDD', LEFT_BEHIND: 'LFT' }
  return map[status] || status?.slice(0, 3) || '—'
}

function commodityShort(type) {
  const info = COMMODITY_MAP[type] || COMMODITY_MAP.DRY_CARGO
  return info.short
}

function commodityChipStyle(type) {
  const info = COMMODITY_MAP[type] || COMMODITY_MAP.DRY_CARGO
  return {
    background: info.color + '15',
    borderColor: info.color + '40',
    color: info.color,
  }
}

// ── KPIs ─────────────────────────────────────────────────────
const grossLbs = computed(() =>
  flightUlds.value.reduce((s, u) => s + (Number(u.grossWeightLbs) || 0), 0)
)

const totalTareLbs = computed(() =>
  flightUlds.value.reduce((s, u) => s + (Number(u.tareLbs) || 0), 0)
)

const bellyTareLbs = computed(() =>
  flightUlds.value
    .filter(u => isBellyPosition(u.position))
    .reduce((s, u) => s + (Number(u.tareLbs) || 0), 0)
)

const netLbs = computed(() => grossLbs.value - totalTareLbs.value)
const payloadLbs = computed(() => grossLbs.value - bellyTareLbs.value)

const flightPositions = computed(() =>
  new Set(flightUlds.value.map(u => u.position).filter(Boolean)).size
)
</script>

<style scoped>
/* minimal, handled via inline styles */
</style>