package thaumcraft.test;
import net.minecraft.core.*;
import net.minecraft.server.level.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.minecraft.world.phys.*;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import thaumcraft.Thaumcraft;
import thaumcraft.storage.HungryChest;
@Mod.EventBusSubscriber(modid="thaumcraft")
public final class HungryChestProbe {
 public static final BlockPos P=new BlockPos(3440,205,8);static int ticks,stage;static long due;public static volatile boolean ready;public static ServerPlayer actor;
 static String mode(){return System.getProperty("thaumcraft.alchemyProbe","");}static void check(boolean b,String s){if(!b)throw new AssertionError(s);}
 static HungryChest chest(ServerLevel l,BlockPos p){return (HungryChest)l.getBlockEntity(p);}
 static ItemEntity drop(ServerLevel l,BlockPos p,ItemStack s){var e=new ItemEntity(l,p.getX()+.5,p.getY()+.9,p.getZ()+.5,s);l.addFreshEntity(e);return e;}
 static void tests(ServerLevel l){l.setBlockAndUpdate(P,HungryChest.BLOCK.get().defaultBlockState());l.setBlockAndUpdate(P.east(),HungryChest.BLOCK.get().defaultBlockState());var chest=chest(l,P);check(chest.getContainerSize()==27&&l.getBlockState(P).getValue(ChestBlock.TYPE)==ChestType.SINGLE,"wrong capacity/connection");var named=new ItemStack(Items.DIAMOND,20);named.set(net.minecraft.core.component.DataComponents.CUSTOM_NAME,net.minecraft.network.chat.Component.literal("Hungry persistence"));var e=drop(l,P,named);check(chest.absorb(e)&&e.isRemoved()&&chest.getItem(0).getCount()==20&&chest.getItem(0).getHoverName().getString().equals("Hungry persistence"),"pickup lost components");check(!chest.absorb(e)&&chest.getItem(0).getCount()==20,"removed item duplicated");var outside=drop(l,P.east(3),new ItemStack(Items.DIAMOND,5));check(!chest.absorb(outside)&&outside.getItem().getCount()==5,"chest vacuumed distant items");outside.discard();
 var handler=chest.getCapability(net.minecraftforge.common.capabilities.ForgeCapabilities.ITEM_HANDLER,Direction.DOWN).orElseThrow(()->new AssertionError("missing automation"));var simulation=handler.extractItem(0,3,true);check(simulation.getCount()==3&&chest.getItem(0).getCount()==20,"simulation extracted items");var actual=handler.extractItem(0,3,false);check(actual.getCount()==3&&chest.getItem(0).getCount()==17,"automation extraction");
 for(int i=0;i<27;i++)chest.setItem(i,new ItemStack(Items.COBBLESTONE,64));chest.setItem(0,new ItemStack(Items.COBBLESTONE,63));e=drop(l,P,new ItemStack(Items.COBBLESTONE,5));check(chest.absorb(e)&&e.isAlive()&&e.getItem().getCount()==4&&chest.getItem(0).getCount()==64,"partial insertion lost leftovers");check(!chest.absorb(e)&&e.getItem().getCount()==4,"full chest consumed items");check(net.minecraft.world.inventory.AbstractContainerMenu.getRedstoneSignalFromContainer(chest)==15,"full comparator");e.discard();
 var neighbor=chest(l,P.east());neighbor.setItem(0,new ItemStack(Items.GOLD_INGOT,7));l.destroyBlock(P.east(),false);var drops=l.getEntitiesOfClass(ItemEntity.class,new AABB(P.east()).inflate(1));check(drops.stream().filter(it->it.getItem().is(Items.GOLD_INGOT)).mapToInt(it->it.getItem().getCount()).sum()==7,"break lost or duplicated contents");drops.forEach(Entity::discard);
 chest.clearContent();chest.setItem(0,named.copy());Thaumcraft.LOGGER.info("HUNGRY_NATIVE_OK: 27 isolated slots, physical component-preserving pickup, no replay/distant pickup, automation simulation/extraction, partial/full conservation, comparator and exact break drops");}
 @SubscribeEvent public static void tick(TickEvent.ServerTickEvent.Post e){if(!mode().startsWith("hungry-"))return;var l=e.server().overworld();if(++ticks<80||stage==9)return;if(stage==0){for(int x=214;x<=216;x++)for(int z=-1;z<=1;z++)l.setChunkForced(x,z,true);due=l.getGameTime()+50;stage=1;return;}if(l.getGameTime()<due)return;stage=9;
 if(mode().equals("hungry-read")){boolean recipe=false;for(var holder:l.recipeAccess().getRecipes())if(holder.value() instanceof thaumcraft.crafting.ArcaneRecipe r&&r.research().equals("HUNGRYCHEST")){check(r.vis()==15&&r.crystals().equals(java.util.Map.of("terra",1,"aqua",1)),"arcane recipe costs");recipe=true;}check(recipe,"missing original arcane recipe");var c=chest(l,P);check(c!=null&&c.getItem(0).getCount()==20&&c.getItem(0).getHoverName().getString().equals("Hungry persistence"),"restart lost inventory/components");Thaumcraft.LOGGER.info("HUNGRY_RESTART_OK: native chunk NBT retained named inventory and custom block entity");e.server().halt(false);return;}
 l.getEntitiesOfClass(Entity.class,new AABB(P).inflate(25),en->!(en instanceof net.minecraft.world.entity.player.Player)).forEach(Entity::discard);for(var q:BlockPos.betweenClosed(P.offset(-10,-1,-8),P.offset(12,5,10)))l.setBlockAndUpdate(q,q.getY()==P.getY()-1?Blocks.GLOWSTONE.defaultBlockState():Blocks.AIR.defaultBlockState());
 if(mode().equals("hungry-client")){if(e.server().getPlayerList().getPlayers().isEmpty()){stage=1;return;}actor=e.server().getPlayerList().getPlayers().getFirst();actor.setGameMode(net.minecraft.world.level.GameType.CREATIVE);actor.getInventory().clearContent();actor.teleportTo(l,P.getX()+.5,P.getY()+1,P.getZ()+3.5,java.util.Set.of(),180,35,true);actor.getAbilities().flying=true;actor.onUpdateAbilities();l.setBlockAndUpdate(P,HungryChest.BLOCK.get().defaultBlockState().setValue(ChestBlock.FACING,Direction.SOUTH));drop(l,P,new ItemStack(Items.DIAMOND,12));ready=true;Thaumcraft.LOGGER.info("HUNGRY_CLIENT_READY: native hungry chest scene with twelve dropped diamonds");return;}
 tests(l);e.server().halt(false);}
}
