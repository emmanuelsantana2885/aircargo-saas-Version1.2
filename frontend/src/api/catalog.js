import api from './client'

export const catalogApi = {
  getCatalog() {
    return api.get('/catalog')
  },
  getReports(type, params = {}) {
    return api.get(`/reports/${type}`, { params, responseType: 'blob' })
  },
  getBiSummary(params = {}) {
    return api.get('/bi/summary', { params })
  },
  getBiByLocation() {
    return api.get('/bi/by-location')
  },
  getBiTimeline(params = {}) {
    return api.get('/bi/timeline', { params })
  },
  getBiTopMawbs(limit = 10) {
    return api.get('/bi/top-mawbs', { params: { limit } })
  },
  getBiFlightPerformance(params = {}) {
    return api.get('/bi/flight-performance', { params })
  },
}
