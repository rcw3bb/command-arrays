package xyz.ronella.command.arrays.windows;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class PowerShellTest {

    @Test
    public void defaultArgs() {
        var expectedArray = new ArrayList<String>();
        expectedArray.add("powershell.exe");
        expectedArray.addAll(List.of("-NoProfile", "-InputFormat", "None", "-ExecutionPolicy", "Bypass"));

        var ps = PowerShell.getBuilder()
                .enableDefaultArgs(true)
                .build();

        assertArrayEquals(expectedArray.toArray(new String[] {}), ps.getCommand());
    }

    @Test
    public void defaultArgsWithOtherArgs() {
        var expectedArray = new LinkedList<String>();
        expectedArray.add("powershell.exe");
        expectedArray.addAll(PowerShell.PowerShellBuilder.DEFAULT_ARGS);
        expectedArray.add("Dummy");
        var ps = PowerShell.getBuilder()
                .addArg("Dummy")
                .enableDefaultArgs(true)
                .build();

        assertArrayEquals(expectedArray.toArray(new String[] {}), ps.getCommand());
    }

    @Test
    public void encodedCommand() {
        var helloWorldOutput = "Write-Output \"Hello World\"";
        var base64HelloWorld = Base64.getEncoder().encodeToString(helloWorldOutput.getBytes(StandardCharsets.UTF_16LE));

        var expectedArray = new LinkedList<String>();
        expectedArray.add("powershell.exe");
        expectedArray.addAll(PowerShell.PowerShellBuilder.DEFAULT_ARGS);
        expectedArray.addAll(List.of("-EncodedCommand",base64HelloWorld));
        var ps = PowerShell.getBuilder()
                .enableDefaultArgs(true)
                .setEncodedCommand(helloWorldOutput)
                .build();

        assertArrayEquals(expectedArray.toArray(new String[] {}), ps.getCommand());
    }

    @Test
    public void multipleEncodedCommand() {
        var helloWorldOutput = "Write-Output \"Hello World\"";
        var base64HelloWorld = Base64.getEncoder().encodeToString(helloWorldOutput.getBytes(StandardCharsets.UTF_16LE));

        var expectedArray = new LinkedList<String>();
        expectedArray.add("powershell.exe");
        expectedArray.addAll(PowerShell.PowerShellBuilder.DEFAULT_ARGS);
        expectedArray.addAll(List.of("-EncodedCommand",base64HelloWorld));
        var ps = PowerShell.getBuilder()
                .enableDefaultArgs(true)
                .setEncodedCommand(helloWorldOutput)
                .setEncodedCommand(helloWorldOutput)
                .build();

        assertArrayEquals(expectedArray.toArray(new String[] {}), ps.getCommand());
    }

    @Test
    public void adminModeWhere() {
        var expected = "powershell.exe -NoProfile -InputFormat None -ExecutionPolicy Bypass Exit (Start-Process \"Where\" -Wait -PassThru -Verb RunAs -argumentlist \"\"\"Where\"\"\").ExitCode";

        var ps = PowerShell.getBuilder()
                .enableDefaultArgs(true)
                .setCommand("Where")
                .addArg("Where")
                .setAdminMode(true)
                .build();

        assertEquals(expected, String.join(" ", ps.getCommand()));
    }

    @Test
    public void nonAdminModeWriteOutput() {
        var expected = "powershell.exe -NoProfile -InputFormat None -ExecutionPolicy Bypass -Command \"& {\"Write-Output\" \"\"\"$Env:windir\"\"\"}\"";

        var ps = PowerShell.getBuilder()
                .enableDefaultArgs(true)
                .setCommand("Write-Output")
                .addArg("$Env:windir")
                .setAdminMode(false)
                .build();

        assertEquals(expected, String.join(" ", ps.getCommand()));
    }

    @Test
    public void justArgs() {
        var expected = "powershell.exe -NoProfile -InputFormat None -ExecutionPolicy Bypass -Command \"& {Write-Output \"\"\"Hello World\"\"\"}\"";

        var ps = PowerShell.getBuilder()
                .enableDefaultArgs(true)
                .addArgs(List.of("-Command", "\"& {Write-Output \"\"\"Hello World\"\"\"}\""))
                .build();

        assertEquals(expected, String.join(" ", ps.getCommand()));
    }

    @Test
    public void argsWithZArgs() {
        var expected = "powershell.exe -NoProfile -InputFormat None -ExecutionPolicy Bypass -Command \"& {Write-Output \"\"\"Hello World\"\"\"}\"";

        var ps = PowerShell.getBuilder()
                .enableDefaultArgs(true)
                .addArg("-Command")
                .addZArg("\"& {Write-Output \"\"\"Hello World\"\"\"}\"")
                .build();

        assertEquals(expected, String.join(" ", ps.getCommand()));
    }

    @Test
    public void justZArgs() {
        var expected = "powershell.exe -NoProfile -InputFormat None -ExecutionPolicy Bypass -Command \"& {Write-Output \"\"\"Hello World\"\"\"}\"";

        var ps = PowerShell.getBuilder()
                .enableDefaultArgs(true)
                .addZArgs(List.of("-Command", "\"& {Write-Output \"\"\"Hello World\"\"\"}\""))
                .build();

        assertEquals(expected, String.join(" ", ps.getCommand()));
    }

    @Test
    public void adminModePreferNonAdminNonElevated() {
        var expected = "powershell.exe -NoProfile -InputFormat None -ExecutionPolicy Bypass Exit (Start-Process \"Where\" -Wait -PassThru -Verb RunAs -argumentlist \"\"\"Where\"\"\").ExitCode";

        var ps = PowerShell.getBuilder()
                .enableDefaultArgs(true)
                .setCommand("Where")
                .addArg("Where")
                .setAdminMode(true)
                .setPreferNonAdminMode(true)
                .build();

        assertEquals(expected, String.join(" ", ps.getCommand()));
    }

    @Test
    public void adminModePreferNonAdminElevated() {
        var expected = "powershell.exe -NoProfile -InputFormat None -ExecutionPolicy Bypass -Command \"& {\"Where\" \"\"\"Where\"\"\"}\"";

        try (var checker = Mockito.mockStatic(RunAsChecker.class)) {
            checker.when(RunAsChecker::isElevatedMode).thenReturn(true);

            var ps = PowerShell.getBuilder()
                    .enableDefaultArgs(true)
                    .setCommand("Where")
                    .addArg("Where")
                    .setAdminMode(true)
                    .setPreferNonAdminMode(true)
                    .build();

            assertEquals(expected, String.join(" ", ps.getCommand()));
        }
    }

    @Test
    public void commandOnly() {
        var expected = "powershell.exe -Command \"& {\"Where\" }\"";

        try (var checker = Mockito.mockStatic(RunAsChecker.class)) {
            checker.when(RunAsChecker::isElevatedMode).thenReturn(true);

            var ps = PowerShell.getBuilder()
                    .setCommand("Where")
                    .build();

            assertEquals(expected, String.join(" ", ps.getCommand()));
        }
    }

    @Test
    public void changeAdminLogic() {
        var expected = "powershell.exe Command: Where Args: Arg1,Arg2";

        try (var checker = Mockito.mockStatic(RunAsChecker.class)) {
            checker.when(RunAsChecker::isElevatedMode).thenReturn(true);

            var ps = PowerShell.getBuilder()
                    .setCommand("Where")
                    .addArgs(List.of("Arg1", "Arg2"))
                    .setAdminMode(true)
                    .setAdminLogic((___command, ___args) -> String.format("Command: %s Args: %s", ___command, String.join(",", ___args)))
                    .build();

            assertEquals(expected, String.join(" ", ps.getCommand()));
        }
    }

    @Test
    public void changeCommandLogic() {
        var expected = "powershell.exe Command: Where Args: Arg1,Arg2";

        try (var checker = Mockito.mockStatic(RunAsChecker.class)) {
            checker.when(RunAsChecker::isElevatedMode).thenReturn(true);

            var ps = PowerShell.getBuilder()
                    .setCommand("Where")
                    .addArgs(List.of("Arg1", "Arg2"))
                    .setCommandLogic((___command, ___args) -> String.format("Command: %s Args: %s", ___command, String.join(",", ___args)))
                    .build();

            assertEquals(expected, String.join(" ", ps.getCommand()));
        }
    }
}
