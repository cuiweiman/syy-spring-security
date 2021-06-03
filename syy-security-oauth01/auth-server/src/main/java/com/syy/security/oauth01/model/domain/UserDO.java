package com.syy.security.oauth01.model.domain;

import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * @Description: 用户
 * @Author: cuiweiman
 * @Since: 2021/5/26 下午1:29
 */
@Data
@Accessors(chain = true)
@Entity(name = "t_user")
public class UserDO implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;

    private String password;

    /**
     * 账户是否没有过期，true-没过期
     */
    private boolean accountNonExpired;

    /**
     * 账户是否没有被锁定，true-没锁定
     */
    private boolean accountNonLocked;

    /**
     * 密码是否没有过期，true-没过期
     */
    private boolean credentialsNonExpired;

    /**
     * 以及账户是否可用，true-可用
     */
    private boolean enabled;

    /**
     * 用户 角色 多对多
     */
    @ManyToMany(fetch = FetchType.EAGER, cascade = CascadeType.PERSIST)
    private List<RoleDO> roleDOS;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        for (RoleDO roleDO : getRoleDOS()) {
            authorities.add(new SimpleGrantedAuthority(roleDO.getName()));
        }
        return authorities;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        UserDO user = (UserDO) o;
        return Objects.equals(username, user.username);
    }

    @Override
    public int hashCode() {
        return Objects.hash(username);
    }

}
