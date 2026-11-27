package com.legend.mc

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
internal fun SkinHubScreen(onBack: () -> Unit) {
    var page by rememberSaveable { mutableStateOf("home") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0B100E))
            .verticalScroll(rememberScrollState())
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        OutlinedButton(
            onClick = {
                if (page == "home") onBack() else page = "home"
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (page == "home") "العودة إلى المزيد" else "العودة إلى السكنات")
        }

        Text(
            "استوديو السكنات",
            color = Color(0xFFFFC857),
            fontSize = 25.sp,
            fontWeight = FontWeight.Black
        )

        when (page) {
            "library" -> {
                Text(
                    "مكتبة السكنات",
                    color = Color.White,
                    fontSize = 21.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "هنا سنضيف السكنات الجاهزة، والبحث والتصنيفات والمعاينة والاستيراد إلى Minecraft Bedrock.",
                    color = Color.LightGray,
                    fontSize = 15.sp
                )
            }

            "studio" -> {
                Text(
                    "صناعة السكنات",
                    color = Color.White,
                    fontSize = 21.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "هنا سنضيف محرر تصميم السكنات وتلوين أجزاء الشخصية وحفظ التصميم وتصديره.",
                    color = Color.LightGray,
                    fontSize = 15.sp
                )
            }

            else -> {
                Text(
                    "اختر ما تريد فعله",
                    color = Color.White,
                    fontSize = 17.sp
                )

                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF17221C)
                    ),
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(Modifier.padding(18.dp)) {
                        Text("🎭 مكتبة السكنات", color = Color.White, fontSize = 19.sp, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(8.dp))
                        Text("تصفح السكنات الجاهزة واختر مظهر شخصيتك.", color = Color.LightGray)
                        Spacer(Modifier.height(14.dp))
                        Button(
                            onClick = { page = "library" },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00C878))
                        ) {
                            Text("فتح المكتبة", color = Color.Black)
                        }
                    }
                }

                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF17221C)
                    ),
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(Modifier.padding(18.dp)) {
                        Text("🎨 استوديو التصميم", color = Color.White, fontSize = 19.sp, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(8.dp))
                        Text("اصنع سكنك الخاص وعدّل ألوانه بنفسك.", color = Color.LightGray)
                        Spacer(Modifier.height(14.dp))
                        Button(
                            onClick = { page = "studio" },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFC857))
                        ) {
                            Text("ابدأ التصميم", color = Color.Black)
                        }
                    }
                }
            }
        }
    }
}
