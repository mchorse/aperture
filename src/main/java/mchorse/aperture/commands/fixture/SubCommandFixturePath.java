package mchorse.aperture.commands.fixture;

import mchorse.aperture.commands.SubCommandBase;
import mchorse.aperture.commands.path.SubCommandPathAdd;
import mchorse.aperture.commands.path.SubCommandPathEdit;
import mchorse.aperture.commands.path.SubCommandPathGoto;
import mchorse.aperture.commands.path.SubCommandPathMove;
import mchorse.aperture.commands.path.SubCommandPathRemove;

/**
 * Camera's sub-command /camera path
 *
 * This sub-command is responsible for manipulating path fixtures. This
 * sub-command provides sub-commands for adding, editing, removing, moving and
 * going to points in the path fixture.
 */
public class SubCommandFixturePath extends SubCommandBase
{
    public SubCommandFixturePath()
    {
        this.add(new SubCommandPathAdd());
        this.add(new SubCommandPathEdit());
        this.add(new SubCommandPathGoto());
        this.add(new SubCommandPathMove());
        this.add(new SubCommandPathRemove());
    }

    @Override
    public String getCommandName()
    {
        return "path";
    }

    @Override
    protected String getHelp()
    {
        return "aperture.commands.camera.path.help";
    }
}