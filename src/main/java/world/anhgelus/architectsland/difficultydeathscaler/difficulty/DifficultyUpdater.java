package world.anhgelus.architectsland.difficultydeathscaler.difficulty;

import world.anhgelus.architectsland.difficultydeathscaler.difficulty.modifier.Modifier;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DifficultyUpdater {
    private int difficultyLevel = 1;

    private final Map<Class<? extends Modifier<?>>, Modifier<?>> map = new HashMap<>();

    public void updateDifficulty(int level) {
        if (level > difficultyLevel) {
            difficultyLevel = level;
        }
    }

    public Modifier<?> getModifier(Class<? extends Modifier<?>> clazz) {
        var val = map.get(clazz);
        if (val != null) return val;
        try {
            val = clazz.getConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                 NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
        map.put(clazz, val);
        return val;
    }

    public List<Modifier<?>> getModifiers() {
        return new ArrayList<>(map.values());
    }

    public net.minecraft.world.Difficulty getDifficulty() {
        return switch (difficultyLevel) {
            case 0 -> net.minecraft.world.Difficulty.PEACEFUL;
            case 1 -> net.minecraft.world.Difficulty.EASY;
            case 2 -> net.minecraft.world.Difficulty.NORMAL;
            case 3 -> net.minecraft.world.Difficulty.HARD;
            default -> throw new IllegalArgumentException("Difficulty level out of range: " + difficultyLevel);
        };
    }
}
