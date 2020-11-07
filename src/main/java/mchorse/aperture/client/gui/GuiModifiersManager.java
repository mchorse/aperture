package mchorse.aperture.client.gui;

import mchorse.aperture.camera.ModifierRegistry;
import mchorse.aperture.camera.ModifierRegistry.ModifierInfo;
import mchorse.aperture.camera.fixtures.AbstractFixture;
import mchorse.aperture.camera.modifiers.AbstractModifier;
import mchorse.aperture.client.gui.dashboard.GuiCameraEditor;
import mchorse.aperture.client.gui.panels.modifiers.GuiAbstractModifierPanel;
import mchorse.mclib.client.gui.framework.elements.GuiElement;
import mchorse.mclib.client.gui.framework.elements.GuiScrollElement;
import mchorse.mclib.client.gui.framework.elements.IGuiElement;
import mchorse.mclib.client.gui.framework.elements.buttons.GuiButtonElement;
import mchorse.mclib.client.gui.framework.elements.buttons.GuiIconElement;
import mchorse.mclib.client.gui.framework.elements.utils.GuiContext;
import mchorse.mclib.client.gui.framework.elements.utils.GuiLabel;
import mchorse.mclib.client.gui.utils.Elements;
import mchorse.mclib.client.gui.utils.Icons;
import mchorse.mclib.client.gui.utils.keys.IKey;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;

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
    private String stringTitle = "aperture.gui.modifiers.title";
    private String stringGlobal = "aperture.gui.modifiers.global";

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

    public GuiLabel title;

    public GuiModifiersManager(Minecraft mc, GuiCameraEditor editor)
    {
        super(mc);

        this.editor = editor;
        this.add = new GuiIconElement(mc, Icons.ADD, (b) -> this.buttons.setVisible(!this.buttons.isVisible()));
        this.add.tooltip(IKey.lang("aperture.gui.modifiers.tooltips.add"));
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
        this.paste.tooltip(IKey.lang("aperture.gui.modifiers.tooltips.paste"));


        this.buttons = new GuiElement(mc)
        {
            @Override
            public void draw(GuiContext context)
            {
                this.area.draw(0x88000000);

                super.draw(context);
            }
        };
        this.buttons.setVisible(false);
        this.buttons.flex().relative(this).x(1, -10).y(24).w(0.5F, 0).anchor(1, 0).column(0).vertical().stretch().padding(2);

        for (byte i = 0; i < ModifierRegistry.getNextId(); i ++)
        {
            ModifierInfo info = ModifierRegistry.CLIENT.get(ModifierRegistry.CLASS_TO_ID.inverse().get(i));
            byte type = info.type;
            int color = info.color.getRGBAColor();

            GuiButtonElement button = new GuiButtonElement(mc, IKey.lang(info.title), (b) ->
            {
                this.addCameraModifier(type, this.getModifiers());
                this.buttons.setVisible(false);
            });

            this.buttons.add(button.color(color));
        }

        this.panels = new GuiScrollElement(mc);
        this.panels.flex().relative(this).y(28).w(1F, 0).h(1, -28).column(0).vertical().stretch().scroll();

        this.add.flex().relative(this).set(0, 4, 20, 20).x(1, -30);
        this.paste.flex().relative(this.add).set(-20, 0, 20, 20);

        this.title = Elements.label(IKey.lang(this.stringGlobal)).background(0x88000000);
        this.title.flex().relative(this).set(10, 10, 0, 20);

        this.add(this.title, this.add, this.paste, this.panels, this.buttons);

        this.hideTooltip();
   }

    public void updateDuration()
    {
        for (IGuiElement element : this.panels.getChildren())
        {
            if (element instanceof GuiAbstractModifierPanel)
            {
                ((GuiAbstractModifierPanel) element).updateDuration();
            }
        }
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
        this.panels.removeAll();
        this.fixture = fixture;
        this.title.label.set(fixture == null ? this.stringGlobal : this.stringTitle);

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

                panel.fillData();
                this.panels.add(panel);
                this.panels.resize();
                panel.initiate();
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
        int h = Math.min(this.panels.scroll.scrollSize + 28, this.area.h);

        if (this.panels.getChildren().isEmpty())
        {
            h = 28;
        }

        Gui.drawRect(this.area.x, this.area.y, this.area.ex(), this.area.y + h, 0xaa000000);

        if (this.buttons.isVisible())
        {
            this.add.area.draw(0x88000000);
        }

        super.draw(context);
    }
}