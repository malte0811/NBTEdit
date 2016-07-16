package malte0811.nbtedit.util;

import java.util.List;

import com.google.common.base.Predicates;

import net.minecraft.entity.Entity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.EntitySelectors;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.MovingObjectPosition.MovingObjectType;
import net.minecraft.util.Vec3;

public final class Utils {
	public static MovingObjectPosition rayTrace(Entity entity) {
		double d0 = 10;
		Vec3 eyePos = entity.getPositionVector().addVector(0, entity.getEyeHeight(), 0);
        Vec3 vec31 = entity.getLook(1);
        Vec3 vec32 = eyePos.addVector(vec31.xCoord * d0, vec31.yCoord * d0, vec31.zCoord * d0);
        MovingObjectPosition block = entity.worldObj.rayTraceBlocks(eyePos, vec32, false, false, true);
		double d1 = block.typeOfHit == MovingObjectType.BLOCK ? block.hitVec.distanceTo(eyePos)
				: Double.MAX_VALUE;
		Vec3 lookVec = entity.getLook(1);
		Vec3 maxRay = eyePos.addVector(lookVec.xCoord * d0, lookVec.yCoord * d0, lookVec.zCoord * d0);
		Entity pointedEntity = null;
		Vec3 vec33 = null;
		float f = 1.0F;
		List<Entity> list = entity.worldObj.getEntitiesInAABBexcluding(entity,
				entity.getEntityBoundingBox().addCoord(lookVec.xCoord * d0, lookVec.yCoord * d0, lookVec.zCoord * d0)
						.expand((double) f, (double) f, (double) f),
				Predicates.and(EntitySelectors.NOT_SPECTATING, (e)->(e.canBeCollidedWith())));
		double d2 = d1;

		for (int j = 0; j < list.size(); ++j) {
			Entity entity1 = (Entity) list.get(j);
			float f1 = entity1.getCollisionBorderSize();
			AxisAlignedBB axisalignedbb = entity1.getEntityBoundingBox().expand((double) f1, (double) f1, (double) f1);
			MovingObjectPosition movingobjectposition = axisalignedbb.calculateIntercept(eyePos, maxRay);

			if (axisalignedbb.isVecInside(eyePos)) {
				if (d2 >= 0.0D) {
					pointedEntity = entity1;
					vec33 = movingobjectposition == null ? eyePos : movingobjectposition.hitVec;
					d2 = 0.0D;
				}
			} else if (movingobjectposition != null) {
				double d3 = eyePos.distanceTo(movingobjectposition.hitVec);

				if (d3 < d2 || d2 == 0.0D) {
					if (entity1 == entity.ridingEntity && !entity.canRiderInteract()) {
						if (d2 == 0.0D) {
							pointedEntity = entity1;
							vec33 = movingobjectposition.hitVec;
						}
					} else {
						pointedEntity = entity1;
						vec33 = movingobjectposition.hitVec;
						d2 = d3;
					}
				}
			}
		}

		if (pointedEntity == null || eyePos.distanceTo(vec33) > d1) {
			return block;
		}
		
		return new MovingObjectPosition(pointedEntity);
	}
}
