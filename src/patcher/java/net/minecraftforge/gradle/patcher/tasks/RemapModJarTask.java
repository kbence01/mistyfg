package net.minecraftforge.gradle.patcher.tasks;

import net.minecraftforge.gradle.common.util.MavenArtifactDownloader;
import net.minecraftforge.gradle.common.util.MinecraftRepo;
import net.minecraftforge.gradle.mcp.MCPRepo;
import org.gradle.api.DefaultTask;
import org.gradle.api.Project;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.TaskAction;
import org.gradle.jvm.tasks.Jar;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

public class RemapModJarTask extends DefaultTask {

    @InputFiles
    private List<String> mods;

    private Project project;

    @TaskAction
    public void doTask() {
        List<File> modsMapped = this.mods.stream().map(mod -> MavenArtifactDownloader.manual(this.project, mod, true)).collect(Collectors.toList());
        System.out.println(this.mods);
    }

    public void mods(List<String> mods) {
        this.mods = mods;
    }

    public List<String> mods() {
        return this.mods;
    }

    public void project(Project project) {
        this.project = project;
    }

}
