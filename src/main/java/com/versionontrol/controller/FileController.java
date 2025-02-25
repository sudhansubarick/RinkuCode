package com.versionontrol.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.versionontrol.entity.FileVersion;
import com.versionontrol.entity.OriginalFile;
import com.versionontrol.entity.User;
import com.versionontrol.service.FileService;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Controller
public class FileController {

    @Autowired
    private FileService fileService;

   

    

    @GetMapping("/")
    public String home(Model model) {
        return "index"; // Render the upload form
    }

    @PostMapping("/upload/{userId}")
    public String uploadOriginalFile(@RequestParam("file") MultipartFile file,@PathVariable Long userId,Model model) throws IOException {
        OriginalFile originalFile = fileService.saveOriginalFile(file,userId);
        
         model.addAttribute("message", "Original file uploaded successfully: " + originalFile.getFileName());
         return "index";
    }

    @PostMapping("/upload-version/{originalFileId}/{userId}")
    public String uploadFileVersion(@RequestParam("file") MultipartFile file, @PathVariable Long originalFileId,@PathVariable Long userId, Model model) throws IOException {
        FileVersion fileVersion = fileService.saveFileVersion(file, originalFileId,userId);
        model.addAttribute("message", "File version uploaded successfully: " + fileVersion.getFileName() + " (Version " + fileVersion.getVersion() + ")");
        return "index";
    }

    //Return in Html page
    // @GetMapping("/versions/{originalFileId}")
    // public String getFileVersions(@PathVariable Long originalFileId, Model model) {
    //     List<FileVersion> versions = fileService.getFileVersions(originalFileId);
    //     model.addAttribute("versions", versions);
    //     return "versions";
    // }


//get the all versions files

    // @GetMapping("/versions/{originalFileId}")
    // public ResponseEntity<List<FileVersion>> getFileVersions(@PathVariable Long originalFileId) {
    //     List<FileVersion> versions = fileService.getFileVersions(originalFileId);
    //     return ResponseEntity.ok(versions); // Returns JSON response
    // }

    @GetMapping("/user/{userId}/original-file/{originalFileId}")
    public ResponseEntity<List<FileVersion>> getFileVersionsByOriginalFileIdAndUserId(
            @PathVariable Long userId,
            @PathVariable Long originalFileId) {
        List<FileVersion> fileVersions = fileService.getFileVersionsByOriginalFileIdAndUserId(originalFileId, userId);
        return ResponseEntity.ok(fileVersions);
    }

    // @GetMapping("/download/{fileId}")
    // public ResponseEntity<Resource> downloadFile(@PathVariable Long fileId) {
       
    //    FileVersion fileVersion = fileVersionRepository.findById(fileId)
    //             .orElseThrow(() -> new RuntimeException("File not found"));
    //     Path filePath = Paths.get(fileVersion.getFilePath());
    //     Resource resource;
    //     try {
    //         resource = new UrlResource(filePath.toUri());
    //     } catch (Exception ex) {
    //         throw new RuntimeException("File not found: " + fileVersion.getFileName(), ex);
    //     }

    //     return ResponseEntity.ok()
    //             .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
    //             .body(resource);
    // }
//Get version File

// @GetMapping("/version/{versionId}")
// public ResponseEntity<FileVersion> getFileVersionById(@PathVariable Long versionId) {
//     FileVersion fileVersion = fileService.getFileVersionById(versionId);
//     return ResponseEntity.ok(fileVersion);
// }

    // // Get a specific file version by userId and versionId
    // @GetMapping("/{userId}/file-versions/{versionId}")
    // public ResponseEntity<FileVersion> getFileVersionByUserAndVersion(
    //         @PathVariable Long userId,
    //         @PathVariable Long versionId) {

    //     FileVersion fileVersion = fileService.getFileVersionByUserAndVersion(userId, versionId);
    //     return ResponseEntity.ok(fileVersion);
    // }

        // Get a file version by versionId, userId, and originalFileId
        @GetMapping("/{userId}/files/{originalFileId}/versions/{versionId}")
        public ResponseEntity<FileVersion> getFileVersion(
            @PathVariable Long userId,
            @PathVariable Long originalFileId,
            @PathVariable Long versionId
        ) {
            FileVersion fileVersion = fileService.getFileVersionByIds(versionId, userId, originalFileId);
            return ResponseEntity.ok(fileVersion);
        }

// //Get original File
//     @GetMapping("/originalFile/{OriginalFileid}")
//     public ResponseEntity<OriginalFile> getFileById(@PathVariable Long OriginalFileid) {
//         OriginalFile file = fileService.getFileById(OriginalFileid);
//         return ResponseEntity.ok(file);
//     }

 // Get Original file by userId and fileId
 @GetMapping("/{userId}/files/{fileId}")
 public ResponseEntity<OriginalFile> getFileByUserIdAndFileId(@PathVariable Long userId, @PathVariable Long fileId) {
     OriginalFile file = fileService.getFileByUserIdAndFileId(userId, fileId);
     return ResponseEntity.ok(file);
 }

   

        // Get the latest version of a file
        @GetMapping("/{userId}/files/{originalFileId}/latest-version")
        public ResponseEntity<FileVersion> getLatestFileVersion(
            @PathVariable Long userId,
            @PathVariable Long originalFileId
        ) {
            FileVersion latestFileVersion = fileService.getLatestFileVersion(originalFileId, userId);
            return ResponseEntity.ok(latestFileVersion);
        }

        @GetMapping("/users")
        public ResponseEntity<List<User>> getAllUsers() {
            List<User> users = fileService.getAllUsers();
            return ResponseEntity.ok(users);
        }
    

}
