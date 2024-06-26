package cc.unilock.fantasymining.config;

import folk.sisby.kaleido.api.ReflectiveConfig;
import folk.sisby.kaleido.lib.quiltconfig.api.annotations.Comment;
import folk.sisby.kaleido.lib.quiltconfig.api.values.TrackedValue;

public class FantasyMiningConfig extends ReflectiveConfig {
	@Comment("Whether to place the player at the lowest possible point in the mining dimension upon their first teleport there.")
	@Comment("Otherwise, they're placed at the highest possible point.")
	public final TrackedValue<Boolean> deep = value(false);

	@Comment("Seed to use for the mining dimension.")
	@Comment("Set to 0 to use the overworld's seed.")
	public final TrackedValue<Long> seed = value(0L);

	@Comment("Whether to grant the player 10 seconds of Slow Falling after teleporting.")
	@Comment("Useful in case the surrounding environment has changed since the player was last there.")
	public final TrackedValue<Boolean> slow_falling = value(true);
}
