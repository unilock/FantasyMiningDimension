package cc.unilock.fantasymining;

import cc.unilock.fantasymining.command.MiningCommand;
import cc.unilock.fantasymining.dimension.DimensionManager;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;
import xyz.nucleoid.fantasy.Fantasy;

public class FantasyMining implements ModInitializer {
	public static final String MOD_ID = "fantasymining";
    //public static final Logger LOGGER = LoggerFactory.getLogger("Fantasy Mining Dimension");

	private static MinecraftServer server;

	@Override
	public void onInitialize() {
		ServerLifecycleEvents.SERVER_STARTED.register(mc -> {
			server = mc;
			DimensionManager.start();
		});
		ServerLifecycleEvents.SERVER_STOPPING.register(mc -> {
			DimensionManager.stop();
			server = null;
		});

		CommandRegistrationCallback.EVENT.register((dispatcher, access, environment) -> {
			MiningCommand.register(dispatcher);
		});
	}

	public static Identifier id(String path) {
		return new Identifier(FantasyMining.MOD_ID, path);
	}

	public static Fantasy getFantasy() {
		return Fantasy.get(server);
	}

	public static MinecraftServer getServer() {
		return server;
	}
}
