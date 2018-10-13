package mchorse.aperture.client.gui.panels.modifiers.widgets;

import java.util.ArrayList;
import java.util.List;

import mchorse.aperture.client.gui.panels.IGuiModule;
import mchorse.mclib.client.gui.utils.Area;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.resources.I18n;

public class GuiActiveWidget implements IGuiModule
{
    public Area area = new Area();
    public byte value;
    public List<String> labels = new ArrayList<String>();

    public GuiActiveWidget()
    {
        this.labels.add(I18n.format("aperture.gui.panels.x"));
        this.labels.add(I18n.format("aperture.gui.panels.y"));
        this.labels.add(I18n.format("aperture.gui.panels.z"));
        this.labels.add(I18n.format("aperture.gui.panels.yaw"));
        this.labels.add(I18n.format("aperture.gui.panels.pitch"));
        this.labels.add(I18n.format("aperture.gui.panels.roll"));
        this.labels.add(I18n.format("aperture.gui.panels.fov"));
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton)
    {
        if (this.area.isInside(mouseX, mouseY))
        {
            int index = (mouseX - this.area.x) / (this.area.w / 7);

            this.value ^= 1 << index;
        }
    }

    @Override
    public void mouseScroll(int x, int y, int scroll)
    {}

    @Override
    public void mouseReleased(int mouseX, int mouseY, int state)
    {}

    @Override
    public void keyTyped(char typedChar, int keyCode)
    {}

    @Override
    public void draw(int mouseX, int mouseY, float partialTicks)
    {
        Gui.drawRect(this.area.x, this.area.y, this.area.x + this.area.w, this.area.y + this.area.h, 0x88000000);
        int w = (this.area.w / 7);

        for (int i = 0; i < 7; i++)
        {
            int x = this.area.x + w * i;
            boolean isSelected = ((this.value >> i) & 0x1) == 1;
            boolean isHover = this.area.isInside(mouseX, mouseY) && (mouseX - this.area.x) / w == i;
            int right = i == 6 ? this.area.x + this.area.w : x + w;

            if (isSelected)
            {
                int color = 0xcc0088ff;

                Gui.drawRect(x, this.area.y, right, this.area.y + this.area.h, color);

                if (i != 6)
                {
                    Gui.drawRect(right - 1, this.area.y, right, this.area.y + this.area.h, 0x22000000);
                }
            }
            else if (isHover)
            {
                Gui.drawRect(x, this.area.y, right, this.area.y + this.area.h, 0x880088ff);
            }

            Minecraft.getMinecraft().currentScreen.drawCenteredString(Minecraft.getMinecraft().fontRenderer, this.labels.get(i), x + w / 2, this.area.y + 7, 0xffffff);
        }
    }
}