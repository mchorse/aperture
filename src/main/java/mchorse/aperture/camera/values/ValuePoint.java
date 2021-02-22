package mchorse.aperture.camera.values;

import com.google.gson.JsonElement;
import io.netty.buffer.ByteBuf;
import mchorse.aperture.camera.data.Angle;
import mchorse.aperture.camera.data.Point;
import mchorse.mclib.client.gui.framework.elements.GuiElement;
import mchorse.mclib.config.gui.GuiConfigPanel;
import mchorse.mclib.config.values.Value;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

public class ValuePoint extends Value
{
    public Point point;

    public ValuePoint(String id, Point Point)
    {
        super(id);

        this.point = Point;
    }

    public Point get()
    {
        return this.point;
    }

    public void set(Point point)
    {
        this.point.set(point);
    }

    @Override
    public Object getValue()
    {
        return this.point.copy();
    }

    @Override
    public void setValue(Object object)
    {
        if (object instanceof Point)
        {
            this.point.set((Point) object);
        }
    }

    @Override
    public void reset()
    {}

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
        this.point.fromJSON(element.getAsJsonObject());
    }

    @Override
    public JsonElement toJSON()
    {
        return this.point.toJSON();
    }

    @Override
    public void fromBytes(ByteBuf buffer)
    {
        super.fromBytes(buffer);

        this.point.set(Point.fromBytes(buffer));
    }

    @Override
    public void toBytes(ByteBuf buffer)
    {
        super.toBytes(buffer);

        this.point.toBytes(buffer);
    }
}
