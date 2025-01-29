package net.minecraftforge.gradle.common.util;

import java.util.List;

public class MistyVersionJson { // GR_LIBS

    public List<String> arguments;

    public List<Library> libraries;

    public List<Library> natives;

    public MistyVersionJson.Library assetIndex;

    public String assetsUrl;

    public int javaVersion;

    public String gameVersion;

    public static class Library {

        public String sha1;
        public String url;
        public String name;

    }

}
