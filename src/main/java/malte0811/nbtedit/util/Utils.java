package malte0811.nbtedit.util;

import malte0811.nbtedit.NBTEdit;
import malte0811.nbtedit.nbt.EditPosKey;
import malte0811.nbtedit.network.MessageBlockUpdate;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EntityPredicates;
import net.minecraft.util.math.*;
import net.minecraft.util.math.RayTraceResult.Type;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraftforge.fml.server.ServerLifecycleHooks;

import java.util.List;
import java.util.Optional;


public final class Utils {
	public static RayTraceResult rayTrace(Entity entity) {
		double d0 = 10;
		Vector3d eyePos = entity.getPositionVec().add(0, entity.getEyeHeight(), 0);
		Vector3d Vector3d1 = entity.getLook(1);
		Vector3d Vector3d2 = eyePos.add(Vector3d1.x * d0, Vector3d1.y * d0, Vector3d1.z * d0);
		RayTraceResult block = entity.world.rayTraceBlocks(new RayTraceContext(eyePos, Vector3d2, RayTraceContext.BlockMode.OUTLINE,
			RayTraceContext.FluidMode.NONE, entity));
		double d1 = block.getType() == Type.BLOCK ? block.getHitVec().distanceTo(eyePos)
			: Double.MAX_VALUE;
		Vector3d lookVec = entity.getLook(1);
		Vector3d maxRay = eyePos.add(lookVec.x * d0, lookVec.y * d0, lookVec.z * d0);
		Entity pointedEntity = null;
		Vector3d target = null;
		float f = 1.0F;
		List<Entity> list = entity.world.getEntitiesInAABBexcluding(entity,
			entity.getBoundingBox().expand(lookVec.x * d0, lookVec.y * d0, lookVec.z * d0)
				.grow((double) f),
			EntityPredicates.NOT_SPECTATING.and(Entity::canBeCollidedWith));
		double d2 = d1;

		for (Entity e : list) {
			float f1 = e.getCollisionBorderSize();
			AxisAlignedBB axisalignedbb = e.getBoundingBox().grow((double) f1);
			Optional<Vector3d> hit = axisalignedbb.rayTrace(eyePos, maxRay);

			if (axisalignedbb.contains(eyePos)) {
				if (d2 >= 0.0D) {
					pointedEntity = e;
					target = hit.orElse(eyePos);
					d2 = 0.0D;
				}
			} else if (hit.isPresent()) {
				Vector3d hitVec = hit.get();
				double d3 = eyePos.distanceTo(hitVec);

				if (d3 < d2 || d2 == 0.0D) {
					if (e == entity.getRidingEntity() && !entity.canRiderInteract()) {
						if (d2 == 0.0D) {
							pointedEntity = e;
							target = hitVec;
						}
					} else {
						pointedEntity = e;
						target = hitVec;
						d2 = d3;
					}
				}
			}
		}

		if (pointedEntity == null || eyePos.distanceTo(target) > d1) {
			return block;
		}

		return new EntityRayTraceResult(pointedEntity);
	}

	public static CompoundNBT getNBTForPos(EditPosKey k, MinecraftServer server) {
		ServerWorld world = server.getWorld(k.dim);
		if (world == null) {
			return null;
		}
		switch (k.type) {
			case PLAYER:
				Entity ent = world.getEntityByUuid(k.entity);
				if (ent != null && !ent.removed) {
					String entityString = EntityType.getKey(ent.getType()).toString(); //minecraft:player
					if (entityString != null) {
						CompoundNBT ret = new CompoundNBT();
						//manually write data (like writeUnlessRemoved(...)), because serialization check
						//returns false for players -> so no nbt data is returned when treating as entity
						ret.putString("id", entityString);
						ent.writeWithoutTypeId(ret);
						return ret;
					}
				}
				break;
			case ENTITY:
				Entity e = world.getEntityByUuid(k.entity);
				if (e != null) {
					CompoundNBT ret = new CompoundNBT();
					e.writeUnlessRemoved(ret);
					return ret;
				}
				break;
			case TILEENTITY:
				TileEntity te = world.getTileEntity(k.tilePos);
				if (te != null) {
					return te.serializeNBT();
				}
				break;
			case HAND:
				ServerPlayerEntity player = ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayerByUUID(k.player);
				if (player == null) {
					return null;
				}
				ItemStack stack = player.getHeldItem(k.hand);
				if (!stack.isEmpty())
					return stack.serializeNBT().copy();
		}
		return null;
	}

	public static void setNBTAtPos(EditPosKey k, CompoundNBT newNbt, MinecraftServer server) {
		ServerWorld world = server.getWorld(k.dim);
		if (world != null) {
			switch (k.type) {
				case PLAYER:
				case ENTITY:
					Entity e = world.getEntityByUuid(k.entity);
					if (e != null) {
						e.read(newNbt);
					}
					break;
				case TILEENTITY:
					TileEntity te = world.getTileEntity(k.tilePos);
					if (te != null) {
						BlockState state = world.getBlockState(k.tilePos);
						te.read(state, newNbt);
						te.markDirty();
						BlockState newState = world.getBlockState(k.tilePos);
						world.notifyBlockUpdate(k.tilePos, state, state, 3);
						world.notifyNeighborsOfStateChange(k.tilePos, newState.getBlock());
						NBTEdit.packetHandler.send(PacketDistributor.TRACKING_CHUNK.with(() -> world.getChunkAt(k.tilePos)),
							new MessageBlockUpdate(k.tilePos));
					}
					break;
				case HAND:
					ServerPlayerEntity player = ServerLifecycleHooks.getCurrentServer()
						.getPlayerList().getPlayerByUUID(k.player);
					ItemStack stack = ItemStack.read(newNbt);
					player.setHeldItem(k.hand, stack);
			}
		}
	}
}
