package com.gdmc.httpinterfacemod.handlers;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.phys.Vec3;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class CommandHandler extends HandlerBase {

    public CommandHandler(MinecraftServer mcServer) {
        super(mcServer);
    }

    @Override
    public void internalHandle(HttpExchange httpExchange) throws IOException {

        Map<String, String> queryParams = parseQueryString(httpExchange.getRequestURI().getRawQuery());

        // POST: x, y, z positions
        int x;
        int y;
        int z;
        String dimension;
        try {
            x = Integer.parseInt(queryParams.getOrDefault("x", "0"));
            y = Integer.parseInt(queryParams.getOrDefault("y", "0"));
            z = Integer.parseInt(queryParams.getOrDefault("z", "0"));

            dimension = queryParams.getOrDefault("dimension", null);
        } catch (NumberFormatException e) {
            throw new HttpException("Could not parse query parameter: " + e.getMessage(), 400);
        }

        // execute command(s)
        InputStream bodyStream = httpExchange.getRequestBody();
        List<String> commands = new BufferedReader(new InputStreamReader(bodyStream)).lines().toList();

        CommandSourceStack cmdSrc = createCommandSource("GDMC-CommandHandler", dimension, new Vec3(x, y, z));

        JsonArray returnValues = new JsonArray();
        for (String command: commands) {
            if (command.length() == 0) {
                continue;
            }
            // requests to run the actual command execution on the main thread
            CompletableFuture<JsonObject> cfs = CompletableFuture.supplyAsync(() -> {
                try {
                    int commandStatus = mcServer.getCommands().getDispatcher().execute(command, cmdSrc);
                    return instructionStatus(
                        commandStatus != 0,
                        commandStatus != 1 && commandStatus != 0 ? String.valueOf(commandStatus) : null
                    );
                } catch (CommandSyntaxException e) {
                    return instructionStatus(false, e.getMessage());
                }
            }, mcServer);

            // block this thread until the above code has run on the main thread
            returnValues.add(cfs.join());
        }

        // Response headers
        Headers responseHeaders = httpExchange.getResponseHeaders();
        setDefaultResponseHeaders(responseHeaders);

        // body
        resolveRequest(httpExchange, returnValues.toString());
    }
}