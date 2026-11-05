# ONEPING

A lightweight, client-side Fabric mod for Minecraft 26.2 focused on honest multiplayer latency diagnostics.

## Features

- Samples the server-reported player latency once per second.
- Keeps a rolling 60-second history.
- Reports current ping, average, median, minimum, maximum, and average sample-to-sample variation (jitter).
- `/oneping` prints the current connection report.
- `/oneping reset` clears the history and starts a fresh measurement window.
- Does not send extra probe packets, modify game packets, spoof latency, or bypass server protections.

## Important: what this mod can and cannot do

Minecraft's displayed multiplayer latency is a round-trip measurement involving your network route and the server. A client-only mod cannot shorten the physical route, fix Wi-Fi interference, change ISP routing, or make a slow server respond faster. ONEPING therefore does not promise fake or guaranteed ping reduction.

The mod's purpose is to show whether the connection is consistently slow or suffering from spikes. Use the measurements to identify whether a change to your network, router, connection type, or server region actually helps. The sampling itself adds no network traffic.

## Requirements

- Minecraft 26.2
- Fabric Loader 0.19.5 or newer
- Fabric API for Minecraft 26.2
- Java 25 or newer

## Build

Run:

```bash
gradle build
```

The mod JAR is generated in `build/libs/`.

## Install

Place the built `oneping-*.jar` file in your Minecraft instance's `mods` folder alongside Fabric API.

## License

MIT
