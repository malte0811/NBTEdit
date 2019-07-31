package malte0811.nbtedit.command;

import com.google.common.collect.BiMap;
import com.google.common.collect.EnumHashBiMap;
import com.google.gson.JsonObject;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.Message;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandExceptionType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import malte0811.nbtedit.NBTEdit;
import malte0811.nbtedit.nbt.EditPosKey;
import malte0811.nbtedit.network.MessageOpenWindow;
import malte0811.nbtedit.util.Utils;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.ArgumentSerializer;
import net.minecraft.command.arguments.ArgumentTypes;
import net.minecraft.command.arguments.BlockPosArgument;
import net.minecraft.command.arguments.IArgumentSerializer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.RayTraceResult.Type;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.fml.network.PacketDistributor;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;

public class CommandNbtEdit {
	private static final String SELF = "self";
	private static final String HAND = "hand";
	private static final String HAND_MAIN = "main";
	private static final String HAND_OFF = "off";

	public static void register(CommandDispatcher<CommandSource> disp) {
		disp.register(Commands.literal("nbtedit")
			.requires((sender) -> sender.hasPermissionLevel(2))
			.executes(data -> editRaytrace(data.getSource()))
			.then(Commands.argument("pos", BlockPosArgument.blockPos())
				.executes(
					data -> editPos(data.getSource(), BlockPosArgument.getLoadedBlockPos(data, "pos"))
				)
				.then(Commands.argument("dim", IntegerArgumentType.integer())
					.executes(
						data -> editPos(data.getSource(), BlockPosArgument.getLoadedBlockPos(data, "pos"),
							IntegerArgumentType.getInteger(data, "dim"))
					)
				)
			)
			.then(Commands.literal("hand")
				.executes(data -> editHand(data.getSource(), Hand.MAIN_HAND))
				.then(Commands.argument("hand", new HandArgument())
					.executes(data -> editHand(data.getSource(), data.getArgument("hand", Hand.class))))
			)
		);
	}

	public static void registerSerializers() {
		ArgumentTypes.register(NBTEdit.MODID + ":hand", HandArgument.class,
			new ArgumentSerializer<>(HandArgument::new));
	}

	private static int editHand(CommandSource source, Hand hand) throws CommandSyntaxException {
		ServerPlayerEntity player = source.asPlayer();
		openEditWindow(player, new EditPosKey(player.getUniqueID(), hand));
		return 0;
	}

	private static final Message NO_OBJECT_MSG = new TranslationTextComponent("nbtedit.no_object");

	private static final CommandExceptionType NO_OBJECT_TYPE = new DynamicCommandExceptionType(
		obj -> NO_OBJECT_MSG
	);

	private static final CommandExceptionType NO_TILE_TYPE = new DynamicCommandExceptionType(
		obj -> NO_OBJECT_MSG
	);

	public static int editRaytrace(CommandSource src) throws CommandSyntaxException {
		PlayerEntity player = src.asPlayer();
		RayTraceResult mop = Utils.rayTrace(player);
		if (mop != null && mop.getType() == Type.BLOCK) {
			BlockPos bPos = ((BlockRayTraceResult) mop).getPos();
			return editPos(src, bPos);
		} else if (mop != null && mop.getType() == Type.ENTITY) {
			Entity e = ((EntityRayTraceResult) mop).getEntity();
			return editEntity(src, e);
		} else {
			throw new CommandSyntaxException(NO_OBJECT_TYPE, NO_OBJECT_MSG);
		}
	}

	private static void openEditWindow(ServerPlayerEntity player, EditPosKey pos) throws CommandSyntaxException {
		NBTEdit.packetHandler.send(PacketDistributor.PLAYER.with(() -> player), new MessageOpenWindow(pos));
	}

	public static int editPos(CommandSource src, BlockPos pos) throws CommandSyntaxException {
		return editPos(src, pos, src.asPlayer().world.dimension.getType().getId());
	}

	public static int editPos(CommandSource src, BlockPos pos, int dim) throws CommandSyntaxException {
		ServerPlayerEntity player = src.asPlayer();
		openEditWindow(player, keyFromPos(pos, player, dim));
		return 0;
	}

	private static EditPosKey keyFromPos(BlockPos p, PlayerEntity player, int dimension) throws CommandSyntaxException {
		if (player.world.getTileEntity(p) == null)
			throw new CommandSyntaxException(NO_TILE_TYPE,
				new TranslationTextComponent("nbtedit.no_te", p.getX(), p.getY(), p.getZ()));
		return new EditPosKey(player.getUniqueID(), dimension, p);
	}

	private static int editEntity(CommandSource src, Entity e) throws CommandSyntaxException {
		ServerPlayerEntity player = src.asPlayer();
		EditPosKey pos = new EditPosKey(player.getUniqueID(), e.world.dimension.getType().getId(), e.getUniqueID());
		openEditWindow(player, pos);
		return 0;
	}

	private static class HandArgument implements ArgumentType<Hand> {
		private static final BiMap<Hand, String> hands = EnumHashBiMap.create(Hand.class);

		static {
			hands.put(Hand.MAIN_HAND, "main");
			hands.put(Hand.OFF_HAND, "off");
		}

		@Override
		public Hand parse(StringReader reader) throws CommandSyntaxException {
			String name = reader.readString().toLowerCase();
			if (!hands.inverse().containsKey(name)) {
				throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherUnknownArgument()
					.createWithContext(new StringReader(name));
			}
			return hands.inverse().get(name);
		}

		@Override
		public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
			for (String s : hands.values()) {
				builder.suggest(s);
			}
			return builder.buildFuture();
		}

		@Override
		public Collection<String> getExamples() {
			return hands.values();
		}
	}
}
