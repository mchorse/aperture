package mchorse.aperture.camera.values;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import io.netty.buffer.ByteBuf;
import mchorse.aperture.camera.fixtures.PathFixture;
import mchorse.mclib.client.gui.framework.elements.GuiElement;
import mchorse.mclib.config.gui.GuiConfigPanel;
import mchorse.mclib.config.values.IConfigValue;
import mchorse.mclib.config.values.Value;
import mchorse.mclib.utils.Interpolation;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

public class ValueInterpolationType extends Value
{
    private PathFixture.InterpolationType interp = PathFixture.InterpolationType.HERMITE;

    public ValueInterpolationType(String id)
    {
        super(id);
    }

    public PathFixture.InterpolationType get()
    {
        return this.interp;
    }

    public void set(PathFixture.InterpolationType interp)
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
        if (object instanceof PathFixture.InterpolationType)
        {
            this.set((PathFixture.InterpolationType) object);
        }
    }

    @Override
    public void reset()
    {
        this.interp = PathFixture.InterpolationType.HERMITE;
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
        this.interp = PathFixture.InterpolationType.valueOf(element.getAsString());
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

        this.interp = PathFixture.InterpolationType.values()[buffer.readInt()];
    }

    @Override
    public void toBytes(ByteBuf buffer)
    {
        super.toBytes(buffer);

        buffer.writeInt(this.interp.ordinal());
    }
}