package malte0811.nbtedit.network;

import malte0811.nbtedit.NBTEdit;
import malte0811.nbtedit.nbt.EditPosKey;
import malte0811.nbtedit.util.Utils;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraft.network.PacketBuffer;

import java.util.function.Supplier;

public class MessageRequestNBT {
	private EditPosKey pos;

	public MessageRequestNBT(EditPosKey e) {
		pos = e;
	}

	public MessageRequestNBT(PacketBuffer buf) {
		pos = EditPosKey.fromBytes(buf);
	}

	public void toBytes(PacketBuffer buf) {
		pos.toBytes(buf);
	}

	public void onMessage(Supplier<NetworkEvent.Context> ctx) {
		ServerPlayerEntity player = ctx.get().getSender();
		if (player == null) {
			return;
		}
		MinecraftServer server = player.server;
		ctx.get().enqueueWork(() -> {
			if (player.hasPermissionLevel(2)) {
				CompoundNBT val = Utils.getNBTForPos(pos, server);
				NBTEdit.packetHandler.reply(new MessageNBTSync(pos, val), ctx.get());
			} else {
				NBTEdit.logger.error("Player " + player.getName().getUnformattedComponentText() +
					" tried to request NBT data from the server but isn't permitted to do so!");
			}
		});
		ctx.get().setPacketHandled(true);
	}
}
