package se.citerus;

import io.javalin.Javalin;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.stream.Collectors;

public class Main {
    public static void main(String[] args) {
        var app = Javalin.create(/*config*/)
                .get("/", ctx -> {
                    String result = restartDocker();
                    ctx.json(Map.of("message", result));
                })
                .start(7070);
    }

    private static String restartDocker() throws IOException, InterruptedException {
        Process process = new ProcessBuilder("java", "-version").redirectErrorStream(true).start();
        int result = process.waitFor();
        System.out.println("Process exit code: " + result);
        return readOutput(process.getInputStream());
    }

    private static String readOutput(InputStream inputStream) throws IOException {
        return new BufferedReader(new InputStreamReader(inputStream)).lines().collect(Collectors.joining(""));
    }
}