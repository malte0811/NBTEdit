package malte0811.nbtedit.nbt;

import malte0811.nbtedit.NBTEdit;
import malte0811.nbtedit.api.INBTEditingProvider;
import malte0811.nbtedit.api.ObjectType;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.JsonToNBT;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.text.ChatType;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.api.distmarker.Dist;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import static malte0811.nbtedit.api.ObjectType.ENTITY;
import static malte0811.nbtedit.api.ObjectType.TILEENTITY;

@Mod.EventBusSubscriber(Dist.CLIENT)
public class VanillaNBTProvider implements INBTEditingProvider {

	private static final Queue<Consumer<CompoundNBT>> RECEIVERS = new ArrayDeque<>();

	private void generateDiff(CompoundNBT curr, CompoundNBT old, Consumer<String> remove, CompoundNBT add) {
		Set<String> keys = new HashSet<>(curr.keySet());
		keys.addAll(old.keySet());
		for (String key : keys) {
			if (curr.contains(key)) {
				if (old.contains(key)) {
					INBT oldTag = old.get(key);
					INBT newTag = curr.get(key);
					assert (oldTag != null && newTag != null);
					if (oldTag.getId() != newTag.getId()) {
						add.put(key, newTag);
					} else {
						byte i = oldTag.getId();
						if (i == Constants.NBT.TAG_COMPOUND) {
							CompoundNBT subDiff = new CompoundNBT();
							generateDiff((CompoundNBT) newTag, (CompoundNBT) oldTag,
								k -> remove.accept(key + "." + k),
								subDiff);
							if (!subDiff.isEmpty()) {
								add.put(key, subDiff);
							}
						} else if (!oldTag.equals(newTag)) {
							add.put(key, newTag);
						}
					}
				} else {
					add.put(key, curr.get(key));
				}
			} else {
				remove.accept("\"" + key + "\"");
			}
		}
	}

	@Override
	public void setNBT(EditPosKey k, CompoundNBT newNbt, CompoundNBT lastKnown) {
		CompoundNBT added = new CompoundNBT();
		final String type;
		if (k.type == TILEENTITY) {
			type = "block " + k.tilePos.getX() + " " + k.tilePos.getY() + " " + k.tilePos.getZ();
		} else {
			//TODO position/reference
			type = "entity";
		}
		String mergeCommand = "/data merge " + type + " ";
		String removeCommand = "/data remove " + type + " ";
		generateDiff(newNbt, lastKnown, (rem) ->
			Minecraft.getInstance().player.sendChatMessage(removeCommand + rem), added);
		if (!added.isEmpty()) {
			Minecraft.getInstance().player.sendChatMessage(mergeCommand + added);
		}
	}

	@Override
	public void requestNBT(EditPosKey k, @Nonnull Consumer<CompoundNBT> out) {
		RECEIVERS.offer(out);
		switch (k.type) {
			case TILEENTITY:
				Minecraft.getInstance().player.sendChatMessage("/data get block " + k.tilePos.getX() + " " + k.tilePos.getY() + " "
					+ k.tilePos.getZ());
				break;
			case ENTITY:
				Minecraft.getInstance().player.sendChatMessage("/data get entity " + k.entity);
				break;
		}
	}

	@Override
	public boolean supportsType(ObjectType type) {
		return type == ENTITY || type == TILEENTITY;
	}


	private static String getText(ITextComponent source) {
		StringBuilder ret = new StringBuilder(source.getUnformattedComponentText());
		for (ITextComponent sibling : source.getSiblings()) {
			ret.append(getText(sibling));
		}
		return ret.toString();
	}

	//"commands.data.block.query"
	//"commands.data.entity.query"
	@SubscribeEvent
	public static void clientChatEvent(ClientChatReceivedEvent ev) {
		if (false && ev.getType() == ChatType.SYSTEM && ev.getMessage() instanceof TranslationTextComponent) {
			TranslationTextComponent msg = (TranslationTextComponent) ev.getMessage();
			String key = msg.getKey();
			//TODO handle commands failing
			if (key.equals("commands.data.block.query")) {
				if (!RECEIVERS.isEmpty()) {
					Object[] params = msg.getFormatArgs();
					if (params.length == 4 && params[3] instanceof ITextComponent) {
						ITextComponent comp = (ITextComponent) params[3];
						String nbtString = getText(comp);
						try {
							CompoundNBT nbt = JsonToNBT.getTagFromJson(nbtString);
							RECEIVERS.poll().accept(nbt);
							ev.setCanceled(true);
						} catch (CommandSyntaxException e) {
							e.printStackTrace();
						}
					}
				}
			} else if (key.equals("commands.data.entity.query")) {
				NBTEdit.logger.error("Entity editing with the vanilla provider isn't supported yet");
			}
		}
	}
}
