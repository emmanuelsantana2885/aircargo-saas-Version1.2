<template>
  <header class="flex items-center justify-between px-4 md:px-6 border-b flex-shrink-0 flex-wrap gap-y-1"
    style="min-height: 44px; background: var(--surface); border-color: var(--border)">

    <div class="flex items-center gap-2">
      <!-- Mobile hamburger -->
      <button v-if="isMobile" @click="$emit('toggleSidebar')"
        class="flex items-center justify-center w-8 h-8 rounded hover:bg-slate-100 transition lg:hidden">
        <IconMenu :size="20" :stroke-width="2" style="color: var(--text)" />
      </button>
      <IconChevronRight :size="12" style="color: var(--muted)" :stroke-width="2" class="hidden sm:block" />
      <span class="text-[11px] md:text-xs font-bold uppercase" style="color: var(--text)">{{ title }}</span>
    </div>

    <div class="flex items-center gap-2 md:gap-4">
      <span class="text-[10px] md:text-xs" style="color: var(--muted)">{{ date }}</span>
    </div>
  </header>
</template>

<script setup>
import { computed, ref, onMounted, onUnmounted } from 'vue'
import { useRoute } from 'vue-router'
import { IconChevronRight, IconMenu } from '@tabler/icons-vue'

defineEmits(['toggleSidebar'])

const route = useRoute()
const isMobile = ref(false)

function checkViewport() {
  isMobile.value = window.innerWidth < 768
}

onMounted(() => {
  checkViewport()
  window.addEventListener('resize', checkViewport)
})

onUnmounted(() => {
  window.removeEventListener('resize', checkViewport)
})

const titles = {
  '/': 'Dashboard',
  '/bookings': 'Bookings',
  '/receipts': 'Receipts',
  '/flights': 'Flights',
  '/mawbs': 'MAWBs',
  '/load-planning': 'Load Planning',
  '/ulds': 'ULDs',
  '/users': 'Usuarios',
  '/settings': 'Configuración',
}
const title = computed(() => titles[route.path] || 'AirCargo')
const date = new Intl.DateTimeFormat('es-DO', {
  weekday: 'short', day: 'numeric', month: 'short'
}).format(new Date())
</script>
