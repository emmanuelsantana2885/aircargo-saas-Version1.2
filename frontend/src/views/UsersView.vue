<template>
  <div class="p-3 md:p-6 max-w-7xl mx-auto">
    <div class="flex flex-col sm:flex-row items-start sm:items-center justify-between mb-5 gap-2">
      <h1 class="text-[13px] font-black tracking-tight text-slate-950 uppercase font-mono">Usuarios</h1>
      <div class="flex gap-1">
        <button v-for="tab in tabs" :key="tab.key" @click="activeTab = tab.key"
          class="px-4 py-1.5 rounded text-xs font-semibold transition-all"
          :class="activeTab === tab.key
            ? 'bg-black text-white'
            : 'bg-[var(--bg)] text-[var(--muted)]'">
          {{ tab.label }}
        </button>
      </div>
    </div>

    <!-- ────────────── TAB: CONNECTED USERS ────────────── -->
    <template v-if="activeTab === 'connected'">
      <div class="rounded-lg overflow-hidden" style="background: var(--surface); border: 1px solid var(--border)">
        <div class="table-scroll-wrapper">
        <table class="w-full text-xs" style="min-width: 700px">
          <thead>
            <tr style="background: var(--accent); color: white">
              <th class="text-left px-4 py-2.5 font-semibold" style="width: 16px"></th>
              <th class="text-left px-4 py-2.5 font-semibold">Nombre</th>
              <th class="text-left px-4 py-2.5 font-semibold">Email</th>
              <th class="text-left px-4 py-2.5 font-semibold">Rol</th>
              <th class="text-left px-4 py-2.5 font-semibold">Último latido</th>
              <th class="text-left px-4 py-2.5 font-semibold">Último login</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="u in connected" :key="u.userId"
              class="border-t transition-colors" style="border-color: var(--border)"
              @mouseenter="$event.currentTarget.style.background = '#eff6ff'"
              @mouseleave="$event.currentTarget.style.background = ''">
              <td class="px-4 py-2.5"><span class="w-1.5 h-1.5 rounded-full inline-block" style="background: #22c55e"></span></td>
              <td class="px-4 py-2.5 font-medium" style="color: var(--text)">{{ u.fullName || u.email }}</td>
              <td class="px-4 py-2.5" style="color: var(--muted)">{{ u.email }}</td>
              <td class="px-4 py-2.5"><span class="text-[10px] font-medium px-2 py-0.5 rounded" style="background: var(--bg); color: var(--text)">{{ roleLabel(u.role) }}</span></td>
              <td class="px-4 py-2.5 text-[10px]" style="color: var(--muted)">{{ formatDate(u.lastHeartbeat) }}</td>
              <td class="px-4 py-2.5 text-[10px]" style="color: var(--muted)">{{ formatDate(u.lastLogin) }}</td>
            </tr>
            <tr v-if="connected.length === 0">
              <td colspan="6" class="px-4 py-8 text-center text-xs italic" style="color: var(--muted)">No hay usuarios conectados</td>
            </tr>
          </tbody>
        </table>
        </div>
      </div>
    </template>
    <template v-if="activeTab === 'audit'">
      <div class="flex items-center justify-between mb-3">
        <div class="flex gap-2">
          <select v-model="filterUser"
            class="px-2 py-1 rounded text-[10px] outline-none"
            style="background: var(--bg); color: var(--text); border: 1px solid var(--border)">
            <option value="">Todos los usuarios</option>
            <option v-for="u in userOptions" :key="u.id" :value="u.id">{{ u.email }}</option>
          </select>
          <button @click="loadLogs"
            class="px-2 py-1 rounded text-[10px] font-medium transition-all hover:brightness-110"
            style="background: var(--accent); color: white">Actualizar</button>
        </div>
      </div>
      <div class="rounded-lg overflow-hidden" style="background: var(--surface); border: 1px solid var(--border)">
        <div class="table-scroll-wrapper">
        <table class="w-full text-xs" style="min-width: 600px">
          <thead>
            <tr style="background: var(--accent); color: white">
              <th class="text-left px-4 py-2.5 font-semibold">Fecha/Hora</th>
              <th class="text-left px-4 py-2.5 font-semibold">Usuario</th>
              <th class="text-left px-4 py-2.5 font-semibold">Acción</th>
              <th class="text-left px-4 py-2.5 font-semibold">Entidad</th>
              <th class="text-left px-4 py-2.5 font-semibold">Detalle</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="(log, idx) in logs" :key="log.id"
              class="border-t transition-all" style="border-color: var(--border)"
              :style="idx % 2 === 0 ? {} : { background: '#f8fafc' }"
              @mouseenter="$event.currentTarget.style.background = '#dbeafe'"
              @mouseleave="$event.currentTarget.style.background = idx % 2 === 0 ? '' : '#f8fafc'">
              <td class="px-4 py-2 whitespace-nowrap text-[10px]" style="color: var(--muted)">{{ formatDate(log.createdAt) }}</td>
              <td class="px-4 py-2" style="color: var(--text)">{{ log.fullName || log.email || '—' }}</td>
              <td class="px-4 py-2"><span class="px-1.5 py-0.5 rounded text-[10px] font-medium" :style="actionColor(log.action)">{{ log.action }}</span></td>
              <td class="px-4 py-2" style="color: var(--text)">{{ log.entityType || '—' }}</td>
              <td class="px-4 py-2 max-w-xs truncate text-[10px]" style="color: var(--muted)">{{ log.details || '—' }}</td>
            </tr>
            <tr v-if="logs.length === 0">
              <td colspan="5" class="px-4 py-8 text-center text-xs italic" style="color: var(--muted)">No hay transacciones registradas</td>
            </tr>
          </tbody>
        </table>
        </div>
      </div>
    </template>

    <!-- ────────────── TAB: ROLES & PERMISOS ────────────── -->
    <template v-if="activeTab === 'roles'">
      <!-- Role selector -->
      <div class="flex items-center gap-4 mb-4">
        <label class="text-[10px] font-bold uppercase tracking-wider shrink-0" style="color: var(--muted)">Roles del sistema</label>
        <select v-model="selectedRole" @change="onRoleChange"
          class="px-3 py-1.5 rounded text-xs outline-none min-w-[200px] transition-all"
          :style="selectedRole
            ? { background: '#dcfce7', color: '#166534', border: '2px solid #22c55e', fontWeight: '600' }
            : { background: 'var(--surface)', color: 'var(--text)', border: '1px solid var(--border)' }">
          <option value="">— Seleccionar rol —</option>
          <option v-for="r in allRoles" :key="r.role" :value="r.role">
            {{ roleLabel(r.role) }} ({{ countAccess(r.views) }}/{{ r.views.length }})
          </option>
        </select>

        <button @click="showConnectedOnly = !showConnectedOnly"
          class="px-3 py-1.5 rounded text-[10px] font-semibold transition-all flex items-center gap-1.5"
          :style="showConnectedOnly
            ? { background: '#22c55e', color: 'white' }
            : { background: 'var(--bg)', color: 'var(--text)', border: '1px solid var(--border)' }">
          <span class="w-1.5 h-1.5 rounded-full" :style="{ background: showConnectedOnly ? 'white' : '#94a3b8' }"></span>
          {{ showConnectedOnly ? 'Conectados' : 'Todos' }}
        </button>
        <span v-if="roleUsers.length" class="text-[10px]" style="color: var(--muted)">{{ roleUsers.length }} usuario(s)</span>
      </div>

      <div v-if="selectedRole" class="space-y-5">
        <!-- Users by role -->
        <div class="rounded-lg overflow-hidden" style="background: var(--surface); border: 1px solid var(--border)">
          <div class="px-4 py-2 text-[10px] font-bold uppercase tracking-wider flex items-center justify-between"
            style="background: var(--bg); color: var(--muted); border-bottom: 1px solid var(--border)">
            <span>Usuarios conectados con rol {{ roleLabel(selectedRole) }}</span>
          </div>
          <table class="w-full text-xs">
            <thead>
              <tr style="background: var(--accent); color: white">
                <th class="text-left px-4 py-2 font-semibold">Nombre</th>
                <th class="text-left px-4 py-2 font-semibold">Email</th>
                <th class="text-left px-4 py-2 font-semibold">Último Login</th>
                <th class="text-center px-4 py-2 font-semibold" style="width: 100px">Transacciones</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="u in roleUsers" :key="u.userId"
                class="border-t transition-all cursor-pointer"
                :style="selectedUserId === u.userId
                  ? { background: '#dbeafe', borderLeft: '4px solid #2563eb', fontWeight: '600' }
                  : { borderColor: 'var(--border)' }"
                @click="selectUser(u)">
                <td class="px-4 py-2" :style="{ color: selectedUserId === u.userId ? '#1e40af' : 'var(--text)' }">
                  <span class="w-1.5 h-1.5 rounded-full inline-block mr-1.5" style="background: #22c55e"></span>
                  {{ u.fullName || '—' }}
                </td>
                <td class="px-4 py-2" :style="{ color: selectedUserId === u.userId ? '#1e40af' : 'var(--muted)' }">{{ u.email }}</td>
                <td class="px-4 py-2 text-[10px]" :style="{ color: selectedUserId === u.userId ? '#1e40af' : 'var(--muted)' }">{{ formatDate(u.lastHeartbeat) }}</td>
                <td class="px-4 py-2 text-center">
                  <span v-if="selectedUserId === u.userId" class="text-[10px] font-medium px-2 py-0.5 rounded" style="background: #2563eb; color: white">
                    {{ userAuditLogs.length }} eventos
                  </span>
                  <span v-else class="text-[10px] hover:text-blue-600 transition-colors" style="color: var(--muted)">Ver</span>
                </td>
              </tr>
              <tr v-if="!roleUsers.length">
                <td colspan="4" class="px-4 py-8 text-center text-xs italic" style="color: var(--muted)">No hay usuarios con este rol</td>
              </tr>
            </tbody>
          </table>
        </div>

        <!-- User audit transactions -->
        <div v-if="selectedUserId" class="rounded-lg overflow-hidden" style="background: var(--surface); border: 1px solid var(--border)">
          <div class="px-4 py-2 text-[10px] font-bold uppercase tracking-wider flex items-center justify-between"
            style="background: var(--bg); color: var(--muted); border-bottom: 1px solid var(--border)">
            <span>Transacciones de {{ selectedUserFullName }}</span>
            <span>{{ userAuditLogs.length }} registros</span>
          </div>
          <div class="overflow-x-auto">
            <table class="w-full text-xs">
              <thead>
                <tr style="background: var(--accent); color: white">
                  <th class="text-left px-3 py-2 font-semibold" style="width: 90px"># Transacción</th>
                  <th class="text-left px-3 py-2 font-semibold" style="width: 130px">Fecha/Hora</th>
                  <th class="text-left px-3 py-2 font-semibold">Acción</th>
                  <th class="text-left px-3 py-2 font-semibold">Entidad</th>
                  <th class="text-left px-3 py-2 font-semibold" style="width: 100px">ID Entidad</th>
                  <th class="text-left px-3 py-2 font-semibold">Detalle</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="(log, idx) in userAuditLogs" :key="log.id"
                  class="border-t transition-all" style="border-color: var(--border)"
                  :style="idx % 2 === 0 ? {} : { background: '#f8fafc' }"
                  @mouseenter="$event.currentTarget.style.background = '#dbeafe'"
                  @mouseleave="$event.currentTarget.style.background = idx % 2 === 0 ? '' : '#f8fafc'">
                  <td class="px-3 py-2 font-mono text-[9px]" style="color: #2563eb; font-weight: 600">{{ (log.id || '').slice(0, 8) }}</td>
                  <td class="px-3 py-2 whitespace-nowrap text-[10px]" style="color: var(--muted)">{{ formatDate(log.createdAt) }}</td>
                  <td class="px-3 py-2"><span class="px-1.5 py-0.5 rounded text-[9px] font-medium" :style="actionColor(log.action)" style="background: var(--bg)">{{ log.action }}</span></td>
                  <td class="px-3 py-2 text-[10px]" style="color: var(--text)">{{ log.entityType || '—' }}</td>
                  <td class="px-3 py-2 font-mono text-[9px]" style="color: var(--muted)">{{ (log.entityId || '').slice(0, 8) || '—' }}</td>
                  <td class="px-3 py-2 max-w-[200px] truncate text-[10px]" style="color: var(--muted)">{{ log.details || '—' }}</td>
                </tr>
                <tr v-if="!userAuditLogs.length">
                  <td colspan="6" class="px-4 py-8 text-center text-xs italic" style="color: var(--muted)">No hay transacciones registradas para este usuario</td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>

        <!-- Role permission grid -->
        <div v-if="selectedRoleData" class="rounded-lg overflow-hidden" style="background: var(--surface); border: 1px solid var(--border)">
          <div class="px-4 py-2 text-[10px] font-bold uppercase tracking-wider flex items-center justify-between"
            style="background: var(--bg); color: var(--muted); border-bottom: 1px solid var(--border)">
            <span>Permisos del rol {{ roleLabel(selectedRole) }}</span>
            <div class="flex gap-2">
              <button @click="toggleAllViews(true)"
                class="px-2 py-1 rounded text-[9px] font-medium transition-all hover:brightness-110"
                style="background: var(--bg); color: var(--text)">Select All</button>
              <button @click="toggleAllViews(false)"
                class="px-2 py-1 rounded text-[9px] font-medium transition-all hover:brightness-110"
                style="background: var(--bg); color: var(--text)">Deselect All</button>
              <button @click="saveRolePermissions"
                class="px-3 py-1 rounded text-[9px] font-bold transition-all hover:brightness-110"
                :style="hasChanges
                  ? { background: '#16a34a', color: 'white' }
                  : { background: '#e5e7eb', color: '#9ca3af', cursor: 'not-allowed' }"
                :disabled="!hasChanges">
                {{ saving ? 'Guardando...' : 'Guardar Cambios' }}
              </button>
            </div>
          </div>
          <div v-for="cat in categories" :key="cat" class="border-b" style="border-color: var(--border)">
            <div class="px-4 py-1.5 text-[9px] font-bold uppercase tracking-wider" style="background: var(--bg); color: var(--muted)">
              {{ categoryLabel(cat) }}
              <span class="ml-2 font-normal normal-case">{{ catViews(cat).length }} transacciones</span>
            </div>
            <div class="divide-y" style="border-color: var(--border)">
              <div v-for="v in catViews(cat)" :key="v.viewCode"
                class="flex items-center justify-between px-4 py-2 hover:bg-slate-50 transition-colors">
                <div class="flex items-center gap-3 min-w-0">
                  <button @click="toggleView(v.viewCode)"
                    class="w-7 h-4 rounded-sm border transition-all shrink-0 relative flex items-center"
                    :style="localPerms[v.viewCode]
                      ? { background: '#2563eb', borderColor: '#2563eb' }
                      : { background: '#ffffff', borderColor: '#cbd5e1' }">
                    <span class="w-[11px] h-[11px] rounded-sm absolute transition-all"
                      :style="localPerms[v.viewCode]
                        ? { background: '#ffffff', left: '11px' }
                        : { background: '#94a3b8', left: '1px' }"></span>
                  </button>
                  <div class="min-w-0">
                    <div class="text-[10px] font-semibold" style="color: var(--text)">{{ v.viewName }}</div>
                    <div class="text-[9px] truncate max-w-md" style="color: var(--muted)">{{ v.viewDescription }}</div>
                  </div>
                </div>
                <div class="text-[8px] font-mono px-1.5 py-0.5 rounded shrink-0" :class="localPerms[v.viewCode] ? 'bg-green-50 text-green-700' : 'bg-slate-100 text-slate-400'">
                  {{ localPerms[v.viewCode] ? 'AUTHORIZED' : 'RESTRICTED' }}
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- No role selected -->
      <div v-if="!selectedRole" class="flex items-center justify-center h-64 text-xs italic" style="color: var(--muted)">
        Selecciona un rol del menú desplegable para administrar permisos y ver usuarios.
      </div>

      <!-- ── View Master Data: mini table ── -->
      <div class="mt-6 rounded-lg overflow-hidden" style="background: var(--surface); border: 1px solid var(--border)">
        <div class="flex items-center justify-between px-4 py-2" style="background: var(--bg); border-bottom: 1px solid var(--border)">
          <span class="text-[10px] font-bold uppercase tracking-wider" style="color: var(--muted)">Catálogo de Transacciones</span>
          <button @click="openViewEditor(null)"
            class="px-3 py-1.5 rounded text-[10px] font-semibold transition-all hover:brightness-110"
            style="background: var(--accent); color: white">+ Nueva Transacción</button>
        </div>
        <table class="w-full text-xs">
          <thead>
            <tr style="background: var(--accent); color: white">
              <th class="text-left px-4 py-2 font-semibold">Código</th>
              <th class="text-left px-4 py-2 font-semibold">Nombre</th>
              <th class="text-left px-4 py-2 font-semibold">Categoría</th>
              <th class="text-left px-4 py-2 font-semibold">Descripción</th>
              <th class="text-center px-4 py-2 font-semibold" style="width: 100px">Acciones</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="view in allViews" :key="view.id"
              class="border-t hover:bg-slate-50 transition-colors" style="border-color: var(--border)">
              <td class="px-4 py-2 font-mono text-[10px]" style="color: var(--text)">{{ view.code }}</td>
              <td class="px-4 py-2 text-xs font-medium" style="color: var(--text)">{{ view.name }}</td>
              <td class="px-4 py-2 text-[10px]" style="color: var(--muted)">{{ categoryLabel(view.category) }}</td>
              <td class="px-4 py-2 text-[10px]" style="color: var(--muted)">{{ view.description }}</td>
              <td class="px-4 py-2 text-center">
                <div class="flex gap-1 justify-center">
                  <button @click="openViewEditor(view)" class="px-2 py-1 rounded text-[9px] font-medium transition-all hover:brightness-110" style="background: var(--bg); color: var(--text)">Editar</button>
                  <button @click="deleteView(view)" class="px-2 py-1 rounded text-[9px] font-medium transition-all hover:brightness-110" style="background: var(--bg); color: var(--muted)">Eliminar</button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- ── View Editor Modal ── -->
      <div v-if="showViewEditor" class="fixed inset-0 z-50 flex items-center justify-center" style="background: rgba(0,0,0,0.4)">
        <div class="w-full max-w-md p-6 rounded-xl shadow-xl" style="background: var(--surface); border: 1px solid var(--border)">
          <h2 class="text-sm font-bold mb-4" style="color: var(--text)">{{ editingView ? 'Editar Transacción' : 'Nueva Transacción' }}</h2>
          <div class="space-y-3">
            <div>
              <label class="block text-[10px] font-medium mb-0.5" style="color: var(--muted)">Código *</label>
              <input v-model="viewForm.code" maxlength="50" placeholder="NUEVA_VISTA"
                class="w-full px-3 py-1.5 rounded text-xs outline-none font-mono"
                style="background: var(--bg); color: var(--text); border: 1px solid var(--border)">
            </div>
            <div>
              <label class="block text-[10px] font-medium mb-0.5" style="color: var(--muted)">Nombre *</label>
              <input v-model="viewForm.name" maxlength="100" placeholder="Nombre de la vista"
                class="w-full px-3 py-1.5 rounded text-xs outline-none"
                style="background: var(--bg); color: var(--text); border: 1px solid var(--border)">
            </div>
            <div>
              <label class="block text-[10px] font-medium mb-0.5" style="color: var(--muted)">Categoría</label>
              <select v-model="viewForm.category"
                class="w-full px-3 py-1.5 rounded text-xs outline-none"
                style="background: var(--bg); color: var(--text); border: 1px solid var(--border)">
                <option value="PRINCIPAL">Principal</option>
                <option value="OPERACIONES">Operaciones</option>
                <option value="CONFIGURACION">Configuración</option>
                <option value="ADMINISTRACION">Administración</option>
              </select>
            </div>
            <div>
              <label class="block text-[10px] font-medium mb-0.5" style="color: var(--muted)">Descripción</label>
              <input v-model="viewForm.description" maxlength="255" placeholder="Descripción de la transacción"
                class="w-full px-3 py-1.5 rounded text-xs outline-none"
                style="background: var(--bg); color: var(--text); border: 1px solid var(--border)">
            </div>
            <div class="flex gap-2 pt-2">
              <button @click="saveView"
                class="flex-1 py-1.5 rounded text-xs font-semibold transition-all hover:brightness-110"
                style="background: var(--accent); color: white">Guardar</button>
              <button @click="showViewEditor = false"
                class="flex-1 py-1.5 rounded text-xs font-semibold transition-all hover:brightness-110"
                style="background: var(--bg); color: var(--text)">Cancelar</button>
            </div>
          </div>
        </div>
      </div>
    </template>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { usersApi } from '../api/users'
import { rolesApi } from '../api/roles'
import { useAuthStore } from '../stores/auth'
import { useToastStore } from '../stores/toast'
import { extractError } from '../utils/error'

const toast = useToastStore()
const auth = useAuthStore()

const activeTab = ref('connected')
const tabs = computed(() => {
  const t = [
    { key: 'connected', label: 'Conectados' },
    { key: 'audit', label: 'Auditoría' },
  ]
  if (auth.role === 'SUPER_USER') {
    t.push({ key: 'roles', label: 'Roles y Permisos' })
  }
  return t
})

const connected = ref([])
const logs = ref([])
const userOptions = ref([])
const filterUser = ref('')
const allRoles = ref([])
const allViews = ref([])
const selectedRole = ref(null)
const localPerms = ref({})
const saving = ref(false)
const showViewEditor = ref(false)
const editingView = ref(null)
const viewForm = ref({ code: '', name: '', description: '', category: 'PRINCIPAL' })
const allUsers = ref([])
const roleUsers = ref([])
const selectedUserId = ref(null)
const userAuditLogs = ref([])
const showConnectedOnly = ref(true)

const selectedUserFullName = computed(() => {
  if (!selectedUserId.value) return ''
  const u = connected.value.find(u => u.userId === selectedUserId.value)
  return u ? (u.fullName || u.email) : ''
})

const selectedRoleData = computed(() => {
  if (!selectedRole.value) return null
  return allRoles.value.find(r => r.role === selectedRole.value) || null
})

const categories = computed(() => {
  if (!selectedRoleData.value) return []
  const cats = [...new Set(selectedRoleData.value.views.map(v => v.category))]
  return cats.sort()
})

function catViews(cat) {
  if (!selectedRoleData.value) return []
  return selectedRoleData.value.views.filter(v => v.category === cat)
}

const hasChanges = computed(() => {
  if (!selectedRoleData.value) return false
  return selectedRoleData.value.views.some(v => localPerms.value[v.viewCode] !== v.canAccess)
})

function countAccess(views) {
  return views.filter(v => v.canAccess).length
}

function categoryLabel(cat) {
  const labels = {
    PRINCIPAL: 'Principal',
    OPERACIONES: 'Operaciones',
    CONFIGURACION: 'Configuración',
    ADMINISTRACION: 'Administración',
  }
  return labels[cat] || cat
}

function roleLabel(r) {
  const labels = {
    READ_ONLY: 'Solo Lectura',
    WAREHOUSE_ASSISTANT: 'Warehouse Asst',
    OPERATIONS: 'Operations',
    TRAFFIC: 'Traffic',
    LOAD_PLANNER: 'Load Planner',
    ADMIN: 'Admin',
    SUPER_USER: 'SuperUser',
  }
  return labels[r] || r
}

function formatDate(ts) {
  if (!ts) return '—'
  const d = new Date(ts)
  return d.toLocaleDateString('es-DO', {
    day: '2-digit', month: '2-digit', year: 'numeric',
    hour: '2-digit', minute: '2-digit',
  })
}

function actionColor(action) {
  if (!action) return {}
  const a = action.toUpperCase()
  if (a.includes('CREATE') || a.includes('CREAR')) return { background: '#dcfce7', color: '#166534' }
  if (a.includes('UPDATE') || a.includes('EDIT') || a.includes('ACTUALIZ')) return { background: '#dbeafe', color: '#1e40af' }
  if (a.includes('DELETE') || a.includes('ELIMIN')) return { background: '#fee2e2', color: '#991b1b' }
  if (a.includes('LOGIN') || a.includes('PASSWORD') || a.includes('RESET')) return { background: '#fef3c7', color: '#92400e' }
  return { background: '#f1f5f9', color: '#475569' }
}

// ── Tab: Connected ──
async function loadConnected() {
  try {
    const res = await usersApi.getConnected()
    connected.value = res.data
  } catch (e) { toast.error(extractError(e)) }
}

// ── Tab: Audit ──
async function loadLogs() {
  try {
    const res = await usersApi.getAuditLogs(filterUser.value || undefined)
    logs.value = res.data
  } catch (e) { toast.error(extractError(e)) }
}

async function loadUserOptions() {
  try {
    const res = await usersApi.getAll()
    userOptions.value = res.data
  } catch (e) { toast.error(extractError(e)) }
}

// ── Tab: Roles ──
async function loadRoles() {
  try {
    const res = await rolesApi.getAllRoles()
    allRoles.value = res.data
  } catch (e) { toast.error(extractError(e)) }
}

async function loadViews() {
  try {
    const res = await rolesApi.getAllViews()
    allViews.value = res.data
  } catch (e) { toast.error(extractError(e)) }
}

async function loadAllUsers() {
  try {
    const res = await usersApi.getAll()
    allUsers.value = res.data
  } catch (e) { toast.error(extractError(e)) }
}

function filterRoleUsers() {
  if (!selectedRole.value) {
    roleUsers.value = []
    return
  }
  if (showConnectedOnly.value) {
    roleUsers.value = connected.value.filter(u => u.role === selectedRole.value)
  } else {
    roleUsers.value = allUsers.value.filter(u => u.role === selectedRole.value)
  }
}

function onRoleChange() {
  selectedUserId.value = null
  userAuditLogs.value = []
  if (!selectedRole.value) {
    roleUsers.value = []
    localPerms.value = {}
    return
  }
  filterRoleUsers()
  const data = allRoles.value.find(r => r.role === selectedRole.value)
  if (data) {
    localPerms.value = {}
    for (const v of data.views) {
      localPerms.value[v.viewCode] = v.canAccess
    }
  }
}

async function selectUser(u) {
  selectedUserId.value = u.userId
  try {
    const res = await usersApi.getAuditLogs(u.userId)
    userAuditLogs.value = res.data
  } catch (e) {
    toast.error(extractError(e))
    userAuditLogs.value = []
  }
}

function toggleView(viewCode) {
  localPerms.value[viewCode] = !localPerms.value[viewCode]
}

function toggleAllViews(val) {
  if (!selectedRoleData.value) return
  for (const v of selectedRoleData.value.views) {
    localPerms.value[v.viewCode] = val
  }
}

async function saveRolePermissions() {
  if (!selectedRole.value || !hasChanges.value) return
  saving.value = true
  try {
    await rolesApi.updateRole(selectedRole.value, localPerms.value)
      toast.success(`Permisos actualizados para ${roleLabel(selectedRole.value)}`)
    await loadRoles()
    onRoleChange()
  } catch (e) { toast.error(extractError(e)) }
  finally { saving.value = false }
}

// View CRUD
function openViewEditor(view) {
  editingView.value = view
  if (view) {
    viewForm.value = { code: view.code, name: view.name, description: view.description || '', category: view.category }
  } else {
    viewForm.value = { code: '', name: '', description: '', category: 'PRINCIPAL' }
  }
  showViewEditor.value = true
}

async function saveView() {
  if (!viewForm.value.code || !viewForm.value.name) {
    toast.error('Código y nombre son requeridos')
    return
  }
  try {
    if (editingView.value) {
      await rolesApi.updateView(editingView.value.id, viewForm.value)
      toast.success('Transacción actualizada')
    } else {
      await rolesApi.createView(viewForm.value)
      toast.success('Transacción creada')
    }
    showViewEditor.value = false
    await Promise.all([loadViews(), loadRoles()])
    if (selectedRole.value) onRoleChange()
  } catch (e) { toast.error(extractError(e)) }
}

async function deleteView(view) {
  if (!confirm(`¿Eliminar la transacción "${view.name}" (${view.code})?`)) return
  try {
    await rolesApi.deleteView(view.id)
    toast.success('Transacción eliminada')
    await Promise.all([loadViews(), loadRoles()])
    if (selectedRole.value) onRoleChange()
  } catch (e) { toast.error(extractError(e)) }
}

watch(showConnectedOnly, () => {
  if (selectedRole.value) filterRoleUsers()
})

const tabsLoaded = { connected: true, audit: false, roles: false }

watch(activeTab, (tab) => {
  if (tab === 'audit' && !tabsLoaded.audit) {
    tabsLoaded.audit = true
    loadLogs()
    loadUserOptions()
  }
  if (tab === 'roles' && !tabsLoaded.roles && auth.role === 'SUPER_USER') {
    tabsLoaded.roles = true
    Promise.all([loadRoles(), loadViews(), loadAllUsers()])
  }
})

onMounted(async () => {
  loadConnected()
  setInterval(loadConnected, 30000)
})
</script>
