package malte0811.nbtedit.network;

import org.apache.logging.log4j.Level;

import io.netty.buffer.ByteBuf;
import malte0811.nbtedit.NBTEdit;
import malte0811.nbtedit.nbt.EditPosKey;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.FMLLog;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class MessageRequestNBT implements IMessage {
	EditPosKey pos;
	
	public MessageRequestNBT(EditPosKey e) {
		pos = e;
	}
	
	public MessageRequestNBT() {}
	@Override
	public void fromBytes(ByteBuf buf) {
		pos = EditPosKey.fromBytes(buf);
	}

	@Override
	public void toBytes(ByteBuf buf) {
		pos.toBytes(buf);
	}
	public static class ServerHandler implements IMessageHandler<MessageRequestNBT, IMessage>
	{
		@Override
		public IMessage onMessage(MessageRequestNBT msg, MessageContext ctx)
		{
			if (NBTEdit.editNbt.checkPermission(ctx.getServerHandler().playerEntity.mcServer, ctx.getServerHandler().playerEntity)) {
				NBTTagCompound val = NBTEdit.commonProxyInstance.getNBT(msg.pos, false);
				return new MessageNBTSync(msg.pos, val);
			}
			FMLLog.log(NBTEdit.MODID, Level.ERROR, "Player "+ctx.getServerHandler().playerEntity.getDisplayNameString()+" tried to request NBT data from the server but isn't permitted to do so!");
			return null;
		}
	}
}
