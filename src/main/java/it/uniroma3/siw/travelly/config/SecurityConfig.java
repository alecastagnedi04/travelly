package it.uniroma3.siw.travelly.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.security.web.SecurityFilterChain;


@Configuration
public class SecurityConfig {


    @Bean
    public PasswordEncoder passwordEncoder() {

        return new BCryptPasswordEncoder();
    }


    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http) throws Exception {


        http

            .authorizeHttpRequests(authorize -> authorize


                /*
                 * =================================================
                 * PAGINE PUBBLICHE
                 * =================================================
                 */
                .requestMatchers(
                    "/",
                    "/login",
                    "/register",
                    "/explore",
                    "/explore/**",
                    "/css/**",
                    "/images/**",
                    "/uploads/**"
                )
                .permitAll()


                /*
                 * =================================================
                 * ADMIN
                 * =================================================
                 */
                .requestMatchers(
                    "/admin/**"
                )
                .hasRole("ADMIN")


                /*
                 * =================================================
                 * VIAGGI PERSONALI
                 * =================================================
                 */
                .requestMatchers(
                    "/trips/**"
                )
                .hasAnyRole(
                    "USER",
                    "ADMIN"
                )


                /*
                 * Tutto il resto richiede login.
                 */
                .anyRequest()
                .authenticated()
            )


            .formLogin(form -> form

                .loginPage(
                    "/login"
                )

                .defaultSuccessUrl(
                    "/trips",
                    true
                )

                .permitAll()
            )


            .logout(logout -> logout

                .logoutSuccessUrl(
                    "/"
                )

                .permitAll()
            );


        return http.build();
    }
}