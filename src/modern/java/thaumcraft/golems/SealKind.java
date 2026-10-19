package thaumcraft.golems;
public enum SealKind {
    PICKUP("SEALCOLLECT",""),FILL("SEALSTORE",""),EMPTY("SEALEMPTY",""),GUARD("SEALGUARD","fighter"),HARVEST("SEALHARVEST","deft"),BREAKER("SEALBREAK","breaker");
    public final String research,trait;
    SealKind(String research,String trait){this.research=research;this.trait=trait;}
    public String id(){return name().toLowerCase(java.util.Locale.ROOT);}
    public boolean accepts(GolemDesign d){return (trait.isEmpty()||d.has(trait))&&(!d.has("clumsy")||this!=HARVEST);}
}
