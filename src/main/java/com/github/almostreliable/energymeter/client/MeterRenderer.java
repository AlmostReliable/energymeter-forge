package com.github.almostreliable.energymeter.client;

import com.github.almostreliable.energymeter.meter.MeterBlock;
import com.github.almostreliable.energymeter.meter.MeterEntity;
import com.github.almostreliable.energymeter.util.TextUtils;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider.Context;
import net.minecraft.core.Direction;

public class MeterRenderer implements BlockEntityRenderer<MeterEntity> {

    private static final float[] ANGLE = {0, 0, 0, 180, 90, -90};
    private static final float PIXEL_SIZE = .3f / 16;
    private static final float OFFSET = 0.001f;
    private static final int MAX_DISTANCE = 30;
    private static final float HALF = .5f;
    private final Minecraft mc;
    private final Font font;

    public MeterRenderer(Context context) {
        mc = Minecraft.getInstance();
        font = context.getFont();
    }

    private static Vector3f getFacingVector(Direction facing) {
        if (facing.ordinal() < 2) {
            // up or down
            return new Vector3f(HALF, facing == Direction.UP ? 1 + OFFSET : -OFFSET, HALF);
        }
        if (facing.ordinal() < 4) {
            // north or south
            return new Vector3f(HALF, HALF, facing == Direction.NORTH ? -OFFSET : 1 + OFFSET);
        }
        // west or east
        return new Vector3f(facing == Direction.WEST ? -OFFSET : 1 + OFFSET, HALF, HALF);
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public void render(
        MeterEntity entity, float partial, PoseStack stack, MultiBufferSource buffer, int light, int overlay
    ) {
        // turn off display if player is too far away
        if (entity.getBlockPos().distSqr(mc.player.blockPosition()) > Math.pow(MAX_DISTANCE, 2)) return;

        // resolve the facing side and resolve the vector used for positioning
        var facing = entity.getBlockState().getValue(MeterBlock.FACING);
        var bottom = entity.getBlockState().getValue(MeterBlock.BOTTOM);
        var vector = getFacingVector(facing);

        stack.pushPose();

        /*
           The rotation of the stack depends on the facing direction of the block and
           where the screen is located.
           The calculations are different if the facing direction is up or down.
           By default, a Quaternion consists of the three axis x, y and z.
           When opening the F3 debug screen, the coloring of the axis is the following:
           x = red, y = green, z = blue
           When using a Quaternion, rotations are always applied in the order z, y, x.
           At this point, we need to keep in mind that the axis are also rotated.
           Example:
           When we rotate 90 degrees around z (blue axis), the red axis (x) becomes
           the green axis (y).
         */

        // move and rotate the position according to the facing
        stack.translate(vector.x(), vector.y(), vector.z());
        if (facing == bottom) {
            stack.mulPose(new Quaternion(0, ANGLE[facing.ordinal()], 180, true));
        } else {
            stack.mulPose(Vector3f.ZN.rotationDegrees(180));
            stack.mulPose(Vector3f.XN.rotationDegrees(facing == Direction.UP ? 90 : -90));
            stack.mulPose(Vector3f.ZN.rotationDegrees(
                facing == Direction.DOWN ? (180 - ANGLE[bottom.ordinal()]) : ANGLE[bottom.ordinal()]));
        }

        // scale the stack so the text fits on the screen
        stack.scale(PIXEL_SIZE, PIXEL_SIZE, 0);

        // format the current flow rate and draw it according to its size, so it's centered
        var text = TextUtils.formatEnergy(entity.getTransferRate(), false);
        var flowRate = text.getA();
        var unit = text.getB() + "/t";
        // flow rate
        font.draw(stack,
            flowRate,
            font.width(flowRate) / -2f,
            -font.lineHeight - OFFSET,
            ChatFormatting.WHITE.getColor()
        );
        // unit
        font.draw(stack, unit, font.width(unit) / -2f, OFFSET, ChatFormatting.WHITE.getColor());

        stack.popPose();
    }
}
