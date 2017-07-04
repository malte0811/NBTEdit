package malte0811.nbtedit.nbt;

import io.netty.buffer.ByteBuf;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.ByteBufUtils;

import java.util.UUID;

public class EditPosKey {
	public final UUID player;
	public final PosType type;
	//entity+TE
	public final int dim;
	//entity
	public final Integer ePos;
	//TE
	public final BlockPos tPos;
	//hand
	public final EnumHand hand;

	private static final EnumHand[] hands = EnumHand.values();

	public EditPosKey(UUID p, int dim, int eId) {
		player = p;
		ePos = eId;
		this.dim = dim;
		tPos = null;
		type = PosType.ENTITY;
		hand = EnumHand.MAIN_HAND;
	}

	public EditPosKey(UUID p, int dim, BlockPos pos) {
		player = p;
		tPos = pos;
		this.dim = dim;
		ePos = null;
		type = PosType.TILEENTITY;
		hand = EnumHand.MAIN_HAND;
	}

	public EditPosKey(UUID p, EnumHand h) {
		player = p;
		tPos = null;
		dim = 0;
		ePos = null;
		type = PosType.HAND;
		hand = h;
	}

	public static EditPosKey fromBytes(ByteBuf buf) {
		UUID user = UUID.fromString(ByteBufUtils.readUTF8String(buf));
		int d = buf.readInt();
		byte t = buf.readByte();
		PosType type = PosType.values[t];
		switch (type) {
			case ENTITY:
				int e = buf.readInt();
				return new EditPosKey(user, d, e);
			case TILEENTITY:
				int x = buf.readInt();
				int y = buf.readInt();
				int z = buf.readInt();
				return new EditPosKey(user, d, new BlockPos(x, y, z));
			case HAND:
				byte h = buf.readByte();
				return new EditPosKey(user, hands[h]);
		}
		return null;
	}

	public void toBytes(ByteBuf buf) {
		ByteBufUtils.writeUTF8String(buf, player.toString());
		buf.writeInt(dim);
		buf.writeByte(type.ordinal());
		switch (type) {
			case ENTITY:
				//entity
				buf.writeInt(ePos);
				break;
			case TILEENTITY:
				//tile entity
				buf.writeInt(tPos.getX());
				buf.writeInt(tPos.getY());
				buf.writeInt(tPos.getZ());
				break;
			case HAND:
				buf.writeByte(hand.ordinal());
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + dim;
		result = prime * result + type.ordinal();
		result = prime * result + ((ePos == null) ? 0 : ePos.hashCode());
		result = prime * result + ((player == null) ? 0 : player.hashCode());
		result = prime * result + ((tPos == null) ? 0 : tPos.hashCode());
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
		if (ePos == null) {
			if (other.ePos != null)
				return false;
		} else if (!ePos.equals(other.ePos))
			return false;
		if (player == null) {
			if (other.player != null)
				return false;
		} else if (!player.equals(other.player))
			return false;
		if (tPos == null) {
			if (other.tPos != null)
				return false;
		} else if (!tPos.equals(other.tPos))
			return false;
		return true;
	}

	public static enum PosType {
		TILEENTITY,
		ENTITY,
		HAND;
		public static final PosType[] values = values();
	}
}