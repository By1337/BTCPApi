package org.by1337.tcpapi.server.backup;

import org.by1337.tcpapi.server.logger.LogManager;
import org.by1337.tcpapi.server.logger.MarkedLogger;

import java.io.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class BackupCreator {
    public static final Logger logger = new MarkedLogger("BACKUP_CREATOR", BackupCreator.class);

    public CompletableFuture<File> createBackUp() {
        return CompletableFuture.supplyAsync(() -> {
            logger.info("creating backup...");
            File backupFolder = new File("./backups");
            if (!backupFolder.exists()) {
                backupFolder.mkdirs();
            }
            List<File> toBackup = findFilesToBackUp(new File("./"), backupFolder);

            try {
                File backupFile = createBackupFile(backupFolder);

                try (ZipOutputStream outputStream = new ZipOutputStream(new FileOutputStream(backupFile))) {
                    for (File file : toBackup) {
                        outputStream.putNextEntry(new ZipEntry(file.getPath().substring(2)));
                        try (InputStream fileInputStream = new FileInputStream(file)) {
                            outputStream.write(toByteArray(fileInputStream));
                        } catch (IOException ioException) {
                            logger.warning("failed to save file: '" + file.getPath() + "' Cause: " + ioException.getMessage());
                        }
                    }
                }

                return backupFile;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        });
    }

    private byte[] toByteArray(InputStream inputStream) throws IOException {
        final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        final byte[] buffer = new byte[0xFFFF];
        int length;
        while ((length = inputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, length);
        }
        outputStream.flush();
        return outputStream.toByteArray();
    }

    private File createBackupFile(File inFolder) throws IOException {
        Calendar calendar = Calendar.getInstance();
        File file = new File(inFolder, "Backup-" + LogManager.getDateFormat(calendar) + ".zip");
        int x = 1;
        while (file.exists()) {
            file = new File(inFolder, "Backup-" + LogManager.getDateFormat(calendar) + "(" + x + ")" + ".zip");
        }
        file.createNewFile();
        return file;
    }

    private List<File> findFilesToBackUp(File workingDir, File ignoreDir) {
        if (workingDir.equals(ignoreDir)) return Collections.emptyList();
        List<File> list = new ArrayList<>();
        if (!workingDir.isDirectory()) {
            list.add(workingDir);
        } else {
            var files = workingDir.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        list.addAll(findFilesToBackUp(file, ignoreDir));
                    } else {
                        list.add(file);
                    }
                }
            }
        }
        return list;
    }
}
