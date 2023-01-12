package by.srinater.performance_tuning.overload;

import by.srinater.performance_tuning.PerformanceTuningMod;
import net.minecraft.network.chat.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ClipContext;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

@Mod.EventBusSubscriber(value = {Dist.DEDICATED_SERVER})
public class Listener {
    public static MinecraftServer Server;
    public static volatile boolean tickBeginNotify;
    public static volatile boolean tickEndNotify;
    public static Timer listenerTimer = new Timer();
    public static TimerTask timerTask = null;
    @SubscribeEvent
    public static void onServerStarted(ServerStartedEvent event)
    {
        Server = event.getServer();
    }
    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event)
    {
        if (event.phase == TickEvent.Phase.START) {
            if (timerTask != null)
                timerTask.cancel();
            timerTask = new TickDelayTask();
            listenerTimer.schedule(timerTask, 2000, 2000);
        }
    }
    @SubscribeEvent
    public static void onServerStopped(ServerStoppedEvent event)
    {
        timerTask.cancel();
        listenerTimer.cancel();;
    }
    static class TickDelayTask extends TimerTask
    {
        @Override
        public void run() {
            for (ServerPlayer player: Server.getPlayerList().getPlayers())
            {
                TranslatableComponent component = new TranslatableComponent("component.server_overload");
                component.setStyle(Style.EMPTY.withBold(true).withColor(TextColor.fromRgb(0xdd0000)));
                player.sendMessage(component, ChatType.SYSTEM, PerformanceTuningMod.ChatUUID);
            }
            PerformanceTuningMod.LOGGER.warn("Server overload! Dumping server thread stack trance:");
            for (var stack :Server.getRunningThread().getStackTrace())
                PerformanceTuningMod.LOGGER.warn(stack.toString());
        }
    }
}
