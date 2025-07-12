package world.anhgelus.architectsland.difficultydeathscaler.datagen;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricAdvancementProvider;
import net.minecraft.advancement.Advancement;
import net.minecraft.advancement.AdvancementEntry;
import net.minecraft.advancement.AdvancementFrame;
import net.minecraft.advancement.criterion.TickCriterion;
import net.minecraft.entity.Entity;
import net.minecraft.entity.boss.WitherEntity;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.mob.ElderGuardianEntity;
import net.minecraft.entity.mob.WardenEntity;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import world.anhgelus.architectsland.difficultydeathscaler.DifficultyDeathScaler;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class AdvancementProvider extends FabricAdvancementProvider {
    public static final int LEAVE_NO_RETURN = 0;
    public static final int LEAVE_NO_RETURN_PLAYER = 1;
    public static final int DIFFICULTY_INCREASE = 2;
    public static final int BUFF_BOSS = 3;

    protected AdvancementProvider(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registryLookup) {
        super(output, registryLookup);
    }

    @Override
    public void generateAdvancement(RegistryWrapper.WrapperLookup wrapperLookup, Consumer<AdvancementEntry> consumer) {
        final var ddsWorld = Advancement.Builder.createUntelemetered()
                .display(
                        Items.GRASS_BLOCK,
                        Text.literal("Difficulty Death Scaler"),
                        Text.literal("Welcome to a world governed by Difficulty Death Scaler!"),
                        Identifier.of(""), // force the "no texture"
                        AdvancementFrame.TASK,
                        true,
                        true,
                        false
                )
                .criterion("joined", TickCriterion.Conditions.createTick())
                .build(consumer, getId("dds_world"));

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

        final var leaveNoReturn = Advancement.Builder.createUntelemetered()
                .parent(noReturnsSteps)
                .display(
                        Items.CORNFLOWER,
                        Text.literal("Left global no return"),
                        Text.literal("GG, you left the no return steps!"),
                        null,
                        AdvancementFrame.CHALLENGE,
                        true,
                        true,
                        true
                )
                .criterion("leave_no_return", ModCriteria.ARBITRARY.create(new ArbitraryCriterion.Conditions(Optional.empty(), LEAVE_NO_RETURN)))
                .build(consumer, getId("leave_no_return"));

        final var leaveNoReturnPlayer = Advancement.Builder.createUntelemetered()
                .parent(noReturnPlayerSteps)
                .display(
                        Items.CORNFLOWER,
                        Text.literal("Left your player no return"),
                        Text.literal("GG, you left the no return steps!"),
                        null,
                        AdvancementFrame.CHALLENGE,
                        true,
                        true,
                        true
                )
                .criterion("leave_no_return_player", ModCriteria.ARBITRARY.create(new ArbitraryCriterion.Conditions(Optional.empty(), LEAVE_NO_RETURN_PLAYER)))
                .build(consumer, getId("leave_no_return_player"));

        final var difficultyIncrease = Advancement.Builder.createUntelemetered()
                .parent(ddsWorld)
                .display(
                        Items.NETHERITE_SWORD,
                        Text.literal("Difficulty is increasing"),
                        Text.literal("You are too strong, you will pay for that."),
                        null,
                        AdvancementFrame.GOAL,
                        true,
                        true,
                        true
                )
                .criterion("difficulty_increase", ModCriteria.ARBITRARY.create(new ArbitraryCriterion.Conditions(Optional.empty(), DIFFICULTY_INCREASE)))
                .build(consumer, getId("difficulty_increase"));

        final var buffBoss = Advancement.Builder.createUntelemetered()
                .parent(ddsWorld)
                .display(
                        Items.NETHERITE_INGOT,
                        Text.literal("Buff a boss"),
                        Text.literal("Buff a boss with a netherite ingot."),
                        null,
                        AdvancementFrame.GOAL,
                        true,
                        true,
                        false
                )
                .criterion("buff_boss", ModCriteria.ARBITRARY.create(new ArbitraryCriterion.Conditions(Optional.empty(), BUFF_BOSS)))
                .build(consumer, getId("buff_boss"));
        final var buffWarden = createBoss(consumer, buffBoss, Items.SCULK_SHRIEKER, "Buff a warden", "Buff a warden with a netherite ingot", AdvancementFrame.GOAL, "buff_warden", WardenEntity.class, false);
        final var killWarden = createBoss(consumer, buffWarden, Items.SCULK_SHRIEKER, "Kill a buffed warden", "Kill a buffed warden to decrease the difficulty", AdvancementFrame.CHALLENGE, "kill_warden", WardenEntity.class, true);
        final var buffDragon = createBoss(consumer, buffBoss, Items.ENDER_EYE, "Buff the Ender Dragon", "Buff the Ender Dragon with a netherite ingot", AdvancementFrame.GOAL, "buff_dragon", EnderDragonEntity.class, false);
        final var killDragon = createBoss(consumer, buffDragon, Items.ENDER_EYE, "Kill the buffed Ender Dragon", "Kill the buffed Ender Dragon to decrease the difficulty", AdvancementFrame.CHALLENGE, "kill_dragon", EnderDragonEntity.class, true);
        final var buffWither = createBoss(consumer, buffBoss, Items.WITHER_SKELETON_SKULL, "Buff the Wither", "Buff the Wither with a netherite ingot", AdvancementFrame.GOAL, "buff_wither", WitherEntity.class, false);
        final var killWither = createBoss(consumer, buffWither, Items.WITHER_SKELETON_SKULL, "Kill the buffed Wither", "Kill the buffed Wither to decrease the difficulty", AdvancementFrame.CHALLENGE, "kill_wither", WitherEntity.class, true);
        final var buffElderGuardian = createBoss(consumer, buffBoss, Items.PRISMARINE, "Buff an elder guardian", "Buff an elder guardian with a netherite ingot", AdvancementFrame.GOAL, "buff_elder_guardian", ElderGuardianEntity.class, false);
        final var killElderGuardian = createBoss(consumer, buffElderGuardian, Items.PRISMARINE, "Kill an elder guardian", "Kill an buffed elder guardian to decrease the difficulty", AdvancementFrame.CHALLENGE, "kill_elder_guardian", ElderGuardianEntity.class, true);
    }

    private AdvancementEntry create(Consumer<AdvancementEntry> consumer, ReachDifficultyCriterion crit, AdvancementEntry parent, Item item, String title, String subtitle, String id, int min, int max) {
        return Advancement.Builder.createUntelemetered()
                .parent(parent)
                .display(
                        item,
                        Text.literal(title),
                        Text.literal(subtitle),
                        null,
                        AdvancementFrame.TASK,
                        true,
                        true,
                        false
                )
                .criterion(id, crit.create(new ReachDifficultyCriterion.Conditions(Optional.empty(), min, max)))
                .build(consumer, getId(id));
    }

    private AdvancementEntry createBoss(Consumer<AdvancementEntry> consumer, AdvancementEntry parent, Item item, String title, String subtitle, AdvancementFrame frame, String id, Class<? extends Entity> entity, boolean killed) {
        return Advancement.Builder.createUntelemetered()
                .parent(parent)
                .display(
                        item,
                        Text.literal(title),
                        Text.literal(subtitle),
                        null,
                        frame,
                        true,
                        true,
                        false
                )
                .criterion(id, ModCriteria.BOSS.create(new BossCriterion.Conditions(Optional.empty(), entity.getName(), killed)))
                .build(consumer, getId(id));
    }

    private String getId(String id) {
        return String.format("%s:%s", DifficultyDeathScaler.MOD_ID, id);
    }
}
