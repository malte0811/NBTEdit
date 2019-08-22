package malte0811.nbtedit.client;

import malte0811.nbtedit.NBTEdit;
import malte0811.nbtedit.nbt.AutoPullConfig;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.api.distmarker.Dist;

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
}
