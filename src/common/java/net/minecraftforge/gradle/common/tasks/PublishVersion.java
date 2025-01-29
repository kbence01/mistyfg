package net.minecraftforge.gradle.common.tasks;

import net.minecraftforge.gradle.common.http.RequestBuilder;
import net.minecraftforge.gradle.common.http.dto.UpdateClientRequest;
import net.minecraftforge.gradle.common.util.MistyGameJson;
import net.minecraftforge.gradle.common.util.MistyVersionJson;
import net.minecraftforge.gradle.common.util.Utils;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.TaskAction;

import java.io.IOException;

public abstract class PublishVersion extends DefaultTask {

    @TaskAction
    public void run() throws IOException {
        MistyVersionJson versionJson = Utils.loadJson(getVersionFile().get().getAsFile(), MistyVersionJson.class);
        MistyGameJson gameJson = Utils.loadJson(getGameFile().get().getAsFile(), MistyGameJson.class);

        System.out.println(versionJson.javaVersion);
        System.out.println(gameJson.gameVersion);

        new RequestBuilder<UpdateClientRequest>()
                .url("http://localhost:8084/launcher/v1/client/version")
                .method("POST")
                .body(new UpdateClientRequest(
                        "commitid", // todo: get from arguments
                        "b54d51ec-c4b7-4066-9e67-d663149db50e", // todo: get from arguments
                        gameJson,
                        versionJson
                ))
                .retrieve();
    }

    @InputFile
    public abstract RegularFileProperty getGameFile();

    @InputFile
    public abstract RegularFileProperty getVersionFile();

}
