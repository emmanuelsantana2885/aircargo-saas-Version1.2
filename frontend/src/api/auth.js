import api from './client'

export const authApi = {
  login: (email, password, totpCode) => api.post('/auth/login', { email, password, totpCode }),
  refresh: (refreshToken) => api.post('/auth/refresh', { refreshToken }),
  setPassword: (email, newPassword, currentPassword) =>
    api.post('/auth/set-password', { email, newPassword, currentPassword }),
  changePassword: (newPassword, currentPassword, totpCode) =>
    api.post('/auth/change-password', { newPassword, currentPassword, totpCode }),
  me: () => api.get('/auth/me'),
}
