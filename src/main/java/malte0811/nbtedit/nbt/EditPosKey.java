package malte0811.nbtedit.nbt;

import io.netty.buffer.ByteBuf;
import malte0811.nbtedit.api.ObjectType;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.ByteBufUtils;

import javax.annotation.Nonnull;
import java.util.UUID;

public class EditPosKey {
	@Nonnull
	public final UUID player;
	@Nonnull
	public final ObjectType type;
	//entity+TE
	public final int dim;
	//entity
	@Nonnull
	public final UUID entity;
	//TE
	@Nonnull
	public final BlockPos tilePos;
	//hand
	@Nonnull
	public final Hand hand;

	private static final Hand[] HANDS = Hand.values();

	public EditPosKey(@Nonnull UUID p, int dim, @Nonnull UUID eId) {
		player = p;
		entity = eId;
		this.dim = dim;
		tilePos = BlockPos.ZERO;
		type = ObjectType.ENTITY;
		hand = Hand.MAIN_HAND;
	}

	public EditPosKey(@Nonnull UUID player, int dim) {
		this.player = player;
		entity = player;
		this.dim = dim;
		tilePos = BlockPos.ZERO;
		type = ObjectType.PLAYER;
		hand = Hand.MAIN_HAND;
	}

	public EditPosKey(@Nonnull UUID p, int dim, @Nonnull BlockPos pos) {
		player = p;
		tilePos = pos;
		this.dim = dim;
		entity = UUID.fromString("00000000-0000-0000-0000-000000000000");
		type = ObjectType.TILEENTITY;
		hand = Hand.MAIN_HAND;
	}

	public EditPosKey(@Nonnull UUID p, @Nonnull Hand h) {
		player = p;
		tilePos = BlockPos.ZERO;
		dim = 0;
		entity = UUID.fromString("00000000-0000-0000-0000-000000000000");
		type = ObjectType.HAND;
		hand = h;
	}

	public static EditPosKey fromBytes(PacketBuffer pBuf) {
		UUID user = UUID.fromString(pBuf.readString(36));
		int d = pBuf.readInt();
		byte t = pBuf.readByte();
		ObjectType type = ObjectType.VALUES[t];
		switch (type) {
			case ENTITY:
			case PLAYER:
				UUID e = pBuf.readUniqueId();
				return new EditPosKey(user, d, e);
			case TILEENTITY:
				BlockPos pos = pBuf.readBlockPos();
				return new EditPosKey(user, d, pos);
			case HAND:
				byte h = pBuf.readByte();
				return new EditPosKey(user, HANDS[h]);
		}
		return null;
	}

	public void toBytes(PacketBuffer pBuf) {
		pBuf.writeString(player.toString());
		pBuf.writeInt(dim);
		pBuf.writeByte(type.ordinal());
		switch (type) {
			case ENTITY:
				//entity
				pBuf.writeUniqueId(entity);
				break;
			case TILEENTITY:
				//tile entity
				pBuf.writeBlockPos(tilePos);
				break;
			case HAND:
				pBuf.writeByte(hand.ordinal());
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + dim;
		result = prime * result + type.ordinal();
		result = prime * result + entity.hashCode();
		result = prime * result + player.hashCode();
		result = prime * result + tilePos.hashCode();
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		EditPosKey other = (EditPosKey) obj;
		if (type != other.type) {
			return false;
		}
		if (dim != other.dim)
			return false;
		if (!entity.equals(other.entity))
			return false;
		if (!player.equals(other.player))
			return false;
		if (!tilePos.equals(other.tilePos))
			return false;
		return true;
	}

}
