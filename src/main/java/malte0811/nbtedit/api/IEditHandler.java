package malte0811.nbtedit.api;

import javax.swing.JMenu;

import malte0811.nbtedit.gui.NBTFrame;
import net.minecraft.nbt.NBTTagCompound;

public interface IEditHandler {
	public void addMenuItems(NBTTagCompound nbt, JMenu j, NBTFrame frame);
}
