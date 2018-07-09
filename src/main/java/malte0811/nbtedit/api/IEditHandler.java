package malte0811.nbtedit.api;

import malte0811.nbtedit.gui.NBTFrame;
import net.minecraft.nbt.NBTTagCompound;

import javax.swing.*;

public interface IEditHandler {
	void addMenuItems(NBTTagCompound nbt, JMenu j, NBTFrame frame);
}
