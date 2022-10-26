package com.eternalfragment.mcjourneymode.gui;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.systems.RenderSystem;
import io.github.cottonmc.cotton.gui.widget.WWidget;
import io.github.cottonmc.cotton.gui.widget.data.InputResult;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundEvents;
import net.minecraft.tag.TagKey;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryEntry;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class WJMItem extends WWidget {
    private List<ItemStack> items;
    private int duration = 25;
    private int ticks = 0;
    private int current = 0;
    private boolean enabled = true;

    @Nullable private Runnable onClick;
    @Nullable private Runnable onRightClick;
    @Nullable private Runnable onMiddleClick;

    public WJMItem(List<ItemStack> items) {
        setItems(items);
    }

    public WJMItem(TagKey<? extends ItemConvertible> tag) {
        this(getRenderStacks(tag));
    }

    public WJMItem(ItemStack stack) {
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

    @Environment(EnvType.CLIENT)
    @Override
    public void paint(MatrixStack matrices, int x, int y, int mouseX, int mouseY) {
        RenderSystem.enableDepthTest();

        MinecraftClient mc = MinecraftClient.getInstance();
        ItemRenderer renderer = mc.getItemRenderer();
        renderer.zOffset = 100f;
        renderer.renderInGui(items.get(current), x + getWidth() / 2 - 9, y + getHeight() / 2 - 9);
        renderer.zOffset = 0f;
    }

    public int getDuration() {
        return duration;
    }

    public WJMItem setDuration(int duration) {
        this.duration = duration;
        return this;
    }

    public List<ItemStack> getItems() {
        return items;
    }

    public WJMItem setItems(List<ItemStack> items) {
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
        Registry<ItemConvertible> registry = (Registry<ItemConvertible>) Registry.REGISTRIES.get(tag.registry().getValue());

        for (RegistryEntry<ItemConvertible> item : registry.getOrCreateEntryList((TagKey<ItemConvertible>) tag)) {
            builder.add(new ItemStack((ItemConvertible) item));
        }

        return builder.build();
    }

    @Environment(EnvType.CLIENT)
    @Override
    public InputResult onClick(int x, int y, int button) {
        super.onClick(x, y, button);
        if (isWithinBounds(x, y)) {
            MinecraftClient.getInstance().getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0F));
            if (onClick!=null) {
                if (button==0){
                    onClick.run();
                }
                if (button==1){
                    onRightClick.run();
                }
                if (button==2){
                    onMiddleClick.run();
                }

            }
            return InputResult.PROCESSED;
        }
        return InputResult.IGNORED;
    }
    @Environment(EnvType.CLIENT)
    @Override
    public void onKeyPressed(int ch, int key, int modifiers) {
        if (isActivationKey(ch)) {
            onClick(0, 0, 0);
        }
    }
    @Nullable
    public Runnable getOnClick() {
        return onClick;
    }
    public WJMItem setOnClick(@Nullable Runnable onClick) {
        this.onClick = onClick;
        return this;
    }
    public WJMItem setOnRightClick(@Nullable Runnable onClick) {
        this.onRightClick = onClick;
        return this;
    }
    public WJMItem setOnMiddleClick(@Nullable Runnable onClick) {
        this.onMiddleClick = onClick;
        return this;
    }
    public boolean isEnabled() {
        return enabled;
    }
    public WJMItem setEnabled(boolean enabled) {
        this.enabled = enabled;
        return this;
    }

}