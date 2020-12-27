package com.gdmc.httpinterfacemod;

import com.gdmc.httpinterfacemod.handlers.ChunkHandler;
import com.gdmc.httpinterfacemod.handlers.CommandHandler;
import com.gdmc.httpinterfacemod.handlers.SetBlockHandler;
import com.sun.net.httpserver.HttpServer;
import net.minecraft.server.MinecraftServer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.InetSocketAddress;

public class GdmcHttpServer {
    private static HttpServer httpServer;
    private static MinecraftServer mcServer;
    private static final Logger LOGGER = LogManager.getLogger();

    public static void startServer(MinecraftServer mcServer) throws IOException {
        GdmcHttpServer.mcServer = mcServer;
        httpServer = HttpServer.create(new InetSocketAddress(9000), 0);
        httpServer.setExecutor(null); // creates a default executor
        createContexts();
        httpServer.start();
    }

    public static void stopServer() {
        httpServer.stop(5);
    }

    private static void createContexts() {
        httpServer.createContext("/command", new CommandHandler(mcServer));
        httpServer.createContext("/chunks", new ChunkHandler(mcServer));
        httpServer.createContext("/setblock", new SetBlockHandler(mcServer));
    }
}



//                    //old chunk palette stuff
//                    ArrayList<String> sectionList = new ArrayList<>();
//                    ListNBT sectionListNBT = new ListNBT();
//                    for(ChunkSection chunkSection : chunk.getSections()) {
//                        if(ChunkSection.isEmpty(chunkSection)) {
//                            sectionList.add("");
//                            continue;
//                        }
//                        PalettedContainer<BlockState> palettedContainer = chunkSection.getData();
//                        CompoundNBT chunkPaletteCompound = new CompoundNBT();
//                        palettedContainer.writeChunkPalette(chunkPaletteCompound, "palette", "data");
//                        sectionList.add(chunkPaletteCompound.toString());
//                    }
