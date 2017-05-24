package malte0811.nbtedit.util;

import java.util.List;

import com.google.common.base.Predicates;

import net.minecraft.entity.Entity;
import net.minecraft.util.EntitySelectors;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.RayTraceResult.Type;
import net.minecraft.util.math.Vec3d;


public final class Utils {
	public static RayTraceResult rayTrace(Entity entity) {
		double d0 = 10;
		Vec3d eyePos = entity.getPositionVector().addVector(0, entity.getEyeHeight(), 0);
        Vec3d Vec3d1 = entity.getLook(1);
        Vec3d Vec3d2 = eyePos.addVector(Vec3d1.xCoord * d0, Vec3d1.yCoord * d0, Vec3d1.zCoord * d0);
        RayTraceResult block = entity.world.rayTraceBlocks(eyePos, Vec3d2, false, false, true);
		double d1 = block.typeOfHit == Type.BLOCK ? block.hitVec.distanceTo(eyePos)
				: Double.MAX_VALUE;
		Vec3d lookVec = entity.getLook(1);
		Vec3d maxRay = eyePos.addVector(lookVec.xCoord * d0, lookVec.yCoord * d0, lookVec.zCoord * d0);
		Entity pointedEntity = null;
		Vec3d Vec3d3 = null;
		float f = 1.0F;
		List<Entity> list = entity.world.getEntitiesInAABBexcluding(entity,
				entity.getEntityBoundingBox().addCoord(lookVec.xCoord * d0, lookVec.yCoord * d0, lookVec.zCoord * d0)
						.expand((double) f, (double) f, (double) f),
				Predicates.and(EntitySelectors.NOT_SPECTATING, (e)->(e.canBeCollidedWith())));
		double d2 = d1;

		for (int j = 0; j < list.size(); ++j) {
			Entity entity1 = (Entity) list.get(j);
			float f1 = entity1.getCollisionBorderSize();
			AxisAlignedBB axisalignedbb = entity1.getEntityBoundingBox().expand((double) f1, (double) f1, (double) f1);
			RayTraceResult RayTraceResult = axisalignedbb.calculateIntercept(eyePos, maxRay);

			if (axisalignedbb.isVecInside(eyePos)) {
				if (d2 >= 0.0D) {
					pointedEntity = entity1;
					Vec3d3 = RayTraceResult == null ? eyePos : RayTraceResult.hitVec;
					d2 = 0.0D;
				}
			} else if (RayTraceResult != null) {
				double d3 = eyePos.distanceTo(RayTraceResult.hitVec);

				if (d3 < d2 || d2 == 0.0D) {
					if (entity1 == entity.getRidingEntity() && !entity.canRiderInteract()) {
						if (d2 == 0.0D) {
							pointedEntity = entity1;
							Vec3d3 = RayTraceResult.hitVec;
						}
					} else {
						pointedEntity = entity1;
						Vec3d3 = RayTraceResult.hitVec;
						d2 = d3;
					}
				}
			}
		}

		if (pointedEntity == null || eyePos.distanceTo(Vec3d3) > d1) {
			return block;
		}
		
		return new RayTraceResult(pointedEntity);
	}
}
