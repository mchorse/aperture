package mchorse.aperture.capabilities.camera;

import net.minecraft.entity.player.EntityPlayer;

/**
 * Default implementation of {@link ICamera}
 */
public class Camera implements ICamera
{
    public ItemInfo camera = new ItemInfo();

    public static ICamera get(EntityPlayer player)
    {
        return player.getCapability(CameraProvider.CAMERA, null);
    }

    @Override
    public String currentProfile()
    {
        return this.camera.filename;
    }

    @Override
    public long currentProfileTimestamp()
    {
        return this.camera.timestamp;
    }

    @Override
    public boolean hasProfile()
    {
        return !this.camera.filename.isEmpty();
    }

    @Override
    public void setCurrentProfile(String filename)
    {
        this.camera.filename = filename;
    }

    @Override
    public void setCurrentProfileTimestamp(long timestamp)
    {
        this.camera.timestamp = timestamp;
    }

    /**
     * Item information class
     *
     * Instance of this class is responsible for storing information about a
     * file item like camera profile or recording with timestamp of when
     * it was changed.
     */
    public static class ItemInfo
    {
        public String filename;
        public long timestamp;

        public ItemInfo()
        {
            this("", -1);
        }

        public ItemInfo(String filename, long timestamp)
        {
            this.filename = filename;
            this.timestamp = timestamp;
        }
    }
}