package dev.exodus.supply.catalog;

import com.google.gson.JsonObject;
import dev.exodus.supply.domain.*;

import java.util.*;

public final class SupplyJsonParser {
    public record Result(SupplyDefinition definition,List<String> errors){}
    private SupplyJsonParser(){}
    public static Result parse(String id,JsonObject json){
        List<String> errors=new ArrayList<>();Set<RadioType> types=new HashSet<>();
        if(json.has("radio_types"))json.getAsJsonArray("radio_types").forEach(v->{try{types.add(RadioType.valueOf(v.getAsString().toUpperCase(Locale.ROOT)));}catch(Exception e){errors.add("radio_types: unknown value "+v);}});
        Integer color=parseColor(json,errors);SupplyCost cost=null;
        if(json.has("cost")){JsonObject c=json.getAsJsonObject("cost");cost=new SupplyCost(string(c,"item",""),integer(c,"count",0));}
        SupplyDefinition d=new SupplyDefinition(id,string(json,"display_name",""),string(json,"icon",""),string(json,"loot_table",""),types,integer(json,"max_requests",0),integer(json,"cooldown_seconds",0),cost,color==null?-1:color,bool(json,"enabled",true),integer(json,"sort_order",0));
        SupplyDefinitionValidator.validate(d).forEach(e->errors.add(e.field()+": "+e.message()));return new Result(d,List.copyOf(errors));
    }
    private static Integer parseColor(JsonObject json,List<String> errors){try{String s=json.getAsJsonObject("drop").get("smoke_color").getAsString();if(!s.matches("#[0-9A-Fa-f]{6}"))throw new IllegalArgumentException();return Integer.parseInt(s.substring(1),16);}catch(Exception e){errors.add("drop.smoke_color: expected #RRGGBB");return null;}}
    private static String string(JsonObject o,String k,String d){return o.has(k)?o.get(k).getAsString():d;}
    private static int integer(JsonObject o,String k,int d){try{return o.has(k)?o.get(k).getAsInt():d;}catch(Exception e){return Integer.MIN_VALUE;}}
    private static boolean bool(JsonObject o,String k,boolean d){return o.has(k)?o.get(k).getAsBoolean():d;}
}
