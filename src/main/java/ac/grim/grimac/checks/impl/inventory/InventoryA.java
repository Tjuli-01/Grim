package ac.grim.grimac.checks.impl.inventory;

import ac.grim.grimac.checks.Check;
import ac.grim.grimac.checks.CheckData;
import ac.grim.grimac.checks.type.PacketCheck;
import ac.grim.grimac.player.GrimPlayer;
import ac.grim.grimac.utils.BetterStream;
import ac.grim.grimac.utils.lists.EvictingQueue;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientClickWindow;


@CheckData(name = "InventoryA")
public class InventoryA extends Check implements PacketCheck {

    public InventoryA(GrimPlayer playerData) {
        super(playerData);
    }

    private long lastTime = 0;
    private long lastDelta = 0;
    private int lastSlot = -1;
    private long minDelta;
    private long maxAverage;
    private int sampleSize;
    private EvictingQueue<Long> samples;

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {

        if (event.getPacketType() == PacketType.Play.Client.CLICK_WINDOW) {
            WrapperPlayClientClickWindow packet = new WrapperPlayClientClickWindow(event);
            if (packet.getWindowClickType() == WrapperPlayClientClickWindow.WindowClickType.PICKUP || packet.getWindowClickType() == WrapperPlayClientClickWindow.WindowClickType.QUICK_MOVE) {
                final long time = event.getTimestamp();
                final long delta = time - lastTime;
                final int slot = packet.getSlot();
                if (delta > minDelta && delta != lastDelta && slot != lastSlot && delta < 750) {
                    samples.add(delta);
                }
                lastTime = time;
                lastDelta = delta;
                lastSlot = slot;
                if (samples.size() != sampleSize) {
                    return;
                }
                final long average = BetterStream.getAverageLong(samples);
                if (average <= maxAverage) {
                    flagAndAlert("avg=" + average);
                }


            }


        }

    }

    @Override
    public void reload() {
        this.maxAverage = getConfig().getIntElse(getConfigName() + ".maxAverage", 65);
        this.minDelta = getConfig().getIntElse(getConfigName() + ".minDelta", 30);
        this.sampleSize = getConfig().getIntElse(getConfigName() + ".sampleSize", 5);
        samples = new EvictingQueue<>(sampleSize);

    }
}
