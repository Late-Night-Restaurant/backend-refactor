package com.backend.simya.global.config.security;

import com.backend.simya.domain.jwt.service.TokenProvider;
import com.backend.simya.global.config.jwt.JwtAccessDeniedHandler;
import com.backend.simya.global.config.jwt.JwtAuthenticationEntryPoint;
import com.backend.simya.global.config.jwt.JwtSecurityConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.filter.CorsFilter;

@Configuration
@EnableWebSecurity   // 기본적인 Web 보안 활성화
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    // jwt 디렉토리에 생성한 클래스들 SecurityConfig에 추가
    private final TokenProvider tokenProvider;
    private final CorsFilter corsFilter;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final JwtAccessDeniedHandler jwtAccessDeniedHandler;

    // 비밀번호 암호화
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // token을 사용하는 인증 방식이기 때문에 csrf는 disable
                .csrf().disable()

                .addFilterBefore(corsFilter, UsernamePasswordAuthenticationFilter.class)

                // JWT 토큰 에외처리
                // Exception Handling 시, 이전에 생성했던 클래스를 추가하여 Exception 상황에 대한 커스텀 적용
                .exceptionHandling()
                .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                .accessDeniedHandler(jwtAccessDeniedHandler)

                // 세션을 사용하지 않으므로 STATELESS 설정 (Security는 기본적으노 세션 방식을 사용)
                .and()
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)

                // 접근 권한 설정
                // token이 없는 상태에서 요청되는 로그인, 회원가입 API는 permitAll로 설정
                .and()
                .authorizeHttpRequests()
                .antMatchers(HttpMethod.OPTIONS).permitAll()
                .antMatchers( "/simya/form-login", "/simya/auth", "/simya/form-signup", "/api/**", "/simya/chat/**", "/chat/**", "/ws/chat", "/ws-stomp", "/pub/**", "/sub/**", "/webjars/**", "/ws-stomp/**").permitAll()
                .anyRequest().authenticated()

                .and()
                .apply(new JwtSecurityConfig(tokenProvider));   // addFilterBefore로 등록했던 JwtSecurityConfig 클래스 적용 추가

        return http.build();
    }

}

