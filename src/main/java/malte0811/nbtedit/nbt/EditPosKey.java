package malte0811.nbtedit.nbt;

import java.util.UUID;

import io.netty.buffer.ByteBuf;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.ByteBufUtils;

public class EditPosKey {
	public final UUID player;
	public final int dim;
	public final Integer ePos;
	public final BlockPos tPos;
	public EditPosKey(UUID p, int dim, int eId) {
		player = p;
		ePos = eId;
		this.dim = dim;
		tPos = null;
	}
	public EditPosKey(UUID p, int dim, BlockPos pos) {
		player = p;
		tPos = pos;
		this.dim = dim;
		ePos = null;
	}
	public static EditPosKey fromBytes(ByteBuf buf) {
		UUID user = UUID.fromString(ByteBufUtils.readUTF8String(buf));
		int d = buf.readInt();
		if (buf.readBoolean()) {
			//entity
			int e = buf.readInt();
			return new EditPosKey(user, d, e);
		} else {
			//tile entity
			int x = buf.readInt();
			int y = buf.readInt();
			int z = buf.readInt();
			return new EditPosKey(user, d, new BlockPos(x, y, z));
		}
	}

	public void toBytes(ByteBuf buf) {
		ByteBufUtils.writeUTF8String(buf, player.toString());
		buf.writeInt(dim);
		buf.writeBoolean(ePos!=null);
		if (ePos!=null) {
			//entity
			buf.writeInt(ePos);
		} else {
			//tile entity
			buf.writeInt(tPos.getX());
			buf.writeInt(tPos.getY());
			buf.writeInt(tPos.getZ());
		}
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + dim;
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
}