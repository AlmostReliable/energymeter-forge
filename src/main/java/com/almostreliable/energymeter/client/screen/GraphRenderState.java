package com.almostreliable.energymeter.client.screen;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.state.gui.GuiElementRenderState;

import org.joml.Matrix3x2fc;

import java.util.ArrayList;
import java.util.List;

final class GraphRenderState implements GuiElementRenderState {

    private final Matrix3x2fc pose;
    private final ScreenRectangle bounds;
    private final List<Line> lines = new ArrayList<>();

    GraphRenderState(Matrix3x2fc pose, ScreenRectangle bounds) {
        this.pose = pose;
        this.bounds = bounds;
    }

    void addLine(int x0, int y0, int x1, int y1, int color) {
        lines.add(new Line(x0, y0, x1, y1, color));
    }

    @Override
    public void buildVertices(VertexConsumer consumer) {
        for (Line line : lines) {
            float dx = line.x1() - line.x0();
            float dy = line.y1() - line.y0();
            float length = (float) Math.sqrt(dx * dx + dy * dy);
            float normalX = length == 0 ? 0 : dx / length;
            float normalY = length == 0 ? 0 : dy / length;

            consumer.addVertexWith2DPose(pose, line.x0(), line.y0())
                .setColor(line.color())
                .setNormal(normalX, normalY, 0)
                .setLineWidth(2);
            consumer.addVertexWith2DPose(pose, line.x1(), line.y1())
                .setColor(line.color())
                .setNormal(normalX, normalY, 0)
                .setLineWidth(2);
        }
    }

    @Override
    public RenderPipeline pipeline() {
        return RenderPipelines.LINES;
    }

    @Override
    public TextureSetup textureSetup() {
        return TextureSetup.noTexture();
    }

    @Override
    public ScreenRectangle scissorArea() {
        return bounds;
    }

    @Override
    public ScreenRectangle bounds() {
        return bounds;
    }

    private record Line(int x0, int y0, int x1, int y1, int color) {}
}
