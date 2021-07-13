package mchorse.aperture.core.transformers;

import java.util.Iterator;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TypeInsnNode;

import mchorse.aperture.utils.mclib.coremod.ClassMethodTransformer;

public class ShaderOptionVariableTransformer extends ClassMethodTransformer
{
    public ShaderOptionVariableTransformer()
    {
        this.setNotch("parseOption", "(Ljava/lang/String;Ljava/lang/String;)Lnet/optifine/shaders/config/ShaderOption;");
        this.setMcp("parseOption", "(Ljava/lang/String;Ljava/lang/String;)Lnet/optifine/shaders/config/ShaderOption;");
    }
    
    @Override
    public void processMethod(String name, MethodNode method)
    {
        InsnList list = method.instructions;
        Iterator<AbstractInsnNode> it = list.iterator();

        while (it.hasNext())
        {
            AbstractInsnNode node = it.next();

            if (node.getOpcode() == Opcodes.NEW && "net/optifine/shaders/config/ShaderOptionVariable".equals(((TypeInsnNode) node).desc))
            {
                ((TypeInsnNode)node).desc = "mchorse/aperture/client/AsmShaderHandler$ShaderUniformOption";
            }
            else if (node.getOpcode() == Opcodes.INVOKESPECIAL && "net/optifine/shaders/config/ShaderOptionVariable".equals(((MethodInsnNode) node).owner))
            {
                ((MethodInsnNode)node).owner = "mchorse/aperture/client/AsmShaderHandler$ShaderUniformOption";
            }
        }
    }
}
