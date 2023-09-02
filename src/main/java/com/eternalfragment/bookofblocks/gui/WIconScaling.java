package com.eternalfragment.bookofblocks.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import io.github.cottonmc.cotton.gui.widget.WWidget;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;

import java.util.Objects;

@Environment(EnvType.CLIENT)
public class WIconScaling extends WWidget {
    private final ItemStack stack;

    @Override
    public boolean canResize() {
        return true; // set to false if you want a static size
    }

    public WIconScaling(ItemStack stack) {
        this.stack = Objects.requireNonNull(stack, "stack");
    }

    @Override
    public void setSize(int x, int y) {
        super.setSize(x, y);
    }
    /*
    //1.19.4 code:
    @Environment(EnvType.CLIENT)
    @Override
    public void paint(MatrixStack matrices, int x, int y, int mouseX, int mouseY) {
        MinecraftClient client = MinecraftClient.getInstance();
        ItemRenderer renderer = client.getItemRenderer();
        MatrixStack modelViewMatrices = RenderSystem.getModelViewStack();
        int wsize = getWidth();
        int hsize = getHeight();
        float wscale = wsize != 16 ? ((float) wsize / 16f) : 1f;
        float hscale = hsize != 16 ? ((float) hsize / 16f) : 1f;

        modelViewMatrices.push();
        modelViewMatrices.translate(x, y, 0);
        modelViewMatrices.scale(wscale, hscale, 1);
        RenderSystem.applyModelViewMatrix();
        MatrixStack mtx=new MatrixStack();
        renderer.renderInGui(mtx,stack, 0, 0);
        modelViewMatrices.pop();
        RenderSystem.applyModelViewMatrix();
    }
    */
    @Environment(EnvType.CLIENT)
    @Override
    public void paint(DrawContext context, int x, int y, int mouseX, int mouseY) {
        RenderSystem.enableDepthTest();
        int wsize = getWidth();
        int hsize = getHeight();
        float wscale = wsize != 16 ? ((float) wsize / 16f) : 1f;
        float hscale = hsize != 16 ? ((float) hsize / 16f) : 1f;
        MatrixStack matrices = context.getMatrices();
        matrices.push();
        matrices.translate(x, y, 0);
        matrices.scale(wscale, hscale, 1);
        context.drawItemWithoutEntity(stack, 0, 0);
        matrices.pop();


    }
}