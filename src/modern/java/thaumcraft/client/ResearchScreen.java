package thaumcraft.client;

import com.google.gson.*;
import java.util.*;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import thaumcraft.Thaumcraft;
import thaumcraft.research.*;

@Mod.EventBusSubscriber(modid=Thaumcraft.MODID,value=Dist.CLIENT)
public final class ResearchScreen extends AbstractContainerScreen<ResearchMenu> {
    private final List<Button> topics=new ArrayList<>();
    private Button advance;
    private int category,listPage,textPage,lastSelected=-1;
    private static final JsonObject RECIPES=ResearchCatalog.load("recipes.json").getAsJsonObject();
    public ResearchScreen(ResearchMenu menu,Inventory inventory,Component title){super(menu,inventory,title,390,226);}
    @SubscribeEvent public static void register(FMLClientSetupEvent event){event.enqueueWork(()->net.minecraft.client.gui.screens.MenuScreens.register(ResearchContent.MENU.get(),ResearchScreen::new));}
    @Override protected void init(){
        super.init();topics.clear();
        for(int i=0;i<7;i++){final int c=i;addRenderableWidget(Button.builder(Component.translatable("research.thaumcraft.category."+ResearchCatalog.CATEGORIES.get(i)),b->{category=c;listPage=0;}).bounds(leftPos+8+i*53,topPos+19,52,18).build());}
        for(int i=0;i<7;i++){final int row=i;topics.add(addRenderableWidget(Button.builder(Component.empty(),b->{var list=visible();int index=listPage*7+row;if(index<list.size()){textPage=0;minecraft.gameMode.handleInventoryButtonClick(menu.containerId,1000+list.get(index));}}).bounds(leftPos+8,topPos+44+i*22,124,20).build()));}
        addRenderableWidget(Button.builder(Component.literal("<"),b->{listPage=Math.max(0,listPage-1);}).bounds(leftPos+8,topPos+204,24,16).build());
        addRenderableWidget(Button.builder(Component.literal(">"),b->{if((listPage+1)*7<visible().size())listPage++;}).bounds(leftPos+108,topPos+204,24,16).build());
        addRenderableWidget(Button.builder(Component.literal("<"),b->{textPage=Math.max(0,textPage-1);}).bounds(leftPos+144,topPos+184,24,16).build());
        addRenderableWidget(Button.builder(Component.literal(">"),b->{int size=font.split(Component.literal(body()),232).size();if((textPage+1)*11<size)textPage++;}).bounds(leftPos+354,topPos+184,24,16).build());
        advance=addRenderableWidget(Button.builder(Component.translatable("research.thaumcraft.advance"),b->{minecraft.gameMode.handleInventoryButtonClick(menu.containerId,0);textPage=0;}).bounds(leftPos+282,topPos+204,96,16).build());
    }
    private List<Integer> visible(){var result=new ArrayList<Integer>();for(int i=0;i<ResearchCatalog.ENTRIES.size();i++)if(ResearchCatalog.ENTRIES.get(i).category().equals(ResearchCatalog.CATEGORIES.get(category))&&menu.visible(i))result.add(i);return result;}
    @Override public void containerTick(){
        super.containerTick();var list=visible();listPage=Math.min(listPage,Math.max(0,(list.size()-1)/7));
        for(int i=0;i<topics.size();i++){var b=topics.get(i);int index=listPage*7+i;b.visible=index<list.size();if(b.visible){int id=list.get(index);var entry=ResearchCatalog.ENTRIES.get(id);b.setMessage(Component.literal(menu.stage(id)>entry.stages().size()?"✓ ":menu.stage(id)>0?"• ":"").append(Component.translatable(entry.name())));}}
        if(lastSelected!=menu.selected()){lastSelected=menu.selected();textPage=0;}advance.active=menu.ready();
    }
    private String label(String key){return Component.translatable(key).getString();}
    private String body(){
        if(!menu.validData())return label("research.thaumcraft.invalid");
        int selected=menu.selected();if(selected<0||selected>=ResearchCatalog.ENTRIES.size())return "";
        var entry=ResearchCatalog.ENTRIES.get(selected);int stage=menu.stage(selected);var text=new StringBuilder();
        if(stage<=0){text.append(label("research.thaumcraft.parents")).append(' ');for(var parent:entry.parents()){var e=ResearchCatalog.BY_KEY.get(parent.split("@")[0]);text.append(e==null?parent:label(e.name())).append(' ');}return text.toString();}
        var page=entry.stages().get(Math.min(stage,entry.stages().size())-1);
        String prose=label(page.text()).replaceAll("(?i)<br\\s*/?>","\n\n").replaceAll("<[^>]*>","").replaceAll("§[0-9a-fk-or]","");text.append(prose).append("\n\n");
        if(stage<=entry.stages().size()){
            for(var item:page.items())text.append(label("research.thaumcraft.deliver")).append(' ').append(item.count()).append(" × ").append(item.legacy().contains("enchanted_placeholder")?label(item.legacy().contains("id:0s")?"equipment.thaumcraft.protection_requirement":item.legacy().contains("id:33s")?"equipment.thaumcraft.silk_requirement":item.legacy().contains("id:35s")?"equipment.thaumcraft.fortune_requirement":"infusion.thaumcraft.sharpness_requirement"):item.id()).append(item.aspect().isEmpty()?"":" ("+label((item.id().equals("thaumcraft:curio")?"item.thaumcraft.curio.":"tc.aspect.")+item.aspect())+")").append('\n');
            for(var item:page.crafts())text.append(label("research.thaumcraft.craft")).append(' ').append(item.id()).append('\n');
            for(var research:page.research())text.append(label("research.thaumcraft.discover")).append(' ').append(research).append('\n');
            for(var cost:page.costs())text.append(label("research.thaumcraft.cost")).append(' ').append(cost.amount()).append(' ').append(cost.kind()).append(' ').append(cost.category()).append('\n');
        }
        for(var recipe:page.recipes()){
            text.append('\n').append(label("research.thaumcraft.recipe")).append(' ').append(recipe).append('\n');
            if(RECIPES.has(recipe)){for(var line:RECIPES.getAsJsonArray(recipe))text.append(line.getAsString()).append('\n');}
            else text.append(label("research.thaumcraft.future_recipe")).append('\n');
        }
        return text.toString();
    }
    @Override protected void extractLabels(GuiGraphicsExtractor graphics,int x,int y){}
    @Override public void extractBackground(GuiGraphicsExtractor graphics,int x,int y,float partial){
        super.extractBackground(graphics,x,y,partial);graphics.fill(leftPos,topPos,leftPos+imageWidth,topPos+imageHeight,0xFF37273E);graphics.fill(leftPos+138,topPos+40,leftPos+382,topPos+202,0xFFF1E4C9);
        graphics.text(font,title,leftPos+8,topPos+6,0xFFFFE9BA,false);
        int index=menu.selected();if(index>=0&&index<ResearchCatalog.ENTRIES.size()){
            var entry=ResearchCatalog.ENTRIES.get(index);int stage=menu.stage(index);graphics.text(font,Component.translatable(entry.name()),leftPos+144,topPos+44,0xFF38293E,false);
            graphics.text(font,Component.translatable("research.thaumcraft.stage",Math.min(stage,entry.stages().size()),entry.stages().size()),leftPos+144,topPos+55,0xFF715367,false);
        }
        var lines=font.split(Component.literal(body()),232);int start=Math.min(textPage*11,Math.max(0,lines.size()-1));for(int i=0;i<11&&start+i<lines.size();i++)graphics.text(font,lines.get(start+i),leftPos+144,topPos+68+i*10,0xFF302638,false);
        graphics.text(font,Component.literal((textPage+1)+" / "+Math.max(1,(lines.size()+10)/11)),leftPos+246,topPos+188,0xFF38293E,false);
        graphics.text(font,Component.literal((listPage+1)+" / "+Math.max(1,(visible().size()+6)/7)),leftPos+52,topPos+208,0xFFFFE9BA,false);
        graphics.text(font,Component.literal("O: "+menu.points(false,category)+"/16  T: "+menu.points(true,category)+"/32"),leftPos+144,topPos+208,0xFFFFE9BA,false);
        graphics.text(font,Component.translatable("research.thaumcraft.scans",menu.scans()),leftPos+236,topPos+6,0xFFFFE9BA,false);
    }
}
