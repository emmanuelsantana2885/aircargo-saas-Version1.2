<template>
  <!-- Mobile overlay backdrop -->
  <div v-if="isMobile && mobileOpen" @click="mobileOpen = false"
    class="fixed inset-0 bg-black/40 z-40 transition-opacity lg:hidden"></div>

  <aside :style="sidebarStyle"
    class="flex flex-col flex-shrink-0 h-full border-r transition-all duration-300 ease-out relative"
    :class="isMobile ? (mobileOpen ? 'translate-x-0' : '-translate-x-full') : ''"
    :style-override="isMobile ? 'position: fixed; z-index: 50; height: 100vh; top: 0; left: 0;' : ''"
    style="background: var(--surface); border-color: var(--border); box-shadow: 4px 0 24px rgba(120, 120, 120, 0.12)">

    <!-- Toggle button (desktop only) -->
    <button v-if="!isMobile" @click="collapsed = !collapsed"
      class="absolute -right-3.5 top-5 z-20 w-7 h-7 flex items-center justify-center transition-opacity hover:opacity-70 active:opacity-50"
      style="background: var(--accent); color: white">
      <IconLayoutSidebarFilled :size="16" :stroke-width="2" />
    </button>

    <!-- Logo -->
    <div class="px-4 py-4 border-b relative overflow-hidden" style="border-color: var(--border)">
      <div class="flex items-center" :class="showCollapsed ? 'justify-center' : 'gap-2.5'">
        <div v-if="!showCollapsed" class="overflow-hidden whitespace-nowrap relative z-10">
          <div class="font-bold title" style="color: var(--text)">AirCargo</div>
          <div class="text-xs" style="color: var(--muted)">{{ auth.selectedSite?.code || 'SDQ' }} Operations</div>
        </div>
        <div v-else class="w-8 h-8 flex items-center justify-center shrink-0"
          style="background: var(--accent)">
          <IconPlaneDepartureFilled :size="20" color="white" :stroke-width="2" />
        </div>
      </div>
      <!-- Watermark airplane -->
      <svg v-if="!showCollapsed" class="absolute -bottom-3 -right-2 opacity-[0.06] pointer-events-none" width="120" height="120" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="0.8" stroke-linecap="round" stroke-linejoin="round" style="color: var(--text)">
        <path d="M2 16l19-4-7-4-3-9-3 9-7 4z"/>
        <path d="M10 16v4l4-2v-4"/>
      </svg>
    </div>

    <!-- Nav -->
    <nav class="flex-1 px-2 py-4 space-y-0.5 overflow-y-auto overflow-x-hidden">
      <div v-if="!showCollapsed" class="text-xs font-bold mb-2 px-2" style="color: var(--muted); letter-spacing: .1em; text-transform: uppercase">
        Principal
      </div>

      <RouterLink v-for="item in mainMenu" :key="item.path" :to="item.path"
        class="nav-link flex items-center transition-colors whitespace-nowrap"
        :class="[showCollapsed ? 'justify-center px-0 py-3' : 'gap-3 px-3 py-2.5', isActive(item.path) ? 'nav-active' : 'nav-default']"
        :style="!isActive(item.path) ? { color: item.color } : {}"
        :title="showCollapsed ? item.label : ''"
        @click="isMobile && (mobileOpen = false)">
        <component :is="item.icon" :size="showCollapsed ? 28 : 22" :stroke-width="1.5" :color="isActive(item.path) ? 'white' : item.color" />
        <template v-if="!showCollapsed">
          <span class="nav-label font-bold" :style="isActive(item.path) ? { borderBottom: `2px solid ${item.color}`, paddingBottom: '1px' } : {}">{{ item.label }}</span>
        </template>
      </RouterLink>

      <div v-if="!showCollapsed" class="text-xs font-bold mt-4 mb-2 px-2" style="color: var(--muted); letter-spacing: .1em; text-transform: uppercase">
        Configuración
      </div>

      <RouterLink v-for="item in settingsMenu" :key="item.path" :to="item.path"
        class="nav-link flex items-center transition-colors whitespace-nowrap"
        :class="[showCollapsed ? 'justify-center px-0 py-3' : 'gap-3 px-3 py-2.5', isActive(item.path) ? 'nav-active' : 'nav-default']"
        :style="!isActive(item.path) ? { color: item.color } : {}"
        :title="showCollapsed ? item.label : ''"
        @click="isMobile && (mobileOpen = false)">
        <component :is="item.icon" :size="showCollapsed ? 28 : 22" :stroke-width="1.5" :color="isActive(item.path) ? 'white' : item.color" />
        <span v-if="!showCollapsed" class="nav-label font-bold" :style="isActive(item.path) ? { borderBottom: `2px solid ${item.color}`, paddingBottom: '1px' } : {}">{{ item.label }}</span>
      </RouterLink>
    </nav>

    <!-- User -->
    <div class="px-2 py-3 border-t" style="border-color: var(--border)">
      <div class="flex items-center px-2 py-2" style="background: var(--bg)"
        :class="showCollapsed ? 'justify-center' : 'gap-2.5'">
        <div class="w-8 h-8 flex items-center justify-center text-xs font-bold shrink-0"
          style="background: var(--accent-light); color: var(--accent)">
          {{ auth.initials }}
        </div>
        <template v-if="!showCollapsed">
          <div class="flex-1 min-w-0">
            <div class="text-xs font-bold truncate" style="color: var(--text)">{{ auth.fullName || auth.email }}</div>
            <div class="text-xs truncate" style="color: var(--muted)">{{ auth.role?.replace('_', ' ') || '' }}</div>
          </div>
          <button @click="handleLogout" title="Cerrar sesión" class="hover:opacity-70 transition-opacity">
            <IconLogout :size="16" style="color: var(--muted)" :stroke-width="1.5" />
          </button>
        </template>
      </div>
    </div>
  </aside>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '../../stores/auth'
import {
  IconGaugeFilled,
  IconCalendarEventFilled,
  IconFileFilled,
  IconPlaneFilled,
  IconTableFilled,
  IconContainerFilled,
  IconStackFilled,
  IconLayoutGridFilled,
  IconUserFilled,
  IconSettingsFilled,
  IconPlaneDepartureFilled,
  IconLayoutSidebarFilled,
  IconLogout,
} from '@tabler/icons-vue'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()
const collapsed = ref(false)
const mobileOpen = ref(false)
const isMobile = ref(false)
const isTablet = ref(false)

const showCollapsed = computed(() => {
  if (isMobile.value) return false
  if (isTablet.value) return true
  return collapsed.value
})

const isActive = (path) => path === '/' ? route.path === '/' : route.path.startsWith(path)

const sidebarStyle = computed(() => {
  if (isMobile.value) {
    return { width: '260px' }
  }
  if (isTablet.value) {
    return { width: '60px' }
  }
  return { width: collapsed.value ? '60px' : 'var(--sidebar-width)' }
})

function checkViewport() {
  const w = window.innerWidth
  isMobile.value = w < 768
  isTablet.value = w >= 768 && w < 1024
  if (isMobile.value || isTablet.value) {
    mobileOpen.value = false
  }
}

function handleLogout() {
  auth.logout()
  router.push('/login')
}

defineExpose({ mobileOpen, isMobile })

onMounted(() => {
  checkViewport()
  window.addEventListener('resize', checkViewport)
})

onUnmounted(() => {
  window.removeEventListener('resize', checkViewport)
})

const allMenuItems = [
  { path: '/',              label: 'Dashboard',           icon: IconGaugeFilled,         view: 'DASHBOARD',       color: '#dc2626' },
  { path: '/bookings',      label: 'Bookings',            icon: IconCalendarEventFilled, view: 'BOOKINGS',        color: '#2563eb' },
  { path: '/receipts',      label: 'Receipts',             icon: IconFileFilled,          view: 'RECEIPTS',        color: '#ea580c' },
  { path: '/flights',       label: 'Flights',             icon: IconPlaneFilled,         view: 'FLIGHTS',         color: '#7c3aed' },
  { path: '/mawbs',         label: 'MAWBs',               icon: IconTableFilled,         view: 'MAWBS',           color: '#16a34a' },
  { path: '/load-planning', label: 'Load Planning',       icon: IconContainerFilled,     view: 'LOAD_PLANNING',   color: '#475569' },
  { path: '/ulds',          label: 'ULDs',                icon: IconStackFilled,         view: 'ULDS',            color: '#0891b2' },
  { path: '/exports',       label: 'Exports',             icon: IconLayoutGridFilled,    view: 'EXPORTS',         color: '#92400e' },
]

const mainMenu = computed(() => allMenuItems.filter(item => auth.canView(item.view)))

const settingsMenu = computed(() => {
  const items = []
  if (auth.canView('USERS')) items.push({ path: '/users', label: 'Usuarios', icon: IconUserFilled, color: '#64748b' })
  if (auth.canView('SETTINGS')) items.push({ path: '/settings', label: 'Configuración', icon: IconSettingsFilled, color: '#0f172a' })
  return items
})
</script>

<style scoped>
.nav-default { color: var(--muted); }
.nav-default:hover { background: var(--bg); color: var(--text); }
.nav-active {
  background: var(--accent);
  color: white;
  font-weight: 700;
}
.nav-label {
  font-size: 14px;
  letter-spacing: 0.02em;
}
</style>
