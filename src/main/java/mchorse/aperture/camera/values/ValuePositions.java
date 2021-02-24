package mchorse.aperture.camera.values;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import io.netty.buffer.ByteBuf;
import mchorse.aperture.camera.data.Position;
import mchorse.mclib.client.gui.framework.elements.GuiElement;
import mchorse.mclib.config.gui.GuiConfigPanel;
import mchorse.mclib.config.values.IConfigValue;
import mchorse.mclib.config.values.Value;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.List;

public class ValuePositions extends Value
{
    private List<Position> position = new ArrayList<Position>();

    public ValuePositions(String id)
    {
        super(id);
    }

    @Override
    public List<IConfigValue> getSubValues()
    {
        List<IConfigValue> list = new ArrayList<IConfigValue>();
        int i = 0;

        for (Position position : this.position)
        {
            list.add(new ValuePosition(this.getId() + "." + i, position));

            i += 1;
        }

        return list;
    }

    public List<Position> get()
    {
        return this.position;
    }

    public void set(List<Position> positions)
    {
        this.position.clear();

        for (Position position : positions)
        {
            this.position.add(position.copy());
        }
    }

    @Override
    public Object getValue()
    {
        List<Position> positions = new ArrayList<Position>();

        for (Position position : this.position)
        {
            positions.add(position.copy());
        }

        return positions;
    }

    @Override
    public void setValue(Object value)
    {
        if (value instanceof List)
        {
            List list = (List) value;

            if (list.isEmpty() || list.get(0) instanceof Position)
            {
                this.set((List<Position>) list);
            }
        }
    }

    @Override
    public void reset()
    {
        this.position.clear();
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
        if (element.isJsonArray())
        {
            this.position.clear();

            JsonArray array = element.getAsJsonArray();

            for (JsonElement child : array)
            {
                if (child.isJsonObject())
                {
                    Position position = new Position();

                    position.fromJSON(child.getAsJsonObject());
                    this.position.add(position);
                }
            }
        }
    }

    @Override
    public JsonElement toJSON()
    {
        JsonArray array = new JsonArray();

        for (Position position : this.position)
        {
            array.add(position.toJSON());
        }

        return array;
    }

    @Override
    public void copy(IConfigValue value)
    {
        if (value instanceof ValuePositions)
        {
            this.set(((ValuePositions) value).get());
        }
    }

    @Override
    public void fromBytes(ByteBuf buffer)
    {
        super.fromBytes(buffer);

        this.position.clear();

        for (int i = 0, c = buffer.readInt(); i < c; i++)
        {
            this.position.add(Position.fromBytes(buffer));
        }
    }

    @Override
    public void toBytes(ByteBuf buffer)
    {
        super.toBytes(buffer);

        buffer.writeInt(this.position.size());

        for (Position position : this.position)
        {
            position.toBytes(buffer);
        }
    }
}