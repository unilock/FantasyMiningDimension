package cc.unilock.fantasymining.data;

import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class PlayerData {
	public Vec3d homePos = Vec3d.ZERO;
	public Identifier homeWorld = World.OVERWORLD.getValue();
	public Vec3d miningPos = Vec3d.ZERO;
}
