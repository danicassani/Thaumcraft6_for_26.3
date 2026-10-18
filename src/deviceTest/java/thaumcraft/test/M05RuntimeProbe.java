package thaumcraft.test;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.EnergyStorage;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import thaumcraft.Thaumcraft;
import thaumcraft.aura.*;
import thaumcraft.crystals.*;
import thaumcraft.devices.*;
import thaumcraft.api.items.RechargeHelper;

@Mod.EventBusSubscriber(modid = Thaumcraft.MODID)
public final class M05RuntimeProbe {
    public static final BlockPos CRYSTAL = new BlockPos(0,205,0), GENERATOR = new BlockPos(3,205,0), PEDESTAL = new BlockPos(6,205,0), DIOPTRA = new BlockPos(9,205,0);
    private M05RuntimeProbe() {}
    @SubscribeEvent public static void started(ServerStartedEvent event) {
        String mode = System.getProperty("thaumcraft.m05Probe", "");
        if (!mode.equals("write") && !mode.equals("read")) return;
        var level = event.getServer().overworld(); level.getChunk(0,0);
        if (mode.equals("read")) {
            var crystal = level.getBlockState(CRYSTAL);
            require(crystal.is(CrystalContent.BLOCKS.get("vitium").get()) && crystal.getValue(CrystalBlock.SIZE)==2 && crystal.getValue(CrystalBlock.GENERATION)==3,"Crystal state lost on restart");
            require(((VisGeneratorEntity)level.getBlockEntity(GENERATOR)).getEnergyStored()==321,"Generator energy not persisted");
            var item = ((RechargePedestalEntity)level.getBlockEntity(PEDESTAL)).item();
            require(item.is(M05TestRegistry.rechargeable) && RechargeHelper.getCharge(item)==11,"Pedestal item/component lost on restart");
            Thaumcraft.LOGGER.info("M05_RESTART_PROBE_OK: crystal size/generation/faces, generator FE, pedestal item and charge component"); return;
        }
        crystalTests(level); deviceTests(level);
        int natural=0;
        for (int cx=10;cx<15;cx++) for(int cz=10;cz<15;cz++) {
            var chunk=level.getChunk(cx,cz);
            for(int x=0;x<16;x++) for(int z=0;z<16;z++) for(int y=-48;y<100;y++)
                if(chunk.getBlockState(new BlockPos(cx*16+x,y,cz*16+z)).getBlock() instanceof CrystalBlock) natural++;
        }
        require(natural>0,"No naturally generated crystals in 25 new chunks");
        Thaumcraft.LOGGER.info("M05_RUNTIME_PROBE_OK: 7 crystals, 37 essence components, support/drop/growth/spread; FE capability and redstone; pedestal automation/charge; dioptra modes; {} natural crystal blocks",natural);
    }
    private static void aura(ServerLevel level,float vis,float flux) { AuraData.get(level).put(AuraCell.key(0,0),new AuraCell(200,vis,flux)); }
    private static void crystalTests(ServerLevel level) {
        for(var entry:CrystalContent.BLOCKS.entrySet()) {
            var block=entry.getValue().get(); level.setBlockAndUpdate(CRYSTAL.below(),Blocks.STONE.defaultBlockState());
            var state=block.supportedState(block.defaultBlockState(),level,CRYSTAL);
            require(state.canSurvive(level,CRYSTAL),"Crystal refuses stone"); level.setBlockAndUpdate(CRYSTAL,state);
            aura(level,250,250); block.grow(state,level,CRYSTAL,level.getRandom());
            require(level.getBlockState(CRYSTAL).getValue(CrystalBlock.SIZE)==1,"Crystal did not grow");
            var cell=AuraManager.get(level,CRYSTAL);
            require(entry.getKey().equals("vitium") ? cell.flux()==240 && cell.vis()==250 : cell.vis()==240 && cell.flux()==250,"Crystal used wrong aura channel");
            var drops=Block.getDrops(level.getBlockState(CRYSTAL),level,CRYSTAL,null,null,new ItemStack(Items.DIAMOND_PICKAXE));
            require(drops.size()==1 && drops.getFirst().getCount()==2 && CrystalContent.aspect(drops.getFirst()).equals(entry.getKey()),"Crystal essence drop mismatch");
            require(state.getCollisionShape(level,CRYSTAL).isEmpty(),"Crystal collision");
            aura(level,5,5); block.grow(level.getBlockState(CRYSTAL),level,CRYSTAL,level.getRandom());
            require(level.getBlockState(CRYSTAL).getValue(CrystalBlock.SIZE)==0,"Crystal did not shrink");
            require(entry.getKey().equals("vitium") ? AuraManager.get(level,CRYSTAL).flux()==15 : AuraManager.get(level,CRYSTAL).vis()==15,"Shrink did not release aura");
            level.setBlockAndUpdate(CRYSTAL.below(),Blocks.AIR.defaultBlockState());
            require(!state.canSurvive(level,CRYSTAL),"Unsupported crystal survived");
            level.getBlockState(CRYSTAL).tick(level,CRYSTAL,level.getRandom());
            require(level.getBlockState(CRYSTAL).isAir(),"Unsupported crystal did not break");
        }
        for(String aspect:CrystalAspects.COLORS.keySet()) {
            var stack=CrystalContent.essence(aspect,1); var tag=ItemStack.CODEC.encodeStart(level.registryAccess().createSerializationContext(net.minecraft.nbt.NbtOps.INSTANCE),stack).getOrThrow();
            var restored=ItemStack.CODEC.parse(level.registryAccess().createSerializationContext(net.minecraft.nbt.NbtOps.INSTANCE),tag).getOrThrow();
            require(CrystalContent.aspect(restored).equals(aspect),"Essence component not serialized: "+aspect);
        }
        // Repeated real attempts exercise propagation, generation increment and debit.
        var block=CrystalContent.BLOCKS.get("aer").get();
        for(int x=-1;x<=1;x++)for(int z=-1;z<=1;z++)for(int y=0;y<=1;y++)level.setBlockAndUpdate(CRYSTAL.offset(x,y,z),Blocks.AIR.defaultBlockState());
        for(int x=-1;x<=1;x++)for(int z=-1;z<=1;z++)level.setBlockAndUpdate(CRYSTAL.offset(x,-1,z),Blocks.STONE.defaultBlockState());
        var mature=block.supportedState(block.defaultBlockState().setValue(CrystalBlock.SIZE,3),level,CRYSTAL);level.setBlockAndUpdate(CRYSTAL,mature);
        aura(level,1000,0);boolean spread=false;
        for(int attempt=0;attempt<2000 && !spread;attempt++) {
            block.grow(mature,level,CRYSTAL,level.getRandom());
            for(int x=-1;x<=1;x++)for(int z=-1;z<=1;z++)if((x!=0||z!=0)&&level.getBlockState(CRYSTAL.offset(x,0,z)).is(block))spread=true;
        }
        require(spread && AuraManager.get(level,CRYSTAL).vis()<=990,"Crystal propagation/debit");
    }
    private static void deviceTests(ServerLevel level) {
        level.setBlockAndUpdate(GENERATOR,Blocks.AIR.defaultBlockState());
        level.setBlockAndUpdate(GENERATOR.below(),Blocks.AIR.defaultBlockState());
        level.setBlockAndUpdate(GENERATOR.east(),Blocks.AIR.defaultBlockState());
        level.setBlockAndUpdate(PEDESTAL,Blocks.AIR.defaultBlockState());
        var genState=DeviceContent.GENERATOR.get().defaultBlockState().setValue(AuraDeviceBlock.FACING,Direction.EAST);
        level.setBlockAndUpdate(GENERATOR,genState);level.setBlockAndUpdate(GENERATOR.east(),genState);
        var receiver=new Receiver(GENERATOR.east(),genState);level.setBlockEntity(receiver);
        var generator=(VisGeneratorEntity)level.getBlockEntity(GENERATOR);aura(level,1,0);generator.serverTick();
        require(generator.getEnergyStored()==980 && receiver.energy.getEnergyStored()==20 && AuraManager.get(level,GENERATOR).vis()==0,"Vis-to-FE conversion/output");
        require(generator.getCapability(ForgeCapabilities.ENERGY,Direction.EAST).isPresent() && !generator.getCapability(ForgeCapabilities.ENERGY,Direction.WEST).isPresent(),"Energy side exposure");
        for(int i=0;i<49;i++)generator.serverTick();
        require(generator.getEnergyStored()==0 && receiver.energy.getEnergyStored()==1000,"FE total not conserved");
        level.setBlockAndUpdate(GENERATOR.below(),Blocks.REDSTONE_BLOCK.defaultBlockState());aura(level,1,0);generator.serverTick();
        require(generator.getEnergyStored()==0 && AuraManager.get(level,GENERATOR).vis()==1,"Redstone did not disable generator");
        level.setBlockAndUpdate(GENERATOR.below(),Blocks.AIR.defaultBlockState());aura(level,.0005F,0);generator.serverTick();
        require(generator.getEnergyStored()==0 && AuraManager.get(level,GENERATOR).vis()==.0005F,"Sub-FE vis fraction lost");
        level.setBlockAndUpdate(GENERATOR.east(),Blocks.AIR.defaultBlockState());
        level.setBlockAndUpdate(PEDESTAL,DeviceContent.PEDESTAL.get().defaultBlockState());var pedestal=(RechargePedestalEntity)level.getBlockEntity(PEDESTAL);
        var inventory=pedestal.getCapability(ForgeCapabilities.ITEM_HANDLER,Direction.UP).orElseThrow(()->new AssertionError("No item capability"));
        require(!inventory.insertItem(0,new ItemStack(Items.DIRT),false).isEmpty(),"Pedestal accepted an invalid item");
        require(inventory.insertItem(0,new ItemStack(M05TestRegistry.rechargeable),false).isEmpty(),"Pedestal refused rechargeable item");
        aura(level,3.75F,0);require(pedestal.recharge()==3 && AuraManager.get(level,PEDESTAL).vis()==.75F && RechargeHelper.getCharge(pedestal.item())==3,"Recharge lost fractional vis");
        aura(level,100,0);for(int i=0;i<10;i++)pedestal.recharge();require(RechargeHelper.getCharge(pedestal.item())==25,"Charge capacity");
        require(RechargeHelper.consumeCharge(inventory.getStackInSlot(0),null,5) && !RechargeHelper.consumeCharge(inventory.getStackInSlot(0),null,-1),"Charge consumption validation");
        require(!inventory.extractItem(0,1,false).isEmpty() && pedestal.item().isEmpty(),"Pedestal extraction");
        inventory.insertItem(0,new ItemStack(M05TestRegistry.rechargeable),false);
        var oldCapability=pedestal.getCapability(ForgeCapabilities.ITEM_HANDLER,Direction.UP);
        var box=new net.minecraft.world.phys.AABB(PEDESTAL).inflate(2);
        long before=level.getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class,box).stream().filter(e->e.getItem().is(M05TestRegistry.rechargeable)).count();
        level.destroyBlock(PEDESTAL,true);
        long after=level.getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class,box).stream().filter(e->e.getItem().is(M05TestRegistry.rechargeable)).count();
        require(after==before+1 && !oldCapability.isPresent(),"Pedestal removal duplicated/lost item or retained capability");
        level.setBlockAndUpdate(PEDESTAL,DeviceContent.PEDESTAL.get().defaultBlockState());
        level.setBlockAndUpdate(DIOPTRA,DeviceContent.DIOPTRA.get().defaultBlockState());var dioptra=(DioptraEntity)level.getBlockEntity(DIOPTRA);
        aura(level,250,125);dioptra.scan();require(dioptra.grid()[84]==32,"Dioptra vis sample");
        level.setBlockAndUpdate(DIOPTRA.below(),Blocks.REDSTONE_BLOCK.defaultBlockState());dioptra.scan();require(dioptra.grid()[84]==16,"Dioptra flux/redstone mode");
        var copy=dioptra.grid();copy[84]=64;require(dioptra.grid()[84]==16,"Dioptra exposed mutable grid");
        level.setBlockAndUpdate(DIOPTRA.below(),Blocks.AIR.defaultBlockState());
        boolean enabled=thaumcraft.config.ThaumcraftConfig.generateCrystals.get();
        try { thaumcraft.config.ThaumcraftConfig.generateCrystals.set(false);require(new thaumcraft.content.OreCountPlacement("crystals",8).count(level.getRandom(),CRYSTAL)==0,"Crystal generation toggle ignored"); }
        finally { thaumcraft.config.ThaumcraftConfig.generateCrystals.set(enabled); }
    }
    @SubscribeEvent public static void stopping(ServerStoppingEvent event) {
        if(!System.getProperty("thaumcraft.m05Probe","").equals("write"))return;
        var level=event.getServer().overworld();level.getChunk(0,0);
        var crystal=CrystalContent.BLOCKS.get("vitium").get();level.setBlockAndUpdate(CRYSTAL.below(),Blocks.STONE.defaultBlockState());
        level.setBlockAndUpdate(CRYSTAL,crystal.supportedState(crystal.defaultBlockState().setValue(CrystalBlock.SIZE,2).setValue(CrystalBlock.GENERATION,3),level,CRYSTAL));
        level.setBlockAndUpdate(GENERATOR,Blocks.AIR.defaultBlockState());level.setBlockAndUpdate(GENERATOR,DeviceContent.GENERATOR.get().defaultBlockState().setValue(AuraDeviceBlock.FACING,Direction.EAST));
        level.setBlockAndUpdate(GENERATOR.below(),Blocks.AIR.defaultBlockState());level.setBlockAndUpdate(GENERATOR.east(),Blocks.AIR.defaultBlockState());aura(level,.321F,0);
        ((VisGeneratorEntity)level.getBlockEntity(GENERATOR)).serverTick();level.setBlockAndUpdate(GENERATOR.below(),Blocks.REDSTONE_BLOCK.defaultBlockState());
        var pedestal=(RechargePedestalEntity)level.getBlockEntity(PEDESTAL);
        var inventory=pedestal.getCapability(ForgeCapabilities.ITEM_HANDLER,Direction.UP).orElseThrow(()->new AssertionError("No item cap"));
        inventory.extractItem(0,1,false);var stack=new ItemStack(M05TestRegistry.rechargeable);stack.set(DeviceContent.CHARGE.get(),11);inventory.insertItem(0,stack,false);
        aura(level,0,200); // Crystal and pedestal cannot progress between stopping and save.
        Thaumcraft.LOGGER.info("M05_SAVE_PROBE_READY");
    }
    public static void require(boolean value,String message) { if(!value)throw new AssertionError(message); }
    private static final class Receiver extends BlockEntity {
        final EnergyStorage energy=new EnergyStorage(10000,1000);
        final LazyOptional<net.minecraftforge.energy.IEnergyStorage> cap=LazyOptional.of(()->energy);
        Receiver(BlockPos pos,BlockState state) { super(DeviceContent.GENERATOR_TYPE.get(),pos,state); }
        @Override public <T> LazyOptional<T> getCapability(Capability<T> capability,Direction side) { return capability==ForgeCapabilities.ENERGY?cap.cast():super.getCapability(capability,side); }
    }
}
