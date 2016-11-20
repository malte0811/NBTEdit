package malte0811.nbtedit.gui;

import java.awt.Component;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;

import org.apache.commons.lang3.tuple.Pair;

import malte0811.nbtedit.NBTEdit;
import malte0811.nbtedit.nbt.NBTUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IResource;
import net.minecraft.nbt.NBTBase;
import net.minecraft.util.ResourceLocation;

public class NBTTreeCellRenderer extends DefaultTreeCellRenderer {

	private static final Icon[] nbtIcons = new Icon[NBTBase.NBT_TYPES.length];
	static {
		for (int i = 1;i<nbtIcons.length;i++) {
			try {
				IResource r = Minecraft.getMinecraft().getResourceManager().getResource(new ResourceLocation(NBTEdit.MODID, "icons/"+NBTBase.NBT_TYPES[i].replace("[]", "_array")+".png"));
				byte[] imageData = org.apache.commons.io.IOUtils.toByteArray(r.getInputStream());
				nbtIcons[i] = new ImageIcon(imageData);
			} catch (Exception x) {
				x.printStackTrace();
			}
		}
	}

	private static final long serialVersionUID = 1L;
	private Icon curr;
	@SuppressWarnings("unchecked")
	@Override
	public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded,
			boolean leaf, int row, boolean hasFocus) {
		curr = null;
		if (value instanceof DefaultMutableTreeNode){
			value = ((DefaultMutableTreeNode) value).getUserObject();
		}
		if (value instanceof Pair<?, ?>) {
			Pair<String, NBTBase> p = (Pair<String, NBTBase>) value;
			String nbtVal = NBTUtils.nbtToString(p.getRight());
			value = p.getLeft()+(nbtVal==null?"":(": "+nbtVal));
			curr = nbtIcons[p.getRight().getId()];
		}
		return super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
	}
	@Override
	public Icon getIcon() {
		return curr;
	}

}
