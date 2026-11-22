package com.legend.mc

import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

data class McCommand(val id:Int, val titleAr:String, val titleEn:String, val cmd:String, val cat:String)
data class ModProject(val name:String, val type:String, val json:String)
data class WorldItem(val name:String, val path:String)
data class SkinItem(val name:String, val emoji:String, val color:Color)
data class InvItem(val name:String, val emoji:String)

object DataStore {
    val defaultCommands = mutableListOf(
        McCommand(1,"إبداع","Creative","/gamemode creative","Player"),
        McCommand(2,"بقاء","Survival","/gamemode survival","Player"),
        McCommand(3,"مغامرة","Adventure","/gamemode adventure","Player"),
        McCommand(4,"مشاهد","Spectator","/gamemode spectator","Player"),
        McCommand(5,"إعطاء","Give","/give @p diamond_sword 1","Items"),
        McCommand(6,"مسح","Clear","/clear @p","Items"),
        McCommand(7,"استدعاء","Summon","/summon zombie","Mobs"),
        McCommand(8,"تيليبورت","Teleport","/tp @p 0 100 0","Player"),
        McCommand(9,"قتل","Kill","/kill @e","Player"),
        McCommand(10,"طقس صافي","Clear Weather","/weather clear","World"),
        McCommand(11,"مطر","Rain","/weather rain","World"),
        McCommand(12,"عاصفة","Thunder","/weather thunder","World"),
        McCommand(13,"وقت نهار","Day","/time set day","World"),
        McCommand(14,"وقت ليل","Night","/time set night","World"),
        McCommand(15,"تأثير","Effect","/effect @p speed 30 2","Effects"),
        McCommand(16,"مسح تأثير","Clear Effect","/effect @p clear","Effects"),
        McCommand(17,"سحر","Enchant","/enchant @p sharpness 5","Items"),
        McCommand(18,"خبرة","XP","/xp 100L @p","Player"),
        McCommand(19,"وضع بلوك","Setblock","/setblock ~ ~ ~ diamond_block","World"),
        McCommand(20,"ملء","Fill","/fill ~ ~ ~ ~10 ~10 ~10 stone","World"),
        McCommand(21,"نسخ","Clone","/clone ~ ~ ~ ~10 ~10 ~10 ~20 ~ ~","World"),
        McCommand(22,"انفجار","Explosion","/summon tnt","World"),
        McCommand(23,"شفاء","Heal","/effect @p instant_health 1 10","Effects"),
        McCommand(24,"طيران","Fly","/ability @p mayfly true","Player"),
        McCommand(25,"نقطة البداية","Set World Spawn","/setworldspawn","World"),
        McCommand(26,"نقطة spawn","Spawnpoint","/spawnpoint @p","Player"),
        McCommand(27,"حفظ","Save","/save-all","World"),
        McCommand(28,"صعوبة سهلة","Easy","/difficulty easy","World"),
        McCommand(29,"صعوبة صعبة","Hard","/difficulty hard","World"),
        McCommand(30,"صعوبة مسالمة","Peaceful","/difficulty peaceful","World"),
        McCommand(31,"إيقاف الوقت","Stop Time","/gamerule doDaylightCycle false","World"),
        McCommand(32,"تشغيل الوقت","Start Time","/gamerule doDaylightCycle true","World"),
        McCommand(33,"حفظ الانفنتوري","Keep Inventory","/gamerule keepInventory true","World"),
        McCommand(34,"بدون سقوط","No Fall","/gamerule fallDamage false","World"),
        McCommand(35,"إظهار الإحداثيات","Show Coords","/gamerule showcoordinates true","Player"),
        McCommand(36,"توقف الموب","Stop Mobs","/gamerule doMobSpawning false","Mobs"),
        McCommand(37,"تشغيل الموب","Start Mobs","/gamerule doMobSpawning true","Mobs"),
        McCommand(38,"عنوان","Title","/title @p title Hello","Player"),
        McCommand(39,"رسالة","Say","/say Hello World","Player"),
        McCommand(40,"رسالة خاصة","Tell","/tell @p Hello","Player"),
        McCommand(41,"حظر","Ban","/ban @p","Player"),
        McCommand(42,"طرد","Kick","/kick @p","Player"),
        McCommand(43,"إلغاء حظر","Pardon","/pardon @p","Player"),
        McCommand(44,"مشغل","OP","/op @p","Player"),
        McCommand(45,"إلغاء مشغل","DeOP","/deop @p","Player"),
        McCommand(46,"قائمة بيضاء","Whitelist","/whitelist add @p","Player"),
        McCommand(47,"جسيمات","Particle","/particle minecraft:flame ~ ~ ~","World"),
        McCommand(48,"صوت","Sound","/playsound mob.cat.meow @p","World"),
        McCommand(49,"إيقاف صوت","Stop Sound","/stopsound @p","World"),
        McCommand(50,"استبدال عنصر","Replace Item","/replaceitem entity @p slot.armor.head diamond_helmet","Items"),
        McCommand(51,"تغيير كتلة","Set Block","/setblock ~ ~-1 ~ bedrock","World"),
        McCommand(52,"إسقاط","Drop","/give @p diamond 64","Items"),
        McCommand(53,"سيف دايموند","Diamond Sword","/give @p diamond_sword","Items"),
        McCommand(54,"سيف نذرايت","Netherite Sword","/give @p netherite_sword","Items"),
        McCommand(55,"بيكاكس","Pickaxe","/give @p diamond_pickaxe","Items"),
        McCommand(56,"درع","Armor","/give @p diamond_chestplate","Items"),
        McCommand(57,"تفاحة ذهبية","Golden Apple","/give @p golden_apple 64","Items"),
        McCommand(58,"اندر بيرل","Ender Pearl","/give @p ender_pearl 64","Items"),
        McCommand(59,"توتم","Totem","/give @p totem_of_undying 64","Items"),
        McCommand(60,"اليترا","Elytra","/give @p elytra","Items"),
        McCommand(61,"قوس","Bow","/give @p bow","Items"),
        McCommand(62,"سهم","Arrow","/give @p arrow 64","Items"),
        McCommand(63,"تي ان تي","TNT","/give @p tnt 64","Items"),
        McCommand(64,"سرج","Saddle","/give @p saddle","Items"),
        McCommand(65,"حصان","Horse","/summon horse","Mobs"),
        McCommand(66,"كريبر","Creeper","/summon creeper","Mobs"),
        McCommand(67,"زومبي","Zombie","/summon zombie","Mobs"),
        McCommand(68,"هيكل","Skeleton","/summon skeleton","Mobs"),
        McCommand(69,"عنكبوت","Spider","/summon spider","Mobs"),
        McCommand(70,"اندرمان","Enderman","/summon enderman","Mobs"),
        McCommand(71,"تنين","Dragon","/summon ender_dragon","Mobs"),
        McCommand(72,"وذر","Wither","/summon wither","Mobs"),
        McCommand(73,"واردن","Warden","/summon warden","Mobs"),
        McCommand(74,"قطة","Cat","/summon cat","Mobs"),
        McCommand(75,"كلب","Dog","/summon wolf","Mobs"),
        McCommand(76,"قرية","Village","/locate village","World"),
        McCommand(77,"معبد","Temple","/locate temple","World"),
        McCommand(78,"قلعة","Fortress","/locate fortress","World"),
        McCommand(79,"تحديد الكل","Select All","/tp @e @p","Player"),
        McCommand(80,"تجميد","Freeze","/effect @e slowness 100 255","Effects"),
        McCommand(81,"سرعة","Speed","/effect @p speed 100 5","Effects"),
        McCommand(82,"قفز عالي","Jump","/effect @p jump_boost 100 5","Effects"),
        McCommand(83,"اختفاء","Invisible","/effect @p invisibility 100 1","Effects"),
        McCommand(84,"قوة","Strength","/effect @p strength 100 5","Effects"),
        McCommand(85,"مقاومة","Resistance","/effect @p resistance 100 5","Effects"),
        McCommand(86,"رؤية ليلية","Night Vision","/effect @p night_vision 100 1","Effects"),
        McCommand(87,"تسمم","Poison","/effect @p poison 10 2","Effects"),
        McCommand(88,"بطء","Slowness","/effect @p slowness 10 2","Effects"),
        McCommand(89,"نار","Fire","/effect @p fire_resistance 100 1","Effects"),
        McCommand(90,"تنفس تحت الماء","Water Breathing","/effect @p water_breathing 100 1","Effects"),
        McCommand(91,"تغيير الاسم","Rename","/tag @p add Legend","Player"),
        McCommand(92,"تغيير الوقت","Time Add","/time add 1000","World"),
        McCommand(93,"تجميد العالم","Freeze World","/tickingarea add circle ~ ~ ~ 10","World"),
        McCommand(94,"تفعيل البلوك","Activate","/execute @p ~ ~ ~ summon lightning_bolt","World"),
        McCommand(95,"صاعقة","Lightning","/summon lightning_bolt","World"),
        McCommand(96,"انفجار ناري","Fireball","/summon fireball","World"),
        McCommand(97,"إيقاف المطر","Stop Rain","/weather clear","World"),
        McCommand(98,"تغيير الحجم","Scale","/attribute @p scale base set 2","Player"),
        McCommand(99,"صحة كاملة","Full Health","/effect @p instant_health 1 255","Effects"),
        McCommand(100,"كل شيء","Give All","/give @p diamond_block 64","Items")
    )
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { LegendApp() }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LegendApp() {
    val ctx = LocalContext.current
    val prefs = remember { ctx.getSharedPreferences("legend", Context.MODE_PRIVATE) }
    var isArabic by remember { mutableStateOf(prefs.getBoolean("arabic", true)) }
    var unlocked by remember { mutableStateOf(prefs.getString("pass", null) == null) }
    var passInput by remember { mutableStateOf("") }
    var tab by remember { mutableStateOf(0) }
    var search by remember { mutableStateOf("") }
    var showFavOnly by remember { mutableStateOf(false) }
    var commands by remember { mutableStateOf(DataStore.defaultCommands.toList()) }
    var mods by remember { mutableStateOf(listOf<ModProject>()) }
    var worlds by remember { mutableStateOf(listOf(WorldItem("My World","/games/com.mojang/minecraftWorlds"))) }
    var themeColor by remember { mutableStateOf(Color(0xFF1A1A1A)) }
    var showSplash by remember { mutableStateOf(true) }
    var progress by remember { mutableStateOf(0) }
    var selectedSkin by remember { mutableStateOf("Steve") }
    var inventory by remember { mutableStateOf(listOf<InvItem>()) }
    fun t(ar:String, en:String) = if(isArabic) ar else en
    if(showSplash) {
        LaunchedEffect(Unit) {
            for(i in 0..100 step 5){ progress = i; delay(80) }
            delay(300); showSplash = false
        }
        Box(Modifier.fillMaxSize().background(Color(0xFF1B1B1B)), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(24.dp)) {
                Text("MC", color = Color(0xFF7CFC00), fontSize = 70.sp, fontWeight = FontWeight.Black)
                Text("LEGEND", color = Color.White, fontSize = 42.sp, fontWeight = FontWeight.Black)
                Spacer(Modifier.height(8.dp))
                Text("إصدار الأسطورة • LEGEND EDITION", color = Color(0xFFFFC107), fontSize = 14.sp)
                Spacer(Modifier.height(24.dp))
                Box(Modifier.fillMaxWidth().height(28.dp).background(Color.Black, RoundedCornerShape(8.dp))) {
                    Box(Modifier.fillMaxWidth(progress/100f).height(28.dp).background(Color(0xFF7CFC00), RoundedCornerShape(8.dp)))
                    Text("$progress%", color = Color.White, modifier = Modifier.align(Alignment.Center), fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.height(16.dp))
                Text(if(progress<100) "جاري تحميل العالم... Loading..." else "جاهز!", color = Color.White)
                Spacer(Modifier.height(24.dp))
                Button(onClick = { showSplash = false }, colors = ButtonDefaults.buttonColors(Color(0xFF4CAF50)), modifier = Modifier.fillMaxWidth().height(56.dp), shape = RoundedCornerShape(4.dp)) {
                    Text("▶  PLAY MINECRAFT", fontSize = 20.sp, fontWeight = FontWeight.Black, color = Color.White)
                }
                Spacer(Modifier.height(12.dp))
                Text("v1.0 • MC LEGEND", color = Color.Gray, fontSize = 12.sp)
            }
        }
        return
    }
    if(!unlocked) {
        Column(Modifier.fillMaxSize().background(themeColor).padding(24.dp), verticalArrangement = Arrangement.Center) {
            Text("🔐 MC Legend App", color = Color.White, fontSize = 28.sp)
            Spacer(Modifier.height(16.dp))
            OutlinedTextField(value = passInput, onValueChange = { passInput = it }, label = { Text(t("كلمة السر","Password")) }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(12.dp))
            Button(onClick = {
                val saved = prefs.getString("pass", null)
                if(saved==null){ prefs.edit().putString("pass", passInput).apply(); unlocked = true }
                else if(saved==passInput) unlocked = true
                else Toast.makeText(ctx, t("كلمة خاطئة","Wrong password"), Toast.LENGTH_LONG).show()
            }, modifier = Modifier.fillMaxWidth()) { Text(t("دخول","Login")) }
            Spacer(Modifier.height(8.dp))
            Button(onClick = {
                try { val i = ctx.packageManager.getLaunchIntentForPackage("com.mojang.minecraftpe"); ctx.startActivity(i) }
                catch(e:Exception){ Toast.makeText(ctx, "Game not installed", Toast.LENGTH_SHORT).show() }
            }, colors = ButtonDefaults.buttonColors(Color(0xFF4CAF50)), modifier = Modifier.fillMaxWidth()) { Text("▶  Open Game Directly") }
        }
        return
    }
    Scaffold(
        topBar = { TopAppBar(title = { Text("⛏️ MC Legend - 100 Commands") }, actions = { TextButton(onClick = { isArabic = !isArabic; prefs.edit().putBoolean("arabic", isArabic).apply() }) { Text(if(isArabic) "EN" else "ع") } }, colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF121212), titleContentColor = Color.White, actionContentColor = Color.White)) },
        bottomBar = {
            NavigationBar(containerColor = Color(0xFF121212)) {
                NavigationBarItem(selected = tab==0, onClick = { tab=0 }, icon = { Text("⌨️") }, label = { Text(t("أوامر","Commands"), color=Color.White) })
                NavigationBarItem(selected = tab==1, onClick = { tab=1 }, icon = { Text("🛠️") }, label = { Text(t("منشئ","Builder"), color=Color.White) })
                NavigationBarItem(selected = tab==2, onClick = { tab=2 }, icon = { Text("🧩") }, label = { Text(t("مودات","Mods"), color=Color.White) })
                NavigationBarItem(selected = tab==3, onClick = { tab=3 }, icon = { Text("👤") }, label = { Text(t("سكنات","Skins"), color=Color.White) })
                NavigationBarItem(selected = tab==4, onClick = { tab=4 }, icon = { Text("🎒") }, label = { Text(t("انفنتوري","Inventory"), color=Color.White) })
                NavigationBarItem(selected = tab==5, onClick = { tab=5 }, icon = { Text("⚙️") }, label = { Text(t("إعدادات","Settings"), color=Color.White) })
            }
        }
    ) { pad ->
        Column(Modifier.padding(pad).padding(12.dp)) {
            when(tab) {
                0 -> {
                    OutlinedTextField(value = search, onValueChange = { search = it }, label = { Text(t("ابحث عن أمر...","Search command...")) }, modifier = Modifier.fillMaxWidth())
                    Row { FilterChip(selected = showFavOnly, onClick = { showFavOnly = !showFavOnly }, label = { Text(t("المفضلة فقط","Favs Only")) }) }
                    LazyColumn {
                        items(commands.filter { it.cmd.contains(search, true) || it.titleAr.contains(search) || it.titleEn.contains(search, true) }) { c ->
                            Card(Modifier.fillMaxWidth().padding(vertical = 4.dp).clickable {
                                val clip = ctx.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                                clip.setPrimaryClip(android.content.ClipData.newPlainText("cmd", c.cmd))
                                Toast.makeText(ctx, t("تم النسخ: ","Copied: ") + c.cmd, Toast.LENGTH_SHORT).show()
                            }, colors = CardDefaults.cardColors(Color(0xFF2A2A2A))) {
                                Column(Modifier.padding(12.dp)) {
                                    Text(if(isArabic) c.titleAr else c.titleEn, color = Color.White, fontWeight = FontWeight.Bold)
                                    Text(c.cmd, color = Color(0xFF0FF0C553))
                                    Row { TextButton(onClick = { Toast.makeText(ctx, t("تم النسخ","Copied"), Toast.LENGTH_SHORT).show() }) { Text(t("نسخ","Copy")) } }
                                }
                            }
                        }
                    }
                }
                1 -> CommandBuilderScreen(isArabic)
                2 -> ModsScreenNew(isArabic, mods, onAdd = { mods = mods + it })
                3 -> SkinsScreen(isArabic, selectedSkin, onSelect = { selectedSkin = it })
                4 -> InventoryScreen(isArabic, inventory, onAdd = { inventory = inventory + it })
                5 -> SettingsScreen(isArabic, onLang = { isArabic = it }, onTheme = { themeColor = it })
            }
        }
    }
}

@Composable
fun CommandBuilderScreen(isArabic:Boolean) {
    var target by remember { mutableStateOf("@p") }
    var item by remember { mutableStateOf("diamond_sword") }
    var amount by remember { mutableStateOf("64") }
    var result by remember { mutableStateOf("") }
    val scroll = rememberScrollState()
    Column(Modifier.verticalScroll(scroll)) {
        Text(if(isArabic) "🛠️ منشئ الأوامر التلقائي" else "Auto Command Builder", fontWeight = FontWeight.Bold, fontSize = 20.sp)
        OutlinedTextField(value = target, onValueChange = { target = it }, label = { Text("Target") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = item, onValueChange = { item = it }, label = { Text("Item") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = amount, onValueChange = { amount = it }, label = { Text("Amount") }, modifier = Modifier.fillMaxWidth())
        Button(onClick = { result = "/give $target $item $amount" }, modifier = Modifier.fillMaxWidth()) { Text(if(isArabic) "توليد الأمر" else "Generate") }
        if(result.isNotEmpty(0xFFFFC107) Text(result, color = Color(), fontWeight = FontWeight.Bold)
    }
}

@Composable
fun ModsScreenNew(isArabic:Boolean, mods:List<ModProject>, onAdd:(ModProject)->Unit) {
    var name by remember { mutableStateOf("") }
    var json by remember { mutableStateOf("") }
    var search by remember { mutableStateOf("") }
    val featured = listOf("One Block 🧱" to "عالم بلوكة واحدة", "Lucky Block 🍀" to "بلوكة الحظ", "Guns Mod 🔫" to "مود الأسلحة", "Cars Mod 🚗" to "مود السيارات", "Dragon Mod 🐉" to "مود التنين", "Herobrine 👻" to "هيروبراين")
    Column {
        Text(if(isArabic) "🧩 المودات - Mods" else "Mods", fontWeight = FontWeight.Bold, fontSize = 22.sp)
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(value = search, onValueChange = { search = it }, label = { Text("🔍  Search for Mods... ابحث عن مود") }, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(8.dp))
        LazyColumn {
            items(featured.filter { it.first.contains(search, true) || it.second.contains(search) }) { (title, desc) ->
                Card(Modifier.fillMaxWidth().padding(vertical = 4.dp), colors = CardDefaults.cardColors(Color(0xFF2D2D2D))) {
                    Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text("🧩", fontSize = 32.sp)
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) { Text(title, color = Color.White, fontWeight = FontWeight.Bold); Text(desc, color = Color.Gray, fontSize = 12.sp) }
                        Button(onClick = { onAdd(ModProject(title, "Mod", "{}")) }, colors = ButtonDefaults.buttonColors(Color(0xFF4CAF50))) { Text("⬇️") }
                    }
                }
            }
            items(mods) { m -> Text("✅ ${m.name}", color = Color.White) }
        }
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("اسم المود") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = json, onValueChange = { json = it }, label = { Text("JSON") }, modifier = Modifier.fillMaxWidth())
        Button(onClick = { onAdd(ModProject(name, "Custom", json)) }, modifier = Modifier.fillMaxWidth()) { Text("إضافة مود") }
    }
}

@Composable
fun SkinsScreen(isArabic:Boolean, selected:String, onSelect:(String)->Unit) {
    val skins = listOf(SkinItem("Steve","🧑", Color(0xFF00BCD4)), SkinItem("Alex","👧", Color(0xFFE91E63)), SkinItem("Herobrine","👻", Color.White), SkinItem("Ninja","🥷", Color.Black), SkinItem("Knight","⚔️", Color(0xFF9E9E9E)), SkinItem("Dragon","🐉", Color(0xFF4CAF50)))
    Column {
        Text(if(isArabic) "👤 اختر سكنك" else "Choose Your Skin", fontWeight = FontWeight.Bold, fontSize = 22.sp)
        Text(if(isArabic) "السكن المختار: $selected" else "Selected: $selected", color = Color(0xFF4CAF50))
        Spacer(Modifier.height(12.dp))
        LazyVerticalGrid(columns = GridCells.Fixed(2), modifier = Modifier.fillMaxSize()) {
            items(skins) { s ->
                Card(Modifier.padding(6.dp).clickable { onSelect(s.name) }, colors = CardDefaults.cardColors(if(selected==s.name) Color(0xFF4CAF50) else Color(0xFF2A2A2A))) {
                    Column(Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(s.emoji, fontSize = 48.sp); Text(s.name, color = Color.White, fontWeight = FontWeight.Bold)
                        if(selected==s.name) Text("✅ مختار", color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
fun InventoryScreen(isArabic:Boolean, inventory:List<InvItem>, onAdd:(InvItem)->Unit) {
    val all = listOf(InvItem("Diamond Sword","⚔️"), InvItem("Diamond Pickaxe","⛏️"), InvItem("Golden Apple","🍎"), InvItem("Ender Pearl","🔮"), InvItem("Diamond Armor","🛡️"), InvItem("TNT","💣"), InvItem("Bow","🏹"), InvItem("Elytra","🪽"), InvItem("Totem","🗿"))
    Column {
        Text(if(isArabic) "🎒 الانفنتوري - Inventory" else "Inventory", fontWeight = FontWeight.Bold, fontSize = 22.sp)
        Text(if(isArabic) "عندك ${inventory.size} اغراض" else "You have ${inventory.size} items", color = Color.Gray)
        Spacer(Modifier.height(12.dp))
        LazyVerticalGrid(columns = GridCells.Fixed(3), modifier = Modifier.weight(1f)) {
            items(all) { item ->
                Card(Modifier.padding(4.dp).clickable { onAdd(item) }, colors = CardDefaults.cardColors(Color(0xFF3A3A3A))) {
                    Column(Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) { Text(item.emoji, fontSize = 32.sp); Text(item.name, color = Color.White, fontSize = 11.sp, textAlign = TextAlign.Center) }
                }
            }
        }
        Text(if(isArabic) "اغراضك:" else "Your Items:", fontWeight = FontWeight.Bold)
        Text(inventory.joinToString(", ") { it.name }, color = Color(0xFFFFC107))
    }
}

@Composable
fun SettingsScreen(isArabic:Boolean, onLang:(Boolean)->Unit, onTheme:(Color)->Unit) {
    Column {
        Text(if(isArabic) "الإعدادات" else "Settings", fontWeight = FontWeight.Bold, fontSize = 22.sp)
        Button(onClick = { onLang(!isArabic) }, modifier = Modifier.fillMaxWidth()) { Text(if(isArabic) "تغيير اللغة - Change Language" else "Change Language") }
        Text(if(isArabic) "لون الثيم:" else "Theme Color:")
        Row { Button(onClick = { onTheme(Color(0xFF1A1A1A)) }) { Text("⬛") }; Button(onClick = { onTheme(Color(0xFF0D47A1)) }) { Text("🟦") }; Button(onClick = { onTheme(Color(0xFF4CAF50)) }) { Text("🟩") } }
    }
}
