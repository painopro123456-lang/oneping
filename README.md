# ONEPING

A lightweight, client-side Fabric mod for Minecraft **26.2**.

## Commands

Run `/oneping` while connected to a multiplayer server.

ONEPING reports:
- Current server-reported latency
- Average latency over the recent sample window
- Minimum and maximum latency
- Jitter, calculated as the average change between consecutive samples
- Number of samples collected (up to 30, sampled approximately once per second)

The sample window resets when you disconnect.

## What it can and cannot optimize

ONEPING does not fake ping, modify gameplay packets, bypass anti-cheat, or require server installation. It is designed to work as a normal client-side Fabric mod on multiplayer servers.

A client mod cannot make the physical route to a server shorter or remove latency caused by distance, ISP routing, congestion, Wi-Fi interference, or server-side processing. Minecraft already uses low-latency TCP settings in its normal connection setup, so blindly toggling socket flags would not be a genuine optimization.

The first release focuses on trustworthy measurements. Any future automatic network tuning will only be added if it produces a repeatable improvement in real tests and remains compatible with ordinary multiplayer servers.

## Requirements

- Minecraft Java Edition 26.2
- Fabric Loader 0.19.5 or newer
- Fabric API 0.161.0+26.2
- Java 25

## Build

Run:

```bash
gradle build
```

The mod JAR is generated in `build/libs/`.

## License

MIT
