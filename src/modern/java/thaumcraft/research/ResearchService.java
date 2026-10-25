package thaumcraft.research;

import java.util.*;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import thaumcraft.config.ThaumcraftConfig;
import thaumcraft.persistence.PlayerWarpStore;

public final class ResearchService {
    private ResearchService(){}
    public static KnowledgeState state(Player player){return KnowledgeStore.read(player).result().orElse(null);}
    public static boolean knows(Player player,String key){if(key==null||key.isEmpty())return true;var state=state(player);return state!=null&&ResearchCatalog.knows(state,key);}
    public static boolean flag(Player player,String key){var state=state(player);return state!=null&&(state.flags().contains(key)||KnowledgeStore.write(player,state.flag(key)));}
    public static void recordCraft(Player player,ItemStack stack){if(!(player.level() instanceof ServerLevel)||stack.isEmpty())return;var state=state(player);if(state==null)return;String key=ResearchCatalog.proof(stack);if(!state.crafts().contains(key))KnowledgeStore.write(player,state.craft(key));}
    public static boolean hasBook(Player player){for(int i=0;i<player.getInventory().getContainerSize();i++)if(player.getInventory().getItem(i).is(ResearchContent.BOOK.get()))return true;return false;}
    public static boolean parents(KnowledgeState state,ResearchCatalog.Entry entry){return entry.parents().stream().allMatch(key->ResearchCatalog.knows(state,key));}
    private static int[] itemPlan(Player player,ResearchCatalog.Stage stage){
        var inventory=player.getInventory();int[] remaining=new int[inventory.getContainerSize()],take=new int[remaining.length];
        for(int i=0;i<remaining.length;i++)remaining[i]=inventory.getItem(i).getCount();
        var required=new ArrayList<>(stage.items());required.sort(Comparator.comparing(r->r.aspect().isEmpty()));
        for(var requirement:required){int needed=requirement.count();for(int i=0;i<remaining.length&&needed>0;i++)if(requirement.matches(inventory.getItem(i))){int n=Math.min(remaining[i],needed);remaining[i]-=n;take[i]+=n;needed-=n;}if(needed>0)return null;}
        return take;
    }
    public static boolean canAdvance(Player player,ResearchCatalog.Entry entry){
        var state=state(player);if(state==null||ResearchCatalog.knows(state,entry.key())||!parents(state,entry))return false;
        if(entry.key().startsWith("!")||entry.hidden()&&entry.parents().isEmpty()&&!state.stages().containsKey(entry.key())&&!state.flags().contains(entry.key()))return false;
        int current=state.stages().getOrDefault(entry.key(),0);if(current>entry.stages().size())return false;
        if(current==0)return true;
        var stage=entry.stages().get(current-1);
        if(stage.crafts().stream().anyMatch(r->!state.crafts().contains(r.proof()))||stage.research().stream().anyMatch(r->!ResearchCatalog.knows(state,r)))return false;
        var cost=new HashMap<String,Integer>();for(var c:stage.costs())cost.merge(c.kind()+":"+c.category(),c.amount()*c.progression(),Integer::sum);
        for(var c:cost.entrySet())if(state.points().getOrDefault(c.getKey(),0)<c.getValue())return false;
        return itemPlan(player,stage)!=null;
    }
    public static boolean advance(Player player,String key){
        if(!(player.level() instanceof ServerLevel server)||!server.getServer().isSameThread())return false;
        var entry=ResearchCatalog.BY_KEY.get(key);if(entry==null||!canAdvance(player,entry))return false;
        var old=state(player);int current=old.stages().getOrDefault(key,0);var next=old;int[] take=new int[player.getInventory().getContainerSize()];int warp=0;
        if(current>0){var stage=entry.stages().get(current-1);take=itemPlan(player,stage);if(take==null)return false;warp+=stage.warp();for(var c:stage.costs())next=next.addPoints(c.kind(),c.category(),-c.amount()*c.progression());}
        int target=current+1;
        // A final page with no requirements is already complete, as in TC6.
        if(target==entry.stages().size()&&entry.stages().get(target-1).free()){warp+=entry.stages().get(target-1).warp();target++;}
        next=next.stage(key,target);
        for(var sibling:entry.siblings()){
            var definition=ResearchCatalog.BY_KEY.get(sibling);
            if(definition==null)next=next.flag(sibling);
            else if(definition.stages().size()==1&&definition.stages().getFirst().free()&&parents(next,definition))next=next.stage(sibling,2);
        }
        PlayerWarpStore.State warpState=null;
        if(warp>0&&!ThaumcraftConfig.wussMode.get()&&player instanceof net.minecraft.server.level.ServerPlayer real){
            var saved=PlayerWarpStore.read(real).result();if(saved.isEmpty())return false;var w=saved.get();
            warpState=thaumcraft.warp.WarpRules.add(w,(warp+1)/2,warp/2,0);
        }
        if(!KnowledgeStore.write(player,next))return false;
        if(warpState!=null&&PlayerWarpStore.write((net.minecraft.server.level.ServerPlayer)player,warpState).result().isEmpty()){KnowledgeStore.write(player,old);return false;}
        for(int i=0;i<take.length;i++)if(take[i]>0)player.getInventory().removeItem(i,take[i]);
        player.getInventory().setChanged();player.inventoryMenu.broadcastChanges();
        player.sendOverlayMessage(Component.translatable(target>entry.stages().size()?"research.thaumcraft.complete":"research.thaumcraft.progress",Component.translatable(entry.name())));
        return true;
    }
    public static void openBook(Player player){
        if(!(player.level() instanceof ServerLevel))return;
        if(!flag(player,"!gotthaumonomicon"))return;
        var state=state(player);if(state!=null&&!state.stages().containsKey("FIRSTSTEPS"))advance(player,"FIRSTSTEPS");
        player.openMenu(new net.minecraft.world.SimpleMenuProvider((id,inventory,p)->new ResearchMenu(id,inventory),Component.translatable("item.thaumcraft.thaumonomicon")));
    }
    /** Operator-only callers may grant an entry; gameplay progress always uses advance(). */
    public static boolean grant(Player player,String key){var entry=ResearchCatalog.BY_KEY.get(key);var state=state(player);return entry!=null&&state!=null&&KnowledgeStore.write(player,state.stage(key,entry.stages().size()+1));}
}
