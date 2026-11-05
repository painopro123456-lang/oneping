package dev.oneping;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;

public final class OnePingClient implements ClientModInitializer {
    private static final int SAMPLE_INTERVAL_TICKS = 20;
    private static final int MAX_SAMPLES = 60;
    private static final Deque<Integer> samples = new ArrayDeque<>();
    private static int ticksUntilSample;

    @Override
    public void onInitializeClient() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null || client.getConnection() == null) {
                samples.clear();
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

            // Sample every second, including repeated values, so averages describe time
            // rather than only moments when the server's displayed ping changes.
            samples.addLast(Math.max(0, info.getLatency()));
            while (samples.size() > MAX_SAMPLES) {
                samples.removeFirst();
            }
        });

        ClientCommandRegistrationCallback.EVENT.register((dispatcher, buildContext) ->
            dispatcher.register(ClientCommands.literal("oneping")
                .then(ClientCommands.literal("reset")
                    .executes(context -> {
                        samples.clear();
                        ticksUntilSample = 0;
                        context.getSource().sendFeedback(Component.literal(
                            "[ONEPING] History cleared. Collecting fresh samples."));
                        return 1;
                    }))
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

                    List<Integer> sorted = new ArrayList<>(samples);
                    Collections.sort(sorted);
                    int min = sorted.get(0);
                    int max = sorted.get(sorted.size() - 1);
                    int median = sorted.get(sorted.size() / 2);
                    long total = 0;
                    long variationTotal = 0;
                    Integer previous = null;

                    for (int sample : samples) {
                        total += sample;
                        if (previous != null) {
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
                        "[ONEPING] 60s avg: " + average + " ms | Median: " + median
                            + " ms | Min: " + min + " ms | Max: " + max + " ms"));
                    context.getSource().sendFeedback(Component.literal(
                        "[ONEPING] Jitter: " + jitter + " ms | Samples: " + count
                            + "/" + MAX_SAMPLES));
                    context.getSource().sendFeedback(Component.literal(
                        "[ONEPING] Use /oneping reset to clear history. Measurements are real; ping is never spoofed."));
                    return 1;
                }))
        );
    }
}
