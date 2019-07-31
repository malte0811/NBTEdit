package malte0811.nbtedit.nbt;

import malte0811.nbtedit.NBTEdit;
import malte0811.nbtedit.network.MessagePushNBT;
import malte0811.nbtedit.network.MessageRequestNBT;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class ClientProxy extends CommonProxy {
	private final Set<AutoPullConfig> autoPulls = Collections.newSetFromMap(new ConcurrentHashMap<>());
	private final Map<EditPosKey, Consumer<CompoundNBT>> WAITING = new HashMap<>();

	public ClientProxy() {
		FMLJavaModLoadingContext.get().getModEventBus().addListener(VanillaNBTProvider::clientChatEvent);
	}

	@Override
	public void requestNBT(EditPosKey k, @Nonnull Consumer<CompoundNBT> out) {
		NBTEdit.packetHandler.sendToServer(new MessageRequestNBT(k));
		WAITING.put(k, out);
	}

	@Override
	public void setNBT(EditPosKey k, CompoundNBT newNbt, CompoundNBT lastKnown) {
		NBTEdit.packetHandler.sendToServer(new MessagePushNBT(k, newNbt));
	}

	@Override
	public void cache(EditPosKey pos, CompoundNBT nbt) {
		if (WAITING.containsKey(pos)) {
			WAITING.remove(pos).accept(nbt);
		}
	}

	@Override
	public Set<AutoPullConfig> getAutoPulls() {
		return autoPulls;
	}
}
