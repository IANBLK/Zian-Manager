"""Import supplied assets without modifying texture pixels; bake unrestricted rotations to OBJ."""
import base64,copy,hashlib,json,math,struct,sys
from pathlib import Path
from zipfile import ZipFile
ROOT=Path(__file__).resolve().parents[1];ASSETS=ROOT/'src/main/resources/assets/zianmanager';DOWNLOADS=Path(sys.argv[1]) if len(sys.argv)>1 else Path('C:/Users/pauln/Downloads')
manifest=[]
def write(path,data):
 path.parent.mkdir(parents=True,exist_ok=True);path.write_text(json.dumps(data,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
def texture(id,index,pixels,uv):
 path=ASSETS/f'textures/imported/{id}/{index}.png';path.parent.mkdir(parents=True,exist_ok=True);path.write_bytes(pixels)
 return {'path':f'zianmanager:textures/imported/{id}/{index}.png','material':f'zianmanager:imported/{id}/{index}','width':uv[0],'height':uv[1],'sha256':hashlib.sha256(pixels).hexdigest()}
def rotate(v,r):
 x,y,z=v
 for i,a in enumerate(r):
  t=math.radians(a);c=math.cos(t);s=math.sin(t)
  if i==0:y,z=y*c-z*s,y*s+z*c
  if i==1:x,z=x*c+z*s,-x*s+z*c
  if i==2:x,y=x*c-y*s,x*s+y*c
 return [x,y,z]
FACE={'down':[(0,0,1),(0,0,0),(1,0,0),(1,0,1)],'up':[(0,1,0),(0,1,1),(1,1,1),(1,1,0)],'north':[(1,1,0),(1,0,0),(0,0,0),(0,1,0)],'south':[(0,1,1),(0,0,1),(1,0,1),(1,1,1)],'west':[(0,1,0),(0,0,0),(0,0,1),(0,1,1)],'east':[(1,1,1),(1,0,1),(1,0,0),(1,1,0)]}
def model(id,d,textures,source,free=False):
 # Cube and group data are retained for animated block rendering.
 groups={g['uuid']:g for g in d.get('groups',[]) if 'uuid' in g};cubes={e.get('uuid',str(i)):e for i,e in enumerate(d['elements'])}
 def node(obj):
  if isinstance(obj,str):return {'cube':obj}
  g=groups.get(obj.get('uuid'),obj);return {'name':g.get('name','group'),'uuid':obj.get('uuid',''),'origin':g.get('origin',[0,0,0]),'rotation':g.get('rotation',[0,0,0]),'children':[node(e) for e in obj.get('children',[])]}
 tree=[node(o) for o in d.get('outliner',[])] or [{'cube':k} for k in cubes]
 hide=set()
 def walk(nodes,ancestors=[]):
  for n in nodes:
   if 'cube' in n:
    if 'key' in ancestors or 'vfx' in ancestors:hide.add(n['cube'])
   else:walk(n.get('children',[]),ancestors+[n.get('name')])
 walk(tree)
 body=[e for k,e in cubes.items() if k not in hide and e.get('visibility',True)]
 lo=[min(e['from'][i] for e in body) for i in range(3)];hi=[max(e['to'][i] for e in body) for i in range(3)]
 scale=14/max(hi[i]-lo[i] for i in range(3)) if free else 1
 offset=[(lo[0]+hi[0])/2,lo[1],(lo[2]+hi[2])/2] if free else [0,0,0]
 raw={'cubes':cubes,'tree':tree,'textures':textures,'animations':d.get('animations',[]),'scale':scale,'offset':offset}
 if free:write(ASSETS/f'models/raw/{id}.json',raw)
 lines=[f'mtllib {id}.mtl'];material=[];count=0
 for i,t in enumerate(textures):material += [f'newmtl texture_{i}','Kd 1 1 1','Ka 1 1 1','d 1',f'map_Kd {t["material"]}']
 def cube(e,transforms):
  nonlocal count,lines
  if e.get('visibility',True)==False or e.get('export',True)==False:return
  if e.get('type','cube')!='cube':raise ValueError('Mesh unsupported; must not silently flatten')
  rot=e.get('rotation',[0,0,0]);pivot=e.get('origin',[0,0,0]);rescale=e.get('rescale',False)
  if isinstance(rot,dict):
   pivot=rot.get('origin',[0,0,0]);rescale=rot.get('rescale',rescale);v=[rot.get(a,0) for a in 'xyz'];
   if 'angle' in rot:v['xyz'.index(rot['axis'])]=rot['angle']
   rot=v
  for side,f in e['faces'].items():
   tex=f.get('texture');
   if tex is None:continue
   if isinstance(tex,str):tex=int(tex.removeprefix('#'))
   uv=f.get('uv',[0,0,0,0]);vertices=[]
   for pick in FACE[side]:
    pos=[e['to'][i] if pick[i] else e['from'][i] for i in range(3)];v=[pos[i]-pivot[i] for i in range(3)];v=rotate(v,rot)
    if rescale:
     for axis,a in enumerate(rot):
      if a:
       fac=1/math.cos(math.radians(a));v=[x if j==axis else x*fac for j,x in enumerate(v)]
    pos=[v[i]+pivot[i] for i in range(3)]
    for g in reversed(transforms):pos=[v+g['origin'][i] for i,v in enumerate(rotate([pos[i]-g['origin'][i] for i in range(3)],g['rotation']))]
    if free:pos=[(pos[i]-offset[i])*scale+[8,0,8][i] for i in range(3)]
    vertices.append([v/16 for v in pos])
   a=[vertices[1][j]-vertices[0][j] for j in range(3)];b=[vertices[2][j]-vertices[0][j] for j in range(3)];normal=[a[1]*b[2]-a[2]*b[1],a[2]*b[0]-a[0]*b[2],a[0]*b[1]-a[1]*b[0]];length=math.sqrt(sum(x*x for x in normal));normal=[x/(length or 1) for x in normal]
   uvs=[(uv[0],uv[1]),(uv[0],uv[3]),(uv[2],uv[3]),(uv[2],uv[1])];turn=(f.get('rotation',0)//90)%4;uvs=uvs[turn:]+uvs[:turn]
   lines.append(f'usemtl texture_{tex}')
   for pos,(u,v) in zip(vertices,uvs):lines += ['v '+' '.join(f'{x:.8f}' for x in pos),f'vt {u/textures[tex]["width"]:.8f} {v/textures[tex]["height"]:.8f}','vn '+' '.join(f'{x:.8f}' for x in normal)]
   lines.append('f '+' '.join(f'{count+i}/{count+i}/{count+i}' for i in range(1,5)));count+=4
 def bake(nodes,parents=[]):
  for n in nodes:
   if 'cube' in n:
    if not free or n['cube'] not in hide:cube(cubes[n['cube']],parents)
   else:bake(n.get('children',[]),parents+[n])
 bake(tree)
 obj=ASSETS/f'models/imported/{id}.obj';obj.parent.mkdir(parents=True,exist_ok=True);obj.write_text('\n'.join(lines)+'\n',encoding='utf-8');obj.with_suffix('.mtl').write_text('\n'.join(material)+'\n',encoding='utf-8')
 j={'parent':'minecraft:block/block','loader':'neoforge:obj','model':f'zianmanager:models/imported/{id}.obj','automatic_culling':False,'flip_v':False,'render_type':'minecraft:cutout','textures':{'particle':textures[0]['material']}}
 if d.get('display'):j['display']={k:v for k,v in d['display'].items() if k in ['thirdperson_righthand','thirdperson_lefthand','firstperson_righthand','firstperson_lefthand','ground','gui','head','fixed']}
 write(ASSETS/f'models/{"block" if free else "item"}/{id}.json',j)
 if free:
  write(ASSETS/f'models/item/{id}.json',{'parent':f'zianmanager:block/{id}'})
  write(ASSETS/f'blockstates/{id}.json',{'variants':{f'facing={face}':{'model':f'zianmanager:block/{id}','y':angle} for face,angle in [('north',0),('east',90),('south',180),('west',270)]}})
 manifest.append({'id':id,'source':source,'cubes':len(cubes),'vertices':count,'textures':textures,'animations':len(d.get('animations',[])),'free':free})
# Hammers: source geometry and display transforms unchanged, four palette variants.
with ZipFile(DOWNLOADS/'Multicolored Hammer Pack.zip') as z:
 d=json.loads(z.read('Multicolored Hammer Pack/json/hammer_model.json'))
 for color in ['blue','green','grey','red']:
  p=z.read(f'Multicolored Hammer Pack/textures/{color}_hammer_texture.png');uv=struct.unpack('>II',p[16:24]);m=copy.deepcopy(d)
  for e in m['elements']:e['uuid']=str(m['elements'].index(e));e['type']='cube'
  model(color+'_hammer',m,[texture(color+'_hammer',0,p,[16,16])],'Multicolored Hammer Pack.zip')
for pack,prefix in [('Fantasy Weapons Pack 1.zip',''),('crate_pack_2.zip','locked_'),('Crates and Stuff Model Pack Update 4.zip','loot_')]:
 with ZipFile(DOWNLOADS/pack) as z:
  seen=set()
  for n in sorted(z.namelist(),key=len):
   if not n.endswith('.bbmodel') or '/keys/.bbmodels/' in n:continue
   stem=Path(n).stem
   if stem in seen:continue
   seen.add(stem);d=json.loads(z.read(n));free=d.get('meta',{}).get('model_format')=='free';id=(prefix+stem) if free else stem
   tx=[]
   for i,t in enumerate(d['textures']):
    p=base64.b64decode(t['source'].split(',',1)[1]);tx.append(texture(id,i,p,[t.get('uv_width',d['resolution']['width']),t.get('uv_height',d['resolution']['height'])]))
   model(id,d,tx,pack+' :: '+n,free)
with ZipFile(DOWNLOADS/'altarbygwambassn2.zip') as z:
 for n in z.namelist():
  if n.startswith('assets/minecraft/models/custom/') and n.endswith('.json'):
   d=json.loads(z.read(n));id='altar_'+Path(n).stem;tx=[];lookup={}
   for key,value in d['textures'].items():
    if key=='particle':continue
    src='assets/minecraft/textures/'+value.removeprefix('minecraft:')+'.png';p=z.read(src);uv=[16,16];lookup[key]=len(tx);tx.append(texture(id,len(tx),p,uv))
    if src+'.mcmeta' in z.namelist():(ASSETS/f'textures/imported/{id}/{len(tx)-1}.png.mcmeta').write_bytes(z.read(src+'.mcmeta'))
   for i,e in enumerate(d['elements']):
    e['uuid']=str(i);e['type']='cube'
    for f in e['faces'].values():f['texture']=lookup[f['texture'].removeprefix('#')]
   model(id,d,tx,'altarbygwambassn2.zip :: '+n)
write(ROOT/'docs/imported-assets.json',{'models':manifest,'archives':[{'name':n,'sha256':hashlib.sha256((DOWNLOADS/n).read_bytes()).hexdigest()} for n in ['Multicolored Hammer Pack.zip','Fantasy Weapons Pack 1.zip','crate_pack_2.zip','Crates and Stuff Model Pack Update 4.zip','altarbygwambassn2.zip']]})
print('Imported',len(manifest),'models; texture pixels unmodified.')
