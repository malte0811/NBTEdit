package malte0811.nbtedit.client;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import com.google.common.collect.ImmutableMap;

import malte0811.nbtedit.nbt.NBTUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;

public class NBTClipboard {
	private static Map<String, NBTBase> copied = new ConcurrentHashMap<>();
	public static void saveToClipboard(NBTBase val, String name) {
		copied.put(name, val);
		saveToDisc();
	}
	public static void deleteEntry(String keyToDelete) {
		copied.remove(keyToDelete);
	}
	public static Map<String, NBTBase> getContent() {
		return ImmutableMap.copyOf(copied);
	}
	public static NBTBase get(String key) {
		return copied.get(key);
	}
	private static void saveToDisc() {
		NBTTagCompound toWrite = new NBTTagCompound();
		for (Entry<String, NBTBase> b:copied.entrySet()) {
			toWrite.setTag(b.getKey(), b.getValue());
		}
		File out = new File(Minecraft.getMinecraft().mcDataDir, "NBTEdit.nbt");
		try {
			out.createNewFile();
			NBTUtils.writeNBT(toWrite, new FileOutputStream(out));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public static void readFromDisc() {
		File in = new File(Minecraft.getMinecraft().mcDataDir, "NBTEdit.nbt");
		if (in.exists()) {
			try {
				NBTTagCompound nbt = NBTUtils.readNBT(new FileInputStream(in));
				for (String k:nbt.getKeySet()) {
					copied.put(k, nbt.getTag(k));
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
