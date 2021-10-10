package dev.rlnt.energymeter.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
import dev.rlnt.energymeter.meter.MeterBlock;
import dev.rlnt.energymeter.meter.MeterEntity;
import dev.rlnt.energymeter.util.TextUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider.Context;
import net.minecraft.core.Direction;
import net.minecraft.util.Tuple;

public class MeterRenderer implements BlockEntityRenderer<MeterEntity> {

    private static final float[] ANGLE = { 0, 0, 0, 180, 90, -90 };
    private static final float PIXEL_SIZE = .3f / 16;
    private static final float OFFSET = 0.001f;
    private static final int MAX_DISTANCE = 30;
    private final Minecraft mc;
    private final Font font;

    public MeterRenderer(Context context) {
        mc = Minecraft.getInstance();
        font = context.getFont();
    }

    private static Vector3f getFacingVector(Direction facing) {
        if (facing.ordinal() < 2) {
            // up or down
            return new Vector3f(.5f, facing == Direction.UP ? 1 + OFFSET : -OFFSET, .5f);
        } else if (facing.ordinal() < 4) {
            // north or south
            return new Vector3f(.5f, .5f, facing == Direction.NORTH ? -OFFSET : 1 + OFFSET);
        } else {
            // west or east
            return new Vector3f(facing == Direction.WEST ? -OFFSET : 1 + OFFSET, .5f, .5f);
        }
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public void render(
        MeterEntity entity,
        float partial,
        PoseStack stack,
        MultiBufferSource buffer,
        int light,
        int overlay
    ) {
        // don't display something if the player is too far away
        if (entity.getBlockPos().distSqr(mc.player.blockPosition()) > Math.pow(MAX_DISTANCE, 2)) return;

        // get the facing side and get the vector used for positioning
        Direction facing = entity.getBlockState().getValue(MeterBlock.FACING);
        Direction bottom = entity.getBlockState().getValue(MeterBlock.BOTTOM);
        Vector3f vector = getFacingVector(facing);

        stack.pushPose();
        // move and rotate the position according to the facing
        stack.translate(vector.x(), vector.y(), vector.z());
        /*
           The rotation of the matrix depends on the facing direction of the block and
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
        if (facing != bottom) {
            stack.mulPose(Vector3f.ZN.rotationDegrees(180));
            stack.mulPose(Vector3f.XN.rotationDegrees(facing == Direction.UP ? 90 : -90));
            stack.mulPose(
                Vector3f.ZN.rotationDegrees(
                    facing == Direction.DOWN ? (180 - ANGLE[bottom.ordinal()]) : ANGLE[bottom.ordinal()]
                )
            );
        } else {
            stack.mulPose(new Quaternion(0, ANGLE[facing.ordinal()], 180, true));
        }
        // scale the matrix so the text fits on the screen
        stack.scale(PIXEL_SIZE, PIXEL_SIZE, 0);
        // format the current flow rate and draw it according to its size, so it's centered
        Tuple<String, String> text = TextUtils.formatEnergy(entity.getTransferRate(), false);
        String flowRate = text.getA();
        String unit = text.getB() + "/t";
        // flow rate
        font.draw(
            stack,
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
