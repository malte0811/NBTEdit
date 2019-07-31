package malte0811.nbtedit.nbt;

import malte0811.nbtedit.api.INBTEditingProvider;
import malte0811.nbtedit.api.ObjectType;
import net.minecraft.nbt.CompoundNBT;

import javax.annotation.Nonnull;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

public class CommonProxy implements INBTEditingProvider {
	@Override
	public void setNBT(EditPosKey k, CompoundNBT newNbt, CompoundNBT lastKnown) {
	}

	@Override
	public void requestNBT(EditPosKey k, @Nonnull Consumer<CompoundNBT> out) {
	}

	@Override
	public boolean supportsType(ObjectType type) {
		return true;
	}


	public void cache(EditPosKey pos, CompoundNBT nbt) {
		throw new IllegalStateException("Can't be called on the server!");
	}

	public Set<AutoPullConfig> getAutoPulls() {
		return new HashSet<>();
	}

	public void openNBTWindow() {

	}
}
