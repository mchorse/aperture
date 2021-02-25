package mchorse.aperture.camera.values;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.netty.buffer.ByteBuf;
import mchorse.aperture.camera.data.Position;
import mchorse.mclib.client.gui.framework.elements.GuiElement;
import mchorse.mclib.config.gui.GuiConfigPanel;
import mchorse.mclib.config.values.IConfigValue;
import mchorse.mclib.config.values.Value;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

public class ValuePosition extends Value
{
    private Position position;
    private ValuePoint pointDelegate;
    private ValueAngle angleDelegate;

    public ValuePosition(String id)
    {
        this(id, new Position());
    }

    public ValuePosition(String id, Position position)
    {
        super(id);

        this.position = position;
        this.pointDelegate = new ValuePoint(id + ".point", this.position.point);
        this.angleDelegate = new ValueAngle(id + ".angle", this.position.angle);
    }

    @Override
    public List<IConfigValue> getSubValues()
    {
        return ImmutableList.of(this.pointDelegate, this.angleDelegate);
    }

    public ValuePoint getPoint()
    {
        return this.pointDelegate;
    }

    public ValueAngle getAngle()
    {
        return this.angleDelegate;
    }

    public Position get()
    {
        return this.position;
    }

    public void set(Position position)
    {
        this.position.set(position);
        this.saveLater();
    }

    @Override
    public Object getValue()
    {
        return this.position.copy();
    }

    @Override
    public void setValue(Object object)
    {
        if (object instanceof Position)
        {
            this.set((Position) object);
        }
    }

    @Override
    public void reset()
    {
        this.position.set(new Position());
    }

    @Override
    public void fromJSON(JsonElement element)
    {
        if (element.isJsonObject())
        {
            this.position.fromJSON((JsonObject) element);
        }
    }

    @Override
    public JsonElement toJSON()
    {
        return this.position.toJSON();
    }

    @Override
    public void copy(IConfigValue value)
    {
        if (value instanceof ValuePosition)
        {
            this.set(((ValuePosition) value).get());
        }
    }

    @Override
    public void fromBytes(ByteBuf buffer)
    {
        super.fromBytes(buffer);

        this.position.set(Position.fromBytes(buffer));
    }

    @Override
    public void toBytes(ByteBuf buffer)
    {
        super.toBytes(buffer);

        this.position.toBytes(buffer);
    }
}
