package org.kiuwn.counterstrikemod;

import com.mojang.logging.LogUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import org.kiuwn.counterstrikemod.Command.BuyCommand;
import org.kiuwn.counterstrikemod.Command.MapCommand;
import org.kiuwn.counterstrikemod.Command.MatchCommand;
import org.kiuwn.counterstrikemod.MatchMaking.MapManager;
import org.kiuwn.counterstrikemod.MatchMaking.Match;
import org.slf4j.Logger;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(Counterstrikemod.MODID)
public class Counterstrikemod {

    // Define mod id in a common place for everything to reference
    public static final String MODID = "counterstrikemod";
    // Directly reference a slf4j logger
    private static final Logger LOGGER = LogUtils.getLogger();
    // Create a Deferred Register to hold Blocks which will all be registered under the "counterstrikemod" namespace
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, MODID);
    // Create a Deferred Register to hold Items which will all be registered under the "counterstrikemod" namespace
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);

    private MapManager mapManager = null;

    private Match match = null;

    private static Counterstrikemod instance;

    private MinecraftServer server;

    public MinecraftServer getServer() {
        return server;
    }

    public static Counterstrikemod getInstance() {
        return instance;
    }

    public MapManager getMapManager() {
        return mapManager;
    }

    public Counterstrikemod() {
        instance = this;
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        // Register the commonSetup method for modloading
        modEventBus.addListener(this::commonSetup);

        // Register the Deferred Register to the mod event bus so blocks get registered
        BLOCKS.register(modEventBus);
        // Register the Deferred Register to the mod event bus so items get registered
        ITEMS.register(modEventBus);

        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);

        setupMapManager();
    }

    private void setupMapManager() {
        File file = new File("maps.yml");
        if (!file.exists()) {
            try {
                file.createNewFile();
                BufferedWriter writer = new BufferedWriter(new FileWriter(file));
                writer.write("<maps></maps>");
                writer.close();
                LOGGER.info("Created maps.yml");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        mapManager = new MapManager(file);
        mapManager.save();
    }

    public Match getMatch() {
        return match;
    }

    public static Logger getLOGGER() {
        return LOGGER;
    }

    public void setMatch(Match match) {
        this.match = match;
    }

    private void commonSetup(final FMLCommonSetupEvent event) {

    }

    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent e) {
        MapCommand.register(e.getDispatcher());
        MatchCommand.register(e.getDispatcher());
        BuyCommand.register(e.getDispatcher());
    }

    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        // Do something when the server starts
        server = event.getServer();
    }

    @SubscribeEvent
    public void onLivingDeath(LivingDeathEvent event) {
        if (match != null) {
            match.onLivingDeath(event);
        }
    }

    @SubscribeEvent
    public void onPlayerRespawnEvent(PlayerEvent.PlayerRespawnEvent event) {
        if (match != null) {
            match.onPlayerRespawnEvent(event);
        }
    }

    @SubscribeEvent
    public void onServerStopping(ServerStoppingEvent event) {
        MapManager.getInstance().save();
    }

    // You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
    @Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {

    }
}
