/*
 * SPDX-FileCopyrightText: 2016 The CyanogenMod Project
 * SPDX-FileCopyrightText: The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package com.libremobileos.setupwizard

import android.content.pm.ActivityInfo
import android.content.res.Resources
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.util.TypedValue
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.VelocityTracker
import android.view.View
import android.view.ViewGroup.MarginLayoutParams
import android.view.WindowManagerPolicyConstants.NAV_BAR_MODE_GESTURAL_OVERLAY
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.DimenRes
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.dynamicanimation.animation.FloatValueHolder
import androidx.dynamicanimation.animation.SpringAnimation
import androidx.dynamicanimation.animation.SpringForce
import com.google.android.setupcompat.util.SystemBarHelper
import kotlin.math.min
import com.libremobileos.setupwizard.base.BaseSetupWizardActivity
import com.libremobileos.setupwizard.util.SetupWizardUtils
import com.libremobileos.setupwizard.widget.RevealHoleView

class FinishActivity : BaseSetupWizardActivity() {

    private val handler = Handler(Looper.getMainLooper())

    private lateinit var rootView: View
    private lateinit var swipeHint: View
    private lateinit var swipeHintIcon: ImageView
    private lateinit var swipeHintText: TextView
    private lateinit var background: RevealHoleView
    private lateinit var brandLogo: ImageView

    private var velocityTracker: VelocityTracker? = null
    private var dragStartY = 0f
    private var dragging = false

    // The closing animation runs on two Material 3 Expressive springs: a spatial one for how far
    // the wallpaper is revealed (hole size, logo scale, hint lift) and an effects one for the
    // content's opacity. While a finger drags, both follow it; on release they take over from
    // where it left them, the reveal with the finger's velocity. The logo leads: it zooms towards
    // the user as the wallpaper opens from its centre, while the hint is already fading out.
    private var revealProgress = 0f
    private var contentAlpha = 1f
    private var screenRevealed = false
    private val revealSpring = progressSpring { applyRevealProgress(it) }
    private val fadeSpring = progressSpring { applyContentAlpha(it) }

    private var edgeToEdgeWallpaperBackgroundTheme: Resources.Theme? = null
    private val revealWallpaper by lazy { !SetupWizardUtils.hasLeanback(this) }

    private val logoStartScale by lazy {
        ResourcesCompat.getFloat(resources, R.dimen.finish_logo_scale)
    }

    private enum class FinishState {
        NONE,
        ANIMATING,
        FINISHED,
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.i(TAG, "onCreate: finishState=$finishState")

        overrideActivityTransition(
            OVERRIDE_TRANSITION_CLOSE,
            R.anim.translucent_enter,
            R.anim.translucent_exit,
        )
        if (LOGV) {
            logActivityState("onCreate savedInstanceState=$savedInstanceState")
        }
        // Edge-to-edge
        val window = window
        window.setDecorFitsSystemWindows(false)

        window.isNavigationBarContrastEnforced = false
        window.isStatusBarContrastEnforced = false

        rootView = findViewById(R.id.root)
        swipeHint = findViewById(R.id.swipe_hint)
        swipeHintIcon = findViewById(R.id.swipe_hint_icon)
        swipeHintText = findViewById(R.id.swipe_hint_text)
        background = findViewById(R.id.background)
        brandLogo = findViewById(R.id.brand_logo)

        brandLogo.scaleX = logoStartScale
        brandLogo.scaleY = logoStartScale

        applyHomeAffordance()

        // Ensure the main layout (not including the background view) does not get obscured by bars.
        ViewCompat.setOnApplyWindowInsetsListener(rootView) { _, windowInsets ->
            val linearLayout = findViewById<View>(R.id.linear_layout)
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            val params = linearLayout.layoutParams as MarginLayoutParams
            params.leftMargin = insets.left
            params.topMargin = insets.top
            params.rightMargin = insets.right
            params.bottomMargin = insets.bottom
            linearLayout.layoutParams = params
            WindowInsetsCompat.CONSUMED
        }

        if (savedInstanceState == null && finishState == FinishState.NONE) {
            playEntranceAnimation()
        }

        if (finishState == FinishState.FINISHED) {
            Log.e(TAG, "Should not start again when finished!")
            finish()
        }
    }

    private fun applyHomeAffordance() {
        if (usesGestureNavigation()) {
            return
        }
        val hintResId =
            if (SetupWizardUtils.hasLeanback(this)) {
                R.string.press_center_to_go_home
            } else {
                R.string.tap_home_to_go_home
            }
        swipeHintIcon.setImageResource(R.drawable.ic_nav_home)
        swipeHintText.setText(hintResId)

        swipeHintIcon.contentDescription = getText(hintResId)
        borderlessRippleResId()?.let { swipeHintIcon.setBackgroundResource(it) }
        swipeHintIcon.setOnClickListener {
            if (finishState != FinishState.NONE) {
                return@setOnClickListener
            }
            commitReveal()
        }
    }

    private fun borderlessRippleResId(): Int? {
        val value = TypedValue()
        val resolved =
            theme.resolveAttribute(android.R.attr.selectableItemBackgroundBorderless, value, true)
        return value.resourceId.takeIf { resolved && it != 0 }
    }

    private fun usesGestureNavigation(): Boolean {
        if (SetupWizardUtils.hasLeanback(this)) {
            return false
        }
        val selected = SetupWizardApp.settingsBundle.getString(NAVIGATION_OPTION_KEY) ?: return true
        return selected == NAV_BAR_MODE_GESTURAL_OVERLAY
    }

    override fun onDestroy() {
        super.onDestroy()
        velocityTracker?.recycle()
        velocityTracker = null
    }

    private fun playEntranceAnimation() {
        val offset = rise(R.dimen.swipe_hint_icon_rise)
        listOf(swipeHintIcon, swipeHintText).forEachIndexed { index, view ->
            view.alpha = 0f
            view.translationY = offset
            view
                .animate()
                .alpha(1f)
                .translationY(0f)
                .setStartDelay(ENTRANCE_HINT_DELAY_MS + index * ENTRANCE_STAGGER_MS)
                .setDuration(ENTRANCE_DURATION_MS)
        }

        brandLogo.alpha = 0f
        brandLogo
            .animate()
            .alpha(1f)
            .setStartDelay(ENTRANCE_LOGO_DELAY_MS)
            .setDuration(ENTRANCE_LOGO_DURATION_MS)
    }

    private fun endEntranceAnimation() {
        listOf(swipeHintIcon, swipeHintText, brandLogo).forEach {
            it.animate().cancel()
            it.alpha = 1f
            it.translationY = 0f
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (finishState != FinishState.NONE || event.repeatCount != 0) {
            return super.onKeyDown(keyCode, event)
        }
        if (keyCode !in COMMIT_KEY_CODES) {
            return super.onKeyDown(keyCode, event)
        }
        commitReveal()
        return true
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (finishState != FinishState.NONE) {
            return super.onTouchEvent(event)
        }
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                endEntranceAnimation()
                // Catch the screen where it is if it is still springing back.
                revealSpring.cancel()
                fadeSpring.cancel()
                dragStartY = event.y + revealProgress * swipeDistance()
                dragging = true
                velocityTracker = VelocityTracker.obtain()
                velocityTracker?.addMovement(event)
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                if (!dragging) return super.onTouchEvent(event)
                velocityTracker?.addMovement(event)
                val dragged = (dragStartY - event.y).coerceAtLeast(0f)
                val progress = (dragged / swipeDistance()).coerceIn(0f, 1f)
                applyRevealProgress(progress)
                applyContentAlpha(1f - (progress / DRAG_FADE_FRACTION).coerceAtMost(1f))
                return true
            }
            MotionEvent.ACTION_UP,
            MotionEvent.ACTION_CANCEL -> {
                if (!dragging) return super.onTouchEvent(event)
                dragging = false
                val tracker = velocityTracker
                tracker?.addMovement(event)
                tracker?.computeCurrentVelocity(1000)
                val upVelocity = -(tracker?.yVelocity ?: 0f)
                tracker?.recycle()
                velocityTracker = null
                val progressVelocity = upVelocity / swipeDistance()
                if (revealProgress >= COMMIT_FRACTION || upVelocity > SWIPE_MIN_VELOCITY) {
                    commitReveal(progressVelocity.coerceAtLeast(0f))
                } else {
                    springBack(progressVelocity)
                }
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    private fun swipeDistance() = rootView.height / 3f

    private fun updateLogoPivot() {
        val drawable = brandLogo.drawable ?: return

        val availableWidth = brandLogo.width - brandLogo.paddingLeft - brandLogo.paddingRight
        val availableHeight = brandLogo.height - brandLogo.paddingTop - brandLogo.paddingBottom
        if (availableWidth <= 0 || availableHeight <= 0) {
            return
        }

        val scale =
            min(
                availableWidth / drawable.intrinsicWidth.toFloat(),
                availableHeight / drawable.intrinsicHeight.toFloat(),
            )
        val width = drawable.intrinsicWidth * scale
        val height = drawable.intrinsicHeight * scale
        val left = brandLogo.paddingLeft + (availableWidth - width) / 2f
        val top = brandLogo.paddingTop + (availableHeight - height) / 2f

        brandLogo.pivotX = left + width * MARK_CENTER_X
        brandLogo.pivotY = top + height * MARK_CENTER_Y
    }

    private fun applyRevealProgress(progress: Float) {
        revealProgress = progress
        updateLogoPivot()
        (brandLogo.parent as? View)?.let {
            val parentLocation = IntArray(2)
            val backgroundLocation = IntArray(2)
            it.getLocationOnScreen(parentLocation)
            background.getLocationOnScreen(backgroundLocation)
            background.holeCenterX =
                parentLocation[0] - backgroundLocation[0] + brandLogo.left + brandLogo.pivotX
            background.holeCenterY =
                parentLocation[1] - backgroundLocation[1] + brandLogo.top + brandLogo.pivotY
        }
        // The spring may overshoot either way: the hole never goes below nothing, while the
        // logo and text follow the overshoot and settle with it.
        background.holeRadius = background.fullRadius * REVEAL_REACH * progress.coerceAtLeast(0f)
        brandLogo.apply {
            val scale = logoStartScale * (1f + LOGO_GROWTH * progress)
            scaleX = scale
            scaleY = scale
        }
        swipeHintIcon.translationY = -rise(R.dimen.swipe_hint_icon_rise) * progress
        swipeHintText.translationY = -rise(R.dimen.swipe_hint_text_rise) * progress
        maybeFinishReveal()
    }

    private fun applyContentAlpha(alpha: Float) {
        contentAlpha = alpha
        val clamped = alpha.coerceIn(0f, 1f)
        brandLogo.alpha = clamped
        val hintAlpha = ((clamped - HINT_GONE_ALPHA) / (1f - HINT_GONE_ALPHA)).coerceIn(0f, 1f)
        swipeHintIcon.alpha = hintAlpha
        swipeHintText.alpha = hintAlpha
        maybeFinishReveal()
    }

    private fun rise(@DimenRes dimen: Int) = resources.getDimensionPixelSize(dimen).toFloat()

    private fun progressSpring(onUpdate: (Float) -> Unit) =
        SpringAnimation(FloatValueHolder()).apply {
            setMinimumVisibleChange(SpringAnimation.MIN_VISIBLE_CHANGE_SCALE)
            spring = SpringForce()
            addUpdateListener { _, value, _ -> onUpdate(value) }
        }

    private fun SpringAnimation.animate(
        from: Float,
        to: Float,
        stiffness: Float,
        dampingRatio: Float,
        velocity: Float = 0f,
    ) {
        cancel()
        spring.finalPosition = to
        spring.stiffness = stiffness
        spring.dampingRatio = dampingRatio
        setStartValue(from)
        setStartVelocity(velocity)
        start()
    }

    private fun springBack(velocity: Float) {
        revealSpring.animate(
            revealProgress,
            0f,
            SPATIAL_DEFAULT_STIFFNESS,
            SPATIAL_DEFAULT_DAMPING,
            velocity,
        )
        fadeSpring.animate(contentAlpha, 1f, EFFECTS_DEFAULT_STIFFNESS, EFFECTS_DAMPING)
    }

    private fun disableNavigation() {
        swipeHint.visibility = View.INVISIBLE
        SystemBarHelper.setBackButtonVisible(window, false)
    }

    override fun applyForwardTransition() {
        // no-op
    }

    override val layoutResId = R.layout.finish_activity

    override val installFooterBar = false

    override fun getTheme(): Resources.Theme {
        val theme = super.getTheme()
        if (!revealWallpaper) {
            return theme
        }
        return edgeToEdgeWallpaperBackgroundTheme
            ?: theme
                .apply { applyStyle(R.style.EdgeToEdgeWallpaperBackground, true) }
                .also { edgeToEdgeWallpaperBackgroundTheme = it }
    }

    override fun onNextPressed() {
        when (finishState) {
            FinishState.NONE -> commitReveal()
            else -> Log.e(TAG, "Unexpected state $finishState when navigating next")
        }
    }

    private fun commitReveal(velocity: Float = 0f) {
        endEntranceAnimation()
        finishState = FinishState.ANIMATING
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED)
        disableNavigation()
        if (!revealWallpaper) {
            fadeOutAndFinish()
            return
        }
        // A full-screen change: the slow spatial spring, carrying on at the finger's speed. The
        // content fades on the default effects spring, gone before the hole has passed it.
        revealSpring.animate(
            revealProgress,
            1f,
            SPATIAL_SLOW_STIFFNESS,
            SPATIAL_SLOW_DAMPING,
            velocity,
        )
        fadeSpring.animate(contentAlpha, 0f, EFFECTS_DEFAULT_STIFFNESS, EFFECTS_DAMPING)
        revealSpring.addEndListener { _, _, _, _ -> maybeFinishReveal(springsEnded = true) }
        fadeSpring.addEndListener { _, _, _, _ -> maybeFinishReveal(springsEnded = true) }
    }

    /**
     * Ends the reveal once the hole covers the screen and the content is gone, without waiting for
     * the spring's overshoot to settle: nothing on screen changes after that point.
     */
    private fun maybeFinishReveal(springsEnded: Boolean = false) {
        if (finishState != FinishState.ANIMATING || rootView.visibility != View.VISIBLE) {
            return
        }
        // Remembered: the overshoot brings the hole back a little after it first fills the screen.
        if (revealProgress * REVEAL_REACH >= 1f) {
            screenRevealed = true
        }
        val done = screenRevealed && contentAlpha <= CONTENT_GONE_ALPHA
        val settled = springsEnded && !revealSpring.isRunning && !fadeSpring.isRunning
        if (!done && !settled) {
            return
        }
        // Hidden first: cancelling the springs calls back into here.
        rootView.visibility = View.INVISIBLE
        revealSpring.cancel()
        fadeSpring.cancel()
        handler.post {
            if (LOGV) {
                Log.v(TAG, "Animation ended")
            }
            finishAfterAnimation()
        }
    }

    private fun fadeOutAndFinish() {
        findViewById<View>(R.id.linear_layout)
            .animate()
            .alpha(0f)
            .setDuration(FADE_OUT_DURATION_MS)
            .withEndAction {
                rootView.visibility = View.INVISIBLE
                handler.post { finishAfterAnimation() }
            }
    }

    private fun finishAfterAnimation() {
        SetupWizardUtils.finishSetupWizard(this)
        finishState = FinishState.FINISHED
    }

    companion object {
        private const val TAG = "FinishActivity"

        private const val MARK_CENTER_X = 256f / 512f
        private const val MARK_CENTER_Y = 256f / 512f

        private const val ENTRANCE_HINT_DELAY_MS = 150L
        private const val ENTRANCE_STAGGER_MS = 80L
        private const val ENTRANCE_DURATION_MS = 350L
        private const val ENTRANCE_LOGO_DELAY_MS = 400L
        private const val ENTRANCE_LOGO_DURATION_MS = 450L

        private const val COMMIT_FRACTION = 0.4f
        private const val SWIPE_MIN_VELOCITY = 600f

        // How far into the swipe the logo has faded away; the hint is gone at HINT_GONE_ALPHA.
        private const val DRAG_FADE_FRACTION = 0.5f
        private const val HINT_GONE_ALPHA = 0.5f
        private const val CONTENT_GONE_ALPHA = 0.01f

        // Material 3 Expressive motion scheme springs (ExpressiveMotionTokens).
        private const val SPATIAL_DEFAULT_STIFFNESS = 380f
        private const val SPATIAL_DEFAULT_DAMPING = 0.8f
        private const val SPATIAL_SLOW_STIFFNESS = 200f
        private const val SPATIAL_SLOW_DAMPING = 0.8f
        private const val EFFECTS_DEFAULT_STIFFNESS = 1600f
        private const val EFFECTS_DAMPING = 1f

        // The hole aims past the farthest corner so it covers the screen while still moving,
        // instead of creeping into the corners as the spring settles.
        private const val REVEAL_REACH = 1.35f

        private const val FADE_OUT_DURATION_MS = 400L

        private val COMMIT_KEY_CODES =
            setOf(
                KeyEvent.KEYCODE_DPAD_CENTER,
                KeyEvent.KEYCODE_ENTER,
                KeyEvent.KEYCODE_NUMPAD_ENTER,
                KeyEvent.KEYCODE_BUTTON_A,
            )

        // The logo grows by this much of its size per swipe distance: +50 % by the time it has
        // faded out.
        private const val LOGO_GROWTH = 1f

        // Static so a relaunch after the wizard has finished is recognised and dropped rather
        // than replaying the reveal; @Volatile because it is written from animation callbacks.
        @Volatile private var finishState = FinishState.NONE
    }
}
