package thaumcraft.banners;

import com.mojang.serialization.*;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.*;
import net.minecraft.nbt.*;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.TypedEntityData;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.*;
import thaumcraft.alchemy.AlchemyContent;
import thaumcraft.devices.AuraDeviceEntity;

public final class AspectBannerEntity extends AuraDeviceEntity {
 public record Data(int version,String aspect){public static final Codec<Data> CODEC=RecordCodecBuilder.create(i->i.group(Codec.intRange(1,1).fieldOf("version").forGetter(Data::version),thaumcraft.essentia.TubeState.FILTER.fieldOf("aspect").forGetter(Data::aspect)).apply(i,Data::new));}
 private String aspect="";private Tag unsupported;
 public AspectBannerEntity(BlockPos p,BlockState s){super(BannerContent.TYPE.get(),p,s);}
 public boolean supported(){return unsupported==null;}
 public String aspect(){return aspect;}
 public void serverTick(){}
 public InteractionResult decorate(Player player,InteractionHand hand){
  if(level==null||!supported()||((AspectBannerBlock)getBlockState().getBlock()).dye==null||!player.mayBuild()||!level.mayInteract(player,worldPosition)||player.distanceToSqr(net.minecraft.world.phys.Vec3.atCenterOf(worldPosition))>64)return InteractionResult.FAIL;
  var held=player.getItemInHand(hand);boolean erase=player.isShiftKeyDown();String next=erase?"":AlchemyContent.aspect(held);
  if(!erase&&(!held.is(AlchemyContent.PHIAL.get())||AlchemyContent.amount(held)!=10||!thaumcraft.crystals.CrystalAspects.COLORS.containsKey(next)))return InteractionResult.PASS;
  if(next.equals(aspect))return InteractionResult.FAIL;
  if(!level.isClientSide()){aspect=next;if(!erase&&!player.getAbilities().instabuild)held.shrink(1);sync();level.playSound(null,worldPosition,net.minecraft.sounds.SoundEvents.WOOL_HIT,net.minecraft.sounds.SoundSource.BLOCKS,1,1);}
  return InteractionResult.SUCCESS;
 }
 public void placeContents(ItemStack item){if(supported()){aspect=((AspectBannerBlock)getBlockState().getBlock()).dye==null?"":item.getOrDefault(BannerContent.ASPECT.get(),"");sync();}}
 public ItemStack droppedItem(){var item=new ItemStack(getBlockState().getBlock());if(!aspect.isEmpty())item.set(BannerContent.ASPECT.get(),aspect);if(!supported()&&level!=null)item.set(net.minecraft.core.component.DataComponents.BLOCK_ENTITY_DATA,TypedEntityData.of(getType(),saveWithFullMetadata(level.registryAccess())));return item;}
 protected void saveAdditional(ValueOutput out){super.saveAdditional(out);out.store("banner",Codec.PASSTHROUGH,new Dynamic<>(NbtOps.INSTANCE,supported()?Data.CODEC.encodeStart(NbtOps.INSTANCE,new Data(1,aspect)).getOrThrow():unsupported));}
 protected void loadAdditional(ValueInput in){super.loadAdditional(in);aspect="";unsupported=null;var raw=in.read("banner",Codec.PASSTHROUGH);if(raw.isPresent()){var tag=raw.get().convert(NbtOps.INSTANCE).getValue();var data=Data.CODEC.parse(NbtOps.INSTANCE,tag).result();if(data.isPresent())aspect=data.get().aspect();else unsupported=tag.copy();}else{var old=in.getStringOr("aspect","");if(thaumcraft.crystals.CrystalAspects.COLORS.containsKey(old))aspect=old;}}
}
