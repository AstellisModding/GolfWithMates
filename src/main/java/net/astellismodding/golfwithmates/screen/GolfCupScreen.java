package net.astellismodding.golfwithmates.screen;

import net.astellismodding.golfwithmates.network.SetCupDataPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.PacketDistributor;

@OnlyIn(Dist.CLIENT)
public class GolfCupScreen extends Screen {

    private static final int PANEL_W = 220;
    private static final int PANEL_H = 150;
    private static final int BORDER  = 0xFF555555;
    private static final int BG      = 0xDD000000;
    private static final int LABEL   = 0xAAAAAA;

    private final BlockPos cupPos;
    private final String   initialCourseName;
    private int    par;
    private String disguiseBlock;

    private EditBox courseNameBox;

    // Computed in init — used only for rendering the icon
    private int iconX, iconY;

    private GolfCupScreen(BlockPos cupPos, String courseName, int par, String disguiseBlock) {
        super(Component.translatable("screen.golfwithmates.golf_cup"));
        this.cupPos            = cupPos;
        this.initialCourseName = courseName;
        this.par               = par;
        this.disguiseBlock     = disguiseBlock;
    }

    public static void open(BlockPos pos, String courseName, int par, String disguiseBlock) {
        Minecraft.getInstance().setScreen(new GolfCupScreen(pos, courseName, par, disguiseBlock));
    }

    // -------------------------------------------------------------------------
    // Layout
    // -------------------------------------------------------------------------

    @Override
    protected void init() {
        int left = (width  - PANEL_W) / 2;
        int top  = (height - PANEL_H) / 2;

        // Course name field
        courseNameBox = new EditBox(font,
                left + 10, top + 36, PANEL_W - 20, 16,
                Component.translatable("screen.golfwithmates.course_name"));
        courseNameBox.setMaxLength(40);
        courseNameBox.setValue(initialCourseName);
        addRenderableWidget(courseNameBox);

        // Par [-] [+]
        addRenderableWidget(Button.builder(Component.literal("-"), b -> { if (par > 1) par--; })
                .pos(left + 60, top + 63).size(18, 18).build());
        addRenderableWidget(Button.builder(Component.literal("+"), b -> { if (par < 9) par++; })
                .pos(left + 100, top + 63).size(18, 18).build());

        // Disguise icon position (for render only)
        iconX = left + 10;
        iconY = top + 92;

        // Clear disguise button
        addRenderableWidget(Button.builder(
                Component.translatable("screen.golfwithmates.disguise_clear"), b -> disguiseBlock = "")
                .pos(left + PANEL_W - 46, iconY - 1).size(42, 18).build());

        // Save / Cancel
        addRenderableWidget(Button.builder(Component.translatable("gui.done"), b -> {
            PacketDistributor.sendToServer(
                    new SetCupDataPayload(cupPos, courseNameBox.getValue().trim(), par, disguiseBlock));
            onClose();
        }).pos(left + PANEL_W - 92, top + PANEL_H - 24).size(42, 18).build());

        addRenderableWidget(Button.builder(Component.translatable("gui.cancel"), b -> onClose())
                .pos(left + PANEL_W - 46, top + PANEL_H - 24).size(42, 18).build());
    }

    // -------------------------------------------------------------------------
    // Rendering
    // -------------------------------------------------------------------------

    @Override
    public void renderBackground(GuiGraphics g, int mx, int my, float pt) {
        super.renderBackground(g, mx, my, pt);
        int left = (width  - PANEL_W) / 2;
        int top  = (height - PANEL_H) / 2;

        g.fill(left, top, left + PANEL_W, top + PANEL_H, BG);
        g.renderOutline(left, top, PANEL_W, PANEL_H, BORDER);

        // Title
        g.drawCenteredString(font, title, width / 2, top + 8, 0xFFFFFF);

        // Course name label
        g.drawString(font, Component.translatable("screen.golfwithmates.course_name"),
                left + 10, top + 26, LABEL, false);

        // Par label + value
        g.drawString(font, Component.translatable("screen.golfwithmates.par"),
                left + 10, top + 68, LABEL, false);
        g.drawCenteredString(font, String.valueOf(par), left + 88, top + 68, 0xFFFFFF);

        // Disguise row label
        g.drawString(font, Component.translatable("screen.golfwithmates.disguise"),
                left + 10, top + 82, LABEL, false);

        // Block icon slot background
        g.fill(iconX - 1, iconY - 1, iconX + 17, iconY + 17, BORDER);
        g.fill(iconX, iconY, iconX + 16, iconY + 16, 0xFF1A1A1A);

        // Block icon, name, or placeholder
        if (!disguiseBlock.isEmpty()) {
            ResourceLocation rl = ResourceLocation.tryParse(disguiseBlock);
            if (rl != null) {
                Block block = BuiltInRegistries.BLOCK.get(rl);
                ItemStack icon = new ItemStack(block.asItem());
                if (!icon.isEmpty()) {
                    g.renderItem(icon, iconX, iconY);
                    g.drawString(font, icon.getHoverName(), iconX + 20, iconY + 4, 0xFFFFFF, false);
                }
            }
        } else {
            g.drawCenteredString(font, "—", iconX + 8, iconY + 4, 0x555555);
            g.drawString(font, Component.translatable("screen.golfwithmates.disguise_none"),
                    iconX + 20, iconY + 4, 0x777777, false);
            g.drawString(font, Component.translatable("screen.golfwithmates.disguise_hint"),
                    left + 10, iconY + 20, 0x555555, false);
        }
    }

    @Override
    public boolean isPauseScreen() { return false; }
}
