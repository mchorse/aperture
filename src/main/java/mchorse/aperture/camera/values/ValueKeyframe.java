package mchorse.aperture.camera.values;

import com.google.gson.JsonElement;
import io.netty.buffer.ByteBuf;
import mchorse.mclib.client.gui.framework.elements.GuiElement;
import mchorse.mclib.config.gui.GuiConfigPanel;
import mchorse.mclib.config.values.IConfigValue;
import mchorse.mclib.config.values.Value;
import mchorse.mclib.utils.keyframes.Keyframe;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

public class ValueKeyframe extends Value
{
    private Keyframe keyframe;

    public ValueKeyframe(String id, Keyframe keyframe)
    {
        super(id);

         this.keyframe = keyframe;
    }

    public Keyframe get()
    {
        return this.keyframe;
    }

    public void set(Keyframe channel)
    {
        this.keyframe.copy(channel);
    }

    @Override
    public List<IConfigValue> getSubValues()
    {
        return super.getSubValues();
    }

    @Override
    public Object getValue()
    {
        Keyframe channel = new Keyframe();

        channel.copy(this.keyframe);

        return channel;
    }

    @Override
    public void setValue(Object value)
    {
        if (value instanceof Keyframe)
        {
            this.set((Keyframe) value);
        }
    }

    @Override
    public void reset()
    {
        this.keyframe.copy(new Keyframe());
    }

    @Override
    public void resetServer()
    {}

    @Override
    @SideOnly(Side.CLIENT)
    public List<GuiElement> getFields(Minecraft minecraft, GuiConfigPanel guiConfigPanel)
    {
        return null;
    }

    @Override
    public void fromJSON(JsonElement element)
    {
        if (element.isJsonObject())
        {
            this.keyframe.fromJSON(element.getAsJsonObject());
        }
    }

    @Override
    public JsonElement toJSON()
    {
        return this.keyframe.toJSON();
    }

    @Override
    public void copy(IConfigValue value)
    {
        if (value instanceof ValueKeyframe)
        {
            this.set(((ValueKeyframe) value).get());
        }
    }

    @Override
    public void fromBytes(ByteBuf buffer)
    {
        super.fromBytes(buffer);

        this.keyframe.fromBytes(buffer);
    }

    @Override
    public void toBytes(ByteBuf buffer)
    {
        super.toBytes(buffer);

        this.keyframe.toBytes(buffer);
    }
}