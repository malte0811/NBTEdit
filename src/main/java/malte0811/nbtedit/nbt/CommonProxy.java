package malte0811.nbtedit.nbt;

import java.util.HashSet;
import java.util.Set;

import malte0811.nbtedit.NBTEdit;
import malte0811.nbtedit.network.MessageNBTSync;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;

public class CommonProxy {
	public NBTTagCompound getNBT(EditPosKey k, boolean sync) {
		switch (k.type) {
		case ENTITY:	
			Entity e = DimensionManager.getWorld(k.dim).getEntityByID(k.ePos);
			if (e!=null) {
				return e.serializeNBT();
			}
			break;
		case TILEENTITY:
			TileEntity te = DimensionManager.getWorld(k.dim).getTileEntity(k.tPos);
			if (te!=null) {
				return te.serializeNBT();
			}
			break;
		case HAND:
			EntityPlayerMP player = FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().getPlayerByUUID(k.player);
			ItemStack stack = player.getHeldItem(k.hand);
			if (!stack.isEmpty())
				return stack.serializeNBT().copy();
		}
		return null;
	}
	public void setNBT(EditPosKey k, NBTTagCompound newNbt) {
		switch (k.type) {
		case ENTITY:
			Entity e = DimensionManager.getWorld(k.dim).getEntityByID(k.ePos);
			if (e!=null) {
				e.readFromNBT(newNbt);
			}
			break;
		case TILEENTITY:
			TileEntity te = DimensionManager.getWorld(k.dim).getTileEntity(k.tPos);
			if (te!=null) {
				World w = te.getWorld();
				IBlockState state = w.getBlockState(k.tPos);
				te.readFromNBT(newNbt);
				te.markDirty();
				IBlockState newState = w.getBlockState(k.tPos);
				w.notifyBlockUpdate(k.tPos,state,state,3);
				//TODO what is the last argument?
				w.notifyNeighborsOfStateChange(k.tPos, newState.getBlock(), true);
				NBTEdit.packetHandler.sendToAllAround(new MessageNBTSync(k, newNbt, false), new TargetPoint(k.dim, k.tPos.getX(), k.tPos.getY(), k.tPos.getZ(), 100));
			}
			break;
		case HAND:
			EntityPlayerMP player = FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().getPlayerByUUID(k.player);
			ItemStack stack = new ItemStack(newNbt);
			player.setHeldItem(k.hand, stack);
		}
	}
	public void cache(EditPosKey pos, NBTTagCompound nbt) {
		throw new IllegalStateException("Can't be called on the server!");
	}
	public void syncNBT(EditPosKey pos, NBTTagCompound nbt) {}
	public Set<AutoPullConfig> getAutoPulls() {
		return new HashSet<>();
	}
}
