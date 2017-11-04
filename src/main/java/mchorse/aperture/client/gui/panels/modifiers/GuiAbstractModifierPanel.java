package mchorse.aperture.client.gui.panels.modifiers;

import mchorse.aperture.camera.modifiers.AbstractModifier;
import mchorse.aperture.client.gui.panels.IGuiModule;
import net.minecraft.client.gui.FontRenderer;

public abstract class GuiAbstractModifierPanel<T extends AbstractModifier> implements IGuiModule
{
    public T modifier;
    public FontRenderer font;

    public int x;
    public int y;

    public GuiAbstractModifierPanel(T modifier, FontRenderer font)
    {
        this.modifier = modifier;
        this.font = font;
    }

    public void update(int x, int y)
    {
        this.x = x;
        this.y = y;
    }

    public int getHeight()
    {
        return 20;
    }
}