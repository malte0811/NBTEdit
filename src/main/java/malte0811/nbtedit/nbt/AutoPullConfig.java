package malte0811.nbtedit.nbt;

import malte0811.nbtedit.gui.NBTFrame;

public final class AutoPullConfig {
	public final NBTFrame frame;
	public int delta;
	public int counter;

	public AutoPullConfig(NBTFrame key, int d) {
		frame = key;
		delta = d;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((frame == null) ? 0 : frame.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof AutoPullConfig)) {
			return false;
		}
		AutoPullConfig other = (AutoPullConfig) obj;
		if (frame == null) {
			if (other.frame != null) {
				return false;
			}
		} else if (!frame.equals(other.frame)) {
			return false;
		}
		return true;
	}
}
