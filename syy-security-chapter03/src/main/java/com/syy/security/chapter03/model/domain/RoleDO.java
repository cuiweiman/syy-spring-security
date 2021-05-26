package com.syy.security.chapter03.model.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

/**
 * @Description: 角色
 * @Author: cuiweiman
 * @Since: 2021/5/26 下午1:25
 */
@Data
@Entity(name = "t_role")
@NoArgsConstructor
@AllArgsConstructor
public class RoleDO {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String nameZh;

    public RoleDO(String name, String nameZh) {
        this.name = name;
        this.nameZh = nameZh;
    }
}
