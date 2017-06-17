package mchorse.aperture.camera.destination;

import java.io.File;
import java.util.Iterator;

import org.apache.commons.io.FileUtils;

import mchorse.aperture.ClientProxy;
import mchorse.aperture.camera.CameraControl;
import mchorse.aperture.camera.CameraProfile;
import mchorse.aperture.camera.CameraUtils;

/**
 * Client destination
 * 
 * Saves and reloads camera prfoile from Aperture's config folder.
 */
public class ClientDestination extends AbstractDestination
{
    public ClientDestination(String filename)
    {
        super(filename);
    }

    @Override
    public void save(CameraProfile profile)
    {
        try
        {
            FileUtils.write(new File(ClientProxy.cameras, this.filename + ".json"), CameraUtils.toJSON(profile));

            /* TODO: inform user about success */
        }
        catch (Exception e)
        {
            /* TODO: inform user about error */
        }
    }

    @Override
    public void reload(CameraControl control, CameraProfile profile)
    {
        try
        {
            FileUtils.readFileToString(new File(ClientProxy.cameras, this.filename + ".json"));
            Iterator<CameraProfile> it = control.profiles.iterator();

            while (it.hasNext())
            {
                CameraProfile old = it.next();
            }
        }
        catch (Exception e)
        {
            /* TODO: inform user about error */
        }
    }
}