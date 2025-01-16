# Difficulty Death Scaler

A fabric mod scaling difficulty on number of death like Demon's Soul.

When a player dies, the mod increases the difficulty.

After 12 hours, if no one has died, or if you killed a boss, the difficulty is reset to the previous stage.
If you were at 8 deaths, the mod will forget 3 deaths, and you will now be at 5 deaths.

Check the [wiki](https://architects-land.github.io/difficulty-death-scaler/) to understand how the mod works.
It has a lot of features including:
- penalizing player who dies every 5 seconds
- scoped difficulty (one global and one per player)
- increasing difficulty automatically if the game is too simple
- decreasing difficulty if players didn't die

## Command

The mod adds one command: `/difficultydeathscaler` (or `/dds`)
- `/difficultydeathscaler get` gives the current global difficulty (or `/ddsg`)
- `/difficultydeathscaler get [selector]` gives the current player difficulty targeted by the selector
(`/ddsp` gives your current difficulty)
- `/difficultydeathscaler set global [number of death]` sets the number of death in global difficulty (OP command)
- `/difficultydeathscaler set player [selector] difficulty [number of death]` sets the number of death in player
difficulty targeted by the selector (OP command)
- `/difficultydeathscaler set player [selector] daily-death [number of death]` sets the number of death in 24h for
targeted player by the selector (OP command)

## Configuration

This mod can be configured via gamerules.
Check the [wiki](https://architects-land.github.io/difficulty-death-scaler/) to have more information.
