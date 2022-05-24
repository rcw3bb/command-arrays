package xyz.ronella.command.arrays.windows;

import xyz.ronella.trivial.decorator.ListAdder;
import xyz.ronella.trivial.handy.ICommandArray;
import xyz.ronella.trivial.handy.impl.CommandArray;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.BiFunction;

/**
 * PowerShell implemenation of ICommandArray.
 *
 * @author Ron Webb
 * @since 1.0.0
 */
public final class PowerShell implements ICommandArray {

    /**
     * The program to use for this implementation.
     */
    public static final String PROGRAM = "powershell.exe";

    private final ICommandArray array;

    private PowerShell(final PowerShellBuilder builder) {
        this.array = CommandArray.getBuilder()
                .setProgram(PROGRAM)
                .addArgs(builder.args)
                .addZArgs(builder.zArgs)
                .build();
    }

    /**
     * Generates the powershell command in arrays.
     * @return The command in array.
     */
    @Override
    public String[] getCommand() {
        return array.getCommand();
    }

    /**
     * Access the builder of the PowerShell command array.
     * @return An instance of PowerShellBuilder.
     */
    public static PowerShellBuilder getBuilder() {
        return new PowerShellBuilder();
    }

    /**
     * The only class the can create an intance of PowerShell command array.
     */
    public final static class PowerShellBuilder {

        /**
         * The default arguments to be added to the powershell command when it is enabled.
         */
        public static final List<String> DEFAULT_ARGS = List.of("-NoProfile", "-InputFormat", "None", "-ExecutionPolicy", "Bypass");

        private String command;
        private final List<String> args;
        private final List<String> zArgs;
        private final List<String> inputArgs;
        private List<String> encodedCommand;
        private boolean hasDefaultArgs;
        private boolean isAdminMode;
        private boolean prefNonAdminMode;
        private BiFunction<String, List<String>, String> adminLogic;
        private BiFunction<String, List<String>, String> commandLogic;

        private PowerShellBuilder() {
            args = new ArrayList<>();
            zArgs = new ArrayList<>();
            inputArgs = new ArrayList<>();
        }

        private String quote(final String text) {
            return String.format("\"%s\"", text);
        }

        private String tripleQuote(final String text) {
            return String.format("\"\"\"%s\"\"\"", text);
        }

        private boolean determineAdminMode() {
            if (isAdminMode) {
                if (prefNonAdminMode && RunAsChecker.isElevatedMode()) {
                    return false;
                }
                else {
                    return isAdminMode;
                }
            }
            return false;
        }

        private StringBuilder inputArgsToStringBuilder(final List<String> args) {
            final var sbArgs = new StringBuilder();
            args.forEach(___arg -> sbArgs.append(sbArgs.length()>0 ? ",": "").append(tripleQuote(___arg)));
            return sbArgs;
        }

        private void adminModeLogic(final ListAdder<String> addrArgs) {
            addrArgs.add(() -> {
                final var logic = Optional.ofNullable(this.adminLogic);
                return logic.orElseGet(()-> (___command, ___args)-> {
                    final var sbArgs = inputArgsToStringBuilder(___args);
                    return String.format("Exit (Start-Process %s -Wait -PassThru -Verb RunAs%s%s).ExitCode",
                            quote(___command), sbArgs.length() == 0 ? "" : " -argumentlist ", sbArgs);
                }).apply(command, inputArgs);
            });
        }

        private void prepareArgs() {
            final var addrArgs = new ListAdder<>(args);
            addrArgs.addAll(()-> hasDefaultArgs, DEFAULT_ARGS);

            final var addrInputArgs = new ListAdder<>(inputArgs);
            addrInputArgs.addAll(()-> null!=encodedCommand, encodedCommand);

            if (determineAdminMode()) {
                adminModeLogic(addrArgs);
            }
            else {
                if (null != command) {
                    addrArgs.add(() -> {
                        final var logic = Optional.ofNullable(this.commandLogic);
                        return logic.orElseGet(()-> (___command, ___args)-> {
                            final var sbArgs = inputArgsToStringBuilder(___args);
                            return String.format("-Command \"& {%s %s}\"", quote(command), sbArgs);
                        }).apply(command, inputArgs);
                    });
                } else {
                    addrArgs.addAll(inputArgs);
                }
            }
        }

        /**
         * Create an instance of PowerShell command array.
         * @return An instance of PowerShell command array.
         */
        public PowerShell build() {
            prepareArgs();
            return new PowerShell(this);
        }

        /**
         * Set the command to use with powershell.
         * @param command The command to use with powershell.
         * @return An instance of PowerShellBuilder.
         */
        public PowerShellBuilder setCommand(final String command) {
            this.command = command;
            return this;
        }

        /**
         * Enable the default arguments for powershell. Don't enable this if you want the
         * PowerShellBuilder.DEFAULT_ARGS to be added to the powershell script.
         * You can just use the normal addArg/addArgs method.
         * @param enable Set this to true add the PowerShellBuilder.DEFAULT_ARGS arguments.
         * @return An instance of PowerShellBuilder.
         */
        public PowerShellBuilder enableDefaultArgs(final boolean enable) {
            this.hasDefaultArgs = enable;
            return this;
        }

        /**
         * Use this to add normal arguments to powershell.
         * @param args The arguments to be added.
         * @return An instance of PowerShellBuilder.
         */
        public PowerShellBuilder addArgs(final Collection<String> args) {
            this.inputArgs.addAll(args);
            return this;
        }

        /**
         * Use this to add normal argument to powershell.
         * @param arg The single argument to be added.
         * @return An instance of PowerShellBuilder.
         */
        public PowerShellBuilder addArg(final String arg) {
            this.inputArgs.add(arg);
            return this;
        }

        /**
         * Use this to add arguments after the normal arguments.
         * @param zArgs The arguments to be added.
         * @return An instance of PowerShellBuilder.
         */
        public PowerShellBuilder addZArgs(final Collection<String> zArgs) {
            this.zArgs.addAll(zArgs);
            return this;
        }

        /**
         * Use this to add an argument after the normal arguments.
         * @param zArg The argument to be added.
         * @return An instance of PowerShellBuilder.
         */
        public PowerShellBuilder addZArg(final String zArg) {
            this.zArgs.add(zArg);
            return this;
        }

        /**
         * Add the command as encoded for encoded command paramter.
         * @param command The command to feed on -EncodedCommand parameter.
         * @return An instance of PowerShellBuilder.
         */
        public PowerShellBuilder setEncodedCommand(final String command) {
            final var base64Command = Base64.getEncoder().encodeToString(command.getBytes(StandardCharsets.UTF_16LE));
            if (determineAdminMode()) {
                this.command = PROGRAM;
            }
            encodedCommand = List.of("-EncodedCommand", base64Command);
            return this;
        }

        /**
         * Generate the command to be executed in elevated mode (i.e. RunAs).
         * @param isAdminMode Set to true to run comman in elevated mode.
         * @return An instance of PowerShellBuilder.
         */
        public PowerShellBuilder setAdminMode(final boolean isAdminMode) {
            this.isAdminMode = isAdminMode;
            return this;
        }

        /**
         * Don't generate the command for elevated mode (i.e. RunAs) if the process is already in elevated mode.
         * @param prefNonAdminMode Set to true to not generate in command for elevated mode if it is already running in
         *                           elevated mode.
         * @return An instance of PowerShellBuilder.
         */
        public PowerShellBuilder setPreferNonAdminMode(final boolean prefNonAdminMode) {
            this.prefNonAdminMode = prefNonAdminMode;
            return this;
        }

        /**
         * Overrides the generation of command for elevated mode (i.e. RunAs). The default is the following:
         *
         * {@code (___command, ___args)-> {
         *     var sbArgs = inputArgsToStringBuilder(___args);
         *     return String.format("Exit (Start-Process %s -Wait -PassThru -Verb RunAs%s%s).ExitCode",
         *             quote(___command), (sbArgs.length() == 0 ? "" : " -argumentlist "), sbArgs);
         * }}
         *
         * @param adminLogic Must hold the override logic.
         * @return An instance of PowerShellBuilder.
         */
        public PowerShellBuilder setAdminLogic(final BiFunction<String, List<String>, String> adminLogic) {
            this.adminLogic = adminLogic;
            return this;
        }

        /**
         * Overrides the generation of command. The default is the following:
         *
         * {@code (___command, ___args)-> {
         *         var sbArgs = inputArgsToStringBuilder(___args);
         *         return String.format("-Command \"& {%s %s}\"", quote(command), sbArgs);
         *     }).apply(command, inputArgs);
         * }}
         *
         * @param commandLogic Must hold the override logic.
         * @return An instance of PowerShellBuilder.
         */
        public PowerShellBuilder setCommandLogic(final BiFunction<String, List<String>, String> commandLogic) {
            this.commandLogic = commandLogic;
            return this;
        }
    }
}
