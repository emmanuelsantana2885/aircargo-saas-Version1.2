<template>
  <div class="p-3 md:p-6 max-w-[1600px] mx-auto flex flex-col h-screen">

    <!-- ═══ HEADER ═══ -->
    <div class="flex flex-col sm:flex-row items-start sm:items-center justify-between mb-4 shrink-0 gap-2">
      <div class="flex items-center gap-3">
        <h1 class="text-[13px] font-black tracking-tight text-slate-950 uppercase font-mono">Exports</h1>
        <div v-if="rows.length" class="h-4 w-[1px] bg-slate-300"></div>
        <span v-if="rows.length" class="text-[11px] font-mono font-bold text-slate-500 uppercase tracking-widest">
          {{ rows.length }} registro(s)
        </span>
      </div>
      <div class="flex items-center gap-2">
        <button @click="showImport = !showImport"
          class="flex items-center gap-1.5 text-[10px] px-3 py-2 rounded border border-slate-300 font-mono uppercase tracking-wider font-bold transition active:scale-95 shadow-sm text-slate-950 hover:bg-slate-100">
          <IconUpload :size="14" />
          Importar
        </button>
        <button @click="handleExport" :disabled="!rows.length"
          class="flex items-center gap-1.5 text-[10px] px-3 py-2 rounded border border-slate-300 font-mono uppercase tracking-wider font-bold transition active:scale-95 shadow-sm text-slate-950 hover:bg-slate-100 disabled:opacity-40 disabled:cursor-not-allowed">
          <IconDownload :size="14" />
          CSV
        </button>
      </div>
    </div>

    <!-- ═══ FILTER BAR ═══ -->
    <div class="flex flex-col sm:flex-row items-start sm:items-center gap-3 mb-4 shrink-0 p-3 rounded-lg border border-slate-200 bg-slate-50">
      <div>
        <label class="text-[10px] font-bold uppercase tracking-widest text-slate-400 block mb-1 font-mono">Tipo</label>
        <select v-model="exportType"
          class="px-3 py-1.5 rounded border border-slate-300 text-[11px] font-bold font-mono uppercase tracking-wider outline-none bg-white text-slate-900 cursor-pointer hover:border-slate-400 transition min-w-[130px]">
          <option v-for="t in types" :key="t.value" :value="t.value">{{ t.label }}</option>
        </select>
      </div>

      <div class="h-6 w-[1px] bg-slate-300 self-end mb-0.5"></div>

      <div>
        <label class="text-[10px] font-bold uppercase tracking-widest text-slate-400 block mb-1 font-mono">Desde</label>
        <input type="date" v-model="dateFrom"
          class="px-3 py-1.5 rounded border border-slate-300 text-[11px] font-mono outline-none bg-white text-slate-900 cursor-pointer hover:border-slate-400 transition w-[150px]">
      </div>
      <div>
        <label class="text-[10px] font-bold uppercase tracking-widest text-slate-400 block mb-1 font-mono">Hasta</label>
        <input type="date" v-model="dateTo"
          class="px-3 py-1.5 rounded border border-slate-300 text-[11px] font-mono outline-none bg-white text-slate-900 cursor-pointer hover:border-slate-400 transition w-[150px]">
      </div>

      <div class="self-end mb-0.5">
        <button @click="loadData" :disabled="loading"
          class="flex items-center gap-1.5 px-4 py-1.5 rounded text-[11px] font-bold font-mono uppercase tracking-wider transition active:scale-95 shadow-sm"
          :class="loading ? 'bg-slate-400 text-white cursor-wait' : 'bg-slate-900 text-white hover:bg-slate-800'">
          <IconSearch :size="14" />
          {{ loading ? 'Consultando...' : 'Consultar' }}
        </button>
      </div>

      <div class="self-end mb-0.5">
        <button @click="clearFilters"
          class="flex items-center gap-1 px-3 py-1.5 rounded border border-slate-300 text-[10px] font-bold font-mono uppercase tracking-wider text-slate-600 hover:bg-slate-100 transition">
          Limpiar
        </button>
      </div>
    </div>

    <!-- ═══ IMPORT PANEL (collapsible) ═══ -->
    <transition name="slide">
      <div v-if="showImport" class="mb-4 shrink-0 rounded-lg overflow-hidden border border-slate-200 bg-slate-50">
        <div class="flex items-center justify-between px-4 py-2.5 border-b border-slate-200 bg-white">
          <div class="flex items-center gap-2">
            <IconUpload :size="14" class="text-slate-500" />
            <span class="text-[11px] font-bold font-mono uppercase tracking-wider text-slate-700">Importar Load Planning</span>
          </div>
          <button @click="showImport = false" class="text-slate-400 hover:text-slate-600 transition text-sm">✕</button>
        </div>
        <div class="p-4 flex items-center gap-4">
          <div class="border-2 border-dashed rounded-lg px-6 py-4 text-center transition-colors flex-1"
            :style="dragOver
              ? { borderColor: 'var(--accent)', background: '#eff6ff' }
              : { borderColor: '#cbd5e1', background: 'white' }"
            @dragover.prevent="dragOver = true"
            @dragleave="dragOver = false"
            @drop.prevent="handleDrop">
            <IconUpload :size="24" class="mx-auto mb-1.5 text-slate-400" />
            <p class="text-[11px] font-mono text-slate-500 mb-2">
              {{ selectedFile ? selectedFile.name : 'Arrastra el archivo .xlsx aquí' }}
            </p>
            <input ref="fileInput" type="file" accept=".xlsx,.xls" @change="handleFileSelect" class="hidden">
            <button @click="$refs.fileInput.click()"
              class="text-[10px] font-bold font-mono uppercase tracking-wider text-slate-900 underline underline-offset-2 hover:text-slate-600 transition">
              Seleccionar archivo
            </button>
          </div>
          <button @click="handleImport" :disabled="!selectedFile || importing"
            class="px-5 py-2.5 rounded text-[11px] font-bold font-mono uppercase tracking-wider transition active:scale-95 shrink-0"
            :class="!selectedFile || importing ? 'bg-slate-300 text-white cursor-not-allowed' : 'bg-slate-900 text-white hover:bg-slate-800 shadow-sm'">
            <span class="flex items-center gap-1.5">
              <IconUpload :size="14" />
              {{ importing ? 'Importando...' : 'Importar' }}
            </span>
          </button>
        </div>

        <!-- Import result -->
        <div v-if="result" class="mx-4 mb-4 rounded-lg p-3 text-[11px] font-mono space-y-2"
          :style="result.failedSheets === 0
            ? { background: '#d1fae5', color: '#065f46', border: '1px solid #a7f3d0' }
            : { background: '#fef3c7', color: '#92400e', border: '1px solid #fde68a' }">
          <div class="font-bold flex items-center gap-2">
            <IconCheck v-if="result.failedSheets === 0" :size="14" />
            <IconAlertCircle v-else :size="14" />
            {{ result.successSheets }} de {{ result.totalSheets }} hojas importadas
          </div>
          <div class="grid grid-cols-5 gap-3 text-[10px]">
            <div><span class="font-bold">ULDs:</span> {{ result.totalUldsCreated }} / {{ result.totalUldsUpdated }}</div>
            <div><span class="font-bold">MAWBs:</span> {{ result.totalMawbsCreated }}</div>
            <div><span class="font-bold">Bookings:</span> {{ result.totalBookingsCreated }}</div>
            <div><span class="font-bold">Links:</span> {{ result.totalUldAwbsCreated }}</div>
          </div>
          <div v-for="sr in result.sheetResults" :key="sr.sheetName" class="pt-2 border-t border-current/20">
            <div class="font-semibold flex items-center gap-1.5">
              <IconCircleCheck v-if="sr.success" :size="12" style="color: #059669" />
              <IconCircleX v-else :size="12" style="color: #dc2626" />
              {{ sr.sheetName }} — {{ sr.flightNumber || 'N/A' }}
            </div>
            <div v-if="sr.error" class="mt-1" style="color: #dc2626">{{ sr.error }}</div>
            <div v-if="sr.warnings?.length" class="mt-1 space-y-0.5 opacity-75">
              <div v-for="(w, i) in sr.warnings" :key="i">⚠ {{ w }}</div>
            </div>
          </div>
        </div>
        <div v-if="importError" class="mx-4 mb-4 rounded-lg p-3 text-[11px] font-mono" style="background: #fee2e2; color: #991b1b; border: 1px solid #fca5a5">
          {{ importError }}
        </div>
      </div>
    </transition>

    <!-- ═══ TABLE ═══ -->
    <div class="flex-1 min-h-0 rounded-lg overflow-hidden border border-slate-300 bg-white flex flex-col shadow-sm">
      <!-- Table header bar -->
      <div class="bg-slate-800 border-b border-slate-600 px-4 py-2 flex items-center justify-between shrink-0">
        <span class="text-[11px] font-bold text-white uppercase tracking-wider font-mono">
          {{ typeLabel }} — Auditoría
        </span>
        <span class="text-[10px] font-mono text-slate-300">
          {{ rows.length > 0 ? rows.length + ' registro(s)' : '' }}
        </span>
      </div>

      <!-- Loading -->
      <div v-if="loading" class="flex-1 flex items-center justify-center">
        <div class="flex items-center gap-2">
          <div class="w-4 h-4 border-2 border-slate-300 border-t-slate-800 rounded-full animate-spin"></div>
          <span class="text-[11px] font-mono text-slate-500 uppercase tracking-wider">Cargando datos...</span>
        </div>
      </div>

      <!-- Error -->
      <div v-else-if="tableError" class="flex-1 flex items-center justify-center">
        <div class="text-center">
          <IconAlertCircle :size="32" class="mx-auto mb-2 text-red-400" />
          <span class="text-[11px] font-mono text-red-500">{{ tableError }}</span>
        </div>
      </div>

      <!-- Empty -->
      <div v-else-if="!rows.length" class="flex-1 flex items-center justify-center">
        <div class="text-center">
          <IconSearch :size="32" class="mx-auto mb-2 text-slate-300" />
          <p class="text-[11px] font-mono text-slate-400 uppercase tracking-wider">
            Selecciona tipo y presiona Consultar
          </p>
        </div>
      </div>

      <!-- Data -->
      <div v-else class="flex-1 min-h-0 overflow-auto">
        <div class="table-scroll-wrapper">
        <table class="text-[11px] font-mono" style="table-layout: fixed; min-width: 900px">
          <colgroup>
            <col v-for="(col, ci) in cols" :key="col" :style="colStyle(ci)" />
          </colgroup>
          <thead class="sticky top-0 z-20">
            <tr class="bg-slate-700 text-white">
              <th v-for="(col, ci) in cols" :key="col"
                class="text-left px-3 py-2 font-bold uppercase tracking-wider whitespace-nowrap relative border-r border-slate-600 last:border-r-0"
                :class="isNumCol(col) ? 'text-center' : ''"
                :style="colStyle(ci)">
                {{ col }}
                <div class="absolute right-0 top-0 bottom-0 w-2 cursor-col-resize group z-40"
                  @pointerdown="startColResize(ci, $event)">
                  <div class="w-0.5 h-full mx-auto bg-transparent group-hover:bg-white/40 transition-colors"></div>
                </div>
              </th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="(row, ri) in rows" :key="ri"
              class="border-t border-slate-200 hover:bg-blue-50 transition-colors"
              :class="ri % 2 === 0 ? 'bg-white' : 'bg-slate-50/50'">
              <td v-for="(col, ci) in cols" :key="col"
                class="px-3 py-1.5 whitespace-nowrap overflow-hidden text-ellipsis"
                :class="isNumCol(col) ? 'text-center font-semibold' : ''"
                :style="cellStyle(col, row[col])">
                {{ row[col] }}
              </td>
            </tr>
          </tbody>
        </table>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, reactive } from 'vue'
import { exportData, importLoadPlanning } from '../api/exports'
import {
  IconSearch,
  IconDownload,
  IconUpload,
  IconCheck,
  IconAlertCircle,
  IconCircleCheck,
  IconCircleX,
} from '@tabler/icons-vue'

const types = [
  { value: 'MAWBS', label: 'MAWBs' },
  { value: 'BOOKINGS', label: 'Bookings' },
  { value: 'RECEIPTS', label: 'Receipts' },
  { value: 'FLIGHTS', label: 'Flights' },
  { value: 'ULDS', label: 'ULDs' },
  { value: 'HAWBS', label: 'HAWBs' },
]

const exportType = ref('RECEIPTS')
const dateFrom = ref('')
const dateTo = ref('')

const rows = ref([])
const cols = ref([])
const loading = ref(false)
const tableError = ref('')

// Import state
const fileInput = ref(null)
const selectedFile = ref(null)
const dragOver = ref(false)
const importing = ref(false)
const result = ref(null)
const importError = ref('')
const showImport = ref(false)

const typeLabel = computed(() => types.find(t => t.value === exportType.value)?.label || exportType.value)

function isNumCol(col) {
  return ['pieces', 'skids', 'actual kg', 'chargeable kg', 'reported weight kg', 'chargeable weight kg', 'weight kg', 'tare lbs', 'reserved kg'].includes(col)
}

function cellStyle(col, val) {
  if (!val || val === '') return { color: '#94a3b8' }
  if (col === 'transaction type') {
    const colors = { CREATE: '#059669', UPDATE: '#d97706', READ: '#6366f1', DELETE: '#dc2626' }
    return { color: colors[val] || '#1e293b', fontWeight: '700' }
  }
  if (col === 'user role') {
    return { color: '#7c3aed', fontWeight: '600' }
  }
  return { color: '#1e293b' }
}

function clearFilters() {
  exportType.value = 'RECEIPTS'
  dateFrom.value = ''
  dateTo.value = ''
}

// ── Column resize ──
const DEFAULT_COL_WIDTH = 140
const colWidths = reactive({})
let resizeColIndex = null
let resizeStartX = 0
let resizeStartWidth = 0

function startColResize(colIdx, e) {
  resizeColIndex = colIdx
  resizeStartX = e.clientX
  resizeStartWidth = colWidths[colIdx] || DEFAULT_COL_WIDTH
  document.addEventListener('pointermove', onColResize)
  document.addEventListener('pointerup', stopColResize)
  e.preventDefault()
}

function onColResize(e) {
  if (resizeColIndex === null) return
  const diff = e.clientX - resizeStartX
  colWidths[resizeColIndex] = Math.max(60, resizeStartWidth + diff)
}

function stopColResize() {
  resizeColIndex = null
  document.removeEventListener('pointermove', onColResize)
  document.removeEventListener('pointerup', stopColResize)
}

function colStyle(colIdx) {
  const w = colWidths[colIdx] || DEFAULT_COL_WIDTH
  return { width: w + 'px', minWidth: w + 'px', maxWidth: w + 'px' }
}

async function loadData() {
  loading.value = true
  tableError.value = ''

  try {
    const res = await exportData(exportType.value, 'csv', dateFrom.value || null, dateTo.value || null, true)
    const text = await res.data.text()
    const parsed = parseCsv(text)
    cols.value = parsed.headers
    rows.value = parsed.rows
  } catch (e) {
    tableError.value = 'Error al consultar datos'
    cols.value = []
    rows.value = []
  } finally {
    loading.value = false
  }
}

function parseCsv(text) {
  const lines = text.split('\n').filter(l => l.trim())
  if (!lines.length) return { headers: [], rows: [] }

  function splitLine(line) {
    const result = []
    let current = ''
    let inQuotes = false
    for (let i = 0; i < line.length; i++) {
      const ch = line[i]
      if (ch === '"') {
        if (inQuotes && i + 1 < line.length && line[i + 1] === '"') {
          current += '"'
          i++
        } else {
          inQuotes = !inQuotes
        }
      } else if (ch === ',' && !inQuotes) {
        result.push(current)
        current = ''
      } else {
        current += ch
      }
    }
    result.push(current.replace(/\r$/, ''))
    return result
  }

  const headers = splitLine(lines[0])
  const rows = []
  for (let i = 1; i < lines.length; i++) {
    const vals = splitLine(lines[i])
    const row = {}
    for (let j = 0; j < headers.length; j++) {
      row[headers[j]] = vals[j] || ''
    }
    rows.push(row)
  }
  return { headers, rows }
}

function handleExport() {
  exportData(exportType.value, 'csv', dateFrom.value || null, dateTo.value || null, true)
    .then(res => {
      const blob = new Blob([res.data], { type: 'text/csv' })
      const url = URL.createObjectURL(blob)
      const a = document.createElement('a')
      a.href = url
      a.download = `AUDIT_${exportType.value}_${new Date().toISOString().slice(0, 10)}.csv`
      a.click()
      URL.revokeObjectURL(url)
    })
    .catch(() => {
      tableError.value = 'Error al exportar CSV'
    })
}

// Import handlers
function handleFileSelect(e) {
  selectedFile.value = e.target.files[0] || null
  result.value = null
  importError.value = ''
}

function handleDrop(e) {
  dragOver.value = false
  const f = e.dataTransfer?.files?.[0]
  if (f && (f.name.endsWith('.xlsx') || f.name.endsWith('.xls'))) {
    selectedFile.value = f
    result.value = null
    importError.value = ''
  }
}

function handleImport() {
  if (!selectedFile.value || importing.value) return
  importing.value = true
  result.value = null
  importError.value = ''
  ;(async () => {
    try {
      const res = await importLoadPlanning(selectedFile.value)
      result.value = res.data
    } catch (err) {
      importError.value = err.response?.data?.error || err.message || 'Error al importar'
    } finally {
      importing.value = false
    }
  })()
}
</script>

<style scoped>
.slide-enter-active, .slide-leave-active {
  transition: all 0.2s ease;
}
.slide-enter-from, .slide-leave-to {
  opacity: 0;
  transform: translateY(-8px);
  max-height: 0;
  margin-bottom: 0;
  padding: 0;
}
.slide-enter-to, .slide-leave-from {
  opacity: 1;
  transform: translateY(0);
  max-height: 500px;
}
</style>
