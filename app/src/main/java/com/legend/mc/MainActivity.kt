package com.legend.mc

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val DarkBackground = Color(0xFF0B100E)
private val CardColor = Color(0xFF17221C)
private val Emerald = Color(0xFF00C878)
private val Gold = Color(0xFFFFC857)
private val WhiteText = Color(0xFFF2F5F2)
private val GrayText = Color(0xFF9EAAA2)

private data class MCCommand(
    val name: String,
    val command: String,
    val category: String,
    val description: String,
    val keywords: List<String> = emptyList()
)

private val commandLibrary = listOf(
    MCCommand(
        name = "الانتقال إلى لاعب",
        command = "/tp @s Player",
        category = "الانتقال",
        description = "ينقلك إلى اللاعب الذي تستبدل Player باسمه.",
        keywords = listOf("انتقال", "لاعب", "teleport", "tp")
    ),
    MCCommand(
        name = "تغيير الوقت إلى النهار",
        command = "/time set day",
        category = "الوقت",
        description = "يجعل الوقت نهارًا.",
        keywords = listOf("نهار", "صباح", "day", "time")
    ),
    MCCommand(
        name = "تغيير الوقت إلى الليل",
        command = "/time set night",
        category = "الوقت",
        description = "يجعل الوقت ليلًا.",
        keywords = listOf("ليل", "مساء", "night", "time")
    ),
    MCCommand(
        name = "إزالة المطر",
        command = "/weather clear",
        category = "الطقس",
        description = "يجعل الطقس صافيًا ويوقف المطر.",
        keywords = listOf("طقس", "مطر", "صافي", "weather", "clear")
    ),
    MCCommand(
        name = "الحصول على ألماس",
        command = "/give @s diamond 64",
        category = "الأدوات",
        description = "يعطيك 64 قطعة ألماس. قد تحتاج إلى تفعيل الغش في العالم.",
        keywords = listOf("ألماس", "الماس", "دايموند", "diamond", "give")
    ),
    MCCommand(
        name = "تفعيل الطيران",
        command = "/ability @s mayfly true",
        category = "القدرات",
        description = "يسمح بالطيران في الإصدارات التي تدعم هذا الأمر.",
        keywords = listOf("طيران", "اطير", "fly", "mayfly", "ability")
    ),
    MCCommand(
        name = "استدعاء قروي",
        command = "/summon villager",
        category = "القرويون",
        description = "يستدعي قرويًا في موقعك.",
        keywords = listOf("قروي", "قرويين", "villager", "summon")
    ),
    MCCommand(
        name = "تغيير وضع اللعب إلى الإبداعي",
        command = "/gamemode creative",
        category = "القدرات",
        description = "يغيّر وضع اللعب إلى الإبداعي.",
        keywords = listOf("إبداعي", "كرييتف", "creative", "gamemode")
    )
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MCLegendTheme {
                MCApp()
            }
        }
    }
}

@Composable
private fun MCLegendTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = Emerald,
            secondary = Gold,
            background = DarkBackground,
            surface = CardColor,
            onPrimary = Color.Black,
            onBackground = WhiteText,
            onSurface = WhiteText
        ),
        content = content
    )
}

@Composable
private fun MCApp() {
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }

    val tabs = listOf(
        Triple("الرئيسية", "⌂", 0),
        Triple("الأوامر", "⌘", 1),
        Triple("المساعد", "✦", 2),
        Triple("العالم", "🌍", 3),
        Triple("المزيد", "☰", 4)
    )

    Scaffold(
        containerColor = DarkBackground,
        bottomBar = {
            NavigationBar(
                containerColor = CardColor,
                contentColor = WhiteText
            ) {
                tabs.forEach { tab ->
                    NavigationBarItem(
                        selected = selectedTab == tab.third,
                        onClick = { selectedTab = tab.third },
                        icon = { Text(tab.second, fontSize = 21.sp) },
                        label = { Text(tab.first, fontSize = 10.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Emerald,
                            selectedTextColor = Emerald,
                            indicatorColor = Emerald.copy(alpha = 0.15f),
                            unselectedIconColor = GrayText,
                            unselectedTextColor = GrayText
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTab) {
                0 -> HomeScreen(onNavigate = { selectedTab = it })
                1 -> CommandsScreen()
                2 -> AssistantScreen()
                3 -> WorldScreen()
                4 -> MoreScreen()
            }
        }
    }
}

@Composable
private fun Header(subtitle: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 22.dp, bottom = 18.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(76.dp)
                .background(
                    color = Emerald.copy(alpha = 0.13f),
                    shape = RoundedCornerShape(22.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text("◆", color = Emerald, fontSize = 48.sp)
        }
        Spacer(Modifier.height(8.dp))
        Text(
            text = "MC LEGEND",
            color = Gold,
            fontSize = 29.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 2.sp
        )
        Text(subtitle, color = GrayText, fontSize = 13.sp)
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        text = title,
        color = Gold,
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(vertical = 12.dp)
    )
}

@Composable
private fun FeatureCard(
    emoji: String,
    title: String,
    description: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = CardColor)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .background(
                        color = Emerald.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(15.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(emoji, fontSize = 26.sp)
            }
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    title,
                    color = WhiteText,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Spacer(Modifier.height(4.dp))
                Text(description, color = GrayText, fontSize = 12.sp)
            }
            Text("‹", color = Emerald, fontSize = 27.sp)
        }
    }
}

@Composable
private fun HomeScreen(onNavigate: (Int) -> Unit) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 18.dp)
    ) {
        item { Header("مرحبًا بك في عالم الأساطير") }
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF15382A)),
                shape = RoundedCornerShape(22.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(20.dp)) {
                    Text(
                        "مرحبًا أيها الأسطوري!",
                        color = Gold,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "اكتشف الأوامر والمودات والعوالم، واصنع مغامرتك الخاصة.",
                        color = WhiteText,
                        fontSize = 13.sp
                    )
                    Spacer(Modifier.height(15.dp))
                    Button(
                        onClick = { onNavigate(1) },
                        colors = ButtonDefaults.buttonColors(containerColor = Emerald)
                    ) {
                        Text("اكتشف الأوامر", color = Color.Black)
                    }
                }
            }
        }
        item { SectionTitle("اكتشف أقسام التطبيق") }
        item {
            FeatureCard(
                "⌘",
                "مكتبة الأوامر",
                "أوامر ماين كرافت مع الشرح والنسخ"
            ) { onNavigate(1) }
        }
        item {
            FeatureCard(
                "✦",
                "المساعد الأسطوري",
                "اسأل عن الأوامر وطريقة استخدامها"
            ) { onNavigate(2) }
        }
        item {
            FeatureCard(
                "🌍",
                "العوالم والتحديات",
                "أفكار مغامرات وتحديات جديدة"
            ) { onNavigate(3) }
        }
        item {
            FeatureCard(
                "🧑‍🌾",
                "القرويون والتجارة",
                "تعرف على المهن والصفقات"
            ) { onNavigate(4) }
        }
        item {
            FeatureCard(
                "🎨",
                "السكنات والموارد",
                "قسم مخصص للسكنات وحزم الموارد"
            ) { onNavigate(4) }
        }
        item {
            Spacer(Modifier.height(20.dp))
            Text(
                "MC LEGEND • اصنع أسطورتك",
                color = GrayText,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(20.dp))
        }
    }
}

@Composable
private fun CommandsScreen() {
    var search by rememberSaveable { mutableStateOf("") }
    var selectedCategory by rememberSaveable { mutableStateOf("الكل") }
    val clipboard = LocalClipboardManager.current

    val categories = listOf(
        "الكل", "الانتقال", "الوقت", "الطقس", "الأدوات", "القدرات", "القرويون"
    )
    val filteredCommands = commandLibrary.filter { command ->
        val matchesCategory = selectedCategory == "الكل" ||
            command.category == selectedCategory
        val matchesSearch = command.name.contains(search, ignoreCase = true) ||
            command.command.contains(search, ignoreCase = true) ||
            command.description.contains(search, ignoreCase = true)
        matchesCategory && matchesSearch
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Header("مكتبة أوامر ماين كرافت")
        OutlinedTextField(
            value = search,
            onValueChange = { search = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("ابحث عن أمر...") },
            singleLine = true,
            shape = RoundedCornerShape(15.dp)
        )
        Spacer(Modifier.height(10.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            categories.forEach { category ->
                FilterChip(
                    selected = selectedCategory == category,
                    onClick = { selectedCategory = category },
                    label = { Text(category) }
                )
            }
        }
        Spacer(Modifier.height(8.dp))
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(filteredCommands, key = { it.command }) { command ->
                CommandCard(
                    command = command,
                    onCopy = {
                        clipboard.setText(AnnotatedString(command.command))
                    }
                )
            }
            if (filteredCommands.isEmpty()) {
                item {
                    Text(
                        "لا توجد أوامر مطابقة للبحث.",
                        color = GrayText,
                        modifier = Modifier.padding(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun CommandCard(command: MCCommand, onCopy: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CardColor),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(Modifier.padding(15.dp)) {
            Text(command.name, color = Gold, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(7.dp))
            Text(command.command, color = Emerald, fontSize = 14.sp)
            Spacer(Modifier.height(5.dp))
            Text(command.description, color = GrayText, fontSize = 12.sp)
            Spacer(Modifier.height(10.dp))
            Button(
                onClick = onCopy,
                colors = ButtonDefaults.buttonColors(containerColor = Emerald)
            ) {
                Text("نسخ الأمر", color = Color.Black)
            }
        }
    }
}

@Composable
private fun AssistantScreen() {
    var question by rememberSaveable { mutableStateOf("") }
    var answer by rememberSaveable { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(18.dp)
    ) {
        Header("المساعد الأسطوري")
        Card(
            colors = CardDefaults.cardColors(containerColor = CardColor),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(Modifier.padding(18.dp)) {
                Text(
                    "✦ مساعد MC LEGEND",
                    color = Gold,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "اكتب ما تريد فعله في ماين كرافت، وسأبحث لك في مكتبة الشروحات المحلية.",
                    color = GrayText
                )
                Spacer(Modifier.height(15.dp))
                OutlinedTextField(
                    value = question,
                    onValueChange = { question = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("مثال: كيف أحصل على ألماس؟") }
                )
                Spacer(Modifier.height(12.dp))
                Button(
                    onClick = {
                        val query = question.trim()
                        val found = if (query.isBlank()) {
                            emptyList()
                        } else {
                            commandLibrary.filter { command ->
                                command.name.contains(query, ignoreCase = true) ||
                                    command.description.contains(query, ignoreCase = true) ||
                                    command.command.contains(query, ignoreCase = true) ||
                                    command.keywords.any { keyword ->
                                        query.contains(keyword, ignoreCase = true) ||
                                            keyword.contains(query, ignoreCase = true)
                                    }
                            }
                        }
                        answer = when {
                            query.isBlank() -> "اكتب سؤالك أولًا، مثل: كيف أغيّر الوقت؟"
                            found.isNotEmpty() -> found.joinToString("\n\n") {
                                "${it.name}\n${it.command}\n${it.description}"
                            }
                            else -> "لم أجد إجابة مطابقة في المكتبة الحالية. جرّب كلمات مثل: الوقت، الطقس، الألماس، القروي أو الطيران."
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Emerald)
                ) {
                    Text("اشرح لي", color = Color.Black)
                }
                if (answer.isNotEmpty()) {
                    Spacer(Modifier.height(18.dp))
                    Text("الشرح:", color = Emerald, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    Text(answer, color = WhiteText)
                }
            }
        }
        SectionTitle("أمثلة يمكنك السؤال عنها")
        listOf(
            "كيف أغيّر الوقت؟",
            "كيف أحصل على ألماس؟",
            "كيف أستدعي قرويًا؟",
            "كيف أفعّل الطيران؟"
        ).forEach { example ->
            Text("• $example", color = GrayText, modifier = Modifier.padding(vertical = 6.dp))
        }
        Spacer(Modifier.height(20.dp))
        Text(
            "ملاحظة: هذا مساعد محلي يعتمد على الشروحات الموجودة داخل التطبيق، ولا يحتاج إلى اتصال بالإنترنت.",
            color = GrayText,
            fontSize = 11.sp
        )
    }
}

@Composable
private fun WorldScreen() {
    val challenges = listOf(
        "تحدي البقاء: ابدأ دون أدوات وحاول البقاء 7 أيام.",
        "تحدي السماء: ابنِ منزلك فوق جزيرة عائمة.",
        "تحدي القروي: أنشئ قرية واحمها من الوحوش.",
        "تحدي الكهوف: استكشف الكهوف دون مغادرة العالم.",
        "تحدي البناء: ابنِ قلعة ضخمة خلال يوم واحد."
    )
    val worldIdeas = listOf(
        Triple("🏝️", "عالم الجزر", "جزر صغيرة وتحديات للبقاء."),
        Triple("🏰", "عالم الممالك", "قرى وقلاع وممالك متنافسة."),
        Triple("🌲", "عالم الغابات", "مغامرات وسط الغابات والكهوف.")
    )
    var chosenChallenge by rememberSaveable { mutableStateOf(challenges.first()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(18.dp)
    ) {
        Header("العوالم والتحديات")
        Text(
            "تحديك المقترح",
            color = Gold,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = CardColor),
            shape = RoundedCornerShape(18.dp)
        ) {
            Column(Modifier.padding(16.dp)) {
                Text(chosenChallenge, color = WhiteText)
                Spacer(Modifier.height(12.dp))
                Button(
                    onClick = {
                        val alternatives = challenges.filterNot { it == chosenChallenge }
                        chosenChallenge = alternatives.random()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Emerald)
                ) {
                    Text("اقترح تحديًا آخر", color = Color.Black)
                }
            }
        }
        SectionTitle("أفكار جاهزة للعوالم")
        worldIdeas.forEach { idea ->
            FeatureCard(idea.first, idea.second, idea.third) { }
        }
        SectionTitle("تحديات إضافية")
        challenges.forEachIndexed { index, challenge ->
            FeatureCard("🌍", "التحدي ${index + 1}", challenge) {
                chosenChallenge = challenge
            }
        }
        Spacer(Modifier.height(16.dp))
        Text(
            "هذه اقتراحات وأفكار تساعدك على إنشاء عالمك داخل Minecraft؛ التطبيق لا ينشئ العالم داخل اللعبة تلقائيًا.",
            color = GrayText,
            fontSize = 11.sp
        )
    }
}

@Composable
private fun MoreScreen() {
    var notificationsEnabled by rememberSaveable { mutableStateOf(true) }
    var selectedSection by rememberSaveable { mutableStateOf("") }

    if (selectedSection.isNotEmpty()) {
        MoreDetailScreen(
            section = selectedSection,
            onBack = { selectedSection = "" }
        )
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(18.dp)
        ) {
            Header("المزيد من MC LEGEND")

            SectionTitle("القرويون والتجارة")

            FeatureCard(
                "🌾",
                "القروي المزارع",
                "تعرف على مهنة المزارع وتجارته."
            ) {
                selectedSection = "farmer"
            }

            FeatureCard(
                "📚",
                "القروي أمين المكتبة",
                "تعرف على الكتب المسحورة وتجارة المكتبي."
            ) {
                selectedSection = "librarian"
            }

            FeatureCard(
                "⚒️",
                "القروي صانع الأدوات والأسلحة",
                "تعرف على صانع الأدوات وصانع الأسلحة."
            ) {
                selectedSection = "smith"
            }

            FeatureCard(
                "🗺️",
                "القروي رسام الخرائط",
                "تعرف على الخرائط وطرق الاستكشاف."
            ) {
                selectedSection = "cartographer"
            }

            SectionTitle("السكنات والموارد")

            FeatureCard(
                "🎨",
                "السكنات",
                "تعرف على قسم السكنات."
            ) {
                selectedSection = "skins"
            }

            FeatureCard(
                "🧱",
                "حزم الموارد",
                "تعرف على حزم المظاهر والموارد."
            ) {
                selectedSection = "resources"
            }

            SectionTitle("التحديثات والإعدادات")

            Card(
                colors = CardDefaults.cardColors(
                    containerColor = CardColor
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text(
                        "إشعارات التحديثات",
                        color = WhiteText,
                        fontWeight = FontWeight.Bold
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "تفعيل خيار الإشعارات",
                            color = GrayText,
                            modifier = Modifier.weight(1f)
                        )

                        Switch(
                            checked = notificationsEnabled,
                            onCheckedChange = {
                                notificationsEnabled = it
                            }
                        )
                    }

                    Text(
                        "هذا المفتاح يحفظ التفضيل داخل الشاشة فقط حاليًا. الإشعارات التلقائية تحتاج إلى إعداد إضافي.",
                        color = GrayText,
                        fontSize = 11.sp
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            Text(
                "MC LEGEND",
                color = Gold,
                fontSize = 22.sp,
                fontWeight = FontWeight.Black,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            Text(
                "الإصدار 2.0 • اصنع أسطورتك",
                color = GrayText,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            Spacer(Modifier.height(20.dp))
        }
    }
}

@Composable
private fun MoreDetailScreen(
    section: String,
    onBack: () -> Unit
) {
    if (section == "skins") {
        SkinHubScreen(onBack = onBack)
        return
    }

    val title = when (section) {
        "farmer" -> "القروي المزارع"
        "librarian" -> "القروي أمين المكتبة"
        "smith" -> "صانع الأدوات والأسلحة"
        "cartographer" -> "القروي رسام الخرائط"
        "skins" -> "السكنات"
        "resources" -> "حزم الموارد"
        else -> "تفاصيل القسم"
    }

    val emoji = when (section) {
        "farmer" -> "🌾"
        "librarian" -> "📚"
        "smith" -> "⚒️"
        "cartographer" -> "🗺️"
        "skins" -> "🎨"
        "resources" -> "🧱"
        else -> "◆"
    }

    val description = when (section) {
        "farmer" ->
            "القروي المزارع متخصص في تجارة المحاصيل وبعض الأطعمة. يمكنه مساعدتك في الحصول على الزمرد مقابل المحاصيل."

        "librarian" ->
            "أمين المكتبة من أهم القرويين؛ فقد يعرض كتبًا مسحورة ومواد مرتبطة بالكتب والتجارة."

        "smith" ->
            "يوجد صانع الأدوات وصانع الأسلحة، ولكل منهما محطة عمل وتجارة مختلفة."

        "cartographer" ->
            "رسام الخرائط يساعدك على شراء الخرائط وبعض الأدوات التي تفيدك في الاستكشاف."

        "skins" ->
            "هذا القسم مخصص للسكنات التي يمكنك استخدامها لتغيير مظهر شخصيتك في Minecraft."

        "resources" ->
            "حزم الموارد تغيّر مظهر اللعبة، مثل الخامات والأصوات وبعض عناصر الواجهة."

        else -> "معلومات هذا القسم."
    }

    val tips = when (section) {
        "farmer" -> listOf(
            "محطة عمل المزارع هي السمّادة (Composter).",
            "قد يتاجر بالقمح والجزر والبطاطس والبنجر، وتختلف الصفقات حسب المستوى والإصدار.",
            "إتمام الصفقات يساعد القروي على رفع مستواه وفتح عروض إضافية."
        )

        "librarian" -> listOf(
            "محطة عمل أمين المكتبة هي المنصة (Lectern).",
            "قد يعرض كتبًا مسحورة ومواد ورقية وكتبًا.",
            "تختلف عروض التجارة بحسب مستوى القروي وإصدار اللعبة."
        )

        "smith" -> listOf(
            "محطة عمل صانع الأدوات هي طاولة الحدادة (Smithing Table).",
            "محطة عمل صانع الأسلحة هي حجر الجلخ (Grindstone).",
            "قد يبيعان أدوات وأسلحة، وتختلف العروض حسب مستوى القروي."
        )

        "cartographer" -> listOf(
            "محطة عمل رسام الخرائط هي طاولة رسم الخرائط (Cartography Table).",
            "يمكنه التجارة بالورق والخرائط وبعض أدوات الاستكشاف.",
            "قد تختلف الخرائط والعروض حسب إصدار اللعبة ومستوى القروي."
        )

        "skins" -> listOf(
            "السكن يغيّر مظهر شخصيتك داخل اللعبة.",
            "تأكد من توافق السكن مع إصدار Minecraft الذي تستخدمه.",
            "مكتبة تنزيل واستيراد السكنات غير متاحة في التطبيق حاليًا."
        )

        "resources" -> listOf(
            "حزم الموارد قد تغيّر الخامات والأصوات ومظاهر عناصر اللعبة.",
            "تحقق من توافق الحزمة مع إصدار Minecraft Bedrock لديك.",
            "استعمل ملفات من مصادر موثوقة. استيراد الحزم من داخل التطبيق غير متاح حاليًا."
        )

        else -> listOf("لا توجد معلومات إضافية حاليًا.")
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(18.dp)
    ) {
        OutlinedButton(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("العودة إلى المزيد")
        }

        Header(title)

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    CardColor,
                    RoundedCornerShape(18.dp)
                )
                .padding(22.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(emoji, fontSize = 48.sp)
        }

        SectionTitle("نبذة عن القسم")

        Text(
            description,
            color = WhiteText,
            fontSize = 15.sp,
            lineHeight = 23.sp
        )

        SectionTitle("معلومات ونصائح")

        tips.forEach { tip ->
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = CardColor
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 5.dp)
            ) {
                Text(
                    "•  $tip",
                    color = WhiteText,
                    fontSize = 14.sp,
                    lineHeight = 22.sp,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }

        Spacer(Modifier.height(20.dp))
    }
}
