package world.anhgelus.architectsland.difficultydeathscaler.difficulty;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import world.anhgelus.architectsland.difficultydeathscaler.DifficultyDeathScaler;

import java.util.*;

public sealed interface DifficultyState<T extends DifficultyState<T>> {
	static ServerData get(MinecraftServer server) {
		return new ServerData(server);
	}

	static PlayerData get(ServerPlayer player) {
		return new ServerData(player.level().getServer()).getPlayer(player.getUUID());
	}

	T increaseDeath();

	int deaths();

	T deaths(int d);

	int deathsTotal();

	long timeBeforeReduce();

	T timeBeforeReduce(long t);

	long secondsLowerDifficulty();

	T secondsLowerDifficulty(long s);

	T increaseSecondsLowerDifficulty();

	record ServerData(MinecraftServer server) implements DifficultyState<ServerData> {
		private static final AttachmentType<Integer> DEATHS = AttachmentRegistry.createPersistent(
			DifficultyDeathScaler.id("deaths"),
			Codec.INT
		);
		private static final AttachmentType<Integer> DEATHS_TOTAL =
			AttachmentRegistry.createPersistent(
				DifficultyDeathScaler.id("deaths_total"),
				Codec.INT
			);
		private static final AttachmentType<Long> TIME_BEFORE_REDUCE =
			AttachmentRegistry.createPersistent(
				DifficultyDeathScaler.id("time_before_reduce"),
				Codec.LONG
			);
		private static final AttachmentType<Long> SECONDS_LOWER_DIFFICULTY =
			AttachmentRegistry.createPersistent(
				DifficultyDeathScaler.id("seconds_lower_difficulty"),
				Codec.LONG
			);
		private static final AttachmentType<Long> TIME_BEFORE_INCREASE =
			AttachmentRegistry.createPersistent(
				DifficultyDeathScaler.id("time_before_increase"),
				Codec.LONG
			);
		private static final AttachmentType<Boolean> INCREASE_ENABLED =
			AttachmentRegistry.createPersistent(
				DifficultyDeathScaler.id("increase_enabled"),
				Codec.BOOL
			);
		private static final AttachmentType<Map<String, PlayerData>> PLAYERS =
			AttachmentRegistry.create(
				DifficultyDeathScaler.id("players"),
				builder -> builder
					.initializer(HashMap::new)
					.persistent(Codec.unboundedMap(Codec.STRING, PlayerData.CODEC))
			);

		public PlayerData getPlayer(UUID uuid) {
			return server.globalAttachments()
				.getAttached(PLAYERS)
				.getOrDefault(uuid.toString(), new PlayerData());
		}

		public ServerData setPlayer(UUID uuid, PlayerData data) {
			server.globalAttachments()
				.getAttached(PLAYERS)
				.put(uuid.toString(), data);
			return this;
		}

		public Collection<Map.Entry<String, PlayerData>> getPlayers() {
			return server.globalAttachments().getAttached(PLAYERS).entrySet();
		}

		public long timeBeforeIncrease() {
			return server.globalAttachments().getAttachedOrElse(TIME_BEFORE_INCREASE, 0L);
		}

		public ServerData timeBeforeIncrease(long t) {
			server.globalAttachments().setAttached(TIME_BEFORE_INCREASE, t);
			return this;
		}

		public boolean increaseEnabled() {
			return server.globalAttachments().getAttachedOrElse(INCREASE_ENABLED, false);
		}

		public ServerData increaseEnabled(boolean b) {
			server.globalAttachments().setAttached(INCREASE_ENABLED, b);
			return this;
		}

		@Override
		public ServerData increaseDeath() {
			final var g = server.globalAttachments();
			g.modifyAttached(DEATHS_TOTAL, d -> d + 1);
			g.modifyAttached(DEATHS, d -> d + 1);
			return this;
		}

		@Override
		public int deaths() {
			return server.globalAttachments().getAttachedOrElse(DEATHS, 0);
		}

		@Override
		public ServerData deaths(int d) {
			server.globalAttachments().setAttached(DEATHS, d);
			return this;
		}

		@Override
		public int deathsTotal() {
			return server.globalAttachments().getAttachedOrElse(DEATHS_TOTAL, 0);
		}

		@Override
		public long timeBeforeReduce() {
			return server.globalAttachments().getAttachedOrElse(TIME_BEFORE_REDUCE, 0L);
		}

		@Override
		public ServerData timeBeforeReduce(long t) {
			server.globalAttachments().setAttached(TIME_BEFORE_REDUCE, t);
			return this;
		}

		@Override
		public long secondsLowerDifficulty() {
			return server.globalAttachments().getAttachedOrElse(SECONDS_LOWER_DIFFICULTY, 0L);
		}

		@Override
		public ServerData secondsLowerDifficulty(long s) {
			server.globalAttachments().setAttached(SECONDS_LOWER_DIFFICULTY, s);
			return this;
		}

		@Override
		public ServerData increaseSecondsLowerDifficulty() {
			server.globalAttachments().modifyAttached(SECONDS_LOWER_DIFFICULTY, s -> s + 1);
			return this;
		}
	}

	record PlayerData(
		int deaths,
		int deathsTotal,
		long timeBeforeReduce,
		long secondsLowerDifficulty,
		List<Long> deathDayEnd,
		int bonusHearts
	) implements DifficultyState<PlayerData> {
		private static final Codec<PlayerData> CODEC =
			RecordCodecBuilder.create(i -> i.group(
				Codec.INT.fieldOf("deaths").forGetter(PlayerData::deaths),
				Codec.INT.fieldOf("deaths_total").forGetter(PlayerData::deathsTotal),
				Codec.LONG.fieldOf("time_before_reduce").forGetter(PlayerData::timeBeforeReduce),
				Codec.LONG.fieldOf("seconds_lower_difficulty").forGetter(PlayerData::secondsLowerDifficulty),
				Codec.list(Codec.LONG).fieldOf("death_day_end").forGetter(PlayerData::deathDayEnd),
				Codec.INT.fieldOf("bonus_hearts").forGetter(PlayerData::bonusHearts)
			).apply(i, PlayerData::new));

		private PlayerData() {
			this(0, 0, 0, 0, List.of(), 0);
		}

		public List<Long> deathDayEnd() {
			return new ArrayList<>(deathDayEnd);
		}

		public PlayerData deathDayEnd(List<Long> ll) {
			return new PlayerData(deaths, deathsTotal, timeBeforeReduce, secondsLowerDifficulty,
				new ArrayList<>(ll), bonusHearts);
		}

		public PlayerData addDeathDayEnd(long t) {
			deathDayEnd.add(t);
			return this;
		}

		public PlayerData bonusHearts(int h) {
			return new PlayerData(deaths, deathsTotal, timeBeforeReduce, secondsLowerDifficulty,
				deathDayEnd, h);
		}

		public PlayerData increaseBonusHearts() {
			return new PlayerData(deaths, deathsTotal, timeBeforeReduce, secondsLowerDifficulty,
				deathDayEnd, bonusHearts + 1);
		}

		@Override
		public PlayerData increaseDeath() {
			return new PlayerData(deaths + 1, deathsTotal + 1, timeBeforeReduce,
				secondsLowerDifficulty, deathDayEnd, bonusHearts);
		}

		@Override
		public PlayerData deaths(int d) {
			return new PlayerData(d, deathsTotal, timeBeforeReduce, secondsLowerDifficulty,
				deathDayEnd, bonusHearts);
		}

		@Override
		public PlayerData timeBeforeReduce(long t) {
			return new PlayerData(deaths, deathsTotal, t, secondsLowerDifficulty, deathDayEnd,
				bonusHearts);
		}

		@Override
		public PlayerData secondsLowerDifficulty(long s) {
			return new PlayerData(deaths, deathsTotal, timeBeforeReduce, s, deathDayEnd,
				bonusHearts);
		}

		@Override
		public PlayerData increaseSecondsLowerDifficulty() {
			return new PlayerData(deaths, deathsTotal, timeBeforeReduce,
				secondsLowerDifficulty + 1, deathDayEnd, bonusHearts);
		}
	}
}
