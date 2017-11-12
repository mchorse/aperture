package mchorse.aperture.camera.modifiers;

import com.google.gson.annotations.Expose;

import io.netty.buffer.ByteBuf;

/**
 * Abstract component modifier
 */
public abstract class ComponentModifier extends AbstractModifier
{
    /**
     * Byte value that uses only 7 bits for determining which components 
     * should be processed. 
     */
    @Expose
    public byte active;

    /**
     * Whether current given bit is 1 
     */
    public boolean isActive(int bit)
    {
        return (this.active >> bit & 1) == 1;
    }

    @Override
    public void toByteBuf(ByteBuf buffer)
    {
        super.toByteBuf(buffer);

        buffer.writeByte(this.active);
    }

    @Override
    public void fromByteBuf(ByteBuf buffer)
    {
        super.fromByteBuf(buffer);

        this.active = buffer.readByte();
    }
}