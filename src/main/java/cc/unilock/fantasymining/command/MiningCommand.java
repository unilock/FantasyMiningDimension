package cc.unilock.fantasymining.command;

import cc.unilock.fantasymining.FantasyMining;
import cc.unilock.fantasymining.data.PlayerData;
import cc.unilock.fantasymining.data.StateManager;
import cc.unilock.fantasymining.dimension.DimensionManager;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import net.fabricmc.fabric.api.dimension.v1.FabricDimensions;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.TeleportTarget;
import net.minecraft.world.chunk.WorldChunk;

import java.util.concurrent.CompletableFuture;

public class MiningCommand {
	public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
		dispatcher.register(CommandManager.literal("mining")
			.requires(src -> src.hasPermissionLevel(0))
			.executes(ctx -> {
				ServerCommandSource src = ctx.getSource();
				if (!src.isExecutedByPlayer()) {
					src.sendError(Text.literal("This command must be executed by a player."));
					return -1;
				}
				ServerPlayerEntity player = src.getPlayer();
				assert player != null;

				CompletableFuture.runAsync(() -> teleportSafePos(player));

				return Command.SINGLE_SUCCESS;
			}));
	}

	private static void teleportSafePos(ServerPlayerEntity player) {
		PlayerData data = StateManager.getPlayerData(player);

		final ServerWorld world;
		final Vec3d pos;

		// TODO: Surely there's a better way to do this?
		boolean useHomeWorld = true;
		boolean useSpawnWorld = false;
		boolean useOverWorld = false;
		boolean useHomePos = true;
		boolean useSpawnPos = false;
		boolean useOverPos = false;

		if (player.getServerWorld().getRegistryKey().equals(DimensionManager.getHandle().getRegistryKey())) {
			ServerWorld homeWorld = FantasyMining.getServer().getWorld(RegistryKey.of(RegistryKeys.WORLD, data.homeWorld));
			if (homeWorld != null) {
				world = homeWorld;
			} else {
				useHomeWorld = false;
				ServerWorld spawnWorld = FantasyMining.getServer().getWorld(player.getSpawnPointDimension());
				if (spawnWorld != null) {
					world = spawnWorld;
					useSpawnWorld = true;
				} else {
					world = FantasyMining.getServer().getOverworld();
					useOverWorld = true;
				}
			}

			if (data.homePos != Vec3d.ZERO) {
				pos = data.homePos;
			} else {
				useHomePos = false;
				if (player.getSpawnPointPosition() != null) {
					pos = player.getSpawnPointPosition().toCenterPos();
					useSpawnPos = true;
				} else {
					pos = FantasyMining.getServer().getOverworld().getSpawnPos().toCenterPos();
					useOverPos = true;
				}
			}

			if (!((useHomePos && useHomeWorld) || (useSpawnPos && useSpawnWorld) || (useOverPos && useOverWorld))) {
				throw new RuntimeException("[Fantasy Mining Dimension] Could not teleport player! No valid home position and world was found!");
			}

			data.miningPos = player.getPos();
		} else {
			world = DimensionManager.getHandle().asWorld();

			if (data.miningPos != Vec3d.ZERO) {
				pos = data.miningPos;
			} else {
				pos = getSafePos(world);
			}

			data.homePos = player.getPos();
			data.homeWorld = player.getServerWorld().getRegistryKey().getValue();
		}

		StateManager.getServerState(player.getServer()).markDirty();

		TeleportTarget target = new TeleportTarget(pos, Vec3d.ZERO, 0, 0);
		FabricDimensions.teleport(player, world, target);
	}

	private static Vec3d getSafePos(ServerWorld world) {
		WorldChunk chunk = world.getChunk(world.getRandom().nextBetween(-16, 16), world.getRandom().nextBetween(-16, 16));
		int min = world.getBottomY();
		int max = world.getHeight();

		BlockPos.Mutable pos = new BlockPos.Mutable();
		for (int y = min; y < max; y++) {
			for (int x = 0; x < 16; x++) {
				for (int z = 0; z < 16; z++) {
					pos.set(x, y, z);
					if (chunk.getBlockState(pos).isAir() && chunk.getBlockState(pos.up(1)).isAir() && chunk.getBlockState(pos.up(2)).isAir()) {
						BlockPos absolutePos = chunk.getPos().getStartPos().add(pos.getX(), pos.getY(), pos.getZ());
						return new Vec3d(absolutePos.getX() + 0.5, absolutePos.getY() + 1, absolutePos.getZ() + 0.5);
					}
				}
			}
		}

		return null;
	}
}
