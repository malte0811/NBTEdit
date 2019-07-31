package malte0811.nbtedit.api;

import malte0811.nbtedit.nbt.EditPosKey;
import net.minecraft.nbt.CompoundNBT;

import javax.annotation.Nonnull;
import java.util.function.Consumer;

public interface INBTEditingProvider {
	void setNBT(EditPosKey k, CompoundNBT newNbt, CompoundNBT lastKnown);
	void requestNBT(EditPosKey k, @Nonnull Consumer<CompoundNBT> out);
	boolean supportsType(ObjectType type);
}
