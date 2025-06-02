package com.fennel.aceinterview.aceauth.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository("SysUserRepository")
public interface SysUserRepository extends JpaRepository<SysUser,Long> {

    SysUser findByUserId(String userId);
}
