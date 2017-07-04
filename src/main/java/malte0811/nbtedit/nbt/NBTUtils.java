package malte0811.nbtedit.nbt;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.nbt.*;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.common.network.ByteBufUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class NBTUtils {
	public static String nbtToString(NBTBase nbt) {
		String ret = null;
		switch (NBTBase.NBT_TYPES[nbt.getId()]) {//"BYTE", "SHORT", "INT", "LONG", "FLOAT", "DOUBLE", "BYTE[]", "STRING", "LIST", "COMPOUND", "INT[]"
			case "COMPOUND":
			case "LIST":
				break;
			case "BYTE":
				ret = Byte.toString(((NBTTagByte) nbt).getByte());
				break;
			case "SHORT":
				ret = Short.toString(((NBTTagShort) nbt).getShort());
				break;
			case "INT":
				ret = Integer.toString(((NBTTagInt) nbt).getInt());
				break;
			case "LONG":
				ret = Long.toString(((NBTTagLong) nbt).getLong());
				break;
			case "FLOAT":
				ret = Float.toString(((NBTTagFloat) nbt).getFloat());
				break;
			case "DOUBLE":
				ret = Double.toString(((NBTTagDouble) nbt).getDouble());
				break;
			case "BYTE[]":
				ret = byteArrayToString((((NBTTagByteArray) nbt).getByteArray()));
				break;
			case "STRING":
				ret = ((NBTTagString) nbt).getString();
				break;
			case "INT[]":
				ret = intArrayToString(((NBTTagIntArray) nbt).getIntArray());
				break;
		}
		return ret;
	}

	public static String byteArrayToString(byte[] inbs) {
		String ret = "";
		for (int i = 0; i < inbs.length; i++) {
			ret += (i == 0 ? "" : ";") + inbs[i];
		}
		return ret;
	}

	public static String intArrayToString(int[] in) {
		String ret = "";
		for (int i = 0; i < in.length; i++) {
			ret += (i == 0 ? "" : ";") + in[i];
		}
		return ret;
	}

	public static NBTBase stringToNbt(String in, NBTBase curr) {
		if (in == null)
			return curr;
		NBTBase ret = null;
		switch (NBTBase.NBT_TYPES[curr.getId()]) {//"BYTE", "SHORT", "INT", "LONG", "FLOAT", "DOUBLE", "BYTE[]", "STRING", "LIST", "COMPOUND", "INT[]"
			case "COMPOUND":
			case "LIST":
				break;
			case "BYTE":
				ret = new NBTTagByte(Byte.parseByte(in));
				break;
			case "SHORT":
				ret = new NBTTagShort(Short.parseShort(in));
				break;
			case "INT":
				ret = new NBTTagInt(Integer.parseInt(in));
				break;
			case "LONG":
				ret = new NBTTagLong(Long.parseLong(in));
				break;
			case "FLOAT":
				ret = new NBTTagFloat(Float.parseFloat(in));
				break;
			case "DOUBLE":
				ret = new NBTTagDouble(Double.parseDouble(in));
				break;
			case "BYTE[]":
				ret = new NBTTagByteArray(stringToByteArray(in));
				break;
			case "STRING":
				ret = new NBTTagString(in);
				break;
			case "INT[]":
				ret = new NBTTagIntArray(stringToIntArray(in));
				break;
		}
		return ret;
	}

	public static int[] stringToIntArray(String in) {
		String[] data = in.split(";");
		int[] ret = new int[data.length];
		for (int i = 0; i < data.length; i++) {
			ret[i] = Integer.parseInt(data[i]);
		}
		return ret;
	}

	public static byte[] stringToByteArray(String in) {
		String[] data = in.split(";");
		byte[] ret = new byte[data.length];
		for (int i = 0; i < data.length; i++) {
			ret[i] = Byte.parseByte(data[i]);
		}
		return ret;
	}

	public static void writeNBT(NBTTagCompound nbt, OutputStream out) throws IOException {
		PacketBuffer buffer = new PacketBuffer(Unpooled.buffer());
		ByteBufUtils.writeTag(buffer, nbt);
		out.write(buffer.array());
		out.flush();
	}

	public static NBTTagCompound readNBT(InputStream in) throws IOException {
		int read;
		ByteBuf bb = new PacketBuffer(Unpooled.buffer());
		while ((read = in.read()) != -1) {
			bb.writeByte(read);
		}
		return ByteBufUtils.readTag(bb);
	}
}
