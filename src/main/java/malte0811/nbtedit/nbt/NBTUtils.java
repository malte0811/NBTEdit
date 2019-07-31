package malte0811.nbtedit.nbt;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import malte0811.nbtedit.NBTEdit;
import net.minecraft.nbt.*;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import org.apache.logging.log4j.Level;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

public class NBTUtils {
	public static String nbtToString(INBT nbt) {
		String ret = null;
		switch (nbt.getId()) {
			case NBT.TAG_COMPOUND:
			case NBT.TAG_LIST:
				break;
			case NBT.TAG_BYTE:
				ret = Byte.toString(((ByteNBT) nbt).getByte());
				break;
			case NBT.TAG_SHORT:
				ret = Short.toString(((ShortNBT) nbt).getShort());
				break;
			case NBT.TAG_INT:
				ret = Integer.toString(((IntNBT) nbt).getInt());
				break;
			case NBT.TAG_LONG:
				ret = Long.toString(((LongNBT) nbt).getLong());
				break;
			case NBT.TAG_FLOAT:
				ret = Float.toString(((FloatNBT) nbt).getFloat());
				break;
			case NBT.TAG_DOUBLE:
				ret = Double.toString(((DoubleNBT) nbt).getDouble());
				break;
			case NBT.TAG_BYTE_ARRAY:
				ret = Arrays.toString((((ByteArrayNBT) nbt).getByteArray()));
				break;
			case NBT.TAG_STRING:
				ret = nbt.getString();
				break;
			case NBT.TAG_INT_ARRAY:
				ret = Arrays.toString(((IntArrayNBT) nbt).getIntArray());
				break;
			case NBT.TAG_LONG_ARRAY:
				ret = Arrays.toString(((LongArrayNBT) nbt).getAsLongArray());
				break;
		}
		return ret;
	}

	public static INBT stringToNbt(String in, INBT curr) {
		if (in == null)
			return curr;
		INBT ret = null;
		try {
			switch (curr.getId()) {
				case NBT.TAG_COMPOUND:
				case NBT.TAG_LIST:
					break;
				case NBT.TAG_BYTE:
					ret = new ByteNBT(Byte.parseByte(in));
					break;
				case NBT.TAG_SHORT:
					ret = new ShortNBT(Short.parseShort(in));
					break;
				case NBT.TAG_INT:
					ret = new IntNBT(Integer.parseInt(in));
					break;
				case NBT.TAG_LONG:
					ret = new LongNBT(Long.parseLong(in));
					break;
				case NBT.TAG_FLOAT:
					ret = new FloatNBT(Float.parseFloat(in));
					break;
				case NBT.TAG_DOUBLE:
					ret = new DoubleNBT(Double.parseDouble(in));
					break;
				case NBT.TAG_BYTE_ARRAY:
					ret = new ByteArrayNBT(stringToArray(in, Byte::parseByte));
					break;
				case NBT.TAG_STRING:
					ret = new StringNBT(in);
					break;
				case NBT.TAG_INT_ARRAY:
					ret = new IntArrayNBT(stringToArray(in, Integer::parseInt));
					break;
				case NBT.TAG_LONG_ARRAY:
					ret = new LongArrayNBT(stringToArray(in, Long::parseLong));
					break;

			}
		} catch (Exception x) {
			ret = null;
			NBTEdit.logger.catching(Level.WARN, x);
		}
		return ret;
	}

	private static <T> List<T> stringToArray(String in, Function<String, T> parse) {
		String[] data = in.split(", ");
		List<T> ret = new ArrayList<>(data.length);
		for (String aData : data) {
			ret.add(parse.apply(aData));
		}
		return ret;
	}

	public static void writeNBT(CompoundNBT nbt, OutputStream out) throws IOException {
		PacketBuffer buffer = new PacketBuffer(Unpooled.buffer());
		buffer.writeCompoundTag(nbt);
		out.write(buffer.array());
		out.flush();
	}

	public static CompoundNBT readNBT(InputStream in) throws IOException {
		int read;
		PacketBuffer bb = new PacketBuffer(Unpooled.buffer());
		while ((read = in.read()) != -1) {
			bb.writeByte(read);
		}
		return bb.readCompoundTag();
	}
}
