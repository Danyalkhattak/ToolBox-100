package com.dannyk.toolbox.tools

import com.dannyk.toolbox.domain.model.Category
import com.dannyk.toolbox.domain.model.Tool
import androidx.compose.ui.graphics.Color
import androidx.compose.material.icons.filled.Check

object ToolRegistry {
    
    val allTools: List<Tool> = listOf(
        // Calculators (1-10)
        Tool(
            id = 1,
            name = "Basic Calculator",
            description = "Perform basic arithmetic operations",
            category = Category.CALCULATORS,
            route = "tool/basic_calculator",
            iconResName = "calculate"
        ),
        Tool(
            id = 2,
            name = "Scientific Calculator",
            description = "Advanced scientific calculations with trigonometry, logarithms, and more",
            category = Category.CALCULATORS,
            route = "tool/scientific_calculator",
            iconResName = "science"
        ),
        Tool(
            id = 3,
            name = "Percentage Calculator",
            description = "Calculate percentages, increases, decreases, and ratios",
            category = Category.CALCULATORS,
            route = "tool/percentage_calculator",
            iconResName = "percent"
        ),
        Tool(
            id = 4,
            name = "Discount Calculator",
            description = "Calculate discounted prices and savings",
            category = Category.CALCULATORS,
            route = "tool/discount_calculator",
            iconResName = "local_offer"
        ),
        Tool(
            id = 5,
            name = "Tip Calculator",
            description = "Calculate tips and split bills easily",
            category = Category.CALCULATORS,
            route = "tool/tip_calculator",
            iconResName = "restaurant"
        ),
        Tool(
            id = 6,
            name = "Split Bill Calculator",
            description = "Split bills among multiple people fairly",
            category = Category.CALCULATORS,
            route = "tool/split_bill",
            iconResName = "group"
        ),
        Tool(
            id = 7,
            name = "Age Calculator",
            description = "Calculate exact age from date of birth",
            category = Category.CALCULATORS,
            route = "tool/age_calculator",
            iconResName = "cake"
        ),
        Tool(
            id = 8,
            name = "Date Difference Calculator",
            description = "Calculate the difference between two dates",
            category = Category.CALCULATORS,
            route = "tool/date_difference",
            iconResName = "event"
        ),
        Tool(
            id = 9,
            name = "Time Difference Calculator",
            description = "Calculate time differences in various units",
            category = Category.CALCULATORS,
            route = "tool/time_difference",
            iconResName = "schedule"
        ),
        Tool(
            id = 10,
            name = "BMI Calculator",
            description = "Calculate Body Mass Index and health category",
            category = Category.CALCULATORS,
            route = "tool/bmi_calculator",
            iconResName = "monitor_weight"
        ),
        
        // Converters (11-20)
        Tool(
            id = 11,
            name = "Length Converter",
            description = "Convert between length units: meters, feet, inches, miles, etc.",
            category = Category.CONVERTERS,
            route = "tool/length_converter",
            iconResName = "straighten"
        ),
        Tool(
            id = 12,
            name = "Weight Converter",
            description = "Convert between weight units: kg, lbs, oz, grams, etc.",
            category = Category.CONVERTERS,
            route = "tool/weight_converter",
            iconResName = "fitness_center"
        ),
        Tool(
            id = 13,
            name = "Temperature Converter",
            description = "Convert between Celsius, Fahrenheit, and Kelvin",
            category = Category.CONVERTERS,
            route = "tool/temperature_converter",
            iconResName = "thermostat"
        ),
        Tool(
            id = 14,
            name = "Area Converter",
            description = "Convert between area units: sq meters, acres, hectares, etc.",
            category = Category.CONVERTERS,
            route = "tool/area_converter",
            iconResName = "crop_square"
        ),
        Tool(
            id = 15,
            name = "Volume Converter",
            description = "Convert between volume units: liters, gallons, cups, etc.",
            category = Category.CONVERTERS,
            route = "tool/volume_converter",
            iconResName = "water_drop"
        ),
        Tool(
            id = 16,
            name = "Speed Converter",
            description = "Convert between speed units: mph, km/h, m/s, knots, etc.",
            category = Category.CONVERTERS,
            route = "tool/speed_converter",
            iconResName = "speed"
        ),
        Tool(
            id = 17,
            name = "Time Converter",
            description = "Convert between time units: seconds, minutes, hours, days, etc.",
            category = Category.CONVERTERS,
            route = "tool/time_converter",
            iconResName = "timer"
        ),
        Tool(
            id = 18,
            name = "Data Storage Converter",
            description = "Convert between digital storage units: bytes, KB, MB, GB, TB",
            category = Category.CONVERTERS,
            route = "tool/data_storage_converter",
            iconResName = "storage"
        ),
        Tool(
            id = 19,
            name = "Number System Converter",
            description = "Convert between binary, octal, decimal, and hexadecimal",
            category = Category.CONVERTERS,
            route = "tool/number_system_converter",
            iconResName = "binary"
        ),
        Tool(
            id = 20,
            name = "Roman Numeral Converter",
            description = "Convert between numbers and Roman numerals",
            category = Category.CONVERTERS,
            route = "tool/roman_numeral_converter",
            iconResName = "looks_one"
        ),
        
        // Math (21-30)
        Tool(
            id = 21,
            name = "Fraction Calculator",
            description = "Add, subtract, multiply, and divide fractions",
            category = Category.MATH,
            route = "tool/fraction_calculator",
            iconResName = "fraction"
        ),
        Tool(
            id = 22,
            name = "Ratio Calculator",
            description = "Simplify and calculate ratios",
            category = Category.MATH,
            route = "tool/ratio_calculator",
            iconResName = "compare_arrows"
        ),
        Tool(
            id = 23,
            name = "Proportion Calculator",
            description = "Solve proportion problems",
            category = Category.MATH,
            route = "tool/proportion_calculator",
            iconResName = "tune"
        ),
        Tool(
            id = 24,
            name = "Average Calculator",
            description = "Calculate mean, median, mode, and range",
            category = Category.MATH,
            route = "tool/average_calculator",
            iconResName = "analytics"
        ),
        Tool(
            id = 25,
            name = "GCD & LCM",
            description = "Find Greatest Common Divisor and Least Common Multiple",
            category = Category.MATH,
            route = "tool/gcd_lcm",
            iconResName = "functions"
        ),
        Tool(
            id = 26,
            name = "Prime Number Checker",
            description = "Check if a number is prime and find prime factors",
            category = Category.MATH,
            route = "tool/prime_checker",
            iconResName = "verified"
        ),
        Tool(
            id = 27,
            name = "Factorial Calculator",
            description = "Calculate factorial of any number",
            category = Category.MATH,
            route = "tool/factorial_calculator",
            iconResName = "exclamation"
        ),
        Tool(
            id = 28,
            name = "Power Calculator",
            description = "Calculate powers and exponents",
            category = Category.MATH,
            route = "tool/power_calculator",
            iconResName = "power"
        ),
        Tool(
            id = 29,
            name = "Square Root Calculator",
            description = "Calculate square roots and nth roots",
            category = Category.MATH,
            route = "tool/square_root_calculator",
            iconResName = "square_root"
        ),
        Tool(
            id = 30,
            name = "Random Number Generator",
            description = "Generate random numbers within a range",
            category = Category.MATH,
            route = "tool/random_number_generator",
            iconResName = "casino"
        ),
        
        // Text (31-40)
        Tool(
            id = 31,
            name = "Word Counter",
            description = "Count words, characters, sentences, and paragraphs",
            category = Category.TEXT,
            route = "tool/word_counter",
            iconResName = "format_list_numbered"
        ),
        Tool(
            id = 32,
            name = "Character Counter",
            description = "Count characters with and without spaces",
            category = Category.TEXT,
            route = "tool/character_counter",
            iconResName = "text_fields"
        ),
        Tool(
            id = 33,
            name = "Sentence Counter",
            description = "Count sentences and analyze text structure",
            category = Category.TEXT,
            route = "tool/sentence_counter",
            iconResName="subject"
        ),
        Tool(
            id = 34,
            name = "Case Converter",
            description = "Convert text to uppercase, lowercase, title case, and more",
            category = Category.TEXT,
            route = "tool/case_converter",
            iconResName = "format_case"
        ),
        Tool(
            id = 35,
            name = "Text Reverser",
            description = "Reverse text, words, or lines",
            category = Category.TEXT,
            route = "tool/text_reverser",
            iconResName = "undo"
        ),
        Tool(
            id = 36,
            name = "Remove Duplicate Lines",
            description = "Remove duplicate lines from text",
            category = Category.TEXT,
            route = "tool/remove_duplicates",
            iconResName = "content_copy_off"
        ),
        Tool(
            id = 37,
            name = "Sort Lines",
            description = "Sort lines alphabetically or numerically",
            category = Category.TEXT,
            route = "tool/sort_lines",
            iconResName = "sort_by_alpha"
        ),
        Tool(
            id = 38,
            name = "Find & Replace",
            description = "Find and replace text with options",
            category = Category.TEXT,
            route = "tool/find_replace",
            iconResName = "find_replace"
        ),
        Tool(
            id = 39,
            name = "Text Diff",
            description = "Compare two texts and show differences",
            category = Category.TEXT,
            route = "tool/text_diff",
            iconResName = "diff"
        ),
        Tool(
            id = 40,
            name = "Slug Generator",
            description = "Generate URL-friendly slugs from text",
            category = Category.TEXT,
            route = "tool/slug_generator",
            iconResName = "link"
        ),
        
        // Developer (41-50)
        Tool(
            id = 41,
            name = "JSON Formatter",
            description = "Format and beautify JSON code",
            category = Category.DEVELOPER,
            route = "tool/json_formatter",
            iconResName = "data_object"
        ),
        Tool(
            id = 42,
            name = "JSON Validator",
            description = "Validate JSON syntax and find errors",
            category = Category.DEVELOPER,
            route = "tool/json_validator",
            iconResName = "check_circle"
        ),
        Tool(
            id = 43,
            name = "Base64 Encoder",
            description = "Encode text to Base64 format",
            category = Category.DEVELOPER,
            route = "tool/base64_encoder",
            iconResName = "lock"
        ),
        Tool(
            id = 44,
            name = "Base64 Decoder",
            description = "Decode Base64 encoded text",
            category = Category.DEVELOPER,
            route = "tool/base64_decoder",
            iconResName = "lock_open"
        ),
        Tool(
            id = 45,
            name = "URL Encoder",
            description = "Encode special characters in URLs",
            category = Category.DEVELOPER,
            route = "tool/url_encoder",
            iconResName = "link"
        ),
        Tool(
            id = 46,
            name = "URL Decoder",
            description = "Decode URL-encoded strings",
            category = Category.DEVELOPER,
            route = "tool/url_decoder",
            iconResName = "link_off"
        ),
        Tool(
            id = 47,
            name = "HTML Entity Encoder",
            description = "Encode HTML entities",
            category = Category.DEVELOPER,
            route = "tool/html_entity_encoder",
            iconResName = "code"
        ),
        Tool(
            id = 48,
            name = "HTML Entity Decoder",
            description = "Decode HTML entities",
            category = Category.DEVELOPER,
            route = "tool/html_entity_decoder",
            iconResName = "html"
        ),
        Tool(
            id = 49,
            name = "UUID Generator",
            description = "Generate unique UUIDs/GUIDs",
            category = Category.DEVELOPER,
            route = "tool/uuid_generator",
            iconResName = "fingerprint"
        ),
        Tool(
            id = 50,
            name = "Regex Tester",
            description = "Test regular expressions with live results",
            category = Category.DEVELOPER,
            route = "tool/regex_tester",
            iconResName = "regular_expression"
        ),
        
        // Security (51-60)
        Tool(
            id = 51,
            name = "Password Generator",
            description = "Generate strong, secure passwords",
            category = Category.SECURITY,
            route = "tool/password_generator",
            iconResName = "password"
        ),
        Tool(
            id = 52,
            name = "Password Strength Checker",
            description = "Check password strength and get suggestions",
            category = Category.SECURITY,
            route = "tool/password_strength",
            iconResName = "security_update"
        ),
        Tool(
            id = 53,
            name = "SHA-256 Hash",
            description = "Generate SHA-256 hash of text",
            category = Category.SECURITY,
            route = "tool/sha256_hash",
            iconResName = "enhanced_encryption"
        ),
        Tool(
            id = 54,
            name = "SHA-1 Hash",
            description = "Generate SHA-1 hash of text",
            category = Category.SECURITY,
            route = "tool/sha1_hash",
            iconResName = "vpn_key"
        ),
        Tool(
            id = 55,
            name = "MD5 Hash",
            description = "Generate MD5 hash of text",
            category = Category.SECURITY,
            route = "tool/md5_hash",
            iconResName = "key"
        ),
        Tool(
            id = 56,
            name = "HMAC Generator",
            description = "Generate HMAC signatures",
            category = Category.SECURITY,
            route = "tool/hmac_generator",
            iconResName = "verified_user"
        ),
        Tool(
            id = 57,
            name = "Random Token Generator",
            description = "Generate random tokens for API keys, sessions, etc.",
            category = Category.SECURITY,
            route = "tool/token_generator",
            iconResName = "token"
        ),
        Tool(
            id = 58,
            name = "PIN Generator",
            description = "Generate random PIN codes",
            category = Category.SECURITY,
            route = "tool/pin_generator",
            iconResName = "dialpad"
        ),
        Tool(
            id = 59,
            name = "Passphrase Generator",
            description = "Generate memorable passphrases",
            category = Category.SECURITY,
            route = "tool/passphrase_generator",
            iconResName = "chat"
        ),
        Tool(
            id = 60,
            name = "Hash Identifier",
            description = "Identify unknown hash types",
            category = Category.SECURITY,
            route = "tool/hash_identifier",
            iconResName = "help"
        ),
        
        // Internet (61-70)
        Tool(
            id = 61,
            name = "My IP Address",
            description = "Display your public IP address information",
            category = Category.INTERNET,
            route = "tool/my_ip_address",
            iconResName = "ip"
        ),
        Tool(
            id = 62,
            name = "DNS Lookup",
            description = "Look up DNS records for a domain",
            category = Category.INTERNET,
            route = "tool/dns_lookup",
            iconResName = "dns"
        ),
        Tool(
            id = 63,
            name = "Ping Tester",
            description = "Test network connectivity to a host",
            category = Category.INTERNET,
            route = "tool/ping_tester",
            iconResName = "network_check"
        ),
        Tool(
            id = 64,
            name = "HTTP Status Checker",
            description = "Check HTTP status codes of URLs",
            category = Category.INTERNET,
            route = "tool/http_status",
            iconResName = "http"
        ),
        Tool(
            id = 65,
            name = "User-Agent Viewer",
            description = "View and analyze User-Agent strings",
            category = Category.INTERNET,
            route = "tool/user_agent_viewer",
            iconResName = "smartphone"
        ),
        Tool(
            id = 66,
            name = "URL Parser",
            description = "Parse and analyze URL components",
            category = Category.INTERNET,
            route = "tool/url_parser",
            iconResName = "link"
        ),
        Tool(
            id = 67,
            name = "QR Code Generator",
            description = "Generate QR codes from text or URLs",
            category = Category.INTERNET,
            route = "tool/qr_code_generator",
            iconResName = "qr_code"
        ),
        Tool(
            id = 68,
            name = "QR Code Scanner",
            description = "Scan QR codes using camera",
            category = Category.INTERNET,
            route = "tool/qr_code_scanner",
            iconResName = "qr_code_scanner"
        ),
        Tool(
            id = 69,
            name = "Wi-Fi QR Generator",
            description = "Generate Wi-Fi QR codes for easy sharing",
            category = Category.INTERNET,
            route = "tool/wifi_qr_generator",
            iconResName = "wifi"
        ),
        Tool(
            id = 70,
            name = "Barcode Scanner",
            description = "Scan barcodes using camera",
            category = Category.INTERNET,
            route = "tool/barcode_scanner",
            iconResName = "barcode"
        ),
        
        // Images & Colors (71-80)
        Tool(
            id = 71,
            name = "Image Compressor",
            description = "Compress images while maintaining quality",
            category = Category.IMAGE_COLOR,
            route = "tool/image_compressor",
            iconResName = "compress"
        ),
        Tool(
            id = 72,
            name = "Image Resizer",
            description = "Resize images to specific dimensions",
            category = Category.IMAGE_COLOR,
            route = "tool/image_resizer",
            iconResName = "crop"
        ),
        Tool(
            id = 73,
            name = "Image Cropper",
            description = "Crop images to desired area",
            category = Category.IMAGE_COLOR,
            route = "tool/image_cropper",
            iconResName = "crop_rotate"
        ),
        Tool(
            id = 74,
            name = "Image Format Converter",
            description = "Convert image formats (PNG, JPG, WebP)",
            category = Category.IMAGE_COLOR,
            route = "tool/image_format_converter",
            iconResName = "image"
        ),
        Tool(
            id = 75,
            name = "Color Picker",
            description = "Pick colors from screen or palette",
            category = Category.IMAGE_COLOR,
            route = "tool/color_picker",
            iconResName = "colorize"
        ),
        Tool(
            id = 76,
            name = "HEX to RGB Converter",
            description = "Convert between HEX and RGB color formats",
            category = Category.IMAGE_COLOR,
            route = "tool/hex_rgb_converter",
            iconResName = "palette"
        ),
        Tool(
            id = 77,
            name = "RGB to HSL Converter",
            description = "Convert between RGB and HSL color formats",
            category = Category.IMAGE_COLOR,
            route = "tool/rgb_hsl_converter",
            iconResName = "gradient"
        ),
        Tool(
            id = 78,
            name = "Color Palette Generator",
            description = "Generate harmonious color palettes",
            category = Category.IMAGE_COLOR,
            route = "tool/palette_generator",
            iconResName = "color_lens"
        ),
        Tool(
            id = 79,
            name = "Contrast Checker",
            description = "Check color contrast for accessibility",
            category = Category.IMAGE_COLOR,
            route = "tool/contrast_checker",
            iconResName = "contrast"
        ),
        Tool(
            id = 80,
            name = "Image Metadata Viewer",
            description = "View EXIF metadata from images",
            category = Category.IMAGE_COLOR,
            route = "tool/metadata_viewer",
            iconResName = "info"
        ),
        
        // Files (81-90)
        Tool(
            id = 81,
            name = "Image to PDF",
            description = "Convert images to PDF documents",
            category = Category.FILES,
            route = "tool/image_to_pdf",
            iconResName = "picture_as_pdf"
        ),
        Tool(
            id = 82,
            name = "PDF to Images",
            description = "Extract images from PDF files",
            category = Category.FILES,
            route = "tool/pdf_to_images",
            iconResName = "pdf"
        ),
        Tool(
            id = 83,
            name = "File Size Converter",
            description = "Convert file sizes between units",
            category = Category.FILES,
            route = "tool/file_size_converter",
            iconResName = "storage"
        ),
        Tool(
            id = 84,
            name = "File Hash Checker",
            description = "Verify file integrity with hashes",
            category = Category.FILES,
            route = "tool/file_hash_checker",
            iconResName = "verified"
        ),
        Tool(
            id = 85,
            name = "Text to PDF",
            description = "Convert text content to PDF",
            category = Category.FILES,
            route = "tool/text_to_pdf",
            iconResName = "description"
        ),
        Tool(
            id = 86,
            name = "Text to QR Code",
            description = "Generate QR code from text",
            category = Category.FILES,
            route = "tool/text_to_qr",
            iconResName = "qr_code_2"
        ),
        Tool(
            id = 87,
            name = "QR Code to Text",
            description = "Extract text from QR code image",
            category = Category.FILES,
            route = "tool/qr_to_text",
            iconResName = "qr_code"
        ),
        Tool(
            id = 88,
            name = "File Extension Info",
            description = "Look up file extension details",
            category = Category.FILES,
            route = "tool/file_extension_info",
            iconResName = "insert_drive_file"
        ),
        Tool(
            id = 89,
            name = "MIME Type Lookup",
            description = "Find MIME types for file extensions",
            category = Category.FILES,
            route = "tool/mime_type_lookup",
            iconResName = "web"
        ),
        Tool(
            id = 90,
            name = "File Name Generator",
            description = "Generate organized file names",
            category = Category.FILES,
            route = "tool/filename_generator",
            iconResName = "edit_note"
        ),
        
        // Everyday (91-100)
        Tool(
            id = 91,
            name = "Stopwatch",
            description = "Precise stopwatch with lap times",
            category = Category.EVERYDAY,
            route = "tool/stopwatch",
            iconResName = "timer"
        ),
        Tool(
            id = 92,
            name = "Countdown Timer",
            description = "Set countdown timers with notifications",
            category = Category.EVERYDAY,
            route = "tool/countdown_timer",
            iconResName = "alarm"
        ),
        Tool(
            id = 93,
            name = "Pomodoro Timer",
            description = "Focus timer based on Pomodoro technique",
            category = Category.EVERYDAY,
            route = "tool/pomodoro_timer",
            iconResName = "hourglass_top"
        ),
        Tool(
            id = 94,
            name = "World Clock",
            description = "View current time across different time zones",
            category = Category.EVERYDAY,
            route = "tool/world_clock",
            iconResName = "public"
        ),
        Tool(
            id = 95,
            name = "Metronome",
            description = "Keep tempo with adjustable BPM",
            category = Category.EVERYDAY,
            route = "tool/metronome",
            iconResName = "music_note"
        ),
        Tool(
            id = 96,
            name = "Coin Flip",
            description = "Virtual coin flip simulator",
            category = Category.EVERYDAY,
            route = "tool/coin_flip",
            iconResName = "currency_exchange"
        ),
        Tool(
            id = 97,
            name = "Dice Roller",
            description = "Roll dice with customizable sides",
            category = Category.EVERYDAY,
            route = "tool/dice_roller",
            iconResName = "casino"
        ),
        Tool(
            id = 98,
            name = "Random Choice Picker",
            description = "Make random choices from a list",
            category = Category.EVERYDAY,
            route = "tool/random_choice",
            iconResName = "shuffle"
        ),
        Tool(
            id = 99,
            name = "Habit Counter",
            description = "Track daily habits and streaks",
            category = Category.EVERYDAY,
            route = "tool/habit_counter",
            iconResName = "track_changes"
        ),
        Tool(
            id = 100,
            name = "Notes",
            description = "Quick notes and memos",
            category = Category.EVERYDAY,
            route = "tool/notes",
            iconResName = "note"
        )
    )
    
    fun getToolById(id: Int): Tool? = allTools.find { it.id == id }
    
    fun getToolsByCategory(category: Category): List<Tool> = 
        allTools.filter { it.category == category }
    
    fun searchTools(query: String): List<Tool> {
        val lowerQuery = query.lowercase()
        return allTools.filter { tool ->
            tool.name.lowercase().contains(lowerQuery) ||
            tool.description.lowercase().contains(lowerQuery) ||
            tool.category.displayName.lowercase().contains(lowerQuery) ||
            tool.category.name.lowercase().contains(lowerQuery)
        }
    }
}
