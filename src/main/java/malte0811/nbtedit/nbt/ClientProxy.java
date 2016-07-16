package malte0811.nbtedit.nbt;

import java.util.HashMap;
import java.util.Map;

import malte0811.nbtedit.NBTEdit;
import malte0811.nbtedit.network.MessagePushNBT;
import malte0811.nbtedit.network.MessageRequestNBT;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;

public class ClientProxy extends CommonProxy {
	private Map<EditPosKey, NBTTagCompound> cache = new HashMap<>();
	@Override
	public NBTTagCompound getNBT(EditPosKey k, boolean sync) {
		if (sync) {
			if (Minecraft.getMinecraft().isSingleplayer())
				return super.getNBT(k, sync);
			NBTEdit.packetHandler.sendToServer(new MessageRequestNBT(k));
			try {
				synchronized (this) {
					while (!cache.containsKey(k)) {
						wait();
					}
				}
				return cache.get(k);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			return null;
		} else {
			return cache.get(k);
		}
	}
	@Override
	public void setNBT(EditPosKey k, NBTTagCompound newNbt) {
		NBTEdit.packetHandler.sendToServer(new MessagePushNBT(k, newNbt));
	}
	@Override
	public void cache(EditPosKey pos, NBTTagCompound nbt) {
		if (nbt!=null) {
			cache.put(pos, nbt);
		} else {
			cache.remove(pos);
		}
	}
	
}
