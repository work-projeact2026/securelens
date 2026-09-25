package com.example

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.example.ui.components.SecureBottomNav
import com.example.ui.screens.home.HomeScreen
import com.example.ui.screens.intruder.*
import com.example.ui.screens.onboarding.*
import com.example.ui.screens.recorder.RecorderScreen
import com.example.ui.screens.scan.*
import com.example.ui.screens.settings.*
import com.example.ui.screens.vault.*
import com.example.ui.theme.SecureBackground
import com.example.ui.theme.SecureLensTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val settingsStore = SecureLensApp.instance.settingsStore
            val isDark by settingsStore.isDarkMode.collectAsState(initial = false)
            val currentLang by settingsStore.selectedLanguage.collectAsState(initial = "en")
            val coroutineScope = rememberCoroutineScope()

            SecureLensTheme(darkTheme = isDark) {
                val navController = rememberNavController()
                val currentBackStack by navController.currentBackStackEntryAsState()
                val currentRoute = currentBackStack?.destination?.route ?: ""

                // Multiple permissions launcher for quick grant flow
                val permissionLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestMultiplePermissions()
                ) { /* permissions evaluated per feature */ }

                // Top level bottom nav tabs
                val isBottomNavVisible = currentRoute in listOf(
                    "home", "devices", "rooms", "settings"
                )

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = SecureBackground,
                    contentWindowInsets = WindowInsets.safeDrawing,
                    bottomBar = {
                        if (isBottomNavVisible) {
                            SecureBottomNav(
                                currentRoute = currentRoute,
                                onNavigate = { destination ->
                                    if (destination != currentRoute) {
                                        navController.navigate(destination) {
                                            popUpTo("home") { saveState = true }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    }
                                }
                            )
                        }
                    }
                ) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = "splash",
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        // 1. Splash Screen
                        composable("splash") {
                            SplashScreen(
                                onSplashFinished = {
                                    coroutineScope.launch {
                                        val completed = settingsStore.isOnboardingCompleted.first()
                                        if (completed) {
                                            navController.navigate("home") {
                                                popUpTo("splash") { inclusive = true }
                                            }
                                        } else {
                                            navController.navigate("language_select") {
                                                popUpTo("splash") { inclusive = true }
                                            }
                                        }
                                    }
                                }
                            )
                        }

                        // 2. Language Selection
                        composable("language_select") {
                            LanguageSelectionScreen(
                                currentLanguage = currentLang,
                                onLanguageSelected = { lang ->
                                    coroutineScope.launch {
                                        settingsStore.setSelectedLanguage(lang)
                                        navController.navigate("onboarding_carousel")
                                    }
                                }
                            )
                        }

                        // 3. Onboarding Carousel (4 pages matching D01)
                        composable("onboarding_carousel") {
                            OnboardingCarouselScreen(
                                onFinishOnboarding = {
                                    navController.navigate("promotion")
                                }
                            )
                        }

                        // 4. Feature / Promotion Screen
                        composable("promotion") {
                            PromotionScreen(
                                onContinueFree = {
                                    navController.navigate("permissions_setup")
                                }
                            )
                        }

                        // 5. Permissions Setup
                        composable("permissions_setup") {
                            PermissionsSetupScreen(
                                onRequestPermission = { perm ->
                                    permissionLauncher.launch(arrayOf(perm))
                                },
                                onContinue = {
                                    navController.navigate("vault_pin_create")
                                }
                            )
                        }

                        // 6. Vault PIN Creation
                        composable("vault_pin_create") {
                            var tempPin by remember { mutableStateOf("") }
                            VaultPinSetupScreen(
                                isConfirming = false,
                                onPinEntered = { pin ->
                                    tempPin = pin
                                    navController.navigate("vault_pin_confirm/$pin")
                                },
                                onSkip = {
                                    coroutineScope.launch {
                                        settingsStore.setVaultPin("")
                                        settingsStore.setOnboardingCompleted(true)
                                        navController.navigate("setup_complete") {
                                            popUpTo("onboarding_carousel") { inclusive = true }
                                        }
                                    }
                                }
                            )
                        }

                        composable(
                            "vault_pin_confirm/{initialPin}",
                            arguments = listOf(navArgument("initialPin") { type = NavType.StringType })
                        ) { backStackEntry ->
                            val initialPin = backStackEntry.arguments?.getString("initialPin") ?: ""
                            VaultPinSetupScreen(
                                isConfirming = true,
                                onPinEntered = { confirmedPin ->
                                    if (confirmedPin == initialPin) {
                                        coroutineScope.launch {
                                            settingsStore.setVaultPin(confirmedPin)
                                            settingsStore.setOnboardingCompleted(true)
                                            navController.navigate("setup_complete") {
                                                popUpTo("onboarding_carousel") { inclusive = true }
                                            }
                                        }
                                    }
                                }
                            )
                        }

                        // 7. Setup Complete
                        composable("setup_complete") {
                            SetupCompleteScreen(
                                onGoToApp = {
                                    navController.navigate("home") {
                                        popUpTo("setup_complete") { inclusive = true }
                                    }
                                }
                            )
                        }

                        // 8. Main Home Screen (D02)
                        composable("home") {
                            HomeScreen(
                                onStartFullScan = { navController.navigate("scan_methods") },
                                onScanWifi = {
                                    permissionLauncher.launch(
                                        arrayOf(
                                            Manifest.permission.ACCESS_FINE_LOCATION,
                                            Manifest.permission.ACCESS_WIFI_STATE
                                        )
                                    )
                                    navController.navigate("wifi_scan")
                                },
                                onScanBluetooth = {
                                    permissionLauncher.launch(
                                        arrayOf(
                                            Manifest.permission.BLUETOOTH_SCAN,
                                            Manifest.permission.BLUETOOTH_CONNECT
                                        )
                                    )
                                    navController.navigate("bluetooth_scan")
                                },
                                onCameraDetection = {
                                    permissionLauncher.launch(arrayOf(Manifest.permission.CAMERA))
                                    navController.navigate("camera_finder")
                                },
                                onTipsGuide = { navController.navigate("help_support") },
                                onMagneticScan = { navController.navigate("magnetic_scan") },
                                onThermalView = {
                                    permissionLauncher.launch(arrayOf(Manifest.permission.CAMERA))
                                    navController.navigate("thermal_view")
                                },
                                onSecureRecorder = {
                                    permissionLauncher.launch(
                                        arrayOf(
                                            Manifest.permission.CAMERA,
                                            Manifest.permission.RECORD_AUDIO,
                                            Manifest.permission.POST_NOTIFICATIONS
                                        )
                                    )
                                    navController.navigate("recorder")
                                },
                                onPrivateVault = { navController.navigate("vault_unlock") },
                                onIntruderGuard = { navController.navigate("intruder_guard") },
                                onNotificationClick = { navController.navigate("notification_settings") }
                            )
                        }

                        // 9. Devices Tab (D07)
                        composable("devices") {
                            DetectedDevicesScreen(
                                onDeviceClick = { deviceId ->
                                    navController.navigate("device_detail/$deviceId")
                                },
                                onFilterClick = {}
                            )
                        }

                        // 10. Device Details (D05)
                        composable(
                            "device_detail/{deviceId}",
                            arguments = listOf(navArgument("deviceId") { type = NavType.StringType })
                        ) { backStackEntry ->
                            val deviceId = backStackEntry.arguments?.getString("deviceId") ?: ""
                            DeviceDetailScreen(
                                deviceId = deviceId,
                                onBackClick = { navController.popBackStack() },
                                onHelpClick = { navController.navigate("help_support") }
                            )
                        }

                        // 11. Rooms Tab (D06)
                        composable("rooms") {
                            RoomsScreen(
                                onRoomClick = { _, _ ->
                                    navController.navigate("scan_methods")
                                }
                            )
                        }

                        // 12. Settings Tab (D08)
                        composable("settings") {
                            SettingsScreen(
                                onScanHistoryClick = { navController.navigate("scan_history") },
                                onPrivacySecurityClick = { navController.navigate("vault_security") },
                                onNotificationsClick = { navController.navigate("notification_settings") },
                                onLanguageClick = { navController.navigate("language_select") },
                                onStorageClick = { navController.navigate("storage_management") },
                                onHelpClick = { navController.navigate("help_support") },
                                onAboutClick = { navController.navigate("about") }
                            )
                        }

                        // 13. Scanner Suite Screens
                        composable("scan_methods") {
                            ScanMethodsScreen(
                                onBackClick = { navController.popBackStack() },
                                onSelectWifi = { navController.navigate("wifi_scan") },
                                onSelectBluetooth = { navController.navigate("bluetooth_scan") },
                                onSelectMagnetic = { navController.navigate("magnetic_scan") },
                                onSelectOptical = { navController.navigate("camera_finder") },
                                onSelectThermal = { navController.navigate("thermal_view") },
                                onHelpClick = { navController.navigate("help_support") }
                            )
                        }

                        composable("wifi_scan") {
                            WifiScanScreen(
                                onBackClick = { navController.popBackStack() },
                                onViewAllDevices = { navController.navigate("devices") },
                                onHelpClick = { navController.navigate("help_support") }
                            )
                        }

                        composable("bluetooth_scan") {
                            BluetoothScanScreen(
                                onBackClick = { navController.popBackStack() },
                                onViewAllDevices = { navController.navigate("devices") },
                                onHelpClick = { navController.navigate("help_support") }
                            )
                        }

                        composable("magnetic_scan") {
                            MagneticScanScreen(
                                onBackClick = { navController.popBackStack() },
                                onHelpClick = { navController.navigate("help_support") }
                            )
                        }

                        composable("camera_finder") {
                            CameraFinderScreen(
                                onBackClick = { navController.popBackStack() },
                                onHelpClick = { navController.navigate("help_support") },
                                onPhotoSaved = { navController.navigate("vault_unlock") }
                            )
                        }

                        composable("thermal_view") {
                            ThermalSimulatorScreen(
                                onBackClick = { navController.popBackStack() },
                                onHelpClick = { navController.navigate("help_support") }
                            )
                        }

                        // 14. Intruder Guard Screens (D16)
                        composable("intruder_guard") {
                            IntruderGuardScreen(
                                onBackClick = { navController.popBackStack() },
                                onViewAllHistory = { navController.navigate("intruder_history") },
                                onEventClick = { eventId ->
                                    navController.navigate("intruder_event/$eventId")
                                },
                                onHelpClick = { navController.navigate("help_support") }
                            )
                        }

                        composable("intruder_history") {
                            IntruderHistoryScreen(
                                onBackClick = { navController.popBackStack() },
                                onEventClick = { eventId ->
                                    navController.navigate("intruder_event/$eventId")
                                }
                            )
                        }

                        composable(
                            "intruder_event/{eventId}",
                            arguments = listOf(navArgument("eventId") { type = NavType.StringType })
                        ) { backStackEntry ->
                            val eventId = backStackEntry.arguments?.getString("eventId") ?: ""
                            IntruderEventDetailScreen(
                                eventId = eventId,
                                onBackClick = { navController.popBackStack() }
                            )
                        }

                        // 15. Secure Video / Audio Recorder (D17)
                        composable("recorder") {
                            RecorderScreen(
                                onBackClick = { navController.popBackStack() },
                                onViewInVault = {
                                    navController.navigate("vault_unlock")
                                },
                                onHelpClick = { navController.navigate("help_support") }
                            )
                        }

                        // 16. Private Encrypted Vault Screens (D18, D19, D20)
                        composable("vault_unlock") {
                            VaultUnlockScreen(
                                onUnlockSuccess = {
                                    navController.navigate("vault_gallery") {
                                        popUpTo("vault_unlock") { inclusive = true }
                                    }
                                },
                                onBackClick = { navController.popBackStack() }
                            )
                        }

                        composable("vault_gallery") {
                            VaultGalleryScreen(
                                onMediaClick = { mediaId ->
                                    navController.navigate("vault_media/$mediaId")
                                },
                                onBackClick = { navController.navigate("home") },
                                onSecuritySettingsClick = { navController.navigate("vault_security") }
                            )
                        }

                        composable(
                            "vault_media/{mediaId}",
                            arguments = listOf(navArgument("mediaId") { type = NavType.StringType })
                        ) { backStackEntry ->
                            val mediaId = backStackEntry.arguments?.getString("mediaId") ?: ""
                            MediaViewerScreen(
                                mediaId = mediaId,
                                onBackClick = { navController.popBackStack() },
                                onDeleted = { navController.popBackStack() }
                            )
                        }

                        composable("vault_security") {
                            VaultSecurityScreen(
                                onBackClick = { navController.popBackStack() },
                                onLockVault = {
                                    navController.navigate("vault_unlock") {
                                        popUpTo("vault_gallery") { inclusive = true }
                                    }
                                }
                            )
                        }

                        // 17. Settings Sub-Screens
                        composable("scan_history") {
                            ScanHistoryScreen(
                                onBackClick = { navController.popBackStack() },
                                onStartScan = { navController.navigate("scan_methods") }
                            )
                        }

                        composable("notification_settings") {
                            NotificationSettingsScreen(
                                onBackClick = { navController.popBackStack() }
                            )
                        }

                        composable("storage_management") {
                            StorageManagementScreen(
                                onBackClick = { navController.popBackStack() },
                                onOpenVault = { navController.navigate("vault_unlock") }
                            )
                        }

                        composable("help_support") {
                            HelpSupportScreen(
                                onBackClick = { navController.popBackStack() }
                            )
                        }

                        composable("about") {
                            AboutScreen(
                                onBackClick = { navController.popBackStack() }
                            )
                        }
                    }
                }
            }
        }
    }
}
