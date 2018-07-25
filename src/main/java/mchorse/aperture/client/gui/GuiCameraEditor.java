package mchorse.aperture.client.gui;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import mchorse.aperture.Aperture;
import mchorse.aperture.ClientProxy;
import mchorse.aperture.camera.CameraProfile;
import mchorse.aperture.camera.CameraRunner;
import mchorse.aperture.camera.data.Angle;
import mchorse.aperture.camera.data.Point;
import mchorse.aperture.camera.data.Position;
import mchorse.aperture.camera.fixtures.AbstractFixture;
import mchorse.aperture.client.gui.GuiFixturesPopup.IFixtureSelector;
import mchorse.aperture.client.gui.GuiPlaybackScrub.IScrubListener;
import mchorse.aperture.client.gui.GuiProfilesManager.IProfileListener;
import mchorse.aperture.client.gui.GuiTrackpad.ITrackpadListener;
import mchorse.aperture.client.gui.config.GuiCameraConfig;
import mchorse.aperture.client.gui.config.GuiConfigCameraOptions;
import mchorse.aperture.client.gui.panels.GuiAbstractFixturePanel;
import mchorse.aperture.client.gui.panels.IFixturePanel;
import mchorse.aperture.client.gui.widgets.buttons.GuiTextureButton;
import mchorse.aperture.events.CameraEditorPlaybackStateEvent;
import mchorse.aperture.events.CameraEditorScrubbedEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
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
public class GuiCameraEditor extends GuiScreen implements IScrubListener, IFixtureSelector, IProfileListener, ITrackpadListener
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

    /* Strings */
    private String stringSpeed = I18n.format("aperture.gui.editor.speed");
    private String stringX = I18n.format("aperture.gui.panels.x");
    private String stringY = I18n.format("aperture.gui.panels.y");
    private String stringZ = I18n.format("aperture.gui.panels.z");
    private String stringYaw = I18n.format("aperture.gui.panels.yaw");
    private String stringPitch = I18n.format("aperture.gui.panels.pitch");
    private String stringRoll = I18n.format("aperture.gui.panels.roll");
    private String stringFov = I18n.format("aperture.gui.panels.fov");

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
     * Show go to frame thing 
     */
    private boolean showFrame;

    /**
     * FOV which was user had before entering the GUI 
     */
    private float lastFov = 70.0F;

    /**
     * This property saves state for the sync option, to allow more friendly
     */
    public boolean haveScrubbed;

    /**
     * Whether cameras are sync'd every render tick. Usable for target based
     * fixtures only
     */
    public boolean syncing;

    /**
     * Whether camera editor should display camera information 
     */
    public boolean displayPosition;

    /**
     * Maximum scrub duration
     */
    public int maxScrub = 0;

    /**
     * Flight mode 
     */
    public Flight flight = new Flight();

    /**
     * Position 
     */
    public Position position;

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
    public GuiButton openModifiers;
    public GuiButton openProfiles;

    public GuiButton add;
    public GuiButton dupe;
    public GuiButton remove;

    public GuiButton goTo;
    public GuiTrackpad frame;

    /* Widgets */
    public GuiCameraConfig config;
    public GuiFixturesPopup popup;
    public GuiPlaybackScrub scrub;
    public GuiProfilesManager profiles;
    public GuiConfigCameraOptions cameraOptions;

    /**
     * Current fixture panel to display
     */
    public IFixturePanel<AbstractFixture> fixturePanel;

    /**
     * GUI camera modifiers panel 
     */
    public GuiModifiersManager modifiers;

    /**
     * Teleport player and setup position, motion and angle based on the value
     * was scrubbed from playback scrubber.
     */
    @Override
    public void scrubbed(GuiPlaybackScrub scrub, int value, boolean fromScrub)
    {
        if (!this.frame.text.isFocused())
        {
            this.frame.setValue(value);
        }

        if (!this.runner.isRunning() && (this.syncing || this.runner.outside.active))
        {
            this.updatePlayer(value, 0.0F);
        }
        else
        {
            this.runner.ticks = value;
        }

        if (fromScrub)
        {
            this.haveScrubbed = true;

            ClientProxy.EVENT_BUS.post(new CameraEditorScrubbedEvent(this.runner.isRunning(), this.scrub.value));
        }
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
            this.scrub.index = -1;
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

        this.modifiers.setFixture(fixture);
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

    @Override
    public void setTrackpadValue(GuiTrackpad trackpad, float value)
    {
        this.scrub.setValueFromScrub((int) value);
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
        this.cameraOptions = new GuiConfigCameraOptions(this);
        this.modifiers = new GuiModifiersManager(this);

        this.config = new GuiCameraConfig();
        this.config.options.add(this.cameraOptions);

        this.position = new Position(0, 0, 0, 0, 0);

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
        this.tooltips.put(12, I18n.format("aperture.gui.tooltips.modifiers"));
        this.tooltips.put(13, I18n.format("aperture.gui.tooltips.goto"));
        this.tooltips.put(50, I18n.format("aperture.gui.tooltips.add"));
        this.tooltips.put(51, I18n.format("aperture.gui.tooltips.dupe"));
        this.tooltips.put(52, I18n.format("aperture.gui.tooltips.remove"));
    }

    /**
     * Reset the camera editor
     */
    public void reset()
    {
        this.setProfile(null);
        this.scrub.value = 0;
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
        this.position.set(player);
        this.selectProfile(ClientProxy.control.currentProfile);
        this.profiles.init();

        Minecraft.getMinecraft().gameSettings.hideGUI = true;
        GuiIngameForge.renderHotbar = false;
        GuiIngameForge.renderCrosshairs = false;

        this.maxScrub = 0;
        this.visible = true;
        this.haveScrubbed = false;
        this.flight.enabled = false;
        this.lastFov = Minecraft.getMinecraft().gameSettings.fovSetting;

        this.runner.attachOutside();
    }

    public CameraProfile getProfile()
    {
        return this.profile;
    }

    public EntityPlayer getCamera()
    {
        return this.runner.outside.active ? this.runner.outside.camera : this.mc.player;
    }

    /**
     * Update player to current value in the scrub
     */
    public void updatePlayerCurrently(float ticks)
    {
        if ((this.syncing || this.runner.outside.active) && !this.runner.isRunning())
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

        this.position.set(player);
        this.profile.applyProfile(tick, ticks, this.position);

        this.position.apply(this.getCamera());
        ClientProxy.control.setRollAndFOV(this.position.angle.roll, this.position.angle.fov);
    }

    /**
     * This method should be invoked when values in the panel were modified
     */
    public void updateValues()
    {
        this.frame.min = 0;

        if (this.profile != null)
        {
            this.scrub.max = Math.max((int) this.profile.getDuration(), this.maxScrub);
            this.scrub.setValue(this.scrub.value);
            this.frame.max = this.profile.getDuration();
            this.frame.setValue(this.scrub.value);
        }
        else
        {
            this.scrub.max = this.maxScrub;
            this.scrub.setValue(0);
            this.frame.max = 0;
            this.frame.setValue(0);
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
        this.openModifiers = new GuiTextureButton(12, x + 2, y + 2, EDITOR_TEXTURE).setTexPos(80, 32).setActiveTexPos(80, 48);
        x -= w;
        this.save = new GuiTextureButton(9, x + 2, y + 2, EDITOR_TEXTURE);

        x -= w * 3.5;
        this.add = new GuiTextureButton(50, x + 2, y + 2, EDITOR_TEXTURE).setTexPos(224, 0).setActiveTexPos(224, 16);
        x += w;
        this.dupe = new GuiTextureButton(51, x + 2, y + 2, EDITOR_TEXTURE).setTexPos(176, 32).setActiveTexPos(176, 48);
        x += w;
        this.remove = new GuiTextureButton(52, x + 2, y + 2, EDITOR_TEXTURE).setTexPos(240, 0).setActiveTexPos(240, 16);
        y = 0;

        x = 80;
        this.goTo = new GuiTextureButton(13, x + 2, y + 2, EDITOR_TEXTURE).setTexPos(144, 32).setActiveTexPos(144, 48);
        x -= 20;
        this.moveForward = new GuiTextureButton(5, x + 2, y + 2, EDITOR_TEXTURE).setTexPos(144, 0).setActiveTexPos(144, 16);
        x -= w;
        this.moveDuration = new GuiTextureButton(6, x + 2, y + 2, EDITOR_TEXTURE).setTexPos(192, 0).setActiveTexPos(192, 16);
        x -= w;
        this.copyPosition = new GuiTextureButton(7, x + 2, y + 2, EDITOR_TEXTURE).setTexPos(176, 0).setActiveTexPos(176, 16);
        x -= w;
        this.moveBackward = new GuiTextureButton(8, x + 2, y + 2, EDITOR_TEXTURE).setTexPos(160, 0).setActiveTexPos(160, 16);

        this.frame = new GuiTrackpad(this, this.fontRenderer);
        this.frame.update(this.goTo.x - 2, this.goTo.y + 18, 80, 20);
        this.frame.title = "Frame";
        this.frame.amplitude = 1.0F;

        this.cameraProfileWasChanged(this.profile);
        this.updatePlauseButton();

        this.buttonList.add(this.toNextFixture);
        this.buttonList.add(this.nextFrame);
        this.buttonList.add(this.plause);
        this.buttonList.add(this.prevFrame);
        this.buttonList.add(this.toPrevFixture);

        this.buttonList.add(this.goTo);
        this.buttonList.add(this.moveForward);
        this.buttonList.add(this.moveDuration);
        this.buttonList.add(this.copyPosition);
        this.buttonList.add(this.moveBackward);

        this.buttonList.add(this.add);
        this.buttonList.add(this.dupe);
        this.buttonList.add(this.remove);
        this.buttonList.add(this.save);
        this.buttonList.add(this.openConfig);
        this.buttonList.add(this.openModifiers);
        this.buttonList.add(this.openProfiles);

        /* Setup areas of widgets */
        this.scrub.area.set(10, this.height - 20, this.width - 20, 20);
        this.popup.update(this.add.xPosition - 44, 20, 62, 102);

        if (this.fixturePanel != null)
        {
            this.fixturePanel.update(this);
        }

        this.config.update(width - 180, 20, 160, this.height - 80);
        this.profiles.update(width - 160, 20, 160, this.height - 80);
        this.modifiers.update(width - 240, 20, 200, this.height - 80);

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
            this.jumpToNextFrame();
        }
        else if (id == 2)
        {
            this.runner.toggle(this.profile, this.scrub.value);
            this.updatePlauseButton();

            this.playing = this.runner.isRunning();

            if (!this.playing)
            {
                this.runner.attachOutside();
                this.updatePlayerCurrently(0.0F);
            }

            ClientProxy.EVENT_BUS.post(new CameraEditorPlaybackStateEvent(this.playing, this.scrub.value));
        }
        else if (id == 3)
        {
            this.jumpToPrevFrame();
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
        else if (id == 6)
        {
            this.shiftDurationToCursor();
        }
        else if (id == 7 && this.fixturePanel != null)
        {
            ((GuiAbstractFixturePanel<AbstractFixture>) this.fixturePanel).editFixture(this.getCamera());
        }
        else if (id == 9 && this.profile != null)
        {
            this.profile.save();
        }
        else if (id == 10)
        {
            this.profiles.visible = !this.profiles.visible;
            this.config.visible = false;
            this.modifiers.visible = false;
            this.popup.visible = false;
        }
        else if (id == 11)
        {
            this.config.visible = !this.config.visible;
            this.profiles.visible = false;
            this.modifiers.visible = false;
            this.popup.visible = false;
        }
        else if (id == 12)
        {
            this.modifiers.visible = !this.modifiers.visible;
            this.profiles.visible = false;
            this.config.visible = false;
            this.popup.visible = false;
        }
        else if (id == 13)
        {
            this.showFrame = !this.showFrame;
        }

        /* Add, dupe and remove */
        if (id == 50)
        {
            this.popup.visible = !this.popup.visible;
            this.profiles.visible = false;
            this.config.visible = false;
            this.modifiers.visible = false;
        }
        else if (id == 51 && this.scrub.index >= 0)
        {
            AbstractFixture fixture = this.profile.get(this.scrub.index).clone();

            this.profile.add(fixture);
            this.pickCameraFixture(fixture, 0);

            this.updateValues();
        }
        else if (id == 52)
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
     * Jump to the next frame (tick)
     */
    private void jumpToNextFrame()
    {
        this.scrub.setValueFromScrub(this.scrub.value + 1);
    }

    /**
     * Jump to the previous frame (tick) 
     */
    private void jumpToPrevFrame()
    {
        this.scrub.setValueFromScrub(this.scrub.value - 1);
    }

    /**
     * Shift duration to the cursor  
     */
    private void shiftDurationToCursor()
    {
        if (this.fixturePanel == null)
        {
            return;
        }

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

    /**
     * Jump to the next camera fixture
     */
    private void jumpToNextFixture()
    {
        this.scrub.setValueFromScrub((int) this.profile.calculateOffset(this.scrub.value, true));
    }

    /**
     * Jump to previous fixture
     */
    private void jumpToPrevFixture()
    {
        this.scrub.setValueFromScrub((int) this.profile.calculateOffset(this.scrub.value - 1, false));
    }

    /**
     * Override default behavior to restore the rendering of hotbar and
     * crosshairs.
     */
    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException
    {
        if (keyCode == Keyboard.KEY_F1)
        {
            this.visible = !this.visible;
        }

        if (!this.hasActiveTextfields())
        {
            if (keyCode == 1)
            {
                Minecraft.getMinecraft().gameSettings.hideGUI = false;
                Minecraft.getMinecraft().gameSettings.fovSetting = this.lastFov;
                GuiIngameForge.renderHotbar = true;
                GuiIngameForge.renderCrosshairs = true;

                this.mc.displayGuiScreen((GuiScreen) null);

                if (!this.runner.isRunning())
                {
                    this.runner.detachOutside();
                }

                if (this.mc.currentScreen == null)
                {
                    this.mc.setIngameFocus();
                }

                return;
            }
            else
            {
                this.handleKeys(typedChar, keyCode);
            }
        }

        if (!this.visible)
        {
            return;
        }

        if (this.showFrame)
        {
            this.frame.keyTyped(typedChar, keyCode);
        }

        this.profiles.keyTyped(typedChar, keyCode);
        this.modifiers.keyTyped(typedChar, keyCode);
        this.config.keyTyped(typedChar, keyCode);

        if (this.profile == null)
        {
            return;
        }

        if (this.fixturePanel != null)
        {
            this.fixturePanel.keyTyped(typedChar, keyCode);
        }
    }

    /**
     * Whether camera editor has any active text fields on the screen 
     * (used to determine whether it's appropriate to handle shortcuts). 
     */
    private boolean hasActiveTextfields()
    {
        return this.profiles.hasAnyActiveTextfields() || this.modifiers.hasActiveTextfields() || (this.fixturePanel != null && this.fixturePanel.hasActiveTextfields()) || this.frame.text.isFocused();
    }

    /**
     * Handle shortcut keys when text fields aren't selected
     */
    private void handleKeys(char typedChar, int keyCode)
    {
        if (keyCode == Keyboard.KEY_F)
        {
            /* Toggle flight */
            this.cameraOptions.flight.playPressSound(this.mc.getSoundHandler());
            this.cameraOptions.flight.mousePressed(this.mc, this.cameraOptions.flight.x + 1, this.cameraOptions.flight.y + 1);
            this.cameraOptions.actionButtonPerformed(this.cameraOptions.flight);
        }

        if (this.flight.enabled)
        {
            return;
        }

        boolean shift = Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT);

        if (keyCode == Keyboard.KEY_S)
        {
            if (Keyboard.isKeyDown(Keyboard.KEY_LCONTROL) && this.profile != null)
            {
                /* Save camera profile */
                this.save.playPressSound(this.mc.getSoundHandler());
                this.profile.save();
            }
            else
            {
                /* Toggle sync */
                this.cameraOptions.sync.playPressSound(this.mc.getSoundHandler());
                this.cameraOptions.sync.mousePressed(this.mc, this.cameraOptions.sync.x + 1, this.cameraOptions.sync.y + 1);
                this.cameraOptions.actionButtonPerformed(this.cameraOptions.sync);
            }

        }
        else if (keyCode == Keyboard.KEY_O)
        {
            /* Toggle outside mode */
            this.cameraOptions.outside.playPressSound(this.mc.getSoundHandler());
            this.cameraOptions.outside.mousePressed(this.mc, this.cameraOptions.outside.x + 1, this.cameraOptions.outside.y + 1);
            this.cameraOptions.actionButtonPerformed(this.cameraOptions.outside);
        }
        else if (keyCode == Keyboard.KEY_SPACE)
        {
            try
            {
                /* Play/pause */
                this.actionPerformed(this.plause);
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
        else if (keyCode == Keyboard.KEY_D)
        {
            /* Deselect current fixture */
            this.pickCameraFixture(null, 0);
        }
        else if (keyCode == Keyboard.KEY_M)
        {
            this.shiftDurationToCursor();
        }
        else if (keyCode == Keyboard.KEY_RIGHT)
        {
            if (shift)
            {
                this.jumpToNextFixture();
            }
            else
            {
                this.jumpToNextFrame();
            }
        }
        else if (keyCode == Keyboard.KEY_LEFT)
        {
            if (shift)
            {
                this.jumpToPrevFixture();
            }
            else
            {
                this.jumpToPrevFrame();
            }
        }
        else if (keyCode == Keyboard.KEY_B && this.fixturePanel != null)
        {
            /* Copy the position */
            ((GuiAbstractFixturePanel<AbstractFixture>) this.fixturePanel).editFixture(this.getCamera());
        }
        else if (keyCode == Keyboard.KEY_O)
        {
            this.modifiers.visible = !this.modifiers.visible;
        }
    }

    @Override
    public void handleMouseInput() throws IOException
    {
        super.handleMouseInput();

        int x = Mouse.getEventX() * this.width / this.mc.displayWidth;
        int y = this.height - Mouse.getEventY() * this.height / this.mc.displayHeight - 1;

        int scroll = -Mouse.getEventDWheel();

        if (scroll != 0)
        {
            if (this.profiles.visible)
            {
                this.profiles.mouseScroll(x, y, scroll);
            }

            if (this.modifiers.visible)
            {
                this.modifiers.mouseScroll(x, y, scroll);
            }

            if (this.flight.enabled)
            {
                this.flight.speed += Math.copySign(0.1F, scroll);
                this.flight.speed = MathHelper.clamp(this.flight.speed, 0.1F, 50F);
            }
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException
    {
        if (!this.visible)
        {
            return;
        }

        if (this.showFrame)
        {
            this.frame.mouseClicked(mouseX, mouseY, mouseButton);
        }

        if (this.modifiers.visible)
        {
            this.modifiers.mouseClicked(mouseX, mouseY, mouseButton);

            if (this.modifiers.isInside(mouseX, mouseY))
            {
                return;
            }
        }

        if (this.profile == null || this.profiles.isInside(mouseX, mouseY))
        {
            this.profiles.mouseClicked(mouseX, mouseY, mouseButton);

            return;
        }
        else
        {
            this.profiles.name.setFocused(false);
        }

        this.config.mouseClicked(mouseX, mouseY, mouseButton);

        if (this.config.isInside(mouseX, mouseY))
        {
            return;
        }

        boolean wasVisible2 = this.popup.visible;

        this.popup.mouseClicked(mouseX, mouseY, mouseButton);

        if (this.popup.visible && this.popup.area.isInside(mouseX, mouseY))
        {
            return;
        }

        boolean wasVisible = this.config.visible;

        super.mouseClicked(mouseX, mouseY, mouseButton);

        if (this.config.visible && wasVisible && !this.config.area.isInside(mouseX, mouseY))
        {
            this.config.visible = false;
        }

        if (this.popup.visible && wasVisible2 && !this.popup.area.isInside(mouseX, mouseY))
        {
            this.popup.visible = false;
        }

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

        if (this.showFrame)
        {
            this.frame.mouseReleased(mouseX, mouseY, state);
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
        this.modifiers.mouseReleased(mouseX, mouseY, state);
    }

    /**
     * Draw everything on the screen
     */
    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        boolean isRunning = this.runner.isRunning();

        if (this.flight.enabled)
        {
            this.flight.animate(this.position);
            this.position.apply(this.getCamera());

            if (this.syncing && this.haveScrubbed && this.fixturePanel != null)
            {
                ((GuiAbstractFixturePanel<AbstractFixture>) this.fixturePanel).editFixture(this.getCamera());
            }
        }

        if (!this.visible || (isRunning && Aperture.proxy.config.camera_minema))
        {
            /* Little tip for the users who don't know what they did */
            if (!isRunning)
            {
                this.fontRenderer.drawStringWithShadow(I18n.format("aperture.gui.editor.f1"), 5, this.height - 12, 0xffffff);
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

            if (this.modifiers.visible)
            {
                Gui.drawRect(width - 60, 0, width - 40, 20, 0xaa000000);
            }

            if (this.popup.visible)
            {
                Gui.drawRect(this.add.xPosition - 2, 0, this.add.xPosition + 18, 20, 0xaa000000);
            }

            if (this.showFrame)
            {
                Gui.drawRect(this.goTo.x - 2, 0, this.goTo.x + 18, 20, 0xaa000000);
                this.frame.draw(mouseX, mouseY, partialTicks);
            }

            super.drawScreen(mouseX, mouseY, partialTicks);

            boolean running = this.runner.isRunning();

            if (running)
            {
                this.scrub.value = (int) this.runner.ticks;
                this.scrub.value = MathHelper.clamp(this.scrub.value, this.scrub.min, this.scrub.max);
                this.frame.setValue(this.scrub.value);
            }

            if (!running && this.playing)
            {
                this.updatePlauseButton();
                this.runner.attachOutside();
                this.scrub.setValueFromScrub(0);

                ClientProxy.EVENT_BUS.post(new CameraEditorPlaybackStateEvent(false, this.scrub.value));
                this.playing = false;
            }

            /* Sync the player on current tick */
            if ((this.runner.outside.active || (this.syncing && this.haveScrubbed)) && !this.flight.enabled)
            {
                this.updatePlayerCurrently(0.0F);
            }

            /* Display flight speed */
            if (this.flight.enabled)
            {
                String speed = String.format(this.stringSpeed + ": %.1f", this.flight.speed);
                int width = this.fontRenderer.getStringWidth(speed);
                int x = this.width - 10 - width;
                int y = this.height - 30;

                Gui.drawRect(x - 2, y - 2, x + width + 2, y + 9, 0x88000000);
                this.fontRenderer.drawStringWithShadow(speed, x, y, 0xffffff);
            }

            /* Display camera attributes */
            if ((this.syncing || running) && this.displayPosition)
            {
                Position pos = running ? this.runner.getPosition() : this.position;
                Point point = pos.point;
                Angle angle = pos.angle;

                String[] labels = new String[] {this.stringX + ": " + point.x, this.stringY + ": " + point.y, this.stringZ + ": " + point.z, this.stringYaw + ": " + angle.yaw, this.stringPitch + ": " + angle.pitch, this.stringRoll + ": " + angle.roll, this.stringFov + ": " + angle.fov};
                int i = 6;

                for (String label : labels)
                {
                    int width = this.fontRenderer.getStringWidth(label);
                    int y = this.height - 30 - 12 * i;

                    Gui.drawRect(8, y - 2, 9 + width + 2, y + 9, 0x88000000);
                    this.fontRenderer.drawStringWithShadow(label, 10, y, 0xffffff);

                    i--;
                }
            }

            /* Draw widgets */
            this.scrub.draw(mouseX, mouseY, partialTicks);
            this.drawIcons();

            if (this.fixturePanel != null)
            {
                this.fixturePanel.draw(mouseX, mouseY, partialTicks);
            }

            this.popup.draw(mouseX, mouseY, partialTicks);
        }

        this.config.draw(mouseX, mouseY, partialTicks);
        this.profiles.draw(mouseX, mouseY, partialTicks);
        this.modifiers.draw(mouseX, mouseY, partialTicks);

        if (this.profile != null)
        {
            for (GuiButton button : this.buttonList)
            {
                String label = this.tooltips.get(button.id);

                if (label != null && mouseX >= button.x && mouseY >= button.y && mouseX < button.x + button.width && mouseY < button.y + button.height)
                {
                    this.drawTooltip(label, button.x, button.y + button.height + 6);
                }
            }
        }
    }

    /**
     * Draw icons for indicating different active states (like syncing 
     * or flight mode) 
     */
    private void drawIcons()
    {
        if (!this.syncing && !this.flight.enabled)
        {
            return;
        }

        int x = this.width - 18;
        int y = 22;

        this.mc.renderEngine.bindTexture(EDITOR_TEXTURE);

        GlStateManager.color(1, 1, 1, 1);

        if (this.syncing)
        {
            Gui.drawModalRectWithCustomSizedTexture(x, y, 64, 32, 16, 16, 256, 256);
            x -= 20;
        }

        if (this.flight.enabled)
        {
            Gui.drawModalRectWithCustomSizedTexture(x, y, 64, 48, 16, 16, 256, 256);
        }
    }

    /**
     * Draw a tooltip for any hovered button 
     */
    private void drawTooltip(String label, int x, int y)
    {
        int width = this.fontRenderer.getStringWidth(label);

        if (x + width + 4 > this.width)
        {
            x = this.width - (width + 4);
        }

        if (x - 4 < 4)
        {
            x = 4;
        }

        Gui.drawRect(x - 3, y - 3, x + width + 3, y + this.fontRenderer.FONT_HEIGHT + 3, 0xaa000000);
        this.fontRenderer.drawStringWithShadow(label, x, y, 0xffffff);
    }
}