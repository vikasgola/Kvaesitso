package de.mm20.launcher2.preferences

import android.content.Context
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class LauncherSettingsData(
    val schemaVersion: Int = 1,

    val uiColorScheme: ColorScheme = ColorScheme.System,
    val uiTheme: ThemeDescriptor = ThemeDescriptor.Default,
    val uiCompatModeColors: Boolean = false,
    val uiFont: Font = Font.Outfit,
    val uiBaseLayout: BaseLayout = BaseLayout.PullDown,
    val uiOrientation: ScreenOrientation = ScreenOrientation.Auto,

    val wallpaperDim: Boolean = false,
    val wallpaperBlur: Boolean = true,
    val wallpaperBlurRadius: Int = 32,

    val mediaAllowList: Set<String> = emptySet(),
    val mediaDenyList: Set<String> = emptySet(),

    val clockWidgetCompact: Boolean = false,
    val clockWidgetStyle: ClockWidgetStyle = ClockWidgetStyle.Digital1(),
    val clockWidgetColors: ClockWidgetColors = ClockWidgetColors.Auto,
    val clockWidgetAlarmPart: Boolean = true,
    val clockWidgetBatteryPart: Boolean = true,
    val clockWidgetMusicPart: Boolean = true,
    val clockWidgetDatePart: Boolean = true,
    val clockWidgetFillHeight: Boolean = true,
    val clockWidgetAlignment: ClockWidgetAlignment = ClockWidgetAlignment.Bottom,

    val homeScreenDock: Boolean = false,

    val favoritesEnabled: Boolean = true,
    val favoritesFrequentlyUsed: Boolean = true,
    val favoritesFrequentlyUsedRows: Int = 1,
    val favoritesEditButton: Boolean = true,

    val fileSearchProviders: Set<String> = setOf("local"),

    val contactSearchEnabled: Boolean = true,

    val calendarSearchEnabled: Boolean = true,

    val shortcutSearchEnabled: Boolean = true,

    val calculatorEnabled: Boolean = true,

    val unitConverterEnabled: Boolean = true,
    val unitConverterCurrencies: Boolean = true,

    val wikipediaSearchEnabled: Boolean = false,
    val geminiSearchEnabled: Boolean = false,
    val geminiCustomUrl: String? = null,
    val wikipediaSearchImages: Boolean = false,
    val wikipediaCustomUrl: String? = null,

    val websiteSearchEnabled: Boolean = false,

    val badgesNotifications: Boolean = true,
    val badgesSuspendedApps: Boolean = true,
    val badgesCloudFiles: Boolean = true,
    val badgesShortcuts: Boolean = true,
    val badgesPlugins: Boolean = true,

    val gridColumnCount: Int = 5,
    val gridIconSize: Int = 48,
    val gridLabels: Boolean = true,

    val searchBarStyle: SearchBarStyle = SearchBarStyle.Transparent,
    val searchBarColors: SearchBarColors = SearchBarColors.Auto,
    val searchBarKeyboard: Boolean = true,
    val searchLaunchOnEnter: Boolean = true,
    val searchBarBottom: Boolean = false,
    val searchBarFixed: Boolean = false,

    val searchResultsReversed: Boolean = false,
    val searchResultOrder: SearchResultOrder = SearchResultOrder.Weighted,

    val rankingWeightFactor: WeightFactor = WeightFactor.Default,

    val hiddenItemsShowButton: Boolean = true,

    val iconsShape: IconShape = IconShape.PlatformDefault,
    val iconsAdaptify: Boolean = false,
    val iconsThemed: Boolean = false,
    val iconsForceThemed: Boolean = false,
    val iconsPack: String? = null,
    val iconsPackThemed: Boolean = false,

    val easterEgg: Boolean = false,

    val systemBarsHideStatus: Boolean = false,
    val systemBarsHideNav: Boolean = false,
    val systemBarsStatusColors: SystemBarColors = SystemBarColors.Auto,
    val systemBarsNavColors: SystemBarColors = SystemBarColors.Auto,

    val surfacesOpacity: Float = 1f,
    val surfacesRadius: Int = 24,
    val surfacesBorderWidth: Int = 0,
    val surfacesShape: SurfaceShape = SurfaceShape.Rounded,

    val widgetsEditButton: Boolean = true,

    val gesturesSwipeDown: GestureAction = GestureAction.Notifications,
    val gesturesSwipeLeft: GestureAction = GestureAction.NoAction,
    val gesturesSwipeRight: GestureAction = GestureAction.NoAction,
    val gesturesDoubleTap: GestureAction = GestureAction.ScreenLock,
    val gesturesLongPress: GestureAction = GestureAction.NoAction,
    val gesturesHomeButton: GestureAction = GestureAction.NoAction,

    val animationsCharging: Boolean = true,

    val stateTagsMultiline: Boolean = false,

    val weatherProvider: String = "metno",
    val weatherAutoLocation: Boolean = true,
    val weatherLocation: LatLon? = null,
    val weatherLocationName: String? = null,
    val weatherLastLocation: LatLon? = null,
    val weatherLastUpdate: Long = 0L,
    val weatherProviderSettings: Map<String, ProviderSettings> = emptyMap(),
    val weatherImperialUnits: Boolean = false,

    ) {
    constructor(
        context: Context,
    ) : this(
        weatherImperialUnits = context.resources.getBoolean(R.bool.default_imperialUnits),
        gridColumnCount = context.resources.getInteger(R.integer.config_columnCount),
    )
}

@Serializable
enum class ColorScheme {
    Light,
    Dark,
    System,
}

@Serializable
enum class Font {
    Outfit,
    System,
}


@Serializable
sealed interface ThemeDescriptor {
    @Serializable
    @SerialName("default")
    data object Default : ThemeDescriptor

    @Serializable
    @SerialName("bw")
    data object BlackAndWhite : ThemeDescriptor

    @Serializable
    @SerialName("custom")
    data class Custom(
        val id: String,
    ) : ThemeDescriptor
}

@Serializable
sealed interface ClockWidgetStyle {
    @Serializable
    @SerialName("digital1")
    data class Digital1(
        val outlined: Boolean = false,
        val variant: Variant = Variant.Default,
    ) : ClockWidgetStyle {
        @Serializable
        enum class Variant {
            Default,
            MDY,
            OnePlus,
        }
    }

    @Serializable
    @SerialName("digital2")
    data object Digital2 : ClockWidgetStyle

    @Serializable
    @SerialName("orbit")
    data object Orbit : ClockWidgetStyle

    @Serializable
    @SerialName("analog")
    data object Analog : ClockWidgetStyle

    @Serializable
    @SerialName("binary")
    data object Binary : ClockWidgetStyle

    @Serializable
    @SerialName("empty")
    data object Empty : ClockWidgetStyle
}

@Serializable
enum class ClockWidgetColors {
    Auto,
    Light,
    Dark,
}

@Serializable
enum class ClockWidgetAlignment {
    Top,
    Center,
    Bottom,
}

@Serializable
enum class SearchBarStyle {
    Transparent,
    Solid,
    Hidden,
}

@Serializable
enum class SearchBarColors {
    Auto,
    Light,
    Dark,
}

@Serializable
enum class IconShape {
    PlatformDefault,
    Circle,
    Square,
    RoundedSquare,
    Triangle,
    Squircle,
    Hexagon,
    Pentagon,
    Teardrop,
    Pebble,
    EasterEgg,
}

@Serializable
enum class SystemBarColors {
    Auto,
    Light,
    Dark,
}

@Serializable
enum class SurfaceShape {
    Rounded,
    Cut,
}

@Serializable
enum class BaseLayout {
    PullDown,
    Pager,
    PagerReversed,
}

@Serializable
enum class ScreenOrientation {
    Auto,
    Portrait,
    Landscape,
}

@Serializable
sealed interface GestureAction {
    @Serializable
    @SerialName("no_action")
    data object NoAction : GestureAction

    @Serializable
    @SerialName("notifications")
    data object Notifications : GestureAction

    @Serializable
    @SerialName("quick_settings")
    data object QuickSettings : GestureAction

    @Serializable
    @SerialName("screen_lock")
    data object ScreenLock : GestureAction

    @Serializable
    @SerialName("search")
    data object Search : GestureAction

    @Serializable
    @SerialName("power_menu")
    data object PowerMenu : GestureAction

    @Serializable
    @SerialName("recents")
    data object Recents : GestureAction

    @Serializable
    @SerialName("launch_searchable")
    data class Launch(val key: String?) : GestureAction
}

@Serializable
enum class SearchResultOrder {
    Weighted,
    Alphabetical,
    LaunchCount,
}

@Serializable
enum class WeightFactor {
    Default,
    Low,
    High,
}

@Serializable
data class LatLon(
    val lat: Double,
    val lon: Double,
)

@Serializable
data class ProviderSettings(
    val locationId: String? = null,
    val locationName: String? = null,
)