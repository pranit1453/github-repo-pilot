package com.pranit.github.security.config;

import com.pranit.github.authentication.oauth2.service.GithubOAuth2UserService;
import com.pranit.github.authentication.service.impl.OAuth2AuthenticationFailureHandler;
import com.pranit.github.authentication.service.impl.OAuth2AuthenticationSuccessHandler;
import com.pranit.github.properties.CorsProperties;
import com.pranit.github.security.endpoints.PublicEndpointProvider;
import com.pranit.github.security.entrypoint.AuthenticationTokenEntryPoint;
import com.pranit.github.security.filter.AuthenticationTokenFilter;
import jakarta.servlet.DispatcherType;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableMethodSecurity
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(
            final HttpSecurity http,
            final AuthenticationTokenEntryPoint entryPoint,
            final PublicEndpointProvider publicEndpointProvider,
            final GithubOAuth2UserService githubOAuth2UserService,
            final OAuth2AuthenticationSuccessHandler successHandler,
            final OAuth2AuthenticationFailureHandler failureHandler,
            final AuthenticationTokenFilter filter,
            final CorsProperties corsProperties) {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource(corsProperties)))
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(request -> request
                        .dispatcherTypeMatchers(DispatcherType.ASYNC).permitAll()
                        .requestMatchers(publicEndpointProvider.publicEndpoints()).permitAll()
                        .anyRequest().authenticated())
                .oauth2Login(oauth2 -> oauth2
                        .userInfoEndpoint(userInfo -> userInfo
                                .userService(githubOAuth2UserService))
                        .successHandler(successHandler)
                        .failureHandler(failureHandler)
                )
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(entryPoint))
                .addFilterBefore(filter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    AuthenticationManager authenticationManager(final AuthenticationConfiguration builder) {
        return builder.getAuthenticationManager();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource(final CorsProperties properties) {
        final var config = new CorsConfiguration();
        config.setAllowedOrigins(properties.allowedOrigins());
        config.setAllowedMethods(properties.allowedMethods());
        config.setAllowedHeaders(properties.allowedHeaders());
        config.setAllowCredentials(properties.allowCredentials());

        final var source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return source;
    }
}
