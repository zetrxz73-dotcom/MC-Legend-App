
package com.legend.mc

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
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

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MCTheme {
                MCApp()
            }
        }
    }
}

@Composable
fun MCTheme(content: @Composable () -> Unit) {
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

data class MCCommand(
    val name: String,
    val command: String,
    val category: String,
    val description: String
)

private val commands = listOf(
    MCCommand(
        "الانتقال إلى لاعب",
        "/tp @s Player",
        "الانتقال",
        "ينقلك إلى اللاعب الذي تحدد اسمه."
    ),
    MCCommand(
        "تغيير الوقت إلى النهار",
        "/time set day",
        "الوقت",
        "يجعل الوقت نهارًا."
    ),
    MCCommand(
        "تغيير الوقت إلى الليل",
        "/time set night",
        "الوقت",
        "يجعل الوقت ليلًا."
    ),
    MCCommand(
        "تغيير الطقس",
        "/weather clear",
        "الطقس",
        "يجعل الطقس صافيًا."
    ),
    MCCommand(
        "الحصول على ألماس",
        "/give @s diamond 64",
        "الأدوات",
        "يعطيك 64 قطعة ألماس."
    ),
    MCCommand(
        "الطيران",
        "/ability @s mayfly true",
        "القدرات",
        "يسمح بالطيران في الإصدارات التي تدعم هذا الأمر."
    ),
    MCCommand(
        "استدعاء قروي",
        "/summon villager",
        "القرويون",
        "يستدعي قرويًا في موقعك."
    ),
    MCCommand(
        "تغيير وضع اللعب",
        "/gamemode creative",
        "القدرات",
        "يغير وضع اللعب إلى الإبداعي."
    )
)

@Composable
fun MCApp() {
    var selectedTab by remember { mutableIntStateOf(0) }

    Scaffold(
        containerColor = DarkBackground,
        bottomBar = {
            NavigationBar(
                containerColor = CardColor,
                contentColor = WhiteText
            ) {
                val tabs = listOf(
                    Triple("الرئيسية", "⌂", 0),
                    Triple("الأوامر", "⌘", 1),
                    Triple("المساعد", "✦", 2),
                    Triple("العالم", "🌍", 3),
                    Triple("المزيد", "☰", 4)
                )

                tabs.forEach { tab ->
                    NavigationBarItem(
                        selected = selectedTab == tab.third,
                        onClick = { selectedTab = tab.third },
                        icon = {
                            Text(tab.second, fontSize = 21.sp)
                        },
                        label = {
                            Text(tab.first, fontSize = 10.sp)
                        },
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
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (selectedTab) {
                0 -> HomeScreen(
                    onNavigate = { selectedTab = it }
                )
                1 -> CommandsScreen()
                2 -> AssistantScreen()
                3 -> WorldScreen()
                4 -> MoreScreen(
                    onNavigate = { selectedTab = it }
                )
            }
        }
    }
}

@Composable
fun Header(
    subtitle: String = "عالمك الأسطوري يبدأ هنا"
) {
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
                    Emerald.copy(alpha = 0.13f),
                    RoundedCornerShape(22.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text("◆", color = Emerald, fontSize = 48.sp)
        }

        Spacer(Modifier.height(8.dp))

        Text(
            "MC LEGEND",
            color = Gold,
            fontSize = 29.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 2.sp
        )

        Text(
            subtitle,
            color = GrayText,
            fontSize = 13.sp
        )
    }
}

@Composable
fun SectionTitle(title: String) {
    Text(
        title,
        color = Gold,
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(vertical = 12.dp)
    )
}

@Composable
fun FeatureCard(
    emoji: String,
    title: String,
    description: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = CardColor
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .background(
                        Emerald.copy(alpha = 0.12f),
                        RoundedCornerShape(15.dp)
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
                Text(
                    description,
                    color = GrayText,
                    fontSize = 12.sp
                )
            }

            Text("‹", color = Emerald, fontSize = 27.sp)
        }
    }
}

@Composable
fun HomeScreen(onNavigate: (Int) -> Unit) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 18.dp)
    ) {
        item {
            Header("مرحبًا بك في عالم الأساطير")
        }

        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF15382A)
                ),
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
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Emerald
                        )
                    ) {
                        Text("اكتشف الأوامر")
                    }
                }
            }
        }

        item {
            SectionTitle("اكتشف أقسام التطبيق")
        }

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
                "المساعد الذكي",
                "تعرف على الأوامر وطريقة استخدامها"
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
                "🧑",
                "القرويون والتجارة",
                "تعرف على المهن والصفقات"
            ) { onNavigate(4) }
        }

        item {
            FeatureCard(
                "🎨",
                "السكنات والموارد",
                "اكتشف تخصيصات عالمك"
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
fun CommandsScreen() {
    var search by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("الكل") }
    val clipboard = LocalClipboardManager.current

    val categories = listOf(
        "الكل", "الانتقال", "الوقت", "الطقس",
        "الأدوات", "القدرات", "القرويون"
    )

    val filtered = commands.filter {
        (category == "الكل" || it.category == category) &&
        (it.name.contains(search, ignoreCase = true) ||
         it.command.contains(search, ignoreCase = true))
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
                .horizontalScroll(rememberScrollState())
        ) {
            categories.forEach {
                FilterChip(
                    selected = category == it,
                    onClick = { category = it },
                    label = { Text(it) },
                    modifier = Modifier.padding(end = 5.dp)
                )
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {
            items(filtered) { command ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = CardColor
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(Modifier.padding(15.dp)) {
                        Text(
                            command.name,
                            color = Gold,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(Modifier.height(7.dp))

                        Text(
                            command.command,
                            color = Emerald,
                            fontSize = 14.sp
                        )

                        Spacer(Modifier.height(5.dp))

                        Text(
                            command.description,
                            color = GrayText,
                            fontSize = 12.sp
                        )

                        Spacer(Modifier.height(10.dp))

                        Button(
                            onClick = {
                                clipboard.setText(
                                    AnnotatedString(command.command)
                                )
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Emerald
                            )
                        ) {
                            Text("نسخ الأمر")
                        }
                    }
                }
            }

            if (filtered.isEmpty()) {
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
fun AssistantScreen() {
    var question by remember { mutableStateOf("") }
    var answer by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(18.dp)
    ) {
        Header("المساعد الأسطوري")

        Card(
            colors = CardDefaults.cardColors(
                containerColor = CardColor
            ),
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
                    "اكتب اسم الأمر أو ما تريد فعله في ماين كرافت، وسأعرض لك شرحًا من المكتبة المحلية.",
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
                        val found = commands.filter {
                            it.name.contains(question, true) ||
                            it.description.contains(question, true) ||
                            it.command.contains(question, true)
                        }

                        answer = if (found.isNotEmpty()) {
                            found.joinToString("\n\n") {
                                "${it.name}\n${it.command}\n${it.description}"
                            }
                        } else {
                            "لم أجد شرحًا مطابقًا في المكتبة الحالية. جرّب كتابة اسم أمر مثل الوقت أو الطقس أو الألماس."
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("اشرح لي")
                }

                if (answer.isNotEmpty()) {
                    Spacer(Modifier.height(18.dp))
                    Text(
                        "الشرح:",
                        color = Emerald,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(answer, color = WhiteText)
                }
            }
        }

        SectionTitle("أمثلة يمكنك السؤال عنها")

        listOf(
            "كيف أغير الوقت؟",
            "كيف أحصل على ألماس؟",
            "كيف أستدعي قرويًا؟",
            "كيف أنتقل إلى لاعب؟"
        ).forEach {
            Text(
                "• $it",
                color = GrayText,
                modifier = Modifier.padding(vertical = 6.dp)
            )
        }
    }
}

@Composable
fun WorldScreen() {
    val challenges = listOf(
        "تحدي البقاء: ابدأ دون أدوات وحاول البقاء 7 أيام.",
        "تحدي السماء: ابنِ منزلك فوق جزيرة عائمة.",
        "تحدي القروي: أنشئ قرية واحمها من الوحوش.",
        "تحدي الكهوف: استكشف الكهوف دون مغادرة العالم.",
        "تحدي البناء: ابنِ قلعة ضخمة خلال يوم واحد."
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(18.dp)
    ) {
        Header("العوالم والتحديات")

        Text(
            "اختر مغامرتك القادمة!",
            color = Gold,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )

        challenges.forEachIndexed { index, challenge ->
            FeatureCard(
                "🌍",
                "التحدي ${index + 1}",
                challenge
            ) {}
        }

        SectionTitle("منشئ أفكار العوالم")

        Text(
            "يمكنك استخدام هذه الأفكار لتصميم عالم جديد في ماين كرافت.",
            color = GrayText
        )

        FeatureCard(
            "🏝️",
            "عالم الجزر",
            "جزر صغيرة وتحديات للبقاء."
        ) {}

        FeatureCard(
            "🏰",
            "عالم الممالك",
            "قرى وقلاع وممالك متنافسة."
        ) {}

        FeatureCard(
            "🌲",
            "عالم الغابات",
            "مغامرات وسط الغابات والكهوف."
        ) {}
    }
}

@Composable
fun MoreScreen(onNavigate: (Int) -> Unit) {
    var notifications by remember { mutableStateOf(true) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(18.dp)
    ) {
        Header("المزيد من MC LEGEND")

        SectionTitle("القرويون والتجارة")

        FeatureCard(
            "🧑‍🌾",
            "القروي المزارع",
            "يعمل في الزراعة ويتاجر بالمحاصيل."
        ) {}

        FeatureCard(
            "📚",
            "القروي أمين المكتبة",
            "يتاجر بالكتب والأغراض السحرية."
        ) {}

        FeatureCard(
            "⚒️",
            "القروي الحداد",
            "يتاجر بالأدوات والأسلحة والدروع."
        ) {}

        FeatureCard(
            "🗺️",
            "القروي رسام الخرائط",
            "يبيع الخرائط ويقدم خرائط الاستكشاف."
        ) {}

        SectionTitle("السكنات والموارد")

        FeatureCard(
            "🎨",
            "السكنات",
            "قسم مخصص لعرض السكنات وإضافتها لاحقًا."
        ) {}

        FeatureCard(
            "🧱",
            "حزم الموارد",
            "قسم لحزم المظاهر والموارد المتوافقة."
        ) {}

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
                        checked = notifications,
                        onCheckedChange = {
                            notifications = it
                        }
                    )
                }

                Text(
                    "هذا الخيار يحفظ حالة التفضيل داخل الشاشة حاليًا. إرسال إشعارات تلقائية يحتاج إلى إعداد إضافي لاحقًا.",
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
    }
}
