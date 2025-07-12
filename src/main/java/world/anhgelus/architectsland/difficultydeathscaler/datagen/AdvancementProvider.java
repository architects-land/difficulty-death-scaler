package world.anhgelus.architectsland.difficultydeathscaler.datagen;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricAdvancementProvider;
import net.minecraft.advancement.Advancement;
import net.minecraft.advancement.AdvancementEntry;
import net.minecraft.advancement.AdvancementFrame;
import net.minecraft.advancement.criterion.TickCriterion;
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
    protected AdvancementProvider(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registryLookup) {
        super(output, registryLookup);
    }

    @Override
    public void generateAdvancement(RegistryWrapper.WrapperLookup wrapperLookup, Consumer<AdvancementEntry> consumer) {
        final var ddsWorld = Advancement.Builder.create()
                .display(
                        Items.GRASS_BLOCK,
                        Text.literal("Difficulty Death Scaler world"),
                        Text.literal("Welcome to a world governed by Difficulty Death Scaler!"),
                        Identifier.ofVanilla("textures/gui/advancements/backgrounds/adventure.png"),
                        AdvancementFrame.TASK,
                        true,
                        true,
                        false
                )
                .criterion("joined", TickCriterion.Conditions.createTick())
                .build(consumer, getId("dds_world"));

        final var easySteps = create(consumer, ddsWorld, Items.POPPY, "Easy steps reached!", "The world is easier than vanilla", "easy_steps", 0);
        final var ominousSteps = create(consumer, easySteps, Items.SHIELD, "Ominous steps reached!", "The world is becoming harder", "ominous_steps", 7);
        final var dangerousSteps = create(consumer, ominousSteps, Items.SKELETON_SKULL, "Dangerous steps reached!...", "The world wants to kill you", "dangerous_steps", 20);
        final var evilSteps = create(consumer, dangerousSteps, Items.WITHER_SKELETON_SKULL, "Evil steps reached...", "Please stops dying", "evil_steps", 28);
        final var noReturnsSteps = create(consumer, evilSteps, Items.WITHER_ROSE, "No return is possible now...", "Your nightmares are true...", "no_return_steps", 37);
    }

    private AdvancementEntry create(Consumer<AdvancementEntry> consumer, AdvancementEntry parent, Item item, String title, String subtitle, String id, int difficulty) {
        return Advancement.Builder.create()
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
                .criterion(id, ModCriteria.REACH_DIFFICULTY.create(new ReachDifficultyCriterion.Conditions(Optional.empty(), difficulty)))
                .build(consumer, getId(id));
    }

    private String getId(String id) {
        return String.format("%s:%s", DifficultyDeathScaler.MOD_ID, id);
    }
}
