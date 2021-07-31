package mchorse.aperture.core;

import mchorse.aperture.core.transformers.GlStateManagerTransformer;
import mchorse.aperture.core.transformers.ShaderOptionVariableConstTransformer;
import mchorse.aperture.core.transformers.ShaderOptionVariableTransformer;
import mchorse.aperture.core.transformers.ShaderPackParserTransformer;
import mchorse.aperture.core.transformers.ShadersTransformer;
import mchorse.aperture.core.transformers.WorldTransformer;
import mchorse.aperture.utils.mclib.coremod.CoreClassTransformer;
import net.minecraftforge.fml.relauncher.FMLLaunchHandler;
import net.minecraftforge.fml.relauncher.Side;

public class APCoreClassTransformer extends CoreClassTransformer
{
    private WorldTransformer world = new WorldTransformer();
    private GlStateManagerTransformer manager = new GlStateManagerTransformer();
    private ShadersTransformer shaders = new ShadersTransformer();
    private ShaderOptionVariableTransformer option = new ShaderOptionVariableTransformer();
    private ShaderOptionVariableConstTransformer constOption = new ShaderOptionVariableConstTransformer();
    private ShaderPackParserTransformer parser = new ShaderPackParserTransformer();

    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass)
    {
        if (FMLLaunchHandler.side() != Side.CLIENT)
        {
            return basicClass;
        }
        
        if (checkName(name, "amu", "net.minecraft.world.World"))
        {
            System.out.println("APCoreMod: Transforming World class (" + name + ")");

            return this.world.transform(name, basicClass);
        }
        else if (checkName(name, "bus", "net.minecraft.client.renderer.GlStateManager"))
        {
            System.out.println("APCoreMod: Transforming GlStateManager class (" + name + ")");

            return this.manager.transform(name, basicClass);
        }
        else if (name.equals("net.optifine.shaders.Shaders"))
        {
            System.out.println("APCoreMod: Transforming Shaders class (" + name + ")");

            return this.shaders.transform(name, basicClass);
        }
        else if (name.equals("net.optifine.shaders.config.ShaderPackParser"))
        {
            System.out.println("APCoreMod: Transforming ShaderPackParser class (" + name + ")");

            return this.parser.transform(name, basicClass);
        }
        else if (name.equals("net.optifine.shaders.config.ShaderOptionVariable"))
        {
            System.out.println("APCoreMod: Transforming ShaderOptionVariable class (" + name + ")");

            return this.option.transform(name, basicClass);
        }
        else if (name.equals("net.optifine.shaders.config.ShaderOptionVariableConst"))
        {
            System.out.println("APCoreMod: Transforming ShaderOptionVariableConst class (" + name + ")");

            return this.constOption.transform(name, basicClass);
        }

        return basicClass;
    }
}
