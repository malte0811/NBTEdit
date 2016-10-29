package malte0811.nbtedit.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.ParallelGroup;
import javax.swing.GroupLayout.SequentialGroup;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.filechooser.FileFilter;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import malte0811.nbtedit.NBTEdit;
import malte0811.nbtedit.api.API;
import malte0811.nbtedit.api.IEditHandler;
import malte0811.nbtedit.client.NBTClipboard;
import malte0811.nbtedit.nbt.AutoPullConfig;
import malte0811.nbtedit.nbt.EditPosKey;
import malte0811.nbtedit.nbt.NBTUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagByte;
import net.minecraft.nbt.NBTTagByteArray;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagDouble;
import net.minecraft.nbt.NBTTagFloat;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.nbt.NBTTagIntArray;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagLong;
import net.minecraft.nbt.NBTTagShort;
import net.minecraft.nbt.NBTTagString;

public class NBTFrame extends JFrame {
	private static final long serialVersionUID = -8001779180003715111L;
	public EditPosKey editPos;
	NBTTagCompound nbtRoot;
	JTree tree;
	JScrollPane scroll;
	JButton push = new JButton("Push NBT to world");
	JButton pull = new JButton("Pull NBT from world");
	JButton autoPull = new JButton("Enable auto pulling");
	JPanel panel = new JPanel();
	JMenuBar bar;
	public NBTFrame(EditPosKey pos) {
		super("NBTEdit");
		editPos = pos;
		add(panel);
		initGUI();
		pullNbt();
		push.addActionListener(new PushListener());
		pull.addActionListener(new PullListener());
		autoPull.addActionListener(this::setupAutoPull);
		addWindowListener(new CloseListener());
		setSize(500, 500);
		setVisible(true);
	}
	public void pullNbt() {
		nbtRoot = NBTEdit.proxy.getNBT(editPos, true);
		updateNbt();
	}
	private void initGUI() {
		tree = new JTree(new DefaultMutableTreeNode("nbtroot"));

		GroupLayout gl = new GroupLayout(panel);
		ParallelGroup hor = gl.createParallelGroup();
		SequentialGroup vert = gl.createSequentialGroup();
		scroll = new JScrollPane(tree);
		hor.addComponent(scroll);
		vert.addComponent(scroll);
		ParallelGroup vertInner = gl.createParallelGroup();
		SequentialGroup horInner = gl.createSequentialGroup();
		vertInner.addComponent(pull);
		horInner.addComponent(pull);
		vertInner.addComponent(push);
		horInner.addComponent(push);
		vertInner.addComponent(autoPull);
		horInner.addComponent(autoPull);

		hor.addGroup(horInner);
		vert.addGroup(vertInner);
		gl.setHorizontalGroup(hor);
		gl.setVerticalGroup(vert);
		tree.addMouseListener(new NBTMouseListener());
		tree.addKeyListener(new NBTKeyListener());
		panel.setLayout(gl);

		bar = new JMenuBar();
		JMenu menu = new JMenu("Clipboard");
		JMenuItem i = new JMenuItem("Load");
		i.setToolTipText("Load .nbt file to internal clipboard");
		i.addActionListener((a)->{loadFileToClipboard();});
		menu.add(i);
		i = new JMenuItem("Save");
		i.setToolTipText("Save internal clipboard to .nbt file");
		i.addActionListener((a)->{saveClipboardToFile();});
		menu.add(i);
		i = new JMenuItem("Delete");
		i.setToolTipText("Delete entry in the internal clipboard");
		i.addActionListener((a)->{deleteClipboardEntry();});
		menu.add(i);

		bar.add(menu);
		setJMenuBar(bar);
	}
	public void updateNbt() {
		if (nbtRoot==null) {
			JOptionPane.showMessageDialog(this, "The object being edited was removed.");
			return;
		}
		DefaultMutableTreeNode node = genTreeFromNbt(nbtRoot);
		DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
		Enumeration<TreePath> expanded = tree.getExpandedDescendants(new TreePath(model.getRoot()));
		TreePath selected = tree.getSelectionPath();
		model.setRoot(node);
		if (expanded!=null)
			updateExpansionAndSelection(expanded, selected, tree);

		IEditHandler h = API.get(nbtRoot);
		if (h!=null) {
			JMenu menu = new JMenu("Extra");
			h.addMenuItems(nbtRoot, menu, this);
			if (menu.getItemCount()>0) {
				for (int i = 0;i<bar.getMenuCount();i++) {
					if (bar.getMenu(i).getText().equals("Extra")) {
						bar.remove(i);
						i--;
					}
				}
				bar.add(menu);
				setJMenuBar(bar);
			}
		}
		this.repaint();
	}
	private void updateExpansionAndSelection(Enumeration<TreePath> expanded, TreePath selected, JTree dest) {
		Map<String, TreePath> destMap = new HashMap<>();
		buildMap((TreeNode) dest.getModel().getRoot(), null, destMap);
		while (expanded.hasMoreElements()) {
			TreePath curr = expanded.nextElement();
			TreePath dstVersion = destMap.get(curr.toString());
			if (dstVersion!=null) {
				dest.expandPath(dstVersion);
			}
		}
		if (selected!=null) {
			dest.setSelectionPath(destMap.get(selected.toString()));
		}
	}
	private void buildMap(TreeNode node, TreePath base, Map<String, TreePath> l) {
		base = base!=null?base.pathByAddingChild(node):new TreePath(node);
		l.put(base.toString(), base);
		if (node.getAllowsChildren()) {
			for (int i = 0;i<node.getChildCount();i++) {
				buildMap(node.getChildAt(i), base, l);
			}
		}
	}
	public DefaultMutableTreeNode genTreeFromNbt(NBTTagCompound nbt) {
		DefaultMutableTreeNode root = new DefaultMutableTreeNode("nbtroot");
		for (String k:nbt.getKeySet()) {
			NBTBase b = nbt.getTag(k);
			root.add(getNodeForBase(b, k));
		}
		return root;
	}
	private MutableTreeNode getNodeForBase(NBTBase nbt, String key) {
		String type = NBTBase.NBT_TYPES[nbt.getId()];
		switch (type) {
		case "COMPOUND":
			MutableTreeNode sub = genTreeFromNbt((NBTTagCompound)nbt);
			sub.setUserObject(key+":NBTTagCompound");
			return sub;
		case "LIST":
			DefaultMutableTreeNode list = new DefaultMutableTreeNode(key+":NBTTagList");
			NBTTagList l = (NBTTagList) nbt;
			for (int i = 0;i<l.tagCount();i++) {
				list.add(getNodeForBase(l.get(i), Integer.toString(i)));
			}
			return list;
		default:
			return new DefaultMutableTreeNode(key+":"+nbt);
		}
	}
	//
	// Action listener methods
	//
	private void setupAutoPull(ActionEvent e) {
		Set<AutoPullConfig> configs = NBTEdit.proxy.getAutoPulls();
		AutoPullConfig tmp = new AutoPullConfig(this, 0);
		if (configs.contains(tmp)) {
			configs.remove(tmp);
			autoPull.setText("Enable auto pulling");
		} else {
			String in = JOptionPane.showInputDialog(this, "Ticks to wait between automatic pulls:", "10");
			try {
				int delay = Integer.parseInt(in);
				if (delay<=0)
					throw new IllegalArgumentException("Delay must be greater than zero");
				AutoPullConfig real = new AutoPullConfig(this, delay);
				configs.add(real);
				autoPull.setText("Disable auto pulling");
			} catch (Exception x) {
				JOptionPane.showMessageDialog(this, "Failed to set up auto pulling. Check the log for details.");
				x.printStackTrace();
			}
		}
	}
	private void edit(TreePath t) {
		if (t==null)
			return;
		NBTBase curr = nbtRoot;
		NBTBase above = null;
		String key = null;
		for (int i = 1;i<t.getPathCount();i++) {
			String path = t.getPathComponent(i).toString();
			if (curr instanceof NBTTagCompound) {
				key = path.substring(0, path.indexOf(':'));
				above = curr;
				curr = ((NBTTagCompound) curr).getTag(key);
			} else if (curr instanceof NBTTagList) {
				key = path.substring(0, path.indexOf(':'));
				above = curr;
				curr = ((NBTTagList) curr).get(Integer.parseInt(key));
			}
		}
		String init = NBTUtils.nbtToString(curr);
		if (init!=null) {
			String ret = JOptionPane.showInputDialog(this, "Please enter the new value", init);
			NBTBase nbtNew = NBTUtils.stringToNbt(ret, curr);
			if (above instanceof NBTTagCompound) {
				((NBTTagCompound) above).setTag(key, nbtNew);
			} else if (above instanceof NBTTagList) {
				((NBTTagList) above).set(Integer.parseInt(key), nbtNew);
			}
			updateNbt();
		}
	}
	private NBTBase get(TreePath t) {
		if (t==null)
			return null;
		NBTBase curr = nbtRoot;
		String key = null;
		for (int i = 1;i<t.getPathCount();i++) {
			String path = t.getPathComponent(i).toString();
			if (curr instanceof NBTTagCompound) {
				key = path.substring(0, path.indexOf(':'));
				curr = ((NBTTagCompound) curr).getTag(key);
			} else if (curr instanceof NBTTagList) {
				key = path.substring(0, path.indexOf(':'));
				curr = ((NBTTagList) curr).get(Integer.parseInt(key));
			}
		}
		return curr;
	}
	private void delete(TreePath t) {
		NBTBase curr = nbtRoot;
		NBTBase above = null;
		String key = null;
		for (int i = 1;i<t.getPathCount();i++) {
			String path = t.getPathComponent(i).toString();
			if (curr instanceof NBTTagCompound) {
				key = path.substring(0, path.indexOf(':'));
				above = curr;
				curr = ((NBTTagCompound) curr).getTag(key);
			} else if (curr instanceof NBTTagList) {
				key = path.substring(0, path.indexOf(':'));
				above = curr;
				curr = ((NBTTagList) curr).get(Integer.parseInt(key));
			}
		}
		if (above instanceof NBTTagCompound) {
			((NBTTagCompound) above).removeTag(key);
		} else if (above instanceof NBTTagList) {
			((NBTTagList) above).removeTag(Integer.parseInt(key));
		}
		updateNbt();
	}
	private void copy(TreePath t) {
		NBTBase nbt = get(t);
		String name = JOptionPane.showInputDialog(this, "Name for this tag: ");
		if (name!=null) {
			NBTClipboard.saveToClipboard(nbt, name);
		}
	}
	private void paste(TreePath t) {
		Map<String, NBTBase> map = NBTClipboard.getContent();
		if (map.size()==0) {
			JOptionPane.showMessageDialog(this, "No clipboard entries found!");
		} else {
			String[] keys = map.keySet().toArray(new String[map.size()]);
			String sel = (String) JOptionPane.showInputDialog(this, "Select which tag to paste: ", "", JOptionPane.INFORMATION_MESSAGE, null, keys, keys[0]);
			NBTBase nbt = map.get(sel);
			NBTTagCompound into = (NBTTagCompound) get(t);
			String name = JOptionPane.showInputDialog(this, "Select the name for the new tag: ");
			if (name!=null) {
				into.setTag(name, nbt);
				updateNbt();
			}
		}
	}
	private void loadFileToClipboard() {
		JFileChooser choose = new JFileChooser(Minecraft.getMinecraft().mcDataDir);
		choose.setFileFilter(new FileFilter() {

			@Override
			public String getDescription() {
				return "*.nbt";
			}

			@Override
			public boolean accept(File f) {
				return f.isFile()&&f.getName().endsWith(".nbt");
			}
		});
		int val = choose.showOpenDialog(this);
		if (val==JFileChooser.APPROVE_OPTION) {
			File in = choose.getSelectedFile();
			try {
				NBTTagCompound nbt = NBTUtils.readNBT(new FileInputStream(in));
				String name = JOptionPane.showInputDialog(this, "Name of the clipboard: ");
				if (name!=null) {
					NBTClipboard.saveToClipboard(nbt.getTag("content"), name);
				}
			} catch (IOException e) {
				JOptionPane.showMessageDialog(this, "Failed to load file. Check the log for details.");
				e.printStackTrace();
			}

		}
	}
	private void saveClipboardToFile() {
		Map<String, NBTBase> map = NBTClipboard.getContent();
		if (map.size()==0) {
			JOptionPane.showMessageDialog(this, "No clipboard entries found!");
		} else {
			String[] keys = map.keySet().toArray(new String[map.size()]);
			String sel = (String) JOptionPane.showInputDialog(this, "Select which tag to save: ", "", JOptionPane.INFORMATION_MESSAGE, null, keys, keys[0]);
			NBTBase nbtTmp = map.get(sel);
			String name = JOptionPane.showInputDialog(this, "Name of the file: ");
			if (name!=null) {
				if (!name.endsWith(".nbt")) {
					name = name+".nbt";
				}
				NBTTagCompound nbt = new NBTTagCompound();
				nbt.setTag("content", nbtTmp);
				File out = new File(Minecraft.getMinecraft().mcDataDir, name);
				try {
					NBTUtils.writeNBT(nbt, new FileOutputStream(out));
				} catch (IOException e) {
					JOptionPane.showMessageDialog(this, "Failed to save to file. Check the log for details.");
					e.printStackTrace();
				}
			}
		}
	}
	private void deleteClipboardEntry() {
		Map<String, NBTBase> map = NBTClipboard.getContent();
		if (map.size()==0) {
			JOptionPane.showMessageDialog(this, "No clipboard entries found!");
		} else {
			String[] keys = map.keySet().toArray(new String[map.size()]);
			String sel = (String) JOptionPane.showInputDialog(this, "Select which tag to delete: ", "", JOptionPane.INFORMATION_MESSAGE, null, keys, keys[0]);
			if (sel!=null) {
				NBTClipboard.deleteEntry(sel);
			}
		}
	}

	private void writeTag(NBTTagCompound nbt) {
		String name = JOptionPane.showInputDialog(this, "Name of the file: ");
		if (name!=null) {
			if (!name.endsWith(".nbt")) {
				name = name+".nbt";
			}
			File out = new File(Minecraft.getMinecraft().mcDataDir, name);
			try {
				NBTUtils.writeNBT(nbt, new FileOutputStream(out));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private class NBTKeyListener extends KeyAdapter{

		@Override
		public void keyPressed(KeyEvent e) {
			if (e.getKeyCode()==KeyEvent.VK_ENTER) {
				edit(tree.getSelectionPath());
			} else if (e.getKeyCode()==KeyEvent.VK_DELETE) {
				delete(tree.getSelectionPath());
			}
		}
	}
	private class NBTMouseListener extends MouseAdapter{
		@Override
		public void mouseClicked(MouseEvent e) {
			if (e.getButton()==1&&e.getClickCount()==2) {
				edit(tree.getSelectionPath());
			} else if (e.getButton()==3) {
				JPopupMenu menu = createMenu(tree.getSelectionPath());
				if (menu!=null) {
					menu.show(NBTFrame.this, e.getX(), e.getY());
				}
			}
		}
	}
	private JPopupMenu createMenu(TreePath tp) {
		NBTBase nbt = get(tp);
		if (nbt==null)
			return null;
		JPopupMenu ret = new JPopupMenu();
		JMenuItem m = ret.add("Delete tag");
		m.addActionListener((a)->{delete(tp);});
		m = ret.add("Copy tag");
		m.addActionListener((a)->{copy(tp);});
		if (nbt instanceof NBTTagCompound) {
			m = ret.add("Paste tag");
			m.addActionListener((a)->{paste(tp);});
			m = ret.add("Write tag to file");
			m.addActionListener((a)->{writeTag((NBTTagCompound)nbt);});
			ret.addSeparator();
			for (int i = 1;i<NBTBase.NBT_TYPES.length;i++) {
				addAddOption(ret, i, nbt);
			}
		} else if (nbt instanceof NBTTagList) {
			ret.addSeparator();
			int type = ((NBTTagList) nbt).getTagType();
			if (type==0) {
				for (int i = 1;i<NBTBase.NBT_TYPES.length;i++) {
					addAddOption(ret, i, nbt);
				}
			} else {
				addAddOption(ret, type, nbt);
			}
		}
		return ret;
	}
	private void addAddOption(JPopupMenu ret, int id, NBTBase nbt) {
		JMenuItem m = null;
		switch (id) {
		case 1:
			m = ret.add("Add byte");
			m.addActionListener((a)->{
				if (nbt instanceof NBTTagCompound) {
					String name = JOptionPane.showInputDialog(NBTFrame.this, "Name of the new entry:");
					if (name!=null) {
						((NBTTagCompound)nbt).setByte(name, (byte)0);
					}
				} else {
					assert nbt instanceof NBTTagList:"nbt has to be a compound or a list";
				((NBTTagList)nbt).appendTag(new NBTTagByte((byte)0));
				}
				updateNbt();
			});
			break;
		case 2:
			m = ret.add("Add short");
			m.addActionListener((a)->{
				if (nbt instanceof NBTTagCompound) {
					String name = JOptionPane.showInputDialog(NBTFrame.this, "Name of the new entry:");
					if (name!=null) {
						((NBTTagCompound)nbt).setShort(name, (short)0);
					}
				} else {
					assert nbt instanceof NBTTagList:"nbt has to be a compound or a list";
				((NBTTagList)nbt).appendTag(new NBTTagShort((short)0));
				}
				updateNbt();
			});
			break;
		case 3:
			m = ret.add("Add int");
			m.addActionListener((a)->{
				if (nbt instanceof NBTTagCompound) {
					String name = JOptionPane.showInputDialog(NBTFrame.this, "Name of the new entry:");
					if (name!=null) {
						((NBTTagCompound)nbt).setInteger(name, 0);
					}
				} else {
					assert nbt instanceof NBTTagList:"nbt has to be a compound or a list";
				((NBTTagList)nbt).appendTag(new NBTTagInt(0));
				}
				updateNbt();
			});
			break;
		case 4:
			m = ret.add("Add long");
			m.addActionListener((a)->{
				if (nbt instanceof NBTTagCompound) {
					String name = JOptionPane.showInputDialog(NBTFrame.this, "Name of the new entry:");
					if (name!=null) {
						((NBTTagCompound)nbt).setLong(name, 0);
					}
				} else {
					assert nbt instanceof NBTTagList:"nbt has to be a compound or a list";
				((NBTTagList)nbt).appendTag(new NBTTagLong(0));
				}
				updateNbt();
			});
			break;
		case 5:
			m = ret.add("Add float");
			m.addActionListener((a)->{
				if (nbt instanceof NBTTagCompound) {
					String name = JOptionPane.showInputDialog(NBTFrame.this, "Name of the new entry:");
					if (name!=null) {
						((NBTTagCompound)nbt).setFloat(name, 0);
					}
				} else {
					assert nbt instanceof NBTTagList:"nbt has to be a compound or a list";
				((NBTTagList)nbt).appendTag(new NBTTagFloat(0));
				}
				updateNbt();
			});
			break;
		case 6:
			m = ret.add("Add double");
			m.addActionListener((a)->{
				if (nbt instanceof NBTTagCompound) {
					String name = JOptionPane.showInputDialog(NBTFrame.this, "Name of the new entry:");
					if (name!=null) {
						((NBTTagCompound)nbt).setDouble(name, 0);
					}
				} else {
					assert nbt instanceof NBTTagList:"nbt has to be a compound or a list";
				((NBTTagList)nbt).appendTag(new NBTTagDouble(0));
				}
				updateNbt();
			});
			break;
		case 7:
			m = ret.add("Add byte[]");
			m.addActionListener((a)->{
				if (nbt instanceof NBTTagCompound) {
					String name = JOptionPane.showInputDialog(NBTFrame.this, "Name of the new entry:");
					if (name!=null) {
						((NBTTagCompound)nbt).setByteArray(name, new byte[]{1, 1, 2, 3, 5, 8});
					}
				} else {
					assert nbt instanceof NBTTagList:"nbt has to be a compound or a list";
				((NBTTagList)nbt).appendTag(new NBTTagByteArray(new byte[]{1, 1, 2, 3, 5, 8}));
				}
				updateNbt();
			});
			break;
		case 8:
			m = ret.add("Add String");
			m.addActionListener((a)->{
				if (nbt instanceof NBTTagCompound) {
					String name = JOptionPane.showInputDialog(NBTFrame.this, "Name of the new entry:");
					if (name!=null) {
						((NBTTagCompound)nbt).setString(name, "");
					}
				} else {
					assert nbt instanceof NBTTagList:"nbt has to be a compound or a list";
				((NBTTagList)nbt).appendTag(new NBTTagString(""));
				}
				updateNbt();
			});
			break;
		case 9:
			m = ret.add("Add NBTTagList");
			m.addActionListener((a)->{
				if (nbt instanceof NBTTagCompound) {
					String name = JOptionPane.showInputDialog(NBTFrame.this, "Name of the new entry:");
					if (name!=null) {
						((NBTTagCompound)nbt).setTag(name, new NBTTagList());
					}
				} else {
					assert nbt instanceof NBTTagList:"nbt has to be a compound or a list";
				((NBTTagList)nbt).appendTag(new NBTTagList());
				}
				updateNbt();
			});
			break;
		case 10:
			m = ret.add("Add NBTTagCompound");
			m.addActionListener((a)->{
				if (nbt instanceof NBTTagCompound) {
					String name = JOptionPane.showInputDialog(NBTFrame.this, "Name of the new entry:");
					if (name!=null) {
						((NBTTagCompound)nbt).setTag(name, new NBTTagCompound());
					}
				} else {
					assert nbt instanceof NBTTagList:"nbt has to be a compound or a list";
				((NBTTagList)nbt).appendTag(new NBTTagCompound());
				}
				updateNbt();
			});
			break;
		case 11:
			m = ret.add("Add int[]");
			m.addActionListener((a)->{
				if (nbt instanceof NBTTagCompound) {
					String name = JOptionPane.showInputDialog(NBTFrame.this, "Name of the new entry:");
					if (name!=null) {
						((NBTTagCompound)nbt).setIntArray(name, new int[]{1, 1, 2, 3, 5, 8});
					}
				} else {
					assert nbt instanceof NBTTagList:"nbt has to be a compound or a list";
				((NBTTagList)nbt).appendTag(new NBTTagIntArray(new int[]{1, 1, 2, 3, 5, 8}));
				}
				updateNbt();
			});
		}
	}

	private class PushListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			try {
				NBTEdit.proxy.setNBT(editPos, nbtRoot);
			} catch (RuntimeException x) {
				x.printStackTrace();
				JOptionPane.showMessageDialog(NBTFrame.this, x.getMessage(), "Exception!", JOptionPane.ERROR_MESSAGE);
			}
		}

	}
	private class PullListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			try {
				pullNbt();
			} catch (RuntimeException x) {
				x.printStackTrace();
				JOptionPane.showMessageDialog(NBTFrame.this, x.getMessage(), "Exception!", JOptionPane.ERROR_MESSAGE);
			}
		}

	}
	private class CloseListener extends WindowAdapter {
		@Override
		public void windowClosed(WindowEvent e) {
			AutoPullConfig toRemove = new AutoPullConfig(NBTFrame.this, 0);
			NBTEdit.proxy.getAutoPulls().remove(toRemove);
		}
	}
}
