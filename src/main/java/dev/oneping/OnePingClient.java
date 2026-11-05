package dev.oneping;

import java.util.ArrayDeque;
import java.util.Deque;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;

public final class OnePingClient implements ClientModInitializer {
    private static final int SAMPLE_INTERVAL_TICKS = 20;
    private static final int MAX_SAMPLES = 30;
    private static final Deque<Integer> samples = new ArrayDeque<>();
    private static int ticksUntilSample = 0;
    private static int previousPing = -1;

    @Override
    public void onInitializeClient() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null || client.getConnection() == null) {
                samples.clear();
                previousPing = -1;
                ticksUntilSample = 0;
                return;
            }

            if (++ticksUntilSample < SAMPLE_INTERVAL_TICKS) {
                return;
            }
            ticksUntilSample = 0;

            PlayerInfo info = client.getConnection().getPlayerInfo(client.player.getUUID());
            if (info == null) {
                return;
            }

            int ping = Math.max(0, info.getLatency());
            if (samples.isEmpty() || ping != previousPing) {
                samples.addLast(ping);
                previousPing = ping;
                while (samples.size() > MAX_SAMPLES) {
                    samples.removeFirst();
                }
            }
        });

        ClientCommandRegistrationCallback.EVENT.register((dispatcher, buildContext) ->
            dispatcher.register(ClientCommands.literal("oneping")
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

                    int current = Math.max(0, info.getLatency());
                    if (samples.isEmpty()) {
                        samples.add(current);
                    }

                    int min = Integer.MAX_VALUE;
                    int max = Integer.MIN_VALUE;
                    long total = 0;
                    long variationTotal = 0;
                    int previous = -1;

                    for (int sample : samples) {
                        min = Math.min(min, sample);
                        max = Math.max(max, sample);
                        total += sample;
                        if (previous >= 0) {
                            variationTotal += Math.abs(sample - previous);
                        }
                        previous = sample;
                    }

                    int count = samples.size();
                    int average = (int) Math.round((double) total / count);
                    int jitter = count > 1
                        ? (int) Math.round((double) variationTotal / (count - 1))
                        : 0;

                    String quality = current < 60 ? "Excellent"
                        : current < 100 ? "Good"
                        : current < 180 ? "Fair"
                        : "High latency";

                    context.getSource().sendFeedback(Component.literal(
                        "[ONEPING] Current: " + current + " ms (" + quality + ")"));
                    context.getSource().sendFeedback(Component.literal(
                        "[ONEPING] Average: " + average + " ms | Min: " + min
                            + " ms | Max: " + max + " ms"));
                    context.getSource().sendFeedback(Component.literal(
                        "[ONEPING] Jitter: " + jitter + " ms | Samples: " + count
                            + "/" + MAX_SAMPLES));
                    context.getSource().sendFeedback(Component.literal(
                        "[ONEPING] Readings are real server-reported latency; nothing is spoofed."));
                    return 1;
                }))
        );
    }
}
