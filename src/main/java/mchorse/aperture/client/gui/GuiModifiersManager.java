package mchorse.aperture.client.gui;

import mchorse.aperture.camera.ModifierRegistry;
import mchorse.aperture.camera.ModifierRegistry.ModifierInfo;
import mchorse.aperture.camera.fixtures.AbstractFixture;
import mchorse.aperture.camera.modifiers.AbstractModifier;
import mchorse.aperture.client.gui.panels.modifiers.GuiAbstractModifierPanel;
import mchorse.mclib.client.gui.framework.elements.GuiElement;
import mchorse.mclib.client.gui.framework.elements.GuiScrollElement;
import mchorse.mclib.client.gui.framework.elements.buttons.GuiButtonElement;
import mchorse.mclib.client.gui.framework.elements.buttons.GuiIconElement;
import mchorse.mclib.client.gui.framework.elements.utils.GuiContext;
import mchorse.mclib.client.gui.utils.Icons;
import mchorse.mclib.client.gui.utils.resizers.layout.ColumnResizer;
import mchorse.mclib.utils.MathUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.resources.I18n;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GuiModifiersManager extends GuiElement
{
    /**
     * Registry of camera modifier panels 
     */
    public static final Map<Class<? extends AbstractModifier>, Class<? extends GuiAbstractModifierPanel<? extends AbstractModifier>>> PANELS = new HashMap<>();

    /* Strings */
    private String stringTitle = I18n.format("aperture.gui.modifiers.title");
    private String stringGlobal = I18n.format("aperture.gui.modifiers.global");

    private AbstractModifier clipboard;

    /**
     * Fixture whose modifiers are getting managed 
     */
    public AbstractFixture fixture;

    /**
     * Whether one of the modifier was removed, added or moved, so the 
     * manager knew that there is something going on.
     */
    public boolean modified = false;

    /**
     * Add buttons (which add different {@link AbstractModifier}s)
     */
    public GuiElement buttons;

    /**
     * Modifier panels 
     */
    public GuiScrollElement panels;

    /**
     * Button to show add buttons 
     */
    public GuiIconElement add;

    /**
     * Button to paste a modifier in the clipboard
     */
    public GuiIconElement paste;

    /**
     * Reference to the parent screen (the camera editor) 
     */
    public GuiCameraEditor editor;

    public GuiModifiersManager(Minecraft mc, GuiCameraEditor editor)
    {
        super(mc);

        this.editor = editor;
        this.add = new GuiIconElement(mc, Icons.ADD, (b) -> this.buttons.setVisible(!this.buttons.isVisible()));

        this.paste = new GuiIconElement(mc, Icons.PASTE, (b) ->
        {
            if (this.clipboard != null)
            {
                AbstractModifier modifier = this.clipboard.copy();

                this.getModifiers().add(modifier);
                this.setFixture(this.fixture);
                this.editor.updateProfile();
            }
        });

        this.buttons = new GuiElement(mc);
        this.buttons.setVisible(false);
        this.buttons.flex().relative(this.area).x(1, 0).y(20).w(0.5F, 0).anchor(1, 0);
        ColumnResizer.apply(this.buttons, 0).vertical().stretch();

        for (ModifierInfo info : ModifierRegistry.CLIENT.values())
        {
            byte type = info.type;
            int color = 0xff000000 + info.color.getHex();

            GuiButtonElement button = new GuiButtonElement(mc, I18n.format(info.title), (b) ->
            {
                this.addCameraModifier(type, this.getModifiers());
                this.buttons.setVisible(false);
            });

            button.color(color).flex().relative(this.area).h(20);
            this.buttons.add(button);
        }

        this.panels = new GuiScrollElement(mc);
        this.panels.flex().relative(this.area).y(20).w(1F, 0).h(1, -20);
        ColumnResizer.apply(this.panels, 0).vertical().stretch().scroll();

        this.add.flex().relative(this.area).set(0, 2, 16, 16).x(1, -18);
        this.paste.flex().relative(this.add.resizer()).set(-20, 0, 16, 16);
        this.add(this.add, this.paste, this.panels, this.buttons);

        this.hideTooltip();
    }

    public List<AbstractModifier> getModifiers()
    {
        if (this.fixture != null)
        {
            return this.fixture.getModifiers();
        }
        else if (this.editor.getProfile() != null)
        {
            return this.editor.getProfile().getModifiers();
        }

        return null;
    }

    public void setClipboard(AbstractModifier modifier)
    {
        this.clipboard = modifier.copy();
    }

    /**
     * Set fixture current fixture to manage  
     */
    public void setFixture(AbstractFixture fixture)
    {
        this.panels.clear();
        this.fixture = fixture;

        if (this.getModifiers() == null)
        {
            return;
        }

        for (AbstractModifier modifier : this.getModifiers())
        {
            this.addModifier(modifier);
        }
    }

    /**
     * Add a camera modifier for current
     */
    @SuppressWarnings("unchecked")
    public void addModifier(AbstractModifier modifier)
    {
        Class<? extends GuiAbstractModifierPanel<? extends AbstractModifier>> clazz = PANELS.get(modifier.getClass());

        if (clazz != null)
        {
            try
            {
                GuiAbstractModifierPanel<AbstractModifier> panel = (GuiAbstractModifierPanel<AbstractModifier>) clazz.getConstructor(Minecraft.class, modifier.getClass(), GuiModifiersManager.class).newInstance(this.mc, modifier, this);

                this.panels.add(panel);
                this.panels.resize();
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }

    public void moveModifier(GuiAbstractModifierPanel<? extends AbstractModifier> panel, int direction)
    {
        int index = this.panels.getChildren().indexOf(panel);

        if (index == -1)
        {
            return;
        }

        int to = index + direction;

        if (to < 0 || to >= this.panels.getChildren().size())
        {
            return;
        }

        List<AbstractModifier> modifiers = this.getModifiers();

        this.panels.getChildren().add(to, this.panels.getChildren().remove(index));
        modifiers.add(to, modifiers.remove(index));
        this.resize();
        this.editor.updateProfile();
        this.modified = true;
    }

    public void removeModifier(GuiAbstractModifierPanel<? extends AbstractModifier> panel)
    {
        this.panels.getChildren().remove(panel);
        this.getModifiers().remove(panel.modifier);

        this.resize();
        this.editor.updateProfile();
        this.modified = true;
    }

    private void addCameraModifier(int id, List<AbstractModifier> modifiers)
    {
        try
        {
            AbstractModifier modifier = ModifierRegistry.fromType((byte) id);

            modifiers.add(modifier);
            this.addModifier(modifier);
            this.editor.updateProfile();
        }
        catch (Exception e)
        {}
    }

    @Override
    public void draw(GuiContext context)
    {
        /* Background */
        int h = Math.min(this.panels.scroll.scrollSize + 20, this.area.h);

        if (this.panels.getChildren().isEmpty())
        {
            h = 20;
        }

        Gui.drawRect(this.area.x, this.area.y, this.area.ex(), this.area.y + h, 0xaa000000);
        Gui.drawRect(this.area.x, this.area.y, this.area.ex(), this.area.y + 20, 0x88000000);
        this.font.drawStringWithShadow(this.fixture == null ? this.stringGlobal : this.stringTitle, this.area.x + 6, this.area.y + 10 - this.font.FONT_HEIGHT / 2, 0xffffff);

        super.draw(context);
    }
}