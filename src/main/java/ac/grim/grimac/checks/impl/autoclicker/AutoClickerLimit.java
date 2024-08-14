package ac.grim.grimac.checks.impl.autoclicker;

import ac.grim.grimac.checks.Check;
import ac.grim.grimac.checks.CheckData;
import ac.grim.grimac.checks.type.PacketCheck;
import ac.grim.grimac.player.GrimPlayer;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.player.DiggingAction;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerDigging;


@CheckData(name = "AutoClickerLimit")
public class AutoClickerLimit extends Check implements PacketCheck {

    public AutoClickerLimit(GrimPlayer playerData) {
        super(playerData);
    }

    private long lastTime = 0;
    private long lastSec = 0;
    private int maxCPS;
    private int currentCPS = 0;
    private long minDelta;
    private boolean isDigging = false;

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (event.getPacketType() == PacketType.Play.Client.ANIMATION) {
            final long time = event.getTimestamp();
            final long delta = time - lastTime;
            if (delta > minDelta && !isDigging) {
                currentCPS++;
            }
            lastTime = time;
            final long currentSec = event.getTimestamp() / 1000;
            if (currentSec != lastSec) {
                lastSec = currentSec;
                if (currentCPS > maxCPS) {
                    flagAndAlert("cps=" + currentCPS);
                }
                currentCPS = 0;
            }
        }
        if (event.getPacketType() == PacketType.Play.Client.PLAYER_DIGGING) {
            WrapperPlayClientPlayerDigging packet = new WrapperPlayClientPlayerDigging(event);
            if(packet.getAction() == DiggingAction.START_DIGGING) {
                isDigging = true;
            }
            if(packet.getAction() == DiggingAction.CANCELLED_DIGGING || packet.getAction() == DiggingAction.FINISHED_DIGGING) {
                isDigging = false;
            }
        }
        if(event.getPacketType() == PacketType.Play.Client.INTERACT_ENTITY) {
            isDigging = false; //Need to do this in case the player breaks instant break blocks.. i could just check for that but im lazy and this also does the job
        }

    }

    @Override
    public void reload() {
        this.maxCPS = getConfig().getIntElse(getConfigName() + ".maxCPS", 17);
        this.minDelta = getConfig().getIntElse(getConfigName() + ".minDelta", 40);

    }
}
