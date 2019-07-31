package malte0811.nbtedit.api;

import malte0811.nbtedit.gui.NBTFrame;
import net.minecraft.nbt.CompoundNBT;

import javax.swing.*;

public interface IEditHandler {
	void addMenuItems(CompoundNBT nbt, JMenu j, NBTFrame frame);
}
