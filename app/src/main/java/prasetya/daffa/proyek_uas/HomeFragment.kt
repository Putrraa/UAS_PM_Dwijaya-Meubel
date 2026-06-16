package prasetya.daffa.proyek_uas

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import prasetya.daffa.proyek_uas.databinding.HomeFragmentBinding

class HomeFragment : Fragment(), View.OnClickListener {

    private lateinit var b: HomeFragmentBinding
    private lateinit var dots: Array<ImageView>
    private val handler = Handler(Looper.getMainLooper())
    private var currentPage = 0

    private val slideImages = listOf(
        R.drawable.slide1,
        R.drawable.slide2,
        R.drawable.slide3,
        R.drawable.slide4,
        R.drawable.slide5
    )

    private val autoScrollRunnable = object : Runnable {
        override fun run() {
            val count = slideImages.size
            currentPage = (currentPage + 1) % count
            b.viewPagerBanner.setCurrentItem(currentPage, true)
            handler.postDelayed(this, 3000)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        b = HomeFragmentBinding.inflate(inflater, container, false)

        b.btnShop.setOnClickListener(this)
        setupCarousel()

        return b.root
    }

    private fun setupCarousel() {
        val adapter = SlideAdapter(slideImages)
        b.viewPagerBanner.adapter = adapter

        setupDots(slideImages.size)

        b.viewPagerBanner.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                currentPage = position
                updateDots(position)
            }
        })

        handler.postDelayed(autoScrollRunnable, 3000)
    }

    private fun setupDots(count: Int) {
        dots = Array(count) { ImageView(requireContext()) }
        val params = LinearLayout.LayoutParams(16, 16).apply {
            setMargins(6, 0, 6, 0)
        }
        dots.forEachIndexed { index, dot ->
            dot.layoutParams = params
            dot.setImageDrawable(
                ContextCompat.getDrawable(
                    requireContext(),
                    if (index == 0) R.drawable.dot_active else R.drawable.dot_inactive
                )
            )
            b.dotsIndicator.addView(dot)
        }
    }

    private fun updateDots(activeIndex: Int) {
        dots.forEachIndexed { index, dot ->
            dot.setImageDrawable(
                ContextCompat.getDrawable(
                    requireContext(),
                    if (index == activeIndex) R.drawable.dot_active else R.drawable.dot_inactive
                )
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        handler.removeCallbacks(autoScrollRunnable)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btnShop -> {
                parentFragmentManager.beginTransaction()
                    .replace(R.id.frame_container, ShopFragment())
                    .addToBackStack(null)
                    .commit()

                val mainAct = activity as? MainActivity
                mainAct?.setSelectedNav(R.id.shop)
            }
        }
    }
}