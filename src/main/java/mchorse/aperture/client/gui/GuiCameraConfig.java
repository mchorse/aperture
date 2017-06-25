package mchorse.aperture.client.gui;

import mchorse.aperture.Aperture;
import mchorse.aperture.ClientProxy;
import mchorse.aperture.client.gui.panels.IButtonListener;
import mchorse.aperture.client.gui.panels.IGuiModule;
import mchorse.aperture.client.gui.widgets.GuiButtonList;
import mchorse.aperture.utils.Area;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fml.client.config.GuiCheckBox;
import net.minecraftforge.fml.common.Loader;

public class GuiCameraConfig implements IGuiModule, IButtonListener
{
    public Area area = new Area();
    public GuiButtonList buttons;
    public boolean visible;

    /* Camera options */
    public GuiCheckBox minema;
    public GuiCheckBox spectator;
    public GuiCheckBox renderPath;
    public GuiCheckBox sync;

    public GuiCameraEditor editor;

    public GuiCameraConfig(GuiCameraEditor editor)
    {
        this.editor = editor;
        this.buttons = new GuiButtonList(Minecraft.getMinecraft(), this);
    }

    /**
     * Is mouse pointer inside 
     */
    public boolean isInside(int x, int y)
    {
        return this.visible && this.area.isInside(x, y);
    }

    public void update(int x, int y, int w, int h)
    {
        this.area.set(x, y, w, h);

        x += 4;
        y += 4;

        /* Don't show that if Minema mod isn't present */
        if (Loader.isModLoaded("minema"))
        {
            this.minema = new GuiCheckBox(-1, x, y, "Minema", Aperture.proxy.config.camera_minema);
            this.minema.packedFGColour = 0xffffff;
            y += 20;

            this.buttons.add(this.minema);
        }

        this.spectator = new GuiCheckBox(-2, x, y, "Spectator", Aperture.proxy.config.camera_spectator);
        this.spectator.packedFGColour = 0xffffff;
        y += 20;

        this.renderPath = new GuiCheckBox(-3, x, y, "Show path", ClientProxy.renderer.render);
        this.renderPath.packedFGColour = 0xffffff;
        y += 20;

        this.sync = new GuiCheckBox(-4, x, y, "Sync", this.editor.syncing);
        this.sync.packedFGColour = 0xffffff;

        this.buttons.clear();
        this.buttons.add(this.spectator);
        this.buttons.add(this.renderPath);
        this.buttons.add(this.sync);

        int max = 0;

        for (GuiButton button : this.buttons.buttons)
        {
            if (button.width > max)
            {
                max = button.width;
            }
        }

        this.area.x = this.area.x + this.area.w - (max + 8);
        this.area.w = max + 8;

        for (GuiButton button : this.buttons.buttons)
        {
            button.xPosition = this.area.x + 4;
        }

        this.area.h = this.buttons.buttons.size() * 20;
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton)
    {
        if (this.visible)
        {
            if (!this.area.isInside(mouseX, mouseY))
            {
                this.visible = false;

                return;
            }

            this.buttons.mouseClicked(mouseX, mouseY, mouseButton);
        }
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int state)
    {}

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

            System.out.println(ClientProxy.renderer.render);
        }
        else if (id == -4)
        {
            this.editor.syncing = this.sync.isChecked();
        }

        if (save)
        {
            Aperture.proxy.onConfigChange(Aperture.proxy.forge);
            Aperture.proxy.forge.save();
            Aperture.proxy.config.reload();
        }
    }

    @Override
    public void keyTyped(char typedChar, int keyCode)
    {}

    @Override
    public void draw(int mouseX, int mouseY, float partialTicks)
    {
        if (this.visible)
        {
            Gui.drawRect(this.area.x, this.area.y, this.area.x + this.area.w, this.area.y + this.area.h, 0xaa000000);

            this.buttons.draw(mouseX, mouseY);
        }
    }
}