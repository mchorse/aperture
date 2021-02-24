package mchorse.aperture.camera.values;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import io.netty.buffer.ByteBuf;
import mchorse.aperture.camera.data.InterpolationType;
import mchorse.mclib.client.gui.framework.elements.GuiElement;
import mchorse.mclib.config.gui.GuiConfigPanel;
import mchorse.mclib.config.values.IConfigValue;
import mchorse.mclib.config.values.Value;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

public class ValueInterpolationType extends Value
{
    private InterpolationType interp = InterpolationType.HERMITE;

    public ValueInterpolationType(String id)
    {
        super(id);
    }

    public InterpolationType get()
    {
        return this.interp;
    }

    public void set(InterpolationType interp)
    {
        this.interp = interp;
        this.saveLater();
    }

    @Override
    public Object getValue()
    {
        return this.interp;
    }

    @Override
    public void setValue(Object object)
    {
        if (object instanceof InterpolationType)
        {
            this.set((InterpolationType) object);
        }
    }

    @Override
    public void reset()
    {
        this.interp = InterpolationType.HERMITE;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public List<GuiElement> getFields(Minecraft minecraft, GuiConfigPanel guiConfigPanel)
    {
        return null;
    }

    @Override
    public void fromJSON(JsonElement element)
    {
        this.interp = InterpolationType.valueOf(element.getAsString());
    }

    @Override
    public JsonElement toJSON()
    {
        return new JsonPrimitive(this.interp.toString());
    }

    @Override
    public void copy(IConfigValue value)
    {
        if (value instanceof ValueInterpolationType)
        {
            this.set(((ValueInterpolationType) value).get());
        }
    }

    @Override
    public void fromBytes(ByteBuf buffer)
    {
        super.fromBytes(buffer);

        this.interp = InterpolationType.values()[buffer.readInt()];
    }

    @Override
    public void toBytes(ByteBuf buffer)
    {
        super.toBytes(buffer);

        buffer.writeInt(this.interp.ordinal());
    }
}