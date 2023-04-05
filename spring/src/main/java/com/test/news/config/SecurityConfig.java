package com.test.news.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import com.test.news.oauth2.CustomAccessDenyHandler;
import com.test.news.oauth2.CustomOAuthUserService;

import jakarta.servlet.DispatcherType;
import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
        private final CustomOAuthUserService customOAuthUserService;
        private final CustomAccessDenyHandler customAccessDenyHandler;

        private static final String[] URL_TO_PERMITALL = {
                        "/images/**", "/css/**", // 리소스
                        "PropertySource", "/", "/main", // 메인
                        "/v3/api-docs/**", "/swagger-ui/**", // swagger util
                        "/query_search", "/search", // 검색 페이지
                        "/user/denied", "/board/list/**", "/user/login", "/favicon.ico"

        };

        private static final String[] URL_TO_PERMIT_USER = {

        };

        @Bean
        public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
                http
                                .csrf(csrf -> csrf.disable())
                                .exceptionHandling(handling -> handling
                                                .accessDeniedHandler(customAccessDenyHandler)
                                                .accessDeniedPage("/user/denied"))
                                .authorizeHttpRequests(request -> request
                                                .dispatcherTypeMatchers(DispatcherType.FORWARD).permitAll()
                                                .requestMatchers(URL_TO_PERMITALL).permitAll()
                                                .requestMatchers(URL_TO_PERMIT_USER).hasRole("USER")
                                                .anyRequest().authenticated());
                http
                                .formLogin(formlogin -> formlogin.disable())
                                .oauth2Login(login -> login
                                                .loginPage("/user/denied")
                                                .defaultSuccessUrl("/user/loginSuccess")
                                                // .successHandler(new LoginSuccessHandler("/"))
                                                // .failureUrl("/user/login")
                                                .authorizationEndpoint()
                                                .baseUri("/oauth2/authorize")
                                                .and()
                                                .redirectionEndpoint()
                                                .baseUri("/oauth2/callback/*")
                                                .and()
                                                .userInfoEndpoint()
                                                .userService(customOAuthUserService));

                http
                                .sessionManagement(manage -> manage
                                                .maximumSessions(1)
                                                .maxSessionsPreventsLogin(false));

                http
                                .logout(logout -> logout
                                                .logoutRequestMatcher(new AntPathRequestMatcher("/user/logout"))
                                                .logoutSuccessUrl("/")
                                                .clearAuthentication(true)
                                                .invalidateHttpSession(true));

                return http.build();
        }
}
