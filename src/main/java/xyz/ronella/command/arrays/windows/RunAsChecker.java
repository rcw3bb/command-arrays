package xyz.ronella.command.arrays.windows;

import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.nio.file.Paths;

/**
 * The class that checks if the service can write in SystemRoot.
 *
 * @author Ron Webb
 * @since 1.0.0
 */
public final class RunAsChecker {

    private RunAsChecker() {
    }

    /**
     * Check if running in elevated mode.
     *
     * @return True when running in elevated mode.
     */
    public static boolean isElevatedMode() {
        final String pid = ManagementFactory.getRuntimeMXBean().getName().replace("@", "-");
        final String fileName = String.format("runas-checker-%s.dummy", pid);
        final File file = Paths.get(System.getenv("SystemRoot"), fileName).toFile();
        boolean output = false;
        try {
            if (file.createNewFile()) {
                file.delete();
                output = true;
            }
        } catch (IOException e) {
            if ("Access is denied".equalsIgnoreCase(e.getMessage())) {
                output = false;
            } else {
                e.printStackTrace(System.err);
            }
        }
        return output;
    }
}
