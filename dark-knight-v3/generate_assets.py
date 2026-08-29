from pathlib import Path
import json, math, random, struct, zlib

ROOT = Path('src/main/resources/assets/sleepless_knight')
SIZE = 512
CELL = 64
random.seed(93173)


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


def blank(w, h, color=(28, 24, 38, 255)):
    return [[color for _ in range(w)] for _ in range(h)]


def put(p, x, y, c):
    if 0 <= y < len(p) and 0 <= x < len(p[0]):
        p[y][x] = tuple(clamp(v) if i < 3 else v for i, v in enumerate(c))


def line(p, x0, y0, x1, y1, c):
    steps = max(abs(x1-x0), abs(y1-y0), 1)
    for i in range(steps+1):
        x = round(x0 + (x1-x0)*i/steps)
        y = round(y0 + (y1-y0)*i/steps)
        put(p, x, y, c)


def steel_pixel(x, y, variant=0):
    # Readable blackened violet steel: dark, but never pure black.
    wave = 5.0 * math.sin(x * 0.20) + 3.5 * math.sin((x+y) * 0.095)
    vertical = (1.0 - (y % CELL) / (CELL - 1)) * 7.0
    grain = random.randint(-4, 4)
    if variant == 0:
        base = (47, 42, 62)
    elif variant == 1:
        base = (57, 50, 75)
    else:
        base = (39, 35, 54)
    return (
        clamp(base[0] + wave + vertical + grain),
        clamp(base[1] + wave * 0.55 + vertical * 0.45 + grain),
        clamp(base[2] + wave * 1.35 + vertical + grain),
        255
    )


def fill_cell(p, cx, cy, kind):
    x0, y0 = cx*CELL, cy*CELL
    for yy in range(y0, y0+CELL):
        for xx in range(x0, x0+CELL):
            lx, ly = xx-x0, yy-y0
            if kind == 'void':
                v = 9 + int(3*math.sin((lx+ly)*0.18))
                c = (v+2, v, v+8, 255)
            elif kind == 'steel':
                c = steel_pixel(xx, yy, 0)
            elif kind == 'steel_bright':
                c = steel_pixel(xx, yy, 1)
            elif kind == 'steel_dark':
                c = steel_pixel(xx, yy, 2)
            elif kind == 'leather':
                grain = random.randint(-5, 5)
                stripe = 4 if (lx // 7) % 2 == 0 else -2
                c = (clamp(63+grain+stripe), clamp(39+grain//2), clamp(32+grain//2), 255)
            elif kind == 'chain':
                base = 18 + random.randint(-3, 3)
                c = (base+2, base+1, base+8, 255)
            elif kind == 'blade':
                edge = min(lx, CELL-1-lx)
                sheen = max(0, 13 - abs(lx - CELL*0.54))
                n = random.randint(-3, 3)
                c = (clamp(73+n+sheen*1.6), clamp(69+n+sheen*1.2), clamp(94+n+sheen*2.0), 255)
            elif kind == 'glow':
                pulse = 12*math.sin((lx+ly)*0.17)
                c = (clamp(208+pulse), clamp(190+pulse*0.65), 255, 255)
            else:
                c = (40, 35, 55, 255)
            p[yy][xx] = c

    # Cell-local detailing, safely contained with 5px padding.
    if kind.startswith('steel'):
        # Edge bands and sparse scratches that read as plated armor.
        for k in range(2):
            y = y0 + 8 + k*41
            for x in range(x0+5, x0+59):
                r,g,b,a = p[y][x]
                p[y][x] = (clamp(r+18), clamp(g+15), clamp(b+23), a)
        for _ in range(12):
            sx = random.randint(x0+7, x0+50)
            sy = random.randint(y0+7, y0+54)
            ln = random.randint(3, 9)
            line(p, sx, sy, min(x0+57, sx+ln), max(y0+5, sy-random.randint(0,2)), (92, 84, 118, 255))
    elif kind == 'chain':
        for yy in range(y0+5, y0+59, 6):
            shift = 3 if ((yy-y0)//6) % 2 else 0
            for xx in range(x0+5+shift, x0+59, 6):
                for dx,dy,c in [
                    (0,0,(91,88,108,255)), (1,0,(56,53,69,255)),
                    (0,1,(46,44,59,255)), (1,1,(14,14,20,255))
                ]:
                    put(p, xx+dx, yy+dy, c)
    elif kind == 'leather':
        for yy in range(y0+9, y0+58, 13):
            line(p, x0+7, yy, x0+56, yy, (89, 57, 43, 255))
    elif kind == 'blade':
        # Purple steel edges and a cleaner center, matching the supplied sword.
        for yy in range(y0+4, y0+60):
            put(p, x0+7, yy, (111, 92, 145, 255))
            put(p, x0+56, yy, (96, 82, 126, 255))
        for _ in range(8):
            sx = random.randint(x0+12, x0+48)
            sy = random.randint(y0+8, y0+55)
            line(p, sx, sy, sx+random.randint(2,7), sy-1, (139, 129, 166, 255))


def make_entity():
    p = blank(SIZE, SIZE)
    # Material map follows the UV cells used in DarkKnightModel exactly.
    cells = {
        (0,0): 'void', (1,0): 'steel', (2,0): 'steel_bright', (3,0): 'steel_dark',
        (0,1): 'chain', (1,1): 'steel_bright', (2,1): 'steel', (3,1): 'steel_bright',
        (4,1): 'leather', (5,1): 'chain', (6,1): 'steel_dark', (7,1): 'glow',
        (0,2): 'chain', (1,2): 'steel', (2,2): 'steel_bright', (3,2): 'steel_dark',
        (0,3): 'chain', (1,3): 'steel', (2,3): 'steel_bright', (3,3): 'steel_dark',
        (4,3): 'leather', (5,3): 'steel_bright', (6,3): 'blade', (7,3): 'glow'
    }
    for cy in range(8):
        for cx in range(8):
            fill_cell(p, cx, cy, cells.get((cx,cy), 'steel_dark'))

    # Gentle violet highlight in top-row helmet cells.
    for cx in (1,2,3):
        x0 = cx*CELL
        for y in range(5, 59):
            x = x0 + 44 + int(3*math.sin(y*0.2))
            r,g,b,a = p[y][x]
            p[y][x] = (clamp(r+20), clamp(g+11), clamp(b+32), a)

    save_png(ROOT/'textures/entity/dark_knight.png', SIZE, SIZE, p)


def make_egg():
    w=h=16
    p=[[(0,0,0,0) for _ in range(w)] for _ in range(h)]
    spans = {
        1:(7,9), 2:(5,11), 3:(4,12), 4:(3,13), 5:(3,13), 6:(2,14), 7:(2,14),
        8:(2,14), 9:(2,14), 10:(3,13), 11:(3,13), 12:(4,12), 13:(5,11), 14:(7,9)
    }
    for y,(x0,x1) in spans.items():
        for x in range(x0,x1+1):
            d=abs(x-8)+abs(y-8)*0.30
            p[y][x]=(clamp(54-d*2), clamp(46-d), clamp(76-d), 255)
    for x,y in [(5,5),(9,4),(11,7),(6,9),(9,11),(5,12),(8,7)]:
        p[y][x]=(202,180,255,255)
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

make_entity()
make_egg()
make_json()
print('Generated 512x512 Dark Knight material atlas and spawn egg assets')
