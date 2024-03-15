package cc.unilock.fantasymining.data;

import cc.unilock.fantasymining.FantasyMining;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;

public class PlayerData {
	public Vec3d homePos = Vec3d.ZERO;
	public Identifier homeWorld = FantasyMining.id("impossible");
	public Vec3d miningPos = Vec3d.ZERO;
}
