package mchorse.aperture.client.gui.panels.modifiers.widgets;

import java.util.ArrayList;
import java.util.List;

import mchorse.aperture.client.gui.panels.IGuiModule;
import mchorse.aperture.utils.Area;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;

public class GuiActiveWidget implements IGuiModule
{
    public Area area = new Area();
    public byte value;
    public List<String> labels = new ArrayList<String>();

    public GuiActiveWidget()
    {
        this.labels.add("X");
        this.labels.add("Y");
        this.labels.add("Z");
        this.labels.add("Yaw");
        this.labels.add("Pitch");
        this.labels.add("Roll");
        this.labels.add("FOV");
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton)
    {
        if (this.area.isInside(mouseX, mouseY))
        {
            int index = (mouseX - this.area.x) / (this.area.w / 7);

            this.value ^= 1 << index;

            System.out.println(Integer.toBinaryString(this.value));
        }
    }

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

            if (isSelected)
            {
                int color = 0xcc0088ff;
                int right = i == 6 ? this.area.x + this.area.w : x + w;

                Gui.drawRect(x, this.area.y, right, this.area.y + this.area.h, color);

                if (i != 6)
                {
                    Gui.drawRect(right - 1, this.area.y, right, this.area.y + this.area.h, 0x22000000);
                }
            }

            Minecraft.getMinecraft().currentScreen.drawCenteredString(Minecraft.getMinecraft().fontRendererObj, this.labels.get(i), x + w / 2, this.area.y + 7, 0xffffff);
        }
    }
}