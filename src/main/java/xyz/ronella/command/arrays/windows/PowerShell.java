package xyz.ronella.command.arrays.windows;

import xyz.ronella.command.arrays.windows.internal.RunAsChecker;
import xyz.ronella.trivial.decorator.ListAdder;
import xyz.ronella.trivial.decorator.StringBuilderAppender;
import xyz.ronella.trivial.handy.ICommandArray;
import xyz.ronella.trivial.handy.impl.CommandArray;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.BooleanSupplier;
import java.util.stream.Collectors;

/**
 * PowerShell implementation of ICommandArray.
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
        final var arrayBuilder = CommandArray.getBuilder()
                .addArgs(builder.args);

        if (!builder.stopProgramName) {
            arrayBuilder.setProgram(PROGRAM);
        }

        this.array = arrayBuilder.build();
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

        private static final String REGEX_RAW_PREFIX = "^[\"'&{].*";
        private static final String REGEX_LITERAL = "^[Ll][Ii][Tt][Ee][Rr][Aa][Ll]:";
        private static final String LITERAL_MATCHER = String.format("%s(.*)", REGEX_LITERAL);
        private String command;
        private final List<String> progArgs;
        private final List<String> args;
        private final List<String> inputArgs;
        private final List<String> inputZArgs;
        private final List<String> adminModeHeader;
        private final List<String> encodedArgs;
        private boolean hasDefaultArgs;
        private Boolean isAdminMode;
        private boolean prefNonAdminMode;
        private boolean stopProgramName;
        private boolean isRawArgs;
        private BiFunction<String, List<String>, String> adminLogic;

        private PowerShellBuilder() {
            progArgs = new ArrayList<>();
            args = new ArrayList<>();
            inputArgs = new ArrayList<>();
            inputZArgs = new ArrayList<>();
            adminModeHeader = new ArrayList<>();
            encodedArgs = new ArrayList<>();
        }

        private String condQuote(final String text) {
            return condQuote(text, false);
        }

        private String condQuote(final String text, final boolean forceQuote) {
            var output = quote(text);

            if (text.matches(LITERAL_MATCHER)) {
                final var cleanedText = text.replaceFirst(REGEX_LITERAL, "");
                output = forceQuote ? quote(cleanedText) : cleanedText;
            }

            return output;
        }

        private String quote(final String text) {
            return String.format("\"%s\"", text);
        }

        private String condTripleQuote(final String text) {
            return argToSingleLine(text).matches(LITERAL_MATCHER)
                    ? text.replaceFirst(REGEX_LITERAL, "")
                    : tripleQuote(text);
        }

        private String tripleQuote(final String text) {
            return String.format("\"\"\"%s\"\"\"", text);
        }

        private boolean determineAdminMode() {
            boolean output=false;
            if (null!=isAdminMode && isAdminMode && !(prefNonAdminMode && RunAsChecker.isElevatedMode())) {
                output = isAdminMode;
            }
            return output;
        }

        private StringBuilder inputArgsToStringBuilder(final List<String> args, final String delimiter) {
            final var sbArgs = new StringBuilder();
            final var appenderArgs = new StringBuilderAppender(sbArgs);
            args.forEach(___arg -> {
                appenderArgs.append(() -> sbArgs.length() > 0, delimiter);
                if (rawArgQualifier(___arg)) {
                    appenderArgs.append(___arg);
                } else {
                    appenderArgs.append(condTripleQuote(___arg));
                }
            });
            return sbArgs;
        }

        private void adminModeLogic(final ListAdder<String> addrArgs, final boolean isAdmin) {
            addrArgs.add(() -> {
                final var logic = Optional.ofNullable(this.adminLogic);
                final var internalCommand =  Optional.ofNullable(command).orElse(PROGRAM);
                return logic.orElseGet(()-> (___command, ___args)-> {
                    final var sbArgs = inputArgsToStringBuilder(___args, ",");
                    final var adminCommand = String.format("Exit (Start-Process %s -Wait -PassThru%s%s%s).ExitCode",
                            condQuote(___command, true), isAdmin ? " -Verb RunAs": "", sbArgs.length() == 0 ? "" : " -argumentlist ", sbArgs);
                    final var argsAdder = new ListAdder<>(args);
                    argsAdder.addAll(()-> PROGRAM.equals(___command.toLowerCase(Locale.ROOT)), List.of("-WindowStyle","Hidden"));
                    argsAdder.add("-EncodedCommand");
                    final var sbAdminCommand = new StringBuilder();
                    adminModeHeader.stream().map(___header -> ___header + "\n").forEach(sbAdminCommand::append);
                    sbAdminCommand.append(adminCommand);
                    return encodeCommand(sbAdminCommand.toString());
                }).apply(internalCommand, getAllInputArgs());
            });
        }

        private List<String> getAllInputArgs() {
            final var allInputs = new ArrayList<String>();
            final var adder = new ListAdder<>(allInputs);
            adder.addAll(()-> !inputArgs.isEmpty(), inputArgs);
            adder.addAll(()-> !inputZArgs.isEmpty(), inputZArgs);

            return allInputs;
        }

        private String argToSingleLine(final String arg) {
            return arg.replaceAll("[\n\r]", " ");
        }

        private boolean rawRegexQualifier(final String arg) {
            final var singeLineArg = argToSingleLine(arg);
            return singeLineArg.matches(REGEX_RAW_PREFIX);
        }

        private boolean rawArgQualifier(final String arg) {
            return isRawArgs || encodedArgs.contains(arg) || rawRegexQualifier(arg);
        }

        private void prepareArgs() {
            final var addrArgs = new ListAdder<>(args);
            addrArgs.addAll(()-> hasDefaultArgs, DEFAULT_ARGS);
            addrArgs.addAll(()-> !progArgs.isEmpty(), progArgs);

            if (null != isAdminMode) {
                adminModeLogic(addrArgs, determineAdminMode());
            }
            else {
                final var allInputs = getAllInputArgs();
                addrArgs.add(()-> command!=null, ()-> command.matches(REGEX_RAW_PREFIX) ? command : condQuote(command));
                addrArgs.addAll(()-> !allInputs.isEmpty(), allInputs.stream()
                        .map(___arg -> rawArgQualifier(___arg) ? ___arg : condTripleQuote(___arg))
                        .collect(Collectors.toList()));
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
         * Set the command to use with powershell.
         * @param when Only apply the method when this returns true.
         * @param command The command to use with powershell.
         * @return An instance of PowerShellBuilder.
         */
        public PowerShellBuilder setCommand(final BooleanSupplier when, final String command) {
            if (when.getAsBoolean()) {
                this.command = command;
            }
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
         * Use this to add normal arguments to powershell.
         * @param when Only apply the method when this returns true.
         * @param args The arguments to be added.
         * @return An instance of PowerShellBuilder.
         */
        public PowerShellBuilder addArgs(final BooleanSupplier when, final Collection<String> args) {
            if (when.getAsBoolean()) {
                this.inputArgs.addAll(args);
            }
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
         * Use this to add header command for adminModeLogic.
         *
         * @param header The single header command to be added.
         * @return An instance of PowerShellBuilder.
         */
        public PowerShellBuilder addAdminHeader(final String header) {
            this.adminModeHeader.add(header);
            return this;
        }

        /**
         * Use this to add header command for adminModeLogic.
         * @param when Only apply the method when this returns true.
         * @param header The single header command to be added.
         * @return An instance of PowerShellBuilder.
         */
        public PowerShellBuilder addAdminHeader(final BooleanSupplier when, final String header) {
            if (when.getAsBoolean()) {
                this.adminModeHeader.add(header);
            }
            return this;
        }

        /**
         * Use this to add header commands for adminModeLogic.
         *
         * @param headers The header commands to be added.
         * @return An instance of PowerShellBuilder.
         */
        public PowerShellBuilder addAdminHeader(final Collection<String> headers) {
            this.adminModeHeader.addAll(headers);
            return this;
        }

        /**
         * Use this to add header commands for adminModeLogic.
         * @param when Only apply the method when this returns true.
         * @param headers The header commands to be added.
         * @return An instance of PowerShellBuilder.
         */
        public PowerShellBuilder addAdminHeader(final BooleanSupplier when, final Collection<String> headers) {
            if (when.getAsBoolean()) {
                this.adminModeHeader.addAll(headers);
            }
            return this;
        }

        /**
         * Use this to add normal arguments to powershell program itself.
         * @param args The arguments to be added.
         * @return An instance of PowerShellBuilder.
         */
        public PowerShellBuilder addPArgs(final List<String> args) {
            this.progArgs.addAll(args);
            return this;
        }

        /**
         * Use this to add normal arguments to powershell program itself.
         * @param when Only apply the method when this returns true.
         * @param args The arguments to be added.
         * @return An instance of PowerShellBuilder.
         */
        public PowerShellBuilder addPArgs(final BooleanSupplier when, final List<String> args) {
            if (when.getAsBoolean()) {
                this.progArgs.addAll(args);
            }
            return this;
        }

        /**
         * Use this to add normal argument to powershell program itself.
         * @param arg The single argument to be added.
         * @return An instance of PowerShellBuilder.
         */
        public PowerShellBuilder addPArg(final String arg) {
            this.progArgs.add(arg);
            return this;
        }

        /**
         * Use this to add normal argument to powershell program itself.
         * @param when Only apply the method when this returns true.
         * @param arg The single argument to be added.
         * @return An instance of PowerShellBuilder.
         */
        public PowerShellBuilder addPArg(final BooleanSupplier when, final String arg) {
            if (when.getAsBoolean()) {
                this.progArgs.add(arg);
            }
            return this;
        }

        private String encodeCommand(final String command) {
            return Base64.getEncoder().encodeToString(command.getBytes(StandardCharsets.UTF_16LE));
        }

        /**
         * Use this to add normal encoded argument to powershell.
         * This must be an argument to -EncodedCommand parameter.
         * @param arg The single argument to be added.
         * @return An instance of PowerShellBuilder.
         */
        public PowerShellBuilder addEncodedArg(final String arg) {
            final var encodedArg = encodeCommand(arg);
            this.inputArgs.add(encodedArg);
            this.encodedArgs.add(encodedArg);
            return this;
        }

        /**
         * Use this to add normal encoded argument to powershell.
         * This must be an argument to -EncodedCommand parameter.
         * @param when Only apply the method when this returns true.
         * @param arg The single argument to be added.
         * @return An instance of PowerShellBuilder.
         */
        public PowerShellBuilder addEncodedArg(final BooleanSupplier when, final String arg) {
            if (when.getAsBoolean()) {
                addEncodedArg(arg);
            }
            return this;
        }

        /**
         * Use this to add normal argument to powershell.
         * @param when Only apply the method when this returns true.
         * @param arg The single argument to be added.
         * @return An instance of PowerShellBuilder.
         */
        public PowerShellBuilder addArg(final BooleanSupplier when, final String arg) {
            if (when.getAsBoolean()) {
                this.inputArgs.add(arg);
            }
            return this;
        }

        /**
         * Use this to add arguments after the normal arguments.
         * @param zArgs The arguments to be added.
         * @return An instance of PowerShellBuilder.
         */
        public PowerShellBuilder addZArgs(final Collection<String> zArgs) {
            this.inputZArgs.addAll(zArgs);
            return this;
        }

        /**
         * Use this to add arguments after the normal arguments.
         * @param when Only apply the method when this returns true.
         * @param zArgs The arguments to be added.
         * @return An instance of PowerShellBuilder.
         */
        public PowerShellBuilder addZArgs(final BooleanSupplier when, final Collection<String> zArgs) {
            if (when.getAsBoolean()) {
                this.inputZArgs.addAll(zArgs);
            }
            return this;
        }

        /**
         * Use this to add an argument after the normal arguments.
         * @param zArg The argument to be added.
         * @return An instance of PowerShellBuilder.
         */
        public PowerShellBuilder addZArg(final String zArg) {
            this.inputZArgs.add(zArg);
            return this;
        }

        /**
         * Use this to add an argument after the normal arguments.
         * @param when Only apply the method when this returns true.
         * @param zArg The argument to be added.
         * @return An instance of PowerShellBuilder.
         */
        public PowerShellBuilder addZArg(final BooleanSupplier when, final String zArg) {
            if (when.getAsBoolean()) {
                this.inputZArgs.add(zArg);
            }
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
         * Overrides the generation of command when using in admin mode.
         *
         * @param adminLogic Must hold the override logic.
         * @return An instance of PowerShellBuilder.
         */
        public PowerShellBuilder setAdminLogic(final BiFunction<String, List<String>, String> adminLogic) {
            this.adminLogic = adminLogic;
            return this;
        }

        /**
         * Stop the program name from being included when set to true.
         * @param suppress Set to true to stop the program name from being included in the generated command array.
         * @return An instance of PowerShellBuilder.
         */
        public PowerShellBuilder suppressProgramName(final boolean suppress) {
            this.stopProgramName = suppress;
            return this;
        }

        /**
         * Don't do any reprocessing of arguments when set to true.
         * An example of reprocessing is adding triple quotes when preparing the admin mode command.
         *
         * @param raw Set to true to avoid argument reprocessing.
         * @return An instance of PowerShellBuilder.
         */
        public PowerShellBuilder setRawArgs(final boolean raw) {
            this.isRawArgs = raw;
            return this;
        }
    }
}
