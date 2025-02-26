package world.anhgelus.architectsland.difficultydeathscaler.boss;

import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import world.anhgelus.architectsland.difficultydeathscaler.DifficultyDeathScaler;
import world.anhgelus.architectsland.difficultydeathscaler.utils.Getters;

import java.util.HashSet;
import java.util.Set;

public class DragonBuff {
    // variables for function
    private final static int ALPHA = 15;
    private final static int BETA = 5;
    private final static int BASE = 200;

    public final static Identifier MODIFIER_ID = Identifier.of("dds_health");

    private final EnderDragonEntity enderDragon;
    private final Set<ServerPlayerEntity> players = new HashSet<>();
    private int n = 0;

    private int adjustHealth;

    public DragonBuff(EnderDragonEntity enderDragon) {
        this.enderDragon = enderDragon;
    }

    public void playerEntersEnd(ServerPlayerEntity player) {
        if (players.contains(player)) return;
        final var playersBefore = n++;
//        players.add(player);

        final var difficulty = Getters.GLOBAL_DIFFICULTY_GETTER.get().getNumberOfDeath();
        final var oh = adjustedNewHealth(playersBefore, difficulty); // old health
        final var nh = adjustedNewHealth(n, difficulty); // new health
        if (nh - oh < 0) {
            DifficultyDeathScaler.LOGGER.warn("Dragon's health is lower: {} (now) vs {} (before)", nh, oh);
            return;
        }
        final var val = ((float) nh / BASE) - 1;
        buff(enderDragon, val);
        // heal the dragon the difference
        enderDragon.heal(nh - oh);
        DifficultyDeathScaler.LOGGER.info("Dragon's health: {}", nh);
    }

    private int adjustedNewHealth(int players, int difficulty) {
        return newHealth(players, difficulty) + adjustHealth;
    }

    public void setAdjustHealth(int adjustHealth) {
        this.adjustHealth = adjustHealth;
        final var val = ((float) adjustedNewHealth(players.size(), Getters.GLOBAL_DIFFICULTY_GETTER.get().getNumberOfDeath()) / BASE) - 1;
        buff(enderDragon, val);
        if (adjustHealth > 0) enderDragon.heal(adjustHealth);
    }

    public boolean isDragonAlive() {
        return enderDragon.isAlive();
    }

    private static int newHealth(int players, int difficulty) {
        players = Math.min(players, 40); // cap to 40
        return (int) Math.floor(
                ALPHA * (Math.log(players + 1) * Math.pow(((double) difficulty / BETA + 1), 2) - Math.log(2)) + BASE
        ); // alpha*( ln(players+1) * (difficulty/beta + 1)^2 - ln(2) ) + base
    }

    private static void buff(EnderDragonEntity enderDragon, float val) {
        final var attr = enderDragon.getAttributeInstance(EntityAttributes.MAX_HEALTH);
        if (attr == null) {
            DifficultyDeathScaler.LOGGER.warn("Dragon's health attribute is null");
            return;
        }
        if (attr.hasModifier(MODIFIER_ID)) attr.removeModifier(MODIFIER_ID);
        final var modifier = new EntityAttributeModifier(MODIFIER_ID, val, EntityAttributeModifier.Operation.ADD_MULTIPLIED_BASE);
        attr.addTemporaryModifier(modifier);
    }
}
