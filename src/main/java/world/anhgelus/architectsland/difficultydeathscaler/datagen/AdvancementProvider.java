package world.anhgelus.architectsland.difficultydeathscaler.datagen;

import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricAdvancementProvider;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementType;
import net.minecraft.advancements.triggers.PlayerTrigger;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.monster.ElderGuardian;
import net.minecraft.world.entity.monster.warden.Warden;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import world.anhgelus.architectsland.difficultydeathscaler.datagen.criterion.*;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import static world.anhgelus.architectsland.difficultydeathscaler.DifficultyDeathScaler.id;

public class AdvancementProvider extends FabricAdvancementProvider {
	public static final int LEAVE_NO_RETURN = 0;
	public static final int LEAVE_NO_RETURN_PLAYER = 1;
	public static final int DIFFICULTY_INCREASE = 2;
	public static final int BUFF_BOSS = 3;

	public final static int BOUNTY_BE_PRESENT = 0;
	public final static int BOUNTY_RECEIVE = 1;
	public final static int BOUNTY_KILLER = 2;
	public final static int BOUNTY_KILLED = 3;

	protected AdvancementProvider(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registryLookup) {
		super(output, registryLookup);
	}

	@Override
	public void generateAdvancement(HolderLookup.Provider wrapperLookup, Consumer<AdvancementHolder> consumer) {
		final var ddsWorld = Advancement.Builder.recipeAdvancement()
			.rootDisplay(
				Items.RESPAWN_ANCHOR,
				Component.literal("Difficulty Death Scaler"),
				Component.literal("Welcome to a world governed by Difficulty Death Scaler!"),
				id(""),
				AdvancementType.TASK,
				true,
				true,
				false
			)
			.addCriterion("joined", PlayerTrigger.TriggerInstance.tick())
			.save(consumer, id("dds_world"));

		// global difficulty
		final var easySteps = create(consumer, ModCriteria.REACH_GLOBAL_DIFFICULTY, ddsWorld, Items.POPPY, "Easy steps reached!", "The world is easier than vanilla", "easy_steps", 0, 7);
		final var ominousSteps = create(consumer, ModCriteria.REACH_GLOBAL_DIFFICULTY, easySteps, Items.SHIELD, "Ominous steps reached!", "The world is becoming harder", "ominous_steps", 7, 20);
		final var dangerousSteps = create(consumer, ModCriteria.REACH_GLOBAL_DIFFICULTY, ominousSteps, Items.SKELETON_SKULL, "Dangerous steps reached!...", "The world wants to kill you", "dangerous_steps", 20, 28);
		final var evilSteps = create(consumer, ModCriteria.REACH_GLOBAL_DIFFICULTY, dangerousSteps, Items.WITHER_SKELETON_SKULL, "Evil steps reached...", "Please stops dying", "evil_steps", 28, 37);
		final var noReturnsSteps = create(consumer, ModCriteria.REACH_GLOBAL_DIFFICULTY, evilSteps, Items.WITHER_ROSE, "No return is possible now...", "Your nightmares become true...", "no_return_steps", 37, -1);

		// player difficulty
		final var easyPlayerSteps = create(consumer, ModCriteria.REACH_PLAYER_DIFFICULTY, ddsWorld, Items.DANDELION, "Easy player steps reached!", "You have op buff", "easy_player_steps", 0, 2);
		final var ominousPlayerSteps = create(consumer, ModCriteria.REACH_PLAYER_DIFFICULTY, easyPlayerSteps, Items.GOLDEN_AXE, "Ominous player steps reached!", "Debuff are starting...", "ominous_player_steps", 2, 5);
		final var dangerousPlayerSteps = create(consumer, ModCriteria.REACH_PLAYER_DIFFICULTY, ominousPlayerSteps, Items.PIGLIN_HEAD, "Dangerous player steps reached!...", "You are weak", "dangerous_player_steps", 5, 10);
		final var evilPlayerSteps = create(consumer, ModCriteria.REACH_PLAYER_DIFFICULTY, dangerousPlayerSteps, Items.ZOMBIE_HEAD, "Evil player steps reached...", "You are really weak, proceed with caution", "evil_player_steps", 10, 12);
		final var noReturnPlayerSteps = create(consumer, ModCriteria.REACH_PLAYER_DIFFICULTY, evilPlayerSteps, Items.PLAYER_HEAD, "No return player steps reached...", "The only solution is waiting 24 hours...", "no_return_player_steps", 15, -1);

		final var leaveNoReturn = Advancement.Builder.recipeAdvancement()
			.parent(noReturnsSteps)
			.display(
				Items.CORNFLOWER,
				Component.literal("Left global no return"),
				Component.literal("GG, you left the no return steps!"),
				AdvancementType.CHALLENGE,
				true,
				true,
				true
			)
			.addCriterion("leave_no_return", ModCriteria.ARBITRARY.createCriterion(new ArbitraryCriterion.Conditions(Optional.empty(), LEAVE_NO_RETURN)))
			.save(consumer, id("leave_no_return"));

		final var leaveNoReturnPlayer = Advancement.Builder.recipeAdvancement()
			.parent(noReturnPlayerSteps)
			.display(
				Items.CORNFLOWER,
				Component.literal("Left your player no return"),
				Component.literal("GG, you left the no return steps!"),
				AdvancementType.CHALLENGE,
				true,
				true,
				true
			)
			.addCriterion("leave_no_return_player", ModCriteria.ARBITRARY.createCriterion(new ArbitraryCriterion.Conditions(Optional.empty(), LEAVE_NO_RETURN_PLAYER)))
			.save(consumer, id("leave_no_return_player"));

		final var difficultyIncrease = Advancement.Builder.recipeAdvancement()
			.parent(ddsWorld)
			.display(
				Items.NETHERITE_SWORD,
				Component.literal("Difficulty is increasing"),
				Component.literal("You are too strong, you will pay for that."),
				AdvancementType.GOAL,
				true,
				true,
				true
			)
			.addCriterion("difficulty_increase", ModCriteria.ARBITRARY.createCriterion(new ArbitraryCriterion.Conditions(Optional.empty(), DIFFICULTY_INCREASE)))
			.save(consumer, id("difficulty_increase"));

		final var buffBoss = Advancement.Builder.recipeAdvancement()
			.parent(ddsWorld)
			.display(
				Items.NETHERITE_INGOT,
				Component.literal("Buff a boss"),
				Component.literal("Buff a boss with a netherite ingot."),
				AdvancementType.GOAL,
				true,
				true,
				false
			)
			.addCriterion("buff_boss", ModCriteria.ARBITRARY.createCriterion(new ArbitraryCriterion.Conditions(Optional.empty(), BUFF_BOSS)))
			.save(consumer, id("buff_boss"));
		final var buffWarden = createBoss(consumer, buffBoss, Items.SCULK_SHRIEKER, "Buff a warden", "Buff a warden with a netherite ingot", AdvancementType.GOAL, "buff_warden", Warden.class, false);
		final var killWarden = createBoss(consumer, buffWarden, Items.SCULK_SHRIEKER, "Kill a buffed warden", "Kill a buffed warden to decrease the difficulty", AdvancementType.CHALLENGE, "kill_warden", Warden.class, true);
		final var buffDragon = createBoss(consumer, buffBoss, Items.ENDER_EYE, "Buff the Ender Dragon", "Buff the Ender Dragon with a netherite ingot", AdvancementType.GOAL, "buff_dragon", EnderDragon.class, false);
		final var killDragon = createBoss(consumer, buffDragon, Items.ENDER_EYE, "Kill the buffed Ender Dragon", "Kill the buffed Ender Dragon to decrease the difficulty", AdvancementType.CHALLENGE, "kill_dragon", EnderDragon.class, true);
		final var buffWither = createBoss(consumer, buffBoss, Items.WITHER_SKELETON_SKULL, "Buff the Wither", "Buff the Wither with a netherite ingot", AdvancementType.GOAL, "buff_wither", WitherBoss.class, false);
		final var killWither = createBoss(consumer, buffWither, Items.WITHER_SKELETON_SKULL, "Kill the buffed Wither", "Kill the buffed Wither to decrease the difficulty", AdvancementType.CHALLENGE, "kill_wither", WitherBoss.class, true);
		final var buffElderGuardian = createBoss(consumer, buffBoss, Items.PRISMARINE, "Buff an elder guardian", "Buff an elder guardian with a netherite ingot", AdvancementType.GOAL, "buff_elder_guardian", ElderGuardian.class, false);
		final var killElderGuardian = createBoss(consumer, buffElderGuardian, Items.PRISMARINE, "Kill an elder guardian", "Kill an buffed elder guardian to decrease the difficulty", AdvancementType.CHALLENGE, "kill_elder_guardian", ElderGuardian.class, true);

		final var bountyBePresent = createBounty(consumer, ddsWorld, Items.DIAMOND_SWORD, "Bounty aware", "Be present during a bounty", AdvancementType.TASK, "bounty_be_present", BOUNTY_BE_PRESENT);
		final var bountyReceive = createBounty(consumer, bountyBePresent, Items.DIAMOND_CHESTPLATE, "Be hunted", "Receive a bounty on you", AdvancementType.GOAL, "bounty_receive", BOUNTY_RECEIVE);
		final var bountyKilled = createBounty(consumer, bountyReceive, Items.CHAINMAIL_CHESTPLATE, "Lose everything", "Killed for the reward", AdvancementType.GOAL, "bounty_killed", BOUNTY_KILLED);
		final var bountyKill = createBounty(consumer, bountyBePresent, Items.NETHERITE_SWORD, "Hunter", "Hunt for the bounty", AdvancementType.CHALLENGE, "bounty_kill", BOUNTY_KILLER);
	}

	private AdvancementHolder create(Consumer<AdvancementHolder> consumer, ReachDifficultyCriterion crit, AdvancementHolder parent, Item item, String title, String subtitle, String id, int min, int max) {
		return Advancement.Builder.recipeAdvancement()
			.parent(parent)
			.display(
				item,
				Component.literal(title),
				Component.literal(subtitle),
				AdvancementType.TASK,
				true,
				true,
				false
			)
			.addCriterion(id, crit.createCriterion(new ReachDifficultyCriterion.Conditions(Optional.empty(), min, max)))
			.save(consumer, id(id));
	}

	private AdvancementHolder createBoss(Consumer<AdvancementHolder> consumer, AdvancementHolder parent, Item item, String title, String subtitle, AdvancementType frame, String id, Class<? extends Entity> entity, boolean killed) {
		return Advancement.Builder.recipeAdvancement()
			.parent(parent)
			.display(
				item,
				Component.literal(title),
				Component.literal(subtitle),
				frame,
				true,
				true,
				false
			)
			.addCriterion(id, ModCriteria.BOSS.createCriterion(new BossCriterion.Conditions(Optional.empty(), entity.getName(), killed)))
			.save(consumer, id(id));
	}

	private AdvancementHolder createBounty(Consumer<AdvancementHolder> consumer, AdvancementHolder parent, Item item, String title, String subtitle, AdvancementType frame, String id, int type) {
		return Advancement.Builder.recipeAdvancement()
			.parent(parent)
			.display(
				item,
				Component.literal(title),
				Component.literal(subtitle),
				frame,
				true,
				true,
				false
			)
			.addCriterion(id, ModCriteria.BOUNTY.createCriterion(new BountyCriterion.Conditions(Optional.empty(), type)))
			.save(consumer, id(id));
	}
}
