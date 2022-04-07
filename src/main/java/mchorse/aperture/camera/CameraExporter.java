package mchorse.aperture.camera;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import info.ata4.minecraft.minema.Minema;
import info.ata4.minecraft.minema.MinemaAPI;
import info.ata4.minecraft.minema.client.event.MinemaEventbus;
import info.ata4.minecraft.minema.client.modules.modifiers.TimerModifier;
import info.ata4.minecraft.minema.client.modules.video.vr.CubeFace;
import mchorse.aperture.utils.EntitySelector;
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
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

import javax.vecmath.Matrix4d;
import javax.vecmath.Vector3d;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.DoubleBuffer;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * This is an exporter for camera, entity and morph tracking data
 * @author Christian F. (known as Chryfi)
 */
public class CameraExporter
{
    /* Matrix Variables to calculate transformations */
    private final DoubleBuffer buffer = BufferUtils.createDoubleBuffer(16);
    private final double[] doubles = new double[16];
    private final Matrix4d modelview = new Matrix4d();
    private final Matrix4d camera = new Matrix4d();

    private JsonObject wrapper = new JsonObject();
    private JsonArray cameraData = new JsonArray();
    private JsonObject entityData = new JsonObject();
    private JsonObject morphData = new JsonObject();

    private Map<String, TrackingPacket> registeredTrackingPackets = new HashMap<>();

    /**
     * 1 to 1 relationship
     * Key is the name of the entity (indexed if duplicate)
     * Value is the UUID.toString() of the entity
     */
    private BiMap<String, String> entityNameRemaps = HashBiMap.create();

    private double[] trackingInitialPos = {0,0,0};
    private Set<Entity> entities = new HashSet<>();
    private CameraRunner runner;

    /* run variables */
    private int frame;
    private int heldframes; //to determine double frames with minema's held frames
    private double motionblurFrames;
    private boolean building = false;
    private boolean trackedCamera = false;

    /* user defined variables */
    private boolean relativeOrigin = false;
    private String selector; //for entities

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
     * Sets the entity selector String and then tries to find these entities.
     * @param selector
     */
    public void setEntitiesSelector(String selector)
    {
        this.selector = selector;
    }

    public String getEntitiesSelector()
    {
        return this.selector;
    }

    public int getFrame()
    {
        return (int) Math.floor(this.frame / this.motionblurFrames);
    }

    public boolean isTracking()
    {
        return this.building;
    }

    public void setRelativeOrigin(boolean relativeOrigin)
    {
        this.relativeOrigin = relativeOrigin;
    }

    private void readMVP()
    {
        this.buffer.clear();

        GL11.glGetDouble(GL11.GL_MODELVIEW_MATRIX, this.buffer);
        this.buffer.get(this.doubles);
        this.modelview.set(this.doubles);
        this.modelview.transpose();
    }

    /**
     * reset the class variables to default values - standby for next export process
     */
    public void reset()
    {
        this.buffer.clear();

        for (int i = 0; i < this.doubles.length; i++)
        {
            this.doubles[i] = 0;
        }

        this.modelview.setIdentity();
        this.camera.setIdentity();

        this.wrapper = new JsonObject();
        this.cameraData = new JsonArray();
        this.entityData = new JsonObject();
        this.morphData = new JsonObject();

        this.registeredTrackingPackets.forEach((key, value) ->
        {
            value.reset(); //let the morph know, to delete this tracker
        });

        this.registeredTrackingPackets.clear(); //avoid memory leak
        this.entityNameRemaps.clear();

        if (!this.relativeOrigin)
        {
            this.trackingInitialPos[0] = 0;
            this.trackingInitialPos[1] = 0;
            this.trackingInitialPos[2] = 0;
        }

        this.building = false;
        this.frame = 0;
        this.motionblurFrames = 0;
        this.heldframes = 0;
        this.runner = null;
        this.trackedCamera = false;
    }

    /**
     * Adds the tracking packet to this class.
     * @param tracker
     * @return true if the tracker was added
     */
    public boolean addTracker(TrackingPacket tracker)
    {
        if (!tracker.combiningMorphs)
        {
            this.registerTracker(tracker);
        }
        else
        {
            int endFrame = this.getFrame() - 100;

            /* if the tracker has been tracked already, check the end frame */
            if (this.registeredTrackingPackets.containsKey(tracker.name))
            {
                JsonArray frames = this.morphData.getAsJsonArray(tracker.name);

                if (frames.size() > 0)
                {
                    int startFrame = frames.get(0).getAsJsonObject().get("frame").getAsInt();
                    endFrame = startFrame + frames.size() - 1;
                }
            }

            if (this.getFrame() - 1 == endFrame)
            {
                tracker.trackingData = this.morphData.getAsJsonArray(tracker.name);
            }
            else
            {
                /* if it cant append the tracking data or if it was not yet tracked */
                this.registerTracker(tracker);
            }
        }

        return true;
    }

    /**
     * Checks for duplicate names and composes unique index names if duplicates are presents.
     * It adds the tracking name and the tracker to the hashmap of registered trackers and ot the jsonObjet for the tracking export
     * @param tracker
     */
    private void registerTracker(TrackingPacket tracker)
    {
        tracker.name = this.checkDuplicateName(tracker.name, this.registeredTrackingPackets);

        this.morphData.add(tracker.name, tracker.trackingData);
        this.registeredTrackingPackets.put(tracker.name, tracker);
    }

    /**
     * This method checks the Map for duplicate keys.
     * In case the String already exists as a key, it composes a unique indexed name, Syntax: name.number
     * @param name name of the tracker
     * @param map the Map which should be checked
     * @return either an indexed name, like "name.1" if it found one duplicate, or if it didn't find a duplicate it returns the original name
     */
    private String checkDuplicateName(String name, Map map)
    {
        int counter = 0;

        while (map.containsKey(name + ((counter == 0) ? "" : "." + counter)))
        {
            counter++;
        }

        return name + ((counter == 0) ? "" : "." + counter);
    }

    @Optional.Method(modid = Minema.MODID)
    public void start(CameraRunner runner)
    {
        if (this.building)
        {
            return;
        }

        this.wrapper.add("information", this.getHeaderInformation());

        this.wrapper.add("camera_tracking", this.cameraData);

        this.wrapper.add("entity_tracking", this.entityData);

        this.wrapper.add("morph_tracking", this.morphData);

        this.runner = runner;
        this.building = true;
        this.motionblurFrames = Math.round(Minema.instance.getConfig().getFrameRate()) / ((int) Minema.instance.getConfig().frameRate.get().doubleValue());

        MinemaEventbus.cameraBUS.registerListener((e) -> this.addCameraFrame());
        MinemaEventbus.endRenderBUS.registerListener((e) -> this.frameEnd());
    }

    /**
     * Checks if the current frame should be skipped.
     * @return true if this is not building or heldframes > 1 or !TimerModifier.isFirstFrame() && !TimerModifier.canRecord() (for motionblur frames)
     */
    @Optional.Method(modid = Minema.MODID)
    public boolean skipFrame()
    {
        boolean ignoreMotionblurFrame = this.frame % ((int) this.motionblurFrames) != 0;

        return !this.building || this.heldframes < Minema.instance.getConfig().heldFrames.get() || ignoreMotionblurFrame || TimerModifier.getCubeFace() != CubeFace.FRONT;
    }

    /**
     * Execute this method at the end of a frame
     * It updates the frame counter and tracks entities.
     */
    @Optional.Method(modid = Minema.MODID)
    public void frameEnd()
    {
        this.addEntitiesData(Minecraft.getMinecraft().getRenderPartialTicks());

        this.frame = (this.heldframes >= Minema.instance.getConfig().heldFrames.get()) ? this.frame + 1 : this.frame;
    }

    /**
     * Update the heldframes counter. Do this at the beginning of a frame.
     * A frame should be rendered at the last heldframe.
     */
    @Optional.Method(modid = Minema.MODID)
    private void updateHeldFrames()
    {
        int minemaHF = Minema.instance.getConfig().heldFrames.get();

        this.heldframes = (this.heldframes < minemaHF) ? this.heldframes + 1 : 1;
    }

    @Optional.Method(modid = Minema.MODID)
    public JsonObject getHeaderInformation()
    {
        JsonObject information = new JsonObject();
        JsonArray resolution = new JsonArray();

        resolution.add(Minema.instance.getConfig().frameWidth.get());
        resolution.add(Minema.instance.getConfig().frameHeight.get());

        information.add("fps", new JsonPrimitive(Minema.instance.getConfig().frameRate.get()));

        information.add("motionblur_fps", new JsonPrimitive(Minema.instance.getConfig().frameRate.get())); //new JsonPrimitive(Minema.instance.getConfig().getFrameRate()));

        information.add("dynamic_fov", new JsonPrimitive(false)); //new JsonPrimitive(OptifineHelper.dynamicFov()));
        information.add("resolution", resolution);
        information.add("held_frames", new JsonPrimitive(Minema.instance.getConfig().heldFrames.get()));
        information.add("required_import_version", new JsonPrimitive(160));

        return information;
    }

    public void track(TrackingPacket tracker, EntityLivingBase entity, float partialTicks)
    {
        if (this.skipFrame())
        {
            return;
        }

        /* only track morph if the camera was tracked first */
        if (!this.trackedCamera)
        {
            return;
        }

        Matrix4d rotation;
        Vector3d pos = new Vector3d();
        Vector3d scale = new Vector3d();

        Matrix4d[] transformation = MatrixUtils.getTransformation();

        pos.x = transformation[0].m03;
        pos.y = transformation[0].m13;
        pos.z = transformation[0].m23;

        rotation = transformation[1];

        scale.x = transformation[2].m00;
        scale.y = transformation[2].m11;
        scale.z = transformation[2].m22;

        JsonObject frame = new JsonObject();
        JsonArray positionData = new JsonArray();
        JsonArray rotationData = new JsonArray();
        JsonArray scaleData = new JsonArray();

        /* position data */
        positionData.add(pos.x - this.trackingInitialPos[0]);
        positionData.add(pos.y - this.trackingInitialPos[1]);
        positionData.add(pos.z - this.trackingInitialPos[2]);

        /* rotation matrix data */
        JsonArray jsonRotX = new JsonArray();
        jsonRotX.add(rotation.m00);
        jsonRotX.add(rotation.m01);
        jsonRotX.add(rotation.m02);

        JsonArray jsonRotY = new JsonArray();
        jsonRotY.add(rotation.m10);
        jsonRotY.add(rotation.m11);
        jsonRotY.add(rotation.m12);

        JsonArray jsonRotZ = new JsonArray();
        jsonRotZ.add(rotation.m20);
        jsonRotZ.add(rotation.m21);
        jsonRotZ.add(rotation.m22);

        rotationData.add(jsonRotX);
        rotationData.add(jsonRotY);
        rotationData.add(jsonRotZ);

        /* scale data */
        scaleData.add(scale.x);
        scaleData.add(scale.y);
        scaleData.add(scale.z);

        if (tracker.trackingData.size() == 0)
        {
            frame.addProperty("frame",  this.getFrame());
        }

        frame.add("position", positionData);
        frame.add("rotation", rotationData);
        frame.add("scale", scaleData);

        tracker.trackingData.add(frame);
    }

    private void addEntitiesData(float partialTick)
    {
        if (this.skipFrame())
        {
            return;
        }

        /* update entities */
        this.tryFindingEntity();

        for (Entity entity : this.entities)
        {
            JsonArray frameArray = this.addEntityTracker(entity);

            double x = Interpolations.lerp(entity.lastTickPosX, entity.posX, partialTick);
            double y = Interpolations.lerp(entity.lastTickPosY, entity.posY, partialTick);
            double z = Interpolations.lerp(entity.lastTickPosZ, entity.posZ, partialTick);

            JsonObject frame = new JsonObject();
            JsonArray positionData = new JsonArray();
            JsonArray angleData = new JsonArray();

            positionData.add(x - this.trackingInitialPos[0]);
            positionData.add(y - this.trackingInitialPos[1]);
            positionData.add(z - this.trackingInitialPos[2]);

            if (frameArray.size() == 0)
            {
                frame.addProperty("frame",  this.getFrame());
            }

            frame.add("position", positionData);

            if (entity instanceof EntityLivingBase)
            {
                double bodyYaw = ((EntityLivingBase) entity).prevRenderYawOffset + (((EntityLivingBase) entity).renderYawOffset - ((EntityLivingBase) entity).prevRenderYawOffset) * partialTick;

                angleData.add(0);
                angleData.add(bodyYaw);
                angleData.add(0);
                frame.add("body_rotation", angleData);
            }

            frameArray.add(frame);
        }
    }

    /**
     * Searches whether the entity has been tracked already.
     * @param entity
     * @return the jsonArray of the already tracked entity or a new jsonArray and adds it to the entityData with a unique name
     */
    private JsonArray addEntityTracker(Entity entity)
    {
        String name = this.entityNameRemaps.inverse().get(entity.getCachedUniqueIdString());
        JsonArray frameArray;

        if (name != null)
        {
            frameArray = this.entityData.getAsJsonArray(name);
        }
        else
        {
            /* The entity has not been tracked yet */

            name = this.checkDuplicateName(entity.getName(), this.entityNameRemaps);
            frameArray = new JsonArray();

            /* add the entity to the json structure and to the name remaps */
            this.entityData.add(name, frameArray);
            this.entityNameRemaps.put(name, entity.getCachedUniqueIdString());
        }

        return frameArray;
    }

    @Optional.Method(modid = Minema.MODID)
    private void addCameraFrame()
    {
        this.updateHeldFrames();

        if (this.skipFrame())
        {
            return;
        }

        this.readMVP();
        this.camera.set(this.modelview);

        this.trackedCamera = true;

        Matrix4d translation = new Matrix4d();
        Minecraft mc = Minecraft.getMinecraft();
        Entity renderEntity = mc.getRenderViewEntity();

        double x = Interpolations.lerp(renderEntity.lastTickPosX, renderEntity.posX, mc.getRenderPartialTicks());
        double y = Interpolations.lerp(renderEntity.lastTickPosY, renderEntity.posY, mc.getRenderPartialTicks());
        double z = Interpolations.lerp(renderEntity.lastTickPosZ, renderEntity.posZ, mc.getRenderPartialTicks());

        translation.setIdentity();
        translation.setTranslation(new Vector3d(-x, -y, -z));
        this.modelview.mul(translation);
        this.modelview.invert();

        Vector3d pos = new Vector3d(this.modelview.m03, this.modelview.m13, this.modelview.m23);

        if (this.frame == 0)
        {
            if (!this.relativeOrigin)
            {
                this.trackingInitialPos[0] = pos.x;
                this.trackingInitialPos[1] = pos.y;
                this.trackingInitialPos[2] = pos.z;
            }
        }

        JsonObject frame = new JsonObject();
        JsonArray positionData = new JsonArray();
        JsonArray angleData = new JsonArray();

        positionData.add(pos.x - this.trackingInitialPos[0]);
        positionData.add(pos.y - this.trackingInitialPos[1]);
        positionData.add(pos.z - this.trackingInitialPos[2]);

        angleData.add(this.runner.getPosition().angle.fov);
        angleData.add(this.runner.getPosition().angle.roll);
        angleData.add(this.runner.getPosition().angle.yaw);
        angleData.add(this.runner.getPosition().angle.pitch);

        frame.add("position", positionData);
        frame.add("angle", angleData);

        this.cameraData.add(frame);
    }

    @Optional.Method(modid = Minema.MODID)
    public void exportTrackingData(String filename)
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

    /**
     * Try finding entity based on entity selector or target's UUID
     */
    public void tryFindingEntity()
    {
        this.entities.clear();

        if (this.selector != null && !this.selector.isEmpty() && FMLCommonHandler.instance().getSide() == Side.CLIENT)
        {
            String[] selectorArray = this.selector.split(" - ");

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

    public static class TrackingPacket
    {
        private String name;
        private JsonArray trackingData = new JsonArray();
        private boolean reset = false;
        private boolean combiningMorphs;

        public TrackingPacket(String name, boolean combiningMorphs)
        {
            this.name = name;
            this.combiningMorphs = combiningMorphs;
        }

        public boolean isReset()
        {
            return this.reset;
        }

        public String getName()
        {
            return this.name;
        }

        private void reset()
        {
            this.trackingData = new JsonArray();
            this.reset = true; //morph should check for this value to delete the packet
        }
    }
}
