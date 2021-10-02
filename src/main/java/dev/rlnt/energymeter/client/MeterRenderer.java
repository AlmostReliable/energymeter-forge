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
    private static final int MAX_DISTANCE = 20;
    private final Minecraft mc;
    private final Font font;

    public MeterRenderer(Context context) {
        mc = Minecraft.getInstance();
        font = context.getFont();
    }

    private static Vector3f getFacingVector(Direction facing) {
        final float y = .5f;
        if (facing.ordinal() < 2) {
            throw new IllegalStateException("Facing can't be up or down!");
        } else if (facing.ordinal() < 4) {
            // north or south
            return new Vector3f(.5f, y, facing == Direction.NORTH ? -OFFSET : 1 + OFFSET);
        } else {
            // west or east
            return new Vector3f(facing == Direction.WEST ? -OFFSET : 1 + OFFSET, y, .5f);
        }
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public void render(
        MeterEntity entity,
        float partial,
        PoseStack matrix,
        MultiBufferSource buffer,
        int light,
        int overlay
    ) {
        // don't display something if the player is too far away
        if (entity.getBlockPos().distSqr(mc.player.blockPosition()) > Math.pow(MAX_DISTANCE, 2)) return;

        // get the facing side and get the vector used for positioning
        Direction facing = entity.getBlockState().getValue(MeterBlock.HORIZONTAL_FACING);
        Vector3f vector = getFacingVector(facing);

        matrix.pushPose();
        // move and rotate the position according to the facing
        matrix.translate(vector.x(), vector.y(), vector.z());
        matrix.mulPose(new Quaternion(0, ANGLE[facing.ordinal()], 180, true));
        // scale the matrix so the text fits on the screen
        matrix.scale(PIXEL_SIZE, PIXEL_SIZE, 0);
        // format the current flow rate and draw it according to its size, so it's centered
        Tuple<String, String> text = TextUtils.formatEnergy(entity.getTransferRate(), false);
        String flowRate = text.getA();
        String unit = text.getB() + "/t";
        // flow rate
        font.draw(
            matrix,
            flowRate,
            font.width(flowRate) / -2f,
            -font.lineHeight - OFFSET,
            ChatFormatting.WHITE.getColor()
        );
        // unit
        font.draw(matrix, unit, font.width(unit) / -2f, OFFSET, ChatFormatting.WHITE.getColor());

        matrix.popPose();
    }
}
