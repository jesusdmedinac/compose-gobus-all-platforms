sealed interface Platform
data object Android : Platform
data object iOS : Platform
data object Jvm : Platform

expect fun getPlatform(): Platform
