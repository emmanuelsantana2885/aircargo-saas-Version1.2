import api from './client.js'

export const scanApi = {
  lookup:   (code, uldId) => api.get('/scan/lookup', { params: { code, uldId } }),
  piece:    (dto) => api.post('/scan/piece', dto),
  undoLast: (uldId, mawbId) => api.delete('/scan/piece/last', { params: { uldId, mawbId } }),
  eventSource: (flightId) => {
    const token = localStorage.getItem('aircargo_auth')
    const params = token ? `?token=${token}` : ''
    return new EventSource(`/api/scan/events/${flightId}${params}`)
  },
}
