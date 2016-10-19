package malte0811.nbtedit.nbt;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;

public class CommonProxy {
	public NBTTagCompound getNBT(EditPosKey k, boolean sync) {
		if (k.ePos!=null) {
			Entity e = DimensionManager.getWorld(k.dim).getEntityByID(k.ePos);
			if (e!=null) {
				return e.serializeNBT();
			}
		} else if (k.tPos!=null) {
			TileEntity te = DimensionManager.getWorld(k.dim).getTileEntity(k.tPos);
			if (te!=null) {
				return te.serializeNBT();
			}
		}
		return null;
	}
	public void setNBT(EditPosKey k, NBTTagCompound newNbt) {
		if (k.ePos!=null) {
			Entity e = DimensionManager.getWorld(k.dim).getEntityByID(k.ePos);
			if (e!=null) {
				e.readFromNBT(newNbt);
			}
		} else if (k.tPos!=null) {
			TileEntity te = DimensionManager.getWorld(k.dim).getTileEntity(k.tPos);
			if (te!=null) {
				World w = te.getWorld();
				IBlockState state = w.getBlockState(k.tPos);
				te.readFromNBT(newNbt);
				te.markDirty();
				IBlockState newState = w.getBlockState(k.tPos);
				w.notifyBlockUpdate(k.tPos,state,state,3);
				w.notifyNeighborsOfStateChange(k.tPos, newState.getBlock());
				w.markBlockRangeForRenderUpdate(k.tPos, k.tPos);
			}
		}
	}
	public void cache(EditPosKey pos, NBTTagCompound nbt) {
		throw new IllegalStateException("Can't be called on the server!!!!!");
	}
}
