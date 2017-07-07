package mchorse.aperture.client.gui;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.lwjgl.input.Mouse;

import mchorse.aperture.Aperture;
import mchorse.aperture.ClientProxy;
import mchorse.aperture.camera.CameraProfile;
import mchorse.aperture.camera.CameraRunner;
import mchorse.aperture.camera.Position;
import mchorse.aperture.camera.fixtures.AbstractFixture;
import mchorse.aperture.camera.fixtures.CircularFixture;
import mchorse.aperture.camera.fixtures.FollowFixture;
import mchorse.aperture.camera.fixtures.IdleFixture;
import mchorse.aperture.camera.fixtures.LookFixture;
import mchorse.aperture.camera.fixtures.PathFixture;
import mchorse.aperture.client.gui.GuiFixturesPopup.IFixtureSelector;
import mchorse.aperture.client.gui.GuiPlaybackScrub.IScrubListener;
import mchorse.aperture.client.gui.GuiProfilesManager.IProfileListener;
import mchorse.aperture.client.gui.panels.GuiAbstractFixturePanel;
import mchorse.aperture.client.gui.panels.GuiCircularFixturePanel;
import mchorse.aperture.client.gui.panels.GuiFollowFixturePanel;
import mchorse.aperture.client.gui.panels.GuiIdleFixturePanel;
import mchorse.aperture.client.gui.panels.GuiLookFixturePanel;
import mchorse.aperture.client.gui.panels.GuiPathFixturePanel;
import mchorse.aperture.client.gui.panels.IFixturePanel;
import mchorse.aperture.client.gui.widgets.buttons.GuiTextureButton;
import mchorse.aperture.events.CameraEditorPlaybackStateEvent;
import mchorse.aperture.events.CameraEditorScrubbedEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.client.GuiIngameForge;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Camera editor GUI
 *
 * This GUI provides tools for managing camera profiles. 
 */
@SideOnly(Side.CLIENT)
public class GuiCameraEditor extends GuiScreen implements IScrubListener, IFixtureSelector, IProfileListener
{
    /**
     * Camera editor texture
     */
    public static final ResourceLocation EDITOR_TEXTURE = new ResourceLocation("aperture:textures/gui/camera_editor.png");

    /**
     * Registry of editing camera fixture panels. Per every fixture class type
     * there is supposed to be a class that is responsible for editing a
     * fixture.
     */
    public static final Map<Class<? extends AbstractFixture>, IFixturePanel<? extends AbstractFixture>> PANELS = new HashMap<Class<? extends AbstractFixture>, IFixturePanel<? extends AbstractFixture>>();

    /**
     * Currently editing camera profile
     */
    private CameraProfile profile;

    /**
     * Profile runner
     */
    private CameraRunner runner;

    /**
     * Flag for observing the runner
     */
    private boolean playing = false;

    /**
     * Whether things on the screen are visible (usable for previewing camera 
     * profile playback in the camera editor without exiting it) 
     */
    private boolean visible = true;

    /**
     * Last player's position upon entering this GUI 
     */
    private BlockPos lastPos;

    /**
     * Whether cameras are sync'd every render tick. Usable for target based
     * fixtures only
     */
    public boolean syncing;

    /**
     * Maximum scrub duration
     */
    public int maxScrub = 0;

    public Map<Integer, String> tooltips = new HashMap<Integer, String>();

    /* GUI fields */

    /**
     * Play/pause button (very clever name, eh?)
     */
    public GuiTextureButton plause;

    public GuiButton nextFrame;
    public GuiButton prevFrame;

    public GuiButton toPrevFixture;
    public GuiButton toNextFixture;

    public GuiButton moveForward;
    public GuiButton moveBackward;

    public GuiButton copyPosition;
    public GuiButton moveDuration;

    public GuiTextureButton save;
    public GuiButton openConfig;
    public GuiButton openProfiles;

    public GuiButton add;
    public GuiButton remove;

    /* Widgets */
    public GuiCameraConfig config;
    public GuiFixturesPopup popup;
    public GuiPlaybackScrub scrub;
    public GuiProfilesManager profiles;

    /**
     * Current fixture panel to display
     */
    public IFixturePanel<AbstractFixture> fixturePanel;

    static
    {
        FontRenderer font = Minecraft.getMinecraft().fontRendererObj;

        /* Registering per fixture panels */
        PANELS.put(IdleFixture.class, new GuiIdleFixturePanel(font));
        PANELS.put(PathFixture.class, new GuiPathFixturePanel(font));
        PANELS.put(LookFixture.class, new GuiLookFixturePanel(font));
        PANELS.put(FollowFixture.class, new GuiFollowFixturePanel(font));
        PANELS.put(CircularFixture.class, new GuiCircularFixturePanel(font));
    }

    /**
     * Teleport player and setup position, motion and angle based on the value
     * was scrubbed from playback scrubber.
     */
    @Override
    public void scrubbed(GuiPlaybackScrub scrub, int value)
    {
        if (!this.runner.isRunning() && this.syncing)
        {
            this.updatePlayer(value, 0.0F);
        }
        else
        {
            this.runner.setTicks(value);
        }

        ClientProxy.EVENT_BUS.post(new CameraEditorScrubbedEvent(this.runner.isRunning(), this.scrub.value));
    }

    /**
     * Pick a camera fixture
     *
     * This method is responsible for setting current fixture panel which in
     * turn then will allow to edit properties of the camera fixture
     */
    @SuppressWarnings("unchecked")
    public void pickCameraFixture(AbstractFixture fixture, long duration)
    {
        if (fixture == null)
        {
            this.fixturePanel = null;
        }
        else
        {
            IFixturePanel<AbstractFixture> panel = (IFixturePanel<AbstractFixture>) PANELS.get(fixture.getClass());

            if (panel != null)
            {
                panel.select(fixture, duration);
                panel.update(this);

                this.fixturePanel = panel;

                if (this.syncing)
                {
                    this.scrub.setValue((int) panel.currentOffset());
                }

                this.scrub.index = this.profile.getAll().indexOf(fixture);
            }
            else
            {
                this.fixturePanel = null;
            }
        }
    }

    /**
     * Add a fixture to camera profile
     */
    @Override
    public void createFixture(AbstractFixture fixture)
    {
        if (fixture != null)
        {
            this.profile.add(fixture);
            this.updateValues();
            this.pickCameraFixture(fixture, 0);
        }
    }

    /**
     * Camera profile was selected from the profile manager 
     */
    @Override
    public void selectProfile(CameraProfile profile)
    {
        if (profile == this.profile)
        {
            return;
        }

        ClientProxy.control.currentProfile = profile;

        this.setProfile(profile);
        this.cameraProfileWasChanged(profile);
        this.pickCameraFixture(null, 0);
    }

    public void cameraProfileWasChanged(CameraProfile profile)
    {
        if (this.save != null)
        {
            boolean dirty = profile == null ? false : profile.dirty;

            int x = dirty ? 128 : 112;
            int y = dirty ? 0 : 0;

            this.save.setTexPos(x, y).setActiveTexPos(x, y + 16);
        }
    }

    /**
     * Initialize the camera editor with a camera profile.
     */
    public GuiCameraEditor(CameraRunner runner)
    {
        this.runner = runner;
        this.scrub = new GuiPlaybackScrub(this, null);
        this.popup = new GuiFixturesPopup(this);
        this.profiles = new GuiProfilesManager(this);
        this.config = new GuiCameraConfig(this);

        /* Initiating tooltips */
        this.tooltips.put(0, I18n.format("aperture.gui.tooltips.jump_next_fixture"));
        this.tooltips.put(1, I18n.format("aperture.gui.tooltips.jump_next_frame"));
        this.tooltips.put(2, I18n.format("aperture.gui.tooltips.plause"));
        this.tooltips.put(3, I18n.format("aperture.gui.tooltips.jump_prev_frame"));
        this.tooltips.put(4, I18n.format("aperture.gui.tooltips.jump_prev_fixture"));

        this.tooltips.put(5, I18n.format("aperture.gui.tooltips.move_up"));
        this.tooltips.put(6, I18n.format("aperture.gui.tooltips.move_duration"));
        this.tooltips.put(7, I18n.format("aperture.gui.tooltips.copy_position"));
        this.tooltips.put(8, I18n.format("aperture.gui.tooltips.move_down"));

        this.tooltips.put(9, I18n.format("aperture.gui.tooltips.save"));
        this.tooltips.put(10, I18n.format("aperture.gui.tooltips.profiles"));
        this.tooltips.put(11, I18n.format("aperture.gui.tooltips.config"));
        this.tooltips.put(50, I18n.format("aperture.gui.tooltips.add"));
        this.tooltips.put(51, I18n.format("aperture.gui.tooltips.remove"));
    }

    /**
     * Reset the camera editor
     */
    public void reset()
    {
        this.setProfile(null);
        this.scrub.value = 0;
        this.lastPos = null;
    }

    /**
     * Set camera profile
     */
    public void setProfile(CameraProfile profile)
    {
        boolean isOldSame = this.profile == profile;

        this.profile = profile;
        this.scrub.setProfile(profile);

        if (!isOldSame)
        {
            this.fixturePanel = null;
        }

        if (this.profile == null)
        {
            this.profiles.visible = true;
        }
    }

    /**
     * Update the state of camera editor (should be invoked upon opening this 
     * screen)
     */
    public void updateCameraEditor(EntityPlayer player)
    {
        this.selectProfile(ClientProxy.control.currentProfile);
        this.profiles.init();

        GuiIngameForge.renderHotbar = false;
        GuiIngameForge.renderCrosshairs = false;
        Minecraft.getMinecraft().gameSettings.hideGUI = true;

        this.maxScrub = 0;
        this.visible = true;

        /* Disable syncing if the player is too far away */
        BlockPos lastPos = new BlockPos(player);

        if (this.lastPos != null)
        {
            int dx = Math.abs(lastPos.getX() - this.lastPos.getX());
            int dy = Math.abs(lastPos.getY() - this.lastPos.getY());
            int dz = Math.abs(lastPos.getZ() - this.lastPos.getZ());

            if (dx > 48 || dy > 48 || dz > 48)
            {
                this.syncing = false;
            }
        }

        this.lastPos = lastPos;
    }

    public CameraProfile getProfile()
    {
        return this.profile;
    }

    /**
     * Update player to current value in the scrub
     */
    public void updatePlayerCurrently(float ticks)
    {
        if (this.syncing && !this.runner.isRunning())
        {
            this.updatePlayer(this.scrub.value, ticks);
        }
    }

    /**
     * Update player
     */
    public void updatePlayer(long tick, float ticks)
    {
        long duration = this.profile.getDuration();

        tick = tick < 0 ? 0 : tick;
        tick = tick > duration ? duration : tick;

        EntityPlayer player = Minecraft.getMinecraft().player;
        Position pos = new Position(player);

        this.profile.applyProfile(tick, ticks, pos);

        pos.apply(player);
        ClientProxy.control.setRollAndFOV(pos.angle.roll, pos.angle.fov);
    }

    /**
     * This method should be invoked when values in the panel were modified
     */
    public void updateValues()
    {
        if (this.profile != null)
        {
            this.scrub.max = Math.max((int) this.profile.getDuration(), this.maxScrub);
            this.scrub.setValue(this.scrub.value);
        }
        else
        {
            this.scrub.max = this.maxScrub;
            this.scrub.setValue(0);
        }
    }

    /**
     * Makes camera profile as dirty as possible 
     */
    public void updateProfile()
    {
        if (this.profile != null)
        {
            this.profile.dirty();
        }
    }

    /**
     * This GUI shouldn't pause the game, because camera runs on the world's
     * update loop.
     */
    @Override
    public boolean doesGuiPauseGame()
    {
        return false;
    }

    /**
     * Initiate buttons and other GUI elements
     */
    @Override
    public void initGui()
    {
        int x = this.width / 2 + 50;
        int y = 0;
        int w = 20;

        /* Setup buttons */
        x -= w;
        this.toNextFixture = new GuiTextureButton(0, x + 2, y + 2, EDITOR_TEXTURE).setTexPos(64, 0).setActiveTexPos(64, 16);
        x -= w;
        this.nextFrame = new GuiTextureButton(1, x + 2, y + 2, EDITOR_TEXTURE).setTexPos(32, 0).setActiveTexPos(32, 16);
        x -= w;
        this.plause = new GuiTextureButton(2, x + 2, y + 2, EDITOR_TEXTURE);
        x -= w;
        this.prevFrame = new GuiTextureButton(3, x + 2, y + 2, EDITOR_TEXTURE).setTexPos(48, 0).setActiveTexPos(48, 16);
        x -= w;
        this.toPrevFixture = new GuiTextureButton(4, x + 2, y + 2, EDITOR_TEXTURE).setTexPos(80, 0).setActiveTexPos(80, 16);

        x = this.width - w;
        this.openProfiles = new GuiTextureButton(10, x + 2, y + 2, EDITOR_TEXTURE).setTexPos(96, 0).setActiveTexPos(96, 16);
        x -= w;
        this.openConfig = new GuiTextureButton(11, x + 2, y + 2, EDITOR_TEXTURE).setTexPos(208, 0).setActiveTexPos(208, 16);
        x -= w;
        this.save = new GuiTextureButton(9, x + 2, y + 2, EDITOR_TEXTURE);
        x -= w;
        this.add = new GuiTextureButton(50, x + 2, y + 2, EDITOR_TEXTURE).setTexPos(224, 0).setActiveTexPos(224, 16);
        x -= w;
        this.remove = new GuiTextureButton(51, x + 2, y + 2, EDITOR_TEXTURE).setTexPos(240, 0).setActiveTexPos(240, 16);

        x = 60;
        this.moveForward = new GuiTextureButton(5, x + 2, y + 2, EDITOR_TEXTURE).setTexPos(144, 0).setActiveTexPos(144, 16);
        x -= w;
        this.moveDuration = new GuiTextureButton(6, x + 2, y + 2, EDITOR_TEXTURE).setTexPos(192, 0).setActiveTexPos(192, 16);
        x -= w;
        this.copyPosition = new GuiTextureButton(7, x + 2, y + 2, EDITOR_TEXTURE).setTexPos(176, 0).setActiveTexPos(176, 16);
        x -= w;
        this.moveBackward = new GuiTextureButton(8, x + 2, y + 2, EDITOR_TEXTURE).setTexPos(160, 0).setActiveTexPos(160, 16);

        this.cameraProfileWasChanged(this.profile);
        this.updatePlauseButton();

        this.buttonList.add(this.toNextFixture);
        this.buttonList.add(this.nextFrame);
        this.buttonList.add(this.plause);
        this.buttonList.add(this.prevFrame);
        this.buttonList.add(this.toPrevFixture);

        this.buttonList.add(this.moveForward);
        this.buttonList.add(this.moveDuration);
        this.buttonList.add(this.copyPosition);
        this.buttonList.add(this.moveBackward);

        this.buttonList.add(this.add);
        this.buttonList.add(this.remove);
        this.buttonList.add(this.save);
        this.buttonList.add(this.openConfig);
        this.buttonList.add(this.openProfiles);

        /* Setup areas of widgets */
        this.scrub.area.set(10, this.height - 20, this.width - 20, 20);
        this.popup.update(width / 2 - 32, height / 2 - 51, 62, 102);

        if (this.fixturePanel != null)
        {
            this.fixturePanel.update(this);
        }

        this.config.update(width - 180, 20, 160, this.height - 80);
        this.profiles.update(width - 160, 20, 160, this.height - 80);

        if (this.profile == null)
        {
            this.profiles.visible = true;
        }

        this.updateValues();
    }

    /**
     * Update display icon of the plause button
     */
    private void updatePlauseButton()
    {
        int x = this.runner.isRunning() ? 16 : 0;

        this.plause.setTexPos(x, 0).setActiveTexPos(x, 16);
    }

    /**
     * Gets invoked if a button was pressed
     */
    @Override
    protected void actionPerformed(GuiButton button) throws IOException
    {
        int id = button.id;

        /* Playback panel */
        if (id == 0)
        {
            this.jumpToNextFixture();
        }
        else if (id == 1)
        {
            this.scrub.setValue(this.scrub.value + 1);
        }
        else if (id == 2)
        {
            this.runner.toggle(this.profile, this.scrub.value);
            this.updatePlauseButton();

            this.playing = this.runner.isRunning();

            ClientProxy.EVENT_BUS.post(new CameraEditorPlaybackStateEvent(this.playing, this.scrub.value));
        }
        else if (id == 3)
        {
            this.scrub.setValue(this.scrub.value - 1);
        }
        else if (id == 4)
        {
            this.jumpToPrevFixture();
        }
        else if (id == 5 || id == 8)
        {
            /* Move fixture forward or backward */
            CameraProfile profile = this.profile;
            int index = this.scrub.index;
            int to = index + (id == 8 ? -1 : 1);

            profile.move(index, to);

            if (profile.has(to))
            {
                this.scrub.index = to;
            }
        }
        else if (id == 6 && this.fixturePanel != null)
        {
            /* Move duration to the scrub location */
            AbstractFixture fixture = this.profile.get(this.scrub.index);
            long offset = this.profile.calculateOffset(fixture);

            if (this.scrub.value > offset)
            {
                fixture.setDuration(this.scrub.value - offset);
                this.updateProfile();

                this.updateValues();
                this.fixturePanel.select(fixture, 0);
            }
        }
        else if (id == 7 && this.fixturePanel != null)
        {
            ((GuiAbstractFixturePanel<AbstractFixture>) this.fixturePanel).editFixture();
        }
        else if (id == 9 && this.profile != null)
        {
            this.profile.save();
        }
        else if (id == 10)
        {
            this.profiles.visible = !this.profiles.visible;
            this.config.visible = false;
        }
        else if (id == 11)
        {
            this.config.visible = true;
            this.profiles.visible = false;
        }

        /* Add and remove */
        if (id == 50)
        {
            this.popup.visible = true;
        }
        else if (id == 51)
        {
            int index = this.scrub.index;
            int size = this.profile.getCount();

            if (index >= 0 && index < size)
            {
                this.profile.remove(index);
                this.scrub.index--;

                if (this.scrub.index >= 0)
                {
                    this.pickCameraFixture(this.profile.get(this.scrub.index), 0);
                }
                else
                {
                    this.pickCameraFixture(null, 0);
                }

                this.updateValues();
            }
        }
    }

    /**
     * Jump to the next camera fixture
     */
    private void jumpToNextFixture()
    {
        this.scrub.setValue((int) this.profile.calculateOffset(this.scrub.value, true));
    }

    /**
     * Jump to previous fixture
     */
    private void jumpToPrevFixture()
    {
        this.scrub.setValue((int) this.profile.calculateOffset(this.scrub.value - 1, false));
    }

    /**
     * Override default behavior to restore the rendering of hotbar and
     * crosshairs.
     */
    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException
    {
        if (keyCode == 59)
        {
            this.visible = !this.visible;
        }

        if (keyCode == 1)
        {
            GuiIngameForge.renderHotbar = true;
            GuiIngameForge.renderCrosshairs = true;
            Minecraft.getMinecraft().gameSettings.hideGUI = false;

            this.mc.displayGuiScreen((GuiScreen) null);

            if (this.mc.currentScreen == null)
            {
                this.mc.setIngameFocus();
            }
        }

        if (!this.visible)
        {
            /* Toggle playback in the visible mode */
            if (keyCode == 32)
            {
                this.actionPerformed(this.plause);
            }

            return;
        }

        this.profiles.keyTyped(typedChar, keyCode);

        if (this.profile == null)
        {
            return;
        }

        if (this.fixturePanel != null)
        {
            this.fixturePanel.keyTyped(typedChar, keyCode);
        }
    }

    @Override
    public void handleMouseInput() throws IOException
    {
        super.handleMouseInput();

        int x = Mouse.getEventX() * this.width / this.mc.displayWidth;
        int y = this.height - Mouse.getEventY() * this.height / this.mc.displayHeight - 1;

        int scroll = -Mouse.getEventDWheel();

        this.profiles.mouseScroll(x, y, scroll);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException
    {
        if (!this.visible)
        {
            return;
        }

        if (this.profile == null || this.profiles.isInside(mouseX, mouseY))
        {
            this.profiles.mouseClicked(mouseX, mouseY, mouseButton);

            return;
        }

        this.config.mouseClicked(mouseX, mouseY, mouseButton);

        if (this.config.isInside(mouseX, mouseY))
        {
            return;
        }

        this.popup.mouseClicked(mouseX, mouseY, mouseButton);

        if (this.popup.visible && this.popup.area.isInside(mouseX, mouseY))
        {
            return;
        }

        super.mouseClicked(mouseX, mouseY, mouseButton);

        if (this.fixturePanel != null)
        {
            this.fixturePanel.mouseClicked(mouseX, mouseY, mouseButton);
        }

        this.scrub.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state)
    {
        if (!this.visible || this.profile == null)
        {
            return;
        }

        if (this.config.isInside(mouseX, mouseY))
        {
            return;
        }

        if (this.popup.isInside(mouseX, mouseY) || this.profiles.isInside(mouseX, mouseY))
        {
            return;
        }

        super.mouseReleased(mouseX, mouseY, state);

        if (this.fixturePanel != null)
        {
            this.fixturePanel.mouseReleased(mouseX, mouseY, state);
        }

        this.scrub.mouseReleased(mouseX, mouseY, state);
    }

    /**
     * Draw everything on the screen
     */
    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        boolean isRunning = this.runner.isRunning();

        if (!this.visible || (isRunning && Aperture.proxy.config.camera_minema))
        {
            /* Little tip for the users who don't know what they did */
            if (!isRunning)
            {
                this.fontRendererObj.drawStringWithShadow(I18n.format("aperture.gui.editor.f1"), 5, this.height - 12, 0xffffff);
            }

            return;
        }

        if (this.profile != null)
        {
            this.drawGradientRect(0, 0, width, 20, 0x66000000, 0);

            if (this.profiles.visible)
            {
                Gui.drawRect(width - 20, 0, width, 20, 0xaa000000);
            }

            if (this.config.visible)
            {
                Gui.drawRect(width - 40, 0, width - 20, 20, 0xaa000000);
            }

            super.drawScreen(mouseX, mouseY, partialTicks);

            /* Sync the player on current tick */
            if (this.syncing)
            {
                this.updatePlayerCurrently(0.0F);
            }

            boolean running = this.runner.isRunning();

            if (running)
            {
                this.scrub.value = (int) this.runner.getTicks();
                this.scrub.value = MathHelper.clamp(this.scrub.value, 0, this.scrub.max);
            }

            if (!running && this.playing)
            {
                this.updatePlauseButton();

                ClientProxy.EVENT_BUS.post(new CameraEditorPlaybackStateEvent(false, this.scrub.value));
                this.playing = false;
            }

            /* Draw widgets */
            this.scrub.draw(mouseX, mouseY, partialTicks);

            if (this.fixturePanel != null)
            {
                this.fixturePanel.draw(mouseX, mouseY, partialTicks);
            }

            this.popup.draw(mouseX, mouseY, partialTicks);
        }

        this.config.draw(mouseX, mouseY, partialTicks);
        this.profiles.draw(mouseX, mouseY, partialTicks);

        if (this.profile != null)
        {
            for (GuiButton button : this.buttonList)
            {
                String label = this.tooltips.get(button.id);

                if (label != null && mouseX >= button.xPosition && mouseY >= button.yPosition && mouseX < button.xPosition + button.width && mouseY < button.yPosition + button.height)
                {
                    this.drawTooltip(label, button.xPosition, button.yPosition + button.height + 6);
                }
            }
        }
    }

    private void drawTooltip(String label, int x, int y)
    {
        int width = this.fontRendererObj.getStringWidth(label);

        if (x + width + 4 > this.width)
        {
            x = this.width - (width + 4);
        }

        if (x - 4 < 4)
        {
            x = 4;
        }

        Gui.drawRect(x - 3, y - 3, x + width + 3, y + this.fontRendererObj.FONT_HEIGHT + 3, 0xaa000000);
        this.fontRendererObj.drawStringWithShadow(label, x, y, 0xffffff);
    }
}