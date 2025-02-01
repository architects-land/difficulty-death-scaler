# Player Difficulty

The player difficulty affects only the player linked.
It mainly modifies player's attributes.

If you die more 5 times or more in less than 24 hours, you are temp ban for 12 hours.
This feature prevents a player to increase the difficulty too much.

You can modify the temp ban with these gamerules:
- `difficulty-death-scaler:enableTempBan` to disable the temp ban (default: true)
- `difficulty-death-scaler:deathBeforeTempBan` to set the number of death before a ban (default: 5)
- `difficulty-death-scaler:tempBanDuration` to set the temp ban's duration in hour (default: 12)

<deflist collapsible="true">
    <def title="0 death" default-state="expanded">
        <p>This step is the default one, and it is easier than most vanilla survival.</p>
        <p>The block break speed is increased by 40% (is like a haste 2 effect)</p>
        <p>The movement speed is increased by 10% (is like a speed 1 effect)</p>
    </def>
    <def title="1 death" default-state="collapsed">
        <p>Reset movement speed.</p>
        <p>Set break speed to 20% (is like a haste 2 effect)</p>
    </def>
    <def title="2 deaths" default-state="collapsed">
        Remove one heart
    </def>
    <def title="3 and 4 deaths" default-state="collapsed">
        <p>Reset block break speed</p>
        <p>Remove another heart (two hearts removed).</p>
    </def>
    <def title="5 and 6 deaths" default-state="collapsed">
        Remove another heart (three hearts removed).
    </def>
    <def title="7 deaths" default-state="collapsed">
        Set movement speed to -10% (is like a slowness 1 effect).
    </def>
    <def title="8 and 9 deaths" default-state="collapsed">
        Decrease block break speed by 20% (is like a mining fatigue 1 effect).
    </def>
    <def title="10 and 11 deaths" default-state="collapsed">
        Remove another heart (four hearts removed).
    </def>
    <def title="12, 13 and 14 deaths" default-state="collapsed">
        Set movement speed to -20% (is like a slowness 2 effect).
    </def>
    <def title="15 deaths" default-state="collapsed">
        Remove another heart (five hearts removed).
    </def> 
</deflist>
