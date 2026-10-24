package thaumcraft.client;
import java.util.*;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import thaumcraft.Thaumcraft;
import thaumcraft.thaumaturgy.*;

/** Native container with a pannable, zoomable graph. Every edit is an authoritative menu action. */
@Mod.EventBusSubscriber(modid=Thaumcraft.MODID,value=Dist.CLIENT)
public final class FocalScreen extends AbstractContainerScreen<FocalMenu> {
 private final Button[] buttons=new Button[25];private final List<Icon> graphButtons=new ArrayList<>(),palette=new ArrayList<>();
 private List<FocusCanvas.Node> nodes=List.of();private FocusProgram drawn;private int kind,lastSelection=-2;private float zoom=1,panX=12,panY=24;private boolean dragging;
 public FocalScreen(FocalMenu menu,Inventory inv,Component title){super(menu,inv,title,360,228);inventoryLabelX=100;inventoryLabelY=134;}
 @SubscribeEvent public static void screens(FMLClientSetupEvent e){e.enqueueWork(()->{net.minecraft.client.gui.screens.MenuScreens.register(ThaumaturgyContent.MENU.get(),FocalScreen::new);net.minecraft.client.gui.screens.MenuScreens.register(ThaumaturgyContent.POUCH_MENU.get(),PouchScreen::new);});}
 @SubscribeEvent public static void renderers(net.minecraftforge.client.event.EntityRenderersEvent.RegisterRenderers e){e.registerEntityRenderer(ThaumaturgyContent.TURRET_ARROW.get(),net.minecraft.client.renderer.entity.TippableArrowRenderer::new);e.registerEntityRenderer(ThaumaturgyContent.GRAPPLE.get(),net.minecraft.client.renderer.entity.ThrownItemRenderer::new);e.registerEntityRenderer(ThaumaturgyContent.COLLAPSER.get(),net.minecraft.client.renderer.entity.ThrownItemRenderer::new);e.registerEntityRenderer(ThaumaturgyContent.PROJECTILE.get(),net.minecraft.client.renderer.entity.ThrownItemRenderer::new);e.registerEntityRenderer(ThaumaturgyContent.SPELLBAT.get(),net.minecraft.client.renderer.entity.BatRenderer::new);e.registerEntityRenderer(ThaumaturgyContent.FIELD.get(),net.minecraft.client.renderer.entity.NoopRenderer::new);}
 private static net.minecraft.network.chat.MutableComponent label(String key){return Component.translatable("focal.thaumcraft."+key);}
 private void send(int action){minecraft.gameMode.handleInventoryButtonClick(menu.containerId,action);}
 protected void init(){super.init();graphButtons.clear();palette.clear();drawn=null;lastSelection=-2;
  String[] names={"craft","medium","effect","power","duration","add","remove","modifier","branch","forks","cone","radius","seconds","target","plan_x","plan_y","plan_z","plan_mode","speed","option","fortune","silk","graph_enter","graph_back","graph_remove"};
  for(int i=0;i<25;i++){final int action=i;buttons[i]=addRenderableWidget(Button.builder(label(names[i]),b->send(action)).bounds(leftPos+232,topPos+78,58,14).build());buttons[i].setTooltip(Tooltip.create(label(names[i])));}
  buttons[0].setX(leftPos+258);buttons[0].setY(topPos+112);buttons[0].setWidth(94);buttons[0].setHeight(16);
  int n=0;for(int action:new int[]{5,6,22,24}){buttons[action].setX(leftPos+112+n++*28);buttons[action].setY(topPos+3);buttons[action].setWidth(24);buttons[action].setHeight(14);}
  buttons[5].setMessage(Component.literal("E+"));buttons[6].setMessage(Component.literal("E−"));buttons[24].setMessage(Component.literal("→−"));
  for(int k=0;k<3;k++){final int mode=k;addRenderableWidget(Button.builder(Component.literal(k==0?"M":k==1?"E":"+"),b->{kind=mode;refreshPalette();containerTick();}).bounds(leftPos+232+k*40,topPos+3,38,14).build()).setTooltip(Tooltip.create(label(k==0?"medium":k==1?"effect":"modifier")));}
  addRenderableWidget(Button.builder(Component.literal("↺"),b->center()).bounds(leftPos+88,topPos+3,18,14).build()).setTooltip(Tooltip.create(label("canvas_center")));
  refreshPalette();containerTick();
 }
 private void refreshPalette(){for(var b:palette)removeWidget(b);palette.clear();var choices=kind==0?FocusProgram.MEDIA:kind==1?FocusProgram.EFFECTS:FocusProgram.MODIFIERS;int base=kind==0?80:kind==1?88:100;
  for(int i=0;i<choices.size();i++){String name=choices.get(i);final int action=base+i;var b=new Icon(leftPos+232+i%6*20,topPos+36+i/6*20,18,name,button->send(action));palette.add(addRenderableWidget(b));}
 }
 private void rebuild(){for(var b:graphButtons)removeWidget(b);graphButtons.clear();nodes=FocusCanvas.nodes(menu.program());for(int i=0;i<nodes.size();i++){final int index=i;var n=nodes.get(i);graphButtons.add(addRenderableWidget(new Icon(0,0,24,n.name(),b->send(FocusCanvas.FIRST_SELECT+index))));}drawn=menu.program();positionNodes();}
 private void positionNodes(){for(int i=0;i<graphButtons.size();i++){var b=graphButtons.get(i);var n=nodes.get(i);int size=Math.max(12,Math.round(24*zoom)),x=leftPos+8+Math.round(panX+n.x()*zoom),y=topPos+20+Math.round(panY+n.y()*zoom);b.setX(x);b.setY(y);b.setWidth(size);b.setHeight(size);b.visible=x>=leftPos+8&&x+size<=leftPos+224&&y>=topPos+20&&y+size<=topPos+104;b.active=!menu.working();b.selected=menu.value(29)==i;}}
 public int[] nodeCenter(int index){var b=graphButtons.get(index);return new int[]{b.getX()+b.getWidth()/2,b.getY()+b.getHeight()/2};}
 public int nodeCount(){return nodes.size();}
 public void center(){if(nodes.isEmpty())return;int w=nodes.stream().mapToInt(FocusCanvas.Node::x).max().orElse(0)+24,h=nodes.stream().mapToInt(FocusCanvas.Node::y).max().orElse(0)+24;zoom=Math.max(.5F,Math.min(1F,Math.min(192F/w,64F/h)));panX=(216-w*zoom)/2;panY=(84-h*zoom)/2;positionNodes();}
 private FocusProgram.Branch path(){var selected=menu.value(29);if(selected<0||selected>=nodes.size())return menu.program().root();var n=nodes.get(selected);var p=menu.program().at(n.address());return p==null?menu.program().root():n.branch()==0?p.root():p.branches().get(n.branch()-1);}
 public void containerTick(){super.containerTick();if(buttons[0]==null)return;if(!menu.program().equals(drawn))rebuild();if(menu.value(29)!=lastSelection){kind=Math.clamp(menu.value(28),0,2);lastSelection=menu.value(29);refreshPalette();}
  for(var b:buttons){b.visible=false;b.active=!menu.working();}for(int i:new int[]{0,5,6,22,24})buttons[i].visible=true;var path=path();
  int[] props=kind==1?new int[]{3,4,20,21}:kind==2?new int[]{9,10}:switch(menu.value(3)){case 2->new int[]{18,19};case 3->new int[]{11,12};case 4,6->new int[]{13};case 5->new int[]{14,15,16,17};default->new int[]{};};
  for(int i=0;i<props.length;i++){int action=props[i];var b=buttons[action];b.visible=kind!=1||!path.effects().isEmpty();if(kind==1&&action>=20)b.visible&=menu.value(4)==6||menu.value(4)==7;if(kind==2)b.visible&=menu.value(11)==1;b.setX(leftPos+232+i%2*62);b.setY(topPos+78+i/2*16);}
  buttons[0].active&=menu.value(30)==0;buttons[0].setTooltip(null);buttons[5].active&=menu.value(8)<FocusProgram.MAX_EFFECTS;buttons[6].active&=!path.effects().isEmpty()&&(path.effects().size()>1||!path.next().isEmpty());buttons[22].active&=menu.value(26)==1||menu.value(25)<FocusProgram.MAX_DEPTH-1&&menu.program().graphNodes().size()<8;buttons[24].active&=menu.value(26)==1&&!path.effects().isEmpty();buttons[22].setMessage(Component.literal(menu.value(26)==1?"→":"→+"));
  buttons[3].setMessage(Component.translatable("focal.thaumcraft.canvas_"+(menu.value(4)==9?"depth":"power"),menu.value(5)));buttons[4].setMessage(Component.translatable("focal.thaumcraft.canvas_duration",menu.value(6)));
  for(int i:new int[]{9,10,11,12,20})buttons[i].setMessage(label(i==9?"forks":i==10?"cone":i==11?"radius":i==12?"seconds":"fortune").append(": "+menu.value(i==9?12:i==10?13:i==11?14:i==12?15:23)));
  buttons[13].setMessage(label(menu.value(16)==1?"friend":"enemy"));for(int i=14;i<17;i++)buttons[i].setMessage(Component.literal("XYZ".charAt(i-14)+": "+menu.value(i+3)));buttons[17].setMessage(label(menu.value(20)==1?"plan_surface":"plan_full"));buttons[15].active&=menu.value(20)==0;
  buttons[18].setMessage(Component.literal("V: "+String.format(Locale.ROOT,"%.2f",new FocusProgram.ProjectileSettings(menu.value(21),0).velocity())));buttons[19].setMessage(label("projectile_"+menu.value(22)));buttons[21].setMessage(label(menu.value(24)==1?"silk_yes":"silk_no"));
  for(var b:palette){b.active=!menu.working();int index=palette.indexOf(b);b.selected=index==(kind==0?menu.value(3):kind==1?menu.value(4):menu.value(11));if(kind==0&&menu.value(10)==1&&menu.value(11)==2)b.active=false;}
  positionNodes();
 }
 protected void extractLabels(GuiGraphicsExtractor g,int mx,int my){}
 public void extractBackground(GuiGraphicsExtractor g,int mx,int my,float partial){super.extractBackground(g,mx,my,partial);g.fill(leftPos,topPos,leftPos+360,topPos+228,0xffb9afcc);g.fill(leftPos+7,topPos+19,leftPos+225,topPos+105,0xff211b30);g.text(font,label("canvas_title"),leftPos+8,topPos+7,0xff30243c,false);g.text(font,label(kind==0?"medium":kind==1?"effect":"modifier"),leftPos+232,topPos+23,0xff30243c,false);
  g.enableScissor(leftPos+8,topPos+20,leftPos+224,topPos+104);for(var n:nodes)if(n.parent()>=0){var p=nodes.get(n.parent());int x1=leftPos+8+Math.round(panX+(p.x()+24)*zoom),y1=topPos+20+Math.round(panY+(p.y()+12)*zoom),x2=leftPos+8+Math.round(panX+n.x()*zoom),y2=topPos+20+Math.round(panY+(n.y()+12)*zoom),middle=(x1+x2)/2;g.fill(Math.min(x1,middle),y1,Math.max(x1,middle)+1,y1+1,0xff937ab5);g.fill(middle,Math.min(y1,y2),middle+1,Math.max(y1,y2)+1,0xff937ab5);g.fill(Math.min(middle,x2),y2,Math.max(middle,x2)+1,y2+1,0xff937ab5);}g.disableScissor();
  for(var slot:menu.slots)if(slot.x>=0){g.fill(leftPos+slot.x-1,topPos+slot.y-1,leftPos+slot.x+17,topPos+slot.y+17,0xff17141f);g.fill(leftPos+slot.x,topPos+slot.y,leftPos+slot.x+16,topPos+slot.y+16,0xff7d718f);}
  g.text(font,Component.translatable("focal.thaumcraft.complexity",menu.value(7),menu.value(9),menu.value(8)),leftPos+8,topPos+134,menu.value(7)>menu.value(9)?0xff8b2430:0xff30243c,false);
  g.text(font,Component.translatable("focal.thaumcraft.canvas_levels",menu.program().levels()),leftPos+8,topPos+153,0xff30243c,false);g.text(font,Component.literal("Vis: "+menu.program().craftingVis(menu.value(9))),leftPos+8,topPos+165,0xff30243c,false);
  if(menu.working())g.text(font,Component.translatable("focal.thaumcraft.progress",menu.value(2)-menu.value(1),menu.value(2)),leftPos+8,topPos+184,0xff30243c,false);
  if(menu.value(30)>0&&menu.value(30)<6){int y=topPos+182;for(var line:font.split(label("canvas_issue_"+menu.value(30)),86)){g.text(font,line,leftPos+8,y,0xff8b2430,false);y+=9;}}
  if(mx>=buttons[0].getX()&&mx<buttons[0].getX()+buttons[0].getWidth()&&my>=buttons[0].getY()&&my<buttons[0].getY()+buttons[0].getHeight()){var lines=new ArrayList<Component>();if(menu.value(30)!=0)lines.add(label("canvas_issue_"+menu.value(30)));lines.add(Component.translatable("focal.thaumcraft.canvas_levels",menu.program().levels()));lines.add(Component.literal("Vis: "+menu.program().craftingVis(menu.value(9))));for(var cost:new TreeMap<>(menu.program().crystals()).entrySet())lines.add(Component.translatable("tc.aspect."+cost.getKey()).append(" × "+cost.getValue()));g.setComponentTooltipForNextFrame(font,lines,mx,my);}
  if(inside(mx,my)&&!hoverNode(mx,my))g.setTooltipForNextFrame(font,label("canvas_help"),mx,my);
 }
 private boolean inside(double x,double y){return x>=leftPos+8&&x<leftPos+224&&y>=topPos+20&&y<topPos+104;}
 private boolean hoverNode(double x,double y){return graphButtons.stream().anyMatch(b->b.visible&&b.isMouseOver(x,y));}
 public boolean mouseClicked(MouseButtonEvent e,boolean doubleClick){if(inside(e.x(),e.y())&&e.button()==InputConstants.MOUSE_BUTTON_LEFT){for(int i=0;i<graphButtons.size();i++){var node=graphButtons.get(i);if(node.visible&&node.active&&node.isMouseOver(e.x(),e.y())){send(FocusCanvas.FIRST_SELECT+i);return true;}}}if(inside(e.x(),e.y())&&(e.button()==InputConstants.MOUSE_BUTTON_RIGHT||e.button()==InputConstants.MOUSE_BUTTON_LEFT&&!hoverNode(e.x(),e.y()))){dragging=true;return true;}return super.mouseClicked(e,doubleClick);}
 public boolean mouseDragged(MouseButtonEvent e,double dx,double dy){if(dragging){panX+=dx;panY+=dy;positionNodes();return true;}return super.mouseDragged(e,dx,dy);}
 public boolean mouseReleased(MouseButtonEvent e){if(dragging){dragging=false;return true;}return super.mouseReleased(e);}
 public boolean mouseScrolled(double x,double y,double horizontal,double vertical){if(inside(x,y)){float old=zoom;zoom=Math.clamp(zoom+(float)vertical*.1F,.5F,1.75F);float localX=(float)x-leftPos-8,localY=(float)y-topPos-20;panX=localX-(localX-panX)*zoom/old;panY=localY-(localY-panY)*zoom/old;positionNodes();return true;}return super.mouseScrolled(x,y,horizontal,vertical);}
 private static final class Icon extends Button {
  final String name;boolean selected;
  Icon(int x,int y,int size,String name,OnPress press){super(x,y,size,size,Component.translatable(name.equals("impact")?"focal.thaumcraft.canvas_impact":"focus.thaumcraft."+name),press,DEFAULT_NARRATION);this.name=name;setTooltip(Tooltip.create(getMessage()));}
  protected void extractContents(GuiGraphicsExtractor g,int mx,int my,float partial){g.fill(getX(),getY(),getX()+width,getY()+height,selected?0xfff0cf70:isHovered?0xffcfb9eb:0xff655278);g.fill(getX()+1,getY()+1,getX()+width-1,getY()+height-1,active?0xff30253f:0xff4d4555);String icon=name.equals("none")||name.equals("impact")?"root":name;g.blit(RenderPipelines.GUI_TEXTURED,Identifier.parse("thaumcraft:textures/foci/"+icon+".png"),getX()+2,getY()+2,0,0,width-4,height-4,32,32,32,32);}
 }
}
