from pathlib import Path
import hashlib,json,math,struct
ROOT=Path(__file__).resolve().parents[1]
ASSETS=ROOT/'src/main/resources/assets/zianmanager'
report=json.loads((ROOT/'docs/imported-assets.json').read_text(encoding='utf-8'))
for model in report['models']:
 id=model['id'];obj=ASSETS/f'models/imported/{id}.obj';lines=obj.read_text(encoding='utf-8').splitlines();vertices=[line for line in lines if line.startswith('v ')];faces=[line for line in lines if line.startswith('f ')];assert len(vertices)==model['vertices'] and len(faces)*4==len(vertices)
 for line in lines:
  if line.startswith(('v ','vt ','vn ')):assert all(math.isfinite(float(v)) for v in line.split()[1:])
  if line.startswith('f '):assert all(1<=int(v.split('/')[0])<=len(vertices) for v in line.split()[1:])
 for i,texture in enumerate(model['textures']):
  assert texture['material'].startswith('zianmanager:item/');assert (ASSETS/'textures'/ (texture['material'].removeprefix('zianmanager:')+'.png')).exists();path=ASSETS/f'textures/item/imported/{id}/{i}.png';data=path.read_bytes();assert hashlib.sha256(data).hexdigest()==texture['sha256'];assert struct.unpack('>II',data[16:24])[0]>0
  meta=path.with_suffix('.png.mcmeta')
  if meta.exists():assert isinstance(json.loads(meta.read_text(encoding='utf-8')),dict)
 if model['free']:
  raw=json.loads((ASSETS/f'geometry/{id}.json').read_text(encoding='utf-8'));assert len(raw['cubes'])==model['cubes'];assert len(raw['animations'])==model['animations'];seen=[]
  def visit(nodes):
   for node in nodes:
    if 'cube' in node:seen.append(node['cube'])
    else:visit(node['children'])
  visit(raw['tree']);assert set(seen)==set(raw['cubes']);assert len(seen)==len(set(seen))
  for cube in raw['cubes'].values():
   for face in cube['faces'].values():
    if face.get('texture') is not None:assert 0<=int(face['texture'])<len(raw['textures'])
print(f"Verified {len(report['models'])} models: finite geometry, original PNG hashes, valid indices, complete cube hierarchy and animation data.")

assert not (ASSETS/"models/raw").exists(), "Animation data must not be treated as block models"
