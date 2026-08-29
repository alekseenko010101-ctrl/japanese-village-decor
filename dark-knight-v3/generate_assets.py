from pathlib import Path
import json, math, random, struct, zlib

ROOT = Path('src/main/resources/assets/sleepless_knight')
random.seed(7319)

def clamp(v):
    return max(0, min(255, int(v)))

def save_png(path, w, h, pixels):
    path.parent.mkdir(parents=True, exist_ok=True)
    raw = bytearray()
    for row in pixels:
        raw.append(0)
        for px in row:
            raw.extend(px)
    def chunk(tag, data):
        return struct.pack('>I', len(data)) + tag + data + struct.pack('>I', zlib.crc32(tag + data) & 0xffffffff)
    png = b'\x89PNG\r\n\x1a\n'
    png += chunk(b'IHDR', struct.pack('>IIBBBBB', w, h, 8, 6, 0, 0, 0))
    png += chunk(b'IDAT', zlib.compress(bytes(raw), 9))
    png += chunk(b'IEND', b'')
    path.write_bytes(png)

def base_texture(w=512, h=512):
    p=[]
    for y in range(h):
        row=[]
        for x in range(w):
            wave = 6*math.sin(x*0.07) + 5*math.sin(y*0.043)
            grain = random.randint(-5,5)
            r = 42 + wave*0.55 + grain
            g = 38 + wave*0.35 + grain*0.6
            b = 58 + wave*0.90 + grain
            row.append((clamp(r),clamp(g),clamp(b),255))
        p.append(row)
    return p

def rect(p,x0,y0,x1,y1,c,noise=0,edge=False):
    for y in range(max(0,y0),min(len(p),y1)):
        for x in range(max(0,x0),min(len(p[0]),x1)):
            n=random.randint(-noise,noise) if noise else 0
            rr,gg,bb,aa=c
            if edge and (x-x0<2 or x1-1-x<2 or y-y0<2 or y1-1-y<2):
                rr+=26; gg+=22; bb+=34
            p[y][x]=(clamp(rr+n),clamp(gg+n),clamp(bb+n),aa)

def line(p,x0,y0,x1,y1,c):
    steps=max(abs(x1-x0),abs(y1-y0),1)
    for i in range(steps+1):
        x=round(x0+(x1-x0)*i/steps); y=round(y0+(y1-y0)*i/steps)
        if 0<=y<len(p) and 0<=x<len(p[0]): p[y][x]=c

def make_entity():
    p=base_texture()
    steel=(50,45,68,255); steel2=(62,55,82,255); edge=(126,112,151,255)
    dark=(22,20,31,255); leather=(72,43,34,255); chain=(25,24,32,255)
    silver=(161,163,176,255); silver_hi=(205,207,217,255)
    rune=(184,148,255,255); rune_hi=(231,219,255,255)
    rect(p,0,0,360,64,steel2,7,True)
    rect(p,0,0,38,32,dark,3,False)
    rect(p,0,80,448,180,steel,7,True)
    rect(p,384,80,448,116,leather,5,True)
    rect(p,0,144,120,184,chain,4,False)
    rect(p,0,208,250,280,steel2,8,True)
    rect(p,256,208,320,250,leather,5,True)
    rect(p,288,208,336,250,(96,86,106,255),5,True)
    rect(p,336,208,448,290,silver,6,True)
    rect(p,416,208,448,280,silver_hi,3,False)
    rect(p,0,288,280,360,steel,7,True)
    rect(p,448,80,490,130,rune,3,False)
    for y in range(82,128,5):
        for x in range(450,488,7):
            if random.random()<0.42: p[y][x]=rune_hi
    for y in range(146,180,4):
        for x in range(2,116,4):
            p[y][x]=(86,82,99,255)
            if x+1<512: p[y][x+1]=(41,40,51,255)
    for _ in range(320):
        region=random.choice([(40,2,350,62),(0,82,360,138),(0,210,245,278),(0,290,270,358)])
        x=random.randint(region[0],region[2]); y=random.randint(region[1],region[3])
        ln=random.randint(2,8)
        c=edge if random.random()<0.25 else (94,83,116,255)
        line(p,x,y,min(511,x+ln),max(0,y-random.randint(0,2)),c)
    save_png(ROOT/'textures/entity/dark_knight.png',512,512,p)

def make_egg():
    w=h=16; clear=(0,0,0,0)
    p=[[clear for _ in range(w)] for _ in range(h)]
    shape=[(6,1,10),(4,2,12),(3,4,13),(2,6,14),(2,9,14),(3,12,13),(5,14,11)]
    for y,x0,x1 in shape:
        for yy in range(y,min(h,y+2)):
            for x in range(x0,x1):
                d=abs(x-8)+abs(yy-8)*0.35
                p[yy][x]=(clamp(63-d*2),clamp(53-d),clamp(83-d),255)
    for x,y in [(6,5),(9,4),(5,9),(10,10),(8,7),(7,12)]:
        p[y][x]=(196,161,255,255)
    save_png(ROOT/'textures/item/dark_knight_spawn_egg.png',16,16,p)

def write(path,text):
    path.parent.mkdir(parents=True,exist_ok=True)
    path.write_text(text,encoding='utf-8')

def make_json():
    write(ROOT/'lang/ru_ru.json', json.dumps({
        'entity.sleepless_knight.dark_knight':'Тёмный рыцарь',
        'item.sleepless_knight.dark_knight_spawn_egg':'Яйцо призыва Тёмного рыцаря',
        'subtitles.sleepless_knight.dark_knight_appear':'Тёмный рыцарь появляется'
    },ensure_ascii=False,indent=2))
    write(ROOT/'lang/en_us.json', json.dumps({
        'entity.sleepless_knight.dark_knight':'Dark Knight',
        'item.sleepless_knight.dark_knight_spawn_egg':'Dark Knight Spawn Egg',
        'subtitles.sleepless_knight.dark_knight_appear':'Dark Knight appears'
    },indent=2))
    write(ROOT/'models/item/dark_knight_spawn_egg.json', json.dumps({
        'parent':'minecraft:item/generated',
        'textures':{'layer0':'sleepless_knight:item/dark_knight_spawn_egg'}
    },indent=2))
    write(ROOT/'items/dark_knight_spawn_egg.json', json.dumps({
        'model':{'type':'minecraft:model','model':'sleepless_knight:item/dark_knight_spawn_egg'}
    },indent=2))

make_entity(); make_egg(); make_json()
print('Generated v4 knight texture and spawn egg assets')
