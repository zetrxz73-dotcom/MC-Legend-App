package com.mclegend.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF121212), titleContentColor = Color.White, actionIconContentColor = Color.White)
            )
        },
        containerColor = Color(0xFF121212)
    ) { pad ->
        Column(modifier = Modifier.padding(pad).padding(12.dp).fillMaxSize()) {
            OutlinedTextField(value = search, onValueChange = { search = it }, label = { Text(if (isArabic) "ابحث..." else "Search...") }, modifier = Modifier.fillMaxWidth())
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
        Command("Fly - Enchant", "/give @p elytra", "Get elytra to fly", "اجنحة طيران", "Items"),
        Command("Day - Clear Mobs", "/kill @e[type=!player]", "Clear mobs", "مسح الوحوش", "Mobs"),
        Command("Give Gold", "/give @p gold_ingot 64", "Gold", "ذهب", "Items"),
        Command("Give Iron", "/give @p iron_ingot 64", "Iron", "حديد", "Items"),
        Command("Give Enchanted Apple", "/give @p enchanted_golden_apple 10", "Enchanted apple", "تفاحة ذهبية", "Items"),
        Command("Set Spawn", "/setworldspawn", "Set spawn", "تعيين البداية", "World")
    )
}
