package thaumcraft.golems;
public enum SealKind {
    PICKUP("SEALCOLLECT",""),FILL("SEALSTORE",""),EMPTY("SEALEMPTY",""),GUARD("SEALGUARD","fighter"),HARVEST("SEALHARVEST","deft"),BREAKER("SEALBREAK","breaker"),
    PICKUP_ADVANCED("SEALCOLLECT&&MINDBIOTHAUMIC",""),FILL_ADVANCED("SEALSTORE&&MINDBIOTHAUMIC",""),EMPTY_ADVANCED("SEALEMPTY&&MINDBIOTHAUMIC",""),GUARD_ADVANCED("SEALGUARD&&MINDBIOTHAUMIC","fighter"),BREAKER_ADVANCED("SEALBREAK&&MINDBIOTHAUMIC","breaker"),PROVIDER("SEALPROVIDE",""),STOCK("SEALSTOCK",""),USE("SEALUSE","deft"),LUMBER("SEALLUMBER","breaker"),BUTCHER("SEALBUTCHER","fighter");
    public final String research,trait;
    SealKind(String research,String trait){this.research=research;this.trait=trait;}
    public String id(){return name().toLowerCase(java.util.Locale.ROOT);}
    public boolean advanced(){return name().endsWith("_ADVANCED");}
    public SealKind base(){return advanced()?switch(this){case PICKUP_ADVANCED->PICKUP;case FILL_ADVANCED->FILL;case EMPTY_ADVANCED->EMPTY;case GUARD_ADVANCED->GUARD;case BREAKER_ADVANCED->BREAKER;default->this;}:this;}
    public int filterSlots(){return advanced()&&base()!=GUARD||this==PROVIDER||this==STOCK?9:1;}
    public boolean hasArea(){return base()==PICKUP||base()==GUARD||base()==BREAKER||this==HARVEST||this==LUMBER||this==BUTCHER;}
    public boolean hasFilters(){return base()!=GUARD&&this!=LUMBER&&this!=BUTCHER;}
    public boolean accepts(GolemDesign d){return (trait.isEmpty()||d.has(trait))&&(!(advanced()||this==USE||this==LUMBER||this==BUTCHER)||d.has("smart"))&&(!d.has("clumsy")||this!=HARVEST&&this!=PROVIDER&&this!=STOCK&&base()!=PICKUP&&base()!=FILL&&base()!=EMPTY);}
}
