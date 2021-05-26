package com.syy.security.chapter03.repository;

import com.syy.security.chapter03.SyySecurityChapter03AppTest;
import com.syy.security.chapter03.model.domain.RoleDO;
import com.syy.security.chapter03.model.domain.UserDO;
import com.syy.security.chapter03.model.repository.UserRepository;
import org.assertj.core.util.Lists;
import org.junit.Assert;
import org.junit.Test;

import javax.annotation.Resource;
import java.util.List;

/**
 * @Description: 填充数据
 * @Author: cuiweiman
 * @Since: 2021/5/26 下午4:46
 */
public class UserRepositoryTest extends SyySecurityChapter03AppTest {

    @Resource
    private UserRepository userRepository;

    @Test
    public void insertAccount() {
        UserDO me = new UserDO();
        me.setUsername("cwm").setPassword("250")
                .setAccountNonExpired(true).setAccountNonLocked(true)
                .setCredentialsNonExpired(true).setEnabled(true);
        List<RoleDO> meRoleList = Lists.newArrayList(
                new RoleDO("ROLE_me", "管理员")
        );
        me.setRoleDOS(meRoleList);
        UserDO meUser = userRepository.save(me);

        UserDO myGirl = new UserDO();
        myGirl.setUsername("syy").setPassword("520")
                .setAccountNonExpired(true).setAccountNonLocked(true)
                .setCredentialsNonExpired(true).setEnabled(true);
        List<RoleDO> myGirlRoleList = Lists.newArrayList(
                new RoleDO("ROLE_myGirl", "普通用户")
        );
        myGirl.setRoleDOS(myGirlRoleList);
        UserDO myGirlUser = userRepository.save(myGirl);

        Assert.assertNotNull(meUser);
        Assert.assertNotNull(myGirlUser);
    }

}
