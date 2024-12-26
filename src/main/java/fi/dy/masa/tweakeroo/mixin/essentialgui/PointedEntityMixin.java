package fi.dy.masa.tweakeroo.mixin.essentialgui;

import fi.dy.masa.tweakeroo.config.FeatureToggle;
import fi.dy.masa.tweakeroo.util.CameraEntity;
import lordrius.essentialgui.gui.hud.PointedEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.hit.HitResult;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(PointedEntity.class)
public abstract class PointedEntityMixin {

    @Redirect(
            method = "<init>",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/client/MinecraftClient;crosshairTarget:Lnet/minecraft/util/hit/HitResult;",
                    opcode = Opcodes.GETFIELD
            )
    )
    private HitResult onInit(MinecraftClient mc) {
        CameraEntity entity;
        HitResult hit;
        if (FeatureToggle.TWEAK_FREE_CAMERA.getBooleanValue() &&
            null != (entity = CameraEntity.getCamera()) &&
            null != (hit = entity.getCrosshairTarget())) {
            return hit;
        }
        return mc.crosshairTarget;
    }

    @Redirect(
            method = "drawPlayerSkin",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/client/MinecraftClient;player:Lnet/minecraft/client/network/ClientPlayerEntity;",
                    opcode = Opcodes.GETFIELD
            )
    )
    private ClientPlayerEntity onDrawPlayerSkin(MinecraftClient mc) {
        CameraEntity entity;
        if (FeatureToggle.TWEAK_FREE_CAMERA.getBooleanValue() &&
            null != (entity = CameraEntity.getCamera())) {
            return entity;
        }
        return mc.player;
    }

    @Redirect(
            method = "getEntityExtendedProperties",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/client/MinecraftClient;player:Lnet/minecraft/client/network/ClientPlayerEntity;",
                    opcode = Opcodes.GETFIELD
            )
    )
    private ClientPlayerEntity onGetEntityExtendedProperties(MinecraftClient mc) {
        CameraEntity entity;
        if (FeatureToggle.TWEAK_FREE_CAMERA.getBooleanValue() &&
            null != (entity = CameraEntity.getCamera())) {
            return entity;
        }
        return mc.player;
    }


    @Redirect(
            method = "isFoxTrustingPlayer",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/client/MinecraftClient;player:Lnet/minecraft/client/network/ClientPlayerEntity;",
                    opcode = Opcodes.GETFIELD
            )
    )
    private ClientPlayerEntity onIsFoxTrustingPlayer(MinecraftClient mc) {
        CameraEntity entity;
        if (FeatureToggle.TWEAK_FREE_CAMERA.getBooleanValue() &&
            null != (entity = CameraEntity.getCamera())) {
            return entity;
        }
        return mc.player;
    }

}
