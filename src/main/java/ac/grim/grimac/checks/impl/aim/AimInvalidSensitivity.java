package ac.grim.grimac.checks.impl.aim;

import ac.grim.grimac.checks.Check;
import ac.grim.grimac.checks.CheckData;
import ac.grim.grimac.checks.type.RotationCheck;
import ac.grim.grimac.player.GrimPlayer;
import ac.grim.grimac.utils.anticheat.update.RotationUpdate;

@CheckData(name = "AimInvalidSensitivity")
public class AimInvalidSensitivity extends Check implements RotationCheck {
    public AimInvalidSensitivity(GrimPlayer playerData) {
        super(playerData);
    }


    @Override
    public void process(final RotationUpdate rotationUpdate) {

        int sensitivityX = (int) (player.getHorizontalSensitivity() * 200);
        int sensitivityY = (int) (player.getVerticalSensitivity() * 200);
        if (sensitivityX > 200 || sensitivityX < -1 || sensitivityY > 200 || sensitivityY < -1) {
            //need to check for < -1 cause cinematic camera
            flagAndAlert("sensitivityX=" + sensitivityX + "% sensitivityY=" + sensitivityY + "%");
        }
    }

    @Override
    public void reload() {
        super.reload();

    }
}
