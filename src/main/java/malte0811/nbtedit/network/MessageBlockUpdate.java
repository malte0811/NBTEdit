package malte0811.nbtedit.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class MessageBlockUpdate implements IMessage {
	private BlockPos pos;

	public MessageBlockUpdate(BlockPos p) {
		pos = p;
	}

	public MessageBlockUpdate() {
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		PacketBuffer b = new PacketBuffer(buf);
		pos = b.readBlockPos();
	}

	@Override
	public void toBytes(ByteBuf buf) {
		PacketBuffer b = new PacketBuffer(buf);
		b.writeBlockPos(pos);
	}

	public static class ClientHandler implements IMessageHandler<MessageBlockUpdate, IMessage> {
		@Override
		public IMessage onMessage(MessageBlockUpdate msg, MessageContext ctx) {
			Minecraft.getMinecraft().addScheduledTask(() -> {
				Minecraft.getMinecraft().world.markBlockRangeForRenderUpdate(msg.pos, msg.pos);
			});
			return null;
		}
	}
}
