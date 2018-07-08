package malte0811.nbtedit.command;

import malte0811.nbtedit.NBTEdit;
import malte0811.nbtedit.nbt.EditPosKey;
import malte0811.nbtedit.network.MessageOpenWindow;
import malte0811.nbtedit.util.Utils;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.RayTraceResult.Type;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class CommandNbtEdit extends CommandBase {
	private static final String SELF = "self";
	private static final String HAND = "hand";
	private static final String HAND_MAIN = "main";
	private static final String HAND_OFF = "off";
	@Nonnull
	@Override
	public String getName() {
		return "nbtedit";
	}

	@Nonnull
	@Override
	public String getUsage(@Nonnull ICommandSender sender) {
		return "/nbtedit [<x> <y> <z> [<dim>]] or [hand [<off/main>]] or [self (experimental)]";
	}

	@Override
	public void execute(@Nonnull MinecraftServer s, @Nonnull ICommandSender sender, @Nonnull String[] args) throws CommandException {
		EntityPlayer player = getCommandSenderAsPlayer(sender);
		EditPosKey pos;
		if ((args.length == 1 || args.length == 2) && args[0].equalsIgnoreCase(HAND)) {
			EnumHand h = EnumHand.MAIN_HAND;
			if (args.length == 2) {
				switch (args[1].toLowerCase()) {
					case HAND_MAIN:
						h = EnumHand.MAIN_HAND;
						break;
					case HAND_OFF:
						h = EnumHand.OFF_HAND;
						break;
					default:
						throw new CommandException("nbtedit.invalid_hand", args[1]);
				}
			}
			pos = new EditPosKey(player.getUniqueID(), h);
		} else if (args.length == 1 && args[0].equalsIgnoreCase(SELF)) {
			pos = new EditPosKey(player.getUniqueID(), player.world.provider.getDimension(), player.getEntityId());
		} else if (args.length == 3 || args.length == 4) {
			BlockPos p = parseBlockPos(s, args, 0, false);
			int w = player.world.provider.getDimension();
			if (args.length == 4) {
				w = parseInt(args[3]);
			}
			pos = keyFromPos(p, player, w);
		} else {
			RayTraceResult mop = Utils.rayTrace(player);
			if (mop != null && mop.typeOfHit == Type.BLOCK) {
				BlockPos bPos = mop.getBlockPos();
				pos = keyFromPos(bPos, player, player.world.provider.getDimension());
			} else if (mop != null && mop.typeOfHit == Type.ENTITY) {
				Entity e = mop.entityHit;
				pos = new EditPosKey(player.getUniqueID(), e.world.provider.getDimension(), e.getEntityId());
			} else {
				throw new CommandException("nbtedit.no_object");
			}
		}
		NBTEdit.packetHandler.sendTo(new MessageOpenWindow(pos), (EntityPlayerMP) player);
	}

	private EditPosKey keyFromPos(BlockPos p, EntityPlayer player, int dimension) throws CommandException {
		if (player.world.getTileEntity(p) == null)
			throw new CommandException("nbtedit.no_te", p.getX(), p.getY(), p.getZ());
		return new EditPosKey(player.getUniqueID(), dimension, p);
	}

	@Nonnull
	@Override
	public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos) {
		List<String> ret = new ArrayList<>();
		if (args.length == 1) {
			ret.add(SELF);
			ret.add(HAND);
		} else if (args.length == 2 && args[0].equalsIgnoreCase(HAND)) {
			ret.add(HAND_MAIN);
			ret.add(HAND_OFF);
		}
		if (args.length == 1 || (args.length > 1 && args.length <= 3)) {
			try {
				if (args.length > 1) {
					Integer.parseInt(args[0]);
				}
				ret.addAll(getTabCompletionCoordinate(args, 0, targetPos));
			} catch (NumberFormatException x) {
			}
		}
		return getListOfStringsMatchingLastWord(args, ret);
	}
}
