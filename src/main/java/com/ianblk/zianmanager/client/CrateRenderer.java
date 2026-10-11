package com.ianblk.zianmanager.client;
import com.google.gson.*;
import com.ianblk.zianmanager.ManagerBlocks;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.blockentity.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import java.util.*;
/** Reads original cubes, UVs, pivots and animation channels, without a geometry-flattening dependency. */
public final class CrateRenderer implements BlockEntityRenderer<ManagerBlocks.ChestEntity> {
 private static final Map<String,Mesh> CACHE=new HashMap<>();
 public CrateRenderer(BlockEntityRendererProvider.Context context){}
 public static void clear(){CACHE.clear();}
 @Override public net.minecraft.world.phys.AABB getRenderBoundingBox(ManagerBlocks.ChestEntity chest){return new net.minecraft.world.phys.AABB(chest.getBlockPos()).inflate(2);}
 @Override public void render(ManagerBlocks.ChestEntity chest,float partial,PoseStack pose,MultiBufferSource buffers,int light,int overlay){
  if(!(chest.getBlockState().getBlock() instanceof ManagerBlocks.DungeonChest block) || block.model.isEmpty() || chest.getLevel()==null)return;
  var mesh=CACHE.computeIfAbsent(block.model,Mesh::load);if(mesh==null)return;double age=(chest.getLevel().getGameTime()-chest.animationStarted+partial)/20.0;
  pose.pushPose();pose.translate(0.5,0,0.5);pose.mulPose(Axis.YP.rotationDegrees(-(chest.getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING).toYRot()+180)));pose.scale(mesh.scale,mesh.scale,mesh.scale);pose.translate(-mesh.offset[0]/16,-mesh.offset[1]/16,-mesh.offset[2]/16);mesh.draw(mesh.tree,pose,buffers,light,overlay,new float[]{0,0,0},age);pose.popPose();
 }
 private static final class Mesh {
  final JsonObject cubes;final JsonArray tree,textures;final JsonObject open,idle;final float scale;final float[] offset;
  Mesh(JsonObject raw){cubes=raw.getAsJsonObject("cubes");tree=raw.getAsJsonArray("tree");textures=raw.getAsJsonArray("textures");scale=raw.get("scale").getAsFloat();offset=vector(raw.getAsJsonArray("offset"));JsonObject o=null,i=null;for(var a:raw.getAsJsonArray("animations")){var d=a.getAsJsonObject();if(d.get("name").getAsString().endsWith(".open"))o=d;if(d.get("name").getAsString().endsWith(".idle"))i=d;}open=o;idle=i;}
  static Mesh load(String id){try(var reader=Minecraft.getInstance().getResourceManager().openAsReader(ResourceLocation.fromNamespaceAndPath("zianmanager","geometry/"+id+".json"))){return new Mesh(JsonParser.parseReader(reader).getAsJsonObject());}catch(Exception error){com.ianblk.zianmanager.ZianManager.LOGGER.error("Cannot load imported crate model {}",id,error);return null;}}
  void draw(JsonArray nodes,PoseStack pose,MultiBufferSource buffers,int light,int overlay,float[] parent,double age){
   double length=open==null?0:open.get("length").getAsDouble();boolean active=open!=null && age>=0 && age<length+0.5;double fade=active?Math.min(1,(length+0.5-age)/0.5):0;JsonObject animation=active?open:idle;double time=active?Math.min(age,length):idle==null?0:age%Math.max(0.01,idle.get("length").getAsDouble());
   for(var entry:nodes){var node=entry.getAsJsonObject();if(node.has("cube")){cube(cubes.getAsJsonObject(node.get("cube").getAsString()),pose,buffers,light,overlay,parent);continue;}String name=node.get("name").getAsString(),uuid=node.get("uuid").getAsString();var origin=vector(node.getAsJsonArray("origin"));var base=vector(node.getAsJsonArray("rotation"));float[] rot=channel(animation,uuid,"rotation",time,0),pos=channel(animation,uuid,"position",time,0),size=channel(animation,uuid,"scale",time,1);
    if(!active && (name.equals("key") || name.equals("vfx")))continue;
    if(active && fade<1){for(int j=0;j<3;j++){rot[j]*=fade;pos[j]*=fade;size[j]=1+(size[j]-1)*(float)fade;}if(name.equals("key") || name.equals("vfx"))for(int j=0;j<3;j++)size[j]*=fade;}
    if(Math.abs(size[0])<0.0001 || Math.abs(size[1])<0.0001 || Math.abs(size[2])<0.0001)continue;
    pose.pushPose();pose.translate((origin[0]-parent[0]+pos[0])/16,(origin[1]-parent[1]+pos[1])/16,(origin[2]-parent[2]+pos[2])/16);rotation(pose,new float[]{base[0]+rot[0],base[1]+rot[1],base[2]+rot[2]});pose.scale(size[0],size[1],size[2]);draw(node.getAsJsonArray("children"),pose,buffers,light,overlay,origin,age);pose.popPose();
   }
  }
  private void cube(JsonObject cube,PoseStack pose,MultiBufferSource buffers,int light,int overlay,float[] parent){if(cube.has("visibility") && !cube.get("visibility").getAsBoolean())return;var from=vector(cube.getAsJsonArray("from"));var to=vector(cube.getAsJsonArray("to"));var origin=cube.has("origin")?vector(cube.getAsJsonArray("origin")):new float[]{0,0,0};pose.pushPose();pose.translate((origin[0]-parent[0])/16,(origin[1]-parent[1])/16,(origin[2]-parent[2])/16);if(cube.has("rotation"))rotation(pose,vector(cube.getAsJsonArray("rotation")));
   for(var face:cube.getAsJsonObject("faces").entrySet()){var f=face.getValue().getAsJsonObject();if(!f.has("texture") || f.get("texture").isJsonNull())continue;int index=f.get("texture").getAsInt();var texture=textures.get(index).getAsJsonObject();var consumer=buffers.getBuffer(RenderType.entityCutoutNoCull(ResourceLocation.parse(texture.get("path").getAsString())));float[] uv=vector4(f.getAsJsonArray("uv"));float tw=texture.get("width").getAsFloat(),th=texture.get("height").getAsFloat();var direction=net.minecraft.core.Direction.byName(face.getKey());var info=FaceInfo.fromFacing(direction);float[] positions={from[1],to[1],from[2],to[2],from[0],to[0]};
    int turn=f.has("rotation")?(f.get("rotation").getAsInt()/90)%4:0;float[][] uvs={{uv[0],uv[1]},{uv[0],uv[3]},{uv[2],uv[3]},{uv[2],uv[1]}};
    for(int v=0;v<4;v++){var vertex=info.getVertexInfo(v);var coord=uvs[(v+turn)%4];consumer.addVertex(pose.last().pose(),(positions[vertex.xFace]-origin[0])/16,(positions[vertex.yFace]-origin[1])/16,(positions[vertex.zFace]-origin[2])/16).setColor(255,255,255,255).setUv(coord[0]/tw,coord[1]/th).setOverlay(overlay).setLight(light).setNormal(pose.last(),direction.getStepX(),direction.getStepY(),direction.getStepZ());}
   }pose.popPose();
  }
 }
 private static float[] channel(JsonObject animation,String uuid,String channel,double time,float fallback){float[] empty={fallback,fallback,fallback};if(animation==null || !animation.getAsJsonObject("animators").has(uuid))return empty;var frames=new ArrayList<JsonObject>();var animator=animation.getAsJsonObject("animators").getAsJsonObject(uuid);if(!animator.has("keyframes"))return empty;for(var f:animator.getAsJsonArray("keyframes"))if(f.getAsJsonObject().get("channel").getAsString().equals(channel))frames.add(f.getAsJsonObject());if(frames.isEmpty())return empty;frames.sort(Comparator.comparingDouble(f->f.get("time").getAsDouble()));if(time<=frames.getFirst().get("time").getAsDouble())return point(frames.getFirst(),false);if(time>=frames.getLast().get("time").getAsDouble())return point(frames.getLast(),true);int at=0;while(at+1<frames.size() && frames.get(at+1).get("time").getAsDouble()<time)at++;var a=frames.get(at);var b=frames.get(at+1);double duration=b.get("time").getAsDouble()-a.get("time").getAsDouble(),t=(time-a.get("time").getAsDouble())/duration;var va=point(a,true);var vb=point(b,false);var before=point(frames.get(Math.max(0,at-1)),true);var after=point(frames.get(Math.min(frames.size()-1,at+2)),false);float[] out=new float[3];String interpolation=b.get("interpolation").getAsString();for(int i=0;i<3;i++){if(interpolation.equals("catmullrom"))out[i]=(float)(0.5*((2*va[i])+(-before[i]+vb[i])*t+(2*before[i]-5*va[i]+4*vb[i]-after[i])*t*t+(-before[i]+3*va[i]-3*vb[i]+after[i])*t*t*t));else if(interpolation.equals("bezier")){double ax=a.has("bezier_right_time")?a.getAsJsonArray("bezier_right_time").get(i).getAsDouble():duration/3;double bx=b.has("bezier_left_time")?b.getAsJsonArray("bezier_left_time").get(i).getAsDouble():-duration/3;double ay=a.has("bezier_right_value")?a.getAsJsonArray("bezier_right_value").get(i).getAsDouble():0;double by=b.has("bezier_left_value")?b.getAsJsonArray("bezier_left_value").get(i).getAsDouble():0;double low=0,high=1,u=t;for(int j=0;j<16;j++){u=(low+high)/2;double x=bezier(0,ax,duration+bx,duration,u);if(x<time-a.get("time").getAsDouble())low=u;else high=u;}out[i]=(float)bezier(va[i],va[i]+ay,vb[i]+by,vb[i],u);}else out[i]=(float)(va[i]+(vb[i]-va[i])*t);}return out;}
 private static double bezier(double a,double b,double c,double d,double t){double s=1-t;return s*s*s*a+3*s*s*t*b+3*s*t*t*c+t*t*t*d;}
 private static float[] point(JsonObject key,boolean post){var values=key.getAsJsonArray("data_points");var point=values.get(post?values.size()-1:0).getAsJsonObject();return new float[]{Float.parseFloat(point.get("x").getAsString().trim()),Float.parseFloat(point.get("y").getAsString().trim()),Float.parseFloat(point.get("z").getAsString().trim())};}
 private static float[] vector(JsonArray array){return new float[]{array.get(0).getAsFloat(),array.get(1).getAsFloat(),array.get(2).getAsFloat()};}
 private static float[] vector4(JsonArray array){return new float[]{array.get(0).getAsFloat(),array.get(1).getAsFloat(),array.get(2).getAsFloat(),array.get(3).getAsFloat()};}
 private static void rotation(PoseStack pose,float[] r){pose.mulPose(Axis.ZP.rotationDegrees(r[2]));pose.mulPose(Axis.YP.rotationDegrees(r[1]));pose.mulPose(Axis.XP.rotationDegrees(r[0]));}
}
