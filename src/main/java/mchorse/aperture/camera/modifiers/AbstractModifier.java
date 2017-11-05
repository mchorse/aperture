package mchorse.aperture.camera.modifiers;

import com.google.gson.JsonObject;
import com.google.gson.annotations.Expose;

import io.netty.buffer.ByteBuf;
import mchorse.aperture.camera.Position;
import mchorse.aperture.camera.fixtures.AbstractFixture;

/**
 * Abstract camera modifier
 * 
 * Camera modifiers are special blocks of logic which post-processes 
 * {@link Position} after it was computed by an {@link AbstractFixture}. 
 */
public abstract class AbstractModifier
{
    /* Types */
    public static final byte SHAKE = 1;

    /**
     * Whether this modifier is enabled 
     */
    @Expose
    public boolean enabled = true;

    public static AbstractModifier readFromByteBuf(ByteBuf buffer)
    {
        try
        {
            AbstractModifier modifier = createFromType(buffer.readByte());

            modifier.fromByteBuf(buffer);

            return modifier;
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return null;
    }

    public static AbstractModifier createFromType(byte type) throws Exception
    {
        if (type == SHAKE) return new ShakeModifier();

        throw new Exception("Modifier with type '" + type + "' not exists!");
    }

    /**
     * Apply modifier on given position
     */
    public abstract void modify(long ticks, AbstractFixture fixture, float partialTick, Position pos);

    public abstract byte getType();

    public void toJSON(JsonObject object)
    {}

    public void fromJSON(JsonObject object)
    {}

    public void toByteBuf(ByteBuf buffer)
    {
        buffer.writeByte(this.getType());
        buffer.writeBoolean(this.enabled);
    }

    public void fromByteBuf(ByteBuf buffer)
    {
        this.enabled = buffer.readBoolean();
    }
}