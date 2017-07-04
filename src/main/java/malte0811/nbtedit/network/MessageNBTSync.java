package malte0811.nbtedit.network;

import io.netty.buffer.ByteBuf;
import malte0811.nbtedit.NBTEdit;
import malte0811.nbtedit.nbt.EditPosKey;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class MessageNBTSync implements IMessage {
	EditPosKey pos;
	NBTTagCompound value;
	boolean forCache = true;

	public MessageNBTSync(EditPosKey k, NBTTagCompound val, boolean cache) {
		pos = k;
		value = val;
		forCache = cache;
	}

	public MessageNBTSync() {
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		pos = EditPosKey.fromBytes(buf);
		if (buf.readBoolean()) {
			value = ByteBufUtils.readTag(buf);
			forCache = buf.readBoolean();
		}
	}

	@Override
	public void toBytes(ByteBuf buf) {
		pos.toBytes(buf);
		buf.writeBoolean(value != null);
		if (value != null) {
			ByteBufUtils.writeTag(buf, value);
			buf.writeBoolean(forCache);
		}
	}

	public static class ClientHandler implements IMessageHandler<MessageNBTSync, IMessage> {
		@Override
		public IMessage onMessage(MessageNBTSync msg, MessageContext ctx) {
			if (msg.forCache) {
				synchronized (NBTEdit.proxy) {
					NBTEdit.proxy.cache(msg.pos, msg.value);
					NBTEdit.proxy.notifyAll();
				}
			} else {
				NBTEdit.proxy.syncNBT(msg.pos, msg.value);
			}
			return null;
		}
	}
}
