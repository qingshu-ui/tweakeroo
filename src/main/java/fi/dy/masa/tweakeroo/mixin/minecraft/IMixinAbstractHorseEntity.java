package fi.dy.masa.tweakeroo.mixin.minecraft;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import net.minecraft.entity.passive.AbstractHorseEntity;
import net.minecraft.inventory.SimpleInventory;

@Mixin(AbstractHorseEntity.class)
public interface IMixinAbstractHorseEntity
{
    @Accessor("items")
    SimpleInventory tweakeroo_getHorseInventory();
}
