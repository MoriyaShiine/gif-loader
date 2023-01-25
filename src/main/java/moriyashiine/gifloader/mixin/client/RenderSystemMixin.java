package moriyashiine.gifloader.mixin.client;

import com.mojang.blaze3d.systems.RenderSystem;
import moriyashiine.gifloader.client.GifLoader;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Environment(EnvType.CLIENT)
@Mixin(RenderSystem.class)
public class RenderSystemMixin {
	@ModifyVariable(method = "_setShaderTexture(ILnet/minecraft/util/Identifier;)V", at = @At("HEAD"), argsOnly = true)
	private static Identifier gifloader$fixGif(Identifier value) {
		return GifLoader.getFrame(value);
	}
}
