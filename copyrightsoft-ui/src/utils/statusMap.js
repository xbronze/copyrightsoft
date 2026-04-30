/**
 * 前后端状态码文案映射。
 * 统一管理业务状态、审核状态、风险级别、角色与主体类型，避免各页面散落硬编码。
 */
const BIZ_STATUS_MAP = {
  SUBMITTED: '已提交',
  AUTO_CHECKED: '自动核验完成',
  PENDING_REVIEW: '待人工复核',
  ONCHAIN_SUCCESS: '上链成功',
  ONCHAIN_FAILED: '上链失败',
  REJECTED: '已驳回'
}

const AUDIT_STATUS_MAP = {
  PENDING: '待审核',
  APPROVED: '审核通过',
  REJECTED: '审核驳回'
}

const RISK_LEVEL_MAP = {
  LOW: '低风险',
  MEDIUM: '中风险',
  HIGH: '高风险'
}

const REVIEW_RESULT_MAP = {
  APPROVED: '复核通过',
  REJECTED: '复核驳回'
}

const ENTERPRISE_ROLE_MAP = {
  OWNER: '企业管理员',
  DEVELOPER: '企业开发者',
  LEGAL: '企业法务'
}

const LEGAL_SCOPE_MAP = {
  SELF: '仅本人',
  ALL: '全企业'
}

const ROLE_MAP = {
  USER: '旧普通用户',
  INDIVIDUAL_DEVELOPER: '个人开发者',
  ENTERPRISE_DEVELOPER: '企业开发者',
  ENTERPRISE_LEGAL: '企业法务',
  AUDITOR: '审核员',
  ADMIN: '管理员'
}

const ACCOUNT_TYPE_MAP = {
  INDIVIDUAL: '个人主体',
  ENTERPRISE: '企业主体'
}

export function toBizStatusText(code) {
  return BIZ_STATUS_MAP[code] || code || '-'
}

export function toAuditStatusText(code) {
  return AUDIT_STATUS_MAP[code] || code || '-'
}

export function toRiskLevelText(code) {
  return RISK_LEVEL_MAP[code] || code || '-'
}

export function toReviewResultText(code) {
  return REVIEW_RESULT_MAP[code] || code || '-'
}

export function toEnterpriseRoleText(code) {
  return ENTERPRISE_ROLE_MAP[code] || code || '-'
}

export function toLegalScopeText(code) {
  return LEGAL_SCOPE_MAP[code] || code || '-'
}

export function toRoleText(code) {
  return ROLE_MAP[code] || code || '-'
}

export function toAccountTypeText(code) {
  return ACCOUNT_TYPE_MAP[code] || code || '-'
}

export function toSubjectTypeText(code) {
  return toAccountTypeText(code)
}
