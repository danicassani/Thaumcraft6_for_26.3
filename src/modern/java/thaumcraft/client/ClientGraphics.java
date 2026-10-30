package thaumcraft.client;
import thaumcraft.config.ThaumcraftConfig;
/** Read during state extraction, so deferred rendering retains one consistent setting. */
public final class ClientGraphics {
 private ClientGraphics(){}
 public static boolean advancedPortals(){return !ThaumcraftConfig.CLIENT.isLoaded()||!ThaumcraftConfig.disableShaders.get();}
 public static boolean crookedLabels(){return !ThaumcraftConfig.CLIENT.isLoaded()||ThaumcraftConfig.crooked.get();}
}
