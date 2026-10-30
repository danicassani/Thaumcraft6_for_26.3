"""Explicit TC6 restorations + documented material equivalents for Minecraft 26.3.
No universal aspect fallback. Registry sample is from the native port.78 audit.
"""
from pathlib import Path
import json
root=Path(__file__).resolve().parents[1]
missing=json.loads((root/'docs/ASPECT-COVERAGE-BASELINE.json').read_text())['missing']
items={};aliases={}
def add(names,**values):
 for name in names.split():items[name if ':' in name else 'minecraft:'+name]=values
add('thaumcraft:amber',vinculum=10,vitreus=10)
add('thaumcraft:ore_amber',terra=5,vinculum=10,vitreus=10)
add('thaumcraft:ore_cinnabar',terra=5,metallum=10,alkimia=5,mortuus=5)
add('thaumcraft:ore_quartz',terra=5,vitreus=10)
add('thaumcraft:cluster_cinnabar',ordo=5,metallum=15,terra=5,alkimia=5,mortuus=5)
add('thaumcraft:cluster_quartz',ordo=5,vitreus=10)
add('thaumcraft:chunk_beef thaumcraft:chunk_chicken thaumcraft:chunk_pork thaumcraft:chunk_fish thaumcraft:chunk_rabbit thaumcraft:chunk_mutton',victus=5,perditio=1)
add('thaumcraft:nugget_rareearth',terra=5,ordo=5,metallum=5)
add('thaumcraft:void_seed',alienis=10,praecantatio=5)
add('thaumcraft:ingot_void',metallum=10,alienis=5)
add('thaumcraft:nugget_tin',metallum=1,vitreus=1)
add('thaumcraft:nugget_silver',metallum=1,desiderium=1)
add('thaumcraft:nugget_lead',metallum=1,ordo=1)
add('thaumcraft:nugget_quartz',vitreus=1)
add('thaumcraft:cluster_tin',metallum=15,terra=5,ordo=5,vitreus=10)
add('thaumcraft:cluster_silver',metallum=15,terra=5,ordo=5,desiderium=10)
add('thaumcraft:cluster_lead',metallum=15,terra=5,ordo=15)
add('deepslate cobbled_deepslate calcite tuff dripstone_block pointed_dripstone basalt blackstone reinforced_deepslate petrified_oak_slab',terra=5)
add('sulfur potent_sulfur sulfur_spike',terra=5,ignis=5,alkimia=5)
add('cinnabar',terra=5,metallum=10,alkimia=5,mortuus=5)
add('rooted_dirt',terra=5,herba=2)
add('mud packed_mud',terra=5,aqua=3)
add('soul_soil',terra=3,vinculum=1,spiritus=3)
add('ancient_debris',terra=5,metallum=15,ignis=5)
add('netherite_scrap',metallum=15,ignis=5)
add('raw_iron',metallum=15)
add('raw_gold',metallum=10,desiderium=10)
add('raw_copper',metallum=10,permutatio=5)
add('nether_gold_ore',terra=5,metallum=10,desiderium=10)
add('deepslate_coal_ore',terra=5,potentia=15,ignis=15)
add('amethyst_shard',vitreus=10,sensus=5)
add('small_amethyst_bud medium_amethyst_bud large_amethyst_bud amethyst_cluster budding_amethyst',vitreus=10,sensus=5,terra=5)
add('sugar_cane',herba=5,aqua=3,aer=2)
add('carrot',herba=5,victus=5,sensus=5)
add('potato',herba=5,victus=5,terra=5)
add('cactus',herba=5,aqua=5,aversio=1)
add('bamboo bamboo_block stripped_bamboo_block mangrove_roots',herba=20)
add('mushroom_stem crimson_fungus warped_fungus crimson_roots warped_roots nether_sprouts weeping_vines twisting_vines crimson_nylium warped_nylium',herba=5,ignis=2)
add('short_grass tall_grass fern large_fern bush dead_bush red_shrub short_dry_grass tall_dry_grass vine hanging_roots moss_block pale_moss_block pale_hanging_moss big_dripleaf small_dripleaf',herba=5)
add('kelp seagrass sea_pickle',herba=5,aqua=5)
add('sweet_berries glow_berries',herba=5,victus=5)
add('torchflower_seeds pitcher_pod',herba=5,victus=1)
add('honeycomb resin_clump',herba=5,vinculum=2)
add('honey_bottle',aqua=5,victus=5,desiderium=5)
add('ender_pearl',alienis=10,motus=5,praecantatio=5)
add('nether_wart',herba=5,alkimia=5)
add('nether_star',potentia=25,praecantatio=25,desiderium=25)
add('breeze_rod',aer=15,motus=5)
add('heavy_core',metallum=15,terra=15,instrumentum=5)
add('echo_shard',vitreus=5,mortuus=5,sensus=5)
add('sculk sculk_vein sculk_catalyst sculk_shrieker sculk_sensor',mortuus=5,sensus=5,terra=5)
add('phantom_membrane',bestia=5,volatus=5,exanimis=5)
add('turtle_scute armadillo_scute',bestia=5,praemunio=5)
add('nautilus_shell',aqua=5,bestia=5,praemunio=5)
add('heart_of_the_sea',aqua=20,potentia=10,praecantatio=5)
add('salmon tropical_fish pufferfish',bestia=5,victus=5,aqua=5)
add('turtle_egg sniffer_egg frogspawn',victus=5,bestia=5)
add('water_bucket',metallum=12,aqua=15)
add('milk_bucket',metallum=12,aqua=5,victus=5)
add('powder_snow_bucket',metallum=12,gelum=15)
add('goat_horn',bestia=5,sensus=10)
add('bell',metallum=10,sensus=10)
add('trident',aqua=10,aversio=15,metallum=5)
add('trial_key ominous_trial_key',metallum=5,vinculum=5,praecantatio=5)
add('ominous_bottle',vitreus=5,alienis=5,praecantatio=5)
colors='white orange magenta light_blue yellow lime pink gray light_gray cyan purple blue brown green red black'.split()
for name in missing:
 ns,stem=name.split(':')
 if ns!='minecraft':continue
 if stem.endswith('_dye'):add(stem,sensus=5)
 if stem.endswith('_pottery_sherd'):add(stem,terra=5,cognitio=5)
 if stem.endswith('_smithing_template'):add(stem,terra=5,cognitio=5,fabrico=5)
 if stem.startswith('music_disc_'):aliases[name]='minecraft:music_disc_13'
 if stem.endswith('_banner_pattern'):add(stem,cognitio=5,sensus=5)
 if 'coral' in stem:add(stem,aqua=5,bestia=5 if not stem.startswith('dead_') else 1)
 if stem.endswith('_skull') or stem.endswith('_head'):add(stem,mortuus=5,spiritus=5)
 if stem.endswith('_bucket') and stem not in ('water_bucket','milk_bucket','powder_snow_bucket'):add(stem,metallum=12,aqua=5,bestia=5)
 if stem.endswith('_map'):aliases[name]='minecraft:map'
 if stem.endswith('froglight') or stem in ('shroomlight','firefly_bush','glow_lichen'):add(stem,lux=5,herba=5)
 if stem.endswith('flower') or stem in ('dandelion','golden_dandelion','poppy','blue_orchid','allium','azure_bluet','red_tulip','orange_tulip','white_tulip','pink_tulip','oxeye_daisy','cornflower','lily_of_the_valley','wither_rose','pitcher_plant','pink_petals','wildflowers','sunflower','lilac','rose_bush','peony','open_eyeblossom','closed_eyeblossom'):add(stem,herba=5,victus=1,sensus=5)
 if stem.startswith('waxed_') or stem.startswith(('exposed_','weathered_','oxidized_')):
  base=stem.removeprefix('waxed_')
  for prefix in ('exposed_','weathered_','oxidized_'):base=base.removeprefix(prefix)
  if base=='copper':base='copper_block'
  aliases[name]='minecraft:'+base
 if stem.startswith('infested_'):aliases[name]='minecraft:'+stem.removeprefix('infested_')
 if stem.endswith('_bundle'):aliases[name]='minecraft:bundle'
 if stem.endswith('_shulker_box'):aliases[name]='minecraft:shulker_box'
for a,b in {'chipped_anvil':'anvil','damaged_anvil':'anvil','crying_obsidian':'obsidian','suspicious_gravel':'gravel','carved_pumpkin':'pumpkin','blue_egg':'egg','brown_egg':'egg','glow_ink_sac':'ink_sac','filled_map':'map','enchanted_golden_apple':'golden_apple','written_book':'book','moss_carpet':'moss_block','pale_moss_carpet':'pale_moss_block','resin_brick':'resin_clump','shelf_mushroom':'brown_mushroom'}.items():aliases['minecraft:'+a]='minecraft:'+b
# These transformations happen in the world rather than in RecipeManager.
for color in colors:
 aliases['minecraft:'+color+'_concrete']='minecraft:'+color+'_concrete_powder'
 for material in ('wool','carpet','stained_glass','stained_glass_pane','terracotta','glazed_terracotta','concrete_powder','bed','banner','harness','cushion'):
  # Colour never removes the underlying material; dyes remain separately studyable.
  if color!='white':aliases.setdefault('minecraft:'+color+'_'+material,'minecraft:white_'+material)
add('candle',lux=5,herba=2)
for color in colors:aliases['minecraft:'+color+'_candle']='minecraft:candle'
# Native vanilla roots without ordinary production recipes.
add('white_terracotta',terra=15,aqua=5,sensus=1)
add('white_glazed_terracotta',terra=15,ignis=1,sensus=2)
add('white_concrete_powder',terra=10,perditio=5,sensus=1)
add('white_stained_glass',vitreus=5,sensus=1)
add('white_wool',bestia=15,fabrico=5)
# Retain the original catalog's values whenever it already provides an exact definition.
# Restore missing TC6 transformations which are performed by salis, not RecipeManager.
complex_values={}
for item,base,bonus in [
 ('thaumcraft:arcane_workbench','minecraft:crafting_table',{'praecantatio':5}),
 ('thaumcraft:thaumonomicon','minecraft:bookshelf',{'praecantatio':10}),
 ('thaumcraft:research_table','thaumcraft:table_wood',{'cognitio':10}),
 ('thaumcraft:crucible','minecraft:cauldron',{'fabrico':20,'alkimia':20}),
 ('thaumcraft:smelter_basic','minecraft:furnace',{'ignis':10,'alkimia':10}),
 ('thaumcraft:bucket_pure','minecraft:water_bucket',{'auram':5}),
 ('thaumcraft:slab_arcane_brick','minecraft:stone_brick_slab',{'praecantatio':1}),
 ('thaumcraft:pillar_arcane','thaumcraft:stone_arcane',{'ordo':5}),
 ('thaumcraft:pillar_ancient','thaumcraft:stone_ancient',{'ordo':5}),
 ('thaumcraft:pillar_eldritch','thaumcraft:stone_eldritch_tile',{'ordo':5})]:
 aliases[item]=base;complex_values[item]=bonus
add('wet_sponge',terra=5,vinculum=5,aqua=5)
add('spore_blossom',herba=5,victus=1,sensus=5)
add('warped_wart_block',herba=15,alkimia=5)
add('bee_nest',herba=15,bestia=5)
add('gilded_blackstone',terra=5,metallum=5,desiderium=5)
add('copper_golem_statue',metallum=30,machina=5)
add('copper_horse_armor copper_nautilus_armor',metallum=15,praemunio=10,bestia=5)
add('iron_nautilus_armor',metallum=15,praemunio=10,bestia=5)
add('golden_nautilus_armor',metallum=10,praemunio=15,bestia=5)
add('diamond_nautilus_armor',vitreus=15,praemunio=20,bestia=5)
add('netherite_nautilus_armor',metallum=20,praemunio=20,bestia=5,ignis=5)
add('purple_dye',sensus=5)
add('disc_fragment_5',sensus=2,vitreus=1)
add('thaumcraft:primordial_nugget',aer=10,ignis=10,aqua=10,terra=10,ordo=10,perditio=10)
add('thaumcraft:candle_white thaumcraft:candle_black',lux=5,herba=2)
add('thaumcraft:nitor_black thaumcraft:nitor_purple',lux=5,ignis=5,potentia=5)
aliases['thaumcraft:nugget_iron']='minecraft:iron_nugget'
aliases['minecraft:cracked_deepslate_tiles']='minecraft:deepslate_tiles'
for color in 'white orange magenta lightblue yellow lime pink gray silver cyan purple blue brown green red black'.split():aliases['thaumcraft:candle_'+color]='minecraft:candle'
out={'items':dict(sorted(items.items())),'aliases':dict(sorted(aliases.items())),'complex':complex_values}
(root/'src/modern/resources/assets/thaumcraft/knowledge/aspect-supplement.json').write_text(json.dumps(out,indent=2)+'\n')
print('Supplement:',len(items),'definitions,',len(aliases),'equivalences')
