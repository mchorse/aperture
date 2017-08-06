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

    public GuiCheckBox minema;
    public GuiCheckBox spectator;
    public GuiCheckBox renderPath;
    public GuiCheckBox sync;
    public GuiCheckBox flight;

    public int max;
    public int x;
    public int y;

    public GuiConfigCameraOptions(GuiCameraEditor editor)
    {
        super(editor);

        /* Don't show that if Minema mod isn't present */
        if (Loader.isModLoaded("minema"))
        {
            this.minema = new GuiCheckBox(-1, 0, 0, I18n.format("aperture.gui.config.minema"), Aperture.proxy.config.camera_minema);
            this.minema.packedFGColour = 0xffffff;

            this.buttons.add(this.minema);
        }

        this.spectator = new GuiCheckBox(-2, 0, 0, I18n.format("aperture.gui.config.spectator"), Aperture.proxy.config.camera_spectator);
        this.spectator.packedFGColour = 0xffffff;

        this.renderPath = new GuiCheckBox(-3, 0, 0, I18n.format("aperture.gui.config.show_path"), ClientProxy.renderer.render);
        this.renderPath.packedFGColour = 0xffffff;

        this.sync = new GuiCheckBox(-4, 0, 0, I18n.format("aperture.gui.config.sync"), this.editor.syncing);
        this.sync.packedFGColour = 0xffffff;

        this.flight = new GuiCheckBox(-5, 0, 0, I18n.format("aperture.gui.config.flight"), this.editor.flight.enabled);
        this.flight.packedFGColour = 0xffffff;

        this.buttons.add(this.spectator);
        this.buttons.add(this.renderPath);
        this.buttons.add(this.sync);
        this.buttons.add(this.flight);

        for (GuiButton button : this.buttons.buttons)
        {
            this.max = Math.max(this.max, button.width);
        }
    }

    @Override
    public int getWidth()
    {
        return Math.max(this.max + 8, Minecraft.getMinecraft().fontRendererObj.getStringWidth(this.title) + 8);
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

        for (GuiButton button : this.buttons.buttons)
        {
            button.xPosition = x + 4;
            button.yPosition = y + 4 + i * 20 + 16;

            i++;
        }

        this.x = x;
        this.y = y;
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
            this.editor.syncing = this.sync.isChecked();
        }
        else if (id == -5)
        {
            this.editor.flight.enabled = this.flight.isChecked();
            this.editor.haveScrubbed = true;
        }

        if (save)
        {
            Aperture.proxy.onConfigChange(Aperture.proxy.forge);
            Aperture.proxy.forge.save();
            Aperture.proxy.config.reload();
        }
    }

    @Override
    public void draw(int mouseX, int mouseY, float partialTicks)
    {
        super.draw(mouseX, mouseY, partialTicks);

        Minecraft.getMinecraft().fontRendererObj.drawString(this.title, this.x + 4, this.y + 4, 0xffffff, true);
    }
}