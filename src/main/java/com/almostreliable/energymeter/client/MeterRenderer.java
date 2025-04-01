package com.almostreliable.energymeter.client;

import com.almostreliable.energymeter.block.FacingEntityBlock;
import com.almostreliable.energymeter.block.entity.MeterBlockEntity;
import com.almostreliable.energymeter.util.NumberFormatter;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider.Context;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

public class MeterRenderer implements BlockEntityRenderer<MeterBlockEntity> {

    private static final int MAX_DISTANCE = 32;
    private static final float HALF = 1f / 2f;
    private static final float SCALE = 1f / 40f;
    private final Font font;

    public MeterRenderer(Context context) {
        font = context.getFont();
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public void render(
        MeterBlockEntity blockEntity, float partial, PoseStack stack, MultiBufferSource buffer, int packedLight, int packedOverlay
    ) {
        // turn off display if player is too far away
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null || blockEntity.getBlockPos().distSqr(player.blockPosition()) > Math.pow(MAX_DISTANCE, 2)) {
            return;
        }

        // resolve the facing side and resolve the vector used for positioning
        BlockState blockState = blockEntity.getBlockState();
        Direction facing = FacingEntityBlock.getFacingDir(blockState);
        Direction bottom = FacingEntityBlock.getBottomDir(blockState);

        stack.pushPose();
        stack.translate(HALF, HALF, HALF);

        switch (facing) {
            case UP -> stack.mulPose(bottom.getRotation());
            case DOWN -> {
                stack.mulPose(bottom.getOpposite().getRotation());
                stack.mulPose(Axis.XP.rotationDegrees(180));
            }
            default -> {
                stack.mulPose(facing.getRotation());
                stack.mulPose(Axis.XP.rotationDegrees(90));
            }
        }

        stack.translate(0, SCALE, -HALF - 0.000_1);

        stack.scale(SCALE, SCALE, SCALE);

        NumberFormatter.FormatResult energyRateFormatted = NumberFormatter.formatEnergy(blockEntity.getEnergyRate());
        drawText(energyRateFormatted.getEnergy(), -font.lineHeight, stack, buffer);

        stack.scale(0.8f, 0.8f, 0.8f);
        drawText(energyRateFormatted.getUnitPerTick(), 0, stack, buffer);

        stack.popPose();
    }

    @SuppressWarnings("DataFlowIssue")
    private void drawText(String text, float y, PoseStack stack, MultiBufferSource buffer) {
        font.drawInBatch(
            text,
            font.width(text) / -2f,
            y,
            ChatFormatting.WHITE.getColor(),
            false,
            stack.last().pose(),
            buffer,
            Font.DisplayMode.NORMAL,
            0,
            LightTexture.FULL_BRIGHT
        );
    }
}
