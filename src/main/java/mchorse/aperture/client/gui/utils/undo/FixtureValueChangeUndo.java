package mchorse.aperture.client.gui.utils.undo;

import mchorse.aperture.camera.CameraProfile;
import mchorse.aperture.camera.fixtures.AbstractFixture;
import mchorse.aperture.client.gui.GuiCameraEditor;
import mchorse.aperture.utils.undo.IUndo;

public class FixtureValueChangeUndo implements IUndo<CameraProfile>
{
    public int index;
    public String name;
    public Object oldValue;
    public Object newValue;

    public static FixtureValueChangeUndo create(GuiCameraEditor editor, String name, Object newValue)
    {
        return new FixtureValueChangeUndo(editor.getProfile(), editor.getFixture(), name, newValue);
    }

    public FixtureValueChangeUndo(CameraProfile profile, AbstractFixture fixture, String name, Object newValue)
    {
        this.index = profile.getAll().indexOf(fixture);
        this.name = name;

        this.oldValue = fixture.getProperty(name).getValue();
        this.newValue = newValue;
    }

    @Override
    public boolean isMergeable(IUndo<CameraProfile> undo)
    {
        return undo instanceof FixtureValueChangeUndo && ((FixtureValueChangeUndo) undo).name.equals(this.name);
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
