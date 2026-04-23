package cn.blockchain.copyrightsoft;

import cn.blockchain.copyrightsoft.auth.AuthDomainRules;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class AuthDomainRulesTests {

    @Test
    void shouldSupportLegacyUserRoleNormalization() {
        Assertions.assertEquals(
                AuthDomainRules.ROLE_INDIVIDUAL_DEVELOPER,
                AuthDomainRules.normalizeRole(AuthDomainRules.ROLE_USER_LEGACY)
        );
    }

    @Test
    void shouldValidateAccountTypeRoleCompatibility() {
        Assertions.assertTrue(AuthDomainRules.isRoleCompatibleWithAccountType(
                AuthDomainRules.ACCOUNT_TYPE_INDIVIDUAL,
                AuthDomainRules.ROLE_INDIVIDUAL_DEVELOPER
        ));
        Assertions.assertTrue(AuthDomainRules.isRoleCompatibleWithAccountType(
                AuthDomainRules.ACCOUNT_TYPE_ENTERPRISE,
                AuthDomainRules.ROLE_ENTERPRISE_DEVELOPER
        ));
        Assertions.assertFalse(AuthDomainRules.isRoleCompatibleWithAccountType(
                AuthDomainRules.ACCOUNT_TYPE_ENTERPRISE,
                AuthDomainRules.ROLE_INDIVIDUAL_DEVELOPER
        ));
    }
}
