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

public class CommandNbtEdit extends CommandBase {

	@Override
	public String getName() {
		return "nbtedit";
	}

	@Override
	public String getUsage(ICommandSender sender) {
		return "/nbtedit [<x> <y> <z> [<dim>]] or [hand [<off/main>]] or [self (experimental)]";
	}

	@Override
	public void execute(MinecraftServer s, ICommandSender sender, String[] args) throws CommandException {
		EntityPlayer player = getCommandSenderAsPlayer(sender);
		EditPosKey pos = null;
		RayTraceResult mop = Utils.rayTrace(player);
		if ((args.length == 1 || args.length == 2) && args[0].equalsIgnoreCase("hand")) {
			EnumHand h = EnumHand.MAIN_HAND;
			if (args.length == 2) {
				switch (args[1].toLowerCase()) {
					case "main":
						h = EnumHand.MAIN_HAND;
						break;
					case "off":
						h = EnumHand.OFF_HAND;
						break;
					default:
						throw new CommandException(args[1] + " is not a valid hand");
				}
			}
			pos = new EditPosKey(player.getUniqueID(), h);
		} else if (args.length==1&&args[0].equalsIgnoreCase("self")) {
			pos = new EditPosKey(player.getUniqueID(), player.world.provider.getDimension(), player.getEntityId());
		} else if (args.length == 3 || args.length == 4) {
			BlockPos p = parseBlockPos(s, args, 0, false);
			int w = player.world.provider.getDimension();
			if (args.length == 4) {
				w = parseInt(args[3]);
			}
			pos = keyFromPos(p, player, w);
		} else if (mop != null && mop.typeOfHit == Type.BLOCK) {
			BlockPos bPos = mop.getBlockPos();
			pos = keyFromPos(bPos, player, player.world.provider.getDimension());
		} else if (mop != null && mop.typeOfHit == Type.ENTITY) {
			Entity e = mop.entityHit;
			pos = new EditPosKey(player.getUniqueID(), e.world.provider.getDimension(), e.getEntityId());
		} else {
			throw new CommandException("No object found for editing");
		}
		if (pos != null) {
			NBTEdit.packetHandler.sendTo(new MessageOpenWindow(pos), (EntityPlayerMP) player);
		}
	}

	private EditPosKey keyFromPos(BlockPos p, EntityPlayer player, int dimension) throws CommandException {
		if (player.world.getTileEntity(p) == null)
			throw new CommandException("No TileEntity found at {" + p.getX() + ", " + p.getY() + ", " + p.getZ() + "}");
		return new EditPosKey(player.getUniqueID(), dimension, p);
	}
}
