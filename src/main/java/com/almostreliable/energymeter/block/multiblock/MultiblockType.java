package com.almostreliable.energymeter.block.multiblock;

import com.almostreliable.energymeter.EnergyMeter;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringRepresentable;

import com.google.common.base.Preconditions;

import org.jetbrains.annotations.Nullable;

import java.util.Locale;

public enum MultiblockType implements StringRepresentable {

    NONE(null, null, null, null, "~"),
    SELF(null, null, null, null, "~"),
    NORMAL(SELF, SELF, SELF, SELF, "LRUD"),
    MIDDLE(NONE, NONE, NONE, NONE, ""),
    VERTICAL(NONE, NONE, SELF, SELF, "LR"),
    HORIZONTAL(SELF, SELF, NONE, NONE, "UD"),
    SIDE_U(HORIZONTAL, NONE, NONE, NONE, "U"),
    SIDE_D(NONE, HORIZONTAL, NONE, NONE, "D"),
    SIDE_L(NONE, NONE, VERTICAL, NONE, "L"),
    SIDE_R(NONE, NONE, NONE, VERTICAL, "R"),
    CORNER_U(NORMAL, NONE, SELF, SELF, "LRU"),
    CORNER_D(NONE, NORMAL, SELF, SELF, "LRD"),
    CORNER_L(SELF, SELF, NORMAL, NONE, "LUD"),
    CORNER_R(SELF, SELF, NONE, NORMAL, "RUD"),
    CORNER_UL(CORNER_L, NONE, CORNER_U, NONE, "LU"),
    CORNER_UR(CORNER_R, NONE, NONE, CORNER_U, "RU"),
    CORNER_DL(NONE, CORNER_L, CORNER_D, NONE, "LD"),
    CORNER_DR(NONE, CORNER_R, NONE, CORNER_D, "RD");

    private final @Nullable MultiblockType up;
    private final @Nullable MultiblockType down;
    private final @Nullable MultiblockType left;
    private final @Nullable MultiblockType right;
    private final String posInMultiblock;

    MultiblockType(
        @Nullable MultiblockType up, @Nullable MultiblockType down, @Nullable MultiblockType left, @Nullable MultiblockType right,
        String posInMultiblock
    ) {
        this.up = up;
        this.down = down;
        this.left = left;
        this.right = right;
        this.posInMultiblock = posInMultiblock;
    }

    public static MultiblockType fromPosInMultiblock(String posInMultiblock) {
        for (var value : values()) {
            if (value.posInMultiblock.equals(posInMultiblock)) {
                return value;
            }
        }

        return NORMAL;
    }

    @Override
    public String getSerializedName() {
        return name().toLowerCase(Locale.ROOT);
    }

    public ResourceLocation getTexture() {
        return EnergyMeter.getRL("block/" + getSerializedName());
    }

    public ResourceLocation getFrontTexture() {
        return EnergyMeter.getRL("block/" + getSerializedName() + "_front");
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
