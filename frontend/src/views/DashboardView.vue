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
        <table class="w-full border-collapse text-[11px] font-mono" style="min-width: 1100px">
          <thead class="sticky top-0 z-20">
            <tr class="bg-slate-700 text-white text-[11px] font-bold uppercase tracking-wider border-b border-slate-500 shadow-sm">
              <th class="text-center px-2 py-2.5 whitespace-nowrap w-8">#</th>
              <th class="text-center px-2 py-2.5 whitespace-nowrap w-8">
                <button @click="toggleAllExpanded" class="flex items-center justify-center gap-1 hover:opacity-70 transition"
                  :title="allExpanded ? 'Colapsar todos' : 'Expandir todos'">
                  <span class="text-[12px]">{{ allExpanded ? '▲' : '▼' }}</span>
                </button>
              </th>
              <th class="text-left px-2 py-2.5 whitespace-nowrap">Vuelo</th>
              <th class="text-center px-2 py-2.5 whitespace-nowrap">Ruta</th>
              <th class="text-center px-2 py-2.5 whitespace-nowrap">Fecha</th>
              <th class="text-center px-2 py-2.5 whitespace-nowrap">Estado</th>
              <th class="text-center px-2 py-2.5 whitespace-nowrap">ULDs</th>
              <th class="text-center px-2 py-2.5 whitespace-nowrap">Pos</th>
              <th class="text-right px-2 py-2.5 whitespace-nowrap">Gross Lbs</th>
              <th class="text-right px-2 py-2.5 whitespace-nowrap">Tare Lbs</th>
              <th class="text-right px-2 py-2.5 whitespace-nowrap">Neto Lbs</th>
              <th class="text-center px-2 py-2.5 whitespace-nowrap">Docs</th>
              <th class="text-right px-2 py-2.5 whitespace-nowrap text-emerald-600">Payload Lbs</th>
              <th class="text-center px-2 py-2.5 whitespace-nowrap">Commodities</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="loading" class="h-32">
              <td colspan="15" class="text-center text-[12px] font-mono text-slate-400 ">Cargando datos...</td>
            </tr>
            <tr v-else-if="filteredFlights.length === 0" class="h-32">
              <td colspan="15" class="text-center text-[12px] font-mono text-slate-400 uppercase tracking-widest">No hay vuelos en este rango</td>
            </tr>
            <template v-for="(f, fi) in filteredFlights" :key="f.id">
              <tr class="border-b border-slate-100 transition-colors hover:bg-slate-50"
                :class="{ 'bg-slate-50/50': isExpanded(f.id) }">
                <td class="text-center px-2 py-2 text-slate-400">{{ fi + 1 }}</td>
                <td class="text-center px-2 py-2">
                  <button @click="toggleExpand(f.id)"
                    class="flex items-center justify-center w-6 h-6 rounded hover:bg-slate-200 transition text-slate-500 hover:text-slate-900"
                    :aria-expanded="isExpanded(f.id)"
                    :title="isExpanded(f.id) ? 'Colapsar detalle' : 'Expandir detalle'">
                    <span class="text-[10px] transition-transform duration-200" :style="{ transform: isExpanded(f.id) ? 'rotate(180deg)' : '' }">▼</span>
                  </button>
                </td>
                <td class="px-2 py-2 font-mono text-slate-950">UPS-{{ f.flightNumber }}</td>
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
                <td class="text-center px-2 py-2">
                  <CommodityChips :flight-id="f.id" :max-visible="3" />
                </td>
              </tr>

              <!-- Drill-down row -->
              <tr v-show="isExpanded(f.id)" class="bg-slate-50/30 border-t border-slate-200">
                <td colspan="15" class="p-0">
                  <div class="p-3 md:p-4 border-t border-slate-200" style="animation: slideDown 0.2s ease-out;">
                    <FlightDetail :flight="f" :flight-id="f.id" />
                  </div>
                </td>
              </tr>
            </template>
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
import CommodityChips from '../components/CommodityChips.vue'
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

const totalNetPayload = computed(() => {
  return filteredFlights.value.reduce((s, f) => s + payloadLbs(f.id), 0)
})

const totalUldsCount = computed(() => {
  return filteredFlights.value.reduce((s, f) => s + flightUlds(f.id).length, 0)
})

const totalMawbsCount = computed(() => {
  return filteredFlights.value.reduce((s, f) => s + flightMawbs(f.id).length, 0)
})

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
  const headers = ['Flight Number', 'Route', 'Date', 'Status', 'ULD Count', 'Positions', 'Gross Lbs', 'Tare Lbs', 'Net Lbs', 'Payload Lbs']
  const rows = filteredFlights.value.map(f => [
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
  ])
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