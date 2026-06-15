package prasetya.daffa.proyek_uas

import android.app.Dialog
import android.net.Uri
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ImageButton
import android.widget.MediaController
import android.widget.VideoView
import androidx.fragment.app.Fragment
import prasetya.daffa.proyek_uas.databinding.AboutFragmentBinding

class AboutFragment : Fragment(), View.OnClickListener {

    private lateinit var b: AboutFragmentBinding
    private var currentPosition = 0
    private var fullscreenDialog: Dialog? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        b = AboutFragmentBinding.inflate(inflater, container, false)

        b.btnShop.setOnClickListener(this)

        setupVideo(b.videoView)

        b.btnFullscreen.setOnClickListener {
            openFullscreen()
        }

        return b.root
    }

    private fun setupVideo(videoView: VideoView, startPosition: Int = 0) {
        val mediaController = MediaController(requireContext())
        mediaController.setAnchorView(videoView)
        videoView.setMediaController(mediaController)

        val videoUri = Uri.parse(
            "android.resource://${requireContext().packageName}/${R.raw.video_toko}"
        )
        videoView.setVideoURI(videoUri)
        videoView.requestFocus()

        videoView.setOnPreparedListener { mp ->
            mp.setVideoScalingMode(android.media.MediaPlayer.VIDEO_SCALING_MODE_SCALE_TO_FIT)
            mp.isLooping = true
            if (startPosition > 0) videoView.seekTo(startPosition)
            videoView.start()
        }
    }

    private fun openFullscreen() {
        currentPosition = b.videoView.currentPosition
        b.videoView.pause()

        fullscreenDialog = Dialog(requireContext(), android.R.style.Theme_Black_NoTitleBar_Fullscreen)

        val fullscreenView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_video_fullscreen, null)

        val fsVideoView = fullscreenView.findViewById<VideoView>(R.id.fsVideoView)
        val btnClose    = fullscreenView.findViewById<ImageButton>(R.id.btnCloseFullscreen)

        fullscreenDialog?.setContentView(fullscreenView)
        fullscreenDialog?.window?.apply {
            setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
            setLayout(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT
            )
        }

        // ✅ Tombol back HP
        fullscreenDialog?.setOnKeyListener { _, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_BACK &&
                event.action == KeyEvent.ACTION_UP) {
                closeFullscreen(fsVideoView)
                true
            } else false
        }

        // ✅ Swipe kanan untuk close
        fullscreenView.setOnTouchListener(object : View.OnTouchListener {
            private var startX = 0f

            override fun onTouch(v: View?, event: MotionEvent?): Boolean {
                when (event?.action) {
                    MotionEvent.ACTION_DOWN -> startX = event.x
                    MotionEvent.ACTION_UP -> {
                        val swipeDistance = event.x - startX
                        if (swipeDistance > 200) {
                            closeFullscreen(fsVideoView)
                        }
                    }
                }
                return false
            }
        })

        setupVideo(fsVideoView, currentPosition)

        btnClose.setOnClickListener {
            closeFullscreen(fsVideoView)
        }

        fullscreenDialog?.setOnDismissListener {
            if (b.videoView.currentPosition == 0 && currentPosition > 0) {
                b.videoView.seekTo(currentPosition)
            }
            b.videoView.start()
        }

        fullscreenDialog?.show()
    }

    // ✅ Fungsi close terpusat
    private fun closeFullscreen(fsVideoView: VideoView) {
        currentPosition = fsVideoView.currentPosition
        fsVideoView.pause()
        fullscreenDialog?.dismiss()
        fullscreenDialog = null
        b.videoView.seekTo(currentPosition)
        b.videoView.start()
    }

    override fun onPause() {
        super.onPause()
        currentPosition = b.videoView.currentPosition
        b.videoView.pause()
    }

    override fun onResume() {
        super.onResume()
        if (fullscreenDialog?.isShowing != true) {
            b.videoView.seekTo(currentPosition)
            b.videoView.start()
        }
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