package malte0811.nbtedit.client;

import com.google.common.collect.ImmutableMap;
import malte0811.nbtedit.nbt.NBTUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.CompoundNBT;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

public class NBTClipboard {
	private static final Map<String, INBT> copied = new ConcurrentHashMap<>();

	public static void saveToClipboard(INBT val, String name) {
		copied.put(name, val.copy());
		saveToDisc();
	}

	public static void deleteEntry(String keyToDelete) {
		copied.remove(keyToDelete);
	}

	public static Map<String, INBT> getContent() {
		return ImmutableMap.copyOf(copied);
	}

	public static INBT get(String key) {
		return copied.get(key);
	}

	private static void saveToDisc() {
		CompoundNBT toWrite = new CompoundNBT();
		for (Entry<String, INBT> b : copied.entrySet()) {
			toWrite.put(b.getKey(), b.getValue());
		}
		File out = new File(Minecraft.getInstance().gameDir, "NBTEdit.nbt");
		try {
			out.createNewFile();
			NBTUtils.writeNBT(toWrite, new FileOutputStream(out));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void readFromDisc() {
		File in = new File(Minecraft.getInstance().gameDir, "NBTEdit.nbt");
		if (in.exists()) {
			try {
				CompoundNBT nbt = NBTUtils.readNBT(new FileInputStream(in));
				for (String k : nbt.keySet()) {
					copied.put(k, nbt.get(k));
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
