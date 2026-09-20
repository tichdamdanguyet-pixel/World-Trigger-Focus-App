package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Female
import androidx.compose.material.icons.filled.Male
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.SportsKabaddi
import androidx.compose.material.icons.filled.TrackChanges
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.R
import com.example.model.AVAILABLE_POSITIONS
import com.example.model.AgentProfile
import com.example.model.GENDER_OPTIONS
import com.example.ui.AgentUiState
import com.example.ui.theme.Cyan300
import com.example.ui.theme.Cyan400
import com.example.ui.theme.Cyan500
import com.example.ui.theme.Cyan950
import com.example.ui.theme.GreenSuccess
import com.example.ui.theme.RankARank
import com.example.ui.theme.RankBRank
import com.example.ui.theme.RankCRank
import com.example.ui.theme.RankSRank
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate300
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate600
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900
import com.example.ui.theme.Slate950

@Composable
fun AgentRegistrationScreen(
    uiState: AgentUiState,
    onSaveProfile: (AgentProfile) -> Unit,
    onDismiss: (() -> Unit)? = null,
    isEditMode: Boolean = false,
    modifier: Modifier = Modifier
) {
    var name by remember(uiState.profile) { mutableStateOf(uiState.profile.name) }
    var age by remember(uiState.profile) { mutableIntStateOf(uiState.profile.age) }
    var gender by remember(uiState.profile) { mutableStateOf(uiState.profile.gender) }
    var position by remember(uiState.profile) { mutableStateOf(uiState.profile.position) }
    var pictureUri by remember(uiState.profile) { mutableStateOf(uiState.profile.pictureUri) }

    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Modern Android Photo Picker (zero-permission)
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            pictureUri = uri.toString()
            errorMessage = null
        }
    }

    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Slate950)
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Header Banner with Sci-Fi Art
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Slate800, RoundedCornerShape(14.dp)),
            colors = CardDefaults.cardColors(containerColor = Slate900),
            shape = RoundedCornerShape(14.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_border_onboarding),
                        contentDescription = "Border Registration Header",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )

                    // Gradient overlay
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(Color.Transparent, Slate900.copy(alpha = 0.95f))
                                )
                            )
                    )

                    // Close button if editing
                    if (onDismiss != null) {
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(8.dp)
                                .background(Slate950.copy(alpha = 0.7f), CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Đóng",
                                tint = Slate300
                            )
                        }
                    }

                    // Tactical Badge Top Left
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(12.dp)
                            .background(Slate950.copy(alpha = 0.85f), RoundedCornerShape(6.dp))
                            .border(1.dp, Cyan500.copy(alpha = 0.6f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Badge,
                                contentDescription = null,
                                tint = Cyan400,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isEditMode) "CẬP NHẬT ĐỊNH DANH" else "HỒ SƠ KHAI BÁO BAN ĐẦU",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.Monospace,
                                color = Cyan300
                            )
                        }
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (isEditMode) "CẬP NHẬT HỒ SƠ ĐẶC VỤ" else "ĐĂNG KÝ HỒ SƠ ĐẶC VỤ BORDER",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace,
                        color = Cyan300,
                        letterSpacing = 1.sp,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Vui lòng hoàn tất thông tin định danh tác chiến để kết nối với mạng lưới chỉ huy Border Defense Agency.",
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        color = Slate400,
                        textAlign = TextAlign.Center,
                        lineHeight = 15.sp
                    )
                }
            }
        }

        // AGENT PICTURE OF YOURSELF (AVATAR CARD)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Slate800, RoundedCornerShape(14.dp))
                .testTag("card_agent_picture"),
            colors = CardDefaults.cardColors(containerColor = Slate900),
            shape = RoundedCornerShape(14.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "ẢNH ĐẠI DIỆN ĐẶC VỤ (PICTURE OF YOURSELF)",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = Cyan400,
                    letterSpacing = 0.5.sp
                )

                // Circular Frame with Cyber reticle styling
                Box(
                    modifier = Modifier
                        .size(110.dp)
                        .clip(CircleShape)
                        .background(Cyan950)
                        .border(3.dp, Cyan400, CircleShape)
                        .clickable {
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        }
                        .testTag("avatar_preview_container"),
                    contentAlignment = Alignment.Center
                ) {
                    if (!pictureUri.isNullOrBlank()) {
                        AsyncImage(
                            model = pictureUri,
                            contentDescription = "Ảnh đặc vụ",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Image(
                            painter = painterResource(id = R.drawable.avatar_belen),
                            contentDescription = "Ảnh đại diện mặc định",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }

                    // Hover hint overlay
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.25f)),
                        contentAlignment = Alignment.BottomCenter
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Slate950.copy(alpha = 0.75f))
                                .padding(vertical = 3.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.AddAPhoto,
                                contentDescription = null,
                                tint = Cyan300,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "ĐỔI ẢNH",
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                color = Cyan300
                            )
                        }
                    }
                }

                // Buttons to update photo
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Button(
                        onClick = {
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Cyan500,
                            contentColor = Slate950
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("btn_pick_picture")
                    ) {
                        Icon(
                            imageVector = Icons.Default.PhotoLibrary,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (pictureUri == null) "TẢI ẢNH BẢN THÂN (SELFIE/PHOTO)" else "THAY ĐỔI ẢNH KHÁC",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                if (pictureUri != null) {
                    Text(
                        text = "Dùng lại avatar mặc định Belén",
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        color = Slate400,
                        modifier = Modifier
                            .clickable { pictureUri = null }
                            .padding(4.dp)
                    )
                }
            }
        }

        // AGENT DETAILS FORM
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Slate800, RoundedCornerShape(14.dp))
                .testTag("card_agent_form"),
            colors = CardDefaults.cardColors(containerColor = Slate900),
            shape = RoundedCornerShape(14.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 1. TÊN ĐẶC VỤ (NAME)
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "TÊN ĐẶC VỤ (NAME)",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = Slate300
                        )
                        Text(
                            text = "${name.length}/30",
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace,
                            color = Slate500
                        )
                    }

                    OutlinedTextField(
                        value = name,
                        onValueChange = {
                            if (it.length <= 30) {
                                name = it
                                errorMessage = null
                            }
                        },
                        placeholder = {
                            Text(
                                text = "Nhập tên đặc vụ của bạn...",
                                color = Slate600,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 12.sp
                            )
                        },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_agent_name"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Cyan400,
                            unfocusedBorderColor = Slate800,
                            focusedTextColor = Slate100,
                            unfocusedTextColor = Slate200,
                            cursorColor = Cyan400
                        ),
                        shape = RoundedCornerShape(10.dp)
                    )
                }

                // 2. TUỔI ĐẶC VỤ (AGE)
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "TUỔI ĐẶC VỤ (AGE)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = Slate300
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        IconButton(
                            onClick = { if (age > 10) age-- },
                            modifier = Modifier
                                .background(Slate800, RoundedCornerShape(8.dp))
                                .border(1.dp, Slate700, RoundedCornerShape(8.dp))
                                .testTag("btn_age_decrease")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Remove,
                                contentDescription = "Giảm tuổi",
                                tint = Cyan300
                            )
                        }

                        OutlinedTextField(
                            value = age.toString(),
                            onValueChange = { str ->
                                val filtered = str.filter { it.isDigit() }
                                val parsed = filtered.toIntOrNull()
                                if (parsed != null && parsed in 1..99) {
                                    age = parsed
                                    errorMessage = null
                                } else if (filtered.isEmpty()) {
                                    age = 0
                                }
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            textStyle = androidx.compose.ui.text.TextStyle(
                                textAlign = TextAlign.Center,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Black,
                                fontSize = 16.sp
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("input_agent_age"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Cyan400,
                                unfocusedBorderColor = Slate800,
                                focusedTextColor = Slate100,
                                unfocusedTextColor = Slate200
                            ),
                            shape = RoundedCornerShape(10.dp)
                        )

                        IconButton(
                            onClick = { if (age < 99) age++ },
                            modifier = Modifier
                                .background(Slate800, RoundedCornerShape(8.dp))
                                .border(1.dp, Slate700, RoundedCornerShape(8.dp))
                                .testTag("btn_age_increase")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Tăng tuổi",
                                tint = Cyan300
                            )
                        }
                    }
                }

                // 3. GIỚI TÍNH (GENDER)
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "GIỚI TÍNH (GENDER)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = Slate300
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        GENDER_OPTIONS.forEach { opt ->
                            val isSelected = gender.equals(opt, ignoreCase = true)
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) Cyan500 else Slate800)
                                    .border(1.dp, if (isSelected) Cyan400 else Slate700, RoundedCornerShape(8.dp))
                                    .clickable {
                                        gender = opt
                                        errorMessage = null
                                    }
                                    .padding(vertical = 10.dp)
                                    .testTag("gender_chip_$opt"),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = when (opt) {
                                            "Nữ" -> Icons.Default.Female
                                            "Nam" -> Icons.Default.Male
                                            else -> Icons.Default.Face
                                        },
                                        contentDescription = null,
                                        tint = if (isSelected) Slate950 else Slate300,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = opt,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace,
                                        color = if (isSelected) Slate950 else Slate300
                                    )
                                }
                            }
                        }
                    }
                }

                // 4. VỊ TRÍ TÁC CHIẾN (COMBAT POSITION)
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "VỊ TRÍ TÁC CHIẾN (COMBAT POSITION)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = Slate300
                    )

                    AVAILABLE_POSITIONS.forEach { pos ->
                        val isSelected = position.equals(pos.title, ignoreCase = true)
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(
                                    width = if (isSelected) 1.5.dp else 1.dp,
                                    color = if (isSelected) Cyan400 else Slate800,
                                    shape = RoundedCornerShape(10.dp)
                                )
                                .clickable {
                                    position = pos.title
                                    errorMessage = null
                                }
                                .testTag("position_card_${pos.code}"),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) Cyan950.copy(alpha = 0.35f) else Slate950
                            ),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .background(
                                                if (isSelected) Cyan500 else Slate800,
                                                RoundedCornerShape(6.dp)
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = pos.code,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Black,
                                            fontFamily = FontFamily.Monospace,
                                            color = if (isSelected) Slate950 else Slate300
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(10.dp))

                                    Column {
                                        Text(
                                            text = pos.title,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = FontFamily.Monospace,
                                            color = if (isSelected) Cyan300 else Slate200
                                        )
                                        Text(
                                            text = pos.description,
                                            fontSize = 9.sp,
                                            fontFamily = FontFamily.Monospace,
                                            color = Slate400,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }

                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = "Selected",
                                        tint = Cyan400,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Error message if any
        AnimatedVisibility(
            visible = errorMessage != null,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            errorMessage?.let { msg ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF3B1219), RoundedCornerShape(8.dp))
                        .border(1.dp, RankSRank, RoundedCornerShape(8.dp))
                        .padding(10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = msg,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        color = RankSRank,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        // Submit Button
        Button(
            onClick = {
                val trimmedName = name.trim()
                if (trimmedName.isEmpty()) {
                    errorMessage = "Vui lòng nhập tên đặc vụ của bạn!"
                    return@Button
                }
                if (age <= 0) {
                    errorMessage = "Vui lòng nhập độ tuổi hợp lệ (> 0)!"
                    return@Button
                }

                val profile = AgentProfile(
                    name = trimmedName,
                    age = age,
                    gender = gender,
                    position = position,
                    pictureUri = pictureUri,
                    isRegistered = true
                )
                onSaveProfile(profile)
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = Cyan500,
                contentColor = Slate950
            ),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("btn_save_profile")
        ) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (isEditMode) "LƯU CẬP NHẬT HỒ SƠ" else "KÍCH HOẠT HỒ SƠ & BƯỚC VÀO TRẬN ĐỊA",
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Black,
                fontSize = 12.sp,
                letterSpacing = 1.sp
            )
        }

        if (isEditMode && onDismiss != null) {
            OutlinedButton(
                onClick = onDismiss,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Slate300),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .testTag("btn_cancel_edit_profile")
            ) {
                Text(
                    text = "HỦY THAY ĐỔI",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}
