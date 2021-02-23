package mchorse.aperture.client.gui.utils.undo;

import mchorse.aperture.camera.CameraProfile;
import mchorse.aperture.client.gui.GuiCameraEditor;
import mchorse.aperture.utils.undo.IUndo;

public class FixtureValueChangeUndo implements IUndo<CameraProfile>
{
    public int index;
    public String name;
    public Object oldValue;
    public Object newValue;

    private boolean mergable = true;

    public static FixtureValueChangeUndo create(GuiCameraEditor editor, String name, Object newValue)
    {
        return create(editor, name, editor.getFixture().getProperty(name).getValue(), newValue);
    }

    public static FixtureValueChangeUndo create(GuiCameraEditor editor, String name, Object oldValue, Object newValue)
    {
        int index = editor.getProfile().getAll().indexOf(editor.getFixture());

        return new FixtureValueChangeUndo(index, name, oldValue, newValue);
    }

    public FixtureValueChangeUndo(int index, String name, Object oldValue, Object newValue)
    {
        this.index = index;
        this.name = name;
        this.oldValue = oldValue;
        this.newValue = newValue;
    }

    public FixtureValueChangeUndo unmergable()
    {
        this.mergable = false;

        return this;
    }

    @Override
    public boolean isMergeable(IUndo<CameraProfile> undo)
    {
        return this.mergable && undo instanceof FixtureValueChangeUndo && ((FixtureValueChangeUndo) undo).name.equals(this.name);
    }

    @Override
    public void merge(IUndo<CameraProfile> undo)
    {
        if (undo instanceof FixtureValueChangeUndo)
        {
            FixtureValueChangeUndo prop = (FixtureValueChangeUndo) undo;

            this.newValue = prop.newValue;
        }
    }

    @Override
    public void undo(CameraProfile context)
    {
        context.get(this.index).getProperty(this.name).setValue(this.oldValue);
    }

    @Override
    public void redo(CameraProfile context)
    {
        context.get(this.index).getProperty(this.name).setValue(this.newValue);
    }
}
