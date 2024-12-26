package fi.dy.masa.tweakeroo

import net.fabricmc.loader.api.FabricLoader
import org.objectweb.asm.tree.ClassNode
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin
import org.spongepowered.asm.mixin.extensibility.IMixinInfo

var isEssentialGuiPresent = false
const val mixinPackage = "fi.dy.masa.tweakeroo.mixin"

class MixinPlugin: IMixinConfigPlugin {
    override fun onLoad(mixinPackage: String?) {
        val loader = FabricLoader.getInstance()
        isEssentialGuiPresent = loader.isModLoaded("essentialgui")
    }

    override fun getRefMapperConfig(): String? {
        return null
    }

    override fun shouldApplyMixin(targetClassName: String, mixinClassName: String): Boolean {
        if(mixinClassName.startsWith("$mixinPackage.minecraft")) return true
        if(mixinClassName.startsWith("$mixinPackage.essentialgui") && isEssentialGuiPresent) return true
        return false
    }

    override fun acceptTargets(myTargets: MutableSet<String>?, otherTargets: MutableSet<String>?) {
    }

    override fun getMixins(): MutableList<String>? {
        return null
    }

    override fun preApply(
        targetClassName: String?,
        targetClass: ClassNode?,
        mixinClassName: String?,
        mixinInfo: IMixinInfo?
    ) {

    }

    override fun postApply(
        targetClassName: String?,
        targetClass: ClassNode?,
        mixinClassName: String?,
        mixinInfo: IMixinInfo?
    ) {

    }
}