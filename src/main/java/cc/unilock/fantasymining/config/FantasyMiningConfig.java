package cc.unilock.fantasymining.config;

import folk.sisby.kaleido.api.ReflectiveConfig;
import folk.sisby.kaleido.lib.quiltconfig.api.annotations.Comment;
import folk.sisby.kaleido.lib.quiltconfig.api.values.TrackedValue;

public class FantasyMiningConfig extends ReflectiveConfig {
	@Comment("Whether to place the player at the lowest possible point in the mining dimension upon their first teleport there.")
	@Comment("Otherwise they're placed at the highest possible point.")
	public final TrackedValue<Boolean> deep = value(true);
}
