package org.by1337.tcpapi.server.util;

import org.by1337.tcpapi.server.logger.LogManager;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;

public class CrashReport {
    private final String title;
    @Nullable
    private final Throwable exception;

    public CrashReport(String title, @Nullable Throwable exception) {
        this.title = title;
        this.exception = exception;
    }
    public void saveAndPrint(){
        StringBuilder sb = new StringBuilder();
        sb.append("---- Crash Report ----\n");
        sb.append("//\n");
        sb.append("Time: ");
        sb.append((new SimpleDateFormat()).format(new Date()));
        sb.append("\n");
        sb.append("Description: ");
        sb.append(this.title);
        sb.append("\n\n");
       // sb.append("Version: ").append(Version.current.name()).append("\n");

        sb.append("Operating System: ").append(System.getProperty("os.name")).append(" (").append(System.getProperty("os.arch"))
                .append(") version ").append(System.getProperty("os.version")).append("\n");

        sb.append("Java VM Version: ").append(System.getProperty("java.vm.name"))
                .append(" (").append(System.getProperty("java.vm.info")).append("), ").append(System.getProperty("java.vm.vendor")).append("\n");

        sb.append("Memory: ").append(getMemoryInfo()).append("\n");
        sb.append("CPUs: ").append(Runtime.getRuntime().availableProcessors()).append("\n");
        sb.append("JVM Flags: ").append(getJVMFlags()).append("\n");
        sb.append("Thread: ").append(Thread.currentThread().getName()).append("\n");
        sb.append("Throwable:\n");
        if (exception != null) LogManager.writeStackTrace(exception, sb);
        else sb.append(" null");
        sb.append("\n");

        File file = new File("./crash-reports");
        if (!file.exists()) file.mkdir();

        File crashFile = new File(file, "latest.txt");

        if (crashFile.exists()) {
            String fileName = LogManager.getDateFormat(Calendar.getInstance());
            File renamedLogFile = new File(file, fileName + ".txt");
            int x = 1;
            while (renamedLogFile.exists()) {
                renamedLogFile = new File(file, fileName + "-" + x + ".txt");
                x++;
            }
            crashFile.renameTo(renamedLogFile);
        }

        try {
            crashFile.createNewFile();
            Files.writeString(crashFile.toPath(), sb.toString());
        } catch (IOException e) {
            LogManager.getLogger().log(Level.SEVERE, "failed to saveAndPrint crash report!", e);
        }
        LogManager.getLogger().severe(sb.toString());
    }

    private String getJVMFlags(){
        RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
        List<String> list  = runtimeMXBean.getInputArguments().stream().filter((s) -> s.startsWith("-X")).toList();
        return String.format("%d total; %s", list.size(), String.join(" ", list));
    }
    private String getMemoryInfo(){
        Runtime runtime = Runtime.getRuntime();
        long maxMemory = runtime.maxMemory();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long maxMb = maxMemory / 1024L / 1024L;
        long totalMb = totalMemory / 1024L / 1024L;
        long freeMb = freeMemory / 1024L / 1024L;
        return freeMemory + " bytes (" + freeMb + " MB) / " + totalMemory + " bytes (" + totalMb + " MB) up to " + maxMemory + " bytes (" + maxMb + " MB)";
    }


    public Throwable getException() {
        return exception;
    }
}
