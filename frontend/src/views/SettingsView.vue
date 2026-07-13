<template>
  <div class="p-3 md:p-6 max-w-6xl mx-auto">
    <div class="flex flex-col sm:flex-row items-start sm:items-center justify-between mb-5 gap-2">
      <h1 class="text-[13px] font-black tracking-tight text-slate-950 uppercase font-mono">Configuración</h1>
    </div>

    <!-- Tabs -->
    <div class="flex gap-1 mb-4">
      <button @click="activeTab = 'users'"
        class="px-4 py-1.5 rounded text-xs font-semibold transition-all"
        :style="activeTab === 'users'
          ? { background: 'var(--accent)', color: 'white' }
          : { background: 'var(--bg)', color: 'var(--muted)' }">
        Usuarios
      </button>
      <button v-if="auth.role === 'SUPER_USER'" @click="activeTab = 'sites'"
        class="px-4 py-1.5 rounded text-xs font-semibold transition-all"
        :style="activeTab === 'sites'
          ? { background: 'var(--accent)', color: 'white' }
          : { background: 'var(--bg)', color: 'var(--muted)' }">
        Sitios
      </button>
    </div>

    <!-- ============ USERS TAB ============ -->
    <template v-if="activeTab === 'users'">
      <div class="flex items-center justify-between mb-3">
        <div class="flex items-center gap-3">
          <div class="relative flex-1 max-w-xs">
            <input v-model="searchQuery" placeholder="Buscar por email o nombre..."
              class="w-full px-3 py-1.5 rounded text-xs outline-none"
              style="background: var(--bg); color: var(--text); border: 1px solid var(--border)">
          </div>
          <span class="text-xs" style="color: var(--muted)">{{ filteredUsers.length }} usuarios</span>
        </div>
        <button @click="openCreate"
          class="px-3 py-1.5 rounded text-xs font-semibold transition-all hover:brightness-110 active:scale-[0.98]"
          style="background: var(--accent); color: white">
          + Nuevo usuario
        </button>
      </div>

      <!-- Users table -->
      <div class="rounded-lg overflow-hidden" style="background: var(--surface); border: 1px solid var(--border)">
        <div class="table-scroll-wrapper">
        <table class="w-full text-xs" style="min-width: 800px">
          <thead>
            <tr style="background: var(--accent); color: white">
              <th class="text-left px-4 py-2.5 font-semibold">Email</th>
              <th class="text-left px-4 py-2.5 font-semibold">Nombre</th>
              <th class="text-left px-4 py-2.5 font-semibold">Rol</th>
              <th class="text-left px-4 py-2.5 font-semibold">Sitios</th>
              <th class="text-center px-4 py-2.5 font-semibold" style="width: 80px">Activo</th>
              <th class="text-center px-4 py-2.5 font-semibold" style="width: 100px">Contraseña</th>
              <th class="text-right px-4 py-2.5 font-semibold" style="width: 180px">Acciones</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="user in filteredUsers" :key="user.id"
              class="border-t transition-colors hover:bg-slate-50"
              style="border-color: var(--border)">
              <td class="px-4 py-2.5" style="color: var(--text)">{{ user.email }}</td>
              <td class="px-4 py-2.5" style="color: var(--text)">{{ user.fullName }}</td>
              <td class="px-4 py-2.5">
                <span class="text-[10px] font-medium px-2 py-0.5 rounded"
                  style="background: var(--bg); color: var(--text)">
                  {{ roleLabel(user.role) }}
                </span>
              </td>
              <td class="px-4 py-2.5">
                <span v-for="site in userSiteNames(user.siteIds)" :key="site"
                  class="inline-block text-[10px] font-medium px-1.5 py-0.5 rounded mr-1 mb-0.5"
                  style="background: var(--bg); color: var(--text)">
                  {{ site }}
                </span>
                <span v-if="!user.siteIds?.length" class="text-[10px]" style="color: var(--muted)">—</span>
              </td>
              <td class="px-4 py-2.5 text-center">
                <span class="text-[10px] font-medium px-2 py-0.5 rounded"
                  :style="user.isActive
                    ? { background: '#e5e5e5', color: '#111111' }
                    : { background: '#f5f5f5', color: '#999999' }">
                  {{ user.isActive ? 'Sí' : 'No' }}
                </span>
              </td>
              <td class="px-4 py-2.5 text-center">
                <span class="text-[10px] font-medium"
                  :style="{ color: user.hasPasswordSet ? 'var(--text)' : 'var(--muted)' }">
                  {{ user.hasPasswordSet ? 'Establecida' : 'Pendiente' }}
                </span>
              </td>
              <td class="px-4 py-2.5 text-right">
                <div class="flex gap-1 justify-end">
                  <button @click="startEdit(user)"
                    class="px-2 py-1 rounded text-[10px] font-medium transition-all hover:brightness-110"
                    style="background: var(--bg); color: var(--text)">Editar</button>
                  <button @click="resetPass(user)"
                    class="px-2 py-1 rounded text-[10px] font-medium transition-all hover:brightness-110"
                    style="background: var(--bg); color: var(--text)">Reset pass</button>
                  <button @click="removeUser(user)"
                    class="px-2 py-1 rounded text-[10px] font-medium transition-all hover:brightness-110"
                    style="background: var(--bg); color: var(--muted)">Eliminar</button>
                </div>
              </td>
            </tr>
            <tr v-if="filteredUsers.length === 0">
              <td colspan="7" class="px-4 py-8 text-center text-xs italic" style="color: var(--muted)">
                No hay usuarios
              </td>
            </tr>
          </tbody>
        </table>
        </div>
      </div>

      <!-- Edit modal -->
      <div v-if="editingUser" class="fixed inset-0 z-50 flex items-center justify-center p-3"
        style="background: rgba(0,0,0,0.4)">
        <div class="w-full max-w-md p-4 md:p-6 rounded-xl shadow-xl max-h-[90vh] overflow-y-auto" style="background: var(--surface); border: 1px solid var(--border)">
          <h2 class="text-sm font-bold mb-4" style="color: var(--text)">Editar usuario</h2>
          <div class="space-y-3">
            <div>
              <label class="block text-[10px] font-medium mb-0.5" style="color: var(--muted)">Email</label>
              <input v-model="editForm.email" type="email"
                class="w-full px-3 py-1.5 rounded text-xs outline-none"
                style="background: var(--bg); color: var(--text); border: 1px solid var(--border)">
            </div>
            <div>
              <label class="block text-[10px] font-medium mb-0.5" style="color: var(--muted)">Nombre</label>
              <input v-model="editForm.fullName"
                class="w-full px-3 py-1.5 rounded text-xs outline-none"
                style="background: var(--bg); color: var(--text); border: 1px solid var(--border)">
            </div>
            <div>
              <label class="block text-[10px] font-medium mb-0.5" style="color: var(--muted)">Rol</label>
              <select v-model="editForm.role"
                class="w-full px-3 py-1.5 rounded text-xs outline-none"
                style="background: var(--bg); color: var(--text); border: 1px solid var(--border)">
                <option v-for="r in roles" :key="r" :value="r">{{ roleLabel(r) }}</option>
              </select>
            </div>
            <div>
              <label class="block text-[10px] font-medium mb-0.5" style="color: var(--muted)">Sitios</label>
              <div class="space-y-1 max-h-32 overflow-y-auto">
                <label v-for="site in allSites" :key="site.id"
                  class="flex items-center gap-2 text-xs cursor-pointer" style="color: var(--text)">
                  <input type="checkbox" :value="site.id" v-model="editForm.siteIds"
                    class="rounded border-slate-300">
                  {{ site.name }} ({{ site.code }})
                </label>
              </div>
            </div>
            <div>
              <label class="block text-[10px] font-medium mb-0.5" style="color: var(--muted)">Activo</label>
              <select v-model="editForm.isActive"
                class="w-full px-3 py-1.5 rounded text-xs outline-none"
                style="background: var(--bg); color: var(--text); border: 1px solid var(--border)">
                <option :value="true">Sí</option>
                <option :value="false">No</option>
              </select>
            </div>
            <div class="flex gap-2 pt-2">
              <button @click="saveEdit"
                class="flex-1 py-1.5 rounded text-xs font-semibold transition-all hover:brightness-110"
                style="background: var(--accent); color: white">Guardar</button>
              <button @click="cancelEdit"
                class="flex-1 py-1.5 rounded text-xs font-semibold transition-all hover:brightness-110"
                style="background: var(--bg); color: var(--text)">Cancelar</button>
            </div>
          </div>
        </div>
      </div>

      <!-- Create modal -->
      <div v-if="showCreate" class="fixed inset-0 z-50 flex items-center justify-center"
        style="background: rgba(0,0,0,0.4)">
        <div class="w-full max-w-md p-6 rounded-xl shadow-xl" style="background: var(--surface); border: 1px solid var(--border)">
          <h2 class="text-sm font-bold mb-4" style="color: var(--text)">Nuevo usuario</h2>
          <div class="space-y-3">
            <div>
              <label class="block text-[10px] font-medium mb-0.5" style="color: var(--muted)">Email</label>
              <input v-model="createForm.email" type="email" required
                class="w-full px-3 py-1.5 rounded text-xs outline-none"
                style="background: var(--bg); color: var(--text); border: 1px solid var(--border)">
            </div>
            <div>
              <label class="block text-[10px] font-medium mb-0.5" style="color: var(--muted)">Nombre</label>
              <input v-model="createForm.fullName" required
                class="w-full px-3 py-1.5 rounded text-xs outline-none"
                style="background: var(--bg); color: var(--text); border: 1px solid var(--border)">
            </div>
            <div>
              <label class="block text-[10px] font-medium mb-0.5" style="color: var(--muted)">Rol</label>
              <select v-model="createForm.role"
                class="w-full px-3 py-1.5 rounded text-xs outline-none"
                style="background: var(--bg); color: var(--text); border: 1px solid var(--border)">
                <option v-for="r in roles" :key="r" :value="r">{{ roleLabel(r) }}</option>
              </select>
            </div>
            <div>
              <label class="block text-[10px] font-medium mb-0.5" style="color: var(--muted)">Sitios</label>
              <div class="space-y-1 max-h-32 overflow-y-auto">
                <label v-for="site in allSites" :key="site.id"
                  class="flex items-center gap-2 text-xs cursor-pointer" style="color: var(--text)">
                  <input type="checkbox" :value="site.id" v-model="createForm.siteIds"
                    class="rounded border-slate-300">
                  {{ site.name }} ({{ site.code }})
                </label>
              </div>
            </div>
            <div class="flex gap-2 pt-2">
              <button @click="saveCreate"
                class="flex-1 py-1.5 rounded text-xs font-semibold transition-all hover:brightness-110"
                style="background: var(--accent); color: white">Crear</button>
              <button @click="showCreate = false"
                class="flex-1 py-1.5 rounded text-xs font-semibold transition-all hover:brightness-110"
                style="background: var(--bg); color: var(--text)">Cancelar</button>
            </div>
          </div>
        </div>
      </div>
    </template>

    <!-- ============ SITES TAB (SuperUser only) ============ -->
    <template v-if="activeTab === 'sites'">
      <div class="flex items-center justify-between mb-3">
        <span class="text-xs" style="color: var(--muted)">{{ allSites.length }} sitios</span>
        <button @click="openSiteCreate"
          class="px-3 py-1.5 rounded text-xs font-semibold transition-all hover:brightness-110 active:scale-[0.98]"
          style="background: var(--accent); color: white">
          + Nuevo sitio
        </button>
      </div>

      <!-- Sites table -->
      <div class="rounded-lg overflow-hidden" style="background: var(--surface); border: 1px solid var(--border)">
        <div class="table-scroll-wrapper">
        <table class="w-full text-xs" style="min-width: 500px">
          <thead>
            <tr style="background: var(--accent); color: white">
              <th class="text-left px-4 py-2.5 font-semibold">Código</th>
              <th class="text-left px-4 py-2.5 font-semibold">Nombre</th>
              <th class="text-left px-4 py-2.5 font-semibold">País</th>
              <th class="text-center px-4 py-2.5 font-semibold" style="width: 80px">Activo</th>
              <th class="text-right px-4 py-2.5 font-semibold" style="width: 140px">Acciones</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="site in allSites" :key="site.id"
              class="border-t transition-colors hover:bg-slate-50"
              style="border-color: var(--border)">
              <td class="px-4 py-2.5 font-mono font-semibold" style="color: var(--text)">{{ site.code }}</td>
              <td class="px-4 py-2.5" style="color: var(--text)">{{ site.name }}</td>
              <td class="px-4 py-2.5" style="color: var(--text)">{{ site.country || '—' }}</td>
              <td class="px-4 py-2.5 text-center">
                <span class="text-[10px] font-medium px-2 py-0.5 rounded"
                  :style="site.isActive
                    ? { background: '#e5e5e5', color: '#111111' }
                    : { background: '#f5f5f5', color: '#999999' }">
                  {{ site.isActive ? 'Sí' : 'No' }}
                </span>
              </td>
              <td class="px-4 py-2.5 text-right">
                <div class="flex gap-1 justify-end">
                  <button @click="startSiteEdit(site)"
                    class="px-2 py-1 rounded text-[10px] font-medium transition-all hover:brightness-110"
                    style="background: var(--bg); color: var(--text)">Editar</button>
                  <button @click="removeSite(site)"
                    class="px-2 py-1 rounded text-[10px] font-medium transition-all hover:brightness-110"
                    style="background: var(--bg); color: var(--muted)">Eliminar</button>
                </div>
              </td>
            </tr>
            <tr v-if="allSites.length === 0">
              <td colspan="5" class="px-4 py-8 text-center text-xs italic" style="color: var(--muted)">
                No hay sitios
              </td>
            </tr>
          </tbody>
        </table>
        </div>
      </div>

      <!-- Site edit modal -->
      <div v-if="editingSite" class="fixed inset-0 z-50 flex items-center justify-center p-3"
        style="background: rgba(0,0,0,0.4)">
        <div class="w-full max-w-md p-4 md:p-6 rounded-xl shadow-xl max-h-[90vh] overflow-y-auto" style="background: var(--surface); border: 1px solid var(--border)">
          <h2 class="text-sm font-bold mb-4" style="color: var(--text)">Editar sitio</h2>
          <div class="space-y-3">
            <div>
              <label class="block text-[10px] font-medium mb-0.5" style="color: var(--muted)">Código</label>
              <input v-model="siteForm.code" maxlength="10" required
                class="w-full px-3 py-1.5 rounded text-xs outline-none font-mono uppercase"
                style="background: var(--bg); color: var(--text); border: 1px solid var(--border)">
            </div>
            <div>
              <label class="block text-[10px] font-medium mb-0.5" style="color: var(--muted)">Nombre</label>
              <input v-model="siteForm.name" required
                class="w-full px-3 py-1.5 rounded text-xs outline-none"
                style="background: var(--bg); color: var(--text); border: 1px solid var(--border)">
            </div>
            <div>
              <label class="block text-[10px] font-medium mb-0.5" style="color: var(--muted)">País</label>
              <input v-model="siteForm.country" maxlength="60"
                class="w-full px-3 py-1.5 rounded text-xs outline-none"
                style="background: var(--bg); color: var(--text); border: 1px solid var(--border)">
            </div>
            <div>
              <label class="block text-[10px] font-medium mb-0.5" style="color: var(--muted)">Activo</label>
              <select v-model="siteForm.isActive"
                class="w-full px-3 py-1.5 rounded text-xs outline-none"
                style="background: var(--bg); color: var(--text); border: 1px solid var(--border)">
                <option :value="true">Sí</option>
                <option :value="false">No</option>
              </select>
            </div>
            <div class="flex gap-2 pt-2">
              <button @click="saveSiteEdit"
                class="flex-1 py-1.5 rounded text-xs font-semibold transition-all hover:brightness-110"
                style="background: var(--accent); color: white">Guardar</button>
              <button @click="editingSite = null"
                class="flex-1 py-1.5 rounded text-xs font-semibold transition-all hover:brightness-110"
                style="background: var(--bg); color: var(--text)">Cancelar</button>
            </div>
          </div>
        </div>
      </div>

      <!-- Site create modal -->
      <div v-if="showSiteCreate" class="fixed inset-0 z-50 flex items-center justify-center"
        style="background: rgba(0,0,0,0.4)">
        <div class="w-full max-w-md p-6 rounded-xl shadow-xl" style="background: var(--surface); border: 1px solid var(--border)">
          <h2 class="text-sm font-bold mb-4" style="color: var(--text)">Nuevo sitio</h2>
          <div class="space-y-3">
            <div>
              <label class="block text-[10px] font-medium mb-0.5" style="color: var(--muted)">Código</label>
              <input v-model="siteCreateForm.code" maxlength="10" required
                class="w-full px-3 py-1.5 rounded text-xs outline-none font-mono uppercase"
                style="background: var(--bg); color: var(--text); border: 1px solid var(--border)">
            </div>
            <div>
              <label class="block text-[10px] font-medium mb-0.5" style="color: var(--muted)">Nombre</label>
              <input v-model="siteCreateForm.name" required
                class="w-full px-3 py-1.5 rounded text-xs outline-none"
                style="background: var(--bg); color: var(--text); border: 1px solid var(--border)">
            </div>
            <div>
              <label class="block text-[10px] font-medium mb-0.5" style="color: var(--muted)">País</label>
              <input v-model="siteCreateForm.country" maxlength="60"
                class="w-full px-3 py-1.5 rounded text-xs outline-none"
                style="background: var(--bg); color: var(--text); border: 1px solid var(--border)">
            </div>
            <div class="flex gap-2 pt-2">
              <button @click="saveSiteCreate"
                class="flex-1 py-1.5 rounded text-xs font-semibold transition-all hover:brightness-110"
                style="background: var(--accent); color: white">Crear</button>
              <button @click="showSiteCreate = false"
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
import { ref, computed, onMounted } from 'vue'
import { usersApi } from '../api/users'
import { sitesApi } from '../api/sites'
import { useAuthStore } from '../stores/auth'
import { useToastStore } from '../stores/toast'
import { extractError } from '../utils/error'

const toast = useToastStore()
const auth = useAuthStore()
const users = ref([])
const allSites = ref([])
const editingUser = ref(null)
const showCreate = ref(false)
const searchQuery = ref('')
const activeTab = ref('users')
const editingSite = ref(null)
const showSiteCreate = ref(false)

const roles = ['READ_ONLY', 'WAREHOUSE_ASSISTANT', 'OPERATIONS', 'TRAFFIC', 'LOAD_PLANNER', 'ADMIN', 'SUPER_USER']

const editForm = ref({ email: '', fullName: '', role: 'READ_ONLY', isActive: true, siteIds: [] })
const createForm = ref({ email: '', fullName: '', role: 'READ_ONLY', siteIds: [] })
const siteForm = ref({ code: '', name: '', country: '', isActive: true })
const siteCreateForm = ref({ code: '', name: '', country: '', isActive: true })

const filteredUsers = computed(() => {
  if (!searchQuery.value) return users.value
  const q = searchQuery.value.toLowerCase()
  return users.value.filter(u =>
    u.email.toLowerCase().includes(q) || (u.fullName || '').toLowerCase().includes(q)
  )
})

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

function userSiteNames(siteIds) {
  if (!siteIds) return []
  return siteIds.map(id => {
    const site = allSites.value.find(s => s.id === id)
    return site ? site.code : id
  })
}

async function loadUsers() {
  try {
    const res = await usersApi.getAll(auth.airlineId)
    users.value = res.data
  } catch (e) { toast.error(extractError(e)) }
}

async function loadSites() {
  try {
    const res = await sitesApi.getAll()
    allSites.value = res.data
  } catch (e) { toast.error(extractError(e)) }
}

function startEdit(user) {
  editingUser.value = user
  editForm.value = {
    email: user.email,
    fullName: user.fullName,
    role: user.role,
    isActive: user.isActive,
    airlineId: user.airlineId,
    siteIds: user.siteIds || [],
  }
}

function cancelEdit() {
  editingUser.value = null
}

async function saveEdit() {
  if (!editingUser.value) return
  const editedId = editingUser.value.id
  try {
    const res = await usersApi.update(editedId, editForm.value)
    editingUser.value = null
    if (editedId === auth.userId) {
      auth.fullName = (res.data?.fullName || editForm.value.fullName)
      auth.persist()
    }
    await loadUsers()
  } catch (e) { toast.error(extractError(e)) }
}

async function removeUser(user) {
  if (!confirm(`¿Eliminar a ${user.email}?`)) return
  try {
    await usersApi.delete(user.id)
    await loadUsers()
  } catch (e) { toast.error(extractError(e)) }
}

async function resetPass(user) {
  if (!confirm(`¿Restablecer contraseña de ${user.email}?`)) return
  try {
    await usersApi.resetPassword(user.id)
    await loadUsers()
  } catch (e) { toast.error(extractError(e)) }
}

function openCreate() {
  createForm.value = { email: '', fullName: '', role: 'READ_ONLY', siteIds: [] }
  showCreate.value = true
}

async function saveCreate() {
  try {
    await usersApi.create({ ...createForm.value, airlineId: auth.airlineId })
    showCreate.value = false
    await loadUsers()
  } catch (e) { toast.error(extractError(e)) }
}

function openSiteCreate() {
  siteCreateForm.value = { code: '', name: '', country: '', isActive: true }
  showSiteCreate.value = true
}

async function saveSiteCreate() {
  try {
    await sitesApi.create(siteCreateForm.value)
    showSiteCreate.value = false
    await loadSites()
  } catch (e) { toast.error(extractError(e)) }
}

function startSiteEdit(site) {
  editingSite.value = site
  siteForm.value = {
    code: site.code,
    name: site.name,
    country: site.country || '',
    isActive: site.isActive,
  }
}

async function saveSiteEdit() {
  if (!editingSite.value) return
  try {
    await sitesApi.update(editingSite.value.id, siteForm.value)
    editingSite.value = null
    await loadSites()
  } catch (e) { toast.error(extractError(e)) }
}

async function removeSite(site) {
  if (!confirm(`¿Eliminar el sitio ${site.name} (${site.code})?`)) return
  try {
    await sitesApi.delete(site.id)
    await loadSites()
  } catch (e) { toast.error(extractError(e)) }
}

onMounted(async () => {
  await loadUsers()
  if (auth.role === 'SUPER_USER') {
    await loadSites()
  }
})
</script>
