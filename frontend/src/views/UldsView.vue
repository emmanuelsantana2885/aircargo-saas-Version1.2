<template>
  <div class="p-3 md:p-5 bg-white h-screen max-h-screen flex flex-col justify-between text-slate-900 font-sans antialiased overflow-hidden select-none">
    <header class="flex flex-col sm:flex-row flex-wrap justify-between items-start sm:items-center gap-2 border-b border-slate-400 pb-3 shrink-0">
      <div class="flex items-center gap-4">
        <div>
          <h1 class="text-[13px] font-black tracking-tight text-slate-950 uppercase font-mono">ULD Management Hub</h1>
          <p class="text-[12px] font-mono text-slate-950 mt-0.5 uppercase tracking-widest font-bold">SDQ Operations // Ground Handling & Pallet Sheets</p>
        </div>

      </div>
      <div class="flex items-center gap-2">
        <span v-if="pendingReceiptCount > 0"
          class="text-[12px] font-mono font-bold text-slate-600 bg-slate-50 border border-slate-200 px-2 py-1 rounded">
          &#9888; {{ pendingReceiptCount }} MAWB(s) sin recibo
        </span>
        <button @click="createNewBlankUld"
          class="bg-slate-950 text-white font-mono text-xs font-black uppercase tracking-wider px-5 py-2 rounded border border-slate-900 hover:bg-slate-800 transition-all flex items-center gap-2 shadow-sm active:scale-95">
          <span class="text-[10px] font-sans">&#65291;</span> Crear ULD
        </button>
      </div>
    </header>



    <section class="flex-1 min-h-0 border border-slate-300 rounded overflow-hidden shadow-sm bg-white flex flex-col mb-1.5">
      <div v-if="appStore.loading && !localUlds.length" class="flex-1 flex items-center justify-center">
        <span class="text-[12px] font-mono text-slate-400 animate-pulse">Cargando ULDs...</span>
      </div>

      <div v-else-if="filteredUlDs.length === 0" class="flex-1 flex items-center justify-center">
        <p class="text-[12px] font-mono text-slate-400 uppercase tracking-widest">No hay ULDs disponibles — crea uno nuevo</p>
      </div>

      <template v-else>
        <!-- Single row per ULD -->
        <div class="flex flex-col gap-px p-2 overflow-y-auto max-h-[120px] shrink-0 scrollbar-none">
          <div v-for="uld in filteredUlDs" :key="uld.uid"
            @click="toggleUldExpansion(uld.uid)"
            class="flex items-center gap-3 rounded border cursor-pointer transition-all px-3 py-2 select-none"
            :class="[expandedUldId === uld.uid
              ? 'border-slate-950 ring-1 ring-slate-950 row-selected'
              : 'border-slate-200 hover:border-slate-400 bg-white hover:shadow-sm',
              uld._isFirstDated ? 'border-t-2 border-t-slate-950 mt-1' : '']"
            :style="uldStatusBorderStyle(uld.status)">

            <span class="text-[11px] font-black text-slate-950 font-mono truncate min-w-[100px] leading-tight flex items-center gap-1.5">
              {{ uld.uldNumber || 'NUEVO-ULD' }}
              <span v-if="uldAgeInDays(uld.createdAt) !== null"
                class="text-[8px] font-bold px-1 py-px rounded leading-none"
                :class="uldAgeBadgeClass(uldAgeInDays(uld.createdAt))">
                {{ uldAgeInDays(uld.createdAt) }}d
              </span>
            </span>
            <span class="text-[8px] font-black px-1 py-px rounded uppercase whitespace-nowrap leading-none shrink-0"
              :class="statusBadgeClass(uld.status)">{{ uld.status }}</span>

            <span class="text-[11px] font-mono text-slate-400 font-semibold truncate leading-tight shrink-0 min-w-[80px]">
              {{ flightLabel(uld) }}
            </span>

            <span class="text-[11px] font-mono text-slate-500 truncate leading-tight shrink-0 min-w-[60px]">
              {{ uld.route ? uld.route.replace(' -> ', '→') : '---' }}
            </span>

            <span class="text-[11px] font-mono font-bold text-slate-950 leading-tight shrink-0 min-w-[70px]">
              {{ Number(uld.grossWeightLbs || 0).toLocaleString() }} lb
            </span>

            <span class="text-[11px] font-mono text-slate-500 leading-tight shrink-0 min-w-[60px]">
              {{ (uld.mawbs || []).length }} MAWB{{ (uld.mawbs || []).length !== 1 ? 's' : '' }}
            </span>

            <div class="flex items-center gap-1 ml-auto min-w-[80px]">
              <div class="flex-1 h-[2px] bg-slate-100 rounded-full overflow-hidden">
                <div class="h-full rounded-full transition-all duration-300"
                  :class="uld.volumePct >= 90 ? 'bg-slate-600' : 'bg-slate-950'"
                  :style="{ width: uld.volumePct + '%' }"></div>
              </div>
              <span class="text-[11px] font-mono font-bold text-slate-400 leading-none">{{ uld.volumePct }}%</span>
            </div>
          </div>
        </div>

        <!-- Expanded form area -->
        <div class="border-t border-slate-200 flex-1 overflow-y-auto scrollbar-none bg-slate-50">
          <div v-for="uld in filteredUlDs" :key="'f-'+uld.uid">
            <div v-show="expandedUldId === uld.uid" class="p-4">
              <div class="bg-white border border-slate-300 rounded shadow-sm max-w-5xl mx-auto p-6 font-mono text-sm relative">
                  <div class="flex justify-between items-center border-b border-slate-300 pb-3 mb-5">
                    <div class="flex items-center gap-2">
                      <span class="text-[11px] font-black text-slate-950 uppercase tracking-wider">ULD PALLET SHEET & MANIFEST</span>
                  </div>
                  <div class="flex items-center gap-2">
                    <span class="text-[11px] font-bold text-slate-400 uppercase">Volumen:</span>
                    <input v-model.number="uld.volumePct" type="number" min="0" max="100" class="w-14 text-center bg-slate-100 border border-slate-300 rounded font-bold text-slate-950 focus:outline-none text-[12px]" />
                    <span class="text-[12px] font-bold text-slate-950">%</span>
                  </div>
                </div>

                <div class="grid grid-cols-4 gap-5 mb-6">
                  <div>
                    <label class="block text-[10px] font-black text-slate-400 uppercase mb-1">Código ULD *</label>
                    <input v-model="uld.uldNumber" type="text" placeholder="PMC-XXXXX" class="w-full bg-slate-50 border border-slate-200 rounded px-3 py-2 text-[12px] font-bold text-slate-950 focus:outline-none focus:border-slate-400 uppercase" />
                  </div>
                  <div>
                    <label class="block text-[10px] font-black text-slate-400 uppercase mb-1">Config / Tipo</label>
                    <select v-model="uld.uldType" class="w-full bg-slate-50 border border-slate-200 rounded px-3 py-2 text-[12px] font-bold text-slate-950 focus:outline-none focus:border-slate-400">
                      <option v-for="t in uldTypes" :key="t" :value="t">{{ t }}</option>
                    </select>
                  </div>
                  <div>
                    <label class="block text-[10px] font-black text-slate-400 uppercase mb-1">Posición</label>
                    <input v-model="uld.position" type="text" placeholder="1L" class="w-full bg-slate-50 border border-slate-200 rounded px-3 py-2 text-[12px] text-slate-950 focus:outline-none focus:border-slate-400 uppercase" />
                  </div>
                  <div>
                    <label class="block text-[10px] font-black text-slate-400 uppercase mb-1">Sello Seguridad</label>
                    <input v-model="uld.sealNumber" type="text" placeholder="SC-XXXXXXXX" class="w-full bg-slate-50 border border-slate-200 rounded px-3 py-2 text-[12px] text-slate-950 focus:outline-none focus:border-slate-400 font-bold" />
                  </div>
                </div>

                <!-- MAWB TABLE -->
                <div class="border border-slate-200 rounded overflow-hidden mb-6">
                  <div class="table-scroll-wrapper">
                  <div class="bg-slate-700 text-white text-[11px] font-bold uppercase grid grid-cols-12 py-3 px-5 tracking-wide items-center gap-2 shadow-sm" style="min-width: 700px">
                    <div class="col-span-3">MAWB</div>
                    <div class="col-span-2">DESCRIPCIÓN</div>
                    <div class="col-span-1 text-right">PCS ASIG</div>
                    <div class="col-span-1 text-right">PCS REC</div>
                    <div class="col-span-1 text-center">%</div>
                    <div class="col-span-1 text-right">DEST</div>
                    <div class="col-span-2 text-center">RECIBO</div>
                    <div class="col-span-1"></div>
                  </div>
                  <div class="divide-y divide-slate-100 max-h-[240px] overflow-y-auto scrollbar-none">
                    <div v-for="(mawb, mIdx) in uld.mawbs" :key="mIdx" class="grid grid-cols-12 items-center py-2 px-5 bg-white gap-2 text-sm">
                      <div class="col-span-3 relative">
                        <input v-model="mawb.awbNumber" @input="onMawbInput(uld, mIdx)" @focus="onMawbInput(uld, mIdx)" @blur="onMawbBlur(uld, mIdx)"
                          placeholder="Escribe MAWB..."
                          class="w-full border-b border-slate-200 focus:outline-none focus:border-slate-950 py-1 bg-transparent font-bold tracking-tight text-slate-950 text-[11px]" />
                        <div v-if="mawb._showSuggestions && mawb._suggestions.length"
                          class="absolute top-full left-0 right-0 z-50 bg-white border border-slate-300 rounded shadow-lg max-h-[160px] overflow-y-auto">
                          <div v-for="s in mawb._suggestions" :key="s.id"
                            @mousedown.prevent="selectMawbSuggestion(uld, mIdx, s)"
                            class="px-2 py-1.5 text-[11px] font-mono cursor-pointer hover:bg-slate-50 border-b border-slate-100 last:border-0"
                            :class="s.availablePieces > 0 ? 'text-slate-950' : 'text-slate-300'">
                            <span class="font-bold">{{ s.awbNumber }}</span>
                            <span class="text-slate-400 ml-1">— {{ s.shipperName || s.consigneeName || '' }}</span>
                            <span class="text-slate-400 text-xs ml-1">[{{ s.commodityType }}]</span>
                            <span v-if="s.availablePieces > 0" class="text-slate-600 ml-1">disp: {{ s.availablePieces }} pz</span>
                            <span v-else class="text-slate-400 ml-1">sin piezas</span>
                          </div>
                        </div>
                      </div>
                      <div class="col-span-2">
                        <input v-model="mawb.commodityType" type="text" :placeholder="mawb.commodityHint || 'Dry Cargo'"
                          class="w-full border-b border-slate-200 focus:outline-none focus:border-slate-950 py-1 bg-transparent font-medium text-slate-950 text-[11px]" />
                      </div>
                      <div class="col-span-1 flex items-center gap-1">
                        <input v-model.number="mawb.pieces" type="number" min="0"
                          class="w-full border-b border-slate-200 focus:border-slate-950 py-1 text-right bg-transparent font-bold text-[11px]" />
                      </div>
                      <div class="col-span-1 text-right font-mono text-[11px] flex items-center justify-end gap-1"
                        :class="mawb.receivedPieces != null ? 'text-slate-600' : 'text-slate-400'">
                        <template v-if="mawb.receivedPieces != null">{{ mawb.receivedPieces }}</template>
                        <span v-else>&mdash;</span>
                      </div>
                      <div class="col-span-1 text-center flex items-center justify-center gap-1">
                        <input v-model.number="mawb.piecesPct" type="number" min="0" max="100"
                          class="w-10 border-b border-slate-200 focus:outline-none focus:border-slate-950 py-1 text-center bg-transparent font-bold text-slate-600 text-[11px]" />
                        <span class="text-[11px] text-slate-400">%</span>
                      </div>
                      <div class="col-span-1 text-right">
                        <input v-model="mawb.destination" type="text" maxlength="3"
                          class="w-full border-b border-slate-200 focus:outline-none focus:border-slate-950 py-1 text-right bg-transparent uppercase font-bold text-slate-950 text-[11px]" />
                      </div>
                      <div class="col-span-2 flex justify-center items-center gap-1 text-[11px] font-mono">
                        <span v-if="mawb.hasReceipt"
                          class="inline-flex items-center gap-1 px-1.5 py-0.5 rounded font-bold text-slate-600 bg-slate-100 border border-slate-200">
                          ✓ Recibido
                        </span>
                        <span v-else
                          class="inline-flex items-center gap-1 px-1.5 py-0.5 rounded font-bold text-slate-500 bg-slate-100 border border-slate-200 cursor-help"
                          title="Esta MAWB no tiene recibo de bodega. Se recomienda emitir el recibo antes del despacho.">
                          &#9888; Pend.
                        </span>
                      </div>
                      <div class="col-span-1 text-center">
                        <button @click="removeMawbRow(uld, mIdx)" class="text-slate-400 hover:text-slate-600 text-xs">&#10005;</button>
                      </div>
                    </div>
                  </div>
                  </div>
                  <div class="p-2 bg-slate-50 border-t border-slate-100 flex justify-between items-center text-[11px] text-slate-500">
                    <button @click="addMawbRow(uld)"
                      class="py-1.5 px-3 border border-dashed border-slate-300 rounded text-center hover:text-slate-950 transition-colors font-bold text-[11px] uppercase">
                      + MAWB
                    </button>
                    <div class="flex items-center gap-2">
                      <span class="font-mono">PCS: {{ totalUldPieces(uld) }} / {{ totalUldReceivedPieces(uld) }} rec</span>
                    </div>
                  </div>
                </div>

                <div class="grid grid-cols-4 gap-5 mb-6">
                  <div>
                    <label class="block text-[10px] font-black text-slate-400 uppercase mb-1">Tara <span class="text-slate-950">(lbs)</span></label>
                    <div class="relative">
                      <input v-model.number="uld.tareLbs" type="number" step="0.1"
                        class="w-full bg-slate-50 border border-slate-200 rounded px-3 py-2 text-[12px] text-slate-950 focus:outline-none focus:border-slate-400" />
                    </div>
                    <div v-if="suggestedTareLbs" class="mt-1 text-[9px] text-slate-400 font-mono">
                      Sugerida: {{ suggestedTareLbs }} lb
                      <button @click="uld.tareLbs = suggestedTareLbs"
                        class="text-slate-500 hover:text-slate-700 underline ml-1">usar</button>
                    </div>
                  </div>
                  <div>
                    <label class="block text-[10px] font-black text-slate-400 uppercase mb-1">Peso Bruto (lbs)</label>
                    <input v-model.number="uld.grossWeightLbs" type="number" class="w-full bg-slate-50 border border-slate-200 rounded px-3 py-2 text-[12px] text-slate-950 focus:outline-none focus:border-slate-400" />
                  </div>
                  <div>
                    <label class="block text-[10px] font-black text-slate-400 uppercase mb-1">Estado</label>
                      <select v-model="uld.status" class="w-full bg-slate-50 border border-slate-200 rounded px-3 py-2 text-[12px] text-slate-950 focus:outline-none focus:border-slate-400 font-bold">
                        <option value="OPEN">OPEN (Abierto)</option>
                        <option value="BUILT">BUILT (Armado)</option>
                        <option value="SEALED">SEALED (Precintado)</option>
                        <option value="LOADED">LOADED (Cargado)</option>
                        <option value="LEFT_BEHIND">LEFT BEHIND (Dejado)</option>
                      </select>
                  </div>
                  <div class="bg-slate-50 flex flex-col justify-center rounded px-3 py-2 border border-slate-200">
                    <span class="text-xs font-black text-slate-600 uppercase tracking-wider">Peso Neto</span>
                    <span class="text-sm font-black text-slate-800">{{ ((uld.grossWeightLbs || 0) - (uld.tareLbs || 0)).toLocaleString() }} lbs</span>
                  </div>
                </div>

                <div class="grid grid-cols-3 gap-4 border-t border-slate-200 pt-5">
                  <div>
                    <label class="block text-[10px] font-black text-slate-400 uppercase mb-1">Ubicación / Puerta</label>
                    <input v-model="uld.door" type="text" placeholder="Puerta 4 / Patio" class="w-full border border-slate-200 rounded px-3 py-2 text-[12px] text-slate-950 bg-slate-50 focus:outline-none uppercase" />
                  </div>
                  <div>
                    <label class="block text-[10px] font-black text-slate-400 uppercase mb-1">Manifestado Por</label>
                    <input v-model="uld.filledBy" type="text" placeholder="Operadores de rampa" class="w-full border border-slate-200 rounded px-3 py-2 text-[12px] text-slate-950 bg-slate-50 focus:outline-none font-bold" />
                  </div>
                  <div>
                    <label class="block text-[10px] font-black text-slate-400 uppercase mb-1">Notas</label>
                    <input v-model="uld.notes" type="text" placeholder="Notas adicionales" class="w-full border border-slate-200 rounded px-3 py-2 text-[12px] text-slate-950 bg-slate-50 focus:outline-none" />
                  </div>
                </div>



                <div class="border-t border-slate-200 pt-5 flex justify-end gap-2 bg-slate-50/50 -mx-6 -mb-6 p-6 rounded-b">
                  <div class="flex items-center gap-4 mr-auto">
                    <div class="flex flex-col">
                      <span class="text-[10px] font-black text-slate-400 uppercase tracking-widest">Vuelo</span>
                      <select v-model="uld.saveFlightId"
                        class="bg-white border border-slate-300 rounded px-2 py-1.5 font-bold text-slate-950 focus:outline-none uppercase text-[10px] min-w-[160px]">
                        <option value="" disabled>Seleccionar vuelo</option>
                        <option v-for="f in appStore.flights" :key="f.id" :value="f.id">
                          {{ airlineCodeById(f.airlineId) }}-{{ f.flightNumber }} ({{ f.origin }}→{{ f.destination }}) {{ f.flightDate }}
                        </option>
                      </select>
                    </div>
                    <div class="flex flex-col">
                      <span class="text-[10px] font-black text-slate-400 uppercase tracking-widest">Creado</span>
                      <span class="text-[12px] font-bold text-slate-950">{{ uld.createdAt ? formatDate(uld.createdAt) : '—' }}</span>
                    </div>
                  </div>
                  <div class="flex items-center gap-2">
                    <button @click="deleteUld(uld)"
                      class="text-slate-400 hover:text-slate-700 font-mono font-black uppercase text-[10px] tracking-widest px-3 py-2 rounded border border-slate-200 hover:border-slate-400 transition-all"
                      :title="uld.backendId ? 'Eliminar ULD' : 'Descartar ULD'">
                      &#10005;
                    </button>
                    <button v-if="uld.backendId" @click="dismountUld(uld)"
                      class="bg-slate-600 hover:bg-slate-700 text-white font-mono font-black uppercase text-[10px] tracking-widest px-4 py-2.5 rounded shadow-md transition-all flex items-center gap-2">
                      Desmontar ULD
                    </button>
                    <button @click="saveUld(uld)"
                      class="bg-slate-800 hover:bg-slate-700 text-white font-mono font-black uppercase text-[12px] tracking-widest px-6 py-2.5 rounded shadow-md transition-all flex items-center gap-2">
                      {{ uld.backendId ? 'Actualizar ULD' : '&#128640; Enviar a Load Planning' }}
                    </button>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <div v-if="!expandedUldId" class="flex items-center justify-center h-full">
            <p class="text-[11px] font-mono text-slate-300 uppercase tracking-widest">Selecciona un ULD para editar</p>
          </div>
        </div>
      </template>
    </section>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useUldsStore } from '../stores/ulds'
import { useAppStore } from '../stores/app'
import { uldsApi } from '../api/ulds'
import { useToastStore } from '../stores/toast'
import { extractError } from '../utils/error'

const uldsStore = useUldsStore()
const appStore = useAppStore()
const toast = useToastStore()

function airlineCodeById(airlineId) {
  const a = appStore.airlines.find(x => x.id === airlineId)
  return a?.code || 'AIR'
}

const uldTypes = ['PMC','PAH','PAG','PAJ','AAY','AAZ','AAD','PIP','BULK','AMP','AMJ']
const statusFlowSteps = ['OPEN', 'BUILT', 'SEALED', 'LOADED']

const specialItems = [
  { id: 'spc-sdq-sdf', awbNumber: 'SDQ/SDF', shipperName: 'Ruta Doméstica SDQ→SDF', consigneeName: 'Ruta Doméstica SDQ→SDF', commodityType: 'DOMESTIC', pieces: 0, destination: 'SDF', isSpecial: true },
  { id: 'spc-sdq-mia', awbNumber: 'SDQ/MIA', shipperName: 'Ruta Doméstica SDQ→MIA', consigneeName: 'Ruta Doméstica SDQ→MIA', commodityType: 'DOMESTIC', pieces: 0, destination: 'MIA', isSpecial: true },
  { id: 'spc-wwef', awbNumber: 'WWEF', shipperName: 'Worldwide Express Freight', consigneeName: 'WWEF', commodityType: 'EXPRESS', pieces: 0, destination: 'MIA', isSpecial: true },
  { id: 'spc-fcc', awbNumber: 'FCC', shipperName: 'Full Container Load', consigneeName: 'FCC Equipment', commodityType: 'EQUIPMENT', pieces: 0, destination: '', isSpecial: true },
  { id: 'spc-empty-uld', awbNumber: 'EMPTY ULD', shipperName: 'Empty ULD', consigneeName: 'Empty ULD Equipment', commodityType: 'EQUIPMENT', pieces: 0, destination: '', isSpecial: true },
  { id: 'spc-empty-bags', awbNumber: 'EMPTY BAGS', shipperName: 'Empty Bags', consigneeName: 'Empty Bags Equipment', commodityType: 'EQUIPMENT', pieces: 0, destination: '', isSpecial: true },
  { id: 'spc-nets', awbNumber: 'NETS', shipperName: 'Cargo Nets', consigneeName: 'Cargo Nets Equipment', commodityType: 'EQUIPMENT', pieces: 0, destination: '', isSpecial: true },
]

const expandedUldId = ref(null)
const filterDate = ref('')

const TARE_MAP = {
  AAY: 460,
  AAD: 540,
  AAZ: 500,
  AMP: 600,
  AMJ: 610,
  PMC: 270,
  PAG: 250,
  PAH: 300,
  PIP: 250,
}

const suggestedTareLbs = computed(() => {
  const expanded = localUlds.value.find(u => u.uid === expandedUldId.value)
  if (!expanded || !expanded.uldType) return null
  return TARE_MAP[expanded.uldType.toUpperCase()] ?? null
})

const filteredUlDs = computed(() => {
  // Pasarela de ULDs flotantes: siempre se muestran todos
  return localUlds.value
})

function formatDate(iso) {
  if (!iso) return '—'
  const d = new Date(iso)
  return d.toLocaleDateString('es-DO', { year: 'numeric', month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit' })
}



// Local ULD list derived from backend + new unsaved
const localUlds = ref([])

const availableMawbs = computed(() => {
  const mawbsWithAvailability = appStore.mawbs.map(m => {
    const receipt = appStore.receipts.find(r => r.mawb?.id === m.id || r.mawbId === m.id)
    const reserved = m.pieces || 0
    const assignedInUlDs = localUlds.value.flatMap(u =>
      (u.mawbs || []).filter(mw => mw.awbNumber === m.awbNumber)
    ).reduce((s, mw) => s + (mw.pieces || 0), 0)
    const receiptPieces = receipt ? (receipt.pieceCount || 0) : 0
    const available = Math.max(0, (reserved > 0 ? reserved : receiptPieces) - assignedInUlDs)
    return { ...m, availablePieces: available }
  })
  return [...specialItems, ...mawbsWithAvailability]
})

function onMawbInput(uld, mIdx) {
  const mawb = uld.mawbs[mIdx]
  const q = (mawb.awbNumber || '').toUpperCase().trim()
  if (!q) {
    mawb._showSuggestions = false
    mawb._suggestions = []
    return
  }
  mawb._suggestions = availableMawbs.value.filter(m => {
    const label = m.awbNumber || ''
    return label.toUpperCase().includes(q)
  }).slice(0, 15)
  mawb._showSuggestions = mawb._suggestions.length > 0
}

function onMawbBlur(uld, mIdx) {
  setTimeout(() => { uld.mawbs[mIdx]._showSuggestions = false }, 200)
}

function selectMawbSuggestion(uld, mIdx, selected) {
  uld.mawbs[mIdx].awbNumber = selected.awbNumber
  uld.mawbs[mIdx]._showSuggestions = false
  onMawbSelect(uld, mIdx)
}

const pendingReceiptCount = computed(() => {
  const allMawbs = localUlds.value.flatMap(u => u.mawbs || [])
  const uniqueAwbs = [...new Set(allMawbs.filter(m => m.awbNumber).map(m => m.awbNumber))]
  return uniqueAwbs.filter(awb => !mawbHasReceipt(awb)).length
})

function mawbReceiptInfo(awbNumber) {
  const m = appStore.mawbs.find(x => x.awbNumber === awbNumber)
  const receipt = appStore.receipts.find(r => r.mawb?.id === m?.id || r.mawbId === m?.id)
  const reservedPieces = m ? (m.pieces || 0) : 0
  // Pieces de esta MAWB asignadas en TODOS los ULDs (excepto el actual)
  const assignedInUlDs = localUlds.value.flatMap(u =>
    (u.mawbs || []).filter(mw => mw.awbNumber === awbNumber)
  ).reduce((s, mw) => s + (mw.pieces || 0), 0)
  const availablePieces = Math.max(0, (reservedPieces > 0 ? reservedPieces : (receipt ? (receipt.pieceCount || 0) : 0)) - assignedInUlDs)
  return {
    hasReceipt: !!receipt,
    receivedPieces: receipt ? (receipt.pieceCount || 0) : 0,
    reservedPieces: reservedPieces,
    availablePieces: availablePieces,
    mawbId: m?.id || null,
  }
}

function getMawbAvailableInfo(awbNumber) {
  const info = mawbReceiptInfo(awbNumber)
  const assignedInUlDs = localUlds.value.flatMap(u =>
    (u.mawbs || []).filter(mw => mw.awbNumber === awbNumber)
  ).reduce((s, mw) => s + (mw.pieces || 0), 0)
  const base = info.reservedPieces > 0 ? info.reservedPieces : info.receivedPieces
  const available = Math.max(0, base - assignedInUlDs)
  if (info.hasReceipt) {
    return `(Recibido: ${info.receivedPieces}, Asignado: ${assignedInUlDs}, Disponible: ${available})`
  }
  return `(Reservado: ${info.reservedPieces}, Asignado: ${assignedInUlDs}, Disponible: ${available})`
}

function mawbHasReceipt(awbNumber) {
  return mawbReceiptInfo(awbNumber).hasReceipt
}

function uldHasPendingReceipt(uld) {
  return (uld.mawbs || []).some(m => m.awbNumber && !mawbHasReceipt(m.awbNumber))
}

function totalUldPieces(uld) {
  return (uld.mawbs || []).reduce((s, m) => s + (m.pieces || 0), 0)
}

function totalUldReceivedPieces(uld) {
  return (uld.mawbs || []).reduce((s, m) => s + ((m.receivedPieces != null ? m.receivedPieces : m.pieces) || 0), 0)
}

function uldPieceMismatchCount(uld) {
  return (uld.mawbs || []).filter(m => m.receivedPieces != null && m.pieces > m.receivedPieces).length
}


function rebuildLocalList() {
  const backend = (appStore.ulds || []).map(u => {
    const flight = appStore.flights.find(f => f.id === u.flightId)
    return {
      uid: u.id,
      backendId: u.id,
      uldNumber: u.uldNumber,
      flightId: u.flightId,
      flightLabel: flight?.flightNumber || 'FLOTANTE',
      route: flight ? (flight.origin + ' -> ' + flight.destination) : 'Sin Vuelo',
      uldType: u.uldType,
      config: u.config,
      position: u.position,
      sealNumber: u.sealNumber,
      tareLbs: u.tareLbs || u.tareWeight || 0,
      grossWeightLbs: u.grossWeightLbs || u.grossWeight || 0,
      status: u.status || 'OPEN',
      filledBy: '',
      notes: u.notes || '',
      door: u.door || '',
      saveFlightId: u.flightId,
      awbs: (u.awbs || []),
      volumePct: 0,
      createdAt: u.createdAt,
      mawbs: (u.awbs || []).map(m => {
      const info = mawbReceiptInfo(m.mawbLabel || '')
      return {
        awbNumber: m.mawbLabel || '',
        _showSuggestions: false,
        _suggestions: [],
        commodityType: m.description || 'DRY_CARGO',
        commodityHint: m.description || '',
        pieces: m.pieces || 0,
        piecesPct: m.piecesPct || 0,
        destination: m.destination || '-',
        mawbId: m.mawbId || null,
        ...info,
      }
    }),
  }
})
  // Calculate volumePct as sum of piecesPct across all MAWBs, capped at 100
  backend.forEach(uld => {
    const total = (uld.mawbs || []).reduce((s, m) => s + (m.piecesPct || 0), 0)
    uld.volumePct = Math.min(total, 100)
  })
  // Merge with existing unsaved local ULDS
  const unsaved = localUlds.value.filter(u => !u.backendId)
  localUlds.value = [...unsaved, ...backend]
}

function createNewBlankUld() {
  localUlds.value.unshift({
    uid: 'new-' + Date.now(),
    backendId: null,
    uldNumber: '',
    flightId: null,
    saveFlightId: null,
    flightLabel: '',
    route: 'SDQ -> MIA',
    uldType: 'PMC',
    config: '',
    position: '',
    sealNumber: '',
    tareLbs: 140,
    grossWeightLbs: 0,
    status: 'OPEN',
    volumePct: 0,
    filledBy: '',
    notes: '',
    door: '',
    mawbs: [],
  })
  expandedUldId.value = localUlds.value[0].uid
}

async function dismountUld(uld) {
  if (!confirm(`¿Desmontar ULD ${uld.uldNumber || ''}? Se eliminarán todas las MAWB asignadas, sello, posición, peso y se dejará en estado OPEN.`)) return
  try {
    // 1. Eliminar todos los ULD-AWB links
    const existing = await import('../api/uldAwbs').then(mod => mod.uldAwbsApi.getByUld(uld.backendId))
    for (const link of (existing.data || [])) {
      await import('../api/uldAwbs').then(mod => mod.uldAwbsApi.delete(link.id))
    }
    // 2. Resetear el ULD
    await uldsApi.update(uld.backendId, {
      airlineId: uld.airlineId || appStore.selectedFlight?.airlineId || null,
      uldNumber: uld.uldNumber,
      position: null,
      sealNumber: null,
        tareLbs: 0,
        grossWeightLbs: 0,
        status: 'OPEN',
        notes: null,
      })
      uld.tareLbs = 0
      // 3. Quitar asignacion de vuelo
      await uldsApi.assignFlight(uld.backendId, null)
    // 4. Recargar
    await appStore.loadUlds()
    rebuildLocalList()
    expandedUldId.value = null
  } catch (e) {
    toast.error(extractError(e))
    alert('Error al desmontar ULD: ' + (e.response?.data?.message || e.message))
  }
}

async function deleteUld(uld) {
  if (!uld.backendId) {
    // ULD local sin guardar — solo descartar del listado
    localUlds.value = localUlds.value.filter(u => u.uid !== uld.uid)
    if (expandedUldId.value === uld.uid) expandedUldId.value = null
    return
  }
  const hasCargo = (uld.mawbs || []).length > 0
  if (hasCargo) {
    if (!confirm(`El ULD ${uld.uldNumber || ''} tiene ${uld.mawbs.length} MAWB(s). Se desmontará primero y luego se eliminará. ¿Continuar?`)) return
    // Dismount first
    try {
      const existing = await import('../api/uldAwbs').then(mod => mod.uldAwbsApi.getByUld(uld.backendId))
      for (const link of (existing.data || [])) {
        await import('../api/uldAwbs').then(mod => mod.uldAwbsApi.delete(link.id))
      }
      await uldsApi.update(uld.backendId, {
        airlineId: uld.airlineId || appStore.selectedFlight?.airlineId || null,
        uldNumber: uld.uldNumber,
        position: null,
        sealNumber: null,
        tareLbs: 0,
        grossWeightLbs: 0,
        status: 'OPEN',
        notes: null,
      })
      uld.tareLbs = 0
      await uldsApi.assignFlight(uld.backendId, null)
    } catch (e) {
      toast.error(extractError(e))
      alert('Error al desmontar ULD: ' + (e.response?.data?.message || e.message))
      return
    }
  } else {
    if (!confirm(`¿Eliminar ULD ${uld.uldNumber || ''} permanentemente?`)) return
  }
  try {
    await uldsApi.delete(uld.backendId)
    localUlds.value = localUlds.value.filter(u => u.uid !== uld.uid)
    if (expandedUldId.value === uld.uid) expandedUldId.value = null
    await appStore.loadUlds()
    rebuildLocalList()
  } catch (e) {
    toast.error(extractError(e))
    alert('Error al eliminar ULD: ' + (e.response?.data?.message || e.message))
  }
}

async function saveUld(uld) {
  if (!uld.uldNumber) {
    alert('Código ULD requerido')
    return
  }
  const flightId = uld.saveFlightId

  if (!flightId) {
    if (!uld.backendId) {
      alert('Selecciona un vuelo para asignar este ULD')
      return
    }
    // Floating ULD sin cambio de vuelo — solo actualizar campos
    try {
      uld.notes = [uld.door ? `Ubicación: ${uld.door}` : '', uld.filledBy ? `Llenado por: ${uld.filledBy}` : '', uld.notes].filter(Boolean).join(' | ')
      await uldsApi.update(uld.backendId, {
        airlineId: uld.airlineId || appStore.selectedFlight?.airlineId || null,
        uldNumber: uld.uldNumber,
        uldType: uld.uldType,
        config: uld.config || null,
        position: uld.position || null,
        sealNumber: uld.sealNumber || null,
        tareLbs: uld.tareLbs || 0,
        grossWeightLbs: uld.grossWeightLbs || 0,
        status: uld.status || 'OPEN',
        notes: uld.notes || null,
      })
      // Recrear links ULD-AWB
      if (uld.backendId) {
        const existing = await import('../api/uldAwbs').then(mod => mod.uldAwbsApi.getByUld(uld.backendId))
        for (const link of (existing.data || [])) {
          await import('../api/uldAwbs').then(mod => mod.uldAwbsApi.delete(link.id))
        }
      }
      for (const m of (uld.mawbs || [])) {
        if (m.awbNumber && uld.backendId) {
          const matchingMawb = appStore.mawbs.find(x => x.awbNumber === m.awbNumber)
          await import('../api/uldAwbs').then(mod => mod.uldAwbsApi.create({
            uldId: uld.backendId,
            mawbId: matchingMawb?.id || null,
            mawbLabel: m.awbNumber,
            description: m.commodityType || 'DRY_CARGO',
            destination: m.destination || 'MIA',
            pieces: m.pieces || 0,
            piecesPct: m.piecesPct || 0,
          }))
        }
      }
      expandedUldId.value = null
      await appStore.loadUlds()
      rebuildLocalList()
    } catch (e) {
      toast.error(extractError(e))
      alert('Error: ' + (e.response?.data?.message || e.message))
    }
    return
  }


  try {
    uld.flightId = flightId
    uld.notes = [uld.door ? `Ubicación: ${uld.door}` : '', uld.filledBy ? `Llenado por: ${uld.filledBy}` : '', uld.notes].filter(Boolean).join(' | ')
    const result = await uldsStore.dispatchUld(uld, flightId)
    uld.backendId = result?.id || uld.backendId
    // Delete existing ULD-AWB links before recreating
    if (uld.backendId) {
      const existing = await import('../api/uldAwbs').then(mod => mod.uldAwbsApi.getByUld(uld.backendId))
      for (const link of (existing.data || [])) {
        await import('../api/uldAwbs').then(mod => mod.uldAwbsApi.delete(link.id))
      }
    }
    // Create ULD-AWB links for each MAWB
    for (const m of (uld.mawbs || [])) {
      if (m.awbNumber && result?.id) {
        const matchingMawb = appStore.mawbs.find(x => x.awbNumber === m.awbNumber)
        await import('../api/uldAwbs').then(mod => mod.uldAwbsApi.create({
          uldId: result.id,
          mawbId: matchingMawb?.id || null,
          mawbLabel: m.awbNumber,
          description: m.commodityType || 'DRY_CARGO',
          destination: m.destination || 'MIA',
          pieces: m.pieces || 0,
          piecesPct: m.piecesPct || 0,
        }))
      }
    }
    expandedUldId.value = null
    await appStore.loadUlds()
    rebuildLocalList()
    await appStore.loadAllMawbs()
  } catch (e) {
    toast.error(extractError(e))
    alert('Error: ' + (e.response?.data?.message || e.message))
  }
}

function toggleUldExpansion(uid) {
  expandedUldId.value = expandedUldId.value === uid ? null : uid
}

function addMawbRow(uld) {
  uld.mawbs.push({ awbNumber: '', commodityType: 'DRY_CARGO', commodityHint: '', pieces: 0, piecesPct: 0, destination: 'MIA', mawbId: null, hasReceipt: false, receivedPieces: 0, _showSuggestions: false, _suggestions: [] })
}

function removeMawbRow(uld, index) {
  uld.mawbs.splice(index, 1)
}

function onMawbSelect(uld, mIdx) {
  const selected = availableMawbs.value.find(m => m.awbNumber === uld.mawbs[mIdx].awbNumber)
  if (selected) {
    uld.mawbs[mIdx].commodityType = selected.commodityType || 'DRY_CARGO'
    uld.mawbs[mIdx].commodityHint = selected.commodityType || ''
    uld.mawbs[mIdx].destination = selected.destination || 'MIA'
    uld.mawbs[mIdx].mawbId = selected.isSpecial ? null : selected.id
    if (!selected.isSpecial) {
      const info = mawbReceiptInfo(selected.awbNumber)
      uld.mawbs[mIdx].hasReceipt = info.hasReceipt
      uld.mawbs[mIdx].receivedPieces = info.receivedPieces
      uld.mawbs[mIdx].reservedPieces = info.reservedPieces
      // Auto-fill pieces from reserved/received
      if (info.receivedPieces > 0) {
        uld.mawbs[mIdx].pieces = info.receivedPieces
      } else if (info.reservedPieces > 0) {
        uld.mawbs[mIdx].pieces = info.reservedPieces
      }
    }
  }
}

function statusBadgeClass(status) {
  switch (status) {
    case 'OPEN': return 'bg-slate-100 text-slate-600 border border-slate-200'
    case 'BUILT': return 'bg-slate-100 text-slate-600 border border-slate-200'
    case 'SEALED': return 'bg-slate-100 text-slate-600 border border-slate-200'
    case 'LOADED': return 'bg-slate-100 text-slate-600 border border-slate-200'
    case 'LEFT_BEHIND': return 'bg-slate-100 text-slate-600 border border-slate-200'
    default: return 'bg-slate-100 text-slate-950 border border-slate-200'
  }
}

function flightLabel(uld) {
  if (!uld.flightLabel) return '---'
  if (uld.flightLabel === 'FLOTANTE') return 'FLOTANTE'
  if (uld.flightLabel === 'TBD') return 'TBD'
  const num = parseInt(uld.flightLabel, 10)
  if (!isNaN(num)) return airlineCodeById(uld.airlineId) + '-' + uld.flightLabel
  return uld.flightLabel
}

function getProgressWidth(status) {
  if (status === 'LEFT_BEHIND') return '100%'
  if (status === 'OPEN') return '0%'
  if (status === 'BUILT') return '33%'
  if (status === 'SEALED') return '66%'
  if (status === 'LOADED') return '100%'
  return '0%'
}

function getLineProgressColor(status) {
  if (status === 'LEFT_BEHIND') return 'bg-slate-400'
  if (status === 'BUILT') return 'bg-slate-500'
  if (status === 'SEALED') return 'bg-slate-600'
  if (status === 'LOADED') return 'bg-slate-700'
  return 'bg-slate-200'
}

function getStatusDotClass(currentStatus, step) {
  if (currentStatus === step) {
    if (step === 'LEFT_BEHIND') return 'bg-slate-400 border-slate-500 scale-125'
    if (step === 'OPEN') return 'bg-slate-500 border-slate-600 scale-125'
    if (step === 'BUILT') return 'bg-slate-600 border-slate-700 scale-125'
    if (step === 'SEALED') return 'bg-slate-600 border-slate-700 scale-125'
    if (step === 'LOADED') return 'bg-slate-700 border-slate-800 scale-125'
  }
  const order = ['OPEN', 'BUILT', 'SEALED', 'LOADED']
  if (currentStatus === 'LEFT_BEHIND') return 'bg-slate-200 border-slate-300'
  if (order.indexOf(currentStatus) >= order.indexOf(step)) return 'bg-slate-400 border-slate-500'
  return 'bg-slate-200 border-slate-300'
}

function uldAgeInDays(createdAt) {
  if (!createdAt) return null
  const created = new Date(createdAt)
  const now = new Date()
  return Math.floor((now - created) / (1000 * 60 * 60 * 24))
}

function uldAgeBadgeClass(days) {
  if (days === null) return ''
  if (days < 7) return 'bg-slate-100 text-slate-600 border border-slate-200'
  if (days <= 30) return 'bg-slate-100 text-slate-600 border border-slate-200'
  return 'bg-slate-100 text-slate-600 border border-slate-200'
}

function uldStatusBorderStyle(status) {
  return { borderLeft: '3px solid #e2e8f0' }
}

onMounted(async () => {
  if (!appStore.airlines.length) await appStore.loadAirlines()
  await Promise.all([
    appStore.loadFlights(),
    appStore.loadAllMawbs(),
    appStore.loadReceipts(),
    appStore.loadUlds(),
  ])
  rebuildLocalList()
})

watch(() => appStore.ulds, () => rebuildLocalList(), { deep: true })
</script>

<style scoped>
.scrollbar-none::-webkit-scrollbar { display: none; }
.scrollbar-none { -ms-overflow-style: none; scrollbar-width: none; }
input[type="number"]::-webkit-inner-spin-button,
input[type="number"]::-webkit-outer-spin-button { -webkit-appearance: none; margin: 0; }
</style>
