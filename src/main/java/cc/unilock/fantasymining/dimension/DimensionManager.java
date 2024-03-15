package cc.unilock.fantasymining.dimension;

import cc.unilock.fantasymining.FantasyMining;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.world.dimension.DimensionType;
import xyz.nucleoid.fantasy.RuntimeWorldConfig;
import xyz.nucleoid.fantasy.RuntimeWorldHandle;

public class DimensionManager {
	private static final Identifier ID = FantasyMining.id("mining");
	private static final RegistryKey<DimensionType> TYPE = RegistryKey.of(RegistryKeys.DIMENSION_TYPE, ID);
	private static final RuntimeWorldConfig CONFIG = new RuntimeWorldConfig()
		.setGenerator(FantasyMining.getServer().getOverworld().getChunkManager().getChunkGenerator())
		.setDimensionType(TYPE)
		.setSeed(FantasyMining.getServer().getOverworld().getSeed());

	private static RuntimeWorldHandle handle = null;

	public static void start() {
		handle = FantasyMining.getFantasy().getOrOpenPersistentWorld(ID, CONFIG);
	}

	public static void stop() {
		handle.unload();
		handle = null;
	}

	public static RuntimeWorldHandle getHandle() {
		return handle;
	}
}
