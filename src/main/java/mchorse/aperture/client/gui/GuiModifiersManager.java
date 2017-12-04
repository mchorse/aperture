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
import mchorse.aperture.client.gui.utils.GuiUtils;
import mchorse.aperture.client.gui.widgets.buttons.GuiTextureButton;
import mchorse.aperture.utils.Color;
import mchorse.aperture.utils.ScrollArea;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.math.MathHelper;

public class GuiModifiersManager
{
    /**
     * Registry of camera modifier panels 
     */
    public static final Map<Class<? extends AbstractModifier>, Class<? extends GuiAbstractModifierPanel<? extends AbstractModifier>>> PANELS = new HashMap<Class<? extends AbstractModifier>, Class<? extends GuiAbstractModifierPanel<? extends AbstractModifier>>>();

    /**
     * Current list of panels 
     */
    public List<GuiAbstractModifierPanel<AbstractModifier>> panels = new ArrayList<GuiAbstractModifierPanel<AbstractModifier>>();

    /* Strings */
    private String stringTitle = I18n.format("aperture.gui.modifiers.title");
    private String stringSelect = I18n.format("aperture.gui.modifiers.select");

    /**
     * Fixture whose modifiers are getting managed 
     */
    public AbstractFixture fixture;

    /**
     * Whether this module visible 
     */
    public boolean visible;

    /**
     * Whether adding
     */
    public boolean adding;

    public boolean modified = false;

    /**
     * Modifier's panel are 
     */
    public ScrollArea area = new ScrollArea(0);

    /**
     * Add buttons (which add different {@link AbstractModifier}s to a 
     * fixture 
     */
    public List<GuiButton> addButtons = new ArrayList<GuiButton>();

    /**
     * Button to show add buttons 
     */
    public GuiButton add;

    /**
     * Reference to the parent screen (the camera editor) 
     */
    public GuiCameraEditor editor;

    public GuiModifiersManager(GuiCameraEditor editor)
    {
        this.editor = editor;
        this.add = new GuiTextureButton(0, 0, 0, GuiCameraEditor.EDITOR_TEXTURE).setTexPos(224, 0).setActiveTexPos(224, 16);

        for (ModifierInfo info : ModifierRegistry.CLIENT.values())
        {
            int color = 0xff000000 + info.color.getHex();
            Color dark = info.color.clone();

            dark.red *= 0.9;
            dark.green *= 0.9;
            dark.blue *= 0.9;

            this.addButtons.add(new GuiFlatButton(info.type, 0, 0, 0, 0, color, 0xff000000 + dark.getHex(), info.title));
        }
    }

    public boolean isInside(int x, int y)
    {
        return this.visible && this.area.isInside(x, y) && y - this.area.y < this.area.scrollSize;
    }

    /**
     * Update, I guess
     */
    public void update(int x, int y, int w, int h)
    {
        this.area.set(x, y, w, h);
        this.area.scrollSize = 20;
        this.recalcPanels();

        GuiUtils.setSize(this.add, x + w - 20 + 2, y + 2, 16, 16);

        int i = 0;

        for (GuiButton button : this.addButtons)
        {
            GuiUtils.setSize(button, x + w - 80, y + 20 + i * 20, 80, 20);

            i++;
        }

        this.area.clamp();
    }

    /**
     * Set fixture current fixture to manage  
     */
    public void setFixture(AbstractFixture fixture)
    {
        if (fixture == null)
        {
            this.panels.clear();
        }
        else if (fixture != this.fixture)
        {
            this.panels.clear();
            this.area.scrollSize = 20;

            for (AbstractModifier modifier : fixture.getModifiers())
            {
                this.addModifier(modifier);
            }
        }

        this.fixture = fixture;
    }

    /**
     * Add a camera modifier for current
     */
    public void addModifier(AbstractModifier modifier)
    {
        Class<? extends GuiAbstractModifierPanel<? extends AbstractModifier>> clazz = PANELS.get(modifier.getClass());

        if (clazz != null)
        {
            try
            {
                FontRenderer font = this.editor.mc.fontRendererObj;
                GuiAbstractModifierPanel<AbstractModifier> panel = (GuiAbstractModifierPanel<AbstractModifier>) clazz.getConstructor(modifier.getClass(), GuiModifiersManager.class, FontRenderer.class).newInstance(modifier, this, font);

                panel.update(this.area.x, this.area.y + this.area.scrollSize, this.area.w);
                this.panels.add(panel);

                this.area.scrollSize += panel.getHeight();
                this.editor.updateProfile();
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }

    public void moveModifier(GuiAbstractModifierPanel<? extends AbstractModifier> panel, int direction)
    {
        int index = this.panels.indexOf(panel);

        if (index == -1)
        {
            return;
        }

        int to = index + direction;

        if (to < 0 || to >= this.panels.size())
        {
            return;
        }

        List<AbstractModifier> modifiers = this.fixture.getModifiers();

        this.panels.add(to, this.panels.remove(index));
        modifiers.add(to, modifiers.remove(index));
        this.recalcPanels();
        this.editor.updateProfile();
        this.modified = true;
    }

    public void removeModifier(GuiAbstractModifierPanel<? extends AbstractModifier> panel)
    {
        this.panels.remove(panel);
        this.fixture.getModifiers().remove(panel.modifier);

        this.recalcPanels();
        this.area.clamp();
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

        for (GuiAbstractModifierPanel<AbstractModifier> panel : this.panels)
        {
            panel.update(this.area.x, this.area.y + 20 + h, this.area.w);

            h += panel.getHeight();
        }

        this.area.scrollSize = h + 20;
    }

    public void mouseClicked(int mouseX, int mouseY, int mouseButton)
    {
        if (!this.visible || this.fixture == null)
        {
            return;
        }

        Minecraft mc = Minecraft.getMinecraft();

        if (this.area.isInside(mouseX, mouseY) && mouseX >= this.area.x + this.area.w - 5 && this.area.scrollSize > this.area.h)
        {
            this.area.dragging = true;

            return;
        }

        if (this.adding)
        {
            List<AbstractModifier> modifiers = this.fixture.getModifiers();

            for (GuiButton button : this.addButtons)
            {
                if (button.mousePressed(mc, mouseX, mouseY))
                {
                    this.addCameraModifier(button.id, modifiers);

                    break;
                }
            }

            this.adding = false;

            return;
        }

        if (this.add.mousePressed(mc, mouseX, mouseY))
        {
            this.adding = !this.adding;
        }

        /* Create a copy of button arrays */
        List<GuiAbstractModifierPanel<? extends AbstractModifier>> panels = new ArrayList<GuiAbstractModifierPanel<? extends AbstractModifier>>(this.panels);

        for (GuiAbstractModifierPanel<? extends AbstractModifier> panel : panels)
        {
            if (this.modified)
            {
                break;
            }

            panel.mouseClicked(mouseX, mouseY + this.area.scroll, mouseButton);
        }

        this.modified = false;
    }

    /**
     * Mouse was released
     */
    public void mouseReleased(int mouseX, int mouseY, int state)
    {
        if (!this.visible)
        {
            return;
        }

        this.area.dragging = false;

        for (GuiAbstractModifierPanel<AbstractModifier> panel : this.panels)
        {
            panel.mouseReleased(mouseX, mouseY + this.area.scroll, state);
        }
    }

    public void mouseScroll(int x, int y, int scroll)
    {
        if (this.area.isInside(x, y))
        {
            this.area.scrollBy(scroll / 5);
        }
    }

    /**
     * Key press handling 
     */
    public void keyTyped(char typedChar, int keyCode)
    {
        if (!this.visible)
        {
            return;
        }

        for (GuiAbstractModifierPanel<AbstractModifier> panel : this.panels)
        {
            panel.keyTyped(typedChar, keyCode);
        }
    }

    /**
     * Whether this manager has any active text fields (used to determine 
     * whether it's okay to handle shortcuts). 
     */
    public boolean hasActiveTextfields()
    {
        if (this.visible)
        {
            for (GuiAbstractModifierPanel<AbstractModifier> panel : this.panels)
            {
                if (panel.hasActiveTextfields())
                {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Draw modifiers panel on the screen  
     */
    public void draw(int mouseX, int mouseY, float partialTicks)
    {
        if (!this.visible)
        {
            return;
        }

        if (this.area.dragging)
        {
            float factor = (mouseY - (this.area.y + 20)) / (float) (this.area.h - 20);

            this.area.scroll = (int) (factor * (this.area.scrollSize - this.area.h));
            this.area.clamp();
        }

        Minecraft mc = Minecraft.getMinecraft();
        int h = MathHelper.clamp(this.area.scrollSize, 20, this.area.h);

        if (this.fixture == null)
        {
            h = 45;
        }

        /* Background */
        Gui.drawRect(this.area.x, this.area.y, this.area.x + this.area.w, this.area.y + h, 0xaa000000);
        Gui.drawRect(this.area.x, this.area.y, this.area.x + this.area.w, this.area.y + 20, 0x88000000);
        mc.fontRendererObj.drawStringWithShadow(this.stringTitle, this.area.x + 6, this.area.y + 7, 0xffffff);

        this.add.drawButton(mc, mouseX, mouseY);

        if (this.fixture == null)
        {
            int x = this.area.x + this.area.w / 2;
            int y = this.area.y + 28;

            this.editor.drawCenteredString(mc.fontRendererObj, this.stringSelect, x, y, 0xcccccc);
        }
        else if (h > 0)
        {
            GuiUtils.scissor(this.area.x, this.area.y + 20, this.area.w, h - 20, mc.currentScreen.width, mc.currentScreen.height);
            GlStateManager.pushMatrix();
            GlStateManager.translate(0, -this.area.scroll, 0);

            for (GuiAbstractModifierPanel<AbstractModifier> panel : this.panels)
            {
                panel.draw(mouseX, mouseY + this.area.scroll, partialTicks);
            }

            GlStateManager.popMatrix();
            GL11.glDisable(GL11.GL_SCISSOR_TEST);

            if (this.adding)
            {
                for (GuiButton button : this.addButtons)
                {
                    button.drawButton(mc, mouseX, mouseY);
                }
            }

            if (this.area.scrollSize > this.area.h)
            {
                float factor = this.area.scroll / (float) (this.area.scrollSize - this.area.h);

                int bx = this.area.x + this.area.w - 5;
                int bh = this.area.getScrollBar(40);
                int by = this.area.y + 20 + (int) (factor * (this.area.h - bh - 20));

                Gui.drawRect(bx, by, bx + 5, by + bh, 0xffaaaaaa);
            }
        }
    }
}