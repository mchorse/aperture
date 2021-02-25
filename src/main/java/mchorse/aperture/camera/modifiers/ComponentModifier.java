package mchorse.aperture.camera.modifiers;

import mchorse.mclib.config.values.ValueInt;

/**
 * Abstract component modifier
 */
public abstract class ComponentModifier extends AbstractModifier
{
    /**
     * Active value that uses only 7 bits for determining which components
     * should be processed. 
     */
    public final ValueInt active = new ValueInt("active");

    public ComponentModifier()
    {
        super();

        this.register(this.active);
    }

    /**
     * Whether current given bit is 1 
     */
    public boolean isActive(int bit)
    {
        return (this.active.get() >> bit & 1) == 1;
    }
}