package malte0811.nbtedit.network;

import net.minecraft.client.Minecraft;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class MessageBlockUpdate {
	private BlockPos pos;

	public MessageBlockUpdate(BlockPos p) {
		pos = p;
	}

	public MessageBlockUpdate(PacketBuffer buf) {
		pos = buf.readBlockPos();
	}

	public void toBytes(PacketBuffer buf) {
		buf.writeBlockPos(pos);
	}

	public void onMessage(Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> Minecraft.getInstance().world.markForRerender(pos));
		ctx.get().setPacketHandled(true);
	}
}
