from pathlib import Path
import json, math, random, struct, zlib

ROOT = Path('src/main/resources/assets/sleepless_knight')
random.seed(4421)


def save_png(path, w, h, pixels):
    path.parent.mkdir(parents=True, exist_ok=True)
    raw = bytearray()
    for y in range(h):
        raw.append(0)
        for x in range(w):
            raw.extend(pixels[y][x])
    def chunk(tag, data):
        return struct.pack('>I', len(data)) + tag + data + struct.pack('>I', zlib.crc32(tag + data) & 0xffffffff)
    png = b'\x89PNG\r\n\x1a\n'
    png += chunk(b'IHDR', struct.pack('>IIBBBBB', w, h, 8, 6, 0, 0, 0))
    png += chunk(b'IDAT', zlib.compress(bytes(raw), 9))
    png += chunk(b'IEND', b'')
    path.write_bytes(png)


def clamp(v):
    return max(0, min(255, int(v)))


def base_steel(w=256, h=256):
    p = []
    for y in range(h):
        row = []
        for x in range(w):
            # blackened steel, subtly violet, deliberately no magenta/pink
            band = 6 * math.sin(x * 0.115) + 4 * math.sin(y * 0.071)
            grain = random.randint(-7, 7)
            edge = 5 * math.sin((x + y) * 0.035)
            r = 27 + band + grain + edge
            g = 25 + band * 0.55 + grain * 0.5
            b = 38 + band * 1.2 + grain + edge
            row.append((clamp(r), clamp(g), clamp(b), 255))
        p.append(row)
    return p


def rect(p, x0, y0, x1, y1, c, noise=0, rim=False):
    h, w = len(p), len(p[0])
    x0=max(0,x0); y0=max(0,y0); x1=min(w,x1); y1=min(h,y1)
    for y in range(y0,y1):
        for x in range(x0,x1):
            n = random.randint(-noise, noise) if noise else 0
            rr,gg,bb,aa = c
            if rim and (x-x0<2 or x1-1-x<2 or y-y0<2 or y1-1-y<2):
                rr+=30; gg+=27; bb+=38
            p[y][x]=(clamp(rr+n),clamp(gg+n),clamp(bb+n),aa)


def line(p, x0,y0,x1,y1,c,width=1):
    dx=x1-x0; dy=y1-y0; steps=max(abs(dx),abs(dy),1)
    for i in range(steps+1):
        x=round(x0+dx*i/steps); y=round(y0+dy*i/steps)
        for yy in range(y-width,y+width+1):
            for xx in range(x-width,x+width+1):
                if 0<=yy<len(p) and 0<=xx<len(p[0]): p[yy][xx]=c


def make_entity():
    p=base_steel()
    # Distinct material fields aligned to all UV regions used by the model.
    armor=(38,35,53,255); armor2=(48,43,65,255); edge=(91,84,112,255)
    leather=(62,39,31,255); chain=(20,20,27,255); silver=(156,160,171,255)
    # helmet / flare / visor
    rect(p,0,0,64,42,armor,8,True); rect(p,68,0,120,30,armor2,7,True); rect(p,118,0,145,18,(17,15,24,255),3,True)
    # chest / abdomen / collar / belt
    rect(p,0,44,64,79,armor2,7,True); rect(p,64,44,124,64,armor,6,True); rect(p,76,55,126,71,leather,6,True)
    # chain / tassets
    rect(p,0,80,40,108,chain,5,False); rect(p,40,80,64,111,(34,28,43,255),5,True)
    # arms
    rect(p,64,64,192,94,armor,8,True); rect(p,64,96,192,111,armor2,7,True)
    # legs
    rect(p,0,112,192,148,armor,8,True)
    # sword grip + guard
    rect(p,192,128,207,151,leather,5,True); rect(p,208,128,255,151,(87,82,96,255),5,True)
    # sword blade / tip / fuller
    rect(p,0,160,58,218,silver,7,True); rect(p,32,160,62,226,(105,109,123,255),5,True); rect(p,48,160,64,218,(192,197,206,255),3,False)
    # pale mint rune UV fields
    mint=(146,209,190,255); mint_hi=(201,240,226,255)
    rect(p,192,0,207,24,mint,4,False); rect(p,208,0,226,20,mint,4,False); rect(p,224,0,244,20,mint_hi,3,False)
    # chainmail dots/rings in the chain areas
    for (x0,y0,x1,y1) in [(0,80,40,108)]:
        for y in range(y0+2,y1-1,4):
            for x in range(x0+2,x1-1,4):
                p[y][x]=(83,82,95,255); p[y][min(x+1,255)]=(43,43,52,255)
    # controlled scratches, avoiding pink artifacts
    for _ in range(140):
        x=random.randrange(0,192); y=random.randrange(0,149)
        length=random.randrange(2,8)
        c=(79,74,96,255) if random.random()<0.75 else (112,108,126,255)
        line(p,x,y,min(255,x+length),max(0,y-random.randrange(0,3)),c,0)
    save_png(ROOT/'textures/entity/dark_knight.png',256,256,p)


def make_egg():
    w=h=16
    transparent=(0,0,0,0)
    p=[[transparent for _ in range(w)] for _ in range(h)]
    # vanilla-like egg silhouette, dark steel with mint rune flecks
    shape=[(6,1,10),(4,2,12),(3,4,13),(2,6,14),(2,9,14),(3,12,13),(5,14,11)]
    for y,x0,x1 in shape:
        maxy=min(h,y+2)
        for yy in range(y,maxy):
            for x in range(x0,x1):
                if 0<=yy<h and 0<=x<w:
                    d=abs(x-8)+abs(yy-8)*0.35
                    p[yy][x]=(clamp(47-d*2),clamp(42-d),clamp(65-d),255)
    for x,y in [(6,5),(9,4),(5,9),(10,10),(8,7),(7,12)]:
        p[y][x]=(137,211,190,255)
    save_png(ROOT/'textures/item/dark_knight_spawn_egg.png',16,16,p)


def write_text(path, text):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(text, encoding='utf-8')


def make_json():
    write_text(ROOT/'lang/ru_ru.json', json.dumps({
        'entity.sleepless_knight.dark_knight':'Тёмный рыцарь',
        'item.sleepless_knight.dark_knight_spawn_egg':'Яйцо призыва Тёмного рыцаря'
    }, ensure_ascii=False, indent=2))
    write_text(ROOT/'lang/en_us.json', json.dumps({
        'entity.sleepless_knight.dark_knight':'Dark Knight',
        'item.sleepless_knight.dark_knight_spawn_egg':'Dark Knight Spawn Egg'
    }, indent=2))
    write_text(ROOT/'models/item/dark_knight_spawn_egg.json', json.dumps({
        'parent':'minecraft:item/generated',
        'textures':{'layer0':'sleepless_knight:item/dark_knight_spawn_egg'}
    }, indent=2))
    write_text(ROOT/'items/dark_knight_spawn_egg.json', json.dumps({
        'model':{'type':'minecraft:model','model':'sleepless_knight:item/dark_knight_spawn_egg'}
    }, indent=2))

make_entity(); make_egg(); make_json()
print('Generated Sleepless Knight entity texture, spawn egg texture and item data')
