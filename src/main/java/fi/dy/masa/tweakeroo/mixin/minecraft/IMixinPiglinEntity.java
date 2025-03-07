package fi.dy.masa.tweakeroo.mixin.minecraft;

import net.minecraft.entity.mob.PiglinEntity;
import net.minecraft.inventory.SimpleInventory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(PiglinEntity.class)
public interface IMixinPiglinEntity
{
    @Accessor("inventory")
    SimpleInventory tweakeroo_inventory();
}