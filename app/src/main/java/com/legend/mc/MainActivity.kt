package com.legend.mc

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.json.JSONArray
import org.json.JSONObject

// ===== البيانات =====
data class McCommand(val id:Int, val titleAr:String, val titleEn:String, val cmd:String, val cat:String, var fav:Boolean=false)
data class ModProject(val name:String, val type:String, val json:String)
data class WorldItem(val name:String, val path:String)

object DataStore {
    // 200 أمر - هنا عينة 20 والباقي بنكمله في الملف الثاني
    val defaultCommands = mutableListOf(
        McCommand(1,"وضع إبداع","Creative Mode","/gamemode creative","اللاعب / Player"),
        McCommand(2,"وضع بقاء","Survival Mode","/gamemode survival","اللاعب / Player"),
        McCommand(3,"وضع مشاهدة","Spectator","/gamemode spectator","اللاعب / Player"),
        McCommand(4,"اعطاء دايموند","Give Diamond","/give @p diamond 64","أدوات / Items"),
        McCommand(5,"طقس صافي","Clear Weather","/weather clear","العالم / World"),
        McCommand(6,"وقت نهار","Set Day","/time set day","العالم / World"),
        McCommand(7,"استدعاء تنين","Summon Dragon","/summon ender_dragon","وحوش / Mobs"),
        McCommand(8,"قتل الوحوش","Kill Mobs","/kill @e[type=!player]","وحوش / Mobs"),
        McCommand(9,"تأثير سرعة","Speed Effect","/effect @p speed 100 2","تأثيرات / Effects"),
        McCommand(10,"نقل آني","Teleport","/tp @p 0 100 0","اللاعب / Player"),
        McCommand(11,"إيقاف الوقت","Stop Time","/gamerule dodaylightcycle false","العالم / World"),
        McCommand(12,"حفظ المخزون","Keep Inventory","/gamerule keepinventory true","العالم / World"),
        McCommand(13,"انفجار","Explosion","/summon tnt","العالم / World"),
        McCommand(14,"اعطاء سيف نذرايت","Netherite Sword","/give @p netherite_sword 1","أدوات / Items"),
        McCommand(15,"درع دايموند","Diamond Armor","/give @p diamond_chestplate 1","دروع / Armor"),
        McCommand(16,"استدعاء حصان","Summon Horse","/summon horse","وحوش / Mobs"),
        McCommand(17,"مطر","Rain","/weather rain","العالم / World"),
        McCommand(18,"ليل","Night","/time set night","العالم / World"),
        McCommand(19,"شفاء","Heal","/effect @p instant_health 1 10","تأثيرات / Effects"),
        McCommand(20,"طيران","Fly","/ability @p mayfly true","اللاعب / Player")
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
    var worlds by remember { mutableStateOf(listOf(WorldItem("عالمي الأول / My World","/games/com.mojang/minecraftWorlds"))) }
    var themeColor by remember { mutableStateOf(Color(0xFF1A1A2E)) }

    fun t(ar:String, en:String) = if(isArabic) ar else en

    if(!unlocked) {
        // شاشة كلمة المرور
        Column(Modifier.fillMaxSize().background(themeColor).padding(24.dp), verticalArrangement = Arrangement.Center) {
            Text(t("🔒 تطبيق الأسطورة","🔒 Legend App"), color=Color.White, fontSize=28.sp, fontWeight=FontWeight.Bold)
            Spacer(Modifier.height(16.dp))
            OutlinedTextField(value=passInput, onValueChange={passInput=it}, label={Text(t("كلمة المرور","Password"))}, modifier=Modifier.fillMaxWidth())
            Spacer(Modifier.height(12.dp))
            Button(onClick={
                val saved = prefs.getString("pass", null)
                if(saved==null){ prefs.edit().putString("pass", passInput).apply(); unlocked=true }
                else if(saved==passInput) unlocked=true
                else Toast.makeText(ctx, t("كلمة مرور خاطئة","Wrong password"), Toast.LENGTH_SHORT).show()
            }, modifier=Modifier.fillMaxWidth()) { Text(t("دخول","Login")) }
            Spacer(Modifier.height(8.dp))
            Button(onClick={
                try{
                    val i = ctx.packageManager.getLaunchIntentForPackage("com.mojang.minecraftpe")
                    ctx.startActivity(i)
                }catch(e:Exception){ Toast.makeText(ctx, t("اللعبة غير مثبتة","Game not installed"), Toast.LENGTH_SHORT).show() }
            }, modifier=Modifier.fillMaxWidth(), colors=ButtonDefaults.buttonColors(containerColor=Color(0xFF00C853))) {
                Text(t("🎮 دخول للعبة مباشرة","🎮 Open Game Directly"))
            }
        }
        return
    }

    Scaffold(
        topBar={ TopAppBar(title={Text(t("MC الأسطورة - 200 أمر","MC Legend - 200 Commands"))},
            actions={
                TextButton(onClick={ isArabic=!isArabic; prefs.edit().putBoolean("arabic",isArabic).apply() }){ Text(if(isArabic) "EN" else "عربي", color=Color.White) }
            }, colors=TopAppBarDefaults.topAppBarColors(containerColor=Color(0xFF16213E), titleContentColor=Color.White)
        )},
        bottomBar={
            NavigationBar{
                NavigationBarItem(selected=tab==0, onClick={tab=0}, icon={}, label={Text(t("أوامر","Commands"))})
                NavigationBarItem(selected=tab==1, onClick={tab=1}, icon={}, label={Text(t("منشئ","Builder"))})
                NavigationBarItem(selected=tab==2, onClick={tab=2}, icon={}, label={Text(t("مودات","Mods"))})
                NavigationBarItem(selected=tab==3, onClick={tab=3}, icon={}, label={Text(t("عوالم","Worlds"))})
                NavigationBarItem(selected=tab==4, onClick={tab=4}, icon={}, label={Text(t("إعدادات","Settings"))})
            }
        }
    ){ pad ->
        Column(Modifier.padding(pad).padding(12.dp)) {
            when(tab){
                0 -> {
                    OutlinedTextField(value=search, onValueChange={search=it}, label={Text(t("🔍 ابحث عن أي أمر...","🔍 Search any command..."))}, modifier=Modifier.fillMaxWidth())
                    Row{ FilterChip(selected=showFavOnly, onClick={showFavOnly=!showFavOnly}, label={Text(t("⭐ المفضلة فقط","⭐ Favorites only"))}) }
                    LazyColumn{
                        items(commands.filter{
                            (!showFavOnly || it.fav) && (it.cmd.contains(search,true) || it.titleAr.contains(search) || it.titleEn.contains(search,true))
                        }){ c ->
                            Card(Modifier.fillMaxWidth().padding(vertical=4.dp).clickable{
                                val clip = ctx.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                                clip.setPrimaryClip(android.content.ClipData.newPlainText("cmd", c.cmd))
                                Toast.makeText(ctx, t("تم النسخ: ","Copied: ")+c.cmd, Toast.LENGTH_SHORT).show()
                            }){
                                Column(Modifier.padding(12.dp)){
                                    Text(if(isArabic) c.titleAr else c.titleEn, fontWeight=FontWeight.Bold)
                                    Text(c.cmd, color=Color(0xFF00C853))
                                    Row{
                                        TextButton(onClick={ commands = commands.map{ if(it.id==c.id) it.copy(fav=!it.fav) else it } }){ Text(if(c.fav) "❤️" else "🤍") }
                                        TextButton(onClick={ commands = commands.filter{ it.id!=c.id } }){ Text(t("🗑 حذف","🗑 Delete")) }
                                        TextButton(onClick={ Toast.makeText(ctx, t("تمت الإضافة للمفضلة","Added"), Toast.LENGTH_SHORT).show() }){ Text(t("نسخ","Copy")) }
                                    }
                                }
                            }
                        }
                    }
                }
                1 -> CommandBuilderScreen(isArabic)
                2 -> ModsScreen(isArabic, mods, onAdd={ mods = mods + it })
                3 -> WorldsScreen(isArabic, worlds)
                4 -> SettingsScreen(isArabic, onLang={isArabic=it}, onTheme={themeColor=it})
            }
        }
    }
}

@Composable fun CommandBuilderScreen(isArabic:Boolean){
    var target by remember{mutableStateOf("@p")}
    var item by remember{mutableStateOf("diamond")}
    var amount by remember{mutableStateOf("64")}
    var result by remember{mutableStateOf("")}
    Column(Modifier.verticalScroll(rememberScrollState())){
        Text(if(isArabic) "🛠 منشئ الأوامر التلقائي" else "🛠 Auto Command Builder", fontWeight=FontWeight.Bold, fontSize=20.sp)
        OutlinedTextField(value=target, onValueChange={target=it}, label={Text("Target @p / @a / @e")}, modifier=Modifier.fillMaxWidth())
        OutlinedTextField(value=item, onValueChange={item=it}, label={Text(if(isArabic) "العنصر" else "Item")}, modifier=Modifier.fillMaxWidth())
        OutlinedTextField(value=amount, onValueChange={amount=it}, label={Text(if(isArabic) "الكمية" else "Amount")}, modifier=Modifier.fillMaxWidth())
        Button(onClick={ result="/give $target $item $amount" }, modifier=Modifier.fillMaxWidth().padding(top=8.dp)){ Text(if(isArabic) "توليد الأمر" else "Generate") }
        if(result.isNotEmpty()){ Text(result, color=Color(0xFF00C853), fontWeight=FontWeight.Bold, modifier=Modifier.padding(top=12.dp)) }
    }
}

@Composable fun ModsScreen(isArabic:Boolean, mods:List<ModProject>, onAdd:(ModProject)->Unit){
    var name by remember{mutableStateOf("")}
    var json by remember{mutableStateOf("{\n  \"name\": \"My Armor\",\n  \"type\": \"armor\",\n  \"defense\": 10\n}")}
    Column(Modifier.verticalScroll(rememberScrollState())){
        Text(if(isArabic) "🎨 صنع مود / درع" else "🎨 Create Mod / Armor", fontWeight=FontWeight.Bold, fontSize=20.sp)
        OutlinedTextField(value=name, onValueChange={name=it}, label={Text(if(isArabic) "اسم المود" else "Mod Name")}, modifier=Modifier.fillMaxWidth())
        Text(if(isArabic) "محرر JSON:" else "JSON Editor:", modifier=Modifier.padding(top=12.dp))
        OutlinedTextField(value=json, onValueChange={json=it}, modifier=Modifier.fillMaxWidth().height(200.dp))
        Button(onClick={ onAdd(ModProject(name,"armor",json)) }, modifier=Modifier.fillMaxWidth().padding(top=8.dp)){ Text(if(isArabic) "💾 حفظ المشروع" else "💾 Save Project") }
        Button(onClick={ /* هنا نفتح مدير الملفات للاستيراد */ }, modifier=Modifier.fillMaxWidth()){ Text(if(isArabic) "📁 استيراد من مدير الملفات" else "📁 Import from Files") }
        mods.forEach{ Text("• ${it.name} - ${it.type}") }
    }
}

@Composable fun WorldsScreen(isArabic:Boolean, worlds:List<WorldItem>){
    Column{
        Text(if(isArabic) "🌍 إدارة العوالم" else "🌍 Manage Worlds", fontWeight=FontWeight.Bold, fontSize=20.sp)
        worlds.forEach{ w -> Card(Modifier.fillMaxWidth().padding(4.dp)){ Column(Modifier.padding(12.dp)){ Text(w.name); Text(w.path, fontSize=12.sp) } } }
        Text(if(isArabic) "يمكنك إضافة عالم جديد، نسخه، حذفه من هنا" else "Add, copy, delete worlds here", modifier=Modifier.padding(top=8.dp))
    }
}

@Composable fun SettingsScreen(isArabic:Boolean, onLang:(Boolean)->Unit, onTheme:(Color)->Unit){
    Column{
        Text(if(isArabic) "⚙️ الإعدادات / التخصيص" else "⚙️ Settings / Customize", fontWeight=FontWeight.Bold, fontSize=20.sp)
        Button(onClick={ onLang(!isArabic) }, modifier=Modifier.fillMaxWidth().padding(top=12.dp)){ Text(if(isArabic) "Switch to English" else "التبديل للعربية") }
        Text(if(isArabic) "لون الواجهة:" else "Theme Color:", modifier=Modifier.padding(top=12.dp))
        Row{ Button(onClick={onTheme(Color(0xFF1A1A2E))}){Text("Dark")} ; Spacer(Modifier.width(8.dp)); Button(onClick={onTheme(Color(0xFF0D47A1))}){Text("Blue")} ; Spacer(Modifier.width(8.dp)); Button(onClick={onTheme(Color(0xFF1B5E20))}){Text("Green")} }
    }
}

