package malte0811.nbtedit.nbt;

import io.netty.buffer.ByteBuf;
import malte0811.nbtedit.api.ObjectType;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Hand;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.DimensionType;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.ByteBufUtils;

import javax.annotation.Nonnull;
import java.util.UUID;

public class EditPosKey {
	@Nonnull
	public final UUID player;
	@Nonnull
	public final ObjectType type;
	//entity+TE
	public final RegistryKey<World> dim;
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

	public EditPosKey(@Nonnull UUID p, RegistryKey<World> dim, @Nonnull UUID eId) {
		player = p;
		entity = eId;
		this.dim = dim;
		tilePos = BlockPos.ZERO;
		type = ObjectType.ENTITY;
		hand = Hand.MAIN_HAND;
	}

	public EditPosKey(@Nonnull UUID player, RegistryKey<World> dim) {
		this.player = player;
		entity = player;
		this.dim = dim;
		tilePos = BlockPos.ZERO;
		type = ObjectType.PLAYER;
		hand = Hand.MAIN_HAND;
	}

	public EditPosKey(@Nonnull UUID player, @Nonnull UUID target) {
		this.player = player; //command sender
		entity = target; //target player
		this.dim = World.OVERWORLD;
		tilePos = BlockPos.ZERO;
		type = ObjectType.PLAYER;
		hand = Hand.MAIN_HAND;
	}

	public EditPosKey(@Nonnull UUID p, RegistryKey<World> dim, @Nonnull BlockPos pos) {
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
		dim = World.OVERWORLD;
		entity = UUID.fromString("00000000-0000-0000-0000-000000000000");
		type = ObjectType.HAND;
		hand = h;
	}

	public static EditPosKey fromBytes(PacketBuffer pBuf) {
		UUID user = UUID.fromString(pBuf.readString(36));
		ResourceLocation dimensionName = new ResourceLocation(pBuf.readString(512));
		RegistryKey<World> dimension = RegistryKey.getOrCreateKey(Registry.WORLD_KEY, dimensionName);
		byte typeId = pBuf.readByte();
		ObjectType type = ObjectType.VALUES[typeId];
		switch (type) {
			case PLAYER:
				UUID p = pBuf.readUniqueId();
				return new EditPosKey(user, p);
			case ENTITY:
				UUID e = pBuf.readUniqueId();
				return new EditPosKey(user, dimension, e);
			case TILEENTITY:
				BlockPos pos = pBuf.readBlockPos();
				return new EditPosKey(user, dimension, pos);
			case HAND:
				byte h = pBuf.readByte();
				return new EditPosKey(user, HANDS[h]);
		}
		return null;
	}

	public void toBytes(PacketBuffer pBuf) {
		pBuf.writeString(player.toString());
		pBuf.writeString(dim.getLocation().toString());
		pBuf.writeByte(type.ordinal());
		switch (type) {
			case PLAYER:
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
		result = prime * result + dim.hashCode();
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
