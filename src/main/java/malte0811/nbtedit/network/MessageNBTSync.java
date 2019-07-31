package malte0811.nbtedit.network;

import io.netty.buffer.ByteBuf;
import malte0811.nbtedit.NBTEdit;
import malte0811.nbtedit.nbt.EditPosKey;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraft.network.PacketBuffer;
import java.util.function.Supplier;

public class MessageNBTSync {
	private EditPosKey pos;
	private CompoundNBT value;

	public MessageNBTSync(EditPosKey k, CompoundNBT val) {
		pos = k;
		value = val;
	}

	public MessageNBTSync(PacketBuffer buf) {
		pos = EditPosKey.fromBytes(buf);
		if (buf.readBoolean()) {
			value = buf.readCompoundTag();
		}
	}

	public void toBytes(PacketBuffer buf) {
		pos.toBytes(buf);
		buf.writeBoolean(value != null);
		if (value != null) {
			buf.writeCompoundTag(value);
		}
	}

	public void onMessage(Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> NBTEdit.proxy.cache(pos, value));
		ctx.get().setPacketHandled(true);
	}
}
