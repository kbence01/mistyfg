/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.minecraftforge.gradle.mcp.function;

import net.minecraftforge.gradle.common.util.HashStore;
import net.minecraftforge.gradle.common.util.Utils;
import net.minecraftforge.gradle.mcp.util.MCPEnvironment;
import org.gradle.jvm.toolchain.JavaLanguageVersion;
import org.gradle.jvm.toolchain.JavaToolchainService;

import javax.annotation.Nullable;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

class ExecuteFunction implements MCPFunction {

    private static final Pattern REPLACE_PATTERN = Pattern.compile("^\\{(\\w+)\\}$");

    protected final File jar;
    protected final String[] jvmArgs;
    protected String[] runArgs;
    protected final Map<String, String> envVars;

    @Nullable
    private Integer javaVersion;
    private Map<String, String> data;

    public ExecuteFunction(File jar, String[] jvmArgs, String[] runArgs, Map<String, String> envVars) {
        this(jar, jvmArgs, runArgs, envVars, null);
    }

    public ExecuteFunction(File jar, String[] jvmArgs, String[] runArgs, Map<String, String> envVars, @Nullable Integer javaVersion) {
        this.jar = jar;
        this.jvmArgs = jvmArgs;
        this.runArgs = runArgs;
        this.envVars = envVars;
        this.javaVersion = javaVersion;
    }

    @Override
    public void loadData(Map<String, String> data) {
        this.data = data;
    }

    @Override
    public void initialize(MCPEnvironment environment, ZipFile zip) throws IOException {
        analyzeAndExtract(environment, zip, jvmArgs);
        analyzeAndExtract(environment, zip, runArgs);
    }

    @Override
    public File execute(MCPEnvironment environment) throws IOException, InterruptedException, ExecutionException {
        // Add an output and log argument if there wasn't one
        System.out.println("[DEBUG] EXECUTE FUNCTION START");
        Map<String, Object> arguments = environment.getArguments();
        String outputExtension = (String)arguments.getOrDefault("outputExtension", "jar");
        arguments.computeIfAbsent("output", k -> environment.getFile("output." + outputExtension));
        arguments.computeIfAbsent("log", k -> environment.getFile("log.log"));

        // Get input and output files
        File output = (File)environment.getArguments().get("output");
        System.out.println("[DEBUG] " + output.getAbsolutePath());
        System.out.println("asd");

        // Find out what the inputs are
        Map<String, Object> replacedArgs = new HashMap<>();
        List<String> jvmArgList = applyVariableSubstitutions(environment, Arrays.asList(jvmArgs), arguments, replacedArgs);
        List<String> runArgList = applyVariableSubstitutions(environment, Arrays.asList(runArgs), arguments, replacedArgs);

        replacedArgs.remove("output");
        replacedArgs.remove("log");

        HashStore hashStore = new HashStore(environment.project).load(environment.getFile("lastinput.sha1"));
        hashStore.add("args", String.join(" ", runArgs));
        hashStore.add("jvmargs", String.join(" ", jvmArgs));
        hashStore.add("jar", jar);
        replacedArgs.forEach((key, value) -> {
            if (value instanceof File) {
                hashStore.add(key, (File)value);
            } else if (value instanceof String) {
                hashStore.add(key, (String)value);
            }
        });
        addInputs(hashStore);
        if (hashStore.isSame() && output.exists()) return output;

        // Delete previous output
        if (output.exists()) output.delete();

        // Set up working directory
        File workingDir = environment.getWorkingDir();
        workingDir.mkdirs();

        // Locate main class in jar file
        JarFile jarFile = new JarFile(jar);
        String mainClass = jarFile.getManifest().getMainAttributes().getValue(Attributes.Name.MAIN_CLASS);
        jarFile.close();

        // Do not implicitly use the Java version that Gradle itself is using.
        // Instead use a launcher compatible with the version required by MCP.
        JavaToolchainService toolchainService = environment.project.getExtensions().getByType(JavaToolchainService.class);
        JavaLanguageVersion toolchainVersion = this.javaVersion != null ? JavaLanguageVersion.of(this.javaVersion) : environment.getJavaVersion();
        String launcher = toolchainService.launcherFor(spec -> spec.getLanguageVersion().set(toolchainVersion))
                .get()
                .getExecutablePath()
                .getAsFile()
                .getAbsolutePath();

        System.out.println("TOOLCHAIN VERSION " + toolchainVersion.toString());

        System.out.println("EXECUTE START");

        // Execute command
        try (BufferedOutputStream log_out = new BufferedOutputStream(new FileOutputStream(environment.getFile("console.log")))) {
            environment.project.javaexec(java -> {
                PrintWriter writer = new PrintWriter(log_out);
                Function<String, String> quote = s -> '"' + s + '"';
                writer.println("JVM:         " + launcher);
                writer.println("JVM Args:    " + jvmArgList.stream().map(quote).collect(Collectors.joining(", ")));
                writer.println("Run Args:    " + runArgList.stream().map(quote).collect(Collectors.joining(", ")));
                writer.println("Classpath:   " + jar.getAbsolutePath());
                writer.println("Working Dir: " + workingDir.getAbsolutePath());
                writer.println("Main Class:  " + mainClass);
                writer.flush();
                java.executable(launcher);
                java.setJvmArgs(jvmArgList);
                java.setArgs(runArgList);
                java.setClasspath(environment.project.files(jar));
                java.setWorkingDir(workingDir);
                java.getMainClass().set(mainClass);
                java.setStandardOutput(log_out);
            }).rethrowFailure().assertNormalExitValue();
        }

        // Return the output file
        hashStore.save();
        return output;
    }

    private List<String> applyVariableSubstitutions(MCPEnvironment environment, List<String> list, Map<String, Object> arguments, Map<String, Object> inputs) {
        return list.stream().map(s -> applyVariableSubstitutions(environment, s, arguments, inputs)).collect(Collectors.toList());
    }

    private String applyVariableSubstitutions(MCPEnvironment environment, String value, Map<String, Object> arguments, Map<String, Object> inputs) {
        Matcher matcher = REPLACE_PATTERN.matcher(value);
        if (!matcher.find()) return value; // Not a replaceable string

        String argName = matcher.group(1);
        if (argName != null) {
            Object argument = arguments.get(argName);
            if (argument instanceof File) {
                inputs.put(argName, argument);
                return ((File)argument).getAbsolutePath();
            } else if (argument instanceof String) {
                inputs.put(argName, argument);
                return (String)argument;
            }

            String dataElement = data.get(argName);
            if (dataElement != null) {
                inputs.put(argName, environment.getFile(dataElement));
                return dataElement;
            }
        }
        throw new IllegalStateException("The string '" + value + "' did not return a valid substitution match!");
    }

    private void analyzeAndExtract(MCPEnvironment environment, ZipFile zip, String[] args) throws IOException {
        for (String arg : args) {
            Matcher matcher = REPLACE_PATTERN.matcher(arg);
            if (!matcher.find()) continue;

            String argName = matcher.group(1);
            if (argName == null) continue;


            String referencedData = data.get(argName);
            if (referencedData == null) continue;

            ZipEntry entry = zip.getEntry(referencedData);
            if (entry == null) continue;
            String entryName = entry.getName();

            if (entry.isDirectory()) {
                Utils.extractDirectory(environment::getFile, zip, entryName);
            } else {
                Utils.extractFile(zip, entry, environment.getFile(entryName));
            }
        }
    }

    protected void addInputs(HashStore cache) {

    }

}
