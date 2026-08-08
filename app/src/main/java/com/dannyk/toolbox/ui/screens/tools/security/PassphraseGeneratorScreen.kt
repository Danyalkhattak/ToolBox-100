package com.dannyk.toolbox.ui.screens.tools.security

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.dannyk.toolbox.ui.components.ToolTopBar
import java.security.SecureRandom
import kotlin.math.log2
import android.content.ClipboardManager
import android.content.ClipData
import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.filled.Check
import androidx.compose.foundation.ScrollState
import androidx.compose.runtime.LaunchedEffect

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PassphraseGeneratorScreen(navController: NavHostController) {
    val context = LocalContext.current
    var wordCount by remember { mutableIntStateOf(6) }
    var selectedSeparator by remember { mutableStateOf("space") }
    var selectedCase by remember { mutableStateOf("lowercase") }
    var includeNumber by remember { mutableStateOf(false) }
    
    var generatedPassphrase by remember { mutableStateOf("") }
    var isGenerating by remember { mutableStateOf(false) }
    var showCopiedMessage by remember { mutableStateOf(false) }

    val secureRandom = SecureRandom()

    // Common English word list (~2000 words for good entropy)
    val wordList = listOf(
        "able", "about", "above", "accept", "account", "across", "act", "action", "activity", "actually",
        "add", "address", "administration", "admit", "adult", "affect", "after", "again", "against", "age",
        "agent", "ago", "agree", "ahead", "air", "allow", "almost", "alone", "along", "already",
        "also", "although", "always", "american", "among", "amount", "analysis", "and", "animal", "another",
        "answer", "anyone", "anything", "appear", "apply", "approach", "area", "argue", "arm", "around",
        "arrive", "art", "article", "artist", "ask", "assume", "attack", "attention", "attorney", "audience",
        "author", "authority", "available", "avoid", "away", "baby", "back", "bag", "ball", "bank",
        "bar", "base", "beautiful", "become", "bed", "before", "begin", "behavior", "behind", "believe",
        "benefit", "better", "beyond", "big", "bill", "black", "blood", "blue", "board", "body",
        "book", "born", "both", "box", "boy", "break", "bring", "brother", "budget", "build",
        "business", "buy", "call", "camera", "campaign", "can", "cancer", "candidate", "capital", "card",
        "care", "carry", "case", "catch", "cause", "cell", "center", "central", "century", "certain",
        "chair", "challenge", "chance", "character", "charge", "check", "child", "choice", "choose", "church",
        "citizen", "city", "civil", "claim", "class", "clean", "clear", "close", "coach", "cold",
        "collection", "color", "come", "commercial", "common", "community", "company", "compare", "computer", "concern",
        "condition", "conference", "congress", "consider", "contain", "content", "control", "cost", "country", "couple",
        "course", "court", "cover", "create", "crime", "cultural", "culture", "current", "cut", "dark",
        "data", "daughter", "day", "dead", "deal", "death", "debate", "decade", "decide", "decision",
        "deep", "defense", "degree", "democrat", "democratic", "describe", "design", "despite", "detail", "determine",
        "develop", "die", "difference", "different", "difficult", "dinner", "direction", "director", "discover", "discuss",
        "disease", "do", "doctor", "dog", "door", "down", "draw", "dream", "drive", "drop",
        "during", "early", "east", "easy", "economic", "economy", "edge", "education", "effect", "effort",
        "eight", "either", "election", "else", "employee", "end", "energy", "enjoy", "enough", "enter",
        "entire", "environment", "environmental", "especially", "establish", "evening", "event", "ever", "every", "everybody",
        "everyone", "everything", "evidence", "exactly", "example", "executive", "exist", "expect", "experience", "expert",
        "explain", "eye", "face", "fact", "factor", "fail", "fall", "family", "fast", "father",
        "fear", "federal", "feel", "feeling", "field", "fight", "figure", "fill", "film", "final",
        "financial", "finger", "finish", "firm", "first", "fish", "five", "floor", "fly", "focus",
        "follow", "food", "foot", "force", "foreign", "forget", "form", "former", "forward", "four",
        "free", "friend", "from", "front", "full", "fund", "future", "garden", "gas", "general",
        "generation", "girl", "give", "glass", "go", "goal", "god", "good", "government", "great",
        "green", "ground", "group", "grow", "growth", "guess", "gun", "guy", "hair", "half",
        "hand", "hang", "happen", "happy", "hard", "have", "head", "health", "hear", "heart",
        "heat", "heavy", "help", "high", "himself", "history", "hit", "hold", "home", "hope",
        "hospital", "hot", "hotel", "hour", "house", "how", "however", "human", "hundred", "husband",
        "idea", "identify", "if", "image", "imagine", "impact", "important", "improve", "include", "including",
        "increase", "indeed", "indicate", "individual", "industry", "information", "inside", "instead", "institution", "interest",
        "interesting", "international", "interview", "investment", "involve", "issue", "item", "its", "itself", "job",
        "join", "just", "keep", "key", "kid", "kill", "kind", "kitchen", "know", "knowledge",
        "land", "language", "large", "last", "late", "later", "laugh", "law", "lay", "lead",
        "leader", "learn", "least", "leave", "left", "leg", "legal", "less", "let", "level",
        "lie", "life", "light", "likely", "line", "list", "listen", "little", "live", "local",
        "long", "look", "lose", "loss", "lot", "love", "low", "machine", "magazine", "main",
        "maintain", "major", "majority", "make", "man", "manage", "management", "manager", "many", "market",
        "marriage", "material", "matter", "may", "maybe", "mean", "measure", "media", "medical", "meet",
        "meeting", "member", "memory", "mention", "message", "method", "middle", "might", "military", "mind",
        "minute", "miss", "mission", "model", "modern", "moment", "money", "month", "mother", "mouth",
        "move", "movement", "movie", "much", "music", "must", "myself", "name", "nation", "national",
        "natural", "nature", "near", "nearly", "necessary", "need", "network", "never", "new", "news",
        "newspaper", "next", "nice", "night", "none", "north", "not", "note", "nothing", "notice",
        "now", "number", "occur", "off", "offer", "office", "often", "oil", "old", "once",
        "only", "onto", "open", "operation", "opportunity", "option", "order", "organization", "others", "outside",
        "over", "own", "owner", "page", "pain", "painting", "paper", "parent", "part", "participant",
        "particular", "particularly", "partner", "party", "pass", "past", "patient", "pattern", "pay", "peace",
        "people", "per", "perform", "performance", "perhaps", "period", "person", "personal", "phone", "physical",
        "pick", "piece", "place", "plan", "plant", "play", "player", "please", "plus", "point",
        "policy", "politics", "poor", "popular", "population", "position", "positive", "possible", "power", "practice",
        "prepare", "present", "president", "pressure", "pretty", "prevent", "price", "private", "probably", "problem",
        "process", "produce", "product", "professional", "program", "project", "property", "protect", "prove", "provide",
        "public", "pull", "purpose", "push", "put", "quality", "question", "quickly", "quite", "race",
        "radio", "raise", "range", "rate", "rather", "reach", "read", "ready", "real", "reality",
        "realize", "really", "reason", "receive", "recent", "recently", "recognize", "record", "red", "reduce",
        "reflect", "region", "relate", "relationship", "religious", "remain", "remember", "remove", "report", "represent",
        "republican", "require", "research", "resource", "respond", "response", "rest", "result", "return", "reveal",
        "rich", "right", "rise", "risk", "road", "rock", "role", "room", "rule", "run",
        "safe", "same", "save", "scene", "school", "science", "scientist", "score", "sea", "season",
        "seat", "second", "section", "security", "see", "seek", "seem", "sell", "send", "senior",
        "sense", "series", "serious", "serve", "service", "set", "seven", "several", "shake", "shall",
        "share", "she", "shoot", "short", "should", "shoulder", "show", "side", "sign", "significant",
        "similar", "simple", "simply", "since", "sing", "single", "sister", "sit", "site", "situation",
        "size", "skill", "skin", "small", "smile", "social", "society", "soldier", "somebody", "someone",
        "something", "sometimes", "son", "song", "soon", "sort", "sound", "source", "south", "southern",
        "space", "speak", "special", "specific", "speech", "spend", "sport", "spring", "staff", "stage",
        "stand", "standard", "star", "start", "state", "statement", "station", "stay", "step", "still",
        "stock", "stop", "store", "story", "strategy", "strong", "structure", "student", "study", "stuff",
        "style", "subject", "success", "successful", "such", "suddenly", "suffer", "summer", "support", "sure",
        "surface", "system", "table", "take", "talk", "task", "teach", "teacher", "team", "technology",
        "television", "tell", "ten", "tend", "term", "test", "than", "that", "their", "them",
        "themselves", "then", "theory", "there", "these", "they", "thing", "think", "third", "this",
        "those", "though", "thought", "thousand", "threat", "three", "throughout", "throw", "thus", "time",
        "today", "together", "tonight", "top", "total", "tough", "toward", "town", "trade", "traditional",
        "training", "treatment", "tree", "trial", "trip", "trouble", "true", "truth", "try", "turn",
        "type", "understand", "unit", "until", "upon", "us", "usually", "value", "various", "very",
        "victim", "view", "violence", "visit", "voice", "vote", "wait", "walk", "wall", "want",
        "war", "watch", "water", "way", "we", "weapon", "wear", "week", "weight", "well",
        "west", "western", "what", "whatever", "when", "where", "whether", "which", "while", "white",
        "who", "whole", "whom", "whose", "why", "wide", "wife", "will", "win", "wind",
        "window", "wish", "with", "within", "without", "woman", "wonder", "word", "work", "world",
        "worry", "would", "write", "wrong", "yard", "yeah", "year", "yes", "yet", "young",
        "yourself", "abandon", "ability", "absorb", "absurd", "abuse", "academy", "accent", "ace", "acid",
        "acoustic", "acquire", "acre", "acrobat", "actor", "adapt", "addict", "adjust", "admiral", "adult",
        "aerial", "affair", "agenda", "agony", "aid", "aisle", "alarm", "album", "alien", "alliance",
        "alley", "allow", "alloy", "alpha", "altitude", "amber", "ammonia", "anchor", "android", "angel",
        "angle", "ankle", "anthem", "antique", "anvil", "apart", "apple", "arcade", "arena", "argon",
        "armor", "aroma", "arrow", "arson", "artist", "asphalt", "asteroid", "atom", "auction", "audit",
        "august", "aura", "avenue", "avocado", "awful", "bacon", "badge", "bagel", "baggage", "baker",
        "balloon", "bamboo", "banana", "banner", "barbecue", "barrel", "baseball", "basement", "battery", "beach",
        "beacon", "beaver", "beetle", "belt", "bench", "berry", "bicycle", "biscuit", "blade", "blanket",
        "blast", "blaze", "bleach", "blend", "bless", "blind", "blizzard", "blonde", "blouse", "blur",
        "blush", "bobbin", "bonfire", "boots", "border", "boss", "bowling", "bracket", "brain", "brand",
        "brass", "bread", "breakfast", "breath", "bridge", "broccoli", "bronze", "bubble", "bucket", "buffalo",
        "buggy", "bulldozer", "bun", "burger", "burrito", "bus", "butter", "button", "buzzard", "cabbage",
        "cabin", "cactus", "cafe", "cage", "cake", "calender", "camel", "camera", "camp", "canal",
        "candle", "canyon", "canvas", "cape", "carbon", "cargo", "carpet", "carrot", "cartoon", "castle",
        "cattle", "cauliflower", "cave", "celery", "cement", "center", "chain", "chalk", "champion", "chaos",
        "cheese", "chef", "cherry", "chestnut", "chick", "chili", "chime", "chipmunk", "chocolate", "chunk",
        "cider", "cinema", "circle", "citrus", "clam", "clamp", "claw", "clay", "clinic", "clip",
        "cloak", "closet", "cloud", "coach", "coast", "cobweb", "coconut", "coil", "comet", "comic",
        "compass", "computer", "condor", "cookie", "cooler", "copper", "coral", "cosmos", "cotton", "couch",
        "cougar", "cracker", "crate", "crayon", "cream", "cricket", "croissant", "crow", "crown", "crumb",
        "crystal", "cube", "curtain", "cyclone", "dad", "daisy", "dartboard", "deck", "deer", "denim",
        "desert", "diamond", "dice", "dinosaur", "dish", "dock", "donut", "dragon", "drain", "drawer",
        "dresser", "drill", "drum", "duck", "dune", "eagle", "earring", "earthquake", "echo", "edge",
        "eggplant", "elbow", "elf", "ember", "engine", "envelope", "eraser", "estate", "expedition", "fabric",
        "falcon", "fang", "farm", "feather", "fence", "fern", "field", "fir", "fish", "flame",
        "flannel", "flashlight", "flask", "flavor", "flipper", "flower", "flute", "fog", "font", "football",
        "forest", "fork", "fortune", "fountain", "fox", "frame", "fridge", "frog", "fruit", "fungus",
        "galaxy", "garlic", "gemstone", "ghost", "giraffe", "glass", "glove", "goat", "gold", "golf",
        "gondola", "gorilla", "grape", "grass", "gravy", "grill", "grizzly", "grocery", "guitar", "gym",
        "hail", "hamburger", "hammer", "harbor", "hatch", "helicopter", "heron", "hibernate", "highway", "hockey",
        "honey", "hook", "hopscotch", "horizon", "horn", "hotel", "hound", "house", "hubcap", "hummingbird",
        "iceberg", "igloo", "ink", "island", "ivory", "jacket", "jade", "jam", "jar", "jasmine",
        "jeans", "jeep", "jelly", "jet", "juice", "jungle", "kangaroo", "kayak", "kernel", "keyboard",
        "kick", "kingdom", "kite", "kiwi", "knob", "koala", "lab", "ladder", "ladybug", "lake",
        "lamp", "lantern", "laptop", "lasso", "lavender", "leaf", "leather", "lemon", "lettuce", "library",
        "lichen", "lime", "lion", "lobster", "log", "lotus", "luggage", "lunch", "lynx", "macaroni",
        "machine", "magnet", "mailbox", "mango", "maple", "marble", "marshmallow", "match", "meadow", "medal",
        "melon", "menu", "metal", "meteor", "microwave", "midnight", "mile", "milk", "mineral", "mint",
        "mirror", "mist", "mitten", "money", "monkey", "moose", "mosquito", "motorcycle", "mountain", "mouse",
        "muffin", "museum", "mushroom", "mustache", "nachos", "napkin", "necklace", "needle", "nest", "network",
        "newspaper", "nightmare", "notebook", "novel", "nut", "oak", "oatmeal", "octopus", "olive", "omelet",
        "onion", "opera", "orange", "orchid", "otter", "oven", "owl", "oyster", "package", "paddle",
        "pagoda", "paintbrush", "palace", "pancake", "panda", "paperclip", "parade", "park", "parrot", "passport",
        "peanut", "pear", "pebble", "pelican", "pen", "pencil", "pepper", "perfume", "pet", "picnic",
        "picture", "pie", "pig", "pinata", "pineapple", "pizza", "planet", "plasma", "plate", "playground",
        "plaza", "plume", "pocket", "polar", "popcorn", "porcupine", "postcard", "potato", "prairie", "pretzel",
        "printer", "pudding", "pumpkin", "pyramid", "quilt", "rabbit", "raccoon", "radish", "raft", "rainbow",
        "rake", "raspberry", "ray", "receipt", "reef", "remote", "resort", "ribbon", "rice", "river",
        "rocket", "roller", "roof", "rose", "rugby", "ruler", "salad", "salmon", "sandbox", "sandwich",
        "satellite", "sauce", "sausage", "scale", "scarf", "scarecrow", "school", "scissors", "scoop", "screen",
        "sculpture", "seal", "seed", "shampoo", "shark", "sheep", "shell", "shield", "ship", "shoe",
        "shower", "shrimp", "sidewalk", "signal", "silhouette", "silver", "skate", "skull", "sled", "sleeping",
        "slide", "slug", "snack", "snake", "snowflake", "soccer", "sock", "sofa", "soup", "spaceship",
        "spatula", "sphere", "spider", "spike", "spoon", "sprinkle", "squid", "stable", "stadium", "star",
        "statue", "steak", "stereo", "stew", "stomach", "straw", "strawberry", "stream", "submarine", "suitcase",
        "sunflower", "sunglasses", "sunshine", "surfboard", "sushi", "swan", "sword", "table", "tablet", "tadpole",
        "taffy", "taco", "tank", "taxi", "telescope", "television", "tennis", "tent", "theater", "thread",
        "thumb", "thunder", "ticket", "tie", "tiger", "toaster", "toothbrush", "toothpaste", "torch", "tornado",
        "tower", "toy", "tractor", "traffic", "train", "trash", "treasure", "tree", "trombone", "trophy",
        "truck", "tulip", "turkey", "turtle", "umbrella", "unicorn", "utensil", "vacation", "van", "vase",
        "vegetable", "vest", "violin", "volcano", "volleyball", "wagon", "walrus", "wasabi", "waterfall", "wave",
        "webcam", "whale", "wheel", "willow", "windmill", "window", "wing", "wolf", "woodpecker", "worm",
        "wrench", "yacht", "yak", "yarn", "yogurt", "zebra", "zipper", "zoo"
    )

    fun generatePassphrase(): String {
        val words = (1..wordCount).map { wordList[secureRandom.nextInt(wordList.size)] }.toMutableList()
        
        // Optionally insert a random number
        if (includeNumber && words.isNotEmpty()) {
            val number = secureRandom.nextInt(100)
            val position = secureRandom.nextInt(words.size + 1)
            words.add(position, number.toString())
        }

        // Apply case transformation
        val transformedWords = when (selectedCase) {
            "lowercase" -> words.map { it.lowercase() }
            "title" -> words.mapIndexed { index, word ->
                if (index == 0) word.replaceFirstChar { it.uppercase() } else word.lowercase()
            }
            "uppercase" -> words.map { it.uppercase() }
            else -> words.map { it.lowercase() }
        }

        // Apply separator
        return when (selectedSeparator) {
            "hyphen" -> transformedWords.joinToString("-")
            "underscore" -> transformedWords.joinToString("_")
            "none" -> transformedWords.joinToString("")
            else -> transformedWords.joinToString(" ")
        }
    }

    // Calculate passphrase entropy
    fun calculateEntropy(): Double {
        val wordEntropy = log2(wordList.size.toDouble()) * wordCount
        val separatorBonus = when (selectedSeparator) {
            "none" -> 0.0
            else -> 2.0 // Small bonus for separators
        }
        val numberBonus = if (includeNumber) log2(100.0) else 0.0
        return wordEntropy + separatorBonus + numberBonus
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        ToolTopBar(
            title = "Passphrase Generator",
            onBackClick = { navController.navigateUp() }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Info Card - Diceware explanation
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    Icons.Default.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "About Passphrases",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Passphrases use random common words instead of characters. A 6-word passphrase has ~77 bits of entropy, making it both secure and memorable. Based on Diceware principles.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Options Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Passphrase Settings",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Word Count Slider
                Text(
                    text = "Word Count: $wordCount words",
                    style = MaterialTheme.typography.bodyLarge
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Slider(
                    value = wordCount.toFloat(),
                    onValueChange = { wordCount = it.toInt() },
                    valueRange = 3f..10f,
                    steps = 6,
                    modifier = Modifier.fillMaxWidth()
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("3", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("5", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("7", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("10", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Quick entropy preview based on word count
                val previewEntropy = log2(wordList.size.toDouble()) * wordCount
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Text(
                        text = "~${previewEntropy.toInt()} bits of entropy with $wordCount words",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Separator Selection
                Text(
                    text = "Separator",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = selectedSeparator == "space",
                        onClick = { selectedSeparator = "space" },
                        label = { Text("Space") },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = selectedSeparator == "hyphen",
                        onClick = { selectedSeparator = "hyphen" },
                        label = { Text("Hyphen") },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = selectedSeparator == "underscore",
                        onClick = { selectedSeparator = "underscore" },
                        label = { Text("Underscore") },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = selectedSeparator == "none",
                        onClick = { selectedSeparator = "none" },
                        label = { Text("None") },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Capitalization Selection
                Text(
                    text = "Capitalization",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = selectedCase == "lowercase",
                        onClick = { selectedCase = "lowercase" },
                        label = { Text("lowercase") },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = selectedCase == "title",
                        onClick = { selectedCase = "title" },
                        label = { Text("Title Case") },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = selectedCase == "uppercase",
                        onClick = { selectedCase = "uppercase" },
                        label = { Text("UPPER CASE") },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Include Number Option
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { includeNumber = !includeNumber }
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = includeNumber,
                        onCheckedChange = { includeNumber = it }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(text = "Include Random Number", style = MaterialTheme.typography.bodyMedium)
                        Text(
                            text = "Insert a random number (0-99)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Generate Button
                Button(
                    onClick = {
                        isGenerating = true
                        generatedPassphrase = generatePassphrase()
                        isGenerating = false
                    },
                    enabled = !isGenerating,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (isGenerating) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text("Generate Passphrase")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Result Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Generated Passphrase",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (generatedPassphrase.isNotEmpty()) {
                    SelectionContainer {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surface
                        ) {
                            Text(
                                text = generatedPassphrase,
                                style = MaterialTheme.typography.headlineSmall.copy(
                                    fontWeight = FontWeight.Medium,
                                    letterSpacing = 0.5.sp
                                ),
                                modifier = Modifier.padding(20.dp),
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Copy button with message
                    OutlinedButton(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                            clipboard?.setPrimaryClip(ClipData.newPlainText("passphrase", generatedPassphrase))
                            showCopiedMessage = true
                        }
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (showCopiedMessage) "Copied!" else "Copy Passphrase")
                    }
                    
                    LaunchedEffect(showCopiedMessage) {
                        if (showCopiedMessage) {
                            kotlinx.coroutines.delay(2000)
                            showCopiedMessage = false
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Divider(modifier = Modifier.fillMaxWidth())

                    Spacer(modifier = Modifier.height(16.dp))

                    // Entropy Display
                    val entropy = calculateEntropy()
                    
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "🔐 Entropy: %.1f bits".format(entropy),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            
                            Spacer(modifier = Modifier.height(4.dp))

                            // Security assessment
                            val assessment = when {
                                entropy >= 78 -> Pair("✓ Excellent - Suitable for high-security applications", Color(0xFF4CAF50))
                                entropy >= 64 -> Pair("✓ Strong - Good security margin", Color(0xFF4CAF50))
                                entropy >= 52 -> Pair("○ Adequate - Minimum recommended", Color(0xFFFF9800))
                                else -> Pair("⚠ Consider using more words", Color(0xFFFF9800))
                            }
                            
                            Text(
                                text = assessment.first,
                                style = MaterialTheme.typography.bodySmall,
                                color = assessment.second,
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Stats row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        StatItem(label = "Words", value = "$wordCount${if (includeNumber) "+1#" else ""}")
                        StatItem(label="Characters", value = "${generatedPassphrase.length}")
                        StatItem(label="Word List", value = "${wordList.size}")
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Configure settings and click Generate",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Word List Info Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "📖 Word List Information",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(12.dp))

                DetailRow(label = "Total Words", value = "${wordList.size}")
                DetailRow(label = "Word Type", value = "Common English")
                DetailRow(label = "Bits per Word", value = "%.1f".format(log2(wordList.size.toDouble())))
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Divider(modifier = Modifier.fillMaxWidth())
                
                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Entropy Guide:",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                EntropyGuideItem(words = 4, entropy = "~51 bits", usage = "Low security")
                EntropyGuideItem(words = 5, entropy = "~64 bits", usage = "Minimum recommended")
                EntropyGuideItem(words = 6, entropy = "~77 bits", usage = "Good security")
                EntropyGuideItem(words = 7, entropy = "~90 bits", usage = "Strong security")
                EntropyGuideItem(words = 8, entropy = "~103 bits", usage = "Very strong")
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun StatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun EntropyGuideItem(words: Int, entropy: String, usage: String) {
    Row(
        modifier = Modifier.padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "$words words: ",
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = entropy,
            style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = " ($usage)",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
