package malte0811.nbtedit.network;

import malte0811.nbtedit.NBTEdit;
import malte0811.nbtedit.gui.NBTFrame;
import malte0811.nbtedit.nbt.EditPosKey;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class MessageOpenWindow {
	private EditPosKey pos;

	public MessageOpenWindow(EditPosKey k) {
		pos = k;
	}

	public MessageOpenWindow(PacketBuffer buf) {
		pos = EditPosKey.fromBytes(buf);
	}

	public void toBytes(PacketBuffer buf) {
		pos.toBytes(buf);
	}

	public void onMessage(Supplier<NetworkEvent.Context> ctxGetter) {
		NetworkEvent.Context ctx = ctxGetter.get();
		new NBTFrame(pos);
		ctx.enqueueWork(
			() -> NBTEdit.proxy.openNBTWindow()
		);
		ctx.setPacketHandled(true);
	}
}
