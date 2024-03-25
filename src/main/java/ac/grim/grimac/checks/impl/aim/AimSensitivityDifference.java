package ac.grim.grimac.checks.impl.aim;

import ac.grim.grimac.checks.Check;
import ac.grim.grimac.checks.CheckData;
import ac.grim.grimac.checks.type.RotationCheck;
import ac.grim.grimac.player.GrimPlayer;
import ac.grim.grimac.utils.anticheat.update.RotationUpdate;

@CheckData(name = "AimSensitivityDifference")
public class AimSensitivityDifference extends Check implements RotationCheck {
    public AimSensitivityDifference(GrimPlayer playerData) {
        super(playerData);
    }
    private int maxSensitivityDifference;
    private int minSensitivity;
    private int lastSensitivityX = -50; //-50 is not a valid sensitivity just to make sure we will get every sensitivity change
    private int lastSensitivityY = -50;
    private boolean sensitivityXChange = false;
    private boolean sensitivityYChange = false;
    @Override
    public void process(final RotationUpdate rotationUpdate) {

        int sensitivityX = (int) (player.getHorizontalSensitivity() * 200);
        int sensitivityY = (int) (player.getVerticalSensitivity() * 200);
        if(sensitivityX != lastSensitivityX) {
            sensitivityXChange = true;
        }
        if(sensitivityY != lastSensitivityY) {
            sensitivityYChange = true;
        }


        if ((Math.abs(sensitivityX - sensitivityY) > maxSensitivityDifference) && (sensitivityXChange == sensitivityYChange) && (sensitivityX > minSensitivity) && (sensitivityY > minSensitivity)) {
            flagAndAlert("sensitivityX=" + sensitivityX + "% sensitivityY=" + sensitivityY + "%");
        }

        lastSensitivityX = sensitivityX;
        lastSensitivityY = sensitivityY;
        if(sensitivityXChange && sensitivityYChange) {
            sensitivityXChange = sensitivityYChange = false;
        }
    }

    @Override
    public void reload() {
        super.reload();
        maxSensitivityDifference = getConfig().getIntElse(getConfigName() + ".maxDifference", 5);
        minSensitivity = getConfig().getIntElse(getConfigName() + ".minSensitivity", 1);

    }
}
