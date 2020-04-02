package mchorse.aperture.client.gui.panels.modifiers.widgets;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import mchorse.mclib.client.gui.framework.GuiTooltip;
import mchorse.mclib.client.gui.framework.elements.GuiElement;
import mchorse.mclib.client.gui.framework.elements.utils.GuiContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.resources.I18n;

public class GuiActiveWidget extends GuiElement
{
    public byte value;
    public List<String> labels = new ArrayList<String>();
    public Consumer<Byte> callback;

    public GuiActiveWidget(Minecraft mc, Consumer<Byte> callback)
    {
        super(mc);

        this.labels.add(I18n.format("aperture.gui.panels.x"));
        this.labels.add(I18n.format("aperture.gui.panels.y"));
        this.labels.add(I18n.format("aperture.gui.panels.z"));
        this.labels.add(I18n.format("aperture.gui.panels.yaw"));
        this.labels.add(I18n.format("aperture.gui.panels.pitch"));
        this.labels.add(I18n.format("aperture.gui.panels.roll"));
        this.labels.add(I18n.format("aperture.gui.panels.fov"));

        this.callback = callback;
    }

    @Override
    public boolean mouseClicked(GuiContext context)
    {
        if (super.mouseClicked(context))
        {
            return true;
        }

        if (this.area.isInside(context.mouseX, context.mouseY) && context.mouseButton == 0)
        {
            int index = (context.mouseX - this.area.x) / (this.area.w / 7);

            this.value ^= 1 << index;

            if (this.callback != null)
            {
                this.callback.accept(this.value);
            }

            return true;
        }

        return false;
    }

    @Override
    public void draw(GuiContext context)
    {
        super.draw(context);

        Gui.drawRect(this.area.x, this.area.y, this.area.x + this.area.w, this.area.y + this.area.h, 0x88000000);
        int w = (this.area.w / 7);

        for (int i = 0; i < 7; i++)
        {
            int x = this.area.x + w * i;
            boolean isSelected = ((this.value >> i) & 0x1) == 1;
            boolean isHover = this.area.isInside(context.mouseX, context.mouseY) && (context.mouseX - this.area.x) / w == i;
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

            this.drawCenteredString(this.font, this.labels.get(i), x + w / 2, this.area.my() - this.font.FONT_HEIGHT / 2, 0xffffff);
        }
    }
}