package cn.blockchain.copyrightsoft;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@SpringBootTest
class CopyrightsoftApplicationTests {

	@Test
	void contextLoads() {
		BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

		String rawPassword = "admin123";
		String encodedPassword = encoder.encode(rawPassword);

		System.out.println("原始密码: " + rawPassword);
		System.out.println("BCrypt 加密后: " + encodedPassword);
		System.out.println("\n请在数据库中执行以下 SQL:");
		System.out.println("INSERT INTO users (username, password, email, nickname, role, account_type, display_subject_name, status) VALUES ");
		System.out.println("('admin', '" + encodedPassword + "', 'admin@copyrightsoft.com', '系统管理员', 'ADMIN', 'INDIVIDUAL', '系统管理员', 1);");
	}

}
