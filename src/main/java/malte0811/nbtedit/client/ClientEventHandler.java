package malte0811.nbtedit.client;

import malte0811.nbtedit.NBTEdit;
import malte0811.nbtedit.nbt.AutoPullConfig;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.api.distmarker.Dist;
import malte0811.nbtedit.command.CommandNbtEdit;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;

import java.util.Iterator;

@Mod.EventBusSubscriber(Dist.CLIENT)
public class ClientEventHandler {
	@SubscribeEvent
	public static void onTick(TickEvent.ClientTickEvent event) {
		if (event.phase == Phase.END) {
			for (Iterator<AutoPullConfig> iterator = NBTEdit.proxy.getAutoPulls().iterator(); iterator.hasNext(); ) {
				AutoPullConfig pull = iterator.next();
				if (!pull.frame.isVisible()) {
					iterator.remove();
				}
				pull.counter++;
				if (pull.counter >= pull.delta) {
					pull.counter = 0;
					pull.frame.pullNbt();
				}
			}
		}
	}

	@SubscribeEvent
	public static void serverStarted(FMLServerStartingEvent ev) {
		CommandNbtEdit.register(ev.getCommandDispatcher());
	}
}
