package malte0811.nbtedit;

import org.apache.logging.log4j.Level;

import net.minecraftforge.fml.common.FMLLog;
import net.minecraftforge.fml.common.Loader;

public class Compat {
	public static void registerHandlers() {
		if (Loader.isModLoaded("ImmersiveEngineering")) {
//    		API.registerTileHandler(TileEntityMultiblockPart.class, (nbt, menu, frame)->{
//    			int[] offset = nbt.getIntArray("offset");
//    			if (offset!=null&&offset.length==3&&(offset[0]!=0||offset[1]!=0||offset[2]!=0)) {
//    				JMenuItem i = new JMenuItem("Change to master");
//    				EditPosKey pOld = frame.editPos;
//    				EditPosKey pNew = new EditPosKey(pOld.player, pOld.dim, pOld.tPos.add(-offset[0], -offset[1], -offset[2]));
//    				i.addActionListener((a)->{
//    					frame.dispose();
//    					new NBTFrame(pNew);
//    				});
//    				menu.add(i);
//    			}
//    		});
			FMLLog.log(NBTEdit.MODID, Level.WARN, "ImmersiveEngineering-compatibility is currently not implemented for 1.9+");
    	}
	}
}
