package com.testgenerator.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class ZipHandlerService {

    /**
     * Descomprime un archivo ZIP y retorna un mapa con el contenido de los archivos
     */
    public Map<String, String> unzipFile(MultipartFile zipFile) throws IOException {
        Map<String, String> files = new HashMap<>();

        try (ZipArchiveInputStream zais = new ZipArchiveInputStream(zipFile.getInputStream())) {
            ZipArchiveEntry entry;

            while ((entry = zais.getNextZipEntry()) != null) {
                if (!entry.isDirectory()) {
                    log.debug("Procesando archivo: {}", entry.getName());

                    if (isJavaFile(entry.getName())) {
                        String content = readEntry(zais);
                        files.put(entry.getName(), content);
                    }
                }
            }
        }

        log.info("Total de archivos Java encontrados: {}", files.size());
        return files;
    }

    /**
     * Crea un ZIP con el contenido proporcionado
     */
    public byte[] createZip(Map<String, String> files) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try (ZipArchiveOutputStream zaos = new ZipArchiveOutputStream(baos)) {
            for (Map.Entry<String, String> entry : files.entrySet()) {
                ZipArchiveEntry zipEntry = new ZipArchiveEntry(entry.getKey());
                zaos.putArchiveEntry(zipEntry);
                zaos.write(entry.getValue().getBytes());
                zaos.closeArchiveEntry();
            }
        }

        return baos.toByteArray();
    }

    private String readEntry(ZipArchiveInputStream zais) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len;

        while ((len = zais.read(buffer)) > 0) {
            baos.write(buffer, 0, len);
        }

        return baos.toString("UTF-8");
    }

    private boolean isJavaFile(String filename) {
        return filename.endsWith(".java") && !filename.contains("Test.java");
    }
}
