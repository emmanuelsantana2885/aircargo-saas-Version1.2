import api from './client'

export const airlinesApi = {
  getAll: () => api.get('/airlines'),
  getById: (id) => api.get(`/airlines/${id}`),
  create: (data) => api.post('/airlines', data),
  update: (id, data) => api.put(`/airlines/${id}`, data),
  delete: (id) => api.delete(`/airlines/${id}`),
}
