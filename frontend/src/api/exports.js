import api from './client'

export function exportData(type, format = 'csv', dateFrom = null, dateTo = null, audit = false) {
  const params = { format, audit }
  if (dateFrom) params.dateFrom = dateFrom
  if (dateTo) params.dateTo = dateTo
  return api.get(`/exports/${type}`, { params, responseType: 'blob' })
}

export function getExportJson(type, dateFrom = null, dateTo = null, audit = false) {
  const params = { format: 'json', audit }
  if (dateFrom) params.dateFrom = dateFrom
  if (dateTo) params.dateTo = dateTo
  return api.get(`/exports/${type}`, { params })
}
