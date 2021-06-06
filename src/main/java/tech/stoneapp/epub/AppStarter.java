package tech.stoneapp.epub;

import tech.stoneapp.epub.cli.CommandLineApp;
import tech.stoneapp.epub.gui.GUILauncher;

public class AppStarter {
    public static void main(String[] args) {
        if (args.length == 0) {
            GUILauncher.main(args);
        } else {
            CommandLineApp.main(args);
        }
    }
}