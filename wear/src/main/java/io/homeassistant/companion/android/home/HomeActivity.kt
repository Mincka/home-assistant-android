package io.homeassistant.companion.android.home

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.ButtonDefaults
import androidx.wear.compose.material.Chip
import androidx.wear.compose.material.ChipDefaults
import androidx.wear.compose.material.ScalingLazyColumn
import androidx.wear.compose.material.ScalingLazyListState
import androidx.wear.compose.material.Text
import com.mikepenz.iconics.IconicsDrawable
import com.mikepenz.iconics.compose.Image
import com.mikepenz.iconics.typeface.library.community.material.CommunityMaterial
import io.homeassistant.companion.android.DaggerPresenterComponent
import io.homeassistant.companion.android.PresenterModule
import io.homeassistant.companion.android.R
import io.homeassistant.companion.android.common.dagger.GraphComponentAccessor
import io.homeassistant.companion.android.common.data.integration.Entity
import io.homeassistant.companion.android.onboarding.OnboardingActivity
import io.homeassistant.companion.android.onboarding.integration.MobileAppIntegrationActivity
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

class HomeActivity : ComponentActivity(), HomeView {

    @Inject
    lateinit var presenter: HomePresenter

    companion object {
        private const val TAG = "HomeActivity"

        fun newInstance(context: Context): Intent {
            return Intent(context, HomeActivity::class.java)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        DaggerPresenterComponent
            .builder()
            .appComponent((application as GraphComponentAccessor).appComponent)
            .presenterModule(PresenterModule(this))
            .build()
            .inject(this)

        if (presenter.onViewReady()) {
            setContent {
                LoadHomePage()
            }
        }
    }

    override fun onDestroy() {
        presenter.onFinish()
        super.onDestroy()
    }

    override fun displayOnBoarding() {
        val intent = OnboardingActivity.newInstance(this)
        startActivity(intent)
        finish()
    }

    override fun displayMobileAppIntegration() {
        val intent = MobileAppIntegrationActivity.newInstance(this)
        startActivity(intent)
        finish()
    }

    @Composable
    private fun LoadHomePage() {

        val entities = runBlocking {
            presenter.getEntities()
        }

        val scenes = entities.sortedBy { it.entityId }.filter { it.entityId.split(".")[0] == "scene" }
        val scripts = entities.sortedBy { it.entityId }.filter { it.entityId.split(".")[0] == "script" }
        val lights = entities.sortedBy { it.entityId }.filter { it.entityId.split(".")[0] == "light" }
        val inputBooleans = entities.sortedBy { it.entityId }.filter { it.entityId.split(".")[0] == "input_boolean" }
        val switches = entities.sortedBy { it.entityId }.filter { it.entityId.split(".")[0] == "switch" }

        ScalingLazyColumn(
            modifier = Modifier
                .fillMaxSize(),
            contentPadding = PaddingValues(
                top = 10.dp,
                start = 10.dp,
                end = 10.dp,
                bottom = 40.dp
            ),
            horizontalAlignment = Alignment.CenterHorizontally,
            state = ScalingLazyListState()
        ) {
            if (inputBooleans.isNotEmpty()) {
                items(inputBooleans.size) { index ->
                    if (index == 0)
                        SetTitle(R.string.input_booleans)
                    SetEntityUI(inputBooleans[index], index)
                }
            }
            if (lights.isNotEmpty()) {
                items(lights.size) { index ->
                    if (index == 0)
                        SetTitle(R.string.lights)
                    SetEntityUI(lights[index], index)
                }
            }
            if (scenes.isNotEmpty()) {
                items(scenes.size) { index ->
                    if (index == 0)
                        SetTitle(R.string.scenes)

                    SetEntityUI(scenes[index], index)
                }
            }
            if (scripts.isNotEmpty()) {
                items(scripts.size) { index ->
                    if (index == 0)
                        SetTitle(R.string.scripts)
                    SetEntityUI(scripts[index], index)
                }
            }
            if (switches.isNotEmpty()) {
                items(switches.size) { index ->
                    if (index == 0)
                        SetTitle(R.string.switches)
                    SetEntityUI(switches[index], index)
                }
            }

            items(1) {
                Column {
                    SetTitle(R.string.other)
                    Button(
                        modifier = Modifier
                            .width(140.dp),
                        onClick = { presenter.onLogoutClicked() },
                        colors = ButtonDefaults.primaryButtonColors(backgroundColor = Color.Red)
                    ) {
                        Row {
                            Image(asset = CommunityMaterial.Icon.cmd_exit_run)
                            SetTitle(R.string.logout)
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun SetEntityUI(entity: Entity<Any>, index: Int) {
        val attributes = entity.attributes as Map<String, String>
        val iconBitmap =
            if (attributes["icon"]?.startsWith("mdi") == true) {
                val icon = attributes["icon"]!!.split(":")[1]
                IconicsDrawable(baseContext, "cmd-$icon").icon
            } else {
                when (entity.entityId.split(".")[0]) {
                    "input_boolean", "switch" -> CommunityMaterial.Icon2.cmd_light_switch
                    "light" -> CommunityMaterial.Icon2.cmd_lightbulb
                    "script" -> CommunityMaterial.Icon3.cmd_script_text_outline
                    "scene" -> CommunityMaterial.Icon3.cmd_palette_outline
                    else -> CommunityMaterial.Icon.cmd_cellphone
                }
            }

        Chip(
            modifier = Modifier
                .width(140.dp)
                .padding(top = if (index == 0) 30.dp else 10.dp),
            icon = {
                if (iconBitmap != null) {
                    Image(asset = iconBitmap)
                } else
                    Image(asset = CommunityMaterial.Icon.cmd_cellphone)
            },
            label = {
                Text(
                    text = attributes["friendly_name"].toString()
                )
            },
            onClick = { presenter.onEntityClicked(entity) },
            colors = ChipDefaults.primaryChipColors(backgroundColor = colorResource(id = R.color.colorAccent), contentColor = Color.Black)
        )
    }

    @Composable
    private fun SetTitle(id: Int) {
        Text(
            text = stringResource(id = id),
            textAlign = TextAlign.Center,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .padding(start = 15.dp)
        )
    }
}
