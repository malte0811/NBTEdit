package malte0811.nbtedit.nbt;

import malte0811.nbtedit.NBTEdit;
import malte0811.nbtedit.network.MessageBlockUpdate;
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

import javax.annotation.Nonnull;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

public class CommonProxy {
	public void requestNBT(EditPosKey k, boolean sync, @Nonnull Consumer<NBTTagCompound> out) {
	}

	public void setNBT(EditPosKey k, NBTTagCompound newNbt) {
		switch (k.type) {
			case ENTITY:
				assert k.ePos != null;
				Entity e = DimensionManager.getWorld(k.dim).getEntityByID(k.ePos);
				if (e!=null) {
					e.readFromNBT(newNbt);
				}
				break;
			case TILEENTITY:
				assert k.tPos != null;
				TileEntity te = DimensionManager.getWorld(k.dim).getTileEntity(k.tPos);
				if (te != null) {
					World w = te.getWorld();
					IBlockState state = w.getBlockState(k.tPos);
					te.readFromNBT(newNbt);
					te.markDirty();
					IBlockState newState = w.getBlockState(k.tPos);
					w.notifyBlockUpdate(k.tPos, state, state, 3);
					w.notifyNeighborsOfStateChange(k.tPos, newState.getBlock(), true);
					NBTEdit.packetHandler.sendToAllTracking(new MessageBlockUpdate(k.tPos),
							new TargetPoint(w.provider.getDimension(), k.tPos.getX(), k.tPos.getY(), k.tPos.getZ(), 0));
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

	public Set<AutoPullConfig> getAutoPulls() {
		return new HashSet<>();
	}

	public void registerClientCommands() {

	}
}
