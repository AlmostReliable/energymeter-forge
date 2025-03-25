package com.almostreliable.energymeter.client;

import com.almostreliable.energymeter.block.FacingEntityBlock;
import com.almostreliable.energymeter.block.entity.MeterBlockEntity;

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

        // // format the current flow rate and draw it according to its size, so it's centered
        // var text = TextUtils.formatEnergy(blockEntity.getEnergyRate(), false);
        // var flowRate = text.getA();
        // var unit = text.getB() + "/t";
        // // flow rate
        // drawText(flowRate, -font.lineHeight - OFFSET, stack, buffer);
        // // unit
        // drawText(unit, OFFSET, stack, buffer);

        stack.translate(0.5, 0.5, 0.5);

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

        stack.translate(0, 0, -0.5 - 0.000_1);

        float scale = 1f / 32f;
        stack.scale(scale, scale, scale);

        double energyRate = blockEntity.getEnergyRate();
        String text = String.format("%.2f", energyRate);
        drawText(text, 0, stack, buffer);

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
