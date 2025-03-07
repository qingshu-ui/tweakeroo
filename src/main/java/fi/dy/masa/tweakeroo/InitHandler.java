package fi.dy.masa.tweakeroo;

import net.minecraft.client.MinecraftClient;
import fi.dy.masa.malilib.config.ConfigManager;
import fi.dy.masa.malilib.event.*;
import fi.dy.masa.malilib.interfaces.IInitializationHandler;
import fi.dy.masa.malilib.interfaces.IRenderer;
import fi.dy.masa.malilib.interfaces.IWorldLoadListener;
import fi.dy.masa.malilib.registry.Registry;
import fi.dy.masa.malilib.util.data.ModInfo;
import fi.dy.masa.tweakeroo.config.Callbacks;
import fi.dy.masa.tweakeroo.config.Configs;
import fi.dy.masa.tweakeroo.data.DataManager;
import fi.dy.masa.tweakeroo.data.ServerDataSyncer;
import fi.dy.masa.tweakeroo.event.ClientTickHandler;
import fi.dy.masa.tweakeroo.event.InputHandler;
import fi.dy.masa.tweakeroo.event.RenderHandler;
import fi.dy.masa.tweakeroo.event.WorldLoadListener;
import fi.dy.masa.tweakeroo.gui.GuiConfigs;

public class InitHandler implements IInitializationHandler
{
    @Override
    public void registerModHandlers()
    {
        ConfigManager.getInstance().registerConfigHandler(Reference.MOD_ID, new Configs());
        Registry.CONFIG_SCREEN.registerConfigScreenFactory(
                new ModInfo(Reference.MOD_ID, Reference.MOD_NAME, GuiConfigs::new)
        );
        ServerDataSyncer.getInstance().onGameInit();

        InputEventHandler.getKeybindManager().registerKeybindProvider(InputHandler.getInstance());
        InputEventHandler.getInputManager().registerKeyboardInputHandler(InputHandler.getInstance());
        InputEventHandler.getInputManager().registerMouseInputHandler(InputHandler.getInstance());

        IRenderer renderer = new RenderHandler();
        RenderEventHandler.getInstance().registerGameOverlayRenderer(renderer);
        RenderEventHandler.getInstance().registerTooltipLastRenderer(renderer);
        RenderEventHandler.getInstance().registerWorldLastRenderer(renderer);

        IWorldLoadListener worldListener = new WorldLoadListener();
        WorldLoadHandler.getInstance().registerWorldLoadPreHandler(worldListener);
        WorldLoadHandler.getInstance().registerWorldLoadPostHandler(worldListener);

        ServerHandler.getInstance().registerServerHandler(DataManager.getInstance());

        TickHandler.getInstance().registerClientTickHandler(new ClientTickHandler());
        TickHandler.getInstance().registerClientTickHandler(ServerDataSyncer.getInstance());

        Callbacks.init(MinecraftClient.getInstance());
    }
}
