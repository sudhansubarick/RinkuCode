package com.versionontrol.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.versionontrol.entity.User;

public interface UserRepository extends JpaRepository<User,Long>{

}
