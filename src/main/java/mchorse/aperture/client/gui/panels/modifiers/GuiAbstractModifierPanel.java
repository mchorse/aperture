package mchorse.aperture.client.gui.panels.modifiers;

import mchorse.aperture.camera.modifiers.AbstractModifier;
import mchorse.aperture.client.gui.GuiCameraEditor;
import mchorse.aperture.client.gui.GuiModifiersManager;
import mchorse.aperture.client.gui.panels.IGuiModule;
import mchorse.aperture.client.gui.utils.GuiUtils;
import mchorse.aperture.client.gui.widgets.buttons.GuiTextureButton;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;

public abstract class GuiAbstractModifierPanel<T extends AbstractModifier> implements IGuiModule
{
    public T modifier;
    public GuiModifiersManager modifiers;
    public FontRenderer font;

    public String title = "";
    public int x;
    public int y;
    public int w;

    public GuiButton remove;

    public GuiAbstractModifierPanel(T modifier, GuiModifiersManager modifiers, FontRenderer font)
    {
        this.modifier = modifier;
        this.modifiers = modifiers;
        this.font = font;

        this.remove = new GuiTextureButton(0, 0, 0, GuiCameraEditor.EDITOR_TEXTURE).setTexPos(32, 32).setActiveTexPos(32, 48);
    }

    public void update(int x, int y, int w)
    {
        this.x = x;
        this.y = y;
        this.w = w;

        GuiUtils.setSize(this.remove, x + w - 18, y + 2, 16, 16);
    }

    public int getHeight()
    {
        return 20;
    }

    public boolean hasActiveTextfields()
    {
        return false;
    }

    @Override
    public void draw(int mouseX, int mouseY, float partialTicks)
    {
        int h = this.getHeight();

        this.font.drawStringWithShadow(this.title, this.x + 5, this.y + 7, 0xffffff);

        if (mouseX >= this.x && mouseY >= this.y && mouseX <= this.x + this.w && mouseY <= this.y + h)
        {
            this.remove.drawButton(this.modifiers.editor.mc, mouseX, mouseY);
        }
    }
}