package net.minecraftforge.gradle.common.util;

import java.util.List;

public class MistyGameJson { // GR_CLIENT

    public Game game;

    public String gameVersion;

    public List<MistyVersionJson.Library> libraries;

    public static class Game {

        public MistyVersionJson.Library original;
        public MistyVersionJson.Library universal;
        public MistyVersionJson.Library srg;
        public MistyVersionJson.Library slim;
        public MistyVersionJson.Library extra;
        public MistyVersionJson.Library client;
        public MistyVersionJson.Library patches;
        public MistyVersionJson.Library srg_mappings;

    }

}
