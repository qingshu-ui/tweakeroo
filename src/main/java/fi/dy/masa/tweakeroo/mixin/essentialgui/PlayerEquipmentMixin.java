package fi.dy.masa.tweakeroo.mixin.essentialgui;

import fi.dy.masa.tweakeroo.config.FeatureToggle;
import fi.dy.masa.tweakeroo.util.CameraEntity;
import lordrius.essentialgui.gui.hud.PlayerEquipment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.hit.HitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(PlayerEquipment.class)
public abstract class PlayerEquipmentMixin {

    @Redirect(
            method = "<init>",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/client/MinecraftClient;crosshairTarget:Lnet/minecraft/util/hit/HitResult;"
            )
    )
    public HitResult onInit(MinecraftClient mc) {
        CameraEntity cameraEntity;
        if (FeatureToggle.TWEAK_FREE_CAMERA.getBooleanValue() &&
            (cameraEntity = CameraEntity.getCamera()) != null) {
            return cameraEntity.getCrosshairTarget();
        }
        return mc.crosshairTarget;
    }
}
