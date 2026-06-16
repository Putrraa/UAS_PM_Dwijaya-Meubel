package prasetya.daffa.proyek_uas

import android.Manifest
import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import prasetya.daffa.proyek_uas.databinding.ContactFragmentBinding

class ContactFragment : Fragment(), View.OnClickListener {

    private lateinit var b: ContactFragmentBinding
    private var dialogMap: MapView? = null
    private var userMarker: Marker? = null
    private val lokasiToko = GeoPoint(-7.745643610925027, 112.024023257671)
    private val locationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true

            if (granted) {
                tampilkanLokasiSaya()
            } else {
                Toast.makeText(requireContext(), "Izin lokasi dibutuhkan untuk GPS", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        b = ContactFragmentBinding.inflate(inflater, container, false)

        b.btnShop.setOnClickListener(this)
        b.btnShowMap.setOnClickListener { tampilkanPopupMap() }
        b.btnMaps.setOnClickListener { bukaGoogleMaps() }

        Configuration.getInstance().load(
            requireContext(),
            requireContext().getSharedPreferences("osmdroid", Context.MODE_PRIVATE)
        )

        return b.root
    }

    override fun onResume() {
        super.onResume()
        dialogMap?.onResume()
    }

    override fun onPause() {
        super.onPause()
        dialogMap?.onPause()
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

    private fun tampilkanPopupMap() {
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)

        val container = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(16), dp(16), dp(16))
        }

        val title = TextView(requireContext()).apply {
            text = "Lokasi Dwijaya Meubel"
            textSize = 18f
            setTextColor(0xFF222222.toInt())
            setPadding(0, 0, 0, dp(12))
        }

        val map = MapView(requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(360)
            )
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)
            controller.setZoom(17.0)
            controller.setCenter(lokasiToko)
        }

        val tokoMarker = Marker(map).apply {
            position = lokasiToko
            setTitle("Dwijaya Meubel")
            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        }
        map.overlays.add(tokoMarker)

        val buttonRow = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(0, dp(12), 0, 0)
        }

        val btnDwijaya = Button(requireContext()).apply {
            text = "Dwijaya"
            isAllCaps = false
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            setOnClickListener {
                map.controller.animateTo(lokasiToko)
                map.controller.setZoom(17.0)
            }
        }

        val btnGps = Button(requireContext()).apply {
            text = "GPS Saya"
            isAllCaps = false
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply {
                marginStart = dp(8)
            }
            setOnClickListener { tampilkanLokasiSaya() }
        }

        val btnTutup = Button(requireContext()).apply {
            text = "Tutup"
            isAllCaps = false
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply {
                marginStart = dp(8)
            }
            setOnClickListener { dialog.dismiss() }
        }

        buttonRow.addView(btnDwijaya)
        buttonRow.addView(btnGps)
        buttonRow.addView(btnTutup)
        container.addView(title)
        container.addView(map)
        container.addView(buttonRow)

        dialog.setContentView(container)
        dialog.setOnShowListener {
            dialogMap = map
            map.onResume()
        }
        dialog.setOnDismissListener {
            map.onPause()
            dialogMap = null
            userMarker = null
        }
        dialog.show()
        dialog.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.92f).toInt(),
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
    }

    private fun bukaGoogleMaps() {
        val intent = Intent(
            Intent.ACTION_VIEW,
            "https://maps.app.goo.gl/4oz86WC9s4QgjCJy9?g_st=aw".toUri()
        )
        startActivity(intent)
    }

    private fun punyaIzinLokasi(): Boolean {
        val context = requireContext()
        val fineGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val coarseGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        return fineGranted || coarseGranted
    }

    @SuppressLint("MissingPermission")
    private fun tampilkanLokasiSaya() {
        if (!punyaIzinLokasi()) {
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
            return
        }

        val map = dialogMap ?: return
        val locationManager = requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val providers = listOf(LocationManager.GPS_PROVIDER, LocationManager.NETWORK_PROVIDER)
            .filter { provider -> locationManager.isProviderEnabled(provider) }

        if (providers.isEmpty()) {
            Toast.makeText(requireContext(), "GPS belum aktif", Toast.LENGTH_SHORT).show()
            return
        }

        val lastLocation = providers
            .mapNotNull { provider -> locationManager.getLastKnownLocation(provider) }
            .maxByOrNull { location -> location.time }

        if (lastLocation != null) {
            tampilkanMarkerUser(map, lastLocation)
            return
        }

        Toast.makeText(requireContext(), "Mencari lokasi...", Toast.LENGTH_SHORT).show()
        val listener = object : LocationListener {
            override fun onLocationChanged(location: Location) {
                tampilkanMarkerUser(map, location)
                locationManager.removeUpdates(this)
            }

            override fun onProviderEnabled(provider: String) = Unit
            override fun onProviderDisabled(provider: String) = Unit

            @Deprecated("Deprecated in Java")
            override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) = Unit
        }

        locationManager.requestSingleUpdate(providers.first(), listener, Looper.getMainLooper())
    }

    private fun tampilkanMarkerUser(map: MapView, location: Location) {
        val posisiUser = GeoPoint(location.latitude, location.longitude)
        userMarker?.let { map.overlays.remove(it) }

        userMarker = Marker(map).apply {
            position = posisiUser
            setTitle("Lokasi Saya")
            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        }

        map.overlays.add(userMarker)
        map.controller.animateTo(posisiUser)
        map.invalidate()
    }

    private fun dp(value: Int): Int {
        return (value * resources.displayMetrics.density).toInt()
    }
}
