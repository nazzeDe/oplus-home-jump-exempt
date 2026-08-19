package com.nazze.oplusjumpallowlist.ui

import android.app.Activity
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.ListView
import android.widget.ProgressBar
import android.widget.Switch
import android.widget.TextView
import com.nazze.oplusjumpallowlist.R
import com.nazze.oplusjumpallowlist.config.AllowlistStore
import java.util.concurrent.Executors

/**
 * Searchable multi-select caller allowlist. Module enablement stays in LSPosed.
 */
class SettingsActivity : Activity() {
    private val mainHandler = Handler(Looper.getMainLooper())
    private val loader = Executors.newSingleThreadExecutor()

    private lateinit var store: AllowlistStore
    private lateinit var adapter: AppListAdapter
    private lateinit var searchInput: EditText
    private lateinit var showSystemSwitch: Switch
    private lateinit var loading: ProgressBar
    private lateinit var emptyView: TextView

    private var allApps: List<AppListItem> = emptyList()
    private var selectedPackages: MutableSet<String> = linkedSetOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // targetSdk 35 enables edge-to-edge by default; keep content below ActionBar/status bar
        // so the activity title does not cover the search field.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(true)
        }
        setContentView(R.layout.activity_settings)

        store = AllowlistStore(this)
        selectedPackages = store.getPackages().toMutableSet()

        searchInput = findViewById(R.id.search_input)
        showSystemSwitch = findViewById(R.id.show_system_apps)
        loading = findViewById(R.id.loading)
        emptyView = findViewById(R.id.empty_view)
        val appList = findViewById<ListView>(R.id.app_list)

        adapter = AppListAdapter { packageName, checked ->
            if (checked) {
                selectedPackages.add(packageName)
            } else {
                selectedPackages.remove(packageName)
            }
            store.setPackages(selectedPackages)
        }
        appList.adapter = adapter
        appList.emptyView = emptyView

        showSystemSwitch.isChecked = store.getShowSystemApps()
        showSystemSwitch.setOnCheckedChangeListener { _, isChecked ->
            store.setShowSystemApps(isChecked)
            applyFilter()
        }

        searchInput.addTextChangedListener(
            object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit
                override fun afterTextChanged(s: Editable?) = applyFilter()
            },
        )

        loadInstalledApps()
    }

    override fun onDestroy() {
        loader.shutdownNow()
        super.onDestroy()
    }

    private fun loadInstalledApps() {
        loading.visibility = View.VISIBLE
        loader.execute {
            val pm = packageManager
            val self = packageName
            val loaded = pm.getInstalledApplications(PackageManager.GET_META_DATA)
                .asSequence()
                .filter { it.packageName != self }
                .map { info ->
                    AppListItem(
                        packageName = info.packageName,
                        label = info.loadLabel(pm).toString(),
                        icon = info.loadIcon(pm),
                        isSystem = (info.flags and ApplicationInfo.FLAG_SYSTEM) != 0,
                    )
                }
                .sortedWith(
                    compareBy<AppListItem> { it.label.lowercase() }
                        .thenBy { it.packageName },
                )
                .toList()
            mainHandler.post {
                if (isFinishing || isDestroyed) return@post
                allApps = loaded
                loading.visibility = View.GONE
                applyFilter()
            }
        }
    }

    private fun applyFilter() {
        val query = searchInput.text?.toString()?.trim().orEmpty()
        val showSystem = showSystemSwitch.isChecked
        val filtered = allApps.asSequence()
            .filter { showSystem || !it.isSystem }
            .filter { item ->
                query.isEmpty() ||
                    item.label.contains(query, ignoreCase = true) ||
                    item.packageName.contains(query, ignoreCase = true)
            }
            .toList()
        adapter.submit(filtered, selectedPackages)
        emptyView.setText(
            if (allApps.isEmpty()) R.string.app_list_loading_empty else R.string.app_list_filter_empty,
        )
    }
}
