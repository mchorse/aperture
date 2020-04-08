package mchorse.aperture.client.gui.panels.modifiers;

import mchorse.aperture.camera.ModifierRegistry;
import mchorse.aperture.camera.modifiers.AbstractModifier;
import mchorse.aperture.client.gui.GuiModifiersManager;
import mchorse.mclib.client.gui.framework.elements.GuiElement;
import mchorse.mclib.client.gui.framework.elements.buttons.GuiIconElement;
import mchorse.mclib.client.gui.framework.elements.utils.GuiContext;
import mchorse.mclib.client.gui.utils.Icons;
import mchorse.mclib.client.gui.utils.resizers.layout.ColumnResizer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.resources.I18n;

public abstract class GuiAbstractModifierPanel<T extends AbstractModifier> extends GuiElement
{
    public T modifier;
    public GuiModifiersManager modifiers;

    public String title;

    public GuiIconElement enable;
    public GuiIconElement remove;
    public GuiIconElement moveUp;
    public GuiIconElement moveDown;
    public GuiIconElement copy;

    public GuiElement header;
    public GuiElement fields;

    public GuiAbstractModifierPanel(Minecraft mc, T modifier, GuiModifiersManager modifiers)
    {
        super(mc);

        this.modifier = modifier;
        this.modifiers = modifiers;

        this.fields = new GuiElement(mc);
        ColumnResizer.apply(this.fields, 5).vertical().stretch().height(20).padding(5);

        this.enable = new GuiIconElement(mc, Icons.NONE, (b) ->
        {
            this.modifier.enabled = !this.modifier.enabled;
            this.updateEnable();
            this.modifiers.editor.updateProfile();
        });
        this.remove = new GuiIconElement(mc, Icons.CLOSE, (b) -> this.modifiers.removeModifier(this));
        this.moveUp = new GuiIconElement(mc, Icons.MOVE_UP, (b) -> this.modifiers.moveModifier(this, -1));
        this.moveDown = new GuiIconElement(mc, Icons.MOVE_DOWN, (b) -> this.modifiers.moveModifier(this, 1));
        this.copy = new GuiIconElement(mc, Icons.COPY, (b) -> this.modifiers.setClipboard(this.modifier));

        this.header = new GuiElement(mc);
        this.header.flex().h(20);

        this.remove.flex().relative(this.header.area).set(0, 2, 16, 16).x(1, -18);
        this.enable.flex().relative(this.remove.resizer()).set(-20, 0, 16, 16);
        this.moveUp.flex().relative(this.enable.resizer()).set(-20, 0, 16, 8);
        this.moveDown.flex().relative(this.enable.resizer()).set(-20, 8, 16, 8);
        this.copy.flex().relative(this.moveUp.resizer()).set(-20, 0, 16, 16);

        this.header.add(this.enable, this.remove, this.moveUp, this.moveDown, this.copy);

        ColumnResizer.apply(this, 0).vertical().stretch();
        this.add(this.header, this.fields);

        this.title = I18n.format(ModifierRegistry.CLIENT.get(modifier.getClass()).title);
    }

    @Override
    public void resize()
    {
        super.resize();

        this.updateEnable();
    }

    protected void updateEnable()
    {
        this.enable.both(this.enable.isEnabled() ? Icons.UNLOCKED : Icons.LOCKED);
    }

    @Override
    public void draw(GuiContext context)
    {
        this.header.area.draw(0xaa000000 + ModifierRegistry.CLIENT.get(this.modifier.getClass()).color.getHex());
        this.font.drawStringWithShadow(this.title, this.area.x + 5, this.area.y + 10 - this.font.FONT_HEIGHT / 2, 0xffffff);

        this.header.setVisible(this.area.isInside(context.mouseX, context.mouseY));

        super.draw(context);
    }
}