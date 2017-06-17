package mchorse.aperture.camera.destination;

import mchorse.aperture.camera.CameraControl;
import mchorse.aperture.camera.CameraProfile;

/**
 * Abstract destination class
 * 
 * This class is going to be used by the camera profile to abstract away where 
 * the camera is going to be stored and saved to.
 */
public abstract class AbstractDestination
{
    protected String filename;

    public AbstractDestination(String filename)
    {
        this.setFilename(filename);
    }

    public String getFilename()
    {
        return this.filename;
    }

    public void setFilename(String filename)
    {
        this.filename = filename;
    }

    /**
     * Save given camera profile
     */
    public abstract void save(CameraProfile profile);

    /**
     * Reload camera profile 
     */
    public abstract void reload(CameraControl control, CameraProfile profile);
}