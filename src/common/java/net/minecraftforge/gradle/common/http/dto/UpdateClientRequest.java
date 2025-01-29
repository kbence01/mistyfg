package net.minecraftforge.gradle.common.http.dto;

import net.minecraftforge.gradle.common.util.MistyGameJson;
import net.minecraftforge.gradle.common.util.MistyVersionJson;

public class UpdateClientRequest {

    public String commit_id;

    public String api_key;

    public MistyGameJson gr_client;

    public MistyVersionJson gr_libs;

    public UpdateClientRequest(String commit_id, String api_key, MistyGameJson gr_client, MistyVersionJson gr_libs) {
        this.commit_id = commit_id;
        this.api_key = api_key;
        this.gr_client = gr_client;
        this.gr_libs = gr_libs;
    }

}
