package mchorse.aperture.core.transformers;

import java.util.Iterator;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

import mchorse.aperture.client.AsmRenderingHandler;
import mchorse.aperture.utils.mclib.coremod.ClassTransformer;
import mchorse.aperture.utils.mclib.coremod.CoreClassTransformer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class WorldTransformer extends ClassTransformer
{
    @Override
    public void process(String name, ClassNode node)
    {
        for (MethodNode method : node.methods)
        {
            if (checkName(method, "a", "(Lvg;F)Lbhe;", "getSkyColor", "(Lnet/minecraft/entity/Entity;F)Lnet/minecraft/util/math/Vec3d;") != null ||
                checkName(method, "c", "(F)F", "getCelestialAngle", "(F)F") != null ||
                checkName(method, "e", "(F)Lbhe;", "getCloudColour", "(F)Lnet/minecraft/util/math/Vec3d;") != null ||
                checkName(method, "f", "(F)Lbhe;", "getFogColor", "(F)Lnet/minecraft/util/math/Vec3d;") != null)
            {
                this.process(method);
            }
        }
    }

    private void process(MethodNode method)
    {
        String worldProvider = CoreClassTransformer.get("aym", "net/minecraft/world/WorldProvider");
        InsnList list = method.instructions;
        Iterator<AbstractInsnNode> it = list.iterator();

        while (it.hasNext())
        {
            AbstractInsnNode node = it.next();

            if (node.getOpcode() == Opcodes.INVOKEVIRTUAL && worldProvider.equals(((MethodInsnNode)node).owner))
            {
                MethodInsnNode mnode = (MethodInsnNode) node;
                mnode.setOpcode(Opcodes.INVOKESTATIC);
                mnode.desc = "(L" + mnode.owner + ";" + mnode.desc.substring(1);
                mnode.owner = AsmRenderingHandler.owner;
                
                switch (method.name)
                {
                    case "a":
                        mnode.name = "getSkyColor";
                        break;

                    case "c":
                        mnode.name = "getCelestialAngle";
                        break;

                    case "e":
                        mnode.name = "getCloudColour";
                        break;

                    case "f":
                        mnode.name = "getFogColor";
                        break;

                    default:
                        mnode.name = method.name;
                }
                
                return;
            }
        }
    }
}
