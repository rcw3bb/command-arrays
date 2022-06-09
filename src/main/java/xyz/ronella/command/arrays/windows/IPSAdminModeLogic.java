package xyz.ronella.command.arrays.windows;

import java.util.List;

/**
 * Must hold implementation of PowerShell admin mode logic.
 *
 * @since 1.0.0
 * @author Ron Webb
 */
@FunctionalInterface
public interface IPSAdminModeLogic {

    /**
     * The method that must hold the implementation of the admin mode logic.
     * @param isAdminMode Must be true to indicate that output must be runAs compatible.
     * @param command The command that can be executed in rusAs terminal (i.e. command prompt).
     * @param args The arguments for the command.
     * @return The generated command.
     */
    String generate(boolean isAdminMode, String command, List<String> args);
}
