const KEY = 'aircargo_theme'
const VALID = ['light', 'tokyo']

export function getTheme() {
  const t = localStorage.getItem(KEY)
  return VALID.includes(t) ? t : 'light'
}

export function applyTheme(theme) {
  document.documentElement.setAttribute('data-theme', theme)
}

export function setTheme(theme) {
  const next = VALID.includes(theme) ? theme : 'light'
  localStorage.setItem(KEY, next)
  applyTheme(next)
  return next
}

export function initTheme() {
  applyTheme(getTheme())
}
