package malte0811.nbtedit.nbt;

import malte0811.nbtedit.NBTEdit;
import malte0811.nbtedit.network.MessagePushNBT;
import malte0811.nbtedit.network.MessageRequestNBT;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.client.ClientCommandHandler;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class ClientProxy extends CommonProxy {
	private final Set<AutoPullConfig> autoPulls = Collections.newSetFromMap(new ConcurrentHashMap<>());
	private final Map<EditPosKey, NBTTagCompound> cache = new ConcurrentHashMap<>();
	private final Map<EditPosKey, Consumer<NBTTagCompound>> WAITING = new HashMap<>();

	@Override
	public void requestNBT(EditPosKey k, boolean sync, @Nonnull Consumer<NBTTagCompound> out) {
		if (sync) {
			NBTEdit.packetHandler.sendToServer(new MessageRequestNBT(k));
			WAITING.put(k, out);
		} else {
			out.accept(cache.get(k));
		}
	}

	@Override
	public void setNBT(EditPosKey k, NBTTagCompound newNbt) {
		NBTEdit.packetHandler.sendToServer(new MessagePushNBT(k, newNbt));
	}

	@Override
	public void cache(EditPosKey pos, NBTTagCompound nbt) {
		if (nbt != null) {
			cache.put(pos, nbt);
		} else {
			cache.remove(pos);
		}
		if (WAITING.containsKey(pos)) {
			WAITING.remove(pos).accept(nbt);
		}
	}

	@Override
	public Set<AutoPullConfig> getAutoPulls() {
		return autoPulls;
	}

	public void registerClientCommands() {
		ClientCommandHandler.instance.registerCommand(NBTEdit.editNbt);
	}
}
