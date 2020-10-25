package malte0811.nbtedit.network;

import malte0811.nbtedit.NBTEdit;
import malte0811.nbtedit.nbt.EditPosKey;
import malte0811.nbtedit.util.Utils;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraft.network.PacketBuffer;

import java.util.function.Supplier;

public class MessagePushNBT {
	private EditPosKey pos;
	private CompoundNBT value;

	public MessagePushNBT(EditPosKey k, CompoundNBT val) {
		pos = k;
		value = val;
	}

	public MessagePushNBT(PacketBuffer buf) {
		pos = EditPosKey.fromBytes(buf);
		value = buf.readCompoundTag();
	}

	public void toBytes(PacketBuffer buf) {
		pos.toBytes(buf);
		buf.writeCompoundTag(value);
	}

	public void onMessage(Supplier<NetworkEvent.Context> ctxSupplier) {
		NetworkEvent.Context ctx = ctxSupplier.get();
		ServerPlayerEntity player = ctx.getSender();
		if (player != null) {
			ctx.enqueueWork(() -> {
				if (player.hasPermissionLevel(2)) {
					Utils.setNBTAtPos(pos, value, player.server);
				} else {
					NBTEdit.logger.error("Player " + player.getName().getUnformattedComponentText() +
						" tried to push NBT data to the server but isn't permitted to do so!");
				}
			});
		}
		ctx.setPacketHandled(true);
	}
}
