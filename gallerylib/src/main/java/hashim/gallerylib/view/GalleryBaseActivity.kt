package hashim.gallerylib.view

import android.annotation.TargetApi
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import hashim.gallerylib.R
import hashim.gallerylib.databinding.ActivityBaseGalleryBinding
import hashim.gallerylib.util.GalleryConstants
import java.util.*
import kotlin.properties.Delegates

abstract class GalleryBaseActivity : AppCompatActivity {
    var drawHeader: Boolean by Delegates.notNull<Boolean>()

    internal var showBack: Boolean by Delegates.notNull<Boolean>()
    internal var isCloseIcon: Boolean by Delegates.notNull<Boolean>()
    internal var showMenu: Boolean by Delegates.notNull<Boolean>()
    internal var showAny: Boolean by Delegates.notNull<Boolean>()
    internal var showCall: Boolean by Delegates.notNull<Boolean>()
    internal var appBarWhite: Boolean by Delegates.notNull<Boolean>()
    // to allow sliding menu or not

    private var activityTitleId: Int = 0

    constructor(
        activityTitleId: Int,
        drawHeader: Boolean,
        showBack: Boolean,
        isCloseIcon: Boolean,
        showMenu: Boolean,
        showAny: Boolean,
        showCall: Boolean,
        appBarWhite: Boolean
    ) : super() {
        this.drawHeader = drawHeader
        this.showBack = showBack
        this.isCloseIcon = isCloseIcon
        this.showMenu = showMenu
        this.showAny = showAny
        this.showCall = showCall
        this.appBarWhite = appBarWhite
        this.activityTitleId = activityTitleId
    }

    protected abstract fun doOnCreate(arg0: Bundle?)
    abstract fun initializeViews()

    abstract fun setListener()

    var imm: InputMethodManager by Delegates.notNull<InputMethodManager>()

    lateinit var baseBinding: ActivityBaseGalleryBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        baseBinding = DataBindingUtil.setContentView(this, R.layout.activity_base_gallery)
        //set default color for appbar
        setTranslucentAppBar()

        imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

        var locale = "en"
        if (intent.hasExtra(GalleryConstants.Language))
            locale = intent.getStringExtra(GalleryConstants.Language)!!
        if (locale.contains("ar")) {
            ChangeLocale("ar")
            //RTL
            forceRTLIfSupported()
        } else {
            ChangeLocale("en")
            //LTR
            forceLTRIfSupported()
        }
        //set actionbar
        setDrawHeader(
            drawHeader,
            getString(activityTitleId),
            showBack,
            isCloseIcon,
            showAny,
            showMenu,
            showCall,
            appBarWhite
        )

        doOnCreate(savedInstanceState)
        setListener()
        backBtnAction()
    }

    fun putContentView(activityLayout: Int): ViewDataBinding {
        return DataBindingUtil.inflate(
            layoutInflater,
            activityLayout,
            baseBinding.baseFragment,
            true
        )
    }

    fun setActionBarVisibilty(isVisible: Boolean) {
        baseBinding.layoutContainerActionBar.visibility =
            if (isVisible) View.VISIBLE else View.GONE
    }

    internal fun backBtnAction() {
        baseBinding.ivbackActionBar.setOnClickListener {
            onBackPressed()
        }
    }

    fun setHeaderTitle(title: String) {
        baseBinding.tvTitleCustomActionBar.text = title
    }

    internal fun setTranslucentAppBar() {
        val fullScreen = window.attributes.flags and WindowManager.LayoutParams.FLAG_FULLSCREEN != 0
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (fullScreen) {
                baseBinding.layoutContainerActionBar.setVisibility(View.GONE)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
                    window.statusBarColor = ContextCompat.getColor(this, R.color.colorPrimaryDark)
                }
            } else {
                setAppBarGradient()
            }
        } else {
            if (!fullScreen) {
                setAppBarGradient()
            }
        }
    }

    fun setDrawHeader(
        isShowHeader: Boolean,
        title: String,
        isShowBackIcon: Boolean,
        isCloseIcon: Boolean,
        showAny: Boolean,
        showMenu: Boolean,
        showcallBtn: Boolean,
        appBarWhite: Boolean
    ) {
        setActionBarVisibilty(isShowHeader)
        setBackIconVisibility(isShowBackIcon, isCloseIcon, showMenu, appBarWhite)
        setHeaderTitle(title)
        if (appBarWhite) {
            setAppBarlightAndStatusBarDark(R.color.white)
        } else {
            setAppBarGradient()
            baseBinding.layoutContainerActionBar.setBackgroundResource(R.color.colorPrimary)
            baseBinding.tvTitleCustomActionBar.setTextColor(
                ContextCompat.getColor(
                    this,
                    R.color.white
                )
            )
        }
    }

    fun setBackIconVisibility(
        isShowBackIcon: Boolean,
        isCloseIcon: Boolean,
        showMenu: Boolean, appBarWhite: Boolean
    ) {
        //set icons
        if (appBarWhite) {
            if (isShowBackIcon) {
                if (isCloseIcon) {
                    baseBinding.ivbackActionBar.setImageResource(R.drawable.back_black_icon)
                } else {
                    baseBinding.ivbackActionBar.setImageResource(R.drawable.back_black_icon)
                }
            } else if (showMenu) {
                baseBinding.ivbackActionBar.setImageResource(R.drawable.back_black_icon)
                //disable slide menu
            } else {
                baseBinding.ivbackActionBar.visibility =
                    if (isShowBackIcon || showMenu) View.VISIBLE else View.INVISIBLE
            }
        } else {
            if (isShowBackIcon) {
                if (isCloseIcon) {
                    baseBinding.ivbackActionBar.setImageResource(R.drawable.back_black_icon)
                } else {
                    baseBinding.ivbackActionBar.setImageResource(R.drawable.back_black_icon)
                }
            } else if (showMenu) {
                baseBinding.ivbackActionBar.setImageResource(R.drawable.back_black_icon)
            } else {
                baseBinding.ivbackActionBar.visibility =
                    if (isShowBackIcon || showMenu) View.VISIBLE else View.INVISIBLE
            }
        }
    }

    fun setAppBarlightAndStatusBarDark(color: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            //make statusbar dark text and icons starting from KITKAT
            baseBinding.layoutContainerBaseActivity.systemUiVisibility =
                baseBinding.layoutContainerBaseActivity.systemUiVisibility or
                        View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.statusBarColor = ContextCompat.getColor(this, color)
            window.navigationBarColor = ContextCompat.getColor(this, R.color.black)
        }
        baseBinding.layoutContainerActionBar.setBackgroundResource(color)
        baseBinding.tvTitleCustomActionBar.setTextColor(
            ContextCompat.getColor(
                this,
                R.color.outer_space
            )
        )
        window.setBackgroundDrawableResource(R.color.black)
    }

    fun setAppBarGradient() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            //make statusbar dark text and icons starting from KITKAT
            baseBinding.layoutContainerBaseActivity.systemUiVisibility =
                baseBinding.layoutContainerBaseActivity.systemUiVisibility and
                        View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()

            window.navigationBarColor = ContextCompat.getColor(this, R.color.black)
            window.statusBarColor = ContextCompat.getColor(this, R.color.transparent)
        }
        //set gradient color
        baseBinding.layoutContainerActionBar.setBackgroundResource(R.color.colorPrimary)
        baseBinding.tvTitleCustomActionBar.setTextColor(
            ContextCompat.getColor(
                this,
                R.color.white
            )
        )
        window.setBackgroundDrawable(ContextCompat.getDrawable(this, R.color.colorPrimary))
    }


    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    public fun forceRTLIfSupported() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            window.decorView.layoutDirection = View.LAYOUT_DIRECTION_RTL
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    public fun forceLTRIfSupported() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            window.decorView.layoutDirection = View.LAYOUT_DIRECTION_LTR
        }
    }

    fun ChangeLocale(local: String) {
        val locale = Locale(local)
        Locale.setDefault(locale)
        val config = baseContext.resources.configuration
        config.setLocale(locale)
        baseContext.createConfigurationContext(config)
        baseContext.resources.updateConfiguration(
            config, baseContext.resources
                .displayMetrics
        )
    }

    open fun finish_activity() {
        finish()
        overridePendingTransition(R.anim.slide_out_left, R.anim.slide_from_left_to_right)
    }

    fun hasPermissions(context: Context?, permissions: Array<String>): Boolean {
        if (context != null && permissions != null) {
            for (p in permissions) {
                if (ActivityCompat.checkSelfPermission(
                        context,
                        p
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    return false
                }
            }
        }
        return true
    }
}