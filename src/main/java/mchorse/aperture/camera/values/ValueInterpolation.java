package mchorse.aperture.camera.values;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import io.netty.buffer.ByteBuf;
import mchorse.aperture.camera.data.Angle;
import mchorse.mclib.client.gui.framework.elements.GuiElement;
import mchorse.mclib.config.gui.GuiConfigPanel;
import mchorse.mclib.config.values.Value;
import mchorse.mclib.utils.Interpolation;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

public class ValueInterpolation extends Value
{
    private Interpolation interp = Interpolation.LINEAR;

    public ValueInterpolation(String id)
    {
        super(id);
    }

    public Interpolation get()
    {
        return this.interp;
    }

    public void set(Interpolation interp)
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
        if (object instanceof Interpolation)
        {
            this.set((Interpolation) object);
        }
    }

    @Override
    public void reset()
    {
        this.interp = Interpolation.LINEAR;
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
        this.interp = Interpolation.valueOf(element.getAsString());
    }

    @Override
    public JsonElement toJSON()
    {
        return new JsonPrimitive(this.interp.toString());
    }

    @Override
    public void fromBytes(ByteBuf buffer)
    {
        super.fromBytes(buffer);

        this.interp = Interpolation.values()[buffer.readInt()];
    }

    @Override
    public void toBytes(ByteBuf buffer)
    {
        super.toBytes(buffer);

        buffer.writeInt(this.interp.ordinal());
    }
}