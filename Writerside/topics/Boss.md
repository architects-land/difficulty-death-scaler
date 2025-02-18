# Boss

In vanilla, there are three bosses: Ender Dragon, Wither and Elder Guardian.
For Difficulty Death Scaler, the Warden is also a boss.

All these bosses can be buffed by appliqueing a netherite ingot to them (with the right click).
This buff depends on the boss.

When you kill a buffed boss, you reduce one step of the [global difficulty](Global-Difficulty.md).

## Ender Dragon

The Ender Dragon's health is modified according to the current difficulty and the number of players in the end.

Each time a player joins the fight, the dragon gains more health.

<deflist collapsible="true">
    <def title="Math behind" default-state="collapsed">
        The formula describing the health of the dragon is 
<code>alpha*( ln(players+1) * (difficulty/beta + 1)^2 - ln(2) ) + base</code>, where <code>players</code> is the number 
of players in the end, <code>difficulty</code> is the current difficulty, <code>alpha</code> a constant (15 actually), 
<code>beta</code> another constant (5 actually) and <code>base</code> is another constant describing the base health for 
the dragon (200 actually, like in vanilla).
    </def>
</deflist>

## Buff

The Ender Dragon has more health.

The Wither's stats are not modified, but you can't trap it in bedrock: you have to do the real fight.

The Elder Guardian:
- has a better knockback resistance
- is smaller
- has more health
- is faster
- do more damage

The Warden:
- has a better knockback resistance (it's not very useful actually...)
- is faster
- is angry when he is buffed
