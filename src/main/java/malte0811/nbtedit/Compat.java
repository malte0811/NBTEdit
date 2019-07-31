package malte0811.nbtedit;


import blusunrize.immersiveengineering.common.blocks.TileEntityMultiblockPart;
import malte0811.nbtedit.api.API;
import malte0811.nbtedit.gui.NBTFrame;
import malte0811.nbtedit.nbt.EditPosKey;
import net.minecraftforge.fml.ModList;

import javax.swing.*;

class Compat {
	public static void registerHandlers() {
		if (ModList.get().isLoaded("immersiveengineering")) {
			//TODO IE currently isn't supported, since everything will be different in 1.14+
			//API.registerTileHandler(TileEntityMultiblockPart.class, (nbt, menu, frame)->{
    		//	int[] offset = nbt.getIntArray("offset");
    		//	if (offset.length==3&&(offset[0]!=0||offset[1]!=0||offset[2]!=0)) {
    		//		JMenuItem i = new JMenuItem("Change to master");
    		//		EditPosKey pOld = frame.editPos;
    		//		EditPosKey pNew = new EditPosKey(pOld.player, pOld.dim, pOld.tilePos.add(-offset[0], -offset[1], -offset[2]));
    		//		i.addActionListener((a)->{
    		//			frame.dispose();
    		//			new NBTFrame(pNew);
    		//		});
    		//		menu.add(i);
    		//	}
    		//});
		}
	}
}
