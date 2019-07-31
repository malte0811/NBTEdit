package malte0811.nbtedit.gui;

import malte0811.nbtedit.NBTEdit;
import malte0811.nbtedit.api.API;
import malte0811.nbtedit.api.IEditHandler;
import malte0811.nbtedit.api.INBTEditingProvider;
import malte0811.nbtedit.client.NBTClipboard;
import malte0811.nbtedit.nbt.*;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.*;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.Constants.NBT;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nullable;
import javax.swing.*;
import javax.swing.GroupLayout.ParallelGroup;
import javax.swing.GroupLayout.SequentialGroup;
import javax.swing.filechooser.FileFilter;
import javax.swing.tree.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class NBTFrame extends JFrame {
	private static final long serialVersionUID = -8001779180003715111L;
	public final EditPosKey editPos;
	private CompoundNBT nbtRoot;
	private CompoundNBT lastSynced;
	private JTree tree;
	private JScrollPane scroll;
	private final JButton push = new JButton("Push NBT to world");
	private final JButton pull = new JButton("Pull NBT from world");
	private final JButton autoPull = new JButton("Enable auto pulling");
	private final JPanel panel = new JPanel();
	private JMenuBar bar;
	//TODO choosing from GUI
	private INBTEditingProvider provider = NBTEdit.proxy;//new VanillaNBTProvider();//

	public NBTFrame(EditPosKey pos) {
		super("NBTEdit");
		editPos = pos;
		add(panel);
		initGUI();
		pullNbt();
		push.addActionListener(e -> pushNbt());
		pull.addActionListener(e -> pullNbt());
		autoPull.addActionListener(e -> setupAutoPull());
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		addWindowListener(new CloseListener());
		setSize(500, 500);
		setVisible(true);
	}

	public void pullNbt() {
		provider.requestNBT(editPos, (nbt) -> {
			nbtRoot = nbt;
			lastSynced = nbtRoot!=null?nbtRoot.copy():null;
			SwingUtilities.invokeLater(this::updateNbt);
		});
	}

	private void initGUI() {
		tree = new JTree(new DefaultMutableTreeNode(new ImmutablePair<>("nbtroot", new CompoundNBT())));
		tree.setCellRenderer(new NBTTreeCellRenderer());

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
		i.addActionListener((a) ->
				loadFileToClipboard());
		menu.add(i);
		i = new JMenuItem("Save");
		i.setToolTipText("Save internal clipboard to .nbt file");
		i.addActionListener((a) ->
				saveClipboardToFile());
		menu.add(i);
		i = new JMenuItem("Delete");
		i.setToolTipText("Delete entry in the internal clipboard");
		i.addActionListener((a) ->
				deleteClipboardEntry());
		menu.add(i);

		bar.add(menu);
		setJMenuBar(bar);
	}

	private void updateNbt() {
		if (nbtRoot == null) {
			new Thread(() ->
			{
				Set<AutoPullConfig> configs = NBTEdit.proxy.getAutoPulls();
				AutoPullConfig tmp = new AutoPullConfig(this, 0);
				if (configs.contains(tmp)) {
					configs.remove(tmp);
					autoPull.setText("Enable auto pulling");
				}
				JOptionPane.showMessageDialog(this, "The object being edited was removed.");
			}).start();
			return;
		}
		DefaultMutableTreeNode node = genTreeFromNbt(nbtRoot);
		DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
		Enumeration<TreePath> expanded = tree.getExpandedDescendants(new TreePath(model.getRoot()));
		TreePath selected = tree.getSelectionPath();
		model.setRoot(node);
		updateExpansionAndSelection(expanded, selected, tree, scroll);

		IEditHandler h = API.get(nbtRoot);
		if (h != null) {
			JMenu menu = new JMenu("Extra");
			h.addMenuItems(nbtRoot, menu, this);
			if (menu.getItemCount() > 0) {
				for (int i = 0; i < bar.getMenuCount(); i++) {
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

	private void updateExpansionAndSelection(@Nullable Enumeration<TreePath> expanded, TreePath selected, JTree dest,
											 JScrollPane scroll) {
		Map<String, TreePath> destMap = new HashMap<>();
		buildMap((TreeNode) dest.getModel().getRoot(), null, destMap);
		if (expanded!=null) {
			while (expanded.hasMoreElements()) {
				TreePath curr = expanded.nextElement();
				TreePath dstVersion = destMap.get(stringFromPath(curr));
				if (dstVersion != null) {
					dest.expandPath(dstVersion);
				}
			}
		}
		if (selected != null) {
			dest.setSelectionPath(destMap.get(stringFromPath(selected)));
			dest.makeVisible(dest.getSelectionPath());
			if (dest.getSelectionPath() != null && dest.getPathBounds(dest.getSelectionPath()) != null) {
				Rectangle rect = dest.getPathBounds(dest.getSelectionPath());
				if (rect != null && !scroll.getViewport().getViewRect().contains(rect)) {
					scroll.getViewport().setViewPosition(new Point(0, 0));
					scroll.getViewport().scrollRectToVisible(rect);
				}
			}
		}
	}

	private void buildMap(TreeNode node, TreePath base, Map<String, TreePath> l) {
		base = base != null ? base.pathByAddingChild(node) : new TreePath(node);
		l.put(stringFromPath(base), base);
		if (node.getAllowsChildren()) {
			for (int i = 0; i < node.getChildCount(); i++) {
				buildMap(node.getChildAt(i), base, l);
			}
		}
	}

	private String stringFromPath(TreePath t) {
		StringBuilder ret = new StringBuilder();
		for (int i = 0; i < t.getPathCount(); i++) {
			ret.append(stringFromObject(t.getPathComponent(i))).append(";");
		}
		return ret.toString();
	}

	private DefaultMutableTreeNode genTreeFromNbt(CompoundNBT nbt) {
		DefaultMutableTreeNode root = new DefaultMutableTreeNode(new ImmutablePair<>("nbtroot", nbt));
		for (String k : nbt.keySet()) {
			INBT b = nbt.get(k);
			root.add(getNodeForBase(b, k));
		}
		return root;
	}

	private MutableTreeNode getNodeForBase(INBT nbt, String key) {
		String type = INBT.NBT_TYPES[nbt.getId()];
		switch (type) {
			case "COMPOUND":
				MutableTreeNode sub = genTreeFromNbt((CompoundNBT) nbt);
				sub.setUserObject(new ImmutablePair<>(key, nbt));
				return sub;
			case "LIST":
				DefaultMutableTreeNode list = new DefaultMutableTreeNode(new ImmutablePair<>(key, nbt));
				ListNBT l = (ListNBT) nbt;
				for (int i = 0; i < l.size(); i++) {
					list.add(getNodeForBase(l.get(i), Integer.toString(i)));
				}
				return list;
			default:
				return new DefaultMutableTreeNode(new ImmutablePair<>(key, nbt));
		}
	}

	//
	// Action listener methods
	//
	private void setupAutoPull() {
		Set<AutoPullConfig> configs = NBTEdit.proxy.getAutoPulls();
		AutoPullConfig tmp = new AutoPullConfig(this, 0);
		if (configs.contains(tmp)) {
			configs.remove(tmp);
			autoPull.setText("Enable auto pulling");
		} else {
			String in = JOptionPane.showInputDialog(this, "Ticks to wait between automatic pulls:", "10");
			if (in != null) {
				try {
					int delay = Integer.parseInt(in);
					if (delay <= 0)
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
	}

	private void edit(TreePath path) {
		if (path == null)
			return;
		INBT[] state = {nbtRoot, null};
		String key = findNbtInTree(path, state);
		INBT end = state[0];
		INBT parent = state[1];
		String init = NBTUtils.nbtToString(end);
		if (init != null) {
			String ret = JOptionPane.showInputDialog(this, "Please enter the new value", init);
			INBT nbtNew = NBTUtils.stringToNbt(ret, end);
			if (nbtNew != null) {
				if (parent instanceof CompoundNBT) {
					((CompoundNBT) parent).put(key, nbtNew);
				} else if (parent != null) {
					((ListNBT) parent).set(Integer.parseInt(key), nbtNew);
				}
				updateNbt();
			} else {
				JOptionPane.showMessageDialog(this, "The given input is invalid", "Invalid input!", JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	private String findNbtInTree(TreePath path, INBT[] state) {
		String key = null;
		for (int i = 1; i < path.getPathCount(); i++) {
			key = stringFromObject(path.getPathComponent(i));
			if (state[0] instanceof CompoundNBT) {
				state[1] = state[0];
				state[0] = ((CompoundNBT) state[0]).get(key);
			} else if (state[0] instanceof ListNBT) {
				state[1] = state[0];
				state[0] = ((ListNBT) state[0]).get(Integer.parseInt(key));
			}
		}
		return key;
	}

	private INBT get(TreePath t) {
		if (t == null)
			return null;
		INBT curr = nbtRoot;
		String key;
		for (int i = 1; i < t.getPathCount(); i++) {
			key = stringFromObject(t.getPathComponent(i));
			if (curr instanceof CompoundNBT) {
				curr = ((CompoundNBT) curr).get(key);
			} else if (curr instanceof ListNBT) {
				curr = ((ListNBT) curr).get(Integer.parseInt(key));
			}
		}
		return curr;
	}

	private void delete(TreePath path) {
		INBT[] state = {nbtRoot, null};
		String key = findNbtInTree(path, state);
		INBT parent = state[1];
		if (parent instanceof CompoundNBT) {
			((CompoundNBT) parent).remove(key);
		} else if (parent instanceof ListNBT) {
			((ListNBT) parent).remove(Integer.parseInt(key));
		}
		updateNbt();
	}

	private void copy(TreePath t) {
		INBT nbt = get(t);
		String name = JOptionPane.showInputDialog(this, "Name for this tag: ");
		if (name != null) {
			NBTClipboard.saveToClipboard(nbt, name);
		}
	}

	private void paste(TreePath t) {
		Map<String, INBT> map = NBTClipboard.getContent();
		if (map.size() == 0) {
			JOptionPane.showMessageDialog(this, "No clipboard entries found!");
		} else {
			String[] keys = map.keySet().toArray(new String[0]);
			String sel = (String) JOptionPane.showInputDialog(this, "Select which tag to paste: ", "", JOptionPane.INFORMATION_MESSAGE, null, keys, keys[0]);
			INBT nbt = map.get(sel).copy();
			INBT base = get(t);
			if (base instanceof CompoundNBT) {
				CompoundNBT into = (CompoundNBT) base;
				String name = JOptionPane.showInputDialog(this, "Select the name for the new tag: ");
				if (name != null) {
					into.put(name, nbt);
				}
			} else if (base instanceof ListNBT) {
				((ListNBT)base).add(nbt);
			}
			updateNbt();
		}
	}

	private void loadFileToClipboard() {
		JFileChooser choose = new JFileChooser(Minecraft.getInstance().gameDir);
		choose.setFileFilter(new FileFilter() {

			@Override
			public String getDescription() {
				return "*.nbt";
			}

			@Override
			public boolean accept(File f) {
				return f.isFile() && f.getName().endsWith(".nbt");
			}
		});
		int val = choose.showOpenDialog(this);
		if (val == JFileChooser.APPROVE_OPTION) {
			File in = choose.getSelectedFile();
			try {
				CompoundNBT nbt = NBTUtils.readNBT(new FileInputStream(in));
				String name = JOptionPane.showInputDialog(this, "Name of the clipboard: ");
				if (name != null) {
					NBTClipboard.saveToClipboard(nbt.get("content"), name);
				}
			} catch (IOException e) {
				JOptionPane.showMessageDialog(this, "Failed to load file. Check the log for details.");
				e.printStackTrace();
			}

		}
	}

	private void saveClipboardToFile() {
		Map<String, INBT> map = NBTClipboard.getContent();
		if (map.size() == 0) {
			JOptionPane.showMessageDialog(this, "No clipboard entries found!");
		} else {
			String[] keys = map.keySet().toArray(new String[0]);
			String sel = (String) JOptionPane.showInputDialog(this, "Select which tag to save: ", "", JOptionPane.INFORMATION_MESSAGE, null, keys, keys[0]);
			INBT nbtTmp = map.get(sel);
			String name = JOptionPane.showInputDialog(this, "Name of the file: ");
			if (name != null) {
				if (!name.endsWith(".nbt")) {
					name = name + ".nbt";
				}
				CompoundNBT nbt = new CompoundNBT();
				nbt.put("content", nbtTmp);
				File out = new File(Minecraft.getInstance().gameDir, name);
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
		Map<String, INBT> map = NBTClipboard.getContent();
		if (map.size() == 0) {
			JOptionPane.showMessageDialog(this, "No clipboard entries found!");
		} else {
			String[] keys = map.keySet().toArray(new String[0]);
			String sel = (String) JOptionPane.showInputDialog(this, "Select which tag to delete: ", "", JOptionPane.INFORMATION_MESSAGE, null, keys, keys[0]);
			if (sel != null) {
				NBTClipboard.deleteEntry(sel);
			}
		}
	}

	private void writeTag(CompoundNBT nbt) {
		String name = JOptionPane.showInputDialog(this, "Name of the file: ");
		if (name != null) {
			if (!name.endsWith(".nbt")) {
				name = name + ".nbt";
			}
			File out = new File(Minecraft.getInstance().gameDir, name);
			try {
				NBTUtils.writeNBT(nbt, new FileOutputStream(out));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private class NBTKeyListener extends KeyAdapter {

		@Override
		public void keyPressed(KeyEvent e) {
			if (e.getKeyCode() == KeyEvent.VK_ENTER) {
				edit(tree.getSelectionPath());
			} else if (e.getKeyCode() == KeyEvent.VK_DELETE) {
				delete(tree.getSelectionPath());
			}
		}
	}

	private class NBTMouseListener extends MouseAdapter {
		@Override
		public void mouseClicked(MouseEvent e) {
			if (e.getButton() == 1 && e.getClickCount() == 2) {
				edit(tree.getSelectionPath());
			} else if (e.getButton() == 3) {
				JPopupMenu menu = createMenu(tree.getSelectionPath());
				if (menu != null) {
					menu.show(NBTFrame.this, e.getX(), e.getY());
				}
			}
		}
	}

	private JPopupMenu createMenu(TreePath tp) {
		INBT nbt = get(tp);
		if (nbt == null)
			return null;
		JPopupMenu ret = new JPopupMenu();
		JMenuItem m = ret.add("Delete tag");
		m.addActionListener((a) ->
				delete(tp));
		m = ret.add("Copy tag");
		m.addActionListener((a) ->
				copy(tp));
		if (nbt instanceof CompoundNBT || nbt instanceof ListNBT) {
			m = ret.add("Paste tag");
			m.addActionListener((a) ->
					paste(tp));
		}
		if (nbt instanceof CompoundNBT) {
			m = ret.add("Write tag to file");
			m.addActionListener((a) ->
					writeTag((CompoundNBT) nbt));
			ret.addSeparator();
			for (int i = 1; i < INBT.NBT_TYPES.length; i++) {
				addAddOption(ret, i, nbt);
			}
		} else if (nbt instanceof ListNBT) {
			ret.addSeparator();
			int type = ((ListNBT) nbt).getTagType();
			if (type == 0) {
				for (int i = 1; i < INBT.NBT_TYPES.length; i++) {
					addAddOption(ret, i, nbt);
				}
			} else {
				addAddOption(ret, type, nbt);
			}
		}
		return ret;
	}

	private void addAddOption(JPopupMenu ret, int id, INBT nbt) {
		JMenuItem m;
		Consumer<INBT> set = element -> {
			if (nbt instanceof CompoundNBT) {
				String name = JOptionPane.showInputDialog(NBTFrame.this, "Name of the new entry:");
				if (name != null) {
					((CompoundNBT) nbt).put(name, element);
				}
			} else {
				assert nbt instanceof ListNBT : "nbt has to be a compound or a list";
				((ListNBT) nbt).add(element);
			}
			updateNbt();
		};
		switch (id) {
			case NBT.TAG_BYTE:
				m = ret.add("Add byte");
				m.addActionListener((a) ->
						set.accept(new ByteNBT((byte) 0)));
				break;
			case NBT.TAG_SHORT:
				m = ret.add("Add short");
				m.addActionListener((a) ->
						set.accept(new ShortNBT()));
				break;
			case NBT.TAG_INT:
				m = ret.add("Add int");
				m.addActionListener((a) ->
						set.accept(new IntNBT(0)));
				break;
			case NBT.TAG_LONG:
				m = ret.add("Add long");
				m.addActionListener((a) ->
						set.accept(new LongNBT(0)));
				break;
			case NBT.TAG_FLOAT:
				m = ret.add("Add float");
				m.addActionListener((a) ->
						set.accept(new FloatNBT(0)));
				break;
			case NBT.TAG_DOUBLE:
				m = ret.add("Add double");
				m.addActionListener((a) ->
						set.accept(new DoubleNBT(0)));
				break;
			case NBT.TAG_BYTE_ARRAY:
				m = ret.add("Add byte[]");
				m.addActionListener((a) ->
						set.accept(new ByteArrayNBT(new byte[]{1, 1, 2, 3, 5, 8})));
				break;
			case NBT.TAG_STRING:
				m = ret.add("Add String");
				m.addActionListener((a) ->
						set.accept(new StringNBT("")));
				break;
			case NBT.TAG_LIST:
				m = ret.add("Add ListNBT");
				m.addActionListener((a) ->
						set.accept(new ListNBT()));
				break;
			case NBT.TAG_COMPOUND:
				m = ret.add("Add CompoundNBT");
				m.addActionListener((a) ->
						set.accept(new CompoundNBT()));
				break;
			case NBT.TAG_INT_ARRAY:
				m = ret.add("Add int[]");
				m.addActionListener((a) ->
						set.accept(new IntArrayNBT(new int[]{1, 2, 3, 4, 5, 6})));
				break;
			case NBT.TAG_LONG_ARRAY:
				m = ret.add("Add long[]");
				m.addActionListener((a) ->
						set.accept(new LongArrayNBT(new long[]{1, 2, 3, 4, 5, 6})));
				break;
		}
	}

	private String stringFromObject(Object o) {
		if (o instanceof DefaultMutableTreeNode) {
			o = ((DefaultMutableTreeNode) o).getUserObject();
		}
		if (o instanceof Pair<?, ?>) {
			o = ((Pair<?, ?>) o).getLeft();
		}
		return o.toString();
	}

	private void pushNbt() {
		try {
			provider.setNBT(editPos, nbtRoot, lastSynced);
		} catch (RuntimeException x) {
			x.printStackTrace();
			JOptionPane.showMessageDialog(NBTFrame.this, x.getMessage(), "Exception!", JOptionPane.ERROR_MESSAGE);
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
