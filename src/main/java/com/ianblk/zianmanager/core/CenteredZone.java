package com.ianblk.zianmanager.core;
import com.ianblk.zianmanager.core.Definitions.Point;
public final class CenteredZone {
 private CenteredZone(){}
 public record Bounds(Point first,Point second){}
 public static Bounds bounds(Point center,int radiusX,int radiusZ,int above,int below){if(center==null || radiusX<1 || radiusX>64 || radiusZ<1 || radiusZ>64 || above<1 || below<0 || above+below>64)throw new IllegalArgumentException("Radio: 1–64; altura/profundidad total: máximo 64");int x=(int)Math.floor(center.x()),y=(int)Math.floor(center.y()),z=(int)Math.floor(center.z());return new Bounds(new Point(x-radiusX,y-below,z-radiusZ,0),new Point(x+radiusX,y+above-1,z+radiusZ,0));}
}
