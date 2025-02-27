# Boss

In vanilla, there are three bosses: Ender Dragon, Wither and Elder Guardian.
For Difficulty Death Scaler, the Warden is also a boss.

All these bosses can be buffed by appliqueing a netherite ingot to them (with the right click).
This buff depends on the boss.

When you kill a buffed boss, you reduce one step of the [global difficulty](Global-Difficulty.md).

## Ender Dragon

The Ender Dragon's health is modified according to the current difficulty and to the number of players in the end.

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

The dragon also have their blast resistance adjusted according to the current difficulty.

<deflist collapsible="true">
    <def title="Math behind" default-state="collapsed">
        The formula describing the blast resistance is
<code>min( 1- (difficulty-1)/40 * amount, amount)</code> where <code>difficulty</code> is the difficulty and 
<code>amount</code> is the base damage.
    </def>
</deflist>

## Buff

<procedure title="Elder Guardian" id="elder_guardian" collapsible="true">
    <step>Has a better knockback resistance</step>
    <step>Is smaller</step>
    <step>Has more health</step>
    <step>Is faster</step>
    <step>Do more damage</step>
</procedure>

<procedure title="Ender Dragon" id="ender_dragon" collapsible="true">
    <step>Has more health</step>
</procedure>

<procedure title="Warden" id="warden" collapsible="true">
    <step>Has a better knockback resistance (it's not very useful actually...)</step>
    <step>Is faster</step>
    <step>Is angry when he is buffed</step>
</procedure>

<procedure title="Wither" id="wither" collapsible="true">
    <step>Stats are not modified</step>
    <step>Can't be trapped in bedrock</step>
</procedure>
