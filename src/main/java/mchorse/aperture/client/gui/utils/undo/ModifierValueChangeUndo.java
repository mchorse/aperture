package mchorse.aperture.client.gui.utils.undo;

import mchorse.aperture.client.gui.GuiCameraEditor;
import mchorse.mclib.config.values.IConfigValue;

public class ModifierValueChangeUndo extends FixtureValueChangeUndo
{
    private int panelScroll;

    public static ModifierValueChangeUndo create(GuiCameraEditor editor, int panelScroll, IConfigValue value, Object newValue)
    {
        return create(editor, panelScroll, value, value.getValue(), newValue);
    }

    public static ModifierValueChangeUndo create(GuiCameraEditor editor, int panelScroll, IConfigValue value, Object oldValue, Object newValue)
    {
        int index = editor.getProfile().getAll().indexOf(editor.getFixture());

        return new ModifierValueChangeUndo(index, panelScroll, value, oldValue, newValue);
    }

    public ModifierValueChangeUndo(int index, int panelScroll, IConfigValue value, Object oldValue, Object newValue)
    {
        super(index, value, oldValue, newValue);

        this.panelScroll = panelScroll;
    }

    public ModifierValueChangeUndo(int index, int panelScroll, String name, Object oldValue, Object newValue)
    {
        super(index, name, oldValue, newValue);

        this.panelScroll = panelScroll;
    }

    public int getPanelScroll()
    {
        return this.panelScroll;
    }
}
