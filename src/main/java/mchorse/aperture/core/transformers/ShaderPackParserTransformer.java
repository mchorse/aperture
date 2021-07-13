package mchorse.aperture.core.transformers;

import java.util.Iterator;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

import mchorse.aperture.client.AsmShaderHandler;
import mchorse.aperture.utils.mclib.coremod.ClassMethodTransformer;

public class ShaderPackParserTransformer extends ClassMethodTransformer
{
    public ShaderPackParserTransformer()
    {
        this.setNotch("collectShaderOptions", "(Lnet/optifine/shaders/IShaderPack;Ljava/lang/String;Ljava/util/Map;)V");
        this.setMcp("collectShaderOptions", "(Lnet/optifine/shaders/IShaderPack;Ljava/lang/String;Ljava/util/Map;)V");
    }

    @Override
    public void processMethod(String name, MethodNode method)
    {
        InsnList list = method.instructions;
        Iterator<AbstractInsnNode> it = list.iterator();

        AbstractInsnNode target = null;

        while (it.hasNext())
        {
            AbstractInsnNode node = it.next();

            if (node.getOpcode() == Opcodes.INVOKESTATIC && "getShaderOption".equals(((MethodInsnNode)node).name))
            {
                target = node;

                break;
            }
        }

        if (target != null)
        {
            method.instructions.insertBefore(target, new InsnNode(Opcodes.DUP2));

            list = new InsnList();
            list.add(new VarInsnNode(Opcodes.ALOAD, 2));
            list.add(new MethodInsnNode(Opcodes.INVOKESTATIC, AsmShaderHandler.owner, "getShaderOption", "(Ljava/lang/String;Ljava/lang/String;Lnet/optifine/shaders/config/ShaderOption;Ljava/util/Map;)Lnet/optifine/shaders/config/ShaderOption;", false));

            method.instructions.insert(target, list);
        }
    }
}
