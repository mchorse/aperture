package mchorse.aperture.client.gui;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

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
import mchorse.aperture.client.gui.GuiCameraFixtures.IFixturePicker;
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
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.client.GuiIngameForge;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fml.client.config.GuiCheckBox;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Camera editor GUI
 *
 * This GUI is responsible for editing current camera profile. This dude
 * includes should be able to allow the
 */
@SideOnly(Side.CLIENT)
public class GuiCameraEditor extends GuiScreen implements IScrubListener, IFixturePicker, IFixtureSelector, IProfileListener
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
     * Whether cameras are sync'd every render tick. Usable for target based
     * fixtures only
     */
    private boolean syncing;

    /**
     * Flag for observing the runner
     */
    private boolean playing = false;

    /**
     * Whether things on the screen are visible 
     */
    private boolean visible = true;

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

    /* Camera options */
    public GuiCheckBox minema;
    public GuiCheckBox spectator;
    public GuiCheckBox renderPath;
    public GuiCheckBox sync;

    /* Playback scrub */
    public GuiPlaybackScrub scrub;

    /* Panel that displays camera fixtures */
    public GuiButton add;
    public GuiButton remove;
    public GuiCameraFixtures fixtures;
    public GuiFixturesPopup popup;
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

        /* TODO: Implement hook */
    }

    /**
     * Pick a camera fixture
     *
     * This method is responsible for setting current fixture panel which in
     * turn then will allow to edit properties of the camera fixture
     */
    @Override
    @SuppressWarnings("unchecked")
    public void pickCameraFixture(AbstractFixture fixture)
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
                panel.select(fixture);
                panel.update(this);

                this.fixturePanel = panel;

                if (this.syncing)
                {
                    this.scrub.setValue((int) this.profile.calculateOffset(fixture));
                }
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
    public void selectFixture(AbstractFixture fixture)
    {
        if (fixture != null)
        {
            this.profile.add(fixture);
            this.updateValues();
        }
    }

    /**
     * Camera profile was selected from the profile manager 
     */
    @Override
    public void selectProfile(CameraProfile profile)
    {
        ClientProxy.control.currentProfile = profile;

        this.setProfile(profile);
    }

    /**
     * Initialize the camera editor with a camera profile.
     */
    public GuiCameraEditor(CameraRunner runner)
    {
        this.runner = runner;
        this.scrub = new GuiPlaybackScrub(this, null);
        this.fixtures = new GuiCameraFixtures(this, null);
        this.popup = new GuiFixturesPopup(this);
        this.profiles = new GuiProfilesManager(this);
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
        this.fixtures.setProfile(profile);
        this.profiles.init();

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
        this.setProfile(ClientProxy.control.currentProfile);

        GuiIngameForge.renderHotbar = false;
        GuiIngameForge.renderCrosshairs = false;
        Minecraft.getMinecraft().gameSettings.hideGUI = true;

        this.updateValues();
        this.syncing = false;
        this.visible = true;
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

        EntityPlayer player = Minecraft.getMinecraft().thePlayer;
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
            this.scrub.max = (int) this.profile.getDuration();
            this.scrub.setValue(this.scrub.value);
            this.fixtures.updateScroll();
        }
        else
        {
            this.scrub.max = 0;
            this.scrub.setValue(0);
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
        int x = this.width - 10;
        int y = 5;
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

        x -= w + 10;
        this.moveForward = new GuiTextureButton(5, x + 2, y + 2, EDITOR_TEXTURE).setTexPos(0, 32).setActiveTexPos(0, 48);
        x -= w;
        this.moveDuration = new GuiTextureButton(6, x + 2, y + 2, EDITOR_TEXTURE).setTexPos(48, 32).setActiveTexPos(48, 48);
        x -= w;
        this.copyPosition = new GuiTextureButton(7, x + 2, y + 2, EDITOR_TEXTURE).setTexPos(32, 32).setActiveTexPos(32, 48);
        x -= w;
        this.moveBackward = new GuiTextureButton(8, x + 2, y + 2, EDITOR_TEXTURE).setTexPos(16, 32).setActiveTexPos(16, 48);

        this.updatePlauseButton();

        x = 10;
        y += 4;

        /* Don't show that if Minema mod isn't present */
        if (Loader.isModLoaded("minema"))
        {
            this.minema = new GuiCheckBox(-1, x, y, "Minema", Aperture.proxy.config.camera_minema);
            this.minema.packedFGColour = 0xffffff;
            x += this.minema.width + 5;

            this.buttonList.add(this.minema);
        }

        this.spectator = new GuiCheckBox(-2, x, y, "Spectator", Aperture.proxy.config.camera_spectator);
        this.spectator.packedFGColour = 0xffffff;
        x += this.spectator.width + 5;

        this.renderPath = new GuiCheckBox(-3, x, y, "Show path", ClientProxy.renderer.render);
        this.renderPath.packedFGColour = 0xffffff;
        x += this.renderPath.width + 5;

        this.sync = new GuiCheckBox(-4, x, y, "Sync", this.syncing);
        this.sync.packedFGColour = 0xffffff;

        this.add = new GuiButton(50, this.width - 50, this.height - 20, 20, 20, "+");
        this.remove = new GuiButton(51, this.width - 30, this.height - 20, 20, 20, "-");

        this.buttonList.add(this.toNextFixture);
        this.buttonList.add(this.nextFrame);
        this.buttonList.add(this.plause);
        this.buttonList.add(this.prevFrame);
        this.buttonList.add(this.toPrevFixture);

        this.buttonList.add(this.moveForward);
        this.buttonList.add(this.moveDuration);
        this.buttonList.add(this.copyPosition);
        this.buttonList.add(this.moveBackward);

        this.buttonList.add(this.spectator);
        this.buttonList.add(this.renderPath);
        this.buttonList.add(this.sync);

        this.buttonList.add(this.add);
        this.buttonList.add(this.remove);

        /* Setup areas of widgets */
        this.scrub.area.set(10, 27, this.width - 20, 20);
        this.fixtures.area.set(10, this.height - 20, this.width - 60, 20);
        this.fixtures.updateScroll();
        this.popup.update(width / 2 - 32, height / 2 - 51, 62, 102);

        if (this.fixturePanel != null)
        {
            this.fixturePanel.update(this);
        }

        this.profiles.update(width / 2 - 80, 55, 160, this.height - 85);
        this.profiles.visible = true;
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

            if (this.playing)
            {
                /* TODO: Implement hook to start playing director */
            }
            else
            {
                /* TODO: Implement hook to pause director */
            }
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
            int index = this.fixtures.getIndex();
            int to = index + (id == 8 ? -1 : 1);

            profile.move(index, to);

            if (profile.has(to))
            {
                this.fixtures.setIndex(to);
            }
        }
        else if (id == 6 && this.fixturePanel != null)
        {
            /* Move duration to the scrub location */
            AbstractFixture fixture = this.profile.get(this.fixtures.getIndex());
            long offset = this.profile.calculateOffset(fixture);

            if (this.scrub.value > offset)
            {
                fixture.setDuration(this.scrub.value - offset);
                this.updateValues();
                this.fixturePanel.select(fixture);
            }
        }
        else if (id == 7 && this.fixturePanel != null)
        {
            ((GuiAbstractFixturePanel<AbstractFixture>) this.fixturePanel).editFixture();
        }

        if (id == 50)
        {
            this.popup.visible = true;
        }
        else if (id == 51)
        {
            int index = this.fixtures.getIndex();
            int size = this.profile.getCount();

            if (index >= 0 && index < size)
            {
                this.profile.remove(index);
                this.fixtures.decrementIndex();

                if (this.fixtures.getIndex() >= 0)
                {
                    this.pickCameraFixture(this.profile.get(this.fixtures.getIndex()));
                }
                else
                {
                    this.pickCameraFixture(null);
                }

                this.updateValues();
            }
        }

        /* Options */
        boolean save = false;

        if (id == -1)
        {
            Property prop = Aperture.proxy.forge.getCategory("camera").get("camera_minema");

            prop.set(this.minema.isChecked());
            save = true;
        }
        else if (id == -2)
        {
            Property prop = Aperture.proxy.forge.getCategory("camera").get("camera_spectator");

            prop.set(this.spectator.isChecked());
            save = true;
        }
        else if (id == -3)
        {
            ClientProxy.renderer.toggleRender();
            this.renderPath.setIsChecked(ClientProxy.renderer.render);
        }
        else if (id == -4)
        {
            this.syncing = this.sync.isChecked();
        }

        if (save)
        {
            Aperture.proxy.onConfigChange(Aperture.proxy.forge);
            Aperture.proxy.forge.save();
            Aperture.proxy.config.reload();
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
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException
    {
        if (!this.visible)
        {
            return;
        }

        this.profiles.mouseClicked(mouseX, mouseY, mouseButton);

        if (this.profile == null || this.profiles.isInside(mouseX, mouseY))
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
        this.fixtures.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state)
    {
        if (!this.visible || this.profile == null)
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
        this.fixtures.mouseReleased(mouseX, mouseY, state);
    }

    /**
     * Draw everything on the screen
     */
    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        if (!this.visible)
        {
            /* Little tip for the users who don't know what they did */
            this.fontRendererObj.drawStringWithShadow("Press F1 to show GUI again...", 5, this.height - 12, 0xffffff);

            return;
        }

        if (this.profile != null)
        {
            int x = this.width - 10 - 20 * 5;
            int y = 5;

            Gui.drawRect(x, y, x + 100, y + 20, 0x88000000);
            this.drawHorizontalLine(x, x + 99, y, 0xff000000);
            this.drawHorizontalLine(x, x + 99, y + 19, 0xff000000);
            this.drawVerticalLine(x, y, y + 19, 0xff000000);
            this.drawVerticalLine(x + 99, y, y + 19, 0xff000000);

            Gui.drawRect(x - 90, y, x - 10, y + 20, 0x88000000);
            this.drawHorizontalLine(x - 90, x - 10, y, 0xff000000);
            this.drawHorizontalLine(x - 90, x - 10, y + 19, 0xff000000);
            this.drawVerticalLine(x - 90, y, y + 19, 0xff000000);
            this.drawVerticalLine(x - 10, y, y + 19, 0xff000000);

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
                this.scrub.value = MathHelper.clamp_int(this.scrub.value, 0, this.scrub.max);
            }

            if (!running && this.playing)
            {
                this.updatePlauseButton();

                /* TODO: Implement hook to pause director */
                this.playing = false;
            }

            /* Draw widgets */
            this.scrub.draw(mouseX, mouseY, partialTicks);
            this.fixtures.draw(mouseX, mouseY, partialTicks);

            if (this.fixturePanel != null)
            {
                this.fixturePanel.draw(mouseX, mouseY, partialTicks);
            }

            this.popup.draw(mouseX, mouseY, partialTicks);
        }

        this.profiles.draw(mouseX, mouseY, partialTicks);
    }
}