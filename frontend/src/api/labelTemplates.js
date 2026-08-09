import api from './client'

function filenameFromContentDisposition(disposition, fallback) {
  if (!disposition) return fallback
  const utf = disposition.match(/filename\*=UTF-8''([^;]+)/i)
  if (utf) return decodeURIComponent(utf[1])
  const plain = disposition.match(/filename="?([^";]+)"?/i)
  if (plain) return plain[1]
  return fallback
}

function saveBlob(res, fallbackName) {
  const blob = new Blob([res.data])
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = filenameFromContentDisposition(res.headers?.['content-disposition'], fallbackName)
  a.click()
  URL.revokeObjectURL(url)
}

export const labelTemplatesApi = {
  list:   (type) => api.get('/label-templates', { params: { type } }),
  get:    (id) => api.get(`/label-templates/${id}`),
  create: (dto) => api.post('/label-templates', dto),
  update: (id, dto) => api.put(`/label-templates/${id}`, dto),
  remove: (id) => api.delete(`/label-templates/${id}`),
}

export const labelsApi = {
  generateCargo: (payload) => api.post('/mawbs/labels', payload, { responseType: 'blob' }),
  generatePallet: (payload) => api.post('/ulds/labels', payload, { responseType: 'blob' }),
  downloadCargo: async (payload) => {
    const res = await labelsApi.generateCargo(payload)
    saveBlob(res, `CARGO_LABELS.${payload.format === 'ZPL' ? 'zpl' : 'pdf'}`)
  },
  downloadPallet: async (payload) => {
    const res = await labelsApi.generatePallet(payload)
    saveBlob(res, `PALLET_LABELS.${payload.format === 'ZPL' ? 'zpl' : 'pdf'}`)
  },
}
