package malte0811.nbtedit.api;

import net.minecraft.entity.Entity;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.entity.EntityType;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@SuppressWarnings({"WeakerAccess", "unused"})
public class API {
	private API() {
	}

	private static final Map<EntityType<?>, IEditHandler> entityHandlers = new ConcurrentHashMap<>();
	private static final Map<Class<? extends TileEntity>, IEditHandler> tileHandlers = new ConcurrentHashMap<>();

	public static void registerTileHandler(Class<? extends TileEntity> c, IEditHandler e) {
		if (!tileHandlers.containsKey(c)) {
			tileHandlers.put(c, e);
		} else {
			throw new IllegalArgumentException(c + "is already registered!");
		}
	}

	public static void registerEntityHandler(EntityType<?> c, IEditHandler e) {
		if (!entityHandlers.containsKey(c)) {
			entityHandlers.put(c, e);
		} else {
			throw new IllegalArgumentException(c + "is already registered!");
		}
	}

	//TODO readd class hierarchy things
	public static IEditHandler getEntityHandler(String e) {
		EntityType<?> type = ForgeRegistries.ENTITIES.getValue(new ResourceLocation(e));
		if (type == null)
			return null;
		return entityHandlers.get(type);
	}

	public static IEditHandler getTileHandler(String s) {
		TileEntityType<?> d = ForgeRegistries.TILE_ENTITIES.getValue(new ResourceLocation(s));
		if (d == null) {
			return null;
		}
		Class<?> clazz = d.create().getClass();
		IEditHandler forClass = null;
		while (TileEntity.class.isAssignableFrom(clazz) && forClass == null)
		{
			forClass = tileHandlers.get(clazz);
			if (forClass == null) {
				clazz = clazz.getSuperclass();
			}
		}
		return forClass;
	}

	public static IEditHandler get(CompoundNBT nbt) {
		if (nbt.contains("id")) {
			IEditHandler h = getTileHandler(nbt.getString("id"));
			if (h != null) {
				return h;
			} else {
				return getEntityHandler(nbt.getString("id"));
			}
		}
		return null;
	}
}
