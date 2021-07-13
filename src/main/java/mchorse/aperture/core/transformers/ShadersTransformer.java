package mchorse.aperture.core.transformers;

import java.util.Iterator;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

import mchorse.aperture.client.AsmShaderHandler;
import mchorse.aperture.utils.mclib.coremod.ClassTransformer;

public class ShadersTransformer extends ClassTransformer
{
    @Override
    public void process(String name, ClassNode node)
    {
        for (MethodNode method : node.methods)
        {
            if (method.name.startsWith("setProgramUniform1"))
            {
                this.processSetProgramUniform(method);
            }
            else if (method.name.equals("useProgram"))
            {
                this.processUseProgram(method);
            }
            else if (method.name.matches("^create(?:Vert|Geom|Frag|Comp)Shader$"))
            {
                this.processCreateShader(method);
            }
            else if (method.name.equals("init"))
            {
                this.processInit(method);
            }
            else if (method.name.equals("loadShaderPack"))
            {
                this.processLoadShaderPack(method);
            }
        }
    }
    
    public void processSetProgramUniform(MethodNode method)
    {
        InsnList list = method.instructions;
        Iterator<AbstractInsnNode> it = list.iterator();

        while (it.hasNext())
        {
            AbstractInsnNode node = it.next();

            if (node.getOpcode() == Opcodes.INVOKEVIRTUAL)
            {
                MethodInsnNode mnode = (MethodInsnNode) node;
                mnode.setOpcode(Opcodes.INVOKESTATIC);
                mnode.name = method.name;
                mnode.desc = method.desc;
                mnode.owner = AsmShaderHandler.owner;
                
                return;
            }
        }
    }
    
    public void processUseProgram(MethodNode method)
    {
        InsnList list = method.instructions;
        Iterator<AbstractInsnNode> it = list.iterator();
        
        AbstractInsnNode target = null;

        while (it.hasNext())
        {
            AbstractInsnNode node = it.next();

            if (node.getOpcode() == Opcodes.LDC && "end useProgram".equals(((LdcInsnNode)node).cst))
            {
                target = node;
                
                break;
            }
        }
        
        if (target != null)
        {
            list.insertBefore(target, new MethodInsnNode(Opcodes.INVOKESTATIC, AsmShaderHandler.owner, "updateOptionUniforms", "()V", false));
        }
    }
    
    public void processCreateShader(MethodNode method)
    {
        InsnList list = method.instructions;
        Iterator<AbstractInsnNode> it = list.iterator();

        MethodInsnNode target = null;

        while (it.hasNext())
        {
            AbstractInsnNode node = it.next();

            if (node.getOpcode() == Opcodes.INVOKESTATIC && "resolveIncludes".equals(((MethodInsnNode)node).name))
            {
                target = (MethodInsnNode) node;
                
                break;
            }
        }
        
        if (target != null)
        {
            if (!target.desc.equals("(Ljava/io/BufferedReader;Ljava/lang/String;Lnet/optifine/shaders/IShaderPack;ILjava/util/List;I)Ljava/io/BufferedReader;"))
            {
                list.insert(target, new MethodInsnNode(Opcodes.INVOKESTATIC, "net/optifine/util/LineBuffer", "readAll", "(Ljava/io/Reader;)Lnet/optifine/util/LineBuffer;", false));
            }
            
            target.name = "getCachedShader";
            target.desc = "(Ljava/lang/Object;Ljava/lang/String;Lnet/optifine/shaders/IShaderPack;ILjava/util/List;I)Ljava/io/BufferedReader;";
            target.owner = AsmShaderHandler.owner;
        }
    }
    
    public void processInit(MethodNode method)
    {

        InsnList list = method.instructions;
        Iterator<AbstractInsnNode> it = list.iterator();
        
        AbstractInsnNode target = null;

        while (it.hasNext())
        {
            AbstractInsnNode node = it.next();

            if (node.getOpcode() == Opcodes.RETURN)
            {
                target = node;
                
                break;
            }
        }
        
        if (target != null)
        {
            list.insertBefore(target, new MethodInsnNode(Opcodes.INVOKESTATIC, AsmShaderHandler.owner, "afterInit", "()V", false));
        }
    }
    
    public void processLoadShaderPack(MethodNode method)
    {
        method.instructions.insert(method.instructions.getFirst(), new MethodInsnNode(Opcodes.INVOKESTATIC, AsmShaderHandler.owner, "loadShaderPack", "()V", false));
    }
}
