package tech.stoneapp.epub.util;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

/*
 * Modified from https://stackoverflow.com/a/18004334/9039813
 */

public class DesktopAPI {
    public DesktopAPI() {}

    public static boolean showInFolder(File file) {
        if (!file.exists()) return false;
        String path = file.getAbsolutePath();

        OS osPlatform = getOS();
        String command = "";
        switch (osPlatform) {
            case windows:
                command = String.format("explorer.exe /select,\"%s\"", path);
                break;
            case macos:
                command = String.format("open -R \"%s\"", path);
                break;
            case linux:
                // use Desktop.getDesktop() to handle files on linux, for there are too many cases on linux.
                try {
                    Desktop.getDesktop().browse(file.toURI());
                    return true;
                } catch (IOException e) {
                    return false;
                }
            default:
                return false;
        }
        // only command for Windows and MacOS
        return runCommand(command);
    }

    private static boolean runCommand(String command) {
        Process proc;
        try {
            proc = Runtime.getRuntime().exec(command);
            return proc.waitFor(2, TimeUnit.SECONDS);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static OS getOS() {
        final String os = System.getProperty("os.name").toLowerCase();

        if (os.contains("win")) return OS.windows;
        if (os.contains("mac")) return OS.macos;
        if (os.contains("linux") || os.contains("unix")) return OS.linux;

        return OS.unknown;
    }
}