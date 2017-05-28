package mchorse.aperture.client.gui.panels;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;

/**
 * This interface is responsible for receiving call when a button was clicked
 */
public interface IButtonListener
{
    /**
     * The same thing as {@link GuiScreen}'s actionPerform method
     */
    public void actionButtonPerformed(GuiButton button);
}