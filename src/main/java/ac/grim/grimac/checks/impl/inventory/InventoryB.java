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


@CheckData(name = "InventoryB")
public class InventoryB extends Check implements PacketCheck {

    public InventoryB(GrimPlayer playerData) {
        super(playerData);
    }

    private long lastTime = 0;
    private int lastSlot = -1;

    private long maxAverage;
    private int sampleSize;
    private int maxPing;
    private EvictingQueue<Long> samples;
    private boolean hasItem = false;
    private long lastQuickMoveWithItem = 0;

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {

        if (event.getPacketType() == PacketType.Play.Client.CLICK_WINDOW) {
            WrapperPlayClientClickWindow packet = new WrapperPlayClientClickWindow(event);
            if (packet.getWindowClickType() == WrapperPlayClientClickWindow.WindowClickType.PICKUP || packet.getWindowClickType() == WrapperPlayClientClickWindow.WindowClickType.QUICK_MOVE) {
                if (packet.getWindowClickType() == WrapperPlayClientClickWindow.WindowClickType.PICKUP) {
                    if (!hasItem && !packet.getCarriedItemStack().getType().getName().toString().contains("air")) {
                        hasItem = true;
                    }
                    if (hasItem && packet.getCarriedItemStack().getType().getName().toString().contains("air")) {
                        hasItem = false;
                    }
                }
                if (packet.getWindowClickType() == WrapperPlayClientClickWindow.WindowClickType.QUICK_MOVE) {
                    if (hasItem && !packet.getCarriedItemStack().getType().getName().toString().contains("air")) {
                        lastQuickMoveWithItem = event.getTimestamp();
                    }
                }

                final long time = event.getTimestamp();
                final long delta = time - lastTime;
                final int slot = packet.getSlot();
                final int ping = player.getTransactionPing();
                if (delta < 750 && event.getTimestamp() - lastQuickMoveWithItem > 100 && slot != lastSlot && ping < maxPing){
                    samples.add(delta);
                }
                lastTime = time;
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
        this.maxAverage = getConfig().getIntElse(getConfigName() + ".maxAverage", 10);
        this.maxPing = getConfig().getIntElse(getConfigName() + ".maxPing", 100);
        this.sampleSize = getConfig().getIntElse(getConfigName() + ".sampleSize", 5);

        samples = new EvictingQueue<>(sampleSize);

    }
}
