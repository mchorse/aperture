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

    public GuiButton remove;

    public GuiAbstractModifierPanel(T modifier, GuiModifiersManager modifiers, FontRenderer font)
    {
        this.modifier = modifier;
        this.modifiers = modifiers;
        this.font = font;

        this.remove = new GuiTextureButton(0, 0, 0, GuiCameraEditor.EDITOR_TEXTURE).setTexPos(32, 32).setActiveTexPos(32, 48);
    }

    public void update(int x, int y)
    {
        this.x = x;
        this.y = y;

        GuiUtils.setSize(this.remove, x - 18, y + 2, 16, 16);
    }

    public int getHeight()
    {
        return 20;
    }

    @Override
    public void draw(int mouseX, int mouseY, float partialTicks)
    {
        // Gui.drawRect(this.x - 160, this.y, this.x, this.y + this.getHeight(), 0x88000000 + this.y * 10);

        this.font.drawStringWithShadow(this.title, this.x - 160 + 5, this.y + 7, 0xffffff);

        this.remove.drawButton(this.modifiers.editor.mc, mouseX, mouseY, partialTicks);
    }
}