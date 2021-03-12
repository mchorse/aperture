package mchorse.aperture.camera.values;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import io.netty.buffer.ByteBuf;
import mchorse.aperture.camera.data.RenderFrame;
import mchorse.mclib.config.values.Value;

import java.util.ArrayList;
import java.util.List;

public class ValueRenderFrames extends Value
{
    private List<List<RenderFrame>> frames = new ArrayList<List<RenderFrame>>();

    public ValueRenderFrames(String id)
    {
        super(id);
    }

    public List<List<RenderFrame>> get()
    {
        return this.frames;
    }

    public void set(List<List<RenderFrame>> frames)
    {
        this.frames = frames;
    }

    @Override
    public Object getValue()
    {
        List<List<RenderFrame>> ticks = new ArrayList<List<RenderFrame>>();

        for (List<RenderFrame> frames : this.frames)
        {
            List<RenderFrame> newFrames = new ArrayList<RenderFrame>();

            for (RenderFrame frame : frames)
            {
                newFrames.add(frame.copy());
            }

            ticks.add(frames);
        }

        return ticks;
    }

    @Override
    public void setValue(Object value)
    {
        this.frames.clear();

        if (value instanceof List)
        {
            for (Object object : (List) value)
            {
                if (object instanceof List)
                {
                    List<RenderFrame> frames = new ArrayList<RenderFrame>();

                    for (Object objectFrame : (List) object)
                    {
                        if (objectFrame instanceof RenderFrame)
                        {
                            frames.add(((RenderFrame) objectFrame).copy());
                        }
                    }

                    this.frames.add(frames);
                }
            }
        }
    }

    @Override
    public void reset()
    {
        this.frames.clear();
    }

    @Override
    public void valueFromJSON(JsonElement jsonElement)
    {
        if (!jsonElement.isJsonArray())
        {
            return;
        }

        this.frames.clear();

        for (JsonElement element : jsonElement.getAsJsonArray())
        {
            if (!element.isJsonArray())
            {
                continue;
            }

            List<RenderFrame> frames = new ArrayList<RenderFrame>();

            for (JsonElement elemElement : element.getAsJsonArray())
            {
                if (elemElement.isJsonObject() || elemElement.isJsonArray())
                {
                    RenderFrame frame = new RenderFrame();

                    frame.fromJSON(elemElement);
                    frames.add(frame);
                }
            }

            this.frames.add(frames);
        }
    }

    @Override
    public JsonElement valueToJSON()
    {
        JsonArray array = new JsonArray();

        for (List<RenderFrame> frames : this.frames)
        {
            JsonArray jsonFrames = new JsonArray();

            for (RenderFrame frame : frames)
            {
                jsonFrames.add(frame.toJSON());
            }

            array.add(jsonFrames);
        }

        return array;
    }

    @Override
    public void copy(Value value)
    {
        if (value instanceof ValueRenderFrames)
        {
            this.setValue(((ValueRenderFrames) value).get());
        }
    }

    @Override
    public void fromBytes(ByteBuf buffer)
    {
        super.fromBytes(buffer);

        this.reset();

        for (int i = 0, c = buffer.readInt(); i < c; i ++)
        {
            List<RenderFrame> tick = new ArrayList<RenderFrame>();

            for (int j = 0, d = buffer.readInt(); j < d; j ++)
            {
                RenderFrame frame = new RenderFrame();

                frame.position(buffer.readDouble(), buffer.readDouble(), buffer.readDouble());
                frame.angle(buffer.readFloat(), buffer.readFloat(), buffer.readFloat(), buffer.readFloat());
                frame.pt = buffer.readFloat();

                tick.add(frame);
            }

            this.frames.add(tick);
        }
    }

    @Override
    public void toBytes(ByteBuf buffer)
    {
        super.toBytes(buffer);

        buffer.writeInt(this.frames.size());

        for (List<RenderFrame> tick : this.frames)
        {
            buffer.writeInt(tick.size());

            for (RenderFrame frame : tick)
            {
                buffer.writeDouble(frame.x);
                buffer.writeDouble(frame.y);
                buffer.writeDouble(frame.z);
                buffer.writeFloat(frame.yaw);
                buffer.writeFloat(frame.pitch);
                buffer.writeFloat(frame.roll);
                buffer.writeFloat(frame.fov);
                buffer.writeFloat(frame.pt);
            }
        }
    }
}
