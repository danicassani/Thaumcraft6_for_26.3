package thaumcraft.research;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import java.util.*;
import net.minecraft.resources.RegistryOps;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.*;
/** Only the open server menu is inspected; an unknown item's aspect values never leave the server. */
public final class InventoryInspection {
 public record Request(int token,int container,int slot){}
 public record Reply(int token,String item,boolean known,Map<String,Integer> aspects){public Reply{aspects=Map.copyOf(aspects);}}
 private static final SimpleChannel CHANNEL=ChannelBuilder.named("thaumcraft:inventory_inspection").networkProtocolVersion(1).simpleChannel();
 private static final Map<UUID,Long> LAST=new HashMap<>();private static Reply received;
 public static Reply received(){return received;}public static void clear(){received=null;}
 public static Reply inspect(Player p,Request r){
  var menu=p.containerMenu;if(!(p.level() instanceof net.minecraft.server.level.ServerLevel level)||menu.containerId!=r.container()||r.slot()<0||r.slot()>=menu.slots.size()||!menu.stillValid(p))return new Reply(r.token(),"",false,Map.of());
  var stack=menu.getSlot(r.slot()).getItem();var state=ResearchService.state(p);if(stack.isEmpty()||state==null)return new Reply(r.token(),"",false,Map.of());
  String item=ItemStack.CODEC.encodeStart(RegistryOps.create(JsonOps.INSTANCE,p.registryAccess()),stack.copyWithCount(1)).result().map(Object::toString).orElse("");if(item.length()>32767)return new Reply(r.token(),"",false,Map.of());
  boolean known=state.scans().contains("item:"+ResearchCatalog.proof(stack));return new Reply(r.token(),item,known,known?thaumcraft.alchemy.AspectResolver.item(level,stack):Map.of());
 }
 public static ItemStack item(Reply r,net.minecraft.core.HolderLookup.Provider registries){try{return ItemStack.CODEC.parse(RegistryOps.create(JsonOps.INSTANCE,registries),JsonParser.parseString(r.item())).result().orElse(ItemStack.EMPTY);}catch(RuntimeException ex){return ItemStack.EMPTY;}}
 public static void request(int token,int container,int slot){CHANNEL.send(new Request(token,container,slot),PacketDistributor.SERVER.noArg());}
 public static void register(){
  CHANNEL.messageBuilder(Request.class,NetworkDirection.PLAY_TO_SERVER).encoder((m,b)->{b.writeInt(m.token());b.writeInt(m.container());b.writeInt(m.slot());}).decoder(b->new Request(b.readInt(),b.readInt(),b.readInt())).consumerMainThread((m,c)->{var p=c.getSender();if(p==null)return;long now=p.level().getGameTime();var last=LAST.get(p.getUUID());if(last!=null&&now>=last&&now-last<2)return;LAST.put(p.getUUID(),now);CHANNEL.send(inspect(p,m),PacketDistributor.PLAYER.with(p));}).add();
  CHANNEL.messageBuilder(Reply.class,NetworkDirection.PLAY_TO_CLIENT).encoder((m,b)->{b.writeInt(m.token());b.writeUtf(m.item(),32767);b.writeBoolean(m.known());b.writeVarInt(m.aspects().size());m.aspects().forEach((k,v)->{b.writeUtf(k,32);b.writeVarInt(v);});}).decoder(b->{int token=b.readInt();String item=b.readUtf(32767);boolean known=b.readBoolean();int size=b.readVarInt();if(size<0||size>64||!known&&size>0)throw new IllegalArgumentException("Invalid inventory aspects");var values=new TreeMap<String,Integer>();for(int i=0;i<size;i++){String key=b.readUtf(32);int n=b.readVarInt();if(!thaumcraft.crystals.CrystalAspects.COLORS.containsKey(key)||n<1||n>1_000_000||values.put(key,n)!=null)throw new IllegalArgumentException("Invalid inventory aspect");}return new Reply(token,item,known,values);}).consumerMainThread((m,c)->received=m).add().build();
  net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent.BUS.addListener(e->LAST.remove(e.getEntity().getUUID()));net.minecraftforge.event.server.ServerStoppedEvent.BUS.addListener(e->LAST.clear());
 }
 private InventoryInspection(){}
}
