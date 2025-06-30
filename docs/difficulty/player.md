# Player difficulty

The player difficulty is split into steps.
The activated steps is determined by the player' deaths.
This difficulty is *per player*.

Each time a player dies, the number of deaths is increased.
If a step is reached, it is activated.

If the player don't die during 24 hours, the latest activated step is disabled.
For example, if you are at 15 deaths and the two latest steps were activated at 10 deaths and 13
deaths, after the decrease, the number of deaths will be 10.

If a player dies 5 times or more in less than 24 hours, they are ban for 12 hours.
This feature prevents players to increase the [global difficulty](/difficulty/global) too much.
To configure it, check the [configuration](/configuration).

## Easy steps

**0 death**
Increase block break speed by 40% (is like a haste 2 effect).
Increase movement speed by 10% (is like a speed 1 effect).

**1 death**
Reset movement speed.
Set block break speed's increase to 20% (is like a haste 1 effect).
Limit waypoint transmit range to 2500.

## Ominous steps

**2 deaths**
Remove one heart.
Limit waypoint transmit range to 1000.

**3 deaths**
Reset block break speed.
Remove another heart (two removed).
Limit waypoint transmit range to 500.

## Dangerous steps

**5 deaths**
Remove another heart (three removed).
Limit waypoint transmit range to 250.

**7 deaths**
Decrease movement speed by 10% (is like a slowness 1 effect).

**8 deaths**
Decrease block break speed by 20% (is like a mining fatigue 1 effect).
Limit waypoint transmit range to 150.

## Evil steps

**10 deaths**
Remove another heart (four removed).

**12 deaths**
Decrease movement speed by 20% (is like a slowness 2 effect).
Limit waypoint transmit range to 100.

## No returns steps

**15 deaths**
Remove another heart (five removed).
Limit waypoint transmit range to 50.
