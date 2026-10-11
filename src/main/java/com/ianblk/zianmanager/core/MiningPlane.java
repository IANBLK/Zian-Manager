package com.ianblk.zianmanager.core;
import java.util.*;
public final class MiningPlane {
 private MiningPlane(){}
 public static List<int[]> offsets(String axis){if(!Set.of("X","Y","Z").contains(axis))throw new IllegalArgumentException("Axis");var result=new ArrayList<int[]>();for(int a=-1;a<=1;a++)for(int b=-1;b<=1;b++)if(a!=0 || b!=0)result.add(switch(axis){case "X"->new int[]{0,a,b};case "Y"->new int[]{a,0,b};default->new int[]{a,b,0};});return List.copyOf(result);}
}
