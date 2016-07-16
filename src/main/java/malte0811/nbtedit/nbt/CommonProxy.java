package malte0811.nbtedit.nbt;

import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;

public class CommonProxy {
	public NBTTagCompound getNBT(EditPosKey k, boolean sync) {
		if (k.ePos!=null) {
			Entity e = MinecraftServer.getServer().worldServerForDimension(k.dim).getEntityByID(k.ePos);
			if (e!=null) {
				return e.serializeNBT();
			}
		} else if (k.tPos!=null) {
			TileEntity te = MinecraftServer.getServer().worldServerForDimension(k.dim).getTileEntity(k.tPos);
			if (te!=null) {
				return te.serializeNBT();
			}
		}
		return null;
	}
	public void setNBT(EditPosKey k, NBTTagCompound newNbt) {
		if (k.ePos!=null) {
			Entity e = MinecraftServer.getServer().worldServerForDimension(k.dim).getEntityByID(k.ePos);
			if (e!=null) {
				e.readFromNBT(newNbt);
			}
		} else if (k.tPos!=null) {
			TileEntity te = MinecraftServer.getServer().worldServerForDimension(k.dim).getTileEntity(k.tPos);
			if (te!=null) {
				te.readFromNBT(newNbt);
			}
		}
	}
	public void cache(EditPosKey pos, NBTTagCompound nbt) {
		throw new IllegalStateException("Can't be called on the server!!!!!");
	}
}
