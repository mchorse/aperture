package mchorse.aperture.client.gui.config;

import mchorse.aperture.Aperture;
import mchorse.aperture.ClientProxy;
import mchorse.aperture.client.gui.GuiCameraEditor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fml.client.config.GuiCheckBox;
import net.minecraftforge.fml.common.Loader;

public class GuiConfigCameraOptions extends AbstractGuiConfigOptions
{
    private String title = I18n.format("aperture.gui.config.title");

    public GuiCheckBox outside;
    public GuiCheckBox minema;
    public GuiCheckBox spectator;
    public GuiCheckBox renderPath;
    public GuiCheckBox sync;
    public GuiCheckBox flight;
    public GuiCheckBox displayPosition;
    public GuiCheckBox minecrafttpTeleport;
    public GuiCheckBox tpTeleport;

    public int max;
    public int x;
    public int y;

    public GuiConfigCameraOptions(GuiCameraEditor editor)
    {
        super(editor);

        this.outside = new GuiCheckBox(0, 0, 0, I18n.format("aperture.gui.config.outside"), Aperture.proxy.config.camera_outside);
        this.outside.packedFGColour = 0xffffff;

        this.minema = new GuiCheckBox(-1, 0, 0, I18n.format("aperture.gui.config.minema"), Aperture.proxy.config.camera_minema);
        this.minema.packedFGColour = 0xffffff;

        this.spectator = new GuiCheckBox(-2, 0, 0, I18n.format("aperture.gui.config.spectator"), Aperture.proxy.config.camera_spectator);
        this.spectator.packedFGColour = 0xffffff;

        this.renderPath = new GuiCheckBox(-3, 0, 0, I18n.format("aperture.gui.config.show_path"), Aperture.proxy.config.camera_profile_render);
        this.renderPath.packedFGColour = 0xffffff;

        this.sync = new GuiCheckBox(-4, 0, 0, I18n.format("aperture.gui.config.sync"), this.editor.syncing);
        this.sync.packedFGColour = 0xffffff;

        this.flight = new GuiCheckBox(-5, 0, 0, I18n.format("aperture.gui.config.flight"), this.editor.flight.enabled);
        this.flight.packedFGColour = 0xffffff;

        this.displayPosition = new GuiCheckBox(-6, 0, 0, I18n.format("aperture.gui.config.display_info"), this.editor.displayPosition);
        this.displayPosition.packedFGColour = 0xffffff;

        this.minecrafttpTeleport = new GuiCheckBox(-7, 0, 0, I18n.format("aperture.gui.config.minecrafttp_teleport"), Aperture.proxy.config.minecrafttp_teleport);
        this.minecrafttpTeleport.packedFGColour = 0xffffff;

        this.tpTeleport = new GuiCheckBox(-8, 0, 0, I18n.format("aperture.gui.config.tp_teleport"), Aperture.proxy.config.tp_teleport);
        this.tpTeleport.packedFGColour = 0xffffff;

    }

    @Override
    public int getWidth()
    {
        return Math.max(this.max + 8, Minecraft.getMinecraft().fontRenderer.getStringWidth(this.title) + 8);
    }

    @Override
    public int getHeight()
    {
        return this.buttons.buttons.size() * 20 + 16;
    }

    @Override
    public void update(int x, int y)
    {
        int i = 0;
        this.buttons.clear();

        /* Don't show that if Minema mod isn't present */
        if (Loader.isModLoaded("minema"))
        {
            this.buttons.add(this.minema);
        }

        this.buttons.add(this.outside);
        this.buttons.add(this.spectator);
        this.buttons.add(this.renderPath);
        this.buttons.add(this.sync);
        this.buttons.add(this.flight);
        this.buttons.add(this.displayPosition);

        /* Show tp buttons if in multiplayer */
        if (!Minecraft.getMinecraft().isSingleplayer())
        {
            this.buttons.add(this.minecrafttpTeleport);
            this.buttons.add(this.tpTeleport);
        }

        for (GuiButton button : this.buttons.buttons)
        {
            this.max = Math.max(this.max, button.width);
        }

        for (GuiButton button : this.buttons.buttons)
        {
            button.x = x + 4;
            button.y = y + 4 + i * 20 + 16;

            i++;
        }

        this.x = x;
        this.y = y;

        if (this.minema != null)
        {
            this.minema.setIsChecked(Aperture.proxy.config.camera_minema);
        }

        this.outside.setIsChecked(Aperture.proxy.config.camera_outside);
        this.spectator.setIsChecked(Aperture.proxy.config.camera_spectator);
        this.renderPath.setIsChecked(Aperture.proxy.config.camera_profile_render);
        this.sync.setIsChecked(this.editor.syncing);
        this.flight.setIsChecked(this.editor.flight.enabled);
        this.displayPosition.setIsChecked(this.editor.displayPosition);
        this.minecrafttpTeleport.setIsChecked(Aperture.proxy.config.minecrafttp_teleport);
        this.tpTeleport.setIsChecked(Aperture.proxy.config.tp_teleport);
    }

    @Override
    public boolean isActive()
    {
        return true;
    }

    @Override
    public void actionButtonPerformed(GuiButton button)
    {
        /* Options */
        int id = button.id;
        boolean save = false;

        if (id == 0)
        {
            Property prop = Aperture.proxy.forge.getCategory("outside").get("camera_outside");

            prop.set(this.outside.isChecked());

            Aperture.proxy.forge.save();
            Aperture.proxy.config.reload();

            if (this.outside.isChecked())
            {
                ClientProxy.runner.attachOutside();
                this.editor.updatePlayerCurrently(0.0F);
            }
            else
            {
                ClientProxy.runner.detachOutside();
            }
        }
        else if (id == -1)
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
            this.renderPath.setIsChecked(Aperture.proxy.config.camera_profile_render);
        }
        else if (id == -4)
        {
            this.editor.syncing = this.sync.isChecked();
        }
        else if (id == -5)
        {
            this.editor.flight.enabled = this.flight.isChecked();
            this.editor.haveScrubbed = true;
        }
        else if (id == -6)
        {
            this.editor.displayPosition = !this.editor.displayPosition;
        }
        else if (id == -7)
        {
            Property prop = Aperture.proxy.forge.getCategory("camera").get("minecrafttp_teleport");

            prop.set(this.minecrafttpTeleport.isChecked());
            save = true;
        }
        else if (id == -8)
        {
            Property prop = Aperture.proxy.forge.getCategory("camera").get("tp_teleport");

            prop.set(this.tpTeleport.isChecked());
            save = true;
        }

        if (save)
        {
            Aperture.proxy.forge.save();
            Aperture.proxy.config.reload();
        }
    }

    @Override
    public void draw(int mouseX, int mouseY, float partialTicks)
    {
        super.draw(mouseX, mouseY, partialTicks);

        Minecraft.getMinecraft().fontRenderer.drawString(this.title, this.x + 4, this.y + 4, 0xffffff, true);
    }
}
