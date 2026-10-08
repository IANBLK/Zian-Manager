package com.ianblk.zianmanager.core;
public final class ZonePopulation {
 private ZonePopulation(){}
 public static int target(int base,int players){if(base<1 || base>3 || players<1)throw new IllegalArgumentException("Population");return (int)Math.min(8,base+2L*(players-1));}
}
