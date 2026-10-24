package thaumcraft.client;

import com.google.gson.*;
import java.util.*;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import thaumcraft.Thaumcraft;
import thaumcraft.research.*;

@Mod.EventBusSubscriber(modid=Thaumcraft.MODID,value=Dist.CLIENT)
public final class ResearchScreen extends AbstractContainerScreen<ResearchMenu> {
    private final List<Button> topics=new ArrayList<>(),categories=new ArrayList<>();
    private Button advance,recipeShortcut;
    private int category,listPage,textPage,lastSelected=-1;
    public ResearchScreen(ResearchMenu menu,Inventory inventory,Component title){super(menu,inventory,title,390,226);}
    @SubscribeEvent public static void register(FMLClientSetupEvent e){e.enqueueWork(()->net.minecraft.client.gui.screens.MenuScreens.register(ResearchContent.MENU.get(),ResearchScreen::new));}
    protected void init(){
        super.init();topics.clear();categories.clear();
        for(int i=0;i<7;i++){final int c=i;categories.add(addRenderableWidget(Button.builder(Component.translatable("research.thaumcraft.category."+ResearchCatalog.CATEGORIES.get(i)),b->{category=c;listPage=0;textPage=0;var list=visible();if(!list.isEmpty())minecraft.gameMode.handleInventoryButtonClick(menu.containerId,1000+list.getFirst());}).bounds(leftPos+8+i*53,topPos+19,52,18).build()));}
        for(int i=0;i<7;i++){final int row=i;topics.add(addRenderableWidget(Button.builder(Component.empty(),b->{var list=visible();int index=listPage*7+row;if(index<list.size()){textPage=0;minecraft.gameMode.handleInventoryButtonClick(menu.containerId,1000+list.get(index));}}).bounds(leftPos+8,topPos+44+i*22,124,20).build()));}
        addRenderableWidget(Button.builder(Component.literal("<"),b->listPage=Math.max(0,listPage-1)).bounds(leftPos+8,topPos+204,24,16).build());
        addRenderableWidget(Button.builder(Component.literal(">"),b->{if((listPage+1)*7<visible().size())listPage++;}).bounds(leftPos+108,topPos+204,24,16).build());
        addRenderableWidget(Button.builder(Component.literal("<"),b->textPage=Math.max(0,textPage-1)).bounds(leftPos+144,topPos+184,24,16).build());
        addRenderableWidget(Button.builder(Component.literal(">"),b->{if(textPage+1<pageCount())textPage++;}).bounds(leftPos+354,topPos+184,24,16).build());
        recipeShortcut=addRenderableWidget(Button.builder(Component.translatable("research.thaumcraft.recipe_title"),b->textPage=textPage<textPages()?textPages():0).bounds(leftPos+176,topPos+184,90,16).build());
        advance=addRenderableWidget(Button.builder(Component.translatable("research.thaumcraft.advance"),b->{minecraft.gameMode.handleInventoryButtonClick(menu.containerId,0);textPage=0;}).bounds(leftPos+282,topPos+204,96,16).build());
    }
    private List<Integer> visible(){var result=new ArrayList<Integer>();for(int i=0;i<ResearchCatalog.ENTRIES.size();i++)if(ResearchCatalog.ENTRIES.get(i).category().equals(ResearchCatalog.CATEGORIES.get(category))&&menu.visible(i))result.add(i);result.sort(java.util.Comparator.comparingInt((Integer i)->menu.stage(i)>0&&menu.stage(i)<=ResearchCatalog.ENTRIES.get(i).stages().size()?0:menu.stage(i)>0?1:2).thenComparingInt(i->i));return result;}
    public void containerTick(){
        super.containerTick();if(!menu.categoryUnlocked(category))category=0;int count=0;for(int i=0;i<7;i++)if(menu.categoryUnlocked(i))count++;int width=Math.min(110,371/Math.max(1,count)),position=0;
        for(int i=0;i<7;i++){var b=categories.get(i);b.visible=menu.categoryUnlocked(i);if(b.visible){b.setX(leftPos+8+position++*width);b.setWidth(width-2);b.active=i!=category;}}
        var list=visible();listPage=Math.min(listPage,Math.max(0,(list.size()-1)/7));
        for(int i=0;i<topics.size();i++){var b=topics.get(i);int index=listPage*7+i;b.visible=index<list.size();if(b.visible){int id=list.get(index);var entry=ResearchCatalog.ENTRIES.get(id);b.setMessage(Component.literal(menu.stage(id)>entry.stages().size()?"✓ ":menu.stage(id)>0?"• ":"").append(Component.translatable(entry.name())));}}
        if(lastSelected!=menu.selected()){lastSelected=menu.selected();textPage=0;}advance.active=menu.ready();recipeShortcut.visible=!recipeKeys().isEmpty();textPage=Math.min(textPage,pageCount()-1);
    }
    private ResearchCatalog.Entry entry(){int i=menu.selected();return i>=0&&i<ResearchCatalog.ENTRIES.size()?ResearchCatalog.ENTRIES.get(i):null;}
    private ResearchCatalog.Stage stage(){var e=entry();if(e==null||menu.stage(menu.selected())<=0)return null;return e.stages().get(Math.min(menu.stage(menu.selected()),e.stages().size())-1);}
    private String label(String key){return Component.translatable(key).getString();}
    private String itemName(ResearchCatalog.ItemRequirement r){if(r.legacy().contains("enchanted_placeholder"))return label(r.legacy().contains("id:0s")?"equipment.thaumcraft.protection_requirement":r.legacy().contains("id:33s")?"equipment.thaumcraft.silk_requirement":r.legacy().contains("id:35s")?"equipment.thaumcraft.fortune_requirement":"infusion.thaumcraft.sharpness_requirement");if(r.id().equals("thaumcraft:curio")&&!r.aspect().isEmpty())return label("item.thaumcraft.curio."+r.aspect());var o=new JsonObject();o.addProperty("id",r.id());o.addProperty("aspect",r.aspect());var s=BookRecipes.stack(o);return s.isEmpty()?label("research.thaumcraft.process_page"):s.getHoverName().getString();}
    private String researchName(String key){var e=ResearchCatalog.BY_KEY.get(key.split("@")[0]);if(e!=null)return label(e.name());String flag=key.replaceFirst("^!","");if(key.equals("!gotthaumonomicon"))return label("research.thaumcraft.goal.gotthaumonomicon");if(thaumcraft.crystals.CrystalAspects.COLORS.containsKey(flag))return label("tc.aspect."+flag);return flag.replace('_',' ');}
    private String body(){
        if(!menu.validData())return label("research.thaumcraft.invalid");var e=entry();var page=stage();if(e==null)return "";
        if(page==null)return label("research.thaumcraft.parents")+" "+String.join(", ",e.parents().stream().map(this::researchName).toList());
        var prose=label(page.text()).replaceAll("(?i)<br\\s*/?>","\n\n").replaceAll("<[^>]*>","").replaceAll("§[0-9a-fk-or]","");var text=new StringBuilder();
        if(menu.stage(menu.selected())<=e.stages().size()){int goal=0;
            for(var r:page.crafts())text.append(menu.requirementDone(goal++)?"✓ ":"○ ").append(label("research.thaumcraft.craft")).append(' ').append(itemName(r)).append('\n');
            for(var r:page.research())text.append(menu.requirementDone(goal++)?"✓ ":"○ ").append(label("research.thaumcraft.discover")).append(' ').append(researchName(r)).append('\n');
            for(var r:page.items())text.append(menu.requirementDone(goal++)?"✓ ":"○ ").append(label("research.thaumcraft.deliver")).append(' ').append(r.count()).append(" × ").append(itemName(r)).append('\n');
            for(var r:page.costs())text.append(menu.requirementDone(goal++)?"✓ ":"○ ").append(label("research.thaumcraft.cost")).append(' ').append(r.amount()).append(' ').append(label("research.thaumcraft.points."+r.kind().toLowerCase(Locale.ROOT))).append(" (").append(label("research.thaumcraft.category."+r.category())).append(")\n");
        }return text.append("\n").append(prose).toString();
    }
    private List<String> recipeKeys(){var p=stage();if(p==null)return List.of();var keys=new ArrayList<String>();for(var key:p.recipes())keys.addAll(BookRecipes.expand(key));for(var r:p.crafts())if(BookRecipes.get(r.id())!=null&&!keys.contains(r.id()))keys.add(r.id());return keys;}
    private int textPages(){return Math.max(1,(font.split(Component.literal(body()),232).size()+10)/11);}
    private int pageCount(){return textPages()+recipeKeys().size();}
    protected void extractLabels(GuiGraphicsExtractor g,int x,int y){}
    private void icon(GuiGraphicsExtractor g,ItemStack stack,int x,int y,int mx,int my){if(stack.isEmpty())return;g.fill(x-1,y-1,x+17,y+17,0xFFDDCEB6);g.item(stack,x,y);g.itemDecorations(font,stack,x,y);if(mx>=x&&mx<x+16&&my>=y&&my<y+16)g.setTooltipForNextFrame(font,stack,mx,my);}
    private void recipe(GuiGraphicsExtractor g,String key,int mx,int my){var view=BookRecipes.get(key);if(view==null){int y=topPos+80;for(var l:font.split(Component.translatable("research.thaumcraft.process_page"),220)){g.text(font,l,leftPos+148,y,0xFF302638,false);y+=10;}return;}
        var result=BookRecipes.result(view);g.text(font,font.substrByWidth(result.getHoverName(),225).getString(),leftPos+146,topPos+68,0xFF302638,false);
        String kind=view.get("kind").getAsString();g.text(font,Component.translatable("research.thaumcraft.recipe_kind."+kind),leftPos+146,topPos+80,0xFF715367,false);
        var grid=view.getAsJsonArray("grid");for(int i=0;i<grid.size()&&i<9;i++)icon(g,BookRecipes.stack(grid.get(i)),leftPos+151+(i%3)*20,topPos+98+(i/3)*20,mx,my);
        g.text(font,Component.literal("→"),leftPos+220,topPos+120,0xFF715367,false);icon(g,result,leftPos+243,topPos+118,mx,my);
        if(view.has("input"))icon(g,BookRecipes.stack(view.get("input")),leftPos+219,topPos+98,mx,my);
        if(view.has("vis")&&view.get("vis").getAsInt()>0)g.text(font,Component.translatable("research.thaumcraft.recipe_cost",view.get("vis").getAsInt()),leftPos+278,topPos+100,0xFF715367,false);
        if(view.has("crystals")){int i=0;var crystals=view.getAsJsonObject("crystals");for(var aspect:thaumcraft.crafting.ArcaneRecipe.PRIMALS)if(crystals.has(aspect))icon(g,thaumcraft.crystals.CrystalContent.essence(aspect,crystals.get(aspect).getAsInt()),leftPos+244+(i%6)*21,topPos+153+(i++/6)*20,mx,my);}
        if(view.has("aspects")){int i=0;for(var cost:view.getAsJsonObject("aspects").entrySet()){int xx=leftPos+244+(i%4)*32,yy=topPos+145+(i++/4)*19;if(i>8)break;icon(g,thaumcraft.crystals.CrystalContent.essence(cost.getKey(),1),xx,yy,mx,my);g.text(font,Integer.toString(cost.getValue().getAsInt()),xx+15,yy+6,0xFF302638,false);}}
        if(kind.equals("salis_mundus"))g.text(font,font.substrByWidth(Component.translatable("research.thaumcraft.recipe_returned"),225).getString(),leftPos+146,topPos+169,0xFF715367,false);
    }
    public void extractBackground(GuiGraphicsExtractor g,int mx,int my,float partial){
        super.extractBackground(g,mx,my,partial);g.fill(leftPos,topPos,leftPos+imageWidth,topPos+imageHeight,0xFF37273E);g.fill(leftPos+138,topPos+40,leftPos+382,topPos+202,0xFFF1E4C9);g.text(font,title,leftPos+8,topPos+6,0xFFFFE9BA,false);
        var e=entry();if(e!=null){int s=menu.stage(menu.selected());g.text(font,font.substrByWidth(Component.translatable(e.name()),232).getString(),leftPos+144,topPos+44,0xFF38293E,false);g.text(font,Component.translatable("research.thaumcraft.stage",Math.min(s,e.stages().size()),e.stages().size()),leftPos+144,topPos+55,0xFF715367,false);}
        int texts=textPages();if(textPage>=texts){var keys=recipeKeys();int index=textPage-texts;if(index<keys.size())recipe(g,keys.get(index),mx,my);}else{var lines=font.split(Component.literal(body()),232);for(int i=0,start=textPage*11;i<11&&start+i<lines.size();i++)g.text(font,lines.get(start+i),leftPos+144,topPos+68+i*10,0xFF302638,false);}
        g.text(font,Component.literal((textPage+1)+" / "+pageCount()),leftPos+290,topPos+188,0xFF38293E,false);g.text(font,Component.literal((listPage+1)+" / "+Math.max(1,(visible().size()+6)/7)),leftPos+52,topPos+208,0xFFFFE9BA,false);
        g.text(font,Component.translatable("research.thaumcraft.points_short",menu.points(false,category)/16,menu.points(true,category)/32),leftPos+144,topPos+208,0xFFFFE9BA,false);if(mx>=leftPos+144&&mx<leftPos+278&&my>=topPos+204&&my<topPos+222)g.setTooltipForNextFrame(Component.translatable("research.thaumcraft.points_progress",menu.points(false,category)%16*100/16,menu.points(true,category)%32*100/32),mx,my);g.text(font,Component.translatable("research.thaumcraft.scans",menu.scans()),leftPos+236,topPos+6,0xFFFFE9BA,false);
    }
}
