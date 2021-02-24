package mchorse.aperture.client.gui.utils.undo;

import mchorse.aperture.client.gui.GuiCameraEditor;

public class FixturePointsChangeUndo extends FixtureValueChangeUndo
{
    private int lastPoint;
    private int point;

    public static FixturePointsChangeUndo create(GuiCameraEditor editor, String name, int lastPoint, int point, Object newValue)
    {
        return create(editor, name, point, lastPoint, editor.getFixture().getProperty(name).getValue(), newValue);
    }

    public static FixturePointsChangeUndo create(GuiCameraEditor editor, String name, int lastPoint, int point, Object oldValue, Object newValue)
    {
        int index = editor.getProfile().getAll().indexOf(editor.getFixture());

        return new FixturePointsChangeUndo(index, name, point, lastPoint, oldValue, newValue);
    }

    public FixturePointsChangeUndo(int index, String name, int lastPoint, int point, Object oldValue, Object newValue)
    {
        super(index, name, oldValue, newValue);

        this.lastPoint = lastPoint;
        this.point = point;
    }

    public int getLastPoint()
    {
        return this.lastPoint;
    }

    public int getPoint()
    {
        return this.point;
    }
}
