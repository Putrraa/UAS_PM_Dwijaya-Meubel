package prasetya.daffa.proyek_uas

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import prasetya.daffa.proyek_uas.databinding.AboutFragmentBinding

class AboutFragment : Fragment(), View.OnClickListener {

    private lateinit var b: AboutFragmentBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        b = AboutFragmentBinding.inflate(inflater, container, false)

        b.btnShop.setOnClickListener(this)

        Configuration.getInstance().load(
            requireContext(),
            requireContext().getSharedPreferences(
                "osmdroid",
                Context.MODE_PRIVATE
            )
        )

        val map: MapView = b.map

        map.setTileSource(TileSourceFactory.MAPNIK)
        map.setMultiTouchControls(true)

        // GANTI DENGAN KOORDINAT TOKO KAMU
        val lokasiToko = GeoPoint(
            -7.983908,
            112.621391
        )

        map.controller.setZoom(17.0)
        map.controller.setCenter(lokasiToko)

        val marker = Marker(map)
        marker.position = lokasiToko
        marker.title = "Dwijaya Meubel"
        marker.setAnchor(
            Marker.ANCHOR_CENTER,
            Marker.ANCHOR_BOTTOM
        )

        map.overlays.add(marker)

        return b.root
    }

    override fun onResume() {
        super.onResume()
        b.map.onResume()
    }

    override fun onPause() {
        super.onPause()
        b.map.onPause()
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