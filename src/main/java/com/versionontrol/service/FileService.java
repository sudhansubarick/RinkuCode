package com.versionontrol.service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.versionontrol.entity.FileVersion;
import com.versionontrol.entity.OriginalFile;
import com.versionontrol.entity.User;
import com.versionontrol.repository.FileVersionRepository;
import com.versionontrol.repository.OriginalFileRepository;
import com.versionontrol.repository.UserRepository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Service
public class FileService {

    private final Path fileStorageLocation = Paths.get("uploads").toAbsolutePath().normalize();

    @Autowired
    private OriginalFileRepository originalFileRepository;

    @Autowired
    private FileVersionRepository fileVersionRepository;

    @Autowired
    private UserRepository userRepository;

    public FileService() {
        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (IOException ex) {
            throw new RuntimeException("Could not create the directory to upload files.", ex);
        }
    }

    public OriginalFile saveOriginalFile(MultipartFile file,Long userId) throws IOException {
        String fileName = file.getOriginalFilename();
        Path targetLocation = this.fileStorageLocation.resolve(fileName);

        // Save the file to the filesystem
        Files.copy(file.getInputStream(), targetLocation);

         // Find the user
         User user = userRepository.findById(userId)
         .orElseThrow(() -> new RuntimeException("User not found"));

        // Save original file metadata to the database
        OriginalFile originalFile = new OriginalFile();
        originalFile.setFileName(fileName);
        originalFile.setFilePath(targetLocation.toString());
        originalFile.setCreatedAt(LocalDateTime.now());
        originalFile.setUser(user);
        return originalFileRepository.save(originalFile);
    }

    public FileVersion saveFileVersion(MultipartFile file, Long originalFileId,Long userId) throws IOException {
       // String fileName = file.getOriginalFilename();
       // Path targetLocation = this.fileStorageLocation.resolve(fileName);


        // String fileExtension = "";

        // if (fileName != null && fileName.contains(".")) {
        //     fileExtension = fileName.substring(fileName.lastIndexOf("."));
        //     fileName = fileName.substring(0, fileName.lastIndexOf("."));
        // }


        // // Append Timestamp to Make it Unique
        // String timestamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        // String newFileName = fileName + "_" + timestamp + fileExtension;

        //  Find the original file
        OriginalFile originalFile = originalFileRepository.findById(originalFileId)
                .orElseThrow(() -> new RuntimeException("Original file not found"));

                //  Get latest version number (default to 0 if no previous versions exist)
                int latestVersion = fileVersionRepository.findLatestVersionByOriginalFileId(originalFileId).orElse(0);
                int newFileName = latestVersion + 1;  //  Increment the version
        
                String fileName = originalFile.getFileName() + "_v" + newFileName;

        Path targetLocation = fileStorageLocation.resolve(fileName);

          // Find the user
          User user = userRepository.findById(userId)
          .orElseThrow(() -> new RuntimeException("User not found"));

        // Copy File
        Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

        // Determine the next version number
        int nextVersion = fileVersionRepository.findByOriginalFileIdOrderByVersionDesc(originalFileId)
                .stream()
                .findFirst()
                .map(FileVersion::getVersion)
                .orElse(0) + 1;

        // Save versioned file metadata to the database
        FileVersion fileVersion = new FileVersion();
        fileVersion.setFileName(fileName);
        fileVersion.setFilePath(targetLocation.toString());
        fileVersion.setVersion(nextVersion);
        fileVersion.setCreatedAt(LocalDateTime.now());
        fileVersion.setUser(user);

        // Associate with the original file 
        OriginalFile originalFiles = originalFileRepository.findById(originalFileId)
                .orElseThrow(() -> new RuntimeException("Original file not found"));
        fileVersion.setOriginalFile(originalFiles);

        return fileVersionRepository.save(fileVersion);
    }
//get all version file by user id
    public List<FileVersion> getFileVersionsByOriginalFileIdAndUserId(Long originalFileId, Long userId) {
        return fileVersionRepository.findByOriginalFileIdAndUserId(originalFileId, userId);
    }
   

//Get version File

public FileVersion getFileVersionByIds(Long versionId, Long userId, Long originalFileId) {
    return fileVersionRepository.findByVersionIdAndUserIdAndOriginalFileId(versionId, userId, originalFileId)
            .orElseThrow(() -> new RuntimeException("File version not found for given IDs"));
}

    // //Get original File


    public OriginalFile getFileByUserIdAndFileId(Long userId, Long fileId) {
        return originalFileRepository.findByUserIdAndFileId(userId, fileId)
                .orElseThrow(() -> new RuntimeException("File not found for User ID: " + userId + " and File ID: " + fileId));
    }

    //find latest version file


    public FileVersion getLatestFileVersion(Long originalFileId, Long userId) {
        return fileVersionRepository.findLatestVersionByOriginalFileIdAndUserId(originalFileId, userId)
                .orElseThrow(() -> new RuntimeException("No file versions found for the given Original File ID and User ID"));
    }

    //get all user

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
}