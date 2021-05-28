package com.syy.security.chapter04.model.repository;

import com.syy.security.chapter04.model.domain.UserDO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<UserDO,Integer> {

    UserDO findUserByUsername(String username);

}
