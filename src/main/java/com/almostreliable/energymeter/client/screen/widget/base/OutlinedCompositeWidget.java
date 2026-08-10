package com.almostreliable.energymeter.client.screen.widget.base;

import com.almostreliable.energymeter.core.Constants;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.events.ContainerEventHandler;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.layouts.AbstractLayout;
import net.minecraft.client.gui.layouts.Layout;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.client.gui.layouts.LayoutSettings.LayoutSettingsImpl;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public final class OutlinedCompositeWidget extends LayoutPositionedWidget implements ContainerEventHandler, Layout {

    private static final int HEADER_PADDING = 3;
    private static final int HEADER_OFFSET = 4;
    private static final int INNER_PADDING = 4;

    private final Layout layout;
    private final int color;
    private final LayoutSettingsImpl padding;
    private final List<AbstractWidget> children;

    @Nullable
    private GuiEventListener focused;
    private int minWidth;
    private float alignX;

    private OutlinedCompositeWidget(
        Component message, int color, Layout layout, LayoutSettingsImpl padding, List<AbstractWidget> children
    ) {
        super(0, 0, message);
        this.layout = layout;
        this.color = color;
        this.padding = padding;
        this.children = children;
        recalculateDimensions();
    }

    public static OutlinedCompositeWidget ofLayout(Component title, Layout layout, int color) {
        var padding = new LayoutSettingsImpl()
            .padding(INNER_PADDING)
            .paddingTop(INNER_PADDING + Minecraft.getInstance().font.lineHeight - 3);

        var children = new ArrayList<AbstractWidget>();
        layout.visitWidgets(children::add);
        return new OutlinedCompositeWidget(title, color, layout, padding, children);
    }

    public static OutlinedCompositeWidget ofLayout(Component title, Layout layout) {
        return ofLayout(title, layout, Constants.COLOR_WHITE);
    }

    public static OutlinedCompositeWidget ofElement(Component title, LayoutElement element) {
        return ofLayout(title, new SingleElementLayout(element));
    }

    private void recalculateDimensions() {
        var widgetWidth = Math.max(minWidth, layout.getWidth());
        width = widgetWidth + padding.paddingLeft + padding.paddingRight;
        height = layout.getHeight() + padding.paddingTop + padding.paddingBottom;
    }

    @Override
    protected void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        var title = getMessage();
        var headerWidth = font.width(title);
        var x = getX();
        var y = getY();
        var minY = y + font.lineHeight - 3;
        var maxX = x + width;
        var maxY = y + height;

        graphics.fill(x, minY, maxX - HEADER_OFFSET - headerWidth - 2 * HEADER_PADDING, minY + 1, color);
        graphics.fill(maxX - HEADER_OFFSET, minY, maxX, minY + 1, color);
        graphics.fill(x, maxY - 1, maxX, maxY, color);
        graphics.fill(x, minY + 1, x + 1, maxY - 1, color);
        graphics.fill(maxX - 1, minY + 1, maxX, maxY - 1, color);

        graphics.text(
            font,
            title,
            maxX - headerWidth - HEADER_OFFSET - HEADER_PADDING,
            y,
            color,
            false
        );

        for (var child : children) {
            child.extractRenderState(graphics, mouseX, mouseY, partialTick);
        }
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        // delegates the call to all children
        // needed to override the default implementation of the AbstractWidget
        return ContainerEventHandler.super.mouseClicked(event, doubleClick);
    }

    @Override
    public void visitChildren(Consumer<LayoutElement> visitor) {
        layout.visitChildren(visitor);
    }

    @Override
    public void arrangeElements() {
        layout.arrangeElements();
        if (alignX > 0 && minWidth > 0) {
            var layoutWidth = layout.getWidth();
            var x = (int) ((minWidth - layoutWidth) * alignX);
            layout.visitChildren(element -> element.setX(element.getX() + x));
        }
        recalculateDimensions();
    }

    @Override
    public List<? extends GuiEventListener> children() {
        return children;
    }

    @Override
    public boolean isDragging() {
        return false;
    }

    @Override
    public void setDragging(boolean isDragging) {}

    @Nullable
    @Override
    public GuiEventListener getFocused() {
        return focused;
    }

    @Override
    public void setFocused(@Nullable GuiEventListener focused) {
        if (this.focused != null && this.focused == focused && this.focused.isFocused()) return;

        if (this.focused != null && this.focused.isFocused()) {
            this.focused.setFocused(false);
        }

        if (focused != null) {
            focused.setFocused(true);
        }

        this.focused = focused;
    }

    @Override
    public void setX(int x) {
        super.setX(x);
        layout.setX(x + padding.paddingLeft);
    }

    @Override
    public void setY(int y) {
        super.setY(y);
        layout.setY(y + padding.paddingTop);
    }

    public void setMinWidth(int minWidth) {
        this.minWidth = minWidth;
        this.alignX = 0.5f;
    }

    private static final class SingleElementLayout extends AbstractLayout {

        private final LayoutElement element;

        private SingleElementLayout(LayoutElement element) {
            super(0, 0, element.getWidth(), element.getHeight());
            this.element = element;
        }

        @Override
        public void visitChildren(Consumer<LayoutElement> visitor) {
            visitor.accept(element);
        }
    }
}
