package cn.blockchain.copyrightsoft.auth;

import java.util.Set;

public final class AuthDomainRules {

    public static final String ACCOUNT_TYPE_INDIVIDUAL = "INDIVIDUAL";
    public static final String ACCOUNT_TYPE_ENTERPRISE = "ENTERPRISE";

    public static final String ROLE_INDIVIDUAL_DEVELOPER = "INDIVIDUAL_DEVELOPER";
    public static final String ROLE_ENTERPRISE_DEVELOPER = "ENTERPRISE_DEVELOPER";
    public static final String ROLE_AUDITOR = "AUDITOR";
    public static final String ROLE_ADMIN = "ADMIN";

    // Legacy role kept for compatibility during migration window.
    public static final String ROLE_USER_LEGACY = "USER";

    private static final Set<String> INDIVIDUAL_ALLOWED_ROLES = Set.of(ROLE_INDIVIDUAL_DEVELOPER);
    private static final Set<String> ENTERPRISE_ALLOWED_ROLES = Set.of(ROLE_ENTERPRISE_DEVELOPER);
    private static final Set<String> PLATFORM_ROLES = Set.of(ROLE_AUDITOR, ROLE_ADMIN);
    private static final Set<String> DEVELOPER_ROLES = Set.of(
            ROLE_INDIVIDUAL_DEVELOPER,
            ROLE_ENTERPRISE_DEVELOPER,
            ROLE_USER_LEGACY
    );

    private AuthDomainRules() {
    }

    public static boolean isAccountTypeValid(String accountType) {
        return ACCOUNT_TYPE_INDIVIDUAL.equals(accountType) || ACCOUNT_TYPE_ENTERPRISE.equals(accountType);
    }

    public static boolean isRoleCompatibleWithAccountType(String accountType, String role) {
        if (ROLE_USER_LEGACY.equals(role)) {
            return ACCOUNT_TYPE_INDIVIDUAL.equals(accountType);
        }
        if (ACCOUNT_TYPE_INDIVIDUAL.equals(accountType)) {
            return INDIVIDUAL_ALLOWED_ROLES.contains(role);
        }
        if (ACCOUNT_TYPE_ENTERPRISE.equals(accountType)) {
            return ENTERPRISE_ALLOWED_ROLES.contains(role);
        }
        return false;
    }

    public static boolean isPlatformRole(String role) {
        return PLATFORM_ROLES.contains(role);
    }

    public static boolean isDeveloperRole(String role) {
        return DEVELOPER_ROLES.contains(role);
    }

    public static String normalizeRole(String role) {
        if (ROLE_USER_LEGACY.equals(role)) {
            return ROLE_INDIVIDUAL_DEVELOPER;
        }
        return role;
    }
}
