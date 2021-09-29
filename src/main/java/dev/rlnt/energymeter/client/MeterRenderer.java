package dev.rlnt.energymeter.client;

import com.mojang.blaze3d.matrix.MatrixStack;
import dev.rlnt.energymeter.meter.MeterBlock;
import dev.rlnt.energymeter.meter.MeterTile;
import dev.rlnt.energymeter.util.TextUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.util.Direction;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.util.text.TextFormatting;

public class MeterRenderer extends TileEntityRenderer<MeterTile> {

    private static final float[] ANGLE = { 0, 0, 0, 180, 90, -90 };
    private static final float PIXEL_SIZE = .3f / 16;
    private static final float OFFSET = 0.001f;
    private static final int MAX_DISTANCE = 20;
    private final Minecraft mc;
    private final FontRenderer font;

    public MeterRenderer(final TileEntityRendererDispatcher dispatcher) {
        super(dispatcher);
        mc = Minecraft.getInstance();
        font = mc.font;
    }

    private static Vector3f getFacingVector(final Direction facing) {
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
        final MeterTile tile,
        final float partial,
        final MatrixStack matrix,
        final IRenderTypeBuffer buffer,
        final int light,
        final int overlay
    ) {
        // don't display something if the player is too far away
        if (tile.getBlockPos().distSqr(mc.player.blockPosition()) > Math.pow(MAX_DISTANCE, 2)) return;

        // get the facing side and get the vector used for positioning
        final Direction facing = tile.getBlockState().getValue(MeterBlock.HORIZONTAL_FACING);
        final Vector3f vector = getFacingVector(facing);

        matrix.pushPose();
        // move and rotate the position according to the facing
        matrix.translate(vector.x(), vector.y(), vector.z());
        matrix.mulPose(new Quaternion(0, ANGLE[facing.ordinal()], 180, true));
        // scale the matrix so the text fits on the screen
        matrix.scale(PIXEL_SIZE, PIXEL_SIZE, 0);
        // format the current flow rate and draw it according to its size, so it's centered
        final Tuple<String, String> text = TextUtils.formatEnergy(tile.getTransferRate(), false);
        final String flowRate = text.getA();
        final String unit = text.getB() + "/t";
        // flow rate
        font.draw(
            matrix,
            flowRate,
            font.width(flowRate) / -2f,
            -font.lineHeight - OFFSET,
            TextFormatting.WHITE.getColor()
        );
        // unit
        font.draw(matrix, unit, font.width(unit) / -2f, OFFSET, TextFormatting.WHITE.getColor());

        matrix.popPose();
    }
}
