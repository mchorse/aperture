package mchorse.aperture.camera;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import info.ata4.minecraft.minema.Minema;
import info.ata4.minecraft.minema.MinemaAPI;
import mchorse.aperture.camera.data.Position;
import mchorse.aperture.camera.minema.MinemaIntegration;
import mchorse.aperture.utils.EntitySelector;
import mchorse.aperture.utils.OptifineHelper;
import mchorse.mclib.utils.Interpolations;
import mchorse.mclib.utils.MatrixUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.vecmath.*;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

/**
 * This is an exporter for camera, entity and morph tracking data
 * @author Christian F. (known as Chryfi)
 */
public class CameraExporter
{
    private boolean relativeOrigin = false;
    private boolean globalCoordinates = false;
    private JsonObject wrapper = new JsonObject();
    private JsonArray trackingData = new JsonArray();
    private JsonObject entityData = new JsonObject();
    private JsonObject morphData = new JsonObject();
    private HashMap<String, TrackingPacket> morphNames = new HashMap<>();
    private double[] trackingInitialPos = {0,0,0};
    private int heldframes = 0; //to determine double frames with minema's held frames
    private int heldframesEntity = 0; //to determine double frames with minema's held frames for entities (maybe redundant?)
    private List<Entity> entities = new ArrayList<>();
    private static Matrix4f matrix = new Matrix4f();
    private int frame = 0;

    public boolean building = false;
    public String selector; //for entities

    public void setRelativeOrigin(boolean state)
    {
        this.relativeOrigin = state;
    }

    public void setGlobalCoordinates(boolean state)
    {
        this.globalCoordinates = state;
    }

    public void setOriginX(double x)
    {
        this.trackingInitialPos[0] = x;
    }

    public void setOriginY(double y)
    {
        this.trackingInitialPos[1] = y;
    }

    public void setOriginZ(double z)
    {
        this.trackingInitialPos[2] = z;
    }

    /**
     * @param tracker
     * @return
     */
    public boolean addTracker(TrackingPacket tracker)
    {
        String number = "0";

        while(this.morphNames.containsKey(tracker.name+((number.equals("0")) ? "" : "."+number)))
        {
            number = Integer.toString(Integer.valueOf(number)+1);
        }

        tracker.name += !number.equals("0") ? "."+number : "";

        this.morphData.add(tracker.name, tracker.trackingData);
        this.morphNames.put(tracker.name, tracker);

        return true;
    }

    /**
     * reset the class variables to default values - standby for next export process
     */
    public void reset()
    {
        this.trackingData = new JsonArray();
        this.entityData = new JsonObject();
        this.morphData = new JsonObject();
        this.wrapper = new JsonObject();

        this.morphNames.forEach((key, value) ->
        {
            value.reset(); //let the morph know, to delete this tracker
        });

        this.morphNames.clear(); //avoid memory leak

        if (!this.relativeOrigin)
        {
            this.trackingInitialPos[0] = 0;
            this.trackingInitialPos[1] = 0;
            this.trackingInitialPos[2] = 0;
        }

        this.building = false;
        this.frame = 0;
        this.heldframes = 0;
        this.heldframesEntity = 0;
    }

    @Optional.Method(modid = Minema.MODID)
    public void build(Position position, float partialTick)
    {
        this.building = true;

        if (this.frame == 0) //execute once at the beginning of making the json structure
        {
            if (MinemaIntegration.isAvailable())
            {
                this.wrapper.add("information", getHeaderInformation());
            }

            this.wrapper.add("camera_tracking", this.trackingData);

            if(!this.entities.isEmpty())
            {
                this.wrapper.add("entity_tracking", this.entityData);
            }

            this.wrapper.add("morph_tracking", this.morphData);

            if (!this.relativeOrigin && !this.globalCoordinates)
            {
                this.trackingInitialPos[0] = position.point.x;
                this.trackingInitialPos[1] = position.point.y + 1.62;
                this.trackingInitialPos[2] = position.point.z;
            }
        }

        addCameraFrame(position);
        addEntitiesData(partialTick);

        this.frame = (this.heldframes == 1) ? this.frame + 1 : this.frame;
    }

    private JsonObject getHeaderInformation()
    {
        JsonObject information = new JsonObject();
        JsonArray resolution = new JsonArray();

        resolution.add(Minema.instance.getConfig().frameWidth.get());
        resolution.add(Minema.instance.getConfig().frameHeight.get());

        information.add("fps", new JsonPrimitive(Minema.instance.getConfig().frameRate.get()));

        try //try whether Minema 3.6+ is used
        {
            information.add("motionblur_fps", new JsonPrimitive(Minema.instance.getConfig().getFrameRate()));
        }
        catch(Exception e) {}

        information.add("dynamic_fov", new JsonPrimitive(OptifineHelper.dynamicFov()));
        information.add("resolution", resolution);
        information.add("held_frames", new JsonPrimitive(Minema.instance.getConfig().heldFrames.get()));
        information.add("required_import_version", new JsonPrimitive(120));

        return information;
    }

    public void track(TrackingPacket tracker, EntityLivingBase entity, float partialTicks)
    {
        JsonObject frame = new JsonObject();
        JsonArray positionData = new JsonArray();
        JsonArray rotationData = new JsonArray();

        MatrixUtils.Transformation modelView = MatrixUtils.extractTransformations(MatrixUtils.matrix, MatrixUtils.readModelView(matrix));

        Vector3d pos = new Vector3d(modelView.getTranslation3f());
        Matrix4f rotation = new Matrix4f();
        Matrix4f rotTmp = new Matrix4f();
        Matrix4f dummy = new Matrix4f();

        rotTmp.setIdentity();
        rotation.setIdentity();
        dummy.setIdentity();

        rotation.m00 = modelView.getRotation3f().m00;
        rotation.m01 = modelView.getRotation3f().m01;
        rotation.m02 = modelView.getRotation3f().m02;
        rotation.m10 = modelView.getRotation3f().m10;
        rotation.m11 = modelView.getRotation3f().m11;
        rotation.m12 = modelView.getRotation3f().m12;
        rotation.m20 = modelView.getRotation3f().m20;
        rotation.m21 = modelView.getRotation3f().m21;
        rotation.m22 = modelView.getRotation3f().m22;

        /*convert minecraft to blender axis
        rotTmp.rotZ((float)Math.toRadians(90));
        rotTmp.rotX((float)Math.toRadians(90));
        rotTmp.mul(rotation);
        rotation.set(rotTmp);*/

        MatrixUtils.Transformation transformation = new MatrixUtils.Transformation(dummy, rotation, dummy);
        Vector3f rot = transformation.getRotation(MatrixUtils.Transformation.RotationOrder.ZYX);


        pos.add(new Vector3d(Interpolations.lerp(entity.prevPosX, entity.posX, partialTicks),
                             Interpolations.lerp(entity.prevPosY, entity.posY, partialTicks),
                             Interpolations.lerp(entity.prevPosZ, entity.posZ, partialTicks)));

        if (tracker.trackingData.size() == 0)
        {
            frame.addProperty("frame",  this.frame-1);
        }

        positionData.add(pos.x - this.trackingInitialPos[0]);
        positionData.add(pos.y - this.trackingInitialPos[1]);
        positionData.add(pos.z - this.trackingInitialPos[2]);

        rotationData.add(rot.x);
        rotationData.add(rot.y);
        rotationData.add(rot.z);

        frame.add("position", positionData);
        frame.add("rotation", rotationData);

        if (MinemaIntegration.isAvailable())
        {
            if (Minema.instance.getConfig().heldFrames.get()>1)
            {
                tracker.heldframes = (tracker.heldframes<Minema.instance.getConfig().heldFrames.get()) ? tracker.heldframes + 1 : 1;

                if (tracker.heldframes>1)
                {
                    JsonObject prevFrame = tracker.trackingData.get(tracker.trackingData.size()-1).getAsJsonObject();
                    JsonArray prevPositionData = prevFrame.get("position").getAsJsonArray();
                    JsonArray prevAngleData = prevFrame.get("rotation").getAsJsonArray();

                    if (prevAngleData.equals(rotationData) && prevPositionData.equals(positionData))
                    {
                        return;
                    }
                    else
                    {
                        tracker.heldframes = 1;
                    }
                }
            }
        }

        tracker.trackingData.add(frame);
    }

    private void addEntitiesData(float partialTick)
    {
        if (this.checkForDead())
        {
            this.tryFindingEntity(this.selector);
        }

        if (!this.entities.isEmpty() && MinemaIntegration.isAvailable() && Minema.instance.getConfig().heldFrames.get() > 1)
        {
            this.heldframesEntity = (this.heldframesEntity < Minema.instance.getConfig().heldFrames.get()) ? this.heldframesEntity + 1 : 1;
        }

        for (Entity entity : this.entities)
        {
            JsonArray entityFrameArray = new JsonArray();

            if (this.frame == 0) //execute once at the beginning of exporting
            {
                this.entityData.add(entity.getName(), entityFrameArray);
            }

            double x = entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * partialTick;
            double y = entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * partialTick;
            double z = entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * partialTick;

            JsonObject frame = new JsonObject();
            JsonArray positionData = new JsonArray();
            JsonArray angleData = new JsonArray();

            positionData.add(x - this.trackingInitialPos[0]);
            positionData.add(y - this.trackingInitialPos[1]);
            positionData.add(z - this.trackingInitialPos[2]);
            frame.add("position", positionData);

            if (entity instanceof EntityLivingBase)
            {
                double bodyYaw = ((EntityLivingBase) entity).prevRenderYawOffset + (((EntityLivingBase) entity).renderYawOffset - ((EntityLivingBase) entity).prevRenderYawOffset) * partialTick;

                angleData.add(0);
                angleData.add(bodyYaw);
                angleData.add(0);
                frame.add("body_rotation", angleData);
            }

            entityFrameArray = this.entityData.getAsJsonArray(entity.getName());

            if (MinemaIntegration.isAvailable())
            {
                if (Minema.instance.getConfig().heldFrames.get() > 1 && this.heldframesEntity > 1)
                {
                    JsonObject prevFrame = entityFrameArray.get(entityFrameArray.size()-1).getAsJsonObject();
                    JsonArray prevPositionData = prevFrame.get("position").getAsJsonArray();
                    JsonArray prevBodyRotationData = prevFrame.get("body_rotation").getAsJsonArray();

                    boolean angleComparison = true;

                    if (prevBodyRotationData != null && !prevBodyRotationData.equals(angleData))
                    {
                        angleComparison = false;
                    }

                    if (prevPositionData.equals(positionData) && angleComparison)
                    {
                        continue;
                    }
                    else
                    {
                        /*
                         * expects that heldframes has the same effect on every entity each frame
                         * (otherwise you would need individual heldframe checks for every entity
                         */
                        this.heldframesEntity = 1;
                    }
                }
            }

            if(entityFrameArray != null)
            {
                entityFrameArray.add(frame);
            }
        }
    }

    private void addCameraFrame(Position position)
    {
        JsonObject frame = new JsonObject();
        JsonArray positionData = new JsonArray();
        JsonArray angleData = new JsonArray();

        positionData.add(position.point.x - this.trackingInitialPos[0]);
        positionData.add(position.point.y - this.trackingInitialPos[1] + 1.62);
        positionData.add(position.point.z - this.trackingInitialPos[2]);

        angleData.add(position.angle.fov);
        angleData.add(position.angle.roll);
        angleData.add(position.angle.yaw);
        angleData.add(position.angle.pitch);

        frame.add("position", positionData);
        frame.add("angle", angleData);

        if (MinemaIntegration.isAvailable())
        {
            if (Minema.instance.getConfig().heldFrames.get()>1)
            {
                this.heldframes = (this.heldframes<Minema.instance.getConfig().heldFrames.get()) ? this.heldframes + 1 : 1;

                if (this.heldframes>1)
                {
                    JsonObject prevFrame = this.trackingData.get(this.trackingData.size()-1).getAsJsonObject();
                    JsonArray prevPositionData = prevFrame.get("position").getAsJsonArray();
                    JsonArray prevAngleData = prevFrame.get("angle").getAsJsonArray();

                    if (prevAngleData.equals(angleData) && prevPositionData.equals(positionData))
                    {
                        return;
                    }
                    else
                    {
                        this.heldframes = 1;
                    }
                }
            }
        }

        this.trackingData.add(frame);
    }

    @Optional.Method(modid = Minema.MODID)
    public void exportTrackingData(String filename)
    {
        if (MinemaIntegration.isAvailable())
        {
            try
            {
                FileWriter file = new FileWriter(MinemaAPI.getCapturePath().toURI().getPath() + filename);

                file.write(this.wrapper.toString());
                file.close();
                System.out.println("Successfully created the tracking data file.");
            }
            catch (IOException e)
            {
                System.out.println("An error occurred during writing the tracking data file.");
                e.printStackTrace();
            }
        }
    }

    /**
     * Try finding entity based on entity selector or target's UUID
     */
    public void tryFindingEntity(String selector)
    {
        this.entities.clear();

        if (selector != null && !selector.isEmpty() && FMLCommonHandler.instance().getSide() == Side.CLIENT)
        {
            String[] selectorArray = selector.split(" - ");

            this.tryFindingEntityClient(selectorArray);
        }
    }

    @SideOnly(Side.CLIENT)
    private void tryFindingEntityClient(String[] selectorArray)
    {
        EntityPlayer player = Minecraft.getMinecraft().player;

        for (String selector : selectorArray)
        {
            if (!selector.contains("@"))
            {
                selector = "@e[name=" + selector + "]";
            }

            try
            {
                this.entities.addAll(EntitySelector.matchEntities(player, selector, Entity.class));
            }
            catch (Exception e) { }
        }
    }

    /**
     * Check for dead entities
     */
    protected boolean checkForDead()
    {
        if (this.entities.isEmpty())
        {
            return true;
        }

        Iterator<Entity> it = this.entities.iterator();

        while (it.hasNext())
        {
            Entity entity = it.next();

            if (entity.isDead || entity == Minecraft.getMinecraft().player)
            {
                it.remove();
            }
        }

        return this.entities.isEmpty();
    }

    public static class TrackingPacket
    {
        private String name;
        private JsonArray trackingData = new JsonArray();
        private boolean reset = false;
        private int heldframes = 0; //to determine double frames with minema's held frames

        public TrackingPacket(String name)
        {
            this.name = name;
        }

        public void reset()
        {
            this.trackingData = new JsonArray();
            this.reset = true; //morph should check for this value to delete the packet
        }

        public boolean isReset()
        {
            return this.reset;
        }


        public String getName()
        {
            return this.name;
        }

        public void addTrackingData(JsonElement element)
        {
            this.trackingData.add(element);
        }
    }
}
