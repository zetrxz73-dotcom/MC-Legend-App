@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
package com.legend.mc

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class Command(val name: String, val cmd: String, val desc: String, val descAr: String, val cat: String)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { MCApp() }
    }
}

@Composable
fun MCApp() {
    var isArabic by remember { mutableStateOf(false) }
    var unlocked by remember { mutableStateOf(false) }
    var password by remember { mutableStateOf("") }
    var search by remember { mutableStateOf("") }
    var selectedCat by remember { mutableStateOf("All") }
    var selected by remember { mutableStateOf<Command?>(null) }
    var favorites by remember { mutableStateOf(setOf<String>()) }

    if (!unlocked) {
        Box(modifier = Modifier.fillMaxSize().background(Color(0xFF121212)), contentAlignment = Alignment.Center) {
            Card(modifier = Modifier.padding(20.dp).fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E))) {
                Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("⛏️ MC Legend", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(if (isArabic) "اكتب كلمة السر للدخول" else "Enter password", color = Color.Gray)
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(value = password, onValueChange = { password = it }, label = { Text("Password") }, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(onClick = { if (password == "1234") unlocked = true }, modifier = Modifier.fillMaxWidth()) {
                        Text(if (isArabic) "دخول" else "Unlock")
                    }
                    TextButton(onClick = { isArabic = !isArabic }) { Text(if (isArabic) "English" else "عربي") }
                    Text("Pass: 1234", color = Color.Gray, fontSize = 12.sp)
                }
            }
        }
        return
    }

    val all = allCommands()
    val cats = listOf("All") + all.map { it.cat }.distinct()
    val filtered = all.filter {
        (selectedCat == "All" || it.cat == selectedCat) &&
        (search.isEmpty() || it.name.contains(search, true) || it.cmd.contains(search, true))
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("⛏️ MC Legend") },
                actions = { TextButton(onClick = { isArabic = !isArabic }) { Text(if (isArabic) "EN" else "ع", color = Color.White) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF121212), titleContentColor = Color.White)
            )
        },
        containerColor = Color(0xFF121212)
    ) { pad ->
        Column(modifier = Modifier.padding(pad).padding(12.dp).fillMaxSize()) {
            OutlinedTextField(value = search, onValueChange = { search = it }, label = { Text(if (isArabic) "ابحث..." else "Search...") }, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                cats.forEach { cat ->
                    FilterChip(
                        selected = selectedCat == cat,
                        onClick = { selectedCat = cat },
                        label = { Text(cat) }
                    )
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(filtered) { c ->
                    Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)), modifier = Modifier.fillMaxWidth().clickable { selected = c }) {
                        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(c.name, color = Color.White, fontWeight = FontWeight.Bold)
                                Text(if (isArabic) c.descAr else c.desc, color = Color.Gray, fontSize = 12.sp)
                                Text(c.cmd, color = Color(0xFF4CAF50), fontSize = 12.sp)
                            }
                            Text(if (favorites.contains(c.cmd)) "⭐" else "☆", fontSize = 22.sp, modifier = Modifier.clickable {
                                favorites = if (favorites.contains(c.cmd)) favorites - c.cmd else favorites + c.cmd
                            })
                        }
                    }
                }
            }
        }
    }

    if (selected != null) {
        AlertDialog(
            onDismissRequest = { selected = null },
            title = { Text(selected!!.name) },
            text = { Text(selected!!.cmd + "\n\n" + if (isArabic) selected!!.descAr else selected!!.desc) },
            confirmButton = { TextButton(onClick = { selected = null }) { Text("OK") } }
        )
    }
}

fun allCommands(): List<Command> {
    return listOf(
        Command("Gamemode Creative", "/gamemode creative", "Creative mode", "وضع الابداع", "Game"),
        Command("Gamemode Survival", "/gamemode survival", "Survival mode", "وضع البقاء", "Game"),
        Command("Give Diamond Sword", "/give @p diamond_sword", "Get diamond sword", "سيف دايموند", "Items"),
        Command("Give Diamonds", "/give @p diamond 64", "Get 64 diamonds", "64 دايموند", "Items"),
        Command("Teleport", "/tp @p ~ ~ ~", "Teleport", "انتقال", "Game"),
        Command("Time Day", "/time set day", "Set day", "وقت نهار", "World"),
        Command("Time Night", "/time set night", "Set night", "وقت ليل", "World"),
        Command("Weather Clear", "/weather clear", "Clear weather", "جو صافي", "World"),
        Command("Weather Rain", "/weather rain", "Rain", "مطر", "World"),
        Command("Summon Zombie", "/summon zombie", "Summon zombie", "استدعاء زومبي", "Mobs"),
        Command("Summon Creeper", "/summon creeper", "Summon creeper", "استدعاء كريبر", "Mobs"),
        Command("Kill All", "/kill @e", "Kill all entities", "قتل الكل", "Game"),
        Command("Heal", "/effect give @p regeneration 10 2", "Heal", "علاج", "Game"),
        Command("Speed", "/effect give @p speed 30 2", "Speed", "سرعة", "Game"),
        Command("Fly - Elytra", "/give @p elytra", "Get elytra to fly", "اجنحة طيران", "Items"),
        Command("Clear Mobs", "/kill @e[type=!player]", "Clear mobs", "مسح الوحوش", "Mobs"),
        Command("Give Gold", "/give @p gold_ingot 64", "Gold", "ذهب", "Items"),
        Command("Give Iron", "/give @p iron_ingot 64", "Iron", "حديد", "Items"),
        Command("Give Enchanted Apple", "/give @p enchanted_golden_apple 10", "Enchanted apple", "تفاحة ذهبية", "Items"),
        Command("Set Spawn", "/setworldspawn", "Set spawn", "تعيين البداية", "World")
    )
}

                    Spacer(Modifier.height(9.dp))

                    Row(
                        Modifier.fillMaxWidth().horizontalScroll(
                            rememberScrollState()
                        ),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        categories.forEach { category ->
                            FilterChip(
                                selected = selectedCat == category,
                                onClick = { selectedCat = category },
                                label = {
                                    Text(categoryLabel(category, isArabic))
                                }
                            )
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    Text(
                        "${visibleCommands.size} / ${commands.size}",
                        color = Purple,
                        fontSize = 12.sp
                    )

                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(bottom = 12.dp)
                    ) {
                        items(visibleCommands, key = { it.id }) { command ->
                            CommandCard(
                                command = command,
                                isArabic = isArabic,
                                favorite = favorites.contains(command.id),
                                onFavorite = {
                                    favorites =
                                        if (favorites.contains(command.id))
                                            favorites - command.id
                                        else favorites + command.id
                                    saveAll()
                                },
                                onCopy = { copyCommand(command) },
                                onEdit = {
                                    editing = command
                                    showCommandDialog = true
                                },
                                onDelete = {
                                    commands = commands.filterNot {
                                        it.id == command.id
                                    }
                                    favorites = favorites - command.id
                                    saveAll()
                                }
                            )
                        }
                    }
                }

                1 -> {
                    SectionTitle(
                        if (isArabic)
                            "الأوامر المفضلة ⭐"
                        else "Favorite commands ⭐"
                    )

                    val favCommands =
                        commands.filter { favorites.contains(it.id) }

                    if (favCommands.isEmpty()) {
                        EmptyMessage(
                            if (isArabic)
                                "لم تضف أوامر للمفضلة بعد"
                            else "No favorites yet"
                        )
                    }

                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(favCommands, key = { it.id }) { command ->
                            CommandCard(
                                command,
                                isArabic,
                                true,
                                onFavorite = {
                                    favorites = favorites - command.id
                                    saveAll()
                                },
                                onCopy = { copyCommand(command) },
                                onEdit = {
                                    editing = command
                                    showCommandDialog = true
                                },
                                onDelete = {
                                    commands = commands.filterNot {
                                        it.id == command.id
                                    }
                                    favorites = favorites - command.id
                                    saveAll()
                                }
                            )
                        }
                    }
                }

                2 -> {
                    SectionTitle(
                        if (isArabic)
                            "آخر الأوامر المنسوخة"
                        else "Recently copied"
                    )

                    if (history.isEmpty()) {
                        EmptyMessage(
                            if (isArabic)
                                "لا يوجد سجل حتى الآن"
                            else "History is empty"
                        )
                    }

                    LazyColumn {
                        items(history) { cmd ->
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = Panel
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                            ) {
                                Row(
                                    Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        cmd,
                                        color = Green,
                                        modifier = Modifier.weight(1f),
                                        fontSize = 12.sp
                                    )

                                    TextButton(onClick = {
                                        val clip =
                                            context.getSystemService(
                                                Context.CLIPBOARD_SERVICE
                                            ) as ClipboardManager

                                        clip.setPrimaryClip(
                                            ClipData.newPlainText(
                                                "Minecraft command",
                                                cmd
                                            )
                                        )

                                        toast(
                                            context,
                                            if (isArabic) "تم النسخ" else "Copied"
                                        )
                                    }) {
                                        Text(
                                            if (isArabic) "نسخ" else "Copy"
                                        )
                                    }
                                }
                            }
                        }
                    }

                    TextButton(
                        onClick = {
                            history = emptyList()
                            saveAll()
                        }
                    ) {
                        Text(
                            if (isArabic)
                                "مسح السجل"
                            else "Clear history"
                        )
                    }
                }

                3 -> {
                    SectionTitle(
                        if (isArabic)
                            "مدير المودات والإضافات"
                        else "Add-on manager"
                    )

                    Text(
                        if (isArabic)
                            "استورد ملفات mcpack أو mcaddon لفتحها باستخدام Minecraft، أو أنشئ إضافة تجريبية خاصة بك."
                        else
                            "Import mcpack or mcaddon files, or create a starter add-on.",
                        color = Color.LightGray,
                        fontSize = 12.sp
                    )

                    Spacer(Modifier.height(10.dp))

                    Button(
                        onClick = {
                            importLauncher.launch(arrayOf("*/*"))
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            if (isArabic)
                                "＋ استيراد ملف مود"
                            else "＋ Import add-on"
                        )
                    }

                    Spacer(Modifier.height(8.dp))

                    Text(
                        if (isArabic)
                            "الملفات المستوردة"
                        else "Imported files",
                        color = Purple,
                        fontWeight = FontWeight.Bold
                    )

                    if (importedMods.isEmpty()) {
                        EmptyMessage(
                            if (isArabic)
                                "لم تستورد ملفات بعد"
                            else "No imported files"
                        )
                    }

                    importedMods.forEach { raw ->
                        val uri = Uri.parse(raw)

                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = Panel
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp)
                        ) {
                            Row(
                                Modifier.padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    uri.lastPathSegment ?: "Addon file",
                                    color = Color.White,
                                    modifier = Modifier.weight(1f),
                                    fontSize = 11.sp
                                )

                                TextButton(
                                    onClick = {
                                        openUri(context, uri)
                                    }
                                ) {
                                    Text(
                                        if (isArabic) "فتح" else "Open"
                                    )
                                }

                                TextButton(
                                    onClick = {
                                        importedMods =
                                            importedMods - raw

                                        prefs.edit()
                                            .putString(
                                                "imported_mods",
                                                importedMods.joinToString("\n")
                                            )
                                            .apply()
                                    }
                                ) {
                                    Text("×")
                                }
                            }
                        }
                    }

                    HorizontalDivider(
                        Modifier.padding(vertical = 10.dp),
                        color = Color.DarkGray
                    )

                    SectionTitle(
                        if (isArabic)
                            "إنشاء إضافة تجريبية"
                        else "Create starter add-on"
                    )

                    OutlinedTextField(
                        addonName,
                        { addonName = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = {
                            Text(
                                if (isArabic)
                                    "اسم الإضافة"
                                else "Add-on name"
                            )
                        },
                        singleLine = true
                    )

                    OutlinedTextField(
                        functionName,
                        { functionName = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = {
                            Text(
                                if (isArabic)
                                    "اسم ملف الوظيفة"
                                else "Function file name"
                            )
                        },
                        singleLine = true
                    )

                    OutlinedTextField(
                        functionText,
                        { functionText = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 100.dp),
                        label = {
                            Text(
                                if (isArabic)
                                    "أوامر الوظيفة (كل أمر في سطر)"
                                else "Function commands"
                            )
                        }
                    )

                    Button(
                        onClick = {
                            val safe = addonName.trim()
                                .ifBlank { "MC_Legend_Addon" }
                                .replace(
                                    Regex("[^A-Za-z0-9_-]"),
                                    "_"
                                )

                            exportLauncher.launch("$safe.mcaddon")
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            if (isArabic)
                                "تصدير MCADDON"
                            else "Export MCADDON"
                        )
                    }

                    Text(
                        if (isArabic)
                            "هذه أداة بداية تنشئ حزمة Behavior Pack بسيطة بملف manifest ووظيفة mcfunction."
                        else
                            "Creates a starter Behavior Pack with manifest and mcfunction.",
                        color = Color.Gray,
                        fontSize = 10.sp
                    )
                }

                else -> {
                    SectionTitle(
                        if (isArabic) "الإعدادات" else "Settings"
                    )

                    Button(
                        onClick = {
                            val launch =
                                context.packageManager
                                    .getLaunchIntentForPackage(
                                        "com.mojang.minecraftpe"
                                    )

                            if (launch != null) {
                                context.startActivity(launch)
                            } else {
                                toast(
                                    context,
                                    if (isArabic)
                                        "لم يتم العثور على Minecraft"
                                    else "Minecraft not found"
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            if (isArabic)
                                "🎮 فتح Minecraft مباشرة"
                            else "🎮 Launch Minecraft"
                        )
                    }

                    OutlinedButton(
                        onClick = {
                            showPasswordDialog = true
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            if (isArabic)
                                "🔐 تغيير كلمة المرور"
                            else "🔐 Change password"
                        )
                    }

                    OutlinedButton(
                        onClick = {
                            isArabic = !isArabic
                            prefs.edit()
                                .putBoolean("arabic", isArabic)
                                .apply()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            if (isArabic)
                                "تغيير اللغة إلى English"
                            else "Change language to العربية"
                        )
                    }

                    Spacer(Modifier.height(12.dp))

                    Text(
                        if (isArabic) "عن التطبيق" else "About",
                        color = Purple,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        "MC Legend • Bedrock Tools",
                        color = Color.White
                    )

                    Text(
                        if (isArabic)
                            "الأوامر تُنسخ فقط ولا تُنفذ تلقائياً. الصقها داخل عالمك مع تفعيل Cheats. كلمة المرور محلية وبسيطة وليست تشفيراً أمنياً."
                        else
                            "Commands are copied, not executed automatically. Paste them in your world with Cheats enabled.",
                        color = Color.Gray,
                        fontSize = 11.sp
                    )
                }
            }
        }
    }

    if (showCommandDialog) {
        CommandEditorDialog(
            initial = editing,
            isArabic = isArabic,
            onDismiss = {
                showCommandDialog = false
                editing = null
            },
            onSave = { name, cmd, desc, descAr, cat ->
                val item = Command(
                    editing?.id ?: UUID.randomUUID().toString(),
                    name,
                    cmd,
                    desc,
                    descAr,
                    cat
                )

                commands =
                    if (editing == null)
                        commands + item
                    else
                        commands.map {
                            if (it.id == item.id) item else it
                        }

                saveAll()
                showCommandDialog = false
                editing = null
            }
        )
    }

    if (showPasswordDialog) {
        AlertDialog(
            onDismissRequest = {
                showPasswordDialog = false
            },
            title = {
                Text(
                    if (isArabic)
                        "تغيير كلمة المرور"
                    else "Change password"
                )
            },
            text = {
                Column {
                    OutlinedTextField(
                        newPassword,
                        { newPassword = it },
                        label = {
                            Text(
                                if (isArabic)
                                    "كلمة المرور الجديدة"
                                else "New password"
                            )
                        },
                        visualTransformation =
                            PasswordVisualTransformation()
                    )

                    OutlinedTextField(
                        confirmPassword,
                        { confirmPassword = it },
                        label = {
                            Text(
                                if (isArabic)
                                    "تأكيد كلمة المرور"
                                else "Confirm password"
                            )
                        },
                        visualTransformation =
                            PasswordVisualTransformation()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (newPassword.length < 4) {
                            toast(
                                context,
                                if (isArabic)
                                    "اجعل كلمة المرور 4 أحرف على الأقل"
                                else "Use at least 4 characters"
                            )
                        } else if (newPassword != confirmPassword) {
                            toast(
                                context,
                                if (isArabic)
                                    "كلمتا المرور غير متطابقتين"
                                else "Passwords do not match"
                            )
                        } else {
                            password = newPassword

                            prefs.edit()
                                .putString("password", password)
                                .apply()

                            newPassword = ""
                            confirmPassword = ""
                            showPasswordDialog = false

                            toast(
                                context,
                                if (isArabic)
                                    "تم تغيير كلمة المرور"
                                else "Password updated"
                            )
                        }
                    }
                ) {
                    Text(if (isArabic) "حفظ" else "Save")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showPasswordDialog = false
                    }
                ) {
                    Text(if (isArabic) "إلغاء" else "Cancel")
                }
            }
        )
    }
}

@Composable
fun CommandCard(
    command: Command,
    isArabic: Boolean,
    favorite: Boolean,
    onFavorite: () -> Unit,
    onCopy: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Panel
        )
    ) {
        Column(Modifier.padding(12.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    Text(
                        command.name,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        if (isArabic) command.descAr else command.desc,
                        color = Color.Gray,
                        fontSize = 11.sp
                    )
                }

                TextButton(onClick = onFavorite) {
                    Text(if (favorite) "⭐" else "☆")
                }
            }

            Spacer(Modifier.height(5.dp))

            Text(
                command.cmd,
                color = Green,
                fontSize = 12.sp
            )

            Spacer(Modifier.height(7.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Button(
                    onClick = onCopy,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(if (isArabic) "نسخ الأمر" else "Copy")
                }

                OutlinedButton(
                    onClick = onEdit
                ) {
                    Text(if (isArabic) "تعديل" else "Edit")
                }

                TextButton(onClick = onDelete) {
                    Text(
                        "✕",
                        color = Color.Red
                    )
                }
            }
        }
    }
}

@Composable
fun CommandEditorDialog(
    initial: Command?,
    isArabic: Boolean,
    onDismiss: () -> Unit,
    onSave: (
        String,
        String,
        String,
        String,
        String
    ) -> Unit
) {
    var name by remember(initial) {
        mutableStateOf(initial?.name ?: "")
    }

    var cmd by remember(initial) {
        mutableStateOf(initial?.cmd ?: "/")
    }

    var desc by remember(initial) {
        mutableStateOf(initial?.desc ?: "")
    }

    var descAr by remember(initial) {
        mutableStateOf(initial?.descAr ?: "")
    }

    var cat by remember(initial) {
        mutableStateOf(initial?.cat ?: "Custom")
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                if (isArabic)
                    if (initial == null) "إضافة أمر جديد"
                    else "تعديل الأمر"
                else
                    if (initial == null) "Add command"
                    else "Edit command"
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(7.dp)
            ) {
                OutlinedTextField(
                    name,
                    { name = it },
                    label = {
                        Text(if (isArabic) "اسم الأمر" else "Name")
                    },
                    singleLine = true
                )

                OutlinedTextField(
                    cmd,
                    { cmd = it },
                    label = {
                        Text(if (isArabic) "كود الأمر" else "Command")
                    }
                )

                OutlinedTextField(
                    descAr,
                    { descAr = it },
                    label = {
                        Text(if (isArabic) "الوصف العربي" else "Arabic description")
                    }
                )

                OutlinedTextField(
                    desc,
                    { desc = it },
                    label = {
                        Text(if (isArabic) "الوصف الإنجليزي" else "English description")
                    }
                )

                OutlinedTextField(
                    cat,
                    { cat = it },
                    label = {
                        Text(if (isArabic) "التصنيف" else "Category")
                    },
                    singleLine = true
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (
                        name.isNotBlank() &&
                        cmd.isNotBlank() &&
                        cat.isNotBlank()
                    ) {
                        onSave(
                            name.trim(),
                            cmd.trim(),
                            desc.trim(),
                            descAr.trim(),
                            cat.trim()
                        )
                    }
                }
            ) {
                Text(if (isArabic) "حفظ" else "Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(if (isArabic) "إلغاء" else "Cancel")
            }
        }
    )
}

@Composable
fun SectionTitle(title: String) {
    Text(
        title,
        color = Purple,
        fontSize = 19.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(vertical = 9.dp)
    )
}

@Composable
fun EmptyMessage(message: String) {
    Box(
        Modifier
            .fillMaxWidth()
            .padding(25.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            message,
            color = Color.Gray
        )
    }
}

fun categoryLabel(cat: String, isArabic: Boolean): String {
    if (!isArabic) return cat

    return when (cat) {
        "All" -> "الكل"
        "Game" -> "اللعبة"
        "Items" -> "الأدوات"
        "World" -> "العالم"
        "Mobs" -> "الوحوش"
        "Effects" -> "التأثيرات"
        "Building" -> "البناء"
        "Custom" -> "مخصص"
        else -> cat
    }
}

fun toast(context: Context, message: String) {
    Toast.makeText(
        context,
        message,
        Toast.LENGTH_SHORT
    ).show()
}

fun openUri(context: Context, uri: Uri) {
    try {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "*/*")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(intent)
    } catch (_: Exception) {
        toast(context, "تعذر فتح الملف")
    }
}

fun loadImported(
    prefs: android.content.SharedPreferences
): List<String> {
    return prefs.getString("imported_mods", "")
        ?.split("\n")
        ?.filter { it.isNotBlank() }
        ?: emptyList()
}

fun saveCommands(
    context: Context,
    commands: List<Command>
) {
    val array = JSONArray()

    commands.forEach { c ->
        val obj = JSONObject().apply {
            put("id", c.id)
            put("name", c.name)
            put("cmd", c.cmd)
            put("desc", c.desc)
            put("descAr", c.descAr)
            put("cat", c.cat)
        }
        array.put(obj)
    }

    context.getSharedPreferences(
        "mc_legend",
        Context.MODE_PRIVATE
    ).edit()
        .putString("commands", array.toString())
        .apply()
}

fun loadCommands(context: Context): List<Command> {
    val prefs = context.getSharedPreferences(
        "mc_legend",
        Context.MODE_PRIVATE
    )

    val saved = prefs.getString("commands", null)

    if (saved.isNullOrBlank()) {
        return allCommands()
    }

    return try {
        val array = JSONArray(saved)

        (0 until array.length()).map { index ->
            val obj = array.getJSONObject(index)

            Command(
                obj.optString("id", UUID.randomUUID().toString()),
                obj.optString("name"),
                obj.optString("cmd"),
                obj.optString("desc"),
                obj.optString("descAr"),
                obj.optString("cat", "Custom")
            )
        }
    } catch (_: Exception) {
        allCommands()
    }
}

fun exportStarterAddon(
    context: Context,
    uri: Uri,
    addonName: String,
    functionName: String,
    functionText: String
) {
    val packId = UUID.randomUUID().toString()
    val moduleId = UUID.randomUUID().toString()

    val safeFunction = functionName
        .trim()
        .ifBlank { "welcome" }
        .replace(Regex("[^A-Za-z0-9_-]"), "_")

    val manifest = """
{
  "format_version": 2,
  "header": {
    "name": "$addonName",
    "description": "Created with MC Legend",
    "uuid": "$packId",
    "version": [1, 0, 0],
    "min_engine_version": [1, 20, 0]
  },
  "modules": [
    {
      "type": "data",
      "uuid": "$moduleId",
      "version": [1, 0, 0]
    }
  ]
}
""".trimIndent()

    context.contentResolver.openOutputStream(uri)?.use { output ->
        ZipOutputStream(output).use { zip ->

            fun addFile(name: String, content: String) {
                zip.putNextEntry(ZipEntry(name))
                zip.write(content.toByteArray(Charsets.UTF_8))
                zip.closeEntry()
            }

            addFile("manifest.json", manifest)

            addFile(
                "functions/$safeFunction.mcfunction",
                functionText
            )
        }
    } ?: throw IllegalStateException("Cannot open output file")
}

fun allCommands(): List<Command> {
    val result = mutableListOf<Command>()

    fun add(
        name: String,
        cmd: String,
        desc: String,
        descAr: String,
        cat: String
    ) {
        result.add(
            Command(
                UUID.randomUUID().toString(),
                name,
                cmd,
                desc,
                descAr,
                cat
            )
        )
    }

    // الأوامر الأساسية

    val basics = listOf(
        Triple("Creative", "/gamemode creative @p", "وضع الإبداع"),
        Triple("Survival", "/gamemode survival @p", "وضع البقاء"),
        Triple("Adventure", "/gamemode adventure @p", "وضع المغامرة"),
        Triple("Spectator", "/gamemode spectator @p", "وضع المشاهدة"),
        Triple("Day", "/time set day", "جعل الوقت نهاراً"),
        Triple("Night", "/time set night", "جعل الوقت ليلاً"),
        Triple("Noon", "/time set noon", "وقت الظهر"),
        Triple("Midnight", "/time set midnight", "منتصف الليل"),
        Triple("Clear Weather", "/weather clear", "طقس صاف"),
        Triple("Rain", "/weather rain", "طقس ممطر"),
        Triple("Thunder", "/weather thunder", "عاصفة رعدية"),
        Triple("Set Spawn", "/setworldspawn", "تحديد نقطة البداية"),
        Triple("Kill Self", "/kill @s", "إنهاء اللاعب"),
        Triple("Heal", "/effect @p regeneration 30 2 true", "علاج اللاعب"),
        Triple("Clear Inventory", "/clear @p", "تنظيف الحقيبة"),
        Triple("Locate Village", "/locate structure village", "البحث عن قرية"),
        Triple("Locate Mansion", "/locate structure mansion", "البحث عن قصر"),
        Triple("Locate Stronghold", "/locate structure stronghold", "البحث عن معقل"),
        Triple("Locate Ancient City", "/locate structure ancient_city", "البحث عن مدينة قديمة"),
        Triple("Teleport Home", "/tp @p 0 100 0", "الانتقال إلى نقطة المنزل"),
        Triple("Show Coordinates", "/gamerule showcoordinates true", "إظهار الإحداثيات"),
        Triple("Keep Inventory", "/gamerule keepinventory true", "الاحتفاظ بالأغراض"),
        Triple("Disable Fire", "/gamerule firedamage false", "إيقاف ضرر النار"),
        Triple("Disable Mob Griefing", "/gamerule mobgriefing false", "منع تخريب الوحوش"),
        Triple("Disable Day Cycle", "/gamerule dodaylightcycle false", "تثبيت دورة النهار")
    )

    basics.forEach {
        add(it.first, it.second, it.third, it.third, "Game")
    }

    // التأثيرات

    val effects = listOf(
        "speed", "slowness", "haste", "mining_fatigue",
        "strength", "instant_health", "instant_damage",
        "jump_boost", "nausea", "regeneration",
        "resistance", "fire_resistance", "water_breathing",
        "invisibility", "blindness", "night_vision",
        "hunger", "weakness", "poison", "wither",
        "health_boost", "absorption", "saturation",
        "levitation", "slow_falling", "conduit_power",
        "dolphins_grace", "bad_omen", "hero_of_the_village",
        "darkness"
    )

    effects.forEach { effect ->
        for (level in 1..3) {
            add(
                "$effect Level $level",
                "/effect @p $effect 60 $level true",
                "Apply $effect",
                "تأثير $effect المستوى $level",
                "Effects"
            )
        }
    }

    // الأدوات والأغراض

    val items = listOf(
        "diamond", "emerald", "gold_ingot", "iron_ingot",
        "netherite_ingot", "coal", "redstone", "lapis_lazuli",
        "amethyst_shard", "quartz", "apple", "golden_apple",
        "enchanted_golden_apple", "bread", "cooked_beef",
        "cooked_chicken", "cooked_porkchop", "golden_carrot",
        "diamond_sword", "diamond_pickaxe", "diamond_axe",
        "diamond_shovel", "diamond_hoe", "netherite_sword",
        "netherite_pickaxe", "netherite_axe", "bow", "crossbow",
        "arrow", "shield", "trident", "totem_of_undying",
        "elytra", "fishing_rod", "flint_and_steel",
        "ender_pearl", "ender_eye", "experience_bottle",
        "obsidian", "crying_obsidian", "tnt", "torch",
        "lantern", "beacon", "anvil", "enchanting_table",
        "crafting_table", "ender_chest", "chest", "hopper",
        "diamond_block", "gold_block", "iron_block",
        "emerald_block", "netherite_block"
    )

    items.forEach { item ->
        add(
            "Give $item",
            "/give @p $item 16",
            "Get 16 $item",
            "الحصول على 16 من $item",
            "Items"
        )
    }

    // استدعاء الوحوش والحيوانات

    val mobs = listOf(
        "zombie", "skeleton", "creeper", "spider",
        "cave_spider", "enderman", "witch", "blaze",
        "ghast", "slime", "magma_cube", "phantom",
        "drowned", "husk", "stray", "pillager",
        "ravager", "vindicator", "evoker", "vex",
        "warden", "wither", "ender_dragon", "iron_golem",
        "snow_golem", "villager", "wandering_trader",
        "cow", "sheep", "pig", "chicken", "horse",
        "wolf", "cat", "ocelot", "rabbit", "fox",
        "bee", "dolphin", "turtle", "panda",
        "polar_bear", "llama", "goat", "camel"
    )

    mobs.forEach { mob ->
        add(
            "Summon $mob",
            "/summon $mob ~ ~ ~",
            "Summon $mob",
            "استدعاء $mob",
            "Mobs"
        )
    }

    // أوامر البناء

    val blocks = listOf(
        "stone", "cobblestone", "dirt", "grass_block",
        "sand", "glass", "oak_planks", "spruce_planks",
        "birch_planks", "jungle_planks", "acacia_planks",
        "dark_oak_planks", "mangrove_planks",
        "cherry_planks", "bricks", "stone_bricks",
        "obsidian", "glowstone", "sea_lantern",
        "diamond_block", "gold_block", "iron_block",
        "emerald_block", "netherite_block",
        "crafting_table", "furnace", "chest",
        "barrel", "bookshelf", "lantern", "white_wool",
        "red_wool", "blue_wool", "tnt", "hay_block",
        "slime_block", "honey_block"
    )

    blocks.forEach { block ->
        add(
            "Place $block",
            "/setblock ~ ~-1 ~ $block",
            "Place block below you",
            "وضع الكتلة أسفلك: $block",
            "Building"
        )
    }

    // قواعد اللعبة

    val rules = listOf(
        "keepinventory",
        "mobgriefing",
        "dodaylightcycle",
        "doweathercycle",
        "domobspawning",
        "naturalregeneration",
        "falldamage",
        "firedamage",
        "drowningdamage",
        "tntexplodes",
        "showcoordinates",
        "pvp",
        "doentitydrops",
        "mobloot",
        "sendcommandfeedback"
    )

    rules.forEach { rule ->
        listOf("true", "false").forEach { value ->
            add(
                "$rule $value",
                "/gamerule $rule $value",
                "Change gamerule",
                "تغيير قاعدة اللعبة $rule إلى $value",
                "World"
            )
        }
    }

    return result
}
