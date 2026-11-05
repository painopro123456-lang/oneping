package dev.oneping;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;

public final class OnePingClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) ->
            dispatcher.register(LiteralArgumentBuilder.<net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource>literal("oneping")
                .executes(context -> {
                    Minecraft client = Minecraft.getInstance();

                    if (client.player == null || client.getConnection() == null) {
                        context.getSource().sendFeedback(Component.literal(
                            "[ONEPING] Join a multiplayer server first."));
                        return 0;
                    }

                    PlayerInfo info = client.getConnection().getPlayerInfo(client.player.getUUID());
                    if (info == null) {
                        context.getSource().sendFeedback(Component.literal(
                            "[ONEPING] Server latency is not available yet."));
                        return 0;
                    }

                    int latency = info.getLatency();
                    String quality = latency < 60 ? "Excellent"
                        : latency < 100 ? "Good"
                        : latency < 180 ? "Fair"
                        : "High latency";

                    context.getSource().sendFeedback(Component.literal(
                        "[ONEPING] Server-reported latency: " + latency + " ms (" + quality + ")."));
                    context.getSource().sendFeedback(Component.literal(
                        "[ONEPING] This is a real server-reported measurement; it is not modified."));
                    context.getSource().sendFeedback(Component.literal(
                        "[ONEPING] For lower latency, use Ethernet/strong Wi-Fi and close bandwidth-heavy downloads."));
                    return 1;
                }))
        );
    }
}
