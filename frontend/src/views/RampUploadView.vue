<template>
  <div class="p-4 bg-slate-100 min-h-screen font-mono flex items-center justify-center select-none">
    <div class="p-6 bg-white border border-slate-300 rounded-lg shadow-pencil max-w-xl w-full">
      
      <div class="border-b pb-3 mb-4 flex justify-between items-center">
        <div>
          <h3 class="text-[13px] font-black text-slate-900 uppercase tracking-wider">
            Ingesta Masiva // Manifiesto de Rampa
          </h3>
          <p class="text-xs text-slate-500 mt-0.5">MÓDULO: LOAD PLANNING & WEIGHT CONTROL</p>
        </div>
        <span class="bg-slate-950 text-white text-[9px] font-black px-2 py-0.5 rounded uppercase tracking-widest">
          Excel Parser Active
        </span>
      </div>

      <div 
        @dragover.prevent="isDragging = true"
        @dragleave.prevent="isDragging = false"
        @drop.prevent="handleFileDrop"
        :class="[
          isDragging ? 'border-slate-500 bg-slate-50/40' : 'border-slate-300 bg-slate-50/50',
          selectedFile ? 'border-solid' : 'border-dashed'
        ]"
        class="border-2 rounded-lg p-8 transition-all text-center flex flex-col items-center justify-center cursor-pointer group"
        @click="$refs.fileInput.click()"
      >
        <input 
          type="file" 
          ref="fileInput" 
          class="hidden" 
          accept=".xlsx, .xls" 
          @change="handleFileSelect" 
        />

        <div class="space-y-2">
          <div v-if="!selectedFile" class="text-slate-400 group-hover:text-slate-600 transition-colors">
            📊 <span class="text-xs font-black block mt-1 uppercase tracking-wider">Arrastra el archivo de Rampa aquí</span>
            <span class="text-[9px] block text-slate-400">Formatos aceptados: .XLSX / .XLS con celdas combinadas</span>
          </div>
          
          <div v-else class="text-slate-700 font-black text-xs">
            📄 {{ selectedFile.name }}
            <span class="text-[9px] block text-slate-400 font-normal mt-1">
              Size: {{ (selectedFile.size / 1024).toFixed(2) }} KB
            </span>
          </div>
        </div>
      </div>

      <div class="mt-4 space-y-2 text-xs">
        <div v-if="uploading" class="p-3 bg-slate-50 border-l-4 border-l-slate-500 text-slate-950 rounded font-bold animate-pulse">
          ⏳ PARSEANDO MATRIZ EN LA JVM... DESCOMBINANDO REGIONES Y CALCULANDO LIBRAS...
        </div>

        <div v-if="serverError" class="p-3 bg-slate-50 border-l-4 border-l-slate-500 text-slate-950 rounded font-black uppercase leading-relaxed text-xs">
          ❌ {{ serverError }}
        </div>

        <div v-if="successMessage" class="p-3 bg-slate-50 border-l-4 border-l-slate-500 text-slate-950 rounded font-black uppercase text-xs">
          ✔ {{ successMessage }}
        </div>
      </div>

      <button 
        @click="processRampManifest"
        :disabled="!selectedFile || uploading"
        :class="selectedFile && !uploading ? 'bg-slate-950 hover:bg-slate-900 text-white cursor-pointer' : 'bg-slate-200 text-slate-400 cursor-not-allowed'"
        class="w-full font-black uppercase tracking-widest py-2.5 rounded text-xs transition-colors mt-4"
      >
        Sincronizar con Base de Datos
      </button>

    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import axios from 'axios'
import api from '../api/client'
import { useToastStore } from '../stores/toast'
import { extractError } from '../utils/error'

const toast = useToastStore()

// IDs de prueba consistentes con tu base de datos relacional
const flightId = ref('33500000-0000-0000-0000-000000000000') // Reemplazar dinámicamente con router.params
const airlineId = ref('11111111-1111-1111-1111-111111111111') // Vendor/Tenant ID actual

const isDragging = ref(false)
const selectedFile = ref(null)
const uploading = ref(false)
const serverError = ref(null)
const successMessage = ref(null)

function handleFileSelect(event) {
  const files = event.target.files
  if (files.length > 0) {
    selectedFile.value = files[0]
    resetAlerts()
  }
}

function handleFileDrop(event) {
  isDragging.value = false
  const files = event.dataTransfer.files
  if (files.length > 0) {
    const file = files[0]
    if (file.name.endsWith('.xlsx') || file.name.endsWith('.xls')) {
      selectedFile.value = file
      resetAlerts()
    } else {
      serverError.value = 'TIPO DE ARCHIVO NO VALIDO. DEBE SER UN EXCEL DE CARGO (.XLSX).'
    }
  }
}

function resetAlerts() {
  serverError.value = null
  successMessage.value = null
}

async function processRampManifest() {
  if (!selectedFile.value) return

  uploading.value = true
  resetAlerts()

  // Construcción del payload Multipart
  const formData = new FormData()
  formData.append('file', selectedFile.value)
  formData.append('airlineId', airlineId.value)

  try {
    const url = `/load-planning/flight/${flightId.value}/upload-manifest`
    const response = await api.post(url, formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })

    if (response.data.success) {
      successMessage.value = response.data.message
      selectedFile.value = null // Limpiar archivo tras éxito
    }
  } catch (error) {
    toast.error(extractError(error))
    if (error.response && error.response.data && error.response.data.error) {
      serverError.value = error.response.data.error
    } else {
      serverError.value = 'ERROR CRÍTICO: No se pudo conectar con el microservicio de Load Planning o el formato de celdas falló.'
    }
  } finally {
    uploading.value = false
  }
}
</script>

<style scoped>
.shadow-pencil { box-shadow: 0px 1px 2px rgba(15, 32, 67, 0.05), 1px 3px 6px rgba(15, 32, 67, 0.04); }
</style>
