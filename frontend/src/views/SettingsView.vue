<template>
  <div class="ds-page max-w-6xl mx-auto">
    <div class="ds-section-header mb-4">
      <h1 class="ds-title">System Settings</h1>
    </div>

    <!-- Tabs -->
    <div class="flex gap-1 mb-4">
      <button @click="activeTab = 'users'"
        :class="activeTab === 'users' ? 'ds-btn-primary' : 'ds-btn-secondary'">
        Usuarios
      </button>
      <button v-if="auth.role === 'SUPER_USER'" @click="activeTab = 'sites'"
        :class="activeTab === 'sites' ? 'ds-btn-primary' : 'ds-btn-secondary'">
        Sitios
      </button>
      <button v-if="canManageSettings" @click="activeTab = 'airlines'"
        :class="activeTab === 'airlines' ? 'ds-btn-primary' : 'ds-btn-secondary'">
        Aerolíneas
      </button>
      <button v-if="canManageSettings" @click="activeTab = 'uldconfig'"
        :class="activeTab === 'uldconfig' ? 'ds-btn-primary' : 'ds-btn-secondary'">
        Config ULD
      </button>

    </div>

    <!-- ============ USERS TAB ============ -->
    <template v-if="activeTab === 'users'">
      <div class="flex items-center justify-between mb-3">
        <div class="flex items-center gap-3">
          <div class="relative flex-1 max-w-xs">
            <input v-model="searchQuery" placeholder="Buscar por email o nombre..."
              class="ds-input">
          </div>
          <span class="ds-stat">{{ filteredUsers.length }} usuarios</span>
        </div>
        <button @click="openCreate" class="ds-btn-primary">
          + Nuevo usuario
        </button>
      </div>

      <!-- Users table -->
      <div class="ds-table-section">
        <div class="table-scroll-wrapper flex-1 min-h-0 overflow-y-auto">
        <table class="w-full text-sm" style="min-width: 800px">
          <thead>
            <tr class="bg-slate-800 text-white text-[13px] font-bold uppercase tracking-wider [&>th]:px-4 [&>th]:py-2.5 [&>th]:text-left [&>th]:font-semibold">
              <th>Email</th>
              <th>Nombre</th>
              <th>Rol</th>
              <th>Sitios</th>
              <th class="text-center" style="width: 80px">Activo</th>
              <th class="text-center" style="width: 100px">Contraseña</th>
              <th class="text-center" style="width: 80px">MFA</th>
              <th class="text-right" style="width: 240px">Acciones</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="user in filteredUsers" :key="user.id"
              class="border-b border-slate-100 transition-colors hover:bg-slate-50/80">
              <td class="text-slate-900">{{ user.email }}</td>
              <td class="text-slate-900">{{ user.fullName }}</td>
              <td>
                <span class="ds-label bg-slate-100 px-2 py-0.5 rounded">
                  {{ roleLabel(user.role) }}
                </span>
              </td>
              <td>
                <span v-for="site in userSiteNames(user.siteIds)" :key="site"
                  class="ds-label bg-slate-100 px-1.5 py-0.5 rounded mr-1 mb-0.5">
                  {{ site }}
                </span>
                <span v-if="!user.siteIds?.length" class="text-[12px] text-slate-400">—</span>
              </td>
              <td class="text-center">
                <span class="text-[12px] font-medium px-2 py-0.5 rounded"
                  :class="user.isActive ? 'bg-slate-200 text-slate-900' : 'bg-slate-100 text-slate-400'">
                  {{ user.isActive ? 'Sí' : 'No' }}
                </span>
              </td>
              <td class="text-center">
                <span class="text-[12px] font-medium"
                  :class="user.mustChangePassword ? 'text-red-600' : (user.passwordSet ? 'text-slate-900' : 'text-slate-400')">
                  {{ user.mustChangePassword ? 'Pendiente' : (user.passwordSet ? 'Establecida' : 'Sin contraseña') }}
                </span>
              </td>
              <td class="text-center">
                <span class="text-[12px] font-medium px-2 py-0.5 rounded"
                  :class="user.mfaEnabled
                    ? (user.mfaLocked
                      ? 'bg-red-50 text-red-800'
                      : 'bg-green-50 text-green-800')
                    : 'bg-slate-100 text-slate-400'">
                  {{ user.mfaLocked ? 'Bloqueado' : (user.mfaEnabled ? 'Activo' : 'Inactivo') }}
                </span>
              </td>
              <td class="text-right">
                <div class="flex gap-1 justify-end flex-wrap">
                  <button @click="startEdit(user)" class="ds-btn-secondary !px-2 !py-1 !text-[12px]">Editar</button>
                  <button @click="resetPass(user)" class="ds-btn-secondary !px-2 !py-1 !text-[12px]">Reset pass</button>
                  <button @click="genTempPassword(user)"
                    class="px-2 py-1 rounded text-[12px] font-medium transition-all hover:brightness-110 bg-blue-50 text-blue-700">Gen Temp</button>
                  <template v-if="user.mfaEnabled">
                    <button v-if="!user.mfaLocked" @click="lockMfaUser(user)"
                      class="px-2 py-1 rounded text-[12px] font-medium transition-all hover:brightness-110 bg-red-50 text-red-800">Lock</button>
                    <button v-if="user.mfaLocked" @click="unlockMfaUser(user)"
                      class="px-2 py-1 rounded text-[12px] font-medium transition-all hover:brightness-110 bg-green-50 text-green-800">Unlock</button>
                    <button @click="disableMfaUser(user)"
                      class="px-2 py-1 rounded text-[12px] font-medium transition-all hover:brightness-110 bg-red-50 text-red-800">Disable MFA</button>
                  </template>
                  <button v-if="!user.mfaEnabled" @click="openMfaSetup(user)"
                    class="px-2 py-1 rounded text-[12px] font-medium transition-all hover:brightness-110 bg-green-50 text-green-800">Enable MFA</button>
                  <button @click="removeUser(user)" class="ds-btn-secondary !px-2 !py-1 !text-[12px]">Eliminar</button>
                </div>
              </td>
            </tr>
            <tr v-if="filteredUsers.length === 0">
              <td colspan="8" class="px-4 py-8 text-center text-sm italic text-slate-400">
                No hay usuarios
              </td>
            </tr>
          </tbody>
        </table>
        </div>
      </div>

      <!-- Edit modal -->
      <div v-if="editingUser" class="ds-modal-backdrop">
        <div class="ds-modal-panel max-w-md max-h-[90vh] overflow-y-auto">
          <div class="ds-modal-header">
            <h2 class="ds-modal-title">Editar usuario</h2>
          </div>
          <div class="p-6 space-y-3">
            <div>
              <label class="ds-label block mb-0.5">Email</label>
              <input v-model="editForm.email" type="email" class="ds-input">
            </div>
            <div>
              <label class="ds-label block mb-0.5">Nombre</label>
              <input v-model="editForm.fullName" class="ds-input">
            </div>
            <div>
              <label class="ds-label block mb-0.5">Rol</label>
              <select v-model="editForm.role" class="ds-input">
                <option v-for="r in roles" :key="r" :value="r">{{ roleLabel(r) }}</option>
              </select>
            </div>
            <div>
              <label class="ds-label block mb-0.5">Sitios</label>
              <div class="space-y-1 max-h-32 overflow-y-auto">
                <label v-for="site in allSites" :key="site.id"
                  class="flex items-center gap-2 text-sm cursor-pointer text-slate-900">
                  <input type="checkbox" :value="site.id" v-model="editForm.siteIds"
                    class="rounded border-slate-300">
                  {{ site.name }} ({{ site.code }})
                </label>
              </div>
            </div>
            <div>
              <label class="ds-label block mb-0.5">Activo</label>
              <select v-model="editForm.isActive" class="ds-input">
                <option :value="true">Sí</option>
                <option :value="false">No</option>
              </select>
            </div>
            <div class="flex gap-2 pt-2">
              <button @click="saveEdit" class="ds-btn-primary flex-1 justify-center">Guardar</button>
              <button @click="cancelEdit" class="ds-btn-secondary flex-1 justify-center">Cancelar</button>
            </div>
          </div>
        </div>
      </div>

      <!-- Create modal -->
      <div v-if="showCreate" class="ds-modal-backdrop">
        <div class="ds-modal-panel max-w-md">
          <div class="ds-modal-header">
            <h2 class="ds-modal-title">Nuevo usuario</h2>
          </div>
          <div class="p-6 space-y-3">
            <div>
              <label class="ds-label block mb-0.5">Email</label>
              <input v-model="createForm.email" type="email" required class="ds-input">
            </div>
            <div>
              <label class="ds-label block mb-0.5">Nombre</label>
              <input v-model="createForm.fullName" required class="ds-input">
            </div>
            <div>
              <label class="ds-label block mb-0.5">Rol</label>
              <select v-model="createForm.role" class="ds-input">
                <option v-for="r in roles" :key="r" :value="r">{{ roleLabel(r) }}</option>
              </select>
            </div>
            <div>
              <label class="ds-label block mb-0.5">Sitios</label>
              <div class="space-y-1 max-h-32 overflow-y-auto">
                <label v-for="site in allSites" :key="site.id"
                  class="flex items-center gap-2 text-sm cursor-pointer text-slate-900">
                  <input type="checkbox" :value="site.id" v-model="createForm.siteIds"
                    class="rounded border-slate-300">
                  {{ site.name }} ({{ site.code }})
                </label>
              </div>
            </div>
            <div class="flex gap-2 pt-2">
              <button @click="saveCreate" class="ds-btn-primary flex-1 justify-center">Crear</button>
              <button @click="showCreate = false" class="ds-btn-secondary flex-1 justify-center">Cancelar</button>
            </div>
          </div>
        </div>
      </div>

      <!-- MFA Setup modal -->
      <div v-if="showMfaSetup" class="ds-modal-backdrop">
        <div class="ds-modal-panel max-w-sm">
          <div class="ds-modal-header">
            <h2 class="ds-modal-title">Configurar MFA</h2>
          </div>
          <div class="p-6">
            <p class="text-[13px] mb-4 text-slate-500">
              Escanea el código QR con Google Authenticator o Microsoft Authenticator.
            </p>
            <div class="text-center mb-4">
              <div class="inline-block p-3 rounded-lg border border-slate-200 bg-white">
                <img v-if="mfaOtpAuthUrl" :src="`https://api.qrserver.com/v1/create-qr-code/?size=180x180&data=${encodeURIComponent(mfaOtpAuthUrl)}`"
                  alt="QR Code" class="w-[180px] h-[180px]" />
              </div>
            </div>
            <div class="mb-4">
              <label class="ds-label block mb-0.5">Clave secreta (copia manual)</label>
              <code class="ds-input block text-[12px] break-all font-mono">{{ mfaSecret }}</code>
            </div>
            <div class="mb-4">
              <label class="ds-label block mb-0.5">Código de verificación</label>
              <input v-model="mfaVerifyCode" type="text" inputmode="numeric" maxlength="6"
                placeholder="000000"
                class="ds-input text-center font-mono tracking-wider"
                @keyup.enter="confirmMfaEnable" />
            </div>
            <div class="flex gap-2">
              <button @click="confirmMfaEnable"
                :disabled="mfaVerifyCode.length !== 6"
                class="ds-btn-primary flex-1 justify-center disabled:opacity-40">Habilitar</button>
              <button @click="cancelMfaSetup" class="ds-btn-secondary flex-1 justify-center">Cancelar</button>
            </div>
          </div>
        </div>
      </div>

      <!-- Temp Password modal -->
      <div v-if="showTempPassword" class="ds-modal-backdrop">
        <div class="ds-modal-panel max-w-sm">
          <div class="ds-modal-header">
            <h2 class="ds-modal-title">Contraseña Temporal Generada</h2>
          </div>
          <div class="p-6">
            <p class="text-[13px] mb-4 text-slate-500">
              Comparte esta contraseña con el usuario. Solo se muestra una vez.
            </p>
            <div class="mb-4 p-3 rounded-lg bg-green-50 border border-green-200">
              <label class="block text-[12px] font-medium mb-1 text-green-800">Contraseña temporal</label>
              <div class="flex items-center gap-2">
                <code class="flex-1 text-sm font-mono break-all px-2 py-1 rounded bg-white text-green-800 border border-green-200">
                  {{ generatedPassword }}
                </code>
                <button @click="copyPassword"
                  class="px-2 py-1 rounded text-[12px] font-medium transition-all hover:brightness-110 bg-green-100 text-green-800">
                  Copiar
                </button>
              </div>
            </div>
            <p class="text-[12px] mb-4 text-slate-400">
              El usuario deberá cambiar esta contraseña en su primer inicio de sesión.
            </p>
            <button @click="showTempPassword = false" class="ds-btn-primary w-full justify-center">
              Cerrar
            </button>
          </div>
        </div>
      </div>
    </template>

    <!-- ============ SITES TAB (SuperUser only) ============ -->
    <template v-if="activeTab === 'sites'">
      <div class="flex items-center justify-between mb-3">
        <span class="ds-stat">{{ allSites.length }} sitios</span>
        <button @click="openSiteCreate" class="ds-btn-primary">
          + Nuevo sitio
        </button>
      </div>

      <!-- Sites table -->
      <div class="ds-table-section">
        <div class="table-scroll-wrapper flex-1 min-h-0 overflow-y-auto">
        <table class="w-full text-sm" style="min-width: 500px">
          <thead>
            <tr class="bg-slate-800 text-white text-[13px] font-bold uppercase tracking-wider [&>th]:px-4 [&>th]:py-2.5 [&>th]:text-left [&>th]:font-semibold">
              <th>Código</th>
              <th>Nombre</th>
              <th>País</th>
              <th class="text-center" style="width: 80px">Activo</th>
              <th class="text-right" style="width: 140px">Acciones</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="site in allSites" :key="site.id"
              class="border-b border-slate-100 transition-colors hover:bg-slate-50/80">
              <td class="font-mono font-semibold text-slate-900">{{ site.code }}</td>
              <td class="text-slate-900">{{ site.name }}</td>
              <td class="text-slate-900">{{ site.country || '—' }}</td>
              <td class="text-center">
                <span class="text-[12px] font-medium px-2 py-0.5 rounded"
                  :class="site.isActive ? 'bg-slate-200 text-slate-900' : 'bg-slate-100 text-slate-400'">
                  {{ site.isActive ? 'Sí' : 'No' }}
                </span>
              </td>
              <td class="text-right">
                <div class="flex gap-1 justify-end">
                  <button @click="startSiteEdit(site)" class="ds-btn-secondary !px-2 !py-1 !text-[12px]">Editar</button>
                  <button @click="removeSite(site)" class="ds-btn-secondary !px-2 !py-1 !text-[12px]">Eliminar</button>
                </div>
              </td>
            </tr>
            <tr v-if="allSites.length === 0">
              <td colspan="5" class="px-4 py-8 text-center text-sm italic text-slate-400">
                No hay sitios
              </td>
            </tr>
          </tbody>
        </table>
        </div>
      </div>

      <!-- Site edit modal -->
      <div v-if="editingSite" class="ds-modal-backdrop">
        <div class="ds-modal-panel max-w-md max-h-[90vh] overflow-y-auto">
          <div class="ds-modal-header">
            <h2 class="ds-modal-title">Editar sitio</h2>
          </div>
          <div class="p-6 space-y-3">
            <div>
              <label class="ds-label block mb-0.5">Código</label>
              <input v-model="siteForm.code" maxlength="10" required class="ds-input font-mono uppercase">
            </div>
            <div>
              <label class="ds-label block mb-0.5">Nombre</label>
              <input v-model="siteForm.name" required class="ds-input">
            </div>
            <div>
              <label class="ds-label block mb-0.5">País</label>
              <input v-model="siteForm.country" maxlength="60" class="ds-input">
            </div>
            <div>
              <label class="ds-label block mb-0.5">Activo</label>
              <select v-model="siteForm.isActive" class="ds-input">
                <option :value="true">Sí</option>
                <option :value="false">No</option>
              </select>
            </div>
            <div class="flex gap-2 pt-2">
              <button @click="saveSiteEdit" class="ds-btn-primary flex-1 justify-center">Guardar</button>
              <button @click="editingSite = null" class="ds-btn-secondary flex-1 justify-center">Cancelar</button>
            </div>
          </div>
        </div>
      </div>

      <!-- Site create modal -->
      <div v-if="showSiteCreate" class="ds-modal-backdrop">
        <div class="ds-modal-panel max-w-md">
          <div class="ds-modal-header">
            <h2 class="ds-modal-title">Nuevo sitio</h2>
          </div>
          <div class="p-6 space-y-3">
            <div>
              <label class="ds-label block mb-0.5">Código</label>
              <input v-model="siteCreateForm.code" maxlength="10" required class="ds-input font-mono uppercase">
            </div>
            <div>
              <label class="ds-label block mb-0.5">Nombre</label>
              <input v-model="siteCreateForm.name" required class="ds-input">
            </div>
            <div>
              <label class="ds-label block mb-0.5">País</label>
              <input v-model="siteCreateForm.country" maxlength="60" class="ds-input">
            </div>
            <div class="flex gap-2 pt-2">
              <button @click="saveSiteCreate" class="ds-btn-primary flex-1 justify-center">Crear</button>
              <button @click="showSiteCreate = false" class="ds-btn-secondary flex-1 justify-center">Cancelar</button>
            </div>
          </div>
        </div>
      </div>
    </template>

    <!-- ============ AIRLINES TAB (ADMIN / SuperUser) ============ -->
    <template v-if="activeTab === 'airlines'">
      <div class="flex items-center justify-between mb-3">
        <span class="ds-stat">{{ airlines.length }} aerolíneas</span>
        <button @click="openAirlineCreate" class="ds-btn-primary">
          + Nueva aerolínea
        </button>
      </div>

      <div class="ds-table-section">
        <div class="table-scroll-wrapper flex-1 min-h-0 overflow-y-auto">
        <table class="w-full text-sm" style="min-width: 600px">
          <thead>
            <tr class="bg-slate-800 text-white text-[13px] font-bold uppercase tracking-wider [&>th]:px-4 [&>th]:py-2.5 [&>th]:text-left [&>th]:font-semibold">
              <th>Código</th>
              <th>Nombre</th>
              <th>IATA</th>
              <th>País</th>
              <th class="text-center" style="width: 80px">Activo</th>
              <th class="text-right" style="width: 140px">Acciones</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="a in airlines" :key="a.id"
              class="border-b border-slate-100 transition-colors hover:bg-slate-50/80">
              <td class="font-mono font-semibold text-slate-900">{{ a.code }}</td>
              <td class="text-slate-900">{{ a.name }}</td>
              <td class="font-mono text-slate-700">{{ a.iataCode || '—' }}</td>
              <td class="text-slate-900">{{ a.country || '—' }}</td>
              <td class="text-center">
                <span class="text-[12px] font-medium px-2 py-0.5 rounded"
                  :class="a.isActive ? 'bg-slate-200 text-slate-900' : 'bg-slate-100 text-slate-400'">
                  {{ a.isActive ? 'Sí' : 'No' }}
                </span>
              </td>
              <td class="text-right">
                <div class="flex gap-1 justify-end">
                  <button @click="startAirlineEdit(a)" class="ds-btn-secondary !px-2 !py-1 !text-[12px]">Editar</button>
                  <button @click="removeAirline(a)" class="ds-btn-secondary !px-2 !py-1 !text-[12px]">Eliminar</button>
                </div>
              </td>
            </tr>
            <tr v-if="airlines.length === 0">
              <td colspan="6" class="px-4 py-8 text-center text-sm italic text-slate-400">
                No hay aerolíneas
              </td>
            </tr>
          </tbody>
        </table>
        </div>
      </div>

      <!-- Airline edit modal -->
      <div v-if="editingAirline" class="ds-modal-backdrop">
        <div class="ds-modal-panel max-w-md">
          <div class="ds-modal-header">
            <h2 class="ds-modal-title">Editar aerolínea</h2>
          </div>
          <div class="p-6 space-y-3">
            <div>
              <label class="ds-label block mb-0.5">Código</label>
              <input v-model="airlineForm.code" maxlength="10" required class="ds-input font-mono uppercase">
            </div>
            <div>
              <label class="ds-label block mb-0.5">Nombre</label>
              <input v-model="airlineForm.name" required class="ds-input">
            </div>
            <div>
              <label class="ds-label block mb-0.5">Código IATA</label>
              <input v-model="airlineForm.iataCode" maxlength="3" class="ds-input font-mono uppercase">
            </div>
            <div>
              <label class="ds-label block mb-0.5">País</label>
              <input v-model="airlineForm.country" maxlength="60" class="ds-input">
            </div>
            <div>
              <label class="ds-label block mb-0.5">Activo</label>
              <select v-model="airlineForm.isActive" class="ds-input">
                <option :value="true">Sí</option>
                <option :value="false">No</option>
              </select>
            </div>
            <div class="flex gap-2 pt-2">
              <button @click="saveAirlineEdit" class="ds-btn-primary flex-1 justify-center">Guardar</button>
              <button @click="editingAirline = null" class="ds-btn-secondary flex-1 justify-center">Cancelar</button>
            </div>
          </div>
        </div>
      </div>

      <!-- Airline create modal -->
      <div v-if="showAirlineCreate" class="ds-modal-backdrop">
        <div class="ds-modal-panel max-w-md">
          <div class="ds-modal-header">
            <h2 class="ds-modal-title">Nueva aerolínea</h2>
          </div>
          <div class="p-6 space-y-3">
            <div>
              <label class="ds-label block mb-0.5">Código</label>
              <input v-model="airlineCreateForm.code" maxlength="10" required class="ds-input font-mono uppercase">
            </div>
            <div>
              <label class="ds-label block mb-0.5">Nombre</label>
              <input v-model="airlineCreateForm.name" required class="ds-input">
            </div>
            <div>
              <label class="ds-label block mb-0.5">Código IATA</label>
              <input v-model="airlineCreateForm.iataCode" maxlength="3" class="ds-input font-mono uppercase">
            </div>
            <div>
              <label class="ds-label block mb-0.5">País</label>
              <input v-model="airlineCreateForm.country" maxlength="60" class="ds-input">
            </div>
            <div class="flex gap-2 pt-2">
              <button @click="saveAirlineCreate" class="ds-btn-primary flex-1 justify-center">Crear</button>
              <button @click="showAirlineCreate = false" class="ds-btn-secondary flex-1 justify-center">Cancelar</button>
            </div>
          </div>
        </div>
      </div>
    </template>

    <!-- ============ ULD CONFIG TAB (ADMIN / SuperUser) ============ -->
    <template v-if="activeTab === 'uldconfig'">
      <div class="flex items-center justify-between mb-3 flex-wrap gap-2">
        <div class="flex items-center gap-3">
          <span class="ds-stat">{{ typeConfigs.length }} configs</span>
          <select v-model="configAirlineId" @change="loadTypeConfig" class="ds-input !w-auto font-mono">
            <option v-for="a in airlines" :key="a.id" :value="a.id">{{ a.code }} — {{ a.name }}</option>
          </select>
        </div>
        <button @click="addTypeConfigRow" class="ds-btn-primary">
          + Nueva fila
        </button>
      </div>

      <div class="ds-table-section">
        <div class="table-scroll-wrapper flex-1 min-h-0 overflow-y-auto">
        <table class="w-full text-sm" style="min-width: 700px">
          <thead>
            <tr class="bg-slate-800 text-white text-[13px] font-bold uppercase tracking-wider [&>th]:px-4 [&>th]:py-2.5 [&>th]:text-left [&>th]:font-semibold">
              <th>Tipo ULD</th>
              <th class="text-right">Tara default (lbs)</th>
              <th class="text-right">Max Gross (lbs)</th>
              <th>Notas</th>
              <th class="text-right" style="width: 120px">Acciones</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="(cfg, idx) in typeConfigs" :key="idx"
              class="border-b border-slate-100">
              <td>
                <select v-model="cfg.uldType" class="ds-input font-mono">
                  <option v-for="t in uldTypes" :key="t" :value="t">{{ t }}</option>
                </select>
              </td>
              <td>
                <input v-model.number="cfg.defaultTareLbs" type="number" step="0.1" class="ds-input text-right font-mono">
              </td>
              <td>
                <input v-model.number="cfg.maxGrossLbs" type="number" step="0.1" class="ds-input text-right font-mono">
              </td>
              <td>
                <input v-model="cfg.notes" class="ds-input" placeholder="Observaciones">
              </td>
              <td class="text-right">
                <div class="flex gap-1 justify-end">
                  <button @click="removeTypeConfigRow(idx)" class="ds-btn-secondary !px-2 !py-1 !text-[12px]">Quitar</button>
                </div>
              </td>
            </tr>
            <tr v-if="typeConfigs.length === 0">
              <td colspan="5" class="px-4 py-8 text-center text-sm italic text-slate-400">
                Sin configuración para esta aerolínea. Añade filas y guarda.
              </td>
            </tr>
          </tbody>
        </table>
        </div>
      </div>

      <div class="flex items-center justify-end gap-2 mt-3">
        <button @click="loadTypeConfig" class="ds-btn-secondary">Descartar cambios</button>
        <button @click="saveTypeConfig" class="ds-btn-primary">Guardar configuración</button>
      </div>
    </template>


  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { usersApi } from '../api/users'
import { sitesApi } from '../api/sites'
import { airlinesApi } from '../api/airlines'
import { uldTypeConfigApi } from '../api/uldTypeConfig'
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

const canManageSettings = computed(() => ['ADMIN', 'SUPER_USER'].includes(auth.role))

const airlines = ref([])
const editingAirline = ref(null)
const showAirlineCreate = ref(false)
const airlineForm = ref({ id: null, code: '', name: '', iataCode: '', country: '', isActive: true })
const airlineCreateForm = ref({ code: '', name: '', iataCode: '', country: '', isActive: true })

const uldTypes = ['PMC', 'PAH', 'PAG', 'PAJ', 'AAY', 'AAZ', 'AAD', 'PIP', 'BULK', 'AMP', 'AMJ']
const typeConfigs = ref([])
const configAirlineId = ref(null)

async function loadAirlines() {
  try {
    const res = await airlinesApi.getAll()
    airlines.value = res.data || []
  } catch (e) {
    toast.error(extractError(e, 'No se pudieron cargar las aerolíneas'))
  }
}

function openAirlineCreate() {
  airlineCreateForm.value = { code: '', name: '', iataCode: '', country: '', isActive: true }
  showAirlineCreate.value = true
}

async function saveAirlineCreate() {
  const f = airlineCreateForm.value
  if (!f.code || !f.name) { toast.warning('Código y nombre son obligatorios'); return }
  try {
    await airlinesApi.create({ ...f, isActive: f.isActive ?? true })
    toast.success('Aerolínea creada')
    showAirlineCreate.value = false
    await loadAirlines()
  } catch (e) {
    toast.error(extractError(e, 'Error al crear la aerolínea'))
  }
}

function startAirlineEdit(a) {
  airlineForm.value = {
    id: a.id, code: a.code, name: a.name,
    iataCode: a.iataCode || '', country: a.country || '',
    isActive: a.isActive !== false,
  }
  editingAirline.value = a
}

async function saveAirlineEdit() {
  const f = airlineForm.value
  if (!f.code || !f.name) { toast.warning('Código y nombre son obligatorios'); return }
  try {
    await airlinesApi.update(f.id, { ...f })
    toast.success('Aerolínea actualizada')
    editingAirline.value = null
    await loadAirlines()
  } catch (e) {
    toast.error(extractError(e, 'Error al actualizar la aerolínea'))
  }
}

async function removeAirline(a) {
  if (!confirm(`¿Eliminar la aerolínea ${a.name} (${a.code})?`)) return
  try {
    await airlinesApi.delete(a.id)
    toast.success('Aerolínea eliminada')
    await loadAirlines()
  } catch (e) {
    toast.error(extractError(e, 'Error al eliminar la aerolínea'))
  }
}

async function loadTypeConfig() {
  if (!configAirlineId.value) return
  try {
    const res = await uldTypeConfigApi.getByAirline(configAirlineId.value)
    typeConfigs.value = (res.data || []).map(c => ({
      ...c,
      defaultTareLbs: c.defaultTareLbs != null ? Number(c.defaultTareLbs) : 0,
      maxGrossLbs: c.maxGrossLbs != null ? Number(c.maxGrossLbs) : null,
    }))
  } catch (e) {
    toast.error(extractError(e, 'No se pudo cargar la configuración ULD'))
  }
}

function addTypeConfigRow() {
  if (!configAirlineId.value) { toast.warning('Selecciona una aerolínea primero'); return }
  typeConfigs.value.push({
    id: null, airlineId: configAirlineId.value, uldType: 'PMC',
    defaultTareLbs: 0, maxGrossLbs: null, notes: '',
  })
}

function removeTypeConfigRow(idx) {
  typeConfigs.value.splice(idx, 1)
}

async function saveTypeConfig() {
  if (!configAirlineId.value) return
  const rows = typeConfigs.value.map(r => ({
    uldType: r.uldType,
    defaultTareLbs: r.defaultTareLbs || 0,
    maxGrossLbs: r.maxGrossLbs || null,
    notes: r.notes || null,
  }))
  try {
    await uldTypeConfigApi.replaceForAirline(configAirlineId.value, rows)
    toast.success('Configuración ULD guardada')
    await loadTypeConfig()
  } catch (e) {
    toast.error(extractError(e, 'Error al guardar la configuración ULD'))
  }
}

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

const showMfaSetup = ref(false)
const mfaSetupUser = ref(null)
const mfaSecret = ref('')
const mfaOtpAuthUrl = ref('')
const mfaVerifyCode = ref('')
const showTempPassword = ref(false)
const generatedPassword = ref('')

async function openMfaSetup(user) {
  try {
    const res = await usersApi.mfaSetup(user.id)
    mfaSetupUser.value = user
    mfaSecret.value = res.data.secret
    mfaOtpAuthUrl.value = res.data.otpAuthUrl
    mfaVerifyCode.value = ''
    showMfaSetup.value = true
  } catch (e) { toast.error(extractError(e)) }
}

async function confirmMfaEnable() {
  if (!mfaSetupUser.value || mfaVerifyCode.value.length !== 6) return
  try {
    await usersApi.mfaEnable(mfaSetupUser.value.id, mfaSecret.value, mfaVerifyCode.value)
    showMfaSetup.value = false
    mfaSetupUser.value = null
    toast.success('MFA habilitado correctamente')
    await loadUsers()
  } catch (e) {
    toast.error(e.response?.data?.error || 'Código inválido')
  }
}

function cancelMfaSetup() {
  showMfaSetup.value = false
  mfaSetupUser.value = null
  mfaSecret.value = ''
  mfaOtpAuthUrl.value = ''
  mfaVerifyCode.value = ''
}

async function disableMfaUser(user) {
  if (!confirm(`¿Deshabilitar MFA para ${user.email}?`)) return
  try {
    await usersApi.mfaDisable(user.id)
    await loadUsers()
  } catch (e) { toast.error(extractError(e)) }
}

async function lockMfaUser(user) {
  if (!confirm(`¿Bloquear la cuenta de ${user.email}? No podrá iniciar sesión.`)) return
  try {
    await usersApi.mfaLock(user.id)
    await loadUsers()
  } catch (e) { toast.error(extractError(e)) }
}

async function unlockMfaUser(user) {
  if (!confirm(`¿Desbloquear la cuenta de ${user.email}?`)) return
  try {
    await usersApi.mfaUnlock(user.id)
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

async function genTempPassword(user) {
  if (!confirm(`¿Generar contraseña temporal para ${user.email}?`)) return
  try {
    const res = await usersApi.generateTempPassword(user.id)
    generatedPassword.value = res.data.tempPassword
    showTempPassword.value = true
    await loadUsers()
  } catch (e) { toast.error(extractError(e)) }
}

async function copyPassword() {
  try {
    await navigator.clipboard.writeText(generatedPassword.value)
    toast.success('Contraseña copiada al portapapeles')
  } catch {
    toast.error('No se pudo copiar')
  }
}

onMounted(async () => {
  await loadUsers()
  if (auth.role === 'SUPER_USER') {
    await loadSites()
  }
  if (canManageSettings.value) {
    await loadAirlines()
    if (airlines.value.length) {
      configAirlineId.value = airlines.value[0].id
    }
  }
})
</script>
