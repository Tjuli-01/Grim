package ac.grim.grimac.checks.impl.misc;

import ac.grim.grimac.checks.Check;
import ac.grim.grimac.checks.CheckData;
import ac.grim.grimac.checks.type.PacketCheck;
import ac.grim.grimac.player.GrimPlayer;
import ac.grim.grimac.utils.BetterStream;
import ac.grim.grimac.utils.lists.EvictingQueue;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;


@CheckData(name = "FastPlace", experimental = false)
public class FastPlace extends Check implements PacketCheck {

    public FastPlace(GrimPlayer playerData) {
        super(playerData);
    }

    private long lastTime = 0;
    private long lastDelta = 0;
    private int sampleSize;
    private long maxAverage;
    private long minDelta;
    private EvictingQueue<Long> samples;

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if(event.getPacketType() == PacketType.Play.Client.PLAYER_BLOCK_PLACEMENT || event.getPacketType() == PacketType.Play.Client.USE_ITEM) {
            final long time = event.getTimestamp();
            final long delta = time - lastTime;
            if(delta > minDelta && delta != lastDelta && delta < 5000) {samples.add(delta);}
            lastTime = time;
            lastDelta = delta;
            if(samples.size() != sampleSize) {return;}

            final long average = BetterStream.getAverageLong(samples);
            if(average <= maxAverage) {
                flagAndAlert("avg=" + average);
            }
        }
    }

    @Override
    public void reload() {
        this.maxAverage = getConfig().getIntElse(getConfigName() + ".maxAverage", 50);
        this.sampleSize = getConfig().getIntElse(getConfigName() + ".sampleSize", 15);
        this.minDelta = getConfig().getIntElse(getConfigName() + ".minDelta", 30);
        samples = new EvictingQueue<>(sampleSize);
    }
}
