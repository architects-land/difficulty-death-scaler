package world.anhgelus.architectsland.difficultydeathscaler.boss;

import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import world.anhgelus.architectsland.difficultydeathscaler.DifficultyDeathScaler;

import java.util.HashSet;
import java.util.Set;

import static world.anhgelus.architectsland.difficultydeathscaler.DifficultyDeathScaler.getGlobalDifficulty;

public class DragonBuff {
	public final static Identifier MODIFIER_ID = Identifier.parse("dds_health");
	// variables for function
	private final static int ALPHA = 15;
	private final static int BETA = 5;
	private final static int BASE = 200;
	private final EnderDragon enderDragon;
	private final Set<ServerPlayer> players = new HashSet<>();
	private int oldHealth;

	public DragonBuff(EnderDragon enderDragon) {
		this.enderDragon = enderDragon;
	}

	private static int newHealth(int players, int difficulty) {
		players = Math.min(players, 40); // cap to 40
		return (int) Math.floor(
			ALPHA * (Math.log(players + 1) * Math.pow(((double) difficulty / BETA + 1), 2) - Math.log(2)) + BASE
		); // alpha*( ln(players+1) * (difficulty/beta + 1)^2 - ln(2) ) + base
	}

	private static void buff(EnderDragon enderDragon, float val) {
		final var attr = enderDragon.getAttribute(Attributes.MAX_HEALTH);
		if (attr == null) {
			DifficultyDeathScaler.LOGGER.warn("Dragon's health attribute is null");
			return;
		}
		if (attr.hasModifier(MODIFIER_ID)) attr.removeModifier(MODIFIER_ID);
		final var modifier = new AttributeModifier(MODIFIER_ID, val, AttributeModifier.Operation.ADD_MULTIPLIED_BASE);
		attr.addTransientModifier(modifier);
	}

	public void playerEntersEnd(ServerPlayer player) {
		if (players.contains(player)) return;
		players.add(player);

		final var difficulty = getGlobalDifficulty().getNumberOfDeath();
		updateDragonHealth(difficulty, false);
	}

	public void updateDragonHealth(int difficulty, boolean bypassCheck) {
		final var nh = newHealth(players.size(), difficulty); // new health
		if (!bypassCheck && nh - oldHealth < 0) {
			DifficultyDeathScaler.LOGGER.warn("Dragon's health is lower: {} (now) vs {} (before)", nh, oldHealth);
			return;
		}
		final var val = ((float) nh / BASE) - 1;
		buff(enderDragon, val);
		// heal the dragon the difference
		enderDragon.heal(Math.abs(nh - oldHealth));
		oldHealth = nh;
		DifficultyDeathScaler.LOGGER.info("Dragon's health: {}", nh);
	}

	public boolean isDragonAlive() {
		return enderDragon.isAlive();
	}
}
