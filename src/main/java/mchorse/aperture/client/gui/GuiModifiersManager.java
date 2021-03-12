package mchorse.aperture.client.gui;

import mchorse.aperture.camera.CameraProfile;
import mchorse.aperture.camera.ModifierRegistry;
import mchorse.aperture.camera.ModifierRegistry.ModifierInfo;
import mchorse.aperture.camera.fixtures.AbstractFixture;
import mchorse.aperture.camera.modifiers.AbstractModifier;
import mchorse.aperture.camera.values.ValueModifiers;
import mchorse.aperture.client.gui.panels.modifiers.GuiAbstractModifierPanel;
import mchorse.aperture.client.gui.utils.undo.ModifierValueChangeUndo;
import mchorse.aperture.utils.undo.CompoundUndo;
import mchorse.aperture.utils.undo.IUndo;
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
import mchorse.mclib.config.values.Value;
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
                this.addModifier(this.clipboard.copy());
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
                AbstractModifier modifier = this.addCameraModifier(type);

                if (modifier != null)
                {
                    this.addModifier(modifier);
                }

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

    /* Undo/redo */

    public IUndo<CameraProfile> undo(List<AbstractModifier> modifiers)
    {
        CameraProfile profile = this.editor.getProfile();
        int cursor = this.editor.timeline.value;

        if (this.fixture != null)
        {
            int index = profile.fixtures.indexOf(this.fixture);

            return new ModifierValueChangeUndo(index, this.panels.scroll.scroll, this.fixture.modifiers.getPath(), this.fixture.modifiers.getValue(), modifiers).cursor(cursor).noMerging();
        }

        return new ModifierValueChangeUndo(-1, this.panels.scroll.scroll, profile.modifiers.getPath(), profile.modifiers.getValue(), modifiers).cursor(cursor).noMerging();
    }

    public IUndo<CameraProfile> undo(Value value, Object newValue)
    {
        CameraProfile profile = this.editor.getProfile();
        int cursor = this.editor.timeline.value;
        int index = -1;

        if (this.fixture != null)
        {
            index = profile.fixtures.indexOf(this.fixture);
        }

        return new ModifierValueChangeUndo(index, this.panels.scroll.scroll, value.getPath(), value.getValue(), newValue).cursor(cursor);
    }

    public void handleUndo(IUndo<CameraProfile> undo, boolean redo)
    {
        int scroll = -1;

        if (undo instanceof ModifierValueChangeUndo)
        {
            scroll = ((ModifierValueChangeUndo) undo).getPanelScroll();
        }
        else if (undo instanceof CompoundUndo && ((CompoundUndo) undo).has(ModifierValueChangeUndo.class))
        {
            scroll = ((ModifierValueChangeUndo) ((CompoundUndo<CameraProfile>) undo).getFirst(ModifierValueChangeUndo.class)).getPanelScroll();
        }

        if (scroll >= 0)
        {
            this.panels.scroll.scrollTo(scroll);
        }

        for (IGuiElement element : this.panels.getChildren())
        {
            if (element instanceof GuiAbstractModifierPanel)
            {
                ((GuiAbstractModifierPanel) element).handleUndo(undo, redo);
            }
        }
    }

    public void cameraEditorOpened()
    {
        for (IGuiElement element : this.panels.getChildren())
        {
            if (element instanceof GuiAbstractModifierPanel)
            {
                ((GuiAbstractModifierPanel) element).initiate();
            }
        }
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

    public ValueModifiers getModifiers()
    {
        if (this.fixture != null)
        {
            return this.fixture.modifiers;
        }
        else if (this.editor.getProfile() != null)
        {
            return this.editor.getProfile().modifiers;
        }

        return null;
    }

    /**
     * Set fixture current fixture to manage
     */
    public void setFixture(AbstractFixture fixture)
    {
        this.panels.removeAll();
        this.fixture = fixture;
        this.title.label.set(fixture == null ? this.stringGlobal : this.stringTitle);

        ValueModifiers modifiers = this.getModifiers();

        if (modifiers == null)
        {
            return;
        }

        for (int i = 0; i < modifiers.size(); i ++)
        {
            AbstractModifier modifier = modifiers.get(i);

            this.addModifierPanel(modifier);
        }
    }

    public void setClipboard(AbstractModifier modifier)
    {
        this.clipboard = modifier.copy();
    }

    /**
     * Add a camera modifier for current
     */
    @SuppressWarnings("unchecked")
    public void addModifierPanel(AbstractModifier modifier)
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

    public void addModifier(AbstractModifier modifier)
    {
        ValueModifiers value = this.getModifiers();
        List<AbstractModifier> modifiers = (List<AbstractModifier>) value.getValue();

        modifiers.add(modifier);

        this.editor.postUndo(this.undo(modifiers), false, false);

        value.add(modifier);
        this.addModifierPanel(modifier);
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

        ValueModifiers value = this.getModifiers();
        List<AbstractModifier> modifiers = (List<AbstractModifier>) value.getValue();

        modifiers.add(to, modifiers.remove(index));

        this.editor.postUndo(this.undo(modifiers), false, false);
        this.panels.getChildren().add(to, this.panels.getChildren().remove(index));
        value.add(to, value.remove(index));
        this.resize();

        this.modified = true;
    }

    public void removeModifier(GuiAbstractModifierPanel<? extends AbstractModifier> panel)
    {
        ValueModifiers value = this.getModifiers();
        List<AbstractModifier> modifiers = (List<AbstractModifier>) this.getModifiers().getValue();
        int index = value.indexOf(panel.modifier);

        modifiers.remove(index);

        this.editor.postUndo(this.undo(modifiers), false, false);
        this.panels.getChildren().remove(panel);
        value.remove(index);
        this.resize();

        this.modified = true;
    }

    private AbstractModifier addCameraModifier(int id)
    {
        try
        {
            return ModifierRegistry.fromType((byte) id);
        }
        catch (Exception e)
        {}

        return null;
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