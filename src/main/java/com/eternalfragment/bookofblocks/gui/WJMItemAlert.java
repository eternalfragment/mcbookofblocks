package com.eternalfragment.bookofblocks.gui;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.systems.RenderSystem;
import io.github.cottonmc.cotton.gui.widget.WWidget;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.TagKey;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class WJMItemAlert extends WWidget {
    private List<ItemStack> items;
    private int duration = 25;
    private int ticks = 0;
    private int current = 0;
    private boolean enabled = true;

    public WJMItemAlert(List<ItemStack> items) {
        setItems(items);
    }

    public WJMItemAlert(TagKey<? extends ItemConvertible> tag) {
        this(getRenderStacks(tag));
    }

    public WJMItemAlert(ItemStack stack) {
        this(Collections.singletonList(stack));
    }

    @Override
    public boolean canResize() {
        return true;
    }

    @Environment(EnvType.CLIENT)
    @Override
    public void tick() {
        if (ticks++ >= duration) {
            ticks = 0;
            current = (current + 1) % items.size();
        }
    }
/*
//1.19.4 code:
    @Environment(EnvType.CLIENT)
    @Override
    public void paint(MatrixStack matrices, int x, int y, int mouseX, int mouseY) {
        RenderSystem.enableDepthTest();

        MinecraftClient mc = MinecraftClient.getInstance();
        ItemRenderer renderer = mc.getItemRenderer();
        //renderer.zOffset = 110f;
        MatrixStack mtx=new MatrixStack();
        renderer.renderInGui(mtx, items.get(current), x + getWidth() / 2 - 9, y + getHeight() / 2 - 9);
        //renderer.zOffset = 0f;
    }*/
    @Environment(EnvType.CLIENT)
    @Override
    public void paint(DrawContext context, int x, int y, int mouseX, int mouseY) {
        RenderSystem.enableDepthTest();
        context.drawItemWithoutEntity(items.get(current), x + getWidth() / 2 - 8, y + getHeight() / 2 - 8);
    }


    public int getDuration() {
        return duration;
    }

    public WJMItemAlert setDuration(int duration) {
        this.duration = duration;
        return this;
    }

    public List<ItemStack> getItems() {
        return items;
    }

    public WJMItemAlert setItems(List<ItemStack> items) {
        Objects.requireNonNull(items, "stacks == null!");
        if (items.isEmpty()) throw new IllegalArgumentException("The stack list is empty!");

        this.items = items;

        // Reset the state
        current = 0;
        ticks = 0;

        return this;
    }

    private static List<ItemStack> getRenderStacks(TagKey<? extends ItemConvertible> tag) {
        ImmutableList.Builder<ItemStack> builder = ImmutableList.builder();
        Registry<ItemConvertible> registry = (Registry<ItemConvertible>) Registries.REGISTRIES.get(tag.registry().getValue());

        for (RegistryEntry<ItemConvertible> item : registry.getOrCreateEntryList((TagKey<ItemConvertible>) tag)) {
            builder.add(new ItemStack((ItemConvertible) item));
        }

        return builder.build();
    }
    public boolean isEnabled() {
        return enabled;
    }
    public WJMItemAlert setEnabled(boolean enabled) {
        this.enabled = enabled;
        return this;
    }

}