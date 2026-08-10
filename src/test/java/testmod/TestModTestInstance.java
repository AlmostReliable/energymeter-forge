package testmod;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.Holder;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.GameTestInstance;
import net.minecraft.gametest.framework.TestData;
import net.minecraft.gametest.framework.TestEnvironmentDefinition;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;

import java.util.function.Consumer;

public class TestModTestInstance extends GameTestInstance {

    public static final MapCodec<TestModTestInstance> CODEC = RecordCodecBuilder.mapCodec(
        instance -> instance.group(
            Identifier.CODEC.fieldOf("function").forGetter(TestModTestInstance::function),
            TestData.CODEC.forGetter(TestModTestInstance::info)
        ).apply(instance, TestModTestInstance::new)
    );

    private final Identifier function;

    public TestModTestInstance(Identifier function, TestData<Holder<TestEnvironmentDefinition<?>>> info) {
        super(info);
        this.function = function;
    }

    @Override
    public void run(GameTestHelper helper) {
        Consumer<GameTestHelper> resolved = TestFunctions.get(function);
        if (resolved == null) {
            throw new IllegalStateException("trying to access missing test function: " + function);
        }
        resolved.accept(helper);
    }

    @Override
    public MapCodec<TestModTestInstance> codec() {
        return CODEC;
    }

    @Override
    protected MutableComponent typeDescription() {
        return Component.literal("Test Mod Function Test");
    }

    @Override
    public Component describe() {
        return describeType()
            .append(descriptionRow("test_instance.description.function", function.toString()))
            .append(describeInfo());
    }

    private Identifier function() {
        return function;
    }
}
