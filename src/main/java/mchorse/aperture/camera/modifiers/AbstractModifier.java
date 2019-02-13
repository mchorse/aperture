package mchorse.aperture.camera.modifiers;

import com.google.gson.JsonObject;
import com.google.gson.annotations.Expose;

import io.netty.buffer.ByteBuf;
import mchorse.aperture.camera.CameraProfile;
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
     * Modify (apply, filter, process, however you name it) modifier on 
     * given position
     * 
     * @param ticks - Amount of ticks from start
     * @param offset - Amount of ticks from current camera fixture
     * @param fixture - Currently running camera fixture
     */
    public abstract void modify(long ticks, long offset, AbstractFixture fixture, float partialTick, CameraProfile profile, Position pos);

    @Override
    public abstract AbstractModifier clone();

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