package testmod.test;

import com.almostreliable.energymeter.ModConstants;

import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(ModConstants.MOD_ID)
@PrefixGameTestTemplate(false)
public class FooTest {

    @GameTest(template = "empty_test_structure")
    public void test(GameTestHelper helper) {
        System.out.println("Hello, World!");
        helper.succeed();
    }
}
