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

				FantasyMining.getServer().execute(() -> teleportSafePos(player));

				return Command.SINGLE_SUCCESS;
			}));
	}

	// TODO: See PlayerManager#respawnPlayer
	private static void teleportSafePos(ServerPlayerEntity player) {
		PlayerData data = StateManager.getPlayerData(player);

		final ServerWorld world;
		final Vec3d pos;

		if (player.getServerWorld().getRegistryKey().equals(DimensionManager.getHandle().getRegistryKey())) {
			ServerWorld homeWorld = FantasyMining.getServer().getWorld(RegistryKey.of(RegistryKeys.WORLD, data.homeWorld));
			if (homeWorld != null) {
				world = homeWorld;
			} else {
				// This should fallback to World.OVERWORLD if not set, but we'll check it again anyway
				ServerWorld spawnWorld = FantasyMining.getServer().getWorld(player.getSpawnPointDimension());
				if (spawnWorld != null) {
					world = spawnWorld;
				} else {
					world = FantasyMining.getServer().getOverworld();
				}
			}

			if (data.homePos != Vec3d.ZERO) {
				pos = data.homePos;
			} else {
				if (player.getSpawnPointPosition() != null) {
					pos = player.getSpawnPointPosition().toCenterPos();
				} else {
					pos = FantasyMining.getServer().getOverworld().getSpawnPos().toCenterPos();
				}
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
		boolean deep = FantasyMining.CONFIG.deep.value();
		int min = world.getBottomY();
		int max = world.getHeight() - 1;

		BlockPos.Mutable pos = new BlockPos.Mutable();
		for (int y = (deep ? min : max); (deep ? y < max : y >= min); y = (deep ? y + 1 : y - 1)) {
			for (int x = 0; x < 16; x++) {
				for (int z = 0; z < 16; z++) {
					pos.set(x, y, z);
					if (chunk.getBlockState(pos).isAir() && chunk.getBlockState(pos.up()).isAir() && !chunk.getBlockState(pos.down()).isAir()) {
						BlockPos absolutePos = chunk.getPos().getStartPos().add(pos.getX(), pos.getY(), pos.getZ());
						return new Vec3d(absolutePos.getX() + 0.5, absolutePos.getY() + 1, absolutePos.getZ() + 0.5);
					}
				}
			}
		}

		return null;
	}
}
