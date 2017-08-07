package mchorse.aperture.client.gui.config;

import java.util.ArrayList;
import java.util.List;

import mchorse.aperture.client.gui.panels.IGuiModule;
import mchorse.aperture.utils.Area;
import net.minecraft.client.gui.Gui;

public class GuiCameraConfig implements IGuiModule
{
    public Area area = new Area();
    public List<AbstractGuiConfigOptions> options = new ArrayList<AbstractGuiConfigOptions>();
    public List<AbstractGuiConfigOptions> activeOptions = new ArrayList<AbstractGuiConfigOptions>();

    public boolean visible;

    /**
     * Is mouse pointer inside 
     */
    public boolean isInside(int x, int y)
    {
        return this.visible && this.area.isInside(x, y);
    }

    public void update(int x, int y, int w, int h)
    {
        int width = 0;
        int height = 0;

        this.activeOptions.clear();

        for (AbstractGuiConfigOptions options : this.options)
        {
            if (!options.isActive())
            {
                continue;
            }

            width += options.getWidth();
            height = Math.max(height, options.getHeight());

            options.update(x + w - width, y);
            this.activeOptions.add(options);
        }

        this.area.set(x + w - width, y, width, height);
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton)
    {
        if (this.visible)
        {
            for (AbstractGuiConfigOptions options : this.activeOptions)
            {
                options.mouseClicked(mouseX, mouseY, mouseButton);
            }
        }
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int state)
    {
        if (this.visible)
        {
            for (AbstractGuiConfigOptions options : this.activeOptions)
            {
                options.mouseReleased(mouseX, mouseY, state);
            }
        }
    }

    @Override
    public void keyTyped(char typedChar, int keyCode)
    {
        if (this.visible)
        {
            for (AbstractGuiConfigOptions options : this.activeOptions)
            {
                options.keyTyped(typedChar, keyCode);
            }
        }
    }

    @Override
    public void draw(int mouseX, int mouseY, float partialTicks)
    {
        if (this.visible)
        {
            Gui.drawRect(this.area.x, this.area.y, this.area.x + this.area.w, this.area.y + this.area.h, 0xaa000000);

            for (AbstractGuiConfigOptions options : this.activeOptions)
            {
                options.draw(mouseX, mouseY, partialTicks);
            }
        }
    }
}