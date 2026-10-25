package thaumcraft.research;

import java.util.*;
import java.util.regex.*;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.*;

/** The six original research markup elements, with explicit page boundaries. */
public final class BookMarkup {
 public enum Kind {TEXT, BREAK, LINE, DIVIDER, PAGE, IMAGE}
 public record Image(String texture,int x,int y,int width,int height,float scale){}
 public record Part(Kind kind,String text,Image image){}
 private static final Pattern TAG=Pattern.compile("(?is)<IMG>(.*?)</IMG>|<(BR|LINE|DIV|PAGE)\\s*/?>|<[^>]*>");
 public static List<Part> parse(String source){var result=new ArrayList<Part>();var m=TAG.matcher(source);int end=0;while(m.find()){if(m.start()>end)result.add(new Part(Kind.TEXT,source.substring(end,m.start()),null));if(m.group(1)!=null){var image=image(m.group(1));if(image!=null)result.add(new Part(Kind.IMAGE,"",image));}else if(m.group(2)!=null)result.add(new Part(switch(m.group(2).toUpperCase(Locale.ROOT)){case "BR"->Kind.BREAK;case "LINE"->Kind.LINE;case "DIV"->Kind.DIVIDER;default->Kind.PAGE;},"",null));end=m.end();}if(end<source.length())result.add(new Part(Kind.TEXT,source.substring(end),null));return List.copyOf(result);}
 private static Image image(String raw){try{var s=raw.trim().split(":");if(s.length!=7||!s[0].equals("thaumcraft")||!s[1].matches("textures/[a-z0-9_/]+\\.png"))return null;int x=Integer.parseInt(s[2]),y=Integer.parseInt(s[3]),w=Integer.parseInt(s[4]),h=Integer.parseInt(s[5]);float scale=Float.parseFloat(s[6]);if(x<0||y<0||w<1||h<1||x+w>512||y+h>512||!Float.isFinite(scale)||scale<=0||w*scale>140||h*scale>208)return null;return new Image(s[0]+":"+s[1],x,y,w,h,scale);}catch(RuntimeException e){return null;}}
 public static Component formatted(String raw){var result=Component.empty();Style style=Style.EMPTY;var text=new StringBuilder();for(int i=0;i<raw.length();i++){char c=raw.charAt(i);if(c=='§'&&i+1<raw.length()){if(!text.isEmpty()){result.append(Component.literal(text.toString()).withStyle(style));text.setLength(0);}var f=ChatFormatting.getByCode(raw.charAt(++i));if(f!=null)style=f==ChatFormatting.RESET?Style.EMPTY:f.ordinal()<=ChatFormatting.WHITE.ordinal()?Style.EMPTY.applyFormat(f):style.applyFormat(f);}else text.append(c);}if(!text.isEmpty())result.append(Component.literal(text.toString()).withStyle(style));return result;}
 public static String searchable(String text){return java.text.Normalizer.normalize(text,java.text.Normalizer.Form.NFD).replaceAll("\\p{M}+","").replaceAll("§.","").toLowerCase(Locale.ROOT);}
 private BookMarkup(){}
}
