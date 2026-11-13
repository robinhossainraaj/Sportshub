package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun DrawerContent(
    onMenuItemClick: (String) -> Unit,
    onCloseDrawer: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .fillMaxWidth()
            .background(Color.Black)
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
            .testTag("app_drawer")
    ) {
        // Drawer Header with Z-Logo SVG representation
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
        ) {
            Canvas(
                modifier = Modifier
                    .size(80.dp)
                    .testTag("drawer_logo")
            ) {
                // outer triangle representer
                val trianglePath = Path().apply {
                    moveTo(6f * density, 3f * density)
                    lineTo(66f * density, 30.5f * density)
                    lineTo(6f * density, 58f * density)
                    close()
                }
                
                drawPath(
                    path = trianglePath,
                    color = Color.White,
                    style = Stroke(
                        width = 4f * density,
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round
                    )
                )

                // inner Z paths
                drawLine(
                    color = Color.White,
                    start = androidx.compose.ui.geometry.Offset(22f * density, 20f * density),
                    end = androidx.compose.ui.geometry.Offset(42f * density, 20f * density),
                    strokeWidth = 3f * density,
                    cap = StrokeCap.Round
                )
                drawLine(
                    color = Color.White,
                    start = androidx.compose.ui.geometry.Offset(42f * density, 20f * density),
                    end = androidx.compose.ui.geometry.Offset(22f * density, 40f * density),
                    strokeWidth = 3f * density,
                    cap = StrokeCap.Round
                )
                drawLine(
                    color = Color.White,
                    start = androidx.compose.ui.geometry.Offset(22f * density, 40f * density),
                    end = androidx.compose.ui.geometry.Offset(42f * density, 40f * density),
                    strokeWidth = 3f * density,
                    cap = StrokeCap.Round
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Version: 2.2",
                color = TextPrimary,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(start = 6.dp)
            )
        }

        HorizontalDivider(color = Color(0xFF1F1F26), thickness = 1.dp)
        Spacer(modifier = Modifier.height(8.dp))

        // MenuItem List
        val menuItems = listOf(
            DrawerItemData("Network Stream", Icons.Default.PlayArrow),
            DrawerItemData("Playlists", Icons.Default.List),
            DrawerItemData("Floating Player", Icons.Default.PlayArrow),
            DrawerItemData("Force Low Quality", Icons.Default.Settings),
            DrawerItemData("Cricket Score", Icons.Default.Star),
            DrawerItemData("Football Score", Icons.Default.Star),
            DrawerItemData("Telegram", Icons.Default.Send),
            DrawerItemData("Contact Us", Icons.Default.Email),
            DrawerItemData("Website", Icons.Default.Info),
            DrawerItemData("Share App", Icons.Default.Share),
            DrawerItemData("Copyright", Icons.Default.Info),
            DrawerItemData("Exit", Icons.AutoMirrored.Default.ExitToApp)
        )

        menuItems.forEach { item ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp, horizontal = 4.dp)
                    .clickable {
                        onMenuItemClick(item.title)
                        onCloseDrawer()
                    }
                    .testTag("drawer_menu_item_${item.title.lowercase().replace(" ", "_")}"),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = item.icon,
                    contentDescription = item.title,
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = item.title,
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

data class DrawerItemData(
    val title: String,
    val icon: ImageVector
)
