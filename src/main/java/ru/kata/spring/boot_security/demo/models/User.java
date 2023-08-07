package ru.kata.spring.boot_security.demo.models;


import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import javax.persistence.*;
import java.util.*;

/**
 * //UserDetails нужен д/того, чтобы преобразовать юзера из БД к определенному стандарту, чтобы его понял Спринг Секьюрити.
 * //Т.е. UserDetails - это такая обертка д/Entity-класса.
 * //UserDetails заведует самым основным: полномочиями - getAuthorities(), паролем - getPassword() и
 * // именем юзера - getUsername()
 **/
@Entity
@Table(name = "users")
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "username") //Имя юзера должно быть уникальным
    private String username;

    @Column(name = "password")
    private String password;

    @ManyToMany(fetch = FetchType.EAGER)
    //Здесь жадная загрузка, чтобы сразу грузились все дочерние зависимости юзера. fetch (извлечение)
    @JoinTable(name = "users_roles",
            joinColumns = @JoinColumn(name = "user_id", referencedColumnName = "id"),//Это колонка текущей сущности, т.е. User.
            inverseJoinColumns = @JoinColumn(name = "role_id", referencedColumnName = "id"))
    //Это колонка второй (обратной) сущности, с которой связан User, т.е. Role.
    private List<Role> roles = new ArrayList<>();

    public User() {
    }

    public User(String username, String password, List<Role> roles) {
        this.username = username;
        this.password = password;
        this.roles = roles;
    }

    public Long getId() {
        return id;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public List<Role> getRoles() {
        return roles;
    }

    public void setRoles(List<Role> roles) {
        this.roles = roles;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return getRoles();
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", roles=" + roles +
                '}';
    }
}


