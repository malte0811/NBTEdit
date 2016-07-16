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
import net.minecraft.util.BlockPos;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.MovingObjectPosition.MovingObjectType;

public class CommandNbtEdit extends CommandBase {

	@Override
	public String getCommandName() {
		return "nbtedit";
	}

	@Override
	public String getCommandUsage(ICommandSender sender) {
		return "/nbtedit [<x> <y> <z> [<dim>]]";
	}
	@Override
	public void processCommand(ICommandSender sender, String[] args) throws CommandException {
		EntityPlayer player = getCommandSenderAsPlayer(sender);
		EditPosKey pos = null;
		MovingObjectPosition mop = Utils.rayTrace(player);
		if (args.length==3||args.length==4) {
			int x = parseInt(args[0]);
			int y = parseInt(args[1]);
			int z = parseInt(args[2]);
			int w = player.worldObj.provider.getDimensionId();
			if (args.length==4) {
				w = parseInt(args[3]);
			}
			pos = new EditPosKey(player.getUniqueID(), w, new BlockPos(x, y, z));
		} else if (mop!=null&&mop.typeOfHit==MovingObjectType.BLOCK) {
			BlockPos bPos = mop.getBlockPos();
			if (player.worldObj.getTileEntity(bPos)==null)
				throw new CommandException("No TileEntity found");
			pos = new EditPosKey(player.getUniqueID(), player.worldObj.provider.getDimensionId(), bPos);
		} else if (mop!=null&&mop.typeOfHit==MovingObjectType.ENTITY) {
			Entity e = mop.entityHit;
			pos = new EditPosKey(player.getUniqueID(), e.worldObj.provider.getDimensionId(), e.getEntityId());
		} else {
			throw new CommandException("No object found for editing");
		}
		if (pos!=null) {
			NBTEdit.packetHandler.sendTo(new MessageOpenWindow(pos), (EntityPlayerMP)player);
		}
	}
}
