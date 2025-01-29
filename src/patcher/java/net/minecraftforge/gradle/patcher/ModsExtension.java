package net.minecraftforge.gradle.patcher;

import groovy.lang.GroovyObjectSupport;
import org.gradle.api.Project;

import javax.inject.Inject;
import javax.inject.Provider;
import java.util.ArrayList;
import java.util.List;

public class ModsExtension extends GroovyObjectSupport {

    public static final String EXTENSION_NAME = "mods";

    protected final Project project;

    private List<String> mods;

    @Inject
    public ModsExtension(final Project project) {
        this.project = project;
        this.mods = new ArrayList<>();
    }

    public void mods(List<String> mods) {
        System.out.println(mods);
        this.mods = mods;
    }

    public List<String> getMods() {
        return mods;
    }

}
