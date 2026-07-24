import api from './client'

export const hawbsApi = {
  getByMawb: (mawbId) => api.get(`/cargo/hawbs/mawb/${mawbId}`),
  create: (dto) => api.post('/cargo/hawbs', dto),
  update: (id, dto) => api.put(`/cargo/hawbs/${id}`, dto),
}
