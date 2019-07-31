package malte0811.nbtedit.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import malte0811.nbtedit.gui.NBTFrame;
import malte0811.nbtedit.nbt.EditPosKey;
import malte0811.nbtedit.util.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.BlockPosArgument;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.RayTraceResult.Type;

public class CommandNbtEdit {
	private static final String SELF = "self";
	private static final String HAND = "hand";
	private static final String HAND_MAIN = "main";
	private static final String HAND_OFF = "off";

	public static void register(CommandDispatcher<CommandSource> disp) {
		disp.register(
			Commands.literal("nbtedit")
			.requires((sender) -> sender.hasPermissionLevel(2))
			.executes(data->editRaytrace(data.getSource()))
			.then(Commands.argument("pos", BlockPosArgument.blockPos())
				.executes(
					data->editPos(data.getSource(), BlockPosArgument.getLoadedBlockPos(data, "pos"))
				)
				.then(Commands.argument("dim", IntegerArgumentType.integer())
					.executes(
						data->editPos(data.getSource(), BlockPosArgument.getLoadedBlockPos(data, "pos"),
							IntegerArgumentType.getInteger(data, "dim"))
					)
				)
			)
		);
	}

	public static int editRaytrace(CommandSource src) throws CommandSyntaxException {
		PlayerEntity player = src.asPlayer();
		RayTraceResult mop = Utils.rayTrace(player);
		EditPosKey pos;
		if (mop != null && mop.getType() == Type.BLOCK) {
			BlockPos bPos = ((BlockRayTraceResult)mop).getPos();
			pos = keyFromPos(bPos, player, player.world.dimension.getType().getId());
		} else if (mop != null && mop.getType() == Type.ENTITY) {
			Entity e = ((EntityRayTraceResult)mop).getEntity();
			pos = new EditPosKey(player.getUniqueID(), e.world.dimension.getType().getId(), e.getUniqueID());
		} else {
			return -1;//TODO throw new CommandSyntaxException("nbtedit.no_object");
		}
		openEditWindow(pos);
		return 0;
	}

	private static void openEditWindow(EditPosKey pos) throws CommandSyntaxException {
		new NBTFrame(pos);
		// Necessary to have this run one tick later, after the GUI closes.
		// The new thread is needed to stop MC from running this immidiately
		new Thread(() ->
				Minecraft.getInstance().deferTask(
						() ->
								Minecraft.getInstance().displayGuiScreen(new ChatScreen(""))))
				.start();
	}

	public static int editPos(CommandSource src, BlockPos pos) throws CommandSyntaxException {
		PlayerEntity player = src.asPlayer();
		openEditWindow(keyFromPos(pos, player, player.world.dimension.getType().getId()));
		return 1;
	}

	public static int editPos(CommandSource src, BlockPos pos, int dim) throws CommandSyntaxException {
		PlayerEntity player = src.asPlayer();
		openEditWindow(keyFromPos(pos, player, dim));
		return 1;
	}

	private static EditPosKey keyFromPos(BlockPos p, PlayerEntity player, int dimension) throws CommandSyntaxException {
		if (player.world.getTileEntity(p) == null)
			return null;//TODO throw new CommandSyntaxException("nbtedit.no_te", p.getX(), p.getY(), p.getZ());
		return new EditPosKey(player.getUniqueID(), dimension, p);
	}
}
