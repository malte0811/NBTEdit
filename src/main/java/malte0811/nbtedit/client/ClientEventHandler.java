package malte0811.nbtedit.client;

import malte0811.nbtedit.NBTEdit;
import malte0811.nbtedit.nbt.AutoPullConfig;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;

public class ClientEventHandler {
	@SubscribeEvent
	public void onTick(TickEvent.ClientTickEvent event) {
		if (event.phase==Phase.END) {
			for (AutoPullConfig pull:NBTEdit.proxy.getAutoPulls()) {
				pull.counter++;
				if (pull.counter>=pull.delta) {
					pull.counter = 0;
					pull.frame.pullNbt();
				}
			}
		}
	}
}
