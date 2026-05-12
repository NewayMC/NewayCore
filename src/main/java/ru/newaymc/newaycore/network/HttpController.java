package ru.newaymc.newaycore.network;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class HttpController {
    public static final Logger LOGGER = LogManager.getLogger(HttpController.class);

    public static String GET(String url, String header) throws Exception {
        HttpClient client = HttpClient.newHttpClient();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header(header, "application/json")
                .timeout(Duration.ofMinutes(2))
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        HttpController.LOGGER.info("Status code: " + response.statusCode());
        return response.body();
    }

    public static String POST(String json, String url, String header) throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header(header, "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        HttpController.LOGGER.info("Status code: " + response.statusCode());
        return response.body();
    }
}
