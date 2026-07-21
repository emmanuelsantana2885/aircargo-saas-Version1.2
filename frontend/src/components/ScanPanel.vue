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

    <!-- Info for unsaved ULD -->
    <div v-if="!uldId" class="bg-blue-50 border border-blue-200 rounded-lg px-3 py-2 mb-3 flex items-center gap-2">
      <span class="text-blue-600 text-sm">ℹ</span>
      <span class="text-[10px] font-bold text-blue-700 uppercase tracking-wide">
        Escanee el código del ULD para asignar el número. Las piezas de MAWB se habilitan después de guardar.
      </span>
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
      <button @click="openCamera"
              class="text-[10px] font-bold px-3 py-1.5 rounded border border-blue-300 text-blue-600 hover:bg-blue-50 transition-colors ml-auto">
        📷 Cámara
      </button>
    </div>

    <!-- Camera Modal -->
    <div v-if="showCamera" class="fixed inset-0 bg-black/60 z-50 flex items-center justify-center" @click.self="closeCamera">
      <div class="bg-white rounded-xl p-6 max-w-lg w-full mx-4 shadow-2xl">
        <div class="flex items-center justify-between mb-4">
          <span class="text-[12px] font-black text-slate-900 uppercase">Escanear con Cámara</span>
          <button @click="closeCamera" class="text-slate-400 hover:text-red-500 text-lg font-bold">✕</button>
        </div>
        <div id="qr-reader" class="w-full rounded-lg overflow-hidden border border-slate-200" style="min-height: 280px;"></div>
        <p class="text-[10px] text-slate-400 mt-3 text-center">Apunte la cámara al código de barras de la pieza</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, nextTick, watch, computed, onBeforeUnmount } from 'vue'
import { scanApi } from '@/api/scan.js'

const props = defineProps({
  active: { type: Boolean, default: false },
  uldId: { type: String, default: '' },
  uldNumber: { type: String, default: 'NUEVO' },
})

const emit = defineEmits(['piece-added', 'piece-removed', 'exit-scan', 'update-mawb-pieces', 'ULD_FOUND', 'uld-number-scanned'])

const scanInput = ref(null)
const scanCode = ref('')
const processing = ref(false)
const scanning = ref(false)
const lastResult = ref(null)
const flashClass = ref('')
const history = ref([])
const showCamera = ref(false)
const canUndo = computed(() => history.value.length > 0 && history.value[0].success)

let html5QrScanner = null

// Auto-focus when activated
watch(() => props.active, async (val) => {
  if (val) {
    await nextTick()
    scanInput.value?.focus()
  } else {
    closeCamera()
  }
})

// Clean up on unmount
onBeforeUnmount(() => {
  closeCamera()
})

async function processScan(codeOverride) {
  const code = codeOverride || scanCode.value?.trim()
  if (!code || processing.value) return

  processing.value = true
  scanning.value = true

  try {
    const isUld = /^(PMC|PAH|PAG|PAJ|AAY|AAZ|AAD|PIP|AMP|AMJ|PMH)-/i.test(code)

    if (isUld && !props.uldId) {
      lastResult.value = { success: true, message: `ULD ${code} asignado`, awbNumber: code }
      flash('emerald')
      emit('uld-number-scanned', code)
    } else if (!isUld && !props.uldId) {
      lastResult.value = { success: false, error: 'Guarde el ULD primero para registrar piezas MAWB', awbNumber: code, time: 'ahora' }
      history.value.unshift(lastResult.value)
      flash('red')
    } else {
      const res = await scanApi.piece({
        uldId: props.uldId,
        awbNumber: code,
        source: 'BARCODE',
      })

      const entry = { awbNumber: code, time: 'ahora' }
      if (res.data.success) {
        Object.assign(entry, {
          success: true,
          message: `Pieza #${res.data.pieceNumber} — ${res.data.awbNumber}`,
          pieceNumber: res.data.pieceNumber,
          totalOnUld: res.data.totalOnUld,
          maxAllowed: res.data.totalOnUld + res.data.availablePieces,
        })
        flash('emerald')
        emit('piece-added', res.data)
      } else {
        Object.assign(entry, { success: false, error: res.data.error })
        flash('red')
      }
      lastResult.value = entry
      history.value.unshift(entry)
      if (history.value.length > 10) history.value.pop()
    }
  } catch (e) {
    const msg = e.response?.data?.error || e.message || 'Error de conexión'
    const entry = { success: false, error: msg, awbNumber: code, time: 'ahora' }
    lastResult.value = entry
    history.value.unshift(entry)
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

async function openCamera() {
  showCamera.value = true
  await nextTick()

  try {
    const { Html5Qrcode } = await import('html5-qrcode')

    // Stop any existing scanner
    if (html5QrScanner) {
      try { await html5QrScanner.stop() } catch {}
      html5QrScanner.clear()
      html5QrScanner = null
    }

    html5QrScanner = new Html5Qrcode('qr-reader')

    await html5QrScanner.start(
      { facingMode: 'environment' },
      {
        fps: 10,
        qrbox: { width: 280, height: 120 },
        aspectRatio: 2.0,
        showTorchButtonIfSupported: true,
      },
      (decodedText) => {
        // On successful scan
        processScan(decodedText)
      },
      () => {} // ignore scan errors (no barcode in frame)
    )
  } catch (err) {
    console.error('Camera error:', err)
    lastResult.value = { success: false, error: 'No se pudo acceder a la cámara', awbNumber: '', time: 'ahora' }
    flash('red')
    closeCamera()
  }
}

async function closeCamera() {
  if (html5QrScanner) {
    try { await html5QrScanner.stop() } catch {}
    try { html5QrScanner.clear() } catch {}
    html5QrScanner = null
  }
  showCamera.value = false
}

function flash(color) {
  flashClass.value = `border-${color}-400 bg-${color}-50`
  setTimeout(() => { flashClass.value = '' }, 600)
}
</script>
