package fi.dy.masa.tweakeroo.mixin.minecraft;

import fi.dy.masa.tweakeroo.tweaks.FakeNightVision;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.MilkBucketItem;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MilkBucketItem.class)
public abstract class MixinMilkBucketItem {

    @Inject(
            method = "finishUsing",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/LivingEntity;clearStatusEffects()Z",
                    shift = At.Shift.AFTER
            )
    )
    private void onFinishUsing(ItemStack stack, World world, LivingEntity user, CallbackInfoReturnable<ItemStack> cir) {
        // Notify fakeNightVision reapply NightVision when effect cleared.
        FakeNightVision.onEffectCleared();
    }
}
