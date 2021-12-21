package com.solegendary.ageofcraft.mixin;

import com.solegendary.ageofcraft.orthoview.OrthoViewClientEvents;
import net.minecraft.client.renderer.GameRenderer;
import com.mojang.math.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GameRenderer.class)
public class OrthoViewMixin {
    @Inject(
            method = "getProjectionMatrix(D)Lcom/mojang/math/Matrix4f;",
            at = @At("HEAD"),
            cancellable = true
    )
    private void getProjectionMatrix(
            double d,
            CallbackInfoReturnable<Matrix4f> cir
    ) {
        if (OrthoViewClientEvents.isEnabled()) {
            cir.setReturnValue(OrthoViewClientEvents.getIsometricProjection());
        }
    }
}