package com.legend.mc

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.util.UUID
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

private const val PREFS = "mc_legend_data"
private const val KEY_COMMANDS = "commands"
private const val KEY_PASSWORD = "password"
private const val KEY_HISTORY = "history"
private const val KEY_MODS = "mods"
private const val KEY_ENGLISH = "english"

data class LegendCommand(
    val id: Int,
    val title: String,
    val command: String,
    val description: String,
    val category: String,
    val favorite: Boolean = false
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(
                colorScheme = darkColorScheme(
                    primary = Color(0xFFAA82FF),
                    secondary = Color(0xFF62D6A5),
                    background = Color(0xFF090B12),
                    surface = Color(0xFF171B28)
                )
            ) {
                MC_Legend_App()
            }
        }
    }
}

@Composable
fun MC_Legend_App() {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences(PREFS, Context.MODE_PRIVATE) }

    var unlocked by rememberSaveable { mutableStateOf(false) }
    var password by rememberSaveable { mutableStateOf("") }
    var storedPassword by remember { mutableStateOf(prefs.getString(KEY_PASSWORD, "1234") ?: "1234") }
    var english by remember { mutableStateOf(prefs.getBoolean(KEY_ENGLISH, false)) }
    var tab by rememberSaveable { mutableStateOf(0) }
    var search by rememberSaveable { mutableStateOf("") }
    var category by rememberSaveable { mutableStateOf("الكل") }
    var editorOpen by remember { mutableStateOf(false) }
    var passwordDialog by remember { mutableStateOf(false) }
    var addonDialog by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<LegendCommand?>(null) }
    var addonName by rememberSaveable { mutableStateOf("MC Legend Pack") }

    val commands = remember {
        mutableStateListOf<LegendCommand>().apply {
            addAll(loadCommands(context))
        }
    }
    val history = remember {
        mutableStateListOf<String>().apply {
            addAll(loadStringList(context, KEY_HISTORY))
        }
    }
    val mods = remember {
        mutableStateListOf<String>().apply {
            addAll(loadStringList(context, KEY_MODS))
        }
    }

    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                val name = getDisplayName(context, uri)
                val folder = File(context.filesDir, "imported_mods")
                if (!folder.exists()) folder.mkdirs()
                val safeName = name.replace(Regex("[^A-Za-z0-9._-]"), "_")
                    .ifBlank { "minecraft_pack.mcpack" }
                val destination = File(folder, "${System.currentTimeMillis()}_$safeName")
                context.contentResolver.openInputStream(uri)?.use { input ->
                    destination.outputStream().use { output -> input.copyTo(output) }
                } ?: throw IllegalStateException("تعذر قراءة الملف")
                mods.add(destination.name)
                saveStringList(context, KEY_MODS, mods)
                toast(context, "تم استيراد الملف إلى مساحة التطبيق")
            } catch (error: Exception) {
                toast(context, "تعذر استيراد الملف: ${error.message}")
            }
        }
    }

    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/zip")
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                exportBehaviorPack(context, uri, addonName)
                toast(context, "تم إنشاء حزمة Behavior Pack")
            } catch (error: Exception) {
                toast(context, "فشل التصدير: ${error.message}")
            }
        }
    }

    if (!unlocked) {
        PasswordPage(
            password = password,
            onPasswordChange = { password = it },
            onLogin = {
                if (password == storedPassword) {
                    unlocked = true
                    password = ""
                } else {
                    toast(context, if (english) "Wrong password" else "كلمة المرور غير صحيحة")
                }
            },
            english = english
        )
        return
    }

    val filtered = commands.filter { item ->
        val matchesSearch = item.title.contains(search, true) ||
            item.command.contains(search, true) ||
            item.description.contains(search, true)
        val matchesCategory = category == "الكل" || item.category == category
        val matchesTab = when (tab) {
            1 -> item.favorite
            else -> true
        }
        matchesSearch && matchesCategory && matchesTab
    }

    Scaffold(
        containerColor = Color(0xFF090B12),
        bottomBar = {
            NavigationBar(containerColor = Color(0xFF151927)) {
                val labels = if (english)
                    listOf("Commands", "Favorites", "History", "Packs", "Settings")
                else
                    listOf("الأوامر", "المفضلة", "السجل", "المودات", "الإعدادات")
                labels.forEachIndexed { index, label ->
                    NavigationBarItem(
                        selected = tab == index,
                        onClick = { tab = index },
                        icon = { Text(listOf("⌘", "★", "◷", "▣", "⚙")[index]) },
                        label = { Text(label, fontSize = 10.sp) }
                    )
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(14.dp)
        ) {
            Text(
                "MC LEGEND",
                color = Color(0xFFB99AFF),
                fontSize = 27.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                if (english) "Minecraft Bedrock command and pack assistant"
                else "مساعد أوامر ومودات Minecraft Bedrock",
                color = Color.LightGray,
                fontSize = 12.sp
            )
            Spacer(Modifier.height(10.dp))

            when (tab) {
                0, 1 -> {
                    Button(
                        onClick = { openMinecraft(context) },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text(if (english) "Open Minecraft" else "فتح Minecraft") }

                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = search,
                        onValueChange = { search = it },
                        label = { Text(if (english) "Search commands" else "ابحث عن أمر") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        OutlinedButton(onClick = { category = "الكل" }) { Text("الكل") }
                        OutlinedButton(onClick = { category = "اللعب" }) { Text("اللعب") }
                        OutlinedButton(onClick = { category = "العالم" }) { Text("العالم") }
                        OutlinedButton(onClick = { category = "التأثيرات" }) { Text("التأثيرات") }
                    }
                    Spacer(Modifier.height(6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("الأوامر: ${filtered.size}", color = Color.LightGray)
                        Spacer(Modifier.weight(1f))
                        Button(onClick = {
                            editing = null
                            editorOpen = true
                        }) { Text("إضافة أمر +") }
                    }
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(filtered, key = { it.id }) { item ->
                            CommandTile(
                                item = item,
                                english = english,
                                onCopy = {
                                    copyCommand(context, item.command)
                                    history.add(0, item.command)
                                    while (history.size > 40) history.removeAt(history.lastIndex)
                                    saveStringList(context, KEY_HISTORY, history)
                                },
                                onFavorite = {
                                    val index = commands.indexOfFirst { it.id == item.id }
                                    if (index >= 0) {
                                        commands[index] = item.copy(favorite = !item.favorite)
                                        saveCommands(context, commands)
                                    }
                                },
                                onEdit = {
                                    editing = item
                                    editorOpen = true
                                },
                                onDelete = {
                                    commands.removeAll { it.id == item.id }
                                    saveCommands(context, commands)
                                }
                            )
                        }
                    }
                }

                2 -> {
                    Text(if (english) "Recently copied commands" else "آخر الأوامر المنسوخة",
                        color = Color.White, fontWeight = FontWeight.Bold)
                    OutlinedButton(onClick = {
                        history.clear()
                        saveStringList(context, KEY_HISTORY, history)
                    }) { Text(if (english) "Clear history" else "مسح السجل") }
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(history) { value ->
                            Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF171B28))) {
                                Row(
                                    Modifier.fillMaxWidth().padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(value, color = Color(0xFF65D6A3), modifier = Modifier.weight(1f))
                                    OutlinedButton(onClick = { copyCommand(context, value) }) {
                                        Text(if (english) "Copy" else "نسخ")
                                    }
                                }
                            }
                        }
                    }
                }

                3 -> {
                    Text(if (english) "Pack manager" else "مدير المودات والحزم",
                        color = Color.White, fontWeight = FontWeight.Bold)
                    Text(
                        if (english) "Import .mcpack/.mcaddon files or create a starter behavior pack."
                        else "استورد ملفات mcpack وmcaddon، أو أنشئ حزمة Behavior Pack أولية.",
                        color = Color.LightGray
                    )
                    Spacer(Modifier.height(10.dp))
                    Button(
                        onClick = { importLauncher.launch(arrayOf("*/*")) },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text(if (english) "Import pack" else "استيراد مود / حزمة") }
                    Button(
                        onClick = { addonDialog = true },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text(if (english) "Create / export MCADDON" else "إنشاء وتصدير MCADDON") }
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(mods) { fileName ->
                            Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF171B28))) {
                                Text(fileName, color = Color.White, modifier = Modifier.padding(12.dp))
                            }
                        }
                    }
                }

                4 -> {
                    Text(if (english) "Settings" else "الإعدادات",
                        color = Color.White, fontWeight = FontWeight.Bold)
                    Button(onClick = { passwordDialog = true }, modifier = Modifier.fillMaxWidth()) {
                        Text(if (english) "Change password" else "تغيير كلمة المرور")
                    }
                    Button(
                        onClick = {
                            english = !english
                            prefs.edit().putBoolean(KEY_ENGLISH, english).apply()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text(if (english) "Switch to Arabic" else "Switch to English") }
                    OutlinedButton(
                        onClick = {
                            unlocked = false
                            tab = 0
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text(if (english) "Lock app" else "قفل التطبيق") }
                    Text(
                        if (english) "Default password: 1234. Commands are copied to the clipboard; they are not executed automatically."
                        else "كلمة المرور الافتراضية: 1234. الأوامر تُنسخ إلى الحافظة ولا تُنفّذ تلقائيًا داخل اللعبة.",
                        color = Color.Gray,
                        fontSize = 12.sp
                    )
                    Text(
                        if (english) "Pack export creates a starter Behavior Pack, not a full visual mod editor."
                        else "تصدير الحزمة ينشئ Behavior Pack أولية، وليس محرر مودات مرئيًا متكاملًا.",
                        color = Color.Gray,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }

    if (editorOpen) {
        CommandDialog(
            command = editing,
            onDismiss = { editorOpen = false },
            onSave = { title, value, description, group ->
                if (editing == null) {
                    val newId = (commands.maxOfOrNull { it.id } ?: 0) + 1
                    commands.add(LegendCommand(newId, title, value, description, group))
                } else {
                    val old = editing!!
                    val index = commands.indexOfFirst { it.id == old.id }
                    if (index >= 0) {
                        commands[index] = old.copy(
                            title = title,
                            command = value,
                            description = description,
                            category = group
                        )
                    }
                }
                saveCommands(context, commands)
                editorOpen = false
            }
        )
    }

    if (passwordDialog) {
        ChangePasswordDialog(
            onDismiss = { passwordDialog = false },
            onSave = { newPassword ->
                storedPassword = newPassword
                prefs.edit().putString(KEY_PASSWORD, newPassword).apply()
                passwordDialog = false
                toast(context, "تم تغيير كلمة المرور")
            }
        )
    }

    if (addonDialog) {
        AlertDialog(
            onDismissRequest = { addonDialog = false },
            title = { Text("إنشاء MCADDON") },
            text = {
                OutlinedTextField(
                    value = addonName,
                    onValueChange = { addonName = it },
                    label = { Text("اسم الحزمة") },
                    singleLine = true
                )
            },
            confirmButton = {
                Button(onClick = {
                    addonDialog = false
                    exportLauncher.launch("${addonName.ifBlank { "MC_Legend_Pack" }}.mcaddon")
                }) { Text("تصدير") }
            },
            dismissButton = {
                OutlinedButton(onClick = { addonDialog = false }) { Text("إلغاء") }
            }
        )
    }
}

@Composable
private fun PasswordPage(
    password: String,
    onPasswordChange: (String) -> Unit,
    onLogin: () -> Unit,
    english: Boolean
) {
    Surface(Modifier.fillMaxSize(), color = Color(0xFF090B12)) {
        Column(
            Modifier.fillMaxSize().padding(24.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text("MC LEGEND", color = Color(0xFFB99AFF), fontSize = 32.sp, fontWeight = FontWeight.Bold)
            Text(
                if (english) "Enter your password" else "أدخل كلمة المرور للدخول",
                color = Color.White
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = password,
                onValueChange = onPasswordChange,
                label = { Text(if (english) "Password" else "كلمة المرور") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(Modifier.height(12.dp))
            Button(onClick = onLogin, modifier = Modifier.fillMaxWidth()) {
                Text(if (english) "Unlock" else "دخول")
            }
            Text(
                if (english) "Initial password: 1234" else "كلمة المرور الأولية: 1234",
                color = Color.Gray,
                fontSize = 12.sp
            )
        }
    }
}

@Composable
private fun CommandTile(
    item: LegendCommand,
    english: Boolean,
    onCopy: () -> Unit,
    onFavorite: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF171B28))
    ) {
        Column(Modifier.padding(12.dp)) {
            Text(item.title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Text(item.description, color = Color.LightGray, fontSize = 12.sp)
            Text(item.command, color = Color(0xFF65D6A3), fontSize = 14.sp)
            Text(item.category, color = Color.Gray, fontSize = 11.sp)
            Spacer(Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                Button(onClick = onCopy) { Text(if (english) "Copy" else "نسخ") }
                OutlinedButton(onClick = onFavorite) { Text(if (item.favorite) "★" else "☆") }
                OutlinedButton(onClick = onEdit) { Text(if (english) "Edit" else "تعديل") }
                OutlinedButton(onClick = onDelete) { Text(if (english) "Delete" else "حذف") }
            }
        }
    }
}

@Composable
private fun CommandDialog(
    command: LegendCommand?,
    onDismiss: () -> Unit,
    onSave: (String, String, String, String) -> Unit
) {
    var title by remember(command?.id) { mutableStateOf(command?.title ?: "") }
    var value by remember(command?.id) { mutableStateOf(command?.command ?: "/") }
    var description by remember(command?.id) { mutableStateOf(command?.description ?: "") }
    var category by remember(command?.id) { mutableStateOf(command?.category ?: "عام") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (command == null) "إضافة أمر" else "تعديل أمر") },
        text = {
            Column {
                OutlinedTextField(title, { title = it }, label = { Text("اسم الأمر") }, singleLine = true)
                OutlinedTextField(value, { value = it }, label = { Text("صيغة الأمر") }, singleLine = true)
                OutlinedTextField(description, { description = it }, label = { Text("الوصف") }, singleLine = true)
                OutlinedTextField(category, { category = it }, label = { Text("التصنيف") }, singleLine = true)
            }
        },
        confirmButton = {
            Button(onClick = {
                if (title.isNotBlank() && value.isNotBlank()) {
                    onSave(title.trim(), value.trim(), description.trim(), category.trim().ifBlank { "عام" })
                }
            }) { Text("حفظ") }
        },
        dismissButton = { OutlinedButton(onClick = onDismiss) { Text("إلغاء") } }
    )
}

@Composable
private fun ChangePasswordDialog(
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var first by remember { mutableStateOf("") }
    var second by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("تغيير كلمة المرور") },
        text = {
            Column {
                OutlinedTextField(first, { first = it }, label = { Text("كلمة المرور الجديدة") }, singleLine = true)
                OutlinedTextField(second, { second = it }, label = { Text("تأكيد كلمة المرور") }, singleLine = true)
            }
        },
        confirmButton = {
            Button(onClick = {
                if (first.length >= 4 && first == second) onSave(first)
            }) { Text("حفظ") }
        },
        dismissButton = { OutlinedButton(onClick = onDismiss) { Text("إلغاء") } }
    )
}

private fun loadCommands(context: Context): List<LegendCommand> {
    val raw = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(KEY_COMMANDS, null)
        ?: return defaultCommands()
    return try {
        val array = JSONArray(raw)
        (0 until array.length()).map { i ->
            val item = array.getJSONObject(i)
            LegendCommand(
                item.optInt("id"),
                item.optString("title"),
                item.optString("command"),
                item.optString("description"),
                item.optString("category"),
                item.optBoolean("favorite")
            )
        }
    } catch (_: Exception) {
        defaultCommands()
    }
}

private fun saveCommands(context: Context, commands: List<LegendCommand>) {
    val array = JSONArray()
    commands.forEach { item ->
        array.put(JSONObject().apply {
            put("id", item.id)
            put("title", item.title)
            put("command", item.command)
            put("description", item.description)
            put("category", item.category)
            put("favorite", item.favorite)
        })
    }
    context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        .edit().putString(KEY_COMMANDS, array.toString()).apply()
}

private fun loadStringList(context: Context, key: String): List<String> {
    val raw = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(key, null) ?: return emptyList()
    return try {
        val array = JSONArray(raw)
        (0 until array.length()).map { array.optString(it) }
    } catch (_: Exception) {
        emptyList()
    }
}

private fun saveStringList(context: Context, key: String, values: List<String>) {
    val array = JSONArray()
    values.forEach { array.put(it) }
    context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        .edit().putString(key, array.toString()).apply()
}

private fun defaultCommands(): List<LegendCommand> {
    val result = mutableListOf<LegendCommand>()
    var id = 1

    fun add(title: String, command: String, description: String, category: String) {
        result.add(LegendCommand(id++, title, command, description, category))
    }

    val basics = listOf(
        listOf("إبداع", "/gamemode creative", "تغيير نمط اللعب", "اللعب"),
        listOf("بقاء", "/gamemode survival", "تغيير نمط اللعب", "اللعب"),
        listOf("مغامرة", "/gamemode adventure", "تغيير نمط اللعب", "اللعب"),
        listOf("نهار", "/time set day", "تغيير الوقت إلى النهار", "العالم"),
        listOf("ليل", "/time set night", "تغيير الوقت إلى الليل", "العالم"),
        listOf("شروق", "/time set sunrise", "تغيير الوقت", "العالم"),
        listOf("غروب", "/time set sunset", "تغيير الوقت", "العالم"),
        listOf("طقس صافٍ", "/weather clear", "إيقاف المطر", "العالم"),
        listOf("مطر", "/weather rain", "تشغيل المطر", "العالم"),
        listOf("عاصفة رعدية", "/weather thunder", "تشغيل العاصفة", "العالم"),
        listOf("قتل اللاعب", "/kill @s", "إنهاء اللاعب الحالي", "اللعب"),
        listOf("قتل الكل", "/kill @e", "حذف الكائنات", "العالم"),
        listOf("إحداثيات", "/gamerule showcoordinates true", "إظهار الإحداثيات", "العالم"),
        listOf("إخفاء الإحداثيات", "/gamerule showcoordinates false", "إخفاء الإحداثيات", "العالم"),
        listOf("حفظ المخزون", "/gamerule keepinventory true", "الاحتفاظ بالأغراض بعد الموت", "العالم"),
        listOf("إيقاف حفظ المخزون", "/gamerule keepinventory false", "إيقاف الاحتفاظ بالأغراض", "العالم"),
        listOf("إيقاف انتشار النار", "/gamerule dofiretick false", "منع انتشار النار", "العالم"),
        listOf("تشغيل انتشار النار", "/gamerule dofiretick true", "السماح بانتشار النار", "العالم"),
        listOf("إيقاف ظهور الوحوش", "/gamerule domobspawning false", "منع ظهور الوحوش", "العالم"),
        listOf("تشغيل ظهور الوحوش", "/gamerule domobspawning true", "السماح بظهور الوحوش", "العالم"),
        listOf("إيقاف دورة النهار", "/gamerule dodaylightcycle false", "تثبيت الوقت", "العالم"),
        listOf("تشغيل دورة النهار", "/gamerule dodaylightcycle true", "إعادة دورة الوقت", "العالم"),
        listOf("إيقاف الطقس", "/gamerule doweathercycle false", "تثبيت الطقس", "العالم"),
        listOf("تشغيل الطقس", "/gamerule doweathercycle true", "إعادة تغير الطقس", "العالم"),
        listOf("رسالة للجميع", "/say مرحباً", "إرسال رسالة في الدردشة", "الدردشة"),
        listOf("عنوان للاعب", "/title @p title مرحباً", "إظهار عنوان", "الدردشة"),
        listOf("عنوان فرعي", "/title @p subtitle أهلاً", "إظهار عنوان فرعي", "الدردشة"),
        listOf("مسح الدردشة", "/clear @p", "مسح أغراض اللاعب", "الأدوات"),
        listOf("إعادة الظهور", "/spawnpoint @p", "تحديد نقطة الظهور", "اللعب"),
        listOf("نقطة العالم", "/setworldspawn", "تحديد ظهور العالم", "العالم"),
        listOf("شفاء", "/effect @p instant_health 1 1", "استعادة الصحة", "التأثيرات"),
        listOf("سرعة", "/effect @p speed 60 1", "زيادة السرعة", "التأثيرات"),
        listOf("قفز", "/effect @p jump_boost 60 2", "زيادة القفز", "التأثيرات"),
        listOf("قوة", "/effect @p strength 60 1", "زيادة قوة الضرب", "التأثيرات"),
        listOf("مقاومة", "/effect @p resistance 60 1", "زيادة المقاومة", "التأثيرات"),
        listOf("تنفس الماء", "/effect @p water_breathing 120 1", "التنفس تحت الماء", "التأثيرات"),
        listOf("رؤية ليلية", "/effect @p night_vision 120 1", "الرؤية في الظلام", "التأثيرات"),
        listOf("إزالة التأثيرات", "/effect @p clear", "إزالة التأثيرات", "التأثيرات"),
        listOf("تحديد صعوبة سهلة", "/difficulty easy", "تغيير الصعوبة", "العالم"),
        listOf("تحديد صعوبة عادية", "/difficulty normal", "تغيير الصعوبة", "العالم"),
        listOf("تحديد صعوبة صعبة", "/difficulty hard", "تغيير الصعوبة", "العالم"),
        listOf("بحث عن قرية", "/locate structure village", "البحث عن قرية قريبة", "الاستكشاف"),
        listOf("بحث عن معبد", "/locate structure temple", "البحث عن معبد", "الاستكشاف"),
        listOf("بحث عن حصن", "/locate structure stronghold", "البحث عن حصن", "الاستكشاف"),
        listOf("حفظ العالم", "/save-all", "حفظ بيانات العالم إن كان مدعوماً", "العالم"),
        listOf("تجربة للاعب", "/xp 100 @p", "إضافة خبرة", "اللعب"),
        listOf("انتقال للأعلى", "/tp @p ~ ~10 ~", "الانتقال عشرة بلوكات للأعلى", "اللعب"),
        listOf("انتقال للأسفل", "/tp @p ~ ~-10 ~", "الانتقال عشرة بلوكات للأسفل", "اللعب"),
        listOf("انتقال للأمام", "/tp @p ~ ~ ~10", "الانتقال للأمام", "اللعب"),
        listOf("انتقال للخلف", "/tp @p ~ ~ ~-10", "الانتقال للخلف", "اللعب"),
        listOf("تحديد وقت الظهر", "/time set noon", "ضبط الوقت إلى الظهر", "العالم"),
        listOf("تحديد وقت منتصف الليل", "/time set midnight", "ضبط الوقت إلى منتصف الليل", "العالم"),
        listOf("تفعيل القتل", "/gamerule pvp true", "السماح بالقتال بين اللاعبين", "العالم"),
        listOf("إيقاف القتل", "/gamerule pvp false", "منع القتال بين اللاعبين", "العالم"),
        listOf("إعطاء تفاحة", "/give @p apple 16", "الحصول على 16 تفاحة", "الأدوات"),
        listOf("إعطاء خبز", "/give @p bread 16", "الحصول على 16 قطعة خبز", "الأدوات")
    )
    basics.forEach { add(it[0], it[1], it[2], it[3]) }

    val items = listOf(
        "diamond", "iron_ingot", "gold_ingot", "emerald", "netherite_ingot",
        "coal", "redstone", "lapis_lazuli", "copper_ingot", "amethyst_shard",
        "iron_sword", "diamond_sword", "netherite_sword", "bow", "crossbow",
        "trident", "shield", "arrow", "iron_pickaxe", "diamond_pickaxe",
        "netherite_pickaxe", "iron_axe", "diamond_axe", "netherite_axe",
        "iron_shovel", "diamond_shovel", "iron_hoe", "diamond_hoe",
        "diamond_helmet", "diamond_chestplate", "diamond_leggings", "diamond_boots",
        "netherite_helmet", "netherite_chestplate", "netherite_leggings", "netherite_boots",
        "elytra", "totem_of_undying", "golden_apple", "enchanted_golden_apple",
        "ender_pearl", "ender_eye", "experience_bottle", "firework_rocket",
        "obsidian", "crying_obsidian", "ancient_debris", "glowstone", "sea_lantern",
        "torch", "lantern", "crafting_table", "furnace", "anvil", "enchanting_table",
        "chest", "ender_chest", "hopper", "beacon", "shulker_box", "totem_of_undying",
        "bread", "cooked_beef", "golden_carrot", "potion", "water_bucket",
        "lava_bucket", "milk_bucket", "saddle", "name_tag", "lead", "map"
    )
    items.forEach { item ->
        add("إعطاء $item", "/give @p $item 1", "الحصول على عنصر $item", "الأدوات")
    }

    val effects = listOf(
        "speed", "slowness", "haste", "mining_fatigue", "strength",
        "instant_health", "instant_damage", "jump_boost", "nausea", "regeneration",
        "resistance", "fire_resistance", "water_breathing", "invisibility",
        "blindness", "night_vision", "hunger", "weakness", "poison",
        "wither", "health_boost", "absorption", "saturation", "levitation",
        "slow_falling", "conduit_power", "dolphins_grace", "bad_omen",
        "hero_of_the_village", "darkness"
    )
    effects.forEach { effect ->
        add("تأثير $effect", "/effect @p $effect 60 1", "تطبيق تأثير $effect لمدة 60 ثانية", "التأثيرات")
    }

    val enchantments = listOf(
        "sharpness", "smite", "bane_of_arthropods", "knockback", "fire_aspect",
        "looting", "efficiency", "silk_touch", "unbreaking", "fortune",
        "power", "punch", "flame", "infinity", "protection",
        "fire_protection", "blast_protection", "projectile_protection",
        "feather_falling", "respiration", "aqua_affinity", "thorns",
        "depth_strider", "frost_walker", "mending"
    )
    enchantments.forEach { enchant ->
        add("تعويذة $enchant", "/enchant @p $enchant 1", "تطبيق تعويذة $enchant على الأداة المناسبة", "التعويذات")
    }

    val eggs = listOf(
        "zombie", "skeleton", "creeper", "spider", "enderman", "witch",
        "slime", "cow", "pig", "sheep", "chicken", "horse", "wolf",
        "cat", "villager", "iron_golem", "snow_golem", "warden",
        "allay", "axolotl", "bee", "fox", "goat", "camel", "frog",
        "turtle", "dolphin", " panda", "parrot", "rabbit"
    )
    eggs.forEach { mob ->
        val idName = mob.trim()
        add("بيضة استدعاء $idName", "/give @p ${idName}_spawn_egg 1", "الحصول على بيضة استدعاء $idName", "الأدوات")
    }

    val gamerules = listOf(
        "mobgriefing true", "mobgriefing false",
        "pvp true", "pvp false",
        "falldamage true", "falldamage false",
        "firedamage true", "firedamage false",
        "tntexplodes true", "tntexplodes false",
        "commandblocksenabled true", "commandblocksenabled false",
        "naturalregeneration true", "naturalregeneration false",
        "drowningdamage true", "drowningdamage false",
        "freezedamage true", "freezedamage false"
    )
    gamerules.forEach { rule ->
        add("قاعدة $rule", "/gamerule $rule", "تغيير قاعدة العالم $rule", "العالم")
    }

    return result
}

private fun copyCommand(context: Context, command: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    clipboard.setPrimaryClip(ClipData.newPlainText("Minecraft command", command))
    toast(context, "تم نسخ الأمر")
}

private fun openMinecraft(context: Context) {
    val intent = context.packageManager.getLaunchIntentForPackage("com.mojang.minecraftpe")
    if (intent != null) {
        context.startActivity(intent)
    } else {
        toast(context, "لم يتم العثور على Minecraft على الجهاز")
    }
}

private fun toast(context: Context, message: String) {
    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
}

private fun getDisplayName(context: Context, uri: Uri): String {
    var name = "minecraft_pack.mcpack"
    context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
        val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        if (index >= 0 && cursor.moveToFirst()) name = cursor.getString(index) ?: name
    }
    return name
}

private fun exportBehaviorPack(context: Context, uri: Uri, packName: String) {
    val manifest = JSONObject().apply {
        put("format_version", 2)
        put("header", JSONObject().apply {
            put("name", packName)
            put("description", "Created with MC Legend")
            put("uuid", UUID.randomUUID().toString())
            put("version", JSONArray().put(1).put(0).put(0))
            put("min_engine_version", JSONArray().put(1).put(20).put(0))
        })
        put("modules", JSONArray().put(JSONObject().apply {
            put("type", "data")
            put("description", "MC Legend starter behavior pack")
            put("uuid", UUID.randomUUID().toString())
            put("version", JSONArray().put(1).put(0).put(0))
        }))
    }

    val output = context.contentResolver.openOutputStream(uri)
        ?: throw IllegalStateException("تعذر فتح ملف التصدير")
    ZipOutputStream(output).use { zip ->
        zip.putNextEntry(ZipEntry("manifest.json"))
        zip.write(manifest.toString(2).toByteArray(Charsets.UTF_8))
        zip.closeEntry()

        zip.putNextEntry(ZipEntry("README.txt"))
        zip.write(
            ("MC Legend starter Behavior Pack\n" +
             "This is a starter pack structure. Add behavior files and valid JSON definitions to extend it.\n")
                .toByteArray(Charsets.UTF_8)
        )
        zip.closeEntry()
    }
}
