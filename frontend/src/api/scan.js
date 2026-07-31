import api from './client.js'

export const scanApi = {
  lookup:   (code, uldId) => api.get('/scan/lookup', { params: { code, uldId } }),
  piece:    (dto) => api.post('/scan/piece', dto),
  undoLast: (uldId, mawbId) => api.delete('/scan/piece/last', { params: { uldId, mawbId } }),
}
