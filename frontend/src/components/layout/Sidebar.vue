<template>
  <div v-if="isMobile && mobileOpen" @click="mobileOpen = false"
    class="fixed inset-0 bg-black/40 z-40 transition-opacity lg:hidden"></div>

  <aside :style="sidebarStyle"
    class="flex flex-col flex-shrink-0 h-full border-r transition-all duration-300 ease-out relative"
    :class="isMobile ? (mobileOpen ? 'translate-x-0' : '-translate-x-full') : ''"
    style="background: var(--surface); border-color: var(--border); box-shadow: 4px 0 24px rgba(120,120,120,0.12)">

    <button v-if="!isMobile" @click="collapsed = !collapsed"
      class="absolute -right-3.5 top-5 z-20 w-7 h-7 flex items-center justify-center transition-opacity hover:opacity-70 active:opacity-50"
      style="background: var(--accent); color: white">
      <IconLayoutSidebarFilled :size="16" :stroke-width="2" />
    </button>

    <!-- Logo -->
    <div class="px-4 py-4 border-b relative overflow-hidden" style="border-color: var(--border); background: linear-gradient(135deg, #1e293b 0%, #334155 25%, #1e3a5f 50%, #2d3a4e 75%, #1e293b 100%); background-size: 200% 200%;">
      <div class="absolute inset-0 opacity-[0.08]" style="background-image: repeating-linear-gradient(45deg, transparent, transparent 2px, rgba(148,163,184,0.3) 2px, rgba(148,163,184,0.3) 3px), repeating-linear-gradient(-45deg, transparent, transparent 3px, rgba(100,116,139,0.2) 3px, rgba(100,116,139,0.2) 4px);"></div>
      <div class="absolute inset-0 opacity-[0.04]" style="background-image: radial-gradient(circle at 30% 50%, rgba(148,163,184,0.4) 0%, transparent 60%), radial-gradient(circle at 70% 30%, rgba(100,116,139,0.3) 0%, transparent 50%);"></div>
      <div class="flex items-center" :class="showCollapsed ? 'justify-center' : 'gap-2.5'">
        <div v-if="!showCollapsed" class="overflow-hidden whitespace-nowrap relative z-10">
          <div class="font-extrabold title text-white" style="font-size: 18px; letter-spacing: 0.08em; text-shadow: 0 1px 4px rgba(0,0,0,0.4)">AirCargo</div>
          <div class="text-[13px] text-slate-300 font-medium tracking-wide">{{ auth.selectedSite?.code || 'SDQ' }} Operations</div>
        </div>
        <div v-else class="w-8 h-8 flex items-center justify-center shrink-0" style="background: var(--accent)">
          <IconLogo :size="20" color="white" :stroke-width="1.8" />
        </div>
      </div>
    </div>

    <!-- Nav -->
    <nav class="flex-1 px-2 py-4 space-y-0.5 overflow-y-auto overflow-x-hidden">
      <div v-if="!showCollapsed" class="text-xs font-bold mb-2 px-2" style="color: var(--muted); letter-spacing: .1em; text-transform: uppercase">Principal</div>

      <RouterLink v-for="item in mainMenu" :key="item.path" :to="item.path"
        class="nav-link group flex items-center whitespace-nowrap rounded-lg transition-all duration-200 ease-out"
        :class="[showCollapsed ? 'justify-center px-0 py-2.5' : 'gap-3 px-3 py-2.5', isActive(item.path) ? 'nav-active' : 'nav-default']"
        :style="!isActive(item.path) ? { color: item.color } : {}"
        :title="showCollapsed ? item.label : ''"
        @click="isMobile && (mobileOpen = false)">
        <div class="ico-frame shrink-0" :class="isActive(item.path) && 'ico-frame-active'" :style="{ '--ic': item.color }">
          <component :is="item.icon" :size="showCollapsed ? 21 : 19" stroke-width="1.6" :color="isActive(item.path) ? 'white' : item.color" />
        </div>
        <template v-if="!showCollapsed">
          <span class="nav-label font-bold" :style="isActive(item.path) ? { borderBottom: `2px solid ${item.color}`, paddingBottom: '1px' } : {}">{{ item.label }}</span>
        </template>
      </RouterLink>

      <div v-if="!showCollapsed" class="text-xs font-bold mt-4 mb-2 px-2" style="color: var(--muted); letter-spacing: .1em; text-transform: uppercase">System Settings</div>

      <RouterLink v-for="item in settingsMenu" :key="item.path" :to="item.path"
        class="nav-link group flex items-center whitespace-nowrap rounded-lg transition-all duration-200 ease-out"
        :class="[showCollapsed ? 'justify-center px-0 py-2.5' : 'gap-3 px-3 py-2.5', isActive(item.path) ? 'nav-active' : 'nav-default']"
        :style="!isActive(item.path) ? { color: item.color } : {}"
        :title="showCollapsed ? item.label : ''"
        @click="isMobile && (mobileOpen = false)">
        <div class="ico-frame shrink-0" :class="isActive(item.path) && 'ico-frame-active'" :style="{ '--ic': item.color }">
          <component :is="item.icon" :size="showCollapsed ? 21 : 19" stroke-width="1.6" :color="isActive(item.path) ? 'white' : item.color" />
        </div>
        <span v-if="!showCollapsed" class="nav-label font-bold" :style="isActive(item.path) ? { borderBottom: `2px solid ${item.color}`, paddingBottom: '1px' } : {}">{{ item.label }}</span>
      </RouterLink>
    </nav>

    <!-- User -->
    <div class="px-2 py-3 border-t" style="border-color: var(--border)">
      <div class="flex items-center px-2 py-2" style="background: var(--bg)"
        :class="showCollapsed ? 'justify-center' : 'gap-2.5'">
        <div class="w-8 h-8 flex items-center justify-center text-xs font-bold shrink-0"
          style="background: var(--accent-light); color: var(--accent)">{{ auth.initials }}</div>
        <template v-if="!showCollapsed">
          <div class="flex-1 min-w-0">
            <div class="text-xs font-bold truncate" style="color: var(--text)">{{ auth.fullName || auth.email }}</div>
            <div class="text-xs truncate" style="color: var(--muted)">{{ auth.role?.replace('_', ' ') || '' }}</div>
          </div>
          <button @click="handleLogout" title="Cerrar sesion" class="hover:opacity-70 transition-opacity">
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
  IconGauge, IconCalendarEvent, IconFileInvoice, IconPlaneDeparture,
  IconClipboardList, IconScale, IconPackage, IconLayoutGrid,
  IconUsers, IconSettings, IconApi,
  IconPlaneDeparture as IconLogo,
  IconLayoutSidebarFilled, IconLogout,
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
  if (isMobile.value) return { width: '260px' }
  if (isTablet.value) return { width: '60px' }
  return { width: collapsed.value ? '60px' : 'var(--sidebar-width)' }
})

function checkViewport() {
  const w = window.innerWidth
  isMobile.value = w < 768
  isTablet.value = w >= 768 && w < 1024
  if (isMobile.value || isTablet.value) mobileOpen.value = false
}
function handleLogout() { auth.logout(); router.push('/login') }
defineExpose({ mobileOpen, isMobile })
onMounted(() => { checkViewport(); window.addEventListener('resize', checkViewport) })
onUnmounted(() => { window.removeEventListener('resize', checkViewport) })

const allMenuItems = [
  { path: '/',              label: 'Dashboard',        icon: IconGauge,          view: 'DASHBOARD',     color: '#dc2626' },
  { path: '/bookings',      label: 'Bookings',         icon: IconCalendarEvent,  view: 'BOOKINGS',      color: '#3b82f6' },
  { path: '/receipts',      label: 'Receipts',         icon: IconFileInvoice,    view: 'RECEIPTS',      color: '#f97316' },
  { path: '/flights',       label: 'Flights',          icon: IconPlaneDeparture, view: 'FLIGHTS',       color: '#8b5cf6' },
  { path: '/mawbs',         label: 'MAWBs',            icon: IconClipboardList,  view: 'MAWBS',         color: '#22c55e' },
  { path: '/load-planning', label: 'Load Planning',    icon: IconScale,          view: 'LOAD_PLANNING', color: '#64748b' },
  { path: '/ulds',          label: 'ULDs',             icon: IconPackage,        view: 'ULDS',          color: '#06b6d4' },
  { path: '/exports',       label: 'Reviews / Audit',  icon: IconLayoutGrid,     view: 'EXPORTS',       color: '#d97706' },
]
const mainMenu = computed(() => allMenuItems.filter(item => auth.canView(item.view)))
const settingsMenu = computed(() => {
  const items = []
  if (auth.canView('USERS')) items.push({ path: '/users', label: 'Users', icon: IconUsers, color: '#94a3b8' })
  if (auth.canView('SETTINGS')) items.push({ path: '/settings', label: 'System Settings', icon: IconSettings, color: '#6366f1' })
  if (auth.canView('API_CATALOG')) items.push({ path: '/api-catalog', label: 'API Catalog', icon: IconApi, color: '#0ea5e9' })
  return items
})
</script>

<style scoped>
.nav-default { color: var(--muted); }
.nav-default:hover { background: var(--bg); color: var(--text); }
.nav-active { background: var(--accent); color: white; font-weight: 700; }
.nav-label { font-size: 14px; letter-spacing: 0.02em; }

/* Trace icon frame */
.ico-frame {
  width: 34px; height: 34px; display: flex; align-items: center; justify-content: center;
  border-radius: 8px; position: relative;
  border: 1.5px solid color-mix(in srgb, var(--ic) 12%, transparent);
  transition: all 0.2s ease;
}
.ico-frame::before {
  content: ''; position: absolute; top: -1px; right: -1px;
  width: 6px; height: 6px; border-top: 1.5px solid var(--ic); border-right: 1.5px solid var(--ic);
  border-radius: 0 3px 0 0; opacity: 0; transition: opacity 0.2s;
}
.nav-default:hover .ico-frame {
  border-color: color-mix(in srgb, var(--ic) 35%, transparent);
  background: color-mix(in srgb, var(--ic) 5%, transparent);
}
.ico-frame-active {
  border-color: color-mix(in srgb, var(--ic) 55%, transparent) !important;
  background: color-mix(in srgb, var(--ic) 10%, transparent) !important;
  border-width: 2px;
}
.ico-frame-active::before { opacity: 0.5; }
</style>
