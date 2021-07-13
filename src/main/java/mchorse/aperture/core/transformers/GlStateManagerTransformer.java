package mchorse.aperture.core.transformers;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

import mchorse.aperture.client.AsmRenderingHandler;
import mchorse.aperture.utils.mclib.coremod.ClassTransformer;

public class GlStateManagerTransformer extends ClassTransformer
{
    @Override
    public void process(String name, ClassNode node)
    {
        for (MethodNode method : node.methods)
        {
            String m = checkName(method, "a", "(F)V", "setFogDensity", "(F)V") != null ? "getFogDensity" : null;
            if (m == null)
            {
                m = checkName(method, "b", "(F)V", "setFogStart", "(F)V") != null ? "getFogStart" : null;
            }
            if (m == null)
            {
                m = checkName(method, "c", "(F)V", "setFogEnd", "(F)V") != null ? "getFogEnd" : null;
            }

            if (m != null)
            {
                InsnList list = new InsnList();
                list.add(new VarInsnNode(Opcodes.FLOAD, 0));
                list.add(new MethodInsnNode(Opcodes.INVOKESTATIC, AsmRenderingHandler.owner, m, "(F)F", false));
                list.add(new VarInsnNode(Opcodes.FSTORE, 0));
                
                method.instructions.insertBefore(method.instructions.getFirst(), list);
            }
        }
    }
}
