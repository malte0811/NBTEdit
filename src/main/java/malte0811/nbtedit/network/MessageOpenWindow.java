package malte0811.nbtedit.network;

import io.netty.buffer.ByteBuf;
import malte0811.nbtedit.gui.NBTFrame;
import malte0811.nbtedit.nbt.EditPosKey;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class MessageOpenWindow implements IMessage {
	EditPosKey pos;

	public MessageOpenWindow(EditPosKey k) {
		pos = k;
	}

	public MessageOpenWindow() {
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		pos = EditPosKey.fromBytes(buf);
	}

	@Override
	public void toBytes(ByteBuf buf) {
		pos.toBytes(buf);
	}

	public static class ClientHandler implements IMessageHandler<MessageOpenWindow, IMessage> {
		@Override
		public IMessage onMessage(MessageOpenWindow msg, MessageContext ctx) {
			new Thread(new Runnable() {
				@Override
				public void run() {
					new NBTFrame(msg.pos);
				}
			}).start();
			return null;
		}
	}
}
