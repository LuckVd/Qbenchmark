package com.webapp.core.controller.facade;

import com.webapp.core.controller.handler.FileHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * File Controller - File access and management APIs
 */
@RestController
@RequestMapping("/api/v1")
public class FileController {

    private static final Logger logger = LoggerFactory.getLogger(FileController.class);

    @Autowired
    private FileHandler fileHandler;

    @GetMapping("/file/read")
    public String readFile(@RequestParam("path") String path) {
        logger.info("Reading file: {}", path);
        return fileHandler.handleRead(path);
    }

    @GetMapping("/file/image")
    public String readImage(@RequestParam("file") String file) {
        return fileHandler.handleImage(file);
    }

    @GetMapping("/file/log")
    public String readLog(@RequestParam("file") String file) {
        return fileHandler.handleLog(file);
    }

    @GetMapping("/file/absolute")
    public String readAbsolute(@RequestParam("path") String path) {
        return fileHandler.handleAbsolute(path);
    }

    @GetMapping("/file/encoded")
    public String readEncoded(@RequestParam("file") String file) {
        return fileHandler.handleEncoded(file);
    }

    @GetMapping("/path/info")
    public String pathInfo(@RequestParam("path") String path) {
        return fileHandler.handlePathInfo(path);
    }
}
