package mchorse.aperture.client.gui.utils.undo;

public class ModifierValueChangeUndo extends FixtureValueChangeUndo
{
    private int panelScroll;

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
