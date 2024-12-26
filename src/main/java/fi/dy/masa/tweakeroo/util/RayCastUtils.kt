@file: Suppress("SpellCheckingInspection")

package fi.dy.masa.tweakeroo.util

import net.minecraft.client.MinecraftClient
import net.minecraft.entity.Entity
import net.minecraft.entity.projectile.ProjectileUtil
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.hit.HitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.util.math.MathHelper
import net.minecraft.util.math.Vec3d
import kotlin.math.max
import kotlin.math.sqrt

/*
 * 本文件中的代码主要来源于通过 Fabric Loom 和 Yarn 映射的 Minecraft 源代码。
 * 原始代码及其逻辑版权归 Mojang Studios（微软子公司）所有。
 *
 * 本代码仅用于符合 Mojang EULA 的 Minecraft Mod 开发。
 * 禁止在 Minecraft Mod 社区范围以外的场景中分发或使用。
 */
object RayCastUtils {
    @JvmStatic
    @JvmOverloads
    fun rayCast(mc: MinecraftClient, cameraEntity: Entity, tickDelta: Float = 1.0f) = findCrosshairTarget(
        cameraEntity,
        mc.player?.blockInteractionRange ?: 4.5,
        mc.player?.entityInteractionRange ?: 3.0,
        tickDelta
    )

    @JvmStatic
    fun findCrosshairTarget(
        camera: Entity,
        blockInteractionRange: Double,
        entityInteraction: Double,
        tickDelta: Float
    ): HitResult {
        var d = max(blockInteractionRange, entityInteraction)
        var e = MathHelper.square(d)
        val vec3d = camera.getCameraPosVec(tickDelta)
        val hitResult = camera.raycast(d, tickDelta, false)
        val f = hitResult.pos.squaredDistanceTo(vec3d)
        if (hitResult.type != HitResult.Type.MISS) {
            e = f
            d = sqrt(f)
        }

        val vec3d2 = camera.getRotationVec(tickDelta)
        val vec3d3 = vec3d.add(vec3d2.x * d, vec3d2.y * d, vec3d2.z * d)
        val g = 1.0f
        val box = camera.boundingBox.stretch(vec3d2.multiply(d)).expand(1.0, 1.0, 1.0)
        val entityHitResult = ProjectileUtil.raycast(camera, vec3d, vec3d3, box, { !it.isSpectator && it.canHit() }, e)
        return if (entityHitResult != null && entityHitResult.pos.squaredDistanceTo(vec3d) < f) {
            ensureTargetInRange(entityHitResult, vec3d, entityInteraction)
        } else {
            ensureTargetInRange(hitResult, vec3d, blockInteractionRange)
        }
    }

    private fun ensureTargetInRange(hitResult: HitResult, cameraPos: Vec3d, interactionRange: Double): HitResult {
        val vec3d = hitResult.pos
        if (!vec3d.isInRange(cameraPos, interactionRange)) {
            val vec3d2 = hitResult.pos
            val direction = Direction.getFacing(vec3d2.x - cameraPos.x, vec3d2.y - cameraPos.y, vec3d2.z - cameraPos.z)
            return BlockHitResult.createMissed(vec3d2, direction, BlockPos.ofFloored(vec3d2))
        }
        return hitResult
    }
}