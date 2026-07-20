<template>
  <div v-if="active" class="border-2 border-dashed rounded-lg p-4 mb-4 transition-all duration-300"
       :class="flashClass">
    <!-- Header -->
    <div class="flex items-center justify-between mb-3">
      <div class="flex items-center gap-2">
        <div class="w-2.5 h-2.5 rounded-full animate-pulse" :class="scanning ? 'bg-emerald-500' : 'bg-slate-300'"></div>
        <span class="text-[11px] font-black text-slate-900 uppercase tracking-wider">
          Modo Scan — ULD: {{ uldNumber }}
        </span>
        <span v-if="lastResult" class="text-[10px] font-bold px-2 py-0.5 rounded-full"
              :class="lastResult.success ? 'bg-emerald-100 text-emerald-700' : 'bg-red-100 text-red-700'">
          {{ lastResult.message || lastResult.error }}
        </span>
      </div>
      <div class="flex items-center gap-2">
        <button @click="$emit('exit-scan')" class="text-[10px] font-bold text-slate-500 hover:text-red-600 uppercase px-2 py-1 rounded hover:bg-red-50">
          ✕ Salir
        </button>
      </div>
    </div>

    <!-- Scan Input -->
    <div class="relative mb-3">
      <input
        ref="scanInput"
        v-model="scanCode"
        @keydown.enter.prevent="processScan"
        type="text"
        placeholder="Escanee código de barras o escriba MAWB/ULD..."
        class="w-full bg-white border-2 border-slate-300 rounded-lg px-4 py-3 text-[13px] font-mono font-bold text-slate-900
               focus:outline-none focus:border-emerald-500 focus:ring-2 focus:ring-emerald-200
               placeholder:text-slate-300 placeholder:font-sans placeholder:font-normal placeholder:text-[12px]"
        :disabled="processing"
      />
      <div v-if="processing" class="absolute right-3 top-1/2 -translate-y-1/2">
        <div class="w-5 h-5 border-2 border-emerald-500 border-t-transparent rounded-full animate-spin"></div>
      </div>
    </div>

    <!-- Scan History -->
    <div v-if="history.length" class="max-h-32 overflow-y-auto space-y-1 mb-2">
      <div v-for="(entry, i) in history" :key="i"
           class="flex items-center gap-2 text-[10px] py-1 px-2 rounded"
           :class="entry.success ? 'bg-emerald-50 text-emerald-700' : 'bg-red-50 text-red-600'">
        <span>{{ entry.success ? '✓' : '✗' }}</span>
        <span class="font-bold">{{ entry.awbNumber }}</span>
        <span v-if="entry.pieceNumber">Pieza #{{ entry.pieceNumber }}</span>
        <span v-if="entry.totalOnUld">({{ entry.totalOnUld }}/{{ entry.maxAllowed }})</span>
        <span class="ml-auto opacity-60">{{ entry.time }}</span>
      </div>
    </div>

    <!-- Actions -->
    <div class="flex items-center gap-2 mt-2">
      <button @click="undoLast" :disabled="!canUndo"
              class="text-[10px] font-bold px-3 py-1.5 rounded border transition-colors"
              :class="canUndo ? 'border-slate-300 text-slate-600 hover:bg-slate-100' : 'border-slate-200 text-slate-300 cursor-not-allowed'">
        ↩ Deshacer Último
      </button>
    </div>

    <!-- Camera Modal -->
    <div v-if="showCamera" class="fixed inset-0 bg-black/60 z-50 flex items-center justify-center" @click.self="showCamera = false">
      <div class="bg-white rounded-xl p-6 max-w-lg w-full mx-4 shadow-2xl">
        <div class="flex items-center justify-between mb-4">
          <span class="text-[12px] font-black text-slate-900 uppercase">Cámara</span>
          <button @click="showCamera = false" class="text-slate-400 hover:text-red-500">✕</button>
        </div>
        <div id="qr-reader" class="w-full rounded-lg overflow-hidden"></div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, nextTick, watch, computed } from 'vue'
import { scanApi } from '@/api/scan.js'

const props = defineProps({
  active: { type: Boolean, default: false },
  uldId: { type: String, default: '' },
  uldNumber: { type: String, default: 'NUEVO' },
})

const emit = defineEmits(['piece-added', 'piece-removed', 'exit-scan', 'update-mawb-pieces', 'ULD_FOUND'])

const scanInput = ref(null)
const scanCode = ref('')
const processing = ref(false)
const scanning = ref(false)
const lastResult = ref(null)
const flashClass = ref('')
const history = ref([])
const showCamera = ref(false)
const canUndo = computed(() => history.value.length > 0 && history.value[0].success)

// Auto-focus when activated
watch(() => props.active, async (val) => {
  if (val) {
    await nextTick()
    scanInput.value?.focus()
  }
})

async function processScan() {
  const code = scanCode.value?.trim()
  if (!code || processing.value) return

  processing.value = true
  scanning.value = true

  try {
    // Check if it looks like a ULD number
    const isUld = /^(PMC|PAH|PAG|PAJ|AAY|AAZ|AAD|PIP|AMP|AMJ|PMH)-/i.test(code)

    if (isUld && !props.uldId) {
      // Lookup ULD
      const res = await scanApi.lookup(code)
      if (res.data.type === 'ULD') {
        lastResult.value = { success: true, message: `ULD ${res.data.uldNumber} encontrada`, awbNumber: code }
        flash('emerald')
        // Emit event to navigate to this ULD
        emit('ULD_FOUND', res.data)
      }
    } else {
      // Register piece
      const res = await scanApi.piece({
        uldId: props.uldId,
        awbNumber: code,
        source: 'BARCODE',
      })

      if (res.data.success) {
        lastResult.value = {
          success: true,
          message: `Pieza #${res.data.pieceNumber} — ${res.data.awbNumber}`,
          awbNumber: res.data.awbNumber,
          pieceNumber: res.data.pieceNumber,
          totalOnUld: res.data.totalOnUld,
          maxAllowed: res.data.totalOnUld + res.data.availablePieces,
          time: 'ahora'
        }
        history.value.unshift(lastResult.value)
        if (history.value.length > 10) history.value.pop()
        flash('emerald')
        emit('piece-added', res.data)
      } else {
        lastResult.value = { success: false, error: res.data.error, awbNumber: code, time: 'ahora' }
        history.value.unshift(lastResult.value)
        if (history.value.length > 10) history.value.pop()
        flash('red')
      }
    }
  } catch (e) {
    const msg = e.response?.data?.error || e.message || 'Error de conexión'
    lastResult.value = { success: false, error: msg, awbNumber: code, time: 'ahora' }
    history.value.unshift(lastResult.value)
    if (history.value.length > 10) history.value.pop()
    flash('red')
  } finally {
    processing.value = false
    scanning.value = false
    scanCode.value = ''
    await nextTick()
    scanInput.value?.focus()
  }
}

async function undoLast() {
  if (!canUndo.value) return
  const last = history.value[0]
  try {
    await scanApi.undoLast(props.uldId, last.mawbId)
    history.value.shift()
    lastResult.value = { success: true, message: 'Última pieza eliminada', awbNumber: last.awbNumber }
    flash('amber')
    emit('piece-removed', { awbNumber: last.awbNumber, pieceNumber: last.pieceNumber })
  } catch {
    lastResult.value = { success: false, error: 'No se pudo deshacer', awbNumber: last.awbNumber }
  }
}

function flash(color) {
  flashClass.value = `border-${color}-400 bg-${color}-50`
  setTimeout(() => { flashClass.value = '' }, 600)
}
</script>
