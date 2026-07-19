<template>
  <div class="flex items-center gap-1 flex-wrap justify-center min-w-[80px]">
    <template v-for="item in visible" :key="item.type">
      <span
        class="inline-flex items-center gap-1 px-2 py-0.5 rounded text-[9px] font-mono font-medium uppercase tracking-tight border"
        :style="chipStyle(item.type)"
        :title="`${item.label}: ${item.weight} lbs (${item.pieces} pcs)`">
        <span class="w-1.5 h-1.5 rounded-full" :style="{ background: chipColor(item.type) }"></span>
        <span>{{ item.shortLabel }}</span>
        <span class="text-[8px] text-slate-500 font-normal">{{ item.weight }} lbs</span>
      </span>
    </template>
    <span v-if="hidden > 0" class="px-1.5 py-0.5 text-[9px] font-mono text-slate-400" title="{{ hidden }} más">
      +{{ hidden }}
    </span>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useAppStore } from '../stores/app'

const props = defineProps({
  flightId: { type: String, required: true },
  maxVisible: { type: Number, default: 3 },
})

const appStore = useAppStore()

const COMMODITY_MAP = {
  PERISHABLE:       { label: 'PERISHABLE',        short: 'PER',  color: '#ef4444' },
  DRY_CARGO:        { label: 'DRY CARGO',         short: 'DRY',  color: '#64748b' },
  ELECTRONICS:      { label: 'ELECTRONICS',       short: 'ELEC', color: '#8b5cf6' },
  HIGH_VALUES:      { label: 'HIGH VALUES',       short: 'HIGH', color: '#f59e0b' },
  CIGARETTES:       { label: 'CIGARETTES',        short: 'CIG',  color: '#78716c' },
  SMALL_PACKAGES:   { label: 'SMALL PACKAGES',    short: 'SMP',  color: '#06b6d4' },
  WWEF:             { label: 'WWEF',              short: 'WWEF', color: '#ec4899' },
  LIVE_PLANTS:      { label: 'LIVE PLANTS',       short: 'PLNT', color: '#22c55e' },
  GENERAL:          { label: 'GENERAL',           short: 'GEN',  color: '#94a3b8' },
  COMAT:            { label: 'COMAT',             short: 'COMT', color: '#a3a3a3' },
  FCC:              { label: 'FCC',               short: 'FCC',  color: '#78716c' },
  EMPTY_ULD:        { label: 'EMPTY ULD',         short: 'EMP',  color: '#d1d5db' },
  EMPTY_PALLET:     { label: 'EMPTY PALLET',      short: 'EPT',  color: '#d1d5db' },
  RED_TAG:          { label: 'RED TAG',           short: 'RED',  color: '#dc2626' },
  EMPTY_BAGS:       { label: 'EMPTY BAGS',        short: 'BAG',  color: '#a3a3a3' },
  NETS:             { label: 'NETS',              short: 'NET',  color: '#52525b' },
  SDQ_SDF:          { label: 'SDQ-SDF',           short: 'SDQ',  color: '#2563eb' },
  SDQ_MIA:          { label: 'SDQ-MIA',           short: 'MIA',  color: '#2563eb' },
}

function mawbsForFlight() {
  return appStore.mawbs.filter(m => m.flightId === props.flightId)
}

const aggregated = computed(() => {
  const map = {}
  const mawbs = mawbsForFlight()

  mawbs.forEach(m => {
    const type = m.commodityType || 'DRY_CARGO'
    const info = COMMODITY_MAP[type] || COMMODITY_MAP.DRY_CARGO
    const weight = Number(m.chargeableWeightKg || m.reportedWeightKg || 0) * 2.20462 // kg → lbs
    const pieces = Number(m.pieces || 0)

    if (!map[type]) {
      map[type] = { type, label: info.label, shortLabel: info.short, color: info.color, weight: 0, pieces: 0 }
    }
    map[type].weight += weight
    map[type].pieces += pieces
  })

  return Object.values(map)
    .filter(c => c.weight > 0 || c.pieces > 0)
    .sort((a, b) => b.weight - a.weight)
})

const visible = computed(() => aggregated.value.slice(0, props.maxVisible))
const hidden = computed(() => Math.max(0, aggregated.value.length - props.maxVisible))

function chipStyle(type) {
  const info = COMMODITY_MAP[type] || COMMODITY_MAP.DRY_CARGO
  return {
    background: info.color + '15',
    borderColor: info.color + '40',
    color: info.color,
  }
}

function chipColor(type) {
  const info = COMMODITY_MAP[type] || COMMODITY_MAP.DRY_CARGO
  return info.color
}
</script>

<style scoped>
/* chips are inline-styled */
</style>