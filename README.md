# ONEPING

A lightweight, client-side Fabric mod for Minecraft **26.2**.

## What it does

- Run `/oneping` in multiplayer to display the latency reported by the server for your player.
- Labels the reading as a simple quality estimate.
- Does not alter packets, spoof latency, bypass anti-cheat, or require server installation.

## Important: what a client mod can and cannot do

A client-side mod cannot directly shorten the physical network route to a server or remove latency caused by distance, congestion, Wi-Fi, or the server itself. ONEPING starts with honest measurement rather than pretending to reduce the number shown.

Future optimization work will only be added when it can be measured and shown not to break protocol behavior or multiplayer compatibility.

## Requirements

- Minecraft Java Edition 26.2
- Fabric Loader 0.19.5 or newer
- Fabric API for 26.2
- Java 25

## Build

Run:

```bash
gradle build
```

The mod JAR is generated in `build/libs/`.

## License

MIT
