package thaumcraft.equipment;
import java.util.*;
import com.mojang.serialization.Codec;
import net.minecraft.core.*;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.*;
/** Three original architect planes, bounded to nine real inventory blocks. */
public final class ElementalShovelItem extends Item {
 private static final DeferredRegister<DataComponentType<?>> COMPONENTS=DeferredRegister.create(Registries.DATA_COMPONENT_TYPE,"thaumcraft");
 public static final RegistryObject<DataComponentType<Integer>> ORIENTATION=COMPONENTS.register("shovel_orientation",()->DataComponentType.<Integer>builder().persistent(Codec.intRange(0,2)).networkSynchronized(ByteBufCodecs.VAR_INT).build());
 public static void register(net.minecraftforge.eventbus.api.bus.BusGroup bus){COMPONENTS.register(bus);}
 public ElementalShovelItem(Properties p){super(p);}

 public InteractionResult use(Level l,Player p,InteractionHand h){if(!p.isShiftKeyDown())return InteractionResult.PASS;if(!l.isClientSide()){var s=p.getItemInHand(h);int mode=(s.getOrDefault(ORIENTATION.get(),0)+1)%3;s.set(ORIENTATION.get(),mode);((ServerPlayer)p).sendSystemMessage(Component.translatable("equipment.thaumcraft.shovel_plane",mode+1),true);}return InteractionResult.SUCCESS;}
 public InteractionResult useOn(UseOnContext c){var p=c.getPlayer();if(p==null||p.isSpectator())return InteractionResult.PASS;if(p.isShiftKeyDown())return super.useOn(c);var l=c.getLevel();var origin=c.getClickedPos();var state=l.getBlockState(origin);if(state.hasBlockEntity()||state.isAir()||!(state.getBlock().asItem() instanceof BlockItem))return InteractionResult.PASS;if(l.isClientSide())return InteractionResult.SUCCESS;if(!(p instanceof ServerPlayer real)||!p.getAbilities().mayBuild)return InteractionResult.FAIL;
  var tool=c.getItemInHand();boolean changed=false,capture=l.captureBlockSnapshots;l.captureBlockSnapshots=false;
  try{for(var q:ArchitectPlane.select(origin,c.getClickedFace(),p.getDirection(),tool.getOrDefault(ORIENTATION.get(),0))){if(tool.isEmpty())break;if(!l.hasChunkAt(q)||!l.getWorldBorder().isWithinBounds(q)||!l.isInWorldBounds(q)||!l.getBlockState(q).canBeReplaced()||!l.mayInteract(p,q)||!p.mayUseItemAt(q,c.getClickedFace(),tool))continue;
    ItemStack supply=ItemStack.EMPTY;var placing=state;for(int i=0;i<p.getInventory().getContainerSize();i++){var candidate=p.getInventory().getItem(i);if(candidate.is(state.getBlock().asItem())){supply=candidate;break;}}if(supply.isEmpty()&&state.is(net.minecraft.world.level.block.Blocks.GRASS_BLOCK)){for(int i=0;i<p.getInventory().getContainerSize();i++){var candidate=p.getInventory().getItem(i);if(candidate.is(Items.DIRT)){supply=candidate;placing=net.minecraft.world.level.block.Blocks.DIRT.defaultBlockState();break;}}}if(supply.isEmpty()&&!p.getAbilities().instabuild)continue;if(!placing.canSurvive(l,q)||!l.isUnobstructed(placing,q,net.minecraft.world.phys.shapes.CollisionContext.of(p)))continue;
    var snapshot=net.minecraftforge.common.util.BlockSnapshot.create(l.dimension(),l,q);if(l.setBlockAndUpdate(q,placing)){if(net.minecraftforge.event.ForgeEventFactory.onBlockPlace(real,snapshot,c.getClickedFace())){boolean restoring=l.restoringBlockSnapshots;l.restoringBlockSnapshots=true;try{snapshot.restore(true,false);}finally{l.restoringBlockSnapshots=restoring;}continue;}if(!p.getAbilities().instabuild)supply.shrink(1);tool.hurtAndBreak(1,p,c.getHand());changed=true;}}
  }finally{l.captureBlockSnapshots=capture;}if(changed)p.getInventory().setChanged();return changed?InteractionResult.SUCCESS:InteractionResult.FAIL;
 }
}
