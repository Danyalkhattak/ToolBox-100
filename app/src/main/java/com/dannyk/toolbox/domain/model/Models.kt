package com.dannyk.toolbox.domain.model

enum class Category(
    val displayName: String,
    val iconResName: String
) {
    CALCULATORS("Calculators", "calculate"),
    CONVERTERS("Converters", "swap_horiz"),
    MATH("Math", "functions"),
    TEXT("Text", "text_fields"),
    DEVELOPER("Developer", "code"),
    SECURITY("Security", "security"),
    INTERNET("Internet", "language"),
    IMAGE_COLOR("Images & Colors", "palette"),
    FILES("Files", "folder"),
    EVERYDAY("Everyday", "schedule")
}

data class Tool(
    val id: Int,
    val name: String,
    val description: String,
    val category: Category,
    val route: String,
    val iconResName: String,
    val isFavorite: Boolean = false
)

data class FavoriteTool(
    val toolId: Int,
    val addedAt: Long = System.currentTimeMillis()
)

data class RecentTool(
    val toolId: Int,
    val openedAt: Long = System.currentTimeMillis()
)
