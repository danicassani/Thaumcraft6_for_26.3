package thaumcraft.client;

import com.google.gson.JsonParseException;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.model.geom.builders.UVPair;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.geometry.QuadCollection;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Vector3f;
import org.joml.Vector4f;
import thaumcraft.Thaumcraft;

/** Bakes original triangular meshes without Forge 66.0.13's broken OBJ quad cursor. */
@Mod.EventBusSubscriber(modid = Thaumcraft.MODID, value = Dist.CLIENT)
public final class TriangleModels {
    private TriangleModels() {}

    @SubscribeEvent public static void loaders(ModelEvent.RegisterGeometryLoaders event) {
        event.register(Identifier.fromNamespaceAndPath(Thaumcraft.MODID, "triangles"), (json, context) -> {
            List<float[][]> faces = new ArrayList<>();
            for (var element : json.getAsJsonArray("triangles")) {
                var triangle = element.getAsJsonArray();
                if (triangle.size() != 3) throw new JsonParseException("Expected three triangle vertices");
                float[][] face = new float[3][5];
                for (int i = 0; i < 3; i++) {
                    var vertex = triangle.get(i).getAsJsonArray();
                    if (vertex.size() != 5) throw new JsonParseException("Expected x, y, z, u, v");
                    for (int j = 0; j < 5; j++) {
                        face[i][j] = vertex.get(j).getAsFloat();
                        if (!Float.isFinite(face[i][j])) throw new JsonParseException("Non-finite mesh coordinate");
                    }
                }
                faces.add(face);
            }
            return (slots, baker, state, name) -> {
                var material = baker.materials().resolveSlot(slots, "texture", name);
                var sprite = material.sprite();
                var info = baker.interner().materialInfo(BakedQuad.MaterialInfo.of(material, sprite.transparency(), -1, null, 0));
                var transform = state.transformation().blockCenterToCorner();
                var quads = new QuadCollection.Builder();
                for (var face : faces) {
                    Vector3f[] pos = new Vector3f[3];
                    long[] uv = new long[3];
                    for (int i = 0; i < 3; i++) {
                        var vertex = face[i];
                        var p = new Vector4f(vertex[0], vertex[1], vertex[2], 1);
                        transform.transformPosition(p);
                        pos[i] = new Vector3f(p.x(), p.y(), p.z());
                        uv[i] = UVPair.pack(sprite.getU(vertex[3]), sprite.getV(vertex[4]));
                    }
                    var normal = new Vector3f(pos[1]).sub(pos[0]).cross(new Vector3f(pos[2]).sub(pos[0]));
                    var direction = Direction.getApproximateNearest(normal.x(), normal.y(), normal.z());
                    quads.addUnculledFace(new BakedQuad(pos[0], pos[1], pos[2], pos[2], uv[0], uv[1], uv[2], uv[2], direction, info));
                }
                return quads.build();
            };
        });
    }
}
