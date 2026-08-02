const PASSWORD_RULES = {
  length: 12,
}

export function checkPasswordStrength(password = '') {
  return {
    length: password.length >= PASSWORD_RULES.length,
    upper: /[A-Z]/.test(password),
    lower: /[a-z]/.test(password),
    number: /[0-9]/.test(password),
    special: /[^A-Za-z0-9]/.test(password),
  }
}

export function isStrongPassword(password = '') {
  const r = checkPasswordStrength(password)
  return r.length && r.upper && r.lower && r.number && r.special
}

export const passwordRuleLabels = [
  { key: 'length', label: 'Al menos 12 caracteres' },
  { key: 'upper', label: 'Una letra mayúscula' },
  { key: 'lower', label: 'Una letra minúscula' },
  { key: 'number', label: 'Un número' },
  { key: 'special', label: 'Un carácter especial (!@#$...)' },
]
