package mchorse.aperture.capabilities.camera;

import net.minecraft.nbt.NBTBase;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;

/**
 * Recording provider
 *
 * Basic version of a capability provider. Most of the code is taken from
 * {@link MorphingProvider}.
 */
public class CameraProvider implements ICapabilitySerializable<NBTBase>
{
    @CapabilityInject(ICamera.class)
    public static final Capability<ICamera> CAMERA = null;

    private ICamera instance = CAMERA.getDefaultInstance();

    @Override
    public boolean hasCapability(Capability<?> capability, EnumFacing facing)
    {
        return capability == CAMERA;
    }

    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing facing)
    {
        return capability == CAMERA ? CAMERA.<T> cast(this.instance) : null;
    }

    @Override
    public NBTBase serializeNBT()
    {
        return CAMERA.getStorage().writeNBT(CAMERA, this.instance, null);
    }

    @Override
    public void deserializeNBT(NBTBase nbt)
    {
        CAMERA.getStorage().readNBT(CAMERA, this.instance, null, nbt);
    }
}