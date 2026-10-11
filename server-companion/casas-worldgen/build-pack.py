from pathlib import Path
import json,hashlib,copy,zipfile,collections
import argparse
p=argparse.ArgumentParser();p.add_argument('export',type=Path);p.add_argument('output',type=Path);args=p.parse_args()
src=json.loads(args.export.read_text(encoding='utf-8'))
allowed={'minecraft','biomesoplenty','wythers'}
choices={}
for region,points in src['regions'].items():
 for point in points:
  key=json.dumps(point['parameters'],sort_keys=True,separators=(',',':'))
  choices.setdefault(key,{})[region]=point
selected=[]
weights={'vanilla':45,'primary':35,'secondary':17,'rare':3}
for key,options in sorted(choices.items()):
 total=sum(weights[r] for r in options);roll=int(hashlib.sha256(key.encode()).hexdigest()[:16],16)%total
 for region in weights:
  if region not in options:continue
  roll-=weights[region]
  if roll<0:selected.append(copy.deepcopy(options[region]));break
used=sorted({p['biome'] for p in selected});assert all(x in src['biomes'] for x in used)
assert all(x.split(':')[0] in {'minecraft','biomesoplenty'} for x in used)
clones={id:'rassvet_casas:'+('bop/' if id.startswith('biomesoplenty:') else 'vanilla/')+id.split(':',1)[1] for id in used}
files={};removed=[]
def emit(name,value):files[name]=json.dumps(value,ensure_ascii=False,indent=2)+'\n'
for id in used:
 biome=copy.deepcopy(src['biomes'][id]);steps=[]
 for index,features in enumerate(biome['features']):
  clean=[]
  for feature in features:
   assert isinstance(feature,str),('Inline feature',id,feature)
   keep=index not in {3,4,5} and feature.split(':')[0] in allowed and not any(x in feature for x in ['monster_room','desert_well','fossil','campsite','pile_hay'])
   if keep:clean.append(feature)
   else:removed.append([id,feature])
  steps.append(clean)
 biome['features']=steps
 for category,spawns in biome.get('spawners',{}).items():biome['spawners'][category]=[s for s in spawns if s['type'].startswith('minecraft:')]
 ocelots=[s for s in biome.get('spawners',{}).get('monster',[]) if s['type']=='minecraft:ocelot']
 if ocelots:
  biome['spawners']['monster']=[s for s in biome['spawners']['monster'] if s['type']!='minecraft:ocelot']
  biome['spawners'].setdefault('creature',[]).extend(ocelots)
 emit('data/rassvet_casas/worldgen/biome/'+clones[id].split(':',1)[1]+'.json',biome)
for p in selected:p['biome']=clones[p['biome']]
def remap(obj):
 if isinstance(obj,str):return clones.get(obj,obj)
 if isinstance(obj,list):return [remap(x) for x in obj]
 if isinstance(obj,dict):return {k:remap(v) for k,v in obj.items()}
 return obj
noise=remap(src['noise_settings']);noise['surface_rule']={'type':'minecraft:sequence','sequence':[remap(src['bop_surface_rule']),noise['surface_rule']]}
emit('data/rassvet_casas/worldgen/noise_settings/casas.json',noise)
emit('data/rassvet_casas/tags/worldgen/biome/all.json',{'replace':False,'values':list(clones.values())})
foreign=[x for x in src['placed_features'] if x.split(':')[0] not in allowed]
if foreign:emit('data/rassvet_casas/neoforge/biome_modifier/remove_foreign_features.json',{'type':'neoforge:remove_features','biomes':'#rassvet_casas:all','features':foreign})
emit('data/rassvet/dimension/casas.json',{'type':'minecraft:overworld','generator':{'type':'minecraft:noise','settings':'rassvet_casas:casas','biome_source':{'type':'minecraft:multi_noise','biomes':selected}}})
emit('pack.mcmeta',{'pack':{'pack_format':48,'description':'Rassvet Mundo Casas: isolated BOP + WWOO biomes without structures'}})
with zipfile.ZipFile(args.output,'w',zipfile.ZIP_DEFLATED) as z:
 for name,text in files.items():z.writestr(name,text)
manifest={'biomes':clones,'climatePoints':len(selected),'sources':dict(collections.Counter(p.split(':')[0] for p in used)),'removed':removed,'foreignFeaturesRemoved':foreign,'structureTagsCopied':0,'weights':weights}
args.output.with_suffix('.manifest.json').write_text(json.dumps(manifest,ensure_ascii=False,indent=2),encoding='utf-8');print('Prepared',len(used),'isolated biomes,',len(selected),'climate points;',len(foreign),'foreign features excluded;',len(removed),'structure/decor entries removed.')
