package cn.blockchain.copyrightsoft.config;

import cn.blockchain.copyrightsoft.auth.AuthDomainRules;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
/**
 * Spring Security 访问控制配置。
 * <p>
 * 采用无状态 JWT 认证，不启用服务端会话；通过路由前缀划分认证与授权边界：
 * auth/query 开放访问，audit/admin/业务写操作按角色受控。
 */
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/api/copyright/query/**").permitAll()
                // 申请与个人记录仅开发者角色可访问，审核/管理员独立授权。
                .requestMatchers("/api/copyright/apply", "/api/copyright/my-records")
                    .hasAnyRole(
                        AuthDomainRules.ROLE_INDIVIDUAL_DEVELOPER,
                        AuthDomainRules.ROLE_ENTERPRISE_DEVELOPER,
                        AuthDomainRules.ROLE_USER_LEGACY
                    )
                .requestMatchers("/api/audit/**")
                    .hasAnyRole(AuthDomainRules.ROLE_AUDITOR, AuthDomainRules.ROLE_ADMIN)
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }


    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        // 当前仅放行本地前端开发地址，部署时应改为环境化配置。
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:3000"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
