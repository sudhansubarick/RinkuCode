package com.versionontrol.controller;


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
import java.util.List;

@Controller
public class FileController {

    private final FileService fileService;

    FileController(FileService fileService) {
        this.fileService = fileService;
    }

    @GetMapping("/")
    public String home(Model model) {
        return "index"; // Render the upload form
    }

    @PostMapping("/upload/{userId}")
    public String uploadOriginalFile(@RequestParam("file") MultipartFile file, @PathVariable Long userId, Model model) throws IOException {
        OriginalFile originalFile = fileService.saveOriginalFile(file, userId);

        model.addAttribute("message", "Original file uploaded successfully: " + originalFile.getFileName());
        return "index";
    }

    @PostMapping("/upload-version/{originalFileId}/{userId}")
    public String uploadFileVersion(@RequestParam("file") MultipartFile file, @PathVariable Long originalFileId, @PathVariable Long userId, Model model) throws IOException {
        FileVersion fileVersion = fileService.saveFileVersion(file, originalFileId, userId);
        model.addAttribute("message", "File version uploaded successfully: " + fileVersion.getFileName() + " (Version " + fileVersion.getVersion() + ")");
        return "index";
    }

    //get all version file by userid and original fileid
    @GetMapping("/user/{userId}/original-file/{originalFileId}")
    public ResponseEntity<List<FileVersion>> getFileVersionsByOriginalFileIdAndUserId(
            @PathVariable Long userId,
            @PathVariable Long originalFileId) {
        List<FileVersion> fileVersions = fileService.getFileVersionsByOriginalFileIdAndUserId(originalFileId, userId);
        return ResponseEntity.ok(fileVersions);
    }

    // Get a specific file version by versionId, userId, and originalFileId
    @GetMapping("/{userId}/files/{originalFileId}/versions/{versionId}")
    public ResponseEntity<FileVersion> getFileVersion(
            @PathVariable Long userId,
            @PathVariable Long originalFileId,
            @PathVariable Long versionId
    ) {
        FileVersion fileVersion = fileService.getFileVersionByIds(versionId, userId, originalFileId);
        return ResponseEntity.ok(fileVersion);
    }

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
