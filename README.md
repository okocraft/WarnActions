![GitHub release (latest SemVer)](https://img.shields.io/github/v/release/okocraft/WarnActions)
![GitHub Workflow Status](https://img.shields.io/github/actions/workflow/status/okocraft/WarnActions/maven.yml?branch=main)
![GitHub](https://img.shields.io/github/license/okocraft/WarnActions)

# WarnActions

A Velocity plugin that executes additional actions when the player is warned.

## Configuration

```yml
auto-punishments: # Additional punishments on warn
  1:
    type: "KICK"
    reason: "You are warned and kicked. Reason: %REASON%"

auto-commands: # Commands that are executed after warn
  1: "alert %PLAYER% is warned (reason: %REASON%)"

```

## License

This project is under the Apache License version 2.0. Please see [LICENSE](LICENSE) for more info.

Copyright © 2023-2024, OKOCRAFT and Siroshun09
