package mchorse.aperture.camera;

import mchorse.aperture.Aperture;
import mchorse.aperture.ClientProxy;
import mchorse.aperture.camera.data.InterpolationType;
import mchorse.aperture.camera.data.Position;
import mchorse.aperture.camera.fixtures.AbstractFixture;
import mchorse.aperture.camera.fixtures.CircularFixture;
import mchorse.aperture.camera.fixtures.DollyFixture;
import mchorse.aperture.camera.fixtures.KeyframeFixture;
import mchorse.aperture.camera.fixtures.ManualFixture;
import mchorse.aperture.camera.fixtures.PathFixture;
import mchorse.aperture.camera.smooth.Filter;
import mchorse.aperture.camera.smooth.SmoothCamera;
import mchorse.aperture.client.KeyboardHandler;
import mchorse.aperture.client.gui.GuiCameraEditor;
import mchorse.aperture.client.gui.panels.GuiAbstractFixturePanel;
import mchorse.aperture.client.gui.panels.GuiManualFixturePanel;
import mchorse.aperture.utils.TimeUtils;
import mchorse.mclib.utils.Color;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.PlayerTickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

import javax.vecmath.Vector2d;

/**
 * Profile path renderer
 *
 * This class is responsible for rendering current loaded profile.
 */
@SideOnly(Side.CLIENT)
public class CameraRenderer
{
    /**
     * Background texture for a fixture rendering.
     */
    public static final ResourceLocation TEXTURE = new ResourceLocation(Aperture.MOD_ID, "textures/gui/fixture.png");

    protected Minecraft mc = Minecraft.getMinecraft();

    public SmoothCamera smooth = new SmoothCamera();
    public Filter roll = new Filter();
    public Filter fov = new Filter();

    protected double playerX;
    protected double playerY;
    protected double playerZ;

    private Position prev = new Position(0, 0, 0, 0, 0);
    private Position next = new Position(0, 0, 0, 0, 0);
    private Color color = new Color();

    /**
     * Toggle path rendering
     */
    public void toggleRender()
    {
        Aperture.profileRender.set(!Aperture.profileRender.get());
        Aperture.profileRender.getConfig().save();
    }

    /**
     * Orient the camera
     *
     * This method is responsible for setting the roll of the camera and also
     * locking yaw and pitch during profile running.
     */
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onCameraOrient(EntityViewRenderEvent.CameraSetup event)
    {
        CameraRunner runner = ClientProxy.runner;

        float ticks = (float) event.getRenderPartialTicks();
        EntityPlayer player = this.mc.player;
        float playerYaw = runner.outside.active ? runner.outside.camera.rotationYaw : player.rotationYaw;

        /**
         * Apply camera angles only in case if it's the player. The 
         * reason behind this check is that mods like CFM which render 
         * the world to a texture won't get affected the camera changes.
         * 
         * The 0.001 part is added due to float precision error.
         */
        if (Math.abs((event.getYaw() - 180) - playerYaw) > 0.001)
        {
            return;
        }

        if (runner.isRunning())
        {
            event.setYaw(-180 + runner.yaw);
            event.setPitch(runner.pitch);

            if (Aperture.outside.get() && !Aperture.outsideHidePlayer.get())
            {
                this.mc.gameSettings.thirdPersonView = 1;
            }
        }
        else if (Minecraft.getMinecraft().currentScreen instanceof GuiCameraEditor)
        {
            Position position = ClientProxy.getCameraEditor().position;

            event.setYaw(-180 + position.angle.yaw);
            event.setPitch(position.angle.pitch);
        }
        else if (this.smooth.enabled.get() && !this.mc.isGamePaused())
        {
            /* Yaw and pitch */
            float yaw = this.smooth.getInterpYaw(ticks);
            float pitch = this.smooth.getInterpPitch(ticks);

            event.setYaw(-180 + yaw);
            event.setPitch(-pitch);

            player.rotationYaw = yaw;
            player.rotationPitch = -pitch;

            player.prevRotationYaw = yaw;
            player.prevRotationPitch = -pitch;

            /* Roll and FOV */
            if (this.roll.acc != 0.0F)
            {
                ClientProxy.control.roll = this.roll.interpolate(ticks);
            }
            else
            {
                this.roll.set(ClientProxy.control.roll);
            }

            if (this.fov.acc != 0.0F)
            {
                this.fov.interpolate(ticks);
                this.fov.value = MathHelper.clamp(this.fov.value, 0.0001F, 179.9999F);

                this.mc.gameSettings.fovSetting = this.fov.value;
            }
            else
            {
                this.fov.set(this.mc.gameSettings.fovSetting);
            }
        }

        float roll = ClientProxy.control.getRoll(ticks);

        if (roll == 0)
        {
            return;
        }

        event.setRoll(roll);
    }

    /**
     * This is updating smooth camera
     */
    @SubscribeEvent
    public void onPlayerTick(PlayerTickEvent event)
    {
        SmoothCamera camera = ClientProxy.renderer.smooth;
        EntityPlayer player = this.mc.player;

        if (event.side == Side.CLIENT && event.player == player)
        {
            if (camera.enabled.get())
            {
                /* Copied from EntityRenderer */
                float sensetivity = this.mc.gameSettings.mouseSensitivity * 0.6F + 0.2F;
                float finalSensetivity = sensetivity * sensetivity * sensetivity * 8.0F;
                float dx = this.mc.mouseHelper.deltaX * finalSensetivity * 0.15F;
                float dy = this.mc.mouseHelper.deltaY * finalSensetivity * 0.15F;

                /* Updating smooth camera */
                camera.update(this.mc.player, dx, dy);

                /* Roll and FOV acceleration */
                KeyboardHandler keys = ClientProxy.keys;

                float roll = keys.addRoll.isKeyDown() ? 1 : (keys.reduceRoll.isKeyDown() ? -1 : 0F);
                float fov = keys.addFov.isKeyDown() ? 1 : (keys.reduceFov.isKeyDown() ? -1 : 0F);

                this.roll.accelerate(roll * this.roll.factor.get());
                this.fov.accelerate(fov * this.fov.factor.get());
            }

            if (event.phase == TickEvent.Phase.START)
            {
                GuiManualFixturePanel.update();
            }
        }
    }

    /**
     * Render all camera fixtures
     */
    @SubscribeEvent
    public void onLastRender(RenderWorldLastEvent event)
    {
        if (GuiManualFixturePanel.recording)
        {
            GuiAbstractFixturePanel panel = ClientProxy.getCameraEditor().panel.delegate;

            if (panel instanceof GuiManualFixturePanel)
            {
                ((GuiManualFixturePanel) panel).recordFrame(this.mc.player, event.getPartialTicks());
            }
        }

        int shader = GL11.glGetInteger(GL20.GL_CURRENT_PROGRAM);

        if (shader != 0)
        {
            OpenGlHelper.glUseProgram(0);
        }

        CameraProfile profile = ClientProxy.control.currentProfile;
        CameraRunner runner = ClientProxy.runner;

        boolean badProfile = profile == null || profile.size() < 1;

        if (!Aperture.profileRender.get()) return;
        if (runner.isRunning()) return;
        if (badProfile) return;

        EntityPlayer player = runner.outside.active ? runner.outside.camera : this.mc.player;
        float ticks = event.getPartialTicks();

        this.playerX = player.prevPosX + (player.posX - player.prevPosX) * ticks;
        this.playerY = player.prevPosY + (player.posY - player.prevPosY) * ticks;
        this.playerZ = player.prevPosZ + (player.posZ - player.prevPosZ) * ticks;

        GlStateManager.pushAttrib();
        GlStateManager.enableTexture2D();
        GlStateManager.enableBlend();
        GL11.glLineWidth(4);


        for (int i = 0; i < profile.fixtures.size(); i++)
        {
            AbstractFixture fixture = profile.fixtures.get(i);

            if (fixture instanceof PathFixture)
            {
                ((PathFixture) fixture).disableSpeed();
            }

            fixture.applyFixture(0, 0.0F, profile, this.prev);
            fixture.applyFixture(fixture.getDuration(), 0.0F, profile, this.next);

            long duration = fixture.getDuration();

            double distX = Math.abs(this.next.point.x - this.prev.point.x);
            double distY = Math.abs(this.next.point.y - this.prev.point.y);
            double distZ = Math.abs(this.next.point.z - this.prev.point.z);

            this.color.set(fixture.color.get(), false);

            if (this.color.getRGBColor() == 0)
            {
                this.color.copy(FixtureRegistry.CLIENT.get(fixture.getClass()).color);
            }

            if (distX + distY + distZ >= 0.5)
            {
                this.drawCard(color, i, duration, this.next);
            }

            this.drawCard(this.color, i, duration, this.prev);
            this.drawFixture(this.color, fixture, this.prev, this.next);

            if (fixture instanceof PathFixture)
            {
                ((PathFixture) fixture).reenableSpeed();
            }
        }

        GlStateManager.disableBlend();
        GlStateManager.popAttrib();

        if (shader != 0)
        {
            OpenGlHelper.glUseProgram(shader);
        }
    }

    /**
     * Draw a fixture's fixture
     */
    private void drawFixture(Color color, AbstractFixture fixture, Position prev, Position next)
    {
        if (fixture instanceof PathFixture || fixture instanceof KeyframeFixture || fixture instanceof DollyFixture || fixture instanceof ManualFixture)
        {
            this.drawPathFixture(color, fixture, prev, next);
        }
        else if (fixture instanceof CircularFixture)
        {
            this.drawCircularFixture(color, fixture, prev, next);
        }
    }

    /**
     * Draw the passed path fixture
     */
    private void drawPathFixture(Color color, AbstractFixture fixture, Position prev, Position next)
    {
        CameraProfile profile = ClientProxy.control.currentProfile;

        long duration = fixture.getDuration();
        int size = (int) (duration / 5);
        PathFixture path = null;

        if (fixture instanceof PathFixture)
        {
            path = (PathFixture) fixture;
            size = path.size();
        }

        final int p = 15;

        BufferBuilder builder = Tessellator.getInstance().getBuffer();

        GlStateManager.disableTexture2D();
        builder.setTranslation(-this.playerX, -this.playerY, -this.playerZ);
        builder.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR);

        if (fixture instanceof DollyFixture)
        {
            builder.pos(prev.point.x, prev.point.y, prev.point.z).color(color.r, color.g, color.b, 1F).endVertex();
            builder.pos(next.point.x, next.point.y, next.point.z).color(color.r, color.g, color.b, 1F).endVertex();
        }
        else
        {
            for (int i = 0; i < size; i++)
            {
                for (int j = 0; j < p; j++)
                {
                    fixture.applyFixture((long) ((float) (j + i * p) / (float) (size * p) * duration), 0, profile, prev);
                    fixture.applyFixture((long) ((float) (j + i * p + 1) / (float) (size * p) * duration), 0, profile, next);

                    builder.pos(prev.point.x, prev.point.y, prev.point.z).color(color.r, color.g, color.b, 1F).endVertex();
                    builder.pos(next.point.x, next.point.y, next.point.z).color(color.r, color.g, color.b, 1F).endVertex();
                }
            }
        }

        Tessellator.getInstance().draw();

        if (path != null && path.interpolation.get() == InterpolationType.CIRCULAR && path.size() > 0)
        {
            Vector2d center = path.getCenter();
            double y = 0;

            for (int i = 0; i < path.size(); i++)
            {
                y += path.points.get(i).point.y;
            }

            y /= path.size();

            /* Draw anchor */
            GL11.glPointSize(10);
            builder.begin(GL11.GL_POINTS, DefaultVertexFormats.POSITION_COLOR);
            builder.pos(center.x, y, center.y).color(0F, 0F, 0F, 1F).endVertex();
            builder.pos(center.x, y, center.y).color(0F, 0F, 0F, 1F).endVertex();

            Tessellator.getInstance().draw();

            GL11.glPointSize(8);
            builder.begin(GL11.GL_POINTS, DefaultVertexFormats.POSITION_COLOR);
            builder.pos(center.x, y, center.y).color(1F, 1F, 1F, 1F).endVertex();
            builder.pos(center.x, y, center.y).color(1F, 1F, 1F, 1F).endVertex();

            Tessellator.getInstance().draw();
        }

        builder.setTranslation(0, 0, 0);
        GlStateManager.enableTexture2D();

        if (path != null)
        {
            for (int i = 1; i < path.size() - 1; i++)
            {
                fixture.applyFixture(path.getTickForPoint(i), 0, profile, prev);

                this.drawPathPoint(color, prev, i);
            }
        }
    }

    /**
     * Draw the path point
     *
     * This method is responsible for drawing a square with a label which shows
     * the index of that point. This is very useful for path point management.
     */
    private void drawPathPoint(Color color, Position position, int index)
    {
        GlStateManager.pushMatrix();
        GlStateManager.pushAttrib();
        GlStateManager.enableBlend();
        GlStateManager.color(1, 1, 1);

        this.mc.renderEngine.bindTexture(TEXTURE);

        double x = position.point.x - this.playerX;
        double y = position.point.y - this.playerY;
        double z = position.point.z - this.playerZ;

        GL11.glNormal3f(0, 1, 0);
        GlStateManager.translate(x, y, z);

        if (Minecraft.getMinecraft().currentScreen instanceof GuiCameraEditor)
        {
            Position pos = ClientProxy.getCameraEditor().position;

            GlStateManager.rotate(-pos.angle.yaw, 0, 1, 0);
            GlStateManager.rotate(pos.angle.pitch, 1, 0, 0);
        }
        else
        {
            GlStateManager.rotate(-this.mc.getRenderManager().playerViewY, 0, 1, 0);
            GlStateManager.rotate(this.mc.getRenderManager().playerViewX, 1, 0, 0);
        }

        float factor = 0.1F;
        float minX = -factor;
        float minY = -factor;
        float maxX = factor;
        float maxY = factor;

        int tw = 34;

        int tx = 32;
        int tx2 = 34;

        float texX = (float) tx / (float) tw;
        float texY = 0;
        float texRX = (float) tx2 / (float) tw;
        float texRY = 2.0F / 16.0F;

        BufferBuilder vb = Tessellator.getInstance().getBuffer();

        vb.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        vb.pos(minX, minY, 0).tex(texRX, texRY).endVertex();
        vb.pos(minX, maxY, 0).tex(texRX, texY).endVertex();
        vb.pos(maxX, maxY, 0).tex(texX, texY).endVertex();
        vb.pos(maxX, minY, 0).tex(texX, texRY).endVertex();

        Tessellator.getInstance().draw();
        GlStateManager.disableBlend();

        String indexString = String.valueOf(index);
        int indexWidth = this.mc.fontRenderer.getStringWidth(indexString) / 2;

        GlStateManager.rotate(180, 0, 0, 1);
        GlStateManager.scale(0.03f, 0.03f, 0.03f);
        GlStateManager.translate(0, -3.5, -0.1);
        GlStateManager.translate(0, -12, 0);

        this.mc.fontRenderer.drawString(indexString, -indexWidth, 0, -1);

        GlStateManager.popAttrib();
        GlStateManager.popMatrix();
        GlStateManager.color(1, 1, 1, 1);
    }

    /**
     * Draw the passed circular fixture
     */
    private void drawCircularFixture(Color color, AbstractFixture fixture, Position prev, Position next)
    {
        CameraProfile profile = ClientProxy.control.currentProfile;
        float circles = Math.min(((CircularFixture) fixture).circles.get(), 360);
        long duration = fixture.getDuration();

        BufferBuilder vb = Tessellator.getInstance().getBuffer();

        GlStateManager.disableTexture2D();
        vb.setTranslation(-this.playerX, -this.playerY, -this.playerZ);
        vb.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR);

        for (int i = 0; i < circles; i += 5)
        {
            float a = i / circles * duration;
            float b = (i + 3) / circles * duration;

            fixture.applyFixture((long) a, a - (int) a, profile, prev);
            fixture.applyFixture((long) b, b - (int) b, profile, next);

            if (i == 0)
            {
                vb.pos(prev.point.x, prev.point.y, prev.point.z).color(color.r, color.g, color.b, 1F).endVertex();
            }

            vb.pos(next.point.x, next.point.y, next.point.z).color(color.r, color.g, color.b, 1F).endVertex();
        }

        Tessellator.getInstance().draw();
        vb.setTranslation(0, 0, 0);
        GlStateManager.enableTexture2D();
    }

    /**
     * Draw the card of the fixture with the information about this fixture,
     * like duration and stuff.
     */
    private void drawCard(Color color, int index, long duration, Position pos)
    {
        GlStateManager.pushMatrix();
        GlStateManager.pushAttrib();
        GlStateManager.enableBlend();
        GlStateManager.color(color.r, color.g, color.b, 0.8F);

        this.mc.renderEngine.bindTexture(TEXTURE);

        double x = pos.point.x - this.playerX;
        double y = pos.point.y - this.playerY;
        double z = pos.point.z - this.playerZ;

        GL11.glNormal3f(0, 1, 0);
        GlStateManager.translate(x, y, z);

        if (Minecraft.getMinecraft().currentScreen instanceof GuiCameraEditor)
        {
            Position position = ClientProxy.getCameraEditor().position;

            GlStateManager.rotate(-position.angle.yaw, 0, 1, 0);
            GlStateManager.rotate(position.angle.pitch, 1, 0, 0);
        }
        else
        {
            GlStateManager.rotate(-this.mc.getRenderManager().playerViewY, 0, 1, 0);
            GlStateManager.rotate(this.mc.getRenderManager().playerViewX, 1, 0, 0);
        }

        float factor = 0.5F;

        float minX = -factor;
        float minY = -factor;
        float maxX = factor;
        float maxY = factor;

        int tw = 34;
        int i = 0;

        int tx = i * 16;
        int tx2 = tx + 16;

        float texX = (float) tx / (float) tw;
        float texY = 0;
        float texRX = (float) tx2 / (float) tw;
        float texRY = 1;

        BufferBuilder vb = Tessellator.getInstance().getBuffer();

        vb.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        vb.pos(minX, minY, 0).tex(texRX, texRY).endVertex();
        vb.pos(minX, maxY, 0).tex(texRX, texY).endVertex();
        vb.pos(maxX, maxY, 0).tex(texX, texY).endVertex();
        vb.pos(maxX, minY, 0).tex(texX, texRY).endVertex();

        Tessellator.getInstance().draw();
        GlStateManager.disableBlend();

        String indexString = String.valueOf(index);
        String durationString = TimeUtils.formatTime(duration) + (Aperture.editorSeconds.get() ? "s" : "t");
        int indexWidth = this.mc.fontRenderer.getStringWidth(indexString) / 2;
        int durationWidth = this.mc.fontRenderer.getStringWidth(durationString) / 2;

        GlStateManager.rotate(180, 0, 0, 1);
        GlStateManager.scale(0.05f, 0.05f, 0.05f);
        GlStateManager.translate(0, -3.5, -0.1);

        this.mc.fontRenderer.drawString(indexString, -indexWidth, 0, -1);

        GlStateManager.translate(0, -13, 0);
        GlStateManager.scale(0.5f, 0.5f, 0.5f);

        this.mc.fontRenderer.drawString(durationString, -durationWidth, 0, -1);

        GlStateManager.popAttrib();
        GlStateManager.popMatrix();
        GlStateManager.color(1, 1, 1, 1);
    }
}