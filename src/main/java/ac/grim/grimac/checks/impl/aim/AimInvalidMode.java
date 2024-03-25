package ac.grim.grimac.checks.impl.aim;

import ac.grim.grimac.checks.Check;
import ac.grim.grimac.checks.CheckData;
import ac.grim.grimac.checks.type.RotationCheck;
import ac.grim.grimac.player.GrimPlayer;
import ac.grim.grimac.utils.anticheat.update.RotationUpdate;
import ac.grim.grimac.utils.math.GrimMath;

@CheckData(name = "AimInvalidMode")
public class AimInvalidMode extends Check implements RotationCheck {
    public AimInvalidMode(GrimPlayer playerData) {
        super(playerData);
    }

    double lastModeX = -5;
    double lastModeY = -5;
    int xRotsSinceModeChange = 0, yRotsSinceModeChange = 0;
    int maxRots;


    @Override
    public void process(final RotationUpdate rotationUpdate) {

        double modeX = rotationUpdate.getProcessor().modeX;
        double modeY = rotationUpdate.getProcessor().modeY;
        double sensitivityX = ((int) (player.getHorizontalSensitivity() * 200));
        double sensitivityY = ((int) (player.getVerticalSensitivity() * 200));
        if(sensitivityX < 5 || sensitivityY < 5  || sensitivityX > 195 || sensitivityY > 195) {
            //rotationUpdate.isCinematic() sadly doesn't do anything (it isn't even set). if you don't believe me check with Ctrl + Shift + F
            xRotsSinceModeChange = yRotsSinceModeChange = 0;
            return;
        }

        if (((modeX == lastModeX) != (modeY == lastModeY)) && (lastModeX != -5 && lastModeY != -5 && lastModeX < 1 && lastModeY < 1)) {
            if (rotationUpdate.getDeltaXRotABS() > 0 && rotationUpdate.getDeltaXRotABS() < 5 && rotationUpdate.getProcessor().divisorX > GrimMath.MINIMUM_DIVISOR) {
                xRotsSinceModeChange++;
            }
            if (rotationUpdate.getDeltaYRotABS() > 0 && rotationUpdate.getDeltaYRotABS() < 5 && rotationUpdate.getProcessor().divisorY > GrimMath.MINIMUM_DIVISOR) {
                yRotsSinceModeChange++;
            }

            //Need some buffer bc the player can change sensitivity
            if (xRotsSinceModeChange >= maxRots && yRotsSinceModeChange >= maxRots) {
                flagAndAlert("modeX=" + modeX + " lmodeX=" + lastModeX + " modeY=" + modeY + " lmodeY=" + lastModeY);
            }

            return; //return here so the check keeps flagging till both modes change
        } else {
            xRotsSinceModeChange = yRotsSinceModeChange = 0;
        }
        lastModeX = modeX;
        lastModeY = modeY;


    }

    @Override
    public void reload() {
        super.reload();
        maxRots = getConfig().getIntElse(getConfigName() + ".maxRots", 80);

    }
}
