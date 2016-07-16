package malte0811.nbtedit.api;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;

public class API {
	private API() {}
	private static Map<Class<? extends Entity>, IEditHandler> entityHandlers = new ConcurrentHashMap<>();
	private static Map<Class<? extends TileEntity>, IEditHandler> tileHandlers = new ConcurrentHashMap<>();
	public static void registerTileHandler(Class<? extends TileEntity> c, IEditHandler e) {
		if (!tileHandlers.containsKey(c)) {
			tileHandlers.put(c, e);
		} else {
			throw new IllegalArgumentException(c+"is already registered!");
		}
	}
	public static void registerEntityHandler(Class<? extends Entity> c, IEditHandler e) {
		if (!entityHandlers.containsKey(c)) {
			entityHandlers.put(c, e);
		} else {
			throw new IllegalArgumentException(c+"is already registered!");
		}
	}
	public static IEditHandler getEntityHandler(String e) {
		Class<?> c = EntityList.NAME_TO_CLASS.get(e);
		while (c!=null&&c!=Entity.class&&c!=Object.class) {
			if (entityHandlers.containsKey(c)) {
				return entityHandlers.get(c);
			}
			c = c.getSuperclass();
		}
		return null;
	}
	public static IEditHandler getTileHandler(String s) {
		Class<?> c = TileEntity.nameToClassMap.get(s);
		while (c!=null&&c!=Entity.class&&c!=Object.class) {
			if (tileHandlers.containsKey(c)) {
				return tileHandlers.get(c);
			}
			c = c.getSuperclass();
		}
		return null;
	}
	public static IEditHandler get(NBTTagCompound nbt) {
		if (nbt.hasKey("id")) {
			IEditHandler h = getTileHandler(nbt.getString("id"));
			if (h!=null) {
				return h;
			} else {
				return getEntityHandler(nbt.getString("id"));
			}
		}
		return null;
	}
}
