package thaumcraft.thaumaturgy;
import java.util.*;
import com.mojang.serialization.Codec;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.*;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.*;
import net.minecraftforge.eventbus.api.bus.BusGroup;
import thaumcraft.*;
import thaumcraft.content.*;
public final class ThaumaturgyContent {
    private static final DeferredRegister<DataComponentType<?>> COMPONENTS=DeferredRegister.create(Registries.DATA_COMPONENT_TYPE,Thaumcraft.MODID);
    private static final DeferredRegister<BlockEntityType<?>> TYPES=DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE,Thaumcraft.MODID);
    private static final DeferredRegister<MenuType<?>> MENUS=DeferredRegister.create(Registries.MENU,Thaumcraft.MODID);
    public static final RegistryObject<DataComponentType<FocusProgram>> PROGRAM=COMPONENTS.register("focus_program",()->DataComponentType.<FocusProgram>builder().persistent(FocusProgram.CODEC).networkSynchronized(ByteBufCodecs.fromCodec(FocusProgram.CODEC)).build());
    public static final RegistryObject<DataComponentType<PlanControls.Selection>> PLAN_SELECTION=COMPONENTS.register("plan_selection",()->DataComponentType.<PlanControls.Selection>builder().persistent(PlanControls.Selection.CODEC).networkSynchronized(ByteBufCodecs.fromCodec(PlanControls.Selection.CODEC)).build());
    public static final RegistryObject<DataComponentType<ItemStack>> FOCUS=COMPONENTS.register("caster_focus",()->DataComponentType.<ItemStack>builder().persistent(ItemStack.CODEC).networkSynchronized(ItemStack.STREAM_CODEC).build());
    public static final RegistryObject<FocalBlock> TABLE=Registrar.BLOCKS.register("wand_workbench",()->new FocalBlock(net.minecraft.world.level.block.state.BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK,SimpleContent.id("wand_workbench"))).strength(2).noOcclusion()));
    public static final RegistryObject<BlockEntityType<FocalEntity>> TABLE_TYPE=TYPES.register("wand_workbench",()->new BlockEntityType<>(FocalEntity::new,Set.of(TABLE.get())));
    public static final RegistryObject<MenuType<FocalMenu>> MENU=MENUS.register("focal_manipulator",()->new MenuType<>(FocalMenu::new,FeatureFlags.VANILLA_SET));
    public static final RegistryObject<EntityType<FocusProjectile>> PROJECTILE=Registrar.ENTITIES.register("focus_projectile",()->EntityType.Builder.<FocusProjectile>of(FocusProjectile::new,MobCategory.MISC).sized(.25F,.25F).clientTrackingRange(8).updateInterval(1).build(ResourceKey.create(Registries.ENTITY_TYPE,SimpleContent.id("focus_projectile"))));
    public static final RegistryObject<EntityType<FocusFieldEntity>> FIELD=Registrar.ENTITIES.register("focus_field",()->EntityType.Builder.<FocusFieldEntity>of(FocusFieldEntity::new,MobCategory.MISC).sized(.25F,.25F).fireImmune().clientTrackingRange(8).updateInterval(5).build(ResourceKey.create(Registries.ENTITY_TYPE,SimpleContent.id("focus_field"))));
    public static final RegistryObject<EntityType<SpellBat>> SPELLBAT=Registrar.ENTITIES.register("focus_spellbat",()->EntityType.Builder.<SpellBat>of(SpellBat::new,MobCategory.MISC).sized(.5F,.9F).clientTrackingRange(8).updateInterval(2).build(ResourceKey.create(Registries.ENTITY_TYPE,SimpleContent.id("focus_spellbat"))));
    public static final RegistryObject<RiftHoleBlock> HOLE=Registrar.BLOCKS.register("hole",()->new RiftHoleBlock(net.minecraft.world.level.block.state.BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK,SimpleContent.id("hole"))).strength(-1,3600000).noOcclusion().noLootTable().lightLevel(s->10).pushReaction(net.minecraft.world.level.material.PushReaction.IMMOVEABLE)));
    public static void register(BusGroup bus){
        SimpleContent.block("table_stone","stone","");SimpleContent.material("primordial_pearl");SimpleContent.material("primordial_nugget");
        SimpleContent.item("focus_1",p->new FocusItem(p,15));SimpleContent.item("focus_2",p->new FocusItem(p,25));SimpleContent.item("focus_3",p->new FocusItem(p,50));
        SimpleContent.item("wand_workbench",p->new BlockItem(TABLE.get(),p.useBlockDescriptionPrefix()));
        SimpleContent.item("caster_basic",CasterItem::new);
        net.minecraftforge.event.entity.EntityAttributeCreationEvent.BUS.addListener(e->e.put(SPELLBAT.get(),net.minecraft.world.entity.ambient.Bat.createAttributes().add(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH,5).build()));RiftData.register();PlanControls.register();
        COMPONENTS.register(bus);TYPES.register(bus);MENUS.register(bus);
        net.minecraftforge.event.entity.player.ItemTooltipEvent.BUS.addListener(e->{var s=e.getItemStack();var program=s.get(PROGRAM.get());if(program==null&&s.getItem() instanceof CasterItem){var f=s.get(FOCUS.get());if(f!=null)program=f.get(PROGRAM.get());}if(program!=null){
            e.getToolTip().add(net.minecraft.network.chat.Component.translatable("focus.thaumcraft.details",net.minecraft.network.chat.Component.translatable("focus.thaumcraft."+program.medium()),program.complexity(),program.vis()));
            e.getToolTip().add(net.minecraft.network.chat.Component.translatable("focus.thaumcraft.materials",program.levels(),program.crystals()));
            e.getToolTip().add(net.minecraft.network.chat.Component.translatable("focal.thaumcraft.inventory_crystals"));
            if(!program.modifier().equals("none"))e.getToolTip().add(net.minecraft.network.chat.Component.translatable("focus.thaumcraft."+program.modifier()).append(program.modifier().equals("scatter")?" • "+program.forks()+" / "+program.cone()+"°":""));
            for(int index=0;index<=program.branches().size();index++){var path=index==0?program.root():program.branches().get(index-1);if(program.split())e.getToolTip().add(net.minecraft.network.chat.Component.translatable("focal.thaumcraft.path",index+1).append(" • ").append(net.minecraft.network.chat.Component.translatable("focus.thaumcraft."+path.medium())));
                if(path.medium().equals("plan"))e.getToolTip().add(net.minecraft.network.chat.Component.translatable("focal.thaumcraft.plan_settings",program.plan().x(),program.plan().y(),program.plan().z(),net.minecraft.network.chat.Component.translatable("focal.thaumcraft."+(program.plan().surface()?"plan_surface":"plan_full"))));
                if(path.medium().equals("cloud"))e.getToolTip().add(net.minecraft.network.chat.Component.translatable("focal.thaumcraft.field_settings",path.radius(),path.seconds()));
                if(path.medium().equals("mine")||path.medium().equals("spellbat"))e.getToolTip().add(net.minecraft.network.chat.Component.translatable("focal.thaumcraft."+(path.friendly()?"friend":"enemy")));
                if(path.medium().equals("projectile"))e.getToolTip().add(net.minecraft.network.chat.Component.translatable("focal.thaumcraft.speed").append(": "+path.projectile().velocity()+" • ").append(net.minecraft.network.chat.Component.translatable("focal.thaumcraft.projectile_"+path.projectile().option())));
                for(var effect:path.effects()){
                    var line=net.minecraft.network.chat.Component.translatable("focus.thaumcraft."+effect.kind()).append(effect.kind().equals("rift")?" "+effect.depth()+" / "+effect.duration()+"s":" "+effect.power());
                    if(effect.harvest())line.append(" • ").append(net.minecraft.network.chat.Component.translatable("focal.thaumcraft.fortune")).append(" "+effect.fortune()).append(" • ").append(net.minecraft.network.chat.Component.translatable("focal.thaumcraft."+(effect.silk()?"silk_yes":"silk_no")));
                    e.getToolTip().add(line);
                }
            }
        }});
    }
}
