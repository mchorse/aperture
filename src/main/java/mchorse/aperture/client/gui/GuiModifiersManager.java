package mchorse.aperture.client.gui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.lwjgl.opengl.GL11;

import mchorse.aperture.camera.ModifierRegistry;
import mchorse.aperture.camera.ModifierRegistry.ModifierInfo;
import mchorse.aperture.camera.fixtures.AbstractFixture;
import mchorse.aperture.camera.modifiers.AbstractModifier;
import mchorse.aperture.client.gui.GuiFixturesPopup.GuiFlatButton;
import mchorse.aperture.client.gui.panels.modifiers.GuiAbstractModifierPanel;
import mchorse.aperture.utils.Color;
import mchorse.mclib.client.gui.framework.GuiTooltip;
import mchorse.mclib.client.gui.framework.elements.GuiButtonElement;
import mchorse.mclib.client.gui.framework.elements.GuiElement;
import mchorse.mclib.client.gui.framework.elements.GuiElements;
import mchorse.mclib.client.gui.utils.GuiUtils;
import mchorse.mclib.client.gui.utils.ScrollArea;
import mchorse.mclib.client.gui.widgets.buttons.GuiTextureButton;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.math.MathHelper;

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
     * Modifier's panel are 
     */
    public ScrollArea scroll = new ScrollArea(0);

    /**
     * Add buttons (which add different {@link AbstractModifier}s to a 
     * fixture 
     */
    @SuppressWarnings("rawtypes")
    public GuiElements<GuiButtonElement> addButtons = new GuiElements<>();

    /**
     * Modifier panels 
     */
    public GuiElements<GuiAbstractModifierPanel<AbstractModifier>> panels = new GuiElements<>();

    /**
     * Button to show add buttons 
     */
    public GuiButtonElement<GuiTextureButton> add;

    /**
     * Button to paste a modifier in the clipboard
     */
    public GuiButtonElement<GuiTextureButton> paste;

    /**
     * Reference to the parent screen (the camera editor) 
     */
    public GuiCameraEditor editor;

    public GuiModifiersManager(Minecraft mc, GuiCameraEditor editor)
    {
        super(mc);

        this.editor = editor;
        this.createChildren();

        this.add = GuiButtonElement.icon(mc, GuiCameraEditor.EDITOR_TEXTURE, 224, 0, 224, 16, (b) ->
        {
            this.addButtons.setVisible(!this.addButtons.isVisible());
        });

        this.paste = GuiButtonElement.icon(mc, GuiCameraEditor.EDITOR_TEXTURE, 144, 64, 144, 80, (b) ->
        {
            if (this.clipboard != null)
            {
                AbstractModifier modifier = this.clipboard.copy();

                this.getModifiers().add(modifier);
                this.setFixture(this.fixture);
                this.editor.updateProfile();
            }
        });

        int i = 0;

        for (ModifierInfo info : ModifierRegistry.CLIENT.values())
        {
            int color = 0xff000000 + info.color.getHex();
            Color dark = info.color.clone();

            dark.red *= 0.9;
            dark.green *= 0.9;
            dark.blue *= 0.9;

            GuiButtonElement<GuiFlatButton> button = new GuiButtonElement<GuiFlatButton>(mc, new GuiFlatButton(info.type, 0, 0, 0, 0, color, 0xff000000 + dark.getHex(), I18n.format(info.title)), (b) ->
            {
                this.addCameraModifier(b.button.id, this.getModifiers());
                this.addButtons.setVisible(false);
            });

            button.resizer().parent(this.area).set(0, 20 + i * 20, 80, 20).x(1, -80);
            this.addButtons.add(button);

            i++;
        }

        this.addButtons.setVisible(false);

        this.add.resizer().parent(this.area).set(0, 2, 16, 16).x(1, -18);
        this.paste.resizer().relative(this.add.resizer()).set(-20, 0, 16, 16);
        this.children.add(this.add, this.paste, this.addButtons);
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
        this.panels.elements.clear();
        this.scroll.scrollSize = 20;
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

                panel.resizer().parent(this.scroll).set(0, this.scroll.scrollSize, 0, panel.getHeight()).w(1, 0);
                panel.resize(this.editor.width, this.editor.height);
                this.panels.add(panel);

                this.scroll.scrollSize += panel.getHeight();
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }

    public void moveModifier(GuiAbstractModifierPanel<? extends AbstractModifier> panel, int direction)
    {
        int index = this.panels.elements.indexOf(panel);

        if (index == -1)
        {
            return;
        }

        int to = index + direction;

        if (to < 0 || to >= this.panels.elements.size())
        {
            return;
        }

        List<AbstractModifier> modifiers = this.getModifiers();

        this.panels.elements.add(to, this.panels.elements.remove(index));
        modifiers.add(to, modifiers.remove(index));
        this.recalcPanels();
        this.editor.updateProfile();
        this.modified = true;
    }

    public void removeModifier(GuiAbstractModifierPanel<? extends AbstractModifier> panel)
    {
        this.panels.elements.remove(panel);
        this.getModifiers().remove(panel.modifier);

        this.recalcPanels();
        this.scroll.clamp();
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

    private void recalcPanels()
    {
        int h = 0;

        for (GuiAbstractModifierPanel<AbstractModifier> panel : this.panels.elements)
        {
            panel.resizer().parent(this.scroll).set(0, h + 20, 0, panel.getHeight()).w(1, 0);
            panel.resize(this.editor.width, this.editor.height);

            h += panel.getHeight();
        }

        this.scroll.scrollSize = h + 20;
    }

    @Override
    public void resize(int width, int height)
    {
        super.resize(width, height);

        this.scroll.copy(this.area);
        this.recalcPanels();
    }

    @Override
    public boolean mouseClicked(int mouseX, int mouseY, int mouseButton)
    {
        if (super.mouseClicked(mouseX, mouseY, mouseButton) || this.scroll.mouseClicked(mouseX, mouseY))
        {
            return true;
        }

        /* Create a copy of button arrays */
        List<GuiAbstractModifierPanel<? extends AbstractModifier>> panels = new ArrayList<GuiAbstractModifierPanel<? extends AbstractModifier>>(this.panels.elements);

        for (GuiAbstractModifierPanel<? extends AbstractModifier> panel : panels)
        {
            if (this.modified)
            {
                break;
            }

            panel.mouseClicked(mouseX, mouseY + this.scroll.scroll, mouseButton);
        }

        this.modified = false;

        int h = MathHelper.clamp_int(this.scroll.scrollSize, 20, this.scroll.h);

        if (this.visible && this.area.isInside(mouseX, mouseY) && mouseY - this.area.y <= h)
        {
            return true;
        }

        return false;
    }

    @Override
    public boolean mouseScrolled(int mouseX, int mouseY, int scroll)
    {
        int h = MathHelper.clamp_int(this.scroll.scrollSize, 20, this.scroll.h);

        return super.mouseScrolled(mouseX, mouseY, scroll) || this.panels.mouseScrolled(mouseX, mouseY + this.scroll.scroll, scroll) || (this.scroll.mouseScroll(mouseX, mouseY, scroll) && mouseY - this.area.y <= h);
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int state)
    {
        super.mouseReleased(mouseX, mouseY, state);
        this.scroll.mouseReleased(mouseX, mouseY);
        this.panels.mouseReleased(mouseX, mouseY + this.scroll.scroll, state);
    }

    @Override
    public void keyTyped(char typedChar, int keyCode)
    {
        super.keyTyped(typedChar, keyCode);

        this.panels.keyTyped(typedChar, keyCode);
    }

    @Override
    public boolean hasActiveTextfields()
    {
        return super.hasActiveTextfields() || this.panels.hasActiveTextfields();
    }

    @Override
    public void draw(GuiTooltip tooltip, int mouseX, int mouseY, float partialTicks)
    {
        this.scroll.drag(mouseX, mouseY);

        int h = MathHelper.clamp_int(this.scroll.scrollSize, 20, this.scroll.h);

        /* Background */
        Gui.drawRect(this.scroll.x, this.scroll.y, this.scroll.x + this.scroll.w, this.scroll.y + h, 0xaa000000);
        Gui.drawRect(this.scroll.x, this.scroll.y, this.scroll.x + this.scroll.w, this.scroll.y + 20, 0x88000000);
        this.font.drawStringWithShadow(this.fixture == null ? this.stringGlobal : this.stringTitle, this.scroll.x + 6, this.scroll.y + 7, 0xffffff);

        if (h > 0)
        {
            GuiUtils.scissor(this.scroll.x, this.scroll.y + 20, this.scroll.w, h - 20, this.editor.width, this.editor.height);
            GlStateManager.pushMatrix();
            GlStateManager.translate(0, -this.scroll.scroll, 0);

            this.panels.draw(tooltip, mouseX, mouseY + this.scroll.scroll, partialTicks);

            GlStateManager.popMatrix();
            GL11.glDisable(GL11.GL_SCISSOR_TEST);

            if (this.scroll.scrollSize > this.scroll.h)
            {
                float factor = this.scroll.scroll / (float) (this.scroll.scrollSize - this.scroll.h);

                int bx = this.scroll.x + this.scroll.w - 5;
                int bh = this.scroll.getScrollBar(40);
                int by = this.scroll.y + 20 + (int) (factor * (this.scroll.h - bh - 20));

                Gui.drawRect(bx, by, bx + 5, by + bh, 0xffaaaaaa);
            }
        }

        super.draw(tooltip, mouseX, mouseY, partialTicks);
    }
}