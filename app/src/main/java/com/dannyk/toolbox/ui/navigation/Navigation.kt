package com.dannyk.toolbox.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.dannyk.toolbox.ui.screens.HomeScreen
import com.dannyk.toolbox.ui.screens.settings.SettingsScreen
// Calculator Tools
import com.dannyk.toolbox.ui.screens.tools.calculator.*
// Converter Tools
import com.dannyk.toolbox.ui.screens.tools.converters.*
// Math Tools
import com.dannyk.toolbox.ui.screens.tools.math.*
// Text Tools
import com.dannyk.toolbox.ui.screens.tools.text.*
// Developer Tools
import com.dannyk.toolbox.ui.screens.tools.developer.*
// Security Tools
import com.dannyk.toolbox.ui.screens.tools.security.*
// Internet Tools
import com.dannyk.toolbox.ui.screens.tools.internet.*
// Image & Color Tools
import com.dannyk.toolbox.ui.screens.tools.image.*
// File Tools
import com.dannyk.toolbox.ui.screens.tools.files.*
// Everyday Tools
import com.dannyk.toolbox.ui.screens.tools.everyday.*

@Composable
fun ToolBoxNavHost(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = "home"
    ) {
        composable("home") {
            HomeScreen(navController = navController)
        }
        
        // Calculator Tools (1-10)
        composable("tool/basic_calculator") { BasicCalculatorScreen(navController) }
        composable("tool/scientific_calculator") { ScientificCalculatorScreen(navController) }
        composable("tool/percentage_calculator") { PercentageCalculatorScreen(navController) }
        composable("tool/discount_calculator") { DiscountCalculatorScreen(navController) }
        composable("tool/tip_calculator") { TipCalculatorScreen(navController) }
        composable("tool/split_bill") { SplitBillScreen(navController) }
        composable("tool/age_calculator") { AgeCalculatorScreen(navController) }
        composable("tool/date_difference") { DateDifferenceScreen(navController) }
        composable("tool/time_difference") { TimeDifferenceScreen(navController) }
        composable("tool/bmi_calculator") { BMICalculatorScreen(navController) }
        
        // Converter Tools (11-20)
        composable("tool/length_converter") { LengthConverterScreen(navController) }
        composable("tool/weight_converter") { WeightConverterScreen(navController) }
        composable("tool/temperature_converter") { TemperatureConverterScreen(navController) }
        composable("tool/area_converter") { AreaConverterScreen(navController) }
        composable("tool/volume_converter") { VolumeConverterScreen(navController) }
        composable("tool/speed_converter") { SpeedConverterScreen(navController) }
        composable("tool/time_converter") { TimeConverterScreen(navController) }
        composable("tool/data_storage_converter") { DataStorageConverterScreen(navController) }
        composable("tool/number_system_converter") { NumberSystemConverterScreen(navController) }
        composable("tool/roman_numeral_converter") { RomanNumeralConverterScreen(navController) }
        
        // Math Tools (21-30)
        composable("tool/fraction_calculator") { FractionCalculatorScreen(navController) }
        composable("tool/ratio_calculator") { RatioCalculatorScreen(navController) }
        composable("tool/proportion_calculator") { ProportionCalculatorScreen(navController) }
        composable("tool/average_calculator") { AverageCalculatorScreen(navController) }
        composable("tool/gcd_lcm") { GCDLCMScreen(navController) }
        composable("tool/prime_checker") { PrimeCheckerScreen(navController) }
        composable("tool/factorial_calculator") { FactorialCalculatorScreen(navController) }
        composable("tool/power_calculator") { PowerCalculatorScreen(navController) }
        composable("tool/square_root_calculator") { SquareRootCalculatorScreen(navController) }
        composable("tool/random_number_generator") { RandomNumberGeneratorScreen(navController) }
        
        // Text Tools (31-40)
        composable("tool/word_counter") { WordCounterScreen(navController) }
        composable("tool/character_counter") { CharacterCounterScreen(navController) }
        composable("tool/sentence_counter") { SentenceCounterScreen(navController) }
        composable("tool/case_converter") { CaseConverterScreen(navController) }
        composable("tool/text_reverser") { TextReverserScreen(navController) }
        composable("tool/remove_duplicates") { RemoveDuplicatesScreen(navController) }
        composable("tool/sort_lines") { SortLinesScreen(navController) }
        composable("tool/find_replace") { FindReplaceScreen(navController) }
        composable("tool/text_diff") { TextDiffScreen(navController) }
        composable("tool/slug_generator") { SlugGeneratorScreen(navController) }
        
        // Developer Tools (41-50)
        composable("tool/json_formatter") { JSONFormatterScreen(navController) }
        composable("tool/json_validator") { JSONValidatorScreen(navController) }
        composable("tool/base64_encoder") { Base64EncoderScreen(navController) }
        composable("tool/base64_decoder") { Base64DecoderScreen(navController) }
        composable("tool/url_encoder") { URLEncoderScreen(navController) }
        composable("tool/url_decoder") { URLDecoderScreen(navController) }
        composable("tool/html_entity_encoder") { HTMLEntityEncoderScreen(navController) }
        composable("tool/html_entity_decoder") { HTMLEntityDecoderScreen(navController) }
        composable("tool/uuid_generator") { UUIDGeneratorScreen(navController) }
        composable("tool/regex_tester") { RegexTesterScreen(navController) }
        
        // Security Tools (51-60)
        composable("tool/password_generator") { PasswordGeneratorScreen(navController) }
        composable("tool/password_strength") { PasswordStrengthScreen(navController) }
        composable("tool/sha256_hash") { SHA256HashScreen(navController) }
        composable("tool/sha1_hash") { SHA1HashScreen(navController) }
        composable("tool/md5_hash") { MD5HashScreen(navController) }
        composable("tool/hmac_generator") { HMACGeneratorScreen(navController) }
        composable("tool/token_generator") { TokenGeneratorScreen(navController) }
        composable("tool/pin_generator") { PINGeneratorScreen(navController) }
        composable("tool/passphrase_generator") { PassphraseGeneratorScreen(navController) }
        composable("tool/hash_identifier") { HashIdentifierScreen(navController) }
        
        // Internet Tools (61-70)
        composable("tool/my_ip_address") { MyIPAddressScreen(navController) }
        composable("tool/dns_lookup") { DNSLookupScreen(navController) }
        composable("tool/ping_tester") { PingTesterScreen(navController) }
        composable("tool/http_status") { HTTPStatusScreen(navController) }
        composable("tool/user_agent_viewer") { UserAgentViewerScreen(navController) }
        composable("tool/url_parser") { URLParserScreen(navController) }
        composable("tool/qr_code_generator") { QRCodeGeneratorScreen(navController) }
        composable("tool/qr_code_scanner") { QRCodeScannerScreen(navController) }
        composable("tool/wifi_qr_generator") { WiFiQRGeneratorScreen(navController) }
        composable("tool/barcode_scanner") { BarcodeScannerScreen(navController) }
        
        // Image & Color Tools (71-80)
        composable("tool/image_compressor") { ImageCompressorScreen(navController) }
        composable("tool/image_resizer") { ImageResizerScreen(navController) }
        composable("tool/image_cropper") { ImageCropperScreen(navController) }
        composable("tool/image_format_converter") { ImageFormatConverterScreen(navController) }
        composable("tool/color_picker") { ColorPickerScreen(navController) }
        composable("tool/hex_rgb_converter") { HexRGBConverterScreen(navController) }
        composable("tool/rgb_hsl_converter") { RGBHSLConverterScreen(navController) }
        composable("tool/palette_generator") { PaletteGeneratorScreen(navController) }
        composable("tool/contrast_checker") { ContrastCheckerScreen(navController) }
        composable("tool/metadata_viewer") { MetadataViewerScreen(navController) }
        
        // File Tools (81-90)
        composable("tool/image_to_pdf") { ImageToPDFScreen(navController) }
        composable("tool/pdf_to_images") { PDFToImagesScreen(navController) }
        composable("tool/file_size_converter") { FileSizeConverterScreen(navController) }
        composable("tool/file_hash_checker") { FileHashCheckerScreen(navController) }
        composable("tool/text_to_pdf") { TextToPDFScreen(navController) }
        composable("tool/text_to_qr") { TextToQRScreen(navController) }
        composable("tool/qr_to_text") { QRToTextScreen(navController) }
        composable("tool/file_extension_info") { FileExtensionInfoScreen(navController) }
        composable("tool/mime_type_lookup") { MimeTypeLookupScreen(navController) }
        composable("tool/filename_generator") { FilenameGeneratorScreen(navController) }
        
        // Everyday Tools (91-100)
        composable("tool/stopwatch") { StopwatchScreen(navController) }
        composable("tool/countdown_timer") { CountdownTimerScreen(navController) }
        composable("tool/pomodoro_timer") { PomodoroTimerScreen(navController) }
        composable("tool/world_clock") { WorldClockScreen(navController) }
        composable("tool/metronome") { MetronomeScreen(navController) }
        composable("tool/coin_flip") { CoinFlipScreen(navController) }
        composable("tool/dice_roller") { DiceRollerScreen(navController) }
        composable("tool/random_choice") { RandomChoiceScreen(navController) }
        composable("tool/habit_counter") { HabitCounterScreen(navController) }
        composable("tool/notes") { NotesScreen(navController) }
        
        // Settings
        composable("settings") {
            SettingsScreen(navController = navController)
        }
    }
}
