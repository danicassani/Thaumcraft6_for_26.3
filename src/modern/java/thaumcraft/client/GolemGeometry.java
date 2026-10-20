package thaumcraft.client;
import java.util.*;
import com.google.gson.*;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
/** Immutable meshes rebuilt from the active resource pack on renderer reload. */
final class GolemGeometry {
 static Map<String,Map<String,float[][][]>> load(ResourceManager manager){var meshes=new HashMap<String,Map<String,float[][][]>>();try(var reader=manager.openAsReader(Identifier.fromNamespaceAndPath("thaumcraft","models/golems/parts.json"))){var root=JsonParser.parseReader(reader).getAsJsonObject();for(var model:root.entrySet()){var groups=new LinkedHashMap<String,float[][][]>();for(var group:model.getValue().getAsJsonObject().entrySet()){var values=group.getValue().getAsJsonArray();float[][][] triangles=new float[values.size()][3][8];for(int i=0;i<values.size();i++)for(int j=0;j<3;j++)for(int k=0;k<8;k++)triangles[i][j][k]=values.get(i).getAsJsonArray().get(j).getAsJsonArray().get(k).getAsFloat();groups.put(group.getKey(),triangles);}meshes.put(model.getKey(),groups);}}catch(java.io.IOException e){throw new IllegalStateException("Missing original golem meshes",e);}return meshes;}
 static void draw(float[][][] triangles,PoseStack pose,SubmitNodeCollector collector,Identifier texture,int light,int color){if(triangles==null)return;collector.submitCustomGeometry(pose,RenderTypes.entityCutout(texture),(transform,out)->{for(var triangle:triangles)for(int i:new int[]{0,1,2,2}){var v=triangle[i];out.addVertex(transform.pose(),v[0],v[1],v[2]).setColor(color).setUv(v[3],v[4]).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(transform,v[5],v[6],v[7]);}});}
}
