package com.versionontrol.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.versionontrol.entity.FileVersion;

public interface FileVersionRepository extends JpaRepository<FileVersion,Long>{

    List<FileVersion> findByOriginalFileIdOrderByVersionDesc(Long originalFileId);
    List<FileVersion> findByOriginalFileIdAndUserId(Long originalFileId, Long userId);

    Optional<FileVersion> findById(Long versionId);

    @Query("SELECT MAX(f.version) FROM FileVersion f WHERE f.originalFile.id = :originalFileId")
    Optional<Integer> findLatestVersionByOriginalFileId(@Param("originalFileId") Long originalFileId);

// To select latest version file
    // @Query("SELECT fv FROM FileVersion fv WHERE fv.originalFile.id = :originalFileId ORDER BY fv.version DESC LIMIT 1")
    // Optional<FileVersion> findLatestVersionByOriginalFileId(@Param("originalFileId") Long originalFileId);

    // To select latest version file using original file id and userid

    @Query("SELECT fv FROM FileVersion fv " +
           "WHERE fv.originalFile.id = :originalFileId AND fv.originalFile.user.id = :userId " +
           "ORDER BY fv.version DESC LIMIT 1")
    Optional<FileVersion> findLatestVersionByOriginalFileIdAndUserId(
        @Param("originalFileId") Long originalFileId, 
        @Param("userId") Long userId
    );

// // To select specific version file through user id
//     @Query("SELECT fv FROM FileVersion fv WHERE fv.id = :versionId AND fv.originalFile.user.id = :userId")
//     Optional<FileVersion> findByVersionIdAndUserId(@Param("versionId") Long versionId, @Param("userId") Long userId);

// To select specific version file using userid,original file id and version file id
@Query("SELECT fv FROM FileVersion fv WHERE fv.id = :versionId AND fv.originalFile.id = :originalFileId AND fv.originalFile.user.id = :userId")
Optional<FileVersion> findByVersionIdAndUserIdAndOriginalFileId(
    @Param("versionId") Long versionId,
    @Param("userId") Long userId,
    @Param("originalFileId") Long originalFileId
);
}
