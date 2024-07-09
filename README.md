# Difficulty Death Scaler

A fabric mod scaling difficulty on number of death like Demon's Soul.

When a player dies, the mod increases the difficulty.

After 12 hours, if no one has died, or if you killed a boss, the difficulty is reset to the previous stage.
If you were at 8 deaths, the mod will forget 3 deaths, and you will now be at 5 deaths.

Level of difficulty:

| Death⋅s | Difficulty | playersSleepingPercentage | maxHearts | naturalRegeneration |
|---------|------------|---------------------------|-----------|---------------------|
| 0       | Normal     | 30                        | 10        | true                |
| 1       | Normal     | 70                        | 10        | true                |
| 3       | Hard       | 70                        | 10        | true                |
| 5       | Hard       | 100                       | 10        | true                |
| 7       | Hard       | 100                       | 9         | true                |
| 10      | Hard       | 100                       | 8         | true                |
| 12      | Hard       | 100                       | 7         | true                |
| 15      | Hard       | 100                       | 6         | true                |
| 17      | Hard       | 100                       | 5         | true                |
| 20+     | Hard       | 100                       | 5         | false               |

## Technologies

- Minecraft 1.21
- Fabric 0.15.11
- Fabric API 0.100.4+1.21
