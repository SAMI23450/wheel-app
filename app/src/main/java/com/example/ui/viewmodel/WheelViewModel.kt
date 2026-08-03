package com.example.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.model.CoinFlipHistoryEntity
import com.example.data.model.SliceSerializer
import com.example.data.model.SpinHistoryEntity
import com.example.data.model.UserEntity
import com.example.data.model.WheelConfigEntity
import com.example.data.model.WheelSlice
import com.example.data.repository.AppRepository
import android.media.AudioManager
import android.media.ToneGenerator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.random.Random

class WheelViewModel(private val repository: AppRepository) : ViewModel() {

    // Network connectivity status
    val isOnline: StateFlow<Boolean> = repository.isOnline
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = true
        )

    // Current logged-in user
    private val _currentUser = MutableStateFlow<UserEntity>(
        UserEntity("guest_user", "Guest User", "guest@wheelapp.local", "Guest")
    )
    val currentUser: StateFlow<UserEntity> = _currentUser.asStateFlow()

    // Sync state message
    private val _syncMessage = MutableStateFlow<String>("Fully synchronized")
    val syncMessage: StateFlow<String> = _syncMessage.asStateFlow()

    // Selected tab: "wheel", "coin_flip", "favorites", "history", "profile"
    private val _currentTab = MutableStateFlow("wheel")
    val currentTab: StateFlow<String> = _currentTab.asStateFlow()

    // Set active tab
    fun setTab(tab: String) {
        _currentTab.value = tab
    }

    // Dynamic queries tied to the active user's ID
    val savedWheels: StateFlow<List<WheelConfigEntity>> = _currentUser
        .flatMapLatest { user -> repository.getWheelConfigs(user.id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val favoriteWheels: StateFlow<List<WheelConfigEntity>> = _currentUser
        .flatMapLatest { user -> repository.getFavoriteWheelConfigs(user.id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val spinHistory: StateFlow<List<SpinHistoryEntity>> = _currentUser
        .flatMapLatest { user -> repository.getSpinHistory(user.id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val coinFlipHistory: StateFlow<List<CoinFlipHistoryEntity>> = _currentUser
        .flatMapLatest { user -> repository.getCoinFlipHistory(user.id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- WHEEL SECTION STATES ---
    private val _activeSlices = MutableStateFlow<List<WheelSlice>>(emptyList())
    val activeSlices: StateFlow<List<WheelSlice>> = _activeSlices.asStateFlow()

    private val _wheelTitle = MutableStateFlow("My Spin Wheel")
    val wheelTitle: StateFlow<String> = _wheelTitle.asStateFlow()

    private val _loadedConfigId = MutableStateFlow<Int?>(null)
    val loadedConfigId: StateFlow<Int?> = _loadedConfigId.asStateFlow()

    private val _isCurrentWheelFavorite = MutableStateFlow(false)
    val isCurrentWheelFavorite: StateFlow<Boolean> = _isCurrentWheelFavorite.asStateFlow()

    // Spinning animation state
    private val _isSpinning = MutableStateFlow(false)
    val isSpinning: StateFlow<Boolean> = _isSpinning.asStateFlow()

    // Rotation angle in degrees
    private val _wheelRotation = MutableStateFlow(0f)
    val wheelRotation: StateFlow<Float> = _wheelRotation.asStateFlow()

    // Spin winning results popup dialog
    private val _spinWinner = MutableStateFlow<WheelSlice?>(null)
    val spinWinner: StateFlow<WheelSlice?> = _spinWinner.asStateFlow()

    // Confetti trigger
    private val _showConfetti = MutableStateFlow(false)
    val showConfetti: StateFlow<Boolean> = _showConfetti.asStateFlow()

    // --- COIN FLIP SECTION STATES ---
    private val _isFlipping = MutableStateFlow(false)
    val isFlipping: StateFlow<Boolean> = _isFlipping.asStateFlow()

    private val _coinResult = MutableStateFlow<String?>("Heads")
    val coinResult: StateFlow<String?> = _coinResult.asStateFlow()

    private val _coinRotationX = MutableStateFlow(0f)
    val coinRotationX: StateFlow<Float> = _coinRotationX.asStateFlow()

    // Auth error state
    private val _authError = MutableStateFlow<String?>(null)
    val authError: StateFlow<String?> = _authError.asStateFlow()

    fun clearAuthError() {
        _authError.value = null
    }

    // --- SOUND EFFECTS SETTING & PLAYBACK ---
    private val _isSoundEnabled = MutableStateFlow(repository.isSoundEnabled())
    val isSoundEnabled: StateFlow<Boolean> = _isSoundEnabled.asStateFlow()

    fun setSoundEnabled(enabled: Boolean) {
        _isSoundEnabled.value = enabled
        repository.setSoundEnabled(enabled)
    }

    // --- THEME SETTING ---
    private val _appTheme = MutableStateFlow(repository.getAppTheme())
    val appTheme: StateFlow<String> = _appTheme.asStateFlow()

    fun setAppTheme(theme: String) {
        _appTheme.value = theme
        repository.setAppTheme(theme)
    }

    // --- LANGUAGE SETTING ---
    private val _appLanguage = MutableStateFlow(repository.getAppLanguage())
    val appLanguage: StateFlow<String> = _appLanguage.asStateFlow()

    fun setAppLanguage(lang: String) {
        _appLanguage.value = lang
        repository.setAppLanguage(lang)
    }

    fun playSpinCompletionSound() {
        if (!_isSoundEnabled.value) return
        viewModelScope.launch(Dispatchers.Default) {
            try {
                val toneGen = ToneGenerator(AudioManager.STREAM_MUSIC, 85)
                toneGen.startTone(ToneGenerator.TONE_PROP_BEEP, 80)
                delay(100)
                toneGen.startTone(ToneGenerator.TONE_PROP_ACK, 100)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun playCoinFlipSound() {
        if (!_isSoundEnabled.value) return
        viewModelScope.launch(Dispatchers.Default) {
            try {
                val toneGen = ToneGenerator(AudioManager.STREAM_MUSIC, 85)
                toneGen.startTone(ToneGenerator.TONE_PROP_ACK, 120)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    init {
        // Load remembered user session from SharedPreferences/Database
        viewModelScope.launch {
            val lastUserId = repository.getLastUserId()
            if (lastUserId != null) {
                val user = repository.getUser(lastUserId)
                if (user != null) {
                    _currentUser.value = user
                    _syncMessage.value = "Welcome back, ${user.displayName}!"
                    
                    // Auto-load first saved wheel if available
                    delay(150)
                    val userWheels = savedWheels.value
                    if (userWheels.isNotEmpty()) {
                        loadWheelConfig(userWheels.first())
                    }
                }
            }
        }

        // Observe network state to simulate secure backups
        viewModelScope.launch {
            isOnline.collect { online ->
                _syncMessage.value = if (online) "Online: Synced with Secure Cloud" else "Offline: Safe Local Storage Only"
            }
        }
    }

    // --- AUTH ACTIONS ---
    fun login(email: String, name: String, provider: String, password: String = "", isRegistering: Boolean = false) {
        viewModelScope.launch {
            _authError.value = null
            val userId = "${provider.lowercase()}_${email.replace(".", "_")}"
            val existingUser = repository.getUser(userId)

            if (provider == "Email") {
                if (isRegistering) {
                    if (existingUser != null) {
                        _authError.value = "An account with this email already exists."
                        return@launch
                    }
                    val newUser = UserEntity(userId, name, email, provider, password)
                    repository.saveUser(newUser)
                    completeLogin(newUser)
                } else {
                    if (existingUser == null) {
                        _authError.value = "No account found with this email. Please register."
                        return@launch
                    }
                    if (existingUser.password != password) {
                        _authError.value = "Incorrect password. Please try again."
                        return@launch
                    }
                    completeLogin(existingUser)
                }
            } else {
                // Social provider login
                val user = existingUser ?: UserEntity(userId, name, email, provider, "")
                if (existingUser == null) {
                    repository.saveUser(user)
                }
                completeLogin(user)
            }
        }
    }

    fun loginAnonymously() {
        viewModelScope.launch {
            _authError.value = null
            // Generate a persistent, unique anonymous user ID
            val uuid = java.util.UUID.randomUUID().toString().take(6)
            val userId = "anon_$uuid"
            val name = "Anonymous User ($uuid)"
            val user = UserEntity(userId, name, "anonymous@wheelapp.local", "Anonymous", "")
            
            repository.saveUser(user)
            completeLogin(user)
        }
    }

    private suspend fun completeLogin(user: UserEntity) {
        repository.saveLastUserId(user.id)
        _currentUser.value = user
        _syncMessage.value = "Synced: Logged in via ${user.provider}"
        setTab("wheel")
        
        delay(100)
        val userWheels = savedWheels.value
        if (userWheels.isNotEmpty()) {
            loadWheelConfig(userWheels.first())
        } else {
            _loadedConfigId.value = null
            _isCurrentWheelFavorite.value = false
            _activeSlices.value = emptyList()
            _wheelTitle.value = "My Spin Wheel"
        }
    }

    fun logout() {
        viewModelScope.launch {
            repository.saveLastUserId(null)
            _currentUser.value = UserEntity("guest_user", "Guest User", "guest@wheelapp.local", "Guest")
            _loadedConfigId.value = null
            _isCurrentWheelFavorite.value = false
            _activeSlices.value = emptyList()
            _wheelTitle.value = "My Spin Wheel"
            _syncMessage.value = "Fully synchronized"
        }
    }

    // --- WHEEL ACTIONS ---
    fun setWheelTitle(title: String) {
        _wheelTitle.value = title
    }

    fun addSlice(name: String, colorHex: String) {
        val newList = _activeSlices.value.toMutableList()
        newList.add(WheelSlice(name, colorHex))
        _activeSlices.value = newList
    }

    fun removeSlice(index: Int) {
        if (index in 0 until _activeSlices.value.size) {
            val newList = _activeSlices.value.toMutableList()
            newList.removeAt(index)
            _activeSlices.value = newList
        }
    }

    fun updateSlice(index: Int, name: String, colorHex: String) {
        if (index in 0 until _activeSlices.value.size) {
            val newList = _activeSlices.value.toMutableList()
            newList[index] = WheelSlice(name, colorHex)
            _activeSlices.value = newList
        }
    }

    fun clearAllSlices() {
        _activeSlices.value = emptyList()
    }

    // Spin logic with high fidelity easing simulation
    fun spinWheel() {
        if (_isSpinning.value || _activeSlices.value.isEmpty()) return

        viewModelScope.launch {
            _isSpinning.value = true
            _spinWinner.value = null
            _showConfetti.value = false

            val sliceCount = _activeSlices.value.size
            val winnerIndex = Random.nextInt(sliceCount)
            val selectedWinner = _activeSlices.value[winnerIndex]

            // Easing physics values
            // 360 degrees / slices
            val degreesPerSlice = 360f / sliceCount
            // Target angle (ensure pointer is at the top i.e. 270 degrees in canvas space)
            // Slices start from 0 at 3-o'clock (0 degrees). A pointer at 12-o'clock is 270 degrees.
            // Slices rotate clockwise, so to align a slice with 12-o'clock (270deg), we rotate:
            // Angle = 270 - (winnerIndex * degreesPerSlice + degreesPerSlice / 2)
            val halfSliceOffset = degreesPerSlice / 2f
            val baseTargetAngle = 270f - (winnerIndex * degreesPerSlice + halfSliceOffset)
            val rotationsCount = 5 + Random.nextInt(3) // 5 to 7 full rotations
            val totalTargetRotation = (rotationsCount * 360f) + (baseTargetAngle % 360f)

            val durationMs = 4500L
            val intervals = 120
            val delayInterval = durationMs / intervals

            var currentAngle = _wheelRotation.value % 360f
            val distance = totalTargetRotation - currentAngle

            for (i in 0..intervals) {
                val progress = i.toFloat() / intervals
                // Cubic easeOut: f(x) = 1 - (1 - x)^3
                val easeProgress = 1f - Math.pow((1f - progress).toDouble(), 3.0).toFloat()
                _wheelRotation.value = currentAngle + (distance * easeProgress)
                delay(delayInterval)
            }

            // Lock final visual rotation
            _wheelRotation.value = totalTargetRotation

            // Declare winner!
            _spinWinner.value = selectedWinner
            _showConfetti.value = true
            _isSpinning.value = false
            playSpinCompletionSound()

            // Save to spin history
            insertSpinHistory(selectedWinner.name)
        }
    }

    fun dismissWinnerPopup() {
        _spinWinner.value = null
        _showConfetti.value = false
    }

    // Save current configuration to database
    fun saveCurrentWheel() {
        viewModelScope.launch {
            val title = _wheelTitle.value.ifBlank { "My Custom Wheel" }
            val slices = _activeSlices.value
            val serialized = SliceSerializer.serialize(slices)
            
            val config = WheelConfigEntity(
                id = _loadedConfigId.value ?: 0,
                userId = _currentUser.value.id,
                title = title,
                slicesSerialized = serialized,
                isFavorite = _isCurrentWheelFavorite.value
            )

            val newId = repository.saveWheelConfig(config)
            if (_loadedConfigId.value == null) {
                _loadedConfigId.value = newId
            }
            _syncMessage.value = "Wheel configuration saved!"
        }
    }

    // Toggle favorite on current loaded wheel
    fun toggleFavoriteCurrentWheel() {
        val configId = _loadedConfigId.value
        val nextFavorite = !_isCurrentWheelFavorite.value
        _isCurrentWheelFavorite.value = nextFavorite

        if (configId != null) {
            viewModelScope.launch {
                repository.updateFavoriteStatus(configId, nextFavorite)
                _syncMessage.value = if (nextFavorite) "Added to Favorites!" else "Removed from Favorites"
            }
        } else {
            // Not saved in db yet, save it now with favorite true
            saveCurrentWheel()
        }
    }

    // Toggle favorite on lists
    fun toggleFavoriteSavedWheel(config: WheelConfigEntity) {
        viewModelScope.launch {
            val nextState = !config.isFavorite
            repository.updateFavoriteStatus(config.id, nextState)
            if (config.id == _loadedConfigId.value) {
                _isCurrentWheelFavorite.value = nextState
            }
        }
    }

    // Load saved wheel configuration onto spin screen
    fun loadWheelConfig(config: WheelConfigEntity) {
        _loadedConfigId.value = config.id
        _wheelTitle.value = config.title
        _isCurrentWheelFavorite.value = config.isFavorite
        _activeSlices.value = SliceSerializer.deserialize(config.slicesSerialized)
        setTab("wheel")
    }

    // Delete a wheel configuration
    fun deleteWheelConfig(config: WheelConfigEntity) {
        viewModelScope.launch {
            repository.deleteWheelConfig(config)
            if (_loadedConfigId.value == config.id) {
                _loadedConfigId.value = null
                _isCurrentWheelFavorite.value = false
            }
            _syncMessage.value = "Deleted configuration successfully"
        }
    }

    // Insert history
    private suspend fun insertSpinHistory(winnerName: String) {
        try {
            val spin = SpinHistoryEntity(
                userId = _currentUser.value.id,
                wheelName = _wheelTitle.value.ifBlank { "Custom Wheel" },
                winnerName = winnerName
            )
            repository.insertSpinHistory(spin)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun clearSpinHistory() {
        viewModelScope.launch {
            repository.clearSpinHistory(_currentUser.value.id)
            _syncMessage.value = "Spin history cleared"
        }
    }

    // --- COIN FLIP ACTIONS ---
    fun flipCoin() {
        if (_isFlipping.value) return

        viewModelScope.launch {
            _isFlipping.value = true
            _coinResult.value = null

            val landsHeads = Random.nextBoolean()
            val targetResult = if (landsHeads) "Heads" else "Tails"

            // Spins around X axis
            // Minimum 6 full flips (2160 degrees) plus offset if Tail (Tails is on the back: 180 degree rotation)
            val extraDegrees = if (targetResult == "Heads") 0f else 180f
            val totalRotationX = (8 * 360f) + extraDegrees

            val durationMs = 2500L
            val intervals = 80
            val delayInterval = durationMs / intervals

            var currentRot = _coinRotationX.value % 360f
            val distance = totalRotationX - currentRot

            for (i in 0..intervals) {
                val progress = i.toFloat() / intervals
                // Custom parabolic + deceleration spin progression
                val easeProgress = 1f - Math.pow((1f - progress).toDouble(), 2.5).toFloat()
                _coinRotationX.value = currentRot + (distance * easeProgress)
                delay(delayInterval)
            }

            // Lock final exact rotations
            _coinRotationX.value = totalRotationX

            _coinResult.value = targetResult
            _isFlipping.value = false
            playCoinFlipSound()

            // Save to coin history
            val flip = CoinFlipHistoryEntity(
                userId = _currentUser.value.id,
                result = targetResult
            )
            repository.insertCoinFlipHistory(flip)
        }
    }

    fun clearCoinHistory() {
        viewModelScope.launch {
            repository.clearCoinFlipHistory(_currentUser.value.id)
            _syncMessage.value = "Coin flip history cleared"
        }
    }
}

// Custom factory to inject dependency
class WheelViewModelFactory(private val repository: AppRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(WheelViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return WheelViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
