package thaumcraft.research;
import java.util.*;
import com.google.gson.*;
/** Original atlas coordinates, frame metadata and translated icon references. */
public final class ResearchAtlas {
 public record Topic(String key,float x,float y,Set<String> meta,List<JsonObject> icons){}
 public static final Map<String,Topic> TOPICS;
 static {var map=new LinkedHashMap<String,Topic>();for(var entry:ResearchCatalog.load("research_visuals.json").getAsJsonObject().entrySet()){var j=entry.getValue().getAsJsonObject();var icons=new ArrayList<JsonObject>();for(var i:j.getAsJsonArray("icons"))icons.add(i.getAsJsonObject());map.put(entry.getKey(),new Topic(entry.getKey(),j.get("x").getAsFloat(),j.get("y").getAsFloat(),Set.copyOf(ResearchCatalog.strings(j,"meta")),List.copyOf(icons)));}TOPICS=Map.copyOf(map);}
 /** Zoom about the pointer, with reversible world/screen transforms. */
 public static final class Camera {
  private double x,y,zoom=1;
  public double x(){return x;}public double y(){return y;}public double zoom(){return zoom;}
  public double screenX(double world){return x+world*zoom;}public double screenY(double world){return y+world*zoom;}
  public double worldX(double screen){return (screen-x)/zoom;}public double worldY(double screen){return (screen-y)/zoom;}
  public void center(double wx,double wy,double width,double height){x=width/2-wx*zoom;y=height/2-wy*zoom;}
  public void pan(double dx,double dy){if(Double.isFinite(dx)&&Double.isFinite(dy)){x=Math.clamp(x+dx,-8192,8192);y=Math.clamp(y+dy,-8192,8192);}}
  public void zoom(double delta,double sx,double sy){if(!Double.isFinite(delta)||!Double.isFinite(sx)||!Double.isFinite(sy))return;double wx=worldX(sx),wy=worldY(sy);zoom=Math.clamp(zoom+delta*.1,.5,2);x=sx-wx*zoom;y=sy-wy*zoom;}
 }
 private ResearchAtlas(){}
}
