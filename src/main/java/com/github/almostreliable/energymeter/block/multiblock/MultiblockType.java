package com.github.almostreliable.energymeter.block.multiblock;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringRepresentable;

import com.github.almostreliable.energymeter.util.Utils;
import com.google.common.base.Preconditions;

import org.jetbrains.annotations.Nullable;

public enum MultiblockType implements StringRepresentable {

    NONE(null, null, null, null),
    SELF(null, null, null, null),
    NORMAL(SELF, SELF, SELF, SELF),
    MIDDLE(NONE, NONE, NONE, NONE),
    VERTICAL(NONE, NONE, SELF, SELF),
    HORIZONTAL(SELF, SELF, NONE, NONE),
    SIDE_U(HORIZONTAL, NONE, NONE, NONE),
    SIDE_D(NONE, HORIZONTAL, NONE, NONE),
    SIDE_L(NONE, NONE, VERTICAL, NONE),
    SIDE_R(NONE, NONE, NONE, VERTICAL),
    CORNER_U(NORMAL, NONE, SELF, SELF),
    CORNER_D(NONE, NORMAL, SELF, SELF),
    CORNER_L(SELF, SELF, NORMAL, NONE),
    CORNER_R(SELF, SELF, NONE, NORMAL),
    CORNER_UL(CORNER_L, NONE, CORNER_U, NONE),
    CORNER_UR(CORNER_R, NONE, NONE, CORNER_U),
    CORNER_DL(NONE, CORNER_L, CORNER_D, NONE),
    CORNER_DR(NONE, CORNER_R, NONE, CORNER_D);

    private final @Nullable MultiblockType up;
    private final @Nullable MultiblockType down;
    private final @Nullable MultiblockType left;
    private final @Nullable MultiblockType right;

    MultiblockType(
        @Nullable MultiblockType up, @Nullable MultiblockType down, @Nullable MultiblockType left, @Nullable MultiblockType right
    ) {
        this.up = up;
        this.down = down;
        this.left = left;
        this.right = right;
    }

    @Override
    public String getSerializedName() {
        return name().toLowerCase();
    }

    public ResourceLocation getTexture() {
        return Utils.getRL("block/" + name().toLowerCase());
    }

    public ResourceLocation getFrontTexture() {
        return Utils.getRL("block/" + name().toLowerCase() + "_front");
    }

    @Nullable
    public ResourceLocation getUpTexture() {
        return getSidedTexture(up);
    }

    @Nullable
    public ResourceLocation getDownTexture() {
        return getSidedTexture(down);
    }

    @Nullable
    public ResourceLocation getLeftTexture() {
        return getSidedTexture(left);
    }

    @Nullable
    public ResourceLocation getRightTexture() {
        return getSidedTexture(right);
    }

    @Nullable
    private ResourceLocation getSidedTexture(@Nullable MultiblockType side) {
        Preconditions.checkNotNull(side, "side for " + this + " must not be null");

        if (side == NONE) return null;
        if (side == SELF) return getTexture();
        return side.getTexture();
    }
}
