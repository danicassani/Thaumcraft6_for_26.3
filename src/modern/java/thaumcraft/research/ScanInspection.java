package thaumcraft.research;

import java.util.*;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.network.*;
import thaumcraft.crystals.CrystalAspects;

/** Private, read-only inspection. Undiscovered aspect values are never sent to the client. */
public final class ScanInspection {
    public record Snapshot(Identifier dimension,int kind,int entityId,long block,String identity,String nameKey,boolean known,boolean scannable,Map<String,Integer> aspects){
        public Snapshot{aspects=Map.copyOf(aspects);if(aspects.size()>64||!known&&!aspects.isEmpty())throw new IllegalArgumentException("Invalid scan inspection");}
        public boolean matches(ScanTarget t){return t!=null&&kind==t.kind()&&identity.equals(t.identity())&&(kind!=ScanTarget.ENTITY||entityId==t.entityId())&&(kind!=ScanTarget.BLOCK||block==t.blockPos().asLong());}
    }
    private static final SimpleChannel CHANNEL=ChannelBuilder.named("thaumcraft:scan_inspection").networkProtocolVersion(1).simpleChannel();
    private static Snapshot client;
    public static Snapshot client(){return client;}
    public static void clear(){client=null;}
    public static Snapshot inspect(ServerPlayer p){return describe(p,ScanTarget.find(p));}
    public static Snapshot describe(net.minecraft.world.entity.player.Player p,ScanTarget target){
        if(target==null||!(p.level() instanceof net.minecraft.server.level.ServerLevel level))return null;
        var knowledge=ResearchService.state(p);if(knowledge==null)return null;
        var values=new TreeMap<String,Integer>();target.aspects(level).forEach((key,n)->{if(CrystalAspects.COLORS.containsKey(key)&&n>0&&n<=1_000_000)values.put(key,n);});boolean known=knowledge.scans().contains(target.identity());
        boolean scannable=!known&&(!values.isEmpty()||!AspectCatalog.triggers(target.creature(),target.id()).isEmpty());
        return new Snapshot(level.dimension().identifier(),target.kind(),target.entityId(),target.blockPos().asLong(),target.identity(),target.nameKey(),known,scannable,known?values:Map.of());
    }
    public static void register(){
        CHANNEL.messageBuilder(Snapshot.class,NetworkDirection.PLAY_TO_CLIENT).encoder((m,b)->{
            b.writeIdentifier(m.dimension());b.writeVarInt(m.kind());b.writeInt(m.entityId());b.writeLong(m.block());b.writeUtf(m.identity(),192);b.writeUtf(m.nameKey(),256);b.writeBoolean(m.known());b.writeBoolean(m.scannable());b.writeVarInt(m.aspects().size());m.aspects().forEach((k,v)->{b.writeUtf(k,32);b.writeVarInt(v);});
        }).decoder(b->{
            var dimension=b.readIdentifier();int kind=b.readVarInt(),id=b.readInt();long block=b.readLong();String identity=b.readUtf(192),name=b.readUtf(256);boolean known=b.readBoolean(),scannable=b.readBoolean();int count=b.readVarInt();
            if(kind<0||kind>3||count<0||count>64)throw new IllegalArgumentException("Invalid scan target");var aspects=new TreeMap<String,Integer>();
            for(int i=0;i<count;i++){String key=b.readUtf(32);int n=b.readVarInt();if(!CrystalAspects.COLORS.containsKey(key)||n<=0||n>1_000_000||aspects.put(key,n)!=null)throw new IllegalArgumentException("Invalid scan aspect");}
            return new Snapshot(dimension,kind,id,block,identity,name,known,scannable,aspects);
        }).consumerMainThread((m,c)->client=m.kind()==0?null:m).add().build();
        TickEvent.PlayerTickEvent.Post.BUS.addListener(e->{if(e.player() instanceof ServerPlayer p&&p.tickCount%5==0&&p.connection!=null&&ScanTarget.holdingMeter(p)){
            var snapshot=inspect(p);if(snapshot==null)snapshot=new Snapshot(p.level().dimension().identifier(),0,-1,0,"","",false,false,Map.of());
            CHANNEL.send(snapshot,PacketDistributor.PLAYER.with(p));
        }});
    }
}
