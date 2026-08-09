import api from './client'

export const uldTypeConfigApi = {
  getAll: () => api.get('/uld-type-config'),
  getByAirline: (airlineId) => api.get(`/uld-type-config/${airlineId}`),
  getById: (id) => api.get(`/uld-type-config/config/${id}`),
  create: (data) => api.post('/uld-type-config', data),
  update: (id, data) => api.put(`/uld-type-config/${id}`, data),
  delete: (id) => api.delete(`/uld-type-config/${id}`),
  replaceForAirline: (airlineId, rows) => api.put(`/uld-type-config/airline/${airlineId}/bulk`, rows),
}
