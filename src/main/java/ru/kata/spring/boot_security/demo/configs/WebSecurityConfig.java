package ru.kata.spring.boot_security.demo.configs;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    private final UserDetailsService userDetailsService;
    private final SuccessUserHandler successUserHandler; //логика перенаправления юзеров по ролям на нужные url

    @Autowired
    public WebSecurityConfig(UserDetailsService userDetailsService, SuccessUserHandler successUserHandler) {
        this.userDetailsService = userDetailsService;
        this.successUserHandler = successUserHandler;
    }

    //Настраиваем конфигурацию самого Спринг Секьюрити
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .authorizeRequests()
                .antMatchers("/index", "/").permitAll() //url-адреса "/" и "/index" разрешены всем юзерам, в т.ч. не аутентифицированным
                .antMatchers("/admin/**").hasRole("ADMIN")//В "/admin/**" могут заходить только юзеры с ролью "ADMIN"
                .antMatchers("/user/**").hasAnyRole("USER", "ADMIN")
                .anyRequest().authenticated()//Все остальные url-адреса доступны только аутентифицированным
                .and()//разделитель

                .formLogin().successHandler(successUserHandler).permitAll()
                .and()//разделитель

                .logout().permitAll(); //Разлог разрешен всем
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.authenticationProvider(authenticationProvider());
    }

    /**
     * //Преобразователь паролей в хэш. Потому что пароли в БД лежат в преобразованном виде.
     * //Чтобы сравнить введенный с формы пароль с паролем в БД, нужно преобразовать в такой же вид пароль с формы.
     **/

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * //DaoAuthenticationProvider — это имплементация AuthenticationProvider, который получает информацию
     * //о пользователе с UserDetailsService.
     * //В AuthenticationProvider заложена логика сверки credentials, т.е. username и пароля, введенного с формы,
     * //с username и паролем в БД, т.е. он проверяет, есть ли такой юзер в БД или нет.
     * // Можно ли его аутентифицировать.
     **/
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider();
        authenticationProvider.setPasswordEncoder(passwordEncoder()); //Кодировщик, чтобы преобразовать введенный с формы пароль к хэшу, т.е. к тому виду пароля, который лежит в БД
        authenticationProvider.setUserDetailsService(userDetailsService);
        return authenticationProvider;
    }
}