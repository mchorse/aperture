package mchorse.aperture.client.gui.widgets;

import java.util.ArrayList;
import java.util.List;

import mchorse.aperture.client.gui.panels.IButtonListener;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;

public class GuiButtonList
{
    private Minecraft mc;
    private IButtonListener listener;

    public List<GuiButton> buttons = new ArrayList<GuiButton>();

    public GuiButtonList(Minecraft mc, IButtonListener listener)
    {
        this.mc = mc;
        this.listener = listener;
    }

    public void clear()
    {
        this.buttons.clear();
    }

    public void add(GuiButton button)
    {
        this.buttons.add(button);
    }

    /**
     * Mouse clicked
     *
     * This method is responsible for detecting which button was pressed and
     * notification of the listener when it is pressed.
     */
    public void mouseClicked(int mouseX, int mouseY, int mouseButton)
    {
        if (mouseButton == 0)
        {
            for (int i = 0; i < this.buttons.size(); ++i)
            {
                GuiButton button = this.buttons.get(i);

                if (button.mousePressed(this.mc, mouseX, mouseY))
                {
                    button.playPressSound(this.mc.getSoundHandler());

                    if (this.listener != null)
                    {
                        this.listener.actionButtonPerformed(button);
                    }
                }
            }
        }
    }

    /**
     * Just draw the buttons
     */
    public void draw(int mouseX, int mouseY)
    {
        for (GuiButton button : this.buttons)
        {
            button.drawButton(this.mc, mouseX, mouseY);
        }
    }
}