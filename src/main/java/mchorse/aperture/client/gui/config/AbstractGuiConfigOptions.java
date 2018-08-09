package mchorse.aperture.client.gui.config;

import mchorse.aperture.client.gui.GuiCameraEditor;
import mchorse.aperture.client.gui.panels.IButtonListener;
import mchorse.aperture.client.gui.panels.IGuiModule;
import mchorse.aperture.client.gui.widgets.GuiButtonList;
import net.minecraft.client.Minecraft;

public abstract class AbstractGuiConfigOptions implements IGuiModule, IButtonListener
{
    public GuiButtonList buttons;
    public GuiCameraEditor editor;

    public AbstractGuiConfigOptions(GuiCameraEditor editor)
    {
        this.editor = editor;
        this.buttons = new GuiButtonList(Minecraft.getMinecraft(), this);
    }

    public abstract int getWidth();

    public abstract int getHeight();

    public abstract void update(int x, int y);

    public abstract boolean isActive();

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton)
    {
        this.buttons.mouseClicked(mouseX, mouseY, mouseButton);
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
        this.buttons.draw(mouseX, mouseY, partialTicks);
    }
}