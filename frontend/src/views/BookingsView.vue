<template>
  <div class="ds-page">

    <header class="ds-section-header">
      <div class="flex flex-wrap items-center gap-3 md:gap-4">
        <div>
          <h1 class="ds-title">Bookings Hub</h1>
          <p class="ds-subtitle">SDQ Control Desk</p>
        </div>
        <div class="ds-divider"></div>
        <div class="flex flex-col gap-0.5">
          <span class="text-[13px] font-black text-slate-950 uppercase tracking-widest">Vuelo</span>
          <select v-model="localFlightId" @change="onFlightChange"
            class="bg-slate-100 border border-slate-300 rounded px-3 py-1.5 font-black text-slate-950 focus:outline-none uppercase tracking-widest text-[14px] cursor-pointer min-w-[160px]">
            <option value="" disabled>Seleccionar vuelo</option>
            <option v-for="flight in flightList" :key="flight.id" :value="flight.id">
              {{ airlineCodeById(flight.airlineId) }}-{{ flight.flightNumber }} ({{ flight.origin }}→{{ flight.destination }}) — {{ flight.flightDate }}
            </option>
          </select>
        </div>
        <div v-if="store.selectedFlight" class="flex gap-3 text-[13px] font-mono font-bold text-slate-700">
          <span>{{ store.selectedFlight.aircraftReg || '—' }}</span>
          <span>{{ store.selectedFlight.flightDate }}</span>
        </div>
        <div class="relative">
          <button @click.stop="showStatusFilter = !showStatusFilter"
            class="flex items-center gap-1.5 px-2.5 py-1.5 rounded border font-mono uppercase tracking-wider text-[14px] transition"
            :class="statusFilter ? 'bg-slate-50 border-slate-400 text-slate-800' : 'bg-white border-slate-300 text-slate-600 hover:bg-slate-100'">
            <span class="inline-block w-2 h-2 rounded-full" :class="statusFilter ? statusFilterColor(statusFilter) : 'bg-slate-400'"></span>
            {{ statusFilterLabel }}
            <span v-if="statusFilter" @click.stop="statusFilter = ''" class="ml-0.5 text-[14px] hover:text-slate-600">✕</span>
          </button>
          <div v-if="showStatusFilter" class="absolute top-full left-0 mt-1 bg-white border border-slate-300 rounded shadow-lg z-10 min-w-[140px]">
            <div @click="statusFilter = ''; showStatusFilter = false" class="px-3 py-1.5 text-[14px] font-mono cursor-pointer hover:bg-slate-100 text-slate-500" :class="!statusFilter ? 'bg-slate-50 font-bold' : ''">Todos</div>
            <div v-for="opt in statusOptions" :key="opt.value" @click="statusFilter = opt.value; showStatusFilter = false"
              class="px-3 py-1.5 text-[14px] font-mono cursor-pointer hover:bg-slate-100 flex items-center gap-2"
              :class="statusFilter === opt.value ? 'bg-slate-50 font-bold' : ''">
              <span class="inline-block w-2 h-2 rounded-full" :class="opt.colorClass"></span>
              {{ opt.label }}
            </div>
          </div>
        </div>
      </div>
      <div class="flex items-center gap-2">
        <button @click="triggerImport" class="ds-btn-secondary">
          <span class="text-[14px] font-semibold leading-none">↑</span> Import XLSX
        </button>
        <button @click="exportCSV" class="ds-btn-secondary">
          <span class="text-[14px] font-semibold leading-none">↓</span> Export CSV
        </button>
        <button @click="openCreate" class="ds-btn-primary">
          <span class="text-[14px] font-semibold leading-none">+</span> New Booking
        </button>
        <input type="file" ref="fileInput" @change="handleFileImport" accept=".xlsx,.xls" class="hidden" />
      </div>
    </header>

    <section class="ds-table-section">
      <div class="table-scroll-wrapper flex-1 min-h-0">
      <div class="ds-table-header" style="min-width: 900px">
        <div class="col-span-2 text-left">Booking ID</div>
        <div class="col-span-2 text-left">Vuelo / Fecha</div>
        <div class="col-span-2 text-left">Agente / Broker</div>
        <div class="col-span-2 text-left">Shipper <span class="text-slate-300 font-normal">(Recibo)</span></div>
        <div class="col-span-1 text-center">Piezas</div>
        <div class="col-span-1 text-right pr-2">Peso</div>
        <div class="col-span-1 text-center bg-slate-800 py-0.5 rounded border border-slate-600 text-white font-black tracking-wide">Estatus MAWB</div>
        <div class="col-span-1"></div>
      </div>

      <div v-if="store.loading && !store.bookings.length" class="flex-1 flex items-center justify-center">
        <span class="text-[14px] font-mono text-slate-950 ">Cargando bookings...</span>
      </div>

      <div v-else-if="deduplicatedBookings.length === 0" class="flex-1 flex items-center justify-center">
        <p class="text-[14px] font-mono text-slate-950 uppercase tracking-widest">{{ store.selectedFlightId ? 'No hay reservas para este vuelo' : 'No hay reservas. Crea una con el botón New Booking.' }}</p>
      </div>

      <div v-else class="divide-y divide-slate-100 text-[13px] text-slate-950 overflow-y-auto flex-1 min-h-0 scrollbar-none">
        <div v-for="b in deduplicatedBookings" :key="b.id"
          class="ds-table-row">

          <div class="col-span-2 font-mono font-black text-slate-950 relative z-10 text-[13px] flex items-center gap-2">
            <span>{{ b.awbNumber || b.id?.slice(0, 8) || 'N/A' }}</span>
            <span v-if="b._dupCount > 1" class="inline-flex items-center justify-center min-w-[18px] h-[18px] px-1 rounded-full bg-slate-100 text-slate-700 text-[12px] font-bold" title="Reservas duplicadas agrupadas">{{ b._dupCount }}x</span>
          </div>

          <div class="col-span-2 font-mono font-bold text-[18px] text-slate-950 relative z-10 flex flex-col leading-tight">
            <span>{{ flightNumber(b.flightId) || '—' }}</span>
            <span v-if="b.flightId" class="text-[13px] text-slate-500 font-semibold">{{ flightDate(b.flightId) }}</span>
          </div>

          <div class="col-span-2 text-slate-950 font-semibold relative z-10 truncate pr-3">
            {{ b.clientName || '—' }}
          </div>

          <div class="col-span-2 text-slate-900 font-bold relative z-10 truncate pr-2 font-mono text-[13px] flex flex-col leading-tight">
            <span>{{ bookingReceipt(b)?.shipperName || b.shipperName || '—' }}</span>
            <span v-if="bookingReceipt(b)" class="text-[13px] text-slate-600 font-semibold">&#10003; Recibido</span>
          </div>

          <div class="col-span-1 text-center font-mono font-bold text-slate-900 relative z-10">
            <span v-if="bookingReceipt(b)">{{ bookingReceipt(b).pieceCount || '—' }}</span>
            <span v-else>{{ b.skids || b.units || '—' }}</span>
          </div>

          <div class="col-span-1 text-right font-mono font-bold text-slate-950 relative z-10 pr-2">
            <template v-if="bookingReceipt(b)">
              {{ Number(bookingReceipt(b).chargeableWeightKg || bookingReceipt(b).actualWeightKg || 0).toLocaleString() }}<span class="text-[13px] text-slate-950 font-normal font-mono">k</span>
            </template>
            <template v-else>
              {{ b.reservedKg ? Number(b.reservedKg).toLocaleString() : '—' }}<span class="text-[13px] text-slate-950 font-normal font-mono">k</span>
            </template>
          </div>

          <div class="col-span-1 flex items-center justify-center gap-1.5 relative z-10">
            <div class="flex items-center gap-2 text-[17px] font-mono" :title="'MAWB: ' + getMawbStatus(b)">
              <span class="inline-block w-2.5 h-2.5" :class="getMawbStatusClass(b)"></span>
              <span class="px-1.5 py-0.5 rounded text-[12px] font-medium" style="background: var(--bg); color: var(--text)">{{ getMawbStatus(b) }}</span>
              <span v-if="getMawbStatus(b) !== '—'" class="text-slate-300 text-[13px]">·</span>
            </div>
          </div>
          <div class="col-span-1 flex justify-end relative z-10">
            <button @click.stop="removeBooking(b)"
              class="text-slate-400 hover:text-slate-600 transition-colors p-1"
              title="Eliminar booking">
              <IconTrash :size="15" :stroke-width="1.5" />
            </button>
          </div>
        </div>
      </div>
      </div>
    </section>
    <div v-if="showModal" class="ds-modal-backdrop" @click.self="closeModal">
      <div class="ds-modal-panel">
        <div class="ds-modal-header">
          <h2 class="ds-modal-title">Nuevo Booking</h2>
          <button @click="closeModal" class="text-slate-400 hover:text-slate-950 transition"><IconX :size="18" :stroke-width="2" /></button>
        </div>
          <div class="space-y-4">
          <div class="grid grid-cols-2 gap-4">
            <div>
              <label class="ds-label">Cliente *</label>
              <input v-model="form.clientName" type="text" placeholder="Nombre del agente" class="ds-input" />
            </div>
            <div>
              <label class="ds-label">Contacto *</label>
              <input v-model="form.contactName" type="text" placeholder="Persona de contacto" class="ds-input" />
            </div>
          </div>
          <div class="grid grid-cols-2 gap-4">
            <div>
              <label class="ds-label">Shipper</label>
              <input v-model="form.shipperName" type="text" placeholder="Nombre del shipper" class="ds-input" />
            </div>
            <div>
              <label class="ds-label">Consignee (CNEE)</label>
              <input v-model="form.cnee" type="text" placeholder="Nombre del consignatario"
                class="ds-input" />
            </div>
          </div>
          <div class="grid grid-cols-2 gap-4">
            <div>
              <label class="ds-label">Número MAWB</label>
              <input v-model="form.awbNumber" type="text" placeholder="UPS-XXX-XXXX"
                class="w-full text-[14px] font-mono px-4 py-2.5 rounded border border-slate-400 outline-none focus:border-slate-950 transition uppercase" />
            </div>
            <div>
              <label class="ds-label">Peso Reservado (kg) *</label>
              <input v-model.number="form.reservedKg" type="number" step="0.001"
                class="ds-input" />
            </div>
          </div>
          <div class="grid grid-cols-2 gap-4">
            <div>
              <label class="ds-label">Destino</label>
              <input v-model="form.destination" type="text" maxlength="3" placeholder="MIA"
                class="ds-input uppercase" />
            </div>
            <div class="grid grid-cols-2 gap-2">
              <div>
                <label class="ds-label">Skids</label>
                <input v-model.number="form.skids" type="number" min="0"
                  class="ds-input" />
              </div>
              <div>
                <label class="ds-label">Unidades</label>
                <input v-model.number="form.units" type="number" min="0"
                  class="ds-input" />
              </div>
            </div>
          </div>
          <div class="grid grid-cols-2 gap-4">
            <div>
              <label class="ds-label">Tipo de Commodity</label>
              <select v-model="form.commodityType"
                class="ds-input">
                <option v-for="c in commodityTypes" :key="c" :value="c">{{ c }}</option>
              </select>
            </div>
            <div>
              <label class="ds-label">Prioridad</label>
              <input v-model.number="form.priority" type="number" min="0" max="10"
                class="ds-input" />
            </div>
          </div>
          <div>
            <label class="ds-label">Notas</label>
            <textarea v-model="form.notes" rows="2" placeholder="Instrucciones especiales..."
              class="ds-input resize-none"></textarea>
          </div>
        </div>
        <div class="flex justify-end gap-2 mt-6 pt-4 border-t border-slate-200">
          <button @click="closeModal"
            class="ds-btn-secondary">
            Cancelar
          </button>
          <button @click="saveBooking" :disabled="saving"
            class="ds-btn-primary">
            <span>{{ saving ? 'Guardando...' : 'Crear Booking' }}</span>
          </button>
        </div>
      </div>
    </div>

    <!-- IMPORT PREVIEW MODAL -->
    <div v-if="showImportModal" class="ds-modal-backdrop" @click.self="closeImportModal">
      <div class="bg-white rounded-xl border border-slate-200 shadow-2xl w-full max-w-4xl max-h-[80vh] flex flex-col">
        <div class="flex justify-between items-center px-6 py-4 border-b border-slate-200 shrink-0">
          <div>
            <h2 class="ds-modal-title">Previsualización de Importación</h2>
            <p class="text-[13px] font-mono text-slate-950 mt-0.5">{{ parsedRows.length }} registros encontrados en el archivo</p>
          </div>
          <button @click="closeImportModal" class="text-slate-950 hover:text-slate-950"><IconX :size="16" :stroke-width="2" /></button>
        </div>

        <div class="overflow-auto flex-1 min-h-0">
          <table class="w-full text-[13px] font-mono" style="min-width: 1100px">
            <thead class="bg-slate-100 sticky top-0 z-10">
              <tr class="text-[13px] font-black text-slate-950 uppercase tracking-wider">
                <th class="text-left px-5 py-3 border-b border-slate-400">#</th>
                <th class="text-left px-5 py-3 border-b border-slate-400">Cliente</th>
                <th class="text-left px-5 py-3 border-b border-slate-400">Contacto</th>
                <th class="text-left px-5 py-3 border-b border-slate-400">Shipper</th>
                <th class="text-left px-5 py-3 border-b border-slate-400">CNEE</th>
                <th class="text-center px-4 py-3 border-b border-slate-400">AWb</th>
                <th class="text-center px-4 py-3 border-b border-slate-400">Skids</th>
                <th class="text-center px-4 py-3 border-b border-slate-400">Uni</th>
                <th class="text-right px-4 py-3 border-b border-slate-400">Kg</th>
                <th class="text-center px-4 py-3 border-b border-slate-400">Dest</th>
                <th class="text-center px-4 py-3 border-b border-slate-400">Com</th>
              </tr>
            </thead>
            <tbody class="divide-y divide-slate-300">
              <tr v-for="(row, idx) in parsedRows" :key="idx" class="hover:bg-slate-100 transition-colors">
                <td class="px-5 py-3 text-slate-950">{{ idx + 1 }}</td>
                <td class="px-5 py-3 font-semibold text-slate-950">{{ row.clientName }}</td>
                <td class="px-5 py-3 text-slate-950">{{ row.contactName }}</td>
                <td class="px-5 py-3 text-slate-950 truncate max-w-[120px]">{{ row.shipperName }}</td>
                <td class="px-5 py-3 text-slate-950 truncate max-w-[120px]">{{ row.cnee }}</td>
                <td class="px-4 py-3 text-center text-slate-950 font-mono">{{ row.awbNumber || '—' }}</td>
                <td class="px-4 py-3 text-center font-bold text-slate-900">{{ row.skids || '—' }}</td>
                <td class="px-4 py-3 text-center font-bold text-slate-900">{{ row.units || '—' }}</td>
                <td class="px-4 py-3 text-right font-bold text-slate-900">{{ row.reservedKg.toLocaleString() }}</td>
                <td class="px-4 py-3 text-center font-bold text-slate-950">{{ row.destination }}</td>
                <td class="px-4 py-3 text-center"><span class="inline-block text-[13px] px-1.5 py-0.5 rounded bg-slate-100 text-slate-950 font-semibold">{{ row.commodityType }}</span></td>
              </tr>
            </tbody>
          </table>
        </div>

        <div class="flex justify-between items-center px-6 py-4 border-t border-slate-200 bg-slate-100 rounded-b-xl shrink-0">
          <span class="text-[13px] font-mono text-slate-950">Se crearán {{ parsedRows.length }} bookings + MAWBs automáticamente</span>
          <div class="flex gap-2">
            <button @click="closeImportModal"
              class="ds-btn-secondary">
              Cancelar
            </button>
            <button @click="confirmImport" :disabled="importing"
              class="ds-btn-primary">
              <span>{{ importing ? 'Importando...' : `Importar ${parsedRows.length} registros` }}</span>
            </button>
          </div>
        </div>
      </div>
    </div>

  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useAppStore } from '../stores/app'
import { IconX, IconTrash } from '@tabler/icons-vue'
import * as XLSX from 'xlsx'
import { downloadCSV } from '../utils/csv'
import { useToastStore } from '../stores/toast'
import { extractError } from '../utils/error'

const store = useAppStore()
const toast = useToastStore()

const showModal = ref(false)
const saving = ref(false)

const fileInput = ref(null)
const showImportModal = ref(false)
const parsedRows = ref([])
const importing = ref(false)

const flightList = computed(() => store.flights)
const localFlightId = ref(store.selectedFlightId)

const statusFilter = ref('')
const showStatusFilter = ref(false)

const statusOptions = [
  { value: 'BOOKED', label: 'Booked', colorClass: 'bg-slate-500' },
  { value: 'RECEIVED', label: 'Received', colorClass: 'bg-slate-500' },
  { value: 'MANIFESTED', label: 'Manifested', colorClass: 'bg-slate-500' },
  { value: 'DEPARTED', label: 'Departed', colorClass: 'bg-slate-950' },
]

const statusFilterLabel = computed(() => {
  if (!statusFilter.value) return 'Status'
  const opt = statusOptions.find(o => o.value === statusFilter.value)
  return opt ? opt.label : 'Status'
})

function statusFilterColor(val) {
  const opt = statusOptions.find(o => o.value === val)
  return opt ? opt.colorClass : 'bg-slate-400'
}

function onFlightChange() {
  if (localFlightId.value) {
    store.selectFlight(localFlightId.value)
  }
}



const COM_MAP = {
  'H/V': 'HIGH_VALUES',
  'GEN': 'GENERAL',
  'PAC': 'SMALL_PACKAGES',
  'WWEF': 'WWEF',
  'CIG': 'CIGARETTES',
  'PLAN': 'LIVE_PLANTS',
  'WAL': 'GENERAL',
  'PROD': 'GENERAL',
}

function parseCommodity(abbr) {
  const key = (abbr || '').trim().toUpperCase()
  return COM_MAP[key] || 'GENERAL'
}

function normalizeAwb(raw) {
  let s = String(raw || '').replace(/[\s\-_\/]/g, '')
  if (/^\d{11}$/.test(s)) {
    s = s.slice(0, 3) + '-' + s.slice(3)
  }
  return s
}

function parseBookingsFromXLSX(data) {
  const rows = []
  for (let i = 2; i < data.length; i++) {
    const r = data[i]
    const clientName = String(r[0] || '').trim()
    if (!clientName || clientName === clientName.toUpperCase()) continue
    const contactName = String(r[1] || '').trim()
    const awbRaw = r[2]
    const awbNumber = normalizeAwb(awbRaw)
    const skids = parseInt(r[3]) || 0
    const units = parseInt(r[4]) || 0
    const reservedKg = parseFloat(r[5]) || 0
    if (skids === 0 && units === 0 && reservedKg === 0) continue
    rows.push({
      clientName,
      contactName,
      shipperName: String(r[19] || '').trim() || contactName,
      cnee: String(r[18] || '').trim(),
      awbNumber,
      skids,
      units,
      reservedKg,
      destination: String(r[9] || '').trim().toUpperCase() || 'MIA',
      commodityType: parseCommodity(r[11]),
      priority: parseInt(r[10]) || 0,
      notes: String(r[20] || '').trim(),
    })
  }
  return rows
}

const commodityTypes = ['DRY_CARGO','ELECTRONICS','PERISHABLE','HIGH_VALUES','CIGARETTES','SMALL_PACKAGES','WWEF','LIVE_PLANTS','GENERAL','COMAT','FCC']

const visibleBookings = computed(() => {
  let list = store.selectedFlightId
    ? store.bookings.filter(b => b.flightId === store.selectedFlightId)
    : store.bookings
  if (statusFilter.value) {
    list = list.filter(b => {
      const m = bookingMawb(b)
      const s = m?.status || 'BOOKED'
      return s === statusFilter.value
    })
  }
  return list
})

const deduplicatedBookings = computed(() => {
  const groups = {}
  for (const b of visibleBookings.value) {
    const key = b.mawbId || b.awbNumber || b.id
    if (!groups[key]) {
      groups[key] = { booking: b, count: 1 }
    } else {
      groups[key].count++
      if ((Number(b.skids) || 0) > (Number(groups[key].booking.skids) || 0)) {
        groups[key].booking = b
      }
    }
  }
  return Object.values(groups).map(g => ({ ...g.booking, _dupCount: g.count }))
})

function flightNumber(flightId) {
  if (!flightId) return '—'
  const f = store.flights.find(f => f.id === flightId)
  return f ? `${airlineCodeById(f.airlineId)}-${f.flightNumber}` : flightId.slice(0, 8)
}

function airlineCodeById(airlineId) {
  const a = store.airlines.find(x => x.id === airlineId)
  return a?.code || 'AIR'
}

function flightDate(flightId) {
  if (!flightId) return ''
  const f = store.flights.find(f => f.id === flightId)
  return f ? f.flightDate : ''
}

const form = ref({
  clientName: '',
  contactName: '',
  shipperName: '',
  cnee: '',
  awbNumber: '',
  skids: 1,
  units: 0,
  reservedKg: null,
  destination: 'MIA',
  commodityType: 'GENERAL',
  priority: 0,
  notes: '',
})

function bookingMawb(b) {
  if (!b.mawbId) return null
  return store.mawbs.find(m => m.id === b.mawbId || m.flightId === b.flightId && m.awbNumber === b.awbNumber) || null
}

function bookingReceipt(b) {
  const m = bookingMawb(b)
  if (!m) return null
  const all = (store.receipts || []).filter(r => r.mawb?.id === m.id || r.mawbId === m.id)
  if (all.length === 0) return null
  // Prefer the general receipt (no hawbId) which contains all pieces
  return all.find(r => !r.hawbId) || all[all.length - 1]
}

function getMawbStatus(b) {
  const m = bookingMawb(b)
  if (!m) return '—'
  return m.status || 'BOOKED'
}

function getMawbStatusClass(b) {
  const s = getMawbStatus(b)
  if (s === 'BOOKED' || s === '—') return 'bg-slate-500'
  if (s === 'RECEIVED') return 'bg-slate-500'
  if (s === 'MANIFESTED') return 'bg-slate-500'
  if (s === 'DEPARTED' || s === 'ARRIVED') return 'bg-slate-950'
  return 'bg-slate-300'
}

function exportCSV() {
  const headers = ['AWB', 'Client', 'Shipper', 'Destination', 'Skids', 'Kg', 'Status', 'Flight']
  const rows = deduplicatedBookings.value.map(b => [
    b.awbNumber || '',
    b.clientName || '',
    b.shipperName || '',
    b.destination || '',
    b.skids || '',
    b.reservedKg || '',
    getMawbStatus(b),
    flightNumber(b.flightId),
  ])
  downloadCSV(headers, rows, `bookings-${new Date().toISOString().slice(0, 10)}.csv`)
}

function openCreate() {
  const flightNum = store.selectedFlight?.flightNumber || 'XXX'
  form.value = {
    clientName: '', contactName: '', shipperName: '', cnee: '',
    awbNumber: `${airlineCodeById(store.selectedFlight?.airlineId)}-${flightNum}-${Date.now().toString(36).toUpperCase()}`,
    skids: 1, units: 0,
    reservedKg: null, destination: 'MIA', commodityType: 'GENERAL', priority: 0, notes: ''
  }
  showModal.value = true
}

function closeModal() {
  showModal.value = false
}

function triggerImport() {
  fileInput.value?.click()
}

function handleFileImport(e) {
  const file = e.target.files?.[0]
  if (!file) return
  const reader = new FileReader()
  reader.onload = (ev) => {
    try {
      const data = new Uint8Array(ev.target.result)
      const wb = XLSX.read(data, { type: 'array' })
      const ws = wb.Sheets[wb.SheetNames[0]]
      const rows = XLSX.utils.sheet_to_json(ws, { header: 1, defval: '' })
      parsedRows.value = parseBookingsFromXLSX(rows)
      showImportModal.value = true
    } catch (err) {
      toast.error(extractError(err))
      alert('Error al leer el archivo: ' + err.message)
    }
  }
  reader.readAsArrayBuffer(file)
  e.target.value = ''
}

function closeImportModal() {
  showImportModal.value = false
  parsedRows.value = []
}

async function confirmImport() {
  if (!store.selectedFlightId) {
    alert('Selecciona un vuelo primero')
    return
  }
  if (parsedRows.value.length === 0) return
  importing.value = true
  let idx = -1
  let success = 0
  let errors = 0
  for (const row of parsedRows.value) {
    idx++
    try {
      const dto = {
        airlineId: store.selectedFlight?.airlineId,
        flightId: store.selectedFlightId,
        awbNumber: row.awbNumber,
        clientName: row.clientName,
        contactName: row.contactName,
        shipperName: row.shipperName || row.clientName,
        cnee: row.cnee,
        reservedKg: row.reservedKg,
        skids: row.skids || 1,
        units: row.units || 0,
        destination: row.destination,
        commodityType: row.commodityType,
        priority: row.priority,
        notes: row.notes,
      }
      const booking = await store.createBooking(dto)
      if (booking?.id) {
        const awbNumber = row.awbNumber || `406-${(Date.now() + idx).toString().slice(-8).padStart(8, '0')}`
        const mawb = await store.createMawb({
          airlineId: store.selectedFlight?.airlineId,
          flightId: store.selectedFlightId,
          awbNumber: awbNumber,
          shipperName: row.shipperName || row.clientName,
          consigneeName: row.cnee || row.clientName,
          origin: store.selectedFlight?.origin || 'SDQ',
          destination: row.destination || store.selectedFlight?.destination || 'MIA',
          pieces: row.skids || row.units || 1,
          reportedWeightKg: row.reservedKg,
          chargeableWeightKg: row.reservedKg,
          commodityType: row.commodityType,
          status: 'BOOKED',
        })
        const mawbData = mawb.mawb || mawb
        if (mawb.weightWarning) {
          console.warn('⚠', mawb.weightWarning)
        }
        if (mawbData?.id) {
          await store.updateBooking(booking.id, { ...dto, mawbId: mawbData.id })
        }
      }
      success++
    } catch (e) {
      toast.error(extractError(e))
      const apiMsg = e.response?.data?.error || e.response?.data?.message || ''
      console.warn('Error importing row:', row.clientName, e.message, apiMsg)
      errors++
    }
  }
  await Promise.all([
    store.loadBookings(store.selectedFlightId),
    store.loadMawbs(store.selectedFlightId),
  ])
  importing.value = false
  closeImportModal()
  alert(`Importación completada: ${success} exitosos, ${errors} errores`)
}

async function saveBooking() {
  if (!form.value.clientName || !form.value.contactName || !form.value.reservedKg) {
    alert('Cliente, Contacto y Peso Reservado son obligatorios')
    return
  }
  if (!store.selectedFlightId) {
    alert('Selecciona un vuelo primero')
    return
  }
  try {
    saving.value = true
    const dto = {
      airlineId: store.selectedFlight?.airlineId,
      flightId: store.selectedFlightId,
      awbNumber: form.value.awbNumber,
      clientName: form.value.clientName,
      contactName: form.value.contactName,
      shipperName: form.value.shipperName || form.value.clientName,
      cnee: form.value.cnee,
      reservedKg: form.value.reservedKg || 0,
      skids: form.value.skids || 1,
      units: form.value.units || 0,
      destination: form.value.destination,
      commodityType: form.value.commodityType,
      priority: form.value.priority,
      notes: form.value.notes,
    }
    const booking = await store.createBooking(dto)
    // Auto-create MAWB from booking so it appears in MawbsView/WarehouseReceiptsView/UldsView
    if (booking?.id) {
      const awbNumber = form.value.awbNumber || `406-${Date.now().toString().slice(-8).padStart(8, '0')}`
      try {
        const mawb = await store.createMawb({
          airlineId: store.selectedFlight?.airlineId,
          flightId: store.selectedFlightId,
          awbNumber: awbNumber,
          shipperName: form.value.shipperName || form.value.clientName,
          consigneeName: form.value.cnee || form.value.clientName,
          origin: store.selectedFlight?.origin || 'SDQ',
          destination: form.value.destination || store.selectedFlight?.destination || 'MIA',
          pieces: form.value.skids || form.value.units || 1,
          reportedWeightKg: form.value.reservedKg || 0,
          chargeableWeightKg: form.value.reservedKg || 0,
          commodityType: form.value.commodityType || 'GENERAL',
          status: 'BOOKED',
        })
        const mawbData = mawb.mawb || mawb
        if (mawb.weightWarning) {
          console.warn('⚠', mawb.weightWarning)
        }
        if (mawbData?.id) {
          await store.updateBooking(booking.id, { ...dto, mawbId: mawbData.id })
        }
      } catch (e2) {
        toast.error(extractError(e2))
        const apiMsg = e2.response?.data?.error || e2.response?.data?.message || ''
        console.warn('MAWB creation non-critical:', e2.message, apiMsg)
      }
    }
    await Promise.all([
      store.loadBookings(store.selectedFlightId),
      store.loadMawbs(store.selectedFlightId),
    ])
    closeModal()
  } catch (e) {
    toast.error(extractError(e))
    alert('Error: ' + (e.response?.data?.message || e.message))
  } finally {
    saving.value = false
  }
}

async function removeBooking(b) {
  const keys = b._dupCount > 1 ? store.bookings.filter(x => (x.mawbId === b.mawbId) || (!b.mawbId && x.awbNumber === b.awbNumber)).map(x => x.clientName).filter(Boolean) : []
  const msg = keys.length > 1
    ? `¿Eliminar ${keys.length} bookings agrupados (${keys.join(', ')})?`
    : `¿Eliminar booking de ${b.clientName || '—'} (${b.awbNumber || b.id?.slice(0, 8) || 'N/A'})?`
  if (!confirm(msg)) return
  try {
    if (b._dupCount > 1) {
      const group = store.bookings.filter(x => (x.mawbId === b.mawbId) || (!b.mawbId && x.awbNumber === b.awbNumber))
      await Promise.all(group.map(x => store.deleteBooking(x.id).catch((e) => { toast.error(extractError(e)) })))
    } else {
      await store.deleteBooking(b.id)
    }
  } catch (e) {
    toast.error(extractError(e))
    const msg = e.response?.data?.error || e.response?.data?.message || e.message
    alert('Error al eliminar: ' + msg)
  }
}

onMounted(async () => {
  if (!store.airlines.length) {
    await store.loadAirlines()
  }
  if (!store.flights.length) {
    await store.loadFlights()
  }
  if (store.selectedFlightId) {
    localFlightId.value = store.selectedFlightId
  }
  store.loadBookings()
  store.loadAllMawbs()
})

watch(() => store.selectedFlightId, (id) => {
  localFlightId.value = id
})
</script>

