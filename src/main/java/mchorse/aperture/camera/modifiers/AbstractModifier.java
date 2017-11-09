package mchorse.aperture.camera.modifiers;

import com.google.gson.JsonObject;
import com.google.gson.annotations.Expose;

import io.netty.buffer.ByteBuf;
import mchorse.aperture.camera.data.Position;
import mchorse.aperture.camera.fixtures.AbstractFixture;

/**
 * Abstract camera modifier
 * 
 * Camera modifiers are special blocks of logic which post-processes 
 * {@link Position} after it was computed by an {@link AbstractFixture}. 
 */
public abstract class AbstractModifier
{
    /**
     * Whether this modifier is enabled 
     */
    @Expose
    public boolean enabled = true;

    /**
     * Apply modifier on given position
     */
    public abstract void modify(long ticks, AbstractFixture fixture, float partialTick, Position pos);

    public void toJSON(JsonObject object)
    {}

    public void fromJSON(JsonObject object)
    {}

    public void toByteBuf(ByteBuf buffer)
    {
        buffer.writeBoolean(this.enabled);
    }

    public void fromByteBuf(ByteBuf buffer)
    {
        this.enabled = buffer.readBoolean();
    }
}