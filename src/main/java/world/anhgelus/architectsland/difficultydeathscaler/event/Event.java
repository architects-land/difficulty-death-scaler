package world.anhgelus.architectsland.difficultydeathscaler.event;

import net.minecraft.server.MinecraftServer;
import net.minecraft.text.Text;

import java.util.Timer;

public enum Event {
    LONG_NIGHT("Polar night.", server -> {}, server -> {}, false),
    BRUTAL_HELL("Axes are wainting for you.", server -> {}, server -> {}),
    PHANTOMS_NIGHTMARE("It never ends.", server -> {}, server -> {}),
    RUN("Don't fight, run!", server -> {}, server -> {}),
    ANVIL_RAIN("It's raining.", server -> {}, server -> {});

    public interface On {
        void on(MinecraftServer server);
    }

    public final String description;
    public final boolean skipNight;
    public final On execStart;
    public final On execStop;

    private static final Timer timer = new Timer();

    Event(String description, On execStart, On execStop, boolean skipNight) {
        this.description = description;
        this.skipNight = skipNight;
        this.execStart = execStart;
        this.execStop = execStop;
    }
    Event(String description, On execStart, On execStop) {
        this.description = description;
        this.skipNight = true;
        this.execStart = execStart;
        this.execStop = execStop;
    }

    public void emit(MinecraftServer server) {
        //TODO: schedule new task with random delay between 2*60*1000 and 5*60*1000
        server.getPlayerManager().broadcast(Text.of(description), false);
        execStart.on(server);
        //TODO: schedule new task to stop event after 20*60*1000
        execStop.on(server);
    }

    public static void stop() {
        timer.cancel();
    }
}
