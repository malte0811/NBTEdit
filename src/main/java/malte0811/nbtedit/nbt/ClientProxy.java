package malte0811.nbtedit.nbt;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import malte0811.nbtedit.NBTEdit;
import malte0811.nbtedit.network.MessagePushNBT;
import malte0811.nbtedit.network.MessageRequestNBT;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class ClientProxy extends CommonProxy {
	private Set<AutoPullConfig> autoPulls = Collections.newSetFromMap(new ConcurrentHashMap<>());
	private Map<EditPosKey, NBTTagCompound> cache = new ConcurrentHashMap<>();
	private Set<EditPosKey> unread = new HashSet<>();
	@Override
	public NBTTagCompound getNBT(EditPosKey k, boolean sync) {
		if (sync) {
			if (Minecraft.getMinecraft().isSingleplayer())
				return super.getNBT(k, sync);
			NBTEdit.packetHandler.sendToServer(new MessageRequestNBT(k));
			try {
				synchronized (this) {
					while (!unread.contains(k)&&Minecraft.getMinecraft().world!=null) {
						wait(1000);
					}
				}
				unread.remove(k);
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
		unread.add(pos);
	}
	@Override
	public void syncNBT(EditPosKey pos, NBTTagCompound nbt) {
		World w = Minecraft.getMinecraft().world;
		TileEntity te = w.getTileEntity(pos.tPos);
		if (te!=null) {
			te.readFromNBT(nbt);
			w.markBlockRangeForRenderUpdate(pos.tPos, pos.tPos);
		}
	}
	@Override
	public Set<AutoPullConfig> getAutoPulls() {
		return autoPulls;
	}
}
