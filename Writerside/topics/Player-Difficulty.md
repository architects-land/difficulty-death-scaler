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
        <p>The luck and the block break speed are increased by 10%</p>
    </def>
    <def title="1 and 2 deaths" default-state="collapsed">
        <p>Reset luck.</p>
        <p>Remove one heart.</p>
    </def>
    <def title="3 and 4 deaths" default-state="collapsed">
        <p>Reset block break speed</p>
        <p>Remove another heart (two hearts removed).</p>
    </def>
    <def title="5 deaths" default-state="collapsed">
        Remove another heart (three hearts removed).
    </def>
    <def title="6 deaths" default-state="collapsed">
        Decrease luck by 10%.
    </def>
    <def title="7 deaths" default-state="collapsed">
        Remove another heart (four hearts removed).
    </def>
    <def title="8 and 9 deaths" default-state="collapsed">
        <p>Decrease block break speed by 10%</p>
        <p>Decrease luck speed by 20%</p>
    </def>
    <def title="10 deaths" default-state="collapsed">
        Remove another heart (five hearts removed).
    </def>
</deflist>
