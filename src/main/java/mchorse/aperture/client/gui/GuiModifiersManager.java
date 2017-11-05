package mchorse.aperture.client.gui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import mchorse.aperture.camera.fixtures.AbstractFixture;
import mchorse.aperture.camera.modifiers.AbstractModifier;
import mchorse.aperture.camera.modifiers.ShakeModifier;
import mchorse.aperture.client.gui.panels.modifiers.GuiAbstractModifierPanel;
import mchorse.aperture.client.gui.panels.modifiers.GuiShakeModifierPanel;
import mchorse.aperture.client.gui.utils.GuiUtils;
import mchorse.aperture.client.gui.widgets.buttons.GuiTextureButton;
import mchorse.aperture.utils.ScrollArea;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.util.math.MathHelper;

/**
 * 
 */
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

    /**
     * Fixture whose modifiers are getting managed 
     */
    public AbstractFixture fixture;

    /**
     * Whether this module visible 
     */
    public boolean visible;

    /**
     * Modifier's panel are 
     */
    public ScrollArea area = new ScrollArea(0);

    public GuiButton add;
    public GuiCameraEditor editor;

    static
    {
        /* Setup all Aperture vanilla camera modifiers */
        PANELS.put(ShakeModifier.class, GuiShakeModifierPanel.class);
    }

    public GuiModifiersManager(GuiCameraEditor editor)
    {
        this.editor = editor;

        this.add = new GuiTextureButton(0, 0, 0, GuiCameraEditor.EDITOR_TEXTURE).setTexPos(224, 0).setActiveTexPos(224, 16);
    }

    public void setFixture(AbstractFixture fixture)
    {
        if (fixture == null)
        {
            this.panels.clear();
        }
        else if (fixture != this.fixture)
        {
            this.panels.clear();

            for (AbstractModifier modifier : fixture.getModifiers())
            {
                this.addModifier(modifier);
            }
        }

        this.fixture = fixture;
    }

    public void addModifier(AbstractModifier modifier)
    {
        Class<? extends GuiAbstractModifierPanel<? extends AbstractModifier>> clazz = PANELS.get(modifier.getClass());

        if (clazz != null)
        {
            try
            {
                FontRenderer font = this.editor.mc.fontRendererObj;
                GuiAbstractModifierPanel<AbstractModifier> panel = (GuiAbstractModifierPanel<AbstractModifier>) clazz.getConstructor(modifier.getClass(), GuiModifiersManager.class, FontRenderer.class).newInstance(modifier, this, font);

                int h = 0;

                for (GuiAbstractModifierPanel<AbstractModifier> otherPanel : this.panels)
                {
                    h += otherPanel.getHeight();
                }

                panel.update(this.area.x + this.area.w, this.area.y + 20 + h);
                this.panels.add(panel);

                this.area.scrollSize = h + panel.getHeight() + 20;
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }

    public void update(int x, int y, int w, int h)
    {
        this.area.set(x, y, w, h);
        this.recalcPanels();

        GuiUtils.setSize(this.add, x + w - 20 + 2, y + 2, 16, 16);
    }

    public void mouseClicked(int mouseX, int mouseY, int mouseButton)
    {
        if (!this.visible || this.fixture == null)
        {
            return;
        }

        Minecraft mc = Minecraft.getMinecraft();
        List<AbstractModifier> modifiers = this.fixture.getModifiers();

        if (this.add.mousePressed(mc, mouseX, mouseY))
        {
            AbstractModifier modifier = new ShakeModifier(10, 0.5F);

            modifiers.add(modifier);
            this.addModifier(modifier);
        }

        Iterator<GuiAbstractModifierPanel<AbstractModifier>> it = this.panels.iterator();

        while (it.hasNext())
        {
            GuiAbstractModifierPanel<AbstractModifier> panel = it.next();

            if (panel.remove.mousePressed(this.editor.mc, mouseX, mouseY))
            {
                it.remove();
                this.fixture.getModifiers().remove(panel.modifier);
                this.recalcPanels();

                continue;
            }

            panel.mouseClicked(mouseX, mouseY, mouseButton);
        }
    }

    private void recalcPanels()
    {
        int h = 0;

        for (GuiAbstractModifierPanel<AbstractModifier> panel : this.panels)
        {
            h += panel.getHeight();

            panel.update(this.area.x + this.area.w, this.area.y + 20 + h);
        }

        this.area.scrollSize = h + 20;
    }

    public void mouseReleased(int mouseX, int mouseY, int state)
    {
        if (!this.visible)
        {
            return;
        }

        for (GuiAbstractModifierPanel<AbstractModifier> panel : this.panels)
        {
            panel.mouseReleased(mouseX, mouseY, state);
        }
    }

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

    public boolean hasActiveTextfields()
    {
        if (this.visible)
        {

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

        Minecraft mc = Minecraft.getMinecraft();
        int h = MathHelper.clamp_int(this.area.scrollSize, 20, this.area.h);

        if (this.fixture == null)
        {
            h = 45;
        }

        Gui.drawRect(this.area.x, this.area.y, this.area.x + this.area.w, this.area.y + h, 0xaa000000);
        Gui.drawRect(this.area.x, this.area.y, this.area.x + this.area.w, this.area.y + 20, 0x88000000);
        mc.fontRendererObj.drawStringWithShadow("Modifiers", this.area.x + 6, this.area.y + 7, 0xffffff);

        this.add.drawButton(mc, mouseX, mouseY);

        if (this.fixture == null)
        {
            this.editor.drawCenteredString(mc.fontRendererObj, "Select camera fixture...", this.area.x + this.area.w / 2, this.area.y + 28, 0xcccccc);
        }
        else
        {
            for (GuiAbstractModifierPanel<AbstractModifier> panel : this.panels)
            {
                panel.draw(mouseX, mouseY, partialTicks);
            }
        }
    }
}