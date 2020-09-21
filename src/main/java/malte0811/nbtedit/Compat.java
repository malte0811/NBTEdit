package malte0811.nbtedit;


import blusunrize.immersiveengineering.common.blocks.generic.MultiblockPartTileEntity;
import malte0811.nbtedit.api.API;
import malte0811.nbtedit.gui.NBTFrame;
import malte0811.nbtedit.nbt.EditPosKey;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.fml.ModList;

import javax.swing.*;

class Compat {
	public static void registerHandlers() {
		if (ModList.get().isLoaded("immersiveengineering")) {
			API.registerTileHandler(MultiblockPartTileEntity.class, (nbt, menu, frame)->{
				BlockPos fromMaster = NBTUtil.readBlockPos(nbt.getCompound("offset"));
				if (!fromMaster.equals(BlockPos.ZERO)) {
					JMenuItem i = new JMenuItem("Change to master");
					EditPosKey pOld = frame.editPos;
					EditPosKey pNew = new EditPosKey(pOld.player, pOld.dim, pOld.tilePos.subtract(fromMaster));
					i.addActionListener((a)->{
						frame.dispose();
						new NBTFrame(pNew);
					});
					menu.add(i);
				}
			});
		}
	}
}
