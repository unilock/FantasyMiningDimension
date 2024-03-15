package cc.unilock.fantasymining.data;

import cc.unilock.fantasymining.FantasyMining;
import net.minecraft.datafixer.DataFixTypes;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtDouble;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateManager;

import java.util.HashMap;
import java.util.UUID;

public class StateManager extends PersistentState {
	private static final Type<StateManager> TYPE = new Type<>(
		StateManager::new,
		StateManager::createFromNbt,
		DataFixTypes.PLAYER // TODO?
	);

	private static final String PLAYERS_KEY = "Players";
	private static final String HOME_POS_KEY = "HomePos";
	private static final String HOME_WORLD_KEY = "HomeWorld";
	private static final String MINING_POS = "MiningPos";

	public HashMap<UUID, PlayerData> players = new HashMap<>();

	@Override
	public NbtCompound writeNbt(NbtCompound nbt) {
		NbtCompound playersNbt = new NbtCompound();
		players.forEach(((uuid, playerData) -> {
			NbtCompound playerNbt = new NbtCompound();

			playerNbt.put(HOME_POS_KEY, toNbtList(playerData.homePos));
			playerNbt.putString(HOME_WORLD_KEY, playerData.homeWorld.toString());
			playerNbt.put(MINING_POS, toNbtList(playerData.miningPos));

			playersNbt.put(uuid.toString(), playerNbt);
		}));
		nbt.put(PLAYERS_KEY, playersNbt);

		return nbt;
	}

	public static StateManager createFromNbt(NbtCompound nbt) {
		StateManager state = new StateManager();

		NbtCompound playersNbt = nbt.getCompound(PLAYERS_KEY);
		playersNbt.getKeys().forEach(key -> {
			NbtCompound playerNbt = playersNbt.getCompound(key);
			PlayerData playerData = new PlayerData();

			NbtList homePos = playerNbt.getList(HOME_POS_KEY, NbtElement.DOUBLE_TYPE);
			NbtList miningPos = playerNbt.getList(MINING_POS, NbtElement.DOUBLE_TYPE);

			playerData.homePos = fromNbtList(homePos);
			playerData.homeWorld = Identifier.tryParse(playerNbt.getString(HOME_WORLD_KEY));
			playerData.miningPos = fromNbtList(miningPos);

			state.players.put(UUID.fromString(key), playerData);
		});

		return state;
	}

	public static StateManager getServerState(MinecraftServer server) {
		PersistentStateManager persistentStateManager = server.getOverworld().getPersistentStateManager();

		StateManager state = persistentStateManager.getOrCreate(TYPE, FantasyMining.MOD_ID);
		state.markDirty();

		return state;
	}

	public static PlayerData getPlayerData(LivingEntity player) {
		StateManager state = getServerState(player.getServer());

		PlayerData playerData = state.players.computeIfAbsent(player.getUuid(), uuid -> new PlayerData());

        return playerData;
	}

	private static NbtList toNbtList(Vec3d vec3d) {
		NbtList list = new NbtList();

		for (double d : new double[]{vec3d.getX(), vec3d.getY(), vec3d.getZ()}) {
			list.add(NbtDouble.of(d));
		}

		return list;
	}

	private static Vec3d fromNbtList(NbtList list) {
		return new Vec3d(list.getDouble(0), list.getDouble(1), list.getDouble(2));
	}
}
