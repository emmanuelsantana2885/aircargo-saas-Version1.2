<template>
  <div v-if="isMobile && mobileOpen" @click="mobileOpen = false"
    class="fixed inset-0 bg-black/40 z-40 transition-opacity lg:hidden"></div>

  <aside :style="sidebarStyle"
    class="flex flex-col flex-shrink-0 h-full border-r transition-all duration-300 ease-out relative"
    :class="isMobile ? (mobileOpen ? 'translate-x-0' : '-translate-x-full') : ''"
    style="background: #16161e; border-color: #2f3b54; box-shadow: 4px 0 32px rgba(0,0,0,0.5)">

    <button v-if="!isMobile" @click="collapsed = !collapsed"
      class="absolute -right-3.5 top-5 z-20 w-7 h-7 flex items-center justify-center transition-opacity hover:opacity-70 active:opacity-50"
      style="background: #7aa2f7; color: white">
      <IconLayoutSidebarFilled :size="16" :stroke-width="2" />
    </button>

    <!-- Logo -->
    <div class="px-4 py-4 border-b relative overflow-hidden" style="border-color: #2f3b54; background: linear-gradient(135deg, #1a1b2e 0%, #1f2235 25%, #24283b 50%, #1a1b2e 75%, #15161e 100%);">
      <div class="absolute inset-0 opacity-[0.06]" style="background-image: repeating-linear-gradient(45deg, transparent, transparent 2px, rgba(122,162,247,0.2) 2px, rgba(122,162,247,0.2) 3px), repeating-linear-gradient(-45deg, transparent, transparent 3px, rgba(122,162,247,0.15) 3px, rgba(122,162,247,0.15) 4px);"></div>
      <div class="absolute inset-0 opacity-[0.03]" style="background-image: radial-gradient(circle at 30% 50%, rgba(122,162,247,0.3) 0%, transparent 60%), radial-gradient(circle at 70% 30%, rgba(187,154,247,0.2) 0%, transparent 50%);"></div>
      <div class="flex items-center" :class="showCollapsed ? 'justify-center' : 'gap-2.5'">
        <div v-if="!showCollapsed" class="overflow-hidden whitespace-nowrap relative z-10">
          <div class="font-extrabold title" style="font-size: 18px; letter-spacing: 0.08em; color: #c0caf5; text-shadow: 0 1px 4px rgba(0,0,0,0.5)">AirCargo</div>
          <div class="text-[13px] font-medium tracking-wide" style="color: #565f89">{{ auth.selectedSite?.code || 'SDQ' }} Operations</div>
        </div>
        <div v-else class="w-8 h-8 flex items-center justify-center shrink-0" style="background: #7aa2f7">
          <IconLogo :size="20" color="white" :stroke-width="1.8" />
        </div>
      </div>
    </div>

    <!-- Nav -->
    <nav class="flex-1 px-2 py-4 space-y-0.5 overflow-y-auto overflow-x-hidden" style="background: #16161e;">
      <div v-if="!showCollapsed" class="text-xs font-bold mb-2 px-2" style="color: #565f89; letter-spacing: .1em; text-transform: uppercase">Principal</div>

      <RouterLink v-for="item in mainMenu" :key="item.path" :to="item.path"
        class="nav-link group flex items-center whitespace-nowrap rounded-lg transition-all duration-200 ease-out"
        :class="[showCollapsed ? 'justify-center px-0 py-2.5' : 'gap-3 px-3 py-2.5', isActive(item.path) ? 'nav-active' : 'nav-default']"
        :style="!isActive(item.path) ? { color: item.color } : {}"
        :title="showCollapsed ? item.label : ''"
        @click="isMobile && (mobileOpen = false)">
        <div class="ico-frame shrink-0" :class="isActive(item.path) && 'ico-frame-active'" :style="{ '--ic': item.color }">
          <component :is="item.icon" :size="showCollapsed ? 21 : 19" stroke-width="1" :color="isActive(item.path) ? 'white' : item.color" />
        </div>
        <template v-if="!showCollapsed">
          <span class="nav-label font-bold" :style="isActive(item.path) ? { borderBottom: `2px solid ${item.color}`, paddingBottom: '1px' } : {}">{{ item.label }}</span>
        </template>
      </RouterLink>

      <div v-if="!showCollapsed" class="text-xs font-bold mt-4 mb-2 px-2" style="color: #565f89; letter-spacing: .1em; text-transform: uppercase">System Settings</div>

      <RouterLink v-for="item in settingsMenu" :key="item.path" :to="item.path"
        class="nav-link group flex items-center whitespace-nowrap rounded-lg transition-all duration-200 ease-out"
        :class="[showCollapsed ? 'justify-center px-0 py-2.5' : 'gap-3 px-3 py-2.5', isActive(item.path) ? 'nav-active' : 'nav-default']"
        :style="!isActive(item.path) ? { color: item.color } : {}"
        :title="showCollapsed ? item.label : ''"
        @click="isMobile && (mobileOpen = false)">
        <div class="ico-frame shrink-0" :class="isActive(item.path) && 'ico-frame-active'" :style="{ '--ic': item.color }">
          <component :is="item.icon" :size="showCollapsed ? 21 : 19" stroke-width="1" :color="isActive(item.path) ? 'white' : item.color" />
        </div>
        <span v-if="!showCollapsed" class="nav-label font-bold" :style="isActive(item.path) ? { borderBottom: `2px solid ${item.color}`, paddingBottom: '1px' } : {}">{{ item.label }}</span>
      </RouterLink>
    </nav>

    <!-- User -->
    <div class="px-2 py-3 border-t" style="border-color: #2f3b54; background: #1a1b2e;">
      <div class="flex items-center px-2 py-2" style="background: #1e2030; border-radius: 8px;"
        :class="showCollapsed ? 'justify-center' : 'gap-2.5'">
        <div class="w-8 h-8 flex items-center justify-center text-xs font-bold shrink-0"
          style="background: rgba(122,162,247,.15); color: #7aa2f7">{{ auth.initials }}</div>
        <template v-if="!showCollapsed">
          <div class="flex-1 min-w-0">
            <div class="text-xs font-bold truncate" style="color: #c0caf5">{{ auth.fullName || auth.email }}</div>
            <div class="text-xs truncate" style="color: #565f89">{{ auth.role?.replace('_', ' ') || '' }}</div>
          </div>
          <button @click="handleLogout" title="Cerrar sesion" class="hover:opacity-70 transition-opacity">
            <IconLogout :size="16" style="color: #565f89" :stroke-width="1.5" />
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
  { path: '/',              label: 'Dashboard',        icon: IconGauge,          view: 'DASHBOARD',     color: '#f7768e' },
  { path: '/bookings',      label: 'Bookings',         icon: IconCalendarEvent,  view: 'BOOKINGS',      color: '#7aa2f7' },
  { path: '/receipts',      label: 'Receipts',         icon: IconFileInvoice,    view: 'RECEIPTS',      color: '#e0af68' },
  { path: '/flights',       label: 'Flights',          icon: IconPlaneDeparture, view: 'FLIGHTS',       color: '#bb9af7' },
  { path: '/mawbs',         label: 'MAWBs',            icon: IconClipboardList,  view: 'MAWBS',         color: '#9ece6a' },
  { path: '/load-planning', label: 'Load Planning',    icon: IconScale,          view: 'LOAD_PLANNING', color: '#565f89' },
  { path: '/ulds',          label: 'ULDs',             icon: IconPackage,        view: 'ULDS',          color: '#7dcfff' },
  { path: '/exports',       label: 'Reviews / Audit',  icon: IconLayoutGrid,     view: 'EXPORTS',       color: '#ff9e64' },
]
const mainMenu = computed(() => allMenuItems.filter(item => auth.canView(item.view)))
const settingsMenu = computed(() => {
  const items = []
  if (auth.canView('USERS')) items.push({ path: '/users', label: 'Users', icon: IconUsers, color: '#a9b1d6' })
  if (auth.canView('SETTINGS')) items.push({ path: '/settings', label: 'System Settings', icon: IconSettings, color: '#bb9af7' })
  if (auth.canView('API_CATALOG')) items.push({ path: '/api-catalog', label: 'API Catalog', icon: IconApi, color: '#7dcfff' })
  return items
})
</script>

<style scoped>
.nav-default { color: #565f89; }
.nav-default:hover { background: rgba(122,162,247,.04); color: #c0caf5; }
.nav-active { background: rgba(122,162,247,.12); color: white; font-weight: 700; }
.nav-label { font-size: 14px; letter-spacing: 0.02em; }

/* Thin icon frame — Tokyo Night */
.ico-frame {
  width: 34px; height: 34px; display: flex; align-items: center; justify-content: center;
  border-radius: 8px; position: relative;
  border: 1px solid color-mix(in srgb, var(--ic) 10%, transparent);
  transition: all 0.2s ease;
}
.ico-frame::before {
  content: ''; position: absolute; top: -1px; right: -1px;
  width: 6px; height: 6px; border-top: 1.5px solid var(--ic); border-right: 1.5px solid var(--ic);
  border-radius: 0 3px 0 0; opacity: 0; transition: opacity 0.2s;
}
.nav-default:hover .ico-frame {
  border-color: color-mix(in srgb, var(--ic) 30%, transparent);
  background: color-mix(in srgb, var(--ic) 4%, transparent);
}
.ico-frame-active {
  border-color: color-mix(in srgb, var(--ic) 45%, transparent) !important;
  background: color-mix(in srgb, var(--ic) 8%, transparent) !important;
  border-width: 1.5px;
}
.ico-frame-active::before { opacity: 0.4; }
</style>
