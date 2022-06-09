package xyz.ronella.command.arrays.windows;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import xyz.ronella.command.arrays.windows.internal.RunAsChecker;

import java.util.*;

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
        expectedArray.addAll(List.of("-NoProfile", "-InputFormat", "None", "-ExecutionPolicy", "Bypass"));
        expectedArray.add("\"\"\"Dummy\"\"\"");
        var ps = PowerShell.getBuilder()
                .addArg("Dummy")
                .enableDefaultArgs(true)
                .build();

        assertArrayEquals(expectedArray.toArray(new String[] {}), ps.getCommand());
    }

    @Test
    public void adminModeWhere() {
        var expected = "powershell.exe -NoProfile -InputFormat None -ExecutionPolicy Bypass -EncodedCommand RQB4AGkAdAAgACgAUwB0AGEAcgB0AC0AUAByAG8AYwBlAHMAcwAgACIAVwBoAGUAcgBlACIAIAAtAFcAYQBpAHQAIAAtAFAAYQBzAHMAVABoAHIAdQAgAC0AVgBlAHIAYgAgAFIAdQBuAEEAcwAgAC0AYQByAGcAdQBtAGUAbgB0AGwAaQBzAHQAIAAiACIAIgBXAGgAZQByAGUAIgAiACIAKQAuAEUAeABpAHQAQwBvAGQAZQA=";

        var ps = PowerShell.getBuilder()
                .enableDefaultArgs(true)
                .setCommand("Where")
                .addArg("Where")
                .setAdminMode(true)
                .build();

        assertEquals(expected, String.join(" ", ps.getCommand()));
    }

    @Test
    public void adminModeWhereHeader() {
        var expected = "powershell.exe -NoProfile -InputFormat None -ExecutionPolicy Bypass -EncodedCommand JABQAHIAbwBnAHIAZQBzAHMAUAByAGUAZgBlAHIAZQBuAGMAZQAgAD0AIAAnAFMAaQBsAGUAbgB0AGwAeQBDAG8AbgB0AGkAbgB1AGUAJwAKAEUAeABpAHQAIAAoAFMAdABhAHIAdAAtAFAAcgBvAGMAZQBzAHMAIAAiACcALQBDAG8AbQBtAGEAbgBkACcAIgAgAC0AVwBhAGkAdAAgAC0AUABhAHMAcwBUAGgAcgB1ACAALQBWAGUAcgBiACAAUgB1AG4AQQBzACAALQBhAHIAZwB1AG0AZQBuAHQAbABpAHMAdAAgACIAVwByAGkAdABlAC0ATwB1AHQAcAB1AHQAIgAsACIAIgAiAEgAZQBsAGwAbwAgAHcAbwByAGwAZAAiACIAIgApAC4ARQB4AGkAdABDAG8AZABlAA==";

        var ps = PowerShell.getBuilder()
                .enableDefaultArgs(true)
                .setCommand("'-Command'")
                .addArgs(List.of("\"Write-Output\"", "Hello world"))
                .setAdminMode(true)
                .addAdminModeHeader("$ProgressPreference = 'SilentlyContinue'")
                .build();

        assertEquals(expected, String.join(" ", ps.getCommand()));
    }

    @Test
    public void adminModeWhereHeaderWhenTrue() {
        var expected = "powershell.exe -NoProfile -InputFormat None -ExecutionPolicy Bypass -EncodedCommand JABQAHIAbwBnAHIAZQBzAHMAUAByAGUAZgBlAHIAZQBuAGMAZQAgAD0AIAAnAFMAaQBsAGUAbgB0AGwAeQBDAG8AbgB0AGkAbgB1AGUAJwAKAEUAeABpAHQAIAAoAFMAdABhAHIAdAAtAFAAcgBvAGMAZQBzAHMAIAAiACcALQBDAG8AbQBtAGEAbgBkACcAIgAgAC0AVwBhAGkAdAAgAC0AUABhAHMAcwBUAGgAcgB1ACAALQBWAGUAcgBiACAAUgB1AG4AQQBzACAALQBhAHIAZwB1AG0AZQBuAHQAbABpAHMAdAAgACIAVwByAGkAdABlAC0ATwB1AHQAcAB1AHQAIgAsACIAIgAiAEgAZQBsAGwAbwAgAHcAbwByAGwAZAAiACIAIgApAC4ARQB4AGkAdABDAG8AZABlAA==";

        var ps = PowerShell.getBuilder()
                .enableDefaultArgs(true)
                .setCommand("'-Command'")
                .addArgs(List.of("\"Write-Output\"", "Hello world"))
                .setAdminMode(true)
                .addAdminModeHeader(()-> true, "$ProgressPreference = 'SilentlyContinue'")
                .build();

        assertEquals(expected, String.join(" ", ps.getCommand()));
    }

    @Test
    public void adminModeWhereHeaderWhenFalse() {
        var expected = "powershell.exe -NoProfile -InputFormat None -ExecutionPolicy Bypass -EncodedCommand RQB4AGkAdAAgACgAUwB0AGEAcgB0AC0AUAByAG8AYwBlAHMAcwAgACIAJwAtAEMAbwBtAG0AYQBuAGQAJwAiACAALQBXAGEAaQB0ACAALQBQAGEAcwBzAFQAaAByAHUAIAAtAFYAZQByAGIAIABSAHUAbgBBAHMAIAAtAGEAcgBnAHUAbQBlAG4AdABsAGkAcwB0ACAAIgBXAHIAaQB0AGUALQBPAHUAdABwAHUAdAAiACwAIgAiACIASABlAGwAbABvACAAdwBvAHIAbABkACIAIgAiACkALgBFAHgAaQB0AEMAbwBkAGUA";

        var ps = PowerShell.getBuilder()
                .enableDefaultArgs(true)
                .setCommand("'-Command'")
                .addArgs(List.of("\"Write-Output\"", "Hello world"))
                .setAdminMode(true)
                .addAdminModeHeader(()-> false, "$ProgressPreference = 'SilentlyContinue'")
                .build();

        assertEquals(expected, String.join(" ", ps.getCommand()));
    }

    @Test
    public void adminModeWhereHeaders() {
        var expected = "powershell.exe -NoProfile -InputFormat None -ExecutionPolicy Bypass -EncodedCommand JABQAHIAbwBnAHIAZQBzAHMAUAByAGUAZgBlAHIAZQBuAGMAZQAgAD0AIAAnAFMAaQBsAGUAbgB0AGwAeQBDAG8AbgB0AGkAbgB1AGUAJwAKAEUAeABpAHQAIAAoAFMAdABhAHIAdAAtAFAAcgBvAGMAZQBzAHMAIAAiACcALQBDAG8AbQBtAGEAbgBkACcAIgAgAC0AVwBhAGkAdAAgAC0AUABhAHMAcwBUAGgAcgB1ACAALQBWAGUAcgBiACAAUgB1AG4AQQBzACAALQBhAHIAZwB1AG0AZQBuAHQAbABpAHMAdAAgACIAVwByAGkAdABlAC0ATwB1AHQAcAB1AHQAIgAsACIAIgAiAEgAZQBsAGwAbwAgAHcAbwByAGwAZAAiACIAIgApAC4ARQB4AGkAdABDAG8AZABlAA==";

        var ps = PowerShell.getBuilder()
                .enableDefaultArgs(true)
                .setCommand("'-Command'")
                .addArgs(List.of("\"Write-Output\"", "Hello world"))
                .setAdminMode(true)
                .addAdminModeHeader(List.of("$ProgressPreference = 'SilentlyContinue'"))
                .build();

        assertEquals(expected, String.join(" ", ps.getCommand()));
    }

    @Test
    public void adminModeWhereHeadersWhenTrue() {
        var expected = "powershell.exe -NoProfile -InputFormat None -ExecutionPolicy Bypass -EncodedCommand JABQAHIAbwBnAHIAZQBzAHMAUAByAGUAZgBlAHIAZQBuAGMAZQAgAD0AIAAnAFMAaQBsAGUAbgB0AGwAeQBDAG8AbgB0AGkAbgB1AGUAJwAKAEUAeABpAHQAIAAoAFMAdABhAHIAdAAtAFAAcgBvAGMAZQBzAHMAIAAiACcALQBDAG8AbQBtAGEAbgBkACcAIgAgAC0AVwBhAGkAdAAgAC0AUABhAHMAcwBUAGgAcgB1ACAALQBWAGUAcgBiACAAUgB1AG4AQQBzACAALQBhAHIAZwB1AG0AZQBuAHQAbABpAHMAdAAgACIAVwByAGkAdABlAC0ATwB1AHQAcAB1AHQAIgAsACIAIgAiAEgAZQBsAGwAbwAgAHcAbwByAGwAZAAiACIAIgApAC4ARQB4AGkAdABDAG8AZABlAA==";

        var ps = PowerShell.getBuilder()
                .enableDefaultArgs(true)
                .setCommand("'-Command'")
                .addArgs(List.of("\"Write-Output\"", "Hello world"))
                .setAdminMode(true)
                .addAdminModeHeader(()-> true, List.of("$ProgressPreference = 'SilentlyContinue'"))
                .build();

        assertEquals(expected, String.join(" ", ps.getCommand()));
    }

    @Test
    public void adminModeWhereHeadersWhenFalse() {
        var expected = "powershell.exe -NoProfile -InputFormat None -ExecutionPolicy Bypass -EncodedCommand RQB4AGkAdAAgACgAUwB0AGEAcgB0AC0AUAByAG8AYwBlAHMAcwAgACIAJwAtAEMAbwBtAG0AYQBuAGQAJwAiACAALQBXAGEAaQB0ACAALQBQAGEAcwBzAFQAaAByAHUAIAAtAFYAZQByAGIAIABSAHUAbgBBAHMAIAAtAGEAcgBnAHUAbQBlAG4AdABsAGkAcwB0ACAAIgBXAHIAaQB0AGUALQBPAHUAdABwAHUAdAAiACwAIgAiACIASABlAGwAbABvACAAdwBvAHIAbABkACIAIgAiACkALgBFAHgAaQB0AEMAbwBkAGUA";

        var ps = PowerShell.getBuilder()
                .enableDefaultArgs(true)
                .setCommand("'-Command'")
                .addArgs(List.of("\"Write-Output\"", "Hello world"))
                .setAdminMode(true)
                .addAdminModeHeader(()-> false, List.of("$ProgressPreference = 'SilentlyContinue'"))
                .build();

        assertEquals(expected, String.join(" ", ps.getCommand()));
    }

    @Test
    public void adminModeEchoTest() {
        var expected = "powershell.exe -EncodedCommand " +
                "RQB4AGkAdAAgACgAUwB0AGEAcgB0AC0AUAByAG8AYwBlAHMAcwAgACIAYwBtAGQAIgAgAC0AVwBhAGkAdAAgAC0AUABhAHMAcwBUAGgAcgB1ACAALQBWAGUAcgBiACAAUgB1AG4AQQBzACAALQBhAHIAZwB1AG0AZQBuAHQAbABpAHMAdAAgACcALwBrACcALAAnAGUAYwBoAG8AIAB0AGUAcwB0ACcAKQAuAEUAeABpAHQAQwBvAGQAZQA=";

        var ps = PowerShell.getBuilder()
                .setCommand("literal:cmd")
                .addArgs(List.of("'/k'", "literal:'echo test'"))
                .setAdminMode(true)
                .build();

        assertEquals(expected, String.join(" ", ps.getCommand()));
    }

    @Test
    public void nonAdminModeEchoTest() {
        var expected = "powershell.exe -EncodedCommand " +
                "RQB4AGkAdAAgACgAUwB0AGEAcgB0AC0AUAByAG8AYwBlAHMAcwAgACIAYwBtAGQAIgAgAC0AVwBhAGkAdAAgAC0AUABhAHMAcwBUAGgAcgB1ACAALQBhAHIAZwB1AG0AZQBuAHQAbABpAHMAdAAgACcALwBrACcALAAnAGUAYwBoAG8AIAB0AGUAcwB0ACcAKQAuAEUAeABpAHQAQwBvAGQAZQA=";

        var ps = PowerShell.getBuilder()
                .setCommand("literal:cmd")
                .addArgs(List.of("'/k'", "literal:'echo test'"))
                .setAdminMode(false)
                .build();

        assertEquals(expected, String.join(" ", ps.getCommand()));
    }

    @Test
    public void normalLiteralEchoTest() {
        var expected = "powershell.exe Write-Output \"Hello world\"";

        var ps = PowerShell.getBuilder()
                .setCommand("literal:Write-Output")
                .addArg("literal:\"Hello world\"")
                .build();

        assertEquals(expected, String.join(" ", ps.getCommand()));
    }

    @Test
    public void justArgs() {
        var expected = "powershell.exe -NoProfile -InputFormat None -ExecutionPolicy Bypass \"-Command\" \"& {Write-Output \"\"\"Hello World\"\"\"}\"";

        var ps = PowerShell.getBuilder()
                .enableDefaultArgs(true)
                .addArgs(List.of("\"-Command\"", "\"& {Write-Output \"\"\"Hello World\"\"\"}\""))
                .build();

        assertEquals(expected, String.join(" ", ps.getCommand()));
    }

    @Test
    public void justArgsWhenTrue() {
        var expected = "powershell.exe -NoProfile -InputFormat None -ExecutionPolicy Bypass \"-Command\" \"& {Write-Output \"\"\"Hello World\"\"\"}\"";

        var ps = PowerShell.getBuilder()
                .enableDefaultArgs(true)
                .addArgs(()-> true, List.of("\"-Command\"", "\"& {Write-Output \"\"\"Hello World\"\"\"}\""))
                .build();

        assertEquals(expected, String.join(" ", ps.getCommand()));
    }

    @Test
    public void justArgsWhenFalse() {
        var expected = "powershell.exe -NoProfile -InputFormat None -ExecutionPolicy Bypass";

        var ps = PowerShell.getBuilder()
                .enableDefaultArgs(true)
                .addArgs(()-> false, List.of("\"-Command\"", "\"& {Write-Output \"\"\"Hello World\"\"\"}\""))
                .build();

        assertEquals(expected, String.join(" ", ps.getCommand()));
    }

    @Test
    public void justArg() {
        var expected = "powershell.exe -NoProfile -InputFormat None -ExecutionPolicy Bypass \"-Command\" \"& {Write-Output \"\"\"Hello World\"\"\"}\"";

        var ps = PowerShell.getBuilder()
                .enableDefaultArgs(true)
                .addArg("\"-Command\"")
                .addArg("\"& {Write-Output \"\"\"Hello World\"\"\"}\"")
                .build();

        assertEquals(expected, String.join(" ", ps.getCommand()));
    }

    @Test
    public void usingRawArgs() {
        var expected = "powershell.exe -NoProfile -InputFormat None -ExecutionPolicy Bypass Arg1 Arg2 Arg3";

        var ps = PowerShell.getBuilder()
                .enableDefaultArgs(true)
                .setRawArgs(true)
                .addArg("Arg1")
                .addArgs(List.of("Arg2","Arg3"))
                .build();

        assertEquals(expected, String.join(" ", ps.getCommand()));
    }

    @Test
    public void suppressProgramName() {
        var expected = "-NoProfile -InputFormat None -ExecutionPolicy Bypass \"\"\"Arg1\"\"\" \"\"\"Arg2\"\"\" \"\"\"Arg3\"\"\"";

        var ps = PowerShell.getBuilder()
                .enableDefaultArgs(true)
                .suppressProgramName(true)
                .addArg("Arg1")
                .addArgs(List.of("Arg2","Arg3"))
                .build();

        assertEquals(expected, String.join(" ", ps.getCommand()));
    }

    @Test
    public void justEncodedArg() {
        var expected = "powershell.exe -NoProfile -InputFormat None -ExecutionPolicy Bypass \"-EncodedCommand\" IgAmACAAewBXAHIAaQB0AGUALQBPAHUAdABwAHUAdAAgACIAIgAiAEgAZQBsAGwAbwAgAFcAbwByAGwAZAAiACIAIgB9ACIA";

        var ps = PowerShell.getBuilder()
                .enableDefaultArgs(true)
                .addArg("\"-EncodedCommand\"")
                .addEncodedArg("\"& {Write-Output \"\"\"Hello World\"\"\"}\"")
                .build();

        assertEquals(expected, String.join(" ", ps.getCommand()));
    }

    @Test
    public void justEncodedArgWhenTrue() {
        var expected = "powershell.exe -NoProfile -InputFormat None -ExecutionPolicy Bypass \"-EncodedCommand\" IgAmACAAewBXAHIAaQB0AGUALQBPAHUAdABwAHUAdAAgACIAIgAiAEgAZQBsAGwAbwAgAFcAbwByAGwAZAAiACIAIgB9ACIA";

        var ps = PowerShell.getBuilder()
                .enableDefaultArgs(true)
                .addArg("\"-EncodedCommand\"")
                .addEncodedArg(()-> true, "\"& {Write-Output \"\"\"Hello World\"\"\"}\"")
                .build();

        assertEquals(expected, String.join(" ", ps.getCommand()));
    }

    @Test
    public void justEncodedArgWhenFalse() {
        var expected = "powershell.exe -NoProfile -InputFormat None -ExecutionPolicy Bypass";

        var ps = PowerShell.getBuilder()
                .enableDefaultArgs(true)
                .addArg(()-> false, "\"-EncodedCommand\"")
                .addEncodedArg(()-> false, "\"& {Write-Output \"\"\"Hello World\"\"\"}\"")
                .build();

        assertEquals(expected, String.join(" ", ps.getCommand()));
    }

    @Test
    public void justArgWhenTrue() {
        var expected = "powershell.exe -NoProfile -InputFormat None -ExecutionPolicy Bypass \"-Command\" \"& {Write-Output \"\"\"Hello World\"\"\"}\"";

        var ps = PowerShell.getBuilder()
                .enableDefaultArgs(true)
                .addArg(()-> true, "\"-Command\"")
                .addArg(()-> true, "\"& {Write-Output \"\"\"Hello World\"\"\"}\"")
                .build();

        assertEquals(expected, String.join(" ", ps.getCommand()));
    }

    @Test
    public void justArgWhenFalse() {
        var expected = "powershell.exe -NoProfile -InputFormat None -ExecutionPolicy Bypass";

        var ps = PowerShell.getBuilder()
                .enableDefaultArgs(true)
                .addArg(()-> false, "\"-Command\"")
                .addArg(()-> false, "\"& {Write-Output \"\"\"Hello World\"\"\"}\"")
                .build();

        assertEquals(expected, String.join(" ", ps.getCommand()));
    }


    @Test
    public void justPArgs() {
        var expected = "powershell.exe -NoProfile -InputFormat None -ExecutionPolicy Bypass -Version 2.0";

        var ps = PowerShell.getBuilder()
                .enableDefaultArgs(true)
                .addPArgs(List.of("-Version", "2.0"))
                .build();

        assertEquals(expected, String.join(" ", ps.getCommand()));
    }

    @Test
    public void justPArgsWhenTrue() {
        var expected = "powershell.exe -NoProfile -InputFormat None -ExecutionPolicy Bypass -Version 2.0";

        var ps = PowerShell.getBuilder()
                .enableDefaultArgs(true)
                .addPArgs(()-> true, List.of("-Version", "2.0"))
                .build();

        assertEquals(expected, String.join(" ", ps.getCommand()));
    }

    @Test
    public void justPArgsWhenFalse() {
        var expected = "powershell.exe -NoProfile -InputFormat None -ExecutionPolicy Bypass";

        var ps = PowerShell.getBuilder()
                .enableDefaultArgs(true)
                .addPArgs(()-> false, List.of("-Version", "2.0"))
                .build();

        assertEquals(expected, String.join(" ", ps.getCommand()));
    }

    @Test
    public void justPArg() {
        var expected = "powershell.exe -NoProfile -InputFormat None -ExecutionPolicy Bypass -Version 2.0";

        var ps = PowerShell.getBuilder()
                .enableDefaultArgs(true)
                .addPArg("-Version")
                .addPArg("2.0")
                .build();

        assertEquals(expected, String.join(" ", ps.getCommand()));
    }

    @Test
    public void justPArgWhenTrue() {
        var expected = "powershell.exe -NoProfile -InputFormat None -ExecutionPolicy Bypass -Version 2.0";

        var ps = PowerShell.getBuilder()
                .enableDefaultArgs(true)
                .addPArg(()-> true, "-Version")
                .addPArg(()-> true, "2.0")
                .build();

        assertEquals(expected, String.join(" ", ps.getCommand()));
    }

    @Test
    public void justPArgWhenFalse() {
        var expected = "powershell.exe -NoProfile -InputFormat None -ExecutionPolicy Bypass";

        var ps = PowerShell.getBuilder()
                .enableDefaultArgs(true)
                .addPArg(()-> false, "-Version")
                .addPArg(()-> false, "2.0")
                .build();

        assertEquals(expected, String.join(" ", ps.getCommand()));
    }

    @Test
    public void argsWithZArgs() {
        var expected = "powershell.exe -NoProfile -InputFormat None -ExecutionPolicy " +
                "Bypass \"-Command\" \"& {Write-Output \"\"\"Hello World\"\"\"}\" \"\"\"-Version\"\"\" \"\"\"2.0\"\"\"";

        var ps = PowerShell.getBuilder()
                .enableDefaultArgs(true)
                .addArg("\"-Command\"")
                .addZArgs(List.of("\"& {Write-Output \"\"\"Hello World\"\"\"}\"", "-Version", "2.0"))
                .build();

        assertEquals(expected, String.join(" ", ps.getCommand()));
    }

    @Test
    public void argsWithZArgsWhenTrue() {
        var expected = "powershell.exe -NoProfile -InputFormat None -ExecutionPolicy " +
                "Bypass \"-Command\" \"& {Write-Output \"\"\"Hello World\"\"\"}\" \"\"\"-Version\"\"\" \"\"\"2.0\"\"\"";

        var ps = PowerShell.getBuilder()
                .enableDefaultArgs(true)
                .addArg(()-> true, "\"-Command\"")
                .addZArgs(()-> true, List.of("\"& {Write-Output \"\"\"Hello World\"\"\"}\"", "-Version", "2.0"))
                .build();

        assertEquals(expected, String.join(" ", ps.getCommand()));
    }

    @Test
    public void argsWithZArgsWhenFalse() {
        var expected = "powershell.exe -NoProfile -InputFormat None -ExecutionPolicy Bypass";

        var ps = PowerShell.getBuilder()
                .enableDefaultArgs(true)
                .addArg(()-> false, "\"-Command\"")
                .addZArgs(()-> false, List.of("\"& {Write-Output \"\"\"Hello World\"\"\"}\"", "-Version", "2.0"))
                .build();

        assertEquals(expected, String.join(" ", ps.getCommand()));
    }

    @Test
    public void argsWithZArg() {
        var expected = "powershell.exe -NoProfile -InputFormat None -ExecutionPolicy Bypass \"-Command\" \"& {Write-Output \"\"\"Hello World\"\"\"}\"";

        var ps = PowerShell.getBuilder()
                .enableDefaultArgs(true)
                .addArg("\"-Command\"")
                .addZArg("\"& {Write-Output \"\"\"Hello World\"\"\"}\"")
                .build();

        assertEquals(expected, String.join(" ", ps.getCommand()));
    }

    @Test
    public void justZArgs() {
        var expected = "powershell.exe -NoProfile -InputFormat None -ExecutionPolicy Bypass '-Command' \"& {Write-Output \"\"\"Hello World\"\"\"}\"";

        var ps = PowerShell.getBuilder()
                .enableDefaultArgs(true)
                .addZArgs(List.of("'-Command'", "\"& {Write-Output \"\"\"Hello World\"\"\"}\""))
                .build();

        assertEquals(expected, String.join(" ", ps.getCommand()));
    }

    @Test
    public void justZArg() {
        var expected = "powershell.exe -NoProfile -InputFormat None -ExecutionPolicy Bypass '-Command' \"& {Write-Output \"\"\"Hello World\"\"\"}\"";

        var ps = PowerShell.getBuilder()
                .enableDefaultArgs(true)
                .addZArg("'-Command'")
                .addZArg("\"& {Write-Output \"\"\"Hello World\"\"\"}\"")
                .build();

        assertEquals(expected, String.join(" ", ps.getCommand()));
    }

    @Test
    public void justZArgWhenTrue() {
        var expected = "powershell.exe -NoProfile -InputFormat None -ExecutionPolicy Bypass '-Command' \"& {Write-Output \"\"\"Hello World\"\"\"}\"";

        var ps = PowerShell.getBuilder()
                .enableDefaultArgs(true)
                .addZArg(()-> true, "'-Command'")
                .addZArg(()-> true, "\"& {Write-Output \"\"\"Hello World\"\"\"}\"")
                .build();

        assertEquals(expected, String.join(" ", ps.getCommand()));
    }

    @Test
    public void justZArgWhenFalse() {
        var expected = "powershell.exe -NoProfile -InputFormat None -ExecutionPolicy Bypass";

        var ps = PowerShell.getBuilder()
                .enableDefaultArgs(true)
                .addZArg(()-> false, "'-Command'")
                .addZArg(()-> false, "\"& {Write-Output \"\"\"Hello World\"\"\"}\"")
                .build();

        assertEquals(expected, String.join(" ", ps.getCommand()));
    }

    @Test
    public void justZArgsWhenTrue() {
        var expected = "powershell.exe -NoProfile -InputFormat None -ExecutionPolicy Bypass '-Command' \"& {Write-Output \"\"\"Hello World\"\"\"}\"";

        var ps = PowerShell.getBuilder()
                .enableDefaultArgs(true)
                .addZArgs(() -> true, List.of("'-Command'", "\"& {Write-Output \"\"\"Hello World\"\"\"}\""))
                .build();

        assertEquals(expected, String.join(" ", ps.getCommand()));
    }

    @Test
    public void justZArgsWhenFalse() {
        var expected = "powershell.exe -NoProfile -InputFormat None -ExecutionPolicy Bypass";

        var ps = PowerShell.getBuilder()
                .enableDefaultArgs(true)
                .addZArgs(() -> false, List.of("'-Command'", "\"& {Write-Output \"\"\"Hello World\"\"\"}\""))
                .build();

        assertEquals(expected, String.join(" ", ps.getCommand()));
    }

    @Test
    public void adminModePreferNonAdminNonElevated() {
        var expected = "powershell.exe -NoProfile -InputFormat None -ExecutionPolicy Bypass -EncodedCommand RQB4AGkAdAAgACgAUwB0AGEAcgB0AC0AUAByAG8AYwBlAHMAcwAgACIAVwBoAGUAcgBlACIAIAAtAFcAYQBpAHQAIAAtAFAAYQBzAHMAVABoAHIAdQAgAC0AVgBlAHIAYgAgAFIAdQBuAEEAcwAgAC0AYQByAGcAdQBtAGUAbgB0AGwAaQBzAHQAIAAiACIAIgBXAGgAZQByAGUAIgAiACIAKQAuAEUAeABpAHQAQwBvAGQAZQA=";

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
        var expected = "powershell.exe -NoProfile -InputFormat None -ExecutionPolicy Bypass -EncodedCommand RQB4AGkAdAAgACgAUwB0AGEAcgB0AC0AUAByAG8AYwBlAHMAcwAgACIALQBDAG8AbQBtAGEAbgBkACIAIAAtAFcAYQBpAHQAIAAtAFAAYQBzAHMAVABoAHIAdQAgAC0AYQByAGcAdQBtAGUAbgB0AGwAaQBzAHQAIAAiAFcAcgBpAHQAZQAtAE8AdQB0AHAAdQB0ACIALAAiACIAIgBIAGUAbABsAG8AIAB3AG8AcgBsAGQAIgAiACIAKQAuAEUAeABpAHQAQwBvAGQAZQA=";

        try (var checker = Mockito.mockStatic(RunAsChecker.class)) {
            checker.when(RunAsChecker::isElevatedMode).thenReturn(true);

            var ps = PowerShell.getBuilder()
                    .enableDefaultArgs(true)
                    .setCommand("-Command")
                    .addArgs(List.of("\"Write-Output\"", "Hello world"))
                    .setAdminMode(true)
                    .setPreferNonAdminMode(true)
                    .build();

            assertEquals(expected, String.join(" ", ps.getCommand()));
        }
    }

    @Test
    public void commandOnly() {
        var expected = "powershell.exe \"-Help\"";

        try (var checker = Mockito.mockStatic(RunAsChecker.class)) {
            checker.when(RunAsChecker::isElevatedMode).thenReturn(true);

            var ps = PowerShell.getBuilder()
                    .setCommand("-Help")
                    .build();

            assertEquals(expected, String.join(" ", ps.getCommand()));
        }
    }

    @Test
    public void commandOnlyWhenTrue() {
        var expected = "powershell.exe \"-Help\"";

        try (var checker = Mockito.mockStatic(RunAsChecker.class)) {
            checker.when(RunAsChecker::isElevatedMode).thenReturn(true);

            var ps = PowerShell.getBuilder()
                    .setCommand(()-> true, "-Help")
                    .build();

            assertEquals(expected, String.join(" ", ps.getCommand()));
        }
    }

    @Test
    public void commandOnlyWhenFalse() {
        var expected = "powershell.exe";

        try (var checker = Mockito.mockStatic(RunAsChecker.class)) {
            checker.when(RunAsChecker::isElevatedMode).thenReturn(true);

            var ps = PowerShell.getBuilder()
                    .setCommand(()-> false, "-Help")
                    .build();

            assertEquals(expected, String.join(" ", ps.getCommand()));
        }
    }

    @Test
    public void changeAdminLogic() {
        var expected = "powershell.exe Command: -Command Args: Arg1,Arg2";

        try (var checker = Mockito.mockStatic(RunAsChecker.class)) {
            checker.when(RunAsChecker::isElevatedMode).thenReturn(true);

            var ps = PowerShell.getBuilder()
                    .setCommand("-Command")
                    .addArgs(List.of("Arg1", "Arg2"))
                    .setAdminMode(true)
                    .setAdminModeLogic((___command, ___args) -> String.format("Command: %s Args: %s", ___command, String.join(",", ___args)))
                    .build();

            assertEquals(expected, String.join(" ", ps.getCommand()));
        }
    }

    @Test
    public void keytoolScriptTest() {
        var expected = "-NoProfile -InputFormat None -ExecutionPolicy Bypass -Command {\n" +
                "& 'C:\\Program Files\\OpenJDK\\jdk-17.0.1\\bin\\keytool.exe' '-delete' '-cacerts' '-storepass' 'changeit' '-alias' 'CiscoUmbrella.cer [sk]'\n" +
                "& 'C:\\Program Files\\OpenJDK\\jdk-17.0.1\\bin\\keytool.exe' '-delete' '-cacerts' '-storepass' 'changeit' '-alias' 'EncryptIt.cer [sk]'\n" +
                "& 'C:\\Program Files\\OpenJDK\\jdk-17.0.1\\bin\\keytool.exe' '-delete' '-cacerts' '-storepass' 'changeit' '-alias' 'R3-2025.cer [sk]'\n" +
                "}";

        try (var checker = Mockito.mockStatic(RunAsChecker.class)) {
            checker.when(RunAsChecker::isElevatedMode).thenReturn(true);

            var scriptCommands = List.of(
                    "& 'C:\\Program Files\\OpenJDK\\jdk-17.0.1\\bin\\keytool.exe' '-delete' '-cacerts' '-storepass' 'changeit' '-alias' 'CiscoUmbrella.cer [sk]'",
                    "& 'C:\\Program Files\\OpenJDK\\jdk-17.0.1\\bin\\keytool.exe' '-delete' '-cacerts' '-storepass' 'changeit' '-alias' 'EncryptIt.cer [sk]'",
                    "& 'C:\\Program Files\\OpenJDK\\jdk-17.0.1\\bin\\keytool.exe' '-delete' '-cacerts' '-storepass' 'changeit' '-alias' 'R3-2025.cer [sk]'"
            );

            var ps = PowerShell.getBuilder()
                    .enableDefaultArgs(true)
                    .suppressProgramName(true)
                    .addArg("literal:-Command")
                    .addArg(String.format("{\n%s\n}", String.join("\n", scriptCommands)))
                    .build();

            assertEquals(expected, String.join(" ", ps.getCommand()));
        }
    }


}
