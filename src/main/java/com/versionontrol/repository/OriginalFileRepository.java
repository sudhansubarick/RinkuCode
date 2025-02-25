package com.versionontrol.repository;


import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.versionontrol.entity.OriginalFile;

public interface OriginalFileRepository extends JpaRepository<OriginalFile,Long> {

      @Query("SELECT f FROM OriginalFile f WHERE f.id = :fileId AND f.user.id = :userId")
    Optional<OriginalFile> findByUserIdAndFileId(@Param("userId") Long userId, @Param("fileId") Long fileId);

  
 
}
