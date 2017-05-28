package mchorse.aperture.client.gui.panels;

import mchorse.aperture.camera.fixtures.AbstractFixture;
import mchorse.aperture.client.gui.GuiCameraEditor;
import mchorse.aperture.client.gui.GuiTrackpad;
import mchorse.aperture.client.gui.GuiTrackpad.ITrackpadListener;
import mchorse.aperture.client.gui.utils.GuiUtils;
import mchorse.aperture.utils.Rect;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiPageButtonList.GuiResponder;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;

/**
 * Base class for abstract fixture panel
 *
 * This panel adds inputs for only two properties (which has every camera
 * fixture): name and duration.
 */
public abstract class GuiAbstractFixturePanel<T extends AbstractFixture> implements IFixturePanel<T>, ITrackpadListener, GuiResponder
{
    /**
     * Currently editing camera fixture
     */
    public T fixture;

    /* Stuff */
    public Rect area = new Rect();
    public FontRenderer font;
    public GuiCameraEditor editor;

    /* GUI fields */
    public GuiTextField name;
    public GuiTrackpad duration;

    /* Dynamic height which can be modified by subclasses */
    public int height = 140;

    public GuiAbstractFixturePanel(FontRenderer font)
    {
        this.name = new GuiTextField(0, font, 0, 0, 0, 0);
        this.name.setGuiResponder(this);

        this.duration = new GuiTrackpad(this, font);
        this.duration.title = "Duration";
        this.duration.min = 1;

        this.font = font;
    }

    /**
     * Why I gotta implement this junk?
     */
    @Override
    public void setEntryValue(int id, boolean value)
    {}

    /**
     * Why I gotta implement this junk?
     */
    @Override
    public void setEntryValue(int id, float value)
    {}

    /**
     * This method is responsible for setting a value of camera fixture
     */
    @Override
    public void setEntryValue(int id, String value)
    {
        if (id == 0)
        {
            this.fixture.setName(value);
        }

        this.editor.updateValues();
    }

    @Override
    public void setTrackpadValue(GuiTrackpad trackpad, float value)
    {
        if (trackpad == this.duration)
        {
            this.fixture.setDuration((long) value);
            this.editor.updateValues();
        }

        this.editor.updatePlayerCurrently(0.0F);
    }

    @Override
    public void select(T fixture)
    {
        this.fixture = fixture;

        this.name.setText(fixture.getName());
        this.name.setCursorPositionZero();

        this.duration.setValue(fixture.getDuration());
        this.duration.amplitude = 1.0F;
    }

    @Override
    public void update(GuiScreen screen)
    {
        this.area.set(10, 62, screen.width - 20, this.height);

        int x = this.area.x;
        int y = this.area.y;

        this.name.xPosition = x + 1;
        this.name.yPosition = y + 1;
        this.name.width = 98;
        this.name.height = 18;

        this.duration.update(x, y + 25, 100, 20);

        this.editor = (GuiCameraEditor) screen;
    }

    @Override
    public void keyTyped(char typedChar, int keyCode)
    {
        this.name.textboxKeyTyped(typedChar, keyCode);
        this.duration.keyTyped(typedChar, keyCode);
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton)
    {
        this.name.mouseClicked(mouseX, mouseY, mouseButton);
        this.duration.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int state)
    {
        this.duration.mouseReleased(mouseX, mouseY, state);
    }

    @Override
    public void draw(int mouseX, int mouseY, float partialTicks)
    {
        this.name.drawTextBox();
        this.duration.draw(mouseX, mouseY, partialTicks);

        if (!this.name.isFocused())
        {
            GuiUtils.drawRightString(this.font, "Name", this.name.xPosition + this.name.width - 4, this.name.yPosition + 5, 0xffaaaaaa);
        }
    }
}