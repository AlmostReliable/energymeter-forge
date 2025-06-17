package testmod.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestInfo;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.entity.SignText;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

@Mixin(GameTestInfo.class)
public abstract class GameTestInfoMixin {

    @Shadow
    @Nullable
    private BlockPos structureBlockPos;

    @Shadow
    @Final
    private ServerLevel level;

    @Shadow
    public abstract Rotation getRotation();

    @Shadow
    public abstract String getTestName();

    @Unique
    @Inject(method = "prepareTestStructure", at = @At("RETURN"))
    private void placeSignWithTestName(CallbackInfoReturnable<GameTestInfo> cir) {
        assert structureBlockPos != null;
        BlockPos blockPos = structureBlockPos.offset(1, 0, -1);
        Rotation rotation = getRotation();

        BlockPos signPos = StructureTemplate.transform(blockPos.offset(0, 1, 0), Mirror.NONE, rotation, blockPos);
        level.setBlockAndUpdate(signPos, Blocks.OAK_SIGN.defaultBlockState().rotate(rotation));

        BlockEntity blockEntity = level.getBlockEntity(signPos);
        if (blockEntity instanceof SignBlockEntity signBlockEntity) {
            SignText signText = energymeter$getSignText();
            signBlockEntity.setText(signText, false);
        }
    }

    @Unique
    @NotNull
    private SignText energymeter$getSignText() {
        String testName = getTestName();
        SignText signText = new SignText();

        if (testName.length() > 15) {
            MutableComponent testNameComponent1 = Component.literal(testName.substring(0, 15));
            signText = signText.setMessage(1, testNameComponent1);
            MutableComponent testNameComponent2 = Component.literal(testName.substring(16, Math.min(testName.length(), 30)));
            signText = signText.setMessage(2, testNameComponent2);

            if (testName.length() > 30) {
                return signText.setMessage(3, Component.literal("..."));
            }
        } else {
            MutableComponent testNameComponent = Component.literal(testName);
            return signText.setMessage(1, testNameComponent);
        }

        return signText;
    }
}
